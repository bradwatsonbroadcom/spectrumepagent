package com.broadcom.http;

/**
*
* @author Brad Watson
*/

import java.util.Properties;

public class HTTPWorker {
    
    public static String getUrlAsText(String url, int timeout, String user, String password, String host, String domain, Properties headers) {
        HTTPRequest request = null;
        if(null != user) {
            request = new HTTPRequest(url, timeout, true, user, password, host, domain, headers);
        } else {
            request = new HTTPRequest(url, timeout, false, user, password, host, domain, headers);
        }
        return request.getResponse();
    }
    
    public static String getUrlAsText(String url, int timeout) {
        return getUrlAsText(url, timeout, null, null, null, null, null);
    }
    
    public static String getUrlAsText(String url) {
        return getUrlAsText(url, 300000, null, null, null, null, null);
    }
}
