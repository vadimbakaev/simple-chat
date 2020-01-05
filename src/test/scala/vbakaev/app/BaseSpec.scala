package vbakaev.app

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, ParallelTestExecution}

abstract class BaseSpec
    extends TestKit(ActorSystem("BaseSpec"))
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with ParallelTestExecution {

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

}
