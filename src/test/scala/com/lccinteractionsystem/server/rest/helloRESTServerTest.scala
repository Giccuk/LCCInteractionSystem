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
class helloRESTServerTest extends FeatureTest with Mockito {
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
            "protocol_id" : "hello",
            "agents": [
              {
                "agent_id":"ges",
                "roles" : [ { "role" : "guest" } ]
              }, 
            ],
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
                "path" : "http://localhost:8888/agent/user/manager/game_institution/$interaction_id/ges"
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
             "agent_id":"hos",
             "roles" : [ { "role" : "host" } ],
           },
           "data" : {}
         }
           """,
         andExpect = Ok,
         withJsonBody = s"""
           {
             "type" : "agent",
             "path":"http://localhost:8888/agent/user/manager/game_institution/$interaction_id/hos"
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
                "path" : "http://localhost:8888/agent/user/manager/game_institution/$interaction_id/ges"
              },
              {
                "type" : "agent",
                "path" : "http://localhost:8888/agent/user/manager/game_institution/$interaction_id/hos"
              }
            ]
          }
          """
        )      
    Thread.sleep(1000)
    
    val hos_path = s"http://localhost:8888/agent/user/manager/game_institution/$interaction_id/hos"
    val ges_path = s"http://localhost:8888/agent/user/manager/game_institution/$interaction_id/ges"
    //6.start game. Attention!!! This step is so important: server has to AUTOMATLY "ask" agent ges about its "next_step". Based on the LCC protocol, agent ges should send a request about nice(hos). 
   //1sr round  
    server.httpGet(
        path=hos_path,//http://localhost:8888/agent/user/manager/game_institution/$interaction_id/hos
        andExpect = Ok,
        withJsonBody = s"""
          {
            "type":"agent",
            "path":"$hos_path",
            "next_steps": ["e(friend(G), _)"]
          }
          """
            )
   /*server.httpGet(
        path=ges_path,
        andExpect = Ok,
        withJsonBody = s"""
          {
            "type":"agent",
            "path":"$ges_path",
            "next_steps":["hello<=a(host,H)" ]
          }
          """
            )*/
    //hos is waiting for something to fill in e(friend(G)) ,so we supply it
    server.httpPost(
        path=s"http://localhost:8888/agent/elicited/user/manager/game_institution/$interaction_id/hos",
        postBody="""
        {"elicited":"e(friend(ges), _)"} 
        """,
        andExpect = Ok
        )
    Thread.sleep(1000)
    
    //2nd round
    server.httpGet(
        path=ges_path,//http://localhost:8888/agent/user/manager/game_institution/$interaction_id/ges
        andExpect=Ok,
        withJsonBody=s"""
          {
            "type":"agent",
            "path":"$ges_path",
            "next_steps": ["e(nice(H))"]
          }
          """
    )
    /*server.httpGet(
        path=hos_path,
        andExpect=Ok,
        withJsonBody=s"""
          {
            "type":"agent",
            "path":"$hos_path",
            "next_steps":["hey<=a(guest,G)"]
          }
          """
    )*/
    //ges is waiting for something to fill in e(want(T)) so we'll supply it
    server.httpPost(
        path=s"http://localhost:8888/agent/elicited/user/manager/game_institution/$interaction_id/ges",
        postBody="""
        {"elicited":"e(nice(hos), _)"} 
        """,
        andExpect = Ok
        )
        //Wait again to make sure it's run through
    Thread.sleep(1000)
    
    //3rd round
    server.httpGet(
        path=hos_path,//http://localhost:8888/agent/elicited/user/manager/game_institution/$interaction_id/hos
        andExpect = Ok,
        withJsonBody = s"""
          {
            "type":"agent",
            "path":"$hos_path",
            "next_steps":[]
          }
          """
            )
            
    server.httpGet(
        path=ges_path,//http://localhost:8888/agent/elicited/user/manager/game_institution/$interaction_id/ges
        andExpect = Ok,
        withJsonBody = s"""
          {
            "type":"agent",
            "path":"$ges_path",
            "next_steps":[]
          }
          """
            )
  }
}