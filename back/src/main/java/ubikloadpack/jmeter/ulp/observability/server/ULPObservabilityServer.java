package ubikloadpack.jmeter.ulp.observability.server;



import java.net.URL;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import jakarta.servlet.http.HttpServlet;
import ubikloadpack.jmeter.ulp.observability.config.ULPObservabilityDefaultConfig;

/**
 * ULP Observability Jetty server with custom servlet (see {@link ubikloadpack.jmeter.ulp.observability.server.ULPObservabilityMetricsServlet})
 * 
 * @author Valentin ZELIONII
 *
 */
public class ULPObservabilityServer {
	
	private final Boolean ENABLE_APP = true;
	
    private static final String WEBAPP_RESOURCES_LOCATION = "webapp";
	private Server server;
    private ServletContextHandler metricsHandler;
    private ServletContextHandler infoHandler;
    private WebAppContext webAppContext;
    private String metricsRoute;
    private String webAppRoute;
    private Integer logFreq;
    
    
    private Integer port;
    
    
    public ULPObservabilityServer() throws Exception {
    	this(
    			ULPObservabilityDefaultConfig.jettyServerPort(),
    			ULPObservabilityDefaultConfig.jettyMetricsRoute(),
    			ULPObservabilityDefaultConfig.jettyWebAppRoute(),
    			ULPObservabilityDefaultConfig.logFrequecny()
    		);
    }
    
    public ULPObservabilityServer(Integer port, String metricsRoute, String webAppRoute, Integer logFreq) throws Exception {
    	this.port = port;
    	this.metricsRoute = metricsRoute;
    	this.webAppRoute = webAppRoute;
    	this.logFreq = logFreq;
    	this.initServer();
    }
    
    
    public void initServer() throws Exception{
    	
    	this.server = new Server();
        ServerConnector connector = new ServerConnector(server);   
        connector.setPort(this.port);
        server.setConnectors(new Connector[] {connector});	
        
        HandlerCollection handlers = new HandlerCollection();
        
        this.metricsHandler = new ServletContextHandler();
        this.metricsHandler.setContextPath(this.metricsRoute);
        handlers.addHandler(metricsHandler);
        
        this.infoHandler = new ServletContextHandler();
        this.infoHandler.setContextPath("/info");
        this.infoHandler.addServlet(new ServletHolder(new ULPObservabilityInfoServlet(this.metricsRoute, this.logFreq)), "/");
        handlers.addHandler(infoHandler);
        
        if(ENABLE_APP) {
        	this.webAppContext = new WebAppContext();
            URL webAppDir = Thread.currentThread().getContextClassLoader().getResource(WEBAPP_RESOURCES_LOCATION);
            if (webAppDir == null) {
                throw new Exception(String.format("No %s directory was found in the JAR file", WEBAPP_RESOURCES_LOCATION));
            }
            webAppContext.setContextPath(this.webAppRoute);
    		webAppContext.setResourceBase(webAppDir.toURI().toString());
    		webAppContext.setParentLoaderPriority(true);
    		handlers.addHandler(webAppContext);
        }
        
        handlers.addHandler(new DefaultHandler());
        
        this.server.setHandler(handlers);
        this.server.setStopAtShutdown(true);
          
    }
    
    
    /**
     * Set servlet routing map
     * 
     * @param servlet HttpServlet to route
     * @param routeName Route path
     */
    public void addServletWithMapping(HttpServlet servlet) {
    	   this.metricsHandler.addServlet(new ServletHolder(servlet), "/");
    		
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
    
    public void stop() throws Exception {
    	if(this.server.isStarted()) {
    		this.server.stop();
    	}
    }

	public Server getServer() {
		
		return server;
	}


	public Integer getPort() {
		
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}
    
    

 }