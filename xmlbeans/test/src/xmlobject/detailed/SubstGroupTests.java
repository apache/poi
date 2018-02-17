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

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;
import tools.xml.XmlComparator;
import xmlobject.substgroup.*;

import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

public class SubstGroupTests extends TestCase
{

    private String URI = "http://xmlobject/substgroup";

    public SubstGroupTests(String name)
    {
        super(name);
    }

    /**
     * Convenience Method to return:
     * <xsd:complexType name="ItemType">
     * <xsd:sequence>
     * <xsd:element name="sku" type="xsd:integer"/>
     * <xsd:element name="name" type="xsd:string"/>
     * </xsd:sequence>
     * </xsd:complexType>
     *
     * @return
     */
    public ItemType getItemType()
    {
        xmlobject.substgroup.ItemType item = ItemType.Factory.newInstance();
        item.setName("ItemType");
        item.setSku(new BigInteger("12"));
        return item;
    }

    /**
     * Convenience Method to return:
     * <xsd:element name="item" type="tns:ItemType"/>
     *
     * @return
     */
    public ItemDocument getItemDoc()
    {
        xmlobject.substgroup.ItemDocument item = ItemDocument.Factory.newInstance();
        ItemType it = item.addNewItem();
        it.setName("ItemDocument");
        it.setSku(new BigInteger("12"));
        item.setItem(it);
        return item;
    }


    /**
     * Convenience Method to return:
     * <xsd:element name="chair" substitutionGroup="tns:item"/>
     *
     * @return
     */
    public ChairDocument getChairDoc()
    {
        xmlobject.substgroup.ChairDocument chair = ChairDocument.Factory.newInstance();
        ItemType item = chair.addNewChair();
        item.setName("Chair");
        item.setSku(new BigInteger("12"));
        chair.setChair(item);
        return chair;
    }

    /**
     * Convenience Method to return:
     * <xsd:element name="notachair" type="tns:ItemType"/>
     *
     * @return
     */
    public NotachairDocument getNotaChairDoc()
    {
        xmlobject.substgroup.NotachairDocument nochair = NotachairDocument.Factory.newInstance();
        ItemType item = nochair.addNewNotachair();
        item.setName("NotAChair");
        item.setSku(new BigInteger("12"));
        nochair.setNotachair(item);
        return nochair;
    }

    /**
     * Convenience Method to return:
     * <xsd:complexType name="BeanBagType">
     * <xsd:complexContent>
     * <xsd:extension base="tns:ItemType">
     * <xsd:sequence>
     * <xsd:element name="size" type="tns:BeanBagSizeType"/>
     * </xsd:sequence>
     * </xsd:extension>
     * </xsd:complexContent>
     * </xsd:complexType>
     *
     * @return
     */
    public BeanBagType getBeanBagType()
    {
        xmlobject.substgroup.BeanBagType bag = BeanBagType.Factory.newInstance();
        BeanBagSizeType size = bag.addNewSize();
        size.setColor("Blue");
        bag.setSize(size);
        bag.setName("BeanBagType");
        bag.setSku(new BigInteger("17"));
        return bag;
    }

    /**
     * Convenience Method to return:
     * <xsd:element name="beanBag" type="tns:BeanBagType"
     * substitutionGroup="tns:item"/>
     *
     * @return
     */
    public BeanBagDocument getBeanBagDoc()
    {
        xmlobject.substgroup.BeanBagDocument bean = BeanBagDocument.Factory.newInstance();
        BeanBagType item = bean.addNewBeanBag();
        item.setName("BeanBagDoc");
        item.setSku(new BigInteger("13"));
        BeanBagSizeType size = item.addNewSize();
        size.setColor("Blue");
        item.setSize(size);
        bean.setBeanBag(item);
        return bean;
    }

    /**
     * Convenience Method to return:
     * <xsd:element name="beanBag" type="tns:BeanBagType"
     * substitutionGroup="tns:item"/>
     *
     * @return
     */
    public BeanBagDocument getBeanBagDocItem()
    {
        xmlobject.substgroup.BeanBagDocument bean = BeanBagDocument.Factory.newInstance();
        ItemType item = bean.addNewItem();
        item.setName("BeanBagDocItem");
        item.setSku(new BigInteger("14"));
        bean.setItem(item);
        return bean;
    }

    /**
     * Convenience Method to return:
     * <xsd:element name="beanBag" type="tns:BeanBagType"
     * substitutionGroup="tns:item"/>
     *
     * @return
     */
    public BeanBagDocument getBeanBagDocBagType()
    {
        xmlobject.substgroup.BeanBagDocument bean = BeanBagDocument.Factory.newInstance();
        ItemType item = bean.addNewBeanBag();
        item.setName("BeanBagDocBeanBag");
        item.setSku(new BigInteger("14"));
        bean.setItem(item);
        return bean;
    }

    /**
     * Convenience Method to return:
     * <xsd:element name="footstool" type="xsd:string"/>     *
     *
     * @return
     */
    public FootstoolDocument getFootStoolDoc()
    {
        xmlobject.substgroup.FootstoolDocument foot = FootstoolDocument.Factory.newInstance();
        foot.setFootstool("FootStool");
        return foot;
    }

    /**
     * TODO: Determine what the proper Return value is
     *
     * @throws Exception
     */
    public void test_invalidSubstitute() throws Exception
    {
        OrderItem order = OrderItem.Factory.newInstance();
        ItemType item = order.addNewItem();
        item.setName("ItemType");
        item.setSku(new BigInteger("42"));

        //FootstoolDocument fsd;
        boolean cClassException = false;
        try {

            //on invalid substitute orignal value is returned.
            FootstoolDocument fsd = (FootstoolDocument) item.substitute(
                    FootstoolDocument.type.getDocumentElementName(),
                    FootstoolDocument.type);
            fail("Class Cast Exception was thrown on invalid substitute ");


        } catch (ClassCastException ccEx) {
            cClassException = true;
        }

        if (!cClassException)
            throw new Exception("An Invalid Substitution did not throw " +
                    "a Class Cast Exception");

        try {
            XmlObject xm = item.substitute(
                    FootstoolDocument.type.getDocumentElementName(),
                    FootstoolDocument.type);

            System.out.println("XM: " + xm.xmlText());
            ArrayList err = new ArrayList();
            XmlOptions xOpts = new XmlOptions().setErrorListener(err);
            //no way this should happen
            if (xm.validate(xOpts)) {
                System.err.println("Invalid substitute validated");

                for (Iterator iterator = err.iterator(); iterator.hasNext();) {
                    System.err.println("Error: " + iterator.next());
                }
            }

            //invalid substitute should leave good state
            System.out.println("Item: " + item.xmlText());

            String exp = "<xml-fragment><sku>42</sku><name>ItemType</name></xml-fragment>";

            Assert.assertTrue("text values should be the same",
                    exp.compareTo(xm.xmlText()) == 0);

        } catch (Exception e) {
            throw e;
        }
    }

    public void test_validSubstitute() throws Exception
    {
        QName name = new QName(URI, "beanBag");
        // get an item
        xmlobject.substgroup.OrderItem order = OrderItem.Factory.newInstance();
        ItemType item = order.addNewItem();
        item.setName("ItemForTest");
        item.setSku(new BigInteger("12"));

        // types and content before substitution
        System.out.println("Before Substitution :\nQNAme Item doc    :" + ItemDocument.type.getName());
        System.out.println("QNAme beanBag elem:" + name);
        System.out.println("item type:" + item.getClass().getName());
        System.out.println("item XMLText      : " + item.xmlText());

        try{
            XmlObject xObj = item.substitute(name, BeanBagType.type);
            System.out.println("After Substitution :\nSubstituted XObj text: "+xObj.xmlText());
            System.out.println("Substituted XObj type: " + xObj.getClass().getName());
            Assert.assertNotSame("Invalid Substitution. Xobj Types after substitution are the same.",xObj.getClass().getName(),item.getClass().getName() );

        }catch(NullPointerException npe){
            System.out.println("NPE Thrown: "+npe.getMessage());
            npe.printStackTrace();
        }

        boolean xvdThrown = false;
        try{
            // invoke some operation on the original XmlObject, it should thrown an XmlValueDisconnectedException
            item.xmlText();
        }catch(XmlValueDisconnectedException xvdEx){
            xvdThrown = true;
        }

        if( !xvdThrown ){
            Assert.fail("Referencing Item  after " +
                    "substitute did not throw the expected XmlValueDisconnectedException");
        }

    }

    /* public void test_item_downcasts_valid() throws Exception
     {
         BigInteger bInt = new BigInteger("12");

         xmlobject.substgroup.OrderItem order = OrderItem.Factory.newInstance();
         ItemType item = order.addNewItem();

         BeanBagType b2Type = (BeanBagType) item.substitute(
                 BeanBagDocument.type.getDocumentElementName(),
                 BeanBagType.type);

         BeanBagSizeType bbSize = b2Type.addNewSize();
         bbSize.setColor("Blue");
         bbSize.setStringValue("Blue");
         b2Type.setSku(bInt);
         b2Type.setSize(bbSize);
         b2Type.setName("BeanBagType");

         ItemType nItem = order.getItem();
         item =
                 (ItemType) nItem.substitute(
                         ItemDocument.type.getDocumentElementName(),
                         ItemType.type);
         System.out.println(
                 "Item: " + item.xmlText(new XmlOptions().setSavePrettyPrint()));
         ArrayList err = new ArrayList();
         XmlOptions opts = new XmlOptions(
                 new XmlOptions().setErrorListener(err));

         if (!item.validate(opts))
             throw new Exception("Downcasting Failed Validation:\n" + err);

         item.setName("ItemType");

         if (!order.validate(opts))
             throw new Exception("Downcasting Failed Validation:\n" + err);

     }*/

    /**
     * Tests substition upcase, from item to Document, then ensure validation
     *
     * @throws Exception
     */
    public void test_valid_sub() throws Exception
    {
        String expectedXML = "<sub:beanBag xmlns:sub=\"http://xmlobject/substgroup\">" +
                "  <sku>12</sku>" +
                "  <name>BeanBagType</name>" +
                "  <size color=\"Blue\">Blue</size>" +
                "</sub:beanBag>";
        XmlObject xm = XmlObject.Factory.parse(expectedXML);
        String itemName = "item";
        BigInteger bInt = new BigInteger("12");

        xmlobject.substgroup.OrderItem order = OrderItem.Factory.newInstance();
        ItemType item = order.addNewItem();
        item.setName(itemName);
        item.setSku(bInt);

        System.out.println("Order: " +
                order.xmlText(new XmlOptions().setSavePrettyPrint()));
        System.out.println("valid: " + order.validate());

        BeanBagType b2Type = (BeanBagType) item.substitute(
                BeanBagDocument.type.getDocumentElementName(),
                BeanBagType.type);

        Assert.assertTrue("Name Value was not as expected\nactual: " +
                b2Type.getName() +
                " exp: " +
                itemName,
                b2Type.getName().compareTo(itemName) == 0);
        Assert.assertTrue("Integer Value was not as Excepted",
                b2Type.getSku().compareTo(bInt) == 0);

        BeanBagSizeType bbSize = b2Type.addNewSize();
        bbSize.setColor("Blue");
        bbSize.setStringValue("Blue");
        b2Type.setSize(bbSize);
        b2Type.setName("BeanBagType");

        System.out.println("b2Type: " +
                b2Type.xmlText(new XmlOptions().setSavePrettyPrint()));
        System.out.println("b2Type: " + b2Type.validate());

        System.out.println("Order: " +
                order.xmlText(new XmlOptions().setSavePrettyPrint()));
        System.out.println("ovalid: " + order.validate());

        tools.xml.XmlComparator.Diagnostic diag = new tools.xml.XmlComparator.Diagnostic();

        if (!XmlComparator.lenientlyCompareTwoXmlStrings(order.xmlText(),
                xm.xmlText(), diag))
            throw new Exception("Compare Values Fails\n" + diag.toString());
    }


    public void test_item_disconnect() throws Exception
    {
        String itemName = "item";
        BigInteger bInt = new BigInteger("12");
        boolean exThrown = false;

        xmlobject.substgroup.OrderItem order = OrderItem.Factory.newInstance();
        ItemType item = order.addNewItem();
        item.setName(itemName);
        item.setSku(bInt);

        System.out.println("Order: " +
                order.xmlText(new XmlOptions().setSavePrettyPrint()));
        System.out.println("valid: " + order.validate());

        BeanBagType b2Type = (BeanBagType) item.substitute(
                BeanBagDocument.type.getDocumentElementName(),
                BeanBagType.type);

        try {
            System.out.println("This should Fail: " + item.xmlText());
            System.out.println("This should Fail: " + item.validate());
        } catch (XmlValueDisconnectedException xmvEx) {
            exThrown = true;
            System.err.println(
                    "Failed as Expected - message: " + xmvEx.getMessage());
        } catch (Exception e) {
            throw e;
        }

        if (!exThrown)
            throw new Exception(
                    "Value Disconnect Exception was not thrown as Expected");
    }


    public void test_item_downcasts_valid() throws Exception
    {
        BigInteger bInt = new BigInteger("12");
        ArrayList err = new ArrayList();
        XmlOptions opts = new XmlOptions(
                new XmlOptions().setErrorListener(err));

        xmlobject.substgroup.OrderItem order = OrderItem.Factory.newInstance();
        ItemType item = order.addNewItem();

        BeanBagType b2Type = (BeanBagType) item.substitute(
                BeanBagDocument.type.getDocumentElementName(),
                BeanBagType.type);

        BeanBagSizeType bbSize = b2Type.addNewSize();
        bbSize.setColor("Blue");
        bbSize.setStringValue("Blue");
        b2Type.setSku(bInt);
        b2Type.setSize(bbSize);
        b2Type.setName("BeanBagType");

        ItemType nItem = order.getItem();

        //nItem.validate(opts);
        if (!nItem.validate(opts))
            System.out.println(
                    "nItem - Downcasting Failed Validation:\n" + err);
        err.clear();

        item = (ItemType) nItem.substitute(
                ItemDocument.type.getDocumentElementName(),
                ItemType.type);

        //System.out.println("Item1: " + item.xmlText());

        if (!item.validate(opts))
            System.out.println("Item - Downcasting Failed Validation:\n" + err);

        XmlError[] xErr = getXmlErrors(err);
        Assert.assertTrue("Length of xm_errors was greater than expected",
                xErr.length == 1);
        Assert.assertTrue("Error Code was not as Expected",
                xErr[0].getErrorCode().compareTo("cvc-complex-type.2.4b") == 0);
        err.clear();

        String nName = "ItemType";
        item.setName(nName);
        System.out.println("Item2: " + item.xmlText());

        if (!order.validate(opts))
            System.out.println(
                    "Order - Downcasting Failed Validation:\n" + err);

        //Check value was set
        if (!(nName.compareTo(order.getItem().getName()) == 0))
            throw new Exception("Name Value was not changed");

        //Check Error message
        String expText = "Element not allowed: size in element item@http://xmlobject/substgroup";
        XmlError[] xErr2 = getXmlErrors(err);
        Assert.assertTrue("Length of xm_errors was greater than expected",
                xErr2.length == 1);
        Assert.assertTrue("Error Code was not as Expected",
                xErr2[0].getErrorCode().compareTo("cvc-complex-type.2.4b") ==
                0);
        Assert.assertTrue("Error Message was not as expected",
                xErr2[0].getMessage().compareTo(expText) == 0);

        err.clear();
    }

    private XmlError[] getXmlErrors(ArrayList c)
    {
        XmlError[] errs = new XmlError[c.size()];
        for (int i = 0; i < errs.length; i++) {
            errs[i] = (XmlError) c.get(i);
        }
        return errs;
    }

    public void test_null_newName() throws Exception
    {
        boolean exThrown = false;
        try {
            xmlobject.substgroup.OrderItem order = OrderItem.Factory.newInstance();
            order.substitute(null, OrderItem.type);
        } catch (IllegalArgumentException iaEx) {
            exThrown = true;
        }
        if (exThrown != true)
            throw new Exception("Exception was not thrown");
    }

    public void test_null_newType() throws Exception
    {
        boolean exThrown = false;
        try {
            OrderItem order = OrderItem.Factory.newInstance();
            order.substitute(OrderItem.type.getDocumentElementName(), null);

        } catch (IllegalArgumentException iaEx) {
            exThrown = true;
        } catch (Exception e) {
            throw e;
        }

        if (exThrown != true)
            throw new Exception("Exception was not thrown");
    }

    public void test_unknownQName() throws Exception
    {
        boolean exThrown = false;
        QName exp = new QName("http://www.w3.org/2001/XMLSchema", "anyType");
        try {
            OrderItem order = OrderItem.Factory.newInstance();
            XmlObject xm = order.substitute(new QName("http://baz", "baz"),
                    OrderItem.type);

            //Verify that the invalid substitution results in an anyType
            Assert.assertTrue("Namespace URIs were not the same",
                    exp.getNamespaceURI().compareTo(
                            xm.type.getName().getNamespaceURI()) == 0);
            Assert.assertTrue("Local Part was not as Expected",
                    xm.type.getName().getLocalPart().compareTo(
                            exp.getLocalPart()) == 0);

        } catch (IllegalArgumentException iaEx) {
            exThrown = true;
        } catch (Exception e) {
            throw e;
        }

        if (exThrown == true)
            throw new Exception("Exception was not thrown");
    }

    public void test_null_Params() throws Exception
    {
        boolean exThrown = false;
        try {
            XmlObject xml = XmlObject.Factory.newInstance();
            xml.substitute(null, null);
        } catch (IllegalArgumentException iaEx) {
            exThrown = true;
        } catch (Exception e) {
            throw e;
        }

        if (exThrown != true)
            throw new Exception("Exception was not thrown");
    }


}
