package org.scalatra.example

import scalatags.Text.all._

object Views {

  def index(label: String, uri: String) = {
    html(
      body(
        h1("Hello, world!"),
        "Say ",
        a(href := uri, label),
        "."
      )
    )

  }

}