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
package scomp.contentType.complex.modelGroup.detailed;

import scomp.common.BaseCase;
import xbean.scomp.contentType.modelGroup.NestedChoiceInSequenceDocument;
import xbean.scomp.contentType.modelGroup.NestedChoiceInSequenceT;
import org.apache.xmlbeans.XmlErrorCodes;

/**
 *
 *
 *
 */
public class NestSequenceChoiceTest extends BaseCase {
    /**
     * Choice group is optional
     */
    public void testChoiceMissing() throws Throwable {
        NestedChoiceInSequenceDocument doc =
                NestedChoiceInSequenceDocument.Factory.newInstance();
        NestedChoiceInSequenceT elt = doc.addNewNestedChoiceInSequence();
        elt.setChildDouble(1.3);
        elt.setChildInt(2);
        elt.setChildStr("foo");

        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }

    }

    public void testAllPresent() throws Throwable {
        NestedChoiceInSequenceDocument doc =
                NestedChoiceInSequenceDocument.Factory.newInstance();
        NestedChoiceInSequenceT elt = doc.addNewNestedChoiceInSequence();
        elt.setChildDouble(1.3);
        elt.setChildInt(2);
        elt.setChildStr("foo");

        elt.setOptchildDouble(1.4);

        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }

        //can't have both set
        elt.setOptchildInt(2);
        elt.setOptchildStr("boo");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT
        };
        assertTrue(compareErrorCodes(errExpected));

        elt.unsetOptchildDouble();
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }

    }

    /**
     * Missing elt. from the sequence in the choice
     */
    public void testIllegal() throws Throwable {
        NestedChoiceInSequenceDocument doc =
                NestedChoiceInSequenceDocument.Factory.newInstance();
        NestedChoiceInSequenceT elt = doc.addNewNestedChoiceInSequence();
        elt.setChildDouble(1.3);
        elt.setChildInt(2);
        elt.setChildStr("foo");

        elt.setOptchildInt(2);
        //optChildStr is missing
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
           XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT
        };
        assertTrue(compareErrorCodes(errExpected));


        elt.setOptchildStr("boo");
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }

    }

    /**
     * Incorrect order in inner sequence
     */
    public void testIllegalOrderInner() throws Throwable {
        String input =
                "<pre:NestedChoiceInSequence  " +
                "xmlns:pre=\"http://xbean/scomp/contentType/ModelGroup\">" +
                "<childStr>foo</childStr>" +
                "<childInt>3</childInt>" +
                "<optchildInt>0</optchildInt>" +
                "<optchildStr>foo</optchildStr>" +
                "</pre:NestedChoiceInSequence>";
        NestedChoiceInSequenceDocument doc =
                NestedChoiceInSequenceDocument.Factory.parse(input);
        assertTrue(!doc.validate(validateOptions));
        showErrors();
       // TODO: why are there 2 different errors: just the order is swapped
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT,
             XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_ELEMENT
        };
        assertTrue(compareErrorCodes(errExpected));


    }

    /**
     * Incorrect order in outer sequence
     */
    public void testIllegalOrderOuter() throws Throwable {
        String input =
                "<pre:NestedChoiceInSequence  " +
                "xmlns:pre=\"http://xbean/scomp/contentType/ModelGroup\">" +
                "<childInt>3</childInt>" +
                "<childStr>foo</childStr>" +
                "<optchildStr>foo</optchildStr>" +
                "<optchildInt>0</optchildInt>" +
                "</pre:NestedChoiceInSequence>";
        NestedChoiceInSequenceDocument doc =
                NestedChoiceInSequenceDocument.Factory.parse(input);
        assertTrue(!doc.validate(validateOptions));
        showErrors();

    }

}
