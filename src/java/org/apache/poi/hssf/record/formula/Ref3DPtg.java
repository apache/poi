

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.hssf.record.formula;

import org.apache.poi.util.LittleEndian;

import org.apache.poi.hssf.util.RangeAddress;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.util.SheetReferences;
import org.apache.poi.util.BitField;
import org.apache.poi.hssf.model.Workbook;

/**
 * Title:        Reference 3D Ptg <P>
 * Description:  Defined a cell in extern sheet. <P>
 * REFERENCE:  <P>
 * @author Libin Roman (Vista Portal LDT. Developer)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 1.0-pre
 */

public class Ref3DPtg extends Ptg {
    public final static byte sid  = 0x3a;
    private final static int  SIZE = 7; // 6 + 1 for Ptg
    private short             field_1_index_extern_sheet;
    private short             field_2_row;
    private short             field_3_column;
    private BitField         rowRelative = new BitField(0x8000);
    private BitField         colRelative = new BitField(0x4000);

    /** Creates new AreaPtg */
    public Ref3DPtg() {}

    public Ref3DPtg(byte[] data, int offset) {
        offset++;
        field_1_index_extern_sheet = LittleEndian.getShort(data, 0 + offset);
        field_2_row          = LittleEndian.getShort(data, 2 + offset);
        field_3_column        = LittleEndian.getShort(data, 4 + offset);
    }
    
    public Ref3DPtg(String cellref, short externIdx ) {
        CellReference c= new CellReference(cellref);
        setRow((short) c.getRow());
        setColumn((short) c.getCol());
        setColRelative(!c.isColAbsolute());
        setRowRelative(!c.isRowAbsolute());   
        setExternSheetIndex(externIdx);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("Ref3dPrg\n");
        buffer.append("Index to Extern Sheet = " + getExternSheetIndex()).append("\n");
        buffer.append("Row = " + getRow()).append("\n");
        buffer.append("Col  = " + getColumn()).append("\n");
        buffer.append("ColRowRel= "
        + isRowRelative()).append("\n");
        buffer.append("ColRel   = " + isColRelative()).append("\n");
        return buffer.toString();
    }

    public void writeBytes(byte [] array, int offset) {
        array[ 0 + offset ] = (byte) (sid + ptgClass);
        LittleEndian.putShort(array, 1 + offset , getExternSheetIndex());
        LittleEndian.putShort(array, 3 + offset , getRow());
        LittleEndian.putShort(array, 5 + offset , getColumnRaw());
    }

    public int getSize() {
        return SIZE;
    }

    public short getExternSheetIndex(){
        return field_1_index_extern_sheet;
    }

    public void setExternSheetIndex(short index){
        field_1_index_extern_sheet = index;
    }

    public short getRow() {
        return field_2_row;
    }

    public void setRow(short row) {
        field_2_row = row;
    }

    public short getColumn() {
        return ( short ) (field_3_column & 0xFF);
    }

    public short getColumnRaw() {
        return field_3_column;
    }

     public boolean isRowRelative()
    {
        return rowRelative.isSet(field_3_column);
    }
    
    public void setRowRelative(boolean rel) {
        field_3_column=rowRelative.setShortBoolean(field_3_column,rel);
    }
    
    public boolean isColRelative()
    {
        return colRelative.isSet(field_3_column);
    }
    
    public void setColRelative(boolean rel) {
        field_3_column=colRelative.setShortBoolean(field_3_column,rel);
    }
    public void setColumn(short column) {
        field_3_column &= 0xFF00;
        field_3_column |= column & 0xFF;
    }

    public void setColumnRaw(short column) {
        field_3_column = column;
    }

   /* public String getArea(){
        RangeAddress ra = new RangeAddress("");

        String result = (ra.numTo26Sys(getColumn()) + (getRow() + 1));

        return result;
    }*/

    public void setArea(String ref){
        RangeAddress ra = new RangeAddress(ref);

        String from = ra.getFromCell();

        setColumn((short) (ra.getXPosition(from) -1));
        setRow((short) (ra.getYPosition(from) -1));

    }

    public String toFormulaString(SheetReferences refs) {
        StringBuffer retval = new StringBuffer();
        if (refs != null) {
            retval.append(refs.getSheetName((int)this.field_1_index_extern_sheet));
            retval.append('!');
        }
        retval.append((new CellReference(getRow(),getColumn(),!isRowRelative(),!isColRelative())).toString()); 
        return retval.toString();
    }

   public byte getDefaultOperandClass() {return Ptg.CLASS_REF;}

   public Object clone() {
     Ref3DPtg ptg = new Ref3DPtg();
     ptg.field_1_index_extern_sheet = field_1_index_extern_sheet;
     ptg.field_2_row = field_2_row;
     ptg.field_3_column = field_3_column;
     return ptg;
   }

}
