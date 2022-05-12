package ubikloadpack.jmeter.ulp.observability.server;



import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import jakarta.servlet.http.HttpServlet;
import ubikloadpack.jmeter.ulp.observability.config.ULPObservabilityDefaultConfig;

/**
 * ULP Observability Jetty server with custom servlet (see {@link ubikloadpack.jmeter.ulp.observability.server.ULPObservabilityServlet})
 * 
 * @author Valentin ZELIONII
 *
 */
public class ULPObservabilityServer {
	
    private Server server;
    private ServletContextHandler contextHandler;
    
    /**
     * Port used by Jetty server
     */
    private Integer port;
    
    
    public ULPObservabilityServer() {
    	this.port = ULPObservabilityDefaultConfig.jettyServerPort();
    	this.initServer();
    }
    
    public ULPObservabilityServer(Integer port) {
    	this.port = port;
    	this.initServer();
    	
    }
    
    
    public void initServer() {
    	
    	this.server = new Server();
        ServerConnector connector = new ServerConnector(server);
        

        connector.setPort(this.port);
        server.setConnectors(new Connector[] {connector});	
        this.contextHandler = new ServletContextHandler();
        this.contextHandler.setContextPath("/");
        this.server.setHandler(contextHandler);
          
    }
    
    
    /**
     * Set servlet routing map
     * 
     * @param servlet HttpServlet to route
     * @param routeName Route path
     */
    public void addServletWithMapping(HttpServlet servlet, String routeName) {
    	
    	   this.contextHandler.addServlet(new ServletHolder(servlet), routeName);
    		
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
    	
    	 this.server.stop();
    	 
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