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


package xmlobject.xmlloader.detailed;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.xmlbeans.*;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import org.tranxml.tranXML.version40.GeographicLocationDocument.GeographicLocation;
import tools.util.JarUtil;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;


public class XmlLoaderMiscTest extends BasicCursorTestCase {
    public XmlLoaderMiscTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(XmlLoaderMiscTest.class);
    }

    public void testClassPath() throws Exception {
        String sClassPath = System.getProperty("java.class.path");
        int i = sClassPath.indexOf(Common.CARLOCATIONMESSAGE_JAR);
        assertTrue(i >= 0);
    }

    public void testNewInstance() throws Exception {
        m_xo = XmlObject.Factory.newInstance();
        m_xc = m_xo.newCursor();
        assertEquals(TokenType.STARTDOC, m_xc.currentTokenType());
        m_xc.toNextToken();
        assertEquals(TokenType.ENDDOC, m_xc.currentTokenType());
    }

    public void testTypeForClass() throws Exception {
        m_xc = XmlObject.Factory.parse(JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM)).newCursor();
        m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + "$this//GeographicLocation");
        m_xc.toNextSelection();
        GeographicLocation gl = (GeographicLocation) m_xc.getObject();
        assertNotNull(gl.schemaType());
        assertEquals(gl.schemaType(), XmlBeans.typeForClass(GeographicLocation.class));
    }

    public void testGetBuiltInTypeSystem() throws Exception {
        SchemaTypeSystem sts = XmlBeans.getBuiltinTypeSystem();
        if (sts == null) {
            fail("XmlBeans.getBuiltinTypeSystem() returned null");
        }
        String sSchemaURI = "http://www.w3.org/2001/XMLSchema";
        QName name = new QName(sSchemaURI, "dateTime");
        SchemaType stDateTime = sts.findType(name);
        assertEquals(14, stDateTime.getBuiltinTypeCode());
    }

    public void testTypeLoaderUnion() throws Exception {
        System.out.println("testTypeLoaderUnion not implemented");
        // TODO
    }

    public void testTypeLoaderForClassLoader() throws Exception {

        SchemaTypeLoader stl = XmlBeans.typeLoaderForClassLoader(CarLocationMessageDocument.class.getClassLoader());
        if (stl == null)
            fail("typeLoaderForClassLoader failed with CarLocationMessageDocument.class");
        m_xo = stl.parse(JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM), null, null);
        m_xc = m_xo.newCursor();
        m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + "$this//FleetID");
        m_xc.toNextSelection();
        assertEquals("FLEETNAME", m_xc.getTextValue());
    }

    public void testGetContextTypeLoader() throws Exception {
        SchemaTypeLoader stl = XmlBeans.getContextTypeLoader();
        if (stl == null)
            fail("getContextTypeLoader failed");

        Vector vThreads = new Vector();
        Set STLset = Collections.synchronizedSet(new HashSet());
        for (int i = 0; i < 10000; i++) {
            Thread t = new BogusThread(STLset);
            vThreads.add(t);
            t.start();
        }
        for (int i = 0; i < 10000; i++) {
            ((BogusThread) vThreads.elementAt(i)).join();
        }
        // each thread should create a unique type loader.
        // so count of objects in set should be 10000
        assertEquals(10000, STLset.size());
    }


    public class BogusThread extends Thread {
        private Set set;

        public BogusThread(Set set) {
            this.set = set;
        }

        public void run() {
            SchemaTypeLoader s = XmlBeans.getContextTypeLoader();
            set.add(s);
        }
    }
}
