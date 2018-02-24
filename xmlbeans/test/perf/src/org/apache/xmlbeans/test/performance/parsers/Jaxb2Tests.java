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
package org.apache.xmlbeans.test.performance.parsers;

import perf.po.Customer;
import perf.po.LineItem;
import perf.po.PurchaseOrder;
import perf.po.Shipper;



import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com)
 *         Date: Aug 24, 2005
 */
public class Jaxb2Tests
{
    public static class LoadOnly
        extends Utils.ParseFile
    {
        public void execute(String file)
            throws Exception
        {
	        JAXBContext jc = JAXBContext.newInstance("perf.po");

            // create an Unmarshaller
            Unmarshaller u = jc.createUnmarshaller();

            // unmarshal a po instance document into a tree of Java content
            // objects composed of classes from the primer.po package.

	        //JAXBElement<?> poElement = (JAXBElement<?>) u.unmarshal(new FileInputStream(path + file));
	        //PurchaseOrder po = (PurchaseOrder) poElement.getValue();

	        perf.po.PurchaseOrder po = (perf.po.PurchaseOrder) u.unmarshal(new FileInputStream(file));
        }
    }

    public static class LoadAndTraverse
        extends Utils.ParseFile
    {
        public void execute(String file)
            throws Exception
        {
	        JAXBContext jc = JAXBContext.newInstance("perf.po");

            // create an Unmarshaller
            Unmarshaller u = jc.createUnmarshaller();

            // unmarshal a po instance document into a tree of Java content
            // objects composed of classes from the primer.po package.

            //JAXBElement<?> poElement = (JAXBElement<?>) u.unmarshal(new FileInputStream(path + file));
            //PurchaseOrder po = (PurchaseOrder) poElement.getValue();

	        perf.po.PurchaseOrder po = (perf.po.PurchaseOrder) u.unmarshal(new FileInputStream(file));

            //System.out.println("Cust name:    " + po.getCustomer().getName());
            //System.out.println("     address: " + po.getCustomer().getAddress());
            //System.out.println("Date: " + po.getDate().toXMLFormat());
            //System.out.println("Shipper name:         " + po.getShipper().getName());
            //System.out.println("        perOunceRate: " + po.getShipper().getPerOunceRate());
            Iterator it = po.getLineItem().iterator();
            while (it.hasNext())
            {
                LineItem lineItem = (LineItem) it.next();

                double price = lineItem.getPrice();
                int quantity = lineItem.getQuantity();
                BigDecimal perUnitOunces = lineItem.getPerUnitOunces();
                String description = lineItem.getDescription();

                //System.out.println("    Line item: " + price + " " + quantity + " " + perUnitOunces + " " + description);
            }
        }
    }

    public static class CreateOnly
        extends Utils.ParseFile
    {
        public void execute(String numberOfLineItems)
            throws Exception
        {
	        perf.po.PurchaseOrder po = new perf.po.PurchaseOrder();
            Shipper shipper = new Shipper();
            po.setShipper(shipper);

            shipper.setPerOunceRate(new BigDecimal(0.744325345));
            shipper.setName("ZipShip: twenty four characters");

            List lineItems = po.getLineItem();
            int liNo = Integer.parseInt(numberOfLineItems);
            for(int i = 0; i<liNo; i++)
            {
                LineItem lineItem = new LineItem();
                lineItems.add(lineItem);
                lineItem.setQuantity(2);
                lineItem.setPrice(21.7945342);
                lineItem.setPerUnitOunces(new BigDecimal(5));
                lineItem.setDescription("Fischer Black and the Revolutionary Idea of Finance");
            }

            Customer cust = new Customer();
            po.setCustomer(cust);
            cust.setAddress("12314 Murkyloshevichy, Anytown, PA");
            cust.setName("Gladys Kravitz Steve Kilisky, Senior Product Manager, AdobeEffects");

            po.setDate(javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar("2003-01-07T14:16:00-05:00"));

            //System.out.println("CreateOnly: " + poDoc);

        }
    }

    public static class CreateAndSave1
        extends Utils.ParseFile
    {
        public void execute(String numberOfLineItems)
            throws Exception
        {
            PurchaseOrder po = new PurchaseOrder();

            Shipper shipper = new Shipper();
            po.setShipper(shipper);

            shipper.setPerOunceRate(new BigDecimal(0.744325345));
            shipper.setName("ZipShip: twenty four characters");

            List lineItems = po.getLineItem();
            int liNo = Integer.parseInt(numberOfLineItems);
            for(int i = 0; i<liNo; i++)
            {
                LineItem lineItem = new LineItem();
                lineItems.add(lineItem);
                lineItem.setQuantity(2);
                lineItem.setPrice(21.7945342);
                lineItem.setPerUnitOunces(new BigDecimal(5));
                lineItem.setDescription("Fischer Black and the Revolutionary Idea of Finance");
            }

            Customer cust = new Customer();
            po.setCustomer(cust);
            cust.setAddress("12314 Murkyloshevichy, Anytown, PA");
            cust.setName("Gladys Kravitz Steve Kilisky, Senior Product Manager, AdobeEffects");

            po.setDate(javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar("2003-01-07T14:16:00-05:00"));

            // Illustrate two methods to create JAXBContext for j2s binding.
            // (1) by root classes newInstance(Class ...)
            JAXBContext context1 = JAXBContext.newInstance(new Class[] {PurchaseOrder.class});
            Marshaller m = context1.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(po, new Utils.NullOutputStream());

            // (2) by package, requires jaxb.index file in package cardfile.
            //     newInstance(String packageNames)
            //JAXBContext context2 = JAXBContext.newInstance("cardfile");
            //Marshaller m2 = context1.createMarshaller();
            //m2.marshal(getCard(), new FileOutputStream(f));
        }
    }

    public static class CreateAndSave2
        extends Utils.ParseFile
    {
        public void execute(String numberOfLineItems)
            throws Exception
        {
            PurchaseOrder po = new PurchaseOrder();

            Shipper shipper = new Shipper();
            po.setShipper(shipper);

            shipper.setPerOunceRate(new BigDecimal(0.744325345));
            shipper.setName("ZipShip: twenty four characters");

            List lineItems = po.getLineItem();
            int liNo = Integer.parseInt(numberOfLineItems);
            for(int i = 0; i<liNo; i++)
            {
                LineItem lineItem = new LineItem();
                lineItems.add(lineItem);
                lineItem.setQuantity(2);
                lineItem.setPrice(21.7945342);
                lineItem.setPerUnitOunces(new BigDecimal(5));
                lineItem.setDescription("Fischer Black and the Revolutionary Idea of Finance");
            }

            Customer cust = new Customer();
            po.setCustomer(cust);
            cust.setAddress("12314 Murkyloshevichy, Anytown, PA");
            cust.setName("Gladys Kravitz Steve Kilisky, Senior Product Manager, AdobeEffects");

            po.setDate(javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar("2003-01-07T14:16:00-05:00"));

            // Illustrate two methods to create JAXBContext for j2s binding.
            // (1) by root classes newInstance(Class ...)
            //JAXBContext context1 = JAXBContext.newInstance(PurchaseOrder.class);
            //Marshaller m = context1.createMarshaller();
            //m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            //m.marshal(po, new FileOutputStream(File.createTempFile("Perf", "JAXB2")));

            // (2) by package, requires jaxb.index file in package cardfile.
            //     newInstance(String packageNames)
	        JAXBContext context2 = JAXBContext.newInstance("perf.po");
            Marshaller m2 = context2.createMarshaller();
            m2.marshal(po, new Utils.NullOutputStream());
        }
    }
}
