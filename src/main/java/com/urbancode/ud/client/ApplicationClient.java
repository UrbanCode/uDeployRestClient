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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@SuppressWarnings("deprecation") // Triggered by DefaultHttpClient
public class ApplicationClient extends UDRestClient {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public ApplicationClient(URI url, String clientUser, String clientPassword, boolean trustAllCerts) {
        super(url, clientUser, clientPassword, trustAllCerts);
    }

    //----------------------------------------------------------------------------------------------
    public ApplicationClient(URI url, String clientUser, String clientPassword) {
        super(url, clientUser, clientPassword);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Construct with a supplied HTTP client. See UDRestClient for configuration
     * requirements and restrictions.
     */
    public ApplicationClient(URI url, DefaultHttpClient client) {
        super(url, client);
    }

    //----------------------------------------------------------------------------------------------
    public String addComponentToApplication(String appName, String compName)
    throws IOException {
        String result = null;

        String uri = url + "/cli/application/addComponentToApp?application=" +
                encodePath(appName) + "&component=" +
                encodePath(compName);

        HttpPut method = new HttpPut(uri);
        HttpResponse response = invokeMethod(method);
        result = getBody(response);
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public String addTagToApplication(String appName, String tagName)
    throws IOException {
        String result = null;

        String uri = url + "/cli/application/tag?application=" +
                encodePath(appName) + "&tag=" +
                encodePath(tagName);

        HttpPut method = new HttpPut(uri);
        HttpResponse response = invokeMethod(method);
        result = getBody(response);
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void addApplicationToTeam(String application, String team, String type)
    throws IOException {
        String uri = url + "/cli/application/teams?team=" +
                encodePath(team) + "&type=" +
                encodePath(type) + "&application=" +
                encodePath(application);
        HttpPut method = new HttpPut(uri);
        invokeMethod(method);
    }

    //----------------------------------------------------------------------------------------------
    public UUID createApplication(String appName, String description,
            String notificationScheme, boolean enforceCompleteSnapshots)
    throws JSONException, IOException {
        UUID result = null;

        JSONObject jsonToSend = new JSONObject();
        //put required data
        jsonToSend.put("name", appName);
        jsonToSend.put("enforceCompleteSnapshots", enforceCompleteSnapshots);
        //put optional data
        if (!"".equals(description) && description != null) {
            jsonToSend.put("description", description);
        }
        if (!"".equals(notificationScheme) && !"none".equalsIgnoreCase(notificationScheme) && notificationScheme != null) {
            jsonToSend.put("notificationScheme", notificationScheme);
        }

        String uri = url + "/cli/application/create";
        HttpPut method = new HttpPut(uri);
        method.setEntity(getStringEntity(jsonToSend));
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONObject jsonResult = new JSONObject(body);
        result = UUID.fromString((String) jsonResult.get("id"));

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public UUID createApplicationProcess(String jsonStringIn)
    throws IOException, JSONException {
        UUID result = null;
        JSONObject jsonToSend = new JSONObject(jsonStringIn);

        String uri = url + "/cli/applicationProcess/create";
        HttpPut method = new HttpPut(uri);
        method.setEntity(getStringEntity(jsonToSend));

        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONObject jsonResult = new JSONObject(body);
        result = UUID.fromString((String) jsonResult.get("id"));

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public JSONArray getApplicationProcessRequestVersions(String processId)
    throws IOException, JSONException {
        JSONArray result = null;
        String uri = url + "/rest/deploy/applicationProcessRequest/" +
                encodePath(processId.toString()) + "/versions";
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        try {
            JSONObject props = new JSONObject(getBody(response));
            result = props.getJSONArray("versions");
        }
        catch (JSONException e) {
            result = new JSONArray(getBody(response));
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public JSONArray getApplicationProcessRequestProperties(String processId)
    throws IOException, JSONException {
        JSONArray result = null;
        String uri = url + "/rest/deploy/applicationProcessRequest/" +
                encodePath(processId.toString()) + "/properties";
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        try {
            JSONObject props = new JSONObject(getBody(response));
            result = props.getJSONArray("properties");
        }
        catch (JSONException e) {
            result = new JSONArray(getBody(response));
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public String getApplicationProcessStatus(String processID)
    throws IOException, JSONException {
        String result = null;

        String uri = url + "/cli/applicationProcessRequest/requestStatus?request=" +
                encodePath(processID);
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONObject resultAsJSON = new JSONObject(body);

        result = resultAsJSON.getString("result");
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public JSONObject getSnapshot(String application, String snapshot)
    throws IOException, JSONException {
        JSONObject result = null;

        //Final path parameter is true/false for inactive snapshots
        String uri = url + "/rest/deploy/application/" +encodePath(application) + "/snapshots/false";
        HttpGet method = new HttpGet(uri);

        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONArray resultJSON = new JSONArray(body);
        for (int i=0; i<resultJSON.length(); i++) {
            JSONObject snapshotJson = (JSONObject) resultJSON.get(i);
            String resultName = (String) snapshotJson.get("name");
            String resultId = (String) snapshotJson.get("id");
            if (snapshot.equals(resultName) || snapshot.equals(resultId)) {
                result = snapshotJson;
                break;
            }
        }

        if (result == null) {
            throw new IOException("Could not find snapshot '"+snapshot+"'");
        }

        return result;

    }

    //----------------------------------------------------------------------------------------------
    public JSONArray getSnapshotVersions(String snapshot)
    throws IOException, JSONException {
        JSONArray result = null;

        //Final path parameter is true/false for inactive snapshots
        String uri = url + "/rest/deploy/snapshot/" +encodePath(snapshot) + "/versions";
        HttpGet method = new HttpGet(uri);

        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        result = new JSONArray(body);

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public JSONObject getApplication(String appName)
    throws IOException, JSONException {
        JSONObject result = null;
        String uri = url + "/cli/application/info?application=" +
        encodePath(appName);
        HttpGet method = new HttpGet(uri);

        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        result = new JSONObject(body);
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public JSONArray getApplicationComponents(String appName)
    throws IOException, JSONException {
        JSONArray result = null;
        String uri = url + "/cli/application/componentsInApplication?application=" +
        encodePath(appName);
        HttpGet method = new HttpGet(uri);

        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        result = new JSONArray(body);
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public UUID requestApplicationProcess(String appName, String processName, String description,
        String envName, String snapshot, boolean onlyChanged,
        Map<String, List<String>> componentVersions)
    throws IOException, JSONException {
        UUID result = null;
        JSONObject jsonToSend = new JSONObject();
        jsonToSend.put("application", appName)
        .put("applicationProcess", processName)
        .put("environment", envName)
        .put("onlyChanged", onlyChanged);
        if (!"".equals(description) && description != null) {
            jsonToSend.put("description", description);
        }
        if (!"".equals(snapshot) && snapshot != null) {
            jsonToSend.put("snapshot", snapshot);
        }
        JSONArray cvMappings = new JSONArray();
        Set<String> keys = componentVersions.keySet();
        for (String component : keys) {
            List<String> versions = componentVersions.get(component);

            for (String version : versions) {
                JSONObject cvMapping = new JSONObject()
                .put("component", component)
                .put("version", version);
                cvMappings.put(cvMapping);
            }
        }
        jsonToSend.put("versions", cvMappings);

        String uri = url + "/cli/applicationProcessRequest/request";
        HttpPut method = new HttpPut(uri);
        method.setEntity(getStringEntity(jsonToSend));
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONObject jsonResult = new JSONObject(body);
        result = UUID.fromString((String) jsonResult.get("requestId"));
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void setApplicationProcessRequestProperty(String processId, String name, String value,
            boolean isSecure)
    throws IOException, JSONException {
        if (StringUtils.isEmpty(processId)) {
            throw new IOException("processId was not supplied");
        }
        if (StringUtils.isEmpty(name)) {
            throw new IOException("name was not supplied");
        }

        String uri = url + "/rest/deploy/applicationProcessRequest/" +
                encodePath(processId) + "/saveProperties";

        JSONArray props = new JSONArray();
        props.put(createNewPropertyJSON(name, value, isSecure));

        HttpPut method = new HttpPut(uri);
        method.setEntity(getStringEntity(props));
        invokeMethod(method);
    }

    //----------------------------------------------------------------------------------------------
    public String setApplicationProperty(String appName, String name, String value, boolean isSecure)
    throws IOException {
        String result = null;
        if ("".equals(appName) || "".equals(name)) {
            throw new IOException("a required argument was not supplied");
        }

        String uri = url + "/cli/application/propValue?application=" +
                encodePath(appName) + "&name=" +
                encodePath(name) + "&value=" +
                encodePath(value) + "&isSecure=" +
                encodePath(String.valueOf(isSecure));

        HttpPut method = new HttpPut(uri);
        retryInvokeMethod(method, "set application property");
        if (isSecure) {
            result = name + "=****";
        }
        else {
            result = name + "=" + value;
        }
        return result;
    }


    /**
     * Create a new snapshot for an application, specifying the exact set of versions to be added
     * to the snapshot.
     *
     * @param versions A Map of component name/ID to a list of version names/IDs from that component
     *                 to put in the new snapshot
     */
    public UUID createSnapshot(String snapshotName, String description,
            String applicationName, Map<String, List<String>> versions)
    throws JSONException, IOException {
        UUID result = null;

        JSONObject jsonToSend = new JSONObject();
        //put required data
        jsonToSend.put("name", snapshotName);
        jsonToSend.put("application", applicationName);
        //put optional data
        if (!StringUtils.isEmpty(description)) {
            jsonToSend.put("description", description);
        }

        // Version pairing array
        JSONArray versionArray = new JSONArray();
        if (versions != null) {
            for (String componentName : versions.keySet()) {
                List<String> versionNames = versions.get(componentName);
                for (String versionName : versionNames) {
                    JSONObject pairing = new JSONObject();
                    pairing.put(componentName, versionName);
                    versionArray.put(pairing);
                }
            }
        }
        jsonToSend.put("versions", versionArray);

        String uri = url + "/cli/snapshot/createSnapshot";
        HttpPut method = new HttpPut(uri);
        method.setEntity(getStringEntity(jsonToSend));
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONObject jsonResult = new JSONObject(body);
        result = UUID.fromString((String) jsonResult.get("id"));

        return result;
    }

    /**
     * Create a snapshot based on the current contents of an environment.
     * @throws JSONException
     */
    public UUID createSnapshotOfEnvironment(String environmentName, String applicationName,
            String name, String description)
    throws IOException, JSONException {
        UUID result = null;

        String uri = url + "/cli/snapshot/createSnapshotOfEnvironment?environment=" +
                encodePath(environmentName) + "&name=" +
                encodePath(name);

        // Optional values
        if (!StringUtils.isEmpty(applicationName)) {
            uri = uri + "&application=" + encodePath(applicationName);
        }
        if (!StringUtils.isEmpty(description)) {
            uri = uri + "&description=" + encodePath(description);
        }

        HttpPut method = new HttpPut(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONObject jsonResult = new JSONObject(body);
        result = UUID.fromString((String) jsonResult.get("id"));

        return result;
    }

    /**
     * Checks if the application named appName has a process named processName
     *
     * @throws JSONException
     * @throws IOException
     */
    public JSONObject getApplicationProcess(String appName, String processName) throws IOException,
            JSONException {
        JSONObject result = null;
        String uri = url + "/cli/applicationProcess/info?application=" + encodePath(appName)
                + "&applicationProcess=" + encodePath(processName);
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
            throw new IOException("404 Not Found");
        }
        String body = getBody(response);
        result = new JSONObject(body);
        return result;
    }

    /**
     * Returns a list of environments defined for the named application
     *
     * @throws JSONException
     * @throws IOException
     */
    public JSONArray getApplicationEnvironments(String appName, String active, String inactive)
            throws IOException, JSONException {
        JSONArray result = null;
        String uri = url + "/cli/application/environmentsInApplication?application="
                + encodePath(appName) + "&active="
                + encodePath(active) + "&inactive="
                + encodePath(inactive);

        HttpGet method = new HttpGet(uri);

        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        result = new JSONArray(body);
        return result;
    }

    /**
     * Removes named component for the named application
     *
     * @throws JSONException
     * @throws IOException
     */
    public JSONObject removeComponentFromApplication(String[] components, String applicationName)
            throws IOException, JSONException {
        String applicationId = getApplicationUUID(applicationName).toString();

        JSONArray list = new JSONArray();

        for (String component : components) {
            String componentId = getComponentUUID(component).toString();
            list.put(componentId);
        }

        JSONObject jsonToSend = new JSONObject();
        jsonToSend.put("components", list);
        String uri = url + "/rest/deploy/application/" + encodePath(applicationId) + "/removeComponents";
        HttpPut method = new HttpPut(uri);
        method.setEntity(getStringEntity(jsonToSend));

        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONObject result = new JSONObject(body);
        return result;
    }

    /**
     * Removes named tag for the named application
     *
     * @throws IOException
     */
    public String removeTagFromApplication(String appName, String tagName)
            throws IOException {
        String result = null;

        String uri = url + "/cli/application/tag?application=" +
                encodePath(appName) + "&tag=" +
                encodePath(tagName);

        HttpDelete method = new HttpDelete(uri);
        HttpResponse response = invokeMethod(method);
        result = getBody(response);
        return result;
    }

    /**
     * Deleted the named application
     *
     * @throws JSONException
     * @throws IOException
     */
    public UUID deleteApplication(String applicationName) throws IOException, JSONException {
        UUID appUUID = getApplicationUUID(applicationName);
        String uri = url + "/rest/deploy/application/" + appUUID.toString();
        HttpDelete method = new HttpDelete(uri);
        invokeMethod(method);

        return appUUID;
    }

    //----------------------------------------------------------------------------------------------
    private UUID getApplicationUUID(String applicationName) throws IOException, JSONException {
        UUID result = null;

        String uri = url + "/cli/application/info?application=" + encodePath(applicationName);
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONObject resultJSON = new JSONObject(body);
        result = UUID.fromString(resultJSON.getString("id"));

        return result;
    }

    //----------------------------------------------------------------------------------------------
    private UUID getComponentUUID(String component) throws IOException, JSONException {
        UUID result = null;

        String uri = url + "/cli/component/info?component=" + encodePath(component);
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONObject resultJSON = new JSONObject(body);
        result = UUID.fromString(resultJSON.getString("id"));

        return result;
    }
}
