package org.scalatra.example

import org.scalatra._

class ScalatraApp extends ScalatraServlet {

  get("/") {
    Views.index("hello parrot", "/rotaugenpapagei.jpg")
  }

  notFound {
    // remove content type in case it was set through an action
    contentType = null
    serveStaticResource() getOrElse resourceNotFound()
  }


}
