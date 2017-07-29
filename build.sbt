
import org.scalatra.sbt._


organization := "org.scalatra"
name := "Scalatra Docker App"
version := "0.1.0-SNAPSHOT"
scalaVersion := "2.12.3"

resolvers += Classpaths.typesafeReleases

val ScalatraVersion = "2.5.1"

libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % ScalatraVersion,
  "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
  "com.iheart" %% "ficus" % "1.4.1",
  "com.lihaoyi" %% "scalatags" % "0.6.5",
  "ch.qos.logback" % "logback-classic" % "1.1.2" % "runtime",
  "org.slf4j" % "slf4j-api" % "1.7.25" % "runtime",
  "org.eclipse.jetty" % "jetty-webapp" % "9.4.6.v20170531",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
)

ScalatraPlugin.scalatraSettings

enablePlugins(JettyPlugin, DockerPlugin)

// exclude application.conf from generated .jar
mappings in (Compile, packageBin) ~= { _.filterNot(_._1.getName == "application.conf") }

mainClass := Some("Launcher")

exportJars := true

docker <<= docker.dependsOn(sbt.Keys.`package`)

dockerfile in docker := {

  val classpath = (fullClasspath in Runtime).value
  val webappDir = (target in webappPrepare).value

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

