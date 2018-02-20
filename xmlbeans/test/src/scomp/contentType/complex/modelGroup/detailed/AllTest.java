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
import xbean.scomp.contentType.modelGroup.AllEltDocument;
import xbean.scomp.contentType.modelGroup.AllT;

import java.math.BigInteger;

import org.apache.xmlbeans.XmlErrorCodes;

/**
 *
 *
 *
 */
public class AllTest extends BaseCase {

    /**
     * Instance should be valid w/ child1 missing
     */
    public void testChild1Optional() throws Throwable {
        doc = AllEltDocument.Factory.newInstance();
        AllT elt = doc.addNewAllElt();
        elt.setChild2("doo");
        elt.setChild3(BigInteger.ONE);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

    }

    /**
     * All group doesn't care about field order
     *
     * @throws Throwable
     */
    public void testOrder() throws Throwable {
        String input =
                "<pre:AllElt xmlns:pre=\"http://xbean/scomp/contentType/ModelGroup\">" +
                "<child3>5</child3><child1>2</child1><child2>bar</child2>" +
                "</pre:AllElt>";
        doc = AllEltDocument.Factory.parse(input);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }


    }

    /**
     * maxOccurs is always 1
     *
     * @throws Throwable
     */
    public void testIllegal() throws Throwable {
        String input =
                "<pre:AllElt xmlns:pre=\"http://xbean/scomp/contentType/ModelGroup\">" +
                "<child3>5</child3><child3>2</child3><child2>bar</child2>" +
                "</pre:AllElt>";
        doc = AllEltDocument.Factory.parse(input);
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        //$TODO: QUESTIONABLE ERRORS: if <child3>2</child3> is replaced by <child1>2</child1>
        //all will be good: why 2 errors?
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_ELEMENT
        };
                   assertTrue(compareErrorCodes(errExpected));

    }


    private AllEltDocument doc;
}
