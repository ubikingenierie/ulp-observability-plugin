# Deploy jar into Github release tag

---

### I Maven release plugin :
	
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

### II Maven release plugin configuration :

Add the maven-release-plugin to the project :
- **In pom.xml :**
```
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-release-plugin</artifactId>
  <version>3.0.0-M6</version>
</plugin>
```

For the required SCM information, the Maven POM offers a dedicated section to configure it :


- **In pom.xml :**
```
<scm>  
	<developerConnection>
		scm:git:https://github.com/[organization]/[repository].git
	</developerConnection>
</scm>
```

Authenticating with the git protocol requires a SSH key. In the context of a CI pipeline, this is not desirable. That is why we are using the **http** protocol.
This requires credentials in the form of a user/password pair written in ***$HOME/.m2/settings.xml***
Each credentials pair requires a unique server identifier.

- **In settings.xml :**
```  
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>[server id]</id>
      <username>[github username]</username>
      <password>[Personal access token]</password>
    </server>
  </servers>
</settings>
```

Personal access tokens (PATs) are an alternative to using passwords for authentication to GitHub. 
  Follow this quick guide to generate a PAT :
https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token
**Make sure to check the permissions**
Then copy-paste it in ***<password>***


- **In pom.xml**
```
<properties>
  <project.scm.id>github</project.scm.id>  
</properties>
```
To configure a Maven project to use a specific server, in pom.xml, add a property with the **project.scm.id** key and the server id as the value.

The project will identify and use the github server configured on the settings file **[server id]**. They must be the same.



- **In pom.xml**
```
<distributionManagement>
	<repository>
	  <id>github</id>
	  <name>Releases</name>
	  <url>https://maven.pkg.github.com/ubikingenierie/ulp-observability-plugin</url>
	</repository>
</distributionManagement>
```

Calling the ***release:perform*** goal launches a Maven fork that runs the deploy phase.
In the context of this project, the artifact is a JAR, and the registry, GitHub.
This translates into the configuration snippet above.

The root of GitHub registry is at https://maven.pkg.github.com
 
 
 ---

### III Github action :

 Github action files are located in ***/.github/workflows/***

 #### There are two action files :

 * **build.yml** is used to build the project everytime there is a push on the *develop* branch. 
 * **release.yml** will run mvn release:prepare and mvn release:perform thanks to the configuration of the maven plugin. It will also release the jar-with-dependencies in the Github tags. This workflow have to be started manualy in Github.
 
#### Actions used these two files :

**actions/checkout@v3 :**

 * This action checks-out your repository under $GITHUB_WORKSPACE, so your workflow can access it.

**actions/setup-java@v3 :**
 * This action provides the following functionality for GitHub Actions runners:
   * Downloading and setting up a requested version of Java.
   * Configuring runner for publishing using Apache Maven.
   * Caching dependencies managed by Apache Maven

**fregante/setup-git-user :**
  * This action sets generic git user and email to enable commiting. New commits and tags will be assigned to the @actions user

**WyriHaximus/github-action-get-previous-tag :**
* GitHub Action that gets the latest tag from Git.
  ***set "fetch-depth: 0" in actions/checkout@v3 to make it work.***

**ncipollo/release-action :**
* This action will upload an artifact to a GitHub release.
  ***use "github-action-get-previous-tag" output to set the "tag" input of this action***

 **Github_token :**

 * At the start of each workflow run, GitHub automatically creates a unique ***GITHUB_TOKEN*** secret to use in your workflow. You can use the GITHUB_TOKEN to authenticate in a workflow run.
 Before each job begins, GitHub fetches an installation access token for the job. The GITHUB_TOKEN expires when a job finishes.
 You can use the GITHUB_TOKEN by using the standard syntax for referencing secrets: ```${{ secrets.GITHUB_TOKEN }}```

 ---
 
 #### links
 
 Maven release plugin documentation
 https://maven.apache.org/maven-release/maven-release-plugin/
 Github action documentation
 https://docs.github.com/en/actions/learn-github-actions/understanding-github-actions

 Action setup git user
 https://github.com/fregante/setup-git-user
 Action get previous tag
 https://github.com/WyriHaximus/github-action-get-previous-tag
 Action release artifact 
 https://github.com/ncipollo/release-action


 Managing Maven releases with GitHub Actions (tutorials)
 https://statusneo.com/ci-cd-with-github-x-apache-maven/
 https://blog.frankel.ch/github-actions-maven-releases/


 #### Tips

 To manualy set tag a version (ex : 1.0.0) on local, run : 
 ```mvn versions:set -DnewVersion=1.0.0-SNAPSHOT```

 To publish jar files into **Github package** set the following property in the parent pom :
 ```<maven.deploy.skip>false</maven.deploy.skip>``` 
 