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

import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlNMTOKEN;
import org.apache.xmlbeans.XmlObject;
import tools.util.JarUtil;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.tranxml.tranXML.version40.CarLocationMessageDocument;


/**
 *
 *
 */
public class GetObjectTest extends BasicCursorTestCase {
    public GetObjectTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(GetObjectTest.class);
    }

    public void testClassPath() throws Exception {
        String sClassPath = System.getProperty("java.class.path");
        int i = sClassPath.indexOf(Common.CARLOCATIONMESSAGE_JAR);
        assertTrue(i >= 0);
    }

    public void testGetObjectFromSTARTDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(
                 JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        assertEquals(true,
                m_xc.getObject() instanceof CarLocationMessageDocument);
    }

    public void testGetObjectFromSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(
                JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        assertEquals(true,
                m_xc.getObject() instanceof CarLocationMessageDocument.CarLocationMessage);
    }

    public void testGetObjectFromATTR() throws Exception {
        m_xo =
                XmlObject.Factory.parse(
                        JarUtil.getResourceFromJar("xbean/xmlcursor/po.xml"));
        m_xc = m_xo.newCursor();
        String sQuery=
                 "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\";  "+
                 "$this//po:shipTo";
        m_xc.selectPath( sQuery );
        m_xc.toNextSelection();
        m_xc.toFirstAttribute();
        assertEquals(true, m_xc.getObject() instanceof XmlNMTOKEN);
    }

    public void testGetObjectFromEND() throws Exception {
        m_xo = XmlObject.Factory.parse(
                JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.END);
        assertEquals(null, m_xc.getObject());
    }

    public void testGetObjectFromENDDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(
                JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        m_xc.toEndDoc();
        assertEquals(null, m_xc.getObject());
    }

    public void testGetObjectFromNAMESPACE() throws Exception {
        m_xo = XmlObject.Factory.parse(
                JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.NAMESPACE);
        assertEquals(null, m_xc.getObject());
    }

    public void testGetObjectFromPROCINST() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_PROCINST);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.PROCINST);
        assertEquals(null, m_xc.getObject());
    }

    public void testGetObjectFromCOMMENT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_COMMENT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.COMMENT);
        assertEquals(null, m_xc.getObject());
    }

    public void testGetObjectFromTEXT() throws Exception {
        m_xo = XmlObject.Factory.parse(
                JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertEquals(null, m_xc.getObject());
    }
}

