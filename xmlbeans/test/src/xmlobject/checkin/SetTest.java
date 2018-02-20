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
package xmlobject.checkin;

import java.util.*;

import junit.framework.*;

import xmlcursor.common.*;


import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.XmlDate;
import org.apache.xmlbeans.XmlCalendar;

import test.xbean.xmlcursor.purchaseOrder.PurchaseOrderDocument;
import test.xbean.xmlcursor.purchaseOrder.USAddress;

import tools.util.JarUtil;


/**
 *
 *
 */
public class SetTest extends BasicCursorTestCase {
    public SetTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(SetTest.class);
    }

    public void testClassPath() throws Exception {
        String sClassPath = System.getProperty("java.class.path");
        int i = sClassPath.indexOf(Common.CARLOCATIONMESSAGE_JAR);
        assertTrue(i >= 0);
        i = sClassPath.indexOf(Common.XMLCURSOR_JAR);
        assertTrue(i >= 0);
    }

    public void testSetFromSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        XmlObject xo = m_xc.getObject();
        xo.set(XmlString.Factory.newValue("newtext"));
        assertEquals("newtext", m_xc.getTextValue());
    }

    public void testSetFromATTR() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        XmlObject xo = m_xc.getObject();
        xo.set(XmlString.Factory.newValue(" new attr text "));
        assertEquals(" new attr text ", m_xc.getTextValue());
    }

    public void testSetFromSTARTstronglyTyped() throws Exception {
       PurchaseOrderDocument pod = (PurchaseOrderDocument) XmlObject.Factory.parse(
                JarUtil.getResourceFromJar("xbean/xmlcursor/po.xml"));
        XmlString xcomment = pod.getPurchaseOrder().xgetComment();
        xcomment.setStringValue("new comment text");
        assertEquals("new comment text", pod.getPurchaseOrder().getComment());
    }

    public void testSetFromATTRstronglyTyped() throws Exception {
        PurchaseOrderDocument pod = (PurchaseOrderDocument) XmlObject.Factory.parse(
                JarUtil.getResourceFromJar("xbean/xmlcursor/po.xml"));
        XmlDate xorderDate = pod.getPurchaseOrder().xgetOrderDate();

        assertFalse(xorderDate==null);

        Calendar d = new XmlCalendar(new java.util.Date());
        xorderDate.setCalendarValue(d);

        // compare year, month, day of the xsd:date type
        assertEquals(d.get(Calendar.YEAR),
                pod.getPurchaseOrder().getOrderDate().get(Calendar.YEAR));
        assertEquals(d.get(Calendar.MONTH),
                pod.getPurchaseOrder().getOrderDate().get(Calendar.MONTH));
        assertEquals(d.get(Calendar.DAY_OF_MONTH),
                pod.getPurchaseOrder().getOrderDate().get(
                        Calendar.DAY_OF_MONTH));
    }

    public void testSetFromFixedATTR() throws Exception {

        PurchaseOrderDocument pod = (PurchaseOrderDocument) XmlObject.Factory.parse(
                JarUtil.getResourceFromJar("xbean/xmlcursor/po.xml"));
        USAddress usa = pod.getPurchaseOrder().getShipTo();
         assertFalse(usa==null);

        XmlString xcountry = usa.xgetCountry();

        xcountry.setStringValue("UK");


        assertEquals(false, pod.validate());
    }

    public void testSetFromComplexType() throws Exception {

        PurchaseOrderDocument pod = (PurchaseOrderDocument) XmlObject.Factory.parse(
                JarUtil.getResourceFromJar("xbean/xmlcursor/po.xml"));
        USAddress usa = pod.getPurchaseOrder().getShipTo();
        assertFalse(usa==null);
        usa.set(
                USAddress.Factory.parse(
                        "<shipTo country=\"UK\"><name>Fred</name><street>paved</street><city>town</city><state>AK</state><zip>00000</zip></shipTo>"));

        // assertTrue(true);
        assertEquals(false, pod.validate());
    }
}

