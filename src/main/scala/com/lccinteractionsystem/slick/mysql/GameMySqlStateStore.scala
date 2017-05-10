package com.lccinteractionsystem.slick.mysql
import com.moseph.scalsc.slick.mysql._
import slick.jdbc.MySQLProfile.api._
import com.moseph.scalsc.environment._

class GameMySqlSlickStateStoreURL(gamedatabase:String, gameuser:String, gamepassword:String,gamedriver:String="com.mysql.jdbc.Driver") 
extends MysqlSlickStateStoreURL("test	","root","","com.mysql.jdbc.Driver") {
  override implicit lazy val db = Database.forURL(url=gamedatabase,driver=gamedriver,user=gameuser,password=gamepassword)
  init
  /*override def state_terminated(id:StateInfo)= {
    println(id.role.id+"'s state is terminated.")
    
  }*/
}