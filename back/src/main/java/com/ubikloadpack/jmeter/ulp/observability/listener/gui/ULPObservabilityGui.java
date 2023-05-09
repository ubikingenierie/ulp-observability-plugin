package com.ubikloadpack.jmeter.ulp.observability.listener.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubikloadpack.jmeter.ulp.observability.config.ULPODefaultConfig;
import com.ubikloadpack.jmeter.ulp.observability.listener.ULPObservabilityListener;

import net.miginfocom.swing.MigLayout;

/**
 * ULP Observability GUI class
 * Extends AbstractListenerGui for basic JMeter listener GUI features
 *
 */
public class ULPObservabilityGui extends AbstractListenerGui{

	
	private static final long serialVersionUID = 1808039838820473713L;
	
	/**
	 * Debug logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ULPObservabilityGui.class);

    private static final String PLUGIN_WIKI_PAGE = "https://www.ubik-ingenierie.com/blog/ubik-load-pack-observability-plugin/";
    
    /**
     * The prefix of the resource bundle properties files (localized under /resources folder in /i18n folder)
     */
    private static final String BUNDLE_BASE_NAME = "gui_labels";
	
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
	 * Micrometer Expiry Time
	 */
	private final JTextField micrometerExpiryTime = new JTextField();
	/**
	 * Log frequency value
	 */
	private final JTextField logFrequency = new JTextField();
	/**
	 * Label field to denote total metrics
	 */
	private final JTextField totalLabel = new JTextField();
	/**
	 * Regex field to filter processed samplers
	 */
	private final JTextField regex = new JTextField();
	/**
	 * Number of registry task threads
	 */
	private final JTextField threadSize = new JTextField();
	/**
	 * Sample result queue capacity
	 */
	private final JTextField bufferCapacity = new JTextField();
	/**
	 * Keep server running after test
	 */
	private final JCheckBox keepJettyServerUpAfterTestEnd = new JCheckBox("Keep server up after test ended");
	

    /**
     * Creates new ULP Observability GUI
     */
	public ULPObservabilityGui() {
	    this.init();
	}
	
    /**
     * Initiates GUI with title and config panels
     */
    private void init() {
    	super.setLayout(new BorderLayout());
	    super.setBorder(makeBorder());
	    super.add(makeTitlePanel(), BorderLayout.NORTH);
	    super.add(createSamplerConfigPanel(),BorderLayout.CENTER);
	    super.add(createHelpLinkPanel(PLUGIN_WIKI_PAGE), BorderLayout.SOUTH);
    }
    
    /**
     * Create new ULP Observability custom config panel
     * 
     * @return 
     */
    private JPanel createSamplerConfigPanel() {
    	JPanel ulpObservabilityConfigPanel = new JPanel(new MigLayout("wrap 2", "[][fill,grow]0px"));
    	ulpObservabilityConfigPanel.setBorder(BorderFactory.createTitledBorder(this.getResourceBundleString("config")));
    	
    	List<Pair<JLabel, JTextField>> labelsAndFields = new ArrayList<>();
    	
    	labelsAndFields.add(Pair.of(new JLabel(this.getResourceBundleString("webServerPort")), this.jettyPort));
    	labelsAndFields.add(Pair.of(new JLabel(this.getResourceBundleString("WebRoute")), this.webAppRoute));
    	labelsAndFields.add(Pair.of(new JLabel(this.getResourceBundleString("percentiles1")), this.pct1));
    	labelsAndFields.add(Pair.of(new JLabel(this.getResourceBundleString("percentiles2")), this.pct2));
    	labelsAndFields.add(Pair.of(new JLabel(this.getResourceBundleString("percentiles3")), this.pct3));
    	labelsAndFields.add(Pair.of(new JLabel(this.getResourceBundleString("regexFilter")), this.regex));
    	labelsAndFields.add(Pair.of(new JLabel(this.getResourceBundleString("openMetricsRoute")), this.metricsRoute));
    	labelsAndFields.add(Pair.of(new JLabel(this.getResourceBundleString("numberrocessingThreads")), this.threadSize));
    	labelsAndFields.add(Pair.of(new JLabel(this.getResourceBundleString("queueCapacity")), this.bufferCapacity));
    	labelsAndFields.add(Pair.of(new JLabel(this.getResourceBundleString("testDuration")), this.micrometerExpiryTime));
    	labelsAndFields.add(Pair.of(new JLabel(this.getResourceBundleString("logFrequency")), this.logFrequency));
    	labelsAndFields.add(Pair.of(new JLabel(this.getResourceBundleString("totalMetricLabel")), this.totalLabel));
    	
    	for(Pair<JLabel, JTextField> labelAndField : labelsAndFields) {
    		JLabel label = labelAndField.getLeft();
    		JTextField textField = labelAndField.getRight();
    		label.setLabelFor(textField);
    		ulpObservabilityConfigPanel.add(label);
    		ulpObservabilityConfigPanel.add(textField);
    	}
    	ulpObservabilityConfigPanel.add(this.keepJettyServerUpAfterTestEnd);
    	
    	return ulpObservabilityConfigPanel;
    }
    
    @Override
 	public String getLabelResource() {
 		return ULPODefaultConfig.pluginName();
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
	
	/**
	 * Check the listener configuration percentiles are in the correct format
	 */
	private Integer validatePercentile(String text, Integer currentValue, String parameter) {
		
		int pctValue = validateNumeric(text,currentValue);
		
		if(pctValue > 100 || pctValue < 0) {
			LOG.error("{} must contain only values between 0 and 100. Found {}",parameter,text);
			return currentValue;
		}
		return pctValue;
	}
	
	/**
	 * Check the listener configuration parameter is numeric
	 */
	private Integer validateNumeric(String text, Integer currentValue) {
		
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
	        LOG.error("Must be a number", e);
	        return currentValue;
	    }
	}
	
	/**
	 * Check the listener configuration parameter is greater than 0
	 */
	private Integer validatePositiveNumeric(String text, Integer currentValue, String parameter) {
		int inputNumber = validateNumeric(text,currentValue);
		if(!(inputNumber>=1)) {
			LOG.error("{} must be greater than 0", parameter);
			return currentValue;
		}
		return inputNumber;
	}
	
	/**
	 * Check if the listener configuration routes are in the correct format
	 */
	private String validateRoute(String text, String currentValue) {
		if(!text.startsWith("/")) {
			LOG.error("Route must start with '/'");
			return currentValue;
		}
		if(text.equals("/config")) {
			LOG.error("Route /config is reserved for plugin configuration route");
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
			ULPObservabilityListener observabilityListener = (ULPObservabilityListener) element;
			
			if(this.metricsRoute.getText().equals(this.webAppRoute.getText())) {
				LOG.error("Jetty Metrics and Web App routes must not be equal");
			} else {
				observabilityListener.setMetricsRoute(validateRoute(metricsRoute.getText(),observabilityListener.getMetricsRoute()));
				observabilityListener.setWebAppRoute(validateRoute(webAppRoute.getText(),observabilityListener.getWebAppRoute()));	
			}
			
			if(this.totalLabel.getText().isBlank()) {
				LOG.error("Invalid label");
			} else {
				observabilityListener.setTotalLabel(this.totalLabel.getText());
			}
			
			observabilityListener.setRegex(this.regex.getText());
			
			keepJettyServerUpAfterTestEnd.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent ae) {
	            	observabilityListener.setKeepJettyServerUpAfterTestEnd(keepJettyServerUpAfterTestEnd.isSelected());
	            }
	        });
			
			observabilityListener.setJettyPort(validateNumeric(jettyPort.getText(), observabilityListener.getJettyPort()));
			observabilityListener.setThreadSize(validatePositiveNumeric(threadSize.getText(), observabilityListener.getThreadSize(),"thread size"));
			observabilityListener.setBufferCapacity(validatePositiveNumeric(bufferCapacity.getText(), observabilityListener.getBufferCapacity(),"buffer capacity"));
			observabilityListener.setPct1(validatePercentile(pct1.getText(), observabilityListener.getPct1(),"percentile 1"));
			observabilityListener.setPct2(validatePercentile(pct2.getText(), observabilityListener.getPct2(),"percentile 2"));
			observabilityListener.setPct3(validatePercentile(pct3.getText(), observabilityListener.getPct3(),"percentile 3"));
			observabilityListener.setMicrometerExpiryTimeInSeconds(micrometerExpiryTime.getText());
			observabilityListener.setLogFreq(validatePositiveNumeric(logFrequency.getText(), observabilityListener.getLogFreq(),"log frequency"));
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
			this.micrometerExpiryTime.setText(ulpObservabilityListener.getMicrometerExpiryTimeInSeconds());
			this.logFrequency.setText(Integer.toString(ulpObservabilityListener.getLogFreq()));
			this.totalLabel.setText(ulpObservabilityListener.getTotalLabel());
			this.regex.setText(ulpObservabilityListener.getRegex());
		}
	}

    /**
     * Set all config parameters to their default values
     */
    @Override
    public void clearGui() {
		super.clearGui();
		this.jettyPort.setText(Integer.toString(ULPODefaultConfig.jettyServerPort()));
		this.metricsRoute.setText(ULPODefaultConfig.jettyMetricsRoute());
		this.webAppRoute.setText(ULPODefaultConfig.jettyWebAppRoute());
		this.threadSize.setText(Integer.toString(ULPODefaultConfig.threadSize()));
		this.bufferCapacity.setText(Integer.toString(ULPODefaultConfig.bufferCapacity()));
		this.pct1.setText(Integer.toString(ULPODefaultConfig.pct1()));
		this.pct2.setText(Integer.toString(ULPODefaultConfig.pct2()));
		this.pct3.setText(Integer.toString(ULPODefaultConfig.pct3()));
		this.micrometerExpiryTime.setText(Integer.toString(ULPODefaultConfig.micrometerExpiryTimeInSeconds()));
		this.logFrequency.setText(Integer.toString(ULPODefaultConfig.logFrequency()));
		this.totalLabel.setText(ULPODefaultConfig.totalLabel());
		this.regex.setText(ULPODefaultConfig.regex());
	}
    
    /**
     * Create the Help link panel
     *
     * @param panel    - supposed to be result of makeTitlePanel()
     * @param helpPage wiki page name, or full URL in case of external wiki
     * @return original panel
     * @see AbstractJMeterGuiComponent
     */
    public Component createHelpLinkPanel(String helpPage) {
        JLabel icon = new JLabel();
        icon.setIcon(new javax.swing.ImageIcon(ULPObservabilityGui.class.getResource("/com/ubikloadpack/jmeter/ulp/observability/information.png")));

        JLabel link = new JLabel(this.getResourceBundleString("helpMe"));
        link.setForeground(Color.blue);
        link.setFont(link.getFont().deriveFont(Font.PLAIN));
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new URIOpener(helpPage));
        Border border = BorderFactory.createMatteBorder(0, 0, 1, 0, java.awt.Color.blue);
        link.setBorder(border);

        JLabel version = new JLabel("");
        version.setFont(version.getFont().deriveFont(Font.PLAIN).deriveFont(11F));
        version.setForeground(Color.GRAY);

        JPanel panelLink = new JPanel(new GridBagLayout());

        GridBagConstraints gridBagConstraints;

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 0);
        panelLink.add(icon, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 3, 0);
        panelLink.add(link, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        panelLink.add(version, gridBagConstraints);
        return panelLink;
    }
    
    private static class URIOpener extends MouseAdapter {

        private final String uri;

        public URIOpener(String aURI) {
            uri = aURI;
        }

        public static void openInBrowser(String string) {
            if (java.awt.Desktop.isDesktopSupported()) {
                try {
                    java.awt.Desktop.getDesktop().browse(java.net.URI.create(string));
                } catch (IOException ignored) {
                    LOG.debug("Failed to open in browser", ignored);
                }
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                openInBrowser(uri);
            }
        }
    }
    
    /**
     * Get the value of the corresponding property in the bundle resources 
     * prefixed with RESOURCE_BUNDLE_PREFIX. The property value is retrieved 
     * based on the JVM's default locale.
     * @param key the key of a resource bundle
     * @return the value of the property.
     */
    private String getResourceBundleString(String key) {
    	return this.getResourceBundleString(BUNDLE_BASE_NAME, Locale.getDefault(), key);
    }
    
    /**
     * Get the value of the corresponding property in the bundle resources.
     * @param baseName The prefix of the resource bundle files.
     * @param locale the locale in which the resource bundle will be returned.
     * @param key the key of the resource bundle.
     * @return the string for the given key from this resource bundle.
     */
    private String getResourceBundleString(String baseName, Locale locale, String key) {
    	ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
    	return bundle.getString(key);
    }
}
