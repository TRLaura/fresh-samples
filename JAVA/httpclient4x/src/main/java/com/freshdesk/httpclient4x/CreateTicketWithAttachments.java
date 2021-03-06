package com.freshdesk.httpclient4x;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.AuthSchemeBase;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;


public class CreateTicketWithAttachments {
    public int createTicketWithAttachments(String apiToken, String apiEndpoint) throws IOException, URISyntaxException {
        final HttpClientBuilder hcBuilder = HttpClientBuilder.create();
        final RequestBuilder reqBuilder = RequestBuilder.post();
        final RequestConfig.Builder rcBuilder = RequestConfig.custom();
        
        // URL object from API endpoint:
        URL url = new URL(apiEndpoint + "/api/v2/tickets");
        final String urlHost = url.getHost();
        final int urlPort = url.getPort();
        final String urlProtocol = url.getProtocol();
        reqBuilder.setUri(url.toURI());
        
        // Authentication:
        List<String> authPrefs = new ArrayList<>();
        authPrefs.add(AuthSchemes.BASIC);
        rcBuilder.setTargetPreferredAuthSchemes(authPrefs);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(urlHost, urlPort, AuthScope.ANY_REALM),
                new UsernamePasswordCredentials(apiToken, "X"));
        hcBuilder.setDefaultCredentialsProvider(credsProvider);
        AuthCache authCache = new BasicAuthCache();
        AuthSchemeBase authScheme = new BasicScheme();
        authCache.put(new HttpHost(urlHost, urlPort, urlProtocol), authScheme);
        HttpClientContext hccContext = HttpClientContext.create();
        hccContext.setAuthCache(authCache);
        
        // Body:
        MultipartEntityBuilder meb = MultipartEntityBuilder.create();
        meb.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        
        meb.addTextBody("email", "email@yourdomain.com");
        meb.addTextBody("subject", "Ticket title");
        meb.addTextBody("description", "Ticket description.");
        meb.addTextBody("status", "2");
        meb.addTextBody("priority", "1");
        
        File attach1 = new File("src/test/resources/a.txt");
        File attach2 = new File("src/test/resources/b.txt");
        
        meb.addBinaryBody("attachments[]", attach1,
                ContentType.TEXT_PLAIN.withCharset("utf-8"), attach1.getName());
        meb.addBinaryBody("attachments[]", attach2,
                ContentType.TEXT_PLAIN.withCharset("utf-8"), attach2.getName());
        reqBuilder.setEntity(meb.build());
        
        // Execute:
        RequestConfig rc = rcBuilder.build();
        reqBuilder.setConfig(rc);
        
        HttpClient hc = hcBuilder.build();
        HttpUriRequest req = reqBuilder.build();
        HttpResponse response = hc.execute(req, hccContext);
        
        // Print out:
        HttpEntity body = response.getEntity();
        InputStream is = body.getContent();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("utf-8")));
        String line;
        StringBuilder sb = new StringBuilder();
        while((line=br.readLine())!=null) {
            sb.append(line);
        }
        int response_status = response.getStatusLine().getStatusCode();
        String response_body = sb.toString();

        System.out.println("Response Status: "+ response_status);
        System.out.println("Body:\n");
        System.out.println(response_body);
        if(response_status > 400) {
            System.out.println("X-Request-Id: " + response.getFirstHeader("x-request-id").getValue());
        }
        else if(response_status==201){ 
            //For creation response_status is 201 where are as for other actions it is 200
            try{
                System.out.println("Ticket Creation Successfull");
                //Creating JSONObject for the response string
                JSONObject response_json = new JSONObject(sb.toString());
                System.out.println("Ticket ID: " + response_json.get("id"));
                System.out.println("Location : " + response.getFirstHeader("location").getValue());
            }
            catch(JSONException e){
                System.out.println("Error in JSON Parsing\n :"+ e);
            }
        }       
        return response_status;
    }
}
