package ubikloadpack.jmeter.ulp.observability.listener.gui;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

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


public class ULPObservabilityGui extends AbstractListenerGui{

	
	private static final long serialVersionUID = 1808039838820473713L;
	private static final Logger log = LoggerFactory.getLogger(ULPObservabilityGui.class);
	
	private final JTextField jettyPort = new JTextField();
	private final JTextField metricsEndpoint = new JTextField();
	private final JTextField percentiles1 = new JTextField();
	private final JTextField percentiles2 = new JTextField();
	private final JTextField percentiles3 = new JTextField();
	private final JTextField precision = new JTextField();
	private final JTextField logFrequency = new JTextField();
	private final JLabel metricsData = new JLabel();
	private final JButton setMetricsDataBtn = new JButton("Open"); 
	private final JCheckBox enableDataOutput = new JCheckBox();

	
	public ULPObservabilityGui() {
	    this.init();
	}
	
	
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
	    add(createSamplerListenerPanel(), BorderLayout.CENTER);
    }
    
    
    private JPanel createSamplerListenerPanel() {
    	JPanel ulpObservabilityPanel = new JPanel();
    	
    	ulpObservabilityPanel.setBorder(BorderFactory.createTitledBorder("Config"));
    	GroupLayout layout = new GroupLayout(ulpObservabilityPanel);
    	ulpObservabilityPanel.setLayout(layout);
    	
    	LinkedHashMap<JLabel, Component> layoutMap = new LinkedHashMap<>();
    	layoutMap.put(new JLabel("Jetty Server port"), this.jettyPort);
    	layoutMap.put(new JLabel("Jetty Metrics endpoint"), this.metricsEndpoint);
    	
    	layoutMap.put(new JLabel("Percentiles 1"), this.percentiles1);
    	layoutMap.put(new JLabel("Percentiles 2"), this.percentiles2);
    	layoutMap.put(new JLabel("Percentiles 3"), this.percentiles3);
    	layoutMap.put(new JLabel("Percentiles precision"), this.precision);
    	
    	layoutMap.put(new JLabel("Log Frequency in seconds (0 = off)"), this.logFrequency);
    	
    	layoutMap.put(new JLabel("Enable Sample data output"), this.enableDataOutput);
 
    	ParallelGroup parallelGroup = layout.createParallelGroup(Alignment.LEADING);
    	SequentialGroup sequentialGroup = layout.createSequentialGroup();
    	
    	for(Entry<JLabel,Component> layoutComponent : layoutMap.entrySet()) {
    		parallelGroup = addParallelGroup(layout, parallelGroup, layoutComponent.getKey(), layoutComponent.getValue());
    		sequentialGroup = addSequentialgroup(layout, sequentialGroup, layoutComponent.getKey(), layoutComponent.getValue());
    	}
    	
    	JLabel dataOutputLabel = new JLabel("Sample data output directory");
    	
    	parallelGroup = parallelGroup
    			.addGroup(layout.createSequentialGroup()
	    			.addComponent(dataOutputLabel)
	                .addPreferredGap(ComponentPlacement.RELATED)
	                .addComponent(this.metricsData))
    			.addGroup(layout.createSequentialGroup()
    	    			.addComponent(this.setMetricsDataBtn));
    	
    	sequentialGroup = sequentialGroup
    			.addGroup(layout.createParallelGroup(Alignment.LEADING)
    					.addComponent(dataOutputLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
    					.addComponent(this.metricsData, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE))
    			.addGroup(layout.createParallelGroup(Alignment.LEADING)
    					.addComponent(this.setMetricsDataBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE));
    	
    	layout.setHorizontalGroup(parallelGroup);
    	layout.setVerticalGroup(sequentialGroup);;
    	
    	
    	return ulpObservabilityPanel;
    }
    
    
    
    private ParallelGroup addParallelGroup(GroupLayout layout, ParallelGroup parallelGroup, JLabel label, Component component) {
    	return parallelGroup.addGroup(layout.createSequentialGroup()
    			.addComponent(label)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(component)
                );
    }
    
    private SequentialGroup addSequentialgroup(GroupLayout layout, SequentialGroup sequentialGroup, JLabel label, Component component) {
    	return sequentialGroup.addGroup(layout.createParallelGroup(Alignment.LEADING)
    			.addComponent(label, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
	            .addComponent(component, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
	            );
    }

    
    @Override
 	public String getLabelResource() {
 		return ULPObservabilityDefaultConfig.PLUGIN_NAME;
 	}
 	
 	
 	@Override
 	public String getStaticLabel() {
 	    return getLabelResource();
 	}
    
 	
	public TestElement createTestElement() {
		
		ULPObservabilityListener ulpObservabilityListener = new ULPObservabilityListener();
		configureTestElement(ulpObservabilityListener);
		return ulpObservabilityListener;
	}
	

    @Override
	public void modifyTestElement(TestElement element) {
		super.configureTestElement(element);
		if(element instanceof ULPObservabilityListener) {
			ULPObservabilityListener sampler = (ULPObservabilityListener) element;
			sampler.setMetricsEndpoint(this.metricsEndpoint.getText());
			sampler.setMetricsData(this.metricsData.getText());
			sampler.dataOutputEnabled(this.enableDataOutput.isSelected());
			
			try {
				sampler.setJettyPort(Integer.parseInt(this.jettyPort.getText()));
			} catch (NumberFormatException e) {
		        log.error("Jetty Post must be a number", e);
		    }
			
			try {
				sampler.setPct1(Integer.parseInt(this.percentiles1.getText()));
			} catch (NumberFormatException e) {
		        log.error("Percentiles 1 must be a number", e);
		    }
			
			try {
				sampler.setPct2(Integer.parseInt(this.percentiles2.getText()));
			} catch (NumberFormatException e) {
		        log.error("Percentiles 2 must be a number", e);
		    }
			
			try {
				sampler.setPct3(Integer.parseInt(this.percentiles3.getText()));
			} catch (NumberFormatException e) {
		        log.error("Percentiles 3 must be a number", e);
		    }
			
			try {
				sampler.setPctPrecision(Integer.parseInt(this.precision.getText()));
			} catch (NumberFormatException e) {
		        log.error("Percentiles Precision must be a number", e);
		    }
			
			try {
				sampler.setLogFreq(Integer.parseInt(this.logFrequency.getText()));
			} catch (NumberFormatException e) {
		        log.error("Log Frequency must be a number", e);
		    }
			
		}
	}
    
	

	 @Override
	 public void configure(TestElement testElement) {
		super.configure(testElement);

		if (testElement instanceof ULPObservabilityListener) {
			ULPObservabilityListener ulpObservabilityListener = (ULPObservabilityListener) testElement;
			this.jettyPort.setText(Integer.toString(ulpObservabilityListener.getJettyPort()));
			this.metricsEndpoint.setText(ulpObservabilityListener.getMetricsEndpoint());
			this.percentiles1.setText(Integer.toString(ulpObservabilityListener.getPct1()));
			this.percentiles2.setText(Integer.toString(ulpObservabilityListener.getPct2()));
			this.percentiles3.setText(Integer.toString(ulpObservabilityListener.getPct3()));
			this.precision.setText(Integer.toString(ulpObservabilityListener.getPctPrecision()));
			this.logFrequency.setText(Integer.toString(ulpObservabilityListener.getLogFreq()));
			this.enableDataOutput.setSelected(ulpObservabilityListener.dataOutputEnabled());
			this.metricsData.setText(new java.io.File(ulpObservabilityListener.getMetricsData()).getAbsolutePath());
		}

	}

	
	
    @Override
    public void clearGui() {
		super.clearGui();
		this.jettyPort.setText(Integer.toString(ULPObservabilityDefaultConfig.JETTY_SERVER_PORT));
		this.metricsEndpoint.setText(ULPObservabilityDefaultConfig.METRICS_ENDPOINT_NAME);
		this.percentiles1.setText(Integer.toString(ULPObservabilityDefaultConfig.PCT1));
		this.percentiles2.setText(Integer.toString(ULPObservabilityDefaultConfig.PCT2));
		this.percentiles3.setText(Integer.toString(ULPObservabilityDefaultConfig.PCT3));
		this.precision.setText(Integer.toString(ULPObservabilityDefaultConfig.NBR_SIGNIFICANT_DIGITS));
		this.logFrequency.setText(Integer.toString(ULPObservabilityDefaultConfig.LOG_FREQUENCY));
		this.metricsData.setText(new java.io.File(ULPObservabilityDefaultConfig.METRIC_DATA).getAbsolutePath());
		this.enableDataOutput.setSelected(ULPObservabilityDefaultConfig.ENABLE_DATA_OUTPUT);
	}

	

	
}

