# Deploy jar into Github release tag

---

### Maven release plugin	
	
The Maven Release plugin manages the release process. It provides two complementary goals, prepare and perform. They do the following :	
	
#### mvn release:prepare
- Change the version in the POM from x-SNAPSHOT to a new version
- Transform the SCM information in the POM to include the final destination of the tag
- Commit the modified POM
- Tag the code in the SCM with a version name
- Bump the version in the POM to a new value y-SNAPSHOT
- Commit the modified POM

#### mvn release:perform

- Checkout from an SCM URL with optional tag
- Run the predefined Maven deploy goal	

---

For the required SCM information, the Maven POM offers a dedicated section to configure it :

&lt;scm>  
	&lt;developerConnection>
		scm:git:https://github.com/[organization]/[repository].git
	&lt;/developerConnection>
&lt;/scm>

Authenticating with the git protocol requires a SSH key. In the context of a CI pipeline, this is not desirable. That is why we are using the **http** protocol.
This requires credentials in the form of a user/password pair written in ***$HOME/.m2/settings.xml***
Each credentials pair requires a unique server identifier.

**Content of settings.xml :**


***&lt;settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              https://maven.apache.org/xsd/settings-1.0.0.xsd">
  &lt;servers>
    &lt;server>
      &lt;id>[server id]&lt;/id>
      &lt;username>[github username]&lt;/username>
      &lt;password>[Github personal access token]&lt;/password>
    &lt;/server>
  &lt;/servers>
&lt;/settings>***




To configure a Maven project to use a specific server, in pom.xml, add a property with the **project.scm.id** key and the server id as the value.


***&lt;properties>
  &lt;project.scm.id>github&lt;/project.scm.id>  
&lt;/properties>***

The project will use the github server configured on the settings file **[server id]**. They must be the same.


Calling the ***release:perform*** goal launches a Maven fork that runs the deploy phase.
In the context of this project, the artifact is a JAR, and the registry, GitHub.
This translates into the following configuration snippet:

&lt;distributionManagement>
	&lt;repository>
	  &lt;id>github</id>
	  &lt;name>Releases</name>
	  &lt;url>https://maven.pkg.github.com/ubikingenierie/ulp-observability-plugin</url>
	&lt;/repository>
&lt;/distributionManagement>

The root of GitHub registry is at https://maven.pkg.github.com
 
 
 ---

### Github action

 Github action files are located in ***/.github/workflows/***

 There are two files :

 * **build.yml** is used to build the project everytime there is a push on the *develop* branch. 
 * **release.yml** will run mvn release:prepare and mvn release:perform thanks to the configuration of the maven plugin. It will also release the jar-with-dependencies in the Github tags. This workflow have to be started manualy in github.
 
  

 
 #### links
 
 Maven release plugin
 https://maven.apache.org/maven-release/maven-release-plugin/

 Github action documentation
 https://docs.github.com/en/actions/learn-github-actions/understanding-github-actions

 Action get previous tag
 https://github.com/WyriHaximus/github-action-get-previous-tag

 Action release artifact 
 https://github.com/ncipollo/release-action

 Managing Maven releases with GitHub Actions (tutorial)
 https://statusneo.com/ci-cd-with-github-x-apache-maven/


 #### Tips

 To manualy set tag version on local, run : 
	***mvn versions:set -DnewVersion=1.0.0-SNAPSHOT***

 To publish jar files into **Github package** set the property 
 ***&lt;maven.deploy.skip>false&lt;/maven.deploy.skip>*** in the parent pom.