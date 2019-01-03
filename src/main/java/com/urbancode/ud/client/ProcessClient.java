/**
 * (c) Copyright IBM Corporation 2011, 2017.
 * (c) Copyright HCL Technologies Ltd. 2018. All Rights Reserved.
 * This is licensed under the following license.
 * The Apache Version 2.0 License (https://www.apache.org/licenses/LICENSE-2.0.txt)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */
package com.urbancode.ud.client;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@SuppressWarnings("deprecation") // Triggered by DefaultHttpClient
public class ProcessClient extends UDRestClient {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public ProcessClient(URI url, String clientUser, String clientPassword) {
        super(url, clientUser, clientPassword);
    }

    //----------------------------------------------------------------------------------------------
    public ProcessClient(URI url, String clientUser, String clientPassword, boolean trustAllCerts) {
        super(url, clientUser, clientPassword, trustAllCerts);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Construct with a supplied HTTP client. See UDRestClient for configuration
     * requirements and restrictions.
     */
    public ProcessClient(URI url, DefaultHttpClient client) {
        super(url, client);
    }

    //----------------------------------------------------------------------------------------------
    public String getGenericProcessResult(String processID, int timeoutInMinutes)
    throws IOException, JSONException, InterruptedException, RemoteException {
        String result = "";
        String workflowTraceID = getWorkflowTraceID(processID);
        String uri = url + "/rest/workflow/workflowTrace/" + encodePath(workflowTraceID);

        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);

        long pollInterval = 1000L;
        long timeoutInterval = timeoutInMinutes * 60L * 1000L;
        long start = System.currentTimeMillis();
        boolean found = false;

        while (!found) {
            if (System.currentTimeMillis() - start > timeoutInterval) {
                throw new RemoteException("Timeout waiting for generic process to finish");
            }
            discardBody(response);
            Thread.sleep(pollInterval);
            method = new HttpGet(uri);
            response = invokeMethod(method);
            String body = getBody(response);
            JSONObject jsonResult = new JSONObject(body);
            JSONObject rootActivity = (JSONObject) jsonResult.get("rootActivity");
            String workflowStatus = (String) rootActivity.get("state");
            if (workflowStatus.equalsIgnoreCase("CLOSED") || workflowStatus.equalsIgnoreCase("COMPLETED")) {
                result = (String) jsonResult.get("result");
                found = true;
            }
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public JSONArray getProcessRequestProperties(String processId)
    throws IOException, JSONException {
        JSONArray result;
        String uri = url + "/rest/process/request/" +
                encodePath(processId) + "/properties";
        HttpGet method = new HttpGet(uri);

        HttpResponse response = invokeMethod(method);
        result = new JSONArray(getBody(response));
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public String getWorkflowTraceID(String processID)
    throws IOException, JSONException {
        String result;
        String uri = url + "/rest/process/request/" + encodePath(processID);
        HttpGet method = new HttpGet(uri);

        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONObject jsonResult = new JSONObject(body);
        result = (String) jsonResult.get("workflowTraceId");
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public UUID requestGenericProcess(String processId, String processVersion, String resource,
            Properties properties)
    throws IOException, JSONException {
        UUID result;
        JSONObject jsonToSend = new JSONObject();
        jsonToSend.put("processId", processId);
        if (processVersion != null) {
            jsonToSend.put("processVersion", processVersion);
        }
        JSONObject propsJson = new JSONObject();
        if (properties != null) {
            for (String propName : properties.stringPropertyNames()) {
                propsJson.put(propName, properties.getProperty(propName));
            }
        }
        jsonToSend.put("resource", resource);
        jsonToSend.put("properties", propsJson);

        String uri = url + "/rest/process/request";
        HttpPost method = new HttpPost(uri);
        method.setEntity(getStringEntity(jsonToSend));
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONObject jsonResult = new JSONObject(body);
        result = UUID.fromString((String) jsonResult.get("id"));

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void setProcessRequestProperty(String processId, String name, String value, boolean isSecure)
    throws IOException, JSONException {
        if (StringUtils.isEmpty(processId)) {
            throw new IOException("processId was not supplied");
        }
        if (StringUtils.isEmpty(name)) {
            throw new IOException("name was not supplied");
        }

        String uri = url + "/rest/process/request/" +
                encodePath(processId) + "/saveProperties";

        JSONArray props = new JSONArray();
        props.put(createNewPropertyJSON(name, value, isSecure));

        HttpPut method = new HttpPut(uri);
        method.setEntity(getStringEntity(props));
        invokeMethod(method);
    }
}
