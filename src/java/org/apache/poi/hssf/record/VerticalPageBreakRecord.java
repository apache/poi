
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        
package org.apache.poi.hssf.record;

/**
 * VerticalPageBreak record that stores page breaks at columns
 * <p>
 * This class is just used so that SID compares work properly in the RecordFactory
 * @see PageBreakRecord
 * @author Danny Mui (dmui at apache dot org) 
 */
public class VerticalPageBreakRecord extends PageBreakRecord {
	
    public static final short sid = PageBreakRecord.VERTICAL_SID;
    
	/**
	 * 
	 */
	public VerticalPageBreakRecord() {
		super();
	}

	/**
	 * @param sid
	 */
	public VerticalPageBreakRecord(short sid) {
		super(sid);
	}

	/**
	 * @param id
	 * @param size
	 * @param data
	 */
	public VerticalPageBreakRecord(short id, short size, byte[] data) {
		super(id, size, data);
	}

	/**
	 * @param id
	 * @param size
	 * @param data
	 * @param offset
	 */
	public VerticalPageBreakRecord(short id, short size, byte[] data, int offset) {
		super(id, size, data, offset);
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.hssf.record.Record#getSid()
	 */
	public short getSid() {
		return PageBreakRecord.VERTICAL_SID;
	}

}
