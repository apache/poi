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

package org.apache.poi.hslf.usermodel;

import java.awt.Dimension;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.hslf.blip.DIB;
import org.apache.poi.hslf.blip.EMF;
import org.apache.poi.hslf.blip.JPEG;
import org.apache.poi.hslf.blip.PICT;
import org.apache.poi.hslf.blip.PNG;
import org.apache.poi.hslf.blip.WMF;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.Units;

/**
 * A class that represents image data contained in a slide show.
 */
public abstract class HSLFPictureData implements PictureData, GenericRecord {

    /**
     * Size of the image checksum calculated using MD5 algorithm.
     */
    protected static final int CHECKSUM_SIZE = 16;

    /**
    * Binary data of the picture
    */
    private byte[] rawdata;
    /**
     * The offset to the picture in the stream
     */
    private int offset;

    /**
     * The instance type/signatures defines if one or two UID instances will be included
     */
    private int uidInstanceCount = 1;

    /**
     * The 1-based index within the pictures stream
     */
    private int index = -1;

    /**
     * Blip signature.
     */
    protected abstract int getSignature();

    public abstract void setSignature(int signature);

    /**
     * The instance type/signatures defines if one or two UID instances will be included
     */
    protected int getUIDInstanceCount() {
        return uidInstanceCount;
    }

    /**
     * The instance type/signatures defines if one or two UID instances will be included
     *
     * @param uidInstanceCount the number of uid sequences
     */
    protected void setUIDInstanceCount(int uidInstanceCount) {
        this.uidInstanceCount = uidInstanceCount;
    }

    /**
     * Returns the raw binary data of this Picture excluding the first 8 bytes
     * which hold image signature and size of the image data.
     *
     * @return picture data
     */
    public byte[] getRawData(){
        return rawdata;
    }

    public void setRawData(byte[] data){
        rawdata = (data == null) ? null : data.clone();
    }

    /**
     * File offset in the 'Pictures' stream
     *
     * @return offset in the 'Pictures' stream
     */
    public int getOffset(){
        return offset;
    }

    /**
     * Set offset of this picture in the 'Pictures' stream.
     * We need to set it when a new picture is created.
     *
     * @param offset in the 'Pictures' stream
     */
    public void setOffset(int offset){
        this.offset = offset;
    }

    /**
     * Returns 16-byte checksum of this picture
     */
    public byte[] getUID(){
        return Arrays.copyOf(rawdata, 16);
    }

    @Override
    public byte[] getChecksum() {
        return getChecksum(getData());
    }

    /**
     * Compute 16-byte checksum of this picture using MD5 algorithm.
     */
    public static byte[] getChecksum(byte[] data) {
        MessageDigest md5 = CryptoFunctions.getMessageDigest(HashAlgorithm.md5);
        md5.update(data);
        return md5.digest();
    }

    /**
     * Write this picture into <code>OutputStream</code>
     */
    public void write(OutputStream out) throws IOException {
        LittleEndian.putUShort(getSignature(), out);

        PictureType pt = getType();
        LittleEndian.putUShort(pt.nativeId + 0xF018, out);

        byte[] rd = getRawData();
        LittleEndian.putInt(rd.length, out);
        out.write(rd);
    }

    /**
     * Create an instance of <code>PictureData</code> by type.
     *
     * @param type type of the picture data.
     * Must be one of the static constants defined in the <code>Picture<code> class.
     * @return concrete instance of <code>PictureData</code>
     */
     public static HSLFPictureData create(PictureType type){
        HSLFPictureData pict;
        switch (type){
            case EMF: pict = new EMF(); break;
            case WMF: pict = new WMF(); break;
            case PICT: pict = new PICT(); break;
            case JPEG: pict = new JPEG(); break;
            case PNG: pict = new PNG(); break;
            case DIB: pict = new DIB(); break;
            default:
                throw new IllegalArgumentException("Unsupported picture type: " + type);
        }
        return pict;
    }

    /**
     * Return 24 byte header which preceeds the actual picture data.
     * <p>
     * The header consists of 2-byte signature, 2-byte type,
     * 4-byte image size and 16-byte checksum of the image data.
     * </p>
     *
     * @return the 24 byte header which preceeds the actual picture data.
     */
    public byte[] getHeader() {
        byte[] header = new byte[16 + 8];
        LittleEndian.putInt(header, 0, getSignature());
        LittleEndian.putInt(header, 4, getRawData().length);
        System.arraycopy(rawdata, 0, header, 8, 16);
        return header;
    }

    /**
     * @return the 1-based index of this pictures within the pictures stream
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index sets the 1-based index of this pictures within the pictures stream
     */
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public final String getContentType() {
        return getType().contentType;
    }

    @Override
    public Dimension getImageDimensionInPixels() {
        Dimension dim = getImageDimension();
        return new Dimension(
            Units.pointsToPixel(dim.getWidth()),
            Units.pointsToPixel(dim.getHeight())
        );
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        final Map<String,Supplier<?>> m = new LinkedHashMap<>();
        m.put("type", this::getType);
        m.put("imageDimension", this::getImageDimension);
        m.put("signature", this::getSignature);
        m.put("uidInstanceCount", this::getUIDInstanceCount);
        m.put("offset", this::getOffset);
        m.put("uid", this::getUID);
        m.put("checksum", this::getChecksum);
        m.put("index", this::getIndex);
        m.put("rawData", this::getRawData);
        return Collections.unmodifiableMap(m);
    }
}
