package vbakaev.app

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, ParallelTestExecution}

import scala.concurrent.duration._
import scala.language.postfixOps

abstract class BaseSpec
    extends TestKit(ActorSystem("BaseSpec"))
    with AnyWordSpecLike
    with Matchers
    with ScalaFutures
    with BeforeAndAfterAll
    with ParallelTestExecution {

  implicit val config: PatienceConfig = PatienceConfig(3 seconds, 30 milliseconds)

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

}
