/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package scomp.contentType.simple.detailed;

import scomp.common.BaseCase;
import xbean.scomp.contentType.builtIn.string.*;
import xbean.scomp.contentType.builtIn.number.*;
import xbean.scomp.contentType.builtIn.date.*;
import org.apache.xmlbeans.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 *
 *
 *
 */
public class BuiltInType extends BaseCase {
    /**
     * testing types String, normalizedString and token
     *
     * @throws Throwable
     */
    public void testStringBasedTypes1() throws Throwable {
        String[] exp = new String[]{
            "\tLead tab,A string on\n 2 lines with 2  spaces",
            "  2 Lead spaces,A string on\n 2 lines with 2  spaces",
            " Lead tab,A string on  2 lines with 2  spaces",
            "  2 Lead spaces,A string on  2 lines with 2  spaces",
            "Lead tab,A string on 2 lines with 2 spaces",
            "2 Lead spaces,A string on 2 lines with 2 spaces"
        };
        StringEltDocument doc = StringEltDocument.Factory.parse(buildString("StringElt", false));
        assertTrue(doc.validate(validateOptions));
        assertEquals("<StringElt" +
                " xmlns=\"http://xbean/scomp/contentType/builtIn/String\">" +
                exp[0] +
                "</StringElt>", doc.xmlText());

        doc = StringEltDocument.Factory.parse(buildString("StringElt", true));
        assertTrue(doc.validate(validateOptions));
        assertEquals(
                exp[1]
                , doc.getStringElt());

        NormalizedStringEltDocument doc1 = NormalizedStringEltDocument.Factory
                .parse(buildString("NormalizedStringElt", false));
        assertTrue(doc.validate(validateOptions));
        assertEquals(exp[2], doc1.getNormalizedStringElt());
        doc1 =
                NormalizedStringEltDocument.Factory
                .parse(buildString("NormalizedStringElt", true));
        assertTrue(doc1.validate(validateOptions));
        assertEquals(
                exp[3] , doc1.getNormalizedStringElt());

        TokenEltDocument doc2 = TokenEltDocument.Factory.parse(buildString("TokenElt", false));
        assertTrue(doc2.validate(validateOptions));
        assertEquals(exp[4] , doc2.getTokenElt());
        doc2 = TokenEltDocument.Factory.parse(buildString("TokenElt", true));
        assertTrue(doc2.validate(validateOptions));
        assertEquals(exp[5], doc2.getTokenElt());
    }

    /**
     * testing types Name, NCName, Language
     *
     * @throws Throwable
     */
    public void testStringBasedTypes2() throws Throwable {
        NameEltDocument nameDoc = NameEltDocument.Factory.newInstance();
        nameDoc.setNameElt("_eltName");
        assertTrue(nameDoc.validate(validateOptions));
        nameDoc.setNameElt(":eltName");
        assertTrue(nameDoc.validate(validateOptions));
        XmlName str = XmlName.Factory.newInstance();
        str.setStringValue("-eltName");
        nameDoc.xsetNameElt(str);
        assertTrue(!nameDoc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID
        };
        assertTrue(compareErrorCodes(errExpected));


        NCNameEltDocument ncNameDoc = NCNameEltDocument.Factory.newInstance();
        ncNameDoc.setNCNameElt(":eltName");
        clearErrors();
        assertTrue(!ncNameDoc.validate(validateOptions));
        showErrors();
        errExpected = new String[]{
            XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID
        };
        assertTrue(compareErrorCodes(errExpected));

        XmlNCName ncn = XmlNCName.Factory.newInstance();
        ncn.setStringValue("_elt.Name");
        ncNameDoc.xsetNCNameElt(ncn);
        assertTrue(ncNameDoc.validate(validateOptions));

        LanguageEltDocument langDoc = LanguageEltDocument.Factory.newInstance();
        langDoc.setLanguageElt("de");
        assertTrue(langDoc.validate(validateOptions));

        langDoc.setLanguageElt("en-US");
        assertTrue(langDoc.validate(validateOptions));
        clearErrors();
        langDoc.setLanguageElt("bulgarian");
        assertTrue(!langDoc.validate(validateOptions));
        showErrors();
        errExpected = new String[]{
            XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID
        };
        assertTrue(compareErrorCodes(errExpected));


    }

    private String buildString(String Elt, boolean leadSpace) {
        StringBuffer sb = new StringBuffer();
        sb.append("<" + Elt);
        sb.append(" xmlns=\"http://xbean/scomp/contentType/builtIn/String\">");
        if (leadSpace)
            sb.append("  2 Lead spaces,A string on\n 2 lines with 2  spaces");
        else
            sb.append("\tLead tab,A string on\n 2 lines with 2  spaces");
        sb.append("</" + Elt + ">");
        return sb.toString();
    }

    public void testNumericypes() throws Throwable {
        FloatEltDocument flDoc =
                FloatEltDocument
                .Factory.parse("<FloatElt " +
                " xmlns=\"http://xbean/scomp/contentType/builtIn/Number\"" +
                ">12.34e+5</FloatElt>");
        assertTrue(flDoc.validate(validateOptions));
        flDoc.setFloatElt(13.5f);
        assertTrue(13.5f == flDoc.getFloatElt());

        DoubleEltDocument doubDoc =
                DoubleEltDocument.Factory.newInstance();
        assertTrue(0 == doubDoc.getDoubleElt());
        XmlDouble val = XmlDouble.Factory.newInstance();
        val.setDoubleValue(13.4d);
        doubDoc.xsetDoubleElt(val);
        assertTrue(doubDoc.validate(validateOptions));

        DecimalEltDocument decDoc =
                DecimalEltDocument.Factory.parse("<DecimalElt " +
                " xmlns=\"http://xbean/scomp/contentType/builtIn/Number\"" +
                ">12.34</DecimalElt>");
        assertTrue(decDoc.validate(validateOptions));
        BigDecimal bdval = new BigDecimal(new BigInteger("10"));
        decDoc.setDecimalElt(bdval);
        assertTrue(bdval == decDoc.getDecimalElt());

        IntegerEltDocument integerDoc =
                IntegerEltDocument.Factory.parse("<IntegerElt " +
                " xmlns=\"http://xbean/scomp/contentType/builtIn/Number\"" +
                ">124353</IntegerElt>");
        assertTrue(decDoc.validate(validateOptions));
        integerDoc.setIntegerElt(BigInteger.ONE);
        assertTrue(BigInteger.ONE == integerDoc.getIntegerElt());

        LongEltDocument longDoc =
                LongEltDocument.Factory.newInstance();
        longDoc.setLongElt(2459871);
        assertTrue(longDoc.validate(validateOptions));

        IntEltDocument intDoc = IntEltDocument.Factory.parse("<IntElt " +
                " xmlns=\"http://xbean/scomp/contentType/builtIn/Number\"" +
                "> -2147483648 </IntElt>");
        assertTrue(intDoc.validate(validateOptions));
        intDoc.setIntElt(2147483647);
        assertTrue(intDoc.validate(validateOptions));
        /**
         * short is derived from int by
         * setting the value of maxInclusive
         * to be 32767 and minInclusive to be -32768.
         */
        ShortEltDocument shDoc = ShortEltDocument.Factory.parse("<ShortElt " +
                " xmlns=\"http://xbean/scomp/contentType/builtIn/Number\"" +
                ">-32768</ShortElt>");
        assertTrue(shDoc.validate(validateOptions));
        assertTrue(-32768 == shDoc.xgetShortElt().getShortValue());
        //largest short is 32767. Don't use set--it would wrap around
        shDoc = ShortEltDocument.Factory.parse("<ShortElt " +
                " xmlns=\"http://xbean/scomp/contentType/builtIn/Number\"" +
                ">32768</ShortElt>");
        assertTrue(!shDoc.validate(validateOptions));
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_VALID
        };
        assertTrue(compareErrorCodes(errExpected));


        ByteEltDocument byteDoc = ByteEltDocument.Factory.newInstance();
        byteDoc.setByteElt((byte) -128);
        assertTrue(byteDoc.validate(validateOptions));
        byteDoc = ByteEltDocument.Factory.parse("<ByteElt " +
                " xmlns=\"http://xbean/scomp/contentType/builtIn/Number\"" +
                ">128</ByteElt>");

        clearErrors();
        assertTrue(!byteDoc.validate(validateOptions));
        showErrors();
        errExpected = new String[]{
            XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_VALID
        };
        assertTrue(compareErrorCodes(errExpected));

        NonPosIntEltDocument nonposIntDoc =
                NonPosIntEltDocument.Factory.parse("<NonPosIntElt " +
                " xmlns=\"http://xbean/scomp/contentType/builtIn/Number\"" +
                ">-0000000</NonPosIntElt>");
        assertTrue(0 == nonposIntDoc.getNonPosIntElt().intValue());
        assertTrue(nonposIntDoc.validate(validateOptions));
        //should be valid but javac complains is setter is called
        nonposIntDoc =
                NonPosIntEltDocument.Factory.parse("<NonPosIntElt " +
                " xmlns=\"http://xbean/scomp/contentType/builtIn/Number\"" +
                ">-12678967543233</NonPosIntElt>");
        assertTrue(nonposIntDoc.validate(validateOptions));

        NegativeIntEltDocument negIntDoc =
                NegativeIntEltDocument.Factory.parse("<NegativeIntElt " +
                " xmlns=\"http://xbean/scomp/contentType/builtIn/Number\"" +
                ">-12678967543233</NegativeIntElt>");
        assertTrue(negIntDoc.validate(validateOptions));

        NonNegIntEltDocument nonnegIntDoc =
                NonNegIntEltDocument.Factory.parse("<NonNegIntElt " +
                " xmlns=\"http://xbean/scomp/contentType/builtIn/Number\"" +
                ">12678967543233</NonNegIntElt>");
        assertTrue(nonnegIntDoc.validate(validateOptions));

        UnsignedLongEltDocument uLongDoc =
                UnsignedLongEltDocument.Factory.parse("<UnsignedLongElt " +
                " xmlns=\"http://xbean/scomp/contentType/builtIn/Number\"" +
                ">18446744073709551615</UnsignedLongElt>");
        assertTrue(nonnegIntDoc.validate(validateOptions));

        UnsignedIntEltDocument uInt =
                UnsignedIntEltDocument.Factory.parse("<UnsignedIntElt " +
                " xmlns=\"http://xbean/scomp/contentType/builtIn/Number\"" +
                ">4294967295</UnsignedIntElt>");
        assertTrue(uInt.validate(validateOptions));

        UnsignedShortEltDocument uShort =
                UnsignedShortEltDocument.Factory.parse("<UnsignedShortElt " +
                " xmlns=\"http://xbean/scomp/contentType/builtIn/Number\"" +
                ">65535</UnsignedShortElt>");

        assertTrue(uShort.validate(validateOptions));

        UnsignedByteEltDocument uByte =
                UnsignedByteEltDocument.Factory.newInstance();
        uByte.setUnsignedByteElt((short) 255);
        assertTrue(uByte.validate(validateOptions));

    }

    public void testDateTime() throws Throwable {
        DateEltDocument date =
                DateEltDocument.Factory.newInstance();

        date.setDateElt(getCalendar());
        assertTrue(date.validate(validateOptions));

        TimeEltDocument time = TimeEltDocument.Factory.parse("<TimeElt xmlns=\"http://xbean/scomp/contentType/builtIn/Date\">" +
                "23:56:00</TimeElt>");
        assertTrue(time.validate(validateOptions));

        DateTimeEltDocument
                dateTime = DateTimeEltDocument.Factory.newInstance();
        dateTime.setDateTimeElt(getCalendar());
        assertTrue(date.validate(validateOptions));

        GYearEltDocument
                year = GYearEltDocument.Factory.parse("<gYearElt xmlns=\"http://xbean/scomp/contentType/builtIn/Date\">" +
                "2004</gYearElt>");
        assertTrue(year.validate(validateOptions));

        GYearMonthEltDocument yrmo =
                GYearMonthEltDocument.Factory.newInstance();
        XmlGYearMonth val = XmlGYearMonth.Factory.newInstance();
        GDate dt = new GDate(getCalendar());
        val.setGDateValue(dt);
        yrmo.xsetGYearMonthElt(val);
        assertTrue(yrmo.validate(validateOptions));

        GMonthEltDocument mo =
                GMonthEltDocument.Factory.newInstance();
        Calendar c = getCalendar();
        c.set(1997, 10, 06);
        mo.setGMonthElt(c);
        assertTrue(mo.validate(validateOptions));

        GMonthDayEltDocument moday =
                GMonthDayEltDocument.Factory.newInstance();
        moday.setGMonthDayElt(getCalendar());
        assertTrue(moday.validate(validateOptions));

        GDayEltDocument day = GDayEltDocument.Factory.parse("<gDayElt xmlns=\"http://xbean/scomp/contentType/builtIn/Date\">" +
                "32</gDayElt>");
        assertTrue(!day.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.DATE
        };
        assertTrue(compareErrorCodes(errExpected));

        day.setGDayElt(c);
        assertTrue(day.validate(validateOptions));


        DurationEltDocument duration =
                DurationEltDocument.Factory.newInstance();
        GDurationBuilder gdb = new GDurationBuilder();
        gdb.setDay(11);
        gdb.setMonth(5);
        gdb.setYear(2004);
        duration.setDurationElt(new GDuration(gdb));
        assertTrue(duration.validate(validateOptions));

    }

    private Calendar getCalendar() {
// get the supported ids for GMT-08:00 (Pacific Standard Time)
        String[] ids = TimeZone.getAvailableIDs(-8 * 60 * 60 * 1000);
        // if no ids were returned, something is wrong. get out.
        if (ids.length == 0)
            return null;


        // create a Pacific Standard Time time zone
        SimpleTimeZone pdt = new SimpleTimeZone(-8 * 60 * 60 * 1000, ids[0]);

        // set up rules for daylight savings time
        pdt.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY,
                2 * 60 * 60 * 1000);
        pdt.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY,
                2 * 60 * 60 * 1000);

        // create a GregorianCalendar with the Pacific Daylight time zone
        // and the current date and time
        Calendar calendar = new GregorianCalendar(pdt);
        Date trialTime = new Date();
        calendar.setTime(trialTime);
        return calendar;
    }
}
