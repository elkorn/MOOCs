package kvstore

import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef
import scala.concurrent.duration._
import akka.actor.Cancellable
import akka.actor.ActorLogging
import scala.language.postfixOps
import akka.event.LoggingReceive

object Replicator {
  case class Replicate(key: String, valueOption: Option[String], id: Long)
  case class Replicated(key: String, id: Long)

  case class Snapshot(key: String, valueOption: Option[String], seq: Long)
  case class SnapshotAck(key: String, seq: Long)

  def props(replica: ActorRef): Props = Props(new Replicator(replica))
}
class Replicator(val replica: ActorRef) extends Actor  with ActorLogging {
  import Replicator._
  import Replica._
  import context.dispatcher

  /*
   * The contents of this actor is just a suggestion, you can implement it in any way you like.
   */

  // map from sequence number to pair of sender and request
  var acks = Map.empty[Long, (ActorRef, Replicate)]
  
  var cancellables = Map.empty[(String, Long), Cancellable]
  // a sequence of not-yet-sent snapshots (you can disregard this if not implementing batching)
  var pending = Vector.empty[Snapshot]

  var _seqCounter = 0L
  def nextSeq = {
    val ret = _seqCounter
    _seqCounter += 1
    ret
  }

  /* TODO Behavior for the Replicator. */
  def receive: Receive = LoggingReceive {
    case Replicate(key, valueOption, seq) => {
      log.info(s"Got Replicate($key, $valueOption, $seq) to $replica")
      acks += (seq) -> (sender, Replicate(key, valueOption, seq))
      log.info(s"[Replicator] Replicating ${key},${valueOption},${seq}")
      cancellables += (key, seq) -> context.system.scheduler.schedule(0 milliseconds, 100 milliseconds) {
        log.info(s"[Replicator] Sending Snapshot(${key}, ${valueOption}, ${seq}) to $replica")
        replica ! Snapshot(key, valueOption, seq)
      }
    }
    
    case SnapshotAck(key, seq) => {
      log.info(s"[Replicator] Got SnapshotAck(${key}, ${seq}) from $sender")
      cancellables(key, seq).cancel
      cancellables -= ((key, seq))
      acks(seq)._1 ! Replicated(acks(seq)._2.key, acks(seq)._2.id)
    }
    
  }

}
