package kvstore

import akka.actor.{ OneForOneStrategy, Props, ActorRef, Actor }
import kvstore.Arbiter._
import scala.collection.immutable.Queue
import akka.actor.SupervisorStrategy.Restart
import scala.annotation.tailrec
import akka.pattern.{ ask, pipe }
import akka.actor.Terminated
import scala.concurrent.duration._
import akka.actor.PoisonPill
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy
import akka.util.Timeout
import akka.actor.Cancellable
import scala.language.postfixOps
import akka.event.LoggingReceive
import akka.actor.ActorLogging

object Replica {
  sealed trait Operation {
    def key: String
    def id: Long
  }
  case class Insert(key: String, value: String, id: Long) extends Operation
  case class Remove(key: String, id: Long) extends Operation
  case class Get(key: String, id: Long) extends Operation

  sealed trait OperationReply
  case class OperationAck(id: Long) extends OperationReply
  case class OperationFailed(id: Long) extends OperationReply
  case class GetResult(key: String, valueOption: Option[String], id: Long) extends OperationReply

  def props(arbiter: ActorRef, persistenceProps: Props): Props = Props(new Replica(arbiter, persistenceProps))
}

class Replica(val arbiter: ActorRef, persistenceProps: Props) extends Actor with ActorLogging {
  import Replica._
  import Replicator._
  import Persistence._
  import context.dispatcher

  /*
   * The contents of this actor is just a suggestion, you can implement it in any way you like.
   */

  var kv = Map.empty[String, String]
  // a map from secondary replicas to replicators
  var numberOfNodes = 0
  // the current set of replicators
  var replicators = Set.empty[ActorRef]

  val persistor = context.actorOf(persistenceProps)
  var acks = Map.empty[(String, Long), ActorRef]
  var replicateAcks = Map.empty[(String, Long), Int]
  var persisting = Map.empty[(String, Long), Cancellable]
  var expectedId = 0

  def receive = {
    case JoinedPrimary => context.become(leader)
    case JoinedSecondary => context.become(replica)
  }

  private def waitForAcknowledgement(key: String, id: Long) = {
    context.system.scheduler.scheduleOnce(1 second) {
      if (persisting.isDefinedAt(key, id)) {
        persisting(key, id).cancel

        log.info("Haven't received Persisted after 1 second.")
        acks(key, id) ! OperationFailed(id)
        persisting -= ((key, id))
      }

      if (replicateAcks(key, id) < numberOfNodes - 1) { // - 1 for primary
        log.info("Data has not been replicated to all secondaries after 1 second.")
        acks(key, id) ! OperationFailed(id)
      } else {
        log.info(s"OperationAck($id)")
        acks(key, id) ! OperationAck(id)
      }

      replicateAcks -= ((key, id))
      acks -= ((key, id))
    }
  }

  /* TODO Behavior for  the leader role. */
  val leader: Receive = LoggingReceive {
    case Get(key, id) => {
      if (kv isDefinedAt key) sender ! GetResult(key, Some(kv(key)), id)
      else sender ! GetResult(key, None, id)
    }

    case Insert(key, value, id) => {
      log.info(s"Inserting ${key},${value}")
      kv += key -> value
      persisting += (key, id) -> context.system.scheduler.schedule(0 millis, 100 millis) {
        log.info("persistor ! Persist(key, Option(value), id)")
        persistor ! Persist(key, Option(value), id)
      }

      replicators foreach { _ ! Replicate(key, Option(value), id) }

      replicateAcks += (key, id) -> 0
      acks += (key, id) -> sender
      waitForAcknowledgement(key, id)
    }

    case Replicated(key, id) if replicateAcks.isDefinedAt(key, id) => {
      log.info(s"Replicated ${key},${id}")
      replicateAcks += (key, id) -> (replicateAcks(key, id) + 1)
    }

    case Remove(key, id) => {
      log.info(s"[Leader] Removing ${key} (${id})")
      persisting += (key, id) -> context.system.scheduler.schedule(0 millis, 100 millis) {
        log.info("persistor ! Persist(key, Option(value), id)")
        persistor ! Persist(key, None, id)
      }

      replicators foreach { _ ! Replicate(key, None, id) }
      replicateAcks += (key, id) -> 0
      acks += (key, id) -> sender
      waitForAcknowledgement(key, id);
    }

    case Snapshot(key, valueOption, id) if id == expectedId => {
      log.info(s"[Leader] Got good Snapshot($key, $valueOption, $id)")
      expectedId += 1
      valueOption match {
        case Some(value) => {
          kv += key -> value
        }

        case None =>
          {
            kv -= key
          }
      }

      persisting += (key, id) -> context.system.scheduler.schedule(0 millis, 100 millis) {
        persistor ! Persist(key, valueOption, id)
      }
    }

    case Snapshot(key, valueOption, id) if id < expectedId =>
      log.info(s"[Replica] Got < Snapshot($key, $valueOption, $id"); sender ! SnapshotAck(key, id)

    case Snapshot(key, valueOption, id) =>
      log.info(s"[Replica] Got FUCK Snapshot($key, $valueOption, $id");

    case Persisted(key, id) => {
      if (persisting.isDefinedAt(key, id)) {
        persisting(key, id).cancel
        persisting -= ((key, id))
      }

      if (acks.isDefinedAt(key, id)) {
        log.info("Sending SnapshotAck")
        acks(key, id) ! SnapshotAck(key, id)
        acks -= ((key, id))
      }

    }

    case Replicas(newReplicas) => {
      log.info(s"received ${newReplicas.size} new replicas.")
      replicators = Set.empty[ActorRef]
      newReplicas foreach { r =>
        val replicator = context.actorOf(Replicator.props(r))
        replicators += replicator
        var i = 0
        kv.foreach { pair =>
          replicator ! Replicate(pair._1, Option(pair._2), i)
          i += 1
        }
      }
      numberOfNodes = newReplicas.size

    }
  }

  /* TODO Behavior for the replica role. */
  val replica: Receive = LoggingReceive {
    case Get(key, id) => {
      if (kv isDefinedAt key) sender ! GetResult(key, Some(kv(key)), id)
      else sender ! GetResult(key, None, id)
    }

    case Snapshot(key, valueOption, id) if id == expectedId => {
      log.info(s"[Replica] Got Snapshot($key, $valueOption, $id")
      acks += (key, id) -> sender
      expectedId += 1
      valueOption match {
        case Some(value) => {
          kv += key -> value
        }

        case None =>
          {
            kv -= key
          }
      }

      persisting += (key, id) -> context.system.scheduler.schedule(0 millis, 100 millis) {
        persistor ! Persist(key, valueOption, id)
      }
    }

    case Persisted(key, id) => {
      persisting(key, id).cancel
      log.info("Sending SnapshotAck")
      acks(key, id) ! SnapshotAck(key, id)
      acks -= ((key, id))
      persisting -= ((key, id))
    }

    case Remove(key, id) => {
      log.info(s"[Replica] Removing ${key} (${id})")
      persisting += (key, id) -> context.system.scheduler.schedule(0 millis, 100 millis) {
        log.info("persistor ! Persist(key, Option(value), id)")
        persistor ! Persist(key, None, id)
      }

      replicators foreach { _ ! Replicate(key, None, id) }
      replicateAcks += (key, id) -> 0
      acks += (key, id) -> sender
      waitForAcknowledgement(key, id);
    }

    case Snapshot(key, valueOption, id) if id < expectedId =>
      log.info(s"[Replica] Got < Snapshot($key, $valueOption, $id"); sender ! SnapshotAck(key, id)

    case Snapshot(key, valueOption, id) => log.info(s"[Replica] Got fallback Snapshot($key, $valueOption, $id")
  }

  arbiter ! Join

}
