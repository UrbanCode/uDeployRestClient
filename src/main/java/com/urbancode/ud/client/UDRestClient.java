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
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.urbancode.commons.httpcomponentsutil.HttpClientBuilder;
import com.urbancode.commons.util.Check;
import com.urbancode.commons.util.IO;
import com.urbancode.commons.web.util.PercentCodec;

@SuppressWarnings("deprecation") // Triggered by DefaultHttpClient
public class UDRestClient {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(UDRestClient.class);


    public static final int WAIT_RANGE_MIN_DEFAULT = 200;   // in milliseconds (ms)
    public static final int WAIT_RANGE_MAX_DEFAULT = 1000;  // in milliseconds (ms)
    public static final int RETRY_LIMIT_DEFAULT = 3;

    /**
     * Create an HTTP client configuring trustAllCerts using agent environment variables
     *
     * @param user The username to associate with the http client connection.
     * @param password The password of the username used to associate with the http client connection.
     * @see #createHttpClient(String,String,boolean)
     *
     * @return DefaultHttpClient
     */
    static public DefaultHttpClient createHttpClient(String user, String password) {
        String verifyServerIdentityString = System.getenv().get("UC_TLS_VERIFY_CERTS");
        Boolean verifiedCerts = Boolean.valueOf(verifyServerIdentityString);
        return createHttpClient(user, password, !verifiedCerts);
    }

    /**
     * Create an HTTP client configured with credentials and any proxy settings
     * from the environment.
     *
     * @param user The username to associate with the http client connection.
     * @param password The password of the username used to associate with the http client connection.
     * @param trustAllCerts Boolean to trust or deny all insecure certifications with the http client.
     *
     * @return DefaultHttpClient
     */
    static public DefaultHttpClient createHttpClient(String user, String password, boolean trustAllCerts) {
        HttpClientBuilder builder = new HttpClientBuilder();
        builder.setPreemptiveAuthentication(true);
        builder.setUsername(user);
        builder.setPassword(password);
        builder.setTrustAllCerts(trustAllCerts);

        if (!StringUtils.isEmpty(System.getenv("PROXY_HOST")) &&
            StringUtils.isNumeric(System.getenv("PROXY_PORT")))
        {
            log.debug("Configuring proxy settings.");
            builder.setProxyHost(System.getenv("PROXY_HOST"));
            builder.setProxyPort(Integer.valueOf(System.getenv("PROXY_PORT")));
        }

        if (!StringUtils.isEmpty(System.getenv("PROXY_USERNAME")) &&
            !StringUtils.isEmpty(System.getenv("PROXY_PASSWORD")))
        {
            log.debug("Configuring proxy settings.");
            builder.setProxyUsername(System.getenv("PROXY_USERNAME"));
            builder.setProxyPassword(System.getenv("PROXY_PASSWORD"));
        }

        return builder.buildClient();
    }

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    final protected URI url;
    final protected String clientUser;
    final protected String clientPassword;
    final protected DefaultHttpClient client;

    //----------------------------------------------------------------------------------------------
    /**
     * @param url The url of the UrbanCode Deploy server.
     * @param clientUser The username of the UrbanCode Deploy user.
     * @param clientPassword The password of the UrbanCode Deploy user.
     *
     */
    public UDRestClient(URI url, String clientUser, String clientPassword) {
        this.url = url;
        this.clientUser = clientUser;
        this.clientPassword = clientPassword;
        client = createHttpClient(clientUser, clientPassword);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @param url The url of the UrbanCode Deploy server.
     * @param clientUser The username of the UrbanCode Deploy user.
     * @param clientPassword The password of the UrbanCode Deploy user.
     * @param trustAllCerts Boolean to trust or deny all insecure certifications with the http client.
     *
     */
    public UDRestClient(URI url, String clientUser, String clientPassword, boolean trustAllCerts) {
        this.url = url;
        this.clientUser = clientUser;
        this.clientPassword = clientPassword;
        client = createHttpClient(clientUser, clientPassword, trustAllCerts);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Create a client with a supplied HTTP client. The client must be
     * configured with any proxy settings, credentials, etc required for correct
     * operation. Subclasses will not be able to access the username or
     * password if the instance is constructed with this method.
     *
     * @param url The url of the UrbanCode Deploy server.
     * @param client The HTTP client builder to make a REST call against the UrbanCode Deploy server.
     *
     */
    public UDRestClient(URI url, DefaultHttpClient client) {
        Check.nonNull(url);
        Check.nonNull(client);
        this.url = url;
        this.client = client;
        clientUser = null;
        clientPassword = null;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @param name The name of the new property being created.
     * @param value The value of the new property being created.
     * @param isSecure Boolean of whether or not the value should be secured.
     *
     * @return JSONObject
     *
     * @throws JSONException
     */
    protected JSONObject createNewPropertyJSON(String name, String value, boolean isSecure)
    throws JSONException {
        JSONObject result = new JSONObject();

        result.put("name", name)
        .put("value", value)
        .put("secure", isSecure);
        return result;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @param propArray A JSONArray object of properties each with a 'name' and 'value'.
     *
     * @return JSONObject
     *
     * @throws JSONException
     */
    protected JSONObject convertPropArrayToKeyValuePairs(JSONArray propArray)
    throws JSONException {
        JSONObject result = new JSONObject();
        for (int i=0; i<propArray.length(); i++) {
            JSONObject prop = propArray.getJSONObject(i);
            JSONObject propDefJSON = null;
            try {
                propDefJSON = (JSONObject) prop.get("propValue");
            }
            catch(JSONException e) {
                //default property values have not been overwritten; fetching defaults
                propDefJSON = (JSONObject) prop.get("propDef");
            }

            String propKey = (String) propDefJSON.get("name");
            String propValue = (String) propDefJSON.get("value");
            result.put(propKey, propValue);
        }
        return result;
    }

    /**
     * @param object The object of which the StringEntity being retrieved.
     *
     * @return StringEntity
     */
    protected StringEntity getStringEntity(Object object) {
        return new StringEntity(object.toString(), "UTF-8");
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @param response The response created by an HTTPClient call.
     *
     * @return String
     */
    protected String getBody(HttpResponse response)
    throws IOException {
        String result = null;
        StringBuilder builder = new StringBuilder();

        if (response.getStatusLine().getStatusCode() != 204) {
            InputStream body = response.getEntity().getContent();
            if (body != null) {
                Reader reader = IO.reader(body, IO.utf8());
                try {
                    IO.copy(reader, builder);
                }
                finally {
                    reader.close();
                }
            }
            result = builder.toString();
        }
        // Else if the response was "No Content" we should pass back null

        return result;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @param response The response created by an HTTPClient call.
     *
     * @throws IOException
     */
    protected void discardBody(HttpResponse response) throws IOException {
        EntityUtils.consumeQuietly(response.getEntity());
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @param request The response created by an HTTPClient call.
     *
     * @return HttpResponse
     */
    protected HttpResponse invokeMethod(HttpRequestBase request)
    throws IOException, ClientProtocolException {
        HttpResponse response = client.execute(request);
        int status = response.getStatusLine().getStatusCode();
        if (status > 299) {
            throw new IOException(String.format("%d %s\n%s",
                    response.getStatusLine().getStatusCode(),
                    response.getStatusLine().getReasonPhrase(),
                    getBody(response)));
        }
        return response;
    }

    /**
     * Invokes the specified REST API method, with retry if the HTTP request
     * returned a status code of 409 (Conflict).
     * Uses default values for wait range and retry limit.
     *
     * @param request HTTP REST API method (for URI)
     * @param logMethodName [Optional, but recommended] if not null, logs message for each retry.
     *    and if the limit is reached. A value of null suppresses log messages.
     *
     * @return response from sending HTTP request
     *
     * @throws IOException
     */
    protected HttpResponse retryInvokeMethod(HttpRequestBase request, String logMethodName)
    throws IOException {

        // Call method with default values.
        return retryInvokeMethod(request, logMethodName,
            WAIT_RANGE_MIN_DEFAULT, WAIT_RANGE_MAX_DEFAULT, RETRY_LIMIT_DEFAULT);
    }


    /**
     * Invokes the specified REST API method, with retry if the HTTP request
     * returned a status code of 409 (Conflict).
     *
     * @param request HTTP REST API method (for URI)
     * @param logMethodName [Optional, but recommended] if not null, logs message for each retry.
     *    and if the limit is reached. A value of null suppresses log messages.
     * @param waitRangeMin minimum number of milliseconds to wait if the invoke needs to retry.
     * @param waitRangeMax maximum number of milliseconds to wait if the invoke needs to retry.
     * @param retryLimit maximum times to retry if invoke fails, does not retry if value is less than 1.
     *
     * @return response from sending HTTP request
     *
     * @throws IOException
     */
    protected HttpResponse retryInvokeMethod(HttpRequestBase request, String logMethodName,
        int waitRangeMin, int waitRangeMax, int retryLimit)
    throws IOException {

        // Ensure coded parameters are not invalid (use defaults if so).
        if (waitRangeMin < 0) {
            log.debug("Retry minimum wait [" + waitRangeMin + "] is invalid, using " + WAIT_RANGE_MIN_DEFAULT);
            waitRangeMin = WAIT_RANGE_MIN_DEFAULT;
        }
        if (waitRangeMax < 0) {
            log.debug("Retry maximum wait [" + waitRangeMax + "] is invalid, using " + WAIT_RANGE_MAX_DEFAULT);
            waitRangeMax = WAIT_RANGE_MAX_DEFAULT;
        }
        // Swap wait range if min/max are reversed
        if (waitRangeMax < waitRangeMin) {
            int tmpMax = waitRangeMax;
            waitRangeMax = waitRangeMin;
            waitRangeMin = tmpMax;
        }

        if (retryLimit < 0) {
            log.debug("Retry limit [" + retryLimit + "] is invalid, using " + RETRY_LIMIT_DEFAULT);
            retryLimit = RETRY_LIMIT_DEFAULT;
        }

        int waitRandomLimit = waitRangeMax - waitRangeMin + 1;
        int tryCount = 0;
        Random random = new Random();
        HttpResponse response = null;
        int status = 0;

        // Loop until we either break or throw an exception
        while (true) {
            response = client.execute(request); // send HTTP request
            status = response.getStatusLine().getStatusCode();

            // If status code is HTTP 409 Conflict error, retry if retry limit is not reached.
            if (status == 409) {
                ++tryCount;
                // Throw error if retry limit is reached.
                if (tryCount > retryLimit) {
                    // Log message if retry limit is not 0 and string parameter != null.
                    if (retryLimit > 0 && logMethodName != null) {
                        System.out.println("Reached retry limit");
                    }
                    // reached retry limit
                    throw new IOException(String.format("%d %s\n%s",
                            response.getStatusLine().getStatusCode(),
                            response.getStatusLine().getReasonPhrase(),
                            getBody(response)));
                }

                // Log retry if logMethodName was specified.
                if (logMethodName != null) {
                    System.out.println("Retrying " + logMethodName);
                }
                // wait random number of milliseconds within specified range
                long wait = random.nextInt(waitRandomLimit)+waitRangeMin;
                try {
                    Thread.sleep(wait);
                }
                catch (InterruptedException e1) {
                    System.out.println(e1);
                }
            }
            else if(status > 299) {
                throw new IOException(String.format("%d %s\n%s",
                        response.getStatusLine().getStatusCode(),
                        response.getStatusLine().getReasonPhrase(),
                        getBody(response)));
            }
            else {
                break;  // return response from the HTTP request
            }
        }
        return response;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @param request
     */
    protected void releaseConnection(HttpRequestBase request) {
        request.releaseConnection();
    }

    /**
     * this method is misnamed; it encodes segments of user-inputted characters
     *
     * @param path String input to be encoded.
     *
     * @return String
     */
    protected String encodePath(String path) {
        String result;
        try {
            result = sanitizePathSegment(path);
        }
        catch (Exception e) {
            log.debug("the user input " + path + " could not be sanitized. defaulting to user input", e);
            result = path;
        }
        return result;
    }
    /**
     * @param path String input to be encoded.
     *
     * @return String
     *
     * @throws URISyntaxException
     * @throws EncoderException
     */
    protected String sanitizePathSegment(String path)
    throws URISyntaxException, EncoderException {
        PercentCodec encoder = new PercentCodec();
        return encoder.encode(path);
    }

    /**
     * Helper method to get a string/string map from a JSONObject so that JSON returned from REST
     * calls can be set as output properties.
     *
     * @param object JSONobject to be converted into a Property Map.
     *
     * @return Map
     */
    public Map<String, String> getJSONAsProperties(JSONObject object) {
        Map<String, String> result = new HashMap<String, String>();

        try {
            Iterator<?> iterator = object.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                Object value = object.get(key);
                if (!(value instanceof JSONObject) && !(value instanceof JSONArray)) {
                    result.put(key, String.valueOf(value));
                }
            }
        }
        catch (JSONException e) {
            throw new RuntimeException("Failed to convert from JSON to map", e);
        }

        return result;
    }
}
