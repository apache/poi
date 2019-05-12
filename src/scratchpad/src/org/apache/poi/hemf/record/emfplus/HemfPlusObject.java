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

package org.apache.poi.hemf.record.emfplus;

import java.io.IOException;
import java.util.function.Supplier;

import org.apache.poi.hemf.record.emfplus.HemfPlusBrush.EmfPlusBrush;
import org.apache.poi.hemf.record.emfplus.HemfPlusFont.EmfPlusFont;
import org.apache.poi.hemf.record.emfplus.HemfPlusHeader.EmfPlusGraphicsVersion;
import org.apache.poi.hemf.record.emfplus.HemfPlusImage.EmfPlusImage;
import org.apache.poi.hemf.record.emfplus.HemfPlusImage.EmfPlusImageAttributes;
import org.apache.poi.hemf.record.emfplus.HemfPlusMisc.EmfPlusObjectId;
import org.apache.poi.hemf.record.emfplus.HemfPlusPath.EmfPlusPath;
import org.apache.poi.hemf.record.emfplus.HemfPlusPen.EmfPlusPen;
import org.apache.poi.hemf.record.emfplus.HemfPlusRegion.EmfPlusRegion;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class HemfPlusObject {
    private static final int MAX_OBJECT_SIZE = 50_000_000;

    /**
     * The ObjectType enumeration defines types of graphics objects that can be created and used in graphics operations.
     */
    public enum EmfPlusObjectType {
        /**
         * The object is not a valid object.
         */
        INVALID(0x00000000, EmfPlusUnknownData::new),
        /**
         * Brush objects fill graphics regions.
         */
        BRUSH(0x00000001, EmfPlusBrush::new),
        /**
         * Pen objects draw graphics lines.
         */
        PEN(0x00000002, EmfPlusPen::new),
        /**
         * Path objects specify sequences of lines, curves, and shapes.
         */
        PATH(0x00000003, EmfPlusPath::new),
        /**
         * Region objects specify areas of the output surface.
         */
        REGION(0x00000004, EmfPlusRegion::new),
        /**
         * Image objects encapsulate bitmaps and metafiles.
         */
        IMAGE(0x00000005, EmfPlusImage::new),
        /**
         * Font objects specify font properties, including typeface style, em size, and font family.
         */
        FONT(0x00000006, EmfPlusFont::new),
        /**
         * String format objects specify text layout, including alignment, orientation, tab stops, clipping,
         * and digit substitution for languages that do not use Western European digits.
         */
        STRING_FORMAT(0x00000007, EmfPlusUnknownData::new),
        /**
         * Image attribute objects specify operations on pixels during image rendering, including color
         * adjustment, grayscale adjustment, gamma correction, and color mapping.
         */
        IMAGE_ATTRIBUTES(0x00000008, EmfPlusImageAttributes::new),
        /**
         * Custom line cap objects specify shapes to draw at the ends of a graphics line, including
         * squares, circles, and diamonds.
         */
        CUSTOM_LINE_CAP(0x00000009, EmfPlusUnknownData::new);

        public final int id;
        public final Supplier<? extends EmfPlusObjectData> constructor;

        EmfPlusObjectType(int id, Supplier<? extends EmfPlusObjectData> constructor) {
            this.id = id;
            this.constructor = constructor;
        }

        public static EmfPlusObjectType valueOf(int id) {
            for (EmfPlusObjectType wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }
    }


    /**
     * The EmfPlusObject record specifies an object for use in graphics operations. The object definition
     * can span multiple records), which is indicated by the value of the Flags field.
     */
    public static class EmfPlusObject implements HemfPlusRecord, EmfPlusObjectId {


        /**
         * Indicates that the object definition continues on in the next EmfPlusObject
         * record. This flag is never set in the final record that defines the object.
         */
        private static final BitField CONTINUABLE = BitFieldFactory.getInstance(0x8000);

        /**
         * Specifies the metafileType of object to be created by this record, from the
         * ObjectType enumeration
         */
        private static final BitField OBJECT_TYPE = BitFieldFactory.getInstance(0x7F00);

        private int flags;
        // for debugging
        private int objectId;
        private EmfPlusObjectData objectData;
        private int totalObjectSize;

        @Override
        public HemfPlusRecordType getEmfPlusRecordType() {
            return HemfPlusRecordType.object;
        }

        @Override
        public int getFlags() {
            return flags;
        }

        public EmfPlusObjectType getObjectType() {
            return EmfPlusObjectType.valueOf(OBJECT_TYPE.getValue(flags));
        }

        public <T extends EmfPlusObjectData> T getObjectData() {
            return (T)objectData;
        }

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, long recordId, int flags) throws IOException {
            this.flags = flags;

            objectId = getObjectId();
            EmfPlusObjectType objectType = getObjectType();
            assert (objectType != null);

            int size = 0;

            totalObjectSize = 0;
            int dataSize2 = (int) dataSize;

            if (CONTINUABLE.isSet(flags)) {
                // If the record is continuable, when the continue bit is set, this field will be present.
                // Continuing objects have multiple EMF+ records starting with EmfPlusContinuedObjectRecord.
                // Each EmfPlusContinuedObjectRecord will contain a TotalObjectSize. Once TotalObjectSize number
                // of bytes has been read, the next EMF+ record will not be treated as part of the continuing object.
                totalObjectSize = leis.readInt();
                size += LittleEndianConsts.INT_SIZE;
                dataSize2 -= LittleEndianConsts.INT_SIZE;
            }

            objectData = objectType.constructor.get();
            size += objectData.init(leis, dataSize2, objectType, flags);

            return size;
        }
    }

    public interface EmfPlusObjectData {
        long init(LittleEndianInputStream leis, long dataSize, EmfPlusObjectType objectType, int flags) throws IOException;
    }

    public static class EmfPlusUnknownData implements EmfPlusObjectData {
        private EmfPlusObjectType objectType;
        private final EmfPlusGraphicsVersion graphicsVersion = new EmfPlusGraphicsVersion();
        private byte[] objectDataBytes;

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, EmfPlusObjectType objectType, int flags) throws IOException {
            this.objectType = objectType;

            long size = graphicsVersion.init(leis);

            objectDataBytes = IOUtils.toByteArray(leis, dataSize - size, MAX_OBJECT_SIZE);

            return dataSize;
        }
    }
}
