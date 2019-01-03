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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@SuppressWarnings("deprecation") // Triggered by DefaultHttpClient
public class ComponentClient extends UDRestClient {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public ComponentClient(URI url, String clientUser, String clientPassword, boolean trustAllCerts) {
        super(url, clientUser, clientPassword, trustAllCerts);
    }

    //----------------------------------------------------------------------------------------------
    public ComponentClient(URI url, String clientUser, String clientPassword) {
        super(url, clientUser, clientPassword);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Construct with a supplied HTTP client. See UDRestClient for configuration
     * requirements and restrictions.
     */
    public ComponentClient(URI url, DefaultHttpClient client) {
        super(url, client);
    }

    //----------------------------------------------------------------------------------------------
    public void addComponentVersionLink(
        String componentName,
        String versionName,
        String linkTitle,
        String linkURL)
    throws JSONException, IOException {
        String uri = url + "/cli/version/addLink?component=" +
            encodePath(componentName) + "&version=" +
            encodePath(versionName) + "&linkName=" +
            encodePath(linkTitle) + "&link=" +
            encodePath(linkURL);

        HttpPut method = new HttpPut(uri);
        invokeMethod(method);
    }

    //----------------------------------------------------------------------------------------------
    public void addComponentVersionStatus(
        String componentName,
        String versionName,
        String statusName)
    throws IOException {
        String uri = url + "/cli/version/addStatus?component=" +
                encodePath(componentName) +
                "&version=" + encodePath(versionName) + "&status=" + encodePath(statusName);
        HttpPut method = new HttpPut(uri);
        invokeMethod(method);
    }

    //----------------------------------------------------------------------------------------------
    public void addComponentToTeam(String component, String team, String type)
    throws IOException {
        String uri = url + "/cli/component/teams?team=" +
                encodePath(team) + "&type=" +
                encodePath(type) + "&component=" +
                encodePath(component);
        HttpPut method = new HttpPut(uri);
        invokeMethod(method);
    }

    //----------------------------------------------------------------------------------------------
    public String addTagToComponent(String componentName, String tagName)
    throws IOException {
        String result = null;

        String uri = url + "/cli/component/tag?component=" +
                encodePath(componentName) + "&tag=" +
                encodePath(tagName);

        HttpPut method = new HttpPut(uri);
        HttpResponse response = invokeMethod(method);
        result = getBody(response);
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public UUID createComponent(String componentName, String description,
            String sourceConfigPlugin, String defaultVersionType, String templateName,
            int templateVersion, boolean importAutomatically, boolean useVfs)
    throws JSONException, IOException {
        return createComponent(componentName, description, sourceConfigPlugin, defaultVersionType,
                templateName, templateVersion, importAutomatically, useVfs, null);
    }

    //----------------------------------------------------------------------------------------------
    public UUID createComponent(String componentName, String description,
            String sourceConfigPlugin, String defaultVersionType, String templateName,
            int templateVersion, boolean importAutomatically, boolean useVfs,
            Map<String, String> properties)
    throws JSONException, IOException {
        UUID result = null;

        JSONObject jsonToSend = new JSONObject();
        //put required data
        jsonToSend.put("name", componentName);
        jsonToSend.put("sourceConfigPlugin", sourceConfigPlugin);
        //must be FULL or INCREMENTAL
        jsonToSend.put("defaultVersionType", defaultVersionType.toUpperCase());
        jsonToSend.put("importAutomatically", importAutomatically);
        jsonToSend.put("useVfs", useVfs);
        //put optional data
        if (!"".equals(description) && description != null) {
            jsonToSend.put("description", description);
        }
        if (!"".equals(templateName) && templateName != null) {
            jsonToSend.put("templateName", templateName);
        }
        if (templateVersion > 0) {
            jsonToSend.put("templateVersion", templateVersion);
        }
        JSONObject propertiesObject = new JSONObject();
        if (properties != null && !properties.isEmpty()) {
            for (Entry<String, String> ent : properties.entrySet()) {
                propertiesObject.put(ent.getKey(), ent.getValue());
            }
        }
        jsonToSend.put("properties", propertiesObject);
        String uri = url + "/cli/component/create";
        HttpPut method = new HttpPut(uri);
        method.setEntity(getStringEntity(jsonToSend));

        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONObject jsonResult = new JSONObject(body);
        result = UUID.fromString((String) jsonResult.get("id"));

        return result;
    }

    /**
     * Deletes the named component
     * @throws JSONException
     * @throws IOException
     */
    public UUID deleteComponent(String componentName)
    throws IOException, JSONException {
        UUID compUUID = getComponentUUID(componentName);
        String uri = url + "/rest/deploy/component/" + compUUID.toString();
        HttpDelete method = new HttpDelete(uri);
        invokeMethod(method);

        return compUUID;
    }

    /**
     * Poll the source configuration for new versions
     * @param properties Runtime properties to pass to the source configuration
     * @throws IOException
     * @throws JSONException
     */
    public void importComponentVersions(String componentName, Map<String, String> properties)
    throws IOException, JSONException {
        JSONObject jsonToSend = new JSONObject();

        jsonToSend.put("component", componentName);

        JSONObject propertiesObject = new JSONObject();
        if (properties != null && !properties.isEmpty()) {
            for (Entry<String, String> ent : properties.entrySet()) {
                propertiesObject.put(ent.getKey(), ent.getValue());
            }
        }

        jsonToSend.put("properties", propertiesObject);
        String uri = url + "/cli/component/integrate";
        HttpPut method = new HttpPut(uri);
        method.setEntity(getStringEntity(jsonToSend));

        HttpResponse response = invokeMethod(method);
    }

    /**
     * Returns a list of applications linked to the named component
     * @throws JSONException
     * @throws IOException
     */
    public JSONArray getComponentApplications(String componentName)
    throws IOException, JSONException {
        JSONArray result = null;
        UUID compUUID = getComponentUUID(componentName);
        String uri = url + "/rest/deploy/component/" + compUUID.toString() + "/applications";

        HttpGet method = new HttpGet(uri);

        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        result = new JSONArray(body);
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void createComponentVersion(String version, String component)
    throws IOException {
        String uri = url + "/cli/version/createVersion?name=" +
            encodePath(version) + "&component=" +
            encodePath(component);
        HttpPost method = new HttpPost(uri);
        HttpResponse response = invokeMethod(method);
        EntityUtils.consume(response.getEntity());
    }

    //----------------------------------------------------------------------------------------------
    public JSONObject getComponentVersionPropSheetDef(String component)
    throws IOException, JSONException {
        JSONObject componentObject = getComponent(component);
        JSONObject versionPropSheet = componentObject.getJSONObject("versionPropSheetDef");

        return versionPropSheet;
    }

    //----------------------------------------------------------------------------------------------
    public Map<String, String> getComponentProperties(String component)
    throws IOException, JSONException {
        Map<String, String> result = new HashMap<String, String>();
        String uri = url + "/cli/component/getProperties?component=" +
            encodePath(component);
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
    public JSONArray getComponentProcessRequestProperties(String processId)
    throws IOException, JSONException {
        JSONArray result = null;
        String uri = url + "/rest/deploy/componentProcessRequest/" +
                encodePath(processId) + "/properties";
        HttpGet method = new HttpGet(uri);

        HttpResponse response = invokeMethod(method);
        JSONObject props = new JSONObject(getBody(response));
        result = props.getJSONArray("properties");
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public JSONObject getComponentProcessRequest(String processId)
    throws IOException, JSONException {
        JSONObject result = null;
        String uri = url + "/rest/deploy/componentProcessRequest/" +
                encodePath(processId);
        HttpGet method = new HttpGet(uri);

        HttpResponse response = invokeMethod(method);
        result = new JSONObject(getBody(response));
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public UUID getComponentUUID(String component)
    throws IOException, JSONException {
        UUID result = null;

        String uri = url + "/cli/component/info?component=" + encodePath(component);
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONObject resultJSON = new JSONObject(body);
        result = UUID.fromString(resultJSON.getString("id"));

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public JSONObject getComponent(String component)
    throws IOException, JSONException {
        String uri = url + "/cli/component/info?component=" + encodePath(component);
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);

        return new JSONObject(body);
    }

    //----------------------------------------------------------------------------------------------
    public void removeComponentVersionStatus(
        String componentName,
        String versionName,
        String statusName)
    throws IOException {
        String uri = url + "/cli/version/status?component=" +
                encodePath(componentName) +
                "&version=" + encodePath(versionName) + "&status=" + encodePath(statusName);
        HttpDelete method = new HttpDelete(uri);
        invokeMethod(method);
    }

    /**
     * Removes named tag for the named component
     *
     * @throws IOException
     */
    public String removeTagFromComponent(String componentName, String tagName)
            throws IOException {
        String result = null;

        String uri = url + "/cli/component/tag?component=" +
                encodePath(componentName) + "&tag=" +
                encodePath(tagName);

        HttpDelete method = new HttpDelete(uri);
        HttpResponse response = invokeMethod(method);
        result = getBody(response);
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void setComponentProcessRequestProperty(String processId, String name, String value,
            boolean isSecure)
    throws IOException, JSONException {
        if (StringUtils.isEmpty(processId)) {
            throw new IOException("processId was not supplied");
        }
        if (StringUtils.isEmpty(name)) {
            throw new IOException("name was not supplied");
        }

        String uri = url + "/rest/deploy/componentProcessRequest/" +
                encodePath(processId) + "/saveProperties";

        JSONArray props = new JSONArray();
        props.put(createNewPropertyJSON(name, value, isSecure));

        HttpPut method = new HttpPut(uri);
        method.setEntity(getStringEntity(props));
        invokeMethod(method);
    }

    //----------------------------------------------------------------------------------------------
    public String setComponentProperty(String componentName, String name, String value, boolean isSecure)
    throws IOException {
        String result = null;
        if ("".equals(componentName) || "".equals(name)) {
            throw new IOException("a required argument was not supplied");
        }

        String uri = url + "/cli/component/propValue?component=" +
                encodePath(componentName) + "&name=" +
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

    public void updateSourceConfigProperty(String componentParam, String name, String value, String description, Boolean secure)
    throws IOException, JSONException {
        JSONObject propJson = createNewPropertyJSON(name, value, secure);
        propJson.put("description", description);

        String uri = url + "/rest/deploy/component/" + encodePath(componentParam) + "/updateSourceConfigProperties/"  + encodePath(name);
        HttpPut method = new HttpPut(uri);
        method.setEntity(getStringEntity(propJson));
        invokeMethod(method);
    }

    public List<String> getComponentVersions(String componentParam, Boolean getInactive)
    throws ClientProtocolException, IOException, JSONException {
        List<String> result = new ArrayList<String>();

        String uri = url + "/rest/deploy/component/" + encodePath(componentParam) + "/versions/" + encodePath(getInactive.toString());
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONArray resultJSON = new JSONArray(body);
        for (int i = 0; i < resultJSON.length(); i++) {
            JSONObject entry = resultJSON.getJSONObject(i);
            result.add(entry.getString("name"));
        }
        return result;
    }

    public JSONArray getComponentVersionsJsonArray(String componentParam, Boolean getInactive)
    throws ClientProtocolException, IOException, JSONException {
        JSONArray result = null;

        String uri = url + "/rest/deploy/component/" + encodePath(componentParam) + "/versions/" + encodePath(getInactive.toString());
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);

        result = new JSONArray(body);
        return result;
    }

    public List<String> getArchivedComponentVersions(String componentParam)
    throws ClientProtocolException, IOException, JSONException {
        List<String> result = new ArrayList<String>();

        String uri = url + "/rest/deploy/component/"+ encodePath(componentParam) +
                "/versions/true?filterFields=archived&filterValue_archived=true&filterType_archived=eq&filterClass_archived=Boolean";
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONArray resultJSON = new JSONArray(body);
        for (int i = 0; i < resultJSON.length(); i++) {
            JSONObject entry = resultJSON.getJSONObject(i);
            result.add(entry.getString("name"));
        }
        return result;
    }

    public String getLatestVersion(String componentParam)
    throws ClientProtocolException, IOException, JSONException {
        String result = null;

        String uri = url + "/rest/deploy/component/" + encodePath(componentParam) + "/latestVersion";
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);

        if (body != null) {
            JSONObject resultJSON = new JSONObject(body);
            result = resultJSON.getString("name");
        }
        return result;
    }

}
