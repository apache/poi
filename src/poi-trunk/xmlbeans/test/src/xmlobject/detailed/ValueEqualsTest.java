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
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlOptions;

import java.util.*;
import javax.xml.namespace.QName;


import java.util.Vector;

import xmlcursor.common.*;

import java.net.URL;

import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import org.tranxml.tranXML.version40.FleetIDDocument;
import tools.util.Util;
import tools.util.JarUtil;


/**
 *
 *
 */
public class ValueEqualsTest extends BasicCursorTestCase {
    public ValueEqualsTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(ValueEqualsTest.class);
    }

    public void testClassPath() throws Exception {
        String sClassPath = System.getProperty("java.class.path");
        int i = sClassPath.indexOf(Common.CARLOCATIONMESSAGE_JAR);
        assertTrue(i >= 0);
    }

    public void testValueEqualsTrue() throws Exception {
        CarLocationMessageDocument clmDoc = (CarLocationMessageDocument)
                XmlObject.Factory.parse(
                           JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xo = XmlObject.Factory.parse(
                   JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        assertEquals(true, clmDoc.valueEquals(m_xo));
    }

}

