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

package org.apache.poi.hdf.extractor;


import org.apache.poi.hdf.extractor.util.*;
import org.apache.poi.hdf.extractor.data.*;
import java.util.*;
import java.io.*;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.filesystem.DocumentEntry;

import org.apache.poi.util.LittleEndian;

/**
 * This class contains the main functionality for the Word file "reader". Much
 * of the code in this class is based on the Word 97 document file format. Only
 * works for non-complex files
 *
 * @author Ryan Ackley
 */
public final class WordDocument {
	// TODO - name this constant properly
	private static final float K_1440_0F = 1440.0f;
/** byte buffer containing the main Document stream*/
  byte[] _header;
  /** contains all style information for this document see Word 97 Doc spec*/
  StyleSheet _styleSheet;
  /** contains All list information for this document*/
  ListTables _listTables;
  /** contains global Document properties for this document*/
  DOP _docProps = new DOP();

  int _currentList = -1;
  int _tableSize;
  int _sectionCounter = 1;
  /** fonts available for this document*/
  FontTable _fonts;

  /** document's text blocks*/
  BTreeSet _text = new BTreeSet();
  /** document's character runs */
  BTreeSet _characterTable = new BTreeSet();
  /** document's paragraphs*/
  BTreeSet _paragraphTable = new BTreeSet();
  /** doucment's sections*/
  BTreeSet _sectionTable = new BTreeSet();

  /** used for XSL-FO conversion*/
  StringBuffer _headerBuffer = new StringBuffer();
  /** used for XSL-FO conversion*/
  StringBuffer _bodyBuffer = new StringBuffer();
  /** used for XSL-FO table conversion*/
  StringBuffer _cellBuffer;
  /** used for XSL-FO table conversion*/
  ArrayList _cells;
  /** used for XSL-FO table conversion*/
  ArrayList _table;

  /** document's header and footer information*/
  byte[] _plcfHdd;

  /** starting position of text in main document stream*/
  int _fcMin;
  /** length of main document text stream*/
  int _ccpText;
  /** length of footnotes text*/
  int _ccpFtn;

  /** The name of the file to write to */
  private static String _outName;

  /** OLE stuff*/
  private InputStream istream;
  /** OLE stuff*/
  private POIFSFileSystem filesystem;

  //used internally
  private static int HEADER_EVEN_INDEX = 0;
  private static int HEADER_ODD_INDEX = 1;
  private static int FOOTER_EVEN_INDEX = 2;
  private static int FOOTER_ODD_INDEX = 3;
  private static int HEADER_FIRST_INDEX = 4;
  private static int FOOTER_FIRST_INDEX = 5;

  /**
   *  right now this function takes one parameter: a Word file, and outputs an
   *  XSL-FO document at c:\test.xml (this is hardcoded)
   */
  public static void main(String args[])
  {
      /*try
      {
        WordDocument file = new WordDocument(args[0], "r");
        Writer out = new BufferedWriter(new FileWriter(args[1]));
        file.writeAllText(out);
        out.flush();
        out.close();
      }
      catch(Throwable t)
      {
        t.printStackTrace();
      }*/
      try
      {
          _outName = args[1];
          WordDocument file = new WordDocument(args[0]);
          file.closeDoc();
      }
      catch(Exception e)
      {
          e.printStackTrace();
      }
      System.exit(0);
  }
  /**
   * Spits out the document text
   *
   * @param out The Writer to write the text to.
   * @throws IOException if there is a problem while reading from the file or
   *         writing out the text.
   */
  public void writeAllText(Writer out) throws IOException
  {
    int textStart = Utils.convertBytesToInt(_header, 0x18);
    int textEnd = Utils.convertBytesToInt(_header, 0x1c);
    ArrayList textPieces = findProperties(textStart, textEnd, _text.root);
    int size = textPieces.size();

    for(int x = 0; x < size; x++)
    {
      TextPiece nextPiece = (TextPiece)textPieces.get(x);
      int start = nextPiece.getStart();
      int end = nextPiece.getEnd();
      boolean unicode = nextPiece.usesUnicode();
      int add = 1;

      if(unicode)
      {
        add = 2;
        char ch;
        for(int y = start; y < end; y += add)
        {
	  ch = (char)Utils.convertBytesToShort(_header, y);
	  out.write(ch);
        }
      }
      else
      {
	String sText = new String(_header, start, end-start);
	out.write(sText);
      }
    }
  }
  /**
   * Constructs a Word document from fileName. Parses the document and places
   * all the important stuff into data structures.
   *
   * @param fileName The name of the file to read.
   * @throws IOException if there is a problem while parsing the document.
   */
  public WordDocument(String fileName) throws IOException
  {
  	this(new FileInputStream(fileName));
  }

  public WordDocument(InputStream inputStream) throws IOException
  {
        //do Ole stuff
        istream = inputStream;
        filesystem = new POIFSFileSystem(istream);

        //get important stuff from the Header block and parse all the
        //data structures
        readFIB();

        //get the SEPS for the main document text
        ArrayList sections = findProperties(_fcMin, _fcMin + _ccpText, _sectionTable.root);

        //iterate through sections, paragraphs, and character runs doing what
        //you will with the data.
        int size = sections.size();
        for(int x = 0; x < size; x++)
        {
          SepxNode node = (SepxNode)sections.get(x);
          int start = node.getStart();
          int end = node.getEnd();
          SEP sep = (SEP)StyleSheet.uncompressProperty(node.getSepx(), new SEP(), _styleSheet);
          writeSection(Math.max(_fcMin, start), Math.min(_fcMin + _ccpText, end), sep, _text, _paragraphTable, _characterTable, _styleSheet);
        }
        //finish
        istream.close();

  }
  /**
   * Extracts the main document stream from the POI file then hands off to other
   * functions that parse other areas.
   *
   * @throws IOException
   */
  private void readFIB() throws IOException
  {
      //get the main document stream
      DocumentEntry headerProps =
        (DocumentEntry)filesystem.getRoot().getEntry("WordDocument");

      //I call it the header but its also the main document stream
      _header = new byte[headerProps.getSize()];
      filesystem.createDocumentInputStream("WordDocument").read(_header);

      //Get the information we need from the header
      int info = LittleEndian.getShort(_header, 0xa);

      _fcMin = LittleEndian.getInt(_header, 0x18);
      _ccpText = LittleEndian.getInt(_header, 0x4c);
      _ccpFtn = LittleEndian.getInt(_header, 0x50);

      int charPLC = LittleEndian.getInt(_header, 0xfa);
      int charPlcSize = LittleEndian.getInt(_header, 0xfe);
      int parPLC = LittleEndian.getInt(_header, 0x102);
      int parPlcSize = LittleEndian.getInt(_header, 0x106);
      boolean useTable1 = (info & 0x200) != 0;

      //process the text and formatting properties
      processComplexFile(useTable1, charPLC, charPlcSize, parPLC, parPlcSize);
  }

  /**
   * Extracts the correct Table stream from the POI filesystem then hands off to
   * other functions to process text and formatting info. the name is based on
   * the fact that in Word 8(97) all text (not character or paragraph formatting)
   * is stored in complex format.
   *
   * @param useTable1 boolean that specifies if we should use table1 or table0
   * @param charTable offset in table stream of character property bin table
   * @param charPlcSize size of character property bin table
   * @param parTable offset in table stream of paragraph property bin table.
   * @param parPlcSize size of paragraph property bin table.
   * @return boolean indocating success of
   * @throws IOException
   */
  private void processComplexFile(boolean useTable1, int charTable,
                                     int charPlcSize, int parTable, int parPlcSize) throws IOException
  {

      //get the location of the piece table
      int complexOffset = LittleEndian.getInt(_header, 0x1a2);

      String tablename=null;
      DocumentEntry tableEntry = null;
      if(useTable1)
      {
          tablename="1Table";
      }
      else
      {
          tablename="0Table";
      }
      tableEntry = (DocumentEntry)filesystem.getRoot().getEntry(tablename);

      //load the table stream into a buffer
      int size = tableEntry.getSize();
      byte[] tableStream = new byte[size];
      filesystem.createDocumentInputStream(tablename).read(tableStream);

      //init the DOP for this document
      initDocProperties(tableStream);
      //load the header/footer raw data for this document
      initPclfHdd(tableStream);
      //parse out the text locations
      findText(tableStream, complexOffset);
      //parse out text formatting
      findFormatting(tableStream, charTable, charPlcSize, parTable, parPlcSize);

  }
  /**
   * Goes through the piece table and parses out the info regarding the text
   * blocks. For Word 97 and greater all text is stored in the "complex" way
   * because of unicode.
   *
   * @param tableStream buffer containing the main table stream.
   * @param beginning of the complex data.
   * @throws IOException
   */
  private void findText(byte[] tableStream, int complexOffset) throws IOException
  {
    //actual text
    int pos = complexOffset;
    //skips through the prms before we reach the piece table. These contain data
    //for actual fast saved files
    while(tableStream[pos] == 1)
    {
        pos++;
        int skip = LittleEndian.getShort(tableStream, pos);
        pos += 2 + skip;
    }
    if(tableStream[pos] != 2)
    {
        throw new IOException("corrupted Word file");
    }
    //parse out the text pieces
    int pieceTableSize = LittleEndian.getInt(tableStream, ++pos);
    pos += 4;
    int pieces = (pieceTableSize - 4) / 12;
    for (int x = 0; x < pieces; x++)
    {
        int filePos = LittleEndian.getInt(tableStream, pos + ((pieces + 1) * 4) + (x * 8) + 2);
        boolean unicode = false;
        if ((filePos & 0x40000000) == 0)
        {
            unicode = true;
        }
        else
        {
            unicode = false;
            filePos &= ~(0x40000000);//gives me FC in doc stream
            filePos /= 2;
        }
        int totLength = LittleEndian.getInt(tableStream, pos + (x + 1) * 4) -
                        LittleEndian.getInt(tableStream, pos + (x * 4));

        TextPiece piece = new TextPiece(filePos, totLength, unicode);
        _text.add(piece);
    }
  }

  /**
   * Does all of the formatting parsing
   *
   * @param tableStream Main table stream buffer.
   * @param charOffset beginning of the character bin table.
   * @param chrPlcSize size of the char bin table.
   * @param parOffset offset of the paragraph bin table.
   * @param size of the paragraph bin table.
   */
  private void findFormatting(byte[] tableStream, int charOffset,
                              int charPlcSize, int parOffset, int parPlcSize) {
      openDoc();
      createStyleSheet(tableStream);
      createListTables(tableStream);
      createFontTable(tableStream);

      //find character runs
      //Get all the chpx info and store it

      int arraySize = (charPlcSize - 4)/8;

      //first we must go through the bin table and find the fkps
      for(int x = 0; x < arraySize; x++)
      {


          //get page number(has nothing to do with document page)
          //containing the chpx for the paragraph
          int PN = LittleEndian.getInt(tableStream, charOffset + (4 * (arraySize + 1) + (4 * x)));

          byte[] fkp = new byte[512];
          System.arraycopy(_header, (PN * 512), fkp, 0, 512);
          //take each fkp and get the chpxs
          int crun = Utils.convertUnsignedByteToInt(fkp[511]);
          for(int y = 0; y < crun; y++)
          {
              //get the beginning fc of each paragraph text run
              int fcStart = LittleEndian.getInt(fkp, y * 4);
              int fcEnd = LittleEndian.getInt(fkp, (y+1) * 4);
              //get the offset in fkp of the papx for this paragraph
              int chpxOffset = 2 * Utils.convertUnsignedByteToInt(fkp[((crun + 1) * 4) + y]);

              //optimization if offset == 0 use "Normal" style
              if(chpxOffset == 0)

              {
                _characterTable.add(new ChpxNode(fcStart, fcEnd, new byte[0]));
                continue;
              }

              int size = Utils.convertUnsignedByteToInt(fkp[chpxOffset]);

              byte[] chpx = new byte[size];
              System.arraycopy(fkp, ++chpxOffset, chpx, 0, size);
              //_papTable.put(Integer.valueOf(fcStart), papx);
              _characterTable.add(new ChpxNode(fcStart, fcEnd, chpx));
          }

      }

      //find paragraphs
      arraySize = (parPlcSize - 4)/8;
      //first we must go through the bin table and find the fkps
      for(int x = 0; x < arraySize; x++)
      {
          int PN = LittleEndian.getInt(tableStream, parOffset + (4 * (arraySize + 1) + (4 * x)));

          byte[] fkp = new byte[512];
          System.arraycopy(_header, (PN * 512), fkp, 0, 512);
          //take each fkp and get the paps
          int crun = Utils.convertUnsignedByteToInt(fkp[511]);
          for(int y = 0; y < crun; y++)
          {
              //get the beginning fc of each paragraph text run
              int fcStart = LittleEndian.getInt(fkp, y * 4);
              int fcEnd = LittleEndian.getInt(fkp, (y+1) * 4);
              //get the offset in fkp of the papx for this paragraph
              int papxOffset = 2 * Utils.convertUnsignedByteToInt(fkp[((crun + 1) * 4) + (y * 13)]);
              int size = 2 * Utils.convertUnsignedByteToInt(fkp[papxOffset]);
              if(size == 0)
              {
                  size = 2 * Utils.convertUnsignedByteToInt(fkp[++papxOffset]);
              }
              else
              {
                  size--;
              }

              byte[] papx = new byte[size];
              System.arraycopy(fkp, ++papxOffset, papx, 0, size);
              _paragraphTable.add(new PapxNode(fcStart, fcEnd, papx));

          }

      }

      //find sections
      int fcMin = Utils.convertBytesToInt(_header, 0x18);
      int plcfsedFC = Utils.convertBytesToInt(_header, 0xca);
      int plcfsedSize = Utils.convertBytesToInt(_header, 0xce);
      byte[] plcfsed = new byte[plcfsedSize];
      System.arraycopy(tableStream, plcfsedFC, plcfsed, 0, plcfsedSize);

      arraySize = (plcfsedSize - 4)/16;

      //openDoc();

      for(int x = 0; x < arraySize; x++)
      {
          int sectionStart = Utils.convertBytesToInt(plcfsed, x * 4) + fcMin;
          int sectionEnd = Utils.convertBytesToInt(plcfsed, (x+1) * 4) + fcMin;
          int sepxStart = Utils.convertBytesToInt(plcfsed, 4 * (arraySize + 1) + (x * 12) + 2);
          int sepxSize = Utils.convertBytesToShort(_header, sepxStart);
          byte[] sepx = new byte[sepxSize];
          System.arraycopy(_header, sepxStart + 2, sepx, 0, sepxSize);
          SepxNode node = new SepxNode(x + 1, sectionStart, sectionEnd, sepx);
          _sectionTable.add(node);
      }


  }

  public void openDoc()
  {
    _headerBuffer.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\r\n");
    _headerBuffer.append("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">\r\n");
    _headerBuffer.append("<fo:layout-master-set>\r\n");

  }
  private HeaderFooter findSectionHdrFtr(int type, int index)
  {
    if(_plcfHdd.length < 50)
    {
      return new HeaderFooter(0,0,0);
    }
    int start = _fcMin + _ccpText + _ccpFtn;
    int end = start;
    int arrayIndex = 0;

    switch(type)
    {
      case HeaderFooter.HEADER_EVEN:
           arrayIndex = (HEADER_EVEN_INDEX + (index * 6));
           break;
      case HeaderFooter.FOOTER_EVEN:
           arrayIndex = (FOOTER_EVEN_INDEX + (index * 6));
           break;
      case HeaderFooter.HEADER_ODD:
           arrayIndex = (HEADER_ODD_INDEX + (index * 6));
           break;
      case HeaderFooter.FOOTER_ODD:
           arrayIndex = (FOOTER_ODD_INDEX + (index * 6));
           break;
      case HeaderFooter.HEADER_FIRST:
           arrayIndex = (HEADER_FIRST_INDEX + (index * 6));
           break;
      case HeaderFooter.FOOTER_FIRST:
           arrayIndex = (FOOTER_FIRST_INDEX + (index * 6));
           break;
    }
    start += Utils.convertBytesToInt(_plcfHdd, (arrayIndex * 4));
    end += Utils.convertBytesToInt(_plcfHdd, (arrayIndex + 1) * 4);

    HeaderFooter retValue = new HeaderFooter(type, start, end);

    if((end - start) == 0 && index > 1)
    {
      retValue = findSectionHdrFtr(type, index - 1);
    }
    return retValue;
  }
  /**
   * inits this document DOP structure.
   *
   * @param tableStream The documents table stream.
   */
  private void initDocProperties(byte[] tableStream)
  {
    int pos = LittleEndian.getInt(_header, 0x192);
    int size = LittleEndian.getInt(_header, 0x196);
    byte[] dop = new byte[size];

    System.arraycopy(tableStream, pos, dop, 0, size);

    _docProps._fFacingPages = (dop[0] & 0x1) > 0;
    _docProps._fpc = (dop[0] & 0x60) >> 5;

    short num = LittleEndian.getShort(dop, 2);
    _docProps._rncFtn = (num & 0x3);
    _docProps._nFtn = (short)(num & 0xfffc) >> 2;
    num = LittleEndian.getShort(dop, 52);
    _docProps._rncEdn = num & 0x3;
    _docProps._nEdn = (short)(num & 0xfffc) >> 2;
    num = LittleEndian.getShort(dop, 54);
    _docProps._epc = num & 0x3;
  }

  public void writeSection(int start, int end, SEP sep, BTreeSet text,
                           BTreeSet paragraphTable, BTreeSet characterTable,
                           StyleSheet stylesheet)
  {

    HeaderFooter titleHeader = findSectionHdrFtr(HeaderFooter.HEADER_FIRST, _sectionCounter);
    HeaderFooter titleFooter = findSectionHdrFtr(HeaderFooter.FOOTER_FIRST, _sectionCounter);
    HeaderFooter oddHeader = findSectionHdrFtr(HeaderFooter.HEADER_ODD, _sectionCounter);
    HeaderFooter evenHeader = findSectionHdrFtr(HeaderFooter.HEADER_EVEN, _sectionCounter);
    HeaderFooter oddFooter = findSectionHdrFtr(HeaderFooter.FOOTER_ODD, _sectionCounter);
    HeaderFooter evenFooter = findSectionHdrFtr(HeaderFooter.FOOTER_EVEN, _sectionCounter);

    String titlePage = null;
    String evenPage = null;
    String oddPage = null;
    String regPage = null;

    String sequenceName = null;

    /*if(sep._fTitlePage)
    {
      titlePage = createPageMaster(sep, "first", _sectionCounter, createRegion("before", "title-header"), createRegion("after", "title-footer"));

      if(!titleHeader.isEmpty())
      {
        addStaticContent("title-header" + _sectionCounter, titleHeader);
      }
      if(!titleFooter.isEmpty())
      {
        addStaticContent("title-footer" + _sectionCounter, titleFooter);
      }
    }*/

    if(_docProps._fFacingPages)
    {
      if(sep._fTitlePage)
      {
        String before = createRegion(true, titleHeader, sep, "title-header" + _sectionCounter);
        String after = createRegion(false, titleFooter, sep, "title-footer" + _sectionCounter);
        titlePage = createPageMaster(sep, "first", _sectionCounter, before, after);
      }
      String before = createRegion(true, evenHeader, sep, "even-header" + _sectionCounter);
      String after = createRegion(false, evenFooter, sep, "even-footer" + _sectionCounter);
      evenPage = createPageMaster(sep, "even", _sectionCounter, before, after);
      before = createRegion(true, oddHeader, sep, "odd-header" + _sectionCounter);
      after = createRegion(false, oddFooter, sep, "odd-footer" + _sectionCounter);
      oddPage = createPageMaster(sep, "odd", _sectionCounter, before, after);
      sequenceName = createEvenOddPageSequence(titlePage, evenPage, oddPage, _sectionCounter);

      openPage(sequenceName, "reference");

      if(sep._fTitlePage)
      {


        if(!titleHeader.isEmpty())
        {
          addStaticContent("title-header" + _sectionCounter, titleHeader);
        }
        if(!titleFooter.isEmpty())
        {
          addStaticContent("title-footer" + _sectionCounter, titleFooter);
        }
      }

      //handle the headers and footers for odd and even pages
      if(!oddHeader.isEmpty())
      {
        addStaticContent("odd-header" + _sectionCounter, oddHeader);
      }
      if(!oddFooter.isEmpty())
      {
        addStaticContent("odd-footer" + _sectionCounter, oddFooter);
      }
      if(!evenHeader.isEmpty())
      {
        addStaticContent("even-header" + _sectionCounter, evenHeader);
      }
      if(!evenFooter.isEmpty())
      {
        addStaticContent("even-footer" + _sectionCounter, evenFooter);
      }
      openFlow();
      addBlockContent(start, end, text, paragraphTable, characterTable);
      closeFlow();
      closePage();
    }
    else
    {
      /*if(sep._fTitlePage)
      {
        String before = createRegion(true, titleHeader, sep);
        String after = createRegion(false, titleFooter, sep);
        titlePage = createPageMaster(sep, "first", _sectionCounter, before, after);
      }*/
      String before = createRegion(true, oddHeader, sep, null);
      String after = createRegion(false, oddFooter, sep, null);
      regPage = createPageMaster(sep, "page", _sectionCounter, before, after);

      if(sep._fTitlePage)
      {
        before = createRegion(true, titleHeader, sep, "title-header" + _sectionCounter);
        after = createRegion(false, titleFooter, sep, "title-footer" + _sectionCounter);
        titlePage = createPageMaster(sep, "first", _sectionCounter, before, after);
        sequenceName = createPageSequence(titlePage, regPage, _sectionCounter);
        openPage(sequenceName, "reference");

        if(!titleHeader.isEmpty())
        {
          addStaticContent("title-header" + _sectionCounter, titleHeader);
        }
        if(!titleFooter.isEmpty())
        {
          addStaticContent("title-footer" + _sectionCounter, titleFooter);
        }
      }
      else
      {
        openPage(regPage, "name");
      }
      if(!oddHeader.isEmpty())
      {
        addStaticContent("xsl-region-before", oddHeader);
      }
      if(!oddFooter.isEmpty())
      {
        addStaticContent("xsl-region-after", oddFooter);
      }
      openFlow();
      addBlockContent(start, end, text, paragraphTable, characterTable);
      closeFlow();
      closePage();
    }
    _sectionCounter++;
  }

  private int calculateHeaderHeight(int start, int end, int pageWidth)
  {
    ArrayList paragraphs = findProperties(start, end, _paragraphTable.root);
    int size = paragraphs.size();
    ArrayList lineHeights = new ArrayList();
    //StyleContext context = StyleContext.getDefaultStyleContext();

    for(int x = 0; x < size; x++)
    {
      PapxNode node = (PapxNode)paragraphs.get(x);
      int parStart = Math.max(node.getStart(), start);
      int parEnd = Math.min(node.getEnd(), end);

      int lineWidth = 0;
      int maxHeight = 0;

      ArrayList textRuns = findProperties(parStart, parEnd, _characterTable.root);
      int charSize = textRuns.size();

      //StringBuffer lineBuffer = new StringBuffer();
      for(int y = 0; y < charSize; y++)
      {
        ChpxNode charNode = (ChpxNode)textRuns.get(y);
        int istd = Utils.convertBytesToShort(node.getPapx(), 0);
        StyleDescription sd = _styleSheet.getStyleDescription(istd);
        CHP chp = (CHP)StyleSheet.uncompressProperty(charNode.getChpx(), sd.getCHP(), _styleSheet);

        //get Font info
        //FontMetrics metrics = getFontMetrics(chp, context);

        int height = 10;//metrics.getHeight();
        maxHeight = Math.max(maxHeight, height);

        int charStart = Math.max(parStart, charNode.getStart());
        int charEnd = Math.min(parEnd, charNode.getEnd());

        ArrayList text = findProperties(charStart, charEnd, _text.root);

        int textSize = text.size();
        StringBuffer buf = new StringBuffer();
        for(int z = 0; z < textSize; z++)
        {

          TextPiece piece = (TextPiece)text.get(z);
          int textStart = Math.max(piece.getStart(), charStart);
          int textEnd = Math.min(piece.getEnd(), charEnd);

          if(piece.usesUnicode())
          {
            addUnicodeText(textStart, textEnd, buf);
          }
          else
          {
            addText(textStart, textEnd, buf);
          }
        }

        String tempString = buf.toString();
        lineWidth += 10 * tempString.length();//metrics.stringWidth(tempString);
        if(lineWidth > pageWidth)
        {
          lineHeights.add(Integer.valueOf(maxHeight));
          maxHeight = 0;
          lineWidth = 0;
        }
      }
      lineHeights.add(Integer.valueOf(maxHeight));
    }
    int sum = 0;
    size = lineHeights.size();
    for(int x = 0; x < size; x++)
    {
      Integer height = (Integer)lineHeights.get(x);
      sum += height.intValue();
    }

    return sum;
  }
/*  private FontMetrics getFontMetrics(CHP chp, StyleContext context)
  {
    String fontName = _fonts.getFont(chp._ftcAscii);
    int style = 0;
    if(chp._bold)
    {
      style |= Font.BOLD;
    }
    if(chp._italic)
    {
      style |= Font.ITALIC;
    }

    Font font = new Font(fontName, style, chp._hps/2);


    return context.getFontMetrics(font);
  }*/
  private String createRegion(boolean before, HeaderFooter header, SEP sep, String name)
  {
    if(header.isEmpty())
    {
      return "";
    }
    String region = "region-name=\"" + name + "\"";
    if(name == null)
    {
      region = "";
    }
    int height = calculateHeaderHeight(header.getStart(), header.getEnd(), sep._xaPage/20);
    int marginTop = 0;
    int marginBottom = 0;
    int extent = 0;
    String where = null;
    String align = null;

    if(before)
    {
      where = "before";
      align = "before";
      marginTop = sep._dyaHdrTop/20;
      extent = height + marginTop;
      sep._dyaTop = Math.max(extent*20, sep._dyaTop);
    }
    else
    {
      where = "after";
      align = "after";
      marginBottom = sep._dyaHdrBottom/20;
      extent = height + marginBottom;
      sep._dyaBottom = Math.max(extent*20, sep._dyaBottom);
    }

    int marginLeft = sep._dxaLeft/20;
    int marginRight = sep._dxaRight/20;

    return "<fo:region-" + where + " display-align=\"" + align + "\" extent=\""
             + extent + "pt\" "+region+"/>";
// org.apache.fop.fo.expr.PropertyException:
// Border and padding for region "xsl-region-before" must be '0'
// (See 6.4.13 in XSL 1.0).
//             extent + "pt\" padding-left=\"" + marginLeft + "pt\" padding-right=\"" +
//             marginRight + "pt\" padding-top=\"" + marginTop + "pt\" padding-bottom=\"" +
//             marginBottom + "pt\" " + region + "/>";

  }
  private String createRegion(String where, String name)
  {
    return "<fo:region-" + where + " overflow=\"scroll\" region-name=\"" + name + "\"/>";
  }
  private String createEvenOddPageSequence(String titlePage, String evenPage, String oddPage, int counter)
  {
    String name = "my-sequence" + counter;
    _headerBuffer.append("<fo:page-sequence-master master-name=\"" + name + "\"> ");
    _headerBuffer.append("<fo:repeatable-page-master-alternatives>");
    if(titlePage != null)
    {
      _headerBuffer.append("<fo:conditional-page-master-reference " +
                           "page-position=\"first\" master-reference=\"" +
                            titlePage + "\"/>");
    }
    _headerBuffer.append("<fo:conditional-page-master-reference odd-or-even=\"odd\" ");
    _headerBuffer.append("master-reference=\""+ oddPage + "\"/> ");
    _headerBuffer.append("<fo:conditional-page-master-reference odd-or-even=\"even\" ");
    _headerBuffer.append("master-reference=\"" + evenPage + "\"/> ");
    _headerBuffer.append("</fo:repeatable-page-master-alternatives>");
    _headerBuffer.append("</fo:page-sequence-master>");
    return name;
  }
  private String createPageSequence(String titlePage, String regPage, int counter)
  {
    String name = null;
    if(titlePage != null)
    {
      name = "my-sequence" + counter;
      _headerBuffer.append("<fo:page-sequence-master master-name=\"" + name + "\"> ");
      _headerBuffer.append("<fo:single-page-master-reference master-reference=\"" + titlePage + "\"/>");
      _headerBuffer.append("<fo:repeatable-page-master-reference master-reference=\"" + regPage + "\"/>");
      _headerBuffer.append("</fo:page-sequence-master>");
    }
    return name;
  }
  private void addBlockContent(int start, int end, BTreeSet text,
                              BTreeSet paragraphTable, BTreeSet characterTable)
  {

    BTreeSet.BTreeNode root = paragraphTable.root;
    ArrayList pars = findProperties(start, end, root);
    //root = characterTable.root;
    int size = pars.size();

    for(int c = 0; c < size; c++)
    {
      PapxNode currentNode = (PapxNode)pars.get(c);
      createParagraph(start, end, currentNode, characterTable, text);
    }
    //closePage();
  }
  private String getTextAlignment(byte jc)
  {
    switch(jc)
    {
      case 0:
        return "start";
      case 1:
        return "center";
      case 2:
        return "end";
      case 3:
        return "justify";
      default:
        return "left";
    }
  }
  private void createParagraph(int start, int end, PapxNode currentNode,
                               BTreeSet characterTable, BTreeSet text)
  {
    StringBuffer blockBuffer = _bodyBuffer;
    byte[] papx = currentNode.getPapx();
    int istd = Utils.convertBytesToShort(papx, 0);
    StyleDescription std = _styleSheet.getStyleDescription(istd);
    PAP pap = (PAP)StyleSheet.uncompressProperty(papx, std.getPAP(), _styleSheet);

    //handle table cells
    if(pap._fInTable > 0)
    {
      if(pap._fTtp == 0)
      {
        if(_cellBuffer == null)
        {
          _cellBuffer = new StringBuffer();
        }
        blockBuffer = _cellBuffer;
      }
      else
      {
        if(_table == null)
        {
          _table = new ArrayList();
        }
        TAP tap = (TAP)StyleSheet.uncompressProperty(papx, new TAP(), _styleSheet);
        TableRow nextRow = new TableRow(_cells, tap);
        _table.add(nextRow);
        _cells = null;
        return;
      }
    }
    else
    {
      //just prints out any table that is stored in _table
      printTable();
    }

    if(pap._ilfo > 0)
    {
      LVL lvl = _listTables.getLevel(pap._ilfo, pap._ilvl);
      addListParagraphContent(lvl, blockBuffer, pap, currentNode, start, end, std);
    }
    else
    {
      addParagraphContent(blockBuffer, pap, currentNode, start, end, std);
    }

  }

  private void addListParagraphContent(LVL lvl, StringBuffer blockBuffer, PAP pap,
                                       PapxNode currentNode, int start, int end,
                                       StyleDescription std)
  {
    pap = (PAP)StyleSheet.uncompressProperty(lvl._papx, pap, _styleSheet, false);

    addParagraphProperties(pap, blockBuffer);

    ArrayList charRuns = findProperties(Math.max(currentNode.getStart(), start),
                                     Math.min(currentNode.getEnd(), end),
                                     _characterTable.root);
    int len = charRuns.size();

    CHP numChp = (CHP)StyleSheet.uncompressProperty(((ChpxNode)charRuns.get(len-1)).getChpx(), std.getCHP(), _styleSheet);

    numChp = (CHP)StyleSheet.uncompressProperty(lvl._chpx, numChp, _styleSheet);

    //StyleContext context = StyleContext.getDefaultStyleContext();
    //FontMetrics metrics = getFontMetrics(numChp, context);
    int indent = -1 * pap._dxaLeft1;
    String bulletText = getBulletText(lvl, pap);

    indent = indent - (bulletText.length() * 10) * 20;//(metrics.stringWidth(bulletText) * 20);

    if(indent > 0)
    {
      numChp._paddingEnd = (short)indent;
    }

    addCharacterProperties(numChp, blockBuffer);
    int listNum = 0;

    //if(number != null)
    //{
    blockBuffer.append(bulletText);
      //listNum = 1;
    //}

    //for(;listNum < lvl._xst.length; listNum++)
    //{
    //  addText(lvl._xst[listNum], blockBuffer);
    //}


    switch (lvl._ixchFollow)
    {
      case 0:
        addText('\u0009', blockBuffer);
        break;
      case 1:
        addText(' ', blockBuffer);
        break;
    }

    closeLine(blockBuffer);
    for(int x = 0; x < len; x++)
    {
      ChpxNode charNode = (ChpxNode)charRuns.get(x);
      byte[] chpx = charNode.getChpx();
      CHP chp = (CHP)StyleSheet.uncompressProperty(chpx, std.getCHP(), _styleSheet);


      addCharacterProperties(chp, blockBuffer);

      int charStart = Math.max(charNode.getStart(), currentNode.getStart());
      int charEnd = Math.min(charNode.getEnd(), currentNode.getEnd());
      ArrayList textRuns = findProperties(charStart, charEnd, _text.root);
      int textRunLen = textRuns.size();
      for(int y = 0; y < textRunLen; y++)
      {
        TextPiece piece = (TextPiece)textRuns.get(y);
        charStart = Math.max(charStart, piece.getStart());
        charEnd = Math.min(charEnd, piece.getEnd());

        if(piece.usesUnicode())
        {
          addUnicodeText(charStart, charEnd, blockBuffer);
        }
        else
        {
          addText(charStart, charEnd, blockBuffer);
        }
        closeLine(blockBuffer);
      }
    }
    closeBlock(blockBuffer);
  }

  private void addParagraphContent(StringBuffer blockBuffer, PAP pap,
                                   PapxNode currentNode, int start, int end,
                                   StyleDescription std)
  {
    addParagraphProperties(pap, blockBuffer);

    ArrayList charRuns = findProperties(Math.max(currentNode.getStart(), start),
                                     Math.min(currentNode.getEnd(), end),
                                     _characterTable.root);
    int len = charRuns.size();

    for(int x = 0; x < len; x++)
    {
      ChpxNode charNode = (ChpxNode)charRuns.get(x);
      byte[] chpx = charNode.getChpx();
      CHP chp = (CHP)StyleSheet.uncompressProperty(chpx, std.getCHP(), _styleSheet);

      addCharacterProperties(chp, blockBuffer);

      int charStart = Math.max(charNode.getStart(), currentNode.getStart());
      int charEnd = Math.min(charNode.getEnd(), currentNode.getEnd());
      ArrayList textRuns = findProperties(charStart, charEnd, _text.root);
      int textRunLen = textRuns.size();
      for(int y = 0; y < textRunLen; y++)
      {
        TextPiece piece = (TextPiece)textRuns.get(y);
        charStart = Math.max(charStart, piece.getStart());
        charEnd = Math.min(charEnd, piece.getEnd());

        if(piece.usesUnicode())
        {
          addUnicodeText(charStart, charEnd, blockBuffer);
        }
        else
        {
          addText(charStart, charEnd, blockBuffer);
        }
        closeLine(blockBuffer);
      }
    }
    closeBlock(blockBuffer);
  }
  private void addText(int start, int end, StringBuffer buf)
  {
    for(int x = start; x < end; x++)
    {
      char ch = '?';


      ch = (char)_header[x];

      addText(ch, buf);
    }
  }
  private void addText(char ch, StringBuffer buf)
  {
    int num = 0xffff & ch;
    if((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') ||
      (ch >= '0' && ch <= '9') || ch == '_' || ch == ' ' || ch == '-' || ch == '.' || ch == '$')
    {
      buf.append(ch);
    }
    else if(num == 0x07 && _cellBuffer != null)
    {

      if(_cells == null)
      {
        _cells = new ArrayList();
      }
      closeLine(_cellBuffer);
      closeBlock(_cellBuffer);
      _cells.add(_cellBuffer.toString());
      _cellBuffer = null;

    }

    else
    {
      /** @todo handle special characters */
      if(num < 0x20)
      num=0x20;
      buf.append("&#");
      buf.append(num);
      buf.append(';');
    }
  }
  private void addUnicodeText(int start, int end, StringBuffer buf)
  {
    for(int x = start; x < end; x += 2)
    {
      char ch = Utils.getUnicodeCharacter(_header, x);
      //if(ch < 0x0020)
      //{
      //  _bodyBuffer.append('?');
      //}
      //else
      //{
        addText(ch, buf);
      //}
    }
  }
  private void addParagraphProperties(PAP pap, StringBuffer buf)
  {
    buf.append("<fo:block ");
    buf.append("text-align=\"" + getTextAlignment(pap._jc) + "\"\r\n");
    buf.append("linefeed-treatment=\"preserve\" ");
    buf.append("white-space-collapse=\"false\" ");

    if(pap._fKeep > 0)
    {
      buf.append("keep-together.within-page=\"always\"\r\n");
    }
    if(pap._fKeepFollow > 0)
    {
      buf.append("keep-with-next.within-page=\"always\"\r\n");
    }
    if(pap._fPageBreakBefore > 0)
    {
      buf.append("break-before=\"page\"\r\n");
    }
    if(pap._fNoAutoHyph == 0)
    {
      buf.append("hyphenate=\"true\"\r\n");
    }
    else
    {
      buf.append("hyphenate=\"false\"\r\n");
    }
    if(pap._dxaLeft > 0)
    {
      buf.append("start-indent=\"" + pap._dxaLeft/K_1440_0F + "in\"\r\n");
    }
    if(pap._dxaRight > 0)
    {
      buf.append("end-indent=\"" + pap._dxaRight/K_1440_0F + "in\"\r\n");
    }
    if(pap._dxaLeft1 != 0)
    {
      buf.append("text-indent=\"" + pap._dxaLeft1/K_1440_0F + "in\"\r\n");
    }
    if(pap._lspd[1] == 0)
    {
      //buf.append("line-height=\"" + pap._lspd[0]/K_1440_0F + "in\"\r\n");
    }
    addBorder(buf, pap._brcTop, "top");
    addBorder(buf, pap._brcBottom, "bottom");
    addBorder(buf, pap._brcLeft, "left");
    addBorder(buf, pap._brcRight, "right");

    buf.append(">");

  }

  private void addCharacterProperties(CHP chp, StringBuffer buf)
  {
    buf.append("<fo:inline ");
    buf.append("font-family=\"" + _fonts.getFont(chp._ftcAscii) + "\" ");
    buf.append("font-size=\"" + (chp._hps / 2) + "pt\" ");
    buf.append("color=\"" + getColor(chp._ico) + "\" ");
    //not supported by fop
    //buf.append("letter-spacing=\"" + ((double)chp._dxaSpace)/K_1440_0F + "in\" ");

    addBorder(buf, chp._brc, "top");
    addBorder(buf, chp._brc, "bottom");
    addBorder(buf, chp._brc, "left");
    addBorder(buf, chp._brc, "right");

    if(chp._italic)
    {
      buf.append("font-style=\"italic\" ");
    }
    if(chp._bold)
    {
      buf.append("font-weight=\"bold\" ");
    }
    if(chp._fSmallCaps)
    {
      buf.append("font-variant=\"small-caps\" ");
    }
    if(chp._fCaps)
    {
      buf.append("text-transform=\"uppercase\" ");
    }
    if(chp._fStrike || chp._fDStrike)
    {
      buf.append("text-decoration=\"line-through\" ");
    }
    if(chp._fShadow)
    {
      int size = chp._hps/24;
      buf.append("text-shadow=\"" + size + "pt\"");
    }
    if(chp._fLowerCase)
    {
      buf.append("text-transform=\"lowercase\" ");
    }
    if(chp._kul > 0)
    {
      buf.append("text-decoration=\"underline\" ");
    }
    if(chp._highlighted)
    {
      buf.append("background-color=\"" + getColor(chp._icoHighlight) + "\" ");
    }
    if(chp._paddingStart != 0)
    {
      buf.append("padding-start=\"" + chp._paddingStart/K_1440_0F + "in\" ");
    }
    if(chp._paddingEnd != 0)
    {
      buf.append("padding-end=\"" + chp._paddingEnd/K_1440_0F + "in\" ");
    }
    buf.append(">");
  }
  private void addStaticContent(String flowName, HeaderFooter content)
  {
    _bodyBuffer.append("<fo:static-content flow-name=\"" + flowName + "\">");
    //_bodyBuffer.append("<fo:float float=\"before\">");
    addBlockContent(content.getStart(), content.getEnd(),_text, _paragraphTable, _characterTable);
    //_bodyBuffer.append("</fo:float>");
    _bodyBuffer.append("</fo:static-content>");

  }
  private String getBulletText(LVL lvl, PAP pap)
  {
    StringBuffer bulletBuffer = new StringBuffer();
    for(int x = 0; x < lvl._xst.length; x++)
    {
      if(lvl._xst[x] < 9)
      {
        LVL numLevel = _listTables.getLevel(pap._ilfo, lvl._xst[x]);
        int num = numLevel._iStartAt;
        if(lvl == numLevel)
        {
          numLevel._iStartAt++;
        }
        else if(num > 1)
        {
          num--;
        }
        bulletBuffer.append(NumberFormatter.getNumber(num, lvl._nfc));

      }
      else
      {
        bulletBuffer.append(lvl._xst[x]);
      }

    }
    return bulletBuffer.toString();
  }
  /**
   * finds all chpx's that are between start and end
   */
  private ArrayList findProperties(int start, int end, BTreeSet.BTreeNode root)
  {
    ArrayList results = new ArrayList();
    BTreeSet.Entry[] entries = root._entries;

    for(int x = 0; x < entries.length; x++)
    {
      if(entries[x] != null)
      {
        BTreeSet.BTreeNode child = entries[x].child;
        PropertyNode xNode = (PropertyNode)entries[x].element;
        if(xNode != null)
        {
          int xStart = xNode.getStart();
          int xEnd = xNode.getEnd();
          if(xStart < end)
          {
            if(xStart >= start)
            {
              if(child != null)
              {
                ArrayList beforeItems = findProperties(start, end, child);
                results.addAll(beforeItems);
              }
              results.add(xNode);
            }
            else if(start < xEnd)
            {
              results.add(xNode);
              //break;
            }
          }
          else
          {
            if(child != null)
            {
              ArrayList beforeItems = findProperties(start, end, child);
              results.addAll(beforeItems);
            }
            break;
          }
        }
        else if(child != null)
        {
          ArrayList afterItems = findProperties(start, end, child);
          results.addAll(afterItems);
        }
      }
      else
      {
        break;
      }
    }
    return results;
  }
  private void openPage(String page, String type)
  {
    _bodyBuffer.append("<fo:page-sequence master-reference=\"" + page + "\">\r\n");
  }
  private void openFlow()
  {
    _bodyBuffer.append("<fo:flow flow-name=\"xsl-region-body\">\r\n");
  }
  private void closeFlow()
  {
    _bodyBuffer.append("</fo:flow>\r\n");
  }
  private void closePage()
  {
    _bodyBuffer.append("</fo:page-sequence>\r\n");
  }
  private void closeLine(StringBuffer buf)
  {
    buf.append("</fo:inline>");
  }
  private void closeBlock(StringBuffer buf)
  {
    buf.append("</fo:block>\r\n");
  }
  private ArrayList findPAPProperties(int start, int end, BTreeSet.BTreeNode root)
  {
    ArrayList results = new ArrayList();
    BTreeSet.Entry[] entries = root._entries;

    for(int x = 0; x < entries.length; x++)
    {
      if(entries[x] != null)
      {
        BTreeSet.BTreeNode child = entries[x].child;
        PapxNode papxNode = (PapxNode)entries[x].element;
        if(papxNode != null)
        {
          int papxStart = papxNode.getStart();
          if(papxStart < end)
          {
            if(papxStart >= start)
            {
              if(child != null)
              {
                ArrayList beforeItems = findPAPProperties(start, end, child);
                results.addAll(beforeItems);
              }
              results.add(papxNode);
            }
          }
          else
          {
            if(child != null)
            {
              ArrayList beforeItems = findPAPProperties(start, end, child);
              results.addAll(beforeItems);
            }
            break;
          }
        }
        else if(child != null)
        {
          ArrayList afterItems = findPAPProperties(start, end, child);
          results.addAll(afterItems);
        }
      }
      else
      {
        break;
      }
    }
    return results;
  }

  private String createPageMaster(SEP sep, String type, int section,
                                  String regionBefore, String regionAfter)
  {
    float height = sep._yaPage/K_1440_0F;
    float width = sep._xaPage/K_1440_0F;
    float leftMargin = sep._dxaLeft/K_1440_0F;
    float rightMargin = sep._dxaRight/K_1440_0F;
    float topMargin = sep._dyaTop/K_1440_0F;
    float bottomMargin = sep._dyaBottom/K_1440_0F;

    //add these to the header
    String thisPage = type + "-page" + section;

    _headerBuffer.append("<fo:simple-page-master master-name=\"" +
                        thisPage + "\"\r\n");
    _headerBuffer.append("page-height=\"" + height + "in\"\r\n");
    _headerBuffer.append("page-width=\"" + width + "in\"\r\n");
    _headerBuffer.append(">\r\n");



    _headerBuffer.append("<fo:region-body ");
    //top right bottom left

    _headerBuffer.append("margin=\"" + topMargin + "in " + rightMargin + "in " +
                         bottomMargin + "in " + leftMargin + "in\"\r\n");

    //String style = null;
    //String color = null;
    addBorder(_headerBuffer, sep._brcTop, "top");
    addBorder(_headerBuffer, sep._brcBottom, "bottom");
    addBorder(_headerBuffer, sep._brcLeft, "left");
    addBorder(_headerBuffer, sep._brcRight, "right");

    if(sep._ccolM1 > 0)
    {
      _headerBuffer.append("column-count=\"" + (sep._ccolM1 + 1) + "\" ");
      if(sep._fEvenlySpaced)
      {
        _headerBuffer.append("column-gap=\"" + sep._dxaColumns/K_1440_0F + "in\"");
      }
      else
      {
        _headerBuffer.append("column-gap=\"0.25in\"");
      }
    }
    _headerBuffer.append("/>\r\n");

    if(regionBefore != null)
    {
      _headerBuffer.append(regionBefore);
    }
    if(regionAfter != null)
    {
      _headerBuffer.append(regionAfter);
    }

    _headerBuffer.append("</fo:simple-page-master>\r\n");
    return thisPage;
  }
  private void addBorder(StringBuffer buf, short[] brc, String where)
  {
    if((brc[0] & 0xff00) != 0 && brc[0] != -1)
    {
      int type = (brc[0] & 0xff00) >> 8;
      float width = (brc[0] & 0x00ff)/8.0f;
      String style = getBorderStyle(brc[0]);
      String color = getColor(brc[1] & 0x00ff);
      String thickness = getBorderThickness(brc[0]);
      buf.append("border-" + where + "-style=\"" + style + "\"\r\n");
      buf.append("border-" + where + "-color=\"" + color + "\"\r\n");
      buf.append("border-" + where + "-width=\"" + width + "pt\"\r\n");
    }
  }
  public void closeDoc()
  {
    _headerBuffer.append("</fo:layout-master-set>");
    _bodyBuffer.append("</fo:root>");
    //_headerBuffer.append();

    //test code
    try
    {
      OutputStreamWriter test = new OutputStreamWriter(new FileOutputStream(_outName), "8859_1");
      test.write(_headerBuffer.toString());
      test.write(_bodyBuffer.toString());
      test.flush();
      test.close();
    }
    catch(Throwable t)
    {
      t.printStackTrace();
    }
  }
  private String getBorderThickness(int style)
  {
    switch(style)
    {
      case 1:
        return "medium";
      case 2:
        return "thick";
      case 3:
        return "medium";
      case 5:
        return "thin";
      default:
        return "medium";
    }
  }


  private String getColor(int ico)
  {
    switch(ico)
    {
      case 1:
        return "black";
      case 2:
        return "blue";
      case 3:
        return "cyan";
      case 4:
        return "green";
      case 5:
        return "magenta";
      case 6:
        return "red";
      case 7:
        return "yellow";
      case 8:
        return "white";
      case 9:
        return "darkblue";
      case 10:
        return "darkcyan";
      case 11:
        return "darkgreen";
      case 12:
        return "darkmagenta";
      case 13:
        return "darkred";
      case 14:
        return "darkyellow";
      case 15:
        return "darkgray";
      case 16:
        return "lightgray";
      default:
        return "black";
    }
  }

  private String getBorderStyle(int type)
  {

    switch(type)
    {
      case 1:
      case 2:
        return "solid";
      case 3:
        return "double";
      case 5:
        return "solid";
      case 6:
        return "dotted";
      case 7:
      case 8:
        return "dashed";
      case 9:
        return "dotted";
      case 10:
      case 11:
      case 12:
      case 13:
      case 14:
      case 15:
      case 16:
      case 17:
      case 18:
      case 19:
        return "double";
      case 20:
        return "solid";
      case 21:
        return "double";
      case 22:
        return "dashed";
      case 23:
        return "dashed";
      case 24:
        return "ridge";
      case 25:
        return "grooved";
      default:
        return "solid";
    }
  }
  /**
   * creates the List data
   *
   * @param tableStream Main table stream buffer.
   */
  private void createListTables(byte[] tableStream)
  {


    int lfoOffset = LittleEndian.getInt(_header, 0x2ea);
    int lfoSize = LittleEndian.getInt(_header, 0x2ee);
    byte[] plflfo = new byte[lfoSize];

    System.arraycopy(tableStream, lfoOffset, plflfo, 0, lfoSize);

    int lstOffset = LittleEndian.getInt(_header, 0x2e2);
    int lstSize = LittleEndian.getInt(_header, 0x2e2);
    if(lstOffset > 0 && lstSize > 0)
    {
      lstSize = lfoOffset - lstOffset;
      byte[] plcflst = new byte[lstSize];
      System.arraycopy(tableStream, lstOffset, plcflst, 0, lstSize);
      _listTables = new ListTables(plcflst, plflfo);
    }

  }
  /**
   * Creates the documents StyleSheet
   *
   * @param tableStream Main table stream buffer.
   *
   */
  private void createStyleSheet(byte[] tableStream)
  {
      int stshIndex = LittleEndian.getInt(_header, 0xa2);
      int stshSize = LittleEndian.getInt(_header, 0xa6);
      byte[] stsh = new byte[stshSize];
      System.arraycopy(tableStream, stshIndex, stsh, 0, stshSize);

      _styleSheet = new StyleSheet(stsh);

  }
  /**
   * creates the Font table
   *
   * @param tableStream Main table stream buffer.
   */
  private void createFontTable(byte[] tableStream)
  {
    int fontTableIndex = LittleEndian.getInt(_header, 0x112);
    int fontTableSize = LittleEndian.getInt(_header, 0x116);
    byte[] fontTable = new byte[fontTableSize];
    System.arraycopy(tableStream, fontTableIndex, fontTable, 0, fontTableSize);
    _fonts = new FontTable(fontTable);
  }


  private void overrideCellBorder(int row, int col, int height,
                                  int width, TC tc, TAP tap)
  {

    if(row == 0)
    {
      if(tc._brcTop[0] == 0 || tc._brcTop[0] == -1)
      {
        tc._brcTop = tap._brcTop;
      }
      if(tc._brcBottom[0] == 0 || tc._brcBottom[0] == -1)
      {
        tc._brcBottom = tap._brcHorizontal;
      }
    }
    else if(row == (height - 1))
    {
      if(tc._brcTop[0] == 0 || tc._brcTop[0] == -1)
      {
        tc._brcTop = tap._brcHorizontal;
      }
      if(tc._brcBottom[0] == 0 || tc._brcBottom[0] == -1)
      {
        tc._brcBottom = tap._brcBottom;
      }
    }
    else
    {
      if(tc._brcTop[0] == 0 || tc._brcTop[0] == -1)
      {
        tc._brcTop = tap._brcHorizontal;
      }
      if(tc._brcBottom[0] == 0 || tc._brcBottom[0] == -1)
      {
        tc._brcBottom = tap._brcHorizontal;
      }
    }
    if(col == 0)
    {
      if(tc._brcLeft[0] == 0 || tc._brcLeft[0] == -1)
      {
        tc._brcLeft = tap._brcLeft;
      }
      if(tc._brcRight[0] == 0 || tc._brcRight[0] == -1)
      {
        tc._brcRight = tap._brcVertical;
      }
    }
    else if(col == (width - 1))
    {
      if(tc._brcLeft[0] == 0 || tc._brcLeft[0] == -1)
      {
        tc._brcLeft = tap._brcVertical;
      }
      if(tc._brcRight[0] == 0 || tc._brcRight[0] == -1)
      {
        tc._brcRight = tap._brcRight;
      }
    }
    else
    {
      if(tc._brcLeft[0] == 0 || tc._brcLeft[0] == -1)
      {
        tc._brcLeft = tap._brcVertical;
      }
      if(tc._brcRight[0] == 0 || tc._brcRight[0] == -1)
      {
        tc._brcRight = tap._brcVertical;
      }
    }
  }
  private void printTable()
  {
    if(_table != null)
    {
      int size = _table.size();

      //local buffers for the table
      StringBuffer tableHeaderBuffer = new StringBuffer();
      StringBuffer tableBodyBuffer = new StringBuffer();

      for(int x = 0; x < size; x++)
      {
        StringBuffer rowBuffer = tableBodyBuffer;
        TableRow row = (TableRow)_table.get(x);
        TAP tap = row.getTAP();
        ArrayList cells = row.getCells();

        if(tap._fTableHeader)
        {
          rowBuffer = tableHeaderBuffer;
        }
        rowBuffer.append("<fo:table-row ");
        if(tap._dyaRowHeight > 0)
        {
          rowBuffer.append("height=\"" + tap._dyaRowHeight/K_1440_0F + "in\" ");
        }
        if(tap._fCantSplit)
        {
          rowBuffer.append("keep-together=\"always\" ");
        }
        rowBuffer.append(">");
        //add cells
        for(int y = 0; y < tap._itcMac; y++)
        {
          TC tc = tap._rgtc[y];
          overrideCellBorder(x, y, size, tap._itcMac, tc, tap);
          rowBuffer.append("<fo:table-cell ");
          rowBuffer.append("width=\"" + (tap._rgdxaCenter[y+1] - tap._rgdxaCenter[y])/K_1440_0F + "in\" ");
          rowBuffer.append("padding-start=\"" + tap._dxaGapHalf/K_1440_0F + "in\" ");
          rowBuffer.append("padding-end=\"" + tap._dxaGapHalf/K_1440_0F + "in\" ");
          addBorder(rowBuffer, tc._brcTop, "top");
          addBorder(rowBuffer, tc._brcLeft, "left");
          addBorder(rowBuffer, tc._brcBottom, "bottom");
          addBorder(rowBuffer, tc._brcRight, "right");
          rowBuffer.append(">");
          rowBuffer.append((String)cells.get(y));
          rowBuffer.append("</fo:table-cell>");
        }
        rowBuffer.append("</fo:table-row>");
      }
      StringBuffer tableBuffer = new StringBuffer();
      tableBuffer.append("<fo:table>");
      if(tableHeaderBuffer.length() > 0)
      {
        tableBuffer.append("<fo:table-header>");
        tableBuffer.append(tableHeaderBuffer.toString());
        tableBuffer.append("</fo:table-header>");
      }
      tableBuffer.append("<fo:table-body>");
      tableBuffer.append(tableBodyBuffer.toString());
      tableBuffer.append("</fo:table-body>");
      tableBuffer.append("</fo:table>");
      _bodyBuffer.append(tableBuffer.toString());
      _table = null;
    }
  }
  private void initPclfHdd(byte[] tableStream)
  {
    int size = Utils.convertBytesToInt(_header, 0xf6);
    int pos = Utils.convertBytesToInt(_header, 0xf2);

    _plcfHdd = new byte[size];

    System.arraycopy(tableStream, pos, _plcfHdd, 0, size);
  }




}
