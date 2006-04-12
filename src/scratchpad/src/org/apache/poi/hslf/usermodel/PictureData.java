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
import org.apache.poi.hslf.model.Picture;

import java.io.OutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A class that represents the image data contained in the Presentation.
 * 
 *
 *  @author Yegor Kozlov
 */
public class PictureData {

	/**
	* The size of the header
	*/
	public static final int HEADER_SIZE = 25;

    protected static final int JPEG_HEADER = -266516832;
    protected static final int PNG_HEADER = -266441216;

	/**
	* Binary data of the picture
	*/
	protected byte[] pictdata;

	/**
	* Header which holds information about this picture
	*/
	protected byte[] header;

    public PictureData(){
        header = new byte[PictureData.HEADER_SIZE];
    }

	/**
	* Read a picture from "Pictures" OLE stream
	*
	* @param pictstream    the bytes to read
	* @param offset        the index of the first byte to read
	*/
	public PictureData(byte[] pictstream, int offset){
		header = new byte[PictureData.HEADER_SIZE];
		System.arraycopy(pictstream, offset, header, 0, header.length);

		int size = LittleEndian.getInt(header, 4) - 17;
		pictdata = new byte[size];
		System.arraycopy(pictstream, offset + PictureData.HEADER_SIZE, pictdata, 0, pictdata.length);
	}

	/**
	* @return  the binary data of this picture
	*/
	public byte[] getData(){
		return pictdata;
	}

    /**
     *  Set picture data
     */
    public void setData(byte[] data) {
        pictdata = data;
        LittleEndian.putInt(header, 4, data.length + 17);
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
     * Set the unique identifier (UID) of this picture.
     *
     * @param uid checksum of the picture data
     */
    public void setUID(byte[] uid){
        System.arraycopy(uid, 0, header, 8, uid.length);
    }

	/**
	* Set the type of this picture.
	*
	* @return type of this picture.
    * Must be one of the static constans defined in the <code>Picture<code> class.
	*/
	public void setType(int format){
        switch (format){
            case Picture.JPEG: LittleEndian.putInt(header, 0, PictureData.JPEG_HEADER); break;
            case Picture.PNG: LittleEndian.putInt(header, 0, PictureData.PNG_HEADER); break;
        }
	}

    /**
     * Returns type of this picture.
     * Must be one of the static constans defined in the <code>Picture<code> class.
     *
     * @return type of this picture.
     */
    public int getType(){
        int format = 0;
        int val = LittleEndian.getInt(header, 0);
        switch (val){
            case PictureData.JPEG_HEADER: format = Picture.JPEG; break;
            case PictureData.PNG_HEADER: format = Picture.PNG; break;
        }
        return format;
    }

    /**
     * Returns the header of the Picture
     *
     * @return the header of the Picture
     */
    public byte[] getHeader(){
        return header;
    }

    /**
     * Compute 16-byte checksum of this picture
     */
    public static byte[] getChecksum(byte[] data) {
        MessageDigest sha;
        try {
            sha = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e){
            throw new RuntimeException(e.getMessage());
        }
        sha.update(data);
        return sha.digest();
    }

    /**
     * Write this picture into <code>OutputStream</code>
     */
    public void write(OutputStream out) throws IOException {
        out.write(header);
        out.write(pictdata);
    }

}
