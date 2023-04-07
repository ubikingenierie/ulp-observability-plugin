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

#### Completed tasks

- Synchronization with plugin configuration
- HTML page with metrics correctly displays multi-axis charts for each type of metric
- Summary of total metrics at the bottom of the page

#### Screenshots

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


### Roadmap

See [our repository issues](https://github.com/ubikingenierie/ulp-observability-plugin/issues?q=is%3Aopen+is%3Aissue+milestone%3A1.0.0)


### Want to contribute

Read our [Contributor documentation](contributer-jumpstart.md)