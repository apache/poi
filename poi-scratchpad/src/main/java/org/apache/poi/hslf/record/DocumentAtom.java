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

package org.apache.poi.hslf.record;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.apache.poi.util.Removal;

/**
 * A Document Atom (type 1001). Holds misc information on the PowerPoint
 * document, lots of them size and scale related.
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public final class DocumentAtom extends RecordAtom {

    /**
     * Holds the different Slide Size values
     */
    public enum SlideSize {
        /** Slide size ratio is consistent with a computer screen. */
        ON_SCREEN,
        /** Slide size ratio is consistent with letter paper. */
        LETTER_SIZED_PAPER,
        /** Slide size ratio is consistent with A4 paper. */
        A4_SIZED_PAPER,
        /** Slide size ratio is consistent with 35mm photo slides. */
        ON_35MM,
        /** Slide size ratio is consistent with overhead projector slides. */
        OVERHEAD,
        /** Slide size ratio is consistent with a banner. */
        BANNER,
        /**
         * Slide size ratio that is not consistent with any of the other specified slide sizes in
         * this enumeration.
         */
        CUSTOM
    }


    private final byte[] _header = new byte[8];
    private static final long _type = RecordTypes.DocumentAtom.typeID;

    private long slideSizeX; // PointAtom, assume 1st 4 bytes = X
    private long slideSizeY; // PointAtom, assume 2nd 4 bytes = Y
    private long notesSizeX; // PointAtom, assume 1st 4 bytes = X
    private long notesSizeY; // PointAtom, assume 2nd 4 bytes = Y
    private long serverZoomFrom; // RatioAtom, assume 1st 4 bytes = from
    private long serverZoomTo;   // RatioAtom, assume 2nd 4 bytes = to

    private final long notesMasterPersist; // ref to NotesMaster, 0 if none
    private final long handoutMasterPersist; // ref to HandoutMaster, 0 if none

    private final int firstSlideNum;
    private int slideSizeType; // see DocumentAtom.SlideSize

    private byte saveWithFonts;
    private final byte omitTitlePlace;
    private final byte rightToLeft;
    private final byte showComments;

    private final byte[] reserved;


    public long getSlideSizeX() { return slideSizeX; }
    public long getSlideSizeY() { return slideSizeY; }
    public long getNotesSizeX() { return notesSizeX; }
    public long getNotesSizeY() { return notesSizeY; }
    public void setSlideSizeX(long x) { slideSizeX = x; }
    public void setSlideSizeY(long y) { slideSizeY = y; }
    public void setNotesSizeX(long x) { notesSizeX = x; }
    public void setNotesSizeY(long y) { notesSizeY = y; }

    public long getServerZoomFrom() { return serverZoomFrom; }
    public long getServerZoomTo()   { return serverZoomTo; }
    public void setServerZoomFrom(long zoom) { serverZoomFrom = zoom; }
    public void setServerZoomTo(long zoom)   { serverZoomTo   = zoom; }

    /** Returns a reference to the NotesMaster, or 0 if none */
    public long getNotesMasterPersist() { return notesMasterPersist; }
    /** Returns a reference to the HandoutMaster, or 0 if none */
    public long getHandoutMasterPersist() { return handoutMasterPersist; }

    public int getFirstSlideNum() { return firstSlideNum; }

    /**
     * The Size of the Document's slides, {@link DocumentAtom.SlideSize} for values.
     */
    public SlideSize getSlideSizeType() { return SlideSize.values()[slideSizeType]; }

    /**
     * The Size of the Document's slides, {@link DocumentAtom.SlideSize} for values.
     * @deprecated replaced by {@link #getSlideSizeType()}
     */
    @Deprecated
    @Removal(version = "6.0.0")
    public SlideSize getSlideSizeTypeEnum() {
        return SlideSize.values()[slideSizeType];
    }

    public void setSlideSize(SlideSize size) {
        slideSizeType = size.ordinal();
    }

    /** Was the document saved with True Type fonts embedded? */
    public boolean getSaveWithFonts() {
        return saveWithFonts != 0;
    }

    /** Set the font embedding state */
    public void setSaveWithFonts(boolean saveWithFonts) {
        this.saveWithFonts = (byte)(saveWithFonts ? 1 : 0);
    }

    /** Have the placeholders on the title slide been omitted? */
    public boolean getOmitTitlePlace() {
        return omitTitlePlace != 0;
    }

    /** Is this a Bi-Directional PPT Doc? */
    public boolean getRightToLeft() {
        return rightToLeft != 0;
    }

    /** Are comment shapes visible? */
    public boolean getShowComments() {
        return showComments != 0;
    }


    /* *************** record code follows ********************** */

    /**
     * For the Document Atom
     */
    /* package */ DocumentAtom(byte[] source, int start, int len) {
        final int maxLen = Math.max(len, 48);
        LittleEndianByteArrayInputStream leis =
            new LittleEndianByteArrayInputStream(source, start, maxLen);

        // Get the header
        leis.readFully(_header);

        // Get the sizes and zoom ratios
        slideSizeX = leis.readInt();
        slideSizeY = leis.readInt();
        notesSizeX = leis.readInt();
        notesSizeY = leis.readInt();
        serverZoomFrom = leis.readInt();
        serverZoomTo = leis.readInt();

        // Get the master persists
        notesMasterPersist = leis.readInt();
        handoutMasterPersist = leis.readInt();

        // Get the ID of the first slide
        firstSlideNum = leis.readShort();

        // Get the slide size type
        slideSizeType = leis.readShort();

        // Get the booleans as bytes
        saveWithFonts = leis.readByte();
        omitTitlePlace = leis.readByte();
        rightToLeft = leis.readByte();
        showComments = leis.readByte();

        // If there's any other bits of data, keep them about
        reserved = IOUtils.safelyAllocate(maxLen-48L, getMaxRecordLength());
        leis.readFully(reserved);
    }

    /**
     * We are of type 1001
     */
    @Override
    public long getRecordType() { return _type; }

    /**
     * Write the contents of the record back, so it can be written
     *  to disk
     */
    @Override
    public void writeOut(OutputStream out) throws IOException {
        // Header
        out.write(_header);

        // The sizes and zoom ratios
        writeLittleEndian((int)slideSizeX,out);
        writeLittleEndian((int)slideSizeY,out);
        writeLittleEndian((int)notesSizeX,out);
        writeLittleEndian((int)notesSizeY,out);
        writeLittleEndian((int)serverZoomFrom,out);
        writeLittleEndian((int)serverZoomTo,out);

        // The master persists
        writeLittleEndian((int)notesMasterPersist,out);
        writeLittleEndian((int)handoutMasterPersist,out);

        // The ID of the first slide
        writeLittleEndian((short)firstSlideNum,out);

        // The slide size type
        writeLittleEndian((short)slideSizeType,out);

        // The booleans as bytes
        out.write(saveWithFonts);
        out.write(omitTitlePlace);
        out.write(rightToLeft);
        out.write(showComments);

        // Reserved data
        out.write(reserved);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        final Map<String, Supplier<?>> m = new LinkedHashMap<>();
        m.put("slideSizeX", this::getSlideSizeX);
        m.put("slideSizeY", this::getSlideSizeY);
        m.put("notesSizeX", this::getNotesSizeX);
        m.put("notesSizeY", this::getNotesSizeY);
        m.put("serverZoomFrom", this::getServerZoomFrom);
        m.put("serverZoomTo", this::getServerZoomTo);
        m.put("notesMasterPersist", this::getNotesMasterPersist);
        m.put("handoutMasterPersist", this::getHandoutMasterPersist);
        m.put("firstSlideNum", this::getFirstSlideNum);
        m.put("slideSize", this::getSlideSizeTypeEnum);
        m.put("saveWithFonts", this::getSaveWithFonts);
        m.put("omitTitlePlace", this::getOmitTitlePlace);
        m.put("rightToLeft", this::getRightToLeft);
        m.put("showComments", this::getShowComments);
        return Collections.unmodifiableMap(m);
    }
}
