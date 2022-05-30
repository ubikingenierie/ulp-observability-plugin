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
	
    /**
     * Webapp resources dir location.
     */
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
    private Integer port;
    
    
    /**
     * Creates new Jetty server and initiates with given current 
     * 
     * @param port Jetty server port to use
     * @param metricsRoute Metrics resource route
     * @param webAppRoute Angular web app route
     * @param logFreq Log frequency
     * @param totalLabel Label assigned for total metrics
     * @param logger Metrics logger
     * @throws Exception Init server exception
     */
    public ULPObservabilityServer(
    		Integer port, 
    		String metricsRoute,
    		String webAppRoute,
    		Integer logFreq,
    		String totalLabel,
    		SampleLogger logger
    		) throws Exception {
    	this.port = port;
    	this.server = new Server();
    	this.webAppContext = new WebAppContext();
    	this.metricsHandler = new ServletContextHandler();
    	this.infoHandler = new ServletContextHandler();
    	this.initServer(
    			metricsRoute,
    			webAppRoute,
    			logFreq,
    			totalLabel,
    			logger
    			);
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
    private void initServer(
    		String metricsRoute,
    		String webAppRoute,
    		Integer logFreq,
    		String totalLabel,
    		SampleLogger logger
    		) throws Exception{
    	
        ServerConnector connector = new ServerConnector(server);   
        connector.setPort(this.port);
        server.setConnectors(new Connector[] {connector});	
        
        HandlerCollection handlers = new HandlerCollection();
        
        this.metricsHandler.setContextPath(metricsRoute);
        this.metricsHandler.addServlet(new ServletHolder(new ULPObservabilityMetricsServlet(logger)), "/");
        handlers.addHandler(metricsHandler);
        

        this.infoHandler.setContextPath("/config");
        this.infoHandler.addServlet(
        		new ServletHolder(
        				new ULPObservabilityConfigServlet(
        						metricsRoute,
        						logFreq,
        						totalLabel
        						)
        				), "/");
        handlers.addHandler(infoHandler);
        
        URL webAppDir =
        		Thread.currentThread()
        		.getContextClassLoader()
        		.getResource(WEBAPP_RESOURCES_LOCATION);
        if (webAppDir == null) {
            throw new Exception("No "+ WEBAPP_RESOURCES_LOCATION +" directory was found in the JAR file");
        }
        
        webAppContext.setContextPath(webAppRoute);
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