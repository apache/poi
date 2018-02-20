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


package xmlobject.detailed;

import junit.framework.*;
import junit.framework.Assert.*;

import java.io.*;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlCursor.XmlBookmark;
import org.apache.xmlbeans.SchemaType;

import javax.xml.namespace.QName;

import xmlcursor.common.*;

import java.net.URL;

import tools.util.Util;
import tools.util.JarUtil;
import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import org.tranxml.tranXML.version40.GeographicLocationDocument.GeographicLocation;
import org.tranxml.tranXML.version40.CodeList309;
import org.tranxml.tranXML.version40.LocationIdentifierDocument.LocationIdentifier;


/**
 *
 *
 */
public class SetIdentityTest extends TestCase {
    public SetIdentityTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(SetIdentityTest.class);
    }

    public void testClassPath() throws Exception {
        String sClassPath = System.getProperty("java.class.path");
        int i = sClassPath.indexOf(Common.CARLOCATIONMESSAGE_JAR);
        assertTrue(i >= 0);
    }

    public void testSetIdentity() throws Exception {
        CarLocationMessageDocument clm =
                (CarLocationMessageDocument) XmlObject.Factory.parse(
                        JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        XmlCursor xc = clm.newCursor();

        xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT +
                      "$this//GeographicLocation");
        xc.toNextSelection();
        GeographicLocation gl = (GeographicLocation) xc.getObject();
        xc.dispose();
        LocationIdentifier li = gl.addNewLocationIdentifier();
        li.setQualifier(CodeList309.FR);
        CodeList309 cl309 = li.xgetQualifier();
        // setQualifier to itself, i.e. x == x
        li.xsetQualifier(cl309);
        gl.setLocationIdentifier(li);
        assertEquals(CodeList309.FR, gl.getLocationIdentifier().getQualifier());

    }

}

