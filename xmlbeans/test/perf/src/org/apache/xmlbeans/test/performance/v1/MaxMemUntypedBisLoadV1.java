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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.test.performance.utils.Constants;
import org.apache.xmlbeans.test.performance.utils.PerfUtil;


public class MaxMemUntypedBisLoadV1
{
 
  public static void main(String[] args) throws Exception
  {
  	int interval = Constants.MEM_INTERVAL;
  	long memory = Runtime.getRuntime().maxMemory();
  	int size = 0;
  	int hash = 0;
    String flavor = null;

    if(args.length == 0)
      flavor = "deep-attributes";
    else
      flavor = args[0];

    MaxMemUntypedBisLoadV1 test = new MaxMemUntypedBisLoadV1();
    PerfUtil util = new PerfUtil();
    
    try
    {
    	System.gc();
    	// infinite for loop - go until get oom
    	for(int i=1; true; i++)
    	{
    		System.gc();
    		byte[] bytes = util.createXmlDataBytes(flavor,Constants.MEM_INITIALSIZE+(interval*i));
    		hash += test.run(new BufferedInputStream(new ByteArrayInputStream(bytes) ));
    		size = bytes.length;
    	}
    }
    catch (OutOfMemoryError oom)
    {
    	System.gc();
        // Class.getSimpleName() is only provided in jdk1.5, so have to trim package name off test name for logging to support 1.4
        System.out.print(Constants.DELIM+test.getClass().getName().substring(test.getClass().getName().lastIndexOf('.')+1)+" flavor="+flavor+" ");
        System.out.print("hash "+hash+" ");
        System.out.print("memory "+memory+" ");
        System.out.println("size="+size);
    }
  }

  private int run(BufferedInputStream p_bis) throws Exception
  {
    // load the buffered input stream
    XmlObject xobj = XmlObject.Factory.parse(p_bis);
    return xobj.hashCode();
  }
}
