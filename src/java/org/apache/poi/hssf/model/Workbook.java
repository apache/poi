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

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherBoolProperty;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherDgRecord;
import org.apache.poi.ddf.EscherDggRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.ddf.EscherRGBProperty;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.ddf.EscherSplitMenuColorsRecord;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BackupRecord;
import org.apache.poi.hssf.record.BookBoolRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.CodepageRecord;
import org.apache.poi.hssf.record.CountryRecord;
import org.apache.poi.hssf.record.DSFRecord;
import org.apache.poi.hssf.record.DateWindow1904Record;
import org.apache.poi.hssf.record.DrawingGroupRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.record.ExtSSTRecord;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.record.ExternSheetRecord;
import org.apache.poi.hssf.record.FileSharingRecord;
import org.apache.poi.hssf.record.FnGroupCountRecord;
import org.apache.poi.hssf.record.FontRecord;
import org.apache.poi.hssf.record.FormatRecord;
import org.apache.poi.hssf.record.HideObjRecord;
import org.apache.poi.hssf.record.HyperlinkRecord;
import org.apache.poi.hssf.record.InterfaceEndRecord;
import org.apache.poi.hssf.record.InterfaceHdrRecord;
import org.apache.poi.hssf.record.MMSRecord;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.PaletteRecord;
import org.apache.poi.hssf.record.PasswordRecord;
import org.apache.poi.hssf.record.PasswordRev4Record;
import org.apache.poi.hssf.record.PrecisionRecord;
import org.apache.poi.hssf.record.ProtectRecord;
import org.apache.poi.hssf.record.ProtectionRev4Record;
import org.apache.poi.hssf.record.RecalcIdRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RefreshAllRecord;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.StyleRecord;
import org.apache.poi.hssf.record.SupBookRecord;
import org.apache.poi.hssf.record.TabIdRecord;
import org.apache.poi.hssf.record.UnicodeString;
import org.apache.poi.hssf.record.UseSelFSRecord;
import org.apache.poi.hssf.record.WindowOneRecord;
import org.apache.poi.hssf.record.WindowProtectRecord;
import org.apache.poi.hssf.record.WriteAccessRecord;
import org.apache.poi.hssf.record.WriteProtectRecord;
import org.apache.poi.hssf.record.formula.NameXPtg;
import org.apache.poi.hssf.record.formula.FormulaShifter;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.formula.EvaluationWorkbook.ExternalSheet;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Low level model implementation of a Workbook.  Provides creational methods
 * for settings and objects contained in the workbook object.
 * <P>
 * This file contains the low level binary records starting at the workbook's BOF and
 * ending with the workbook's EOF.  Use HSSFWorkbook for a high level representation.
 * <P>
 * The structures of the highlevel API use references to this to perform most of their
 * operations.  Its probably unwise to use these low level structures directly unless you
 * really know what you're doing.  I recommend you read the Microsoft Excel 97 Developer's
 * Kit (Microsoft Press) and the documentation at http://sc.openoffice.org/excelfileformat.pdf
 * before even attempting to use this.
 *
 *
 * @author  Luc Girardin (luc dot girardin at macrofocus dot com)
 * @author  Sergei Kozello (sergeikozello at mail.ru)
 * @author  Shawn Laubach (slaubach at apache dot org) (Data Formats)
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Brian Sanders (bsanders at risklabs dot com) - custom palette
 * @author  Dan Sherman (dsherman at isisph.com)
 * @author  Glen Stampoultzis (glens at apache.org)
 * @see org.apache.poi.hssf.usermodel.HSSFWorkbook
 */
public final class Workbook implements Model {
    /**
     * Excel silently truncates long sheet names to 31 chars.
     * This constant is used to ensure uniqueness in the first 31 chars
     */
    private static final int MAX_SENSITIVE_SHEET_NAME_LEN = 31;


    private static final POILogger log = POILogFactory.getLogger(Workbook.class);
    private static final int DEBUG = POILogger.DEBUG;

    /**
     * constant used to set the "codepage" wherever "codepage" is set in records
     * (which is duplicated in more than one record)
     */
    private final static short CODEPAGE = 0x04B0;

    /**
     * this contains the Worksheet record objects
     */
    private final WorkbookRecordList records     = new WorkbookRecordList();

    /**
     * this contains a reference to the SSTRecord so that new stings can be added
     * to it.
     */
    protected SSTRecord        sst         = null;


    private LinkTable linkTable; // optionally occurs if there are  references in the document. (4.10.3)

    /**
     * holds the "boundsheet" records (aka bundlesheet) so that they can have their
     * reference to their "BOF" marker
     */
    private final List<BoundSheetRecord> boundsheets = new ArrayList<BoundSheetRecord>();
    private final List<FormatRecord> formats = new ArrayList<FormatRecord>();
    private final List<HyperlinkRecord> hyperlinks = new ArrayList<HyperlinkRecord>();

    protected int              numxfs      = 0;   // hold the number of extended format records
    protected int              numfonts    = 0;   // hold the number of font records
    private int                maxformatid  = -1;  // holds the max format id
    private boolean            uses1904datewindowing  = false;  // whether 1904 date windowing is being used
    private DrawingManager2    drawingManager;
    private List<EscherBSERecord> escherBSERecords = new ArrayList<EscherBSERecord>();
    private WindowOneRecord windowOne;
    private FileSharingRecord fileShare;
    private WriteAccessRecord writeAccess;
    private WriteProtectRecord writeProtect;

    /**
     * Creates new Workbook with no intitialization --useless right now
     * @see #createWorkbook(List)
     */
    public Workbook() {
    }

    /**
     * read support  for low level
     * API.  Pass in an array of Record objects, A Workbook
     * object is constructed and passed back with all of its initialization set
     * to the passed in records and references to those records held. Unlike Sheet
     * workbook does not use an offset (its assumed to be 0) since its first in a file.
     * If you need an offset then construct a new array with a 0 offset or write your
     * own ;-p.
     *
     * @param recs an array of Record objects
     * @return Workbook object
     */
    public static Workbook createWorkbook(List<Record> recs) {
        if (log.check( POILogger.DEBUG ))
            log.log(DEBUG, "Workbook (readfile) created with reclen=",
                    Integer.valueOf(recs.size()));
        Workbook retval = new Workbook();
        List<Record> records = new ArrayList<Record>(recs.size() / 3);
        retval.records.setRecords(records);

        int k;
        for (k = 0; k < recs.size(); k++) {
            Record rec = recs.get(k);

            if (rec.getSid() == EOFRecord.sid) {
                records.add(rec);
                if (log.check( POILogger.DEBUG ))
                    log.log(DEBUG, "found workbook eof record at " + k);
                break;
            }
            switch (rec.getSid()) {

                case BoundSheetRecord.sid :
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found boundsheet record at " + k);
                    retval.boundsheets.add((BoundSheetRecord) rec);
                    retval.records.setBspos( k );
                    break;

                case SSTRecord.sid :
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found sst record at " + k);
                    retval.sst = ( SSTRecord ) rec;
                    break;

                case FontRecord.sid :
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found font record at " + k);
                    retval.records.setFontpos( k );
                    retval.numfonts++;
                    break;

                case ExtendedFormatRecord.sid :
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found XF record at " + k);
                    retval.records.setXfpos( k );
                    retval.numxfs++;
                    break;

                case TabIdRecord.sid :
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found tabid record at " + k);
                    retval.records.setTabpos( k );
                    break;

                case ProtectRecord.sid :
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found protect record at " + k);
                    retval.records.setProtpos( k );
                    break;

                case BackupRecord.sid :
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found backup record at " + k);
                    retval.records.setBackuppos( k );
                    break;
                case ExternSheetRecord.sid :
                    throw new RuntimeException("Extern sheet is part of LinkTable");
                case NameRecord.sid :
                case SupBookRecord.sid :
                    // LinkTable can start with either of these
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found SupBook record at " + k);
                    retval.linkTable = new LinkTable(recs, k, retval.records);
                    k+=retval.linkTable.getRecordCount() - 1;
                    continue;
                case FormatRecord.sid :
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found format record at " + k);
                    retval.formats.add((FormatRecord) rec);
                    retval.maxformatid = retval.maxformatid >= ((FormatRecord)rec).getIndexCode() ? retval.maxformatid : ((FormatRecord)rec).getIndexCode();
                    break;
                case DateWindow1904Record.sid :
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found datewindow1904 record at " + k);
                    retval.uses1904datewindowing = ((DateWindow1904Record)rec).getWindowing() == 1;
                    break;
                case PaletteRecord.sid:
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found palette record at " + k);
                    retval.records.setPalettepos( k );
                    break;
                case WindowOneRecord.sid:
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found WindowOneRecord at " + k);
                    retval.windowOne = (WindowOneRecord) rec;
                    break;
                case WriteAccessRecord.sid:
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found WriteAccess at " + k);
                    retval.writeAccess = (WriteAccessRecord) rec;
                    break;
                case WriteProtectRecord.sid:
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found WriteProtect at " + k);
                    retval.writeProtect = (WriteProtectRecord) rec;
                    break;
                case FileSharingRecord.sid:
                    if (log.check( POILogger.DEBUG ))
                        log.log(DEBUG, "found FileSharing at " + k);
                    retval.fileShare = (FileSharingRecord) rec;
                default :
            }
            records.add(rec);
        }
        //What if we dont have any ranges and supbooks
        //        if (retval.records.supbookpos == 0) {
        //            retval.records.supbookpos = retval.records.bspos + 1;
        //            retval.records.namepos    = retval.records.supbookpos + 1;
        //        }

        // Look for other interesting values that
        //  follow the EOFRecord
        for ( ; k < recs.size(); k++) {
            Record rec = recs.get(k);
            switch (rec.getSid()) {
                case HyperlinkRecord.sid:
                    retval.hyperlinks.add((HyperlinkRecord)rec);
                    break;
            }
        }

        if (retval.windowOne == null) {
            retval.windowOne = createWindowOne();
        }
        if (log.check( POILogger.DEBUG ))
            log.log(DEBUG, "exit create workbook from existing file function");
        return retval;
    }

    /**
     * Creates an empty workbook object with three blank sheets and all the empty
     * fields.  Use this to create a workbook from scratch.
     */
    public static Workbook createWorkbook()
    {
        if (log.check( POILogger.DEBUG ))
            log.log( DEBUG, "creating new workbook from scratch" );
        Workbook retval = new Workbook();
        List<Record> records = new ArrayList<Record>( 30 );
        retval.records.setRecords(records);
        List<FormatRecord> formats = retval.formats;

        records.add(retval.createBOF());
        records.add(retval.createInterfaceHdr());
        records.add(retval.createMMS());
        records.add(retval.createInterfaceEnd());
        records.add(retval.createWriteAccess());
        records.add(retval.createCodepage());
        records.add(retval.createDSF());
        records.add(retval.createTabId());
        retval.records.setTabpos(records.size() - 1);
        records.add(retval.createFnGroupCount());
        records.add(createWindowProtect());
        records.add(createProtect());
        retval.records.setProtpos(records.size() - 1);
        records.add(createPassword());
        records.add(createProtectionRev4());
        records.add(retval.createPasswordRev4());
        retval.windowOne = createWindowOne();
        records.add(retval.windowOne);
        records.add(retval.createBackup());
        retval.records.setBackuppos(records.size() - 1);
        records.add(retval.createHideObj());
        records.add(retval.createDateWindow1904());
        records.add(retval.createPrecision());
        records.add(createRefreshAll());
        records.add(retval.createBookBool());
        records.add(retval.createFont());
        records.add(retval.createFont());
        records.add(retval.createFont());
        records.add(retval.createFont());
        retval.records.setFontpos( records.size() - 1 );   // last font record position
        retval.numfonts = 4;

        // set up format records
        for (int i = 0; i <= 7; i++) {
            FormatRecord rec = createFormat(i);
            retval.maxformatid = retval.maxformatid >= rec.getIndexCode() ? retval.maxformatid : rec.getIndexCode();
            formats.add(rec);
            records.add(rec);
        }

        for (int k = 0; k < 21; k++) {
            records.add(retval.createExtendedFormat(k));
            retval.numxfs++;
        }
        retval.records.setXfpos( records.size() - 1 );
        for (int k = 0; k < 6; k++) {
            records.add(retval.createStyle(k));
        }
        records.add(retval.createUseSelFS());

        int nBoundSheets = 1; // now just do 1
        for (int k = 0; k < nBoundSheets; k++) {
            BoundSheetRecord bsr = createBoundSheet(k);

            records.add(bsr);
            retval.boundsheets.add(bsr);
            retval.records.setBspos(records.size() - 1);
        }
        records.add( retval.createCountry() );
        for ( int k = 0; k < nBoundSheets; k++ ) {
            retval.getOrCreateLinkTable().checkExternSheet(k);
        }
        retval.sst = new SSTRecord();
        records.add(retval.sst);
        records.add(retval.createExtendedSST());

        records.add(EOFRecord.instance);
        if (log.check( POILogger.DEBUG ))
            log.log( DEBUG, "exit create new workbook from scratch" );
        return retval;
    }


    /**Retrieves the Builtin NameRecord that matches the name and index
     * There shouldn't be too many names to make the sequential search too slow
     * @param name byte representation of the builtin name to match
     * @param sheetNumber 1-based sheet number
     * @return null if no builtin NameRecord matches
     */
    public NameRecord getSpecificBuiltinRecord(byte name, int sheetNumber)
    {
        return getOrCreateLinkTable().getSpecificBuiltinRecord(name, sheetNumber);
    }

    /**
     * Removes the specified Builtin NameRecord that matches the name and index
     * @param name byte representation of the builtin to match
     * @param sheetIndex zero-based sheet reference
     */
    public void removeBuiltinRecord(byte name, int sheetIndex) {
        linkTable.removeBuiltinRecord(name, sheetIndex);
        // TODO - do we need "this.records.remove(...);" similar to that in this.removeName(int namenum) {}?
    }

    public int getNumRecords() {
        return records.size();
    }

    /**
     * gets the font record at the given index in the font table.  Remember
     * "There is No Four" (someone at M$ must have gone to Rocky Horror one too
     * many times)
     *
     * @param idx the index to look at (0 or greater but NOT 4)
     * @return FontRecord located at the given index
     */

    public FontRecord getFontRecordAt(int idx) {
        int index = idx;

        if (index > 4) {
            index -= 1;   // adjust for "There is no 4"
        }
        if (index > (numfonts - 1)) {
            throw new ArrayIndexOutOfBoundsException(
            "There are only " + numfonts
            + " font records, you asked for " + idx);
        }
        FontRecord retval =
        ( FontRecord ) records.get((records.getFontpos() - (numfonts - 1)) + index);

        return retval;
    }

    /**
     * Retrieves the index of the given font
     */
    public int getFontIndex(FontRecord font) {
        for(int i=0; i<=numfonts; i++) {
            FontRecord thisFont =
                ( FontRecord ) records.get((records.getFontpos() - (numfonts - 1)) + i);
            if(thisFont == font) {
                // There is no 4!
                if(i > 3) {
                    return (i+1);
                }
                return i;
            }
        }
        throw new IllegalArgumentException("Could not find that font!");
    }

    /**
     * creates a new font record and adds it to the "font table".  This causes the
     * boundsheets to move down one, extended formats to move down (so this function moves
     * those pointers as well)
     *
     * @return FontRecord that was just created
     */

    public FontRecord createNewFont() {
        FontRecord rec = createFont();

        records.add(records.getFontpos()+1, rec);
        records.setFontpos( records.getFontpos() + 1 );
        numfonts++;
        return rec;
    }

    /**
     * Removes the given font record from the
     *  file's list. This will make all
     *  subsequent font indicies drop by one,
     *  so you'll need to update those yourself!
     */
    public void removeFontRecord(FontRecord rec) {
        records.remove(rec); // this updates FontPos for us
        numfonts--;
    }

    /**
     * gets the number of font records
     *
     * @return   number of font records in the "font table"
     */

    public int getNumberOfFontRecords() {
        return numfonts;
    }

    /**
     * Sets the BOF for a given sheet
     *
     * @param sheetIndex the number of the sheet to set the positing of the bof for
     * @param pos the actual bof position
     */

    public void setSheetBof(int sheetIndex, int pos) {
        if (log.check( POILogger.DEBUG ))
            log.log(DEBUG, "setting bof for sheetnum =", Integer.valueOf(sheetIndex),
                " at pos=", Integer.valueOf(pos));
        checkSheets(sheetIndex);
        getBoundSheetRec(sheetIndex)
        .setPositionOfBof(pos);
    }

    private BoundSheetRecord getBoundSheetRec(int sheetIndex) {
        return boundsheets.get(sheetIndex);
    }

    /**
     * Returns the position of the backup record.
     */

    public BackupRecord getBackupRecord() {
        return ( BackupRecord ) records.get(records.getBackuppos());
    }


    /**
     * sets the name for a given sheet.  If the boundsheet record doesn't exist and
     * its only one more than we have, go ahead and create it.  If it's > 1 more than
     * we have, except
     *
     * @param sheetnum the sheet number (0 based)
     * @param sheetname the name for the sheet
     */
    public void setSheetName(int sheetnum, String sheetname) {
        checkSheets(sheetnum);
        BoundSheetRecord sheet = boundsheets.get(sheetnum);
        sheet.setSheetname(sheetname);
    }

    /**
     * Determines whether a workbook contains the provided sheet name.  For the purpose of
     * comparison, long names are truncated to 31 chars.
     *
     * @param name the name to test (case insensitive match)
     * @param excludeSheetIdx the sheet to exclude from the check or -1 to include all sheets in the check.
     * @return true if the sheet contains the name, false otherwise.
     */
    public boolean doesContainsSheetName(String name, int excludeSheetIdx) {
        String aName = name;
        if (aName.length() > MAX_SENSITIVE_SHEET_NAME_LEN) {
            aName = aName.substring(0, MAX_SENSITIVE_SHEET_NAME_LEN);
        }
        for (int i = 0; i < boundsheets.size(); i++) {
            BoundSheetRecord boundSheetRecord = getBoundSheetRec(i);
            if (excludeSheetIdx == i) {
                continue;
            }
            String bName = boundSheetRecord.getSheetname();
            if (bName.length() > MAX_SENSITIVE_SHEET_NAME_LEN) {
                bName = bName.substring(0, MAX_SENSITIVE_SHEET_NAME_LEN);
            }
            if (aName.equalsIgnoreCase(bName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * sets the order of appearance for a given sheet.
     *
     * @param sheetname the name of the sheet to reorder
     * @param pos the position that we want to insert the sheet into (0 based)
     */

    public void setSheetOrder(String sheetname, int pos ) {
    int sheetNumber = getSheetIndex(sheetname);
    //remove the sheet that needs to be reordered and place it in the spot we want
    boundsheets.add(pos, boundsheets.remove(sheetNumber));
    }

    /**
     * gets the name for a given sheet.
     *
     * @param sheetIndex the sheet number (0 based)
     * @return sheetname the name for the sheet
     */
    public String getSheetName(int sheetIndex) {
        return getBoundSheetRec(sheetIndex).getSheetname();
    }

    /**
     * Gets the hidden flag for a given sheet.
     * Note that a sheet could instead be
     *  set to be very hidden, which is different
     *  ({@link #isSheetVeryHidden(int)})
     *
     * @param sheetnum the sheet number (0 based)
     * @return True if sheet is hidden
     */
    public boolean isSheetHidden(int sheetnum) {
        return getBoundSheetRec(sheetnum).isHidden();
    }

    /**
     * Gets the very hidden flag for a given sheet.
     * This is different from the normal
     *  hidden flag
     *  ({@link #isSheetHidden(int)})
     *
     * @param sheetnum the sheet number (0 based)
     * @return True if sheet is very hidden
     */
    public boolean isSheetVeryHidden(int sheetnum) {
        return getBoundSheetRec(sheetnum).isVeryHidden();
    }

    /**
     * Hide or unhide a sheet
     *
     * @param sheetnum The sheet number
     * @param hidden True to mark the sheet as hidden, false otherwise
     */
    public void setSheetHidden(int sheetnum, boolean hidden) {
        getBoundSheetRec(sheetnum).setHidden(hidden);
    }

    /**
     * Hide or unhide a sheet.
     *  0 = not hidden
     *  1 = hidden
     *  2 = very hidden.
     *
     * @param sheetnum The sheet number
     * @param hidden 0 for not hidden, 1 for hidden, 2 for very hidden
     */
    public void setSheetHidden(int sheetnum, int hidden) {
        BoundSheetRecord bsr = getBoundSheetRec(sheetnum);
        boolean h = false;
        boolean vh = false;
        if(hidden == 0) {
        } else if(hidden == 1) {
            h = true;
        } else if(hidden == 2) {
            vh = true;
        } else {
            throw new IllegalArgumentException("Invalid hidden flag " + hidden + " given, must be 0, 1 or 2");
        }
        bsr.setHidden(h);
        bsr.setVeryHidden(vh);
    }


    /**
     * get the sheet's index
     * @param name  sheet name
     * @return sheet index or -1 if it was not found.
     */
    public int getSheetIndex(String name) {
        int retval = -1;

        for (int k = 0; k < boundsheets.size(); k++) {
            String sheet = getSheetName(k);

            if (sheet.equalsIgnoreCase(name)) {
                retval = k;
                break;
            }
        }
        return retval;
    }

    /**
     * if we're trying to address one more sheet than we have, go ahead and add it!  if we're
     * trying to address >1 more than we have throw an exception!
     */
    private void checkSheets(int sheetnum) {
        if ((boundsheets.size()) <= sheetnum) {   // if we're short one add another..
            if ((boundsheets.size() + 1) <= sheetnum) {
                throw new RuntimeException("Sheet number out of bounds!");
            }
            BoundSheetRecord bsr = createBoundSheet(sheetnum);

            records.add(records.getBspos()+1, bsr);
            records.setBspos( records.getBspos() + 1 );
            boundsheets.add(bsr);
            getOrCreateLinkTable().checkExternSheet(sheetnum);
            fixTabIdRecord();
        }
    }

    /**
     * @param sheetIndex zero based sheet index
     */
    public void removeSheet(int sheetIndex) {
        if (boundsheets.size() > sheetIndex) {
            records.remove(records.getBspos() - (boundsheets.size() - 1) + sheetIndex);
            boundsheets.remove(sheetIndex);
            fixTabIdRecord();
        }

        // Within NameRecords, it's ok to have the formula
        //  part point at deleted sheets. It's also ok to
        //  have the ExternSheetNumber point at deleted
        //  sheets.
        // However, the sheet index must be adjusted, or
        //  excel will break. (Sheet index is either 0 for
        //  global, or 1 based index to sheet)
        int sheetNum1Based = sheetIndex + 1;
        for(int i=0; i<getNumNames(); i++) {
            NameRecord nr = getNameRecord(i);

            if(nr.getSheetNumber() == sheetNum1Based) {
                // Excel re-writes these to point to no sheet
                nr.setSheetNumber(0);
            } else if(nr.getSheetNumber() > sheetNum1Based) {
                // Bump down by one, so still points
                //  at the same sheet
                nr.setSheetNumber(nr.getSheetNumber()-1);
            }
        }
    }

    /**
     * make the tabid record look like the current situation.
     *
     */
    private void fixTabIdRecord() {
        TabIdRecord tir = ( TabIdRecord ) records.get(records.getTabpos());
        short[]     tia = new short[ boundsheets.size() ];

        for (short k = 0; k < tia.length; k++) {
            tia[ k ] = k;
        }
        tir.setTabIdArray(tia);
    }

    /**
     * returns the number of boundsheet objects contained in this workbook.
     *
     * @return number of BoundSheet records
     */

    public int getNumSheets() {
        if (log.check( POILogger.DEBUG ))
            log.log(DEBUG, "getNumSheets=", Integer.valueOf(boundsheets.size()));
        return boundsheets.size();
    }

    /**
     * get the number of ExtendedFormat records contained in this workbook.
     *
     * @return int count of ExtendedFormat records
     */

    public int getNumExFormats() {
        if (log.check( POILogger.DEBUG ))
            log.log(DEBUG, "getXF=", Integer.valueOf(numxfs));
        return numxfs;
    }

    /**
     * gets the ExtendedFormatRecord at the given 0-based index
     *
     * @param index of the Extended format record (0-based)
     * @return ExtendedFormatRecord at the given index
     */

    public ExtendedFormatRecord getExFormatAt(int index) {
        int xfptr = records.getXfpos() - (numxfs - 1);

        xfptr += index;
        ExtendedFormatRecord retval =
        ( ExtendedFormatRecord ) records.get(xfptr);

        return retval;
    }

    /**
     * Removes the given ExtendedFormatRecord record from the
     *  file's list. This will make all
     *  subsequent font indicies drop by one,
     *  so you'll need to update those yourself!
     */
    public void removeExFormatRecord(ExtendedFormatRecord rec) {
        records.remove(rec); // this updates XfPos for us
        numxfs--;
    }


    /**
     * creates a new Cell-type Extneded Format Record and adds it to the end of
     *  ExtendedFormatRecords collection
     *
     * @return ExtendedFormatRecord that was created
     */

    public ExtendedFormatRecord createCellXF() {
        ExtendedFormatRecord xf = createExtendedFormat();

        records.add(records.getXfpos()+1, xf);
        records.setXfpos( records.getXfpos() + 1 );
        numxfs++;
        return xf;
    }

    /**
     * Returns the StyleRecord for the given
     *  xfIndex, or null if that ExtendedFormat doesn't
     *  have a Style set.
     */
    public StyleRecord getStyleRecord(int xfIndex) {
        // Style records always follow after
        //  the ExtendedFormat records
        for(int i=records.getXfpos(); i<records.size(); i++) {
            Record r = records.get(i);
            if(r instanceof ExtendedFormatRecord) {
                continue;
            }
            if(!(r instanceof StyleRecord)) {
                return null;
            }
            StyleRecord sr = (StyleRecord)r;
            if(sr.getXFIndex() == xfIndex) {
                return sr;
            }
        }
        return null;
    }
    /**
     * Creates a new StyleRecord, for the given Extended
     *  Format index, and adds it onto the end of the
     *  records collection
     */
    public StyleRecord createStyleRecord(int xfIndex) {
        // Style records always follow after
        //  the ExtendedFormat records
        StyleRecord newSR = new StyleRecord();
        newSR.setXFIndex(xfIndex);

        // Find the spot
        int addAt = -1;
        for(int i=records.getXfpos(); i<records.size() &&
                addAt == -1; i++) {
            Record r = records.get(i);
            if(r instanceof ExtendedFormatRecord ||
                    r instanceof StyleRecord) {
                // Keep going
            } else {
                addAt = i;
            }
        }
        if(addAt == -1) {
            throw new IllegalStateException("No XF Records found!");
        }
        records.add(addAt, newSR);

        return newSR;
    }

    /**
     * Adds a string to the SST table and returns its index (if its a duplicate
     * just returns its index and update the counts) ASSUMES compressed unicode
     * (meaning 8bit)
     *
     * @param string the string to be added to the SSTRecord
     *
     * @return index of the string within the SSTRecord
     */

    public int addSSTString(UnicodeString string) {
        if (log.check( POILogger.DEBUG ))
          log.log(DEBUG, "insert to sst string='", string);
        if (sst == null) {
            insertSST();
        }
      return sst.addString(string);
    }

    /**
     * given an index into the SST table, this function returns the corresponding String value
     * @return String containing the SST String
     */

    public UnicodeString getSSTString(int str) {
        if (sst == null) {
            insertSST();
        }
        UnicodeString retval = sst.getString(str);

        if (log.check( POILogger.DEBUG ))
            log.log(DEBUG, "Returning SST for index=", Integer.valueOf(str),
                " String= ", retval);
        return retval;
    }

    /**
     * use this function to add a Shared String Table to an existing sheet (say
     * generated by a different java api) without an sst....
     * @see #createExtendedSST()
     * @see org.apache.poi.hssf.record.SSTRecord
     */

    public void insertSST() {
        if (log.check( POILogger.DEBUG ))
            log.log(DEBUG, "creating new SST via insertSST!");
        sst = new SSTRecord();
        records.add(records.size() - 1, createExtendedSST());
        records.add(records.size() - 2, sst);
    }

    /**
     * Serializes all records int the worksheet section into a big byte array. Use
     * this to write the Workbook out.
     *
     * @return byte array containing the HSSF-only portions of the POIFS file.
     */
     // GJS: Not used so why keep it.
//    public byte [] serialize() {
//        log.log(DEBUG, "Serializing Workbook!");
//        byte[] retval    = null;
//
////         ArrayList bytes     = new ArrayList(records.size());
//        int    arraysize = getSize();
//        int    pos       = 0;
//
//        retval = new byte[ arraysize ];
//        for (int k = 0; k < records.size(); k++) {
//
//            Record record = records.get(k);
////             Let's skip RECALCID records, as they are only use for optimization
//        if(record.getSid() != RecalcIdRecord.sid || ((RecalcIdRecord)record).isNeeded()) {
//                pos += record.serialize(pos, retval);   // rec.length;
//        }
//        }
//        log.log(DEBUG, "Exiting serialize workbook");
//        return retval;
//    }

    /**
     * Serializes all records int the worksheet section into a big byte array. Use
     * this to write the Workbook out.
     * @param offset of the data to be written
     * @param data array of bytes to write this to
     */

    public int serialize( int offset, byte[] data )
    {
        if (log.check( POILogger.DEBUG ))
            log.log( DEBUG, "Serializing Workbook with offsets" );

        int pos = 0;

        SSTRecord sst = null;
        int sstPos = 0;
        boolean wroteBoundSheets = false;
        for ( int k = 0; k < records.size(); k++ )
        {

            Record record = records.get( k );
            // Let's skip RECALCID records, as they are only use for optimization
            if ( record.getSid() != RecalcIdRecord.sid || ( (RecalcIdRecord) record ).isNeeded() )
            {
                int len = 0;
                if (record instanceof SSTRecord)
                {
                    sst = (SSTRecord)record;
                    sstPos = pos;
                }
                if (record.getSid() == ExtSSTRecord.sid && sst != null)
                {
                    record = sst.createExtSSTRecord(sstPos + offset);
                }
                if (record instanceof BoundSheetRecord) {
                     if(!wroteBoundSheets) {
                        for (int i = 0; i < boundsheets.size(); i++) {
                            len+= getBoundSheetRec(i)
                                             .serialize(pos+offset+len, data);
                        }
                        wroteBoundSheets = true;
                     }
                } else {
                   len = record.serialize( pos + offset, data );
                }
                /////  DEBUG BEGIN /////
//                if (len != record.getRecordSize())
//                    throw new IllegalStateException("Record size does not match serialized bytes.  Serialized size = " + len + " but getRecordSize() returns " + record.getRecordSize());
                /////  DEBUG END /////
                pos += len;   // rec.length;
            }
        }
        if (log.check( POILogger.DEBUG ))
            log.log( DEBUG, "Exiting serialize workbook" );
        return pos;
    }

    public int getSize()
    {
        int retval = 0;

        SSTRecord sst = null;
        for ( int k = 0; k < records.size(); k++ )
        {
            Record record = records.get( k );
            // Let's skip RECALCID records, as they are only use for optimization
            if ( record.getSid() != RecalcIdRecord.sid || ( (RecalcIdRecord) record ).isNeeded() )
            {
                if (record instanceof SSTRecord)
                    sst = (SSTRecord)record;
                if (record.getSid() == ExtSSTRecord.sid && sst != null)
                    retval += sst.calcExtSSTRecordSize();
                else
                    retval += record.getRecordSize();
            }
        }
        return retval;
    }

    private static BOFRecord createBOF() {
        BOFRecord retval = new BOFRecord();

        retval.setVersion(( short ) 0x600);
        retval.setType(BOFRecord.TYPE_WORKBOOK);
        retval.setBuild(( short ) 0x10d3);
        retval.setBuildYear(( short ) 1996);
        retval.setHistoryBitMask(0x41);   // was c1 before verify
        retval.setRequiredVersion(0x6);
        return retval;
    }

    private static InterfaceHdrRecord createInterfaceHdr() {
        InterfaceHdrRecord retval = new InterfaceHdrRecord();

        retval.setCodepage(CODEPAGE);
        return retval;
    }

    private static MMSRecord createMMS() {
        MMSRecord retval = new MMSRecord();

        retval.setAddMenuCount(( byte ) 0);
        retval.setDelMenuCount(( byte ) 0);
        return retval;
    }

    private static InterfaceEndRecord createInterfaceEnd() {
        return new InterfaceEndRecord();
    }

    /**
     * creates the WriteAccess record containing the logged in user's name
     */
    private static WriteAccessRecord createWriteAccess() {
        WriteAccessRecord retval = new WriteAccessRecord();

        try {
            retval.setUsername(System.getProperty("user.name"));
        } catch (AccessControlException e) {
                // AccessControlException can occur in a restricted context
                // (client applet/jws application or restricted security server)
                retval.setUsername("POI");
        }
        return retval;
    }

    private static CodepageRecord createCodepage() {
        CodepageRecord retval = new CodepageRecord();

        retval.setCodepage(CODEPAGE);
        return retval;
    }

    private static DSFRecord createDSF() {
        return new DSFRecord(false); // we don't even support double stream files
    }

    /**
     * creates the TabId record containing an array
     */
    private static TabIdRecord createTabId() {
        return new TabIdRecord();
    }

    /**
     * creates the FnGroupCount record containing the Magic number constant of 14.
     */
    private static FnGroupCountRecord createFnGroupCount() {
        FnGroupCountRecord retval = new FnGroupCountRecord();

        retval.setCount(( short ) 14);
        return retval;
    }

    /**
     * @return a new WindowProtect record with protect set to false.
     */
    private static WindowProtectRecord createWindowProtect() {
        // by default even when we support it we won't
        // want it to be protected
        return new WindowProtectRecord(false);
    }

    /**
     * @return a new Protect record with protect set to false.
     */
    private static ProtectRecord createProtect() {
        // by default even when we support it we won't
        // want it to be protected
        return new ProtectRecord(false);
    }

    /**
     * @return a new Password record with password set to 0x0000 (no password).
     */
    private static PasswordRecord createPassword() {
        return new PasswordRecord(0x0000); // no password by default!
    }

    /**
     * @return a new ProtectionRev4 record with protect set to false.
     */
    private static ProtectionRev4Record createProtectionRev4() {
        return new ProtectionRev4Record(false);
    }

    /**
     * @return a new PasswordRev4 record with password set to 0.
     */
    private static PasswordRev4Record createPasswordRev4() {
        return new PasswordRev4Record(0x0000);
    }

    /**
     * creates the WindowOne record with the following magic values: <P>
     * horizontal hold - 0x168 <P>
     * vertical hold   - 0x10e <P>
     * width           - 0x3a5c <P>
     * height          - 0x23be <P>
     * options         - 0x38 <P>
     * selected tab    - 0 <P>
     * displayed tab   - 0 <P>
     * num selected tab- 0 <P>
     * tab width ratio - 0x258 <P>
     */
    private static WindowOneRecord createWindowOne() {
        WindowOneRecord retval = new WindowOneRecord();

        retval.setHorizontalHold(( short ) 0x168);
        retval.setVerticalHold(( short ) 0x10e);
        retval.setWidth(( short ) 0x3a5c);
        retval.setHeight(( short ) 0x23be);
        retval.setOptions(( short ) 0x38);
        retval.setActiveSheetIndex( 0x0);
        retval.setFirstVisibleTab(0x0);
        retval.setNumSelectedTabs(( short ) 1);
        retval.setTabWidthRatio(( short ) 0x258);
        return retval;
    }

    /**
     * creates the Backup record with backup set to 0. (loose the data, who cares)
     */
    private static BackupRecord createBackup() {
        BackupRecord retval = new BackupRecord();

        retval.setBackup(( short ) 0);   // by default DONT save backups of files...just loose data
        return retval;
    }

    /**
     * creates the HideObj record with hide object set to 0. (don't hide)
     */
    private static HideObjRecord createHideObj() {
        HideObjRecord retval = new HideObjRecord();
        retval.setHideObj(( short ) 0);   // by default set hide object off
        return retval;
    }

    /**
     * creates the DateWindow1904 record with windowing set to 0. (don't window)
     */
    private static DateWindow1904Record createDateWindow1904() {
        DateWindow1904Record retval = new DateWindow1904Record();

        retval.setWindowing(( short ) 0);   // don't EVER use 1904 date windowing...tick tock..
        return retval;
    }

    /**
     * creates the Precision record with precision set to true. (full precision)
     */
    private static PrecisionRecord createPrecision() {
        PrecisionRecord retval = new PrecisionRecord();
        retval.setFullPrecision(true);   // always use real numbers in calculations!
        return retval;
    }

    /**
     * @return a new RefreshAll record with refreshAll set to false. (do not refresh all calcs)
     */
    private static RefreshAllRecord createRefreshAll() {
        return new RefreshAllRecord(false);
    }

    /**
     * creates the BookBool record with saveLinkValues set to 0. (don't save link values)
     */
    private static BookBoolRecord createBookBool() {
        BookBoolRecord retval = new BookBoolRecord();
        retval.setSaveLinkValues(( short ) 0);
        return retval;
    }

    /**
     * creates a Font record with the following magic values: <P>
     * fontheight           = 0xc8<P>
     * attributes           = 0x0<P>
     * color palette index  = 0x7fff<P>
     * bold weight          = 0x190<P>
     * Font Name Length     = 5 <P>
     * Font Name            = Arial <P>
     */
    private static FontRecord createFont() {
        FontRecord retval = new FontRecord();

        retval.setFontHeight(( short ) 0xc8);
        retval.setAttributes(( short ) 0x0);
        retval.setColorPaletteIndex(( short ) 0x7fff);
        retval.setBoldWeight(( short ) 0x190);
        retval.setFontName("Arial");
        return retval;
    }

    /**
     * Creates a FormatRecord object
     * @param id    the number of the format record to create (meaning its position in
     *        a file as M$ Excel would create it.)
     */
    private static FormatRecord createFormat(int id) {
        // we'll need multiple editions for
        // the different formats

        switch (id) {
            case 0: return new FormatRecord(5, "\"$\"#,##0_);\\(\"$\"#,##0\\)");
            case 1: return new FormatRecord(6, "\"$\"#,##0_);[Red]\\(\"$\"#,##0\\)");
            case 2: return new FormatRecord(7, "\"$\"#,##0.00_);\\(\"$\"#,##0.00\\)");
            case 3: return new FormatRecord(8, "\"$\"#,##0.00_);[Red]\\(\"$\"#,##0.00\\)");
            case 4: return new FormatRecord(0x2a, "_(\"$\"* #,##0_);_(\"$\"* \\(#,##0\\);_(\"$\"* \"-\"_);_(@_)");
            case 5: return new FormatRecord(0x29, "_(* #,##0_);_(* \\(#,##0\\);_(* \"-\"_);_(@_)");
            case 6: return new FormatRecord(0x2c, "_(\"$\"* #,##0.00_);_(\"$\"* \\(#,##0.00\\);_(\"$\"* \"-\"??_);_(@_)");
            case 7: return new FormatRecord(0x2b, "_(* #,##0.00_);_(* \\(#,##0.00\\);_(* \"-\"??_);_(@_)");
        }
        throw new  IllegalArgumentException("Unexpected id " + id);
    }

    /**
     * Creates an ExtendedFormatRecord object
     * @param id    the number of the extended format record to create (meaning its position in
     *        a file as MS Excel would create it.)
     */
    private static ExtendedFormatRecord createExtendedFormat(int id) {   // we'll need multiple editions
        ExtendedFormatRecord retval = new ExtendedFormatRecord();

        switch (id) {

            case 0 :
                retval.setFontIndex(( short ) 0);
                retval.setFormatIndex(( short ) 0);
                retval.setCellOptions(( short ) 0xfffffff5);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 1 :
                retval.setFontIndex(( short ) 1);
                retval.setFormatIndex(( short ) 0);
                retval.setCellOptions(( short ) 0xfffffff5);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0xfffff400);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 2 :
                retval.setFontIndex(( short ) 1);
                retval.setFormatIndex(( short ) 0);
                retval.setCellOptions(( short ) 0xfffffff5);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0xfffff400);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 3 :
                retval.setFontIndex(( short ) 2);
                retval.setFormatIndex(( short ) 0);
                retval.setCellOptions(( short ) 0xfffffff5);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0xfffff400);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 4 :
                retval.setFontIndex(( short ) 2);
                retval.setFormatIndex(( short ) 0);
                retval.setCellOptions(( short ) 0xfffffff5);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0xfffff400);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 5 :
                retval.setFontIndex(( short ) 0);
                retval.setFormatIndex(( short ) 0);
                retval.setCellOptions(( short ) 0xfffffff5);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0xfffff400);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 6 :
                retval.setFontIndex(( short ) 0);
                retval.setFormatIndex(( short ) 0);
                retval.setCellOptions(( short ) 0xfffffff5);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0xfffff400);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 7 :
                retval.setFontIndex(( short ) 0);
                retval.setFormatIndex(( short ) 0);
                retval.setCellOptions(( short ) 0xfffffff5);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0xfffff400);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 8 :
                retval.setFontIndex(( short ) 0);
                retval.setFormatIndex(( short ) 0);
                retval.setCellOptions(( short ) 0xfffffff5);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0xfffff400);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 9 :
                retval.setFontIndex(( short ) 0);
                retval.setFormatIndex(( short ) 0);
                retval.setCellOptions(( short ) 0xfffffff5);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0xfffff400);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 10 :
                retval.setFontIndex(( short ) 0);
                retval.setFormatIndex(( short ) 0);
                retval.setCellOptions(( short ) 0xfffffff5);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0xfffff400);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 11 :
                retval.setFontIndex(( short ) 0);
                retval.setFormatIndex(( short ) 0);
                retval.setCellOptions(( short ) 0xfffffff5);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0xfffff400);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 12 :
                retval.setFontIndex(( short ) 0);
                retval.setFormatIndex(( short ) 0);
                retval.setCellOptions(( short ) 0xfffffff5);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0xfffff400);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 13 :
                retval.setFontIndex(( short ) 0);
                retval.setFormatIndex(( short ) 0);
                retval.setCellOptions(( short ) 0xfffffff5);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0xfffff400);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 14 :
                retval.setFontIndex(( short ) 0);
                retval.setFormatIndex(( short ) 0);
                retval.setCellOptions(( short ) 0xfffffff5);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0xfffff400);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

                // cell records
            case 15 :
                retval.setFontIndex(( short ) 0);
                retval.setFormatIndex(( short ) 0);
                retval.setCellOptions(( short ) 0x1);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0x0);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

                // style
            case 16 :
                retval.setFontIndex(( short ) 1);
                retval.setFormatIndex(( short ) 0x2b);
                retval.setCellOptions(( short ) 0xfffffff5);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0xfffff800);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 17 :
                retval.setFontIndex(( short ) 1);
                retval.setFormatIndex(( short ) 0x29);
                retval.setCellOptions(( short ) 0xfffffff5);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0xfffff800);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 18 :
                retval.setFontIndex(( short ) 1);
                retval.setFormatIndex(( short ) 0x2c);
                retval.setCellOptions(( short ) 0xfffffff5);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0xfffff800);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 19 :
                retval.setFontIndex(( short ) 1);
                retval.setFormatIndex(( short ) 0x2a);
                retval.setCellOptions(( short ) 0xfffffff5);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0xfffff800);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 20 :
                retval.setFontIndex(( short ) 1);
                retval.setFormatIndex(( short ) 0x9);
                retval.setCellOptions(( short ) 0xfffffff5);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0xfffff800);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

                // unused from this point down
            case 21 :
                retval.setFontIndex(( short ) 5);
                retval.setFormatIndex(( short ) 0x0);
                retval.setCellOptions(( short ) 0x1);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0x800);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 22 :
                retval.setFontIndex(( short ) 6);
                retval.setFormatIndex(( short ) 0x0);
                retval.setCellOptions(( short ) 0x1);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0x5c00);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 23 :
                retval.setFontIndex(( short ) 0);
                retval.setFormatIndex(( short ) 0x31);
                retval.setCellOptions(( short ) 0x1);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0x5c00);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 24 :
                retval.setFontIndex(( short ) 0);
                retval.setFormatIndex(( short ) 0x8);
                retval.setCellOptions(( short ) 0x1);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0x5c00);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;

            case 25 :
                retval.setFontIndex(( short ) 6);
                retval.setFormatIndex(( short ) 0x8);
                retval.setCellOptions(( short ) 0x1);
                retval.setAlignmentOptions(( short ) 0x20);
                retval.setIndentionOptions(( short ) 0x5c00);
                retval.setBorderOptions(( short ) 0);
                retval.setPaletteOptions(( short ) 0);
                retval.setAdtlPaletteOptions(( short ) 0);
                retval.setFillPaletteOptions(( short ) 0x20c0);
                break;
        }
        return retval;
    }

    /**
     * creates an default cell type ExtendedFormatRecord object.
     * @return ExtendedFormatRecord with intial defaults (cell-type)
     */
    private static ExtendedFormatRecord createExtendedFormat() {
        ExtendedFormatRecord retval = new ExtendedFormatRecord();

        retval.setFontIndex(( short ) 0);
        retval.setFormatIndex(( short ) 0x0);
        retval.setCellOptions(( short ) 0x1);
        retval.setAlignmentOptions(( short ) 0x20);
        retval.setIndentionOptions(( short ) 0);
        retval.setBorderOptions(( short ) 0);
        retval.setPaletteOptions(( short ) 0);
        retval.setAdtlPaletteOptions(( short ) 0);
        retval.setFillPaletteOptions(( short ) 0x20c0);
        retval.setTopBorderPaletteIdx(HSSFColor.BLACK.index);
        retval.setBottomBorderPaletteIdx(HSSFColor.BLACK.index);
        retval.setLeftBorderPaletteIdx(HSSFColor.BLACK.index);
        retval.setRightBorderPaletteIdx(HSSFColor.BLACK.index);
        return retval;
    }

    /**
     * Creates a StyleRecord object
     * @param id        the number of the style record to create (meaning its position in
     *                  a file as MS Excel would create it.
     */
    private static StyleRecord createStyle(int id) {   // we'll need multiple editions
        StyleRecord retval = new StyleRecord();

        switch (id) {

            case 0 :
                retval.setXFIndex(0x010);
                retval.setBuiltinStyle(3);
                retval.setOutlineStyleLevel(( byte ) 0xffffffff);
                break;

            case 1 :
                retval.setXFIndex(0x011);
                retval.setBuiltinStyle(6);
                retval.setOutlineStyleLevel(( byte ) 0xffffffff);
                break;

            case 2 :
                retval.setXFIndex(0x012);
                retval.setBuiltinStyle(4);
                retval.setOutlineStyleLevel(( byte ) 0xffffffff);
                break;

            case 3 :
                retval.setXFIndex(0x013);
                retval.setBuiltinStyle(7);
                retval.setOutlineStyleLevel(( byte ) 0xffffffff);
                break;

            case 4 :
                retval.setXFIndex(0x000);
                retval.setBuiltinStyle(0);
                retval.setOutlineStyleLevel(( byte ) 0xffffffff);
                break;

            case 5 :
                retval.setXFIndex(0x014);
                retval.setBuiltinStyle(5);
                retval.setOutlineStyleLevel(( byte ) 0xffffffff);
                break;
        }
        return retval;
    }

    /**
     * Creates a palette record initialized to the default palette
     */
    private static PaletteRecord createPalette() {
        return new PaletteRecord();
    }

    /**
     * @return a new UseSelFS object with the use natural language flag set to 0 (false)
     */
    private static UseSelFSRecord createUseSelFS() {
        return new UseSelFSRecord(false);
    }

    /**
     * create a "bound sheet" or "bundlesheet" (depending who you ask) record
     * Always sets the sheet's bof to 0.  You'll need to set that yourself.
     * @param id either sheet 0,1 or 2.
     * @return record containing a BoundSheetRecord
     * @see org.apache.poi.hssf.record.BoundSheetRecord
     * @see org.apache.poi.hssf.record.Record
     */
    private static BoundSheetRecord createBoundSheet(int id) {
        return new BoundSheetRecord("Sheet" + (id+1));
    }

    /**
     * Creates the Country record with the default country set to 1
     * and current country set to 7 in case of russian locale ("ru_RU") and 1 otherwise
     */
    private static CountryRecord createCountry() {
        CountryRecord retval = new CountryRecord();

        retval.setDefaultCountry(( short ) 1);

        // from Russia with love ;)
        if ( Locale.getDefault().toString().equals( "ru_RU" ) ) {
            retval.setCurrentCountry(( short ) 7);
        }
        else {
            retval.setCurrentCountry(( short ) 1);
        }

        return retval;
    }

    /**
     * Creates the ExtendedSST record with numstrings per bucket set to 0x8.  HSSF
     * doesn't yet know what to do with this thing, but we create it with nothing in
     * it hardly just to make Excel happy and our sheets look like Excel's
     */
    private static ExtSSTRecord createExtendedSST() {
        ExtSSTRecord retval = new ExtSSTRecord();
        retval.setNumStringsPerBucket(( short ) 0x8);
        return retval;
    }

    /**
     * lazy initialization
     * Note - creating the link table causes creation of 1 EXTERNALBOOK and 1 EXTERNALSHEET record
     */
    private LinkTable getOrCreateLinkTable() {
        if(linkTable == null) {
            linkTable = new LinkTable((short) getNumSheets(), records);
        }
        return linkTable;
    }

    /** finds the sheet name by his extern sheet index
     * @param externSheetIndex extern sheet index
     * @return sheet name.
     */
    public String findSheetNameFromExternSheet(int externSheetIndex){

        int indexToSheet = linkTable.getIndexToInternalSheet(externSheetIndex);
        if (indexToSheet < 0) {
            // TODO - what does '-1' mean here?
            //error check, bail out gracefully!
            return "";
        }
        if (indexToSheet >= boundsheets.size()) {
            // Not sure if this can ever happen (See bug 45798)
            return ""; // Seems to be what excel would do in this case
        }
        return getSheetName(indexToSheet);
    }
    public ExternalSheet getExternalSheet(int externSheetIndex) {
        String[] extNames = linkTable.getExternalBookAndSheetName(externSheetIndex);
        if (extNames == null) {
            return null;
        }
        return new ExternalSheet(extNames[0], extNames[1]);
    }

    /**
     * Finds the sheet index for a particular external sheet number.
     * @param externSheetNumber     The external sheet number to convert
     * @return  The index to the sheet found.
     */
    public int getSheetIndexFromExternSheetIndex(int externSheetNumber)
    {
        return linkTable.getSheetIndexFromExternSheetIndex(externSheetNumber);
    }

    /** returns the extern sheet number for specific sheet number ,
     *  if this sheet doesn't exist in extern sheet , add it
     * @param sheetNumber sheet number
     * @return index to extern sheet
     */
    public short checkExternSheet(int sheetNumber){
        return (short)getOrCreateLinkTable().checkExternSheet(sheetNumber);
    }

    public int getExternalSheetIndex(String workbookName, String sheetName) {
        return getOrCreateLinkTable().getExternalSheetIndex(workbookName, sheetName);
    }


    /** gets the total number of names
     * @return number of names
     */
    public int getNumNames(){
        if(linkTable == null) {
            return 0;
        }
        return linkTable.getNumNames();
    }

    /** gets the name record
     * @param index name index
     * @return name record
     */
    public NameRecord getNameRecord(int index){
        return linkTable.getNameRecord(index);
    }

    /** creates new name
     * @return new name record
     */
    public NameRecord createName(){
        return addName(new NameRecord());
    }


    /** creates new name
     * @return new name record
     */
    public NameRecord addName(NameRecord name)
    {

        LinkTable linkTable = getOrCreateLinkTable();
        if(linkTable.nameAlreadyExists(name)) {
            throw new IllegalArgumentException(
                "You are trying to assign a duplicated name record: "
                + name.getNameText());
        }
        linkTable.addName(name);

        return name;
    }

    /**
     * Generates a NameRecord to represent a built-in region
     * @return a new NameRecord
     */
    public NameRecord createBuiltInName(byte builtInName, int sheetNumber) {
        if (sheetNumber < 0 || sheetNumber+1 > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Sheet number ["+sheetNumber+"]is not valid ");
        }

        NameRecord name = new NameRecord(builtInName, sheetNumber);

        while(linkTable.nameAlreadyExists(name)) {
            throw new RuntimeException("Builtin (" + builtInName
                    + ") already exists for sheet (" + sheetNumber + ")");
        }
        addName(name);
        return name;
    }


    /** removes the name
     * @param nameIndex name index
     */
    public void removeName(int nameIndex){

        if (linkTable.getNumNames() > nameIndex) {
            int idx = findFirstRecordLocBySid(NameRecord.sid);
            records.remove(idx + nameIndex);
            linkTable.removeName(nameIndex);
        }
    }

    /**
     * Returns a format index that matches the passed in format.  It does not tie into HSSFDataFormat.
     * @param format the format string
     * @param createIfNotFound creates a new format if format not found
     * @return the format id of a format that matches or -1 if none found and createIfNotFound
     */
    public short getFormat(String format, boolean createIfNotFound) {
    Iterator iterator;
    for (iterator = formats.iterator(); iterator.hasNext();) {
        FormatRecord r = (FormatRecord)iterator.next();
        if (r.getFormatString().equals(format)) {
        return (short)r.getIndexCode();
        }
    }

    if (createIfNotFound) {
        return (short)createFormat(format);
    }

    return -1;
    }

    /**
     * Returns the list of FormatRecords in the workbook.
     * @return ArrayList of FormatRecords in the notebook
     */
    public List getFormats() {
    return formats;
    }

    /**
     * Creates a FormatRecord, inserts it, and returns the index code.
     * @param formatString the format string
     * @return the index code of the format record.
     * @see org.apache.poi.hssf.record.FormatRecord
     * @see org.apache.poi.hssf.record.Record
     */
    public int createFormat(String formatString) {

        maxformatid = maxformatid >= 0xa4 ? maxformatid + 1 : 0xa4; //Starting value from M$ empircal study.
        FormatRecord rec = new FormatRecord(maxformatid, formatString);

        int pos = 0;
        while ( pos < records.size() && records.get( pos ).getSid() != FormatRecord.sid )
            pos++;
        pos += formats.size();
        formats.add( rec );
        records.add( pos, rec );
        return maxformatid;
    }



    /**
     * Returns the first occurance of a record matching a particular sid.
     */
    public Record findFirstRecordBySid(short sid) {
        for (Iterator iterator = records.iterator(); iterator.hasNext(); ) {
            Record record = ( Record ) iterator.next();

            if (record.getSid() == sid) {
                return record;
            }
        }
        return null;
    }

    /**
     * Returns the index of a record matching a particular sid.
     * @param sid   The sid of the record to match
     * @return      The index of -1 if no match made.
     */
    public int findFirstRecordLocBySid(short sid) {
        int index = 0;
        for (Iterator iterator = records.iterator(); iterator.hasNext(); ) {
            Record record = ( Record ) iterator.next();

            if (record.getSid() == sid) {
                return index;
            }
            index ++;
        }
        return -1;
    }

    /**
     * Returns the next occurance of a record matching a particular sid.
     */
    public Record findNextRecordBySid(short sid, int pos) {
        int matches = 0;
        for (Iterator iterator = records.iterator(); iterator.hasNext(); ) {
            Record record = ( Record ) iterator.next();

            if (record.getSid() == sid) {
                if (matches++ == pos)
                    return record;
            }
        }
        return null;
    }

    public List getHyperlinks()
    {
        return hyperlinks;
    }

    public List<Record> getRecords() {
        return records.getRecords();
    }

    /**
    * Whether date windowing is based on 1/2/1904 or 1/1/1900.
    * Some versions of Excel (Mac) can save workbooks using 1904 date windowing.
    *
    * @return true if using 1904 date windowing
    */
    public boolean isUsing1904DateWindowing() {
        return uses1904datewindowing;
    }

    /**
     * Returns the custom palette in use for this workbook; if a custom palette record
     * does not exist, then it is created.
     */
    public PaletteRecord getCustomPalette()
    {
      PaletteRecord palette;
      int palettePos = records.getPalettepos();
      if (palettePos != -1) {
        Record rec = records.get(palettePos);
        if (rec instanceof PaletteRecord) {
          palette = (PaletteRecord) rec;
        } else throw new RuntimeException("InternalError: Expected PaletteRecord but got a '"+rec+"'");
      }
      else
      {
          palette = createPalette();
          //Add the palette record after the bof which is always the first record
          records.add(1, palette);
          records.setPalettepos(1);
      }
      return palette;
    }

    /**
     * Finds the primary drawing group, if one already exists
     */
    public void findDrawingGroup() {
        // Need to find a DrawingGroupRecord that
        //  contains a EscherDggRecord
        for(Iterator<Record> rit = records.iterator(); rit.hasNext();) {
            Record r = rit.next();

            if(r instanceof DrawingGroupRecord) {
                DrawingGroupRecord dg = (DrawingGroupRecord)r;
                dg.processChildRecords();

                EscherContainerRecord cr =
                    dg.getEscherContainer();
                if(cr == null) {
                    continue;
                }

                EscherDggRecord dgg = null;
                for(Iterator<EscherRecord> it = cr.getChildIterator(); it.hasNext();) {
                    Object er = it.next();
                    if(er instanceof EscherDggRecord) {
                        dgg = (EscherDggRecord)er;
                    }
                }

                if(dgg != null) {
                    drawingManager = new DrawingManager2(dgg);
                    return;
                }
            }
        }

        // Look for the DrawingGroup record
        int dgLoc = findFirstRecordLocBySid(DrawingGroupRecord.sid);

        // If there is one, does it have a EscherDggRecord?
        if(dgLoc != -1) {
            DrawingGroupRecord dg = (DrawingGroupRecord)records.get(dgLoc);
            EscherDggRecord dgg = null;
            for(Iterator it = dg.getEscherRecords().iterator(); it.hasNext();) {
                Object er = it.next();
                if(er instanceof EscherDggRecord) {
                    dgg = (EscherDggRecord)er;
                }
            }

            if(dgg != null) {
                drawingManager = new DrawingManager2(dgg);
            }
        }
    }

    /**
     * Creates a primary drawing group record.  If it already
     *  exists then it's modified.
     */
    public void createDrawingGroup() {
        if (drawingManager == null) {
            EscherContainerRecord dggContainer = new EscherContainerRecord();
            EscherDggRecord dgg = new EscherDggRecord();
            EscherOptRecord opt = new EscherOptRecord();
            EscherSplitMenuColorsRecord splitMenuColors = new EscherSplitMenuColorsRecord();

            dggContainer.setRecordId((short) 0xF000);
            dggContainer.setOptions((short) 0x000F);
            dgg.setRecordId(EscherDggRecord.RECORD_ID);
            dgg.setOptions((short)0x0000);
            dgg.setShapeIdMax(1024);
            dgg.setNumShapesSaved(0);
            dgg.setDrawingsSaved(0);
            dgg.setFileIdClusters(new EscherDggRecord.FileIdCluster[] {} );
            drawingManager = new DrawingManager2(dgg);
            EscherContainerRecord bstoreContainer = null;
            if (escherBSERecords.size() > 0)
            {
                bstoreContainer = new EscherContainerRecord();
                bstoreContainer.setRecordId( EscherContainerRecord.BSTORE_CONTAINER );
                bstoreContainer.setOptions( (short) ( (escherBSERecords.size() << 4) | 0xF ) );
                for ( Iterator iterator = escherBSERecords.iterator(); iterator.hasNext(); )
                {
                    EscherRecord escherRecord = (EscherRecord) iterator.next();
                    bstoreContainer.addChildRecord( escherRecord );
                }
            }
            opt.setRecordId((short) 0xF00B);
            opt.setOptions((short) 0x0033);
            opt.addEscherProperty( new EscherBoolProperty(EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 524296) );
            opt.addEscherProperty( new EscherRGBProperty(EscherProperties.FILL__FILLCOLOR, 0x08000041) );
            opt.addEscherProperty( new EscherRGBProperty(EscherProperties.LINESTYLE__COLOR, 134217792) );
            splitMenuColors.setRecordId((short) 0xF11E);
            splitMenuColors.setOptions((short) 0x0040);
            splitMenuColors.setColor1(0x0800000D);
            splitMenuColors.setColor2(0x0800000C);
            splitMenuColors.setColor3(0x08000017);
            splitMenuColors.setColor4(0x100000F7);

            dggContainer.addChildRecord(dgg);
            if (bstoreContainer != null)
                dggContainer.addChildRecord( bstoreContainer );
            dggContainer.addChildRecord(opt);
            dggContainer.addChildRecord(splitMenuColors);

            int dgLoc = findFirstRecordLocBySid(DrawingGroupRecord.sid);
            if (dgLoc == -1) {
                DrawingGroupRecord drawingGroup = new DrawingGroupRecord();
                drawingGroup.addEscherRecord(dggContainer);
                int loc = findFirstRecordLocBySid(CountryRecord.sid);

                getRecords().add(loc+1, drawingGroup);
            } else {
                DrawingGroupRecord drawingGroup = new DrawingGroupRecord();
                drawingGroup.addEscherRecord(dggContainer);
                getRecords().set(dgLoc, drawingGroup);
            }

        }
    }

    public WindowOneRecord getWindowOne() {
        return windowOne;
    }

    public EscherBSERecord getBSERecord(int pictureIndex) {
        return escherBSERecords.get(pictureIndex-1);
    }

    public int addBSERecord(EscherBSERecord e) {
        createDrawingGroup();

        // maybe we don't need that as an instance variable anymore
        escherBSERecords.add( e );

        int dgLoc = findFirstRecordLocBySid(DrawingGroupRecord.sid);
        DrawingGroupRecord drawingGroup = (DrawingGroupRecord) getRecords().get( dgLoc );

        EscherContainerRecord dggContainer = (EscherContainerRecord) drawingGroup.getEscherRecord( 0 );
        EscherContainerRecord bstoreContainer;
        if (dggContainer.getChild( 1 ).getRecordId() == EscherContainerRecord.BSTORE_CONTAINER )
        {
            bstoreContainer = (EscherContainerRecord) dggContainer.getChild( 1 );
        }
        else
        {
            bstoreContainer = new EscherContainerRecord();
            bstoreContainer.setRecordId( EscherContainerRecord.BSTORE_CONTAINER );
            List<EscherRecord> childRecords = dggContainer.getChildRecords();
            childRecords.add(1, bstoreContainer);
            dggContainer.setChildRecords(childRecords);
        }
        bstoreContainer.setOptions( (short) ( (escherBSERecords.size() << 4) | 0xF ) );

        bstoreContainer.addChildRecord( e );

        return escherBSERecords.size();
    }

    public DrawingManager2 getDrawingManager()
    {
        return drawingManager;
    }

    public WriteProtectRecord getWriteProtect() {
        if (writeProtect == null) {
           writeProtect = new WriteProtectRecord();
           int i = 0;
           for (i = 0;
                i < records.size() && !(records.get(i) instanceof BOFRecord);
                i++) {
           }
           records.add(i+1, writeProtect);
        }
        return this.writeProtect;
    }

    public WriteAccessRecord getWriteAccess() {
        if (writeAccess == null) {
           writeAccess = createWriteAccess();
           int i = 0;
           for (i = 0;
                i < records.size() && !(records.get(i) instanceof InterfaceEndRecord);
                i++) {
           }
           records.add(i+1, writeAccess);
        }
        return writeAccess;
    }

    public FileSharingRecord getFileSharing() {
        if (fileShare == null) {
           fileShare = new FileSharingRecord();
           int i = 0;
           for (i = 0;
                i < records.size() && !(records.get(i) instanceof WriteAccessRecord);
                i++) {
           }
           records.add(i+1, fileShare);
        }
        return fileShare;
    }

    /**
     * is the workbook protected with a password (not encrypted)?
     */
    public boolean isWriteProtected() {
        if (fileShare == null) {
            return false;
        }
        FileSharingRecord frec = getFileSharing();
        return frec.getReadOnly() == 1;
    }

    /**
     * protect a workbook with a password (not encypted, just sets writeprotect
     * flags and the password.
     * @param password to set
     */
    public void writeProtectWorkbook( String password, String username ) {
        int protIdx = -1;
        FileSharingRecord frec = getFileSharing();
        WriteAccessRecord waccess = getWriteAccess();
        WriteProtectRecord wprotect = getWriteProtect();
        frec.setReadOnly((short)1);
        frec.setPassword(FileSharingRecord.hashPassword(password));
        frec.setUsername(username);
        waccess.setUsername(username);
    }

    /**
     * removes the write protect flag
     */
    public void unwriteProtectWorkbook() {
        records.remove(fileShare);
        records.remove(writeProtect);
        fileShare = null;
        writeProtect = null;
    }

    /**
     * @param refIndex Index to REF entry in EXTERNSHEET record in the Link Table
     * @param definedNameIndex zero-based to DEFINEDNAME or EXTERNALNAME record
     * @return the string representation of the defined or external name
     */
    public String resolveNameXText(int refIndex, int definedNameIndex) {
        return linkTable.resolveNameXText(refIndex, definedNameIndex);
    }

    public NameXPtg getNameXPtg(String name) {
        return getOrCreateLinkTable().getNameXPtg(name);
    }

    /**
     * Check if the cloned sheet has drawings. If yes, then allocate a new drawing group ID and
     * re-generate shape IDs
     *
     * @param sheet the cloned sheet
     */
    public void cloneDrawings(Sheet sheet){

        findDrawingGroup();

        if(drawingManager == null) {
            //this workbook does not have drawings
            return;
        }

        //check if the cloned sheet has drawings
        int aggLoc = sheet.aggregateDrawingRecords(drawingManager, false);
        if(aggLoc != -1) {
            EscherAggregate agg = (EscherAggregate) sheet.findFirstRecordBySid(EscherAggregate.sid);
            EscherContainerRecord escherContainer = agg.getEscherContainer();
            if (escherContainer == null) {
                return;
            }

            EscherDggRecord dgg = drawingManager.getDgg();

            //register a new drawing group for the cloned sheet
            int dgId = drawingManager.findNewDrawingGroupId();
            dgg.addCluster( dgId, 0 );
            dgg.setDrawingsSaved(dgg.getDrawingsSaved() + 1);

            EscherDgRecord dg = null;
            for(Iterator<EscherRecord> it = escherContainer.getChildIterator(); it.hasNext();) {
                EscherRecord er = it.next();
                if(er instanceof EscherDgRecord) {
                    dg = (EscherDgRecord)er;
                    //update id of the drawing in the cloned sheet
                    dg.setOptions( (short) ( dgId << 4 ) );
                } else if (er instanceof EscherContainerRecord){
                    //recursively find shape records and re-generate shapeId
                    List<EscherRecord> spRecords = new ArrayList<EscherRecord>();
                    EscherContainerRecord cp = (EscherContainerRecord)er;
                    cp.getRecordsById(EscherSpRecord.RECORD_ID,  spRecords);
                    for(Iterator<EscherRecord> spIt = spRecords.iterator(); spIt.hasNext();) {
                        EscherSpRecord sp = (EscherSpRecord)spIt.next();
                        int shapeId = drawingManager.allocateShapeId((short)dgId, dg);
                        //allocateShapeId increments the number of shapes. roll back to the previous value
                        dg.setNumShapes(dg.getNumShapes()-1);
                        sp.setShapeId(shapeId);
                    }
                }
            }

        }
    }

    /**
     * Updates named ranges due to moving of cells
     */
    public void updateNamesAfterCellShift(FormulaShifter shifter) {
        for (int i = 0 ; i < getNumNames() ; ++i){
            NameRecord nr = getNameRecord(i);
            Ptg[] ptgs = nr.getNameDefinition();
            if (shifter.adjustFormula(ptgs, nr.getSheetNumber())) {
                nr.setNameDefinition(ptgs);
            }
        }
    }

}
