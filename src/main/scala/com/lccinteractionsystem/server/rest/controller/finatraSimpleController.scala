package com.lccinteractionsystem.server.rest.controller

import javax.inject.Inject
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
 
class finatraSimpleController extends Controller {
 
  get("/hello") { request: Request =>
    "hay"
  }
  get("/proposeroffer") { request: Request =>
    {
      val ss=ExampleCaseClass(id = "123",description = "This is a JSON response body",longValue = 1L,boolValue = true)
      ss.id
    }
  }
  post("/agentid",name="id"){ request: Request=>
    ExampleCaseClass("agentid","secondLine",1L,true)
  }
}



case class ExampleCaseClass(
  id: String,
  description: String,
  longValue: Long,
  boolValue: Boolean){
  def move =println(id)
}

