package com.lccinteractionsystem.protocol

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.moseph.scalsc.akka._
import com.moseph.scalsc._
import com.moseph.scalsc.prolog._
import com.moseph.scalsc.simple._
import com.moseph.scalsc.environment._
import com.moseph.scalsc.artifacts._
import com.moseph.scalsc.utility._
import com.moseph.scalsc.running.SimpleInstitutionTestSetup


@RunWith(classOf[JUnitRunner])
class StandardProtocolsSimpleTests extends SimpleInstitutionTestSetup {
  val non_conforming = false //Do we run the tests that still don't work for unknown reasons?
   
  implicit val proto_loc = ResourceLocation("/phpgameprotocols")

  it should "play shop" in {
    running("ultimategame.inst").with_defaults should runOK
  }
  
}