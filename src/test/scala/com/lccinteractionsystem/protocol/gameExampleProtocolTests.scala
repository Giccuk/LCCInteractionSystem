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


/**
 * Examples of different ways to run protocols and check them against expected messages
 */
@RunWith(classOf[JUnitRunner])
class ExampleProtocolTests extends SimpleInstitutionTestSetup {
  
  /* This is where the protocols are found 
   * It should be a folder in the classpath. In this case,
   * since scalsc/src/test/resources is on the classpath,
   * we can use /standard_protocols to find
   * scalsc/src/test/resources/standard_protocols
   */
  /*implicit val proto_loc = ResourceLocation("/standard_protocols")

  
  /**
   * Simplest way to run the test. The 'with_defaults' means it looks for
   * * a messages file at /standard_protocols/shop.msg
   * * a data file at /standard_protocols/shop.data
   * 
   * If it doesn't have the .with_defaults, then it won't load messages,
   * so won't check against them
   */
  it should "play shop" in {
    running("shop.inst").with_defaults should runOK
  }
  
  /** 
   *  Example test which loads data from a particular file, and checks
   *  against a particular message file
   *  
   *  Data and messages files automatically get .data and .msg appended as appropriate
   *  */
  it should "play alternative shop" in {
    running("shop.inst")
      .with_data("shop.alternative.data")
      .with_messages("shop.alternative.msg") should runOK
  }
  
  /** 
   *  Alternative formulation for using specific data and messages
   *  */
  it should "play alternative shop again" in {
    running("shop.inst")
      .with_data("shop.alternative.data")
      .satisfying("jackie","afford(nikkon900,250)") should 
        produceMessagesIn("shop.alternative.msg")
  }
  
  it should "play shop loading roles from a file with inline messages" in {
    running("shop.inst")
      .with_data("shop.alternative.data") should 
      produceMessages( Seq(
        "a( buyer, jackie ) => ask(nikkon900) => a( shopkeeper, sam )",
        "a( shopkeeper, sam ) => price(nikkon900, 249.0) => a( buyer, jackie )",
        "a( buyer, jackie ) => buy(nikkon900, 249.0) => a( shopkeeper, sam )",
        "a( shopkeeper, sam ) => sold(nikkon900, 249.0) => a( buyer, jackie )"
        ))
  }
  
  /**
   * Checks can be added with 'satisfying' to make sure that the
   * agent can satisfy the given proposition at the start of the
   * interaction, or not_satisfying to make sure they can't
   */
  it should "play shop with checks" in {
    running("shop.inst")
      .satisfying("billie","wants(canonS500)")   // Can satisfy the exact thing
      .satisfying("billie","wants(X)","X"->"canonS500")  //Satisfy with a variable substitution
      .satisfying("billie","afford(canonS500,250)")     // This one has logic behind it in the file
      .not_satisfying("billie","afford(canonS500,251)") //This shouldn't be satisfied
      .not_satisfying("billie","wants(pony)")   // This shouldn't be satisfied
      .with_defaults should runOK              //runOK still used to actually run the thing
  }
  
  /**
   * Actors/roles can be specified as well
   */
  it should "play shop with extra roles" in {
    running("shop.inst")
      .with_roles("a(buyer,norma)","a(shopkeeper,janice)") //These agents won't actually do anything, but this is how to set up extra actors if you need them
      .with_defaults should runOK              //runOK still used to actually run the thing
  }*/
  
  implicit val proto_loc = ResourceLocation("/phpgameprotocols")
  it should "play" in {
    running("ultimategame.inst")
       .with_data("ultimategame.data")
      .with_messages("ultimategame.msg") should runOK

  }
  
}

