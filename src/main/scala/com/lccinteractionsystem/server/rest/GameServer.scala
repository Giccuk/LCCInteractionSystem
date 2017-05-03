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
import com.moseph.scalsc.slick.mysql.MysqlSlickStateStoreURL



object GameServer extends InstitutionRESTServer(new ResourceProtocolStore("/phpgameprotocols")) with Asking {
  val console = new StdInInstitutionConsole {}

  def game_statestore: SlickStateStore=new MysqlSlickStateStoreURL("jdbc:mysql://localhost:3306/lccgame")
  game_statestore.init
  
  val game_factory = new DefaultInstitutionFactory("game_factory","Game institution factory that uses Slick to store agent states") { //when it makes an environment factory, make a special one
     override def get_environment_factory(d:DefaultInstitutionDef) : EnvironmentFactory =
       new SimpleEnvironmentFactory(manager.protocols) {//When you create an agent environment, make a special one
         override def handle(spec:AgentSpec,extra:Any) : Option[EnvironmentBuilder] =
           super[SimpleEnvironmentFactory].handle(spec,extra) map {s => s(game_statestore)} //make the special environment by swapping in the new special store
       }
  }
    
  //Register your factor
  manager.register(game_factory)
  //Now start an institution that uses your factory by passing in Some(<name of factory>)
  manager.start_institution("default",Some("game_factory")).now map console.set_institution

  
  console.run_in_background  
  
  
  
  class GameInstitutionFactory_mysql(id:String,db_config:String) 
  extends DefaultInstitutionFactory(id, "Game institution factory that uses Slick to store agent states") { 
  //when it makes an environment factory, make a special one
  override def get_environment_factory(d:DefaultInstitutionDef) : EnvironmentFactory =
    new SimpleEnvironmentFactory(manager.protocols) {
      //Handle the creation differently
      override def handle(spec:AgentSpec,extra:Any) : Option[EnvironmentBuilder] = {
        val id = spec.agent_id
        //Debug message
        System.err.println(s"------------\n Creating environment for $id!\n--------------")
        try {
          //Start from the default agent environment
          val default_environment = super[SimpleEnvironmentFactory].handle(spec,extra) 
          //Create the store to use
          val my_store = create_store(id)
          //make the special environment by swapping in the new special store
          val my_environment = default_environment map {s => s(my_store)} 
          System.err.println("Made my environment")
          //Any other agent debug stuff here
          System.err.println("DONE\n-----")
          my_environment
        } catch {
          case e:Exception => System.err.println("Couldn't make environment: " + e )
          throw e
        }
      }
    }

  //This is the function we're going to use for creating a game state store
  //Uses the agent ID just for debugging
  def create_store(id:String): SlickStateStore= {
    val store = new MysqlSlickStateStoreURL(db_config)
    System.err.println(s"Creating a new state store for agent $id")
    store.init //connect to a database and table named "scalsc_states"
    store
  }
  }
   
}


