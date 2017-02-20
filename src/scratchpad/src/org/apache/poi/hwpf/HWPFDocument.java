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

package org.apache.poi.hwpf;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hwpf.model.*;
import org.apache.poi.hwpf.model.io.HWPFFileSystem;
import org.apache.poi.hwpf.model.io.HWPFOutputStream;
import org.apache.poi.hwpf.usermodel.*;
import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.filesystem.*;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

import java.io.*;
import java.lang.Character.UnicodeBlock;
import java.util.Iterator;


/**
 * This class acts as the bucket that we throw all of the Word data structures
 * into.
 *
 * @author Ryan Ackley
 */
public final class HWPFDocument extends HWPFDocumentCore {
    static final String PROPERTY_PRESERVE_BIN_TABLES = "org.apache.poi.hwpf.preserveBinTables";
    private static final String PROPERTY_PRESERVE_TEXT_TABLE = "org.apache.poi.hwpf.preserveTextTable";

    private static final String STREAM_DATA = "Data";
    private static final String STREAM_TABLE_0 = "0Table";
    private static final String STREAM_TABLE_1 = "1Table";

    /**
     * table stream buffer
     */
    protected byte[] _tableStream;

    /**
     * data stream buffer
     */
    protected byte[] _dataStream;

    /**
     * Document wide Properties
     */
    protected DocumentProperties _dop;

    /**
     * Contains text of the document wrapped in a obfuscated Word data
     * structure
     */
    protected ComplexFileTable _cft;

    /**
     * Contains text buffer linked directly to single-piece document text piece
     */
    protected StringBuilder _text;

    /**
     * Holds the save history for this document.
     */
    protected SavedByTable _sbt;

    /**
     * Holds the revision mark authors for this document.
     */
    protected RevisionMarkAuthorTable _rmat;
    /**
     * Escher Drawing Group information
     */
    protected EscherRecordHolder _escherRecordHolder;
    /**
     * Holds pictures table
     */
    protected PicturesTable _pictures;
    /**
     * Holds Office Art objects
     */
    @Deprecated
    protected ShapesTable _officeArts;
    /**
     * Holds Office Art objects
     */
    protected OfficeDrawingsImpl _officeDrawingsHeaders;
    /**
     * Holds Office Art objects
     */
    protected OfficeDrawingsImpl _officeDrawingsMain;
    /**
     * Holds the bookmarks tables
     */
    protected BookmarksTables _bookmarksTables;
    /**
     * Holds the bookmarks
     */
    protected Bookmarks _bookmarks;
    /**
     * Holds the ending notes tables
     */
    protected NotesTables _endnotesTables = new NotesTables(NoteType.ENDNOTE);
    /**
     * Holds the footnotes
     */
    protected Notes _endnotes = new NotesImpl(_endnotesTables);
    /**
     * Holds the footnotes tables
     */
    protected NotesTables _footnotesTables = new NotesTables(NoteType.FOOTNOTE);
    /**
     * Holds the footnotes
     */
    protected Notes _footnotes = new NotesImpl(_footnotesTables);
    /**
     * Holds the fields PLCFs
     */
    protected FieldsTables _fieldsTables;
    /**
     * Holds the fields
     */
    protected Fields _fields;
    /**
     * Holds FSBA (shape) information
     */
    private FSPATable _fspaHeaders;
    /**
     * Holds FSBA (shape) information
     */
    private FSPATable _fspaMain;

    protected HWPFDocument() {
        super();
        this._text = new StringBuilder("\r");
    }

    /**
     * This constructor loads a Word document from an InputStream.
     *
     * @param istream The InputStream that contains the Word document.
     * @throws IOException If there is an unexpected IOException from the passed
     *                     in InputStream.
     */
    public HWPFDocument(InputStream istream) throws IOException {
        //do Ole stuff
        this(verifyAndBuildPOIFS(istream));
    }

    /**
     * This constructor loads a Word document from a POIFSFileSystem
     *
     * @param pFileSystem The POIFSFileSystem that contains the Word document.
     * @throws IOException If there is an unexpected IOException from the passed
     *                     in POIFSFileSystem.
     */
    public HWPFDocument(POIFSFileSystem pFileSystem) throws IOException {
        this(pFileSystem.getRoot());
    }

    @Override
    public void write() throws IOException {

    }

    @Override
    public void write(File newFile) throws IOException {

    }

    /**
     * @param pFileSystem The POIFSFileSystem that contains the encrpted Word document.
     * @throws Exception
     * @author TaijiWSwangds
     * This constructor loads a Word document from a encrypted POIFSFileSystem
     */
    public HWPFDocument(POIFSFileSystem pFileSystem, String password) throws Exception {
        this(pFileSystem.getRoot(), password);
    }

    /**
     * This constructor loads a Word document from a specific point
     * in a POIFSFileSystem, probably not the default.
     * Used typically to open embedded documents.
     *
     * @param pFileSystem The POIFSFileSystem that contains the Word document.
     * @throws IOException If there is an unexpected IOException from the passed
     *                     in POIFSFileSystem.
     * @deprecated Use {@link #HWPFDocument(DirectoryNode)} instead
     */
    @Deprecated
    public HWPFDocument(DirectoryNode directory, POIFSFileSystem pFileSystem) throws IOException {
        this(directory);
    }

    /**
     * This constructor loads a Word document from a specific point
     * in a POIFSFileSystem, probably not the default.
     * Used typically to open embeded documents.
     *
     * @param directory The DirectoryNode that contains the Word document.
     * @throws IOException If there is an unexpected IOException from the passed
     *                     in POIFSFileSystem.
     */
    public HWPFDocument(DirectoryNode directory) throws IOException {
        // Load the main stream and FIB
        // Also handles HPSF bits
        super(directory);

        // Is this document too old for us?
        if (_fib.getFibBase().getNFib() < 106) {
            throw new OldWordFileFormatException("The document is too old - Word 95 or older. Try HWPFOldDocument instead?");
        }

        // use the fib to determine the name of the table stream.
        String name = STREAM_TABLE_0;
        if (_fib.getFibBase().isFWhichTblStm()) {
            name = STREAM_TABLE_1;
        }
        // Grab the table stream.
        DocumentEntry tableProps;
        try {
            tableProps =
                    (DocumentEntry) directory.getEntry(name);
        } catch (FileNotFoundException fnfe) {
            throw new IllegalStateException("Table Stream '" + name + "' wasn't found - Either the document is corrupt, or is Word95 (or earlier)");
        }

        // read in the table stream.
        _tableStream = new byte[tableProps.getSize()];
        directory.createDocumentInputStream(name).read(_tableStream);

        _fib.fillVariableFields(_mainStream, _tableStream);

        // read in the data stream.
        try {
            DocumentEntry dataProps =
                    (DocumentEntry) directory.getEntry(STREAM_DATA);
            _dataStream = new byte[dataProps.getSize()];
            directory.createDocumentInputStream(STREAM_DATA).read(_dataStream);
        } catch (FileNotFoundException e) {
            _dataStream = new byte[0];
        }

        // Get the cp of the start of text in the main stream
        // The latest spec doc says this is always zero!
        int fcMin = 0;
        //fcMin = _fib.getFcMin()

        // Start to load up our standard structures.

        _dop = new DocumentProperties(_tableStream, _fib.getFcDop(), _fib.getLcbDop());
        _cft = new ComplexFileTable(_mainStream, _tableStream, _fib.getFcClx(), fcMin);
        TextPieceTable _tpt = _cft.getTextPieceTable();

        // Now load the rest of the properties, which need to be adjusted
        //  for where text really begin
        _cbt = new CHPBinTable(_mainStream, _tableStream, _fib.getFcPlcfbteChpx(), _fib.getLcbPlcfbteChpx(), _tpt);
        _pbt = new PAPBinTable(_mainStream, _tableStream, _dataStream, _fib.getFcPlcfbtePapx(), _fib.getLcbPlcfbtePapx(), _tpt);

        _text = _tpt.getText();

        /*
         * in this mode we preserving PAPX/CHPX structure from file, so text may
         * miss from output, and text order may be corrupted
         */
        boolean preserveBinTables = false;
        try {
            preserveBinTables = Boolean.parseBoolean(System
                    .getProperty(PROPERTY_PRESERVE_BIN_TABLES));
        } catch (Exception exc) {
            // ignore;
        }

        if (!preserveBinTables) {
            _cbt.rebuild(_cft);
            _pbt.rebuild(_text, _cft);
        }

        /*
         * Property to disable text rebuilding. In this mode changing the text
         * will lead to unpredictable behavior
         */
        boolean preserveTextTable = false;
        try {
            preserveTextTable = Boolean.parseBoolean(System
                    .getProperty(PROPERTY_PRESERVE_TEXT_TABLE));
        } catch (Exception exc) {
            // ignore;
        }
        if (!preserveTextTable) {
            _cft = new ComplexFileTable();
            _tpt = _cft.getTextPieceTable();
            final TextPiece textPiece = new SinglentonTextPiece(_text);
            _tpt.add(textPiece);
            _text = textPiece.getStringBuilder();
        }

        // Read FSPA and Escher information
        // _fspa = new FSPATable(_tableStream, _fib.getFcPlcspaMom(),
        // _fib.getLcbPlcspaMom(), getTextTable().getTextPieces());
        _fspaHeaders = new FSPATable(_tableStream, _fib,
                FSPADocumentPart.HEADER);
        _fspaMain = new FSPATable(_tableStream, _fib, FSPADocumentPart.MAIN);

        if (_fib.getFcDggInfo() != 0) {
            _escherRecordHolder = new EscherRecordHolder(_tableStream, _fib.getFcDggInfo(), _fib.getLcbDggInfo());
        } else {
            _escherRecordHolder = new EscherRecordHolder();
        }

        // read in the pictures stream
        _pictures = new PicturesTable(this, _dataStream, _mainStream, _fspaMain, _escherRecordHolder);
        // And the art shapes stream
        _officeArts = new ShapesTable(_tableStream, _fib);

        // And escher pictures
        _officeDrawingsHeaders = new OfficeDrawingsImpl(_fspaHeaders, _escherRecordHolder, _mainStream);
        _officeDrawingsMain = new OfficeDrawingsImpl(_fspaMain, _escherRecordHolder, _mainStream);

        _st = new SectionTable(_mainStream, _tableStream, _fib.getFcPlcfsed(), _fib.getLcbPlcfsed(), fcMin, _tpt, _fib.getSubdocumentTextStreamLength(SubdocumentType.MAIN));
        _ss = new StyleSheet(_tableStream, _fib.getFcStshf());
        _ft = new FontTable(_tableStream, _fib.getFcSttbfffn(), _fib.getLcbSttbfffn());

        int listOffset = _fib.getFcPlfLst();
        int lfoOffset = _fib.getFcPlfLfo();
        if (listOffset != 0 && _fib.getLcbPlfLst() != 0) {
            _lt = new ListTables(_tableStream, listOffset, _fib.getFcPlfLfo(),
                    _fib.getLcbPlfLfo());
        }

        int sbtOffset = _fib.getFcSttbSavedBy();
        int sbtLength = _fib.getLcbSttbSavedBy();
        if (sbtOffset != 0 && sbtLength != 0) {
            _sbt = new SavedByTable(_tableStream, sbtOffset, sbtLength);
        }

        int rmarkOffset = _fib.getFcSttbfRMark();
        int rmarkLength = _fib.getLcbSttbfRMark();
        if (rmarkOffset != 0 && rmarkLength != 0) {
            _rmat = new RevisionMarkAuthorTable(_tableStream, rmarkOffset, rmarkLength);
        }

        _bookmarksTables = new BookmarksTables(_tableStream, _fib);
        _bookmarks = new BookmarksImpl(_bookmarksTables);

        _endnotesTables = new NotesTables(NoteType.ENDNOTE, _tableStream, _fib);
        _endnotes = new NotesImpl(_endnotesTables);
        _footnotesTables = new NotesTables(NoteType.FOOTNOTE, _tableStream, _fib);
        _footnotes = new NotesImpl(_footnotesTables);

        _fieldsTables = new FieldsTables(_tableStream, _fib);
        _fields = new FieldsImpl(_fieldsTables);
    }

    public HWPFDocument(DirectoryNode directory, String password) throws Exception {
        // Load the main stream and FIB
        // Also handles HPSF bits
        super(directory, password);
        byte[] test = new byte[0x10];
        RC4 key = new RC4();
        RC4 tool = new RC4();
        int i, j = 0, blk = 0;
        int end;
        byte x;
        byte[] docId = new byte[16];
        byte[] salt = new byte[64];
        byte[] hashedSalt = new byte[16];
        // Is this document too old for us?
        if (_fib.getFibBase().getNFib() < 106) {
            throw new OldWordFileFormatException("The document is too old - Word 95 or older. Try HWPFOldDocument instead?");
        }

        // use the fib to determine the name of the table stream.
        String name = STREAM_TABLE_0;
        if (_fib.getFibBase().isFWhichTblStm()) {
            name = STREAM_TABLE_1;
        }
        // Grab the table stream.
        DocumentEntry tableProps;
        try {
            tableProps =
                    (DocumentEntry) directory.getEntry(name);
        } catch (FileNotFoundException fnfe) {
            throw new IllegalStateException("Table Stream '" + name + "' wasn't found - Either the document is corrupt, or is Word95 (or earlier)");
        }
        // read in the table stream.
        _tableStream = new byte[tableProps.getSize()];
        end = _tableStream.length;
        byte[] outTable = new byte[tableProps.getSize()];
        directory.createDocumentInputStream(name).read(_tableStream);
        for (i = 0; i < 4; i++) {
            x = _tableStream[i];
        }
        for (i = 0; i < 16; i++) {
            docId[i] = _tableStream[i + 4];
        }
        for (i = 0; i < 16; i++) {
            salt[i] = _tableStream[i + 20];
        }
        for (i = 0; i < 16; i++) {
            hashedSalt[i] = _tableStream[i + 36];
        }
        byte[] pArray = expandPW(password);
        MD5 yy = new MD5();
        if (!verifyPWD(pArray, docId, salt, hashedSalt, yy)) {
            throw new PassWordIsWrongException("the word97-03 file's password is wrong ");
        }
        tool.makeKey(0, key, yy);
        int k = 0;
        int m = 0;
        while (j < end) {
            for (i = 0; i < 0x10; i++) {
                if (k + i >= end - 1) {
                    test[i] = 0;
                    continue;
                }
                test[i] = _tableStream[k + i];
            }
            k += 16;
            tool.rc4(test, 0x10, key);
            for (i = 0; i < 0x10; i++) {
                if (m >= end) break;
                outTable[m++] = test[i];
            }
            j += 0x10;
            if ((j % 0x200) == 0) {
                blk++;
                tool.makeKey(blk, key, yy);
            }
        }

        byte[] outMain = new byte[_mainStream.length];
        blk = 0;
        tool.makeKey(blk, key, yy);
        end = _mainStream.length;
        k = 0;
        j = 0;
        m = 0;
        while (j < end) {
            for (i = 0; i < 0x10; i++) {
                if (k + i >= end - 1) {
                    test[i] = 0;
                    continue;
                }
                test[i] = _mainStream[k + i];
            }
            tool.rc4(test, 0x10, key);
            if (k == 0) {
            }
            for (i = 0; i < 0x10; i++) {
                if (m >= end) break;
                outMain[m++] = test[i];
            }
            j += 0x10;
            k += 16;
            if ((j % 0x200) == 0) {
                blk++;
                tool.makeKey(blk, key, yy);
            }
        }

        for (i = 0; i < _mainStream.length; i++) {
            if (i == 11 || i == 12 || i >= outMain.length) outMain[i] = 0;
            _mainStream[i] = outMain[i];
        }
        for (j = 0; j < _tableStream.length; j++) {
            if (j >= outTable.length) _tableStream[j] = 0;
            _tableStream[j] = outTable[j];
        }
        _fib = new FileInformationBlock(_mainStream);
        _fib.fillVariableFields(_mainStream, _tableStream);
        // read in the data stream.
        byte[] dataStream = null;
        try {
            DocumentEntry dataProps =
                    (DocumentEntry) directory.getEntry(STREAM_DATA);
            _dataStream = new byte[dataProps.getSize()];
            dataStream = new byte[dataProps.getSize()];
            directory.createDocumentInputStream(STREAM_DATA).read(_dataStream);
        } catch (FileNotFoundException e) {
            _dataStream = new byte[0];

        }
        assert dataStream != null;
        blk = 0;
        tool.makeKey(blk, key, yy);
        end = _dataStream.length;
        k = 0;
        j = 0;
        m = 0;
        while (j < end) {
            for (i = 0; i < 0x10; i++) {
                if (k + i >= end - 1) {
                    test[i] = 0;
                    continue;
                }
                test[i] = _dataStream[k + i];
            }
            tool.rc4(test, 0x10, key);
            if (k == 0) {
            }
            for (i = 0; i < 0x10; i++) {
                if (m >= end) break;
                dataStream[m++] = test[i];
            }
            j += 0x10;
            k += 16;
            if ((j % 0x200) == 0) {
                blk++;
                tool.makeKey(blk, key, yy);
            }
        }
        for (j = 0; j < dataStream.length; j++) {
            _dataStream[j] = dataStream[j];
        }
        // Get the cp of the start of text in the main stream
        // The latest spec doc says this is always zero!
        int fcMin = _fib.getFibBase().getFcMin();
        //fcMin = _fib.getFcMin();

        // Start to load up our standard structures.
        int mm = LittleEndian.getInt(_mainStream, 0x01A2);


        _dop = new DocumentProperties(_tableStream, _fib.getFcDop(), _fib.getLcbDop());
        _cft = new ComplexFileTable(_mainStream, _tableStream, mm, fcMin);
        TextPieceTable _tpt = _cft.getTextPieceTable();

        // Now load the rest of the properties, which need to be adjusted
        //  for where text really begin

        _cbt = new CHPBinTable(_mainStream, _tableStream, _fib.getFcPlcfbteChpx(), _fib.getLcbPlcfbteChpx(), _tpt);
        _pbt = new PAPBinTable(_mainStream, _tableStream, dataStream, _fib.getFcPlcfbtePapx(), _fib.getLcbPlcfbtePapx(), _tpt);

        _text = _tpt.getText();
        /*
         * in this mode we preserving PAPX/CHPX structure from file, so text may
         * miss from output, and text order may be corrupted
         */
        boolean preserveBinTables = false;
        try {
            preserveBinTables = Boolean.parseBoolean(System
                    .getProperty(PROPERTY_PRESERVE_BIN_TABLES));
        } catch (Exception exc) {
            //exc.printStackTrace();
        }

        if (!preserveBinTables) {
            _cbt.rebuild(_cft);
            _pbt.rebuild(_text, _cft);
        }

        /*
         * Property to disable text rebuilding. In this mode changing the text
         * will lead to unpredictable behavior
         */
        boolean preserveTextTable = false;
        try {
            preserveTextTable = Boolean.parseBoolean(System
                    .getProperty(PROPERTY_PRESERVE_TEXT_TABLE));
        } catch (Exception exc) {
            // ignore;
        }
        if (!preserveTextTable) {
            _cft = new ComplexFileTable();
            _tpt = _cft.getTextPieceTable();
            final TextPiece textPiece = new SinglentonTextPiece(_text);
            _tpt.add(textPiece);
            _text = textPiece.getStringBuilder();
        }

        // Read FSPA and Escher information
        // _fspa = new FSPATable(_tableStream, _fib.getFcPlcspaMom(),
        // _fib.getLcbPlcspaMom(), getTextTable().getTextPieces());
        _fspaHeaders = new FSPATable(_tableStream, _fib,
                FSPADocumentPart.HEADER);
        _fspaMain = new FSPATable(_tableStream, _fib, FSPADocumentPart.MAIN);

        if (_fib.getFcDggInfo() != 0) {
            _escherRecordHolder = new EscherRecordHolder(_tableStream, _fib.getFcDggInfo(), _fib.getLcbDggInfo());
        } else {
            _escherRecordHolder = new EscherRecordHolder();
        }

        // read in the pictures stream
        _pictures = new PicturesTable(this, dataStream, _mainStream, _fspaMain, _escherRecordHolder);
        // And the art shapes stream
        _officeArts = new ShapesTable(_tableStream, _fib);

        // And escher pictures
        _officeDrawingsHeaders = new OfficeDrawingsImpl(_fspaHeaders, _escherRecordHolder, _mainStream);
        _officeDrawingsMain = new OfficeDrawingsImpl(_fspaMain, _escherRecordHolder, _mainStream);

        _st = new SectionTable(_mainStream, _tableStream, _fib.getFcPlcfsed(), _fib.getLcbPlcfsed(), fcMin, _tpt, _fib.getSubdocumentTextStreamLength(SubdocumentType.MAIN));
        _ss = new StyleSheet(_tableStream, _fib.getFcStshf());
        _ft = new FontTable(_tableStream, _fib.getFcSttbfffn(), _fib.getLcbSttbfffn());
        int listOffset = LittleEndian.getInt(_mainStream, 0x2E2);// _fib.getFcPlfLst();
        int lfoOffset = LittleEndian.getInt(_mainStream, 0x2EA); //_fib.getFcPlfLfo();
        if (listOffset != 0 && _fib.getLcbPlfLst() != 0) {
            _lt = new ListTables(_tableStream, listOffset, lfoOffset, LittleEndian.getInt(_mainStream, 0x2EE)
                    //_fib.getLcbPlfLfo()
            );
        }

        int sbtOffset = _fib.getFcSttbSavedBy();
        int sbtLength = _fib.getLcbSttbSavedBy();
        if (sbtOffset != 0 && sbtLength != 0) {
            _sbt = new SavedByTable(_tableStream, sbtOffset, sbtLength);
        }

        int rmarkOffset = _fib.getFcSttbfRMark();
        int rmarkLength = _fib.getLcbSttbfRMark();
        if (rmarkOffset != 0 && rmarkLength != 0) {
            _rmat = new RevisionMarkAuthorTable(_tableStream, rmarkOffset, rmarkLength);
        }

        _bookmarksTables = new BookmarksTables(_tableStream, _fib);
        _bookmarks = new BookmarksImpl(_bookmarksTables);

        _endnotesTables = new NotesTables(NoteType.ENDNOTE, _tableStream, _fib);
        _endnotes = new NotesImpl(_endnotesTables);
        _footnotesTables = new NotesTables(NoteType.FOOTNOTE, _tableStream, _fib);
        _footnotes = new NotesImpl(_footnotesTables);

        _fieldsTables = new FieldsTables(_tableStream, _fib);
        _fields = new FieldsImpl(_fieldsTables);
    }

    public static String utf8ToUnicode(String inStr) {
        char[] myBuffer = inStr.toCharArray();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < inStr.length(); i++) {
            UnicodeBlock ub = UnicodeBlock.of(myBuffer[i]);
            if (ub == UnicodeBlock.BASIC_LATIN) {
                sb.append(myBuffer[i]);
            } else if (ub == UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
                int j = (int) myBuffer[i] - 65248;
                sb.append((char) j);
            } else {
                short s = (short) myBuffer[i];
                String hexS = Integer.toHexString(s);
                String unicode = "\\u" + hexS;
                sb.append(unicode.toLowerCase());
            }
        }
        return sb.toString();
    }

    public short[] wvSetPassword(String password) throws Exception {
        short[] tt = new short[16];
        char[] temp = utf8ToUnicode(password).toCharArray();
        for (int i = 0; i < temp.length; i++) {
            if (i == 16) break;
            tt[i] = (short) temp[i];
        }
        return tt;
    }

    private boolean verifyPWD(byte pwArray[], byte docId[], byte salt[], byte hashedSalt[], MD5 valContext) {
        RC4 tool = new RC4();
        RC4 key = new RC4();
        int offset = 0, keyOffset = 0;
        int toCopy = 5;
        MD5 md5 = new MD5();
        MD5 md51 = new MD5();
        md5.md5Init();
        md5.md5Update(pwArray, 64);
        md5.getMD5StoreDigest(md5);
        valContext.md5Init();
        while (offset != 16) {
            if ((64 - offset) < 5)
                toCopy = 64 - offset;
            System.arraycopy(md5.digest, keyOffset, pwArray, offset, toCopy);
            offset += toCopy;

            if (offset == 64) {
                valContext.md5Update(pwArray, 64);
                keyOffset = toCopy;
                toCopy = 5 - toCopy;
                offset = 0;
                continue;
            }
            keyOffset = 0;
            toCopy = 5;
            System.arraycopy(docId, 0, pwArray, offset, 16);
            offset += 16;
        }
        pwArray[16] = (byte) 0x80;
        for (int i = 0; i < 47; i++) {
            pwArray[17 + i] = 0;
        }
        pwArray[56] = (byte) 0x80;
        pwArray[57] = (byte) 0x0A;
        valContext.md5Update(pwArray, 64);
        valContext.getMD5StoreDigest(valContext);
        tool.makeKey(0, key, valContext);
        tool.rc4(salt, 16, key);
        tool.rc4(hashedSalt, 16, key);
        salt[16] = (byte) 0x80;
        for (int i = 0; i < 47; i++) salt[17 + i] = 0;
        salt[56] = (byte) 0x80;
        md51.md5Init();
        md51.md5Update(salt, 64);
        md51.getMD5StoreDigest(md51);
        for (int i = 0; i < 16; i++) {
            if (hashedSalt[i] != md51.digest[i]) return false;
        }
        return true;
    }

    private byte[] expandPW(String password) {
        int i;
        byte[] pwArray = new byte[64];
        for (i = 0; i < 64; i++)
            pwArray[i] = 0;
        i = 0;
        char[] passwordArray = new char[16];
        char[] temp = utf8ToUnicode(password).toCharArray();
        for (int j = 0; j < 16; j++) {
            if (j < temp.length) passwordArray[j] = temp[j];
            else passwordArray[j] = 0;
        }
        while (passwordArray[i] != 0) {
            pwArray[(2 * i)] = (byte) (passwordArray[i] & 0xff);
            pwArray[(2 * i) + 1] = (byte) ((passwordArray[i] << 8) & 0xff);
            i++;
        }
        pwArray[2 * i] = (byte) 0x80;
        pwArray[56] = (byte) (i << 4);
        return pwArray;
    }

    @Internal
    public TextPieceTable getTextTable() {
        return _cft.getTextPieceTable();
    }

    @Internal
    @Override
    public StringBuilder getText() {
        return _text;
    }

    public DocumentProperties getDocProperties() {
        return _dop;
    }

    public Range getOverallRange() {
        return new Range(0, _text.length(), this);
    }

    /**
     * Returns the range which covers the whole of the document, but excludes
     * any headers and footers.
     */
    public Range getRange() {
        // // First up, trigger a full-recalculate
        // // Needed in case of deletes etc
        // getOverallRange();
        //
        // if ( getFileInformationBlock().isFComplex() )
        // {
        // /*
        // * Page 31:
        // *
        // * main document must be found by examining the piece table entries
        // * from the 0th piece table entry from the piece table entry that
        // * describes cp=fib.ccpText.
        // */
        // // TODO: review
        // return new Range( _cpSplit.getMainDocumentStart(),
        // _cpSplit.getMainDocumentEnd(), this );
        // }
        //
        // /*
        // * Page 31:
        // *
        // * "In a non-complex file, this means text of the: main document
        // begins
        // * at fib.fcMin in the file and continues through
        // * fib.fcMin+fib.ccpText."
        // */
        // int bytesStart = getFileInformationBlock().getFcMin();
        //
        // int charsStart = getTextTable().getCharIndex( bytesStart );
        // int charsEnd = charsStart
        // + getFileInformationBlock().getSubdocumentTextStreamLength(
        // SubdocumentType.MAIN );

        // it seems much simpler -- sergey
        return getRange(SubdocumentType.MAIN);
    }

    private Range getRange(SubdocumentType subdocument) {
        int startCp = 0;
        for (SubdocumentType previos : SubdocumentType.ORDERED) {
            int length = getFileInformationBlock()
                    .getSubdocumentTextStreamLength(previos);
            if (subdocument == previos)
                return new Range(startCp, startCp + length, this);
            startCp += length;
        }
        throw new UnsupportedOperationException(
                "Subdocument type not supported: " + subdocument);
    }

    /**
     * Returns the {@link Range} which covers all the Footnotes.
     *
     * @return the {@link Range} which covers all the Footnotes.
     */
    public Range getFootnoteRange() {
        return getRange(SubdocumentType.FOOTNOTE);
    }

    /**
     * Returns the {@link Range} which covers all endnotes.
     *
     * @return the {@link Range} which covers all endnotes.
     */
    public Range getEndnoteRange() {
        return getRange(SubdocumentType.ENDNOTE);
    }

    /**
     * Returns the {@link Range} which covers all annotations.
     *
     * @return the {@link Range} which covers all annotations.
     */
    public Range getCommentsRange() {
        return getRange(SubdocumentType.ANNOTATION);
    }

    /**
     * Returns the {@link Range} which covers all textboxes.
     *
     * @return the {@link Range} which covers all textboxes.
     */
    public Range getMainTextboxRange() {
        return getRange(SubdocumentType.TEXTBOX);
    }

    /**
     * Returns the range which covers all "Header Stories".
     * A header story contains a header, footer, end note
     * separators and footnote separators.
     */
    public Range getHeaderStoryRange() {
        return getRange(SubdocumentType.HEADER);
    }

    /**
     * Returns the character length of a document.
     *
     * @return the character length of a document
     */
    public int characterLength() {
        return _text.length();
    }

    /**
     * Gets a reference to the saved -by table, which holds the save history for the document.
     *
     * @return the saved-by table.
     */
    @Internal
    public SavedByTable getSavedByTable() {
        return _sbt;
    }

    /**
     * Gets a reference to the revision mark author table, which holds the revision mark authors for the document.
     *
     * @return the saved-by table.
     */
    @Internal
    public RevisionMarkAuthorTable getRevisionMarkAuthorTable() {
        return _rmat;
    }

    /**
     * @return PicturesTable object, that is able to extract images from this document
     */
    public PicturesTable getPicturesTable() {
        return _pictures;
    }

    @Internal
    public EscherRecordHolder getEscherRecordHolder() {
        return _escherRecordHolder;
    }

    /**
     * @return ShapesTable object, that is able to extract office are shapes
     * from this document
     * @deprecated use {@link #getOfficeDrawingsMain()} instead
     */
    @Deprecated
    @Internal
    public ShapesTable getShapesTable() {
        return _officeArts;
    }

    public OfficeDrawings getOfficeDrawingsHeaders() {
        return _officeDrawingsHeaders;
    }

    public OfficeDrawings getOfficeDrawingsMain() {
        return _officeDrawingsMain;
    }

    /**
     * @return user-friendly interface to access document bookmarks
     */
    public Bookmarks getBookmarks() {
        return _bookmarks;
    }

    /**
     * @return user-friendly interface to access document endnotes
     */
    public Notes getEndnotes() {
        return _endnotes;
    }

    /**
     * @return user-friendly interface to access document footnotes
     */
    public Notes getFootnotes() {
        return _footnotes;
    }

    /**
     * @return FieldsTables object, that is able to extract fields descriptors from this document
     * @deprecated
     */
    @Deprecated
    @Internal
    public FieldsTables getFieldsTables() {
        return _fieldsTables;
    }

    /**
     * Returns user-friendly interface to access document {@link Field}s
     *
     * @return user-friendly interface to access document {@link Field}s
     */
    public Fields getFields() {
        return _fields;
    }

    /**
     * Writes out the word file that is represented by an instance of this class.
     *
     * @param out The OutputStream to write to.
     * @throws IOException If there is an unexpected IOException from the passed
     *                     in OutputStream.
     */
    public void write(OutputStream out)
            throws IOException {
        // initialize our streams for writing.
        HWPFFileSystem docSys = new HWPFFileSystem();
        HWPFOutputStream wordDocumentStream = docSys.getStream(STREAM_WORD_DOCUMENT);
        HWPFOutputStream tableStream = docSys.getStream(STREAM_TABLE_1);
        //HWPFOutputStream dataStream = docSys.getStream("Data");
        int tableOffset = 0;

        // FileInformationBlock fib = (FileInformationBlock)_fib.clone();
        // clear the offsets and sizes in our FileInformationBlock.
        _fib.clearOffsetsSizes();

        // determine the FileInformationBLock size
        int fibSize = _fib.getSize();
        fibSize += POIFSConstants.SMALLER_BIG_BLOCK_SIZE -
                (fibSize % POIFSConstants.SMALLER_BIG_BLOCK_SIZE);

        // preserve space for the FileInformationBlock because we will be writing
        // it after we write everything else.
        byte[] placeHolder = new byte[fibSize];
        wordDocumentStream.write(placeHolder);
        int mainOffset = wordDocumentStream.getOffset();

        // write out the StyleSheet.
        _fib.setFcStshf(tableOffset);
        _ss.writeTo(tableStream);
        _fib.setLcbStshf(tableStream.getOffset() - tableOffset);
        tableOffset = tableStream.getOffset();

        // get fcMin and fcMac because we will be writing the actual text with the
        // complex table.

        /*
         * clx (encoding of the sprm lists for a complex file and piece table
         * for a any file) Written immediately after the end of the previously
         * recorded structure. This is recorded in all Word documents
         * 
         * Microsoft Office Word 97-2007 Binary File Format (.doc)
         * Specification; Page 23 of 210
         */

        // write out the Complex table, includes text.
        _fib.setFcClx(tableOffset);
        _cft.writeTo(wordDocumentStream, tableStream);
        _fib.setLcbClx(tableStream.getOffset() - tableOffset);
        tableOffset = tableStream.getOffset();
        int fcMac = wordDocumentStream.getOffset();

        /*
         * dop (document properties record) Written immediately after the end of
         * the previously recorded structure. This is recorded in all Word
         * documents
         * 
         * Microsoft Office Word 97-2007 Binary File Format (.doc)
         * Specification; Page 23 of 210
         */

        // write out the DocumentProperties.
        _fib.setFcDop(tableOffset);
        _dop.writeTo(tableStream);
        _fib.setLcbDop(tableStream.getOffset() - tableOffset);
        tableOffset = tableStream.getOffset();

        /*
         * plcfBkmkf (table recording beginning CPs of bookmarks) Written
         * immediately after the sttbfBkmk, if the document contains bookmarks.
         * 
         * Microsoft Office Word 97-2007 Binary File Format (.doc)
         * Specification; Page 24 of 210
         */
        if (_bookmarksTables != null) {
            _bookmarksTables.writePlcfBkmkf(_fib, tableStream);
            tableOffset = tableStream.getOffset();
        }

        /*
         * plcfBkmkl (table recording limit CPs of bookmarks) Written
         * immediately after the plcfBkmkf, if the document contains bookmarks.
         * 
         * Microsoft Office Word 97-2007 Binary File Format (.doc)
         * Specification; Page 24 of 210
         */
        if (_bookmarksTables != null) {
            _bookmarksTables.writePlcfBkmkl(_fib, tableStream);
            tableOffset = tableStream.getOffset();
        }

        /*
         * plcfbteChpx (bin table for CHP FKPs) Written immediately after the
         * previously recorded table. This is recorded in all Word documents.
         * 
         * Microsoft Office Word 97-2007 Binary File Format (.doc)
         * Specification; Page 24 of 210
         */

        // write out the CHPBinTable.
        _fib.setFcPlcfbteChpx(tableOffset);
        _cbt.writeTo(wordDocumentStream, tableStream, mainOffset, _cft.getTextPieceTable());
        _fib.setLcbPlcfbteChpx(tableStream.getOffset() - tableOffset);
        tableOffset = tableStream.getOffset();

        /*
         * plcfbtePapx (bin table for PAP FKPs) Written immediately after the
         * plcfbteChpx. This is recorded in all Word documents.
         * 
         * Microsoft Office Word 97-2007 Binary File Format (.doc)
         * Specification; Page 24 of 210
         */

        // write out the PAPBinTable.
        _fib.setFcPlcfbtePapx(tableOffset);
        _pbt.writeTo(wordDocumentStream, tableStream, _cft.getTextPieceTable());
        _fib.setLcbPlcfbtePapx(tableStream.getOffset() - tableOffset);
        tableOffset = tableStream.getOffset();

        /*
         * plcfendRef (endnote reference position table) Written immediately
         * after the previously recorded table if the document contains endnotes
         * 
         * plcfendTxt (endnote text position table) Written immediately after
         * the plcfendRef if the document contains endnotes
         * 
         * Microsoft Office Word 97-2007 Binary File Format (.doc)
         * Specification; Page 24 of 210
         */
        _endnotesTables.writeRef(_fib, tableStream);
        _endnotesTables.writeTxt(_fib, tableStream);
        tableOffset = tableStream.getOffset();

    /*
     * plcffld*** (table of field positions and statuses for annotation
     * subdocument) Written immediately after the previously recorded table,
     * if the ******* subdocument contains fields.
     * 
     * Microsoft Office Word 97-2007 Binary File Format (.doc)
     * Specification; Page 24 of 210
     */

        if (_fieldsTables != null) {
            _fieldsTables.write(_fib, tableStream);
            tableOffset = tableStream.getOffset();
        }

        /*
         * plcffndRef (footnote reference position table) Written immediately
         * after the stsh if the document contains footnotes
         * 
         * plcffndTxt (footnote text position table) Written immediately after
         * the plcffndRef if the document contains footnotes
         * 
         * Microsoft Office Word 97-2007 Binary File Format (.doc)
         * Specification; Page 24 of 210
         */
        _footnotesTables.writeRef(_fib, tableStream);
        _footnotesTables.writeTxt(_fib, tableStream);
        tableOffset = tableStream.getOffset();

        /*
         * plcfsed (section table) Written immediately after the previously
         * recorded table. Recorded in all Word documents
         * 
         * Microsoft Office Word 97-2007 Binary File Format (.doc)
         * Specification; Page 25 of 210
         */

        // write out the SectionTable.
        _fib.setFcPlcfsed(tableOffset);
        _st.writeTo(wordDocumentStream, tableStream);
        _fib.setLcbPlcfsed(tableStream.getOffset() - tableOffset);
        tableOffset = tableStream.getOffset();

        // write out the list tables
        if (_lt != null) {
            /*
             * plcflst (list formats) Written immediately after the end of the
             * previously recorded, if there are any lists defined in the
             * document. This begins with a short count of LSTF structures
             * followed by those LSTF structures. This is immediately followed
             * by the allocated data hanging off the LSTFs. This data consists
             * of the array of LVLs for each LSTF. (Each LVL consists of an LVLF
             * followed by two grpprls and an XST.)
             * 
             * Microsoft Office Word 97-2007 Binary File Format (.doc)
             * Specification; Page 25 of 210
             */
            _lt.writeListDataTo(_fib, tableStream);
            tableOffset = tableStream.getOffset();

            /*
             * plflfo (more list formats) Written immediately after the end of
             * the plcflst and its accompanying data, if there are any lists
             * defined in the document. This consists first of a PL of LFO
             * records, followed by the allocated data (if any) hanging off the
             * LFOs. The allocated data consists of the array of LFOLVLFs for
             * each LFO (and each LFOLVLF is immediately followed by some LVLs).
             * 
             * Microsoft Office Word 97-2007 Binary File Format (.doc)
             * Specification; Page 26 of 210
             */
            _lt.writeListOverridesTo(_fib, tableStream);
            tableOffset = tableStream.getOffset();
        }

        /*
         * sttbfBkmk (table of bookmark name strings) Written immediately after
         * the previously recorded table, if the document contains bookmarks.
         * 
         * Microsoft Office Word 97-2007 Binary File Format (.doc)
         * Specification; Page 27 of 210
         */
        if (_bookmarksTables != null) {
            _bookmarksTables.writeSttbfBkmk(_fib, tableStream);
            tableOffset = tableStream.getOffset();
        }

        /*
         * sttbSavedBy (last saved by string table) Written immediately after
         * the previously recorded table.
         * 
         * Microsoft Office Word 97-2007 Binary File Format (.doc)
         * Specification; Page 27 of 210
         */

        // write out the saved-by table.
        if (_sbt != null) {
            _fib.setFcSttbSavedBy(tableOffset);
            _sbt.writeTo(tableStream);
            _fib.setLcbSttbSavedBy(tableStream.getOffset() - tableOffset);

            tableOffset = tableStream.getOffset();
        }

        // write out the revision mark authors table.
        if (_rmat != null) {
            _fib.setFcSttbfRMark(tableOffset);
            _rmat.writeTo(tableStream);
            _fib.setLcbSttbfRMark(tableStream.getOffset() - tableOffset);

            tableOffset = tableStream.getOffset();
        }

        // write out the FontTable.
        _fib.setFcSttbfffn(tableOffset);
        _ft.writeTo(tableStream);
        _fib.setLcbSttbfffn(tableStream.getOffset() - tableOffset);
        tableOffset = tableStream.getOffset();

        // set some variables in the FileInformationBlock.
        _fib.getFibBase().setFcMin(mainOffset);
        _fib.getFibBase().setFcMac(fcMac);
        _fib.setCbMac(wordDocumentStream.getOffset());

        // make sure that the table, doc and data streams use big blocks.
        byte[] mainBuf = wordDocumentStream.toByteArray();
        if (mainBuf.length < 4096) {
            byte[] tempBuf = new byte[4096];
            System.arraycopy(mainBuf, 0, tempBuf, 0, mainBuf.length);
            mainBuf = tempBuf;
        }

        // Table1 stream will be used
        _fib.getFibBase().setFWhichTblStm(true);

        // write out the FileInformationBlock.
        //_fib.serialize(mainBuf, 0);
        _fib.writeTo(mainBuf, tableStream);

        byte[] tableBuf = tableStream.toByteArray();
        if (tableBuf.length < 4096) {
            byte[] tempBuf = new byte[4096];
            System.arraycopy(tableBuf, 0, tempBuf, 0, tableBuf.length);
            tableBuf = tempBuf;
        }

        byte[] dataBuf = _dataStream;
        if (dataBuf == null) {
            dataBuf = new byte[4096];
        }
        if (dataBuf.length < 4096) {
            byte[] tempBuf = new byte[4096];
            System.arraycopy(dataBuf, 0, tempBuf, 0, dataBuf.length);
            dataBuf = tempBuf;
        }

        // create new document preserving order of entries
        POIFSFileSystem pfs = new POIFSFileSystem();
        boolean docWritten = false;
        boolean dataWritten = false;
        boolean objectPoolWritten = false;
        boolean tableWritten = false;
        boolean propertiesWritten = false;
        for (Iterator<Entry> iter = directory.getEntries(); iter.hasNext(); ) {
            Entry entry = iter.next();
            if (entry.getName().equals(STREAM_WORD_DOCUMENT)) {
                if (!docWritten) {
                    pfs.createDocument(new ByteArrayInputStream(mainBuf),
                            STREAM_WORD_DOCUMENT);
                    docWritten = true;
                }
            } else if (entry.getName().equals(STREAM_OBJECT_POOL)) {
                if (!objectPoolWritten) {
                    _objectPool.writeTo(pfs.getRoot());
                    objectPoolWritten = true;
                }
            } else if (entry.getName().equals(STREAM_TABLE_0)
                    || entry.getName().equals(STREAM_TABLE_1)) {
                if (!tableWritten) {
                    pfs.createDocument(new ByteArrayInputStream(tableBuf),
                            STREAM_TABLE_1);
                    tableWritten = true;
                }
            } else if (entry.getName().equals(
                    SummaryInformation.DEFAULT_STREAM_NAME)
                    || entry.getName().equals(
                    DocumentSummaryInformation.DEFAULT_STREAM_NAME)) {
                if (!propertiesWritten) {
                    writeProperties(pfs);
                    propertiesWritten = true;
                }
            } else if (entry.getName().equals(STREAM_DATA)) {
                if (!dataWritten) {
                    pfs.createDocument(new ByteArrayInputStream(dataBuf),
                            STREAM_DATA);
                    dataWritten = true;
                }
            } else {
                EntryUtils.copyNodeRecursively(entry, pfs.getRoot());
            }
        }

        if (!docWritten)
            pfs.createDocument(new ByteArrayInputStream(mainBuf),
                    STREAM_WORD_DOCUMENT);
        if (!tableWritten)
            pfs.createDocument(new ByteArrayInputStream(tableBuf),
                    STREAM_TABLE_1);
        if (!propertiesWritten)
            writeProperties(pfs);
        if (!dataWritten)
            pfs.createDocument(new ByteArrayInputStream(dataBuf),
                    STREAM_DATA);
        if (!objectPoolWritten)
            _objectPool.writeTo(pfs.getRoot());

        pfs.writeFilesystem(out);
        this.directory = pfs.getRoot();

        /*
         * since we updated all references in FIB and etc, using new arrays to
         * access data
         */
        this.directory = pfs.getRoot();
        this._tableStream = tableStream.toByteArray();
        this._dataStream = dataBuf;
    }

    @Internal
    public byte[] getDataStream() {
        return _dataStream;
    }

    @Internal
    public byte[] getTableStream() {
        return _tableStream;
    }

    public int registerList(HWPFList list) {
        if (_lt == null) {
            _lt = new ListTables();
        }
        return _lt.addList(list.getListData(), list.getLFO(),
                list.getLFOData());
    }

    public void delete(int start, int length) {
        Range r = new Range(start, start + length, this);
        r.delete();
    }
}
