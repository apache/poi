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


/*
 * LessThanPtg.java
 *
 * Created on January 23, 2003, 9:47 AM
 */
package org.apache.poi.hssf.record.formula;

//JDK
import java.util.List;

//POI
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.record.RecordInputStream;

/**
 * Less than operator PTG "<". The SID is taken from the 
 * Openoffice.orgs Documentation of the Excel File Format,
 * Table 3.5.7
 * @author Cameron Riley (criley at ekmail.com)
 */
public class LessThanPtg
    extends OperationPtg
{
    /** the size of the Ptg  */
    public final static int SIZE = 1;

    /** the sid for the less than operator as hex */
    public final static byte sid  = 0x09;    

    /** identifier for LESS THAN char */
    private final static String LESSTHAN = "<";

    /** 
     * Constructor. Creates new LessThanPtg 
     */
    public LessThanPtg()
    {
        //deliberately empty
    }

    /**
     * Constructor. Create a new LessThanPtg.
     * @param in the RecordInputstream to read the record from
     */
    public LessThanPtg(RecordInputStream in)
    {
        //deliberately empty
    }
    
    /**
     * Write the sid to an array
     * @param array the array of bytes to write the sid to
     * @param offset the offset to add the sid to
     */
    public void writeBytes(byte[] array, int offset)
    {
        array[ offset + 0 ] = sid;
    }

    /**
     * Get the size of the sid
     * @return int the size of the sid in terms of byte additions to an array
     */
    public int getSize()
    {
        return SIZE;
    }

    /**
     * Get the type of PTG for Less Than
     * @return int the identifier for the type
     */
    public int getType()
    {
        return TYPE_BINARY;
    }

    /**
     * Get the number of operands for the Less than operator
     * @return int the number of operands
     */
    public int getNumberOfOperands()
    {
        return 2;
    }
    
    /** 
     * Implementation of method from Ptg 
     * @param book the Sheet References
     */
    public String toFormulaString(HSSFWorkbook book)
    {
        return this.LESSTHAN;
    }
       
    /** 
     * Implementation of method from OperationsPtg
     * @param operands a String array of operands
     * @return String the Formula as a String
     */  
    public String toFormulaString(String[] operands) 
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(operands[ 0 ]);
        buffer.append(this.LESSTHAN);
        buffer.append(operands[ 1 ]);
        return buffer.toString();
    }
    
    /**
     * Get the default operands class value
     * @return byte the Ptg Class Value as a byte from the Ptg Parent object
     */
    public byte getDefaultOperandClass() 
    {
        return Ptg.CLASS_VALUE;
    }
    
    /**
     * Implementation of clone method from Object
     * @return Object a clone of this class as an Object
     */       
    public Object clone() 
    {
        return new LessThanPtg();
    }

}
