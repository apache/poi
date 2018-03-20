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
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.XmlCursor.TokenType;
import xmlcursor.common.*;
import tools.util.JarUtil;


/**
 *
 *
 */
public class MultipleCursorSetTest extends TestCase {
    public MultipleCursorSetTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(MultipleCursorSetTest.class);
    }

    public void testClassPath() throws Exception {
        String sClassPath = System.getProperty("java.class.path");
        int i = sClassPath.indexOf(Common.CARLOCATIONMESSAGE_JAR);
        assertTrue(i >= 0);
    }

    public void testMultipleCursorSet() throws Exception {
        XmlCursor xc = XmlObject.Factory.parse(JarUtil.getResourceFromJar(
                Common.TRANXML_FILE_CLM)).newCursor();
        xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT +
                      "$this//EquipmentNumber");
        xc.toNextSelection();
        XmlString xs = (XmlString) xc.getObject();
        assertEquals("123456", xs.getStringValue());
        assertEquals(TokenType.TEXT, xc.toNextToken());
        XmlCursor[] aCursors = new XmlCursor[6];
        for (int i = 0; i < 6; i++) {
            xc.toNextChar(1);
            aCursors[i] = xc.newCursor();
        }
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j != i && j < 6; j++) {
                assertEquals(false, aCursors[i].isAtSamePositionAs(aCursors[j]));
            }
        }
        xs.setStringValue("XYZ");
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                assertEquals(true, aCursors[i].isAtSamePositionAs(aCursors[j]));
            }
            // System.out.println(aCursors[i].currentTokenType());
            // assertEquals(null, aCursors[i].getTextValue());

            try {

                aCursors[i].getTextValue();
                fail("Expecting IllegalStateException");
            } catch (IllegalStateException e) {
            }


        }
        assertEquals("XYZ", xs.getStringValue());

    }

}

