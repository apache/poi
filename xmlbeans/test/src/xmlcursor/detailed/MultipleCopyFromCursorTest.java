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
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlCursor.TokenType;


import xmlcursor.common.*;

import tools.util.JarUtil;
import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import org.tranxml.tranXML.version40.GeographicLocationDocument.GeographicLocation;
import org.tranxml.tranXML.version40.CodeList309;
import org.tranxml.tranXML.version40.LocationIdentifierDocument.LocationIdentifier;

import java.util.ArrayList;


/**
 *
 *
 */
public class MultipleCopyFromCursorTest extends TestCase {
    public MultipleCopyFromCursorTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(MultipleCopyFromCursorTest.class);
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
            for (int i = 0; i < 3; i++) {
                aCursors[i] = xc.newCursor();
                xc.toNextSelection();
            }
            xc.toStartDoc();
            xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT +
                          "$this//GeographicLocation");
            assertTrue( xc.getSelectionCount() > 0 );
            assertTrue( xc.toNextSelection());
            aCursors[0].toLastChild();
            assertEquals("TX", aCursors[0].getTextValue());

            aCursors[0].toNextToken();
            aCursors[0].toNextToken();
            aCursors[0].toNextToken();
            aCursors[0].toNextToken();
            assertEquals(TokenType.END, aCursors[0].currentTokenType());

            aCursors[0].beginElement("LocationIdentifier",
                                      "http://www.tranxml.org/TranXML/Version4.0");
            aCursors[0].insertAttributeWithValue("Qualifier", "FR");
             aCursors[0].toEndToken();
            aCursors[0].toNextToken();//move past the end token
            aCursors[0].insertElementWithText("CountrySubdivisionCode",
                                              "http://www.tranxml.org/TranXML/Version4.0",
                                              "xyz");
            aCursors[0].toCursor(xc);
            GeographicLocation gl = (GeographicLocation) aCursors[0].getObject();
            XmlOptions validateOptions=new XmlOptions();
            ArrayList errors=new ArrayList();
            validateOptions.setErrorListener(errors);
            try{
            assertEquals(true, gl.validate(validateOptions));
            }catch (Throwable t){
                StringBuffer sb=new StringBuffer();
               for (int i = 0; i < errors.size(); i++) {
                XmlError error = (XmlError) errors.get(i);

                sb.append("Message: " + error.getMessage() + "\n");
                if (error.getCursorLocation() != null)
                    System.out.println("Location of invalid XML: " +
                            error.getCursorLocation().xmlText() + "\n");
            }
                throw new Exception(" Validation failed "+sb.toString());
            }

            assertEquals("DALLAS", gl.getCityName().getStringValue());
            assertEquals("TX", gl.getStateOrProvinceCode());
            LocationIdentifier li = gl.getLocationIdentifier();
            assertNotNull("Cursor0: LocationIdentifier unexpectedly null", li);
            assertEquals(CodeList309.FR, gl.getLocationIdentifier().getQualifier());
            assertEquals("xyz", gl.getCountrySubdivisionCode());


            for (int i = 1; i < 3; i++) {
                aCursors[i].removeXml();
                aCursors[0].copyXml(aCursors[i]);
                // must move to PrevElement to get to the START of the copied section.
                aCursors[i].toPrevSibling();

                gl = (GeographicLocation) aCursors[i].getObject();

                assertEquals("DALLAS", gl.getCityName().getStringValue());
                assertEquals("TX", gl.getStateOrProvinceCode());
                li = gl.getLocationIdentifier();
                assertNotNull("Cursor " + i + ": LocationIdentifier unexpectedly null", li);
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


