/**
 * (c) Copyright IBM Corporation 2011, 2017.
 * (c) Copyright HCL Technologies Ltd. 2018. All Rights Reserved.
 * This is licensed under the following license.
 * The Apache Version 2.0 License (https://www.apache.org/licenses/LICENSE-2.0.txt)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */
package com.urbancode.ud.client;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.codec.EncoderException;
import org.junit.Before;
import org.junit.Test;

public class URLEncodingTest {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private UDRestClient client;

    @Before
    public void createClient() throws URISyntaxException {
        client = new UDRestClient(new URI("https://localhost:8443"), "admin", "admin");
    }

    @Test
    public void testSanitizePath()
    throws URISyntaxException, EncoderException {
        assertEquals("basecase", client.sanitizePathSegment("basecase"));
        assertEquals("%20", client.sanitizePathSegment(" "));
        assertEquals("%21", client.sanitizePathSegment("!"));
        assertEquals("%22", client.sanitizePathSegment("\""));
        assertEquals("%23", client.sanitizePathSegment("#"));
        assertEquals("%24", client.sanitizePathSegment("$"));
        assertEquals("%25", client.sanitizePathSegment("%"));
        assertEquals("%26", client.sanitizePathSegment("&"));
        assertEquals("%27", client.sanitizePathSegment("'"));
        assertEquals("%28", client.sanitizePathSegment("("));
        assertEquals("%29", client.sanitizePathSegment(")"));
        assertEquals("%2A", client.sanitizePathSegment("*"));
        assertEquals("%2B", client.sanitizePathSegment("+"));
        assertEquals("%2C", client.sanitizePathSegment(","));
        assertEquals("%2F", client.sanitizePathSegment("/"));
        assertEquals("%3A", client.sanitizePathSegment(":"));
        assertEquals("%3B", client.sanitizePathSegment(";"));
        assertEquals("%3D", client.sanitizePathSegment("="));
        assertEquals("%3F", client.sanitizePathSegment("?"));
        assertEquals("%40", client.sanitizePathSegment("@"));
        assertEquals("%5B", client.sanitizePathSegment("["));
        assertEquals("%5D", client.sanitizePathSegment("]"));
        assertEquals("-", client.sanitizePathSegment("-"));
        assertEquals("_", client.sanitizePathSegment("_"));
        assertEquals(".", client.sanitizePathSegment("."));
        assertEquals("~", client.sanitizePathSegment("~"));
        assertEquals("space%20case", client.sanitizePathSegment("space case"));
        assertEquals("other%20special%20chars%20%21%40%23%24%25%5E%2A%28%29%3Dtest", client.sanitizePathSegment("other special chars !@#$%^*()=test"));
        assertEquals("%2Fslash%20in%20name", client.sanitizePathSegment("/slash in name"));
        assertEquals("%2Ftwo%2Fslashes", client.sanitizePathSegment("/two/slashes"));
        assertEquals("co%3Alon", client.sanitizePathSegment("co:lon"));
        assertEquals("%2Fslashes%2Fand%20%3A%20colon", client.sanitizePathSegment("/slashes/and : colon"));
        assertEquals("%2Fslashesampersand%2F%26%3A%3A%20%3A", client.sanitizePathSegment("/slashesampersand/&:: :"));
        assertEquals("%2Fallspecialchars%2B%40%2F%3A%26%2B%2B%26%3A%20test", client.sanitizePathSegment("/allspecialchars+@/:&++&: test"));
    }
}
