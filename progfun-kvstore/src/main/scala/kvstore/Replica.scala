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
  var secondaries = Map.empty[ActorRef, ActorRef]
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

  /* TODO Behavior for  the leader role. */
  val leader: Receive = LoggingReceive {
    case Get(key, id) => {
      if (kv isDefinedAt key) sender ! GetResult(key, Some(kv(key)), id)
      else sender ! GetResult(key, None, id)
    }

    case Insert(key, value, id) => {
      kv += key -> value
      persisting += (key, id) -> context.system.scheduler.schedule(0 millis, 100 millis) {
        persistor ! Persist(key, Option(value), id)
      }

      replicators foreach { _ ! Replicate(key, Option(value), id) }

      replicateAcks += (key, id) -> 0

      context.system.scheduler.scheduleOnce(1 second) {
        if (persisting.isDefinedAt(key, id)) {
          persisting(key, id).cancel

          acks(key, id) ! OperationFailed(id)
          persisting -= ((key, id))
        }

        if (replicateAcks(key, id) < secondaries.size) {
          acks(key, id) ! OperationFailed(id)
        } else {
          acks(key, id) ! OperationAck(id)
        }

        replicateAcks -= ((key, id))
        acks -= ((key, id))
      }

      acks += (key, id) -> sender
    }

    case Replicated(key, id) if replicateAcks.isDefinedAt(key, id) => {
      replicateAcks += (key, id) -> (replicateAcks(key, id) + 1)
    }

    case Remove(key, id) => {
      if (kv isDefinedAt key) kv -= key
      sender ! OperationAck(id)
    }

    case Persisted(key, id) => {
      persisting(key, id).cancel
      persisting -= ((key, id))
    }

    case Replicas(newReplicas) => {
      newReplicas foreach { r =>
        secondaries += r -> context.actorOf(Replicator.props(r))
        replicators += secondaries(r)
      }
    }
  }

  /* TODO Behavior for the replica role. */
  val replica: Receive = {
    case Get(key, id) => {
      if (kv isDefinedAt key) sender ! GetResult(key, Some(kv(key)), id)
      else sender ! GetResult(key, None, id)
    }

    case Snapshot(key, valueOption, id) if id == expectedId => {
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
      acks(key, id) ! SnapshotAck(key, id)
      acks -= ((key, id))
      persisting -= ((key, id))
    }

    case Snapshot(key, _, id) if id < expectedId => sender ! SnapshotAck(key, id)
  }

  arbiter ! Join

}
