
To manualy set tag version on local : 
	mvn versions:set -DnewVersion=1.0.1-SNAPSHOT
	
		
	
The Maven Release plugin manages the release process. It provides two complementary goals, prepare and perform. They do the following :	
	
mvn release:prepare
- Change the version in the POM from x-SNAPSHOT to a new version
- Transform the SCM information in the POM to include the final destination of the tag
- Commit the modified POM
- Tag the code in the SCM with a version name
- Bump the version in the POM to a new value y-SNAPSHOT
- Commit the modified POM

mvn release:perform

- Checkout from an SCM URL with optional tag
- Run the predefined Maven deploy goal	


For the required SCM information :
<scm>  
	<developerConnection>
		scm:git:https://github.com/[organization]/[repository].git
	</developerConnection>
</scm>

Authenticating with the git protocol requires a SSH key. In the context of a CI pipeline, this is not desirable. That is why we are using the http protocol.
This requires credentials in the form of a user/password pair written in $HOME/.m2/settings.xml
Each credentials pair requires a unique server identifier.

Content of settings.xml :

<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>[server id]</id>
      <username>[github username]</username>
      <password>[Github personal access token]</password>
    </server>
  </servers>
</settings>





To configure a Maven project to use a specific server, in pom.xml, add a property with the project.scm.id key and the server’s id as the value.

<properties>
  <project.scm.id>github</project.scm.id>  
</properties>

The project will use the github server configured on the settings file


Calling the release:perform goal launches a Maven fork that runs the deploy phase.
In the context of this project, the artifact is a JAR, and the registry, GitHub.
This translates into the following configuration snippet:

<distributionManagement>
	<repository>
	  <id>github</id>
	  <name>Releases</name>
	  <url>https://maven.pkg.github.com/ubikingenierie/ulp-observability-plugin</url>
	</repository>
</distributionManagement>
 
 
 
 
 
