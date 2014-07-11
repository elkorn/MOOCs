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

class Replica(val arbiter: ActorRef, persistenceProps: Props) extends Actor {
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

  var expectedId = 0

  def receive = {
    case JoinedPrimary => context.become(leader)
    case JoinedSecondary => context.become(replica)
  }

  /* TODO Behavior for  the leader role. */
  val leader: Receive = {
    case Get(key, id) => {
      if (kv isDefinedAt key) sender ! GetResult(key, Some(kv(key)), id)
      else sender ! GetResult(key, None, id)
    }

    case Insert(key, value, id) => {
      kv += key -> value
      sender ! OperationAck(id)
    }

    case Remove(key, id) => {
      if (kv isDefinedAt key) kv -= key
      sender ! OperationAck(id)
    }
  }

  /* TODO Behavior for the replica role. */
  val replica: Receive = {
    case Get(key, id) => {
      if (kv isDefinedAt key) sender ! GetResult(key, Some(kv(key)), id)
      else sender ! GetResult(key, None, id)
    }

    case Snapshot(key, valueOption, id) if id == expectedId => {
      valueOption match {
        case Some(value) => {
          kv += key -> value
          sender ! SnapshotAck(key, id)
        }

        case None =>
          {
            kv -= key
            sender ! SnapshotAck(key, id)
          }
      }

      expectedId += 1
    }

    case Snapshot(key, _, id) if id < expectedId => sender ! SnapshotAck(key, id)
  }

  arbiter ! Join

}