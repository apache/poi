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

package org.apache.poi.hemf.record.emf;

import static java.nio.charset.StandardCharsets.UTF_16LE;
import static org.apache.poi.hemf.record.emf.HemfDraw.readRectL;
import static org.apache.poi.hemf.record.emf.HemfDraw.readDimensionFloat;
import static org.apache.poi.hemf.record.emf.HemfDraw.readPointL;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hwmf.record.HwmfColorRef;
import org.apache.poi.hwmf.record.HwmfText;
import org.apache.poi.hwmf.record.HwmfText.WmfSetTextAlign;
import org.apache.poi.util.Dimension2DDouble;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.RecordFormatException;

/**
 * Container class to gather all text-related commands
 * This is starting out as read only, and very little is actually
 * implemented at this point!
 */
@Internal
public class HemfText {

    private static final int MAX_RECORD_LENGTH = 1_000_000;

    public enum EmfGraphicsMode {
        GM_COMPATIBLE, GM_ADVANCED
    }

    public static class ExtTextOutA implements HemfRecord {

        protected final Rectangle2D boundsIgnored = new Rectangle2D.Double();

        protected EmfGraphicsMode graphicsMode;

        /**
         * The scale factor to apply along the X/Y axis to convert from page space units to .01mm units.
         * This SHOULD be used only if the graphics mode specified by iGraphicsMode is GM_COMPATIBLE.
         */
        protected final Dimension2D scale = new Dimension2DDouble();

        protected final EmrTextObject textObject;

        public ExtTextOutA() {
            this(false);
        }

        protected ExtTextOutA(boolean isUnicode) {
            textObject = new EmrTextObject(isUnicode);
        }

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.exttextouta;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            if (recordSize < 0 || Integer.MAX_VALUE <= recordSize) {
                throw new RecordFormatException("recordSize must be a positive integer (0-0x7FFFFFFF)");
            }

            // A WMF RectL object. It is not used and MUST be ignored on receipt.
            long size = readRectL(leis, boundsIgnored);

            // A 32-bit unsigned integer that specifies the graphics mode from the GraphicsMode enumeration
            graphicsMode = EmfGraphicsMode.values()[leis.readInt()-1];
            size += LittleEndianConsts.INT_SIZE;

            size += readDimensionFloat(leis, scale);

            // guarantee to read the rest of the EMRTextObjectRecord
            size += textObject.init(leis, recordSize, (int)size);

            return size;
        }

        /**
         *
         * To be implemented!  We need to get the current character set
         * from the current font for {@link ExtTextOutA},
         * which has to be tracked in the playback device.
         *
         * For {@link ExtTextOutW}, the charset is "UTF-16LE"
         *
         * @param charset the charset to be used to decode the character bytes
         * @return text from this text element
         * @throws IOException
         */
        public String getText(Charset charset) throws IOException {
            return textObject.getText(charset);
        }

        /**
         *
         * @return the x offset for the EmrTextObject
         */
        public EmrTextObject getTextObject() {
            return textObject;
        }

        public EmfGraphicsMode getGraphicsMode() {
            return graphicsMode;
        }

        public Dimension2D getScale() {
            return scale;
        }
    }

    public static class ExtTextOutW extends ExtTextOutA {

        public ExtTextOutW() {
            super(true);
        }

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.exttextoutw;
        }

        public String getText() throws IOException {
            return getText(UTF_16LE);
        }
    }

    /**
     * The EMR_SETTEXTALIGN record specifies text alignment.
     */
    public static class EmfSetTextAlign extends WmfSetTextAlign implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setTextAlign;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            /**
             * A 32-bit unsigned integer that specifies text alignment by using a mask of text alignment flags.
             * These are either WMF TextAlignmentMode Flags for text with a horizontal baseline,
             * or WMF VerticalTextAlignmentMode Flags for text with a vertical baseline.
             * Only one value can be chosen from those that affect horizontal and vertical alignment.
             */
            textAlignmentMode = (int)leis.readUInt();
            return LittleEndianConsts.INT_SIZE;
        }
    }

    /**
     * The EMR_SETTEXTCOLOR record defines the current text color.
     */
    public static class SetTextColor implements HemfRecord {
        /** A WMF ColorRef object that specifies the text color value. */
        private final HwmfColorRef colorRef = new HwmfColorRef();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setTextColor;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return colorRef.init(leis);
        }
    }

    public static class EmrTextObject extends HwmfText.WmfExtTextOut {
        protected final boolean isUnicode;
        protected final List<Integer> outputDx = new ArrayList<>();

        public EmrTextObject(boolean isUnicode) {
            super(new EmfExtTextOutOptions());
            this.isUnicode = isUnicode;
        }

        @Override
        public int init(LittleEndianInputStream leis, final long recordSize, final int offset) throws IOException {
            // A WMF PointL object that specifies the coordinates of the reference point used to position the string.
            // The reference point is defined by the last EMR_SETTEXTALIGN record.
            // If no such record has been set, the default alignment is TA_LEFT,TA_TOP.
            long size = readPointL(leis, reference);
            // A 32-bit unsigned integer that specifies the number of characters in the string.
            stringLength = (int)leis.readUInt();
            // A 32-bit unsigned integer that specifies the offset to the output string, in bytes,
            // from the start of the record in which this object is contained.
            // This value MUST be 8- or 16-bit aligned, according to the character format.
            int offString = (int)leis.readUInt();
            size += 2*LittleEndianConsts.INT_SIZE;

            size += options.init(leis);
            // An optional WMF RectL object that defines a clipping and/or opaquing rectangle in logical units.
            // This rectangle is applied to the text output performed by the containing record.
            if (options.isClipped() || options.isOpaque()) {
                size += readRectL(leis, bounds);
            }

            // A 32-bit unsigned integer that specifies the offset to an intercharacter spacing array, in bytes,
            // from the start of the record in which this object is contained. This value MUST be 32-bit aligned.
            int offDx = (int)leis.readUInt();
            size += LittleEndianConsts.INT_SIZE;

            int undefinedSpace1 = (int)(offString-offset-size-2*LittleEndianConsts.INT_SIZE);
            assert (undefinedSpace1 >= 0);
            leis.skipFully(undefinedSpace1);
            size += undefinedSpace1;

            rawTextBytes = IOUtils.safelyAllocate(stringLength*(isUnicode?2:1), MAX_RECORD_LENGTH);
            leis.readFully(rawTextBytes);
            size += rawTextBytes.length;

            outputDx.clear();
            if (offDx > 0) {
                int undefinedSpace2 = (int) (offDx - offset - size - 2 * LittleEndianConsts.INT_SIZE);
                assert (undefinedSpace2 >= 0);
                leis.skipFully(undefinedSpace2);
                size += undefinedSpace2;

                // An array of 32-bit unsigned integers that specify the output spacing between the origins of adjacent
                // character cells in logical units. The location of this field is specified by the value of offDx
                // in bytes from the start of this record. If spacing is defined, this field contains the same number
                // of values as characters in the output string.
                //
                // If the Options field of the EmrText object contains the ETO_PDY flag, then this buffer
                // contains twice as many values as there are characters in the output string, one
                // horizontal and one vertical offset for each, in that order.
                //
                // If ETO_RTLREADING is specified, characters are laid right to left instead of left to right.
                // No other options affect the interpretation of this field.
                while (size < recordSize) {
                    outputDx.add((int) leis.readUInt());
                    size += LittleEndianConsts.INT_SIZE;
                }
            }

            return (int)size;
        }
    }


    public static class ExtCreateFontIndirectW extends HwmfText.WmfCreateFontIndirect
    implements HemfRecord {
        int fontIdx;

        public ExtCreateFontIndirectW() {
            super(new HemfFont());
        }

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.extCreateFontIndirectW;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            // A 32-bit unsigned integer that specifies the index of the logical font object
            // in the EMF Object Table
            fontIdx = (int)leis.readUInt();
            int size = font.init(leis, (int)(recordSize-LittleEndianConsts.INT_SIZE));
            return size+LittleEndianConsts.INT_SIZE;
        }
    }

    public static class EmfExtTextOutOptions extends HwmfText.WmfExtTextOutOptions {
        @Override
        public int init(LittleEndianInputStream leis) {
            // A 32-bit unsigned integer that specifies how to use the rectangle specified in the Rectangle field.
            // This field can be a combination of more than one ExtTextOutOptions enumeration
            flag = (int)leis.readUInt();
            return LittleEndianConsts.INT_SIZE;
        }
    }

    public static class SetTextJustification extends UnimplementedHemfRecord {

    }

    /**
     * Needs to be implemented.  Couldn't find example.
     */
    public static class PolyTextOutA extends UnimplementedHemfRecord {

    }

    /**
     * Needs to be implemented.  Couldn't find example.
     */
    public static class PolyTextOutW extends UnimplementedHemfRecord {

    }

}
