/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package  xmlcursor.detailed;

import junit.framework.*;

import  xmlcursor.common.Common;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;

import test.xbean.xmlcursor.location.LocationDocument.Location;
import test.xbean.xmlcursor.location.LocationDocument;


/**
 *
 *
 */
public class ObjectCursorInteractionTest extends TestCase {
    public ObjectCursorInteractionTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(ObjectCursorInteractionTest.class);
    }

    public void testClassPath() throws Exception {
        String sClassPath = System.getProperty("java.class.path");
        int i = sClassPath.indexOf(Common.XMLCURSOR_JAR);
        assertTrue(i >= 0);
    }

    public void testObjectNullEffectOnCursor() throws Exception {
        String sNamespace = "";
        String sXml =
                "<loc:Location xmlns:loc=\"http://xbean.test/xmlcursor/Location\""
                + sNamespace +
                "><loc:CityName>DALLAS</loc:CityName><StateCode>TX</StateCode>" +
                "</loc:Location>";

       // LocationDocument locDoc = (LocationDocument) XmlObject.Factory.parse(sXml);
        LocationDocument locDoc = LocationDocument.Factory.parse(sXml);
        Location loc = locDoc.getLocation();
        XmlCursor xc0 = loc.newCursor();
        assertEquals("DALLAS", loc.getCityName());
        loc = null;
        System.gc();
        try {
            Thread.sleep(1000);
            xc0.toFirstChild();
            assertEquals("DALLAS", xc0.getTextValue());
        }
        catch (InterruptedException e) {
        }
        finally {
            xc0.dispose();
        }
    }

    public void testCursorDisposalEffectOnObject() throws Exception {
        String sNamespace = "xmlns:loc=\"http://xbean.test/xmlcursor/Location\"";
        String sXml = "<loc:Location " + sNamespace + ">" +
                "<loc:CityName>DALLAS</loc:CityName><loc:StateCode>TX</loc:StateCode></loc:Location>";
        LocationDocument locDoc = LocationDocument.Factory.parse(
                sXml);
        assertEquals(true, locDoc.validate());
        Location loc0 = locDoc.getLocation();
        Location loc1 = locDoc.getLocation();
        XmlCursor xc0 = loc0.newCursor();
        XmlCursor xc1 = loc1.newCursor();

        xc0.toFirstChild();
        xc1.toFirstChild();
        xc0.setTextValue("AUSTIN");
        try {
            assertEquals("AUSTIN", loc0.getCityName());
            loc1.setCityName("SAN ANTONIO");
            xc0.dispose();
            assertEquals("SAN ANTONIO", xc1.getTextValue());
            xc1.setTextValue("HOUSTON");
            xc1.dispose();
            assertEquals("HOUSTON", loc0.getCityName());
        }
        finally {
            xc0.dispose();
            xc1.dispose();
        }
    }

    public void testObjectRefAssignmentEffectOnCursor() throws Exception {
        String sXml0 =
                "<loc:Location xmlns:loc=\"http://xbean.test/xmlcursor/Location\">" +
                "<loc:CityName>DALLAS</loc:CityName>" +
                "<loc:StateCode>TX</loc:StateCode>" +
                "</loc:Location>";
        String sXml1 =
                "<loc:Location xmlns:loc=\"http://xbean.test/xmlcursor/Location\">" +
                "<loc:PostalCode>90210</loc:PostalCode>" +
                "<loc:CountryCode>US</loc:CountryCode>" +
                "</loc:Location>";
        LocationDocument locDoc0 = LocationDocument.Factory.parse(
                sXml0);
        Location loc0 = locDoc0.getLocation();
        XmlCursor xc0 = loc0.newCursor();

        LocationDocument locDoc1 = (LocationDocument) XmlObject.Factory.parse(
                sXml1);
        Location loc1 = locDoc1.getLocation();

        assertEquals("DALLAS", loc0.getCityName());
        assertEquals("TX", loc0.getStateCode());
        assertEquals(null, loc0.getPostalCode());
        assertEquals(null, loc0.getCountryCode());

        loc0 = loc1;

        assertEquals(null, loc0.getCityName());
        assertEquals(null, loc0.getStateCode());
        assertEquals("90210", loc0.getPostalCode());
        assertEquals("US", loc0.getCountryCode());

        try {
            assertEquals(sXml0, xc0.xmlText());
            xc0 = loc0.newCursor();
            assertEquals(sXml1, xc0.xmlText());
        }
        finally {
            xc0.dispose();
        }
    }

    public void testCursorRefAssignmentEffectOnObject() throws Exception {
        String sXml0 =
                "<loc:Location xmlns:loc=\"http://xbean.test/xmlcursor/Location\">" +
                "<loc:CityName>DALLAS</loc:CityName>" +
                "<loc:StateCode>TX</loc:StateCode>" +
                "</loc:Location>";
        LocationDocument locDoc0 = LocationDocument.Factory.parse(
                sXml0);
        Location loc0 = locDoc0.getLocation();
        XmlCursor xc0 = loc0.newCursor();

        String sXml1 =
                "<loc:Location xmlns:loc=\"http://xbean.test/xmlcursor/Location\">" +
                "<loc:PostalCode>90210</loc:PostalCode>" +
                "<loc:CountryCode>US</loc:CountryCode>" +
                "</loc:Location>";
        LocationDocument locDoc1 = LocationDocument.Factory.parse(
                sXml1);
        Location loc1 = locDoc1.getLocation();
        XmlCursor xc1 = loc1.newCursor();

        try {
            assertEquals("DALLAS", loc0.getCityName());
            assertEquals("TX", loc0.getStateCode());
            assertEquals(null, loc0.getPostalCode());
            assertEquals(null, loc0.getCountryCode());

            xc0 = xc1;

            assertEquals("DALLAS", loc0.getCityName());
            assertEquals("TX", loc0.getStateCode());
            assertEquals(null, loc0.getPostalCode());
            assertEquals(null, loc0.getCountryCode());

            loc0 = (Location) xc0.getObject();

            assertEquals(null, loc0.getCityName());
            assertEquals(null, loc0.getStateCode());
            assertEquals("90210", loc0.getPostalCode());
            assertEquals("US", loc0.getCountryCode());

        }
        finally {
            xc0.dispose();
            xc1.dispose();
        }
    }

}

