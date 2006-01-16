/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.hslf.usermodel;

import org.apache.poi.util.LittleEndian;

/**
 * Represents a picture in a PowerPoint document.
 * <p>
 * The information about an image in PowerPoint document is stored in 
 * two places:
 *  <li> EscherBSE container in the Document keeps information about image 
 *    type, image index to refer by slides etc.
 *  <li> "Pictures" OLE stream holds the actual data of the image.
 * </p>
 * <p>
 *  Data in the "Pictures" OLE stream is organized as follows:<br>
 *  For each image there is an entry: 25 byte header + image data.
 *  Image data is the exact content of the JPEG file, i.e. PowerPoint
 *  puts the whole jpeg file there without any modifications.<br>
 *   Header format:
 *    <li> 2 byte: image type. For JPEGs it is 0x46A0, for PNG it is 0x6E00.
 *    <li> 2 byte: unknown.
 *    <li> 4 byte : image size + 17. Looks like shift from the end of 
 *          header but why to add it to the image  size?
 *    <li> next 16 bytes. Unique identifier of this image which is used by 
 *          EscherBSE record.
 *  </p>
 *
 * @author Yegor Kozlov
 */
public class Picture {

	/**
	*  Windows Metafile
	*/
	public static final int WMF = 0x2160;

	/**
	* Macintosh PICT
	*/
	public static final int PICT = 0x5420;

	/**
	*  JPEG
	*/
	public static final int JPEG = 0x46A0;

	/**
	*  PNG
	*/
	public static final int PNG = 0x6E00;

	/**
	* Windows DIB (BMP)
	*/
	public static final int DIB = 0x7A80;

	/**
	* The size of the header
	*/
	public static final int HEADER_SIZE = 25;

	/**
	* Binary data of the picture
	*/
	protected byte[] pictdata;

	/**
	* Header which holds information about this picture
	*/
	protected byte[] header;

	/**
	* Read a picture from "Pictures" OLE stream
	*
	* @param pictstream    the bytes to read
	* @param offset        the index of the first byte to read
	*/
	public Picture(byte[] pictstream, int offset){
		header = new byte[Picture.HEADER_SIZE];
		System.arraycopy(pictstream, offset, header, 0, header.length);

		int size = LittleEndian.getInt(header, 4) - 17;
		pictdata = new byte[size];
		System.arraycopy(pictstream, offset + Picture.HEADER_SIZE, pictdata, 0, pictdata.length);
	}

	/**
	* @return  the binary data of this picture
	*/
	public byte[] getData(){
		return pictdata;
	}

	/**
	* Return image size in bytes
	*
	* @return the size of the picture in bytes
	*/
	public int getSize(){
		return pictdata.length;
	}

	/**
	* Returns the unique identifier (UID) of this picture.
	* The UID is a checksum of the picture data. Its length is 16 bytes
	* and it must be unique across the presentation.
	*
	* @return the unique identifier of this picture
	*/
	public byte[] getUID(){
		byte[] uid = new byte[16];
		System.arraycopy(header, 8, uid, 0, uid.length);
		return uid;
	}

	/**
	* Returns the type of this picture. Must be one of the static constans defined in this class.
	*
	* @return type of this picture.
	*/
	public int getType(){
		int type = LittleEndian.getShort(header, 0);
		return type;
	}
}
