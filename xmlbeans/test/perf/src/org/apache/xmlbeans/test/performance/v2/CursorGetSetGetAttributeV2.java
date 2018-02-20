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
package org.apache.xmlbeans.test.performance.v2;

import java.io.CharArrayReader;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.test.performance.utils.Constants;
import org.apache.xmlbeans.test.performance.utils.PerfUtil;

public class CursorGetSetGetAttributeV2
{

  public static void main(String[] args) throws Exception
  {

    final int iterations = Constants.GET_SET_ITERATIONS;
    String flavor;
    int stringSize = 0;

    // the only flavors that should be used are
    // deep-attributes and wide-attributes
    // because this scenario focuses on attribute values
    if(args.length == 0)
      flavor = "deep-attributes";
    else
      flavor = args[0];

    // the size of the string used is the second arg or default if not specified
    if(args.length < 2)
    {
      stringSize = Constants.STRING_SIZE;
    }
    else
    {
      stringSize = Integer.parseInt(args[1]);
    }

    // create the string to be used for the set
    PerfUtil util = new PerfUtil();
    String stringToSet = util.createString(stringSize);
    CursorGetSetGetAttributeV2 test = new CursorGetSetGetAttributeV2();
    long cputime;
    int hash = 0;

    // get the xml cursor
    char[] chars = util.createXmlData(flavor, Constants.XML_SIZE);
    XmlObject doc = XmlObject.Factory.parse(new CharArrayReader(chars));
    XmlCursor cursor = doc.newCursor();

    // warm up the vm
    cputime = System.currentTimeMillis();
    for(int i=0; i<iterations; i++){     
      hash += test.run(cursor, stringToSet);
      cursor.toStartDoc();
    }
    cputime = System.currentTimeMillis() - cputime;

    // run it again for the real measurement
    cputime = System.currentTimeMillis();
    for(int i=0; i<iterations; i++){     
      hash += test.run(cursor, stringToSet);
      cursor.toStartDoc();
    }
    cputime = System.currentTimeMillis() - cputime;
      
    // print the results
    // Class.getSimpleName() is only provided in jdk1.5, so have to trim package name off test name for logging to support 1.4
    System.out.print(Constants.DELIM+test.getClass().getName().substring(test.getClass().getName().lastIndexOf('.')+1)+" flavor="+flavor+" sizetoset="+stringToSet.length()+" ");
    System.out.print("hash "+hash+" ");
    System.out.print("time "+cputime+" ");
    System.out.print("time/it " + ((double)cputime)/((double)iterations) + "\n");
  }

  private int run(XmlCursor p_cursor, String p_setval) throws Exception
  {
    p_cursor.toFirstChild();
    p_cursor.toFirstChild();
    p_cursor.toFirstAttribute();
    p_cursor.getTextValue();
    p_cursor.setTextValue(p_setval);
    return p_cursor.getTextValue().length() * 17;
  }
}
