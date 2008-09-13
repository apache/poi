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

package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

/**
 * Title:  Record Input Stream<P>
 * Description:  Wraps a stream and provides helper methods for the construction of records.<P>
 *
 * @author Jason Height (jheight @ apache dot org)
 */
public class RecordInputStream extends InputStream {
  /** Maximum size of a single record (minus the 4 byte header) without a continue*/
  public final static short MAX_RECORD_DATA_SIZE = 8224;
  private static final int INVALID_SID_VALUE = -1;
  
  private InputStream in;
  protected short currentSid;
  protected short currentLength = -1;
  protected short nextSid;

  protected byte[] data = new byte[MAX_RECORD_DATA_SIZE];
  protected short recordOffset;
  protected long pos;
  
  private boolean autoContinue = true;

  public RecordInputStream(InputStream in) throws RecordFormatException  {
    this.in = in;
    try {
      nextSid = LittleEndian.readShort(in);
      //Dont increment the pos just yet (technically we are at the start of
      //the record stream until nextRecord is called).      
    } catch (IOException ex) {
      throw new RecordFormatException("Error reading bytes", ex);
    }
  }
  
  /** This method will read a byte from the current record*/
  public int read() {
    checkRecordPosition();

    byte result = data[recordOffset];
    recordOffset += 1;
    pos += 1;
    return result;    
  }
  
  public short getSid() {
    return currentSid;
  }
  
  public short getLength() {
    return currentLength;
  }

  public short getRecordOffset() {
    return recordOffset;
  }

  public long getPos() {
    return pos;
  }

  public boolean hasNextRecord() {
    return nextSid != INVALID_SID_VALUE;
  }
  
  /** Moves to the next record in the stream.
   * 
   * <i>Note: The auto continue flag is reset to true</i>
   */
  
  public void nextRecord() throws RecordFormatException {
    if ((currentLength != -1) && (currentLength != recordOffset)) {
      System.out.println("WARN. Unread "+remaining()+" bytes of record 0x"+Integer.toHexString(currentSid));
    }
    currentSid = nextSid;
    pos += LittleEndian.SHORT_SIZE;
    autoContinue = true;
    try {
      recordOffset = 0;
      currentLength = LittleEndian.readShort(in);     
      if (currentLength > MAX_RECORD_DATA_SIZE)
        throw new RecordFormatException("The content of an excel record cannot exceed "+MAX_RECORD_DATA_SIZE+" bytes");
      pos += LittleEndian.SHORT_SIZE;
      in.read(data, 0, currentLength);

      //Read the Sid of the next record
      if (in.available() < EOFRecord.ENCODED_SIZE) {
          if (in.available() > 0) {
              // some scrap left over?
              // ex45582-22397.xls has one extra byte after the last record
              // Excel reads that file OK
          }
          nextSid = INVALID_SID_VALUE;  
      } else {
          nextSid = LittleEndian.readShort(in);
          if (nextSid == INVALID_SID_VALUE) {
              throw new RecordFormatException("Found sid " + nextSid + " after record with sid 0x"
                      + Integer.toHexString(currentSid).toUpperCase());
          }
      }      
    } catch (IOException ex) {
      throw new RecordFormatException("Error reading bytes", ex);
    }
  }
  
  public void setAutoContinue(boolean enable) {
    this.autoContinue = enable;    
  }
  
  public boolean getAutoContinue() {
    return autoContinue;
  }
  
  protected void checkRecordPosition() {
    if (remaining() <= 0) {
      if (isContinueNext() && autoContinue) {
        nextRecord();
      }
      else throw new ArrayIndexOutOfBoundsException();
    }    
  }
  
  /**
   * Reads an 8 bit, signed value
   */
  public byte readByte() {
    checkRecordPosition();
    
    byte result = data[recordOffset];
    recordOffset += 1;
    pos += 1;
    return result;
  }
  
  /**
   * Reads a 16 bit, signed value
   */
  public short readShort() {
    checkRecordPosition();
    
    short result = LittleEndian.getShort(data, recordOffset);
    recordOffset += LittleEndian.SHORT_SIZE;
    pos += LittleEndian.SHORT_SIZE;
    return result;
  }

  public int readInt() {
    checkRecordPosition();
    
    int result = LittleEndian.getInt(data, recordOffset);
    recordOffset += LittleEndian.INT_SIZE;
    pos += LittleEndian.INT_SIZE;
    return result;
  }

  public long readLong() {
    checkRecordPosition();    
    
    long result = LittleEndian.getLong(data, recordOffset);
    recordOffset += LittleEndian.LONG_SIZE;
    pos += LittleEndian.LONG_SIZE;
    return result;
  }

  /**
   * Reads an 8 bit, unsigned value
   */
  public short readUByte() {
      short s = readByte();
      if(s < 0) {
          s += 256;
      }
      return s;
  }

  /**
   * Reads a 16 bit,un- signed value.
   * @return
   */
  public int readUShort() {
    checkRecordPosition();    
    
    int result = LittleEndian.getUShort(data, recordOffset);
    recordOffset += LittleEndian.SHORT_SIZE;
    pos += LittleEndian.SHORT_SIZE;
    return result;
  }

  public double readDouble() {
    checkRecordPosition();
    long valueLongBits = LittleEndian.getLong(data, recordOffset);
    double result = Double.longBitsToDouble(valueLongBits);
    if (Double.isNaN(result)) {
      throw new RuntimeException("Did not expect to read NaN");
    }
    recordOffset += LittleEndian.DOUBLE_SIZE;
    pos += LittleEndian.DOUBLE_SIZE;
    return result;
  }

  
  public short[] readShortArray() {
    checkRecordPosition();
    
    short[] arr = LittleEndian.getShortArray(data, recordOffset);
    final int size = (2 * (arr.length +1));
    recordOffset += size;
    pos += size;
    
    return arr;
  }
  
  /**     
   *  given a byte array of 16-bit unicode characters, compress to 8-bit and     
   *  return a string     
   *     
   * { 0x16, 0x00 } -0x16     
   *      
   * @param length the length of the final string
   * @return                                     the converted string
   * @exception  IllegalArgumentException        if len is too large (i.e.,
   *      there is not enough data in string to create a String of that     
   *      length)     
   */  
  public String readUnicodeLEString(int length) {
    if ((length < 0) || (((remaining() / 2) < length) && !isContinueNext())) {
            throw new IllegalArgumentException("Illegal length - asked for " + length + " but only " + (remaining()/2) + " left!");
    }

    StringBuffer buf = new StringBuffer(length);
    for (int i=0;i<length;i++) {
      if ((remaining() == 0) && (isContinueNext())){
        nextRecord();
        int compressByte = readByte();
        if(compressByte != 1) throw new IllegalArgumentException("compressByte in continue records must be 1 while reading unicode LE string");
      }
      char ch = (char)readShort();
      buf.append(ch); 
    }
    return buf.toString();
  }
    
  public String readCompressedUnicode(int length) {
    if ((length < 0) || ((remaining() < length) && !isContinueNext())) {
            throw new IllegalArgumentException("Illegal length " + length);
    }

    StringBuffer buf = new StringBuffer(length);
    for (int i=0;i<length;i++) {
      if ((remaining() == 0) && (isContinueNext())) {
          nextRecord();
          int compressByte = readByte();
          if(compressByte != 0) throw new IllegalArgumentException("compressByte in continue records must be 0 while reading compressed unicode");
      }
      byte b = readByte();
      char ch = (char)(0x00FF & b); // avoid sex
      buf.append(ch); 
    }
    return buf.toString();    
  }
  
  /** Returns an excel style unicode string from the bytes reminaing in the record.
   * <i>Note:</i> Unicode strings differ from <b>normal</b> strings due to the addition of
   * formatting information.
   * 
   * @return The unicode string representation of the remaining bytes.
   */
  public UnicodeString readUnicodeString() {
    return new UnicodeString(this);
  }
  
  /** Returns the remaining bytes for the current record.
   * 
   * @return The remaining bytes of the current record.
   */
  public byte[] readRemainder() {
    int size = remaining();
    byte[] result = new byte[size];
    System.arraycopy(data, recordOffset, result, 0, size);
    recordOffset += size;
    pos += size;
    return result;
  }
  
  /** Reads all byte data for the current record, including any
   *  that overlaps into any following continue records.
   * 
   *  @deprecated Best to write a input stream that wraps this one where there is
   *  special sub record that may overlap continue records.
   */  
  public byte[] readAllContinuedRemainder() {
    //Using a ByteArrayOutputStream is just an easy way to get a
    //growable array of the data.
    ByteArrayOutputStream out = new ByteArrayOutputStream(2*MAX_RECORD_DATA_SIZE);

    while (isContinueNext()) {
      byte[] b = readRemainder();      
      out.write(b, 0, b.length);
      nextRecord();
    }
    byte[] b = readRemainder();      
    out.write(b, 0, b.length);    
    
    return out.toByteArray();
  }

  /** The remaining number of bytes in the <i>current</i> record.
   * 
   * @return The number of bytes remaining in the current record
   */
  public int remaining() {
    return (currentLength - recordOffset);
  }

  /** Returns true iif a Continue record is next in the excel stream 
   * 
   * @return True when a ContinueRecord is next.
   */
  public boolean isContinueNext() {
    return (nextSid == ContinueRecord.sid);
  }
}
