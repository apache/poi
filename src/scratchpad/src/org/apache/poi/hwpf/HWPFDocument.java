/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2003 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" and
 *  "Apache POI" must not be used to endorse or promote products
 *  derived from this software without prior written permission. For
 *  written permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  "Apache POI", nor may "Apache" appear in their name, without
 *  prior written permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */
package org.apache.poi.hwpf;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.hwpf.usermodel.CharacterRun;

import org.apache.poi.hwpf.model.hdftypes.*;
import org.apache.poi.hwpf.model.io.*;


/**
 *
 * This class acts as the bucket that we throw all of the Word data structures
 * into.
 *
 * @author Ryan Ackley
 */
public class HWPFDocument
{
  /** OLE stuff*/
  private POIFSFileSystem _filesystem;

  /** The FIB*/
  private FileInformationBlock _fib;

  /** main document stream buffer*/
  private byte[] _mainStream;

  /** table stream buffer*/
  private byte[] _tableStream;

  /** Document wide Properties*/
  private DocumentProperties _dop;

  /** Contains text of the document wrapped in a obfuscated Wod data structure*/
  private ComplexFileTable _cft;

  private TextPieceTable _tpt;

  /** Contains formatting properties for text*/
  private CHPBinTable _cbt;

  /** Contains formatting properties for paragraphs*/
  private PAPBinTable _pbt;

  /** Contains formatting properties for sections.*/
  private SectionTable _st;

  /** Holds styles for this document.*/
  private StyleSheet _ss;

  /** Holds fonts for this document.*/
  private FontTable _ft;


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
    _filesystem = new POIFSFileSystem(istream);

    // read in the main stream.
    DocumentEntry documentProps =
       (DocumentEntry)_filesystem.getRoot().getEntry("WordDocument");
    _mainStream = new byte[documentProps.getSize()];
    _filesystem.createDocumentInputStream("WordDocument").read(_mainStream);

    // use the fib to determine the name of the table stream.
    _fib = new FileInformationBlock(_mainStream);

    String name = "0Table";
    if (_fib.isFWhichTblStm())
    {
      name = "1Table";
    }

    // read in the table stream.
    DocumentEntry tableProps =
      (DocumentEntry)_filesystem.getRoot().getEntry(name);
    _tableStream = new byte[tableProps.getSize()];
    _filesystem.createDocumentInputStream(name).read(_tableStream);

    // get the start of text in the main stream
    int fcMin = _fib.getFcMin();

    // load up our standard structures.
    _dop = new DocumentProperties(_tableStream, _fib.getFcDop());
    _cft = new ComplexFileTable(_mainStream, _tableStream, _fib.getFcClx(), fcMin);
    _tpt = _cft.getTextPieceTable();
    _cbt = new CHPBinTable(_mainStream, _tableStream, _fib.getFcPlcfbteChpx(), _fib.getLcbPlcfbteChpx(), fcMin);
    _pbt = new PAPBinTable(_mainStream, _tableStream, _fib.getFcPlcfbtePapx(), _fib.getLcbPlcfbtePapx(), fcMin);

    // Word XP puts in a zero filled buffer in front of the text and it screws
    // up my system for offsets. This is an adjustment.
    int cpMin = _tpt.getCpMin();
    if (cpMin > 0)
    {
      _cbt.adjustForDelete(0, 0, cpMin);
      _pbt.adjustForDelete(0, 0, cpMin);
    }

    _st = new SectionTable(_mainStream, _tableStream, _fib.getFcPlcfsed(), _fib.getLcbPlcfsed(), fcMin);
    _ss = new StyleSheet(_tableStream, _fib.getFcStshf());
    _ft = new FontTable(_tableStream, _fib.getFcSttbfffn(), _fib.getLcbSttbfffn());

    int x = 0;

  }

  public StyleSheet getStyleSheet()
  {
    return _ss;
  }

  public Range getRange()
  {
    return new Range(0, _fib.getFcMac() - _fib.getFcMin(), this);
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
    int tableOffset = 0;

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

    // make sure that the table and doc stream use big blocks.
    byte[] mainBuf = mainStream.toByteArray();
    if (mainBuf.length < 4096)
    {
      byte[] tempBuf = new byte[4096];
      System.arraycopy(mainBuf, 0, tempBuf, 0, mainBuf.length);
      mainBuf = tempBuf;
    }
    byte[] tableBuf = tableStream.toByteArray();
    if (tableBuf.length < 4096)
    {
      byte[] tempBuf = new byte[4096];
      System.arraycopy(tableBuf, 0, tempBuf, 0, tableBuf.length);
      tableBuf = tempBuf;
    }

    // write out the FileInformationBlock.
    _fib.serialize(mainBuf, 0);

    // spit out the Word document.
    POIFSFileSystem pfs = new POIFSFileSystem();
    pfs.createDocument(new ByteArrayInputStream(mainBuf), "WordDocument");
    pfs.createDocument(new ByteArrayInputStream(tableBuf), "1Table");

    pfs.writeFilesystem(out);
  }

  CHPBinTable getCharacterTable()
  {
    return _cbt;
  }

  PAPBinTable getParagraphTable()
  {
    return _pbt;
  }

  SectionTable getSectionTable()
  {
    return _st;
  }

  TextPieceTable getTextTable()
  {
    return _cft.getTextPieceTable();
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
      CharacterRun run = new CharacterRun();
      run.setBold(true);
      run.setItalic(true);
      run.setCapitalized(true);

      Range range = doc.getRange();
      range.insertBefore("Hello World!!! HAHAHAHAHA I DID IT!!!", run);

      OutputStream out = new FileOutputStream(args[1]);
      doc.write(out);

      out.flush();
      out.close();


    }
    catch (Throwable t)
    {
      t.printStackTrace();
    }
  }
}
