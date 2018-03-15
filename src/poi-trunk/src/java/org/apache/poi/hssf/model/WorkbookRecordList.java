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

package org.apache.poi.hssf.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.record.Record;

public final class WorkbookRecordList {
    private List<Record> records = new ArrayList<>();

    /** holds the position of the protect record */
	private int protpos;
    /** holds the position of the last bound sheet */
	private int bspos;
    /** holds the position of the tabid record */
	private int tabpos;
    /** hold the position of the last font record */
	private int fontpos;
    /** hold the position of the last extended font record */
	private int xfpos;
    /** holds the position of the backup record */
	private int backuppos;
    /** holds the position of last name record */
	private int namepos;
	/** holds the position of sup book */
	private int supbookpos;
	/** holds the position of the extern sheet */
	private int externsheetPos;
	/** hold the position of the palette, if applicable */
	private int palettepos     = -1;


	public void setRecords(List<Record> records) {
	    this.records = records;
	}

	public int size() {
		return records.size();
	}

	public Record get(int i) {
		return records.get(i);
	}

	public void add(int pos, Record r) {
		records.add(pos, r);
		updateRecordPos(pos, true);
	}

	public List<Record> getRecords() {
		return records;
	}

	/**
	 * Find the given record in the record list by identity and removes it
	 *
	 * @param record the identical record to be searched for
	 */
	public void remove( Object record ) {
	   // can't use List.indexOf here because it checks the records for equality and not identity
	   int i = 0;
	   for (Record r : records) {
	       if (r == record) {
	           remove(i);
	           break;
	       }
	       i++;
	   }
	}

	public void remove( int pos ) {
		records.remove(pos);
		updateRecordPos(pos, false);
	}

	public int getProtpos() {
		return protpos;
	}

	public void setProtpos(int protpos) {
		this.protpos = protpos;
	}

	public int getBspos() {
		return bspos;
	}

	public void setBspos(int bspos) {
		this.bspos = bspos;
	}

	public int getTabpos() {
		return tabpos;
	}

	public void setTabpos(int tabpos) {
		this.tabpos = tabpos;
	}

	public int getFontpos() {
		return fontpos;
	}

	public void setFontpos(int fontpos) {
		this.fontpos = fontpos;
	}

	public int getXfpos() {
		return xfpos;
	}

	public void setXfpos(int xfpos) {
		this.xfpos = xfpos;
	}

	public int getBackuppos() {
		return backuppos;
	}

	public void setBackuppos(int backuppos) {
		this.backuppos = backuppos;
	}

	public int getPalettepos() {
		return palettepos;
	}

	public void setPalettepos(int palettepos) {
		this.palettepos = palettepos;
	}


	/**
	 * Returns the namepos.
	 * @return int
	 */
	public int getNamepos() {
		return namepos;
	}

	/**
	 * Returns the supbookpos.
	 * @return int
	 */
	public int getSupbookpos() {
		return supbookpos;
	}

	/**
	 * Sets the namepos.
	 * @param namepos The namepos to set
	 */
	public void setNamepos(int namepos) {
		this.namepos = namepos;
	}

	/**
	 * Sets the supbookpos.
	 * @param supbookpos The supbookpos to set
	 */
	public void setSupbookpos(int supbookpos) {
		this.supbookpos = supbookpos;
	}

	/**
	 * Returns the externsheetPos.
	 * @return int
	 */
	public int getExternsheetPos() {
		return externsheetPos;
	}

	/**
	 * Sets the externsheetPos.
	 * @param externsheetPos The externsheetPos to set
	 */
	public void setExternsheetPos(int externsheetPos) {
		this.externsheetPos = externsheetPos;
	}

    private void updateRecordPos(int pos, boolean add) {
        int delta = (add) ? 1 : -1;
        int p = getProtpos();
        if (p >= pos) {
            setProtpos( p + delta );
        }
        p = getBspos();
        if (p >= pos) {
            setBspos( p + delta );
        }
        p = getTabpos();
        if (p >= pos) {
            setTabpos( p + delta );
        }
        p = getFontpos();
        if (p >= pos) {
            setFontpos( p + delta );
        }
        p = getXfpos();
        if (p >= pos) {
            setXfpos( p + delta );
        }
        p = getBackuppos();
        if (p >= pos) {
            setBackuppos( p + delta );
        }
        p = getNamepos();
        if (p >= pos) {
            setNamepos(p + delta );
        }
        p = getSupbookpos();
        if (p >= pos) {
            setSupbookpos(p + delta);
        }
        p = getPalettepos();
        if (p != -1 && p >= pos) {
            setPalettepos( p + delta );
        }
        p = getExternsheetPos();
        if (p >= pos) {
            setExternsheetPos( p + delta );
        }
    }
}
