package com.broadcom.spectrum;

/**
*
* @author Brad Watson
*/

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.broadcom.apm.WilyMetricReporter;
import com.broadcom.http.HTTPWorker;

public class EPAgent {
	
	private static String address = "";
	private static String modelUrl = "";
	private static String user = "";
	private static String password = "";
	private static String attributes = "";
	private static String metricPath = "";
	private static Properties props;
	private static ArrayList<String> models;
	private static ArrayList<String> metrics;
	private static boolean debug;
	
	public static void main(String[] args) {
		getProps();
		getModels();
		loopQueries();
		printMetrics();
	}
	
	private static void printMetrics() {
		for(String metric : metrics) {
			printMetric(metric);
		}
	}

	private static void loopQueries() {
		metrics = new ArrayList<>();
		Properties headers = new Properties();
		headers.put("Accept", "application/json");
		for(String model : models) {
			String url = address.replace("{MODEL}", model).replace("{ATTRIBUTES}", attributes);
			if(debug) {
				System.out.println(url);
			}
			addMetricsFromJSON(getJSON(url));
		}
	}
	
	private static void getModels() {
		models = new ArrayList<>();
		String json = getJSON(modelUrl.replace("{MODEL}", props.getProperty("spectrum.api.model")));
		if(debug) {
			System.out.println(json);
		}
		JSONArray ja = new JSONObject(json).getJSONObject("association-response-list").getJSONObject("association-responses").getJSONArray("association");
		for (int x = 0; x < ja.length(); x++) {
			JSONObject jO = ja.getJSONObject(x);
			models.add(jO.optString("@rightmh"));
		}
	}
	
	private static String getJSON(String url) {
		Properties headers = new Properties();
		headers.put("Accept", "application/json");
		return HTTPWorker.getUrlAsText(url, 60000, user, password, null, null, headers);
	}
	
	private static void printMetric(String metric) {
		String type = "IntCounter";
		String split[] = metric.split(":");
		String name = split[0];
		String value = split[1];
		WilyMetricReporter.reportMetric(type, metricPath + "|" + name + ":Status", value);
	}
    
    public static void addMetricsFromJSON(String json) {
    	if(debug) {
    		System.out.println(json);
    	}
        JSONArray ja = new JSONObject(json).getJSONObject("model-response-list").getJSONObject("model-responses").getJSONObject("model").getJSONArray("attribute");
        try {
        	String device = "";
        	int status = 0;
            for (int x = 0; x < ja.length(); x++) {
            	JSONObject jO = ja.getJSONObject(x);
            	if(jO.optString("@id").equalsIgnoreCase("0x1006e")) {
            		device = jO.optString("$");
            	} else {
            		status = Integer.parseInt(jO.optString("$"));
            	}
            }
            metrics.add(device + ":" + status);
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private static void getProps() {
    	models = new ArrayList<>();
        props = new Properties();
        
        /**
        * Comment/uncomment the next two lines to change between testing locally or using a properties file from the command line.
        */
        String propertiesFile = System.getProperty("props");
        //String propertiesFile = "c:/CAWILY/epagent/epaplugins/spectrum/spectrum.properties";
        
        try {
            props.load(new FileInputStream(propertiesFile));
        } catch (FileNotFoundException ex) {
        	ex.printStackTrace();
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
        for(int x = 0; x < 100; x++) {
            if(props.containsKey("spectrum.device." + x)) {
            	models.add(props.getProperty("spectrum.device." + x));
            }
        }
        address = props.getProperty("spectrum.api.url");
        modelUrl = props.getProperty("spectrum.api.model.url");
        user = props.getProperty("spectrum.api.user");
        password = props.getProperty("spectrum.api.password");
        attributes = props.getProperty("spectrum.api.attributes");
        metricPath = props.getProperty("spectrum.metric.path");
        if(props.containsKey("spectrum.api.debug")) {
        	debug = Boolean.parseBoolean(props.getProperty("spectrum.api.debug"));
        } else {
        	debug = false;
        }
    }

}
