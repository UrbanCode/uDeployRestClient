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
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@SuppressWarnings("deprecation") // Triggered by DefaultHttpClient
public class SystemClient extends UDRestClient {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public SystemClient(URI url, String clientUser, String clientPassword) {
        super(url, clientUser, clientPassword);
    }

    //----------------------------------------------------------------------------------------------
    public SystemClient(URI url, String clientUser, String clientPassword, boolean trustAllCerts) {
        super(url, clientUser, clientPassword, trustAllCerts);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Construct with a supplied HTTP client. See UDRestClient for configuration
     * requirements and restrictions.
     */
    public SystemClient(URI url, DefaultHttpClient client) {
        super(url, client);
    }

    //----------------------------------------------------------------------------------------------
    public void addUserToTeam(String user, String team, String type)
    throws IOException {
        String uri = url + "/cli/teamsecurity/users?user=" +
                encodePath(user) + "&team=" +
                encodePath(team) + "&type=" +
                encodePath(type);
        HttpPut method = new HttpPut(uri);
        invokeMethod(method);
    }

    //----------------------------------------------------------------------------------------------
    public void addGroupToTeam(String group, String team, String type)
    throws IOException {
        String uri = url + "/cli/teamsecurity/groups?group=" +
                encodePath(group) + "&team=" +
                encodePath(team) + "&type=" +
                encodePath(type);
        HttpPut method = new HttpPut(uri);
        invokeMethod(method);
    }

    //----------------------------------------------------------------------------------------------
    public JSONObject getSystemConfiguration()
    throws JSONException, IOException {
        String uri = url + "/cli/systemConfiguration";
        JSONObject result;

        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        result = new JSONObject(getBody(response));

        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void setSystemConfiguration(Map<String, String> properties)
    throws JSONException, IOException {
        String uri = url + "/cli/systemConfiguration";

        JSONObject jsonToSend = new JSONObject();
        for (String key : properties.keySet()) {
            jsonToSend.put(key, properties.get(key));
        }

        HttpPut method = new HttpPut(uri);
        method.setEntity(getStringEntity(jsonToSend));
        invokeMethod(method);
    }

    //----------------------------------------------------------------------------------------------
    public String setSystemProperty(String name, String value, boolean isSecure)
    throws IOException {
        String result;
        if ("".equals(name)) {
            throw new IOException("a required argument was not supplied");
        }

        String uri = url + "/cli/systemConfiguration/propValue?name=" +
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

}
