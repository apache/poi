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


package xmlcursor.checkin;

import junit.framework.*;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor.TokenType;

import xmlcursor.common.*;

import tools.util.JarUtil;


/**
  *
 *
 */
public class SelectPathTest extends BasicCursorTestCase {
    public SelectPathTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(SelectPathTest.class);
    }

   /**
        *$BUGBUG: Eric's engine doesn't send to Jaxen appropriately
    *
   public void testSelectPathFromEND() throws Exception {
        m_xo = XmlObject.Factory.parse(
                   JarUtil.getResourceFromJar(Common.XMLCASES_JAR,
                        Common.TRANXML_FILE_XMLCURSOR_PO));
        String ns="declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"";
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.END);
        m_xc.selectPath(ns+" $this//city");
        assertEquals(0, m_xc.getSelectionCount());
    }

    public void testSelectPathFromENDDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(
                 JarUtil.getResourceFromJar(Common.XMLCASES_JAR,
                        Common.TRANXML_FILE_XMLCURSOR_PO));
        m_xc = m_xo.newCursor();
        String ns="declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"";
        toNextTokenOfType(m_xc, TokenType.ENDDOC);
        m_xc.selectPath(ns+" .//po:city");
        assertEquals(0, m_xc.getSelectionCount());
    }
        */
    public void testSelectPathNamespace() throws Exception {
        m_xo = XmlObject.Factory.parse(
                JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        String sLocalPath =".//FleetID";
        m_xc.selectPath(sLocalPath);
        assertEquals(0, m_xc.getSelectionCount());
        m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT +
                        sLocalPath);
        assertEquals(1, m_xc.getSelectionCount());
    }

    public void testSelectPathCaseSensitive() throws Exception {
        m_xo = XmlObject.Factory.parse(
                 JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        m_xc = m_xo.newCursor();
        String ns="declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\";";
        m_xc.selectPath(ns+" .//po:ciTy");
        assertEquals(0, m_xc.getSelectionCount());
        m_xc.selectPath(ns+" .//po:city");
        assertEquals(2, m_xc.getSelectionCount());
    }

    public void testSelectPathReservedKeyword() throws Exception {
        m_xo = XmlObject.Factory.parse(
                JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        m_xc = m_xo.newCursor();
        String ns="declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\";";
        m_xc.selectPath(ns+" .//po:item");
        assertEquals(2, m_xc.getSelectionCount());
    }

    public void testSelectPathNull() throws Exception {
        m_xo = XmlObject.Factory.parse(
                 JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        m_xc = m_xo.newCursor();
        // TODO: surround with appropriate t-c once ericvas creates the exception type
        // see bugs 18009 and/or 18718
        try {
            m_xc.selectPath(null);
            fail("Expected RuntimeException");
        } catch (RuntimeException re) {
        }
        assertTrue(true);
    }

    public void testSelectPathInvalidXPath() throws Exception {
        m_xo = XmlObject.Factory.parse(
                 JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        m_xc = m_xo.newCursor();
        // TODO: surround with appropriate t-c once ericvas creates the exception type
        // see bugs 18009 and/or 18718
        try {
            m_xc.selectPath("&GARBAGE");
            assertEquals(0,m_xc.getSelectionCount());
            fail("Expected RuntimeException");
        } catch (RuntimeException re) {
        }
        assertTrue(true);
    }

}

