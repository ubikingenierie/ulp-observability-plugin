package apache.jmeter.ulpobservability.listener.gui;


import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apache.jmeter.ulpobservability.listener.ULPObservabilityListener;
import apache.jmeter.ulpobservability.listener.config.ULPObservabilityConfig;


public class ULPObservabilityGui extends AbstractListenerGui{

	
	private static final Logger log = LoggerFactory.getLogger(ULPObservabilityListener.class);
	
	// The fields to display on the Gui during sampling
	private final JTextField port = new JTextField();
	private final JTextField nbrRequests = new JTextField();
	private final JTextField nbrSuccess = new JTextField();
	private final JTextField nbrErrors = new JTextField();
	
	
	public ULPObservabilityGui() {
		
	    this.init();
		  
	}
	

	/**
	 * initiate the Gui for the plugin
	 */
    private void init() {
    	
    	super.setLayout(new BorderLayout());
	    super.setBorder(makeBorder());
	    super.add(makeTitlePanel(), BorderLayout.NORTH);
	    add(createSamplerListenerPanel(), BorderLayout.CENTER);
    }

    
   /**
    * create a custom panel for extra text, inputs, fields, comments and so on
    */
    public JPanel createSamplerListenerPanel() {
    	return null;
    }
	
	
	
   /**
	* pass the data from the sampler Listener to the Gui
	*/
	@Override
	public void configure(TestElement testElement) {
		super.configure(testElement);
		if (testElement instanceof ULPObservabilityListener) {
			   ULPObservabilityListener ulpObservabilityListener = (ULPObservabilityListener) testElement;
			   /**
			   label.setText(ulpObservabilityListener.getLabel());
			   responseCode.setText(ulpObservabilityListener.getResponseCode());
			   responseTime.setText(ulpObservabilityListener.getResponseTime().toString());
			   success.setSelected(ulpObservabilityListener.getSuccessful());
			   **/
		}

	}
	
	
	/**
	 * pass data from Gui to the sampler Listener (ULPObservabilityListener)
	 */
    public void modifyTestElement(TestElement element) {
		
		
		
	}
    
    
	/**
	 * create and instance of testElement in this case ULPObservabilityListener
	 */
	public TestElement createTestElement() {
		
		/**
		ULPObservabilityListener ulpObservabilityListener = new ULPObservabilityListener();

		ulpObservabilityListener.setProperty(TestElement.GUI_CLASS, ULPObservabilityGui.class.getName());
		ulpObservabilityListener.setProperty(TestElement.TEST_CLASS, ULPObservabilityGui.class.getName());
		this.modifyTestElement(ulpObservabilityListener);
		//ulpObservabilityListener.setCollectorConfigs(defaultCollectors());
		return ulpObservabilityListener;
		**/
		return null;
	}
	

	

   /**
	* set the displayed name for the plugin on Jmeter 
	*/
	public String getLabelResource() {
		
		return ULPObservabilityConfig.PLUGIN_NAME;
	}
	
	
   /**
	* set the static name for the plugin on Jmeter 
	*/
	@Override
	public String getStaticLabel() {
	    return getLabelResource();
	}

	
	
	/**
	 * clearing Gui after closing or recreating a new one
	 */
	public void clearData() {
		
		
		
	}
	

	
	

}

