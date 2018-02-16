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

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlTime;
import com.easypo.XmlPurchaseOrderDocumentBean;
import com.easypo.XmlCustomerBean;
import com.easypo.XmlLineItemBean;

import java.math.BigInteger;

import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import xint.test.PositionDocument;

public class AssortedTests extends TestCase
{
    public AssortedTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(AssortedTests.class); }
    
    // bug 27489
    public static void testSaverCharEscaping() throws XmlException
    {
        String newLine = System.getProperty( "line.separator" );
        XmlObject xdoc = XmlObject.Factory.parse("<test>something</test>");
        XmlCursor cur = xdoc.newCursor();
        cur.toFirstChild();
        // valid chars
        cur.setTextValue("<something or other:\u03C0\uD7FF>");
        Assert.assertEquals("<test>&lt;something or other:\u03C0\uD7FF></test>", xdoc.toString());
        
        // invalid chars - control chars, unicode surrogates, FFFF/FFFE, etc
        cur.setTextValue("<something\0or\1other:\u0045\uFFFE\uD800\uDFFF\uDB80\uDC00\u03C0\uD7FF\u001F>");
        Assert.assertEquals("<test>&lt;something?or?other:\u0045?????\u03C0\uD7FF?></test>", xdoc.toString());
    }
    
    // bug 26140/26104
    public static void testNoTypeInvalid() throws XmlException
    {
        XmlObject xdoc = XmlObject.Factory.parse("<test-no-type>something</test-no-type>");
        Assert.assertTrue("Untyped document should be invalid", !xdoc.validate());
        
        xdoc = XmlObject.Factory.parse("<x:blah xmlns:x=\"http://no-type.com/\"/>");
        Assert.assertTrue("Untyped document should be invalid", !xdoc.validate());
    }
    
    // bug 26790
    public static void testComplexSetter() throws XmlException
    {
        XmlPurchaseOrderDocumentBean xdoc = XmlPurchaseOrderDocumentBean.Factory.parse(
                "<purchase-order xmlns='http://openuri.org/easypo'>" +
                 "<customer>" +
                   "<name>David Bau</name>" +
                   "<address>100 Main Street</address>" + 
                 "</customer>" +
                 "<date>2003-05-18T11:50:00</date>" +
                 "<line-item>" +
                  "<description>Red Candy</description>" +
                  "<per-unit-ounces>0.423</per-unit-ounces>" +
                  "<quantity>4</quantity>" +
                 "</line-item>" +
                 "<line-item>" +
                  "<description>Blue Candy</description>" +
                  "<per-unit-ounces>5.0</per-unit-ounces>" +
                  "<quantity>1</quantity>" +
                 "</line-item>" +
                "</purchase-order>");
        // test copy-within doc
        XmlLineItemBean newItem = xdoc.getPurchaseOrder().addNewLineItem();
        newItem.set(xdoc.getPurchaseOrder().getLineItemArray(0));
        Assert.assertEquals(BigInteger.valueOf(4), xdoc.getPurchaseOrder().getLineItemArray(2).getQuantity());
        xdoc.getPurchaseOrder().setLineItemArray(0, xdoc.getPurchaseOrder().getLineItemArray(1));
        Assert.assertEquals(BigInteger.valueOf(1), xdoc.getPurchaseOrder().getLineItemArray(0).getQuantity());
        
        // test copy-between docs
        XmlLineItemBean anotherItem = XmlLineItemBean.Factory.parse(
                "<xml-fragment xmlns:ep='http://openuri.org/easypo' xmlns:xsi='http://wwww.w3.org/2001/XMLSchema-instance' xsi:type='line-item'>" +
                 "<ep:description>Yellow Balloon</ep:description>" +
                 "<ep:per-unit-ounces>0.001</ep:per-unit-ounces>" +
                 "<ep:quantity>200</ep:quantity>" +
                "</xml-fragment>");
        
        Assert.assertEquals("Yellow Balloon", anotherItem.getDescription());
        xdoc.getPurchaseOrder().setLineItemArray(1, anotherItem);
        
        Assert.assertEquals("Yellow Balloon", xdoc.getPurchaseOrder().getLineItemArray(1).getDescription());
        Assert.assertEquals(BigInteger.valueOf(1), xdoc.getPurchaseOrder().getLineItemArray(0).getQuantity());
        Assert.assertEquals(BigInteger.valueOf(200), xdoc.getPurchaseOrder().getLineItemArray(1).getQuantity());
        Assert.assertEquals(BigInteger.valueOf(4), xdoc.getPurchaseOrder().getLineItemArray(2).getQuantity());
        
        // test copy-to-self
        xdoc.getPurchaseOrder().setLineItemArray(1, xdoc.getPurchaseOrder().getLineItemArray(1));
        Assert.assertEquals("Yellow Balloon", xdoc.getPurchaseOrder().getLineItemArray(1).getDescription());
        Assert.assertEquals(BigInteger.valueOf(1), xdoc.getPurchaseOrder().getLineItemArray(0).getQuantity());
        Assert.assertEquals(BigInteger.valueOf(200), xdoc.getPurchaseOrder().getLineItemArray(1).getQuantity());
        Assert.assertEquals(BigInteger.valueOf(4), xdoc.getPurchaseOrder().getLineItemArray(2).getQuantity());
    }

    // bug 45338
    public static void testComplexGetter() throws Exception
    {
        XmlPurchaseOrderDocumentBean xdoc =
            XmlPurchaseOrderDocumentBean.Factory.parse(
                "<purchase-order xmlns='http://openuri.org/easypo'" +
                    " xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'" +
                    " xsi:type='foo'>" +
                 "<customer xsi:type='bar'>" +
                   "<name>David Bau</name>" +
                   "<address>100 Main Street</address>" +
                 "</customer>" +
                 "<date>2003-05-18T11:50:00</date>" +
                 "<line-item>" +
                  "<description>Blue Candy</description>" +
                  "<per-unit-ounces>5.0</per-unit-ounces>" +
                  "<quantity>1</quantity>" +
                 "</line-item>" +
                "</purchase-order>");

        Assert.assertEquals(false, xdoc.validate());
        Assert.assertEquals(XmlPurchaseOrderDocumentBean.type, xdoc.schemaType());

        // check type of element when xsi:type is bad
        XmlObject cust = xdoc.getPurchaseOrder().getCustomer();
        Assert.assertEquals(XmlCustomerBean.type, cust.schemaType());

        Assert.assertEquals("David Bau", ((XmlCustomerBean)cust).getName());
    }
    
    public static void donttestPrettyPrint() throws Exception
    {
        XmlObject xobj = XmlObject.Factory.parse("<test xmlns:x='foo'>&lt;SHOULDNOTBEATAG&gt;<a>simple<b/></a>&lt;ALSOSHOULDNOTBEATAG&gt;</test>");
        // System.out.println(xobj);
        System.out.println(xobj.xmlText());
        /*
        XmlCursor xcur = xobj.newCursor();
        xcur.toFirstChild();
        xobj = xcur.getObject();
        String result = xobj.toString();
        System.out.println(result);
        
        xcur.toFirstChild();
        xcur.toFirstChild();
        xcur.toFirstContentToken();
        xcur.insertChars("<html><body>this is a test</body></html>");
        
        System.out.println(xobj);
        */
    }
    
    public static void dontTestQNameCopy() throws Exception
    {
        SchemaDocument xobj = SchemaDocument.Factory.parse(
                "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>" +
                "<xs:element name='foo' type='xs:string'/></xs:schema>");
        SchemaDocument xobj2 = SchemaDocument.Factory.parse(
                "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'/>");
        xobj2.getSchema().addNewElement().set(xobj.getSchema().getElementArray(0));
        System.out.println(xobj2);
    }
    
    // don't run on normal drt because it's too slow: about 20-30 secs
    public static void donttestCursorFinalize()
    {
        XmlObject obj = XmlObject.Factory.newInstance();
        int i = 0;
        try
        {
            for (i = 0; i < 2000 * 1000; i++)
            {
                XmlCursor cur = obj.newCursor();
                // cur.dispose(); skipping this depends on finalization or else OOM
            }
        }
        catch (OutOfMemoryError e)
        {
            System.err.println("Did " + i + " iterations before running out of memory");
            throw e;
        }
    }
    
    public static void testOutOfRange() throws Exception
    {
        PositionDocument doc = PositionDocument.Factory.parse("<position xmlns='java:int.test'><lat>43</lat><lon>037</lon></position>");
        Assert.assertEquals(43, doc.getPosition().getLat());
        Assert.assertEquals(37, doc.getPosition().getLon());
        Assert.assertTrue(doc.validate());
        
        doc = PositionDocument.Factory.parse("<position xmlns='java:int.test'><lat>443</lat><lon>737</lon></position>");
        Assert.assertEquals(443, doc.getPosition().getLat());
        Assert.assertEquals(737, doc.getPosition().getLon());
        Assert.assertTrue(!doc.validate());
        
        doc.getPosition().setLat((short)-300);
        doc.getPosition().setLon((short)32767);
        Assert.assertEquals(-300, doc.getPosition().getLat());
        Assert.assertEquals(32767, doc.getPosition().getLon());
        Assert.assertTrue(!doc.validate());
        
        doc.getPosition().setLat((short)43);
        doc.getPosition().setLon((short)127);
        Assert.assertEquals(43, doc.getPosition().getLat());
        Assert.assertEquals(127, doc.getPosition().getLon());
        Assert.assertTrue(doc.validate());
    }
    
    public static void testParse() throws Exception
    {
        XmlTime xt = XmlTime.Factory.parse("<xml-fragment>12:00:00</xml-fragment>");
        Assert.assertEquals("12:00:00", xt.getCalendarValue().toString());
    }

    
    
}
