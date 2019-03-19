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

import static org.apache.poi.hssf.model.InternalWorkbook.OLD_WORKBOOK_DIR_ENTRY_NAME;
import static org.apache.poi.hssf.model.InternalWorkbook.WORKBOOK_DIR_ENTRY_NAMES;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.POIDocument;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherBitmapBlip;
import org.apache.poi.ddf.EscherBlipRecord;
import org.apache.poi.ddf.EscherMetafileBlip;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.hpsf.ClassID;
import org.apache.poi.hpsf.ClassIDPredefined;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hssf.model.DrawingManager2;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.model.InternalSheet.UnsupportedBOFType;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.model.WorkbookRecordList;
import org.apache.poi.hssf.record.AbstractEscherHolderRecord;
import org.apache.poi.hssf.record.BackupRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.DrawingGroupRecord;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.record.FilePassRecord;
import org.apache.poi.hssf.record.FontRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.RecalcIdRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordFactory;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.UnknownRecord;
import org.apache.poi.hssf.record.aggregates.RecordAggregate.RecordVisitor;
import org.apache.poi.hssf.record.common.UnicodeString;
import org.apache.poi.hssf.record.crypto.Biff8DecryptingStream;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.poifs.crypt.ChunkedCipherOutputStream;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.EncryptionVerifier;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.EntryUtils;
import org.apache.poi.poifs.filesystem.FilteringDirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.filesystem.Ole10Native;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.SheetNameFormatter;
import org.apache.poi.ss.formula.udf.AggregatingUDFFinder;
import org.apache.poi.ss.formula.udf.IndexedUDFFinder;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Configurator;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.apache.poi.util.LittleEndianByteArrayOutputStream;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * High level representation of a workbook.  This is the first object most users
 * will construct whether they are reading or writing a workbook.  It is also the
 * top level object for creating new sheets/etc.
 *
 * @see org.apache.poi.hssf.model.InternalWorkbook
 * @see org.apache.poi.hssf.usermodel.HSSFSheet
 */
@SuppressWarnings("WeakerAccess")
public final class HSSFWorkbook extends POIDocument implements org.apache.poi.ss.usermodel.Workbook {

    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

    private static final Pattern COMMA_PATTERN = Pattern.compile(",");

    /**
     * The maximum number of cell styles in a .xls workbook.
     * The 'official' limit is 4,000, but POI allows a slightly larger number.
     * This extra delta takes into account built-in styles that are automatically
     *
     * See http://office.microsoft.com/en-us/excel-help/excel-specifications-and-limits-HP005199291.aspx
     */
    private static final int MAX_STYLES = 4030;

    private static final int DEBUG = POILogger.DEBUG;

    /**
     * used for compile-time performance/memory optimization.  This determines the
     * initial capacity for the sheet collection.  Its currently set to 3.
     * Changing it in this release will decrease performance
     * since you're never allowed to have more or less than three sheets!
     */

    public final static int INITIAL_CAPACITY = Configurator.getIntValue("HSSFWorkbook.SheetInitialCapacity",3);

    /**
     * this is the reference to the low level Workbook object
     */

    private InternalWorkbook workbook;

    /**
     * this holds the HSSFSheet objects attached to this workbook
     */

    protected List<HSSFSheet> _sheets;

    /**
     * this holds the HSSFName objects attached to this workbook
     */

    private ArrayList<HSSFName> names;

    /**
     * this holds the HSSFFont objects attached to this workbook.
     * We only create these from the low level records as required.
     */
    private Map<Integer,HSSFFont> fonts;

    /**
     * holds whether or not to preserve other nodes in the POIFS.  Used
     * for macros and embedded objects.
     */
    private boolean preserveNodes;

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
    private MissingCellPolicy missingCellPolicy = MissingCellPolicy.RETURN_NULL_AND_BLANK;

    private static final POILogger log = POILogFactory.getLogger(HSSFWorkbook.class);

    /**
     * The locator of user-defined functions.
     * By default includes functions from the Excel Analysis Toolpack
     */
    private UDFFinder _udfFinder = new IndexedUDFFinder(AggregatingUDFFinder.DEFAULT);

    public static HSSFWorkbook create(InternalWorkbook book) {
    	return new HSSFWorkbook(book);
    }
    /**
     * Creates new HSSFWorkbook from scratch (start here!)
     *
     */
    public HSSFWorkbook() {
        this(InternalWorkbook.createWorkbook());
    }

    private HSSFWorkbook(InternalWorkbook book) {
        super((DirectoryNode)null);
        workbook = book;
        _sheets = new ArrayList<>(INITIAL_CAPACITY);
        names = new ArrayList<>(INITIAL_CAPACITY);
    }

    /**
     * Given a POI POIFSFileSystem object, read in its Workbook along
     *  with all related nodes, and populate the high and low level models.
     * <p>This calls {@link #HSSFWorkbook(POIFSFileSystem, boolean)} with
     *  preserve nodes set to true.
     *
     * @see #HSSFWorkbook(POIFSFileSystem, boolean)
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem
     * @exception IOException if the stream cannot be read
     */
    public HSSFWorkbook(POIFSFileSystem fs) throws IOException {
        this(fs,true);
    }

    /**
     * Given a POI POIFSFileSystem object, read in its Workbook and populate
     * the high and low level models.  If you're reading in a workbook... start here!
     *
     * @param fs the POI filesystem that contains the Workbook stream.
     * @param preserveNodes whether to preserve other nodes, such as
     *        macros.  This takes more memory, so only say yes if you
     *        need to. If set, will store all of the POIFSFileSystem
     *        in memory
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem
     * @exception IOException if the stream cannot be read
     */
    public HSSFWorkbook(POIFSFileSystem fs, boolean preserveNodes)
            throws IOException {
        this(fs.getRoot(), fs, preserveNodes);
    }

    public static String getWorkbookDirEntryName(DirectoryNode directory) {
        for(String wbName : WORKBOOK_DIR_ENTRY_NAMES) {
            if(directory.hasEntry(wbName)) {
                return wbName;
            }
        }

        // check for an encrypted .xlsx file - they get OLE2 wrapped
        if(directory.hasEntry(Decryptor.DEFAULT_POIFS_ENTRY)) {
        	throw new EncryptedDocumentException("The supplied spreadsheet seems to be an Encrypted .xlsx file. " +
        			"It must be decrypted before use by XSSF, it cannot be used by HSSF");
        }

        // check for previous version of file format
        if(directory.hasEntry(OLD_WORKBOOK_DIR_ENTRY_NAME)) {
            throw new OldExcelFormatException("The supplied spreadsheet seems to be Excel 5.0/7.0 (BIFF5) format. "
                    + "POI only supports BIFF8 format (from Excel versions 97/2000/XP/2003)");
        }

        // throw more useful exceptions for known wrong file-extensions
        if(directory.hasEntry("WordDocument")) {
            throw new IllegalArgumentException("The document is really a DOC file");
        }

        throw new IllegalArgumentException("The supplied POIFSFileSystem does not contain a BIFF8 'Workbook' entry. "
            + "Is it really an excel file? Had: " + directory.getEntryNames());
    }

    /**
     * given a POI POIFSFileSystem object, and a specific directory
     *  within it, read in its Workbook and populate the high and
     *  low level models.  If you're reading in a workbook...start here.
     *
     * @param directory the POI filesystem directory to process from
     * @param fs the POI filesystem that contains the Workbook stream.
     * @param preserveNodes whether to preserve other nodes, such as
     *        macros.  This takes more memory, so only say yes if you
     *        need to. If set, will store all of the POIFSFileSystem
     *        in memory
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem
     * @exception IOException if the stream cannot be read
     */
    public HSSFWorkbook(DirectoryNode directory, POIFSFileSystem fs, boolean preserveNodes)
            throws IOException
    {
       this(directory, preserveNodes);
    }

    /**
     * given a POI POIFSFileSystem object, and a specific directory
     *  within it, read in its Workbook and populate the high and
     *  low level models.  If you're reading in a workbook...start here.
     *
     * @param directory the POI filesystem directory to process from
     * @param preserveNodes whether to preserve other nodes, such as
     *        macros.  This takes more memory, so only say yes if you
     *        need to. If set, will store all of the POIFSFileSystem
     *        in memory
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem
     * @exception IOException if the stream cannot be read
     */
    public HSSFWorkbook(DirectoryNode directory, boolean preserveNodes)
            throws IOException
    {
        super(directory);
        String workbookName = getWorkbookDirEntryName(directory);

        this.preserveNodes = preserveNodes;

        // If we're not preserving nodes, don't track the
        //  POIFS any more
        if(! preserveNodes) {
            clearDirectory();
        }

        _sheets = new ArrayList<>(INITIAL_CAPACITY);
        names  = new ArrayList<>(INITIAL_CAPACITY);

        // Grab the data from the workbook stream, however
        //  it happens to be spelled.
        InputStream stream = directory.createDocumentInputStream(workbookName);

        List<Record> records = RecordFactory.createRecords(stream);

        workbook = InternalWorkbook.createWorkbook(records);
        setPropertiesFromWorkbook(workbook);
        int recOffset = workbook.getNumRecords();

        // convert all LabelRecord records to LabelSSTRecord
        convertLabelRecords(records, recOffset);
        RecordStream rs = new RecordStream(records, recOffset);
        while (rs.hasNext()) {
            try {
                InternalSheet sheet = InternalSheet.createSheet(rs);
                _sheets.add(new HSSFSheet(this, sheet));
            } catch (UnsupportedBOFType eb) {
                // Hopefully there's a supported one after this!
                log.log(POILogger.WARN, "Unsupported BOF found of type " + eb.getType());
            }
        }

        for (int i = 0 ; i < workbook.getNumNames() ; ++i){
            NameRecord nameRecord = workbook.getNameRecord(i);
            HSSFName name = new HSSFName(this, nameRecord, workbook.getNameCommentRecord(nameRecord));
            names.add(name);
        }
    }

    /**
     * Companion to HSSFWorkbook(POIFSFileSystem), this constructs the
     *  POI filesystem around your {@link InputStream}, including all nodes.
     * <p>This calls {@link #HSSFWorkbook(InputStream, boolean)} with
     *  preserve nodes set to true.
     *
     * @see #HSSFWorkbook(InputStream, boolean)
     * @see #HSSFWorkbook(POIFSFileSystem)
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem
     * @exception IOException if the stream cannot be read
     */
    public HSSFWorkbook(InputStream s) throws IOException {
        this(s,true);
    }

    /**
     * Companion to HSSFWorkbook(POIFSFileSystem), this constructs the
     * POI filesystem around your {@link InputStream}.
     *
     * @param s  the POI filesystem that contains the Workbook stream.
     * @param preserveNodes whether to preserve other nodes, such as
     *        macros.  This takes more memory, so only say yes if you
     *        need to.
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem
     * @see #HSSFWorkbook(POIFSFileSystem)
     * @exception IOException if the stream cannot be read
     */
    @SuppressWarnings("resource")   // POIFSFileSystem always closes the stream
    public HSSFWorkbook(InputStream s, boolean preserveNodes)
            throws IOException
    {
        this(new POIFSFileSystem(s).getRoot(), preserveNodes);
    }

    /**
     * used internally to set the workbook properties.
     */

    private void setPropertiesFromWorkbook(InternalWorkbook book)
    {
        this.workbook = book;

        // none currently
    }

    /**
      * This is basically a kludge to deal with the now obsolete Label records.  If
      * you have to read in a sheet that contains Label records, be aware that the rest
      * of the API doesn't deal with them, the low level structure only provides read-only
      * semi-immutable structures (the sets are there for interface conformance with NO
      * Implementation).  In short, you need to call this function passing it a reference
      * to the Workbook object.  All labels will be converted to LabelSST records and their
      * contained strings will be written to the Shared String table (SSTRecord) within
      * the Workbook.
      *
      * @param records a collection of sheet's records.
      * @param offset the offset to search at
      * @see org.apache.poi.hssf.record.LabelRecord
      * @see org.apache.poi.hssf.record.LabelSSTRecord
      * @see org.apache.poi.hssf.record.SSTRecord
      */

     private void convertLabelRecords(List<Record> records, int offset)
     {
         if (log.check( POILogger.DEBUG )) {
            log.log(POILogger.DEBUG, "convertLabelRecords called");
        }
         for (int k = offset; k < records.size(); k++)
         {
             Record rec = records.get(k);

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
         if (log.check( POILogger.DEBUG )) {
            log.log(POILogger.DEBUG, "convertLabelRecords exit");
        }
     }

    /**
     * Retrieves the current policy on what to do when
     *  getting missing or blank cells from a row.
     * The default is to return blank and null cells.
     *  {@link MissingCellPolicy}
     */
    @Override
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
    @Override
    public void setMissingCellPolicy(MissingCellPolicy missingCellPolicy) {
        this.missingCellPolicy = missingCellPolicy;
    }

    /**
     * sets the order of appearance for a given sheet.
     *
     * @param sheetname the name of the sheet to reorder
     * @param pos the position that we want to insert the sheet into (0 based)
     */

    @Override
    public void setSheetOrder(String sheetname, int pos ) {
        int oldSheetIndex = getSheetIndex(sheetname);
        _sheets.add(pos,_sheets.remove(oldSheetIndex));
        workbook.setSheetOrder(sheetname, pos);

        FormulaShifter shifter = FormulaShifter.createForSheetShift(oldSheetIndex, pos);
        for (HSSFSheet sheet : _sheets) {
            sheet.getSheet().updateFormulasAfterCellShift(shifter, /* not used */ -1 );
        }

        workbook.updateNamesAfterCellShift(shifter);
        updateNamedRangesAfterSheetReorder(oldSheetIndex, pos);
        
        updateActiveSheetAfterSheetReorder(oldSheetIndex, pos);
    }
    
    /**
     * copy-pasted from XSSFWorkbook#updateNamedRangesAfterSheetReorder(int, int)
     * 
     * update sheet-scoped named ranges in this workbook after changing the sheet order
     * of a sheet at oldIndex to newIndex.
     * Sheets between these indices will move left or right by 1.
     *
     * @param oldIndex the original index of the re-ordered sheet
     * @param newIndex the new index of the re-ordered sheet
     */
    private void updateNamedRangesAfterSheetReorder(int oldIndex, int newIndex) {
        // update sheet index of sheet-scoped named ranges
        for (final HSSFName name : names) {
            final int i = name.getSheetIndex();
            // name has sheet-level scope
            if (i != -1) {
                // name refers to this sheet
                if (i == oldIndex) {
                    name.setSheetIndex(newIndex);
                }
                // if oldIndex > newIndex then this sheet moved left and sheets between newIndex and oldIndex moved right
                else if (newIndex <= i && i < oldIndex) {
                    name.setSheetIndex(i+1);
                }
                // if oldIndex < newIndex then this sheet moved right and sheets between oldIndex and newIndex moved left
                else if (oldIndex < i && i <= newIndex) {
                    name.setSheetIndex(i-1);
                }
            }
        }
    }

    
    private void updateActiveSheetAfterSheetReorder(int oldIndex, int newIndex) {
        // adjust active sheet if necessary
        int active = getActiveSheetIndex();
        if(active == oldIndex) {
            // moved sheet was the active one
            setActiveSheet(newIndex);
        } else if ((active < oldIndex && active < newIndex) ||
                (active > oldIndex && active > newIndex)) {
            // not affected
        } else if (newIndex > oldIndex) {
            // moved sheet was below before and is above now => active is one less
            setActiveSheet(active-1);
        } else {
            // remaining case: moved sheet was higher than active before and is lower now => active is one more
            setActiveSheet(active+1);
        }
    }

    private void validateSheetIndex(int index) {
        int lastSheetIx = _sheets.size() - 1;
        if (index < 0 || index > lastSheetIx) {
            String range = "(0.." +    lastSheetIx + ")";
            if (lastSheetIx == -1) {
                range = "(no sheets)";
            }
            throw new IllegalArgumentException("Sheet index ("
                    + index +") is out of range " + range);
        }
    }

    /**
     * Selects a single sheet. This may be different to
     * the 'active' sheet (which is the sheet with focus).
     */
    @Override
    public void setSelectedTab(int index) {

        validateSheetIndex(index);
        int nSheets = _sheets.size();
        for (int i=0; i<nSheets; i++) {
               getSheetAt(i).setSelected(i == index);
        }
        workbook.getWindowOne().setNumSelectedTabs((short)1);
    }

    /**
     * Selects multiple sheets as a group. This is distinct from
     * the 'active' sheet (which is the sheet with focus).
     * Unselects sheets that are not in <code>indexes</code>.
     *
     * @param indexes Array of sheets to select, the index is 0-based.
     */
    public void setSelectedTabs(int[] indexes) {
        Collection<Integer> list = new ArrayList<>(indexes.length);
        for (int index : indexes) {
            list.add(index);
        }
        setSelectedTabs(list);
    }
    
    /**
     * Selects multiple sheets as a group. This is distinct from
     * the 'active' sheet (which is the sheet with focus).
     * Unselects sheets that are not in <code>indexes</code>.
     *
     * @param indexes Collection of sheets to select, the index is 0-based.
     */
    public void setSelectedTabs(Collection<Integer> indexes) {

        for (int index : indexes) {
            validateSheetIndex(index);
        }
        // ignore duplicates
        Set<Integer> set = new HashSet<>(indexes);
        int nSheets = _sheets.size();
        for (int i=0; i<nSheets; i++) {
            boolean bSelect = set.contains(i);
            getSheetAt(i).setSelected(bSelect);
        }
        // this is true only if all values in set were valid sheet indexes (between 0 and nSheets-1, inclusive)
        short nSelected = (short) set.size();
        workbook.getWindowOne().setNumSelectedTabs(nSelected);
    }
    
    /**
     * Gets the selected sheets (if more than one, Excel calls these a [Group]). 
     *
     * @return indices of selected sheets
     */
    public Collection<Integer> getSelectedTabs() {
        Collection<Integer> indexes = new ArrayList<>();
        int nSheets = _sheets.size();
        for (int i=0; i<nSheets; i++) {
            HSSFSheet sheet = getSheetAt(i);
            if (sheet.isSelected()) {
                indexes.add(i);
            }
        }
        return Collections.unmodifiableCollection(indexes);
    }
    
    /**
     * Convenience method to set the active sheet.  The active sheet is is the sheet
     * which is currently displayed when the workbook is viewed in Excel.
     * 'Selected' sheet(s) is a distinct concept.
     */
    @Override
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
    @Override
    public int getActiveSheetIndex() {
        return workbook.getWindowOne().getActiveSheetIndex();
    }


    /**
     * sets the first tab that is displayed in the list of tabs
     * in excel. This method does <b>not</b> hide, select or focus sheets.
     * It just sets the scroll position in the tab-bar.
     *
     * @param index the sheet index of the tab that will become the first in the tab-bar
     */
    @Override
    public void setFirstVisibleTab(int index) {
        workbook.getWindowOne().setFirstVisibleTab(index);
    }

    /**
     * sets the first tab that is displayed in the list of tabs in excel.
     */
    @Override
    public int getFirstVisibleTab() {
        return workbook.getWindowOne().getFirstVisibleTab();
    }

    /**
     * Set the sheet name.
     *
     * @param sheetIx number (0 based)
     * @throws IllegalArgumentException if the name is null or invalid
     *  or workbook already contains a sheet with this name
     * @see #createSheet(String)
     * @see org.apache.poi.ss.util.WorkbookUtil#createSafeSheetName(String nameProposal)
     */
    @Override
    public void setSheetName(int sheetIx, String name) {
        if (name == null) {
            throw new IllegalArgumentException("sheetName must not be null");
        }

        if (workbook.doesContainsSheetName(name, sheetIx)) {
            throw new IllegalArgumentException("The workbook already contains a sheet named '" + name + "'");
        }
        validateSheetIndex(sheetIx);
        workbook.setSheetName(sheetIx, name);
    }

    /**
     * @return Sheet name for the specified index
     */
    @Override
    public String getSheetName(int sheetIndex) {
        validateSheetIndex(sheetIndex);
        return workbook.getSheetName(sheetIndex);
    }

    @Override
    public boolean isHidden() {
        return workbook.getWindowOne().getHidden();
    }

    @Override
    public void setHidden(boolean hiddenFlag) {
        workbook.getWindowOne().setHidden(hiddenFlag);
    }

    @Override
    public boolean isSheetHidden(int sheetIx) {
        validateSheetIndex(sheetIx);
        return workbook.isSheetHidden(sheetIx);
    }

    @Override
    public boolean isSheetVeryHidden(int sheetIx) {
        validateSheetIndex(sheetIx);
        return workbook.isSheetVeryHidden(sheetIx);
    }
    
    @Override
    public SheetVisibility getSheetVisibility(int sheetIx) {
        return workbook.getSheetVisibility(sheetIx);
    }

    @Override
    public void setSheetHidden(int sheetIx, boolean hidden) {
        setSheetVisibility(sheetIx, hidden ? SheetVisibility.HIDDEN : SheetVisibility.VISIBLE);
    }

    @Override
    public void setSheetVisibility(int sheetIx, SheetVisibility visibility) {
        validateSheetIndex(sheetIx);
        workbook.setSheetHidden(sheetIx, visibility);
    }

    /** Returns the index of the sheet by his name
     * @param name the sheet name
     * @return index of the sheet (0 based)
     */
    @Override
    public int getSheetIndex(String name){
        return workbook.getSheetIndex(name);
    }

    /** Returns the index of the given sheet
     * @param sheet the sheet to look up
     * @return index of the sheet (0 based). <tt>-1</tt> if not found
     */
    @Override
    public int getSheetIndex(org.apache.poi.ss.usermodel.Sheet sheet) {
        return _sheets.indexOf(sheet);
    }

    /**
     * create an HSSFSheet for this HSSFWorkbook, adds it to the sheets and returns
     * the high level representation.  Use this to create new sheets.
     *
     * @return HSSFSheet representing the new sheet.
     */

    @Override
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

    @Override
    public HSSFSheet cloneSheet(int sheetIndex) {
        validateSheetIndex(sheetIndex);
        HSSFSheet srcSheet = _sheets.get(sheetIndex);
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
        if (filterDbNameIndex != -1) {
            NameRecord newNameRecord = workbook.cloneFilter(filterDbNameIndex, newSheetIndex);
            HSSFName newName = new HSSFName(this, newNameRecord);
            names.add(newName);
        }
        // TODO - maybe same logic required for other/all built-in name records
//        workbook.cloneDrawings(clonedSheet.getSheet());

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
     * Create a new sheet for this Workbook and return the high level representation.
     * Use this to create new sheets.
     *
     * <p>
     *     Note that Excel allows sheet names up to 31 chars in length but other applications
     *     (such as OpenOffice) allow more. Some versions of Excel crash with names longer than 31 chars,
     *     others - truncate such names to 31 character.
     * </p>
     * <p>
     *     POI's SpreadsheetAPI silently truncates the input argument to 31 characters.
     *     Example:
     *
     *     <pre><code>
     *     Sheet sheet = workbook.createSheet("My very long sheet name which is longer than 31 chars"); // will be truncated
     *     assert 31 == sheet.getSheetName().length();
     *     assert "My very long sheet name which i" == sheet.getSheetName();
     *     </code></pre>
     * </p>
     *
     * Except the 31-character constraint, Excel applies some other rules:
     * <p>
     * Sheet name MUST be unique in the workbook and MUST NOT contain the any of the following characters:
     * <ul>
     * <li> 0x0000 </li>
     * <li> 0x0003 </li>
     * <li> colon (:) </li>
     * <li> backslash (\) </li>
     * <li> asterisk (*) </li>
     * <li> question mark (?) </li>
     * <li> forward slash (/) </li>
     * <li> opening square bracket ([) </li>
     * <li> closing square bracket (]) </li>
     * </ul>
     * The string MUST NOT begin or end with the single quote (') character.
     * </p>
     *
     * @param sheetname  sheetname to set for the sheet.
     * @return Sheet representing the new sheet.
     * @throws IllegalArgumentException if the name is null or invalid
     *  or workbook already contains a sheet with this name
     * @see org.apache.poi.ss.util.WorkbookUtil#createSafeSheetName(String nameProposal)
     */
    @Override
    public HSSFSheet createSheet(String sheetname)
    {
        if (sheetname == null) {
            throw new IllegalArgumentException("sheetName must not be null");
        }

        if (workbook.doesContainsSheetName( sheetname, _sheets.size() )) {
            throw new IllegalArgumentException("The workbook already contains a sheet named '" + sheetname + "'");
        }

        HSSFSheet sheet = new HSSFSheet(this);

        workbook.setSheetName(_sheets.size(), sheetname);
        _sheets.add(sheet);
        boolean isOnlySheet = _sheets.size() == 1;
        sheet.setSelected(isOnlySheet);
        sheet.setActive(isOnlySheet);
        return sheet;
    }

    /**
     *  Returns an iterator of the sheets in the workbook
     *  in sheet order. Includes hidden and very hidden sheets.
     *
     * @return an iterator of the sheets.
     */
    @Override
    public Iterator<Sheet> sheetIterator() {
        return new SheetIterator<>();
    }

    /**
     * Alias for {@link #sheetIterator()} to allow
     * foreach loops
     */
    @Override
    public Iterator<Sheet> iterator() {
        return sheetIterator();
    }
    
    private final class SheetIterator<T extends Sheet> implements Iterator<T> {
        final private Iterator<T> it;
        private T cursor;
        @SuppressWarnings("unchecked")
        public SheetIterator() {
            it = (Iterator<T>) _sheets.iterator();
        }
        @Override
        public boolean hasNext() {
            return it.hasNext();
        }
        @Override
        public T next() throws NoSuchElementException {
            cursor = it.next();
            return cursor;
        }
        /**
         * Unexpected behavior may occur if sheets are reordered after iterator
         * has been created. Support for the remove method may be added in the future
         * if someone can figure out a reliable implementation.
         */
        @Override
        public void remove() throws IllegalStateException {
            throw new UnsupportedOperationException("remove method not supported on HSSFWorkbook.iterator(). "+
                        "Use Sheet.removeSheetAt(int) instead.");
        }
    }

    /**
     * get the number of spreadsheets in the workbook (this will be three after serialization)
     * @return number of sheets
     */
    @Override
    public int getNumberOfSheets()
    {
        return _sheets.size();
    }

    private HSSFSheet[] getSheets() {
        HSSFSheet[] result = new HSSFSheet[_sheets.size()];
        _sheets.toArray(result);
        return result;
    }

    /**
     * Get the HSSFSheet object at the given index.
     * @param index of the sheet number (0-based physical &amp; logical)
     * @return HSSFSheet at the provided index
     * @throws IllegalArgumentException if the index is out of range (index
     *            &lt; 0 || index &gt;= getNumberOfSheets()).
     */
    @Override
    public HSSFSheet getSheetAt(int index)
    {
        validateSheetIndex(index);
        return _sheets.get(index);
    }

    /**
     * Get sheet with the given name (case insensitive match)
     * @param name of the sheet
     * @return HSSFSheet with the name provided or <code>null</code> if it does not exist
     */

    @Override
    public HSSFSheet getSheet(String name)
    {
        HSSFSheet retval = null;

        for (int k = 0; k < _sheets.size(); k++)
        {
            String sheetname = workbook.getSheetName(k);

            if (sheetname.equalsIgnoreCase(name))
            {
                retval = _sheets.get(k);
            }
        }
        return retval;
    }

    /**
     * Removes sheet at the given index.<p>
     *
     * Care must be taken if the removed sheet is the currently active or only selected sheet in
     * the workbook. There are a few situations when Excel must have a selection and/or active
     * sheet. (For example when printing - see Bug 40414).<br>
     *
     * This method makes sure that if the removed sheet was active, another sheet will become
     * active in its place.  Furthermore, if the removed sheet was the only selected sheet, another
     * sheet will become selected.  The newly active/selected sheet will have the same index, or
     * one less if the removed sheet was the last in the workbook.
     *
     * @param index of the sheet  (0-based)
     */
    @Override
    public void removeSheetAt(int index) {
        validateSheetIndex(index);
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

        // adjust active sheet
        int active = getActiveSheetIndex();
        if(active == index) {
            // removed sheet was the active one, reset active sheet if there is still one left now
            setActiveSheet(newSheetIndex);
        } else if (active > index) {
            // removed sheet was below the active one => active is one less now
            setActiveSheet(active-1);
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

        return backupRecord.getBackup() != 0;
    }

    int findExistingBuiltinNameRecordIdx(int sheetIndex, byte builtinCode) {
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


    HSSFName createBuiltInName(byte builtinCode, int sheetIndex) {
      NameRecord nameRecord =
        workbook.createBuiltInName(builtinCode, sheetIndex + 1);
      HSSFName newName = new HSSFName(this, nameRecord, null);
      names.add(newName);
      return newName;
    }


    HSSFName getBuiltInName(byte builtinCode, int sheetIndex) {
      int index = findExistingBuiltinNameRecordIdx(sheetIndex, builtinCode);
      return (index < 0) ? null : names.get(index);
    }


    /**
     * create a new Font and add it to the workbook's font table
     * @return new font object
     */

    @Override
    public HSSFFont createFont()
    {
        /*FontRecord font =*/ workbook.createNewFont();
        int fontindex = getNumberOfFontsAsInt() - 1;

        if (fontindex > 3)
        {
            fontindex++;   // THERE IS NO FOUR!!
        }
        if(fontindex >= Short.MAX_VALUE){
            throw new IllegalArgumentException("Maximum number of fonts was exceeded");
        }

        // Ask getFontAt() to build it for us,
        //  so it gets properly cached
        return getFontAt(fontindex);
    }

    /**
     * Finds a font that matches the one with the supplied attributes
     */
    @Override
    public HSSFFont findFont(boolean bold, short color, short fontHeight,
                             String name, boolean italic, boolean strikeout,
                             short typeOffset, byte underline)
    {
        int numberOfFonts = getNumberOfFontsAsInt();
        for (int i = 0; i <= numberOfFonts; i++) {
            // Remember - there is no 4!
            if(i == 4) {
                continue;
            }

            HSSFFont hssfFont = getFontAt(i);
            if (hssfFont.getBold() == bold
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

    @Override
    @Deprecated
    public short getNumberOfFonts() {
        return (short)getNumberOfFontsAsInt();
    }

    @Override
    public int getNumberOfFontsAsInt() {
        return workbook.getNumberOfFontRecords();
    }

    @Override
    @Deprecated
    public HSSFFont getFontAt(short idx) {
        return getFontAt((int)idx);
    }

    @Override
    public HSSFFont getFontAt(int idx) {
        if(fonts == null) {
            fonts = new HashMap<>();
        }

        // So we don't confuse users, give them back
        //  the same object every time, but create
        //  them lazily
        Integer sIdx = idx;
        if(fonts.containsKey(sIdx)) {
            return fonts.get(sIdx);
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
    void resetFontCache() {
        fonts = new HashMap<>();
    }

    /**
     * Create a new Cell style and add it to the workbook's style table.
     * You can define up to 4000 unique styles in a .xls workbook.
     *
     * @return the new Cell Style object
     * @throws IllegalStateException if the number of cell styles exceeded the limit for this type of Workbook.
     */
    @Override
    public HSSFCellStyle createCellStyle()
    {
        if(workbook.getNumExFormats() == MAX_STYLES) {
            throw new IllegalStateException("The maximum number of cell styles was exceeded. " +
                    "You can define up to 4000 styles in a .xls workbook");
        }
        ExtendedFormatRecord xfr = workbook.createCellXF();
        short index = (short) (getNumCellStyles() - 1);
        return new HSSFCellStyle(index, xfr, this);
    }

    /**
     * get the number of styles the workbook contains
     * @return count of cell styles
     */
    @Override
    public int getNumCellStyles()
    {
        return workbook.getNumExFormats();
    }

    /**
     * get the cell style object at the given index
     * @param idx  index within the set of styles
     * @return HSSFCellStyle object at the index
     */
    @Override
    public HSSFCellStyle getCellStyleAt(int idx)
    {
        ExtendedFormatRecord xfr = workbook.getExFormatAt(idx);
        return new HSSFCellStyle((short)idx, xfr, this);
    }

    /**
     * Closes the underlying {@link POIFSFileSystem} from which
     *  the Workbook was read, if any.
     *
     * <p>Once this has been called, no further
     *  operations, updates or reads should be performed on the 
     *  Workbook.
     */
    @Override
    public void close() throws IOException {
        super.close();
    }

    /**
     * Write out this workbook to the currently open {@link File} via the
     *  writeable {@link POIFSFileSystem} it was opened as. 
     *  
     * <p>This will fail (with an {@link IllegalStateException} if the
     *  Workbook was opened read-only, opened from an {@link InputStream}
     *   instead of a File, or if this is not the root document. For those cases, 
     *   you must use {@link #write(OutputStream)} or {@link #write(File)} to 
     *   write to a brand new document.
     */
    @Override
    public void write() throws IOException {
        validateInPlaceWritePossible();
        final DirectoryNode dir = getDirectory();
        
        // Update the Workbook stream in the file
        DocumentNode workbookNode = (DocumentNode)dir.getEntry(
                getWorkbookDirEntryName(dir));
        POIFSDocument workbookDoc = new POIFSDocument(workbookNode);
        workbookDoc.replaceContents(new ByteArrayInputStream(getBytes()));
        
        // Update the properties streams in the file
        writeProperties();
        
        // Sync with the File on disk
        dir.getFileSystem().writeFilesystem();
    }
    
    /**
     * Method write - write out this workbook to a new {@link File}. Constructs
     * a new POI POIFSFileSystem, passes in the workbook binary representation and
     * writes it out. If the file exists, it will be replaced, otherwise a new one
     * will be created.
     * 
     * Note that you cannot write to the currently open File using this method.
     * If you opened your Workbook from a File, you <i>must</i> use the {@link #write()}
     * method instead!
     * 
     * @param newFile The new File you wish to write the XLS to
     *
     * @exception IOException if anything can't be written.
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem
     */
    @Override
    public void write(File newFile) throws IOException {
        try (POIFSFileSystem fs = POIFSFileSystem.create(newFile)) {
            write(fs);
            fs.writeFilesystem();
        }
    }
    
    /**
     * Method write - write out this workbook to an {@link OutputStream}. Constructs
     * a new POI POIFSFileSystem, passes in the workbook binary representation and
     * writes it out.
     * 
     * If {@code stream} is a {@link java.io.FileOutputStream} on a networked drive
     * or has a high cost/latency associated with each written byte,
     * consider wrapping the OutputStream in a {@link java.io.BufferedOutputStream}
     * to improve write performance.
     *
     * @param stream - the java OutputStream you wish to write the XLS to
     *
     * @exception IOException if anything can't be written.
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem
     */
    @Override
	public void write(OutputStream stream) throws IOException {
        try (POIFSFileSystem fs = new POIFSFileSystem()) {
            write(fs);
            fs.writeFilesystem(stream);
        }
    }
    
    /** Writes the workbook out to a brand new, empty POIFS */
    private void write(POIFSFileSystem fs) throws IOException {
        // For tracking what we've written out, used if we're
        //  going to be preserving nodes
        List<String> excepts = new ArrayList<>(1);

        // Write out the Workbook stream
        fs.createDocument(new ByteArrayInputStream(getBytes()), "Workbook");

        // Write out our HPFS properties, if we have them
        writeProperties(fs, excepts);
        
        if (preserveNodes) {
            // Don't write out the old Workbook, we'll be doing our new one
            // If the file had an "incorrect" name for the workbook stream,
            // don't write the old one as we'll use the correct name shortly
            excepts.addAll(Arrays.asList(WORKBOOK_DIR_ENTRY_NAMES));

            // summary information has been already written via writeProperties and might go in a
            // different stream, if the file is cryptoapi encrypted
            excepts.addAll(Arrays.asList(
                DocumentSummaryInformation.DEFAULT_STREAM_NAME,
                SummaryInformation.DEFAULT_STREAM_NAME,
                getEncryptedPropertyStreamName()
            ));

            // Copy over all the other nodes to our new poifs
            EntryUtils.copyNodes(
                    new FilteringDirectoryNode(getDirectory(), excepts)
                    , new FilteringDirectoryNode(fs.getRoot(), excepts)
                    );

            // YK: preserve StorageClsid, it is important for embedded workbooks,
            // see Bugzilla 47920
            fs.getRoot().setStorageClsid(getDirectory().getStorageClsid());
        }
    }

    /**
     * Totals the sizes of all sheet records and eventually serializes them
     */
    private static final class SheetRecordCollector implements RecordVisitor {

        private List<Record> _list;
        private int _totalSize;

        public SheetRecordCollector() {
            _totalSize = 0;
            _list = new ArrayList<>(128);
        }
        public int getTotalSize() {
            return _totalSize;
        }
        @Override
        public void visitRecord(Record r) {
            _list.add(r);
            _totalSize+=r.getRecordSize();

        }
        public int serialize(int offset, byte[] data) {
            int result = 0;
            for (Record rec : _list) {
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
     */
    public byte[] getBytes() {
        if (log.check( POILogger.DEBUG )) {
            log.log(DEBUG, "HSSFWorkbook.getBytes()");
        }
        
        HSSFSheet[] sheets = getSheets();
        int nSheets = sheets.length;

        updateEncryptionInfo();

        // before getting the workbook size we must tell the sheets that
        // serialization is about to occur.
        workbook.preSerialize();
        for (HSSFSheet sheet : sheets) {
            sheet.getSheet().preSerialize();
            sheet.preSerialize();
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

        encryptBytes(retval);
        
        return retval;
    }

    @SuppressWarnings("resource")
    void encryptBytes(byte[] buf) {
        EncryptionInfo ei = getEncryptionInfo();
        if (ei == null) {
            return;
        }
        Encryptor enc = ei.getEncryptor();
        int initialOffset = 0;
        LittleEndianByteArrayInputStream plain = new LittleEndianByteArrayInputStream(buf, 0); // NOSONAR
        LittleEndianByteArrayOutputStream leos = new LittleEndianByteArrayOutputStream(buf, 0); // NOSONAR
        enc.setChunkSize(Biff8DecryptingStream.RC4_REKEYING_INTERVAL);
        byte[] tmp = new byte[1024];
        try {
            ChunkedCipherOutputStream os = enc.getDataStream(leos, initialOffset);
            int totalBytes = 0;
            while (totalBytes < buf.length) {
                IOUtils.readFully(plain, tmp, 0, 4);
                final int sid = LittleEndian.getUShort(tmp, 0);
                final int len = LittleEndian.getUShort(tmp, 2);
                boolean isPlain = Biff8DecryptingStream.isNeverEncryptedRecord(sid);
                os.setNextRecordSize(len, isPlain);
                os.writePlain(tmp, 0, 4);
                if (sid == BoundSheetRecord.sid) {
                    // special case for the field_1_position_of_BOF (=lbPlyPos) field of
                    // the BoundSheet8 record which must be unencrypted
                    byte[] bsrBuf = IOUtils.safelyAllocate(len, MAX_RECORD_LENGTH);
                    plain.readFully(bsrBuf);
                    os.writePlain(bsrBuf, 0, 4);
                    os.write(bsrBuf, 4, len-4);
                } else {
                    int todo = len;
                    while (todo > 0) {
                        int nextLen = Math.min(todo, tmp.length);
                        plain.readFully(tmp, 0, nextLen);
                        if (isPlain) {
                            os.writePlain(tmp, 0, nextLen);
                        } else {
                            os.write(tmp, 0, nextLen);
                        }
                        todo -= nextLen;
                    }
                }
                totalBytes += 4 + len;
            }
            os.close();
        } catch (Exception e) {
            throw new EncryptedDocumentException(e);
        }
    }
    
    /*package*/ InternalWorkbook getWorkbook() {
        return workbook;
    }

    @Override
    public int getNumberOfNames(){
        return names.size();
    }

    @Override
    public HSSFName getName(String name) {
        int nameIndex = getNameIndex(name);
        if (nameIndex < 0) {
            return null;
        }
        return names.get(nameIndex);
    }

    @Override
    public List<HSSFName> getNames(String name) {
        List<HSSFName> nameList = new ArrayList<>();
        for(HSSFName nr : names) {
            if(nr.getNameName().equals(name)) {
                nameList.add(nr);
            }
        }

        return Collections.unmodifiableList(nameList);
    }

    @Override
    public HSSFName getNameAt(int nameIndex) {
        int nNames = names.size();
        if (nNames < 1) {
            throw new IllegalStateException("There are no defined names in this workbook");
        }
        if (nameIndex < 0 || nameIndex > nNames) {
            throw new IllegalArgumentException("Specified name index " + nameIndex
                    + " is outside the allowable range (0.." + (nNames-1) + ").");
        }
        return names.get(nameIndex);
    }

    @Override
    public List<HSSFName> getAllNames() {
        return Collections.unmodifiableList(names);
    }

    public NameRecord getNameRecord(int nameIndex) {
        return getWorkbook().getNameRecord(nameIndex);
    }

    /** gets the named range name
     * @param index the named range index (0 based)
     * @return named range name
     */
    public String getNameName(int index){
        return getNameAt(index).getNameName();
    }

    /**
     * Sets the printarea for the sheet provided
     * <p>
     * i.e. Reference = $A$1:$B$2
     * @param sheetIndex Zero-based sheet index (0 Represents the first sheet to keep consistent with java)
     * @param reference Valid name Reference for the Print Area
     */
    @Override
    public void setPrintArea(int sheetIndex, String reference)
    {
        NameRecord name = workbook.getSpecificBuiltinRecord(NameRecord.BUILTIN_PRINT_AREA, sheetIndex+1);


        if (name == null) {
            name = workbook.createBuiltInName(NameRecord.BUILTIN_PRINT_AREA, sheetIndex+1);
            // adding one here because 0 indicates a global named region; doesn't make sense for print areas
        }
        String[] parts = COMMA_PATTERN.split(reference);
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < parts.length; i++) {
            if(i>0) {
                sb.append(",");
            }
            SheetNameFormatter.appendFormat(sb, getSheetName(sheetIndex));
            sb.append("!");
            sb.append(parts[i]);
        }
        name.setNameDefinition(HSSFFormulaParser.parse(sb.toString(), this, FormulaType.NAMEDRANGE, sheetIndex));
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
    @Override
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
    @Override
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
    @Override
    public void removePrintArea(int sheetIndex) {
        getWorkbook().removeBuiltinRecord(NameRecord.BUILTIN_PRINT_AREA, sheetIndex+1);
    }

    /** creates a new named range and add it to the model
     * @return named range high level
     */
    @Override
    public HSSFName createName(){
        NameRecord nameRecord = workbook.createName();

        HSSFName newName = new HSSFName(this, nameRecord);

        names.add(newName);

        return newName;
    }

    @Override
    public int getNameIndex(String name) {

        for (int k = 0; k < names.size(); k++) {
            String nameName = getNameName(k);

            if (nameName.equalsIgnoreCase(name)) {
                return k;
            }
        }
        return -1;
    }


    /**
     * As {@link #getNameIndex(String)} is not necessarily unique
     * (name + sheet index is unique), this method is more accurate.
     *
     * @param name the name whose index in the list of names of this workbook
     *        should be looked up.
     * @return an index value >= 0 if the name was found; -1, if the name was
     *         not found
     */
    int getNameIndex(HSSFName name) {
      for (int k = 0; k < names.size(); k++) {
        if (name == names.get(k)) {
            return k;
        }
      }
      return -1;
    }


    @Override
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
    @Override
    public HSSFDataFormat createDataFormat() {
    if (formatter == null) {
        formatter = new HSSFDataFormat(workbook);
    }
    return formatter;
    }


    @Override
    public void removeName(String name) {
        int index = getNameIndex(name);
        removeName(index);
    }


    /**
     * As {@link #removeName(String)} is not necessarily unique
     * (name + sheet index is unique), this method is more accurate.
     *
     * @param name the name to remove.
     */
    @Override
    public void removeName(Name name) {
      int index = getNameIndex((HSSFName) name);
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
    public void dumpDrawingGroupRecords(boolean fat) {
        DrawingGroupRecord r = (DrawingGroupRecord) workbook.findFirstRecordBySid( DrawingGroupRecord.sid );
        if (r == null) {
            return;
        }
        r.decode();
        List<EscherRecord> escherRecords = r.getEscherRecords();
        PrintWriter w = new PrintWriter(new OutputStreamWriter(System.out, Charset.defaultCharset()));
        for (EscherRecord escherRecord : escherRecords) {
            if (fat) {
                System.out.println(escherRecord);
            } else {
                escherRecord.display(w, 0);
            }
        }
        w.flush();
    }

    void initDrawings(){
        DrawingManager2 mgr = workbook.findDrawingGroup();
        if(mgr != null) {
            for(HSSFSheet sh : _sheets)  {
                sh.getDrawingPatriarch();
            }
        } else {
            workbook.createDrawingGroup();
        }
    }

    /**
     * Adds a picture to the workbook.
     *
     * @param pictureData       The bytes of the picture
     * @param format            The format of the picture.  One of <code>PICTURE_TYPE_*</code>
     *
     * @return the index to this picture (1 based).
     * @see #PICTURE_TYPE_WMF
     * @see #PICTURE_TYPE_EMF
     * @see #PICTURE_TYPE_PICT
     * @see #PICTURE_TYPE_PNG
     * @see #PICTURE_TYPE_JPEG
     * @see #PICTURE_TYPE_DIB
     */
    @SuppressWarnings("fallthrough")
    @Override
    public int addPicture(byte[] pictureData, int format)
    {
        initDrawings();

        byte[] uid = DigestUtils.md5(pictureData);
        EscherBlipRecord blipRecord;
        int blipSize;
        short escherTag;
        switch (format) {
            case PICTURE_TYPE_WMF:
                // remove first 22 bytes if file starts with magic bytes D7-CD-C6-9A
                // see also http://de.wikipedia.org/wiki/Windows_Metafile#Hinweise_zur_WMF-Spezifikation
                if (LittleEndian.getInt(pictureData) == 0x9AC6CDD7) {
                    byte[] picDataNoHeader = new byte[pictureData.length - 22];
                    System.arraycopy(pictureData, 22, picDataNoHeader, 0, pictureData.length-22);
                    pictureData = picDataNoHeader;
                }
                // fall through
            case PICTURE_TYPE_EMF:
                EscherMetafileBlip blipRecordMeta = new EscherMetafileBlip();
                blipRecord = blipRecordMeta;
                blipRecordMeta.setUID(uid);
                blipRecordMeta.setPictureData(pictureData);
                // taken from libre office export, it won't open, if this is left to 0
                blipRecordMeta.setFilter((byte)-2);
                blipSize = blipRecordMeta.getCompressedSize() + 58;
                escherTag = 0;
                break;
            default:
                EscherBitmapBlip blipRecordBitmap = new EscherBitmapBlip();
                blipRecord = blipRecordBitmap;
                blipRecordBitmap.setUID( uid );
                blipRecordBitmap.setMarker( (byte) 0xFF );
                blipRecordBitmap.setPictureData( pictureData );
                blipSize = pictureData.length + 25;
                escherTag = (short) 0xFF;
    	        break;
        }

        blipRecord.setRecordId((short) (EscherBlipRecord.RECORD_ID_START + format));
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
            case PICTURE_TYPE_JPEG:
                blipRecord.setOptions(HSSFPictureData.MSOBI_JPEG);
                break;
            case PICTURE_TYPE_DIB:
                blipRecord.setOptions(HSSFPictureData.MSOBI_DIB);
                break;
            default:
                throw new IllegalStateException("Unexpected picture format: " + format);
        }

        EscherBSERecord r = new EscherBSERecord();
        r.setRecordId( EscherBSERecord.RECORD_ID );
        r.setOptions( (short) ( 0x0002 | ( format << 4 ) ) );
        r.setBlipTypeMacOS( (byte) format );
        r.setBlipTypeWin32( (byte) format );
        r.setUid( uid );
        r.setTag( escherTag );
        r.setSize( blipSize );
        r.setRef( 0 );
        r.setOffset( 0 );
        r.setBlipRecord( blipRecord );

        return workbook.addBSERecord( r );
    }

    /**
     * Gets all pictures from the Workbook.
     *
     * @return the list of pictures (a list of {@link HSSFPictureData} objects.)
     */
    @Override
    public List<HSSFPictureData> getAllPictures()
    {
        // The drawing group record always exists at the top level, so we won't need to do this recursively.
        List<HSSFPictureData> pictures = new ArrayList<>();
        for (Record r : workbook.getRecords()) {
            if (r instanceof AbstractEscherHolderRecord) {
                ((AbstractEscherHolderRecord) r).decode();
                List<EscherRecord> escherRecords = ((AbstractEscherHolderRecord) r).getEscherRecords();
                searchForPictures(escherRecords, pictures);
            }
        }
        return Collections.unmodifiableList(pictures);
    }

    /**
     * Performs a recursive search for pictures in the given list of escher records.
     *
     * @param escherRecords the escher records.
     * @param pictures the list to populate with the pictures.
     */
    private void searchForPictures(List<EscherRecord> escherRecords, List<HSSFPictureData> pictures)
    {
        for(EscherRecord escherRecord : escherRecords) {

            if (escherRecord instanceof EscherBSERecord)
            {
                EscherBlipRecord blip = ((EscherBSERecord) escherRecord).getBlipRecord();
                if (blip != null)
                {
                    // TODO: Some kind of structure.
                    HSSFPictureData picture = new HSSFPictureData(blip);
					pictures.add(picture);
                }


            }

            // Recursive call.
            searchForPictures(escherRecord.getChildRecords(), pictures);
        }

    }

    static Map<String,ClassID> getOleMap() {
    	Map<String,ClassID> olemap = new HashMap<>();
    	olemap.put("PowerPoint Document", ClassIDPredefined.POWERPOINT_V8.getClassID());
    	for (String str : WORKBOOK_DIR_ENTRY_NAMES) {
    		olemap.put(str, ClassIDPredefined.EXCEL_V7_WORKBOOK.getClassID());
    	}
    	// ... to be continued
    	return olemap;
    }

    /**
     * Adds an OLE package manager object with the given POIFS to the sheet
     *
     * @param poiData an POIFS containing the embedded document, to be added
     * @param label the label of the payload
     * @param fileName the original filename
     * @param command the command to open the payload
     * @return the index of the added ole object
     * @throws IOException if the object can't be embedded
     */
    public int addOlePackage(POIFSFileSystem poiData, String label, String fileName, String command)
    throws IOException {
    	DirectoryNode root = poiData.getRoot();
    	Map<String,ClassID> olemap = getOleMap();
    	for (Map.Entry<String,ClassID> entry : olemap.entrySet()) {
    		if (root.hasEntry(entry.getKey())) {
    			root.setStorageClsid(entry.getValue());
    			break;
    		}
    	}

    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	poiData.writeFilesystem(bos);
        return addOlePackage(bos.toByteArray(), label, fileName, command);
    }

    @Override
    public int addOlePackage(byte[] oleData, String label, String fileName, String command)
    throws IOException {
    	// check if we were created by POIFS otherwise create a new dummy POIFS for storing the package data
    	if (initDirectory()) {
    		preserveNodes = true;
    	}

        // get free MBD-Node
        int storageId = 0;
        DirectoryEntry oleDir = null;
        do {
            String storageStr = "MBD"+ HexDump.toHex(++storageId);
            if (!getDirectory().hasEntry(storageStr)) {
                oleDir = getDirectory().createDirectory(storageStr);
                oleDir.setStorageClsid(ClassIDPredefined.OLE_V1_PACKAGE.getClassID());
            }
        } while (oleDir == null);

        Ole10Native.createOleMarkerEntry(oleDir);
        
        Ole10Native oleNative = new Ole10Native(label, fileName, command, oleData);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        oleNative.writeOut(bos);
        oleDir.createDocument(Ole10Native.OLE10_NATIVE, new ByteArrayInputStream(bos.toByteArray()));

    	return storageId;
    }

    /**
     * Adds the LinkTable records required to allow formulas referencing
     *  the specified external workbook to be added to this one. Allows
     *  formulas such as "[MyOtherWorkbook]Sheet3!$A$5" to be added to the
     *  file, for workbooks not already referenced.
     *
     * @param name The name the workbook will be referenced as in formulas
     * @param workbook The open workbook to fetch the link required information from
     */
    @Override
    public int linkExternalWorkbook(String name, Workbook workbook) {
        return this.workbook.linkExternalWorkbook(name, workbook);
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
        List<HSSFObjectData> objects = new ArrayList<>();
        for (HSSFSheet sheet : _sheets)
        {
            getAllEmbeddedObjects(sheet, objects);
        }
        return Collections.unmodifiableList(objects);
    }

    /**
     * Gets all embedded OLE2 objects from the Workbook.
     *
     * @param sheet embedded object attached to
     * @param objects the list of embedded objects to populate.
     */
    private void getAllEmbeddedObjects(HSSFSheet sheet, List<HSSFObjectData> objects)
    {
        HSSFPatriarch patriarch = sheet.getDrawingPatriarch();
        if (null == patriarch){
            return;
        }
        getAllEmbeddedObjects(patriarch, objects);
    }
    /**
     * Recursively iterates a shape container to get all embedded objects.
     *
     * @param parent the parent.
     * @param objects the list of embedded objects to populate.
     */
    private void getAllEmbeddedObjects(HSSFShapeContainer parent, List<HSSFObjectData> objects)
    {
        for (HSSFShape shape : parent.getChildren()) {
            if (shape instanceof HSSFObjectData) {
                objects.add((HSSFObjectData) shape);
            } else if (shape instanceof HSSFShapeContainer) {
                getAllEmbeddedObjects((HSSFShapeContainer) shape, objects);
            }
        }
    }
    @Override
    public HSSFCreationHelper getCreationHelper() {
        return new HSSFCreationHelper(this);
    }

    /**
     *
     * Returns the locator of user-defined functions.
     * The default instance extends the built-in functions with the Analysis Tool Pack
     *
     * @return the locator of user-defined functions
     */
    /*package*/ UDFFinder getUDFFinder(){
        return _udfFinder;
    }

    /**
     * Register a new toolpack in this workbook.
     *
     * @param toopack the toolpack to register
     */
    @Override
    public void addToolPack(UDFFinder toopack){
        AggregatingUDFFinder udfs = (AggregatingUDFFinder)_udfFinder;
        udfs.add(toopack);
    }

    /**
     * Whether the application shall perform a full recalculation when the workbook is opened.
     * <p>
     * Typically you want to force formula recalculation when you modify cell formulas or values
     * of a workbook previously created by Excel. When set to true, this flag will tell Excel
     * that it needs to recalculate all formulas in the workbook the next time the file is opened.
     * </p>
     * <p>
     * Note, that recalculation updates cached formula results and, thus, modifies the workbook.
     * Depending on the version, Excel may prompt you with "Do you want to save the changes in <em>filename</em>?"
     * on close.
     * </p>
     *
     * @param value true if the application will perform a full recalculation of
     * workbook values when the workbook is opened
     * @since 3.8
     */
    @Override
    public void setForceFormulaRecalculation(boolean value){
        InternalWorkbook iwb = getWorkbook();
        RecalcIdRecord recalc = iwb.getRecalcId();
        recalc.setEngineId(0);
    }

    /**
     * Whether Excel will be asked to recalculate all formulas when the  workbook is opened.
     *
     * @since 3.8
     */
    @Override
    public boolean getForceFormulaRecalculation(){
        InternalWorkbook iwb = getWorkbook();
        RecalcIdRecord recalc = (RecalcIdRecord)iwb.findFirstRecordBySid(RecalcIdRecord.sid);
        return recalc != null && recalc.getEngineId() != 0;
    }

	/**
	 * Changes an external referenced file to another file.
	 * A formula in Excel which references a cell in another file is saved in two parts:
	 * The referenced file is stored in an reference table. the row/cell information is saved separate.
	 * This method invocation will only change the reference in the lookup-table itself.
	 * @param oldUrl The old URL to search for and which is to be replaced
	 * @param newUrl The URL replacement
	 * @return true if the oldUrl was found and replaced with newUrl. Otherwise false
	 */
    public boolean changeExternalReference(String oldUrl, String newUrl) {
    	return workbook.changeExternalReference(oldUrl, newUrl);
    }
    
    @Internal
    public InternalWorkbook getInternalWorkbook() {
        return workbook;
    }
    
    /**
     * Returns the spreadsheet version (EXCLE97) of this workbook
     * 
     * @return EXCEL97 SpreadsheetVersion enum
     * @since 3.14 beta 2
     */
    @Override
    public SpreadsheetVersion getSpreadsheetVersion() {
        return SpreadsheetVersion.EXCEL97;
    }

    @Override
    public EncryptionInfo getEncryptionInfo() {
        FilePassRecord fpr = (FilePassRecord)workbook.findFirstRecordBySid(FilePassRecord.sid);
        return (fpr != null) ? fpr.getEncryptionInfo() : null;
    }


    private void updateEncryptionInfo() {
        // make sure, that we've read all the streams ...
        readProperties();
        FilePassRecord fpr = (FilePassRecord)workbook.findFirstRecordBySid(FilePassRecord.sid);

        String password = Biff8EncryptionKey.getCurrentUserPassword();
        WorkbookRecordList wrl = workbook.getWorkbookRecordList();
        if (password == null) {
            if (fpr != null) {
                // need to remove password data
                wrl.remove(fpr);
            }
        } else {
            // create password record
            if (fpr == null) {
                fpr = new FilePassRecord(EncryptionMode.cryptoAPI);
                wrl.add(1, fpr);
            }

            // check if the password has been changed
            EncryptionInfo ei = fpr.getEncryptionInfo();
            EncryptionVerifier ver = ei.getVerifier();
            byte[] encVer = ver.getEncryptedVerifier();
            Decryptor dec = ei.getDecryptor();
            Encryptor enc = ei.getEncryptor();
            try {
                if (encVer == null || !dec.verifyPassword(password)) {
                    enc.confirmPassword(password);
                } else {
                    byte[] verifier = dec.getVerifier();
                    byte[] salt = ver.getSalt();
                    enc.confirmPassword(password, null, null, verifier, salt, null);
                }
            } catch (GeneralSecurityException e) {
                throw new EncryptedDocumentException("can't validate/update encryption setting", e);
            }
        }
    }
}
