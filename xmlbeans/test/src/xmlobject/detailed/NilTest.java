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

import org.apache.xmlbeans.XmlObject;

import xmlcursor.common.*;

import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import org.tranxml.tranXML.version40.CarLocationMessageDocument.CarLocationMessage;
import test.xbean.xmlcursor.purchaseOrder.PurchaseOrderDocument;
import knextest.bug38361.TestDocument;


import org.apache.xmlbeans.impl.values.XmlValueNotNillableException;
import tools.util.JarUtil;


/**
 *
 *
 */
public class NilTest extends BasicCursorTestCase {
    public NilTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(NilTest.class);
    }

    public void testClassPath() throws Exception {
        String sClassPath = System.getProperty("java.class.path");
        int i = sClassPath.indexOf(Common.CARLOCATIONMESSAGE_JAR);
        assertTrue(i >= 0);
        i = sClassPath.indexOf(Common.XMLCURSOR_JAR);
        assertTrue(i >= 0);
    }

    public void testIsNilFalse() throws Exception {
        CarLocationMessageDocument clmDoc = (CarLocationMessageDocument) XmlObject.Factory.parse(
                   JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        CarLocationMessage clm = clmDoc.getCarLocationMessage();
        assertEquals(false, clm.isNil());
    }

    public void testSetNilNillable() throws Exception {
        PurchaseOrderDocument pod = (PurchaseOrderDocument) XmlObject.Factory.parse(
                JarUtil.getResourceFromJar("xbean/xmlcursor/po.xml"));
        m_xo = pod.getPurchaseOrder().getShipTo().xgetName();
        m_xo.setNil();
        assertEquals(true, m_xo.isNil());
    }

    public void testSetNilNotNillable() throws Exception {
        CarLocationMessageDocument clmDoc = (CarLocationMessageDocument) XmlObject.Factory.parse(
                   JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        try {
            clmDoc.setNil();
            fail("Expected XmlValueNotNillableException");
        }
        catch (XmlValueNotNillableException xvnne) {
        }
        assertTrue(true);
    }

    /**
     * Test case for Radar bug: #38361
     */
    public void nillableTest() throws Exception {
        //Generates a xml document programatically
        TestDocument generated = TestDocument.Factory.newInstance();
        generated.addNewTest();
        generated.getTest().setNilSimple();
        generated.getTest().setNilDate();

        // Generate a xml document by parsing a string
        TestDocument parsed = TestDocument.Factory.parse("<tns:Test xmlns:tns='http://bug38361.knextest'>" +
                "<tns:Simple xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'/>" +
                "<tns:Date xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'/>" +
                "</tns:Test>");

        // Test generated xml doc properties
        assertTrue("Generated XML document is not valid", generated.validate());
        assertTrue("Generated: isNilSimple() failed",
                generated.getTest().isNilSimple());
        assertTrue("Generated: isNilDate() failed",
                generated.getTest().isNilDate());

        // Test parsed xml doc properties
        assertTrue("Parsed XML document is not valid", parsed.validate());
        assertTrue("Parsed: isNilSimple() failed",
                parsed.getTest().isNilSimple());
        assertTrue("Parsed: isNilDate() failed", parsed.getTest().isNilDate());
    }

}

