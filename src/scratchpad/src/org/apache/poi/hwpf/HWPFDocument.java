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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.hwpf.model.BookmarksTables;
import org.apache.poi.hwpf.model.CHPBinTable;
import org.apache.poi.hwpf.model.CPSplitCalculator;
import org.apache.poi.hwpf.model.ComplexFileTable;
import org.apache.poi.hwpf.model.DocumentProperties;
import org.apache.poi.hwpf.model.EscherRecordHolder;
import org.apache.poi.hwpf.model.FSPADocumentPart;
import org.apache.poi.hwpf.model.FSPATable;
import org.apache.poi.hwpf.model.FieldsTables;
import org.apache.poi.hwpf.model.FontTable;
import org.apache.poi.hwpf.model.ListTables;
import org.apache.poi.hwpf.model.NoteType;
import org.apache.poi.hwpf.model.NotesTables;
import org.apache.poi.hwpf.model.PAPBinTable;
import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.hwpf.model.RevisionMarkAuthorTable;
import org.apache.poi.hwpf.model.SavedByTable;
import org.apache.poi.hwpf.model.SectionTable;
import org.apache.poi.hwpf.model.ShapesTable;
import org.apache.poi.hwpf.model.SinglentonTextPiece;
import org.apache.poi.hwpf.model.StyleSheet;
import org.apache.poi.hwpf.model.SubdocumentType;
import org.apache.poi.hwpf.model.TextPiece;
import org.apache.poi.hwpf.model.TextPieceTable;
import org.apache.poi.hwpf.model.io.HWPFFileSystem;
import org.apache.poi.hwpf.model.io.HWPFOutputStream;
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
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.Internal;


/**
 *
 * This class acts as the bucket that we throw all of the Word data structures
 * into.
 *
 * @author Ryan Ackley
 */
public final class HWPFDocument extends HWPFDocumentCore
{
    private static final String PROPERTY_PRESERVE_BIN_TABLES = "org.apache.poi.hwpf.preserveBinTables";
    private static final String PROPERTY_PRESERVE_TEXT_TABLE = "org.apache.poi.hwpf.preserveTextTable";

  /** And for making sense of CP lengths in the FIB */
  @Deprecated
  protected CPSplitCalculator _cpSplit;

  /** table stream buffer*/
  protected byte[] _tableStream;

  /** data stream buffer*/
  protected byte[] _dataStream;

  /** Document wide Properties*/
  protected DocumentProperties _dop;

  /** Contains text of the document wrapped in a obfuscated Word data
  * structure*/
  protected ComplexFileTable _cft;

  /** Contains text buffer linked directly to single-piece document text piece */
  protected StringBuilder _text;

  /** Holds the save history for this document. */
  protected SavedByTable _sbt;
  
  /** Holds the revision mark authors for this document. */
  protected RevisionMarkAuthorTable _rmat;

  /** Holds FSBA (shape) information */
  private FSPATable _fspaHeaders;

  /** Holds FSBA (shape) information */
  private FSPATable _fspaMain;

  /** Escher Drawing Group information */
  protected EscherRecordHolder _escherRecordHolder;

  /** Holds pictures table */
  protected PicturesTable _pictures;

  /** Holds Office Art objects */
  @Deprecated
  protected ShapesTable _officeArts;
  
  /** Holds Office Art objects */
  protected OfficeDrawingsImpl _officeDrawingsHeaders;

  /** Holds Office Art objects */
  protected OfficeDrawingsImpl _officeDrawingsMain;

  /** Holds the bookmarks tables */
  protected BookmarksTables _bookmarksTables;

  /** Holds the bookmarks */
  protected Bookmarks _bookmarks;

  /** Holds the ending notes tables */
  protected NotesTables _endnotesTables = new NotesTables( NoteType.ENDNOTE );

  /** Holds the footnotes */
  protected Notes _endnotes = new NotesImpl( _endnotesTables );

  /** Holds the footnotes tables */
  protected NotesTables _footnotesTables = new NotesTables( NoteType.FOOTNOTE );

  /** Holds the footnotes */
  protected Notes _footnotes = new NotesImpl( _footnotesTables );

  /** Holds the fields PLCFs */
  protected FieldsTables _fieldsTables;

  /** Holds the fields */
  protected Fields _fields;

  protected HWPFDocument()
  {
     super();
     this._text = new StringBuilder("\r");
  }

  /**
   * This constructor loads a Word document from an InputStream.
   *
   * @param istream The InputStream that contains the Word document.
   * @throws IOException If there is an unexpected IOException from the passed
   *         in InputStream.
   */
  public HWPFDocument(InputStream istream) throws IOException
  {
    //do Ole stuff
    this( verifyAndBuildPOIFS(istream) );
  }

  /**
   * This constructor loads a Word document from a POIFSFileSystem
   *
   * @param pfilesystem The POIFSFileSystem that contains the Word document.
   * @throws IOException If there is an unexpected IOException from the passed
   *         in POIFSFileSystem.
   */
  public HWPFDocument(POIFSFileSystem pfilesystem) throws IOException
  {
	this(pfilesystem.getRoot());
  }

  /**
   * This constructor loads a Word document from a specific point
   *  in a POIFSFileSystem, probably not the default.
   * Used typically to open embedded documents.
   *
   * @param pfilesystem The POIFSFileSystem that contains the Word document.
   * @throws IOException If there is an unexpected IOException from the passed
   *         in POIFSFileSystem.
   */
  public HWPFDocument(DirectoryNode directory, POIFSFileSystem pfilesystem) throws IOException
  {
     this(directory);
  }
  
  /**
   * This constructor loads a Word document from a specific point
   *  in a POIFSFileSystem, probably not the default.
   * Used typically to open embeded documents.
   *
   * @param directory The DirectoryNode that contains the Word document.
   * @throws IOException If there is an unexpected IOException from the passed
   *         in POIFSFileSystem.
   */
  public HWPFDocument(DirectoryNode directory) throws IOException
  {
    // Load the main stream and FIB
    // Also handles HPSF bits
	super(directory);

    // Do the CP Split
    _cpSplit = new CPSplitCalculator(_fib);
    
    // Is this document too old for us?
    if(_fib.getNFib() < 106) {
        throw new OldWordFileFormatException("The document is too old - Word 95 or older. Try HWPFOldDocument instead?");
    }

    // use the fib to determine the name of the table stream.
    String name = "0Table";
    if (_fib.isFWhichTblStm())
    {
      name = "1Table";
    }

    // Grab the table stream.
    DocumentEntry tableProps;
	try {
		tableProps =
			(DocumentEntry)directory.getEntry(name);
	} catch(FileNotFoundException fnfe) {
		throw new IllegalStateException("Table Stream '" + name + "' wasn't found - Either the document is corrupt, or is Word95 (or earlier)");
	}

    // read in the table stream.
    _tableStream = new byte[tableProps.getSize()];
    directory.createDocumentInputStream(name).read(_tableStream);

    _fib.fillVariableFields(_mainStream, _tableStream);

    // read in the data stream.
    try
    {
      DocumentEntry dataProps =
          (DocumentEntry)directory.getEntry("Data");
      _dataStream = new byte[dataProps.getSize()];
      directory.createDocumentInputStream("Data").read(_dataStream);
    }
    catch(java.io.FileNotFoundException e)
    {
        _dataStream = new byte[0];
    }

    // Get the cp of the start of text in the main stream
    // The latest spec doc says this is always zero!
    int fcMin = 0;
    //fcMin = _fib.getFcMin()

    // Start to load up our standard structures.
    _dop = new DocumentProperties(_tableStream, _fib.getFcDop());
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
        try
        {
            preserveBinTables = Boolean.parseBoolean( System
                    .getProperty( PROPERTY_PRESERVE_BIN_TABLES ) );
        }
        catch ( Exception exc )
        {
            // ignore;
        }

        if ( !preserveBinTables )
        {
            _cbt.rebuild( _cft );
            _pbt.rebuild( _text, _cft );
        }

        /*
         * Property to disable text rebuilding. In this mode changing the text
         * will lead to unpredictable behavior
         */
        boolean preserveTextTable = false;
        try
        {
            preserveTextTable = Boolean.parseBoolean( System
                    .getProperty( PROPERTY_PRESERVE_TEXT_TABLE ) );
        }
        catch ( Exception exc )
        {
            // ignore;
        }
        if ( !preserveTextTable )
        {
            _cft = new ComplexFileTable();
            _tpt = _cft.getTextPieceTable();
            final TextPiece textPiece = new SinglentonTextPiece( _text );
            _tpt.add( textPiece );
            _text = textPiece.getStringBuilder();
        }

        // Read FSPA and Escher information
        // _fspa = new FSPATable(_tableStream, _fib.getFcPlcspaMom(),
        // _fib.getLcbPlcspaMom(), getTextTable().getTextPieces());
        _fspaHeaders = new FSPATable( _tableStream, _fib,
                FSPADocumentPart.HEADER );
        _fspaMain = new FSPATable( _tableStream, _fib, FSPADocumentPart.MAIN );

    if (_fib.getFcDggInfo() != 0)
    {
        _escherRecordHolder = new EscherRecordHolder(_tableStream, _fib.getFcDggInfo(), _fib.getLcbDggInfo());
    } else
    {
        _escherRecordHolder = new EscherRecordHolder();
    }

    // read in the pictures stream
    _pictures = new PicturesTable(this, _dataStream, _mainStream, _fspaMain, _escherRecordHolder);
    // And the art shapes stream
    _officeArts = new ShapesTable(_tableStream, _fib);

    // And escher pictures
    _officeDrawingsHeaders = new OfficeDrawingsImpl( _fspaHeaders, _escherRecordHolder, _mainStream );
    _officeDrawingsMain = new OfficeDrawingsImpl( _fspaMain , _escherRecordHolder, _mainStream);

    _st = new SectionTable(_mainStream, _tableStream, _fib.getFcPlcfsed(), _fib.getLcbPlcfsed(), fcMin, _tpt, _cpSplit);
    _ss = new StyleSheet(_tableStream, _fib.getFcStshf());
    _ft = new FontTable(_tableStream, _fib.getFcSttbfffn(), _fib.getLcbSttbfffn());

    int listOffset = _fib.getFcPlcfLst();
    int lfoOffset = _fib.getFcPlfLfo();
    if (listOffset != 0 && _fib.getLcbPlcfLst() != 0)
    {
      _lt = new ListTables(_tableStream, _fib.getFcPlcfLst(), _fib.getFcPlfLfo());
    }

    int sbtOffset = _fib.getFcSttbSavedBy();
    int sbtLength = _fib.getLcbSttbSavedBy();
    if (sbtOffset != 0 && sbtLength != 0)
    {
      _sbt = new SavedByTable(_tableStream, sbtOffset, sbtLength);
    }

    int rmarkOffset = _fib.getFcSttbfRMark();
    int rmarkLength = _fib.getLcbSttbfRMark();
    if (rmarkOffset != 0 && rmarkLength != 0)
    {
      _rmat = new RevisionMarkAuthorTable(_tableStream, rmarkOffset, rmarkLength);
    }

    _bookmarksTables = new BookmarksTables( _tableStream, _fib );
    _bookmarks = new BookmarksImpl( _bookmarksTables );

    _endnotesTables = new NotesTables( NoteType.ENDNOTE, _tableStream, _fib );
    _endnotes = new NotesImpl( _endnotesTables );
    _footnotesTables = new NotesTables( NoteType.FOOTNOTE, _tableStream, _fib );
    _footnotes = new NotesImpl( _footnotesTables );

    _fieldsTables = new FieldsTables(_tableStream, _fib);
    _fields = new FieldsImpl(_fieldsTables);
  }

  @Internal
  public TextPieceTable getTextTable()
  {
    return _cft.getTextPieceTable();
  }

    @Internal
    @Override
    public StringBuilder getText()
    {
        return _text;
    }

  @Deprecated
  public CPSplitCalculator getCPSplitCalculator()
  {
	return _cpSplit;
  }

  public DocumentProperties getDocProperties()
  {
    return _dop;
  }

  public Range getOverallRange() {
      return new Range(0, _text.length(), this);
  }

    /**
     * Returns the range which covers the whole of the document, but excludes
     * any headers and footers.
     */
    public Range getRange()
    {
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

    private Range getRange( SubdocumentType subdocument )
    {
        int startCp = 0;
        for ( SubdocumentType previos : SubdocumentType.ORDERED )
        {
            int length = getFileInformationBlock()
                    .getSubdocumentTextStreamLength( previos );
            if ( subdocument == previos )
                return new Range( startCp, startCp + length, this );
            startCp += length;
        }
        throw new UnsupportedOperationException(
                "Subdocument type not supported: " + subdocument );
    }

    /**
     * Returns the {@link Range} which covers all the Footnotes.
     * 
     * @return the {@link Range} which covers all the Footnotes.
     */
    public Range getFootnoteRange()
    {
        return getRange( SubdocumentType.FOOTNOTE );
    }

    /**
     * Returns the {@link Range} which covers all endnotes.
     * 
     * @return the {@link Range} which covers all endnotes.
     */
    public Range getEndnoteRange()
    {
        return getRange( SubdocumentType.ENDNOTE );
    }

    /**
     * Returns the {@link Range} which covers all annotations.
     * 
     * @return the {@link Range} which covers all annotations.
     */
    public Range getCommentsRange()
    {
        return getRange( SubdocumentType.ANNOTATION );
    }

    /**
     * Returns the {@link Range} which covers all textboxes.
     * 
     * @return the {@link Range} which covers all textboxes.
     */
    public Range getMainTextboxRange()
    {
        return getRange( SubdocumentType.TEXTBOX );
    }

  /**
   * Returns the range which covers all "Header Stories".
   * A header story contains a header, footer, end note
   *  separators and footnote separators.
   */
  public Range getHeaderStoryRange() {
	  return getRange( SubdocumentType.HEADER );
  }

  /**
   * Returns the character length of a document.
   * @return the character length of a document
   */
  public int characterLength()
  {
      return _text.length();
  }

  /**
   * Gets a reference to the saved -by table, which holds the save history for the document.
   *
   * @return the saved-by table.
   */
  @Internal
  public SavedByTable getSavedByTable()
  {
    return _sbt;
  }

  /**
   * Gets a reference to the revision mark author table, which holds the revision mark authors for the document.
   *
   * @return the saved-by table.
   */
  @Internal
  public RevisionMarkAuthorTable getRevisionMarkAuthorTable()
  {
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
     *         from this document
     * @deprecated use {@link #getOfficeDrawingsMain()} instead
     */
    @Deprecated
    @Internal
    public ShapesTable getShapesTable()
    {
        return _officeArts;
    }

    public OfficeDrawings getOfficeDrawingsHeaders()
    {
        return _officeDrawingsHeaders;
    }

    public OfficeDrawings getOfficeDrawingsMain()
    {
        return _officeDrawingsMain;
    }

    /**
     * @return user-friendly interface to access document bookmarks
     */
    public Bookmarks getBookmarks()
    {
        return _bookmarks;
    }

    /**
     * @return user-friendly interface to access document endnotes
     */
    public Notes getEndnotes()
    {
        return _endnotes;
    }

    /**
     * @return user-friendly interface to access document footnotes
     */
    public Notes getFootnotes()
    {
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
    public Fields getFields()
    {
        return _fields;
    }

  /**
   * Writes out the word file that is represented by an instance of this class.
   *
   * @param out The OutputStream to write to.
   * @throws IOException If there is an unexpected IOException from the passed
   *         in OutputStream.
   */
  public void write(OutputStream out)
    throws IOException
  {
    // initialize our streams for writing.
    HWPFFileSystem docSys = new HWPFFileSystem();
    HWPFOutputStream mainStream = docSys.getStream("WordDocument");
    HWPFOutputStream tableStream = docSys.getStream("1Table");
    //HWPFOutputStream dataStream = docSys.getStream("Data");
    int tableOffset = 0;

    // FileInformationBlock fib = (FileInformationBlock)_fib.clone();
    // clear the offsets and sizes in our FileInformationBlock.
    _fib.clearOffsetsSizes();

    // determine the FileInformationBLock size
    int fibSize = _fib.getSize();
    fibSize  += POIFSConstants.SMALLER_BIG_BLOCK_SIZE -
        (fibSize % POIFSConstants.SMALLER_BIG_BLOCK_SIZE);

    // preserve space for the FileInformationBlock because we will be writing
    // it after we write everything else.
    byte[] placeHolder = new byte[fibSize];
    mainStream.write(placeHolder);
    int mainOffset = mainStream.getOffset();

    // write out the StyleSheet.
    _fib.setFcStshf(tableOffset);
    _ss.writeTo(tableStream);
    _fib.setLcbStshf(tableStream.getOffset() - tableOffset);
    tableOffset = tableStream.getOffset();

    // get fcMin and fcMac because we will be writing the actual text with the
    // complex table.
    int fcMin = mainOffset;

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
    _cft.writeTo(docSys);
    _fib.setLcbClx(tableStream.getOffset() - tableOffset);
    tableOffset = tableStream.getOffset();
    int fcMac = mainStream.getOffset();

        /*
         * plcfBkmkf (table recording beginning CPs of bookmarks) Written
         * immediately after the sttbfBkmk, if the document contains bookmarks.
         * 
         * Microsoft Office Word 97-2007 Binary File Format (.doc)
         * Specification; Page 24 of 210
         */
        if ( _bookmarksTables != null )
        {
            _bookmarksTables.writePlcfBkmkf( _fib, tableStream );
            tableOffset = tableStream.getOffset();
        }

        /*
         * plcfBkmkl (table recording limit CPs of bookmarks) Written
         * immediately after the plcfBkmkf, if the document contains bookmarks.
         * 
         * Microsoft Office Word 97-2007 Binary File Format (.doc)
         * Specification; Page 24 of 210
         */
        if ( _bookmarksTables != null )
        {
            _bookmarksTables.writePlcfBkmkl( _fib, tableStream );
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
    _cbt.writeTo(docSys, fcMin, _cft.getTextPieceTable());
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
    _pbt.writeTo(docSys, _cft.getTextPieceTable());
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
        _endnotesTables.writeRef( _fib, tableStream );
        _endnotesTables.writeTxt( _fib, tableStream );
        tableOffset = tableStream.getOffset();

    /*
     * plcffld*** (table of field positions and statuses for annotation
     * subdocument) Written immediately after the previously recorded table,
     * if the ******* subdocument contains fields.
     * 
     * Microsoft Office Word 97-2007 Binary File Format (.doc)
     * Specification; Page 24 of 210
     */

    if ( _fieldsTables != null )
    {
        _fieldsTables.write( _fib, tableStream );
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
        _footnotesTables.writeRef( _fib, tableStream );
        _footnotesTables.writeTxt( _fib, tableStream );
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
    _st.writeTo(docSys, fcMin);
    _fib.setLcbPlcfsed(tableStream.getOffset() - tableOffset);
    tableOffset = tableStream.getOffset();

        /*
         * plcflst (list formats) Written immediately after the end of the
         * previously recorded, if there are any lists defined in the document.
         * This begins with a short count of LSTF structures followed by those
         * LSTF structures. This is immediately followed by the allocated data
         * hanging off the LSTFs. This data consists of the array of LVLs for
         * each LSTF. (Each LVL consists of an LVLF followed by two grpprls and
         * an XST.)
         * 
         * Microsoft Office Word 97-2007 Binary File Format (.doc)
         * Specification; Page 25 of 210
         */

    // write out the list tables
    if (_lt != null)
    {
      _fib.setFcPlcfLst(tableOffset);
      _lt.writeListDataTo(tableStream);
      _fib.setLcbPlcfLst(tableStream.getOffset() - tableOffset);
    }

    /*
     * plflfo (more list formats) Written immediately after the end of the
     * plcflst and its accompanying data, if there are any lists defined in
     * the document. This consists first of a PL of LFO records, followed by
     * the allocated data (if any) hanging off the LFOs. The allocated data
     * consists of the array of LFOLVLFs for each LFO (and each LFOLVLF is
     * immediately followed by some LVLs).
     * 
     * Microsoft Office Word 97-2007 Binary File Format (.doc)
     * Specification; Page 26 of 210
     */

    if (_lt != null)
    {
      _fib.setFcPlfLfo(tableStream.getOffset());
      _lt.writeListOverridesTo(tableStream);
      _fib.setLcbPlfLfo(tableStream.getOffset() - tableOffset);
      tableOffset = tableStream.getOffset();
    }

        /*
         * sttbfBkmk (table of bookmark name strings) Written immediately after
         * the previously recorded table, if the document contains bookmarks.
         * 
         * Microsoft Office Word 97-2007 Binary File Format (.doc)
         * Specification; Page 27 of 210
         */
        if ( _bookmarksTables != null )
        {
            _bookmarksTables.writeSttbfBkmk( _fib, tableStream );
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
    if (_sbt != null)
    {
      _fib.setFcSttbSavedBy(tableOffset);
      _sbt.writeTo(tableStream);
      _fib.setLcbSttbSavedBy(tableStream.getOffset() - tableOffset);

      tableOffset = tableStream.getOffset();
    }
    
    // write out the revision mark authors table.
    if (_rmat != null)
    {
      _fib.setFcSttbfRMark(tableOffset);
      _rmat.writeTo(tableStream);
      _fib.setLcbSttbfRMark(tableStream.getOffset() - tableOffset);

      tableOffset = tableStream.getOffset();
    }

    // write out the FontTable.
    _fib.setFcSttbfffn(tableOffset);
    _ft.writeTo(docSys);
    _fib.setLcbSttbfffn(tableStream.getOffset() - tableOffset);
    tableOffset = tableStream.getOffset();

    // write out the DocumentProperties.
    _fib.setFcDop(tableOffset);
    byte[] buf = new byte[_dop.getSize()];
    _fib.setLcbDop(_dop.getSize());
    _dop.serialize(buf, 0);
    tableStream.write(buf);

    // set some variables in the FileInformationBlock.
    _fib.setFcMin(fcMin);
    _fib.setFcMac(fcMac);
    _fib.setCbMac(mainStream.getOffset());

    // make sure that the table, doc and data streams use big blocks.
    byte[] mainBuf = mainStream.toByteArray();
    if (mainBuf.length < 4096)
    {
      byte[] tempBuf = new byte[4096];
      System.arraycopy(mainBuf, 0, tempBuf, 0, mainBuf.length);
      mainBuf = tempBuf;
    }

    // write out the FileInformationBlock.
    //_fib.serialize(mainBuf, 0);
    _fib.writeTo(mainBuf, tableStream);

    byte[] tableBuf = tableStream.toByteArray();
    if (tableBuf.length < 4096)
    {
      byte[] tempBuf = new byte[4096];
      System.arraycopy(tableBuf, 0, tempBuf, 0, tableBuf.length);
      tableBuf = tempBuf;
    }

    byte[] dataBuf = _dataStream;
    if (dataBuf == null)
    {
      dataBuf = new byte[4096];
    }
    if (dataBuf.length < 4096)
    {
      byte[] tempBuf = new byte[4096];
      System.arraycopy(dataBuf, 0, tempBuf, 0, dataBuf.length);
      dataBuf = tempBuf;
    }


    // spit out the Word document.
    POIFSFileSystem pfs = new POIFSFileSystem();
    pfs.createDocument(new ByteArrayInputStream(mainBuf), "WordDocument");
    pfs.createDocument(new ByteArrayInputStream(tableBuf), "1Table");
    pfs.createDocument(new ByteArrayInputStream(dataBuf), "Data");
    writeProperties(pfs);

    pfs.writeFilesystem(out);

        /*
         * since we updated all references in FIB and etc, using new arrays to
         * access data
         */
        this._tableStream = tableStream.toByteArray();
        this._dataStream = dataBuf;
    }

  @Internal
  public byte[] getDataStream()
  {
    return _dataStream;
  }
  @Internal
  public byte[] getTableStream()
  {
	return _tableStream;
  }

  public int registerList(HWPFList list)
  {
    if (_lt == null)
    {
      _lt = new ListTables();
    }
    return _lt.addList(list.getListData(), list.getOverride());
  }

  public void delete(int start, int length)
  {
    Range r = new Range(start, start + length, this);
    r.delete();
  }
}
