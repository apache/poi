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


/*
 * HSSFWorkbook.java
 *
 * Created on September 30, 2001, 3:37 PM
 */
package org.apache.poi.hssf.usermodel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.poi.hssf.eventmodel.EventRecordFactory;
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.MemFuncPtg;
import org.apache.poi.hssf.record.formula.UnionPtg;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * High level representation of a workbook.  This is the first object most users
 * will construct whether they are reading or writing a workbook.  It is also the
 * top level object for creating new sheets/etc.
 *
 * @see org.apache.poi.hssf.model.Workbook
 * @see org.apache.poi.hssf.usermodel.HSSFSheet
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Glen Stampoultzis (glens at apache.org)
 * @author  Shawn Laubach (slaubach at apache dot org)
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

    /**
     * this holds the HSSFName objects attached to this workbook
     */

    private ArrayList names;

    /**
     * holds whether or not to preserve other nodes in the POIFS.  Used
     * for macros and embedded objects.
     */
    private boolean   preserveNodes;

    /**
     * if you do preserve the nodes, you'll need to hold the whole POIFS in
     * memory.
     */
    private POIFSFileSystem poifs;

    /**
     * Used to keep track of the data formatter so that all
     * createDataFormatter calls return the same one for a given
     * book.  This ensures that updates from one places is visible
     * someplace else.
     */
    private HSSFDataFormat formatter;

    private static POILogger log = POILogFactory.getLogger(HSSFWorkbook.class);

    /**
     * Creates new HSSFWorkbook from scratch (start here!)
     *
     */

    public HSSFWorkbook()
    {
        workbook = Workbook.createWorkbook();
        sheets = new ArrayList(INITIAL_CAPACITY);
        names  = new ArrayList(INITIAL_CAPACITY);
    }

    public HSSFWorkbook(POIFSFileSystem fs) throws IOException {
      this(fs,true);
    }

    /**
     * given a POI POIFSFileSystem object, read in its Workbook and populate the high and
     * low level models.  If you're reading in a workbook...start here.
     *
     * @param fs the POI filesystem that contains the Workbook stream.
     * @param preserveNodes whether to preseve other nodes, such as
     *        macros.  This takes more memory, so only say yes if you
     *        need to.
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem
     * @exception IOException if the stream cannot be read
     */

    public HSSFWorkbook(POIFSFileSystem fs, boolean preserveNodes)
            throws IOException
    {
        this.preserveNodes = preserveNodes;

        if (preserveNodes) {
           this.poifs = fs;
        }

        sheets = new ArrayList(INITIAL_CAPACITY);
        names  = new ArrayList(INITIAL_CAPACITY);

        InputStream stream = fs.createDocumentInputStream("Workbook");

        EventRecordFactory factory = new EventRecordFactory();



        List records = RecordFactory.createRecords(stream);

        workbook = Workbook.createWorkbook(records);
        setPropertiesFromWorkbook(workbook);
        int recOffset = workbook.getNumRecords();
        int sheetNum = 0;

        while (recOffset < records.size())
        {
            Sheet sheet = Sheet.createSheet(records, sheetNum++, recOffset );

            recOffset = sheet.getEofLoc()+1;
            sheet.convertLabelRecords(
                    workbook);   // convert all LabelRecord records to LabelSSTRecord
            HSSFSheet hsheet = new HSSFSheet(workbook, sheet);

            sheets.add(hsheet);

            // workbook.setSheetName(sheets.size() -1, "Sheet"+sheets.size());
        }

        for (int i = 0 ; i < workbook.getNumNames() ; ++i){
            HSSFName name = new HSSFName(workbook, workbook.getNameRecord(i));
            names.add(name);
        }
    }

     public HSSFWorkbook(InputStream s) throws IOException {
         this(s,true);
     }

    /**
     * Companion to HSSFWorkbook(POIFSFileSystem), this constructs the POI filesystem around your
     * inputstream.
     *
     * @param s  the POI filesystem that contains the Workbook stream.
     * @param preserveNodes whether to preseve other nodes, such as
     *        macros.  This takes more memory, so only say yes if you
     *        need to.
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem
     * @see #HSSFWorkbook(POIFSFileSystem)
     * @exception IOException if the stream cannot be read
     */

    public HSSFWorkbook(InputStream s, boolean preserveNodes)
            throws IOException
    {
        this(new POIFSFileSystem(s), preserveNodes);
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
     * sets the order of appearance for a given sheet.
     *
     * @param sheetname the name of the sheet to reorder
     * @param pos the position that we want to insert the sheet into (0 based)
     */

    public void setSheetOrder(String sheetname, int pos ) {
        workbook.setSheetOrder(sheetname, pos);
    }

    public final static byte ENCODING_COMPRESSED_UNICODE = 0;
    public final static byte ENCODING_UTF_16             = 1;


    /**
     * set the sheet name. 
     * Will throw IllegalArgumentException if the name is greater than 31 chars
     * or contains /\?*[]
     * @param sheet number (0 based)
     * @param sheet name
     */

    public void setSheetName(int sheet, String name)
    {
        workbook.setSheetName( sheet, name, ENCODING_COMPRESSED_UNICODE );
    }

    public void setSheetName( int sheet, String name, short encoding )
    {
        if (sheet > (sheets.size() - 1))
        {
            throw new RuntimeException("Sheet out of bounds");
        }

        switch ( encoding ) {
        case ENCODING_COMPRESSED_UNICODE:
        case ENCODING_UTF_16:
            break;

        default:
            // TODO java.io.UnsupportedEncodingException
            throw new RuntimeException( "Unsupported encoding" );
        }

        workbook.setSheetName( sheet, name, encoding );
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

    /*
     * get the sheet's index
     * @param name  sheet name
     * @return sheet index or -1 if it was not found.
     */

    /** Returns the index of the sheet by his name
     * @param name the sheet name
     * @return index of the sheet (0 based)
     */
    public int getSheetIndex(String name)
    {
        int retval = workbook.getSheetIndex(name);

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
     * create an HSSFSheet from an existing sheet in the HSSFWorkbook.
     *
     * @return HSSFSheet representing the cloned sheet.
     */

    public HSSFSheet cloneSheet(int sheetNum) {
      HSSFSheet srcSheet = (HSSFSheet)sheets.get(sheetNum);
      String srcName = workbook.getSheetName(sheetNum);
      if (srcSheet != null) {
        HSSFSheet clonedSheet = srcSheet.cloneSheet(workbook);
        WindowTwoRecord windowTwo = (WindowTwoRecord) clonedSheet.getSheet().findFirstRecordBySid(WindowTwoRecord.sid);
        windowTwo.setSelected(sheets.size() == 1);
        windowTwo.setPaged(sheets.size() == 1);

        sheets.add(clonedSheet);
        if (srcName.length()<28) {
            workbook.setSheetName(sheets.size()-1, srcName+"(2)");
        }else {
            workbook.setSheetName(sheets.size()-1,srcName.substring(0,28)+"(2)");
        }
        return clonedSheet;
      }
      return null;
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
     * Sets the repeating rows and columns for a sheet (as found in
     * File->PageSetup->Sheet).  This is function is included in the workbook
     * because it creates/modifies name records which are stored at the
     * workbook level.
     * <p>
     * To set just repeating columns:
     * <pre>
     *  workbook.setRepeatingRowsAndColumns(0,0,1,-1-1);
     * </pre>
     * To set just repeating rows:
     * <pre>
     *  workbook.setRepeatingRowsAndColumns(0,-1,-1,0,4);
     * </pre>
     * To remove all repeating rows and columns for a sheet.
     * <pre>
     *  workbook.setRepeatingRowsAndColumns(0,-1,-1,-1,-1);
     * </pre>
     *
     * @param sheetIndex    0 based index to sheet.
     * @param startColumn   0 based start of repeating columns.
     * @param endColumn     0 based end of repeating columns.
     * @param startRow      0 based start of repeating rows.
     * @param endRow        0 based end of repeating rows.
     */
    public void setRepeatingRowsAndColumns(int sheetIndex,
                                           int startColumn, int endColumn,
                                           int startRow, int endRow)
    {
        // Check arguments
        if (startColumn == -1 && endColumn != -1) throw new IllegalArgumentException("Invalid column range specification");
        if (startRow == -1 && endRow != -1) throw new IllegalArgumentException("Invalid row range specification");
        if (startColumn < -1 || startColumn >= 0xFF) throw new IllegalArgumentException("Invalid column range specification");
        if (endColumn < -1 || endColumn >= 0xFF) throw new IllegalArgumentException("Invalid column range specification");
        if (startRow < -1 || startRow > 65535) throw new IllegalArgumentException("Invalid row range specification");
        if (endRow < -1 || endRow > 65535) throw new IllegalArgumentException("Invalid row range specification");
        if (startColumn > endColumn) throw new IllegalArgumentException("Invalid column range specification");
        if (startRow > endRow) throw new IllegalArgumentException("Invalid row range specification");

        HSSFSheet sheet = getSheetAt(sheetIndex);
        short externSheetIndex = getWorkbook().checkExternSheet(sheetIndex);

        boolean settingRowAndColumn =
                startColumn != -1 && endColumn != -1 && startRow != -1 && endRow != -1;
        boolean removingRange =
                startColumn == -1 && endColumn == -1 && startRow == -1 && endRow == -1;

        boolean isNewRecord = false;
        NameRecord nameRecord;
        nameRecord = findExistingRowColHeaderNameRecord(sheetIndex);
        if (removingRange )
        {
            if (nameRecord != null)
                workbook.removeName(findExistingRowColHeaderNameRecordIdx(sheetIndex+1));
            return;
        }
        if ( nameRecord == null )
        {
            nameRecord = workbook.createBuiltInName(NameRecord.BUILTIN_PRINT_TITLE, sheetIndex+1);
            //does a lot of the house keeping for builtin records, like setting lengths to zero etc
            isNewRecord = true;
        }

        short definitionTextLength = settingRowAndColumn ? (short)0x001a : (short)0x000b;
        nameRecord.setDefinitionTextLength(definitionTextLength);

        Stack ptgs = new Stack();

        if (settingRowAndColumn)
        {
            MemFuncPtg memFuncPtg = new MemFuncPtg();
            memFuncPtg.setLenRefSubexpression(23);
            ptgs.add(memFuncPtg);
        }
        if (startColumn >= 0)
        {
            Area3DPtg area3DPtg1 = new Area3DPtg();
            area3DPtg1.setExternSheetIndex(externSheetIndex);
            area3DPtg1.setFirstColumn((short)startColumn);
            area3DPtg1.setLastColumn((short)endColumn);
            area3DPtg1.setFirstRow((short)0);
            area3DPtg1.setLastRow((short)0xFFFF);
            ptgs.add(area3DPtg1);
        }
        if (startRow >= 0)
        {
            Area3DPtg area3DPtg2 = new Area3DPtg();
            area3DPtg2.setExternSheetIndex(externSheetIndex);
            area3DPtg2.setFirstColumn((short)0);
            area3DPtg2.setLastColumn((short)0x00FF);
            area3DPtg2.setFirstRow((short)startRow);
            area3DPtg2.setLastRow((short)endRow);
            ptgs.add(area3DPtg2);
        }
        if (settingRowAndColumn)
        {
            UnionPtg unionPtg = new UnionPtg();
            ptgs.add(unionPtg);
        }
        nameRecord.setNameDefinition(ptgs);

        if (isNewRecord)
        {
            HSSFName newName = new HSSFName(workbook, nameRecord);
            names.add(newName);
        }

        HSSFPrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setValidSettings(false);

        WindowTwoRecord w2 = (WindowTwoRecord) sheet.getSheet().findFirstRecordBySid(WindowTwoRecord.sid);
        w2.setPaged(true);
    }

    private NameRecord findExistingRowColHeaderNameRecord( int sheetIndex )
    {
        int index = findExistingRowColHeaderNameRecordIdx(sheetIndex);
        if (index == -1)
            return null;
        else
            return (NameRecord)workbook.findNextRecordBySid(NameRecord.sid, index);
    }

    private int findExistingRowColHeaderNameRecordIdx( int sheetIndex )
    {
        int index = 0;
        NameRecord r = null;
        while ((r = (NameRecord) workbook.findNextRecordBySid(NameRecord.sid, index)) != null)
        {
            int nameRecordSheetIndex = workbook.getSheetIndexFromExternSheetIndex(r.getEqualsToIndexToSheet() - 1);
            if (isRowColHeaderRecord( r ) && nameRecordSheetIndex == sheetIndex)
            {
                return index;
            }
            index++;
        }

        return -1;
    }

    private boolean isRowColHeaderRecord( NameRecord r )
    {
        return r.getOptionFlag() == 0x20 && ("" + ((char)7)).equals(r.getNameText());
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
     * Finds a font that matches the one with the supplied attributes
     */
    public HSSFFont findFont(short boldWeight, short color, short fontHeight,
                             String name, boolean italic, boolean strikeout,
                             short typeOffset, byte underline)
    {
//        System.out.println( boldWeight + ", " + color + ", " + fontHeight + ", " + name + ", " + italic + ", " + strikeout + ", " + typeOffset + ", " + underline );
        for (short i = 0; i < workbook.getNumberOfFontRecords(); i++)
        {
            if (i == 4)
                continue;

            FontRecord font = workbook.getFontRecordAt(i);
            HSSFFont hssfFont = new HSSFFont(i, font);
//            System.out.println( hssfFont.getBoldweight() + ", " + hssfFont.getColor() + ", " + hssfFont.getFontHeight() + ", " + hssfFont.getFontName() + ", " + hssfFont.getItalic() + ", " + hssfFont.getStrikeout() + ", " + hssfFont.getTypeOffset() + ", " + hssfFont.getUnderline() );
            if (hssfFont.getBoldweight() == boldWeight
                    && hssfFont.getColor() == color
                    && hssfFont.getFontHeight() == fontHeight
                    && hssfFont.getFontName().equals(name)
                    && hssfFont.getItalic() == italic
                    && hssfFont.getStrikeout() == strikeout
                    && hssfFont.getTypeOffset() == typeOffset
                    && hssfFont.getUnderline() == underline)
            {
//                System.out.println( "Found font" );
                return hssfFont;
            }
        }

//        System.out.println( "No font found" );
        return null;
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

        if (preserveNodes) {
            List excepts = new ArrayList(1);
            excepts.add("Workbook");
            copyNodes(this.poifs,fs,excepts);
        }
        fs.writeFilesystem(stream);
        //poifs.writeFilesystem(stream);
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
        if (log.check( POILogger.DEBUG ))
            log.log(DEBUG, "HSSFWorkbook.getBytes()");

        // before getting the workbook size we must tell the sheets that
        // serialization is about to occur.
        for (int k = 0; k < sheets.size(); k++)
            ((HSSFSheet) sheets.get(k)).getSheet().preSerialize();

        int wbsize = workbook.getSize();

        // log.debug("REMOVEME: old sizing method "+workbook.serialize().length);
        // ArrayList sheetbytes = new ArrayList(sheets.size());
        int totalsize = wbsize;

        for (int k = 0; k < sheets.size(); k++)
        {
            workbook.setSheetBof(k, totalsize);
            totalsize += ((HSSFSheet) sheets.get(k)).getSheet().getSize();
        }


/*        if (totalsize < 4096)
        {
            totalsize = 4096;
        }*/
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
/*        for (int k = pos; k < totalsize; k++)
        {
            retval[k] = 0;
        }*/
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

    /** gets the total number of named ranges in the workboko
     * @return number of named ranges
     */
    public int getNumberOfNames(){
        int result = names.size();
        return result;
    }

    /** gets the Named range
     * @param index position of the named range
     * @return named range high level
     */
    public HSSFName getNameAt(int index){
        HSSFName result = (HSSFName) names.get(index);

        return result;
    }

    /** gets the named range name
     * @param index the named range index (0 based)
     * @return named range name
     */
    public String getNameName(int index){
        String result = getNameAt(index).getNameName();

        return result;
    }

	/**
	 * Sets the printarea for the sheet provided
	 * <p>
	 * i.e. Reference = $A$1:$B$2
	 * @param sheetIndex Zero-based sheet index (0 Represents the first sheet to keep consistent with java)
	 * @param reference Valid name Reference for the Print Area
	 */
	public void setPrintArea(int sheetIndex, String reference)
	{
		NameRecord name = workbook.getSpecificBuiltinRecord(NameRecord.BUILTIN_PRINT_AREA, sheetIndex+1);


		if (name == null)
			name = workbook.createBuiltInName(NameRecord.BUILTIN_PRINT_AREA, sheetIndex+1);
       //adding one here because 0 indicates a global named region; doesnt make sense for print areas

	    short externSheetIndex = getWorkbook().checkExternSheet(sheetIndex);
		name.setExternSheetNumber(externSheetIndex);
		name.setAreaReference(reference);


	}

	/**
	 * For the Convenience of Java Programmers maintaining pointers.
	 * @see setPrintArea(int, String)
	 * @param sheetIndex Zero-based sheet index (0 = First Sheet)
	 * @param startColumn Column to begin printarea
	 * @param endColumn Column to end the printarea
	 * @param startRow Row to begin the printarea
	 * @param endRow Row to end the printarea
	 */
	public void setPrintArea(int sheetIndex, int startColumn, int endColumn,
							  int startRow, int endRow) {

		//using absolute references because they dont get copied and pasted anyway
		CellReference cell = new CellReference(startRow, startColumn, true, true);
		String reference = cell.toString();

		cell = new CellReference(endRow, endColumn, true, true);
		reference = reference+":"+cell.toString();

		setPrintArea(sheetIndex, reference);
	}


	/**
	 * Retrieves the reference for the printarea of the specified sheet, the sheet name is appended to the reference even if it was not specified.
	 * @param sheetIndex Zero-based sheet index (0 Represents the first sheet to keep consistent with java)
	 * @return String Null if no print area has been defined
	 */
	public String getPrintArea(int sheetIndex)
	{
		NameRecord name = workbook.getSpecificBuiltinRecord(NameRecord.BUILTIN_PRINT_AREA, sheetIndex+1);
		if (name == null) return null;
		//adding one here because 0 indicates a global named region; doesnt make sense for print areas

		return name.getAreaReference(workbook);
	}

    /**
     * Delete the printarea for the sheet specified
     * @param sheetIndex Zero-based sheet index (0 = First Sheet)
     */
    public void removePrintArea(int sheetIndex) {
    	getWorkbook().removeBuiltinRecord(NameRecord.BUILTIN_PRINT_AREA, sheetIndex+1);
    }

    /** creates a new named range and add it to the model
     * @return named range high level
     */
    public HSSFName createName(){
        NameRecord nameRecord = workbook.createName();

        HSSFName newName = new HSSFName(workbook, nameRecord);

        names.add(newName);

        return newName;
    }

    /** gets the named range index by his name
     * @param name named range name
     * @return named range index
     */
    public int getNameIndex(String name)
    {
        int retval = -1;

        for (int k = 0; k < names.size(); k++)
        {
            String nameName = getNameName(k);

            if (nameName.equals(name))
            {
                retval = k;
                break;
            }
        }
        return retval;
    }


    /** remove the named range by his index
     * @param index named range index (0 based)
     */
    public void removeName(int index){
        names.remove(index);
        workbook.removeName(index);
    }

    /**
     * Returns the instance of HSSFDataFormat for this workbook.
     * @return the HSSFDataFormat object
     * @see org.apache.poi.hssf.record.FormatRecord
     * @see org.apache.poi.hssf.record.Record
     */
    public HSSFDataFormat createDataFormat() {
	if (formatter == null)
	    formatter = new HSSFDataFormat(workbook);
	return formatter;
    }

    /** remove the named range by his name
     * @param name named range name
     */
    public void removeName(String name){
        int index = getNameIndex(name);

        removeName(index);

    }

    public HSSFPalette getCustomPalette()
    {
        return new HSSFPalette(workbook.getCustomPalette());
    }

   /**
    * Copies nodes from one POIFS to the other minus the excepts
    * @param source is the source POIFS to copy from
    * @param target is the target POIFS to copy to
    * @param excepts is a list of Strings specifying what nodes NOT to copy
    */
   private void copyNodes(POIFSFileSystem source, POIFSFileSystem target,
                          List excepts) throws IOException {
      //System.err.println("CopyNodes called");

      DirectoryEntry root = source.getRoot();
      DirectoryEntry newRoot = target.getRoot();

      Iterator entries = root.getEntries();

      while (entries.hasNext()) {
         Entry entry = (Entry)entries.next();
         if (!isInList(entry.getName(), excepts)) {
             copyNodeRecursively(entry,newRoot);
         }
      }
   }

   private boolean isInList(String entry, List list) {
       for (int k = 0; k < list.size(); k++) {
          if (list.get(k).equals(entry)) {
            return true;
          }
       }
       return false;
   }

   private void copyNodeRecursively(Entry entry, DirectoryEntry target)
   throws IOException {
       //System.err.println("copyNodeRecursively called with "+entry.getName()+
       //                   ","+target.getName());
       DirectoryEntry newTarget = null;
       if (entry.isDirectoryEntry()) {
           newTarget = target.createDirectory(entry.getName());
           Iterator entries = ((DirectoryEntry)entry).getEntries();

           while (entries.hasNext()) {
              copyNodeRecursively((Entry)entries.next(),newTarget);
           }
       } else {
         DocumentEntry dentry = (DocumentEntry)entry;
         DocumentInputStream dstream = new DocumentInputStream(dentry);
         target.createDocument(dentry.getName(),dstream);
         dstream.close();
       }
   }

    /** Test only. Do not use */
    public void insertChartRecord()
    {
        int loc = workbook.findFirstRecordLocBySid(SSTRecord.sid);
        byte[] data = {
           (byte)0x0F, (byte)0x00, (byte)0x00, (byte)0xF0, (byte)0x52,
           (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
           (byte)0x06, (byte)0xF0, (byte)0x18, (byte)0x00, (byte)0x00,
           (byte)0x00, (byte)0x01, (byte)0x08, (byte)0x00, (byte)0x00,
           (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02,
           (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00,
           (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00,
           (byte)0x00, (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x00,
           (byte)0x33, (byte)0x00, (byte)0x0B, (byte)0xF0, (byte)0x12,
           (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xBF, (byte)0x00,
           (byte)0x08, (byte)0x00, (byte)0x08, (byte)0x00, (byte)0x81,
           (byte)0x01, (byte)0x09, (byte)0x00, (byte)0x00, (byte)0x08,
           (byte)0xC0, (byte)0x01, (byte)0x40, (byte)0x00, (byte)0x00,
           (byte)0x08, (byte)0x40, (byte)0x00, (byte)0x1E, (byte)0xF1,
           (byte)0x10, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D,
           (byte)0x00, (byte)0x00, (byte)0x08, (byte)0x0C, (byte)0x00,
           (byte)0x00, (byte)0x08, (byte)0x17, (byte)0x00, (byte)0x00,
           (byte)0x08, (byte)0xF7, (byte)0x00, (byte)0x00, (byte)0x10,
        };
        UnknownRecord r = new UnknownRecord((short)0x00EB,(short)0x005a, data);
        workbook.getRecords().add(loc, r);
    }



}
