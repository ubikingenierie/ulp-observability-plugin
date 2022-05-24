package ubikloadpack.jmeter.ulp.observability.listener.gui;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ubikloadpack.jmeter.ulp.observability.config.ULPObservabilityDefaultConfig;
import ubikloadpack.jmeter.ulp.observability.listener.ULPObservabilityListener;


/**
 * ULP Observability GUI class
 * Extends AbstractListenerGui for basic JMeter listener GUI features
 *
 */
public class ULPObservabilityGui extends AbstractListenerGui{

	
	private static final long serialVersionUID = 1808039838820473713L;
	private static final Logger log = LoggerFactory.getLogger(ULPObservabilityGui.class);
	
	/**
	 * Jetty server port
	 */
	private final JTextField jettyPort = new JTextField();
	/**
	 * Metrics resource route
	 */
	private final JTextField metricsRoute = new JTextField();
	/**
	 * Angular web app route
	 */
	private final JTextField webAppRoute = new JTextField();
	/**
	 * First percentile score value
	 */
	private final JTextField pct1 = new JTextField();
	/**
	 * Second percentile score value
	 */
	private final JTextField pct2 = new JTextField();
	/**
	 * Third percentile score value
	 */
	private final JTextField pct3 = new JTextField();
	/**
	 * Percentile precision value
	 */
	private final JTextField pctPrecision = new JTextField();
	/**
	 * Log frequency value
	 */
	private final JTextField logFrequency = new JTextField();
	/**
	 * Label field to denote total metrics
	 */
	private final JTextField totalLabel = new JTextField();
	/**
	 * Number of registry task threads
	 */
	private final JTextField threadSize = new JTextField();
	/**
	 * Sample result queue capacity
	 */
	private final JTextField bufferCapacity = new JTextField();
	private final JLabel metricsData = new JLabel();
	private final JButton setMetricsDataBtn = new JButton("Open"); 
	private final JCheckBox enableDataOutput = new JCheckBox();

	
	public ULPObservabilityGui() {
	    this.init();
	}
	
    /**
     * Initiates GUI with title and config panels
     */
    private void init() {
    	
    	this.setMetricsDataBtn.addActionListener(new ActionListener() {
    	
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle("Set Sample Data Output Directory");
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setCurrentDirectory(new java.io.File(metricsData.getText()));
				fileChooser.setAcceptAllFileFilterUsed(false);
				int option = fileChooser.showOpenDialog(getPrintableComponent());
				if(option == JFileChooser.APPROVE_OPTION){
		               metricsData.setText(fileChooser.getSelectedFile().getAbsolutePath());
		            }
			}
		});
    	super.setLayout(new BorderLayout());
	    super.setBorder(makeBorder());
	    super.add(makeTitlePanel(), BorderLayout.NORTH);
	    super.add(createSamplerConfigPanel(),BorderLayout.CENTER);
    }
   
    
    
    /**
     * Create new ULP Observability custom config panel
     * 
     * @return 
     */
    private JPanel createSamplerConfigPanel() {
    	JPanel ulpObservabilityConfigPanel = new JPanel();
    	
    	ulpObservabilityConfigPanel.setBorder(BorderFactory.createTitledBorder("Config"));
    	GroupLayout layout = new GroupLayout(ulpObservabilityConfigPanel);
    	ulpObservabilityConfigPanel.setLayout(layout);
    	
    	List<List<Component>> componentGroups = new ArrayList<>();
    	
    	
    	componentGroups.add(
    			new ArrayList<Component>(Arrays.asList(new JLabel("Jetty Server port"), this.jettyPort))
    			);
    	componentGroups.add(
    			new ArrayList<Component>(Arrays.asList(new JLabel("Jetty Metrics route"), this.metricsRoute))
    			);
    	componentGroups.add(
    			new ArrayList<Component>(Arrays.asList(new JLabel("Jetty Wep Application route"), this.webAppRoute))
    			);
    	componentGroups.add(
    			new ArrayList<Component>(Arrays.asList(new JLabel("Number of Processing Threads"), this.threadSize))
    			);
    	componentGroups.add(
    			new ArrayList<Component>(Arrays.asList(new JLabel("Sample Queue Buffer Capacity"), this.bufferCapacity))
    			);
    	componentGroups.add(
    			new ArrayList<Component>(Arrays.asList(new JLabel("Percentiles 1"), this.pct1))
    			);
    	componentGroups.add(
    			new ArrayList<Component>(Arrays.asList(new JLabel("Percentiles 2"), this.pct2))
    			);
    	componentGroups.add(
    			new ArrayList<Component>(Arrays.asList(new JLabel("Percentiles 3"), this.pct3))
    			);
    	componentGroups.add(
    			new ArrayList<Component>(Arrays.asList(new JLabel("Percentiles precision"), this.pctPrecision))
    			);
    	componentGroups.add(
    			new ArrayList<Component>(Arrays.asList(new JLabel("Log Frequency in seconds"), this.logFrequency))
    			);
    	componentGroups.add(
    			new ArrayList<Component>(Arrays.asList(new JLabel("Total metrics label"), this.totalLabel))
    			);
//    	componentGroups.add(
//    			new ArrayList<Component>(Arrays.asList(new JLabel("Enable Sample data output"), this.enableDataOutput))
//    			);
//    	componentGroups.add(
//    			new ArrayList<Component>(Arrays.asList(new JLabel("Sample data output directory"), this.metricsData))
//    			);
//    	componentGroups.add(
//    			new ArrayList<Component>(Arrays.asList(this.setMetricsDataBtn))
//    			);

 
    	ParallelGroup parallelGroup = layout.createParallelGroup(Alignment.LEADING);
    	SequentialGroup sequentialGroup = layout.createSequentialGroup();
    	
    	for(List<Component> componentGroup : componentGroups) {
    		addSequentialgroup(layout, parallelGroup, componentGroup);
    		addParallelGroup(layout, sequentialGroup, componentGroup);
    	}
    
    	
    	layout.setHorizontalGroup(parallelGroup);
    	layout.setVerticalGroup(sequentialGroup);;
    	
    	
    	return ulpObservabilityConfigPanel;
    }
    
    
    private void addSequentialgroup(GroupLayout layout, ParallelGroup parallelGroup, List<Component> components) {
    	SequentialGroup newGroup = layout.createSequentialGroup();
    	for(Component c : components) {
    		newGroup = newGroup.addComponent(c).addPreferredGap(ComponentPlacement.RELATED);
    	
    	}
    	parallelGroup.addGroup(newGroup);
    }
    
    
    private void addParallelGroup(GroupLayout layout, SequentialGroup sequentialGroup, List<Component> components) {
    	ParallelGroup newGroup = layout.createParallelGroup(Alignment.LEADING);
    	for(Component c : components) {
    		newGroup = newGroup.addComponent(c, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE);
    	}
    	sequentialGroup.addGroup(newGroup);
    }

    
    @Override
 	public String getLabelResource() {
 		return ULPObservabilityDefaultConfig.pluginName();
 	}
 	
 	
 	@Override
 	public String getStaticLabel() {
 	    return getLabelResource();
 	}
    
 	
	/**
	 * Create and configure new ULP Observability listener (see {@link ubikloadpack.jmeter.ulp.observability.listener.ULPObservabilityListener})
	 */
	public TestElement createTestElement() {
		ULPObservabilityListener ulpObservabilityListener = new ULPObservabilityListener();
		configureTestElement(ulpObservabilityListener);
		return ulpObservabilityListener;
	}
	
	
	private Integer validateNumeric(String text, Integer currentValue) {
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
	        log.error("Must be a number", e);
	        return currentValue;
	    }
	}
	
	private String validateRoute(String text, String currentValue) {
		if(!text.startsWith("/")) {
			log.error("Route must start with '/'");
			return currentValue;
		}
		if(text.equals("/config")) {
			log.error("Route /config is reserved for plugin configuration route");
			return currentValue;
		}
		return text;	
	}

    /**
     * Validate and modify listener configuration
     */
    @Override
	public void modifyTestElement(TestElement element) {
		super.configureTestElement(element);
		if(element instanceof ULPObservabilityListener) {
			ULPObservabilityListener sampler = (ULPObservabilityListener) element;
			sampler.setMetricsData(this.metricsData.getText());
			sampler.dataOutputEnabled(this.enableDataOutput.isSelected());
			
			if(this.metricsRoute.getText().equals(this.webAppRoute.getText())) {
				log.error("Jetty Metrics and Web App routes must not be equal");
			} else {
				sampler.setMetricsRoute(validateRoute(metricsRoute.getText(),sampler.getMetricsRoute()));
				sampler.setWebAppRoute(validateRoute(webAppRoute.getText(),sampler.getWebAppRoute()));	
			}
			
			if(this.totalLabel.getText().isBlank()) {
				log.error("Invalid label");
			} else {
				sampler.setTotalLabel(this.totalLabel.getText());
			}
			
			sampler.setJettyPort(validateNumeric(jettyPort.getText(), sampler.getJettyPort()));
			sampler.setThreadSize(validateNumeric(threadSize.getText(), sampler.getThreadSize()));
			sampler.setBufferCapacity(validateNumeric(bufferCapacity.getText(), sampler.getBufferCapacity()));
			sampler.setPct1(validateNumeric(pct1.getText(), sampler.getPct1()));
			sampler.setPct2(validateNumeric(pct2.getText(), sampler.getPct2()));
			sampler.setPct3(validateNumeric(pct3.getText(), sampler.getPct3()));
			sampler.setPctPrecision(validateNumeric(pctPrecision.getText(), sampler.getPctPrecision()));
			sampler.setLogFreq(validateNumeric(logFrequency.getText(), sampler.getLogFreq()));
			
		}
	}
    
	

	 /**
	 * Update config panel in case when parameters inside listener change
	 */
	@Override
	 public void configure(TestElement testElement) {
		super.configure(testElement);

		if (testElement instanceof ULPObservabilityListener) {
			ULPObservabilityListener ulpObservabilityListener = (ULPObservabilityListener) testElement;
			this.jettyPort.setText(Integer.toString(ulpObservabilityListener.getJettyPort()));
			this.metricsRoute.setText(ulpObservabilityListener.getMetricsRoute());
			this.webAppRoute.setText(ulpObservabilityListener.getWebAppRoute());
			this.threadSize.setText(Integer.toString(ulpObservabilityListener.getThreadSize()));
			this.bufferCapacity.setText(Integer.toString(ulpObservabilityListener.getBufferCapacity()));
			this.pct1.setText(Integer.toString(ulpObservabilityListener.getPct1()));
			this.pct2.setText(Integer.toString(ulpObservabilityListener.getPct2()));
			this.pct3.setText(Integer.toString(ulpObservabilityListener.getPct3()));
			this.pctPrecision.setText(Integer.toString(ulpObservabilityListener.getPctPrecision()));
			this.logFrequency.setText(Integer.toString(ulpObservabilityListener.getLogFreq()));
			this.totalLabel.setText(ulpObservabilityListener.getTotalLabel());
			this.enableDataOutput.setSelected(ulpObservabilityListener.dataOutputEnabled());
			this.metricsData.setText(new java.io.File(ulpObservabilityListener.getMetricsData()).getAbsolutePath());
		}

	}

	
	
    /**
     * Set all config parameters to their default values
     */
    @Override
    public void clearGui() {
		super.clearGui();
		this.jettyPort.setText(Integer.toString(ULPObservabilityDefaultConfig.jettyServerPort()));
		this.metricsRoute.setText(ULPObservabilityDefaultConfig.jettyMetricsRoute());
		this.webAppRoute.setText(ULPObservabilityDefaultConfig.jettyWebAppRoute());
		this.threadSize.setText(Integer.toString(ULPObservabilityDefaultConfig.threadSize()));
		this.bufferCapacity.setText(Integer.toString(ULPObservabilityDefaultConfig.bufferCapacity()));
		this.pct1.setText(Integer.toString(ULPObservabilityDefaultConfig.pct1()));
		this.pct2.setText(Integer.toString(ULPObservabilityDefaultConfig.pct2()));
		this.pct3.setText(Integer.toString(ULPObservabilityDefaultConfig.pct3()));
		this.pctPrecision.setText(Integer.toString(ULPObservabilityDefaultConfig.pctPrecision()));
		this.logFrequency.setText(Integer.toString(ULPObservabilityDefaultConfig.logFrequecny()));
		this.totalLabel.setText(ULPObservabilityDefaultConfig.totalLabel());
		this.metricsData.setText(new java.io.File(ULPObservabilityDefaultConfig.metricsData()).getAbsolutePath());
		this.enableDataOutput.setSelected(ULPObservabilityDefaultConfig.enableDataOutput());
	}

	

	
}

