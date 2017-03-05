package com.lccinteractionsystem.server.rest

import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter
import com.lccinteractionsystem.server.rest.controller.finatraSimpleController

object simpleapiMain extends simpleapiServer
 
class simpleapiServer extends HttpServer {
 
  override def configureHttp(router: HttpRouter): Unit = {
    router.add[finatraSimpleController]  // We register the controllers we use
  }
   
}