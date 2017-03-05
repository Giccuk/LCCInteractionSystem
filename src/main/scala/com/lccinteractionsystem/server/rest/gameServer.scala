package com.lccinteractionsystem.server.rest

import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.Controller
import com.twitter.finagle.http.Request
import com.twitter.finatra.request.QueryParam
import com.twitter.finatra.http.routing.HttpRouter
import com.moseph.scalsc.utility.FuturesHelper
import scala.concurrent.ExecutionContext.Implicits.global
import com.moseph.scalsc.akka.DefaultLCCActorSystem._
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import org.json4s.JsonAST._
import com.moseph.scalsc.utility.Asking
import com.moseph.scalsc._
import com.moseph.scalsc.utility._
import com.moseph.scalsc.akka.AkkaAgentHandle
import com.moseph.scalsc.server.InstitutionHandle
import com.moseph.scalsc.server.InstitutionManager
import com.moseph.scalsc.server.InteractionHandle
import com.moseph.scalsc.server.InteractionTemplate
import org.json4s.DefaultFormats
import org.json4s.JsonAST.JValue
import com.moseph.scalsc.environment.SimpleProtocolStore
import com.moseph.scalsc.server.InstitutionManagerHandle
import com.moseph.scalsc.environment.ProtocolStore
import com.moseph.scalsc.environment.ResourceProtocolStore
import com.moseph.scalsc.running.StdInInstitutionConsole
import com.moseph.scalsc.server.DefaultInstitutionDef
import com.moseph.scalsc.server.AgentTemplate
import com.moseph.scalsc.server.rest._

import com.moseph.scalsc.slick._
import com.moseph.scalsc.environment._
import com.moseph.scalsc.server._

object GameServer extends InstitutionRESTServer(new ResourceProtocolStore("/phpgameprotocols")) with Asking{
  val console = new StdInInstitutionConsole {} 
//  manager.start_institution("default").now map console.set_institution  
//console.run_in_background
 
  def gameagent_statestore(url:String,driver:String): StateStore=new SlickStateStore(){}
  
  val gameinstitution_factory = new DefaultInstitutionFactory("gameexperiment_factory","Game institution factory that uses Slick to store agent states") { //when it makes an environment factory, make a special one   
     override def get_environment_factory(d:DefaultInstitutionDef) : EnvironmentFactory = 
       new SimpleEnvironmentFactory(manager.protocols) {//When you create an agent environment, make a special one  
         override def handle(spec:AgentSpec,extra:Any) : Option[EnvironmentBuilder] = 
           super[SimpleEnvironmentFactory].handle(spec,extra) map {s => s(gameagent_statestore("jdbc:h2:./db/test","org.h2.Driver"))} //make the special environment by swapping in the new special store 
       }
  }
  
  //Register your factory  
  manager.register(gameinstitution_factory)  
  //Now start an institution that uses your factory by passing in Some(<name of factory>) 
  manager.start_institution("default",Some("gameexperiment_factory")).now map console.set_institution
  console.run_in_background
  
  }


  
  


