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
import com.moseph.scalsc.slick.mysql._
import scala.concurrent.duration._

/**
 * Example game server created by DMR that uses a custom state store,
 * and demonstrates what is in it before and after running the protocol
 */
object GameServerExampleMySQL extends InstitutionRESTServer(new ResourceProtocolStore("/phpgameprotocols")) with Asking{
  
  val dburl="jdbc:mysql://localhost:3306/lccgame"

  //Create a game institution factory
  val gameInstitutin_factory = new GameInstitutionFactory_mysql("gameInstitutin_factory",dburl)
  
  /***************************************************************************
   * The next few lines are just for testing, and showing what is happening 
   ****************************************************************************/
  
  //This is a store we'll use to test whether states are present before and after starting the interaction
  val store = gameInstitutin_factory.create_store("test_store")

  //Need to put the store into an environment so that it works
  val env_builder = new EnvironmentBuilder(protocols=protocols)(store).mock()

  //What states were left over from the previous run?
  System.err.println("States before the run:\n=>"+store.get_all_states().mkString("\n=>"))
  
  /**************************
   * End testing block
   **************************/

  //Start a console to interact with the agents if we want to 
  val console = new StdInInstitutionConsole {}
  
  //Register the factory
  manager.register(gameInstitutin_factory)

  //Now start an institution that uses the factory by passing in Some(<name of factory>)
  val institution = manager.start_institution("game_institution",Some("gameInstitutin_factory")).now.get
  //val institution = manager.start_institution("default").now.get
  System.err.println("Created Institution!")
  console.set_institution(institution)

  console.run_in_background 
   
}


//An InstitutionFactory that will use the Slick state store
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
    val store = new MysqlSlickStateStoreURL(db_config,"host","host")
    System.err.println(s"Creating a new state store for agent $id")
    store
  }

}
