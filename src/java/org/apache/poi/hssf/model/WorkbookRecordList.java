
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
package org.apache.poi.hssf.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.Record;

public class WorkbookRecordList
{
    private List records = new ArrayList();

    private int  protpos     = 0;   // holds the position of the protect record.
    private int  bspos       = 0;   // holds the position of the last bound sheet.
    private int  tabpos      = 0;   // holds the position of the tabid record
    private int  fontpos     = 0;   // hold the position of the last font record
    private int  xfpos       = 0;   // hold the position of the last extended font record
    private int  backuppos   = 0;   // holds the position of the backup record.
    private int  namepos     = 0;   // holds the position of last name record
    private int  supbookpos  = 0;   // holds the position of sup book
    private int  externsheetPos = 0;// holds the position of the extern sheet
    private int  palettepos  = -1;   // hold the position of the palette, if applicable


    public void setRecords( List records )
    {
        this.records = records;
    }

    public int size()
    {
        return records.size();
    }

    public Record get( int i )
    {
        return (Record) records.get(i);
    }

    public void add( int pos, Record r )
    {
        records.add(pos, r);
        if (getProtpos() >= pos) setProtpos( protpos + 1 );
        if (getBspos() >= pos) setBspos( bspos + 1 );
        if (getTabpos() >= pos) setTabpos( tabpos + 1 );
        if (getFontpos() >= pos) setFontpos( fontpos + 1 );
        if (getXfpos() >= pos) setXfpos( xfpos + 1 );
        if (getBackuppos() >= pos) setBackuppos( backuppos + 1 );
        if (getNamepos() >= pos) setNamepos(namepos+1);
        if (getSupbookpos() >= pos) setSupbookpos(supbookpos+1);
        if ((getPalettepos() != -1) && (getPalettepos() >= pos)) setPalettepos( palettepos + 1 );
        if (getExternsheetPos() >= pos) setExternsheetPos(getExternsheetPos() + 1);
    }

    public List getRecords()
    {
        return records;
    }

    public Iterator iterator()
    {
        return records.iterator();
    }

    public void remove( int pos )
    {
        records.remove(pos);
        if (getProtpos() >= pos) setProtpos( protpos - 1 );
        if (getBspos() >= pos) setBspos( bspos - 1 );
        if (getTabpos() >= pos) setTabpos( tabpos - 1 );
        if (getFontpos() >= pos) setFontpos( fontpos - 1 );
        if (getXfpos() >= pos) setXfpos( xfpos - 1 );
        if (getBackuppos() >= pos) setBackuppos( backuppos - 1 );
        if (getNamepos() >= pos) setNamepos(getNamepos()-1);
        if (getSupbookpos() >= pos) setSupbookpos(getSupbookpos()-1);
        if ((getPalettepos() != -1) && (getPalettepos() >= pos)) setPalettepos( palettepos - 1 );
        if (getExternsheetPos() >= pos) setExternsheetPos( getExternsheetPos() -1);
    }

    public int getProtpos()
    {
        return protpos;
    }

    public void setProtpos( int protpos )
    {
        this.protpos = protpos;
    }

    public int getBspos()
    {
        return bspos;
    }

    public void setBspos( int bspos )
    {
        this.bspos = bspos;
    }

    public int getTabpos()
    {
        return tabpos;
    }

    public void setTabpos( int tabpos )
    {
        this.tabpos = tabpos;
    }

    public int getFontpos()
    {
        return fontpos;
    }

    public void setFontpos( int fontpos )
    {
        this.fontpos = fontpos;
    }

    public int getXfpos()
    {
        return xfpos;
    }

    public void setXfpos( int xfpos )
    {
        this.xfpos = xfpos;
    }

    public int getBackuppos()
    {
        return backuppos;
    }

    public void setBackuppos( int backuppos )
    {
        this.backuppos = backuppos;
    }

    public int getPalettepos()
    {
        return palettepos;
    }

    public void setPalettepos( int palettepos )
    {
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

}
