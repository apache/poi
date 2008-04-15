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

package org.apache.poi.hssf.record.formula;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.StringUtil;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.record.RecordFormatException;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.UnicodeString;

/**
 * ArrayPtg - handles arrays
 * 
 * The ArrayPtg is a little wierd, the size of the Ptg when parsing initially only
 * includes the Ptg sid and the reserved bytes. The next Ptg in the expression then follows.
 * It is only after the "size" of all the Ptgs is met, that the ArrayPtg data is actually
 * held after this. So Ptg.createParsedExpression keeps track of the number of 
 * ArrayPtg elements and need to parse the data upto the FORMULA record size.
 *  
 * @author Jason Height (jheight at chariot dot net dot au)
 */

public class ArrayPtg extends Ptg
{
    public final static byte sid  = 0x20;
    protected byte field_1_reserved;
    protected byte field_2_reserved;
    protected byte field_3_reserved;
    protected byte field_4_reserved;
    protected byte field_5_reserved;
    protected byte field_6_reserved;
    protected byte field_7_reserved;
    
    
    protected short  token_1_columns;
    protected short token_2_rows;
    protected Object[][] token_3_arrayValues;

    protected ArrayPtg() {
      //Required for clone methods
    }

    public ArrayPtg(RecordInputStream in)
    {
    	field_1_reserved = in.readByte();
    	field_2_reserved = in.readByte();
    	field_3_reserved = in.readByte();
    	field_4_reserved = in.readByte();
    	field_5_reserved = in.readByte();
    	field_6_reserved = in.readByte();
    	field_7_reserved = in.readByte();
    }
    
    /** 
     * Read in the actual token (array) values. This occurs 
     * AFTER the last Ptg in the expression.
     * See page 304-305 of Excel97-2007BinaryFileFormat(xls)Specification.pdf
     */
    public void readTokenValues(RecordInputStream in) {    	
        token_1_columns = (short)(0x00ff & in.readByte());
        token_2_rows = in.readShort();
        
        //The token_1_columns and token_2_rows do not follow the documentation.
        //The number of physical rows and columns is actually +1 of these values.
        //Which is not explicitly documented.
        token_1_columns++;
        token_2_rows++;        
        
        token_3_arrayValues = new Object[token_1_columns][token_2_rows];
        
        for (int x=0;x<token_1_columns;x++) {
			for (int y=0;y<token_2_rows;y++) {
				byte grbit = in.readByte();
				if (grbit == 0x01) {
					token_3_arrayValues[x][y] = new Double(in.readDouble());
				} else if (grbit == 0x02) {
					//Ignore the doco, it is actually a unicode string with all the
					//trimmings ie 16 bit size, option byte etc
					token_3_arrayValues[x][y] = in.readUnicodeString();
				} else throw new RecordFormatException("Unknown grbit '"+grbit+"' at " + x + "," + y + " with " + in.remaining() + " bytes left");
			}
        }
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer("[ArrayPtg]\n");

        buffer.append("columns = ").append(getColumnCount()).append("\n");
        buffer.append("rows = ").append(getRowCount()).append("\n");
        for (int x=0;x<getColumnCount();x++) {
        	for (int y=0;y<getRowCount();y++) {
        		Object o = token_3_arrayValues[x][y];
       			buffer.append("[").append(x).append("][").append(y).append("] = ").append(o).append("\n"); 
        	}
        }
        return buffer.toString();
    }

    public void writeBytes(byte [] array, int offset)
    {
        array[offset++] = (byte) (sid + ptgClass);
        array[offset++] = field_1_reserved;
        array[offset++] = field_2_reserved;
        array[offset++] = field_3_reserved;
        array[offset++] = field_4_reserved;
        array[offset++] = field_5_reserved;
        array[offset++] = field_6_reserved;
        array[offset++] = field_7_reserved;
        
    }
    public int writeTokenValueBytes(byte [] array, int offset) {
    	int pos = 0;
    	array[pos + offset] = (byte)(token_1_columns-1);
        pos++;
        LittleEndian.putShort(array, pos+offset, (short)(token_2_rows-1));
        pos += 2;
        for (int x=0;x<getColumnCount();x++) {
        	for (int y=0;y<getRowCount();y++) {
        		Object o = token_3_arrayValues[x][y];
        		if (o instanceof Double) {
        			array[pos+offset] = 0x01;
        			pos++;
        			LittleEndian.putDouble(array, pos+offset, ((Double)o).doubleValue());
        			pos+=8;
        		} else if (o instanceof UnicodeString) {
        			array[pos+offset] = 0x02;
        			pos++;        			
        			UnicodeString s = (UnicodeString)o;
        			//JMH TBD Handle string continuation. Id do it now but its 4am.
        	        UnicodeString.UnicodeRecordStats stats = new UnicodeString.UnicodeRecordStats();
        	        s.serialize(stats, pos + offset, array);
        	        pos += stats.recordSize; 
        		} else throw new RuntimeException("Coding error");
        	}
        }
        return pos;
    }

    public void setRowCount(short row)
    {
        token_2_rows = row;
    }

    public short getRowCount()
    {
        return token_2_rows;
    }

    public void setColumnCount(short col)
    {
        token_1_columns = (byte)col;
    }

    public short getColumnCount()
    {
        return token_1_columns;
    }

    /** This size includes the size of the array Ptg plus the Array Ptg Token value size*/
    public int getSize()
    {
    	int size = 1+7+1+2;
        for (int x=0;x<getColumnCount();x++) {
        	for (int y=0;y<getRowCount();y++) {
        		Object o = token_3_arrayValues[x][y];
        		if (o instanceof UnicodeString) {
        			size++;
        	        UnicodeString.UnicodeRecordStats rs = new UnicodeString.UnicodeRecordStats();
                    ((UnicodeString)o).getRecordSize(rs);        			
        			size += rs.recordSize;
        		} else if (o instanceof Double) {
        			size += 9;
        		}
        	}
        }
        return size;
    }

    public String toFormulaString(HSSFWorkbook book)
    {
    	StringBuffer b = new StringBuffer();
    	b.append("{");
        for (int x=0;x<getColumnCount();x++) {
          	for (int y=0;y<getRowCount();y++) {
          		Object o = token_3_arrayValues[x][y];
        		if (o instanceof String) {
        			b.append((String)o);
        		} else if (o instanceof Double) {
        			b.append(((Double)o).doubleValue());
        		}
        		if (y != getRowCount())
        			b.append(",");
          	}
          	if (x != getColumnCount())
          		b.append(";");
          }
        b.append("}");
        return b.toString();
    }
    
    public byte getDefaultOperandClass() {
        return Ptg.CLASS_ARRAY;
    }
    
    public Object clone() {
      ArrayPtg ptg = new ArrayPtg();
      ptg.field_1_reserved = field_1_reserved;
      ptg.field_2_reserved = field_2_reserved;
      ptg.field_3_reserved = field_3_reserved;
      ptg.field_4_reserved = field_4_reserved;
      ptg.field_5_reserved = field_5_reserved;
      ptg.field_6_reserved = field_6_reserved;
      ptg.field_7_reserved = field_7_reserved;
      
      ptg.token_1_columns = token_1_columns;
      ptg.token_2_rows = token_2_rows;
      ptg.token_3_arrayValues = new Object[getColumnCount()][getRowCount()];
      for (int x=0;x<getColumnCount();x++) {
      	for (int y=0;y<getRowCount();y++) {
      		ptg.token_3_arrayValues[x][y] = token_3_arrayValues[x][y];
      	}
      }      
      ptg.setClass(ptgClass);
      return ptg;
    }
}
