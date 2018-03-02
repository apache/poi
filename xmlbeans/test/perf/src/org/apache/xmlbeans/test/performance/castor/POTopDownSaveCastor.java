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
package org.apache.xmlbeans.test.performance.castor;

import java.io.StringWriter;
import java.util.Date;

import org.apache.xmlbeans.test.performance.utils.Constants;

// from castor-generated schema jar(s)
import org.openuri.easypo.PurchaseOrder;
import org.openuri.easypo.Customer;
import org.openuri.easypo.LineItem;
import org.openuri.easypo.Shipper;

public class POTopDownSaveCastor
{
  public static void main(String[] args) throws Exception
  {
    
    final int iterations = Constants.ITERATIONS;

    POTopDownSaveCastor test = new POTopDownSaveCastor();
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
    // create the purchase order
    PurchaseOrder po = new PurchaseOrder();

    // create and initialize the customer
    Customer customer = new Customer();
    customer.setName(Constants.PO_CUSTOMER_NAME);
    customer.setAddress(Constants.PO_CUSTOMER_ADDR);
    po.setCustomer(customer);

    // set the date
    po.setDate(new Date());
  
    // create and initialize the line item array
    LineItem[] lineitems = new LineItem[Constants.PO_NUM_LINEITEMS];
    for(int i=0; i<Constants.PO_NUM_LINEITEMS; i++)
    {
      lineitems[i] = new LineItem();
      lineitems[i].setDescription(Constants.PO_LI_DESC);
      lineitems[i].setPerUnitOunces(Constants.PO_LI_PUO);
      lineitems[i].setPrice(Constants.PO_LI_PRICE);
      lineitems[i].setQuantity(Constants.PO_LI_QUANTITY);
    }
    po.setLineItem(lineitems);
  
    // create and initialize the shipper
    Shipper shipper = new Shipper();
    shipper.setName(Constants.PO_SHIPPER_NAME);
    shipper.setPerOunceRate(Constants.PO_SHIPPER_POR);
    po.setShipper(shipper);

    // grab the instance that was constructed
    StringWriter writer = new StringWriter();
    po.marshal(writer);
    writer.toString();
    writer.close();

    // calculate a hash to return
    int hash = ( po.getLineItem().length ) * 17;
    return hash;
  }

}
