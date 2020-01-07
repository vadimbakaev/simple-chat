package vbakaev.app.chat

import java.util.UUID

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Source}
import akka.util.ByteString

trait ChatConnectionHandlerProvider {

  def connectionHandler(): Flow[ByteString, ByteString, NotUsed]

}

class ChatConnectionHandlerProviderImpl()(implicit mat: Materializer) extends ChatConnectionHandlerProvider {
  private val (sink, source) =
    MergeHub.source[(ByteString, UUID)].toMat(BroadcastHub.sink)(Keep.both).run()

  override def connectionHandler(): Flow[ByteString, ByteString, NotUsed] = {
    val userId = UUID.randomUUID()

    Flow[ByteString]
      .zip(Source.repeat(userId))
      .via(Flow.fromSinkAndSource(sink, source))
      .collect {
        case (msg, senderId) if senderId != userId => msg
      }
  }
}
