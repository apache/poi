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
package scomp.derivation.restriction.detailed;

import scomp.common.BaseCase;
import xbean.scomp.derivation.groupRestriction.*;

import java.math.BigInteger;

import org.apache.xmlbeans.XmlErrorCodes;

/**
 *
 */
public class GroupRestrictionTest extends BaseCase {

    public void testRestrictSequence() throws Throwable {
        RestrictedSequenceEltDocument doc = RestrictedSequenceEltDocument.Factory
                .newInstance();
        RestrictedSequenceT elt = doc.addNewRestrictedSequenceElt();
        elt.setChild1(BigInteger.ONE);

        elt.addChild3(new BigInteger("10"));
        elt.addChild3(new BigInteger("10"));
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
        elt.addChild2("foobar");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT
        };
        assertTrue(compareErrorCodes(errExpected));


    }

    public void testRestrictChoice() throws Throwable {
        RestrictedChoiceEltDocument doc = RestrictedChoiceEltDocument.Factory
                .newInstance();
        RestrictedChoiceT elt = doc.addNewRestrictedChoiceElt();
        elt.addChild2("foobar");
        elt.addChild3(BigInteger.ZERO);
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT};
        assertTrue(compareErrorCodes(errExpected));

        elt.removeChild2(0);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }

    }

    public void testRestrictAll() throws Throwable {
        RestrictedAllEltDocument doc = RestrictedAllEltDocument.Factory
                .newInstance();
        RestrictedAllT elt = doc.addNewRestrictedAllElt();
        elt.setChild2("foobar");
        //child3 can't be missing
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_ELEMENT
        };
        assertTrue(compareErrorCodes(errExpected));

        elt.setChild3(new BigInteger("10"));
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

    public void testAllToSequence() throws Throwable {
        All2SeqEltDocument doc = All2SeqEltDocument.Factory.newInstance();
        All2SequenceT elt = doc.addNewAll2SeqElt();
        elt.setA("foo");
        elt.setC(3);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
        //b not part of restricted type
        elt.setB("bar");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testChoiceToSequence() throws Throwable {
        Choice2SeqEltDocument doc = Choice2SeqEltDocument.Factory.newInstance();
        Choice2SequenceT elt = doc.addNewChoice2SeqElt();
        elt.addA("foo");
        elt.addC(3);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
        //b not part of restricted type
        elt.addB("bar");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT
        };
        assertTrue(compareErrorCodes(errExpected));

    }
}
