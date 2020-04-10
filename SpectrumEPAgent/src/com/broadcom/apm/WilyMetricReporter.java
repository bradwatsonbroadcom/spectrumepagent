package com.broadcom.apm;

/**
*
* @author Brad Watson
*/

public class WilyMetricReporter {
    
    public static void reportMetric(String type, String name, String value) {
        System.out.println("<metric type=\"" + type + "\" name=\""+ name + "\" value=\"" + value + "\"/>");
    }
    
}
