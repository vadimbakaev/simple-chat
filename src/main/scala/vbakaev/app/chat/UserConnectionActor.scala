package vbakaev.app.chat

import java.util.UUID

import akka.actor.Status.{Failure, Success}
import akka.actor.{Actor, ActorLogging, ActorRef, Stash, Terminated}
import akka.util.ByteString
import vbakaev.app.chat.UserConnectionActor._

object UserConnectionActor {
  private val Prefix: String = "userConnectionActor-"
  def name(): String         = Prefix + UUID.randomUUID()

  sealed trait Protocol
  case object StreamInit                                      extends Protocol
  case object StreamAck                                       extends Protocol
  case object StreamComplete                                  extends Protocol
  final case class StreamFailure(t: Throwable)                extends Protocol
  final case class ConnectUser(userConnectionActor: ActorRef) extends Protocol
  private final case class Message(msg: ByteString)           extends Protocol
}

class UserConnectionActor extends Actor with Stash with ActorLogging {

  override def receive: Receive = unConfigured

  val unConfigured: Receive = {
    case ConnectUser(userConnectionActor) =>
      log.debug("User connected {}", userConnectionActor)
      unstashAll()
      context watch userConnectionActor
      context become connected(userConnectionActor)
    case _ =>
      stash()
  }

  def connected(userConnectionActor: ActorRef): Receive = {
    case StreamInit =>
      log.debug("Stream Init")
      sender() ! StreamAck
    case StreamComplete =>
      log.debug("Stream complete")
      userConnectionActor ! Success("complete")
      context stop self
    case StreamFailure(t) =>
      log.warning("Stream failure", t)
      userConnectionActor ! Failure(t)
      context stop self
    case incomingMessage: ByteString =>
      context.actorSelection(s"../$Prefix*") ! Message(incomingMessage)
      sender() ! StreamAck
    case Message(message) =>
      log.debug("Received message {} from {}", message.utf8String, sender())
      if (sender() != self) userConnectionActor ! message
    case Terminated(actor) if actor == userConnectionActor =>
      context stop self
  }

  override def unhandled(message: Any): Unit = {
    log.error("Unexpected message received: " + message)
  }

  override def postStop(): Unit = {
    log.info("User connection actor dead {}", self)
  }
}
