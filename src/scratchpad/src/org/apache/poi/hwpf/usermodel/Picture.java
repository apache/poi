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

package org.apache.poi.hwpf.usermodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.poi.hwpf.model.PictureDescriptor;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Represents embedded picture extracted from Word Document
 * @author Dmitry Romanov
 */
public final class Picture extends PictureDescriptor
{
  private static final POILogger log = POILogFactory.getLogger(Picture.class);

//  public static final int FILENAME_OFFSET = 0x7C;
//  public static final int FILENAME_SIZE_OFFSET = 0x6C;
  static final int PICF_OFFSET = 0x0;
  static final int PICT_HEADER_OFFSET = 0x4;
  static final int MFPMM_OFFSET = 0x6;
  static final int PICF_SHAPE_OFFSET = 0xE;
  static final int UNKNOWN_HEADER_SIZE = 0x49;

    @Deprecated
    public static final byte[] GIF = PictureType.GIF.getSignatures()[0];
    @Deprecated
    public static final byte[] PNG = PictureType.PNG.getSignatures()[0];
    @Deprecated
    public static final byte[] JPG = PictureType.JPEG.getSignatures()[0];
    @Deprecated
    public static final byte[] BMP = PictureType.BMP.getSignatures()[0];
    @Deprecated
    public static final byte[] TIFF = PictureType.TIFF.getSignatures()[0];
    @Deprecated
    public static final byte[] TIFF1 = PictureType.TIFF.getSignatures()[1];

    @Deprecated
    public static final byte[] EMF = PictureType.EMF.getSignatures()[0];
    @Deprecated
    public static final byte[] WMF1 = PictureType.WMF.getSignatures()[0];
    // Windows 3.x
    @Deprecated
    public static final byte[] WMF2 = PictureType.WMF.getSignatures()[1];
    // TODO: DIB, PICT

  public static final byte[] IHDR = new byte[]{'I', 'H', 'D', 'R'};

  public static final byte[] COMPRESSED1 = { (byte)0xFE, 0x78, (byte)0xDA };
  public static final byte[] COMPRESSED2 = { (byte)0xFE, 0x78, (byte)0x9C };

  private int dataBlockStartOfsset;
  private int pictureBytesStartOffset;
  private int dataBlockSize;
  private int size;
//  private String fileName;
  private byte[] rawContent;
  private byte[] content;
  private byte[] _dataStream;
  private int height = -1;
  private int width = -1;

  public Picture(int dataBlockStartOfsset, byte[] _dataStream, boolean fillBytes)
  {
      super (_dataStream, dataBlockStartOfsset);

    this._dataStream = _dataStream;
    this.dataBlockStartOfsset = dataBlockStartOfsset;
    this.dataBlockSize = LittleEndian.getInt(_dataStream, dataBlockStartOfsset);
    this.pictureBytesStartOffset = getPictureBytesStartOffset(dataBlockStartOfsset, _dataStream, dataBlockSize);
    this.size = dataBlockSize - (pictureBytesStartOffset - dataBlockStartOfsset);

    if (size<0) {

    }

    if (fillBytes)
    {
      fillImageContent();
    }
  }

  public Picture(byte[] _dataStream)
  {
        super();

      this._dataStream = _dataStream;
      this.dataBlockStartOfsset = 0;
      this.dataBlockSize = _dataStream.length;
      this.pictureBytesStartOffset = 0;
      this.size = _dataStream.length;
  }

    private void fillWidthHeight()
    {
        PictureType pictureType = suggestPictureType();
        // trying to extract width and height from pictures content:
        switch ( pictureType )
        {
        case JPEG:
            fillJPGWidthHeight();
            break;
        case PNG:
            fillPNGWidthHeight();
            break;
        default:
            // unsupported;
            break;
        }
    }

  /**
   * Tries to suggest a filename: hex representation of picture structure offset in "Data" stream plus extension that
   * is tried to determine from first byte of picture's content.
   *
   * @return suggested file name
   */
  public String suggestFullFileName()
  {
    String fileExt = suggestFileExtension();
    return Integer.toHexString(dataBlockStartOfsset) + (fileExt.length()>0 ? "."+fileExt : "");
  }

  /**
   * Writes Picture's content bytes to specified OutputStream.
   * Is useful when there is need to write picture bytes directly to stream, omitting its representation in
   * memory as distinct byte array.
   *
   * @param out a stream to write to
   * @throws IOException if some exception is occured while writing to specified out
   */
  public void writeImageContent(OutputStream out) throws IOException
  {
    if (rawContent!=null && rawContent.length>0) {
      out.write(rawContent, 0, size);
    } else {
      out.write(_dataStream, pictureBytesStartOffset, size);
    }
  }

  /**
   * @return The offset of this picture in the picture bytes, used
   *  when matching up with {@link CharacterRun#getPicOffset()}
   */
  public int getStartOffset() {
     return dataBlockStartOfsset;
  }

    /**
     * @return picture's content as byte array
     */
    public byte[] getContent()
    {
        fillImageContent();
        return content;
    }

    /**
     * Returns picture's content as it stored in Word file, i.e. possibly in
     * compressed form.
     * 
     * @return picture's content as it stored in Word file
     */
    public byte[] getRawContent()
    {
        fillRawImageContent();
        return rawContent;
    }

  /**
   *
   * @return size in bytes of the picture
   */
  public int getSize()
  {
    return size;
  }

    /**
     * @return the horizontal aspect ratio for picture provided by user
     * @deprecated use more precise {@link #getHorizontalScalingFactor()}
     */
    @Deprecated
    public int getAspectRatioX()
    {
        return mx / 10;
    }

    /**
     * @return Horizontal scaling factor supplied by user expressed in .001%
     *         units
     */
    public int getHorizontalScalingFactor()
    {
        return mx;
    }

    /**
     * @retrn the vertical aspect ratio for picture provided by user
     * @deprecated use more precise {@link #getVerticalScalingFactor()}
     */
    @Deprecated
    public int getAspectRatioY()
    {
        return my / 10;
    }

    /**
     * @return Vertical scaling factor supplied by user expressed in .001% units
     */
    public int getVerticalScalingFactor()
    {
        return my;
    }

    /**
     * Gets the initial width of the picture, in twips, prior to cropping or scaling.
     *
     * @return the initial width of the picture in twips
     */
    public int getDxaGoal() {
        return dxaGoal;
    }

    /**
     * Gets the initial height of the picture, in twips, prior to cropping or scaling.
     *
     * @return the initial width of the picture in twips
     */
    public int getDyaGoal() {
        return dyaGoal;
    }

    /**
     * @return The amount the picture has been cropped on the left in twips
     */
    public int getDxaCropLeft() {
        return dxaCropLeft;
    }

    /**
     * @return The amount the picture has been cropped on the top in twips
     */
    public int getDyaCropTop() {
        return dyaCropTop;
    }

    /**
     * @return The amount the picture has been cropped on the right in twips
     */
    public int getDxaCropRight() {
        return dxaCropRight;
    }

    /**
     * @return The amount the picture has been cropped on the bottom in twips
     */
    public int getDyaCropBottom() {
        return dyaCropBottom;
    }

    /**
     * tries to suggest extension for picture's file by matching signatures of
     * popular image formats to first bytes of picture's contents
     * 
     * @return suggested file extension
     */
    public String suggestFileExtension()
    {
        return suggestPictureType().getExtension();
    }

    /**
     * Returns the MIME type for the image
     * 
     * @return MIME-type for known types of image or "image/unknown" if unknown
     */
    public String getMimeType()
    {
        return suggestPictureType().getMime();
    }

    public PictureType suggestPictureType()
    {
        final byte[] imageContent = getContent();
        for ( PictureType pictureType : PictureType.values() )
            for ( byte[] signature : pictureType.getSignatures() )
                if ( matchSignature( imageContent, signature, 0 ) )
                    return pictureType;

        // TODO: DIB, PICT
        return PictureType.UNKNOWN;
    }

  private static boolean matchSignature(byte[] dataStream, byte[] signature, int pictureBytesOffset)
  {
    boolean matched = pictureBytesOffset < dataStream.length;
    for (int i = 0; (i+pictureBytesOffset) < dataStream.length && i < signature.length; i++)
    {
      if (dataStream[i+pictureBytesOffset] != signature[i])
      {
        matched = false;
        break;
      }
    }
    return matched;
  }

//  public String getFileName()
//  {
//    return fileName;
//  }

//  private static String extractFileName(int blockStartIndex, byte[] dataStream) {
//        int fileNameStartOffset = blockStartIndex + 0x7C;
//        int fileNameSizeOffset = blockStartIndex + FILENAME_SIZE_OFFSET;
//        int fileNameSize = LittleEndian.getShort(dataStream, fileNameSizeOffset);
//
//        int fileNameIndex = fileNameStartOffset;
//        char[] fileNameChars = new char[(fileNameSize-1)/2];
//        int charIndex = 0;
//        while(charIndex<fileNameChars.length) {
//            short aChar = LittleEndian.getShort(dataStream, fileNameIndex);
//            fileNameChars[charIndex] = (char)aChar;
//            charIndex++;
//            fileNameIndex += 2;
//        }
//        String fileName = new String(fileNameChars);
//        return fileName.trim();
//    }

  private void fillRawImageContent()
  {
        if ( rawContent != null && rawContent.length > 0 )
            return;

    this.rawContent = new byte[size];
    System.arraycopy(_dataStream, pictureBytesStartOffset, rawContent, 0, size);
  }

  private void fillImageContent()
  {
        if ( content != null && content.length > 0 )
            return;

    byte[] rawContent = getRawContent();

    // HACK: Detect compressed images.  In reality there should be some way to determine
    //       this from the first 32 bytes, but I can't see any similarity between all the
    //       samples I have obtained, nor any similarity in the data block contents.
    if (matchSignature(rawContent, COMPRESSED1, 32) || matchSignature(rawContent, COMPRESSED2, 32))
    {
      try
      {
        InflaterInputStream in = new InflaterInputStream(
          new ByteArrayInputStream(rawContent, 33, rawContent.length - 33));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int readBytes;
        while ((readBytes = in.read(buf)) > 0)
        {
          out.write(buf, 0, readBytes);
        }
        content = out.toByteArray();
      }
      catch (IOException e)
      {
        // Problems reading from the actual ByteArrayInputStream should never happen
        // so this will only ever be a ZipException.
        log.log(POILogger.INFO, "Possibly corrupt compression or non-compressed data", e);
      }
    } else {
      // Raw data is not compressed.
      content = rawContent;
    }
  }

  private static int getPictureBytesStartOffset(int dataBlockStartOffset, byte[] _dataStream, int dataBlockSize)
  {
    int realPicoffset = dataBlockStartOffset;
    final int dataBlockEndOffset = dataBlockSize + dataBlockStartOffset;

    // Skip over the PICT block
    int PICTFBlockSize = LittleEndian.getShort(_dataStream, dataBlockStartOffset +PICT_HEADER_OFFSET); // Should be 68 bytes

    // Now the PICTF1
    int PICTF1BlockOffset = PICTFBlockSize + PICT_HEADER_OFFSET;
    short MM_TYPE = LittleEndian.getShort(_dataStream, dataBlockStartOffset + PICT_HEADER_OFFSET + 2);
    if(MM_TYPE == 0x66) {
       // Skip the stPicName
       int cchPicName = LittleEndian.getUnsignedByte(_dataStream, PICTF1BlockOffset);
       PICTF1BlockOffset += 1 + cchPicName;
    }
    int PICTF1BlockSize = LittleEndian.getShort(_dataStream, dataBlockStartOffset +PICTF1BlockOffset);

    int unknownHeaderOffset = (PICTF1BlockSize + PICTF1BlockOffset) < dataBlockEndOffset ?  (PICTF1BlockSize + PICTF1BlockOffset) : PICTF1BlockOffset;
    realPicoffset += (unknownHeaderOffset + UNKNOWN_HEADER_SIZE);
    if (realPicoffset>=dataBlockEndOffset) {
        realPicoffset -= UNKNOWN_HEADER_SIZE;
    }
    return realPicoffset;
  }

  private void fillJPGWidthHeight() {
    /*
    http://www.codecomments.com/archive281-2004-3-158083.html

    Algorhitm proposed by Patrick TJ McPhee:

    read 2 bytes
    make sure they are 'ffd8'x
    repeatedly:
    read 2 bytes
    make sure the first one is 'ff'x
    if the second one is 'd9'x stop
    else if the second one is c0 or c2 (or possibly other values ...)
    skip 2 bytes
    read one byte into depth
    read two bytes into height
    read two bytes into width
    else
    read two bytes into length
    skip forward length-2 bytes

    Also used Ruby code snippet from: http://www.bigbold.com/snippets/posts/show/805 for reference
    */
    int pointer = pictureBytesStartOffset+2;
    int firstByte = _dataStream[pointer];
    int secondByte = _dataStream[pointer+1];

    int endOfPicture = pictureBytesStartOffset + size;
    while(pointer<endOfPicture-1) {
      do {
        firstByte = _dataStream[pointer];
        secondByte = _dataStream[pointer+1];
        pointer += 2;
      } while (!(firstByte==(byte)0xFF) && pointer<endOfPicture-1);

      if (firstByte==((byte)0xFF) && pointer<endOfPicture-1) {
        if (secondByte==(byte)0xD9 || secondByte==(byte)0xDA) {
          break;
        } else if ( (secondByte & 0xF0) == 0xC0 && secondByte!=(byte)0xC4 && secondByte!=(byte)0xC8 && secondByte!=(byte)0xCC) {
          pointer += 5;
          this.height = getBigEndianShort(_dataStream, pointer);
          this.width = getBigEndianShort(_dataStream, pointer+2);
          break;
        } else {
          pointer++;
          pointer++;
          int length = getBigEndianShort(_dataStream, pointer);
          pointer+=length;
        }
      } else {
        pointer++;
      }
    }
  }

  private void fillPNGWidthHeight()
  {
    /*
     Used PNG file format description from http://www.wotsit.org/download.asp?f=png
    */
    int HEADER_START = pictureBytesStartOffset + PNG.length + 4;
    if (matchSignature(_dataStream, IHDR, HEADER_START)) {
      int IHDR_CHUNK_WIDTH = HEADER_START + 4;
      this.width = getBigEndianInt(_dataStream, IHDR_CHUNK_WIDTH);
      this.height = getBigEndianInt(_dataStream, IHDR_CHUNK_WIDTH + 4);
    }
  }

  /**
   * returns pixel width of the picture or -1 if dimensions determining was failed
   */
  public int getWidth()
  {
    if (width == -1)
    {
      fillWidthHeight();
    }
    return width;
  }

  /**
   * returns pixel height of the picture or -1 if dimensions determining was failed
   */
  public int getHeight()
  {
    if (height == -1)
    {
      fillWidthHeight();
    }
    return height;
  }

  private static int getBigEndianInt(byte[] data, int offset)
  {
    return (((data[offset] & 0xFF)<< 24) + ((data[offset +1] & 0xFF) << 16) + ((data[offset + 2] & 0xFF) << 8) + (data[offset +3] & 0xFF));
  }

  private static int getBigEndianShort(byte[] data, int offset)
  {
    return (((data[offset] & 0xFF)<< 8) + (data[offset +1] & 0xFF));
  }

}
