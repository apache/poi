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

package org.apache.poi.hslf.util;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.AbstractMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.util.Internal;
import org.apache.poi.util.LocaleID;
import org.apache.poi.util.SuppressForbidden;

@Internal
public final class LocaleDateFormat {

    /**
     * Enum to specify initial remapping of the FormatID based on thd LCID
     */
    public enum MapFormatId {
        NONE, PPT
    }

    private enum MapFormatPPT {
        EN_US(LocaleID.EN_US, "MM/dd/yyyy", 1, 8, "MMMM dd, yyyy", 5, 9, 10, 11, 12, 15, 16, "h:mm a", "h:mm:ss a"),
        EN_AU(LocaleID.EN_AU, 0, 1, "d MMMM, yyy", 2, 5, 9, 10, 11, 12, 15, 16, 13, 14),
        JA_JP(LocaleID.JA_JP, 4, 8, 7, 3, 0, 9, 5, 11, 12, "HH:mm", "HH:mm:ss", 15, 16),
        ZH_TW(LocaleID.ZH_TW, 0, 1, 3, 7, 12, 9, 10, 4, 11, "HH:mm", "HH:mm:ss", "H:mm a", "H:mm:ss a"),
        KO_KR(LocaleID.KO_KR, 0, 1, 6, 3, 4, 10, 7, 12, 11, "HH:mm", "HH:mm:ss", 13, 14 ),
        AR_SA(LocaleID.AR_SA, 0, 1, 2, 3, 4, 5, 8, 7, 8, 1, 10, 11, 5),
        HE_IL(LocaleID.HE_IL, 0, 1, 2, 6, 11, 5, 12, 7, 8, 9, 1, 11, 6),
        SV_SE(LocaleID.SV_SE, 0, 1, 3, 2, 7, 9, 10, 11, 12, 15, 16, 13, 14),
        ZH_CN(LocaleID.ZH_CN, 0, 1, 2, 2, 4, 9, 5, "yyyy\u5E74M\u6708d\u65E5h\u65F6m\u5206", "yyyy\u5E74M\u6708d\u65E5\u661F\u671fWh\u65F6m\u5206s\u79D2", "HH:mm", "HH:mm:ss", "a h\u65F6m\u5206", "a h\u65F6m\u5206s\u79D2"),
        ZH_SG(LocaleID.ZH_SG, 0, 1, 3, 2, 4, 9, 5, "yyyy\u5E74M\u6708d\u65E5h\u65F6m\u5206", "yyyy\u5E74M\u6708d\u65E5\u661F\u671fWh\u65F6m\u5206s\u79D2", "HH:mm", "HH:mm:ss", "a h\u65F6m\u5206", "a h\u65F6m\u5206s\u79D2"),
        ZH_MO(LocaleID.ZH_MO, 0, 1, 3, 2, 4, 9, 5, "yyyy\u5E74M\u6708d\u65E5h\u65F6m\u5206", "yyyy\u5E74M\u6708d\u65E5\u661F\u671fWh\u65F6m\u5206s\u79D2", "HH:mm", "HH:mm:ss", "a h\u65F6m\u5206", "a h\u65F6m\u5206s\u79D2"),
        ZH_HK(LocaleID.ZH_HK, 0, 1, 3, 2, 4, 9, 5, "yyyy\u5E74M\u6708d\u65E5h\u65F6m\u5206", "yyyy\u5E74M\u6708d\u65E5\u661F\u671fWh\u65F6m\u5206s\u79D2", "HH:mm", "HH:mm:ss", "a h\u65F6m\u5206", "a h\u65F6m\u5206s\u79D2"),
        TH_TH(LocaleID.TH_TH, 0, 1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 13, 14),
        VI_VN(LocaleID.VI_VN, 0, 1, 2, 3, 5, 6, 10, 11, 12, 13, 14, 15, 16),
        HI_IN(LocaleID.HI_IN, 1, 2, 3, 5, 7, 11, 13, 0, 1, 5, 10, 11, 14),
        SYR_SY(LocaleID.SYR_SY, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
        NO_MAP(LocaleID.INVALID_O, 0, 1, 3, 2, 5, 9, 10, 11, 12, 15, 16, 13, 14, 4, 6, 7, 8)
        ;

        private final LocaleID lcid;
        private final Object[] mapping;

        private static final Map<LocaleID,MapFormatPPT> LCID_LOOKUP =
            Stream.of(values()).collect(Collectors.toMap(MapFormatPPT::getLocaleID, Function.identity()));

        MapFormatPPT(LocaleID lcid, Object... mapping) {
            this.lcid = lcid;
            this.mapping = mapping;
        }

        public LocaleID getLocaleID() {
            return lcid;
        }

        public static Object mapFormatId(LocaleID lcid, int formatId) {
            Object[] mapping = LCID_LOOKUP.getOrDefault(lcid, NO_MAP).mapping;
            return (formatId >= 0 && formatId < mapping.length) ? mapping[formatId] : formatId;
        }
    }

    private enum MapFormatException {
        CHINESE(
            new LocaleID[]{LocaleID.ZH, LocaleID.ZH_HANS, LocaleID.ZH_HANT, LocaleID.ZH_CN, LocaleID.ZH_SG, LocaleID.ZH_MO, LocaleID.ZH_HK, LocaleID.ZH_YUE_HK},
            0,
            1,
            "yyyy\u5E74M\u6708d\u65E5\u661F\u671FW",
            "yyyy\u5E74M\u6708d\u65E5",
            "yyyy/M/d",
            "yy.M.d",
            "yyyy\u5E74M\u6708d\u65E5\u661F\u671FW",
            "yyyy\u5E74M\u6708d\u65E5",
            "yyyy\u5E74M\u6708d\u65E5\u661F\u671FW",
            "yyyy\u5E74M\u6708",
            "yyyy\u5E74M\u6708",
            "h\u65F6m\u5206s\u79D2",
            "h\u65F6m\u5206",
            "h\u65F6m\u5206",
            "h\u65F6m\u5206",
            "ah\u65F6m\u5206",
            "ah\u65F6m\u5206",
            // no lunar calendar support
            "EEEE\u5E74O\u6708A\u65E5",
            "EEEE\u5E74O\u6708A\u65E5\u661F\u671FW",
            "EEEE\u5E74O\u6708"
        ),
        // no hindu calendar support
        HINDI(
            new LocaleID[]{LocaleID.HI, LocaleID.HI_IN},
            "dd/M/g",
            "dddd, d MMMM yyyy",
            "dd MMMM yyyy",
            "dd/M/yy",
            "yy-M-dd",
            "d-MMMM-yyyy",
            "dd.M.g",
            "dd MMMM. yy",
            "dd MMMM yy",
            "MMMM YY",
            "MMMM-g",
            "dd/M/g HH:mm",
            "dd/M/g HH:mm:ss",
            "HH:mm a",
            "HH:mm:ss a",
            "HH:mm",
            "HH:mm:ss"
        ),
        // https://www.secondsite8.com/customdateformats.htm
        // aa or gg or o, r, i, c -> lunar calendar not supported
        // (aaa) -> lower case week names ... not supported
        JAPANESE(
            new LocaleID[]{LocaleID.JA, LocaleID.JA_JP, LocaleID.JA_PLOC_JP},
            0,
            1,
            "EEEy\u5E74M\u6708d\u65E5",
            "yyyy\u5E74M\u6708d\u65E5",
            "yyyy/M/d",
            "yyyy\u5E74M\u6708d\u65E5",
            "yy\u5E74M\u6708d\u65E5",
            "yyyy\u5E74M\u6708d\u65E5",
            "yyyy\u5E74M\u6708d\u65E5(EEE)",
            "yyyy\u5E74M\u6708",
            "yyyy\u5E74M\u6708",
            "yy/M/d H\u6642m\u5206",
            "yy/M/d H\u6642m\u5206s\u79D2",
            "a h\u6642m\u5206",
            "a h\u6642m\u5206s\u79D2",
            "H\u6642m\u5206",
            "H\u6642m\u5206s\u79D2",
            "yyyy\u5E74M\u6708d\u65E5 EEE\u66DC\u65E5"
        ),
        KOREAN(
            new LocaleID[]{LocaleID.KO,LocaleID.KO_KR},
            0,
            1,
            "yyyy\uB144 M\uC6D4 d\uC77C EEE\uC694\uC77C",
            "yyyy\uB144 M\uC6D4 d\uC77C",
            "yyyy/M/d",
            "yyMMdd",
            "yyyy\uB144 M\uC6D4 d\uC77C",
            "yyyy\uB144 M\uC6D4",
            "yyyy\uB144 M\uC6D4 d\uC77C",
            "yyyy",
            "yyyy\uB144 M\uC6D4",
            "yyyy\uB144 M\uC6D4 d\uC77C a h\uC2DC m\uBD84",
            "yy\uB144 M\uC6D4 d\uC77C H\uC2DC m\uBD84 s\uCD08",
            "a h\uC2DC m\uBD84",
            "a h\uC2DC m\uBD84 s\uCD08",
            "H\uC2DC m\uBD84",
            "H\uC2DC m\uBD84 S\uCD08"
        ),
        HUNGARIAN(
            new LocaleID[]{LocaleID.HU, LocaleID.HU_HU},
            0, 1, 2, 3, 4, 5, 6, "yy. MMM. dd.", "\u2019yy MMM.", "MMMM \u2019yy", 10, 11, 12, "a h:mm", "a h:mm:ss", 15, 16
        ),
        BOKMAL(
            new LocaleID[]{LocaleID.NB_NO},
            0, 1, 2, 3, 4, "d. MMM. yyyy", "d/m yyyy", "MMM. yy", "yyyy.mm.dd", 9, "d. MMM.", 11, 12, 13, 14, 15, 16
        ),
        CZECH(new LocaleID[]{LocaleID.CS, LocaleID.CS_CZ}, 0, 1, 2, 3, 4, 5, 6, 7, 8, "MMMM \u2019yy", 10, 11, 12, 13, 14, 15, 16),
        DANISH(new LocaleID[]{LocaleID.DA, LocaleID.DA_DK}, 0, "d. MMMM yyyy", "yy-MM-dd", "yyyy.MM.dd", 4, "MMMM yyyy", "d.M.yy", "d/M yyyy", "dd.MM.yyyy", "d.M.yyyy", "dd/MM yyyy", 11, 12, 13, 14, 15, 16 ),
        DUTCH(new LocaleID[]{LocaleID.NL,LocaleID.NL_BE,LocaleID.NL_NL}, 0, 1, 2, 3, 4, 5, 6, 7, 8, "MMMM \u2019yy", 10, 11, 12, 13, 14, 15, 16),
        FINISH(new LocaleID[]{LocaleID.FI, LocaleID.FI_FI}, 0, 1, 2, 3, 4, 5, 6, 7, 8, "MMMM \u2019yy", 10, 11, 12, 13, 14, 15, 16),
        FRENCH_CANADIAN(new LocaleID[]{LocaleID.FR_CA}, 0, 1, 2, "yy MM dd", 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16),
        GERMAN(new LocaleID[]{LocaleID.DE,LocaleID.DE_AT,LocaleID.DE_CH,LocaleID.DE_DE,LocaleID.DE_LI,LocaleID.DE_LU}, 0, 1, 2, 3, 4, "yy-MM-dd", 6, "dd. MMM. yyyy", 8, 9, 10, 11, 12, 13, 14, 15, 16),
        ITALIAN(new LocaleID[]{LocaleID.IT,LocaleID.IT_IT,LocaleID.IT_CH}, 0, 1, 2, 3, 4, "d-MMM.-yy", 6, "d. MMM. yy", "MMM. \u2019yy", "MMMM \u2019yy", 10, 11, 12, 13, 14, 15, 16),
        NO_MAP(new LocaleID[]{LocaleID.INVALID_O}, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
        // TODO: add others from [MS-OSHARED] chapter 2.4.4.4
        ;


        private final LocaleID[] lcid;
        private final Object[] mapping;

        private static final Map<LocaleID, MapFormatException> LCID_LOOKUP =
            Stream.of(values()).flatMap(m -> Stream.of(m.lcid).map(l -> new AbstractMap.SimpleEntry<>(l, m)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        MapFormatException(LocaleID[] lcid, Object... mapping) {
            this.lcid = lcid;
            this.mapping = mapping;
        }

        public static Object mapFormatId(LocaleID lcid, int formatId) {
            Object[] mapping = LCID_LOOKUP.getOrDefault(lcid, NO_MAP).mapping;
            return (formatId >= 0 && formatId < mapping.length) ? mapping[formatId] : formatId;
        }
    }

    /**
     * This enum lists and describes the format indices that can be used as inputs to the algorithm. The
     * descriptions given are generalized; the actual format produced can vary from the description,
     * depending on the input locale.
     */
    @SuppressForbidden("DateTimeFormatter::ofLocalizedDate and others will be localized in mapFormatId")
    private enum MapFormatBase {
        /** 0 - Base short date **/
        SHORT_DATE(null,  FormatStyle.MEDIUM, DateTimeFormatter::ofLocalizedDate),
        /** 1 - Base long date. **/
        LONG_DATE(null, FormatStyle.FULL, DateTimeFormatter::ofLocalizedDate),
        /**
         * 2 - Do the following to base long date:
         * - Remove occurrences of "dddd".
         * - Remove the comma symbol (0x002C) and space following "dddd" if present.
         * - Change occurrences of "dd" to "d".
         **/
        LONG_DATE_WITHOUT_WEEKDAY("d. MMMM yyyy", null, null),
        /**
         * 3 - Do the following to base short date:
         * - Change occurrences of "yyyy" to "yy".
         * - Change occurrences of "yy" to "yyyy".
         */
        ALTERNATE_SHORT_DATE("dd/MM/yy", null, null),
        /**
         * 4 - yyyy-MM-dd
         */
        ISO_STANDARD_DATE("yyyy-MM-dd", null, null),
        /**
         * 5 - If the symbol "y" occurs before the symbol "M" occurs in the base short date, the format is
         * "yy-MMM-d". Otherwise, the format is "d-MMM-yy".
         */
        SHORT_DATE_WITH_ABBREVIATED_MONTH("d-MMM-yy", null, null),
        /**
         * 6 - If the forward slash symbol (0x002F) occurs in the base short date, the slash symbol is the
         * period symbol (0x002E). Otherwise, the slash symbol is the forward slash (0x002F).
         * A group is an uninterrupted sequence of qualified symbols where a qualified symbol is "d",
         * "M", or "Y".
         * Identify the first three groups that occur in the base short date. The format is formed by
         * appending the three groups together with single slash symbols separating the groups.
         */
        SHORT_DATE_WITH_SLASHES("d/M/y", null, null),
        /**
         * 7 - Do the following to base long date:
         * - Remove occurrences of "dddd".
         * - Remove the comma symbol (0x002C) and space following "dddd" if present.
         * - Change occurrences of "dd" to "d".
         * - For all right-to-left locales and Lao, change a sequence of any length of "M" to "MMM".
         * - For all other locales, change a sequence of any length of "M" to "MMM".
         * - Change occurrences of "yyyy" to "yy".
         */
        ALTERNATE_SHORT_DATE_WITH_ABBREVIATED_MONTH("d. MMM yy", null, null),
        /**
         * 8 - For American English and Arabic, the format is "d MMMM yyyy".
         * For Hebrew, the format is "d MMMM, yyyy".
         * For all other locales, the format is the same as format 6 with the following additional step:
         * Change occurrences of "yyyy" to "yy".
         */
        ENGLISH_DATE("d MMMM yyyy", null, null),
        /**
         * 9 - Do the following to base long date:
         * - Remove all symbols that occur before the first occurrence of either the "y" symbol or the "M" symbol.
         * - Remove all "d" symbols.
         * - For all locales except Lithuanian, remove all period symbols (0x002E).
         * - Remove all comma symbols (0x002C).
         * - Change occurrences of "yyyy" to "yy".
         */
        MONTH_AND_YEAR("MMMM yy", null, null),
        /**
         * 10 - MMM-yy
         */
        ABBREVIATED_MONTH_AND_YEAR(isOldFmt () ? "MMM-yy" : "LLL-yy", null, null),
        /**
         * 11 - Base short date followed by a space, followed by base time with seconds removed.
         * Seconds are removed by removing all "s" symbols and any symbol that directly precedes an
         * "s" symbol that is not an "h" or "m" symbol.
         */
        DATE_AND_HOUR12_TIME(null, FormatStyle.MEDIUM, (fs) -> new DateTimeFormatterBuilder().appendLocalized(FormatStyle.SHORT, null).appendLiteral("  ").appendLocalized(null, FormatStyle.SHORT).toFormatter()),
        /**
         * 12 - Base short date followed by a space, followed by base time.
         */
        DATE_AND_HOUR12_TIME_WITH_SECONDS(null, FormatStyle.MEDIUM, (fs) -> new DateTimeFormatterBuilder().appendLocalized(FormatStyle.SHORT, null).appendLiteral("  ").appendLocalized(null, fs).toFormatter()),
        /**
         * 13 - For Hungarian, the format is "am/pm h:mm".
         * For all other locales, the format is "h:mm am/pm".
         * In both cases, replace occurrences of the colon symbol (0x003A) with the time separator.
         */
        HOUR12_TIME("K:mm", null, null),
        /**
         * 14 - For Hungarian, the format is "am/pm h:mm:ss".
         * For all other locales, the format is "h:mm:ss am/pm".
         * In both cases, replace occurrences of the colon symbol (0x003A) with the time separator.
         */
        HOUR12_TIME_WITH_SECONDS("K:mm:ss", null, null),
        /**
         * 15 - "HH" followed by the time separator, followed by "mm".
         */
        HOUR24_TIME("HH:mm", null, null),
        /**
         * 16 - "HH" followed by the time separator, followed by "mm", followed by the time separator
         * followed by "ss".
         */
        HOUR24_TIME_WITH_SECONDS("HH:mm:ss", null, null),
        // CHINESE1(null, null, null),
        // CHINESE2(null, null, null),
        // CHINESE3(null, null, null)
        ;


        private final String datefmt;
        private final FormatStyle formatStyle;
        private final Function<FormatStyle,DateTimeFormatter> formatFct;

        MapFormatBase(String datefmt, FormatStyle formatStyle, Function<FormatStyle,DateTimeFormatter> formatFct) {
            this.formatStyle = formatStyle;
            this.datefmt = datefmt;
            this.formatFct = formatFct;
        }

        public static DateTimeFormatter mapFormatId(Locale loc, int formatId) {
            MapFormatBase[] mfb = MapFormatBase.values();
            if (formatId < 0 || formatId >= mfb.length) {
                return DateTimeFormatter.BASIC_ISO_DATE;
            }
            MapFormatBase mf = mfb[formatId];
            return (mf.datefmt == null)
                ? mf.formatFct.apply(mf.formatStyle).withLocale(loc)
                : DateTimeFormatter.ofPattern(mf.datefmt, loc);
        }
    }

    private LocaleDateFormat() {}

    public static DateTimeFormatter map(LocaleID lcid, int formatID, MapFormatId mapFormatId) {
        final Locale loc = Locale.forLanguageTag(lcid.getLanguageTag());
        int mappedFormatId = formatID;
        if (mapFormatId == MapFormatId.PPT) {
            Object mappedFormat = MapFormatPPT.mapFormatId(lcid, formatID);
            if (mappedFormat instanceof String) {
                return DateTimeFormatter.ofPattern((String)mappedFormat,loc);
            } else {
                mappedFormatId = (Integer)mappedFormat;
            }
        }
        Object mappedFormat = MapFormatException.mapFormatId(lcid, mappedFormatId);
        if (mappedFormat instanceof String) {
            return DateTimeFormatter.ofPattern((String)mappedFormat,loc);
        } else {
            return MapFormatBase.mapFormatId(loc, (Integer)mappedFormat);
        }
    }

    private static boolean isOldFmt() {
        return System.getProperty("java.version").startsWith("1.8");
    }
}
