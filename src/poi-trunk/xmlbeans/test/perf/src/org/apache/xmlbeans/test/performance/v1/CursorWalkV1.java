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
//import org.apache.xmlbeans.XmlCursor.TokenType;

public class CursorWalkV1
{

  public static void main(String[] args) throws Exception
  {

    final int iterations = Constants.CURSOR_ITERATIONS;
    String flavor;

    if(args.length == 0)
      flavor = "deep-attributes";
    else
      flavor = args[0];

    CursorWalkV1 test = new CursorWalkV1();
    PerfUtil util = new PerfUtil();
    long cputime;
    int hash = 0;

    // get the xml cursor
    char[] chars = util.createXmlData(flavor, Constants.XML_SIZE);
    //System.out.println("chars="+chars.length);
    XmlObject doc = XmlObject.Factory.parse(new CharArrayReader(chars));
    XmlCursor cursor = doc.newCursor();

    // warm up the vm
    cputime = System.currentTimeMillis();
    for(int i=0; i<iterations; i++){     
      hash += test.run(cursor);
    }
    cputime = System.currentTimeMillis() - cputime;

    // run it again for the real measurement
    cputime = System.currentTimeMillis();
    for(int i=0; i<iterations; i++){     
      hash += test.run(cursor);
    }
    cputime = System.currentTimeMillis() - cputime;
      
    // print the results
    // Class.getSimpleName() is only provided in jdk1.5, so have to trim package name off test name for logging to support 1.4
    System.out.print(Constants.DELIM+test.getClass().getName().substring(test.getClass().getName().lastIndexOf('.')+1)+" flavor="+flavor+" ");
    System.out.print("hash "+hash+" ");
    System.out.print("time "+cputime+"\n");
  }

  private int run(XmlCursor cursor) throws Exception
  {
    int iHash = 0;
    
    while(cursor.hasNextToken()){
    
      // walk the doc with the cursor, computing the hash
      if(cursor.isStart())
      {
        iHash += 17; // add a prime number
      }
      else if(cursor.isContainer())
      {
        iHash += cursor.getTextValue().length();
      }
      else if(cursor.isAttr())
      {
        iHash += cursor.getTextValue().length();
      }
      else if(cursor.isText())
      {
        iHash += cursor.getChars().length();
      }

      cursor.toNextToken();

    }

    // reset the cursor to the beginning 
    cursor.toStartDoc();

    // return the hash value
    return iHash;
  }
}
