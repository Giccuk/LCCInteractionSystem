package com.lccinteractionsystem.server.rest

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
class TrustGameRESTServerTest extends FeatureTest with Mockito {
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
            "protocol_id" : "trustgame_simple",
            "agents": [
              {
                "agent_id":"ian",
                "roles" : [ { "role" : "investor(10,3)" } ]
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
        path=interaction_path,//http://localhost:8888/interaction/user/manager/game_institution/int2142
        andExpect = Ok,
        withJsonBody = s"""
          {
            "type":"interaction",
            "path" : "http://localhost:8888/interaction/user/manager/game_institution/$interaction_id",
            "agents" : [
              {
                "type" : "agent",
                "path" : "http://localhost:8888/agent/user/manager/game_institution/$interaction_id/ian"
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
             "agent_id":"ted",
             "roles" : [ { "role" : "trustee(3)" } ]
           },
           "data" : {}
         }
           """,
         andExpect = Ok,
         withJsonBody = s"""
           {
             "type" : "agent",
             "path":"http://localhost:8888/agent/user/manager/game_institution/$interaction_id/ted"
           }

           """
         )
    //Check that the new agent is there
    server.httpGet(
        path=interaction_path,// Should be  http://localhost:8888/interaction/user/manager/game_institution/int3890
        andExpect = Ok,
        withJsonBody = s"""
          {
            "type":"interaction",
            "path" : "http://localhost:8888/interaction/user/manager/game_institution/$interaction_id",
            "agents" : [
              {
                "type" : "agent",
                "path" : "http://localhost:8888/agent/user/manager/game_institution/$interaction_id/ian"
              },
              {
                "type" : "agent",
                "path" : "http://localhost:8888/agent/user/manager/game_institution/$interaction_id/ted"
              }
            ]
          }

          """
        )      
    Thread.sleep(1000)
    
    val ian_path = s"http://localhost:8888/agent/user/manager/game_institution/$interaction_id/ian"
    val ted_path = s"http://localhost:8888/agent/user/manager/game_institution/$interaction_id/ted"
    //6.start game. Attention!!! This step is so important: server has to AUTOMATLY "ask" agent ges about its "next_step". Based on the LCC protocol, agent ges should send a request about nice(hos). 
    //1st run
    server.httpGet(
        path=ian_path,
        andExpect = Ok,
        withJsonBody = s"""
          {
            "type":"agent",
            "path":"$ian_path",
            "next_steps": ["e(invest(X, T), _)"]
          }
          """
            )

    //hos is waiting for something to fill in e(friend(G)) ,so we supply it
    server.httpPost(
        path=s"http://localhost:8888/agent/elicited/user/manager/game_institution/$interaction_id/ian",
        postBody="""
        {"elicited":"e(invest(4, ted), _)"} 
        """,
        andExpect = Ok
        )
    Thread.sleep(1000)
    
    //Second run
    server.httpGet(
        path=ted_path,
        andExpect=Ok,
        withJsonBody=s"""
          {
            "type":"agent",
            "path":"$ted_path",
            "next_steps": ["e(repay(Y, I), _)"]
          }
          """
    )

    //ian is waiting for something to fill in
    server.httpPost(
        path=s"http://localhost:8888/agent/elicited/user/manager/game_institution/$interaction_id/ted",
        postBody="""
        {"elicited":"e(repay(3, ian), _)"} 
        """,
        andExpect = Ok
        )
        //Wait again to make sure it's run through
    Thread.sleep(1000)
    
    //third run
    server.httpGet(
        path=ian_path,//http://localhost:8888/agent/user/manager/game_institution/$interaction_id/ian
        andExpect = Ok,
        withJsonBody = s"""
          {
            "type":"agent",
            "path":"$ian_path",
            "next_steps":[]
          }
          """
            )
            
    server.httpGet(
        path=ted_path,//http://localhost:8888/agent/user/manager/game_institution/$interaction_id/ted
        andExpect = Ok,
        withJsonBody = s"""
          {
            "type":"agent",
            "path":"$ted_path",
            "next_steps":[]
          }
          """
            )
  }

}