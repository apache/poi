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
import com.easypo.XmlPurchaseOrderDocumentBean.PurchaseOrder;
import com.easypo.XmlLineItemBean;
import com.easypo.XmlShipperBean;
import com.easypo.XmlPurchaseOrderDocumentBean;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.xml.sax.InputSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.File;
import java.io.Reader;
import java.util.List;
import java.util.ArrayList;

import tools.util.JarUtil;

public class SerializationTests extends TestCase
{
    public SerializationTests (String name) { super(name); }
    public static Test suite() { return new TestSuite(SerializationTests.class); }

    public void testXmlObjectSerialization() throws Exception
    {
        String simpleDocument = "<simpleDoc><nestedTag attr=\"sample\">43</nestedTag></simpleDoc>";
        XmlObject doc = XmlObject.Factory.parse(simpleDocument);

        // baseline test
        Assert.assertEquals(simpleDocument, doc.xmlText());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(doc);
        oos.close();

        byte[] byteArray = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(byteArray);
        ObjectInputStream ois = new ObjectInputStream(in);
        XmlObject newdoc = (XmlObject)ois.readObject();
        ois.close();

        Assert.assertEquals(simpleDocument, newdoc.xmlText());
    }

    public void testXBeanSerialization() throws Exception
    {
        XmlPurchaseOrderDocumentBean doc = XmlPurchaseOrderDocumentBean.Factory.newInstance();
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

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(doc.getPurchaseOrder().getLineItemArray(1));
        oos.writeObject(doc);
        oos.writeObject(doc.getPurchaseOrder().getLineItemArray(2));
        oos.close();

        byte[] byteArray = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(byteArray);
        ObjectInputStream ois = new ObjectInputStream(in);
        XmlLineItemBean newli1 = (XmlLineItemBean)ois.readObject();
        XmlPurchaseOrderDocumentBean newdoc = (XmlPurchaseOrderDocumentBean)ois.readObject();
        XmlLineItemBean newli2 = (XmlLineItemBean)ois.readObject();
        ois.close();

        PurchaseOrder neworder = newdoc.getPurchaseOrder();

        Assert.assertEquals(newli1, neworder.getLineItemArray(1));
        Assert.assertEquals(newli2, neworder.getLineItemArray(2));

        Assert.assertEquals("David Bau", neworder.getCustomer().getName());
        Assert.assertEquals("Gladwyne, PA", neworder.getCustomer().getAddress());
        Assert.assertEquals(3, neworder.sizeOfLineItemArray());

        Assert.assertEquals("Burnham's Celestial Handbook, Vol 1", neworder.getLineItemArray(0).getDescription());
        Assert.assertEquals(new BigDecimal("21.79"), neworder.getLineItemArray(0).getPrice());
        Assert.assertEquals(new BigInteger("2"), neworder.getLineItemArray(0).getQuantity());
        Assert.assertEquals(new BigDecimal("5"), neworder.getLineItemArray(0).getPerUnitOunces());

        Assert.assertEquals("Burnham's Celestial Handbook, Vol 2", neworder.getLineItemArray(1).getDescription());
        Assert.assertEquals(new BigDecimal("19.89"), neworder.getLineItemArray(1).getPrice());
        Assert.assertEquals(new BigInteger("2"), neworder.getLineItemArray(1).getQuantity());
        Assert.assertEquals(new BigDecimal("5"), neworder.getLineItemArray(1).getPerUnitOunces());

        Assert.assertEquals("Burnham's Celestial Handbook, Vol 3", neworder.getLineItemArray(2).getDescription());
        Assert.assertEquals(new BigDecimal("19.89"), neworder.getLineItemArray(2).getPrice());
        Assert.assertEquals(new BigInteger("1"), neworder.getLineItemArray(2).getQuantity());
        Assert.assertEquals(new BigDecimal("5"), neworder.getLineItemArray(2).getPerUnitOunces());

        Assert.assertEquals(true, neworder.isSetShipper());
        Assert.assertEquals("UPS", neworder.getShipper().getName());
        Assert.assertEquals(new BigDecimal("0.74"), neworder.getShipper().getPerOunceRate());
    }

    public void testWsdlSerialization()
    {
        // test for TextSaver
        try
        {
            File wsdlFile = JarUtil.getResourceFromJarasFile("xbean/xmlobject/wsdl.xml");

            List loaders = new ArrayList();
            loaders.add(SchemaDocument.type.getTypeSystem());
            SchemaTypeLoader[] loadersArr = (SchemaTypeLoader[])loaders.toArray(new SchemaTypeLoader[1]);
            SchemaTypeLoader loader = XmlBeans.typeLoaderUnion(loadersArr);

            XmlOptions options = new XmlOptions();
            options.setLoadLineNumbers();
            XmlObject wsdlObj = (XmlObject) loader.parse(wsdlFile, XmlObject.type, options);

            Reader reader = wsdlObj.newReader();
            InputSource source = new InputSource(reader);
            source.setSystemId("");


            XmlObject wsdlDefinitions = XmlObject.Factory.parse(reader);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
