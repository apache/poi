package org.apache.poi.hssf.model;

import org.apache.poi.hssf.record.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class WorkbookRecordList
{
    private List records = new ArrayList();

    private int  protpos     = 0;   // holds the position of the protect record.
    private int  bspos       = 0;   // holds the position of the last bound sheet.
    private int  tabpos      = 0;   // holds the position of the tabid record
    private int  fontpos     = 0;   // hold the position of the last font record
    private int  xfpos       = 0;   // hold the position of the last extended font record
    private int  backuppos   = 0;   // holds the position of the backup record.
//    public int  namepos     = 0;   // holds the position of last name record
//    public int  supbookpos  = 0;   // holds the position of sup book
    private int  palettepos  = 0;   // hold the position of the palette, if applicable


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
//        if (namepos >= pos) namepos++;
//        if (supbookpos >= pos) supbookpos++;
        if (getPalettepos() >= pos) setPalettepos( palettepos + 1 );
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
//        if (namepos >= pos) namepos--;
//        if (supbookpos >= pos) supbookpos--;
        if (getPalettepos() >= pos) setPalettepos( palettepos - 1 );
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


}
