/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
/*
 * HDFObjectFactory.java
 *
 * Created on February 24, 2002, 2:17 PM
 */

package org.apache.poi.hdf.model;


//import java.io;

import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.TreeSet;


import org.apache.poi.hdf.model.hdftypes.*;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSDocument;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.util.LittleEndian;




/**
 * The Object Factory takes in a stream and creates the low level objects
 * that represent the data.
 * @author  andy
 */
public class HDFObjectFactory
{

    /** OLE stuff*/
    private POIFSFileSystem _filesystem;
    /** The FIB*/
    private FileInformationBlock _fib;
    /** The DOP*/
    private DocumentProperties _dop;
    /**the StyleSheet*/
    private StyleSheet _styleSheet;
    /**list info */
    private ListTables _listTables;
    /** Font info */
    private FontTable _fonts;

    /** text pieces */
    //BTreeSet _text = new BTreeSet();
    TreeSet _text = new TreeSet();
    /** document sections */
    TreeSet _sections = new TreeSet();
    /** document paragraphs */
    TreeSet _paragraphs = new TreeSet();
    /** document character runs */
    TreeSet _characterRuns = new TreeSet();

    /** main document stream buffer*/
    byte[] _mainDocument;
    /** table stream buffer*/
    byte[] _tableBuffer;



    /** Creates a new instance of HDFObjectFactory
     *
     * @param istream The InputStream that is the Word document
     *
     */
    public HDFObjectFactory(InputStream istream) throws IOException
    {
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

       // initTableStream();
       // initTextPieces();
       // initFormattingProperties();

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
        if(!_fib.isFWhichTblStm())
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
        else
        {
            //parse out the text pieces
            int pieceTableSize = LittleEndian.getInt(_tableBuffer, ++pos);
            pos += 4;
            int pieces = (pieceTableSize - 4) / 12;
            for (int x = 0; x < pieces; x++)
            {
                int filePos = LittleEndian.getInt(_tableBuffer, pos + ((pieces + 1) * 4) + (x * 8) + 2);
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
                int totLength = LittleEndian.getInt(_tableBuffer, pos + (x + 1) * 4) -
                                LittleEndian.getInt(_tableBuffer, pos + (x * 4));

                TextPiece piece = new TextPiece(filePos, totLength, unicode);
                _text.add(piece);

            }

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
        initCharacterProperties();
        initParagraphProperties();
    }
    /**
     * initializes the CharacterProperties BTree
     */
    private void initCharacterProperties()
    {
        int charOffset = _fib.getFcPlcfbteChpx();
        int charPlcSize = _fib.getLcbPlcfbteChpx();

        int arraySize = (charPlcSize - 4)/8;

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
                //_papTable.put(new Integer(fcStart), papx);
                _characterRuns.add(new ChpxNode(fcStart, fcEnd, chpx));
            }

        }
    }
    /**
     * intializes the Paragraph Properties BTree
     */
    private void initParagraphProperties()
    {
        //find paragraphs
        int parOffset = _fib.getFcPlcfbtePapx();
        int parPlcSize = _fib.getLcbPlcfbtePapx();

        int arraySize = (parPlcSize - 4)/8;
        //first we must go through the bin table and find the fkps
        for(int x = 0; x < arraySize; x++)
        {
            int PN = LittleEndian.getInt(_tableBuffer, parOffset + (4 * (arraySize + 1) + (4 * x)));

            byte[] fkp = new byte[512];
            System.arraycopy(_mainDocument, (PN * 512), fkp, 0, 512);
            //take each fkp and get the paps
            int crun = LittleEndian.getUnsignedByte(fkp, 511);
            for(int y = 0; y < crun; y++)
            {
                //get the beginning fc of each paragraph text run
                int fcStart = LittleEndian.getInt(fkp, y * 4);
                int fcEnd = LittleEndian.getInt(fkp, (y+1) * 4);
                //get the offset in fkp of the papx for this paragraph
                int papxOffset = 2 * LittleEndian.getUnsignedByte(fkp, ((crun + 1) * 4) + (y * 13));
                int size = 2 * LittleEndian.getUnsignedByte(fkp, papxOffset);
                if(size == 0)
                {
                    size = 2 * LittleEndian.getUnsignedByte(fkp, ++papxOffset);
                }
                else
                {
                    size--;
                }

                byte[] papx = new byte[size];
                System.arraycopy(fkp, ++papxOffset, papx, 0, size);
                _paragraphs.add(new PapxNode(fcStart, fcEnd, papx));

            }

        }

    }
    /**
     * initializes the SectionProperties BTree
     */
    private void initSectionProperties()
    {
      //find sections
      int fcMin = _fib.getFcMin();
      int plcfsedFC = _fib.getFcPlcfsed();
      int plcfsedSize = _fib.getLcbPlcfsed();
      byte[] plcfsed = new byte[plcfsedSize];
      System.arraycopy(_tableBuffer, plcfsedFC, plcfsed, 0, plcfsedSize);

      int arraySize = (plcfsedSize - 4)/16;


      for(int x = 0; x < arraySize; x++)
      {
          int sectionStart = LittleEndian.getInt(plcfsed, x * 4) + fcMin;
          int sectionEnd = LittleEndian.getInt(plcfsed, (x+1) * 4) + fcMin;
          int sepxStart = LittleEndian.getInt(plcfsed, 4 * (arraySize + 1) + (x * 12) + 2);
          int sepxSize = LittleEndian.getShort(_mainDocument, sepxStart);
          byte[] sepx = new byte[sepxSize];
          System.arraycopy(_mainDocument, sepxStart + 2, sepx, 0, sepxSize);
          SepxNode node = new SepxNode(x + 1, sectionStart, sectionEnd, sepx);
          _sections.add(node);
      }
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
        _dop = new DocumentProperties(dopArray);
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

      _styleSheet = new StyleSheet(stsh);
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
        int lstSize = lstOffset;
        //not sure if this is a mistake or what. I vaguely remember a trick like
        //this
        //int lstSize = LittleEndian.getInt(_header, 0x2e2);
        if(lstOffset > 0 && lstSize > 0)
        {
          lstSize = lfoOffset - lstOffset;
          byte[] plcflst = new byte[lstSize];
          System.arraycopy(_tableBuffer, lstOffset, plcflst, 0, lstSize);
          _listTables = new ListTables(plcflst, plflfo);
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
        _fonts = new FontTable(fontTable);
    }

}
