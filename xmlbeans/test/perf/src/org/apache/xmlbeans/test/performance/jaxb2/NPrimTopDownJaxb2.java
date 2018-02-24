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

import org.apache.xmlbeans.test.performance.utils.Constants;

// from jaxb-generated schema jar(s)
import org.openuri.nonprimitives.jaxb2.NonPrimitives;
import org.openuri.nonprimitives.jaxb2.Numerics;

import javax.xml.datatype.DatatypeFactory;
import java.util.GregorianCalendar;


public class NPrimTopDownJaxb2
{
  public static void main(String[] args) throws Exception
  {
    
    final int iterations = Constants.ITERATIONS;
 
    NPrimTopDownJaxb2 test = new NPrimTopDownJaxb2();
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
    // create the doc
    NonPrimitives nprim = new NonPrimitives();

    // create and initialize the numerics
    for(int i=0; i<Constants.PO_NUM_LINEITEMS; i++)
    {
      //NumericsImpl numerics = new NumericsImpl();
      Numerics numerics = new Numerics();
      numerics.setMydecimal(Constants.myBigDecimal);
      numerics.setMyinteger(Constants.myPosBigInteger);
      numerics.setMyneginteger(Constants.myNegBigInteger);
      numerics.setMynonneginteger(Constants.myPosBigInteger);
      numerics.setMynonposinteger(Constants.myNegBigInteger);
      numerics.setMyposinteger(Constants.myPosBigInteger);
      nprim.getNumerics().add(numerics);
    }
    
    // create and initialize the misc element
    // Date required in javax.xml.datatype.XMLGregorianCalendar format
    GregorianCalendar gdate = new GregorianCalendar();
    nprim.setMydate(DatatypeFactory.newInstance().newXMLGregorianCalendar(Long.toString(gdate.getTimeInMillis())));
    nprim.setMystring(Constants.myString);

    // calculate a hash to return
    int hash = ( nprim.getNumerics().size() ) * 17;
    return hash;
  }
}
