/*
* Licensed Materials - Property of IBM Corp.
* IBM UrbanCode Deploy
* (c) Copyright IBM Corporation 2011, 2016. All Rights Reserved.
*
* U.S. Government Users Restricted Rights - Use, duplication or disclosure restricted by
* GSA ADP Schedule Contract with IBM Corp.
*/
package com.urbancode.ud.client;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@SuppressWarnings("deprecation") // Triggered by DefaultHttpClient
public class PropertyClient extends UDRestClient {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public PropertyClient(URI url, String clientUser, String clientPassword) {
        super(url, clientUser, clientPassword);
    }

    //----------------------------------------------------------------------------------------------
    public PropertyClient(URI url, String clientUser, String clientPassword, boolean trustAllCerts) {
        super(url, clientUser, clientPassword, trustAllCerts);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Construct with a supplied HTTP client. See UDRestClient for configuration
     * requirements and restrictions.
     */
    public PropertyClient(URI url, DefaultHttpClient client) {
        super(url, client);
    }

    //----------------------------------------------------------------------------------------------
    public UUID createPropDef(UUID propSheetDefId, String propSheetDefPath, String name,
                              String description, String label, Boolean required, String type, String value)
    throws IOException, JSONException {
        UUID result;

        String uri = url + "/property/propSheetDef/" + encodePath(propSheetDefPath) + ".-1/propDefs";
        JSONObject propDefObject = new JSONObject();

        propDefObject.put("name", name);
        propDefObject.put("description", description);
        propDefObject.put("label", label);
        propDefObject.put("required", required.toString());
        propDefObject.put("type", type);
        propDefObject.put("value", value);
        propDefObject.put("definitionGroupId", propSheetDefId);

        HttpPut method = new HttpPut(uri);
        method.setEntity(getStringEntity(propDefObject));

        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        result = UUID.fromString(new JSONObject(body).getString("id"));
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public JSONArray getPropSheetDefPropDefs(String propSheetDefPath)
    throws IOException, JSONException {
        JSONArray result = null;

        String uri = url + "/property/propSheetDef/" + encodePath(propSheetDefPath) + ".-1/propDefs";

        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        result = new JSONArray(body);

        return result;
    }

    public JSONArray updatePropDefs(String propSheetDefPath, JSONArray propDefs, boolean deleteExtraProps)
    throws IOException, JSONException {
        JSONArray result = null;

        String uri = url + "/property/propSheetDef/" + encodePath(propSheetDefPath) + ".-1/propDefs/update/"
                + String.valueOf(deleteExtraProps);
        HttpPut method = new HttpPut(uri);
        method.setEntity(getStringEntity(propDefs));
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        result = new JSONArray(body);

        return result;
    }

    public JSONObject updateResourcePropValues(String propSheetPath, String propSheetVer, JSONObject propDefs)
    throws IOException, JSONException {
        JSONObject result = null;

        String uri = url + "/property/propSheet/" + encodePath(propSheetPath) + "." + propSheetVer + "/allPropValuesFromBatch/";
        HttpPut method = new HttpPut(uri);
	method.addHeader("Version", propSheetVer); // Set propsheet version
        method.setEntity(getStringEntity(propDefs));
        HttpResponse response = invokeMethod(method);
        String body = getBody(response);
        if (body.length() > 0)
            result = new JSONObject(body);

        return result;
    }
}
