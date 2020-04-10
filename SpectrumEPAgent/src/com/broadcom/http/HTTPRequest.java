package com.broadcom.http;

/**
*
* @author Brad Watson
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

public class HTTPRequest {
    
    private String response;
    private int returnCode;
    private long time;
    private boolean success = false;
       
    public HTTPRequest(String url, int timeout, boolean auth, String user, String password, String host, String domain, Properties headers) {
        if(url.toLowerCase().contains("https://")) {
            https(url, timeout, auth, user, password, host, domain, headers);
        } else {
            http(url, timeout, auth, user, password, host, domain, headers);
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
    
    public void https(String url, int timeout, boolean auth, String user, String password, String host, String domain, Properties headers) {
        try {
            System.setProperty("jsse.enableSNIExtension","false");
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, new TrustStrategy() {
                        
                        @Override
                        public boolean isTrusted(final X509Certificate[] chain, final String type) {
                            return true;
                        }
                    })
                    .useProtocol("TLS")
                    .build();
            
            SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslContext, new AllowAllHostnameVerifier());
            CookieStore store = new BasicCookieStore();
            
            RequestConfig config = null;
            HttpClientContext context = HttpClientContext.create();
            if(auth) {                CredentialsProvider creds = new BasicCredentialsProvider();
                creds.setCredentials(AuthScope.ANY, new NTCredentials(user, password, host, domain));

                context.setCredentialsProvider(creds);

                config = RequestConfig.custom()
                        .setSocketTimeout(timeout)
                        .setConnectTimeout(timeout)
                        .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM))
                        .build();


            } else {
                config = RequestConfig.custom()
                        .setSocketTimeout(timeout)
                        .setConnectTimeout(timeout)
                        .build();
            }
            
            CloseableHttpClient client = HttpClients.custom()
                    .setSSLSocketFactory(factory)
                    .setDefaultCookieStore(store)
                    .build();
                        
            long start = System.currentTimeMillis();
            HttpGet request = new HttpGet(url);
            if(auth) {
                request.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(user, password), "UTF-8", false));
            }
            request.setConfig(config);
            if(null != headers) {
                Enumeration names = headers.propertyNames();
                while(names.hasMoreElements()) {
                    String header = (String) names.nextElement();
                    String value = headers.getProperty(header);
                    request.setHeader(header, value);
                }
            }
            
            String body = client.execute(request, handler, context);
            long stop = System.currentTimeMillis();
            setTime(stop - start);
            setResponse(body);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(HTTPRequest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
            Logger.getLogger(HTTPRequest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyManagementException ex) {
            Logger.getLogger(HTTPRequest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            setSuccess(false);
            setTime(0);
            Logger.getLogger(HTTPRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private final ResponseHandler<String> handler = new ResponseHandler<String>() {

        @Override
        public String handleResponse(final HttpResponse response) throws IOException {
            int status = response.getStatusLine().getStatusCode();
            setReturnCode(status);
            if(status >= 200 && status < 400) {
                setSuccess(true);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            StringBuilder sb = new StringBuilder();
            while((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    };
    
    public void http(String url, int timeout, boolean auth, String user, String password, String host, String domain, Properties headers) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            RequestConfig config = null;
            HttpClientContext context = HttpClientContext.create();
            if(auth) {
                CredentialsProvider creds = new BasicCredentialsProvider();
                creds.setCredentials(AuthScope.ANY, new NTCredentials(user, password, host, domain));
                context.setCredentialsProvider(creds);
                
                config = RequestConfig.custom()
                    .setSocketTimeout(timeout)
                    .setConnectTimeout(timeout)
                    .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM))
                    .build();
            } else {
                config = RequestConfig.custom()
                    .setSocketTimeout(timeout)
                    .setConnectTimeout(timeout)
                    .build();
            }

            long start = System.currentTimeMillis();
            HttpGet request = new HttpGet(url);
            if(auth) {
                request.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(user, password), "UTF-8", false));
            }
            request.setConfig(config);
            if(null != headers) {
                Enumeration names = headers.propertyNames();
                while(names.hasMoreElements()) {
                    String header = (String) names.nextElement();
                    String value = headers.getProperty(header);
                    request.setHeader(header, value);
                }
            }
            
            String body = client.execute(request, handler, context);
            long stop = System.currentTimeMillis();
            setTime(stop - start);
            setResponse(body);
        } catch (IOException ex) {
            setSuccess(false);
            setTime(0);
            Logger.getLogger(HTTPRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
