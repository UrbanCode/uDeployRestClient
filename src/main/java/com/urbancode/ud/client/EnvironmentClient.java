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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.urbancode.commons.util.StringUtil;

@SuppressWarnings("deprecation") // Triggered by DefaultHttpClient
public class EnvironmentClient extends UDRestClient {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public EnvironmentClient(URI url, String clientUser, String clientPassword) {
        super(url, clientUser, clientPassword);
    }

    //----------------------------------------------------------------------------------------------
    public EnvironmentClient(URI url, String clientUser, String clientPassword, boolean trustAllCerts) {
        super(url, clientUser, clientPassword, trustAllCerts);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Construct with a supplied HTTP client. See UDRestClient for configuration
     * requirements and restrictions.
     */
    public EnvironmentClient(URI url, DefaultHttpClient client) {
        super(url, client);
    }

    //----------------------------------------------------------------------------------------------
    public void addEnvironmentBaseResource(String application, String environment, String resource)
    throws IOException {
        String uri = url + "/cli/environment/addBaseResource?environment=" +
                encodePath(environment) + "&resource=" +
                encodePath(resource);
        if (!StringUtils.isEmpty(application)) {
            uri = uri + "&application=" +
                    encodePath(application);
        }
        HttpPut method = new HttpPut(uri);
        invokeMethod(method);
    }

    //----------------------------------------------------------------------------------------------
    public void addEnvironmentToTeam(String application, String environment, String team, String type)
    throws IOException {
        String uri = url + "/cli/environment/teams?team=" +
                encodePath(team) + "&type=" +
                encodePath(type) + "&environment=" +
                encodePath(environment);
        if (!StringUtils.isEmpty(application)) {
            uri = uri + "&application=" +
                    encodePath(application);
        }
        HttpPut method = new HttpPut(uri);
        invokeMethod(method);
    }

    //----------------------------------------------------------------------------------------------
    public UUID createEnvironment(
        String applicationName,
        String name,
        String description,
        String color,
        boolean requireApprovals,
        String blueprintName,
        String baseResource)
    throws IOException, JSONException {
        return createEnvironment(
                applicationName,
                name,
                description,
                color,
                requireApprovals,
                blueprintName,
                baseResource,
                null);
    }

    //----------------------------------------------------------------------------------------------
    public UUID createEnvironment(
        String applicationName,
        String name,
        String description,
        String color,
        boolean requireApprovals,
        String blueprintName,
        String baseResource,
        String envProfileName)
    throws IOException, JSONException {
        return createEnvironment(
                applicationName,
                name,
                description,
                color,
                requireApprovals,
                blueprintName,
                baseResource,
                envProfileName,
                null);
    }

    //----------------------------------------------------------------------------------------------
    public UUID createEnvironment(
        String applicationName,
        String name,
        String description,
        String color,
        boolean requireApprovals,
        String blueprintName,
        String baseResource,
        String envProfileName,
        JSONObject nodeProperties)
    throws IOException, JSONException {
        UUID result = null;

        if (StringUtils.isEmpty(blueprintName)) {
            String uri = url + "/cli/environment/createEnvironment?application=" +
                encodePath(applicationName) + "&name=" +
                encodePath(name) + "&description=" +
                encodePath(description) + "&color=" +
                encodePath(color) + "&requireApprovals=" +
                String.valueOf(requireApprovals);
            HttpPut method = new HttpPut(uri);
            HttpResponse response = invokeMethod(method);
            String body = getBody(response);
            result = UUID.fromString(body);
        }
        else {
            JSONObject jsonToSend = new JSONObject();
            jsonToSend.put("name", name);
            jsonToSend.put("description", description);
            jsonToSend.put("application", applicationName);
            jsonToSend.put("blueprint", blueprintName);
            jsonToSend.put("baseResource", baseResource);
            jsonToSend.put("color", color);
            jsonToSend.put("requireApprovals", requireApprovals);
            if (!StringUtil.isEmpty(envProfileName)) {
                jsonToSend.put("envProfileName", envProfileName);
            }
            if (nodeProperties != null) {
                jsonToSend.put("nodeProperties", nodeProperties);
            }

            String uri = url + "/cli/environment/provisionEnvironment";
            HttpPut method = new HttpPut(uri);
            method.setEntity(getStringEntity(jsonToSend));

            HttpResponse response = invokeMethod(method);
            String body = getBody(response);
            JSONObject jsonBody = new JSONObject(body);
            result = UUID.fromString(jsonBody.getString("id"));
        }

        return result;
    }


    //----------------------------------------------------------------------------------------------
    public void createDesiredInventoryEntry(String deploymentRequest, String environmentId, String componentId, String versionId, String status)
    throws IOException, JSONException{
        String uri = url + "/rest/inventory/desiredInventory/entries";

        JSONObject entry = new JSONObject();
        entry.put("environmentId", environmentId);
        entry.put("componentId", componentId);
        entry.put("versionId", versionId);
        entry.put("status", status);

        JSONArray entries = new JSONArray();
        entries.put(entry);

        JSONObject requestBody = new JSONObject();
        requestBody.put("deploymentRequest", deploymentRequest);
        requestBody.put("entries", entries);

        HttpPut method = new HttpPut(uri);
        try {
            method.setEntity(getStringEntity(requestBody));
            invokeMethod(method);
        }
        finally {
            releaseConnection(method);
        }
    }

    //----------------------------------------------------------------------------------------------
    public void deleteEnvironment(
        String applicationName,
        String name)
    throws IOException {
        String uri = url + "/cli/environment/deleteEnvironment?environment=" +
            encodePath(name);
        if (!StringUtils.isEmpty(applicationName)) {
            uri = uri + "&application=" +
                    encodePath(applicationName);
        }
        HttpDelete method = new HttpDelete(uri);
        invokeMethod(method);
    }

    //----------------------------------------------------------------------------------------------
    public Map<String, String> getComponentEnvironmentProperties(String compName, String envName, String appName)
    throws IOException, JSONException {
        //return mapping of name-value pairs
        Map<String, String> result = new HashMap<String, String>();
        if ("".equals(envName) || "".equals(appName)) {
            throw new IOException("a required argument was not supplied");
        }

        String uri = url + "/cli/environment/componentProperties?environment=" +
                encodePath(envName) + "&component=" +
                encodePath(compName);
        if (!StringUtils.isEmpty(appName)) {
            uri = uri + "&application=" +
                    encodePath(appName);
        }
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONArray propsJSON = new JSONArray(body);

        for (int i=0; i<propsJSON.length(); i++) {
            JSONObject propObject = (JSONObject) propsJSON.get(i);
            result.put((String)propObject.get("name"), (String)propObject.get("value"));
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public Map<String, String> getEnvironmentProperties(String envName, String appName)
    throws IOException, JSONException {
        //return mapping of name-value pairs
        Map<String, String> result = new HashMap<String, String>();
        if ("".equals(envName) || "".equals(appName)) {
            throw new IOException("a required argument was not supplied");
        }

        String uri = url + "/cli/environment/getProperties?environment=" +
                encodePath(envName);
        if (!StringUtils.isEmpty(appName)) {
            uri = uri + "&application=" +
                    encodePath(appName);
        }
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONArray propsJSON = new JSONArray(body);

        for (int i=0; i<propsJSON.length(); i++) {
            JSONObject propObject = (JSONObject) propsJSON.get(i);
            result.put((String)propObject.get("name"), (String)propObject.get("value"));
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public UUID getEnvironmentUUID(String environmentName, String applicationName)
    throws IOException, JSONException {
        UUID result = null;

        String uri = url + "/cli/environment/info?environment=" + encodePath(environmentName);
        if (applicationName!= null && !"".equals(applicationName)) {
            uri = uri + "&application=" + encodePath(applicationName);
        }
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONObject resultJSON = new JSONObject(body);
        result = UUID.fromString(resultJSON.getString("id"));

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public JSONObject getEnvironment(String environmentName, String applicationName)
    throws IOException, JSONException {
        String uri = url + "/cli/environment/info?environment=" + encodePath(environmentName);
        if (applicationName!= null && !"".equals(applicationName)) {
            uri = uri + "&application=" + encodePath(applicationName);
        }
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        return new JSONObject(body);
    }

    //----------------------------------------------------------------------------------------------
    public void removeEnvironmentBaseResource(String application, String environment, String resource)
    throws IOException {
        String uri = url + "/cli/environment/removeBaseResource?environment=" +
                encodePath(environment) + "&resource=" +
                encodePath(resource);
        if (!StringUtils.isEmpty(application)) {
            uri = uri + "&application=" +
                    encodePath(application);
        }
        HttpPut method = new HttpPut(uri);
        invokeMethod(method);
    }

    //----------------------------------------------------------------------------------------------
    public String setComponentEnvironmentProperty(
            String componentName, String envName, String appName,
            String name, String value, boolean isSecure)
    throws IOException {
        String result;
        if ("".equals(componentName) || "".equals(name) || "".equals(envName)) {
            throw new IOException("a required argument was not supplied");
        }

        String uri = url + "/cli/environment/componentProperties?component=" +
                encodePath(componentName) + "&environment=" +
                encodePath(envName) + "&name=" +
                encodePath(name) + "&value=" +
                encodePath(value) + "&isSecure=" +
                encodePath(String.valueOf(isSecure));
        if (appName != null && !"".equals(appName)) {
            uri = uri + "&application=" + encodePath(appName);
        }
        HttpPut method = new HttpPut(uri);

        retryInvokeMethod(method, "set component environment property");
        if (isSecure) {
            result = name + "=****";
        }
        else {
            result = name + "=" + value;
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public String setEnvironmentProperty(String envName, String appName, String name, String value, boolean isSecure)
    throws IOException {
        String result;
        if ("".equals(envName) || "".equals(name)) {
            throw new IOException("a required argument was not supplied");
        }

        String uri = url + "/cli/environment/propValue?environment=" +
                encodePath(envName) + "&name=" +
                encodePath(name) + "&value=" +
                encodePath(value) + "&isSecure=" +
                encodePath(String.valueOf(isSecure));

        if (!StringUtils.isEmpty(appName)) {
            uri = uri + "&application=" +
                    encodePath(appName);
        }

        HttpPut method = new HttpPut(uri);

        retryInvokeMethod(method, "set environment property");
        if (isSecure) {
            result = name + "=****";
        }
        else {
            result = name + "=" + value;
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public boolean verifyInventoryStatus(String envName, String appName, String componentName,
            String componentVersion, String inventoryStatus)
    throws IOException, JSONException {
        boolean result = false;
        boolean isID = false;
        try {
            @SuppressWarnings("unused")
            UUID envUUID = UUID.fromString(envName);
            isID = true;
        }
        catch (IllegalArgumentException e) {
            //swallow
        }
        String envID = isID? envName : getEnvironmentUUID(envName, appName).toString();
        String uri = url + "/rest/inventory/desiredInventory/" + encodePath(envID);
        HttpGet method = new HttpGet(uri);

        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONArray responseJSON = new JSONArray(body);
        try {
            for (int i=0; i<responseJSON.length(); i++) {
                JSONObject obj = responseJSON.getJSONObject(i);
                JSONObject componentObj = obj.optJSONObject("component");
                if (componentObj != null && componentObj.getString("name").equals(componentName)) {
                    JSONObject versionObj = obj.optJSONObject("version");
                    if (versionObj != null && versionObj.getString("name").equals(componentVersion)) {
                        JSONObject statusObj = obj.optJSONObject("status");
                        if (statusObj != null && statusObj.getString("name").equals(inventoryStatus)) {
                            result = true;
                            break;
                        }

                    }
                }
            }
        }
        catch(JSONException e) {
            result = false;
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public JSONObject getLatestEnvironmentInventoryByComponent(String envName, String appName, String compName)
    throws IOException, JSONException {
        UUID envId = getEnvironmentUUID(envName, appName);
        String uri = url + "/rest/inventory/versionByEnvironmentAndComponent/"+envId.toString()+"/"+compName;
        HttpGet method = new HttpGet(uri);

        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONObject result = new JSONObject(body);
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public JSONArray deleteRedundantVersions(String envName, String appName,
            String compName, boolean deleteRedundant) throws IOException,
            JSONException {
        if ("".equals(envName) || "".equals(appName) || "".equals(compName)) {
            throw new IOException("a required argument was not supplied");
        }

        UUID envId = getEnvironmentUUID(envName, appName);
        String uri = url + "/cli/environment/" + envId.toString()
                         + "/redundantVersions/" + encodePath(compName)
                         + "?deleteRedundant=" + String.valueOf(deleteRedundant);

        HttpDelete method = new HttpDelete(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONArray versionsJson = new JSONArray(body);

        return versionsJson;
    }

    //----------------------------------------------------------------------------------------------
    public JSONArray getOverlappingArtifacts(String envId, String cmpId, String resourceId, String versionId)
            throws IOException,JSONException {
        if (StringUtils.isEmpty(envId)) {
            throw new IOException("envId was not supplied");
        }
        if (StringUtils.isEmpty(cmpId)) {
            throw new IOException("cmpId was not supplied");
        }
        if (StringUtils.isEmpty(resourceId)) {
            throw new IOException("resourceId was not supplied");
        }
        if (StringUtils.isEmpty(versionId)) {
            throw new IOException("versionId was not supplied");
        }

        String uri = url + "/cli/environment/"
                         + envId
                         + "/" + cmpId
                         + "/" + resourceId
                         + "/overlappingArtifacts/" + versionId;
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONArray overlappingArtifactJsonArray = new JSONArray(body);

        return overlappingArtifactJsonArray;
    }

    //----------------------------------------------------------------------------------------------
    public JSONArray deleteAllVersions(String envName, String appName,
            String compName, boolean deleteVersions) throws IOException,
            JSONException {
        if ("".equals(envName) || "".equals(appName) || "".equals(compName)) {
            throw new IOException("a required argument was not supplied");
        }

        UUID envId = getEnvironmentUUID(envName, appName);
        String uri = url + "/cli/environment/" + envId.toString()
                         + "/versions/" + encodePath(compName)
                         + "?deleteVersions=" + String.valueOf(deleteVersions);

        HttpDelete method = new HttpDelete(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONArray versionsJson = new JSONArray(body);

        return versionsJson;
    }
}
