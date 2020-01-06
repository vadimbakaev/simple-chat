package vbakaev.app.chat

import java.util.UUID

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub}
import akka.util.ByteString
import vbakaev.app.chat.ChatConnectionHandlerProviderImpl.MessageWrapper

trait ChatConnectionHandlerProvider {

  def connectionHandler(): Flow[ByteString, ByteString, NotUsed]

}

class ChatConnectionHandlerProviderImpl()(implicit mat: Materializer) extends ChatConnectionHandlerProvider {
  private val (sink, source) = MergeHub.source[MessageWrapper].toMat(BroadcastHub.sink[MessageWrapper])(Keep.both).run()

  override def connectionHandler(): Flow[ByteString, ByteString, NotUsed] = {
    val userId = UUID.randomUUID()

    Flow
      .fromFunction(MessageWrapper(userId, _))
      .via(Flow.fromSinkAndSource(sink, source))
      .collect {
        case MessageWrapper(senderId, msg) if senderId != userId => msg
      }
  }
}

object ChatConnectionHandlerProviderImpl {
  private final case class MessageWrapper(uuid: UUID, msg: ByteString)
}
