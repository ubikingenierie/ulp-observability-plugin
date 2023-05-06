# UbikLoadPack Observability Plugin for JMeter

[UbikLoadPack](https://UbikLoadPack.com) observability plugin is a **Free** and **Open-Source** plugin for [Apache JMeter](https://jmeter.apache.org) which allows you to monitor a Non GUI/Cli performance test (Standalone or distributed) from your favority browser.

It also exposes an Open Metrics endpoint so that OpenMetrics compatible tools like Prometheus can scrape it and make metrics of your performance test available in these tools.

It is easily installable through [JMeter-Plugins manager](<https://jmeter-plugins.org/?search=observability>):

```
 ./PluginsManagerCMD.sh install ulp-observability-plugin
```

See in this [blog](https://www.ubik-ingenierie.com/blog/update-jmeter-plugins-in-ci-cd-pipeline/) the pre-requisites to install any plugin this way.

## Metrics displayed:

- Name of the Sampler used for the load test
- Number of requests
- % Error
- Average time
- Percentiles 1 (aggregate_rpt_pct1 property)
- Percentiles 2 (aggregate_rpt_pct2 property)
- Percentiles 3 (aggregate_rpt_pct3 property)
- Max time
- Throughput in req/s

## JMeter Plugin configuration

[Blog showing installation procedure](https://www.ubik-ingenierie.com/blog/ubik-load-pack-observability-plugin/)

### Screenshots

<p align="center">
<img src=screenshot/ulp_observability1.png><br/>
<em>Observability Listener JMeter Control Panel</em> 
<br/>

### Logs by the plugin in console

<br/>
<img src=screenshot/ulp_observability2.png><br/>
<em>Example of metrics summary in non-graphical mode</em>
<br/>

### Browser view

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


## Connecting the plugin to Prometheus

![Legende](screenshot/Prometheus.png)

### Blog

[Blog showing how to connect Prometheus to plugin](https://www.ubik-ingenierie.com/blog/ubik-load-pack-observability-plugin-connect-the-plugin-to-prometheus/)

### Install Prometheus

- If you haven't installed prometheus on your device yet, 
[Download the latest release of Prometheus](https://prometheus.io/download/) for your platform, then extract it.
- If you use Windows, add the extracted directory to the PATH env variable.
- If you use Linux, add it to the $PATH variable. Add it to the .bashrc file to make it permanent.

### Configure Prometheus

Inside the directory of your JMeter test plan, add the following prometheus.yml file :

```yml
global:
  scrape_interval:     15s # By default, scrape targets every 15 seconds.

  # Attach these labels to any time series or alerts when communicating with
  # external systems (federation, remote storage, Alertmanager).
  external_labels:
    monitor: 'codelab-monitor'

# A Configuration containing Observability plugin endpoint to scrape:

scrape_configs:
  - job_name: 'observability-plugin'
    scrape_interval: 5s
    metrics_path: /ulp-o-metrics

    static_configs:
      - targets: ['localhost:9090']
```

Change 'localhost:9090' above to match the host:port where plugin is running and listening.

### Endpoint content exposed by the plugin

<br/>
<img src=screenshot/ulp_observability5.png><br/>
<em>Sample response from Jetty server for sample metrics in OpenMetrics format</em>
</p>


### Launch Prometheus

- cd to the test plan directory
- then enter this command to start using prometheus (on port 9095, change it with the one you prefer) :

```bash
prometheus  --web.enable-admin-api   --web.listen-address=:9095 --config.file=prometheus.yml
```

- You can then access Prometheus from this [url](http://localhost:9095/graph>)

### Roadmap

See [our repository issues](https://github.com/ubikingenierie/ulp-observability-plugin/issues?q=is%3Aopen+is%3Aissue+milestone%3A1.0.0)

### Want to contribute

Read our [Contributor documentation](contributor-jumpstart.md)