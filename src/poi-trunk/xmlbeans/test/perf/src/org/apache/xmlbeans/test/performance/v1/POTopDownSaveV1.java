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
package org.apache.xmlbeans.test.performance.v1;

import java.io.Reader;
import java.util.Calendar;

import org.apache.xmlbeans.test.performance.utils.Constants;

// from v2-generated schema jar(s)
import org.openuri.easypo.PurchaseOrderDocument;
import org.openuri.easypo.PurchaseOrderDocument.PurchaseOrder;
import org.openuri.easypo.Customer;
import org.openuri.easypo.LineItem;
import org.openuri.easypo.Shipper;


public class POTopDownSaveV1
{
  public static void main(String[] args) throws Exception
  {
    POTopDownSaveV1 test = new POTopDownSaveV1();
    int iterations = Constants.ITERATIONS;
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
    PurchaseOrderDocument podoc = PurchaseOrderDocument.Factory.newInstance();
    PurchaseOrder po = podoc.addNewPurchaseOrder();

    // create and initialize customer
    Customer customer = po.addNewCustomer();
    customer.setName(Constants.PO_CUSTOMER_NAME);
    customer.setAddress(Constants.PO_CUSTOMER_ADDR);

    // set the date
    po.setDate(Calendar.getInstance());

    // create and initialize the line item array
    for (int i=0; i<Constants.PO_NUM_LINEITEMS; i++)
    {
      LineItem li = po.addNewLineItem();
      li.setDescription(Constants.PO_LI_DESC);
      li.setPerUnitOunces(Constants.PO_LI_PUO);
      li.setPrice(Constants.PO_LI_PRICE);
      li.setQuantity(Constants.PO_LI_QUANTITY);
    }

    // create and initialize the shipper
    Shipper shipper = po.addNewShipper();
    shipper.setName(Constants.PO_SHIPPER_NAME);
    shipper.setPerOunceRate(Constants.PO_SHIPPER_POR);

    // grab the instance that was constructed
    Reader reader = po.newReader();
    int c;
    while( (c=reader.read()) != -1){}
    if(null != reader) reader.close();

    // calculate a hash to return
    int hash = ( po.getLineItemArray().length ) * 17;
    return hash;
  }
}
