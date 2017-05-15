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

import com.lccinteractionsystem.slick.mysql._



object GameServer extends InstitutionRESTServer(new ResourceProtocolStore("/phpgameprotocols")) with Asking {
  val console = new StdInInstitutionConsole {}

  def game_statestore=new GameMySqlSlickStateStoreURL("jdbc:mysql://localhost:3306/lccgame","host","host")
  game_statestore.init
  game_statestore.createbackuptable
  
  val game_factory = new DefaultInstitutionFactory("game_factory","Game institution factory that uses Slick to store agent states") { //when it makes an environment factory, make a special one
     override def get_environment_factory(d:DefaultInstitutionDef) : EnvironmentFactory =
       new SimpleEnvironmentFactory(manager.protocols) {//When you create an agent environment, make a special one
         override def handle(spec:AgentSpec,extra:Any) : Option[EnvironmentBuilder] =
           super[SimpleEnvironmentFactory].handle(spec,extra) map {s => s(game_statestore)} //make the special environment by swapping in the new special store
       }
  }
   
  manager.register(game_factory)
  manager.start_institution("game_institution",Some("game_factory")).now map console.set_institution
  
  console.run_in_background  
/*trait SlickStateBackUp {

  class BackUpSlickStates(tag:Tag) extends Table[SlickState](tag,"backup_scalsc_states") {
    def agent_id = column[String]("AGENT_ID")
    def comm_id = column[String]("COMM_ID")
    def role = column[String]("ROLE")
    def protocol = column[String]("PROTOCOL")
    def msg_in = column[String]("MSG_IN")
    def msg_out = column[String]("MSG_OUT")
    def state = column[String]("STATE")
    def * = (agent_id,comm_id,role,protocol,msg_in,msg_out,state) <> (SlickState.tupled,SlickState.unapply)
  } 
  
  val backuptable=TableQuery[BackUpSlickStates]

  def copydata(keyinfo:String,keyinfo2:String,newtable_name:String,oldtable_name:String):DBIO[Int]={ 
    val insertaction:DBIO[Int]=sqlu"INSERT INTO #$newtable_name SELECT * FROM #$oldtable_name WHERE COMM_ID=$keyinfo AND AGENT_ID=$keyinfo2 "
    return insertaction  
  }
  
}*/
   
}


