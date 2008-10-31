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
 * HSSF Chart Title Format Record Type
 */
package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;

import java.util.ArrayList;

/**
 * Describes the formatting runs associated with a chart title.
 */
public class ChartTitleFormatRecord extends Record {
	public static final short sid = 0x1050;
	
	private int 		m_recs;
	
	private class CTFormat {
		private short m_offset;
		private short m_fontIndex;
		
		protected CTFormat(short offset,short fontIdx){
			m_offset = offset;
			m_fontIndex = fontIdx;
		}
		
		public short getOffset(){
			return m_offset;
		}
		public void setOffset(short newOff){
			m_offset = newOff;
		}
		public short getFontIndex() {
			return m_fontIndex;
		}
	}
	
	private ArrayList m_formats;

	public ChartTitleFormatRecord() {
		super();
	}

	public ChartTitleFormatRecord(RecordInputStream in) {
		m_recs = in.readUShort();
		int idx;
		CTFormat ctf;
		if (m_formats == null){
			m_formats = new ArrayList(m_recs);
		}
		for(idx=0;idx<m_recs;idx++) {
			ctf = new CTFormat(in.readShort(),in.readShort());
			m_formats.add(ctf);
		}
	}

	public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              ( short ) (getRecordSize() - 4));   
        int idx;
        CTFormat ctf;
        LittleEndian.putShort(data, 4 + offset,(short)m_formats.size());
        for(idx=0;idx<m_formats.size();idx++){
        	ctf = (CTFormat)m_formats.get(idx);
        	LittleEndian.putShort(data, 6 + (idx * 4) + offset, ctf.getOffset());
        	LittleEndian.putShort(data, 8 + (idx * 4) + offset, ctf.getFontIndex());
        }
        
        return getRecordSize();
    }

    protected int getDataSize() {
        return 2 + (4 * m_formats.size());
    }
    
	public short getSid() {
		return sid;
	}
	
	public int getFormatCount() {
		return m_formats.size();
	}
	
	public void modifyFormatRun(short oldPos,short newLen) {
		short shift = (short)0;
		for(int idx=0;idx < m_formats.size();idx++) {
			CTFormat ctf = (CTFormat)m_formats.get(idx);
			if (shift != 0) {
				ctf.setOffset((short)(ctf.getOffset() + shift));
			} else if ((oldPos == ctf.getOffset()) && (idx < (m_formats.size() - 1))){
				CTFormat nextCTF = (CTFormat)m_formats.get(idx + 1);
				shift = (short)(newLen - (nextCTF.getOffset() - ctf.getOffset()));
			} 
		}
	}
	
	public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[CHARTTITLEFORMAT]\n");
        buffer.append("    .format_runs       = ").append(m_recs)
            .append("\n");
        int idx;
        CTFormat ctf;
        for(idx=0;idx<m_formats.size();idx++){
        	ctf = (CTFormat)m_formats.get(idx);
        	buffer.append("       .char_offset= ").append(ctf.getOffset());
        	buffer.append(",.fontidx= ").append(ctf.getFontIndex());
            buffer.append("\n");
        }
        buffer.append("[/CHARTTITLEFORMAT]\n");
        return buffer.toString();
    }
}
