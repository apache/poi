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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherRecordTypes;
import org.apache.poi.hslf.blip.DIB;
import org.apache.poi.hslf.blip.EMF;
import org.apache.poi.hslf.blip.JPEG;
import org.apache.poi.hslf.blip.PICT;
import org.apache.poi.hslf.blip.PNG;
import org.apache.poi.hslf.blip.WMF;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.Removal;
import org.apache.poi.util.Units;

/**
 * A class that represents image data contained in a slide show.
 */
public abstract class HSLFPictureData implements PictureData, GenericRecord {

    private static final Logger LOGGER = LogManager.getLogger(HSLFPictureData.class);

    /**
     * Size of the image checksum calculated using MD5 algorithm.
     */
    protected static final int CHECKSUM_SIZE = 16;

    /**
     * Size of the image preamble in bytes.
     * <p>
     * The preamble describes how the image should be decoded. All image types have the same preamble format. The
     * preamble has little endian encoding. Below is a diagram of the preamble contents.
     *
     * <pre>
     *  0               1               2               3
     *  0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |           Signature           |          Picture Type         |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |                       Formatted Length                        |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * </pre>
     */
    static final int PREAMBLE_SIZE = 8;

    /**
     * Binary data of the picture, formatted as it will be stored in the {@link HSLFSlideShow}.
     * <p>
     * This does not include the {@link #PREAMBLE_SIZE preamble}.
     */
    private byte[] formattedData;

    /**
     * The instance type/signatures defines if one or two UID instances will be included
     */
    private int uidInstanceCount = 1;

    /**
     * The 1-based index within the pictures stream
     */
    private int index = -1;

    /**
     * {@link EscherRecordTypes#BSTORE_CONTAINER BStore} record tracking all pictures. Should be attached to the
     * slideshow that this picture is linked to.
     */
    final EscherContainerRecord bStore;

    /**
     * Record referencing this picture. Should be attached to the slideshow that this picture is linked to.
     */
    final EscherBSERecord bse;

    /**
     * @deprecated Use {@link HSLFSlideShow#addPicture(byte[], org.apache.poi.sl.usermodel.PictureData.PictureType)} or one of its overloads to create new
     *             {@link HSLFPictureData}. This API led to detached {@link HSLFPictureData} instances (See Bugzilla
     *             46122) and prevented adding additional functionality.
     */
    @Deprecated
    @Removal(version = "5.3")
    public HSLFPictureData() {
        this(new EscherContainerRecord(), new EscherBSERecord());
        LOGGER.atWarn().log("The no-arg constructor is deprecated. Some functionality such as updating pictures won't " +
                "work.");
    }

    /**
     * Creates a new instance.
     *
     * @param bStore {@link EscherRecordTypes#BSTORE_CONTAINER BStore} record tracking all pictures. Should be attached
     *               to the slideshow that this picture is linked to.
     * @param bse Record referencing this picture. Should be attached to the slideshow that this picture is linked to.
     */
    @Internal
    protected HSLFPictureData(EscherContainerRecord bStore, EscherBSERecord bse) {
        this.bStore = Objects.requireNonNull(bStore);
        this.bse = Objects.requireNonNull(bse);
    }

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
     * Returns the formatted, binary data of this picture excluding the {@link #PREAMBLE_SIZE preamble} bytes.
     * <p>
     * Primarily intended for internal POI use. Use {@link #getData()} to retrieve the picture represented by this
     * object.
     *
     * @return Picture data formatted for the HSLF format.
     * @see #getData()
     * @see #formatImageForSlideshow(byte[])
     */
    public byte[] getRawData(){
        return formattedData;
    }

    /**
     * Sets the formatted data for this picture.
     * <p>
     * Primarily intended for internal POI use. Use {@link #setData(byte[])} to change the picture represented by this
     * object.
     *
     * @param data Picture data formatted for the HSLF format. Excludes the {@link #PREAMBLE_SIZE preamble}.
     * @see #setData(byte[])
     * @see #formatImageForSlideshow(byte[])
     * @deprecated Set image data using {@link #setData(byte[])}.
     */
    @Deprecated
    @Removal(version = "5.3")
    public void setRawData(byte[] data){
        formattedData = (data == null) ? null : data.clone();
    }

    /**
     * File offset in the 'Pictures' stream
     *
     * @return offset in the 'Pictures' stream
     */
    public int getOffset(){
        return bse.getOffset();
    }

    /**
     * Set offset of this picture in the 'Pictures' stream.
     * We need to set it when a new picture is created.
     *
     * @param offset in the 'Pictures' stream
     * @deprecated This function was only intended for POI internal use. If you have a use case you're concerned about,
     * please open an issue in the POI issue tracker.
     */
    @Deprecated
    @Removal(version = "5.3")
    public void setOffset(int offset){
        LOGGER.atWarn().log("HSLFPictureData#setOffset is deprecated.");
    }

    /**
     * Returns 16-byte checksum of this picture
     */
    public byte[] getUID(){
        return Arrays.copyOf(formattedData, CHECKSUM_SIZE);
    }

    @Override
    public byte[] getChecksum() {
        return getUID();
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
        LittleEndian.putUShort(pt.nativeId + EscherRecordTypes.BLIP_START.typeID, out);

        byte[] rd = getRawData();
        LittleEndian.putInt(rd.length, out);
        out.write(rd);
    }

    /**
     * Create an instance of {@link HSLFPictureData} by type.
     *
     * @param type type of picture.
     * @return concrete instance of {@link HSLFPictureData}.
     * @deprecated Use {@link HSLFSlideShow#addPicture(byte[], org.apache.poi.sl.usermodel.PictureData.PictureType)} or one of its overloads to create new
     *             {@link HSLFPictureData}. This API led to detached {@link HSLFPictureData} instances (See Bugzilla
     *             46122) and prevented adding additional functionality.
     */
    @Deprecated
    @Removal(version = "5.3")
     public static HSLFPictureData create(PictureType type){
        LOGGER.atWarn().log("HSLFPictureData#create(PictureType) is deprecated. Some functionality such " +
                "as updating pictures won't work.");

        // This record code is a stub. It exists only for API compatibility.
        EscherContainerRecord record = new EscherContainerRecord();
        EscherBSERecord bse = new EscherBSERecord();
        return new HSLFSlideShowImpl.PictureFactory(record, type, new byte[0], 0, 0)
                .setRecord(bse)
                .build();
    }

    /**
     * Creates a new instance of the given image type using data already formatted for storage inside the slideshow.
     * <p>
     * This function is most handy when parsing an existing slideshow, as the picture data are already formatted.
     * @param type Image type.
     * @param recordContainer Record tracking all pictures. Should be attached to the slideshow that this picture is
     *                        linked to.
     * @param bse Record referencing this picture. Should be attached to the slideshow that this picture is linked to.
     * @param data Image data formatted for storage in the slideshow. This does not include the
     *        {@link #PREAMBLE_SIZE preamble}.
     * @param signature Image format-specific signature. See subclasses for signature details.
     * @return New instance.
     *
     * @see #createFromImageData(PictureType, EscherContainerRecord, EscherBSERecord, byte[])
     */
    static HSLFPictureData createFromSlideshowData(
            PictureType type,
            EscherContainerRecord recordContainer,
            EscherBSERecord bse,
            byte[] data,
            int signature
    ) {
        HSLFPictureData instance = newInstance(type, recordContainer, bse);
        instance.setSignature(signature);
        instance.formattedData = data;
        return instance;
    }

    /**
     * Creates a new instance of the given image type using data already formatted for storage inside the slideshow.
     * <p>
     * This function is most handy when adding new pictures to a slideshow, as the image data provided by users is not
     * yet formatted.
     *
     * @param type Image type.
     * @param recordContainer Record tracking all pictures. Should be attached to the slideshow that this picture is
     *                        linked to.
     * @param bse Record referencing this picture. Should be attached to the slideshow that this picture is linked to.
     * @param data Original image data. If these bytes were written to a disk, a common image viewer would be able to
     *             render the image.
     * @return New instance.
     *
     * @see #createFromSlideshowData(PictureType, EscherContainerRecord, EscherBSERecord, byte[], int)
     * @see #setData(byte[])
     */
    static HSLFPictureData createFromImageData(
            PictureType type,
            EscherContainerRecord recordContainer,
            EscherBSERecord bse,
            byte[] data
    ) {
        HSLFPictureData instance = newInstance(type, recordContainer, bse);
        instance.formattedData = instance.formatImageForSlideshow(data);
        return instance;
    }

    private static HSLFPictureData newInstance(
            PictureType type,
            EscherContainerRecord recordContainer,
            EscherBSERecord bse
    ) {
        switch (type) {
            case EMF:
                return new EMF(recordContainer, bse);
            case WMF:
                return new WMF(recordContainer, bse);
            case PICT:
                return new PICT(recordContainer, bse);
            case JPEG:
                return new JPEG(recordContainer, bse);
            case PNG:
                return new PNG(recordContainer, bse);
            case DIB:
                return new DIB(recordContainer, bse);
            default:
                throw new IllegalArgumentException("Unsupported picture type: " + type);
        }
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
        byte[] header = new byte[CHECKSUM_SIZE + PREAMBLE_SIZE];
        LittleEndian.putInt(header, 0, getSignature());
        LittleEndian.putInt(header, 4, getRawData().length);
        System.arraycopy(formattedData, 0, header, PREAMBLE_SIZE, CHECKSUM_SIZE);
        return header;
    }

    /**
     * Returns the 1-based index of this picture.
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

    /**
     * Formats the picture data for storage in the slideshow.
     * <p>
     * Images stored in {@link HSLFSlideShow}s are represented differently than when they are standalone files. The
     * exact formatting differs for each image type.
     *
     * @param data Original image data. If these bytes were written to a disk, a common image viewer would be able to
     *             render the image.
     * @return Formatted image representation.
     */
    protected abstract byte[] formatImageForSlideshow(byte[] data);

    /**
     * @return Size of this picture when stored in the image stream inside the {@link HSLFSlideShow}.
     */
    int getBseSize() {
        return formattedData.length + PREAMBLE_SIZE;
    }

    @Override
    public final void setData(byte[] data) throws IOException {
        /*
         * When working with slideshow pictures, we need to be aware of 2 container units. The first is a list of
         * HSLFPictureData that are the programmatic reference for working with the pictures. The second is the
         * Blip Store. For the purposes of this function, you can think of the Blip Store as containing a list of
         * pointers (with a small summary) to the picture in the slideshow.
         *
         * When updating a picture, we need to update the in-memory data structure (this instance), but we also need to
         * update the stored pointer. When modifying the pointer, we also need to modify all subsequent pointers, since
         * they might shift based on a change in the byte count of the underlying image.
         */
        int oldSize = getBseSize();
        formattedData = formatImageForSlideshow(data);
        int newSize = getBseSize();
        int changeInSize = newSize - oldSize;
        byte[] newUid = getUID();

        boolean foundBseForOldImage = false;

        // Get the BSE records & sort the list by offset, so we can proceed to shift offsets
        @SuppressWarnings("unchecked") // The BStore only contains BSE records
        List<EscherBSERecord> bseRecords = (List<EscherBSERecord>) (Object) bStore.getChildRecords();
        bseRecords.sort(Comparator.comparingInt(EscherBSERecord::getOffset));

        for (EscherBSERecord bse : bseRecords) {

            if (foundBseForOldImage) {

                // The BSE for this picture was modified in a previous iteration, and we are now adjusting
                //  subsequent offsets.
                bse.setOffset(bse.getOffset() + changeInSize);

            } else if (bse == this.bse) { // Reference equals is safe because these BSE belong to the same slideshow

                // This BSE matches the current image. Update the size and UID.
                foundBseForOldImage = true;

                bse.setUid(newUid);

                // Image byte count may have changed, so update the pointer.
                bse.setSize(newSize);
            }
        }
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
