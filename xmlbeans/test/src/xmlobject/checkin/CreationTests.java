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

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import junit.framework.Assert;
import com.easypo.XmlPurchaseOrderDocumentBean;
import com.easypo.XmlPurchaseOrderDocumentBean.PurchaseOrder;
import com.easypo.XmlLineItemBean;
import com.easypo.XmlShipperBean;

import java.math.BigDecimal;
import java.math.BigInteger;

public class CreationTests extends TestCase
{
    public CreationTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(CreationTests.class); }

    public void testCreatePo() throws Exception
    {
        XmlPurchaseOrderDocumentBean doc =
            XmlPurchaseOrderDocumentBean.Factory.newInstance();
        
        PurchaseOrder order = doc.addNewPurchaseOrder();
        order.addNewCustomer().setName("David Bau");
        order.getCustomer().setAddress("Gladwyne, PA");
        XmlLineItemBean li;
        li = order.addNewLineItem();
        li.setDescription("Burnham's Celestial Handbook, Vol 1");
        li.setPrice(new BigDecimal("21.79"));
        li.setQuantity(BigInteger.valueOf(2));
        li.setPerUnitOunces(new BigDecimal("5"));
        li = order.addNewLineItem();
        li.setDescription("Burnham's Celestial Handbook, Vol 2");
        li.setPrice(new BigDecimal("19.89"));
        li.setQuantity(BigInteger.valueOf(2));
        li.setPerUnitOunces(new BigDecimal("5"));
        li = order.addNewLineItem();
        li.setDescription("Burnham's Celestial Handbook, Vol 3");
        li.setPrice(new BigDecimal("19.89"));
        li.setQuantity(BigInteger.valueOf(1));
        li.setPerUnitOunces(new BigDecimal("5"));
        XmlShipperBean sh = order.addNewShipper();
        sh.setName("UPS");
        sh.setPerOunceRate(new BigDecimal("0.74"));

//        System.out.println(doc.xmlText());

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
    }
}
