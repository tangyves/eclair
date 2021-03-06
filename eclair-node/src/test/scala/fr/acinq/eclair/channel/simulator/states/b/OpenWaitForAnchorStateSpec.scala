package fr.acinq.eclair.channel.simulator.states.b

import akka.actor.Props
import akka.testkit.{TestFSMRef, TestProbe}
import fr.acinq.eclair.TestBitcoinClient
import fr.acinq.eclair.TestConstants.{Alice, Bob}
import fr.acinq.eclair.blockchain.{PeerWatcher, WatchConfirmed, WatchSpent}
import fr.acinq.eclair.channel.simulator.states.StateSpecBaseClass
import fr.acinq.eclair.channel.{OPEN_WAITING_THEIRANCHOR, _}
import lightning.{error, open_anchor, open_channel, open_commit_sig}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.concurrent.duration._

/**
  * Created by PM on 05/07/2016.
  */
@RunWith(classOf[JUnitRunner])
class OpenWaitForAnchorStateSpec extends StateSpecBaseClass {

  type FixtureParam = Tuple4[TestFSMRef[State, Data, Channel], TestProbe, TestProbe, TestProbe]

  override def withFixture(test: OneArgTest) = {
    val alice2bob = TestProbe()
    val bob2alice = TestProbe()
    val blockchainA = system.actorOf(Props(new PeerWatcher(new TestBitcoinClient(), 300)))
    val bob2blockchain = TestProbe()
    val paymentHandler = TestProbe()
    val alice: TestFSMRef[State, Data, Channel] = TestFSMRef(new Channel(alice2bob.ref, blockchainA, paymentHandler.ref, Alice.channelParams, "B"))
    val bob: TestFSMRef[State, Data, Channel] = TestFSMRef(new Channel(bob2alice.ref, bob2blockchain.ref, paymentHandler.ref, Bob.channelParams, "A"))
    alice2bob.expectMsgType[open_channel]
    alice2bob.forward(bob)
    bob2alice.expectMsgType[open_channel]
    bob2alice.forward(alice)
    awaitCond(bob.stateName == OPEN_WAIT_FOR_ANCHOR)
    test((bob, alice2bob, bob2alice, bob2blockchain))
  }

  test("recv open_anchor") { case (bob, alice2bob, bob2alice, bob2blockchain) =>
    within(30 seconds) {
      alice2bob.expectMsgType[open_anchor]
      alice2bob.forward(bob)
      awaitCond(bob.stateName == OPEN_WAITING_THEIRANCHOR)
      bob2alice.expectMsgType[open_commit_sig]
      bob2blockchain.expectMsgType[WatchConfirmed]
      bob2blockchain.expectMsgType[WatchSpent]
    }
  }

  test("recv error") { case (bob, alice2bob, bob2alice, bob2blockchain) =>
    within(30 seconds) {
      bob ! error(Some("oops"))
      awaitCond(bob.stateName == CLOSED)
    }
  }

  test("recv CMD_CLOSE") { case (bob, alice2bob, bob2alice, bob2blockchain) =>
    within(30 seconds) {
      bob ! CMD_CLOSE(None)
      awaitCond(bob.stateName == CLOSED)
    }
  }

}
