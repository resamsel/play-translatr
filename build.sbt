name := """translatr"""

version := "3.0.3"

lazy val root = (project in file("."))
	.configs(IntegrationTest)
	.enablePlugins(PlayJava, PlayEbean, BuildInfoPlugin)
	.settings(
		Defaults.itSettings,
		buildInfoKeys := Seq[BuildInfoKey](name, version)
	)

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
	javaJdbc,

	cache,
	"com.typesafe.play.modules" %% "play-modules-redis" % "2.5.0",

	// Database
	"org.postgresql" % "postgresql" % "42.1.3",

	// OAuth for Play
	"com.feth" %% "play-authenticate" % "0.8.3",
	"be.objectify" %% "deadbolt-java" % "2.5.5",

	// Apache Commons IO
	"commons-io" % "commons-io" % "2.5",

	// https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient
	"org.apache.httpcomponents" % "httpclient" % "4.5.3",

	// https://mvnrepository.com/artifact/io.getstream.client/stream-repo-apache
	"io.getstream.client" % "stream-repo-apache" % "1.3.0",

	// https://mvnrepository.com/artifact/org.jsoup/jsoup
	"org.jsoup" % "jsoup" % "1.10.3",

	"io.prometheus" % "simpleclient_common" % "0.8.1",
	"io.prometheus" % "simpleclient_hotspot" % "0.8.1",

	"org.ocpsoft.prettytime" % "prettytime" % "4.0.1.Final",

	"io.swagger" %% "swagger-play2" % "1.5.3",
	"org.webjars" % "swagger-ui" % "2.2.10",

	"com.typesafe.play" %% "play-test" % play.core.PlayVersion.current % "it",
	"org.assertj" % "assertj-core" % "3.15.0" % "it,test",
	"org.mockito" % "mockito-core" % "2.8.47" % "it,test"
)

// re-create maven directory structure
unmanagedSourceDirectories in Compile += baseDirectory.value / "src" / "main" / "java"
unmanagedSourceDirectories in Test += baseDirectory.value / "src" / "test" / "java"

// shares contents of src/test/java with src/it/java
dependencyClasspath in IntegrationTest := (dependencyClasspath in IntegrationTest).value ++ (exportedProducts in Test).value

//
// Eclipse
//
// From: https://github.com/playframework/playframework/issues/3818
//
EclipseKeys.classpathTransformerFactories := EclipseKeys.classpathTransformerFactories.value.init

EclipseKeys.eclipseOutput := Some(".target")

// Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
EclipseKeys.preTasks := Seq(compile in Compile)

// Java project. Don't expect Scala IDE
EclipseKeys.projectFlavor := EclipseProjectFlavor.Java

EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)  // Use .class files instead of generated .scala files for views and routes

EclipseKeys.withSource := true

//
// Docker
//
maintainer := "René Panzar <rene.panzar@gmail.com>"

dockerRepository := Some("resamsel")

dockerBaseImage := "java:8-jre"

dockerExposedPorts in Docker := Seq(9000)

dockerExposedVolumes := Seq("/opt/docker/logs", "/opt/docker/data")

//
// Concat
//
Concat.groups := Seq(
	"styles.css" -> group(Seq(
		"stylesheets/materialize.min.css",
		"stylesheets/nprogress.css",
		"stylesheets/font-awesome.min.css",
		"stylesheets/d3.v3.css",
		"stylesheets/codemirror.css",
		"stylesheets/codemirror.translatr.css",
		"stylesheets/main.css",
		"stylesheets/editor.css",
		"stylesheets/template.css",
		"stylesheets/media.css"
	)),
	"scripts.js" -> group(Seq(
		"javascripts/jquery.min.js",
		"javascripts/jquery.ba-bbq.min.js",
		"javascripts/materialize.min.js",
		"javascripts/jquery.autocomplete.min.js",
		"javascripts/d3.v3.min.js",
		"javascripts/moment.min.js",
		"javascripts/nprogress.js",
		"javascripts/codemirror.js",
		"javascripts/codemirror.xml.js",
		"javascripts/underscore-min.js",
		"javascripts/backbone-min.js",
		"javascripts/backbone.undo.js",
		"javascripts/backbone-pageable.min.js",
		"javascripts/app.js",
		"javascripts/main.js",
		"javascripts/notification.js",
		"javascripts/editor.js"
	))
)

// Put everything into the concat dir
Concat.parentDir := "concat"

// Allows concatenated resources to be used in dev mode
pipelineStages in Assets := Seq(concat)

pipelineStages := Seq(concat)

//
// Tests
//
fork in Test := false
fork in IntegrationTest := false

//
// JaCoCo test coverage
//
jacoco.settings

// Unfortunately, this is really needed
parallelExecution in jacoco.Config := false

fork in jacoco.Config := false

jacoco.excludes in jacoco.Config := Seq(
	"router.*",
	"views.html.*", // should probably not be excluded
	"*.Reverse*",
	"*.routes",
	"*.scala"
)

//
// FindBugs
//
findbugsExcludeFilters := Some(
	<FindBugsFilter>
		<!-- See docs/examples at http://findbugs.sourceforge.net/manual/filter.html -->
		<Match><Class name="~views\.html\..*"/></Match>
		<Match><Class name="~router.Routes.*"/></Match>
		<Match><Class name="~.*controllers\.routes.*"/></Match>
		<Match><Method name="~_ebean.*"/></Match>
		<Match><Field name="~_ebean.*"/></Match>
		<Bug code="SnVI" />
		<Bug code="SA" />
	</FindBugsFilter>
)

findbugsReportType := Some(FindBugsReportType.Html)

findbugsReportPath := Some(crossTarget.value / "findbugs" / "report.html")

//
// Conflict classes
//
conflictClassExcludes ++= Seq(
  "LICENSE",
  "reference.conf"
)

buildInfoOptions += BuildInfoOption.BuildTime
