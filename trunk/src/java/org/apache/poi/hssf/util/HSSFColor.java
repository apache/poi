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

package org.apache.poi.hssf.util;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.util.Removal;


/**
 * Intends to provide support for the very evil index to triplet issue and
 * will likely replace the color constants interface for HSSF 2.0.
 * This class contains static inner class members for representing colors.
 * Each color has an index (for the standard palette in Excel (tm) ),
 * native (RGB) triplet and string triplet.  The string triplet is as the
 * color would be represented by Gnumeric.  Having (string) this here is a bit of a
 * collision of function between HSSF and the HSSFSerializer but I think its
 * a reasonable one in this case.
 */
public class HSSFColor implements Color {

    private static Map<Integer,HSSFColor> indexHash;
    private static Map<HSSFColorPredefined,HSSFColor> enumList;

    private java.awt.Color color;
    private int index;
    private int index2;

    /**
     * Predefined HSSFColors with their given palette index (and an optional 2nd index)
     *
     * @since POI 3.16 beta 2
     */
    public enum HSSFColorPredefined {
        BLACK                (0x08,   -1, 0x000000),
        BROWN                (0x3C,   -1, 0x993300),
        OLIVE_GREEN          (0x3B,   -1, 0x333300),
        DARK_GREEN           (0x3A,   -1, 0x003300),
        DARK_TEAL            (0x38,   -1, 0x003366),
        DARK_BLUE            (0x12, 0x20, 0x000080),
        INDIGO               (0x3E,   -1, 0x333399),
        GREY_80_PERCENT      (0x3F,   -1, 0x333333),
        ORANGE               (0x35,   -1, 0xFF6600),
        DARK_YELLOW          (0x13,   -1, 0x808000),
        GREEN                (0x11,   -1, 0x008000),
        TEAL                 (0x15, 0x26, 0x008080),
        BLUE                 (0x0C, 0x27, 0x0000FF),
        BLUE_GREY            (0x36,   -1, 0x666699),
        GREY_50_PERCENT      (0x17,   -1, 0x808080),
        RED                  (0x0A,   -1, 0xFF0000),
        LIGHT_ORANGE         (0x34,   -1, 0xFF9900),
        LIME                 (0x32,   -1, 0x99CC00),
        SEA_GREEN            (0x39,   -1, 0x339966),
        AQUA                 (0x31,   -1, 0x33CCCC),
        LIGHT_BLUE           (0x30,   -1, 0x3366FF),
        VIOLET               (0x14, 0x24, 0x800080),
        GREY_40_PERCENT      (0x37,   -1, 0x969696),
        PINK                 (0x0E, 0x21, 0xFF00FF),
        GOLD                 (0x33,   -1, 0xFFCC00),
        YELLOW               (0x0D, 0x22, 0xFFFF00),
        BRIGHT_GREEN         (0x0B,   -1, 0x00FF00),
        TURQUOISE            (0x0F, 0x23, 0x00FFFF),
        DARK_RED             (0x10, 0x25, 0x800000),
        SKY_BLUE             (0x28,   -1, 0x00CCFF),
        PLUM                 (0x3D, 0x19, 0x993366),
        GREY_25_PERCENT      (0x16,   -1, 0xC0C0C0),
        ROSE                 (0x2D,   -1, 0xFF99CC),
        LIGHT_YELLOW         (0x2B,   -1, 0xFFFF99),
        LIGHT_GREEN          (0x2A,   -1, 0xCCFFCC),
        LIGHT_TURQUOISE      (0x29, 0x1B, 0xCCFFFF),
        PALE_BLUE            (0x2C,   -1, 0x99CCFF),
        LAVENDER             (0x2E,   -1, 0xCC99FF),
        WHITE                (0x09,   -1, 0xFFFFFF),
        CORNFLOWER_BLUE      (0x18,   -1, 0x9999FF),
        LEMON_CHIFFON        (0x1A,   -1, 0xFFFFCC),
        MAROON               (0x19,   -1, 0x7F0000),
        ORCHID               (0x1C,   -1, 0x660066),
        CORAL                (0x1D,   -1, 0xFF8080),
        ROYAL_BLUE           (0x1E,   -1, 0x0066CC),
        LIGHT_CORNFLOWER_BLUE(0x1F,   -1, 0xCCCCFF),
        TAN                  (0x2F,   -1, 0xFFCC99),

        /**
         * Special Default/Normal/Automatic color.<p>
         * <i>Note:</i> This class is NOT in the default Map returned by HSSFColor.
         * The index is a special case which is interpreted in the various setXXXColor calls.
         */
        AUTOMATIC            (0x40,   -1, 0x000000);

        private HSSFColor color;

        HSSFColorPredefined(int index, int index2, int rgb) {
            this.color = new HSSFColor(index, index2, new java.awt.Color(rgb));
        }

        /**
         * @see HSSFColor#getIndex()
         */
        public short getIndex() {
            return color.getIndex();
        }

        /**
         * @see HSSFColor#getIndex2()
         */
        public short getIndex2() {
            return color.getIndex2();
        }

        /**
         * @see HSSFColor#getTriplet()
         */
        public short [] getTriplet() {
            return color.getTriplet();
        }

        /**
         * @see HSSFColor#getHexString()
         */
        public String getHexString() {
            return color.getHexString();
        }

        /**
         * @return (a copy of) the HSSFColor assigned to the enum
         */
        public HSSFColor getColor() {
            return new HSSFColor(getIndex(), getIndex2(), color.color);
        }
    }


    /** Creates a new instance of HSSFColor */
    public HSSFColor() {
        // automatic index
        this(0x40, -1, java.awt.Color.BLACK);
    }

    public HSSFColor(int index, int index2, java.awt.Color color) {
        this.index = index;
        this.index2 = index2;
        this.color = color;
    }

    /**
     * This function returns all the colours in an unmodifiable Map.
     * The map is cached on first use.
     *
     * @return a Map containing all colours keyed by <tt>Integer</tt> excel-style palette indexes
     */
    public static final synchronized Map<Integer,HSSFColor> getIndexHash() {
        if(indexHash == null) {
           indexHash = Collections.unmodifiableMap( createColorsByIndexMap() );
        }

        return indexHash;
    }
    /**
     * This function returns all the Colours, stored in a Map that
     *  can be edited. No caching is performed. If you don't need to edit
     *  the table, then call {@link #getIndexHash()} which returns a
     *  statically cached imuatable map of colours.
     */
    public static final Map<Integer,HSSFColor> getMutableIndexHash() {
       return createColorsByIndexMap();
    }

    private static Map<Integer,HSSFColor> createColorsByIndexMap() {
        Map<HSSFColorPredefined,HSSFColor> eList = mapEnumToColorClass();
        Map<Integer,HSSFColor> result = new HashMap<Integer,HSSFColor>(eList.size() * 3 / 2);

        for (Map.Entry<HSSFColorPredefined,HSSFColor> colorRef : eList.entrySet()) {
            Integer index1 = (int)colorRef.getKey().getIndex();
            if (!result.containsKey(index1)) {
                result.put(index1, colorRef.getValue());
            }
            Integer index2 = (int)colorRef.getKey().getIndex2();
            if (index2 != -1 && !result.containsKey(index2)) {
                result.put(index2, colorRef.getValue());
            }
        }
        return result;
    }

    /**
     * this function returns all colors in a hastable.  Its not implemented as a
     * static member/staticly initialized because that would be dirty in a
     * server environment as it is intended.  This means you'll eat the time
     * it takes to create it once per request but you will not hold onto it
     * if you have none of those requests.
     *
     * @return a Map containing all colors keyed by String gnumeric-like triplets
     */
    public static Map<String,HSSFColor> getTripletHash()
    {
        return createColorsByHexStringMap();
    }

    private static Map<String,HSSFColor> createColorsByHexStringMap() {
        Map<HSSFColorPredefined,HSSFColor> eList = mapEnumToColorClass();
        Map<String,HSSFColor> result = new HashMap<String,HSSFColor>(eList.size());

        for (Map.Entry<HSSFColorPredefined,HSSFColor> colorRef : eList.entrySet()) {
            String hexString = colorRef.getKey().getHexString();
            if (!result.containsKey(hexString)) {
                result.put(hexString, colorRef.getValue());
            }
        }
        return result;
    }

    /**
     * Maps the Enums to the HSSFColor subclasses, in cases of user code evaluating the classname
     *
     * @deprecated in 3.16 - remove mapping when subclasses are removed and access
     *  HSSFColorPredfined.values() directly (but exclude AUTOMATIC)
     */
    @Deprecated
    @Removal(version="3.18")
    private static synchronized Map<HSSFColorPredefined,HSSFColor> mapEnumToColorClass() {
        if (enumList == null) {
            enumList = new EnumMap<HSSFColorPredefined,HSSFColor>(HSSFColorPredefined.class);
            // AUTOMATIC is not add to list
            enumList.put(HSSFColorPredefined.BLACK, new BLACK());
            enumList.put(HSSFColorPredefined.BROWN, new BROWN());
            enumList.put(HSSFColorPredefined.OLIVE_GREEN, new OLIVE_GREEN());
            enumList.put(HSSFColorPredefined.DARK_GREEN, new DARK_GREEN());
            enumList.put(HSSFColorPredefined.DARK_TEAL, new DARK_TEAL());
            enumList.put(HSSFColorPredefined.DARK_BLUE, new DARK_BLUE());
            enumList.put(HSSFColorPredefined.INDIGO, new INDIGO());
            enumList.put(HSSFColorPredefined.GREY_80_PERCENT, new GREY_80_PERCENT());
            enumList.put(HSSFColorPredefined.ORANGE, new ORANGE());
            enumList.put(HSSFColorPredefined.DARK_YELLOW, new DARK_YELLOW());
            enumList.put(HSSFColorPredefined.GREEN, new GREEN());
            enumList.put(HSSFColorPredefined.TEAL, new TEAL());
            enumList.put(HSSFColorPredefined.BLUE, new BLUE());
            enumList.put(HSSFColorPredefined.BLUE_GREY, new BLUE_GREY());
            enumList.put(HSSFColorPredefined.GREY_50_PERCENT, new GREY_50_PERCENT());
            enumList.put(HSSFColorPredefined.RED, new RED());
            enumList.put(HSSFColorPredefined.LIGHT_ORANGE, new LIGHT_ORANGE());
            enumList.put(HSSFColorPredefined.LIME, new LIME());
            enumList.put(HSSFColorPredefined.SEA_GREEN, new SEA_GREEN());
            enumList.put(HSSFColorPredefined.AQUA, new AQUA());
            enumList.put(HSSFColorPredefined.LIGHT_BLUE, new LIGHT_BLUE());
            enumList.put(HSSFColorPredefined.VIOLET, new VIOLET());
            enumList.put(HSSFColorPredefined.GREY_40_PERCENT, new GREY_40_PERCENT());
            enumList.put(HSSFColorPredefined.PINK, new PINK());
            enumList.put(HSSFColorPredefined.GOLD, new GOLD());
            enumList.put(HSSFColorPredefined.YELLOW, new YELLOW());
            enumList.put(HSSFColorPredefined.BRIGHT_GREEN, new BRIGHT_GREEN());
            enumList.put(HSSFColorPredefined.TURQUOISE, new TURQUOISE());
            enumList.put(HSSFColorPredefined.DARK_RED, new DARK_RED());
            enumList.put(HSSFColorPredefined.SKY_BLUE, new SKY_BLUE());
            enumList.put(HSSFColorPredefined.PLUM, new PLUM());
            enumList.put(HSSFColorPredefined.GREY_25_PERCENT, new GREY_25_PERCENT());
            enumList.put(HSSFColorPredefined.ROSE, new ROSE());
            enumList.put(HSSFColorPredefined.LIGHT_YELLOW, new LIGHT_YELLOW());
            enumList.put(HSSFColorPredefined.LIGHT_GREEN, new LIGHT_GREEN());
            enumList.put(HSSFColorPredefined.LIGHT_TURQUOISE, new LIGHT_TURQUOISE());
            enumList.put(HSSFColorPredefined.PALE_BLUE, new PALE_BLUE());
            enumList.put(HSSFColorPredefined.LAVENDER, new LAVENDER());
            enumList.put(HSSFColorPredefined.WHITE, new WHITE());
            enumList.put(HSSFColorPredefined.CORNFLOWER_BLUE, new CORNFLOWER_BLUE());
            enumList.put(HSSFColorPredefined.LEMON_CHIFFON, new LEMON_CHIFFON());
            enumList.put(HSSFColorPredefined.MAROON, new MAROON());
            enumList.put(HSSFColorPredefined.ORCHID, new ORCHID());
            enumList.put(HSSFColorPredefined.CORAL, new CORAL());
            enumList.put(HSSFColorPredefined.ROYAL_BLUE, new ROYAL_BLUE());
            enumList.put(HSSFColorPredefined.LIGHT_CORNFLOWER_BLUE, new LIGHT_CORNFLOWER_BLUE());
            enumList.put(HSSFColorPredefined.TAN, new TAN());
        }
        return enumList;
    }

    /**
     * returns color standard palette index
     * @return index to the standard palette
     */

    public short getIndex() {
        return (short)index;
    }

    /**
     * returns alternative color standard palette index
     * @return alternative index to the standard palette, if -1 this index is not defined
     */

    public short getIndex2() {
        return (short)index2;
    }

    /**
     * returns  RGB triplet (0, 0, 0)
     * @return  triplet representation like that in Excel
     */

    public short [] getTriplet() {
        return new short[] { (short)color.getRed(), (short)color.getGreen(), (short)color.getBlue() };
    }

    /**
     * returns colon-delimited hex string "0:0:0"
     * @return a hex string exactly like a gnumeric triplet
     */

    public String getHexString() {
        return (Integer.toHexString(color.getRed()*0x101) + ":" +
               Integer.toHexString(color.getGreen()*0x101) + ":" +
               Integer.toHexString(color.getBlue()*0x101)).toUpperCase(Locale.ROOT);
    }

    /**
     * Checked type cast <tt>color</tt> to an HSSFColor.
     *
     * @param color the color to type cast
     * @return the type casted color
     * @throws IllegalArgumentException if color is null or is not an instance of HSSFColor
     */
    public static HSSFColor toHSSFColor(Color color) {
        // FIXME: this method would be more useful if it could convert any Color to an HSSFColor
        // Currently the only benefit of this method is to throw an IllegalArgumentException
        // instead of a ClassCastException.
        if (color != null && !(color instanceof HSSFColor)) {
            throw new IllegalArgumentException("Only HSSFColor objects are supported");
        }
        return (HSSFColor)color;
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    private static class HSSFColorRef extends HSSFColor {
        HSSFColorRef(HSSFColorPredefined colorEnum) {
            super(colorEnum.getIndex(), colorEnum.getIndex2(), colorEnum.color.color);
        }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class BLACK extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.BLACK;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public BLACK() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class BROWN extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.BROWN;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public BROWN() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class OLIVE_GREEN extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.OLIVE_GREEN;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public OLIVE_GREEN() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class DARK_GREEN extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.DARK_GREEN;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public DARK_GREEN() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class DARK_TEAL extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.DARK_TEAL;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public DARK_TEAL() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class DARK_BLUE extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.DARK_BLUE;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public DARK_BLUE() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class INDIGO extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.INDIGO;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public INDIGO() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class GREY_80_PERCENT extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.GREY_80_PERCENT;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public GREY_80_PERCENT() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class DARK_RED extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.DARK_RED;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public DARK_RED() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class ORANGE extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.ORANGE;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public ORANGE() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class DARK_YELLOW extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.DARK_YELLOW;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public DARK_YELLOW() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class GREEN extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.GREEN;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public GREEN() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class TEAL extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.TEAL;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public TEAL() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class BLUE extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.BLUE;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public BLUE() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class BLUE_GREY extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.BLUE_GREY;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public BLUE_GREY() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class GREY_50_PERCENT extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.GREY_50_PERCENT;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public GREY_50_PERCENT() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class RED extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.RED;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public RED() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class LIGHT_ORANGE extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.LIGHT_ORANGE;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public LIGHT_ORANGE() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class LIME extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.LIME;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public LIME() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class SEA_GREEN extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.SEA_GREEN;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public SEA_GREEN() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class AQUA extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.AQUA;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public AQUA() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class LIGHT_BLUE extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.LIGHT_BLUE;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public LIGHT_BLUE() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class VIOLET extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.VIOLET;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public VIOLET() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class GREY_40_PERCENT extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.GREY_40_PERCENT;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public GREY_40_PERCENT() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class PINK extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.PINK;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public PINK() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class GOLD extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.GOLD;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public GOLD() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class YELLOW extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.YELLOW;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public YELLOW() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class BRIGHT_GREEN extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.BRIGHT_GREEN;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public BRIGHT_GREEN() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class TURQUOISE extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.TURQUOISE;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public TURQUOISE() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class SKY_BLUE extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.SKY_BLUE;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public SKY_BLUE() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class PLUM extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.PLUM;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public PLUM() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class GREY_25_PERCENT extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.GREY_25_PERCENT;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public GREY_25_PERCENT() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class ROSE extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.ROSE;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public ROSE() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class TAN extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.TAN;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public TAN() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class LIGHT_YELLOW extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.LIGHT_YELLOW;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public LIGHT_YELLOW() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class LIGHT_GREEN extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.LIGHT_GREEN;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public LIGHT_GREEN() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class LIGHT_TURQUOISE extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.LIGHT_TURQUOISE;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public LIGHT_TURQUOISE() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class PALE_BLUE extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.PALE_BLUE;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public PALE_BLUE() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class LAVENDER extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.LAVENDER;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public LAVENDER() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class WHITE extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.WHITE;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public WHITE() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class CORNFLOWER_BLUE extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.CORNFLOWER_BLUE;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public CORNFLOWER_BLUE() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class LEMON_CHIFFON extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.LEMON_CHIFFON;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public LEMON_CHIFFON() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class MAROON extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.MAROON;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public MAROON() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class ORCHID extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.ORCHID;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public ORCHID() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class CORAL extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.CORAL;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public CORAL() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class ROYAL_BLUE extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.ROYAL_BLUE;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public ROYAL_BLUE() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class LIGHT_CORNFLOWER_BLUE extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.LIGHT_CORNFLOWER_BLUE;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public LIGHT_CORNFLOWER_BLUE() { super(ref); }
    }

    /**
     * @deprecated use {@link HSSFColorPredefined} instead
     */
    @Deprecated
    @Removal(version="3.18")
    public static class AUTOMATIC extends HSSFColorRef {
        private static final HSSFColorPredefined ref = HSSFColorPredefined.AUTOMATIC;
        public static final short index = ref.getIndex();
        public static final int index2 = ref.getIndex2();
        public static final short[] triplet = ref.getTriplet();
        public static final String hexString = ref.getHexString();
        public AUTOMATIC() { super(ref); }

        public static HSSFColor getInstance() {
            return ref.color;
        }
    }
}
