# ULP OBSERVABILITY PLUGIN

Ulp observability plugin is an extension for Jmeter which will allow to display more detailed metrics on load tests (implemented and executed by Jmeter)

## Metrics to display:
- Name of the Sampler used for the load test
- Number of requests
- % Error
- Average time
- Percentiles 1 (aggregate_rpt_pct1 property)
- Percentiles 2 (aggregate_rpt_pct2 property)
- Percentiles 3 (aggregate_rpt_pct3 property)
- Max time
- Throughput in req/s

## Constraints:

- See the lightest possible memory / CPU impact on the JMeter side
- Ability to handle up to 1 million requests per minute
- Select a Javascript graphing library that is free and Open Source compatible or paid but in One Shot only
- Develop the solution in Plugin
- Must not block the caller
- Use a light server in terms of memory / CPU consumption (Embedded Jetty)
- Java 11 compatible code


## Technologies and dependencies:
##### Back
- [Java](https://www.java.com/) : Language used by Jmeter
- [Embedded Jetty](https://www.baeldung.com/jetty-embedded) : to expose the metrics collected by SamplerListener (specific to Jmeter) and calculated by HDRHistogram offers a very light server implementation
- [Micrometer](https://micrometer.io/) : to save samples and calculate more metrics
- [CronScheduler](https://github.com/TimeAndSpaceIO/CronScheduler/) : for a clock drift insensitive task scheduler
- [openapi-generator-maven-plugin](https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-maven-plugin) : to generate APIs and models
- [Jackson](https://github.com/FasterXML/jackson) : for API response serialization
- [Junit](https://www.jmdoudoux.fr/java/dej/chap-junit.htm) : Unit tests

###### Completed tasks
- Multithreaded sample processing
- Custom sampler configuration
- Metrics logging
- Exposing metrics in OpenMetrics format with the Jetty server
- Exposing an HTML page with metric charts
-Javadoc

###### HMI

<p align="center">
<img src=screenshot/ulp_observability1.png><br/>
<em>Observability Listener JMeter Control Panel</em> 
<br/>
<br/>
<img src=screenshot/ulp_observability2.png><br/>
<em>Example of metrics summary in non-graphical mode</em>
<br/>
<br/>
<img src=screenshot/ulp_observability5.png><br/>
<em>Sample response from Jetty server for sample metrics in OpenMetrics format</em>
</p>


##### Front
- [TypeScript](https://www.typescriptlang.org/) : to take advantage of typing for more rigor and data consistency 
- [AngularJs](https://reactjs.org/): for the front and graphing part:
         - States and hooks to facilitate data manipulation (Metrics)
         - npm repo for various dependencies
         - ChartJs available
         - Material UI for elegant rendering
- [ChartJs](https://www.npmjs.com/package/chart.js?activeTab=readme) : Library used a lot, maintained (last update on 02/16/2022) and free
- [ng-openapi-gen](https://www.npmjs.com/package/ng-openapi-gen) : generate objects and services
- [Jest](https://jestjs.io/) : Unit testing, popular and preferred by react and angular community

###### Completed tasks
- Synchronization with plugin configuration
- HTML page with metrics correctly displays multi-axis charts for each type of metric
- Summary of total metrics at the bottom of the page

###### IHM

<p align="center">
<img src=screenshot/ulp_observability3.png><br/>
<em>Example of a metric chart</em> <br />
</p>

- Left y-axis: metric of each group of samples
- Right y-axis: cumulative number of threads in each group of samples
- Graphs currently implemented for: average response, maximum response, percentiles, error percentage and throughput

<br />
<p align="center">
<img src=screenshot/ulp_observability4.png><br/>
<em>Example summary of total metrics</em> <br />
</p>

###### To do
- Serialization/deserialization of sample records to a local file
- Apply checkstyle for the back part
- Create plugin build github action
- Packaging the appropriate plugin (`mvn clean install` from the parent project generating a plugin JAR with a webapp from front build)
- Unit tests
- For the front part: add a table of statistics equivalent to the JMeter dashboard (<https://jmeter.apache.org/usermanual/generating-dashboard.html>)
