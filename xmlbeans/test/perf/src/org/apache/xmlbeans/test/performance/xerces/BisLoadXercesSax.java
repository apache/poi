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

//import java.io.File;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.xmlbeans.test.performance.utils.Constants;
import org.apache.xmlbeans.test.performance.utils.PerfUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class BisLoadXercesSax
{
  public static void main(String[] args) throws Exception
  {
    final int iterations = Constants.CURSOR_ITERATIONS;
    String flavor;

    if(args.length == 0)
      flavor = "deep-attributes";
    else
      flavor = args[0];


    BisLoadXercesSax test = new BisLoadXercesSax();
    PerfUtil util = new PerfUtil();
    long cputime;
    int hash = 0;

    // get the buffered input stram
    byte[] bytes = util.createXmlDataBytes(flavor, Constants.XML_SIZE);
   
    // warm up the vm
    cputime = System.currentTimeMillis();
    for(int i=0; i<iterations; i++){
      hash += test.run(new BufferedInputStream(new ByteArrayInputStream(bytes) ));
    }
    cputime = System.currentTimeMillis() - cputime;

    // run it again for the real measurement
    cputime = System.currentTimeMillis();
    for(int i=0; i<iterations; i++){
      hash += test.run(new BufferedInputStream(new ByteArrayInputStream(bytes) ));
    }
    cputime = System.currentTimeMillis() - cputime;
    
      
    // print the results
    // Class.getSimpleName() is only provided in jdk1.5, so have to trim package name off test name for logging to support 1.4
    System.out.print(Constants.DELIM+test.getClass().getName().substring(test.getClass().getName().lastIndexOf('.')+1)+" flavor="+flavor+" ");
    System.out.print("hash "+hash+" ");
    System.out.print("time "+cputime+"\n");
  }

  private int run(BufferedInputStream bis) throws Exception 
  {
    // create the input source from the bis
    InputSource is = new InputSource(bis);

    // load the input source
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser parser = factory.newSAXParser();
    MyHandler handler = new MyHandler();
    parser.parse(is, handler);

    // calculate and return the hash
    return handler.getTagName().length() * 17;
  }

  // SAX event handler class
  public static class MyHandler extends DefaultHandler
  {
    private String _tagName;
    private boolean bDone = false;

    public String getTagName(){return _tagName;}

    public void startElement(String uri,
                             String localName,
                             String qName,
                             Attributes attributes) throws SAXException
    {
      if (!bDone)
      {
        _tagName = qName;
        bDone = true;
      }

    }
  }

}
