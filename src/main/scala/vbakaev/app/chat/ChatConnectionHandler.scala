package vbakaev.app.chat

import java.util.UUID

import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Tcp}
import akka.util.ByteString
import vbakaev.app.chat.ChatConnectionHandlerImpl.MessageWrapper

trait ChatConnectionHandler {

  def handleConnection(incomingConnection: Tcp.IncomingConnection): Unit

}

class ChatConnectionHandlerImpl()(implicit mat: Materializer) extends ChatConnectionHandler {
  private val (sink, source) = MergeHub.source[MessageWrapper].toMat(BroadcastHub.sink[MessageWrapper])(Keep.both).run()

  override def handleConnection(incomingConnection: Tcp.IncomingConnection): Unit = {
    val userId = UUID.randomUUID()

    incomingConnection.handleWith(
      Flow
        .fromFunction(MessageWrapper(userId, _))
        .via(Flow.fromSinkAndSource(sink, source))
        .collect {
          case MessageWrapper(senderId, msg) if senderId != userId => msg
        }
    )

    ()
  }
}

object ChatConnectionHandlerImpl {
  private final case class MessageWrapper(uuid: UUID, msg: ByteString)
}
