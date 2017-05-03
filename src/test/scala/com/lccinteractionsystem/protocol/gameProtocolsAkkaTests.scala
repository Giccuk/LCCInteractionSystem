package com.lccinteractionsystem.protocol

import com.moseph.scalsc.environment._
import com.moseph.scalsc.artifacts._
import org.junit.runner.RunWith
import akka.actor._
import scala.concurrent.duration._
import org.scalatest.junit.JUnitRunner
import com.moseph.scalsc._
import com.moseph.scalsc.prolog._
import com.moseph.scalsc.visualisation._
import com.moseph.scalsc.running.AkkaProtocolTestSetup
import com.moseph.scalsc.utility.DataLocation
import com.moseph.scalsc.utility.ResourceLocation


@RunWith(classOf[JUnitRunner])
class FullProtocolAkkaTests extends AkkaProtocolTestSetup {
  val non_conforming = false //Do we run the tests that still don't work for unknown reasons?
  
  implicit val proto_loc = ResourceLocation("/phpgameprotocols")

  it should "play shop" in {
    running("ultimategame.inst").with_defaults should runOK
  }
  
}