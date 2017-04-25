package com.lccinteractionsystem.server.mysql

import com.moseph.scalsc.slick.mysql._
import com.moseph.scalsc.environment._

object simpletest extends App {
  
  val db=new MysqlSlickStateStoreURL("jdbc:mysql://localhost:3306/lccgame")  
  db.clear_states
  val firststate= StringState("peter","a(proposer(10),p)","gameprotocols","none","none","nostate")
  db.store_serialised(firststate)
  
}