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
import com.moseph.scalsc.slick.h2.H2SlickStateStore
import scala.concurrent.duration._


/**
 * Example game server created by DMR that uses a custom state store,
 * and demonstrates what is in it before and after running the protocol
 */
object ExampleGameServer extends InstitutionRESTServer(new ResourceProtocolStore("/phpgameprotocols")) with Asking{
  def store_url = "jdbc:h2:./db/test"

  //Create a game institution factory
  val game_factory = new GameInstitutionFactory("game_factory",store_url)
  
  /* The next few lines are just for testing, and showing what is happening */
  //This is a store we'll use to test whether states are present before and after starting the interaction
  val store = game_factory.create_store("test_store")
  
  //Need to put the store into an environment so that it works
  val env_builder = new EnvironmentBuilder(protocols=protocols)(store).mock()

  //What states were left over from the previous run?
  System.err.println("States before the run:\n=>"+store.get_all_states().mkString("\n=>"))
  /* End testing block */

  //Start a console to interact with the agents if we want to
  val console = new StdInInstitutionConsole {}
  //Register the factory
  manager.register(game_factory)
  

  //Now start an institution that uses the factory by passing in Some(<name of factory>)
  val institution = manager.start_institution("default",Some("game_factory")).now.get
  System.err.println("Created Institution!")
  console.set_institution(institution)

  /* At this point, there is an institution, but nothing is actually happening, so we start an interaction */
  
  //Start an interaction, with an agent playing proposer
  val interaction = institution.start_interaction(
      InteractionTemplate("ultimategame_iterate").with_agent("kev","proposer(5,10,i,r)"),
      NoData,"test_interaction_id").now.get
  console.set_interaction(interaction)

  //Start another agent just as an example
  val agent = interaction.create_agent(AgentTemplate("jimmy",Nil,Nil).playing("proposer(5,10,i,r)"),None).now.get
  console.set_agent(agent)

  console.run_in_background  
  
  //Kick everything off, and then we'll see what's happened
  DelayedFuture( 3 seconds )( {
    System.err.println("Ending States:\n=>"+store.get_all_states().mkString("\n=>"))
    //Obviously, get rid of this line if you want the states to be there afterwards
    store.clear_states
  })
   
}

//An InstitutionFactory that will use the Slick state store
class GameInstitutionFactory(id:String,store_url:String) 
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
    val store = new H2SlickStateStore(store_url)
    System.err.println(s"Creating a new state store for agent $id")
    store.init
    store
  }

}
