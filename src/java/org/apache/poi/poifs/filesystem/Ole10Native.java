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

package org.apache.poi.poifs.filesystem;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.StringUtil;

/**
 * Represents an Ole10Native record which is wrapped around certain binary
 * files being embedded in OLE2 documents.
 *
 * @author Rainer Schwarze
 */
public class Ole10Native {

  public static final String OLE10_NATIVE = "\u0001Ole10Native";
  protected static final String ISO1 = "ISO-8859-1";

  // (the fields as they appear in the raw record:)
  private int totalSize;             // 4 bytes, total size of record not including this field
  private short flags1 = 2;          // 2 bytes, unknown, mostly [02 00]
  private String label;              // ASCIIZ, stored in this field without the terminating zero
  private String fileName;           // ASCIIZ, stored in this field without the terminating zero
  private short flags2 = 0;          // 2 bytes, unknown, mostly [00 00]
  private short unknown1 = 3;        // see below
  private String command;            // ASCIIZ, stored in this field without the terminating zero
  private byte[] dataBuffer;         // varying size, the actual native data
  private short flags3 = 0;          // some final flags? or zero terminators?, sometimes not there

  /**
   * Creates an instance of this class from an embedded OLE Object. The OLE Object is expected
   * to include a stream &quot;{01}Ole10Native&quot; which contains the actual
   * data relevant for this class.
   *
   * @param poifs POI Filesystem object
   * @return Returns an instance of this class
   * @throws IOException on IO error
   * @throws Ole10NativeException on invalid or unexcepted data format
   */
  public static Ole10Native createFromEmbeddedOleObject(POIFSFileSystem poifs) throws IOException, Ole10NativeException {
     return createFromEmbeddedOleObject(poifs.getRoot());
  }
  
  /**
   * Creates an instance of this class from an embedded OLE Object. The OLE Object is expected
   * to include a stream &quot;{01}Ole10Native&quot; which contains the actual
   * data relevant for this class.
   *
   * @param directory POI Filesystem object
   * @return Returns an instance of this class
   * @throws IOException on IO error
   * @throws Ole10NativeException on invalid or unexcepted data format
   */
  public static Ole10Native createFromEmbeddedOleObject(DirectoryNode directory) throws IOException, Ole10NativeException {
     boolean plain = false;

     try {
        directory.getEntry("\u0001Ole10ItemName");
        plain = true;
     } catch (FileNotFoundException ex) {
        plain = false;
     }
     
     DocumentEntry nativeEntry = 
        (DocumentEntry)directory.getEntry(OLE10_NATIVE);
     byte[] data = new byte[nativeEntry.getSize()];
     directory.createDocumentInputStream(nativeEntry).read(data);

     return new Ole10Native(data, 0, plain);
  }
  
  /**
   * Creates an instance and fills the fields based on ... the fields
   */
  public Ole10Native(String label, String filename, String command, byte[] data) {
	  setLabel(label);
	  setFileName(filename);
	  setCommand(command);
	  setDataBuffer(data);
  }
  
  /**
   * Creates an instance and fills the fields based on the data in the given buffer.
   *
   * @param data   The buffer containing the Ole10Native record
   * @param offset The start offset of the record in the buffer
   * @throws Ole10NativeException on invalid or unexcepted data format
   */
  public Ole10Native(byte[] data, int offset) throws Ole10NativeException {
    this(data, offset, false);
  }
  /**
   * Creates an instance and fills the fields based on the data in the given buffer.
   *
   * @param data   The buffer containing the Ole10Native record
   * @param offset The start offset of the record in the buffer
   * @param plain Specified 'plain' format without filename
   * @throws Ole10NativeException on invalid or unexcepted data format
   */
  public Ole10Native(byte[] data, int offset, boolean plain) throws Ole10NativeException {
    int ofs = offset;        // current offset, initialized to start

    if (data.length<offset+2) {
      throw new Ole10NativeException("data is too small");
    }

    totalSize = LittleEndian.getInt(data, ofs);
    ofs += LittleEndianConsts.INT_SIZE;

    if (plain) {
      dataBuffer = new byte[totalSize-4];
      System.arraycopy(data, 4, dataBuffer, 0, dataBuffer.length);
      // int dataSize = totalSize - 4;
      
      byte[] oleLabel = new byte[8];
      System.arraycopy(dataBuffer, 0, oleLabel, 0, Math.min(dataBuffer.length, 8));
      label = "ole-"+ HexDump.toHex(oleLabel);
      fileName = label;
      command = label;
    } else {
      flags1 = LittleEndian.getShort(data, ofs);
      ofs += LittleEndianConsts.SHORT_SIZE;
      
      int len = getStringLength(data, ofs);
      label = StringUtil.getFromCompressedUnicode(data, ofs, len - 1);
      ofs += len;
      
      len = getStringLength(data, ofs);
      fileName = StringUtil.getFromCompressedUnicode(data, ofs, len - 1);
      ofs += len;
      
      flags2 = LittleEndian.getShort(data, ofs);
      ofs += LittleEndianConsts.SHORT_SIZE;
      
      unknown1 = LittleEndian.getShort(data, ofs);
      ofs += LittleEndianConsts.SHORT_SIZE;

      len = LittleEndian.getInt(data, ofs);
      ofs += LittleEndianConsts.INT_SIZE;

      command = StringUtil.getFromCompressedUnicode(data, ofs, len - 1);
      ofs += len;
      
      if (totalSize < ofs) {
          throw new Ole10NativeException("Invalid Ole10Native");
      }

      int dataSize = LittleEndian.getInt(data, ofs);
      ofs += LittleEndianConsts.INT_SIZE;

      if (dataSize < 0 || totalSize - (ofs - LittleEndianConsts.INT_SIZE) < dataSize) {
          throw new Ole10NativeException("Invalid Ole10Native");
      }
      
      dataBuffer = new byte[dataSize];
      System.arraycopy(data, ofs, dataBuffer, 0, dataSize);
      ofs += dataSize;
    }
  }

  /*
   * Helper - determine length of zero terminated string (ASCIIZ).
   */
  private static int getStringLength(byte[] data, int ofs) {
    int len = 0;
    while (len+ofs<data.length && data[ofs + len] != 0) {
      len++;
    }
    len++;
    return len;
  }

  /**
   * Returns the value of the totalSize field - the total length of the structure
   * is totalSize + 4 (value of this field + size of this field).
   *
   * @return the totalSize
   */
  public int getTotalSize() {
    return totalSize;
  }

  /**
   * Returns flags1 - currently unknown - usually 0x0002.
   *
   * @return the flags1
   */
  public short getFlags1() {
    return flags1;
  }

  /**
   * Returns the label field - usually the name of the file (without directory) but
   * probably may be any name specified during packaging/embedding the data.
   *
   * @return the label
   */
  public String getLabel() {
    return label;
  }

  /**
   * Returns the fileName field - usually the name of the file being embedded
   * including the full path.
   *
   * @return the fileName
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Returns flags2 - currently unknown - mostly 0x0000.
   *
   * @return the flags2
   */
  public short getFlags2() {
    return flags2;
  }

  /**
   * Returns unknown1 field - currently unknown.
   *
   * @return the unknown1
   */
  public short getUnknown1() {
    return unknown1;
  }

  /**
   * Returns the command field - usually the name of the file being embedded
   * including the full path, may be a command specified during embedding the file.
   *
   * @return the command
   */
  public String getCommand() {
    return command;
  }

  /**
   * Returns the size of the embedded file. If the size is 0 (zero), no data has been
   * embedded. To be sure, that no data has been embedded, check whether
   * {@link #getDataBuffer()} returns <code>null</code>.
   *
   * @return the dataSize
   */
  public int getDataSize() {
    return dataBuffer.length;
  }

  /**
   * Returns the buffer containing the embedded file's data, or <code>null</code>
   * if no data was embedded. Note that an embedding may provide information about
   * the data, but the actual data is not included. (So label, filename etc. are
   * available, but this method returns <code>null</code>.)
   *
   * @return the dataBuffer
   */
  public byte[] getDataBuffer() {
    return dataBuffer;
  }

  /**
   * Returns the flags3 - currently unknown.
   *
   * @return the flags3
   */
  public short getFlags3() {
    return flags3;
  }

  /**
   * Have the contents printer out into an OutputStream, used when writing a
   * file back out to disk (Normally, atom classes will keep their bytes
   * around, but non atom classes will just request the bytes from their
   * children, then chuck on their header and return)
   */
  public void writeOut(OutputStream out) throws IOException {
      byte intbuf[] = new byte[LittleEndianConsts.INT_SIZE];
      byte shortbuf[] = new byte[LittleEndianConsts.SHORT_SIZE];

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      bos.write(intbuf); // total size, will be determined later ..

      LittleEndian.putShort(shortbuf, 0, getFlags1());
      bos.write(shortbuf);

      bos.write(getLabel().getBytes(ISO1));
      bos.write(0);

      bos.write(getFileName().getBytes(ISO1));
      bos.write(0);

      LittleEndian.putShort(shortbuf, 0, getFlags2());
      bos.write(shortbuf);

      LittleEndian.putShort(shortbuf, 0, getUnknown1());
      bos.write(shortbuf);

      LittleEndian.putInt(intbuf, 0, getCommand().length()+1);
      bos.write(intbuf);

      bos.write(getCommand().getBytes(ISO1));
      bos.write(0);

      LittleEndian.putInt(intbuf, 0, getDataBuffer().length);
      bos.write(intbuf);

      bos.write(getDataBuffer());

      LittleEndian.putShort(shortbuf, 0, getFlags3());
      bos.write(shortbuf);

      // update total size - length of length-field (4 bytes)
      byte data[] = bos.toByteArray();
      totalSize = data.length - LittleEndianConsts.INT_SIZE;
      LittleEndian.putInt(data, 0, totalSize);

      out.write(data);
  }

  public void setFlags1(short flags1) {
      this.flags1 = flags1;
  }

  public void setFlags2(short flags2) {
      this.flags2 = flags2;
  }

  public void setFlags3(short flags3) {
      this.flags3 = flags3;
  }

  public void setLabel(String label) {
      this.label = label;
  }

  public void setFileName(String fileName) {
      this.fileName = fileName;
  }

  public void setCommand(String command) {
      this.command = command;
  }

  public void setUnknown1(short unknown1) {
      this.unknown1 = unknown1;
  }

  public void setDataBuffer(byte dataBuffer[]) {
      this.dataBuffer = dataBuffer;
  }
}
