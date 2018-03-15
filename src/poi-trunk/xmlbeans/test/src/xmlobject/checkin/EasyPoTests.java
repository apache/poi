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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import com.easypo.XmlPurchaseOrderDocumentBean;
import com.easypo.XmlPurchaseOrderDocumentBean.PurchaseOrder;



import tools.util.*;

public class EasyPoTests extends TestCase
{
    public EasyPoTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(EasyPoTests.class); }

    public void testEasyPo() throws Exception
    {
        XmlPurchaseOrderDocumentBean doc = (XmlPurchaseOrderDocumentBean)
            XmlObject.Factory.parse(JarUtil.getResourceFromJarasFile(
                                     "xbean/xmlobject/easypo1.xml"));
        Assert.assertEquals(false, doc.isNil());
        PurchaseOrder order = doc.getPurchaseOrder();
        Assert.assertEquals("David Bau", order.getCustomer().getName());
        Assert.assertEquals("Gladwyne, PA", order.getCustomer().getAddress());
        Assert.assertEquals(3, order.sizeOfLineItemArray());

        Assert.assertEquals("Burnham's Celestial Handbook, Vol 1", order.getLineItemArray(0).getDescription());
        Assert.assertEquals(new BigDecimal("21.79"), order.getLineItemArray(0).getPrice());
        Assert.assertEquals(new BigInteger("2"), order.getLineItemArray(0).getQuantity());
        Assert.assertEquals(new BigDecimal("5"), order.getLineItemArray(0).getPerUnitOunces());

        Assert.assertEquals("Burnham's Celestial Handbook, Vol 2", order.getLineItemArray(1).getDescription());
        Assert.assertEquals(new BigDecimal("19.89"), order.getLineItemArray(1).getPrice());
        Assert.assertEquals(new BigInteger("2"), order.getLineItemArray(1).getQuantity());
        Assert.assertEquals(new BigDecimal("5"), order.getLineItemArray(1).getPerUnitOunces());

        Assert.assertEquals("Burnham's Celestial Handbook, Vol 3", order.getLineItemArray(2).getDescription());
        Assert.assertEquals(new BigDecimal("19.89"), order.getLineItemArray(2).getPrice());
        Assert.assertEquals(new BigInteger("1"), order.getLineItemArray(2).getQuantity());
        Assert.assertEquals(new BigDecimal("5"), order.getLineItemArray(2).getPerUnitOunces());

        Assert.assertEquals(true, order.isSetShipper());
        Assert.assertEquals("UPS", order.getShipper().getName());
        Assert.assertEquals(new BigDecimal("0.74"), order.getShipper().getPerOunceRate());

        Assert.assertEquals(3, order.sizeOfLineItemArray());
    }
    
    
    public void testSimpleAutoValidaiton() throws Exception
    {
        XmlPurchaseOrderDocumentBean.Factory.parse(
            "<purchase-order xmlns='http://openuri.org/easypo'/>" );

        try
        {
            XmlPurchaseOrderDocumentBean.Factory.parse(
                "<purchase-orde xmlns='http://openuri.org/easypo'/>" );

            Assert.assertTrue( false );
        }
        catch ( XmlException e )
        {
        }

        try
        {
            XmlPurchaseOrderDocumentBean.Factory.parse(
                "<purchase-order xmlns='http://openuri.org/easyp'/>" );

            Assert.assertTrue( false );
        }
        catch ( XmlException e )
        {
        }
        
        try
        {
            XmlPurchaseOrderDocumentBean.Factory.parse(
                "<f:fragment xmlns:f='http://www.openuri.org/fragment'/>" );

            Assert.assertTrue( false );
        }
        catch ( XmlException e )
        {
        }
        
        try
        {
            XmlPurchaseOrderDocumentBean.Factory.parse(
                "<f:fragment xmlns:f='http://www.openuri.org/fragment'><a/></f:fragment>" );

            Assert.assertTrue( false );
        }
        catch ( XmlException e )
        {
        }
        
        try
        {
            XmlPurchaseOrderDocumentBean.Factory.parse(
                "<f:fragment xmlns:f='http://www.openuri.org/fragment'><a/><a/></f:fragment>" );

            Assert.assertTrue( false );
        }
        catch ( XmlException e )
        {
        }
    }
}
