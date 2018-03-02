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

import org.apache.xmlbeans.test.performance.utils.Constants;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;


public class POTopDownXercesSax
{
  public static void main(String[] args) throws Exception
  {

    final int iterations = Constants.ITERATIONS;
   
    POTopDownXercesSax test = new POTopDownXercesSax();
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
  
    // instantiate the handler
    MyHandler handler = new MyHandler();
    handler.startDocument();
    handler.startPrefixMapping(Constants.prefix,
                               Constants.PO_NS);

    // start the po element
    AttributesImpl attributes = new AttributesImpl();
    attributes.addAttribute(null,
                            null,
                            Constants.qNamespace,
                            Constants.qString,
                            Constants.PO_NS);
    handler.startElement(null,null,Constants.qPO,attributes);
    
    // the customer element
    handler.startElement(Constants.qCustomer);
    handler.startElement(Constants.qName);
    handler.characters(Constants.PO_CUSTOMER_NAME);
    handler.endElement(Constants.qName);
    handler.startElement(Constants.qAddress);
    handler.characters(Constants.PO_CUSTOMER_ADDR);
    handler.endElement(Constants.qAddress);
    handler.endElement(Constants.qCustomer);

    // the date element
    handler.startElement(Constants.qDate);
    handler.characters(Constants.PO_DATE_STRING);
    handler.endElement(Constants.qDate);

    // the line items
    for(int i=0; i<Constants.PO_NUM_LINEITEMS; i++)
    {
      handler.startElement(Constants.qLineItem);
      handler.startElement(Constants.qDescription);
      handler.characters(Constants.PO_LI_DESC);
      handler.endElement(Constants.qDescription);
      handler.startElement(Constants.qPerUnitOunces);
      handler.characters(Constants.PO_LI_PUO_STRING);
      handler.endElement(Constants.qPerUnitOunces);
      handler.startElement(Constants.qPrice);
      handler.characters(Constants.PO_LI_PRICE_STRING);
      handler.endElement(Constants.qPrice);
      handler.startElement(Constants.qQuantity);
      handler.characters(Constants.PO_LI_QUANTITY_STRING);
      handler.endElement(Constants.qQuantity);
      handler.endElement(Constants.qLineItem);
    }

    // the shipper
    handler.startElement(Constants.qShipper);
    handler.startElement(Constants.qName);
    handler.characters(Constants.PO_SHIPPER_NAME);
    handler.endElement(Constants.qName);
    handler.startElement(Constants.qPerOunceRate);
    handler.characters(Constants.PO_SHIPPER_POR_STRING);
    handler.endElement(Constants.qPerOunceRate);
    handler.endElement(Constants.qShipper);


    // end the po element
    handler.endElement(Constants.qPO);

    // calculate a hash to return
    int hash = handler.getLineItemCount() * 17;
    return hash;
  }

  // SAX event handler class
  public static class MyHandler extends DefaultHandler
  {
    private int _hash = 0;
    private StringBuffer _buff;
    private String _uri;
    private String _prefix;
    private final String SES = "<";
    private final String EE = ">";
    private final String EES = "</";
    private final String EQ = "=";

    public int getLineItemCount()
    {
      int licount = 0;
      int i = 0;
      while( i != _buff.lastIndexOf(Constants.qLineItem))
      {
        licount++;
        i = _buff.indexOf(Constants.qLineItem,i+1);
      }

      return (licount/2);
    }

    // sax event handlers
    public void startDocument() throws SAXException
    {
      _buff = new StringBuffer(Constants.initialBuffSize);
    }

    /*
    public void endDocument() throws SAXException
    {
    }
    */

    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
      _prefix = prefix;
      _uri = uri;
    }

    public void endPrefixMapping(String prefix) throws SAXException
    {
      _prefix = null;
      _uri = null;
    }

    public void startElement(String qName) throws SAXException
    {
      this.startElement(null,null,qName,null);
    }

    public void startElement(String uri,
                             String localName,
                             String qName,
                             Attributes attributes) throws SAXException
    {

      if(null != attributes)
      {
        _buff.append(SES);
        _buff.append(qName);
        _buff.append(attributes.getQName(0));
        _buff.append(EQ);
        _buff.append(attributes.getValue(0));
        _buff.append(EE);
      }
      else
      {
        _buff.append(SES);
        _buff.append(qName);
        _buff.append(EE);
      }

    }

    public void endElement(String qName) throws SAXException
    {
      this.endElement(null,null,qName);
    }

    public void endElement(String uri,
                           String localName,
                           String qName) throws SAXException
    {
      _buff.append(EES);
      _buff.append(qName);
      _buff.append(EE);
    }

    public void characters(String _val) throws SAXException
    {
      char[] chars = new char[_val.length()];
      _val.getChars(0,_val.length(),chars,0);
      this.characters(chars,0,chars.length);
    }

    public void characters(char[] ch, int start, int length) throws SAXException
    {
      _buff.append(ch,start,length);
    }
  }


}
