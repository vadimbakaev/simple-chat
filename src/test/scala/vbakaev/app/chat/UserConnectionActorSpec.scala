package vbakaev.app.chat

import akka.actor.Status.{Failure, Success}
import akka.actor.{ActorRef, PoisonPill, Props}
import akka.testkit.TestProbe
import akka.util.ByteString
import vbakaev.app.BaseSpec
import vbakaev.app.chat.UserConnectionActor.{ConnectUser, StreamAck, StreamComplete, StreamFailure, StreamInit}
import vbakaev.app.chat.UserConnectionActorSpec._

class UserConnectionActorSpec extends BaseSpec {

  trait Fixtures {
    val configuredUserActor: ActorRef      = system.actorOf(Props[UserConnectionActor], "userConnectionActor-0")
    val unConfiguredUserActor: ActorRef    = system.actorOf(Props[UserConnectionActor], "userConnectionActor-1")
    val connectionOutcomeProbe1: TestProbe = TestProbe()
    val connectionOutcomeProbe2: TestProbe = TestProbe()
    val senderProbe: TestProbe             = TestProbe()

    configuredUserActor ! ConnectUser(connectionOutcomeProbe1.ref)
  }

  "An user connection actor" when {

    "in waitingForConnection state" should {

      "un stash messages once user connected" in new Fixtures {
        unConfiguredUserActor.tell(StreamInit, senderProbe.ref)

        senderProbe.expectNoMessage()

        unConfiguredUserActor ! ConnectUser(connectionOutcomeProbe1.ref)

        senderProbe.expectMsg(StreamAck)
      }
    }

    "in connected state" should {

      "die after connection outcome actor" in new Fixtures {
        watch(configuredUserActor)

        connectionOutcomeProbe1.ref ! PoisonPill

        expectTerminated(configuredUserActor)
      }

      "answer with Ack once stream initialized" in new Fixtures {
        configuredUserActor.tell(StreamInit, senderProbe.ref)

        senderProbe.expectMsg(StreamAck)
      }

      "die when stream is completed" in new Fixtures {
        watch(configuredUserActor)

        configuredUserActor ! StreamComplete

        expectTerminated(configuredUserActor)
      }

      "notify connection outcome actor stream is completed" in new Fixtures {
        configuredUserActor ! StreamComplete

        connectionOutcomeProbe1.expectMsg(Success("complete"))
      }

      "die when stream is failed" in new Fixtures {
        watch(configuredUserActor)

        configuredUserActor ! StreamFailure(SomeException)

        expectTerminated(configuredUserActor)
      }

      "notify connection outcome actor when stream is failed" in new Fixtures {
        configuredUserActor ! StreamFailure(SomeException)

        connectionOutcomeProbe1.expectMsg(Failure(SomeException))
      }

      "broadcast incoming message to all brothers" in new Fixtures {
        unConfiguredUserActor ! ConnectUser(connectionOutcomeProbe2.ref)

        unConfiguredUserActor ! Message

        connectionOutcomeProbe1.expectMsg(Message)
      }

      "do nothing when receive message from itself" in new Fixtures {
        unConfiguredUserActor ! ConnectUser(connectionOutcomeProbe2.ref)

        unConfiguredUserActor ! Message

        connectionOutcomeProbe2.expectNoMessage()
      }

    }

  }

}

object UserConnectionActorSpec {
  val SomeException: Throwable = new IllegalArgumentException("Marker")
  val Message: ByteString      = ByteString("MessageMarker")
}
