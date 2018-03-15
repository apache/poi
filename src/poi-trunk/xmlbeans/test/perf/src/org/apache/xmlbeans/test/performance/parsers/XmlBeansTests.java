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

import java.io.File;
import java.math.BigDecimal;

import org.openuri.easypo.PurchaseOrderDocument;
import org.openuri.easypo.LineItem;
import org.openuri.easypo.Customer;
import org.openuri.easypo.Shipper;
import org.apache.xmlbeans.XmlCalendar;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com)
 *         Date: Aug 24, 2005
 */
public class XmlBeansTests
{
    public static class LoadOnly
        extends Utils.ParseFile
    {
        public void execute(String file)
            throws Exception
        {
            PurchaseOrderDocument.Factory.parse(new File(file));
        }
    }

    public static class LoadAndTraverseArray
        extends Utils.ParseFile
    {
        public void execute(String file)
            throws Exception
        {
            PurchaseOrderDocument poDoc = PurchaseOrderDocument.Factory.parse(new File(file));
            PurchaseOrderDocument.PurchaseOrder po = poDoc.getPurchaseOrder();

            //System.out.println("Cust name:    " + po.getCustomer().getName());
            //System.out.println("     address: " + po.getCustomer().getAddress());
            //System.out.println("Date: " + po.getDate());
            //System.out.println("Shipper name:         " + po.getShipper().getName());
            //System.out.println("        perOunceRate: " + po.getShipper().getPerOunceRate());

            LineItem[] lineItems = po.getLineItemArray();
            for (int i = 0; i < lineItems.length; i++)
            {
                LineItem lineItem = lineItems[i];
                double price = lineItem.getPrice();
                int quantity = lineItem.getQuantity();
                BigDecimal perUnitOunces = lineItem.getPerUnitOunces();
                String description = lineItem.getDescription();

//                System.out.println("    Line item: " + price + " " + quantity + " " + perUnitOunces + " " + description);
            }
        }
    }

    public static class LoadAndTraverseItem
        extends Utils.ParseFile
    {
        public void execute(String file)
            throws Exception
        {
            PurchaseOrderDocument poDoc = PurchaseOrderDocument.Factory.parse(new File(file));
            PurchaseOrderDocument.PurchaseOrder po = poDoc.getPurchaseOrder();

//            System.out.println("Cust name:    " + po.getCustomer().getName());
//            System.out.println("     address: " + po.getCustomer().getAddress());
//            System.out.println("Date: " + po.getDate());
//            System.out.println("Shipper name:         " + po.getShipper().getName());
//            System.out.println("        perOunceRate: " + po.getShipper().getPerOunceRate());

            int liSize = po.sizeOfLineItemArray();
            for (int i = 0; i<liSize; i++)
            {
                LineItem lineItem = po.getLineItemArray(i);
                double price = lineItem.getPrice();
                int quantity = lineItem.getQuantity();
                BigDecimal perUnitOunces = lineItem.getPerUnitOunces();
                String description = lineItem.getDescription();

//                System.out.println("    Line item: " + price + " " + quantity + " " + perUnitOunces + " " + description);
            }
        }
    }

    public static class CreateOnly
        extends Utils.ParseFile
    {
        public void execute(String numberOfLineItems)
            throws Exception
        {
            PurchaseOrderDocument poDoc = PurchaseOrderDocument.Factory.newInstance();
            PurchaseOrderDocument.PurchaseOrder po = poDoc.addNewPurchaseOrder();

            Shipper shipper = po.addNewShipper();
            shipper.setPerOunceRate(new BigDecimal(0.744325345));
            shipper.setName("ZipShip: twenty four characters");

            int liNo = Integer.parseInt(numberOfLineItems);
            for(int i = 0; i<liNo; i++)
            {
                LineItem lineItem = po.addNewLineItem();
                lineItem.setQuantity(2);
                lineItem.setPrice(21.7945342);
                lineItem.setPerUnitOunces(new BigDecimal(5));
                lineItem.setDescription("Fischer Black and the Revolutionary Idea of Finance");
            }

            Customer cust = po.addNewCustomer();
            cust.setAddress("12314 Murkyloshevichy, Anytown, PA");
            cust.setName("Gladys Kravitz Steve Kilisky, Senior Product Manager, AdobeEffects");

            po.setDate(new XmlCalendar("2003-01-07T14:16:00-05:00"));

//            System.out.println("CreateOnly: " + poDoc);
        }
    }

    public static class CreateAndSave
        extends Utils.ParseFile
    {
        public void execute(String numberOfLineItems)
            throws Exception
        {
            PurchaseOrderDocument poDoc = PurchaseOrderDocument.Factory.newInstance();
            PurchaseOrderDocument.PurchaseOrder po = poDoc.addNewPurchaseOrder();

            Shipper shipper = po.addNewShipper();
            shipper.setPerOunceRate(new BigDecimal(0.744325345));
            shipper.setName("ZipShip: twenty four characters");

            int liNo = Integer.parseInt(numberOfLineItems);
            for(int i = 0; i<liNo; i++)
            {
                LineItem lineItem = po.addNewLineItem();
                lineItem.setQuantity(2);
                lineItem.setPrice(21.7945342);
                lineItem.setPerUnitOunces(new BigDecimal(5));
                lineItem.setDescription("Fischer Black and the Revolutionary Idea of Finance");
            }

            Customer cust = po.addNewCustomer();
            cust.setAddress("12314 Murkyloshevichy, Anytown, PA");
            cust.setName("Gladys Kravitz Steve Kilisky, Senior Product Manager, AdobeEffects");

            po.setDate(new XmlCalendar("2003-01-07T14:16:00-05:00"));

//            System.out.println("CreateOnly: " + poDoc);

            poDoc.save(new Utils.NullOutputStream());
        }
    }
}
