
import org.scalatra._
import javax.servlet.ServletContext

import org.scalatra.example.ScalatraApp

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new ScalatraApp, "/*")
  }
}
