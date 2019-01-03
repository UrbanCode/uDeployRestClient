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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@SuppressWarnings("deprecation") // Triggered by DefaultHttpClient
public class ResourceClient extends UDRestClient {
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public ResourceClient(URI url, String clientUser, String clientPassword) {
        super(url, clientUser, clientPassword);
    }

    //----------------------------------------------------------------------------------------------
    public ResourceClient(URI url, String clientUser, String clientPassword, boolean trustAllCerts) {
        super(url, clientUser, clientPassword, trustAllCerts);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Construct with a supplied HTTP client. See UDRestClient for configuration
     * requirements and restrictions.
     */
    public ResourceClient(URI url, DefaultHttpClient client) {
        super(url, client);
    }

    //----------------------------------------------------------------------------------------------
    public void addResourceToTeam(String resource, String team, String type)
    throws IOException {
        String uri = url + "/cli/resource/teams?team=" +
                encodePath(team) + "&type=" +
                encodePath(type) + "&resource=" +
                encodePath(resource);
        HttpPut method = new HttpPut(uri);
        invokeMethod(method);
    }

    //----------------------------------------------------------------------------------------------
    public String addTagToResource(String resourceName, String tagName)
    throws IOException {
        String result = null;

        String uri = url + "/cli/resource/tag?resource=" +
                encodePath(resourceName) + "&tag=" +
                encodePath(tagName);

        HttpPut method = new HttpPut(uri);
        HttpResponse response = invokeMethod(method);
        result = getBody(response);
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void setRoleOnResource(String resource, String role, Map<String, String> properties)
    throws IOException, JSONException{
        String uri = url + "/rest/resource/resource/" + encodePath(resource) +
                     "/role/" + encodePath(role);

        JSONObject propertiesObject = new JSONObject();

        for (Entry<String, String> ent : properties.entrySet()) {
            propertiesObject.put(ent.getKey(), ent.getValue());
        }

        HttpPost method = new HttpPost(uri);
        try {
            method.setEntity(getStringEntity(propertiesObject));
            invokeMethod(method);
        }
        finally {
            releaseConnection(method);
        }
    }

    //----------------------------------------------------------------------------------------------
    public void createResourceInventoryEntry(String deploymentRequest, String resourceId, String componentId, String versionId, String status)
    throws IOException, JSONException{
        String uri = url + "/rest/inventory/entries";

        JSONObject entry = new JSONObject();
        entry.put("resourceId", resourceId);
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
    public String createResource(String name, String agentName, String agentPoolName, String parentName, String role)
    throws IOException, JSONException {
        String result = null;
        JSONObject jsonToSend = new JSONObject();
        jsonToSend.put("name", name);

        if (!StringUtils.isEmpty(parentName)) {
            jsonToSend.put("parent", parentName);
        }
        if (!StringUtils.isEmpty(role)) {
            JSONObject existingComponent = null;
            ComponentClient componentClient = new ComponentClient(url, client);
            try {
                // Need to try to find a component with the provided role name first so we get the correct Resource Role
                // and not an older one with a duplicate name.
                existingComponent = componentClient.getComponent(role);
            }
            catch (IOException e) {
                // Swallow. We couldn't find one.
            }
            if (existingComponent != null) {
                // Use the id of the resource role associated with the existing component
                role = existingComponent.getJSONObject("resourceRole").getString("id");
            }
            jsonToSend.put("role", role);
        }
        if (!StringUtils.isEmpty(agentName)) {
            jsonToSend.put("agent", agentName);
        }
        else if (!StringUtils.isEmpty(agentPoolName)) {
            jsonToSend.put("agentPool", agentPoolName);
        }

        String uri = url + "/cli/resource/create";
        HttpPut method = new HttpPut(uri);
        method.setEntity(getStringEntity(jsonToSend));

        HttpResponse response = invokeMethod(method);
        result = getBody(response);
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public UUID createResourceRole(
        String roleName,
        String description)
    throws IOException, JSONException {
        UUID result;

        String uri = url + "/rest/resource/resourceRole";
        JSONObject resourceObject = new JSONObject();

        resourceObject.put("name", roleName);
        resourceObject.put("description", description);
        HttpPost method = new HttpPost(uri);
        try {
            method.setEntity(getStringEntity(resourceObject));

            HttpResponse response = invokeMethod(method);
            String body = getBody(response);
            result = UUID.fromString(new JSONObject(body).getString("id"));
        }
        finally {
            releaseConnection(method);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public UUID createSubResource(
        UUID parentId,
        String resourceName,
        String description)
    throws IOException, JSONException {
        UUID result;

        String uri = url + "/rest/resource/resource";
        JSONObject resourceObject = new JSONObject();

        resourceObject.put("name", resourceName);
        resourceObject.put("parentId", parentId);
        resourceObject.put("description", description);


        HttpPut method = new HttpPut(uri);
        try {
            method.setEntity(getStringEntity(resourceObject));
            HttpResponse response = invokeMethod(method);
            String body = getBody(response);
            result = UUID.fromString(new JSONObject(body).getString("id"));
        }
        finally {
            releaseConnection(method);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void deleteResource(String name)
    throws IOException {
        String uri = url + "/cli/resource/deleteResource?resource=" +  encodePath(name);
        HttpDelete method = new HttpDelete(uri);
        HttpResponse response = invokeMethod(method);
        discardBody(response);
    }

    //----------------------------------------------------------------------------------------------
    public void deleteResourceInventoryForComponent(String resourceName, String componentName,
            String version, String inventoryStatus)
    throws IOException {
        String uri = url + "/rest/inventory/resourceInventoryForComponent/?resource=" +
                encodePath(resourceName) + "&component=" +
                encodePath(componentName) + "&version=" +
                encodePath(version) + "&status=" +
                encodePath(inventoryStatus);
        HttpDelete method = new HttpDelete(uri);
        HttpResponse response = invokeMethod(method);
        discardBody(response);
    }

    //----------------------------------------------------------------------------------------------
    public JSONObject getLatestVersionByResourceAndComponent(
            String resourceId, String componentId)
    throws Exception {
        JSONObject result = new JSONObject();

        String uri = url + "/rest/inventory/versionByResourceAndComponent/"+
                     encodePath(resourceId) + "/" + encodePath(componentId);
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        result = new JSONObject(body);

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public JSONObject getResourceByPath(String path)
    throws IOException, JSONException {
        JSONObject result = null;

        String uri = url + "/rest/resource/resource/" + encodePath(path);

        HttpGet method = new HttpGet(uri);
        try {
            HttpResponse response = invokeMethod(method);
            String body = getBody(response);
            result = new JSONObject(body);
        }
        finally {
            releaseConnection(method);
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public JSONObject getResourceById(String id)
    throws IOException, JSONException {
        JSONObject result = null;

        String uri = url + "/rest/resource/resource/" + encodePath(id);

        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        result = new JSONObject(body);

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public JSONArray getResourceChildren(String id)
    throws IOException, JSONException {
        JSONArray result = null;

        String uri = url + "/cli/resource/?parent=" + encodePath(id);

        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        result = new JSONArray(body);

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public String getResourceProperty(String resourceName, String name)
    throws IOException {
        String result;
        if ("".equals(resourceName) || "".equals(name)) {
            throw new IOException("a required argument was not supplied");
        }
        String uri = url + "/cli/resource/getProperty?resource=" +
                encodePath(resourceName) + "&name=" +
                encodePath(name);
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        result = getBody(response);
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public JSONObject getResourceRoleByName(String name)
    throws IOException, JSONException {
        JSONObject result = null;

        String uri = url + "/rest/resource/resourceRole/" + encodePath(name);

        HttpGet method = new HttpGet(uri);
        try {
            HttpResponse response = invokeMethod(method);
            String body = getBody(response);
            result = new JSONObject(body);
        }
        finally {
            releaseConnection(method);
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public JSONArray getResourceRoleProperties(String roleName, String resourceName)
    throws IOException, JSONException {
        JSONArray result;
        if ("".equals(roleName) || "".equals(resourceName)) {
            throw new IOException("a required argument was not supplied");
        }
        String uri = url + "/rest/resource/resource/" +
                encodePath(resourceName) + "/propertiesForRole/" +
                encodePath(roleName);
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        result = new JSONArray(getBody(response));

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public String getResourceRolePropertyForResource(String roleName, String resourceName, String name)
    throws IOException, JSONException {
        JSONArray currentProps = getResourceRoleProperties(roleName, resourceName);
        JSONObject propsJSON = convertPropArrayToKeyValuePairs(currentProps);
        return propsJSON.getString(name);
    }

    //----------------------------------------------------------------------------------------------
    public JSONArray getResourceRoles(String resource)
    throws IOException, JSONException {
        JSONArray result = null;

        String uri = url + "/rest/resource/resource/" + encodePath(resource) + "/roles";
        HttpGet method = new HttpGet(uri);

        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        result = new JSONArray(body);
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public List<String> getResourceRolesAsStrings(String resource)
    throws IOException, JSONException {
        List<String> result = new ArrayList<String>();

        String uri = url + "/cli/resource/getRolesForResource?resource=" +
                encodePath(resource);
        HttpGet method = new HttpGet(uri);

        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONArray resultAsJSON = new JSONArray(body);
        for (int i=0; i<resultAsJSON.length(); i++) {
            String roleName = (String) ((JSONObject) resultAsJSON.get(i)).get("name");
            result.add(roleName);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public JSONArray getResourceTree()
    throws IOException, JSONException {
        JSONArray result = null;

        String uri = url + "/rest/resource/resource/tree";

        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        result = new JSONArray(body);

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void inactivateResource(String name)
    throws IOException {
        String uri = url + "/rest/resource/resource/" +  encodePath(name) + "/inactivate";
        HttpPut method = new HttpPut(uri);
        HttpResponse response = invokeMethod(method);
        discardBody(response);
    }

    //----------------------------------------------------------------------------------------------
    public void removeRoleFromResource(String resource, String role)
    throws IOException {
        String uri = url + "/cli/resource/removeRoleFromResource?resource=" +
                encodePath(resource) + "&role=" +
                encodePath(role);
        HttpDelete method = new HttpDelete(uri);
        invokeMethod(method);
    }

    /**
     * Removes named tag for the named resource
     *
     * @throws IOException
     */
    public String removeTagFromResource(String resourceName, String tagName)
            throws IOException {
        String result = null;

        String uri = url + "/cli/resource/tag?resource=" +
                encodePath(resourceName) + "&tag=" +
                encodePath(tagName);

        HttpDelete method = new HttpDelete(uri);
        HttpResponse response = invokeMethod(method);
        result = getBody(response);
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public String setResourceProperty(String resourceName, String name, String value, boolean isSecure)
    throws IOException {
        String result;
        if ("".equals(resourceName) || "".equals(name)) {
            throw new IOException("a required argument was not supplied");
        }

        String uri = url + "/cli/resource/setProperty?resource=" +
                encodePath(resourceName) + "&name=" +
                encodePath(name) + "&value=" +
                encodePath(value) + "&isSecure=" +
                encodePath(String.valueOf(isSecure));

        HttpPut method = new HttpPut(uri);
        invokeMethod(method);
        if (isSecure) {
            result = name + "=****";
        }
        else {
            result = name + "=" + value;
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public String setResourceRoleProperty(String roleName, String resourceName, String name, String value)
    throws IOException, JSONException {
        String result;
        if ("".equals(roleName) || "".equals(resourceName) || "".equals("name")) {
            throw new IOException("a required argument was not supplied");
        }
        String uri = url + "/rest/resource/resource/" +
                encodePath(resourceName) + "/savePropertiesForRole/" +
                encodePath(roleName);
        JSONArray currentProps = getResourceRoleProperties(roleName, resourceName);
        JSONObject jsonToSend = convertPropArrayToKeyValuePairs(currentProps);
        //replace old value with new value
        jsonToSend.put(name, value);
        HttpPut method = new HttpPut(uri);
        method.setEntity(getStringEntity(jsonToSend));

        invokeMethod(method);
        result = name + "=" + value;
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void waitForResourceCreation(String name)
    throws IOException, InterruptedException, RemoteException {
        String uri = url + "/cli/resource/info?resource=" + encodePath(name);
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        int status = response.getStatusLine().getStatusCode();

        long pollInterval = 1000L;
        long timeoutInterval = 10L * 60L * 1000L;
        long start = System.currentTimeMillis();

        while (status == HttpStatus.SC_NOT_FOUND) {
            if (System.currentTimeMillis() - start > timeoutInterval) {
                throw new RemoteException("Timeout waiting for Resource Creation");
            }
            try {
                discardBody(response);
                Thread.sleep(pollInterval);
                response = invokeMethod(method);
            }
            catch (IOException e) {
                //swallow
            }
        }
        if (status != HttpStatus.SC_OK) {
            throw new IOException(String.format("%d %s\n%s",
                    response.getStatusLine().getStatusCode(),
                    response.getStatusLine().getReasonPhrase(),
                    getBody(response)));
        }
    }

    //----------------------------------------------------------------------------------------------
    public JSONArray compareResourceTrees(String leftResourceId, String rightResourceId)
    throws IOException, JSONException {
        String uri = url + "/rest/resource/resource/compare/"+encodePath(leftResourceId)+"/"+encodePath(rightResourceId);
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);

        String body = getBody(response);
        JSONArray result = new JSONArray(body);

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void syncResourceTrees(JSONArray changes)
    throws IOException {
        HttpPost method = new HttpPost(url + "/rest/resource/resource/applyCompareChanges");
        method.setEntity(getStringEntity(changes));
        invokeMethod(method);
    }

    //----------------------------------------------------------------------------------------------
    public void applyTemplate(String resourceParam, String resourceTemplateId, Properties properties)
    throws JSONException, IOException {
        JSONObject json = new JSONObject();
        json.put("resourceTemplateId", resourceTemplateId);
        json.put("targetResourceId", resourceParam);

        for (Object key: properties.keySet()) {
            String propertyKey = "p_" + (String) key;
            json.put(propertyKey, properties.get(key));
        }

        HttpPut method = new HttpPut(url + "/rest/resource/resource/applyTemplate");
        method.setEntity(getStringEntity(json));
        invokeMethod(method);
    }
}
