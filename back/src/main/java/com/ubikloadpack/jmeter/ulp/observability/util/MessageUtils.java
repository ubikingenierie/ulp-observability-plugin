/**
 * 
 */
package com.ubikloadpack.jmeter.ulp.observability.util;

import java.util.ResourceBundle;

import org.apache.jmeter.util.JMeterUtils;

/**
 * Bundle resource utility class
 *
 */
public class MessageUtils {
    /**
     * The base name of the resource bundle properties files (localized under /resources folder in /i18n folder)
     */
    private static final String BUNDLE_BASE_NAME = "com.ubikloadpack.jmeter.ulp.observability.i18n.messages";
	
	/**
     * Get the corresponding key value in the bundle resource 
     * whose base name is defined by the BUNDLE_BASE_NAME constant.
     * The key value is retrieved based on the JMeter locale.
     * @param key the key of a resource bundle
     * @return the value of the key.
     */
    public static String getMessage(String key) {
    	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, JMeterUtils.getLocale(), Thread.currentThread().getContextClassLoader());
    	return resourceBundle.getString(key);
    }

}
