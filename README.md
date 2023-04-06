# ULP OBSERVABILITY PLUGIN

[UbikLoadPack](https://UbikLoadPack.com) observability plugin is an extension for Apache JMeter which allows you to monitor a performance test (Standalone or distributed) from your browser.

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

- Have the lightest possible memory / CPU footprint on JMeter performances
- Ability to handle up to 1 million requests per minute
- Select a Javascript graphing library that is free and Open Source compatible or paid but in One Shot only
- Develop the solution in Plugin
- Must not block the caller
- Use a light server in terms of memory / CPU consumption (Embedded Jetty)
- Java 11 compatible code

## Technologies and dependencies:

### Back
- [Java](https://www.java.com/) : Language used by JMeter
- [Embedded Jetty](https://www.baeldung.com/jetty-embedded) : to expose the metrics collected by SamplerListener (specific to JMeter) and calculated by HDRHistogram offers a very light server implementation
- [Micrometer](https://micrometer.io/) : to save samples and calculate more metrics
- [CronScheduler](https://github.com/TimeAndSpaceIO/CronScheduler/) : for a clock drift insensitive task scheduler
- [openapi-generator-maven-plugin](https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-maven-plugin) : to generate APIs and models
- [Jackson](https://github.com/FasterXML/jackson) : for API response serialization
- [Junit](https://www.jmdoudoux.fr/java/dej/chap-junit.htm) : Unit tests

#### Default configuration
The default properties we get when we create an ULP observability sampler are in the file ULPODefaultConfig.java.
This class provides the defaults, and specify the keys to use if we want to override them in a jmeter property file.

#### How it works
- When a test is started, the method 'testStarted(String host)' from the ULPObservabilityListener class is triggered.
- This method starts a jetty server using the configuration metionned earlier. This server exposes through 2 Servlets (ULPObservabilityConfigServlet, ULPObservabilityMetricsServlet) which are called by the frontend in order to get the data to render.
- Then it creates X threads (X being equal to the Number of Processing Threads in the sampler GUI). These threads are instances of the class MicrometerTask.java.
As long as the test plan is running, the threads are retrieving the samples results from a queue, and add them to a MicrometerRegistry.
- Micrometer is a library used to store the data that are exposed through the servlets. The MicrometerRegistry class is fed with the samplers data from the current time interval defined by Y seconds, (Y being the Log Frequency in seconds).
It calculates the metrics of the current interval as long as it receives datas. Each Y seconds a cron job (LogTask.java) will reset the data which were used to calculates the metrics for the interval. It keeps the interval metrics results in a logger, but cleans everything else.
- The queue on which the threads are retrieving datas is fed by Jmeter each time an http sampler complete its requests, thanks to the sampleOccurred(SampleEvent e) method overrided in ULPObservabilityListener.
- The first servlet, ULPObservabilityConfigServlet, is used to tell the frontend on which url it can get the intervals datas it needs to render + the log frequency -> time in seconds between each calls to the seconds servlets to get more datas to display. It is equal to Y.
- The second servlet, ULPObservabilityMetricsServlet, can gives 2 things :
    - the last interval metrics which has to be dynamically added to the already displayed graphs with Angular
    - every intervals metrics since the start of the test. It is used one time when the front calls for the first time the backend in order to get everything it needs to display. After that, the front only asks for the last interval datas.

#### Completed tasks

- Multithreaded sample processing
- Distributed testing support
- Custom sampler configuration
- Metrics logging
- Metrics available in OpenMetrics format through the Jetty server
- HTML page with metric charts
- Javadoc

#### Screenshots

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


### Front

- [TypeScript](https://www.typescriptlang.org/) : to take advantage of a strongly typed language for more rigor and data consistency 
- [AngularJs](https://angularjs.org/): for the front and graphing part :
         - States and hooks to facilitate data manipulation (Metrics)
         - npm repo for various dependencies
         - ChartJs available
         - Material UI for elegant rendering
- [ChartJs](https://www.npmjs.com/package/chart.js?activeTab=readme) : Library used a lot, maintained (last update on 02/16/2022) and free

#### How it works
Angular application. It starts by asking the backend informations about the refresh rate of the graphs and the url to retrieve graphs datas on the first servlet.
After that, it asks the backend everything it needs to display on the graphs. Then, every Y seconds, it asks the backend the newest intervall datas and update its graphs with it.

### Completed tasks

- Synchronization with plugin configuration
- HTML page with metrics correctly displays multi-axis charts for each type of metric
- Summary of total metrics at the bottom of the page

### Screenshots

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


### [Roadmap](https://github.com/ubikingenierie/ulp-observability-plugin/issues?q=is%3Aopen+is%3Aissue+milestone%3A1.0.0)
