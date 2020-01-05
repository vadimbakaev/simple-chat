package vbakaev.app

import akka.actor.Status.{Status => _}
import akka.actor._
import akka.stream.ClosedShape
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl._
import com.typesafe.scalalogging.LazyLogging
import pureconfig._
import pureconfig.generic.auto._
import vbakaev.app.chat.{ChatConnectionHandlerProvider, ChatConnectionHandlerProviderImpl}
import vbakaev.app.config.AppConfig

object Main extends App with LazyLogging {
  logger.info("Application started")

  implicit val actorSystem: ActorSystem       = ActorSystem("simple-chat")
  val provider: ChatConnectionHandlerProvider = new ChatConnectionHandlerProviderImpl()

  ConfigSource.default
    .load[AppConfig]
    .fold(
      error =>
        Source
          .single(s"Configuration loading error $error")
          .to(Sink.foreach(msg => logger.error(msg))),
      config => {
        RunnableGraph
          .fromGraph(GraphDSL.create() { implicit builder =>
            val tcpConnection = Tcp().bind(config.tcp.interface, config.tcp.port)
            val tcpConnectionHandler = Sink.foreach[Tcp.IncomingConnection] { connection =>
              connection.handleWith(provider.connectionHandler())
              ()
            }

            tcpConnection ~> tcpConnectionHandler

            ClosedShape
          })
      }
    )
    .run()

}
