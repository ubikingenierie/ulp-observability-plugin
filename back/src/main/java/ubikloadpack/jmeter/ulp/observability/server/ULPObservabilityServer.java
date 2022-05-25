package ubikloadpack.jmeter.ulp.observability.server;



import java.net.URL;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import ubikloadpack.jmeter.ulp.observability.config.PluginConfig;
import ubikloadpack.jmeter.ulp.observability.config.ULPObservabilityDefaultConfig;
import ubikloadpack.jmeter.ulp.observability.log.SampleLogger;

/**
 * ULP Observability Jetty server with custom metrics and plugin config servlets 
 * (see {@link ubikloadpack.jmeter.ulp.observability.server.ULPObservabilityMetricsServlet} and {@link ubikloadpack.jmeter.ulp.observability.server.ULPObservabilityConfigServlet})
 * 
 * @author Valentin ZELIONII
 *
 */
/**
 * @author vazelionii
 *
 */
public class ULPObservabilityServer {
	
    private static final String WEBAPP_RESOURCES_LOCATION = "webapp";
	/**
	 * Jetty server instance
	 */
	private Server server;
    /**
     * Metrics servlet context handler
     */
    private ServletContextHandler metricsHandler;
    /**
     * Plugin config servlet context handler
     */
    private ServletContextHandler infoHandler;
    /**
     * Angular web app context handler
     */
    private WebAppContext webAppContext;
    /**
     * Jetty server port
     */
    private final Integer port;
    
    /**
     * Creates new Jetty server and initiates with default values and unbound empty logger 
     * 
     * @throws Exception Init server exception
     */
    public ULPObservabilityServer() throws Exception {
    	this(new PluginConfig(), new SampleLogger());
    }
    
    
    /**
     * Creates new Jetty server and initiates with given parameters
     * 
     * @param port Jetty server port to use
     * @param metricsRoute Metrics resource route
     * @param webAppRoute Angular web app route
     * @param logFreq Log frequency
     * @param totalLabel Label assigned for total metrics
     * @param logger Metrics logger
     * @throws Exception Init server exception
     */
    public ULPObservabilityServer(PluginConfig pluginConfig, SampleLogger logger) throws Exception {
    	this.port = pluginConfig.getJettyServerPort();
    	this.server = new Server();
    	this.webAppContext = new WebAppContext();
    	this.metricsHandler = new ServletContextHandler();
    	this.infoHandler = new ServletContextHandler();
    	this.initServer(pluginConfig,logger);
    }
    
    
    /**
     * Initiates Jetty server with all available routes
     * 
     * @param metricsRoute
     * @param webAppRoute
     * @param logFreq
     * @param totalLabel
     * @param logger
     * @throws Exception
     */
    private void initServer(PluginConfig pluginConfig, SampleLogger logger) throws Exception{
    	
        ServerConnector connector = new ServerConnector(server);   
        connector.setPort(this.port);
        server.setConnectors(new Connector[] {connector});	
        
        HandlerCollection handlers = new HandlerCollection();
        
        this.metricsHandler.setContextPath(pluginConfig.getMetricsRoute());
        this.metricsHandler.addServlet(new ServletHolder(new ULPObservabilityMetricsServlet(logger)), "/");
        handlers.addHandler(metricsHandler);
        

        this.infoHandler.setContextPath("/config");
        this.infoHandler.addServlet(new ServletHolder(new ULPObservabilityConfigServlet(pluginConfig)), "/");
        handlers.addHandler(infoHandler);
        
        URL webAppDir = Thread.currentThread().getContextClassLoader().getResource(WEBAPP_RESOURCES_LOCATION);
        if (webAppDir == null) {
            throw new Exception(String.format("No %s directory was found in the JAR file", WEBAPP_RESOURCES_LOCATION));
        }
        webAppContext.setContextPath(pluginConfig.getWebAppRoute());
		webAppContext.setResourceBase(webAppDir.toURI().toString());
		webAppContext.setParentLoaderPriority(true);
		handlers.addHandler(webAppContext);

        
        handlers.addHandler(new DefaultHandler());
        
        this.server.setHandler(handlers);
        this.server.setStopAtShutdown(true);
          
    }
    
    
    
    /**
     * Start Jetty server if not already started
     *
     * @throws Exception
     */
    public void start() throws Exception {
    	
    	if(!this.server.isStarted()) {
    		this.server.start();
       	    //this.server.join();
       	    
    	}
    	 
    	 
    }
    
    /**
     * Stop Jetty server if started
     * 
     * @throws Exception
     */
    public void stop() throws Exception {
    	if(this.server.isStarted()) {
    		this.server.stop();
    	}
    }


	public Server getServer() {
		
		return server;
	}


	/**
	 * @return Used Jetty server port
	 */
	public Integer getPort() {
		
		return port;
	}

 }