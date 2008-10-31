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

package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;

/**
 * 
 * The series list record defines the series displayed as an overlay to the main chart record.<br/>
 * TODO - does this record (0x1016) really exist.  It doesn't seem to be referenced in either the OOO or MS doc
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class SeriesListRecord extends Record {
    public final static short sid = 0x1016;
    private  short[]    field_1_seriesNumbers;

    public SeriesListRecord(short[] seriesNumbers) {
    	field_1_seriesNumbers = seriesNumbers;
    }

    public SeriesListRecord(RecordInputStream in) {
    	int nItems = in.readUShort();
    	short[] ss = new short[nItems];
    	for (int i = 0; i < nItems; i++) {
			ss[i] = in.readShort();
			
		}
        field_1_seriesNumbers = ss;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[SERIESLIST]\n");
        buffer.append("    .seriesNumbers= ").append(" (").append( getSeriesNumbers() ).append(" )");
        buffer.append("\n"); 

        buffer.append("[/SERIESLIST]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data) {

        int nItems = field_1_seriesNumbers.length;
        int dataSize = 2 + 2 * nItems;
    	
        LittleEndian.putUShort(data, 0 + offset, sid);
        LittleEndian.putUShort(data, 2 + offset, dataSize);

        LittleEndian.putUShort(data, 4 + offset, nItems);
        
        int pos = offset + 6;
    	for (int i = 0; i < nItems; i++) {
    		LittleEndian.putUShort(data, pos, field_1_seriesNumbers[i]);
    		pos += 2;
    	}

        return 4 + dataSize;
    }

    protected int getDataSize() {
        return field_1_seriesNumbers.length * 2 + 2;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        return new SeriesListRecord((short[]) field_1_seriesNumbers.clone());
    }

    /**
     * Get the series numbers field for the SeriesList record.
     */
    public short[] getSeriesNumbers() {
        return field_1_seriesNumbers;
    }

    /**
     * Set the series numbers field for the SeriesList record.
     */
    public void setSeriesNumbers(short[] field_1_seriesNumbers) {
        this.field_1_seriesNumbers = field_1_seriesNumbers;
    }
}



