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

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import xmlcursor.common.Common;

import tools.util.JarUtil;
import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import org.tranxml.tranXML.version40.EventStatusDocument.EventStatus;
import org.tranxml.tranXML.version40.GeographicLocationDocument.GeographicLocation;

/**
 *
 *
 */
public class CursorVsObjectInsertRemoveTest extends TestCase {
    public CursorVsObjectInsertRemoveTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(CursorVsObjectInsertRemoveTest.class);
    }

    public void testClassPath() throws Exception {
        String sClassPath = System.getProperty("java.class.path");
        int i = sClassPath.indexOf(Common.CARLOCATIONMESSAGE_JAR);
        assertTrue(i >= 0);
    }

    public void testInsertRemove() throws Exception {
        CarLocationMessageDocument clm =
                (CarLocationMessageDocument) XmlObject.Factory.parse(
                        JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        assertNotNull(clm);
        XmlCursor xc = clm.newCursor();
        xc.toFirstChild();
        xc.toFirstChild();
        xc.toNextSibling();
        xc.toNextSibling();
        assertEquals("EventStatus", xc.getName().getLocalPart());
        EventStatus eventStatus = (EventStatus) xc.getObject();
        assertNotNull("Expected non-null EventStatus object", eventStatus);
        String sEventStatusText = xc.getTextValue();
        GeographicLocation glDest = eventStatus.getDestination().getGeographicLocation();
        assertNotNull("Expected non-null GeographicLocation object", glDest);
        glDest.setPostalCode("90210");
        glDest.setCountryCode("US");
        XmlCursor xcPostalCode = glDest.xgetPostalCode().newCursor();
        XmlCursor xcCountryCode = glDest.xgetCountryCode().newCursor();
        try {
            assertEquals("90210", xcPostalCode.getTextValue());
            assertEquals("US", xcCountryCode.getTextValue());
            xcPostalCode.setTextValue("90310");
            xcPostalCode.toNextChar(2);
            assertEquals("90310", glDest.getPostalCode());
            eventStatus.getDestination().getGeographicLocation().unsetPostalCode();
            assertEquals(TokenType.START, xcPostalCode.currentTokenType());
            assertEquals("CountryCode", xcPostalCode.getName().getLocalPart());
            xcCountryCode.removeXml();
            assertEquals(sEventStatusText, xc.getTextValue());
        } finally {
            xcPostalCode.dispose();
            xcCountryCode.dispose();
        }
    }

}

