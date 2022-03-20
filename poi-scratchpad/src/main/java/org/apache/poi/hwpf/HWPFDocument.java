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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hwpf.model.BookmarksTables;
import org.apache.poi.hwpf.model.CHPBinTable;
import org.apache.poi.hwpf.model.ComplexFileTable;
import org.apache.poi.hwpf.model.DocumentProperties;
import org.apache.poi.hwpf.model.FSPADocumentPart;
import org.apache.poi.hwpf.model.FSPATable;
import org.apache.poi.hwpf.model.FieldsTables;
import org.apache.poi.hwpf.model.FontTable;
import org.apache.poi.hwpf.model.ListTables;
import org.apache.poi.hwpf.model.NoteType;
import org.apache.poi.hwpf.model.NotesTables;
import org.apache.poi.hwpf.model.OfficeArtContent;
import org.apache.poi.hwpf.model.PAPBinTable;
import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.hwpf.model.RevisionMarkAuthorTable;
import org.apache.poi.hwpf.model.SavedByTable;
import org.apache.poi.hwpf.model.SectionTable;
import org.apache.poi.hwpf.model.SinglentonTextPiece;
import org.apache.poi.hwpf.model.StyleSheet;
import org.apache.poi.hwpf.model.SubdocumentType;
import org.apache.poi.hwpf.model.TextPiece;
import org.apache.poi.hwpf.model.TextPieceTable;
import org.apache.poi.hwpf.model.io.HWPFFileSystem;
import org.apache.poi.hwpf.usermodel.Bookmarks;
import org.apache.poi.hwpf.usermodel.BookmarksImpl;
import org.apache.poi.hwpf.usermodel.Field;
import org.apache.poi.hwpf.usermodel.Fields;
import org.apache.poi.hwpf.usermodel.FieldsImpl;
import org.apache.poi.hwpf.usermodel.HWPFList;
import org.apache.poi.hwpf.usermodel.Notes;
import org.apache.poi.hwpf.usermodel.NotesImpl;
import org.apache.poi.hwpf.usermodel.OfficeDrawings;
import org.apache.poi.hwpf.usermodel.OfficeDrawingsImpl;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.crypt.ChunkedCipherOutputStream;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.crypt.standard.EncryptionRecord;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.EntryUtils;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndianByteArrayOutputStream;

/**
 * This class acts as the bucket that we throw all of the Word data structures
 * into.
 */
public final class HWPFDocument extends HWPFDocumentCore {
    /*package*/ static final String PROPERTY_PRESERVE_BIN_TABLES = "org.apache.poi.hwpf.preserveBinTables";
    private static final String PROPERTY_PRESERVE_TEXT_TABLE = "org.apache.poi.hwpf.preserveTextTable";
    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 100_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    private static final String STREAM_DATA = "Data";

    /**
     * table stream buffer
     */
    private byte[] _tableStream;

    /**
     * data stream buffer
     */
    private byte[] _dataStream;

    /**
     * Document wide Properties
     */
    private DocumentProperties _dop;

    /**
     * Contains text of the document wrapped in a obfuscated Word data
     * structure
     */
    private ComplexFileTable _cft;

    /**
     * Contains text buffer linked directly to single-piece document text piece
     */
    private StringBuilder _text;

    /**
     * Holds the save history for this document.
     */
    private SavedByTable _sbt;

    /**
     * Holds the revision mark authors for this document.
     */
    private RevisionMarkAuthorTable _rmat;

    /**
     * Holds FSBA (shape) information
     */
    private FSPATable _fspaHeaders;

    /**
     * Holds FSBA (shape) information
     */
    private FSPATable _fspaMain;

    /**
     * Office Art (Escher records) information
     */
    private final OfficeArtContent officeArtContent;

    /**
     * Holds pictures table
     */
    private PicturesTable _pictures;

    /**
     * Holds Office Art objects
     */
    private OfficeDrawingsImpl _officeDrawingsHeaders;

    /**
     * Holds Office Art objects
     */
    private OfficeDrawingsImpl _officeDrawingsMain;

    /**
     * Holds the bookmarks tables
     */
    private BookmarksTables _bookmarksTables;

    /**
     * Holds the bookmarks
     */
    private Bookmarks _bookmarks;

    /**
     * Holds the ending notes tables
     */
    private NotesTables _endnotesTables = new NotesTables(NoteType.ENDNOTE);

    /**
     * Holds the footnotes
     */
    private Notes _endnotes = new NotesImpl(_endnotesTables);

    /**
     * Holds the footnotes tables
     */
    private NotesTables _footnotesTables = new NotesTables(NoteType.FOOTNOTE);

    /**
     * Holds the footnotes
     */
    private Notes _footnotes = new NotesImpl(_footnotesTables);

    /**
     * Holds the fields PLCFs
     */
    private FieldsTables _fieldsTables;

    /**
     * Holds the fields
     */
    private Fields _fields;

    /**
     * @param length the max record length allowed for HWPFDocument
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for HWPFDocument
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    /**
     * This constructor loads a Word document from an InputStream.
     *
     * @param istream The InputStream that contains the Word document.
     * @throws IOException If there is an unexpected IOException from the passed
     *                     in InputStream.
     * @throws org.apache.poi.EmptyFileException If the given stream is empty
     * @throws RuntimeException a number of other runtime exceptions can be thrown, especially if there are problems with the
     * input format
     */
    public HWPFDocument(InputStream istream) throws IOException {
        //do Ole stuff
        this(verifyAndBuildPOIFS(istream));
    }

    /**
     * This constructor loads a Word document from a POIFSFileSystem
     *
     * @param pfilesystem The POIFSFileSystem that contains the Word document.
     * @throws IOException If there is an unexpected IOException from the passed
     *                     in POIFSFileSystem.
     * @throws RuntimeException a number of runtime exceptions can be thrown, especially if there are problems with the
     * input format
     */
    public HWPFDocument(POIFSFileSystem pfilesystem) throws IOException {
        this(pfilesystem.getRoot());
    }

    /**
     * This constructor loads a Word document from a specific point
     * in a POIFSFileSystem, probably not the default.
     * Used typically to open embedded documents.
     *
     * @param directory The DirectoryNode that contains the Word document.
     * @throws IOException If there is an unexpected IOException from the passed
     *                     in POIFSFileSystem.
     * @throws RuntimeException a number of runtime exceptions can be thrown, especially if there are problems with the
     * input format
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
        String name = (_fib.getFibBase().isFWhichTblStm()) ? STREAM_TABLE_1 : STREAM_TABLE_0;

        // Grab the table stream.
        if (!directory.hasEntry(name)) {
            throw new IllegalStateException("Table Stream '" + name + "' wasn't found - Either the document is corrupt, or is Word95 (or earlier)");
        }

        // read in the table stream.
        _tableStream = getDocumentEntryBytes(name, _fib.getFibBase().getLKey(), Integer.MAX_VALUE);

        _fib.fillVariableFields(_mainStream, _tableStream);

        // read in the data stream.
        _dataStream = directory.hasEntry(STREAM_DATA) ? getDocumentEntryBytes(STREAM_DATA, 0, Integer.MAX_VALUE) : new byte[0];

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
            preserveBinTables = Boolean.parseBoolean(System.getProperty(PROPERTY_PRESERVE_BIN_TABLES));
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
            preserveTextTable = Boolean.parseBoolean(System.getProperty(PROPERTY_PRESERVE_TEXT_TABLE));
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

        officeArtContent = new OfficeArtContent(_tableStream, _fib.getFcDggInfo(), _fib.getLcbDggInfo());

        // read in the pictures stream
        _pictures = new PicturesTable(this, _dataStream, _mainStream, _fspaMain, officeArtContent);

        // And escher pictures
        _officeDrawingsHeaders = new OfficeDrawingsImpl(_fspaHeaders, officeArtContent, _mainStream);
        _officeDrawingsMain = new OfficeDrawingsImpl(_fspaMain, officeArtContent, _mainStream);

        _st = new SectionTable(_mainStream, _tableStream, _fib.getFcPlcfsed(), _fib.getLcbPlcfsed(), fcMin, _tpt, _fib.getSubdocumentTextStreamLength(SubdocumentType.MAIN));
        _ss = new StyleSheet(_tableStream, _fib.getFcStshf());
        _ft = new FontTable(_tableStream, _fib.getFcSttbfffn(), _fib.getLcbSttbfffn());

        int listOffset = _fib.getFcPlfLst();
        // int lfoOffset = _fib.getFcPlfLfo();
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

    @Override
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

    @Override
    public Range getOverallRange() {
        return new Range(0, _text.length(), this);
    }

    /**
     * Returns the range which covers the whole of the document, but excludes
     * any headers and footers.
     */
    @Override
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
            if (subdocument == previos) {
                return new Range(startCp, startCp + length, this);
            }
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
    public OfficeArtContent getOfficeArtContent() {
        return officeArtContent;
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
     * Returns user-friendly interface to access document {@link Field}s
     *
     * @return user-friendly interface to access document {@link Field}s
     */
    public Fields getFields() {
        return _fields;
    }

    /**
     * Write out the word file that is represented by this class, to the
     * currently open {@link File}, via the writeable {@link POIFSFileSystem}
     * it was opened as.
     *
     * <p>This will fail (with an {@link IllegalStateException} if the
     * Document was opened read-only, opened from an {@link InputStream}
     * instead of a File, or if this is not the root document. For those cases,
     * you must use {@link #write(OutputStream)} or {@link #write(File)} to
     * write to a brand new document.
     *
     * @since 3.15
     */
    @Override
    public void write() throws IOException {
        validateInPlaceWritePossible();

        // Update the Document+Properties streams in the file
        write(getDirectory().getFileSystem(), false);

        // Sync with the File on disk
        getDirectory().getFileSystem().writeFilesystem();
    }

    /**
     * Writes out the word file that is represented by an instance of this class.
     * <p>
     * If the {@link File} exists, it will be replaced, otherwise a new one
     * will be created
     *
     * @param newFile The File to write to.
     * @throws IOException If there is an unexpected IOException from writing
     *                     to the File.
     * @since 3.15 beta 3
     */
    @Override
    public void write(File newFile) throws IOException {
        POIFSFileSystem pfs = POIFSFileSystem.create(newFile);
        write(pfs, true);
        pfs.writeFilesystem();
    }

    /**
     * Writes out the word file that is represented by an instance of this class.
     * <p>
     * For better performance when writing to files, use {@link #write(File)}.
     * If {@code stream} has a high cost/latency associated with each written byte,
     * consider wrapping the OutputStream in a {@link java.io.BufferedOutputStream}
     * to improve write performance.
     *
     * @param out The OutputStream to write to.
     * @throws IOException If there is an unexpected IOException from the passed
     *                     in OutputStream.
     */
    @Override
    public void write(OutputStream out) throws IOException {
        POIFSFileSystem pfs = new POIFSFileSystem();
        write(pfs, true);
        pfs.writeFilesystem(out);
    }

    private void write(POIFSFileSystem pfs, boolean copyOtherEntries) throws IOException {
        // clear the offsets and sizes in our FileInformationBlock.
        _fib.clearOffsetsSizes();

        // determine the FileInformationBLock size
        int fibSize = _fib.getSize();
        fibSize += POIFSConstants.SMALLER_BIG_BLOCK_SIZE - (fibSize % POIFSConstants.SMALLER_BIG_BLOCK_SIZE);

        // initialize our streams for writing.
        HWPFFileSystem docSys = new HWPFFileSystem();
        ByteArrayOutputStream wordDocumentStream = docSys.getStream(STREAM_WORD_DOCUMENT);
        ByteArrayOutputStream tableStream = docSys.getStream(STREAM_TABLE_1);

        // preserve space for the FileInformationBlock because we will be writing
        // it after we write everything else.
        byte[] placeHolder = IOUtils.safelyAllocate(fibSize, MAX_RECORD_LENGTH);
        wordDocumentStream.write(placeHolder);
        int mainOffset = wordDocumentStream.size();
        int tableOffset = 0;

        // write out EncryptionInfo
        updateEncryptionInfo();
        EncryptionInfo ei = getEncryptionInfo();
        if (ei != null) {
            byte[] buf = new byte[1000];
            LittleEndianByteArrayOutputStream leos = new LittleEndianByteArrayOutputStream(buf, 0);
            leos.writeShort(ei.getVersionMajor());
            leos.writeShort(ei.getVersionMinor());
            if (ei.getEncryptionMode() == EncryptionMode.cryptoAPI) {
                leos.writeInt(ei.getEncryptionFlags());
            }

            ((EncryptionRecord) ei.getHeader()).write(leos);
            ((EncryptionRecord) ei.getVerifier()).write(leos);
            tableStream.write(buf, 0, leos.getWriteIndex());
            tableOffset += leos.getWriteIndex();
            _fib.getFibBase().setLKey(tableOffset);
        }

        // write out the StyleSheet.
        _fib.setFcStshf(tableOffset);
        _ss.writeTo(tableStream);
        _fib.setLcbStshf(tableStream.size() - tableOffset);
        tableOffset = tableStream.size();

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
        _fib.setLcbClx(tableStream.size() - tableOffset);
        tableOffset = tableStream.size();
        int fcMac = wordDocumentStream.size();

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
        _fib.setLcbDop(tableStream.size() - tableOffset);
        tableOffset = tableStream.size();

        /*
         * plcfBkmkf (table recording beginning CPs of bookmarks) Written
         * immediately after the sttbfBkmk, if the document contains bookmarks.
         *
         * Microsoft Office Word 97-2007 Binary File Format (.doc)
         * Specification; Page 24 of 210
         */
        if (_bookmarksTables != null) {
            _bookmarksTables.writePlcfBkmkf(_fib, tableStream);
            tableOffset = tableStream.size();
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
            tableOffset = tableStream.size();
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
        _fib.setLcbPlcfbteChpx(tableStream.size() - tableOffset);
        tableOffset = tableStream.size();

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
        _fib.setLcbPlcfbtePapx(tableStream.size() - tableOffset);
        tableOffset = tableStream.size();

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
        tableOffset = tableStream.size();

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
            tableOffset = tableStream.size();
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
        tableOffset = tableStream.size();

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
        _fib.setLcbPlcfsed(tableStream.size() - tableOffset);
        tableOffset = tableStream.size();

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
            tableOffset = tableStream.size();

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
            tableOffset = tableStream.size();
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
            tableOffset = tableStream.size();
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
            _fib.setLcbSttbSavedBy(tableStream.size() - tableOffset);

            tableOffset = tableStream.size();
        }

        // write out the revision mark authors table.
        if (_rmat != null) {
            _fib.setFcSttbfRMark(tableOffset);
            _rmat.writeTo(tableStream);
            _fib.setLcbSttbfRMark(tableStream.size() - tableOffset);

            tableOffset = tableStream.size();
        }

        // write out the FontTable.
        _fib.setFcSttbfffn(tableOffset);
        _ft.writeTo(tableStream);
        _fib.setLcbSttbfffn(tableStream.size() - tableOffset);
        tableOffset = tableStream.size();

        // set some variables in the FileInformationBlock.
        _fib.getFibBase().setFcMin(mainOffset);
        _fib.getFibBase().setFcMac(fcMac);
        _fib.setCbMac(wordDocumentStream.size());

        // make sure that the table, doc and data streams use big blocks.
        byte[] mainBuf = fillUp4096(wordDocumentStream);

        // Table1 stream will be used
        _fib.getFibBase().setFWhichTblStm(true);

        // write out the FileInformationBlock.
        //_fib.serialize(mainBuf, 0);
        _fib.writeTo(mainBuf, tableStream);

        byte[] tableBuf = fillUp4096(tableStream);
        byte[] dataBuf = fillUp4096(_dataStream);

        // Create a new document - ignoring the order of the old entries
        if (ei == null) {
            write(pfs, mainBuf, STREAM_WORD_DOCUMENT);
            write(pfs, tableBuf, STREAM_TABLE_1);
            write(pfs, dataBuf, STREAM_DATA);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(100000);
            encryptBytes(mainBuf, FIB_BASE_LEN, bos);
            write(pfs, bos.toByteArray(), STREAM_WORD_DOCUMENT);
            bos.reset();
            encryptBytes(tableBuf, _fib.getFibBase().getLKey(), bos);
            write(pfs, bos.toByteArray(), STREAM_TABLE_1);
            bos.reset();
            encryptBytes(dataBuf, 0, bos);
            write(pfs, bos.toByteArray(), STREAM_DATA);
            bos.reset();
        }

        writeProperties(pfs);

        if (copyOtherEntries && ei == null) {
            // For encrypted files:
            // The ObjectPool storage MUST NOT be present and if the file contains OLE objects, the storage
            // objects for the OLE objects MUST be stored in the Data stream as specified in sprmCPicLocation.
            DirectoryNode newRoot = pfs.getRoot();
            _objectPool.writeTo(newRoot);

            for (Entry entry : getDirectory()) {
                String entryName = entry.getName();
                if (!(
                        STREAM_WORD_DOCUMENT.equals(entryName) ||
                                STREAM_TABLE_0.equals(entryName) ||
                                STREAM_TABLE_1.equals(entryName) ||
                                STREAM_DATA.equals(entryName) ||
                                STREAM_OBJECT_POOL.equals(entryName) ||
                                SummaryInformation.DEFAULT_STREAM_NAME.equals(entryName) ||
                                DocumentSummaryInformation.DEFAULT_STREAM_NAME.equals(entryName)
                )) {
                    EntryUtils.copyNodeRecursively(entry, newRoot);
                }
            }
        }

        /*
         * since we updated all references in FIB and etc, using new arrays to
         * access data
         */
        replaceDirectory(pfs.getRoot());
        this._tableStream = tableStream.toByteArray();
        this._dataStream = dataBuf;
    }

    private void encryptBytes(byte[] plain, int encryptOffset, OutputStream bos) throws IOException {
        try {
            EncryptionInfo ei = getEncryptionInfo();
            Encryptor enc = ei.getEncryptor();
            enc.setChunkSize(RC4_REKEYING_INTERVAL);
            ChunkedCipherOutputStream os = enc.getDataStream(bos, 0);
            if (encryptOffset > 0) {
                os.writePlain(plain, 0, encryptOffset);
            }
            os.write(plain, encryptOffset, plain.length - encryptOffset);
            os.close();
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    private static byte[] fillUp4096(byte[] buf) {
        if (buf == null) {
            return new byte[4096];
        } else if (buf.length < 4096) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
            bos.write(buf, 0, buf.length);
            return fillUp4096(bos);
        } else {
            return buf;
        }
    }

    private static byte[] fillUp4096(ByteArrayOutputStream bos) {
        int fillSize = 4096 - bos.size();
        if (fillSize > 0) {
            bos.write(new byte[fillSize], 0, fillSize);
        }
        return bos.toByteArray();
    }

    private static void write(POIFSFileSystem pfs, byte[] data, String name) throws IOException {
        pfs.createOrUpdateDocument(new ByteArrayInputStream(data), name);
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