import org.eclipse.jetty.server._
import org.eclipse.jetty.server.handler.{HandlerCollection, RequestLogHandler}
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.example.ApplicationConfig
import org.scalatra.servlet.ScalatraListener
import org.slf4j.LoggerFactory

object ScalatraLauncher extends App {

  if (args.lift(0).exists(_ == "update")) runUpdate
  else if (args.lift(0).exists(_ == "rescue")) runRescue
  else startServer
  
  def runUpdate: Unit = {
    println("migrating database, ...")
  }
  
  def runRescue: Unit = {
    println("starting rescue mode")
  }

  def startServer: Unit = {

    val appConfig = ApplicationConfig.read

    val log = LoggerFactory.getLogger("ScalatraLauncher")

    // create jetty server
    val server = new Server
    server.setStopTimeout(5000)
    server.setStopAtShutdown(true)

    val config = new HttpConfiguration()
    config.setSendServerVersion(false)
    config.setSendDateHeader(true)

    val connector = new ServerConnector(server, new HttpConnectionFactory(config))
    connector.setHost(appConfig.webServer.host)
    connector.setPort(appConfig.webServer.port)
    connector.setIdleTimeout(30000)
    server.addConnector(connector)

    // in development mode create a symlink from webapp to target/webapp
    val webAppResourceBase = appConfig.webServer.webappDirectory

    // create web application context
    val webAppContext = new WebAppContext
    webAppContext.setContextPath("/")
    webAppContext.setResourceBase(webAppResourceBase)
    webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false")
    webAppContext.setEventListeners(Array(new ScalatraListener))

    // setup request logging (-> http://logback.qos.ch/access.html)
    val requestLog = new NCSARequestLog
    requestLog.setFilename(f"${appConfig.logDirectory}/jetty/yyyy_mm_dd.request.log")
    requestLog.setFilenameDateFormat("yyyy_MM_dd")
    requestLog.setRetainDays(90)
    requestLog.setAppend(true)
    requestLog.setExtended(true)
    requestLog.setLogCookies(false)
    requestLog.setLogTimeZone("GMT")

    val requestLogHandler = new RequestLogHandler()
    requestLogHandler.setRequestLog(requestLog)

    // create a composite handler consisting of webapp & request logging
    val handlers = new HandlerCollection
    handlers.addHandler(webAppContext)
    handlers.addHandler(requestLogHandler)

    server.setHandler(handlers)

    server.start
    server.join

  }

}