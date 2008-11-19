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

import org.apache.poi.util.LittleEndianOutput;

/**
 * Title: Sheet Tab Index Array Record (0x013D)<p/>
 * Description:  Contains an array of sheet id's.  Sheets always keep their ID
 *               regardless of what their name is.<p/>
 * REFERENCE:  PG 412 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<p/>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 *
 */
public final class TabIdRecord extends StandardRecord {
    public final static short sid = 0x13d;
	private static final short[] EMPTY_SHORT_ARRAY = { };
	
    public short[] _tabids;

    public TabIdRecord() {
    	_tabids = EMPTY_SHORT_ARRAY;
    }

    public TabIdRecord(RecordInputStream in) {
    	int nTabs = in.remaining() / 2;
        _tabids = new short[nTabs];
        for (int k = 0; k < _tabids.length; k++) {
            _tabids[ k ] = in.readShort();
        }
    }

    /**
     * set the tab array.  (0,1,2).
     * @param array of tab id's {0,1,2}
     */
    public void setTabIdArray(short[] array) {
        _tabids = array;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[TABID]\n");
        buffer.append("    .elements        = ").append(_tabids.length).append("\n");
        for (int k = 0; k < _tabids.length; k++)
        {
            buffer.append("    .element_").append(k).append(" = ").append(_tabids[ k ]).append("\n");
        }
        buffer.append("[/TABID]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        short[] tabids = _tabids;

        for (int k = 0; k < tabids.length; k++) {
            out.writeShort(tabids[ k ]);
        }
    }

    protected int getDataSize() {
        return _tabids.length * 2;
    }

    public short getSid() {
        return sid;
    }
}
