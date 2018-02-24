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

import java.io.CharArrayReader;

import org.apache.xmlbeans.test.performance.utils.PerfUtil;
import org.apache.xmlbeans.test.performance.utils.Constants;

// required by v2
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;

public class CursorGetElementV1
{

  public static void main(String[] args) throws Exception
  {

    final int iterations = Constants.GET_SET_ITERATIONS;
    String flavor;

    // the only flavors that should be used are
    // wide-text and any others that have text values (none of as 8/18/04)
    // because this scenario focuses on element values
    if(args.length == 0)
      flavor = "wide-text";
    else
      flavor = args[0];

    CursorGetElementV1 test = new CursorGetElementV1();
    PerfUtil util = new PerfUtil();
    long cputime;
    int hash = 0;

    // get the xml cursor
    char[] chars = util.createXmlData(flavor, Constants.XML_SIZE);
    XmlObject doc = XmlObject.Factory.parse(new CharArrayReader(chars));
    XmlCursor cursor = doc.newCursor();

    // warm up the vm
    cputime = System.currentTimeMillis();
    for(int i=0; i<iterations; i++){     
      hash += test.run(cursor);
      cursor.toStartDoc();
    }
    cputime = System.currentTimeMillis() - cputime;

    // run it again for the real measurement
    cputime = System.currentTimeMillis();
    for(int i=0; i<iterations; i++){     
      hash += test.run(cursor);
      cursor.toStartDoc();
    }
    cputime = System.currentTimeMillis() - cputime;
      
    // print the results
    // Class.getSimpleName() is only provided in jdk1.5, so have to trim package name off test name for logging to support 1.4
    System.out.print(Constants.DELIM+test.getClass().getName().substring(test.getClass().getName().lastIndexOf('.')+1)+" flavor="+flavor+" ");
    System.out.print("hash "+hash+" ");
    System.out.print("time "+cputime+"\n");
  }

  private int run(XmlCursor p_cursor) throws Exception
  {
    p_cursor.toFirstChild();
    p_cursor.toFirstChild();
    return p_cursor.getTextValue().length() * 17;
  }
}
