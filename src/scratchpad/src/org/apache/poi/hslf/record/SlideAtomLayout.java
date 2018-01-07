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

import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * Holds the geometry of the Slide, and the ID of the placeholders on the slide.
 * Embedded inside a SlideAtom is a SlideAtomLayout, without the usual record header.
 * Since it's a fixed size and tied to the SlideAtom, we'll hold it here.<p>
 * 
 * This might eventually merged with the XSLF counterpart
 */
@Internal
public class SlideAtomLayout {
    // The different kinds of geometry
    public enum SlideLayoutType {
        /** One title and one subtitle placeholder shapes. */
        TITLE_SLIDE(0x0000),
        /** Presentation slide or main master slide layout with one title and one body placeholder shape. */
        TITLE_BODY(0x0001),
        /** Title master slide layout with one title and one subtitle placeholder shape. */
        MASTER_TITLE(0x0002),
        /** ??? (not documented in spec) */
        MASTER_SLIDE(0x0003),
        /** ??? (not documented in spec) */
        MASTER_NOTES(0x0004),
        /** ??? (not documented in spec) */
        NOTES_TITLE_BODY(0x0005),
        /** Only header, footer and date placeholders */
        HANDOUT(0x0006),
        /** Presentation slide layout with one title placeholder shape. */
        TITLE_ONLY(0x0007),
        /** Presentation slide layout with one title and two body placeholder shapes stacked horizontally. */
        TWO_COLUMNS(0x0008),
        /** Presentation slide layout with one title and two body placeholder shapes stacked vertically. */
        TWO_ROWS(0x0009),
        /** Presentation slide layout with one title and three body placeholder shapes split into two columns. The right column has two rows. */
        COLUMN_TWO_ROWS(0x000A),
        /** Presentation slide layout with one title and three body placeholder shapes split into two columns. The left column has two rows. */
        TWO_ROWS_COLUMN(0x000B),
        /** ??? (not documented in spec) */
        TITLE_2_ROW_BOTTOM_2_COLUMN_BODY(0x000C),
        /** Presentation slide layout with one title and three body placeholder shapes split into two rows. The top row has two columns. */
        TWO_COLUMNS_ROW(0x000D),
        /** Presentation slide layout with one title and four body placeholder shapes. */
        FOUR_OBJECTS(0x000E),
        /** Presentation slide layout with one body placeholder shape. */
        BIG_OBJECT(0x000F),
        /** Presentation slide layout with no placeholder shape. */
        BLANK_SLIDE(0x0010),
        /** Presentation slide layout with a vertical title placeholder shape on the right and a body placeholder shape on the left. */
        VERTICAL_TITLE_BODY(0x0011),
        /** Presentation slide layout with a vertical title placeholder shape on the right and two body placeholder shapes in two columns on the left. */
        VERTICAL_TWO_ROWS(0x0012);

        private int nativeId;
        SlideLayoutType(int nativeId) {
            this.nativeId = nativeId;
        }
        
        public int getNativeId() {
            return nativeId;
        }

        public static SlideLayoutType forNativeID(int nativeId) {
            for (SlideLayoutType ans : values()) {
                if (ans.nativeId == nativeId) {
                    return ans;
                }
            }
            return null;
        }
    }

    /** What geometry type we are */
    private SlideLayoutType geometry;
    /** What placeholder IDs we have */
    private byte[] placeholderIDs;

    /** Retrieve the geometry type */
    public SlideLayoutType getGeometryType() { return geometry; }
    /** Set the geometry type */
    public void setGeometryType(SlideLayoutType geom) { geometry = geom; }

    /**
     * Create a new Embedded SSlideLayoutAtom, from 12 bytes of data
     */
    public SlideAtomLayout(byte[] data) {
        if(data.length != 12) {
            throw new HSLFException("SSlideLayoutAtom created with byte array not 12 bytes long - was " + data.length + " bytes in size");
        }

        // Grab out our data
        geometry = SlideLayoutType.forNativeID(LittleEndian.getInt(data,0));
        placeholderIDs = new byte[8];
        System.arraycopy(data,4,placeholderIDs,0,8);
    }

    /**
     * Write the contents of the record back, so it can be written
     *  to disk. Skips the record header
     */
    public void writeOut(OutputStream out) throws IOException {
        // Write the geometry
        byte[] buf = new byte[4];
        LittleEndian.putInt(buf, 0, geometry.getNativeId());
        out.write(buf);
        // Write the placeholder IDs
        out.write(placeholderIDs);
    }

}
