package com.lccinteractionsystem.server.rest

import com.moseph.scalsc.slick.mysql.MysqlSlickStateStore

object simpletest extends App {
  val ab=new MysqlSlickStateStore("gamedb")
  ab.init
  
}