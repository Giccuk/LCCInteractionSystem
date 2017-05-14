package com.lccinteractionsystem.slick.mysql

import slick.jdbc.MySQLProfile.api._
import slick.driver.MySQLDriver
import slick.jdbc.meta.MTable

import com.moseph.scalsc.slick._
import com.moseph.scalsc.slick.mysql._
import com.moseph.scalsc.environment._


import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}



class GameMySqlSlickStateStoreURL(gamedatabaseurl:String, gameuser:String, gamepassword:String) 
extends MysqlSlickStateStoreURL(gamedatabaseurl, gameuser, gamepassword,"com.mysql.jdbc.Driver") with SlickStateBackUp{
  
  def createbackuptable={
    val backuptables=List(backuptable)//list of tables used for backuping states
    val existingtables=run(MTable.getTables)//get existing tables in database
    val createaction=existingtables.flatMap( v =>{
      val names=v.map(mt=>mt.name.name)
      val createIfNoExit=backuptables.filter(t =>(!names.contains(t.baseTableRow.tableName))).map(_.schema.create)
      run(DBIO.sequence(createIfNoExit))
    })   
    Await.result(createaction,Duration.Inf)
  }
  
  override def state_terminated(id:StateInfo)= { 
    val table_backup=copydata(id.comm_id,id.role.id.toString,backuptable.baseTableRow.tableName,table.baseTableRow.tableName)
    run_now(table_backup)
    remove_state(id)
  }
   
}