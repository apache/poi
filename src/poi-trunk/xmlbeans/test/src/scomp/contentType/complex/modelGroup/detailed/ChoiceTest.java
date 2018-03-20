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


import xbean.scomp.contentType.modelGroup.ChoiceEltDocument;
import xbean.scomp.contentType.modelGroup.ChoiceT;
import xbean.scomp.contentType.modelGroup.MixedChoiceEltDocument;
import xbean.scomp.contentType.modelGroup.MixedChoiceT;
import scomp.common.BaseCase;

import java.math.BigInteger;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlErrorCodes;

/**
 *
 *
 *
 */

//TODO: assert that order of elements in a choice group doesn't matter
public class ChoiceTest extends BaseCase {
    public void testValidCase() throws Throwable {
        ChoiceEltDocument doc = ChoiceEltDocument.Factory.newInstance();
        ChoiceT elt = doc.addNewChoiceElt();
        elt.addChild3(new BigInteger("10"));
        elt.addChild3(BigInteger.ZERO);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

    //more than 1 from choice group
    //TODO: test should pass but error message not good
    public void testChoiceViolation() throws Throwable {
        ChoiceEltDocument doc = ChoiceEltDocument.Factory.newInstance();
        ChoiceT elt = doc.addNewChoiceElt();
        elt.addChild2("foobar");
        elt.addChild3(new BigInteger("10"));
        elt.addChild3(BigInteger.ZERO);

        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testMixedChoice() throws Throwable {
        MixedChoiceEltDocument doc = MixedChoiceEltDocument.Factory.newInstance();
        MixedChoiceT elt = doc.addNewMixedChoiceElt();
        assertTrue(!elt.isSetChild1());
        elt.setChild1(new BigInteger("10"));
        XmlCursor cur = elt.newCursor();
        assertEquals(XmlCursor.TokenType.START, cur.toFirstContentToken());
        cur.toEndToken(); //past child one
        cur.toNextToken();
        cur.insertChars("foobar");
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
        assertEquals("<xml-fragment><child1>10</child1>foobar</xml-fragment>", elt.xmlText());
    }

}
