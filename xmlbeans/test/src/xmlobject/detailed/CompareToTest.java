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

import java.math.BigDecimal;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;

//import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;

import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import org.tranxml.tranXML.version40.EventStatusDocument.EventStatus;
import org.tranxml.tranXML.version40.GeographicLocationDocument.GeographicLocation;
import org.tranxml.tranXML.version40.CityNameDocument.CityName;
import org.tranxml.tranXML.version40.ETADocument.ETA;

import test.xbean.xmlcursor.purchaseOrder.PurchaseOrderDocument;
import tools.util.JarUtil;


/**
 *
 *
 */
public class CompareToTest extends TestCase {
    public CompareToTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(CompareToTest.class);
    }

    /*public void testClassPath() throws Exception {
        String sClassPath = System.getProperty("java.class.path");
        int i = sClassPath.indexOf(Common.CARLOCATIONMESSAGE_JAR);
        assertTrue(i >= 0);
        i = sClassPath.indexOf(Common.XMLCURSOR_JAR);
        assertTrue(i >= 0);
    }
   */
    public void testCompareToEquals() throws Exception {
        CarLocationMessageDocument clmDoc = (CarLocationMessageDocument) XmlObject.Factory.parse(
                   JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        EventStatus[] aEventStatus = clmDoc.getCarLocationMessage()
                .getEventStatusArray();
        if (aEventStatus.length < 1) {
            fail(
                    "Unexpected: Missing EventStatus element.  Test harness failure.");
        } else {
            GeographicLocation gl = aEventStatus[0].getGeographicLocation();
            CityName cname0 = gl.getCityName();
            ETA eta = aEventStatus[0].getETA();
            CityName cname1 = eta.getGeographicLocation().getCityName();
            assertTrue(cname0.valueEquals(cname1));
            try {
                assertTrue(XmlObject.EQUAL == cname0.compareTo(cname1));
                fail("Expected ClassCastException.");
            }
            catch (ClassCastException e) {
                assertTrue(true);
            }
        }
    }

    public void testCompareToNull() throws Exception {
        m_xo = XmlObject.Factory.parse(
                   JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        try {
            assertEquals(XmlObject.NOT_EQUAL, m_xo.compareTo(null));
            assertTrue(false);
        }
        catch (ClassCastException e) {
            assertTrue(true);
        }
    }


    public void testCompareToLessThan() throws Exception {
//        PurchaseOrderDocument poDoc = (PurchaseOrderDocument) XmlObject.Factory.parse(
   //             Common.XML_PURCHASEORDER);
           PurchaseOrderDocument poDoc = (PurchaseOrderDocument) XmlObject.Factory.parse(
              JarUtil.getResourceFromJar("xbean/xmlcursor/po.xml"));

        try {
            BigDecimal bdUSPrice0 = poDoc.getPurchaseOrder().getItems()
                    .getItemArray(0)
                    .getUSPrice();
            BigDecimal bdUSPrice1 = poDoc.getPurchaseOrder().getItems()
                    .getItemArray(1)
                    .getUSPrice();
            assertEquals(XmlObject.LESS_THAN, bdUSPrice1.compareTo(bdUSPrice0));
        }
        catch (NullPointerException npe) {
            fail("Unexpected instance document.  Harness failure.");
        }
    }

    public void testCompareToGreaterThan() throws Exception {
        PurchaseOrderDocument poDoc = (PurchaseOrderDocument)
                XmlObject.Factory.parse(
                JarUtil.getResourceFromJar("xbean/xmlcursor/po.xml"));
        try {
            BigDecimal bdUSPrice0 = poDoc.getPurchaseOrder().getItems()
                    .getItemArray(0)
                    .getUSPrice();
            BigDecimal bdUSPrice1 = poDoc.getPurchaseOrder().getItems()
                    .getItemArray(1)
                    .getUSPrice();
            assertEquals(XmlObject.GREATER_THAN,
                    bdUSPrice0.compareTo(bdUSPrice1));
        }
        catch (NullPointerException npe) {
            fail("Unexpected instance document.  Harness failure.");
        }
    }


    public void testCompareToString() throws Exception {
        m_xo = XmlObject.Factory.parse(
                   JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        try {
            assertEquals(0, m_xo.compareTo(""));
            fail("Expected ClassCastException");
        }
        catch (ClassCastException cce) {
        }
        assertTrue(true);
    }

    public void testCompareValue() throws Exception {
        m_xo = XmlObject.Factory.parse(
                   JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        XmlObject xo = m_xc.getObject();
        assertEquals(XmlObject.NOT_EQUAL, m_xo.compareValue(xo));
    }

    private XmlObject m_xo;
    private XmlCursor m_xc;

}

