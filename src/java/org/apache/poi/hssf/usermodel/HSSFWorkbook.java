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

/*
 * HSSFWorkbook.java
 *
 * Created on September 30, 2001, 3:37 PM
 */
package org.apache.poi.hssf.usermodel;

import org.apache.poi.util.POILogFactory;
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.POILogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * High level representation of a workbook.  This is the first object most users
 * will construct whether they are reading or writing a workbook.  It is also the
 * top level object for creating new sheets/etc.
 *
 * @see org.apache.poi.hssf.model.Workbook
 * @see org.apache.poi.hssf.usermodel.HSSFSheet
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Glen Stampoultzis (glens at apache.org)
 * @version 2.0-pre
 */

public class HSSFWorkbook
        extends java.lang.Object
{
    private static final int DEBUG = POILogger.DEBUG;

    /**
     * used for compile-time performance/memory optimization.  This determines the
     * initial capacity for the sheet collection.  Its currently set to 3.
     * Changing it in this release will decrease performance
     * since you're never allowed to have more or less than three sheets!
     */

    public final static int INITIAL_CAPACITY = 3;

    /**
     * this is the reference to the low level Workbook object
     */

    private Workbook workbook;

    /**
     * this holds the HSSFSheet objects attached to this workbook
     */

    private ArrayList sheets;
    private static POILogger log = POILogFactory.getLogger(HSSFWorkbook.class);

    /**
     * Creates new HSSFWorkbook from scratch (start here!)
     *
     */

    public HSSFWorkbook()
    {
        workbook = Workbook.createWorkbook();
        sheets = new ArrayList(INITIAL_CAPACITY);
    }

    /**
     * given a POI POIFSFileSystem object, read in its Workbook and populate the high and
     * low level models.  If you're reading in a workbook...start here.
     *
     * @param fs the POI filesystem that contains the Workbook stream.
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem
     * @exception IOException if the stream cannot be read
     */

    public HSSFWorkbook(POIFSFileSystem fs)
            throws IOException
    {
        sheets = new ArrayList(INITIAL_CAPACITY);
        InputStream stream = fs.createDocumentInputStream("Workbook");
        List records = RecordFactory.createRecords(stream);

        workbook = Workbook.createWorkbook(records);
        setPropertiesFromWorkbook(workbook);
        int numRecords = workbook.getNumRecords();
        int sheetNum = 0;

        while (numRecords < records.size())
        {
            Sheet sheet = Sheet.createSheet(records, sheetNum++, numRecords);

            numRecords += sheet.getNumRecords();
            sheet.convertLabelRecords(
                    workbook);   // convert all LabelRecord records to LabelSSTRecord
            HSSFSheet hsheet = new HSSFSheet(workbook, sheet);

            sheets.add(hsheet);

            // workbook.setSheetName(sheets.size() -1, "Sheet"+sheets.size());
        }
    }

    /**
     * Companion to HSSFWorkbook(POIFSFileSystem), this constructs the POI filesystem around your
     * inputstream.
     *
     * @param s  the POI filesystem that contains the Workbook stream.
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem
     * @see #HSSFWorkbook(POIFSFileSystem)
     * @exception IOException if the stream cannot be read
     */

    public HSSFWorkbook(InputStream s)
            throws IOException
    {
        this((new POIFSFileSystem(s)));
    }

    /**
     * used internally to set the workbook properties.
     */

    private void setPropertiesFromWorkbook(Workbook book)
    {
        this.workbook = book;

        // none currently
    }

    /**
     * set the sheet name.
     * @param sheet number (0 based)
     * @param sheet name
     */

    public void setSheetName(int sheet, String name)
    {
        if (sheet > (sheets.size() - 1))
        {
            throw new RuntimeException("Sheet out of bounds");
        }
        workbook.setSheetName(sheet, name);
    }

    /**
     * get the sheet name
     * @param sheet Number
     * @return Sheet name
     */

    public String getSheetName(int sheet)
    {
        if (sheet > (sheets.size() - 1))
        {
            throw new RuntimeException("Sheet out of bounds");
        }
        return workbook.getSheetName(sheet);
    }

    /**
     * get the sheet's index
     * @param name  sheet name
     * @return sheet index or -1 if it was not found.
     */

    public int getSheetIndex(String name)
    {
        int retval = -1;

        for (int k = 0; k < sheets.size(); k++)
        {
            String sheet = workbook.getSheetName(k);

            if (sheet.equals(name))
            {
                retval = k;
                break;
            }
        }
        return retval;
    }

    /**
     * create an HSSFSheet for this HSSFWorkbook, adds it to the sheets and returns
     * the high level representation.  Use this to create new sheets.
     *
     * @return HSSFSheet representing the new sheet.
     */

    public HSSFSheet createSheet()
    {

//        if (getNumberOfSheets() == 3)
//            throw new RuntimeException("You cannot have more than three sheets in HSSF 1.0");
        HSSFSheet sheet = new HSSFSheet(workbook);

        sheets.add(sheet);
        workbook.setSheetName(sheets.size() - 1,
                "Sheet" + (sheets.size() - 1));
        WindowTwoRecord windowTwo = (WindowTwoRecord) sheet.getSheet().findFirstRecordBySid(WindowTwoRecord.sid);
        windowTwo.setSelected(sheets.size() == 1);
        windowTwo.setPaged(sheets.size() == 1);
        return sheet;
    }

    /**
     * create an HSSFSheet for this HSSFWorkbook, adds it to the sheets and returns
     * the high level representation.  Use this to create new sheets.
     *
     * @param sheetname     sheetname to set for the sheet.
     * @return HSSFSheet representing the new sheet.
     */

    public HSSFSheet createSheet(String sheetname)
    {

//        if (getNumberOfSheets() == 3)
//            throw new RuntimeException("You cannot have more than three sheets in HSSF 1.0");
        HSSFSheet sheet = new HSSFSheet(workbook);

        sheets.add(sheet);
        workbook.setSheetName(sheets.size() - 1, sheetname);
        WindowTwoRecord windowTwo = (WindowTwoRecord) sheet.getSheet().findFirstRecordBySid(WindowTwoRecord.sid);
        windowTwo.setSelected(sheets.size() == 1);
        windowTwo.setPaged(sheets.size() == 1);
        return sheet;
    }

    /**
     * get the number of spreadsheets in the workbook (this will be three after serialization)
     * @return number of sheets
     */

    public int getNumberOfSheets()
    {
        return sheets.size();
    }

    /**
     * Get the HSSFSheet object at the given index.
     * @param index of the sheet number (0-based physical & logical)
     * @return HSSFSheet at the provided index
     */

    public HSSFSheet getSheetAt(int index)
    {
        return (HSSFSheet) sheets.get(index);
    }

    /**
     * Get sheet with the given name
     * @param name of the sheet
     * @return HSSFSheet with the name provided or null if it does not exist
     */

    public HSSFSheet getSheet(String name)
    {
        HSSFSheet retval = null;

        for (int k = 0; k < sheets.size(); k++)
        {
            String sheetname = workbook.getSheetName(k);

            if (sheetname.equals(name))
            {
                retval = (HSSFSheet) sheets.get(k);
            }
        }
        return retval;
    }

    /**
     * removes sheet at the given index
     * @param index of the sheet  (0-based)
     */

    public void removeSheetAt(int index)
    {
        sheets.remove(index);
        workbook.removeSheet(index);
    }

    /**
     * determine whether the Excel GUI will backup the workbook when saving.
     *
     * @param backupValue   true to indicate a backup will be performed.
     */

    public void setBackupFlag(boolean backupValue)
    {
        BackupRecord backupRecord = workbook.getBackupRecord();

        backupRecord.setBackup(backupValue ? (short) 1
                : (short) 0);
    }

    /**
     * determine whether the Excel GUI will backup the workbook when saving.
     *
     * @return the current setting for backups.
     */

    public boolean getBackupFlag()
    {
        BackupRecord backupRecord = workbook.getBackupRecord();

        return (backupRecord.getBackup() == 0) ? false
                : true;
    }

    /**
     * create a new Font and add it to the workbook's font table
     * @return new font object
     */

    public HSSFFont createFont()
    {
        FontRecord font = workbook.createNewFont();
        short fontindex = (short) (getNumberOfFonts() - 1);

        if (fontindex > 3)
        {
            fontindex++;   // THERE IS NO FOUR!!
        }
        HSSFFont retval = new HSSFFont(fontindex, font);

        return retval;
    }

    /**
     * get the number of fonts in the font table
     * @return number of fonts
     */

    public short getNumberOfFonts()
    {
        return (short) workbook.getNumberOfFontRecords();
    }

    /**
     * get the font at the given index number
     * @param idx  index number
     * @return HSSFFont at the index
     */

    public HSSFFont getFontAt(short idx)
    {
        FontRecord font = workbook.getFontRecordAt(idx);
        HSSFFont retval = new HSSFFont(idx, font);

        return retval;
    }

    /**
     * create a new Cell style and add it to the workbook's style table
     * @return the new Cell Style object
     */

    public HSSFCellStyle createCellStyle()
    {
        ExtendedFormatRecord xfr = workbook.createCellXF();
        short index = (short) (getNumCellStyles() - 1);
        HSSFCellStyle style = new HSSFCellStyle(index, xfr);

        return style;
    }

    /**
     * get the number of styles the workbook contains
     * @return count of cell styles
     */

    public short getNumCellStyles()
    {
        return (short) workbook.getNumExFormats();
    }

    /**
     * get the cell style object at the given index
     * @param idx  index within the set of styles
     * @return HSSFCellStyle object at the index
     */

    public HSSFCellStyle getCellStyleAt(short idx)
    {
        ExtendedFormatRecord xfr = workbook.getExFormatAt(idx);
        HSSFCellStyle style = new HSSFCellStyle(idx, xfr);

        return style;
    }

    /**
     * Method write - write out this workbook to an Outputstream.  Constructs
     * a new POI POIFSFileSystem, passes in the workbook binary representation  and
     * writes it out.
     *
     * @param stream - the java OutputStream you wish to write the XLS to
     *
     * @exception IOException if anything can't be written.
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem
     */

    public void write(OutputStream stream)
            throws IOException
    {
        byte[] bytes = getBytes();
        POIFSFileSystem fs = new POIFSFileSystem();

        fs.createDocument(new ByteArrayInputStream(bytes), "Workbook");
        fs.writeFilesystem(stream);
    }

    /**
     * Method getBytes - get the bytes of just the HSSF portions of the XLS file.
     * Use this to construct a POI POIFSFileSystem yourself.
     *
     *
     * @return byte[] array containing the binary representation of this workbook and all contained
     *         sheets, rows, cells, etc.
     *
     * @see org.apache.poi.hssf.model.Workbook
     * @see org.apache.poi.hssf.model.Sheet
     */

    public byte[] getBytes()
    {
        log.log(DEBUG, "HSSFWorkbook.getBytes()");
        int wbsize = workbook.getSize();

        // log.debug("REMOVEME: old sizing method "+workbook.serialize().length);
        // ArrayList sheetbytes = new ArrayList(sheets.size());
        int totalsize = wbsize;

        for (int k = 0; k < sheets.size(); k++)
        {
            workbook.setSheetBof(k, totalsize);

            // sheetbytes.add((( HSSFSheet ) sheets.get(k)).getSheet().getSize());
            totalsize += ((HSSFSheet) sheets.get(k)).getSheet().getSize();
        }
        if (totalsize < 4096)
        {
            totalsize = 4096;
        }
        byte[] retval = new byte[totalsize];
        int pos = workbook.serialize(0, retval);

        // System.arraycopy(wb, 0, retval, 0, wb.length);
        for (int k = 0; k < sheets.size(); k++)
        {

            // byte[] sb = (byte[])sheetbytes.get(k);
            // System.arraycopy(sb, 0, retval, pos, sb.length);
            pos += ((HSSFSheet) sheets.get(k)).getSheet().serialize(pos,
                    retval);   // sb.length;
        }
        for (int k = pos; k < totalsize; k++)
        {
            retval[k] = 0;
        }
        return retval;
    }

    public int addSSTString(String string)
    {
        return workbook.addSSTString(string);
    }

    public String getSSTString(int index)
    {
        return workbook.getSSTString(index);
    }

    Workbook getWorkbook()
    {
        return workbook;
    }
}
