maven-release-plugin


<scm>
    <connection>scm:git:${project.scm.url}</connection>
    <developerConnection>scm:git:${project.scm.url}</developerConnection>
    <url>git@github.com:ubikingenierie/ulp-observability-plugin.git</url>
    <tag>${project.version}</tag>
</scm>



<distributionManagement>
	<repository>
	  <id>github</id>
	  <name>Releases</name>
	  <url>https://maven.pkg.github.com/ubikingenierie/ulp-observability-plugin</url>
	</repository>
 </distributionManagement>
 
 
 
 
 .m2/settings.xml
 
 
 
 <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>github</id>
      <username>tibodirou</username>
      <privateKey>~/.ssh/id_ed25519</privateKey>
      <password>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</password>
    </server>
  </servers>
</settings>