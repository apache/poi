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
package org.apache.xmlbeans.test.performance.castor;

import org.apache.xmlbeans.test.performance.utils.Constants;

// castor-specific date type
import org.exolab.castor.types.Date;

// from castor-generated schema jar(s)
import org.openuri.nonprimitives.NonPrimitives;
import org.openuri.nonprimitives.Numerics;

public class NPrimTopDownCastor
{
  public static void main(String[] args) throws Exception
  {
    
    final int iterations = Constants.ITERATIONS;

    NPrimTopDownCastor test = new NPrimTopDownCastor();
    long cputime;
    int hash = 0;
    Date date = new Date();

    // warm up the vm
    cputime = System.currentTimeMillis();
    for(int i=0; i<iterations; i++){
      hash += test.run(date);
    }
    cputime = System.currentTimeMillis() - cputime;

    // run it again for the real measurement
    cputime = System.currentTimeMillis();
    for(int i=0; i<iterations; i++){
      hash += test.run(date);
    }
    cputime = System.currentTimeMillis() - cputime;
      
    // print the results
    // Class.getSimpleName() is only provided in jdk1.5, so have to trim package name off test name for logging to support 1.4
    System.out.print(Constants.DELIM+test.getClass().getName().substring(test.getClass().getName().lastIndexOf('.')+1)+" ");
    System.out.print("hash "+hash+" ");
    System.out.print("time "+cputime+"\n");
  }

  private int run(Date p_date) throws Exception
  {
    // create the doc
    NonPrimitives nprim = new NonPrimitives();

    // create and initialize numeric elements
    Numerics[] numerics = new Numerics[Constants.PO_NUM_LINEITEMS];
    for(int i=0; i<Constants.PO_NUM_LINEITEMS; i++)
    {
      numerics[i] = new Numerics();
      numerics[i].setMydecimal(Constants.myBigDecimal);
      numerics[i].setMyinteger(Constants.myInt);
      numerics[i].setMyneginteger(Constants.myNegInt);
      numerics[i].setMynonneginteger(Constants.myInt);
      numerics[i].setMynonposinteger(Constants.myNegInt);
      numerics[i].setMyposinteger(Constants.myInt);
    }
    nprim.setNumerics(numerics);

    // set the date and string fields
    nprim.setMydate(p_date);
    nprim.setMystring(Constants.myString);


    // calculate a hash to return
    int hash = ( nprim.getNumerics().length ) * 17;
    return hash;
  }

}
