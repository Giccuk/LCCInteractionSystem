package com.lccinteractionsystem.restserver.simpletest

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

/**
 * Example server operation with JSON. Test class shows the JSON sent (for POST/PUT requests)
 * and the JSON expected after some of the creations
 */
class UltimateGameRESTServerTest extends FeatureTest with Mockito {
  val server = new EmbeddedHttpServer(new InstitutionRESTServer(new ResourceProtocolStore("/phpgameprotocols")))
  implicit val formats = DefaultFormats
  
  "Server Setup" in {
    //0.Should start with no institutions at all
    server.httpGet(
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
    )
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
    server.httpPost(
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
            "next_steps": ["e(acceptorno(D, X), _)"]
          }
          """
    )

    //peter is waiting for something to fill in
    server.httpPost(
        path=richard_path,
        postBody="""
        {"elicited":"e(acceptornot(reject, 2), _)"} 
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
            )
  }

}