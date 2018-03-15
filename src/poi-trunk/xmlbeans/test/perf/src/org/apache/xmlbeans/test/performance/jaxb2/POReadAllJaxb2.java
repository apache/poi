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
package org.apache.xmlbeans.test.performance.jaxb2;

//import java.io.File;
import java.io.CharArrayReader;

import org.apache.xmlbeans.test.performance.utils.PerfUtil;
import org.apache.xmlbeans.test.performance.utils.Constants;

// required by jaxb
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.util.Iterator;

// from jaxb-generated schema jar(s)
import org.openuri.easypo.jaxb2.PurchaseOrder;
import org.openuri.easypo.jaxb2.Customer;
import org.openuri.easypo.jaxb2.LineItem;
import org.openuri.easypo.jaxb2.Shipper;


public class POReadAllJaxb2
{
  public static void main(String[] args) throws Exception
  {
    final int iterations = Constants.ITERATIONS;
    String filename;

    if(args.length == 0){
      filename = Constants.PO_INSTANCE_1;
    }
    else if(args[0].length() > 1){
      filename = Constants.XSD_DIR+Constants.P+args[0];
    }
    else{
      switch( Integer.parseInt(args[0]) )
      {
      case 1: filename = Constants.PO_INSTANCE_1; break;
      case 2: filename = Constants.PO_INSTANCE_2; break;  
      case 3: filename = Constants.PO_INSTANCE_3; break;
      case 4: filename = Constants.PO_INSTANCE_4; break;
      case 5: filename = Constants.PO_INSTANCE_5; break;
      case 6: filename = Constants.PO_INSTANCE_6; break;
      case 7: filename = Constants.PO_INSTANCE_7; break;
      default: filename = Constants.PO_INSTANCE_1; break;
      }
    }    

    POReadAllJaxb2 test = new POReadAllJaxb2();
    PerfUtil util = new PerfUtil();
    long cputime;
    int hash = 0;

    // get the xmlinstance
    char[] chars = util.fileToChars(filename);
        
    // warm up the vm
    cputime = System.currentTimeMillis();
    for(int i=0; i<iterations; i++){
      CharArrayReader reader = new CharArrayReader(chars);     
      hash += test.run(reader);
    }
    cputime = System.currentTimeMillis() - cputime;

    // run it again for the real measurement
    cputime = System.currentTimeMillis();
    for(int i=0; i<iterations; i++){
      CharArrayReader reader = new CharArrayReader(chars);     
      hash += test.run(reader);
    }
    cputime = System.currentTimeMillis() - cputime;
      
    // print the results
    // Class.getSimpleName() is only provided in jdk1.5, so have to trim package name off test name for logging to support 1.4
    System.out.print(Constants.DELIM+test.getClass().getName().substring(test.getClass().getName().lastIndexOf('.')+1)+" filesize="+chars.length+" ");
    System.out.print("hash "+hash+" ");
    System.out.print("time "+cputime+"\n");
  }

  private int run(CharArrayReader reader) throws Exception
  {
    int iSumStrings = 0;
    // create the xml source from the reader
    StreamSource source = new StreamSource(reader);
    // unmarshall the xml instance
    JAXBContext context = JAXBContext.newInstance("org.openuri.easypo.jaxb2");
    Unmarshaller unmarshaller = context.createUnmarshaller();
    //depricated - unmarshaller.setValidating(false);
    PurchaseOrder po = 
      (PurchaseOrder) unmarshaller.unmarshal(source);

    // retreive the customer element
    Customer customer = po.getCustomer();
    iSumStrings += customer.getAddress().length();
    iSumStrings += customer.getName().length();
    // retreive the date
    po.getDate();
    // retrive the shipper element
    po.getShipper().getName();
    po.getShipper().getPerOunceRate();

    // retreive all line items
    Iterator it = po.getLineItem().iterator();
    // sum the line item prices and get the other childs
    float sum = 0;
    while(it.hasNext()){
      LineItem li = (LineItem) it.next();
      iSumStrings += li.getDescription().length();
      li.getPerUnitOunces();
      li.getQuantity();
      sum += li.getPrice();
    }
    // retreive the shipper element
    Shipper shipper = po.getShipper();
    iSumStrings += shipper.getName().length();
    shipper.getPerOunceRate();

    return iSumStrings;
  }
}
