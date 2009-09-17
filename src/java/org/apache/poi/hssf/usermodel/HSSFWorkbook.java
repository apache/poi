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

package org.apache.poi.hssf.usermodel;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.POIDocument;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherBitmapBlip;
import org.apache.poi.ddf.EscherBlipRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.AbstractEscherHolderRecord;
import org.apache.poi.hssf.record.BackupRecord;
import org.apache.poi.hssf.record.DrawingGroupRecord;
import org.apache.poi.hssf.record.EmbeddedObjectRefSubRecord;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.record.FontRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordFactory;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.UnicodeString;
import org.apache.poi.hssf.record.UnknownRecord;
import org.apache.poi.hssf.record.aggregates.RecordAggregate.RecordVisitor;
import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.MemFuncPtg;
import org.apache.poi.hssf.record.formula.NameXPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.Ref3DPtg;
import org.apache.poi.hssf.record.formula.SheetNameFormatter;
import org.apache.poi.hssf.record.formula.UnionPtg;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.formula.FormulaType;
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
 */
public class HSSFWorkbook extends POIDocument implements org.apache.poi.ss.usermodel.Workbook {
    private static final Pattern COMMA_PATTERN = Pattern.compile(",");
    private static final int MAX_ROW = 0xFFFF;
    private static final short MAX_COLUMN = (short)0x00FF;

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

    protected List _sheets;

    /**
     * this holds the HSSFName objects attached to this workbook
     */

    private ArrayList names;

    /**
     * this holds the HSSFFont objects attached to this workbook.
     * We only create these from the low level records as required.
     */
    private Hashtable fonts;

    /**
     * holds whether or not to preserve other nodes in the POIFS.  Used
     * for macros and embedded objects.
     */
    private boolean   preserveNodes;

    /**
     * Used to keep track of the data formatter so that all
     * createDataFormatter calls return the same one for a given
     * book.  This ensures that updates from one places is visible
     * someplace else.
     */
    private HSSFDataFormat formatter;

    /**
     * The policy to apply in the event of missing or
     *  blank cells when fetching from a row.
     * See {@link MissingCellPolicy}
     */
    private MissingCellPolicy missingCellPolicy = HSSFRow.RETURN_NULL_AND_BLANK;


    /** Extended windows meta file */
    public static final int PICTURE_TYPE_EMF = 2;
    /** Windows Meta File */
    public static final int PICTURE_TYPE_WMF = 3;
    /** Mac PICT format */
    public static final int PICTURE_TYPE_PICT = 4;
    /** JPEG format */
    public static final int PICTURE_TYPE_JPEG = 5;
    /** PNG format */
    public static final int PICTURE_TYPE_PNG = 6;
    /** Device independant bitmap */
    public static final int PICTURE_TYPE_DIB = 7;


    private static POILogger log = POILogFactory.getLogger(HSSFWorkbook.class);

    /**
     * Creates new HSSFWorkbook from scratch (start here!)
     *
     */
    public HSSFWorkbook()
    {
        this(Workbook.createWorkbook());
    }

    protected HSSFWorkbook( Workbook book )
    {
        super(null, null);
        workbook = book;
        _sheets = new ArrayList( INITIAL_CAPACITY );
        names = new ArrayList( INITIAL_CAPACITY );
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
     *        need to. If set, will store all of the POIFSFileSystem
     *        in memory
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem
     * @exception IOException if the stream cannot be read
     */
    public HSSFWorkbook(POIFSFileSystem fs, boolean preserveNodes)
            throws IOException
    {
        this(fs.getRoot(), fs, preserveNodes);
    }

    /**
     * Normally, the Workbook will be in a POIFS Stream
     * called "Workbook". However, some weird XLS generators use "WORKBOOK"
     */
    private static final String[] WORKBOOK_DIR_ENTRY_NAMES = {
        "Workbook", // as per BIFF8 spec
        "WORKBOOK",
    };


    private static String getWorkbookDirEntryName(DirectoryNode directory) {

        String[] potentialNames = WORKBOOK_DIR_ENTRY_NAMES;
        for (int i = 0; i < potentialNames.length; i++) {
            String wbName = potentialNames[i];
            try {
                directory.getEntry(wbName);
                return wbName;
            } catch (FileNotFoundException e) {
                // continue - to try other options
            }
        }

        // check for previous version of file format
        try {
            directory.getEntry("Book");
            throw new OldExcelFormatException("The supplied spreadsheet seems to be Excel 5.0/7.0 (BIFF5) format. "
                    + "POI only supports BIFF8 format (from Excel versions 97/2000/XP/2003)");
        } catch (FileNotFoundException e) {
            // fall through
        }

        throw new IllegalArgumentException("The supplied POIFSFileSystem does not contain a BIFF8 'Workbook' entry. "
            + "Is it really an excel file?");
    }

    /**
     * given a POI POIFSFileSystem object, and a specific directory
     *  within it, read in its Workbook and populate the high and
     *  low level models.  If you're reading in a workbook...start here.
     *
     * @param directory the POI filesystem directory to process from
     * @param fs the POI filesystem that contains the Workbook stream.
     * @param preserveNodes whether to preseve other nodes, such as
     *        macros.  This takes more memory, so only say yes if you
     *        need to. If set, will store all of the POIFSFileSystem
     *        in memory
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem
     * @exception IOException if the stream cannot be read
     */
    public HSSFWorkbook(DirectoryNode directory, POIFSFileSystem fs, boolean preserveNodes)
            throws IOException
    {
        super(directory, fs);
        String workbookName = getWorkbookDirEntryName(directory);

        this.preserveNodes = preserveNodes;

        // If we're not preserving nodes, don't track the
        //  POIFS any more
        if(! preserveNodes) {
           this.filesystem = null;
           this.directory = null;
        }

        _sheets = new ArrayList(INITIAL_CAPACITY);
        names  = new ArrayList(INITIAL_CAPACITY);

        // Grab the data from the workbook stream, however
        //  it happens to be spelled.
        InputStream stream = directory.createDocumentInputStream(workbookName);

        List records = RecordFactory.createRecords(stream);

        workbook = Workbook.createWorkbook(records);
        setPropertiesFromWorkbook(workbook);
        int recOffset = workbook.getNumRecords();
        int sheetNum = 0;

        // convert all LabelRecord records to LabelSSTRecord
        convertLabelRecords(records, recOffset);
        RecordStream rs = new RecordStream(records, recOffset);
        while (rs.hasNext()) {
            Sheet sheet = Sheet.createSheet(rs);
            _sheets.add(new HSSFSheet(this, sheet));
        }

        for (int i = 0 ; i < workbook.getNumNames() ; ++i){
            HSSFName name = new HSSFName(this, workbook.getNameRecord(i));
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
      * This is basically a kludge to deal with the now obsolete Label records.  If
      * you have to read in a sheet that contains Label records, be aware that the rest
      * of the API doesn't deal with them, the low level structure only provides read-only
      * semi-immutable structures (the sets are there for interface conformance with NO
      * impelmentation).  In short, you need to call this function passing it a reference
      * to the Workbook object.  All labels will be converted to LabelSST records and their
      * contained strings will be written to the Shared String tabel (SSTRecord) within
      * the Workbook.
      *
      * @param wb sheet's matching low level Workbook structure containing the SSTRecord.
      * @see org.apache.poi.hssf.record.LabelRecord
      * @see org.apache.poi.hssf.record.LabelSSTRecord
      * @see org.apache.poi.hssf.record.SSTRecord
      */

     private void convertLabelRecords(List records, int offset)
     {
         if (log.check( POILogger.DEBUG ))
             log.log(POILogger.DEBUG, "convertLabelRecords called");
         for (int k = offset; k < records.size(); k++)
         {
             Record rec = ( Record ) records.get(k);

             if (rec.getSid() == LabelRecord.sid)
             {
                 LabelRecord oldrec = ( LabelRecord ) rec;

                 records.remove(k);
                 LabelSSTRecord newrec   = new LabelSSTRecord();
                 int            stringid =
                     workbook.addSSTString(new UnicodeString(oldrec.getValue()));

                 newrec.setRow(oldrec.getRow());
                 newrec.setColumn(oldrec.getColumn());
                 newrec.setXFIndex(oldrec.getXFIndex());
                 newrec.setSSTIndex(stringid);
                       records.add(k, newrec);
             }
         }
         if (log.check( POILogger.DEBUG ))
             log.log(POILogger.DEBUG, "convertLabelRecords exit");
     }

    /**
     * Retrieves the current policy on what to do when
     *  getting missing or blank cells from a row.
     * The default is to return blank and null cells.
     *  {@link MissingCellPolicy}
     */
    public MissingCellPolicy getMissingCellPolicy() {
        return missingCellPolicy;
    }

    /**
     * Sets the policy on what to do when
     *  getting missing or blank cells from a row.
     * This will then apply to all calls to
     *  {@link HSSFRow#getCell(int)}}. See
     *  {@link MissingCellPolicy}.
     * Note that this has no effect on any
     *  iterators, only on when fetching Cells
     *  by their column index.
     */
    public void setMissingCellPolicy(MissingCellPolicy missingCellPolicy) {
        this.missingCellPolicy = missingCellPolicy;
    }

    /**
     * sets the order of appearance for a given sheet.
     *
     * @param sheetname the name of the sheet to reorder
     * @param pos the position that we want to insert the sheet into (0 based)
     */

    public void setSheetOrder(String sheetname, int pos ) {
        _sheets.add(pos,_sheets.remove(getSheetIndex(sheetname)));
        workbook.setSheetOrder(sheetname, pos);
    }

    private void validateSheetIndex(int index) {
        int lastSheetIx = _sheets.size() - 1;
        if (index < 0 || index > lastSheetIx) {
            throw new IllegalArgumentException("Sheet index ("
                    + index +") is out of range (0.." +    lastSheetIx + ")");
        }
    }

    /**
     * Selects a single sheet. This may be different to
     * the 'active' sheet (which is the sheet with focus).
     */
    public void setSelectedTab(int index) {

        validateSheetIndex(index);
        int nSheets = _sheets.size();
        for (int i=0; i<nSheets; i++) {
               getSheetAt(i).setSelected(i == index);
        }
        workbook.getWindowOne().setNumSelectedTabs((short)1);
    }
    /**
     * deprecated May 2008
     * @deprecated use setSelectedTab(int)
     */
    public void setSelectedTab(short index) {
        setSelectedTab((int)index);
    }
    public void setSelectedTabs(int[] indexes) {

        for (int i = 0; i < indexes.length; i++) {
            validateSheetIndex(indexes[i]);
        }
        int nSheets = _sheets.size();
        for (int i=0; i<nSheets; i++) {
            boolean bSelect = false;
            for (int j = 0; j < indexes.length; j++) {
                if (indexes[j] == i) {
                    bSelect = true;
                    break;
                }

            }
               getSheetAt(i).setSelected(bSelect);
        }
        workbook.getWindowOne().setNumSelectedTabs((short)indexes.length);
    }
    /**
     * Convenience method to set the active sheet.  The active sheet is is the sheet
     * which is currently displayed when the workbook is viewed in Excel.
     * 'Selected' sheet(s) is a distinct concept.
     */
    public void setActiveSheet(int index) {

        validateSheetIndex(index);
        int nSheets = _sheets.size();
        for (int i=0; i<nSheets; i++) {
             getSheetAt(i).setActive(i == index);
        }
        workbook.getWindowOne().setActiveSheetIndex(index);
    }

    /**
     * gets the tab whose data is actually seen when the sheet is opened.
     * This may be different from the "selected sheet" since excel seems to
     * allow you to show the data of one sheet when another is seen "selected"
     * in the tabs (at the bottom).
     * @see org.apache.poi.hssf.usermodel.HSSFSheet#setSelected(boolean)
     */
    public int getActiveSheetIndex() {
        return workbook.getWindowOne().getActiveSheetIndex();
    }
    /**
     * deprecated May 2008
     * @deprecated - Misleading name - use getActiveSheetIndex()
     */
    public short getSelectedTab() {
        return (short) getActiveSheetIndex();
    }


    /**
     * sets the first tab that is displayed in the list of tabs
     * in excel.
     * @param index
     */
    public void setFirstVisibleTab(int index) {
        workbook.getWindowOne().setFirstVisibleTab(index);
    }
    /**
     * deprecated May 2008
     * @deprecated - Misleading name - use setFirstVisibleTab()
     */
    public void setDisplayedTab(short index) {
       setFirstVisibleTab(index);
    }

    /**
     * sets the first tab that is displayed in the list of tabs in excel.
     */
    public int getFirstVisibleTab() {
        return workbook.getWindowOne().getFirstVisibleTab();
    }
    /**
     * deprecated May 2008
     * @deprecated - Misleading name - use getFirstVisibleTab()
     */
    public short getDisplayedTab() {
        return (short) getFirstVisibleTab();
    }

    /**
     * Sets the sheet name.
     * Will throw IllegalArgumentException if the name is duplicated or contains /\?*[]
     * Note - Excel allows sheet names up to 31 chars in length but other applications allow more.
     * Excel does not crash with names longer than 31 chars, but silently truncates such names to
     * 31 chars.  POI enforces uniqueness on the first 31 chars.
     *
     * @param sheetIx number (0 based)
     */
    public void setSheetName(int sheetIx, String name) {
        if (workbook.doesContainsSheetName(name, sheetIx)) {
            throw new IllegalArgumentException("The workbook already contains a sheet with this name");
        }
        validateSheetIndex(sheetIx);
        workbook.setSheetName(sheetIx, name);
    }

    /**
     * @return Sheet name for the specified index
     */
    public String getSheetName(int sheetIndex) {
        validateSheetIndex(sheetIndex);
        return workbook.getSheetName(sheetIndex);
    }

    public boolean isHidden() {
        return workbook.getWindowOne().getHidden();
    }

    public void setHidden(boolean hiddenFlag) {
        workbook.getWindowOne().setHidden(hiddenFlag);
    }

    public boolean isSheetHidden(int sheetIx) {
        validateSheetIndex(sheetIx);
        return workbook.isSheetHidden(sheetIx);
    }

    public boolean isSheetVeryHidden(int sheetIx) {
        validateSheetIndex(sheetIx);
        return workbook.isSheetVeryHidden(sheetIx);
    }


    public void setSheetHidden(int sheetIx, boolean hidden) {
        validateSheetIndex(sheetIx);
        workbook.setSheetHidden(sheetIx, hidden);
    }

    public void setSheetHidden(int sheetIx, int hidden) {
        validateSheetIndex(sheetIx);
        workbook.setSheetHidden(sheetIx, hidden);
    }

    /** Returns the index of the sheet by his name
     * @param name the sheet name
     * @return index of the sheet (0 based)
     */
    public int getSheetIndex(String name){
        return workbook.getSheetIndex(name);
    }

    /** Returns the index of the given sheet
     * @param sheet the sheet to look up
     * @return index of the sheet (0 based). <tt>-1</tt> if not found
     */
    public int getSheetIndex(org.apache.poi.ss.usermodel.Sheet sheet) {
        for(int i=0; i<_sheets.size(); i++) {
            if(_sheets.get(i) == sheet) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the external sheet index of the sheet
     *  with the given internal index, creating one
     *  if needed.
     * Used by some of the more obscure formula and
     *  named range things.
     * @deprecated for POI internal use only (formula parsing).  This method is likely to
     * be removed in future versions of POI.
     */
    public int getExternalSheetIndex(int internalSheetIndex) {
        return workbook.checkExternSheet(internalSheetIndex);
    }
    /**
     * @deprecated for POI internal use only (formula rendering).  This method is likely to
     * be removed in future versions of POI.
     */
    public String findSheetNameFromExternSheet(int externSheetIndex){
        // TODO - don't expose internal ugliness like externSheet indexes to the user model API
        return workbook.findSheetNameFromExternSheet(externSheetIndex);
    }
    /**
     * @deprecated for POI internal use only (formula rendering).  This method is likely to
     * be removed in future versions of POI.
     *
     * @param refIndex Index to REF entry in EXTERNSHEET record in the Link Table
     * @param definedNameIndex zero-based to DEFINEDNAME or EXTERNALNAME record
     * @return the string representation of the defined or external name
     */
    public String resolveNameXText(int refIndex, int definedNameIndex) {
        // TODO - make this less cryptic / move elsewhere
        return workbook.resolveNameXText(refIndex, definedNameIndex);
    }




    /**
     * create an HSSFSheet for this HSSFWorkbook, adds it to the sheets and returns
     * the high level representation.  Use this to create new sheets.
     *
     * @return HSSFSheet representing the new sheet.
     */

    public HSSFSheet createSheet()
    {
        HSSFSheet sheet = new HSSFSheet(this);

        _sheets.add(sheet);
        workbook.setSheetName(_sheets.size() - 1, "Sheet" + (_sheets.size() - 1));
        boolean isOnlySheet = _sheets.size() == 1;
        sheet.setSelected(isOnlySheet);
        sheet.setActive(isOnlySheet);
        return sheet;
    }

    /**
     * create an HSSFSheet from an existing sheet in the HSSFWorkbook.
     *
     * @return HSSFSheet representing the cloned sheet.
     */

    public HSSFSheet cloneSheet(int sheetIndex) {
        validateSheetIndex(sheetIndex);
        HSSFSheet srcSheet = (HSSFSheet) _sheets.get(sheetIndex);
        String srcName = workbook.getSheetName(sheetIndex);
        HSSFSheet clonedSheet = srcSheet.cloneSheet(this);
        clonedSheet.setSelected(false);
        clonedSheet.setActive(false);

        String name = getUniqueSheetName(srcName);
        int newSheetIndex = _sheets.size();
        _sheets.add(clonedSheet);
        workbook.setSheetName(newSheetIndex, name);

        // Check this sheet has an autofilter, (which has a built-in NameRecord at workbook level)
        int filterDbNameIndex = findExistingBuiltinNameRecordIdx(sheetIndex, NameRecord.BUILTIN_FILTER_DB);
        if (filterDbNameIndex >=0) {
            NameRecord origNameRecord = workbook.getNameRecord(filterDbNameIndex);
            // copy original formula but adjust 3D refs to the new external sheet index
            int newExtSheetIx = workbook.checkExternSheet(newSheetIndex);
            Ptg[] ptgs = origNameRecord.getNameDefinition();
            for (int i=0; i< ptgs.length; i++) {
                Ptg ptg = ptgs[i];
                ptg = ptg.copy();

                if (ptg instanceof Area3DPtg) {
                    Area3DPtg a3p = (Area3DPtg) ptg;
                    a3p.setExternSheetIndex(newExtSheetIx);
                } else if (ptg instanceof Ref3DPtg) {
                    Ref3DPtg r3p = (Ref3DPtg) ptg;
                    r3p.setExternSheetIndex(newExtSheetIx);
                }
                ptgs[i] = ptg;
            }
            NameRecord newNameRecord = workbook.createBuiltInName(NameRecord.BUILTIN_FILTER_DB, newSheetIndex+1);
            newNameRecord.setNameDefinition(ptgs);
            newNameRecord.setHidden(true);
            HSSFName newName = new HSSFName(this, newNameRecord);
            names.add(newName);

            workbook.cloneDrawings(clonedSheet.getSheet());
        }
        // TODO - maybe same logic required for other/all built-in name records

        return clonedSheet;
    }

    private String getUniqueSheetName(String srcName) {
        int uniqueIndex = 2;
        String baseName = srcName;
        int bracketPos = srcName.lastIndexOf('(');
        if (bracketPos > 0 && srcName.endsWith(")")) {
            String suffix = srcName.substring(bracketPos + 1, srcName.length() - ")".length());
            try {
                uniqueIndex = Integer.parseInt(suffix.trim());
                uniqueIndex++;
                baseName=srcName.substring(0, bracketPos).trim();
            } catch (NumberFormatException e) {
                // contents of brackets not numeric
            }
        }
        while (true) {
            // Try and find the next sheet name that is unique
            String index = Integer.toString(uniqueIndex++);
            String name;
            if (baseName.length() + index.length() + 2 < 31) {
                name = baseName + " (" + index + ")";
            } else {
                name = baseName.substring(0, 31 - index.length() - 2) + "(" + index + ")";
            }

            //If the sheet name is unique, then set it otherwise move on to the next number.
            if (workbook.getSheetIndex(name) == -1) {
              return name;
            }
        }
    }

    /**
     * create an HSSFSheet for this HSSFWorkbook, adds it to the sheets and
     * returns the high level representation. Use this to create new sheets.
     *
     * @param sheetname the name for the new sheet. Note - certain length limits
     * apply. See {@link #setSheetName(int, String)}.
     *
     * @return HSSFSheet representing the new sheet.
     * @throws IllegalArgumentException
     *             if there is already a sheet present with a case-insensitive
     *             match for the specified name.
     */
    public HSSFSheet createSheet(String sheetname)
    {
        if (workbook.doesContainsSheetName( sheetname, _sheets.size() ))
            throw new IllegalArgumentException( "The workbook already contains a sheet of this name" );

        HSSFSheet sheet = new HSSFSheet(this);

        workbook.setSheetName(_sheets.size(), sheetname);
        _sheets.add(sheet);
        boolean isOnlySheet = _sheets.size() == 1;
        sheet.setSelected(isOnlySheet);
        sheet.setActive(isOnlySheet);
        return sheet;
    }

    /**
     * get the number of spreadsheets in the workbook (this will be three after serialization)
     * @return number of sheets
     */

    public int getNumberOfSheets()
    {
        return _sheets.size();
    }

    public int getSheetIndexFromExternSheetIndex(int externSheetNumber) {
        return workbook.getSheetIndexFromExternSheetIndex(externSheetNumber);
    }

    private HSSFSheet[] getSheets() {
        HSSFSheet[] result = new HSSFSheet[_sheets.size()];
        _sheets.toArray(result);
        return result;
    }

    /**
     * Get the HSSFSheet object at the given index.
     * @param index of the sheet number (0-based physical & logical)
     * @return HSSFSheet at the provided index
     */

    public HSSFSheet getSheetAt(int index)
    {
        validateSheetIndex(index);
        return (HSSFSheet) _sheets.get(index);
    }

    /**
     * Get sheet with the given name (case insensitive match)
     * @param name of the sheet
     * @return HSSFSheet with the name provided or <code>null</code> if it does not exist
     */

    public HSSFSheet getSheet(String name)
    {
        HSSFSheet retval = null;

        for (int k = 0; k < _sheets.size(); k++)
        {
            String sheetname = workbook.getSheetName(k);

            if (sheetname.equalsIgnoreCase(name))
            {
                retval = (HSSFSheet) _sheets.get(k);
            }
        }
        return retval;
    }

    /**
     * Removes sheet at the given index.<p/>
     *
     * Care must be taken if the removed sheet is the currently active or only selected sheet in
     * the workbook. There are a few situations when Excel must have a selection and/or active
     * sheet. (For example when printing - see Bug 40414).<br/>
     *
     * This method makes sure that if the removed sheet was active, another sheet will become
     * active in its place.  Furthermore, if the removed sheet was the only selected sheet, another
     * sheet will become selected.  The newly active/selected sheet will have the same index, or
     * one less if the removed sheet was the last in the workbook.
     *
     * @param index of the sheet  (0-based)
     */
    public void removeSheetAt(int index) {
        validateSheetIndex(index);
        boolean wasActive = getSheetAt(index).isActive();
        boolean wasSelected = getSheetAt(index).isSelected();

        _sheets.remove(index);
        workbook.removeSheet(index);

        // set the remaining active/selected sheet
        int nSheets = _sheets.size();
        if (nSheets < 1) {
            // nothing more to do if there are no sheets left
            return;
        }
        // the index of the closest remaining sheet to the one just deleted
        int newSheetIndex = index;
        if (newSheetIndex >= nSheets) {
            newSheetIndex = nSheets-1;
        }
        if (wasActive) {
            setActiveSheet(newSheetIndex);
        }

        if (wasSelected) {
            boolean someOtherSheetIsStillSelected = false;
            for (int i =0; i < nSheets; i++) {
                if (getSheetAt(i).isSelected()) {
                    someOtherSheetIsStillSelected = true;
                    break;
                }
            }
            if (!someOtherSheetIsStillSelected) {
                setSelectedTab(newSheetIndex);
            }
        }
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
     * 2003:File->PageSetup->Sheet, 2007:Page Layout->Print Titles).
     *   This is function is included in the workbook
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
        if (startColumn < -1 || startColumn >= MAX_COLUMN) throw new IllegalArgumentException("Invalid column range specification");
        if (endColumn < -1 || endColumn >= MAX_COLUMN) throw new IllegalArgumentException("Invalid column range specification");
        if (startRow < -1 || startRow > MAX_ROW) throw new IllegalArgumentException("Invalid row range specification");
        if (endRow < -1 || endRow > MAX_ROW) throw new IllegalArgumentException("Invalid row range specification");
        if (startColumn > endColumn) throw new IllegalArgumentException("Invalid column range specification");
        if (startRow > endRow) throw new IllegalArgumentException("Invalid row range specification");

        HSSFSheet sheet = getSheetAt(sheetIndex);
        short externSheetIndex = getWorkbook().checkExternSheet(sheetIndex);

        boolean settingRowAndColumn =
                startColumn != -1 && endColumn != -1 && startRow != -1 && endRow != -1;
        boolean removingRange =
                startColumn == -1 && endColumn == -1 && startRow == -1 && endRow == -1;

        int rowColHeaderNameIndex = findExistingBuiltinNameRecordIdx(sheetIndex, NameRecord.BUILTIN_PRINT_TITLE);
        if (removingRange) {
            if (rowColHeaderNameIndex >= 0) {
                workbook.removeName(rowColHeaderNameIndex);
            }
            return;
        }
        boolean isNewRecord;
        NameRecord nameRecord;
        if (rowColHeaderNameIndex < 0) {
            //does a lot of the house keeping for builtin records, like setting lengths to zero etc
            nameRecord = workbook.createBuiltInName(NameRecord.BUILTIN_PRINT_TITLE, sheetIndex+1);
            isNewRecord = true;
        } else {
            nameRecord = workbook.getNameRecord(rowColHeaderNameIndex);
            isNewRecord = false;
        }

        List temp = new ArrayList();

        if (settingRowAndColumn) {
            final int exprsSize = 2 * 11 + 1; // 2 * Area3DPtg.SIZE + UnionPtg.SIZE
            temp.add(new MemFuncPtg(exprsSize));
        }
        if (startColumn >= 0) {
            Area3DPtg colArea = new Area3DPtg(0, MAX_ROW, startColumn, endColumn,
                    false, false, false, false, externSheetIndex);
            temp.add(colArea);
        }
        if (startRow >= 0) {
            Area3DPtg rowArea = new Area3DPtg(startRow, endRow, 0, MAX_COLUMN,
                    false, false, false, false, externSheetIndex);
            temp.add(rowArea);
        }
        if (settingRowAndColumn) {
            temp.add(UnionPtg.instance);
        }
        Ptg[] ptgs = new Ptg[temp.size()];
        temp.toArray(ptgs);
        nameRecord.setNameDefinition(ptgs);

        if (isNewRecord)
        {
            HSSFName newName = new HSSFName(this, nameRecord);
            names.add(newName);
        }

        HSSFPrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setValidSettings(false);

        sheet.setActive(true);
    }


    private int findExistingBuiltinNameRecordIdx(int sheetIndex, byte builtinCode) {
        for(int defNameIndex =0; defNameIndex<names.size(); defNameIndex++) {
            NameRecord r = workbook.getNameRecord(defNameIndex);
            if (r == null) {
                throw new RuntimeException("Unable to find all defined names to iterate over");
            }
            if (!r.isBuiltInName() || r.getBuiltInName() != builtinCode) {
                continue;
            }
            if (r.getSheetNumber() -1 == sheetIndex) {
                return defNameIndex;
            }
        }
        return -1;
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
        if(fontindex == Short.MAX_VALUE){
            throw new IllegalArgumentException("Maximum number of fonts was exceeded");
        }

        // Ask getFontAt() to build it for us,
        //  so it gets properly cached
        return getFontAt(fontindex);
    }

    /**
     * Finds a font that matches the one with the supplied attributes
     */
    public HSSFFont findFont(short boldWeight, short color, short fontHeight,
                             String name, boolean italic, boolean strikeout,
                             short typeOffset, byte underline)
    {
        for (short i=0; i<=getNumberOfFonts(); i++) {
            // Remember - there is no 4!
            if(i == 4) continue;

            HSSFFont hssfFont = getFontAt(i);
            if (hssfFont.getBoldweight() == boldWeight
                    && hssfFont.getColor() == color
                    && hssfFont.getFontHeight() == fontHeight
                    && hssfFont.getFontName().equals(name)
                    && hssfFont.getItalic() == italic
                    && hssfFont.getStrikeout() == strikeout
                    && hssfFont.getTypeOffset() == typeOffset
                    && hssfFont.getUnderline() == underline)
            {
                return hssfFont;
            }
        }

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
     * Get the font at the given index number
     * @param idx  index number
     * @return HSSFFont at the index
     */
    public HSSFFont getFontAt(short idx) {
        if(fonts == null) fonts = new Hashtable();

        // So we don't confuse users, give them back
        //  the same object every time, but create
        //  them lazily
        Short sIdx = new Short(idx);
        if(fonts.containsKey(sIdx)) {
            return (HSSFFont)fonts.get(sIdx);
        }

        FontRecord font = workbook.getFontRecordAt(idx);
        HSSFFont retval = new HSSFFont(idx, font);
        fonts.put(sIdx, retval);

        return retval;
    }

    /**
     * Reset the fonts cache, causing all new calls
     *  to getFontAt() to create new objects.
     * Should only be called after deleting fonts,
     *  and that's not something you should normally do
     */
    protected void resetFontCache() {
        fonts = new Hashtable();
    }

    /**
     * create a new Cell style and add it to the workbook's style table
     * @return the new Cell Style object
     */

    public HSSFCellStyle createCellStyle()
    {
        ExtendedFormatRecord xfr = workbook.createCellXF();
        short index = (short) (getNumCellStyles() - 1);
        HSSFCellStyle style = new HSSFCellStyle(index, xfr, this);

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
        HSSFCellStyle style = new HSSFCellStyle(idx, xfr, this);

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

        // For tracking what we've written out, used if we're
        //  going to be preserving nodes
        List excepts = new ArrayList(1);

        // Write out the Workbook stream
        fs.createDocument(new ByteArrayInputStream(bytes), "Workbook");

        // Write out our HPFS properties, if we have them
        writeProperties(fs, excepts);

        if (preserveNodes) {
            // Don't write out the old Workbook, we'll be doing our new one
            excepts.add("Workbook");
            // If the file had WORKBOOK instead of Workbook, we'll write it
            //  out correctly shortly, so don't include the old one
            excepts.add("WORKBOOK");

            // Copy over all the other nodes to our new poifs
            copyNodes(this.filesystem,fs,excepts);
        }
        fs.writeFilesystem(stream);
        //poifs.writeFilesystem(stream);
    }

    /**
     * Totals the sizes of all sheet records and eventually serializes them
     */
    private static final class SheetRecordCollector implements RecordVisitor {

        private List _list;
        private int _totalSize;

        public SheetRecordCollector() {
            _totalSize = 0;
            _list = new ArrayList(128);
        }
        public int getTotalSize() {
            return _totalSize;
        }
        public void visitRecord(Record r) {
            _list.add(r);
            _totalSize+=r.getRecordSize();
        }
        public int serialize(int offset, byte[] data) {
            int result = 0;
            int nRecs = _list.size();
            for(int i=0; i<nRecs; i++) {
                Record rec = (Record)_list.get(i);
                result += rec.serialize(offset + result, data);
            }
            return result;
        }
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
    public byte[] getBytes() {
        if (log.check( POILogger.DEBUG )) {
            log.log(DEBUG, "HSSFWorkbook.getBytes()");
        }

        HSSFSheet[] sheets = getSheets();
        int nSheets = sheets.length;

        // before getting the workbook size we must tell the sheets that
        // serialization is about to occur.
        for (int i = 0; i < nSheets; i++) {
            sheets[i].getSheet().preSerialize();
        }

        int totalsize = workbook.getSize();

        // pre-calculate all the sheet sizes and set BOF indexes
        SheetRecordCollector[] srCollectors = new SheetRecordCollector[nSheets];
        for (int k = 0; k < nSheets; k++) {
            workbook.setSheetBof(k, totalsize);
            SheetRecordCollector src = new SheetRecordCollector();
            sheets[k].getSheet().visitContainedRecords(src, totalsize);
            totalsize += src.getTotalSize();
            srCollectors[k] = src;
        }

        byte[] retval = new byte[totalsize];
        int pos = workbook.serialize(0, retval);

        for (int k = 0; k < nSheets; k++) {
            SheetRecordCollector src = srCollectors[k];
            int serializedSize = src.serialize(pos, retval);
            if (serializedSize != src.getTotalSize()) {
                // Wrong offset values have been passed in the call to setSheetBof() above.
                // For books with more than one sheet, this discrepancy would cause excel
                // to report errors and loose data while reading the workbook
                throw new IllegalStateException("Actual serialized sheet size (" + serializedSize
                        + ") differs from pre-calculated size (" + src.getTotalSize()
                        + ") for sheet (" + k + ")");
                // TODO - add similar sanity check to ensure that Sheet.serializeIndexRecord() does not write mis-aligned offsets either
            }
            pos += serializedSize;
        }
        return retval;
    }

    /** @deprecated Do not call this method from your applications. Use the methods
     *  available in the HSSFRow to add string HSSFCells
     */
    public int addSSTString(String string)
    {
        return workbook.addSSTString(new UnicodeString(string));
    }

    /** @deprecated Do not call this method from your applications. Use the methods
     *  available in the HSSFRow to get string HSSFCells
     */
    public String getSSTString(int index)
    {
        return workbook.getSSTString(index).getString();
    }

    protected Workbook getWorkbook()
    {
        return workbook;
    }

    public int getNumberOfNames(){
        int result = names.size();
        return result;
    }

    public HSSFName getName(String name) {
        int nameIndex = getNameIndex(name);
        if (nameIndex < 0) {
            return null;
        }
        return (HSSFName) names.get(nameIndex);
    }

    public HSSFName getNameAt(int nameIndex) {
        int nNames = names.size();
        if (nNames < 1) {
            throw new IllegalStateException("There are no defined names in this workbook");
        }
        if (nameIndex < 0 || nameIndex > nNames) {
            throw new IllegalArgumentException("Specified name index " + nameIndex
                    + " is outside the allowable range (0.." + (nNames-1) + ").");
        }
        return (HSSFName) names.get(nameIndex);
    }

    public NameRecord getNameRecord(int nameIndex) {
        return getWorkbook().getNameRecord(nameIndex);
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


        if (name == null) {
            name = workbook.createBuiltInName(NameRecord.BUILTIN_PRINT_AREA, sheetIndex+1);
            // adding one here because 0 indicates a global named region; doesn't make sense for print areas
        }
        String[] parts = COMMA_PATTERN.split(reference);
        StringBuffer sb = new StringBuffer(32);
        for (int i = 0; i < parts.length; i++) {
            if(i>0) {
                sb.append(",");
            }
            SheetNameFormatter.appendFormat(sb, getSheetName(sheetIndex));
            sb.append("!");
            sb.append(parts[i]);
        }
        name.setNameDefinition(HSSFFormulaParser.parse(sb.toString(), this, FormulaType.CELL, sheetIndex));
    }

    /**
     * For the Convenience of Java Programmers maintaining pointers.
     * @see #setPrintArea(int, String)
     * @param sheetIndex Zero-based sheet index (0 = First Sheet)
     * @param startColumn Column to begin printarea
     * @param endColumn Column to end the printarea
     * @param startRow Row to begin the printarea
     * @param endRow Row to end the printarea
     */
    public void setPrintArea(int sheetIndex, int startColumn, int endColumn,
                              int startRow, int endRow) {

        //using absolute references because they don't get copied and pasted anyway
        CellReference cell = new CellReference(startRow, startColumn, true, true);
        String reference = cell.formatAsString();

        cell = new CellReference(endRow, endColumn, true, true);
        reference = reference+":"+cell.formatAsString();

        setPrintArea(sheetIndex, reference);
    }


    /**
     * Retrieves the reference for the printarea of the specified sheet, the sheet name is appended to the reference even if it was not specified.
     * @param sheetIndex Zero-based sheet index (0 Represents the first sheet to keep consistent with java)
     * @return String Null if no print area has been defined
     */
    public String getPrintArea(int sheetIndex) {
        NameRecord name = workbook.getSpecificBuiltinRecord(NameRecord.BUILTIN_PRINT_AREA, sheetIndex+1);
        //adding one here because 0 indicates a global named region; doesn't make sense for print areas
        if (name == null) {
            return null;
        }

        return HSSFFormulaParser.toFormulaString(this, name.getNameDefinition());
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

        HSSFName newName = new HSSFName(this, nameRecord);

        names.add(newName);

        return newName;
    }

    public int getNameIndex(String name) {

        for (int k = 0; k < names.size(); k++) {
            String nameName = getNameName(k);

            if (nameName.equalsIgnoreCase(name)) {
                return k;
            }
        }
        return -1;
    }


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


    public void removeName(String name) {
        int index = getNameIndex(name);

        removeName(index);
    }

    public HSSFPalette getCustomPalette()
    {
        return new HSSFPalette(workbook.getCustomPalette());
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
        UnknownRecord r = new UnknownRecord((short)0x00EB, data);
        workbook.getRecords().add(loc, r);
    }

    /**
     * Spits out a list of all the drawing records in the workbook.
     */
    public void dumpDrawingGroupRecords(boolean fat)
    {
        DrawingGroupRecord r = (DrawingGroupRecord) workbook.findFirstRecordBySid( DrawingGroupRecord.sid );
        r.decode();
        List escherRecords = r.getEscherRecords();
        PrintWriter w = new PrintWriter(System.out);
        for ( Iterator iterator = escherRecords.iterator(); iterator.hasNext(); )
        {
            EscherRecord escherRecord = (EscherRecord) iterator.next();
            if (fat)
                System.out.println(escherRecord.toString());
            else
                escherRecord.display(w, 0);
        }
        w.flush();
    }

    /**
     * Adds a picture to the workbook.
     *
     * @param pictureData       The bytes of the picture
     * @param format            The format of the picture.  One of <code>PICTURE_TYPE_*</code>
     *
     * @return the index to this picture (1 based).
     */
    public int addPicture(byte[] pictureData, int format)
    {
        byte[] uid = newUID();
        EscherBitmapBlip blipRecord = new EscherBitmapBlip();
        blipRecord.setRecordId( (short) ( EscherBitmapBlip.RECORD_ID_START + format ) );
        switch (format)
        {
            case PICTURE_TYPE_EMF:
                blipRecord.setOptions(HSSFPictureData.MSOBI_EMF);
                break;
            case PICTURE_TYPE_WMF:
                blipRecord.setOptions(HSSFPictureData.MSOBI_WMF);
                break;
            case PICTURE_TYPE_PICT:
                blipRecord.setOptions(HSSFPictureData.MSOBI_PICT);
                break;
            case PICTURE_TYPE_PNG:
                blipRecord.setOptions(HSSFPictureData.MSOBI_PNG);
                break;
            case HSSFWorkbook.PICTURE_TYPE_JPEG:
                blipRecord.setOptions(HSSFPictureData.MSOBI_JPEG);
                break;
            case HSSFWorkbook.PICTURE_TYPE_DIB:
                blipRecord.setOptions(HSSFPictureData.MSOBI_DIB);
                break;
        }

        blipRecord.setUID( uid );
        blipRecord.setMarker( (byte) 0xFF );
        blipRecord.setPictureData( pictureData );

        EscherBSERecord r = new EscherBSERecord();
        r.setRecordId( EscherBSERecord.RECORD_ID );
        r.setOptions( (short) ( 0x0002 | ( format << 4 ) ) );
        r.setBlipTypeMacOS( (byte) format );
        r.setBlipTypeWin32( (byte) format );
        r.setUid( uid );
        r.setTag( (short) 0xFF );
        r.setSize( pictureData.length + 25 );
        r.setRef( 1 );
        r.setOffset( 0 );
        r.setBlipRecord( blipRecord );

        return workbook.addBSERecord( r );
    }

    /**
     * Gets all pictures from the Workbook.
     *
     * @return the list of pictures (a list of {@link HSSFPictureData} objects.)
     */
    public List<HSSFPictureData> getAllPictures()
    {
        // The drawing group record always exists at the top level, so we won't need to do this recursively.
        List<HSSFPictureData> pictures = new ArrayList<HSSFPictureData>();
        Iterator recordIter = workbook.getRecords().iterator();
        while (recordIter.hasNext())
        {
            Object obj = recordIter.next();
            if (obj instanceof AbstractEscherHolderRecord)
            {
                ((AbstractEscherHolderRecord) obj).decode();
                List escherRecords = ((AbstractEscherHolderRecord) obj).getEscherRecords();
                searchForPictures(escherRecords, pictures);
            }
        }
        return pictures;
    }

    /**
     * Performs a recursive search for pictures in the given list of escher records.
     *
     * @param escherRecords the escher records.
     * @param pictures the list to populate with the pictures.
     */
    private void searchForPictures(List escherRecords, List<HSSFPictureData> pictures)
    {
        Iterator recordIter = escherRecords.iterator();
        while (recordIter.hasNext())
        {
            Object obj = recordIter.next();
            if (obj instanceof EscherRecord)
            {
                EscherRecord escherRecord = (EscherRecord) obj;

                if (escherRecord instanceof EscherBSERecord)
                {
                    EscherBlipRecord blip = ((EscherBSERecord) escherRecord).getBlipRecord();
                    if (blip != null)
                    {
                        // TODO: Some kind of structure.
                        pictures.add(new HSSFPictureData(blip));
                    }
                }

                // Recursive call.
                searchForPictures(escherRecord.getChildRecords(), pictures);
            }
        }
    }

    /**
     * Is the workbook protected with a password (not encrypted)?
     */
    public boolean isWriteProtected() {
        return this.workbook.isWriteProtected();
    }

    /**
     * protect a workbook with a password (not encypted, just sets writeprotect
     * flags and the password.
     * @param password to set
     */
    public void writeProtectWorkbook( String password, String username ) {
       this.workbook.writeProtectWorkbook(password, username);
    }

    /**
     * removes the write protect flag
     */
    public void unwriteProtectWorkbook() {
       this.workbook.unwriteProtectWorkbook();
    }

    /**
     * Gets all embedded OLE2 objects from the Workbook.
     *
     * @return the list of embedded objects (a list of {@link HSSFObjectData} objects.)
     */
    public List<HSSFObjectData> getAllEmbeddedObjects()
    {
        List<HSSFObjectData> objects = new ArrayList<HSSFObjectData>();
        for (int i = 0; i < getNumberOfSheets(); i++)
        {
            getAllEmbeddedObjects(getSheetAt(i).getSheet().getRecords(), objects);
        }
        return objects;
    }

    /**
     * Gets all embedded OLE2 objects from the Workbook.
     *
     * @param records the list of records to search.
     * @param objects the list of embedded objects to populate.
     */
    private void getAllEmbeddedObjects(List records, List<HSSFObjectData> objects)
    {
        Iterator recordIter = records.iterator();
        while (recordIter.hasNext())
        {
            Object obj = recordIter.next();
            if (obj instanceof ObjRecord)
            {
                // TODO: More convenient way of determining if there is stored binary.
                // TODO: Link to the data stored in the other stream.
                Iterator subRecordIter = ((ObjRecord) obj).getSubRecords().iterator();
                while (subRecordIter.hasNext())
                {
                    Object sub = subRecordIter.next();
                    if (sub instanceof EmbeddedObjectRefSubRecord)
                    {
                        objects.add(new HSSFObjectData((ObjRecord) obj, filesystem));
                    }
                }
            }
        }
    }

    public CreationHelper getCreationHelper() {
        return new HSSFCreationHelper(this);
    }

    private static byte[] newUID() {
        return new byte[16];
    }

    /**
     * Note - This method should only used by POI internally.
     * It may get deleted or change definition in future POI versions
     */
    public NameXPtg getNameXPtg(String name) {
        return workbook.getNameXPtg(name);
    }
}
