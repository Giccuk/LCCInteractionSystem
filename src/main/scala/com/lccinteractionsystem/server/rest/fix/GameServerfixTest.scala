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
class GameServerTest extends FeatureTest with Mockito {
  val server = new EmbeddedHttpServer(new GameServer_fix("jdbc:mysql://localhost:3306/lccgame_test"))
  implicit val formats = DefaultFormats
  
  "Server Setup" in {

    /** Should really start by clearing the database, and checking all
     *  the right bits are there 
     */
    
    /**
     * The GameServer sets up the institution automatically, so we should find one
     */
    //1.Should start with a game_institution
    server.httpGet(
        path="/institutions",
        andExpect = Ok,
        withJsonBody = """[{ 
          "type":"institution", 
          "path":"http://localhost:8888/institution/user/manager/game_institution" 
          }]"""
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
        
    /*** This is where we start making agents ***/
        
    //4.create first agent and add its knowledge
    val inter_resp = server.httpPost(
        path="/institution/create/user/manager/game_institution",
        postBody = """
{  
  "template":{  
    "protocol_id":"trustgame_simple",
    "agents":[  {  
        "agent_id":"agent1",
        "roles":[  {  "role":"investor(10,3)" } ]
      } ]
  },
  "data":{  }
}
          """,
        andExpect = Ok
        )

        /** Now do some work to get the interaction ID so we can use it **/ 
    val institution_path = "http://localhost:8888/interaction/user/manager/game_institution/"
    val interaction_path = (parse( inter_resp.contentString) \\ "path").extract[String]
    // Should be somthing like: http://localhost:8888/interaction/user/manager/game_institution/int3890
    interaction_path should startWith(institution_path)
    val interaction_id = interaction_path.replaceFirst(institution_path,"")
    interaction_id should startWith("int")
    
    val agent1path = s"http://localhost:8888/agent/user/manager/game_institution/$interaction_id/agent1"
    val agent2path = s"http://localhost:8888/agent/user/manager/game_institution/$interaction_id/agent2"
    
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
                "path" : "http://localhost:8888/agent/user/manager/game_institution/$interaction_id/agent1"
              }
            ]
          }
          """
        )
    
    System.err.println(s"Checking agent: $agent1path")
    Thread.sleep(1000)
     //Check that the agent is in the right state
    server.httpGet(
        path=agent1path,
        andExpect = Ok,
        withJsonBody = s"""
{
    "type": "agent",
    "path": "$agent1path",
    "next_steps": [ "e(invest(X, T), _)" ]
}
          """
        ) 
        
        
     //5. Make a new agent in the interaction
     server.httpPost(
         path=s"http://localhost:8888/interaction/create/user/manager/game_institution/$interaction_id",
         postBody = """
{  
  "template":{  
    "agent_id":"agent2",
    "roles":[  {  "role":"trustee(3)" } ]
  },
  "data":{  }
}
           """,
         andExpect = Ok,
         withJsonBody = s"""
           {
             "type" : "agent",
             "path":"$agent2path"
           }
           """
         )
    //Check that the new agent is there
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
                "path" : "http://localhost:8888/agent/user/manager/game_institution/$interaction_id/agent1"
              },
              {
                "type" : "agent",
                "path" : "http://localhost:8888/agent/user/manager/game_institution/$interaction_id/agent2"
              }
            ]
          }
          """
        )      
    Thread.sleep(1000)
    
    server.httpGet(
        path=agent1path,
        andExpect = Ok,
        withJsonBody = s"""
{
    "type": "agent",
    "path": "$agent1path",
    "next_steps": [ "e(invest(X, T), _)" ]
}
          """
        ) 
    
  }
}