package org.scalatra.example

import com.typesafe.config.{Config, ConfigFactory}

import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ValueReader

case class ApplicationConfig(
                              instanceName: String,
                              environment: AppEnvironment,
                              dataDirectory: String,
                              confDirectory: String,
                              webServer: ServerConfig
                            ) {

  val isProduction = environment.isProduction

  val isDevelopment = environment.isDevelopment

  val workingDirectory: String = f"$dataDirectory/tmp"

  val logDirectory: String = f"$dataDirectory/logs"

  // file containing a log of all successful updates (database, filesystem, configuration, ...)
  val updateLog: String = f"$dataDirectory/update.log"

  // tempates can be found here
  val templateSourceDirectory = f"${webServer.webappDirectory}/WEB-INF/templates/views"

}

case class ServerConfig(host: String, port: Int, webBase: String, webappDirectory: String) {

  val jettySessionPath: String = "sessions"

}

sealed trait AppEnvironment {

  def isProduction = this == AppEnvironment.Production

  def isDevelopment = this == AppEnvironment.Development

}

object AppEnvironment {

  case object Development extends AppEnvironment

  case object Staging extends AppEnvironment

  case object Test extends AppEnvironment

  case object Production extends AppEnvironment

  def fromString(s: String): AppEnvironment = {
    s match {
      case "development" => Development
      case "staging" => Staging
      case "test" => Test
      case "production" => Production
    }
  }

  def toString(s: AppEnvironment): String = {
    s match {
      case Development => "development"
      case Staging => "staging"
      case Test => "test"
      case Production => "production"
    }
  }
}

object ApplicationConfig {

  def read: ApplicationConfig = {

    val config: Config = ConfigFactory.load()

    implicit val webServerReader = new ValueReader[ServerConfig] {

      def read(conf: Config, path: String): ServerConfig = {
        val host = conf.as[String]("host")
        val port = conf.as[Int]("port")
        val webBase = conf.as[String]("webBase")
        val webappDirectory = conf.as[String]("webappDirectory")
        ServerConfig(host, port, webBase, webappDirectory)
      }
    }

    val cfgReader = new ValueReader[ApplicationConfig] {

      def read(conf: Config, path: String): ApplicationConfig = {

        val instanceName = conf.as[String]("instanceName")
        val environment = AppEnvironment.fromString(conf.as[String]("environment"))
        val dataDirectory = conf.as[String]("dataDirectory")
        val confDirectory = conf.as[String]("confDirectory")
        val serverConfig = conf.as[ServerConfig]("")

        ApplicationConfig(instanceName, environment, dataDirectory, confDirectory, serverConfig)

      }
    }

    cfgReader.read(config, "")

  }

}
