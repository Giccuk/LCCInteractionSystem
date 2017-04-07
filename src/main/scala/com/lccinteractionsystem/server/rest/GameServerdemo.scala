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



object GameServerTest_mix extends agentInstitutionRESTServer(new ResourceProtocolStore("/phpgameprotocols")) with Asking{
  val console = new StdInInstitutionConsole {}

  def gamestatestore: StateStore=new MysqlSlickStateStore

  val game_factory = new DefaultInstitutionFactory("game_factory","Game institution factory that uses Slick to store agent states") { //when it makes an environment factory, make a special one
     override def get_environment_factory(d:DefaultInstitutionDef) : EnvironmentFactory =
       new SimpleEnvironmentFactory(manager.protocols) {//When you create an agent environment, make a special one
         override def handle(spec:AgentSpec,extra:Any) : Option[EnvironmentBuilder] =
           super[SimpleEnvironmentFactory].handle(spec,extra).map(s => s(gamestatestore)) //make the special environment by swapping in the new special store
       }
  }

  //Register new factory
  manager.register(game_factory)
  //Start an institution that uses your factory by passing in Some(<name of factory>)
  manager.start_institution("default",Some("game_factory")).now map console.set_institution
  console.run_in_background  
  
  val game_store=new MysqlSlickStateStore
  
  val gamedb=game_store.db
  
  game_store.init
  
  game_store.clear_states

  val s=StringState("peter","a(proposer,peter)","gameprotocol","msg_in","msg_out","wait")
  
  game_store.store_serialised(s)
  
  println("begin")
  val gamestate=game_store.get_state_info(Some("peter"))
  
  gamedb.close() 
  
}

class agentInstitutionRESTServer(val protocols:ProtocolStore) extends HttpServer {
  //TODO: proper protocol store
  val manager = InstitutionManager.create(protocols)

  override def configureHttp(router: HttpRouter) {
    router.add(new InstitutionManagement(manager))
    router.add(new InteractionManagement)
    router.add(new AgentManagement_slick)
  }
}

class AgentManagement_slick extends Controller with ControllerHandlesToJSON {
  implicit val formats = DefaultFormats

  /** Create a new agent */
  post(s"/$interaction_prefix/create/:*") { request : Request =>
    val intr = system.actorSelection(request.params("*")).resolveOne map InteractionHandle
    intr map { interaction =>
      val input = parse(request.getContentString);
      val template = (input \\ "template" ).extract[AgentTemplate]
      val data = input \\ "data" 
      //Currently ugly use of "now" - change to comprehension?
      interaction.create_agent(template, data) now match {
        case Some(ag) => json_response(agent_json(ag,false))
        case None => response.badRequest("Don't understand your JSON")
      }
    }  to_twitter
  }


  /** Display an agent's details */
  get(s"/$agent_prefix/:*") { request : Request =>
    val path = request.params("*")
    val inst = system.actorSelection(path).resolveOne
    
    inst map { i => json_response(agent_json(AkkaAgentHandle(i),true)) } to_twitter
    
  }
  
  //Tells the agent that something has been elicited, i.e. fills in an open e() predicate
  post(s"/$agent_prefix/elicited/:*") { request : Request =>
    val elicitation = parse(request.getContentString).extract[Elicited]
    system.actorSelection(request.params("*")).resolveOne map { a =>
      AkkaAgentHandle(a).inject_elicitation(elicitation.to_lsc)
    }
  }

  //Asks the agent to compute the given predicate and return the result
  post(s"/$agent_prefix/compute/:*") { request : Request =>
    val comp = parse(request.getContentString).extract[Compute]
    system.actorSelection(request.params("*")).resolveOne map { a =>
      AkkaAgentHandle(a).compute(comp.to_lsc) map ans_json map json_response
    } to_twitter 
  }
  
  //Untested
  post(s"/$agent_prefix/knows/:*") { request : Request =>
    val comp = parse(request.getContentString).extract[Knows]
    system.actorSelection(request.params("*")).resolveOne map { a =>
      AkkaAgentHandle(a).knows(comp.to_lsc) map ans_json map json_response
    } to_twitter 
  }
    
  //Untested
  post(s"/$agent_prefix/elicit/:*") { request : Request =>
    val comp = parse(request.getContentString).extract[Elicit]
    system.actorSelection(request.params("*")).resolveOne map { a =>
      AkkaAgentHandle(a).elicit(comp.to_lsc) map ans_json map json_response
    } to_twitter 
  }

  case class Elicited(elicited:String) {
    def to_lsc = LSCParserHelpers.e(elicited)
  }
  case class Knows(knows:String) {
    def to_lsc = LSCParserHelpers.k(knows)
  }
  case class Compute(compute:String) {
    def to_lsc = LSCParserHelpers.i(compute)
  }
  case class Elicit(elicit:String) {
    def to_lsc = LSCParserHelpers.e(elicit)
  }
}


