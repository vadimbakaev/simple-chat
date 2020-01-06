package vbakaev.app.chat

import akka.stream.scaladsl.{Sink, Source}
import akka.stream.testkit.TestSubscriber
import akka.stream.testkit.scaladsl.TestSink
import akka.util.ByteString
import vbakaev.app.BaseSpec
import vbakaev.app.chat.ChatConnectionHandlerProviderImplSpec._

class ChatConnectionHandlerProviderImplSpec extends BaseSpec {

  trait Fixture {
    val subject = new ChatConnectionHandlerProviderImpl()
  }

  "A provider" when {

    "returns connectionHandler flow" should {

      "correctly handle empty source" in new Fixture {
        val matTestValue: TestSubscriber.Probe[ByteString] =
          Source.empty.via(subject.connectionHandler()).runWith(TestSink.probe[ByteString])

        matTestValue
          .request(5)
          .expectNoMessage()
      }

      "ignore incoming messages for the same flow" in new Fixture {
        val matTestValue: TestSubscriber.Probe[ByteString] =
          Source(Messages).via(subject.connectionHandler()).runWith(TestSink.probe[ByteString])

        matTestValue
          .request(5)
          .expectNoMessage()
      }

      "broadcast all messages to parents flow" in new Fixture {
        val matTestValue1: TestSubscriber.Probe[ByteString] =
          Source.empty[ByteString].via(subject.connectionHandler()).runWith(TestSink.probe[ByteString])

        Source(Messages).via(subject.connectionHandler()).runWith(Sink.seq)

        matTestValue1
          .request(5)
          .expectNextN(Messages)
          .expectNoMessage()
      }

    }

  }

}

object ChatConnectionHandlerProviderImplSpec {
  val Messages: List[ByteString] = List("1", "2", "3").map(ByteString(_))
}
