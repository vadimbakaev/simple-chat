package vbakaev.app

import akka.actor.Status.{Status => _, _}
import akka.actor._
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl._
import akka.stream.{ClosedShape, CompletionStrategy, OverflowStrategy}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import vbakaev.app.chat.UserConnectionActor
import vbakaev.app.config.AppConfig

import scala.language.postfixOps
import pureconfig._
import pureconfig.generic.auto._
import vbakaev.app.chat.UserConnectionActor._

object Main extends App with LazyLogging {
  logger.info("Application started")

  implicit val actorSystem: ActorSystem = ActorSystem("simple-chat")

  ConfigSource.default
    .load[AppConfig]
    .fold(
      error =>
        Source
          .single(s"Configuration loading error $error")
          .to(Sink.foreach(msg => logger.error(msg))),
      config => {
        RunnableGraph
          .fromGraph(GraphDSL.create() {
            implicit builder =>
              val tcpConnection = Tcp().bind(config.tcp.interface, config.tcp.port)
              val tcpConnectionHandler = Sink.foreach[Tcp.IncomingConnection] {
                incomingConnection =>
                  val userActor = actorSystem.actorOf(Props[UserConnectionActor], name())

                  val chatMessageConsumer = Sink.actorRefWithBackpressure[ByteString](
                    ref = userActor,
                    onInitMessage = StreamInit,
                    ackMessage = StreamAck,
                    onCompleteMessage = StreamComplete,
                    onFailureMessage = StreamFailure
                  )

                  val chatMessageProducer = Source.actorRef[ByteString](
                    completionMatcher = {
                      case Success(s: CompletionStrategy) => s
                      case Success(_)                     => CompletionStrategy.draining
                      case Success                        => CompletionStrategy.draining
                    }: PartialFunction[Any, CompletionStrategy],
                    failureMatcher = {
                      case Failure(cause) => cause
                    }: PartialFunction[Any, Throwable],
                    bufferSize = 16,
                    overflowStrategy = OverflowStrategy.dropHead
                  )

                  val chatConnectionHandler = Flow.fromSinkAndSourceMat(
                    chatMessageConsumer,
                    chatMessageProducer
                  )(Keep.right)

                  val connectionOutcomeActor = incomingConnection.handleWith(chatConnectionHandler)

                  userActor ! ConnectUser(connectionOutcomeActor)
              }

              tcpConnection ~> tcpConnectionHandler

              ClosedShape
          })
      }
    )
    .run()

}
