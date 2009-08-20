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

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PushbackInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;

import java.util.Iterator;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.POIDocument;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.common.POIFSConstants;

import org.apache.poi.hwpf.model.*;
import org.apache.poi.hwpf.model.io.*;
import org.apache.poi.hwpf.usermodel.*;


/**
 *
 * This class acts as the bucket that we throw all of the Word data structures
 * into.
 *
 * @author Ryan Ackley
 */
public final class HWPFDocument extends POIDocument
//  implements Cloneable
{
  /** The FIB */
  protected FileInformationBlock _fib;
  /** And for making sense of CP lengths in the FIB */
  protected CPSplitCalculator _cpSplit;

  /** main document stream buffer*/
  protected byte[] _mainStream;

  /** table stream buffer*/
  protected byte[] _tableStream;

  /** data stream buffer*/
  protected byte[] _dataStream;

  /** Document wide Properties*/
  protected DocumentProperties _dop;

  /** Contains text of the document wrapped in a obfuscated Word data
  * structure*/
  protected ComplexFileTable _cft;

  protected TextPieceTable _tpt;

  /** Contains formatting properties for text*/
  protected CHPBinTable _cbt;

  /** Contains formatting properties for paragraphs*/
  protected PAPBinTable _pbt;

  /** Contains formatting properties for sections.*/
  protected SectionTable _st;

  /** Holds styles for this document.*/
  protected StyleSheet _ss;

  /** Holds fonts for this document.*/
  protected FontTable _ft;

  /** Hold list tables */
  protected ListTables _lt;

  /** Holds the save history for this document. */
  protected SavedByTable _sbt;

  /** Holds pictures table */
  protected PicturesTable _pictures;

  /** Holds FSBA (shape) information */
  protected FSPATable _fspa;

  /** Escher Drawing Group information */
  protected EscherRecordHolder _dgg;

  /** Holds Office Art objects */
  protected ShapesTable _officeArts;

  protected HWPFDocument()
  {
     super(null, null);
  }

  /**
   * Takens an InputStream, verifies that it's not RTF, builds a
   *  POIFSFileSystem from it, and returns that.
   */
  public static POIFSFileSystem verifyAndBuildPOIFS(InputStream istream) throws IOException {
	// Open a PushbackInputStream, so we can peek at the first few bytes
	PushbackInputStream pis = new PushbackInputStream(istream,6);
	byte[] first6 = new byte[6];
	pis.read(first6);

	// Does it start with {\rtf ? If so, it's really RTF
	if(first6[0] == '{' && first6[1] == '\\' && first6[2] == 'r'
		&& first6[3] == 't' && first6[4] == 'f') {
		throw new IllegalArgumentException("The document is really a RTF file");
	}

	// OK, so it's not RTF
	// Open a POIFSFileSystem on the (pushed back) stream
	pis.unread(first6);
	return new POIFSFileSystem(pis);
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
	this(pfilesystem.getRoot(), pfilesystem);
  }

  /**
   * This constructor loads a Word document from a specific point
   *  in a POIFSFileSystem, probably not the default.
   * Used typically to open embeded documents.
   *
   * @param pfilesystem The POIFSFileSystem that contains the Word document.
   * @throws IOException If there is an unexpected IOException from the passed
   *         in POIFSFileSystem.
   */
  public HWPFDocument(DirectoryNode directory, POIFSFileSystem pfilesystem) throws IOException
  {
    // Sort out the hpsf properties
	super(directory, pfilesystem);

    // read in the main stream.
    DocumentEntry documentProps = (DocumentEntry)
       directory.getEntry("WordDocument");
    _mainStream = new byte[documentProps.getSize()];

    directory.createDocumentInputStream("WordDocument").read(_mainStream);

    // Create our FIB, and check for the doc being encrypted
    _fib = new FileInformationBlock(_mainStream);
    _cpSplit = new CPSplitCalculator(_fib);
    if(_fib.isFEncrypted()) {
    	throw new EncryptedDocumentException("Cannot process encrypted word files!");
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
      filesystem.createDocumentInputStream("Data").read(_dataStream);
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
    _tpt = _cft.getTextPieceTable();

    // Word XP and later all put in a zero filled buffer in
    //  front of the text. This screws up the system for offsets,
    //  which assume we always start at zero. This is an adjustment.
    int cpMin = _tpt.getCpMin();

    // Now load the rest of the properties, which need to be adjusted
    //  for where text really begin
    _cbt = new CHPBinTable(_mainStream, _tableStream, _fib.getFcPlcfbteChpx(), _fib.getLcbPlcfbteChpx(), cpMin, _tpt);
    _pbt = new PAPBinTable(_mainStream, _tableStream, _dataStream, _fib.getFcPlcfbtePapx(), _fib.getLcbPlcfbtePapx(), cpMin, _tpt);

    // Read FSPA and Escher information
    _fspa = new FSPATable(_tableStream, _fib.getFcPlcspaMom(), _fib.getLcbPlcspaMom(), getTextTable().getTextPieces());

    if (_fib.getFcDggInfo() != 0)
    {
        _dgg = new EscherRecordHolder(_tableStream, _fib.getFcDggInfo(), _fib.getLcbDggInfo());
    } else
    {
        _dgg = new EscherRecordHolder();
    }

    // read in the pictures stream
    _pictures = new PicturesTable(this, _dataStream, _mainStream, _fspa, _dgg);
    // And the art shapes stream
    _officeArts = new ShapesTable(_tableStream, _fib);

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

    PlexOfCps plc = new PlexOfCps(_tableStream, _fib.getFcPlcffldMom(), _fib.getLcbPlcffldMom(), 2);
    for (int x = 0; x < plc.length(); x++)
    {
      GenericPropertyNode node = plc.getProperty(x);
      byte[] fld = node.getBytes();
      int breakpoint = 0;
    }
  }

  public StyleSheet getStyleSheet()
  {
    return _ss;
  }

  public FileInformationBlock getFileInformationBlock()
  {
    return _fib;
  }
  public CPSplitCalculator getCPSplitCalculator()
  {
	return _cpSplit;
  }

  public DocumentProperties getDocProperties()
  {
    return _dop;
  }

  /**
   * Returns the range that covers all text in the
   *  file, including main text, footnotes, headers
   *  and comments
   */
  public Range getOverallRange() {
	  // hack to get the ending cp of the document, Have to revisit this.
	  java.util.List text = _tpt.getTextPieces();
	  PropertyNode p = (PropertyNode)text.get(text.size() - 1);

      return new Range(0, p.getEnd(), this);
  }

  /**
   * Returns the range which covers the whole of the
   *  document, but excludes any headers and footers.
   */
  public Range getRange() {
	  // First up, trigger a full-recalculate
	  // Needed in case of deletes etc
	  getOverallRange();

	  // Now, return the real one
	  return new Range(
			  _cpSplit.getMainDocumentStart(),
			  _cpSplit.getMainDocumentEnd(),
			  this
      );
  }

  /**
   * Returns the range which covers all the Footnotes.
   */
  public Range getFootnoteRange() {
	  return new Range(
			  _cpSplit.getFootnoteStart(),
			  _cpSplit.getFootnoteEnd(),
			  this
      );
  }

  /**
   * Returns the range which covers all the Endnotes.
  */
  public Range getEndnoteRange() {
          return new Range(
                          _cpSplit.getEndNoteStart(),
                          _cpSplit.getEndNoteEnd(),
                          this
      );
  }

  /**
   * Returns the range which covers all the Endnotes.
  */
  public Range getCommentsRange() {
          return new Range(
                          _cpSplit.getCommentsStart(),
                          _cpSplit.getCommentsEnd(),
                          this
      );
  }

  /**
   * Returns the range which covers all "Header Stories".
   * A header story contains a header, footer, end note
   *  separators and footnote separators.
   */
  public Range getHeaderStoryRange() {
	  return new Range(
			  _cpSplit.getHeaderStoryStart(),
			  _cpSplit.getHeaderStoryEnd(),
			  this
      );
  }

  /**
   * Returns the character length of a document.
   * @return the character length of a document
   */
  public int characterLength()
  {
    java.util.List textPieces = _tpt.getTextPieces();
    Iterator textIt = textPieces.iterator();

    int length = 0;
    while(textIt.hasNext())
    {
      TextPiece tp = (TextPiece)textIt.next();
      length += tp.characterLength();
    }
    return length;
  }

  public ListTables getListTables()
  {
    return _lt;
  }

  /**
   * Gets a reference to the saved -by table, which holds the save history for the document.
   *
   * @return the saved-by table.
   */
  public SavedByTable getSavedByTable()
  {
    return _sbt;
  }

  /**
   * @return PicturesTable object, that is able to extract images from this document
   */
  public PicturesTable getPicturesTable() {
	  return _pictures;
  }

  /**
   * @return ShapesTable object, that is able to extract office are shapes from this document
   */
  public ShapesTable getShapesTable() {
	  return _officeArts;
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
    fibSize  += POIFSConstants.BIG_BLOCK_SIZE -
        (fibSize % POIFSConstants.BIG_BLOCK_SIZE);

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

    // write out the Complex table, includes text.
    _fib.setFcClx(tableOffset);
    _cft.writeTo(docSys);
    _fib.setLcbClx(tableStream.getOffset() - tableOffset);
    tableOffset = tableStream.getOffset();
    int fcMac = mainStream.getOffset();

    // write out the CHPBinTable.
    _fib.setFcPlcfbteChpx(tableOffset);
    _cbt.writeTo(docSys, fcMin);
    _fib.setLcbPlcfbteChpx(tableStream.getOffset() - tableOffset);
    tableOffset = tableStream.getOffset();

    // write out the PAPBinTable.
    _fib.setFcPlcfbtePapx(tableOffset);
    _pbt.writeTo(docSys, fcMin);
    _fib.setLcbPlcfbtePapx(tableStream.getOffset() - tableOffset);
    tableOffset = tableStream.getOffset();

    // write out the SectionTable.
    _fib.setFcPlcfsed(tableOffset);
    _st.writeTo(docSys, fcMin);
    _fib.setLcbPlcfsed(tableStream.getOffset() - tableOffset);
    tableOffset = tableStream.getOffset();

    // write out the list tables
    if (_lt != null)
    {
      _fib.setFcPlcfLst(tableOffset);
      _lt.writeListDataTo(tableStream);
      _fib.setLcbPlcfLst(tableStream.getOffset() - tableOffset);

      _fib.setFcPlfLfo(tableStream.getOffset());
      _lt.writeListOverridesTo(tableStream);
      _fib.setLcbPlfLfo(tableStream.getOffset() - tableOffset);
      tableOffset = tableStream.getOffset();
    }

    // write out the saved-by table.
    if (_sbt != null)
    {
      _fib.setFcSttbSavedBy(tableOffset);
      _sbt.writeTo(tableStream);
      _fib.setLcbSttbSavedBy(tableStream.getOffset() - tableOffset);

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
  }

  public CHPBinTable getCharacterTable()
  {
    return _cbt;
  }

  public PAPBinTable getParagraphTable()
  {
    return _pbt;
  }

  public SectionTable getSectionTable()
  {
    return _st;
  }

  public TextPieceTable getTextTable()
  {
    return _cft.getTextPieceTable();
  }

  public byte[] getDataStream()
  {
    return _dataStream;
  }
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

  public FontTable getFontTable()
  {
    return _ft;
  }

  public void delete(int start, int length)
  {
    Range r = new Range(start, start + length, this);
    r.delete();
  }

  /**
   * Takes two arguments, 1) name of the Word file to read in 2) location to
   * write it out at.
   * @param args
   */
  public static void main(String[] args)
  {

    try
    {
      HWPFDocument doc = new HWPFDocument(new FileInputStream(args[0]));
      Range r = doc.getRange();
      String str = r.text();
      int x = 0;
//      CharacterRun run = new CharacterRun();
//      run.setBold(true);
//      run.setItalic(true);
//      run.setCapitalized(true);
//
//      Range range = doc.getRange();
//      range.insertBefore("Hello World!!! HAHAHAHAHA I DID IT!!!", run);
//
//      OutputStream out = new FileOutputStream(args[1]);
//      doc.write(out);
//
//      out.flush();
//      out.close();


    }
    catch (Throwable t)
    {
      t.printStackTrace();
    }
  }

//  public Object clone()
//    throws CloneNotSupportedException
//  {
//    _tpt;
//
//    _cbt;
//
//    _pbt;
//
//    _st;
//
//  }
}
