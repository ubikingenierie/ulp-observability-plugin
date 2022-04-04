package ubikloadpack.jmeter.ulp.observability.server;



import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.Dispatcher;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.servlet.FilterMapping;

import java.util.EnumSet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.DispatcherType;
import ubikloadpack.jmeter.ulp.observability.config.ULPObservabilityConfig;

public class ULPObservabilityServer {
	
    private Server server;
    private ServletContextHandler contextHandler;
    private Integer port;
    
    
    public ULPObservabilityServer() {
    	this.port = ULPObservabilityConfig.JETTY_SERVER_PORT;
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
    
    
    public void addServletWithMapping(HttpServlet servlet, String routeName) {
    	
    	   this.contextHandler.addServlet(new ServletHolder(servlet), routeName);
    		
    }
    
    
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