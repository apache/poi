
/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        
package org.apache.poi.hssf.util;


import junit.framework.TestCase;


public class TestCellReference extends TestCase {
    public TestCellReference(String s) {
        super(s);
    }
    
    public void testAbsRef1(){
        CellReference cf = new CellReference("$B$5");
        assertTrue("row is 4",cf.getRow()==4);
        assertTrue("col is 1",cf.getCol()==1);
        assertTrue("row is abs",cf.isRowAbsolute());
        assertTrue("col is abs",cf.isColAbsolute());
        assertTrue("string is $B$5",cf.toString().equals("$B$5"));
    }
    
    public void  testAbsRef2(){
        CellReference cf = new CellReference(4,1,true,true);
        assertTrue("row is 4",cf.getRow()==4);
        assertTrue("col is 1",cf.getCol()==1);
        assertTrue("row is abs",cf.isRowAbsolute());
        assertTrue("col is abs",cf.isColAbsolute());
        assertTrue("string is $B$5",cf.toString().equals("$B$5"));
    }

    public void  testAbsRef3(){
        CellReference cf = new CellReference("B$5");
        assertTrue("row is 4",cf.getRow()==4);
        assertTrue("col is 1",cf.getCol()==1);
        assertTrue("row is abs",cf.isRowAbsolute());
        assertTrue("col is rel",!cf.isColAbsolute());
        assertTrue("string is B$5",cf.toString().equals("B$5"));
    }
    
    public void  testAbsRef4(){
        CellReference cf = new CellReference(4,1,true,false);
        assertTrue("row is 4",cf.getRow()==4);
        assertTrue("col is 1",cf.getCol()==1);
        assertTrue("row is abs",cf.isRowAbsolute());
        assertTrue("col is rel",!cf.isColAbsolute());
        assertTrue("string is B$5",cf.toString().equals("B$5"));
    }
    
    public void  testAbsRef5(){
        CellReference cf = new CellReference("$B5");
        assertTrue("row is 4",cf.getRow()==4);
        assertTrue("col is 1",cf.getCol()==1);
        assertTrue("row is abs",!cf.isRowAbsolute());
        assertTrue("col is rel",cf.isColAbsolute());
        assertTrue("string is B$5",cf.toString().equals("$B5"));
    }
    
    public void  testAbsRef6(){
        CellReference cf = new CellReference(4,1,false,true);
        assertTrue("row is 4",cf.getRow()==4);
        assertTrue("col is 1",cf.getCol()==1);
        assertTrue("row is abs",!cf.isRowAbsolute());
        assertTrue("col is rel",cf.isColAbsolute());
        assertTrue("string is B$5",cf.toString().equals("$B5"));
    }

    public void  testAbsRef7(){
        CellReference cf = new CellReference("B5");
        assertTrue("row is 4",cf.getRow()==4);
        assertTrue("col is 1",cf.getCol()==1);
        assertTrue("row is abs",!cf.isRowAbsolute());
        assertTrue("col is rel",!cf.isColAbsolute());
        assertTrue("string is B$5",cf.toString().equals("B5"));
    }
    
    public void  testAbsRef8(){
        CellReference cf = new CellReference(4,1,false,false);
        assertTrue("row is 4",cf.getRow()==4);
        assertTrue("col is 1",cf.getCol()==1);
        assertTrue("row is abs",!cf.isRowAbsolute());
        assertTrue("col is rel",!cf.isColAbsolute());
        assertTrue("string is B$5",cf.toString().equals("B5"));
    }

    
    public static void main(String [] args) {
        System.out.println("Testing org.apache.poi.hssf.util.TestCellReference");
        junit.textui.TestRunner.run(TestCellReference.class);
    }
    
}
