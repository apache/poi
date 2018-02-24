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
package org.apache.xmlbeans.test.performance.xerces;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xmlbeans.test.performance.utils.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class POTopDownXercesDom
{
  public static void main(String[] args) throws Exception
  {

    final int iterations = Constants.ITERATIONS;
   
    POTopDownXercesDom test = new POTopDownXercesDom();
    long cputime;
    int hash = 0;
        
    // warm up the vm
    cputime = System.currentTimeMillis();
    for(int i=0; i<iterations; i++){
      hash += test.run();
    }
    cputime = System.currentTimeMillis() - cputime;

    // run it again for the real measurement
    cputime = System.currentTimeMillis();
    for(int i=0; i<iterations; i++){
      hash += test.run();
    }
    cputime = System.currentTimeMillis() - cputime;
    
      
    // print the results
    // Class.getSimpleName() is only provided in jdk1.5, so have to trim package name off test name for logging to support 1.4
    System.out.print(Constants.DELIM+test.getClass().getName().substring(test.getClass().getName().lastIndexOf('.')+1)+" ");
    System.out.print("hash "+hash+" ");
    System.out.print("time "+cputime+"\n");
  }

  private int run() throws Exception 
  {
    
    // create the document
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();

    // create the purchase order element
    Element po = doc.createElementNS(Constants.PO_NS,"po:purchase-order");

    // create the customer element
    Element customer = doc.createElementNS(Constants.PO_NS,"po:customer");

    // create the customer name element and add to customer
    Element name = doc.createElementNS(Constants.PO_NS,"po:name");
    Node tCustName = doc.createTextNode(Constants.PO_CUSTOMER_NAME);
    name.appendChild(tCustName);
    customer.appendChild(name);

    // create the customer address element and add to customer
    Element address = doc.createElementNS(Constants.PO_NS,"po:address");
    Node tCustAddr = doc.createTextNode(Constants.PO_CUSTOMER_ADDR);
    address.appendChild(tCustAddr);
    customer.appendChild(address);

    // add the customer to the po
    po.appendChild(customer);

    // create the date element and add it to the po
    Element date = doc.createElementNS(Constants.PO_NS,"po:date");
    Node tDate = doc.createTextNode(Constants.PO_DATE_STRING);
    date.appendChild(tDate);
    po.appendChild(date);

    // create and add the line item elements
    for(int i=0; i<Constants.PO_NUM_LINEITEMS; i++)
    {
      Element li = doc.createElementNS(Constants.PO_NS,"po:line-item");

      // description
      Element desc = doc.createElementNS(Constants.PO_NS,"po:description");
      Node tDesc = doc.createTextNode(Constants.PO_LI_DESC);
      desc.appendChild(tDesc);
      li.appendChild(desc);

      // per-unit-ounces
      Element puo = doc.createElementNS(Constants.PO_NS,"po:per-unit-ounces");
      Node tPuo = doc.createTextNode(Constants.PO_LI_PUO_STRING);
      puo.appendChild(tPuo);
      li.appendChild(puo);

      // price
      Element price = doc.createElementNS(Constants.PO_NS,"po:price");
      Node tPrice = doc.createTextNode(Constants.PO_LI_PRICE_STRING);
      price.appendChild(tPrice);
      li.appendChild(price);

      // quantity
      Element quantity = doc.createElementNS(Constants.PO_NS,"po:quantity");
      Node tQuantity = doc.createTextNode(Constants.PO_LI_QUANTITY_STRING);
      quantity.appendChild(tQuantity);
      li.appendChild(quantity);

      // add the line-item to the po
      po.appendChild(li);
    }

    // create and add the shipper
    Element shipper = doc.createElementNS(Constants.PO_NS,"po:shipper");
    Element shipperName = doc.createElementNS(Constants.PO_NS,"po:name");
    Node tShipperName = doc.createTextNode(Constants.PO_SHIPPER_NAME);
    shipperName.appendChild(tShipperName);
    shipper.appendChild(shipperName);
    Element perouncerate = doc.createElementNS(Constants.PO_NS,"po:per-ounce-rate");
    Node tPerOunceRate = doc.createTextNode(Constants.PO_SHIPPER_POR_STRING);
    perouncerate.appendChild(tPerOunceRate);
    shipper.appendChild(perouncerate);

    // create the hash to return
    int hash = ( po.getElementsByTagNameNS(Constants.PO_NS, "line-item").getLength() ) * 17;
    return hash;
  }
}
