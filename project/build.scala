import com.earldouglas.xwp.XwpPlugin._
import com.mojolly.scalate.ScalatePlugin.ScalateKeys._
import com.mojolly.scalate.ScalatePlugin._
import org.scalatra.sbt._
import sbt.Keys._
import sbt._
import sbtdocker.DockerKeys._
import sbtdocker.DockerPlugin
import sbtdocker.mutable.Dockerfile

object ScalatraDockerBuild extends Build {
  val Organization = "org.scalatra"
  val Name = "Scalatra Docker App"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.11.6"
  val ScalatraVersion = "2.4.0-RC2-2"

  lazy val project = Project (
    "scalatra-docker-app",
    file("."),
    settings = ScalatraPlugin.scalatraSettings ++ scalateSettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
        "net.ceedubs" %% "ficus" % "1.1.2",
        "ch.qos.logback" % "logback-classic" % "1.1.2" % "runtime",
        "org.eclipse.jetty" % "jetty-webapp" % "9.2.10.v20150310" % "compile;container",
        "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
      ),

      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty,  /* default imports should be added here */
            Seq(
              Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
            ),  /* add extra bindings here */
            Some("templates")
          )
        )
      },

      exportJars := true,

      // exclude application.conf from generated .jar
      mappings in (Compile, packageBin) ~= { _.filterNot(_._1.getName == "application.conf") },

      docker <<= docker.dependsOn(prepareWebapp),

      mainClass := Some("Launcher"),

      dockerfile in docker := {

        val classpath = (fullClasspath in Runtime).value
        val webappDir = (webappDest in webapp).value

        val mainclass = mainClass.in(Compile, packageBin).value.getOrElse(sys.error("Expected exactly one main class"))

        // Make a colon separated classpath with the JAR file
        val classpathString = classpath.files.map("/app/lib/" + _.getName).mkString(":")

        new Dockerfile {

          from("java")

          maintainer("Molly Millions <mm@example.org>")

          // Install packages
          runRaw("apt-get update")
          runRaw("apt-get install -y vim curl wget unzip")

          // Install Oracle Java JDK 1.8.x
          //  runRaw("mkdir -p /usr/lib/jvm")
          //  runRaw("""wget --header "Cookie: oraclelicense=accept-securebackup-cookie" -O /usr/lib/jvm/jdk-8u51-linux-x64.tar.gz http://download.oracle.com/otn-pub/java/jdk/8u51-b16/jdk-8u51-linux-x64.tar.gz""")
          //  runRaw("tar xzf /usr/lib/jvm/jdk-8u51-linux-x64.tar.gz --directory /usr/lib/jvm")
          //  runRaw("update-alternatives --install /usr/bin/java java /usr/lib/jvm/jdk1.8.0_51/bin/java 100")
          //  runRaw("update-alternatives --install /usr/bin/javac javac /usr/lib/jvm/jdk1.8.0_51/bin/javac 100")

          // Add all .jar files
          add(classpath.files, "/app/lib/")

          // All all files from webapp
          add(webappDir, "/app/webapp")

          // Remove lib and classes
          runRaw("rm -rf /app/webapp/WEB-INF/lib")
          runRaw("rm -rf /app/webapp/WEB-INF/classes")

          // Define some volumes for persistent data (containers are immutable)
          volume("/app/conf")
          volume("/app/data")

          expose(80)

          workDir("/app")

          cmdRaw("java -Dconfig.file=$CONFIG_FILE -cp " + classpathString + "  " + mainclass)

        }

      }
    )
  ).enablePlugins(DockerPlugin)
}
