/**
 * (c) Copyright IBM Corporation 2011, 2017.
 * (c) Copyright HCL Technologies Ltd. 2018. All Rights Reserved.
 * This is licensed under the following license.
 * The Apache Version 2.0 License (https://www.apache.org/licenses/LICENSE-2.0.txt)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */
package com.urbancode.ud.client;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.urbancode.commons.util.IO;

@SuppressWarnings("deprecation") // Triggered by DefaultHttpClient
public class AgentClient extends UDRestClient {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public AgentClient(URI url, String clientUser, String clientPassword) {
        super(url, clientUser, clientPassword);
    }

    //----------------------------------------------------------------------------------------------
    public AgentClient(URI url, String clientUser, String clientPassword, boolean trustAllCerts) {
        super(url, clientUser, clientPassword, trustAllCerts);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Construct with a supplied HTTP client. See UDRestClient for configuration
     * requirements and restrictions.
     */
    public AgentClient(URI url, DefaultHttpClient client) {
        super(url, client);
    }

    //----------------------------------------------------------------------------------------------
    public void deleteAgent(String name)
    throws IOException {
        String uri = url + "/cli/agentCLI";
        uri = uri + "?agent=" + encodePath(name);

        HttpDelete method = new HttpDelete(uri);
        invokeMethod(method);
    }

    //----------------------------------------------------------------------------------------------
    public int getAgentInstallPackage(File agentZipLocation)
    throws IOException {
        int result = 1;

        String uri = url + "/tools/ibm-ucd-agent.zip";
        HttpGet method = new HttpGet(uri);
        HttpResponse resp = invokeMethod(method);
        IO.copy(resp.getEntity().getContent(), agentZipLocation);
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public String getAgentProperty(String agentName, String name)
    throws IOException {
        String result = null;
        if ("".equals(agentName) || "".equals(name)) {
            throw new IOException("a required argument was not supplied");
        }
        String uri = url + "/cli/agentCLI/getProperty?agent=" +
                encodePath(agentName) + "&name=" +
                encodePath(name);
        HttpGet method = new HttpGet(uri);
        HttpResponse response = invokeMethod(method);
        result = getBody(response);
        return result;
    }

    //----------------------------------------------------------------------------------------------
    public void installAgent(String name, String host, String port, String sshUsername,
            String sshPassword, String installDir, String javaHomePath, String tempDirPath,
            String serverHost, String serverPort, String proxyPort,
            String mutualAuth) throws IOException {
        installAgent(name, host, port, sshUsername, sshPassword, installDir, javaHomePath,
                tempDirPath, serverHost, serverPort, "", proxyPort, mutualAuth);
    }

    //----------------------------------------------------------------------------------------------
    @Deprecated
    public void installAgent(String name, String host, String port, String sshUsername,
            String sshPassword, String installDir, String javaHomePath, String tempDirPath,
            String serverHost, String serverPort, String proxyHost, String proxyPort,
            String mutualAuth) throws IOException {
        String uri = url + "/cli/sshInstallAgent/installNewAgent" +
                "?name="+encodePath(name)+
                "&host="+encodePath(host)+
                "&port="+encodePath(port)+
                "&sshUsername="+encodePath(sshUsername)+
                "&sshPassword="+encodePath(sshPassword)+
                "&sshPassword="+encodePath(sshPassword)+
                "&installDir="+encodePath(installDir)+
                "&javaHomePath="+encodePath(javaHomePath)+
                "&tempDirPath="+encodePath(tempDirPath)+
                "&serverHost="+encodePath(serverHost)+
                "&serverPort="+encodePath(serverPort)+
                "&proxyHost="+encodePath(proxyHost)+
                "&proxyPort="+encodePath(proxyPort)+
                "&mutualAuth="+encodePath(mutualAuth);

        HttpPut method = new HttpPut(uri);
        invokeMethod(method);
    }

    //----------------------------------------------------------------------------------------------
    public String setAgentProperty(String agentName, String name, String value, boolean isSecure)
    throws IOException {
        String result;
        if ("".equals(agentName) || "".equals(name)) {
            throw new IOException("a required argument was not supplied");
        }

        String uri = url + "/cli/agentCLI/setProperty?agent=" +
                encodePath(agentName) + "&name=" +
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

    public JSONObject getAgent(String agentName)
    throws IOException, JSONException{
        JSONObject result = null;

        String uri = url + "/cli/agentCLI/info";
        uri = uri + "?agent=" + encodePath(agentName);
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
}
