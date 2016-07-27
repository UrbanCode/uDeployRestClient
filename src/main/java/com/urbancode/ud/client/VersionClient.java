/*
* Licensed Materials - Property of IBM Corp.
* IBM UrbanCode Deploy
* (c) Copyright IBM Corporation 2011, 2016. All Rights Reserved.
*
* U.S. Government Users Restricted Rights - Use, duplication or disclosure restricted by
* GSA ADP Schedule Contract with IBM Corp.
*/
package com.urbancode.ud.client;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.urbancode.ds.client.AddVersionFilesCommand;
import com.urbancode.ds.client.DownloadVersionFilesCommand;

@SuppressWarnings("deprecation") // Triggered by DefaultHttpClient
public class VersionClient extends UDRestClient{

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    private static final Logger log = Logger.getLogger(ComponentClient.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public VersionClient(URI url, String clientUser, String clientPassword) {
        super(url, clientUser, clientPassword);
    }

    //----------------------------------------------------------------------------------------------
    public VersionClient(URI url, String clientUser, String clientPassword, boolean trustAllCerts) {
        super(url, clientUser, clientPassword, trustAllCerts);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Construct with a supplied HTTP client. See UDRestClient for configuration
     * requirements and restrictions.
     */
    public VersionClient(URI url, DefaultHttpClient client) {
        super(url, client);
    }

    //----------------------------------------------------------------------------------------------
    public UUID createVersion(String component, String version, String description)
    throws IOException, JSONException {
        UUID result = null;
        String uri = url + "/cli/version/createVersion/?name=" +
            encodePath(version) + "&component=" +
            encodePath(component) + "&description=" +
            encodePath(description);

        HttpPost method = new HttpPost(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONObject jsonResult = new JSONObject(body);
        result = UUID.fromString((String) jsonResult.get("id"));

        return result;
    }


    @Deprecated
    //----------------------------------------------------------------------------------------------
    public void addVersionFiles(String component, String version,
        File base, String offset, String[] includes, String[] excludes, boolean saveExecuteBits,
        boolean verbose)
    throws Exception {
        AddVersionFilesCommand command = new AddVersionFilesCommand(url.toString(),
                client,
                component,
                version,
                base,
                offset,
                includes,
                excludes,
                saveExecuteBits,
                verbose);

        command.execute();
    }

    //----------------------------------------------------------------------------------------------
    public void addVersionFiles(String component, String version,
        File base, String offset, String[] includes, String[] excludes, boolean saveExecuteBits,
        boolean verbose, Charset charset, String[] extensions)
    throws Exception {
        AddVersionFilesCommand command = new AddVersionFilesCommand(
                url.toString(),
                client, component,
                version,
                base,
                offset,
                includes,
                excludes,
                saveExecuteBits,
                verbose,
                charset,
                extensions);
        command.execute();
    }

    //----------------------------------------------------------------------------------------------
    public void deleteVersion(UUID versionId)
    throws IOException {
        String uri = url + "/rest/deploy/version/" + versionId.toString();

        HttpDelete method = new HttpDelete(uri);
        try {
            invokeMethod(method);
        }
        finally {
            releaseConnection(method);
        }
    }

    //----------------------------------------------------------------------------------------------
    /*
     * create version through rest , add files to it.
     * will delete the version if upload fails
     */
    public UUID createAndAddVersionFiles(String component, String version, String description,
        File baseDir, String offset, String[] includes, String[] excludes,
        boolean saveExecuteBits, boolean verbose, Charset charset, String[] extensions)
    throws Exception {
        UUID versionId = createVersion(component, version, description);
        try {
            addVersionFiles(component, version, baseDir, offset, includes, excludes, saveExecuteBits,
                    verbose, charset, extensions);
        }
        catch (Exception e) {
            log.error("Error creating version" + e.getMessage());
            log.error("Cleaning up version " + versionId);
            deleteVersion(versionId);
            throw e;
        }

        return versionId;
    }

    //----------------------------------------------------------------------------------------------
    public JSONObject getVersion(String versionId)
        throws IOException, JSONException {
        JSONObject result = null;
        String uri = url + "/rest/deploy/version/" + encodePath(versionId);

        HttpGet method = new HttpGet(uri);

        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        result = new JSONObject(body);

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public Map<String, String> getVersionProperties(String version, String component)
        throws Exception {
        Map<String, String> result = new HashMap<String, String>();
        String uri = url + "/cli/version/versionProperties?version=" + encodePath(version);

        if (component != null) {
            uri = uri + "&component=" + encodePath(component);
        }
        HttpGet method = new HttpGet(uri);

        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        JSONArray propsJSON = new JSONArray(body);

        for (int i = 0; i < propsJSON.length(); i++) {
            JSONObject prop = propsJSON.getJSONObject(i);
            result.put(prop.getString("name"), prop.getString("value"));
        }

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public String setVersionProperty(String versionName, String componentName, String name, String value, boolean isSecure)
    throws IOException {
        String result;
        if ("".equals(versionName) || "".equals(name)) {
            throw new IOException("a required argument was not supplied");
        }

        String uri = url + "/cli/version/versionProperties?version=" +
                encodePath(versionName) + "&name=" +
                encodePath(name) + "&value=" +
                encodePath(value) + "&isSecure=" +
                encodePath(String.valueOf(isSecure));

        if (!StringUtils.isEmpty(componentName)) {
            uri = uri + "&component=" +
                    encodePath(componentName);
        }

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

    //------------------------------------------------------------------------------------------------------
    public void downloadFiles(String component, String version, String download2Path, String singleFilePath)
        throws Exception {
        DownloadVersionFilesCommand dc = new DownloadVersionFilesCommand(url.toString(),
            client,
            component,
            version,
            download2Path,
            singleFilePath);
        dc.execute();
    }
}
