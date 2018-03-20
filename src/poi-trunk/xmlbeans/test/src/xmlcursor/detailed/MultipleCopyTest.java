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
import xmlcursor.common.*;


import tools.util.JarUtil;
import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import org.tranxml.tranXML.version40.GeographicLocationDocument.GeographicLocation;
import org.tranxml.tranXML.version40.CodeList309;
import org.tranxml.tranXML.version40.LocationIdentifierDocument.LocationIdentifier;


/**
 *
 *
 */
public class MultipleCopyTest extends TestCase {
    public MultipleCopyTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(MultipleCopyTest.class);
    }

    public void testClassPath() throws Exception {
        String sClassPath = System.getProperty("java.class.path");
        int i = sClassPath.indexOf(Common.CARLOCATIONMESSAGE_JAR);
        assertTrue(i >= 0);
    }

    public void testMultipleCopy() throws Exception {
        CarLocationMessageDocument clm =
                (CarLocationMessageDocument) XmlObject.Factory.parse(
                        JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        assertNotNull(clm);
        XmlCursor xc = clm.newCursor();
        XmlCursor[] aCursors = new XmlCursor[3];
        try {
            xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT +
                          "$this//GeographicLocation");
            xc.toNextSelection();

            GeographicLocation gl = (GeographicLocation) xc.getObject();
            for (int i = 0; i < 3; i++) {
                aCursors[i] = xc.newCursor();
                xc.toNextSelection();
            }
            LocationIdentifier li = gl.addNewLocationIdentifier();
            li.setQualifier(CodeList309.FR);

            gl.setLocationIdentifier(li);
            assertEquals(CodeList309.FR, gl.getLocationIdentifier().getQualifier());

            gl.setCountrySubdivisionCode("xyz");

            for (int i = 1; i < 3; i++) {
                aCursors[i].removeXml();
                aCursors[0].copyXml(aCursors[i]);
                // must move to PrevElement to get to the START of the copied section.
                aCursors[i].toPrevSibling();

                gl = (GeographicLocation) aCursors[i].getObject();

                assertEquals("DALLAS", gl.getCityName().getStringValue());
                assertEquals("TX", gl.getStateOrProvinceCode());
                assertEquals(CodeList309.FR, gl.getLocationIdentifier().getQualifier());
                assertEquals("xyz", gl.getCountrySubdivisionCode());
            }
        } finally {
            xc.dispose();
            for (int i = 0; i < 3; i++) {
                aCursors[i].dispose();
            }
        }
    }
}

