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

package org.apache.poi.hdf.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hdf.event.HDFLowLevelParsingListener;
import org.apache.poi.hdf.model.hdftypes.*;
import org.apache.poi.hdf.model.util.ParsingState;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.LittleEndian;

/**
 * The Object Factory takes in a stream and creates the low level objects
 * that represent the data.
 * @author  andy
 */
public final class HDFObjectFactory {

    /** OLE stuff*/
    private POIFSFileSystem _filesystem;
    /** The FIB*/
    private FileInformationBlock _fib;

    /** Used to set up the object model*/
    private HDFLowLevelParsingListener _listener;
    /** parsing state for characters */
    private ParsingState _charParsingState;
    /** parsing state for paragraphs */
    private ParsingState _parParsingState;

    /** main document stream buffer*/
    byte[] _mainDocument;
    /** table stream buffer*/
    byte[] _tableBuffer;


    public static void main(String args[])
    {
      try
      {
        HDFObjectFactory f = new HDFObjectFactory(new FileInputStream("c:\\test.doc"));
        int k = 0;
      }
      catch(Throwable t)
      {
        t.printStackTrace();
      }
    }
    /** Creates a new instance of HDFObjectFactory
     *
     * @param istream The InputStream that is the Word document
     *
     */
    protected HDFObjectFactory(InputStream istream, HDFLowLevelParsingListener l) throws IOException
    {
        if (l == null)
        {
            _listener = new HDFObjectModel();
        }
        else
        {
            _listener = l;
        }

        //do Ole stuff
        _filesystem = new POIFSFileSystem(istream);

        DocumentEntry headerProps =
            (DocumentEntry)_filesystem.getRoot().getEntry("WordDocument");

        _mainDocument = new byte[headerProps.getSize()];
        _filesystem.createDocumentInputStream("WordDocument").read(_mainDocument);

        _fib = new FileInformationBlock(_mainDocument);

        initTableStream();
        initTextPieces();
        initFormattingProperties();


    }




    /** Creates a new instance of HDFObjectFactory
     *
     * @param istream The InputStream that is the Word document
     *
     */
    public HDFObjectFactory(InputStream istream) throws IOException
    {
        this(istream, null);
    }

    public static List getTypes(InputStream istream) throws IOException
    {
        List results = new ArrayList(1);

        //do Ole stuff
        POIFSFileSystem filesystem = new POIFSFileSystem(istream);

        DocumentEntry headerProps =
            (DocumentEntry)filesystem.getRoot().getEntry("WordDocument");

        byte[] mainDocument = new byte[headerProps.getSize()];
        filesystem.createDocumentInputStream("WordDocument").read(mainDocument);

        FileInformationBlock fib = new FileInformationBlock(mainDocument);


        results.add(fib);
        return results;
    }


    /**
     * Initializes the table stream
     *
     * @throws IOException
     */
    private void initTableStream() throws IOException
    {
        String tablename = null;
        if(_fib.isFWhichTblStm())
        {
            tablename="1Table";
        }
        else
        {
          tablename="0Table";
        }

        DocumentEntry tableEntry = (DocumentEntry)_filesystem.getRoot().getEntry(tablename);

        //load the table stream into a buffer
        int size = tableEntry.getSize();
        _tableBuffer = new byte[size];
        _filesystem.createDocumentInputStream(tablename).read(_tableBuffer);
    }
    /**
     * Initializes the text pieces. Text is divided into pieces because some
     * "pieces" may only contain unicode characters.
     *
     * @throws IOException
     */
    private void initTextPieces() throws IOException
    {
        int pos = _fib.getFcClx();

        //skips through the prms before we reach the piece table. These contain data
        //for actual fast saved files
        while (_tableBuffer[pos] == 1)
        {
            pos++;
            int skip = LittleEndian.getShort(_tableBuffer, pos);
            pos += 2 + skip;
        }
        if(_tableBuffer[pos] != 2)
        {
            throw new IOException("The text piece table is corrupted");
        }
        //parse out the text pieces
        int pieceTableSize = LittleEndian.getInt(_tableBuffer, ++pos);
        pos += 4;
        int pieces = (pieceTableSize - 4) / 12;
        for (int x = 0; x < pieces; x++) {
            int filePos = LittleEndian.getInt(_tableBuffer, pos + ((pieces + 1) * 4) + (x * 8) + 2);
            boolean unicode = false;
            if ((filePos & 0x40000000) == 0) {
                unicode = true;
            } else {
                unicode = false;
                filePos &= ~(0x40000000);//gives me FC in doc stream
                filePos /= 2;
            }
            int totLength = LittleEndian.getInt(_tableBuffer, pos + (x + 1) * 4) -
                    LittleEndian.getInt(_tableBuffer, pos + (x * 4));

            TextPiece piece = new TextPiece(filePos, totLength, unicode);
            _listener.text(piece);
        }
    }
    /**
     * initializes all of the formatting properties for a Word Document
     */
    private void initFormattingProperties()
    {
        createStyleSheet();
        createListTables();
        createFontTable();

        initDocumentProperties();
        initSectionProperties();
        //initCharacterProperties();
        //initParagraphProperties();
    }
    private void initCharacterProperties(int charOffset, PlexOfCps charPlcf, int start, int end)
    {
        //Initialize paragraph property stuff
        //int currentCharPage = _charParsingState.getCurrentPage();
        int charPlcfLen = charPlcf.length();
        int currentPageIndex = _charParsingState.getCurrentPageIndex();
        FormattedDiskPage fkp = _charParsingState.getFkp();
        int currentChpxIndex = _charParsingState.getCurrentPropIndex();
        int currentArraySize = fkp.size();

        //get the character runs for this paragraph
        int charStart = 0;
        int charEnd = 0;
        //add the character runs
        do
        {
          if (currentChpxIndex < currentArraySize)
          {
            charStart = fkp.getStart(currentChpxIndex);
            charEnd = fkp.getEnd(currentChpxIndex);
            byte[] chpx = fkp.getGrpprl(currentChpxIndex);
            _listener.characterRun(new ChpxNode(Math.max(charStart, start),  Math.min(charEnd, end), chpx));

            if (charEnd < end)
            {
              currentChpxIndex++;
            }
            else
            {
              _charParsingState.setState(currentPageIndex, fkp, currentChpxIndex);
              break;
            }
          }
          else
          {
            int currentCharPage = LittleEndian.getInt(_tableBuffer, charOffset + charPlcf.getStructOffset(++currentPageIndex));
            byte[] byteFkp = new byte[512];
            System.arraycopy(_mainDocument, (currentCharPage * 512), byteFkp, 0, 512);
            fkp = new CHPFormattedDiskPage(byteFkp);
            currentChpxIndex = 0;
            currentArraySize = fkp.size();
          }
        }
        while(currentPageIndex < charPlcfLen);
    }
    private void initParagraphProperties(int parOffset, PlexOfCps parPlcf, int charOffset, PlexOfCps charPlcf, int start, int end)
    {
        //Initialize paragraph property stuff
        //int currentParPage = _parParsingState.getCurrentPage();
        int parPlcfLen = parPlcf.length();
        int currentPageIndex = _parParsingState.getCurrentPageIndex();
        FormattedDiskPage fkp = _parParsingState.getFkp();
        int currentPapxIndex = _parParsingState.getCurrentPropIndex();
        int currentArraySize = fkp.size();

        do
        {
          if (currentPapxIndex < currentArraySize)
          {
            int parStart = fkp.getStart(currentPapxIndex);
            int parEnd = fkp.getEnd(currentPapxIndex);
            byte[] papx = fkp.getGrpprl(currentPapxIndex);
            _listener.paragraph(new PapxNode(Math.max(parStart, start), Math.min(parEnd, end), papx));
            initCharacterProperties(charOffset, charPlcf, Math.max(start, parStart), Math.min(parEnd, end));
            if (parEnd < end)
            {
              currentPapxIndex++;
            }
            else
            {
              //save the state
              _parParsingState.setState(currentPageIndex, fkp, currentPapxIndex);
              break;
            }
          }
          else
          {
            int currentParPage = LittleEndian.getInt(_tableBuffer, parOffset + parPlcf.getStructOffset(++currentPageIndex));
            byte byteFkp[] = new byte[512];
            System.arraycopy(_mainDocument, (currentParPage * 512), byteFkp, 0, 512);
            fkp = new PAPFormattedDiskPage(byteFkp);
            currentPapxIndex = 0;
            currentArraySize = fkp.size();
          }
        }
        while(currentPageIndex < parPlcfLen);
    }
    /**
     * initializes the CharacterProperties BTree
     */
    /*private void initCharacterProperties()
    {
        int charOffset = _fib.getFcPlcfbteChpx();
        int charPlcSize = _fib.getLcbPlcfbteChpx();

        //int arraySize = (charPlcSize - 4)/8;

        //first we must go through the bin table and find the fkps
        for(int x = 0; x < arraySize; x++)
        {

            //get page number(has nothing to do with document page)
            //containing the chpx for the paragraph
            int PN = LittleEndian.getInt(_tableBuffer, charOffset + (4 * (arraySize + 1) + (4 * x)));

            byte[] fkp = new byte[512];
            System.arraycopy(_mainDocument, (PN * 512), fkp, 0, 512);
            //take each fkp and get the chpxs
            int crun = LittleEndian.getUnsignedByte(fkp, 511);
            for(int y = 0; y < crun; y++)
            {
                //get the beginning fc of each paragraph text run
                int fcStart = LittleEndian.getInt(fkp, y * 4);
                int fcEnd = LittleEndian.getInt(fkp, (y+1) * 4);
                //get the offset in fkp of the papx for this paragraph
                int chpxOffset = 2 * LittleEndian.getUnsignedByte(fkp, ((crun + 1) * 4) + y);

                //optimization if offset == 0 use "Normal" style
                if(chpxOffset == 0)

                {
                    _characterRuns.add(new ChpxNode(fcStart, fcEnd, new byte[0]));
                    continue;
                }

                int size = LittleEndian.getUnsignedByte(fkp, chpxOffset);

                byte[] chpx = new byte[size];
                System.arraycopy(fkp, ++chpxOffset, chpx, 0, size);
                //_papTable.put(Integer.valueOf(fcStart), papx);
                _characterRuns.add(new ChpxNode(fcStart, fcEnd, chpx));
            }

        }
    }*/
    /**
     * intializes the Paragraph Properties BTree
     */
    private void initParagraphProperties()
    {
        //paragraphs
        int parOffset = _fib.getFcPlcfbtePapx();
        int parPlcSize = _fib.getLcbPlcfbtePapx();

        //characters
        int charOffset = _fib.getFcPlcfbteChpx();
        int charPlcSize = _fib.getLcbPlcfbteChpx();

        PlexOfCps charPlcf = new PlexOfCps(charPlcSize, 4);
        PlexOfCps parPlcf = new PlexOfCps(parPlcSize, 4);

        //Initialize character property stuff
        int currentCharPage = LittleEndian.getInt(_tableBuffer, charOffset + charPlcf.getStructOffset(0));
        int charPlcfLen = charPlcf.length();
        int currentPageIndex = 0;
        byte[] fkp = new byte[512];
        System.arraycopy(_mainDocument, (currentCharPage * 512), fkp, 0, 512);
        CHPFormattedDiskPage cfkp = new CHPFormattedDiskPage(fkp);
        int currentChpxIndex = 0;
        int currentArraySize = cfkp.size();


        int arraySize = parPlcf.length();

        //first we must go through the bin table and find the fkps
        for(int x = 0; x < arraySize; x++)
        {
            int PN = LittleEndian.getInt(_tableBuffer, parOffset + parPlcf.getStructOffset(x));

            fkp = new byte[512];
            System.arraycopy(_mainDocument, (PN * 512), fkp, 0, 512);

            PAPFormattedDiskPage pfkp = new PAPFormattedDiskPage(fkp);
            //take each fkp and get the paps
            int crun = pfkp.size();
            for(int y = 0; y < crun; y++)
            {
                //get the beginning fc of each paragraph text run
                int fcStart = pfkp.getStart(y);
                int fcEnd = pfkp.getEnd(y);

                //get the papx for this paragraph
                byte[] papx = pfkp.getGrpprl(y);

                _listener.paragraph(new PapxNode(fcStart, fcEnd, papx));

                //get the character runs for this paragraph
                int charStart = 0;
                int charEnd = 0;
                //add the character runs
                do
                {
                  if (currentChpxIndex < currentArraySize)
                  {
                    charStart = cfkp.getStart(currentChpxIndex);
                    charEnd = cfkp.getEnd(currentChpxIndex);
                    byte[] chpx = cfkp.getGrpprl(currentChpxIndex);
                    _listener.characterRun(new ChpxNode(charStart, charEnd, chpx));
                    if (charEnd < fcEnd)
                    {
                      currentChpxIndex++;
                    }
                    else
                    {
                      break;
                    }
                  }
                  else
                  {
                    currentCharPage = LittleEndian.getInt(_tableBuffer, charOffset + charPlcf.getStructOffset(++currentPageIndex));
                    fkp = new byte[512];
                    System.arraycopy(_mainDocument, (currentCharPage * 512), fkp, 0, 512);
                    cfkp = new CHPFormattedDiskPage(fkp);
                    currentChpxIndex = 0;
                    currentArraySize = cfkp.size();
                  }
                }
                while(currentCharPage <= charPlcfLen + 1);

            }

        }

    }
    private void initParsingStates(int parOffset, PlexOfCps parPlcf, int charOffset, PlexOfCps charPlcf)
    {
        int currentCharPage = LittleEndian.getInt(_tableBuffer, charOffset + charPlcf.getStructOffset(0));
        byte[] fkp = new byte[512];
        System.arraycopy(_mainDocument, (currentCharPage * 512), fkp, 0, 512);
        CHPFormattedDiskPage cfkp = new CHPFormattedDiskPage(fkp);
        _charParsingState = new ParsingState(currentCharPage, cfkp);

        int currentParPage = LittleEndian.getInt(_tableBuffer, parOffset + parPlcf.getStructOffset(0));
        fkp = new byte[512];
        System.arraycopy(_mainDocument, (currentParPage * 512), fkp, 0, 512);
        PAPFormattedDiskPage pfkp = new PAPFormattedDiskPage(fkp);
        _parParsingState = new ParsingState(currentParPage, pfkp);
    }
    /**
     * initializes the SectionProperties BTree
     */
    private void initSectionProperties()
    {

      int ccpText = _fib.getCcpText();
      int ccpFtn = _fib.getCcpFtn();

      //sections
      int fcMin = _fib.getFcMin();
      int plcfsedFC = _fib.getFcPlcfsed();
      int plcfsedSize = _fib.getLcbPlcfsed();

      //paragraphs
      int parOffset = _fib.getFcPlcfbtePapx();
      int parPlcSize = _fib.getLcbPlcfbtePapx();

      //characters
      int charOffset = _fib.getFcPlcfbteChpx();
      int charPlcSize = _fib.getLcbPlcfbteChpx();

      PlexOfCps charPlcf = new PlexOfCps(charPlcSize, 4);
      PlexOfCps parPlcf = new PlexOfCps(parPlcSize, 4);

      initParsingStates(parOffset, parPlcf, charOffset, charPlcf);

      //byte[] plcfsed = new byte[plcfsedSize];
      //System.arraycopy(_tableBuffer, plcfsedFC, plcfsed, 0, plcfsedSize);

      PlexOfCps plcfsed = new PlexOfCps(plcfsedSize, 12);
      int arraySize = plcfsed.length();

      int start = fcMin;
      int end = fcMin + ccpText;
      int x = 0;
      int sectionEnd = 0;

      //do the main body sections
      while (x < arraySize)
      {
          int sectionStart = LittleEndian.getInt(_tableBuffer, plcfsedFC + plcfsed.getIntOffset(x)) + fcMin;
          sectionEnd = LittleEndian.getInt(_tableBuffer, plcfsedFC + plcfsed.getIntOffset(x + 1)) + fcMin;
          int sepxStart = LittleEndian.getInt(_tableBuffer, plcfsedFC + plcfsed.getStructOffset(x) + 2);
          int sepxSize = LittleEndian.getShort(_mainDocument, sepxStart);

          byte[] sepx = new byte[sepxSize];
          System.arraycopy(_mainDocument, sepxStart + 2, sepx, 0, sepxSize);
          SepxNode node = new SepxNode(x + 1, sectionStart, sectionEnd, sepx);
          _listener.bodySection(node);
          initParagraphProperties(parOffset, parPlcf, charOffset, charPlcf, sectionStart, Math.min(end, sectionEnd));

          if (sectionEnd > end)
          {
            break;
          }
          x++;
      }
      //do the header sections
      for (; x < arraySize; x++)// && sectionEnd <= end; x++)
      {
          int sectionStart = LittleEndian.getInt(_tableBuffer, plcfsedFC + plcfsed.getIntOffset(x)) + fcMin;
          sectionEnd = LittleEndian.getInt(_tableBuffer, plcfsedFC + plcfsed.getIntOffset(x + 1)) + fcMin;
          int sepxStart = LittleEndian.getInt(_tableBuffer, plcfsedFC + plcfsed.getStructOffset(x) + 2);
          int sepxSize = LittleEndian.getShort(_mainDocument, sepxStart);

          byte[] sepx = new byte[sepxSize];
          System.arraycopy(_mainDocument, sepxStart + 2, sepx, 0, sepxSize);
          SepxNode node = new SepxNode(x + 1, sectionStart, sectionEnd, sepx);
          _listener.hdrSection(node);
          initParagraphProperties(parOffset, parPlcf, charOffset, charPlcf, Math.max(sectionStart, end), sectionEnd);

      }
      _listener.endSections();
    }
    /**
     * Initializes the DocumentProperties object unique to this document.
     */
    private void initDocumentProperties()
    {
        int pos = _fib.getFcDop();
        int size = _fib.getLcbDop();
        byte[] dopArray = new byte[size];

        System.arraycopy(_tableBuffer, pos, dopArray, 0, size);
        _listener.document(new DocumentProperties(dopArray));
    }
    /**
     * Uncompresses the StyleSheet from file into memory.
     */
    private void createStyleSheet()
    {
      int stshIndex = _fib.getFcStshf();
      int stshSize = _fib.getLcbStshf();
      byte[] stsh = new byte[stshSize];
      System.arraycopy(_tableBuffer, stshIndex, stsh, 0, stshSize);

      _listener.styleSheet(new StyleSheet(stsh));
    }
    /**
     * Initializes the list tables for this document
     */
    private void createListTables()
    {
        int lfoOffset = _fib.getFcPlfLfo();
        int lfoSize = _fib.getLcbPlfLfo();
        byte[] plflfo = new byte[lfoSize];

        System.arraycopy(_tableBuffer, lfoOffset, plflfo, 0, lfoSize);

        int lstOffset = _fib.getFcPlcfLst();
        int lstSize = _fib.getLcbPlcfLst();
        if (lstOffset > 0 && lstSize > 0)
        {
          //  The lstSize returned by _fib.getLcbPlcfLst() doesn't appear
          //  to take into account any LVLs.  Therefore, we recalculate
          //  lstSize based on where the LFO section begins (because the
          //  LFO section immediately follows the LST section).
          lstSize = lfoOffset - lstOffset;
          byte[] plcflst = new byte[lstSize];
          System.arraycopy(_tableBuffer, lstOffset, plcflst, 0, lstSize);
          _listener.lists(new ListTables(plcflst, plflfo));
        }
    }
    /**
     * Initializes this document's FontTable;
     */
    private void createFontTable()
    {
        int fontTableIndex = _fib.getFcSttbfffn();
        int fontTableSize = _fib.getLcbSttbfffn();
        byte[] fontTable = new byte[fontTableSize];
        System.arraycopy(_tableBuffer, fontTableIndex, fontTable, 0, fontTableSize);
        _listener.fonts(new FontTable(fontTable));
    }

}
