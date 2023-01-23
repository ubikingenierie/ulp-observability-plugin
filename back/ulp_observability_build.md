1) frontend-maven-plugin :

Install node/npm and then run "ng-build"
The front build is placed in "ulp-observability-front/src/main/front/dist/webapp" as defined in angular.json



2) maven-resources-plugin :

Copy the front build from "ulp-observability-front/src/main/front/dist/webapp" to the back module target directory



3) maven-assembly-plugin (not used anymore):

With its prefabricated descriptor "jar-with-dependencies" create a single jar containing the back, the front and the dependencies in the target directory of the back module.
