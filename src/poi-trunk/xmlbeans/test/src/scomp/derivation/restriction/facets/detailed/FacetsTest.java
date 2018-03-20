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
package scomp.derivation.restriction.facets.detailed;

import scomp.common.BaseCase;
import xbean.scomp.derivation.facets.facets.*;
import xbean.scomp.derivation.facets.facets.DigitsEltDocument;
import xbean.scomp.derivation.facets.facets.EnumEltDocument;
import xbean.scomp.derivation.facets.facets.EnumT;
import xbean.scomp.derivation.facets.facets.LengthEltDocument;
import xbean.scomp.derivation.facets.facets.MinMaxExclusiveDateEltDocument;
import xbean.scomp.derivation.facets.facets.MinMaxExclusiveEltDocument;
import xbean.scomp.derivation.facets.facets.MinMaxInclusiveDateEltDocument;
import xbean.scomp.derivation.facets.facets.MinMaxInclusiveEltDocument;
import xbean.scomp.derivation.facets.facets.MinMaxLengthEltDocument;
import xbean.scomp.derivation.facets.facets.PatternEltDocument;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.xmlbeans.XmlErrorCodes;

/**
 */
public class FacetsTest extends BaseCase {

    public void testMinMaxInclusiveElt() throws Throwable {
        MinMaxInclusiveEltDocument doc =
                MinMaxInclusiveEltDocument.Factory.newInstance();
        doc.setMinMaxInclusiveElt(3);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_MIN_INCLUSIVE_VALID};

        doc.setMinMaxInclusiveElt(1);
        assertTrue(!doc.validate(validateOptions));
        assertTrue(compareErrorCodes(errExpected));

        clearErrors();
        errExpected = new String[]{
            XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_VALID};
        doc.setMinMaxInclusiveElt(11);
        assertTrue(!doc.validate(validateOptions));
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testMinMaxInclusiveDateElt() throws Throwable {
        MinMaxInclusiveDateEltDocument doc =
                MinMaxInclusiveDateEltDocument.Factory.newInstance();
        TimeZone tz = TimeZone.getDefault();
        Calendar c = new GregorianCalendar(tz);
        c.set(2003, 11, 24);
        doc.setMinMaxInclusiveDateElt(c);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
        c = new GregorianCalendar(2003, 11, 28);
        doc.setMinMaxInclusiveDateElt(c);
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_VALID};
        assertTrue(!doc.validate(validateOptions));
        assertTrue(compareErrorCodes(errExpected));


    }

    //valid range should be 3-9
    public void testMinMaxExclusiveElt() throws Throwable {
        MinMaxExclusiveEltDocument doc =
                MinMaxExclusiveEltDocument.Factory.newInstance();
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_MIN_EXCLUSIVE_VALID};

        doc.setMinMaxExclusiveElt(2);
        assertTrue(!doc.validate(validateOptions));
        assertTrue(compareErrorCodes(errExpected));
        doc.setMinMaxExclusiveElt(3);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc.setMinMaxExclusiveElt(9);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }

    }

    //valid range is 12-11 12-24-2003
    public void testMinMaxExclusiveDateElt() throws Throwable {
        MinMaxExclusiveDateEltDocument doc = MinMaxExclusiveDateEltDocument.Factory.newInstance();
        Calendar c = new GregorianCalendar(2003, 11, 25);
        doc.setMinMaxExclusiveDateElt(c);
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_MAX_EXCLUSIVE_VALID};
        assertTrue(!doc.validate(validateOptions));
        assertTrue(compareErrorCodes(errExpected));

        c = new GregorianCalendar(2003, 11, 11);
        doc.setMinMaxExclusiveDateElt(c);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }


    }

    public void testLengthElt() throws Throwable {
        LengthEltDocument doc = LengthEltDocument.Factory.newInstance();
        doc.setLengthElt("foobar");
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_LENGTH_VALID$STRING};

        assertTrue(!doc.validate(validateOptions));
        assertTrue(compareErrorCodes(errExpected));

        doc.setLengthElt("f");
        clearErrors();
        assertTrue(!doc.validate(validateOptions));
        assertTrue(compareErrorCodes(errExpected));

        doc.setLengthElt("fo");
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

    public void testMinMaxLengthElt() throws Throwable {
        MinMaxLengthEltDocument doc = MinMaxLengthEltDocument.Factory.newInstance();
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_MAX_LENGTH_VALID$STRING};

        doc.setMinMaxLengthElt("foobar");
        assertTrue(!doc.validate(validateOptions));
        assertTrue(compareErrorCodes(errExpected));

        doc.setMinMaxLengthElt("f");
        errExpected = new String[]{
            XmlErrorCodes.DATATYPE_MIN_LENGTH_VALID$STRING};
        clearErrors();
        assertTrue(!doc.validate(validateOptions));
        assertTrue(compareErrorCodes(errExpected));

        doc.setMinMaxLengthElt("fo");
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc.setMinMaxLengthElt("fooba");
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }

    }

    public void testDigitsElt() throws Throwable {
        DigitsEltDocument doc = DigitsEltDocument.Factory.newInstance();
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_TOTAL_DIGITS_VALID};

        doc.setDigitsElt(new BigDecimal("234.25"));
        assertTrue(!doc.validate(validateOptions));
        assertTrue(compareErrorCodes(errExpected));

        doc.setDigitsElt(new BigDecimal("12.13"));
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
        clearErrors();
        errExpected = new String[]{
            XmlErrorCodes.DATATYPE_FRACTION_DIGITS_VALID};
        doc.setDigitsElt(new BigDecimal(".145"));
        assertTrue(!doc.validate(validateOptions));
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testWSElt() throws Throwable {
        WSPreserveEltDocument doc = WSPreserveEltDocument.Factory.parse("<WSPreserveElt " +
                "xmlns=\"http://xbean/scomp/derivation/facets/Facets\">" +
                "This is a\ttest.\nThe resulting string should preserve all whitespace     tabs and carriage returns as is\n" +
                "</WSPreserveElt>");


        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

        String expected = "This is a\ttest.\nThe resulting string should preserve all whitespace     tabs and carriage returns as is\n";
        assertEquals(expected, doc.getWSPreserveElt());
    }

    public void testEnumElt() throws Throwable {

        EnumEltDocument doc = EnumEltDocument.Factory.newInstance();
        doc.setEnumElt(EnumT.A);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc = EnumEltDocument.Factory.parse("<EnumElt xmlns=\"http://xbean/scomp/derivation/facets/Facets\">" +
                "foo" +
                "</EnumElt>");
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_ENUM_VALID};

        assertTrue(!doc.validate(validateOptions));
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testPatternElt() throws Throwable {
        PatternEltDocument doc = PatternEltDocument.Factory.newInstance();
        doc.setPatternElt("aedaedaed");
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID};

        doc.setPatternElt("abdadad");
        assertTrue(!doc.validate(validateOptions));
        assertTrue(compareErrorCodes(errExpected));


    }
}
