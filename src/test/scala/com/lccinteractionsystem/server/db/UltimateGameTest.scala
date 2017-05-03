package com.lccinteractionsystem.server.db

import com.moseph.scalsc.utility.BaseTest
import com.twitter.finagle.http.Status._
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.inject.server.FeatureTest
import com.twitter.inject.Mockito
import com.twitter.finatra.json.utils.JsonDiffUtil
import com.moseph.scalsc.environment.ResourceProtocolStore
import com.moseph.scalsc.server.rest._
import org.json4s.native.JsonMethods._
import org.json4s._

import com.moseph.scalsc.slick._
import com.moseph.scalsc.environment._
import com.moseph.scalsc.server._
import com.moseph.scalsc.slick.mysql.MysqlSlickStateStoreURL
import com.moseph.scalsc._
import com.moseph.scalsc.running.StdInInstitutionConsole

/**
 * Example server operation with JSON. Test class shows the JSON sent (for POST/PUT requests)
 * and the JSON expected after some of the creations
 */
class UltimateGameTest extends FeatureTest with Mockito {
  val gameserver=new InstitutionRESTServer(new ResourceProtocolStore("/phpgameprotocols"))
  val server = new EmbeddedHttpServer(gameserver)
  implicit val formats = DefaultFormats
  
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
  gameserver.manager.register(game_factory)
  //Now start an institution that uses your factory by passing in Some(<name of factory>)
  gameserver.manager.start_institution("game_institution",Some("game_factory"))
  //console.run_in_background 
  
  
  "Server Setup" in {
    //0.Should start with no institutions at all
    /*server.httpGet(
        path="/institutions",
        andExpect = Ok,
        withJsonBody = "[]"
    )
    //just a test, not necessary
    server.httpPost(
        path="/create_institution",
        postBody = """
          { "wrong_field" : "game_institution" }
          """,
        andExpect = BadRequest
    )
    //1.create a new institution
    server.httpPost(
        path="/create_institution",
        postBody = """
          { "name" : "game_institution" }
          """,
        andExpect = Ok,
        withJsonBody = """{ 
          "type":"institution", 
          "path":"http://localhost:8888/institution/user/manager/game_institution" 
          } """
    )*/
    //2.get the information about institution
    server.httpGet(
        path="/institutions",
        andExpect = Ok,
        withJsonBody = """
          [{
            "type":"institution",
            "path":"http://localhost:8888/institution/user/manager/game_institution"
            }]
          """
        )
    //3.get the information about the interaction. now nothing to show    
    server.httpGet(
        path="/institution/user/manager/game_institution",
        andExpect = Ok,
        withJsonBody = """
          {
            "type":"institution",
            "path":"http://localhost:8888/institution/user/manager/game_institution",
            "interactions":[]
          }
          """
        )
  
    //4.create first agent and add its knowledge
    val inter_resp = server.httpPost(
        path="/institution/create/user/manager/game_institution",
        postBody = """
          {
            "template":
            {
            "protocol_id" : "ultimategame",
            "agents": [
              {
                "agent_id":"peter",
                "roles" : [ { "role" : "proposer(10)" } ]
              }
            ]
            },
            "data" : {}
          }
          """,
        andExpect = Ok
        )
    val institution_path = "http://localhost:8888/interaction/user/manager/game_institution/"
    val interaction_path = (parse( inter_resp.contentString) \\ "path").extract[String]
    // Should be somthing like: http://localhost:8888/interaction/user/manager/game_institution/int3890
    interaction_path should startWith(institution_path)
    val interaction_id = interaction_path.replaceFirst(institution_path,"")
    interaction_id should startWith("int")
    
    //Check that the interaction is there
    server.httpGet(
        path=interaction_path,
        andExpect = Ok,
        withJsonBody = s"""
          {
            "type":"interaction",
            "path" : "http://localhost:8888/interaction/user/manager/game_institution/$interaction_id",
            "agents" : [
              {
                "type" : "agent",
                "path" : "http://localhost:8888/agent/user/manager/game_institution/$interaction_id/peter"
              }
            ]
          }

          """
        )
    
     //5. Make a new agent in the interaction
     server.httpPost(
         path=s"http://localhost:8888/interaction/create/user/manager/game_institution/$interaction_id",
         postBody = """
         {
           "template":{
             "agent_id":"richard",
             "roles" : [ { "role" : "responder(10)" } ],
           },
           "data" : {}
         }
           """,
         andExpect = Ok,
         withJsonBody = s"""
           {
             "type" : "agent",
             "path":"http://localhost:8888/agent/user/manager/game_institution/$interaction_id/richard"
           }

           """
         )
    //Check that the new agent is there
    server.httpGet(
        path=interaction_path,// Should be somthing like: http://localhost:8888/interaction/user/manager/game_institution/int3890
        andExpect = Ok,
        withJsonBody = s"""
          {
            "type":"interaction",
            "path" : "http://localhost:8888/interaction/user/manager/game_institution/$interaction_id",
            "agents" : [
              {
                "type" : "agent",
                "path" : "http://localhost:8888/agent/user/manager/game_institution/$interaction_id/peter"
              },
              {
                "type" : "agent",
                "path" : "http://localhost:8888/agent/user/manager/game_institution/$interaction_id/richard"
              }
            ]
          }

          """
        )      
    Thread.sleep(1000)
    
    val peter_path = s"http://localhost:8888/agent/user/manager/game_institution/$interaction_id/peter"
    val richard_path = s"http://localhost:8888/agent/user/manager/game_institution/$interaction_id/richard"
    //6.start game. Attention!!! This step is so important: server has to AUTOMATLY "ask" agent ges about its "next_step". Based on the LCC protocol, agent ges should send a request about nice(hos). 
    //1st run
    server.httpGet(
        path=peter_path,
        andExpect = Ok,
        withJsonBody = s"""
          {
            "type":"agent",
            "path":"$peter_path",
            "next_steps": ["e(offernum(X, R), _)"]
          }
          """
            )

    //hos is waiting for something to fill in e(friend(G)) ,so we supply it
  /*  server.httpPost(
        path=s"http://localhost:8888/agent/elicited/user/manager/game_institution/$interaction_id/peter",
        postBody="""
        {"elicited":"e(offernum(2, richard), _)"} 
        """,
        andExpect = Ok
        )
    Thread.sleep(1000)
    
    //Second run
  server.httpGet(
        path=richard_path,
        andExpect=Ok,
        withJsonBody=s"""
          {
            "type":"agent",
            "path":"$richard_path",
            "next_steps": ["e(acceptornot(D, X), _)"]
          }
          """
    )

    //peter is waiting for something to fill in
    server.httpPost(
        path=richard_path,
        postBody="""
        {"elicited":"e(acceptorno(reject, 2), _)"} 
        """,
        andExpect = Ok
        )
        //Wait again to make sure it's run through
    Thread.sleep(1000)
    
    //third run
    server.httpGet(
        path=peter_path,
        andExpect = Ok,
        withJsonBody = s"""
          {
            "type":"agent",
            "path":"$peter_path",
            "next_steps":[]
          }
          """
            )
            
    server.httpGet(
        path=richard_path,
        andExpect = Ok,
        withJsonBody = s"""
          {
            "type":"agent",
            "path":"$richard_path",
            "next_steps":[]
          }
          """
            )*/
  }

}