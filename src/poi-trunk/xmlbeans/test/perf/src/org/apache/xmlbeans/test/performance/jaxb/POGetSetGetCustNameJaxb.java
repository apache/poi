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
package org.apache.xmlbeans.test.performance.jaxb;

//import java.io.File;
//import java.io.IOException;
//import java.io.FileNotFoundException;
import java.io.CharArrayReader;
//import java.lang.UnsupportedOperationException;

import org.apache.xmlbeans.test.performance.utils.PerfUtil;
import org.apache.xmlbeans.test.performance.utils.Constants;

// required by jaxb
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
//import java.util.List;

// from jaxb-generated schema jar(s)
import org.openuri.easypo.Customer;
import org.openuri.easypo.PurchaseOrder;
//import org.openuri.easypo.Customer;
import org.openuri.easypo.LineItem;
//import org.openuri.easypo.Shipper;


public class POGetSetGetCustNameJaxb
{
  public static void main(String[] args) throws Exception
  {
    final int iterations = Constants.GET_SET_ITERATIONS;
    String filename;
    int stringSize = 0;
    String stringToSet = "";

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
  
    // the size of the string used is the second arg or default if not specified
    if(args.length < 2)
    {
      stringSize = Constants.STRING_SIZE;
    }
    else
    {
      stringSize = Integer.parseInt(args[1]);
    }
    
    POGetSetGetCustNameJaxb test = new POGetSetGetCustNameJaxb();
    PerfUtil util = new PerfUtil();
    long cputime;
    int hash = 0;

    // create the string to be used for the set
    stringToSet = util.createString(stringSize);
    
    // get the xmlinstance
    char[] chars = util.fileToChars(filename);
 
    // create the xml source from the reader
    StreamSource source = new StreamSource(new CharArrayReader(chars));
    // unmarshall the xml instance
    JAXBContext context = JAXBContext.newInstance("org.openuri.easypo");
    Unmarshaller unmarshaller = context.createUnmarshaller();
    unmarshaller.setValidating(false);
    PurchaseOrder po = 
      (PurchaseOrder) unmarshaller.unmarshal(source);
    Customer customer = po.getCustomer();
    
    // warm up the vm
    cputime = System.currentTimeMillis();
    for(int i=0; i<iterations; i++){
      CharArrayReader reader = new CharArrayReader(chars);     
      hash += test.run(customer,stringToSet);
    }
    cputime = System.currentTimeMillis() - cputime;

    // run it again for the real measurement
    cputime = System.currentTimeMillis();
    for(int i=0; i<iterations; i++){
      CharArrayReader reader = new CharArrayReader(chars);     
      hash += test.run(customer,stringToSet);
    }
    cputime = System.currentTimeMillis() - cputime;
      
    // print the results
    // Class.getSimpleName() is only provided in jdk1.5, so have to trim package name off test name for logging to support 1.4
    System.out.print(Constants.DELIM+test.getClass().getName().substring(test.getClass().getName().lastIndexOf('.')+1)+" filesize="+chars.length+" ");
    System.out.print("hash "+hash+" ");
    System.out.print("time "+cputime+"\n");
  }

  private int run(Customer p_customer, String p_setval) throws Exception
  {
    p_customer.getName();
    p_customer.setName(p_setval);
    return p_customer.getName().length() * 17;
  }
}
