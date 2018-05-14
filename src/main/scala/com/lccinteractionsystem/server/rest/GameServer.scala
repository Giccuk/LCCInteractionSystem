package com.lccinteractionsystem.server.rest

import com.moseph.scalsc.akka.DefaultLCCActorSystem._
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import org.json4s.JsonAST._
import com.moseph.scalsc.utility.Asking
import com.moseph.scalsc._
import com.moseph.scalsc.utility._
import com.moseph.scalsc.environment.ResourceProtocolStore
import com.moseph.scalsc.running.StdInInstitutionConsole
import com.moseph.scalsc.server.DefaultInstitutionDef
import com.moseph.scalsc.server.rest._
import com.moseph.scalsc.slick._
import com.moseph.scalsc.environment._
import com.moseph.scalsc.server._
import com.lccinteractionsystem.slick.mysql._


object GameServerRunner extends GameServer

class GameServer(url:String="jdbc:mysql://localhost:3306/lccgame_test",user:String="host",password:String="host") extends InstitutionRESTServer(new ResourceProtocolStore("/phpgameprotocols")) with Asking {
  val console = new StdInInstitutionConsole {}

  def game_statestore=new GameMySqlSlickStateStoreURL(url,user,password)
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

   
}


