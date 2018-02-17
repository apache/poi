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

package scomp.contentType.complex.detailed;

import junit.framework.TestCase;

import java.math.BigInteger;

import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlErrorCodes;

import xbean.scomp.contentType.complexTypeTest.EltTypeDocument;
import xbean.scomp.contentType.complexTypeTest.ElementT;
import scomp.common.BaseCase;

/**
 *
 *
 *
 */
public class ElementOnlyContentTest extends BaseCase {

    /**
     * Element only content
     *
     * @throws Throwable
     */

    public void testElementOnly() throws Throwable {
        EltTypeDocument doc = EltTypeDocument.Factory.newInstance();
        ElementT testElt
                = doc.getEltType();
        assertEquals(null, testElt);
        testElt = doc.addNewEltType();
        assertEquals(null, testElt.getChild1());
        assertEquals(null, testElt.xgetChild1());
        testElt.setChild1(new BigInteger("10"));
        testElt.setChild2(new BigInteger("5"));
        testElt.setChild3(new BigInteger("1"));
        assertEquals("<xml-fragment><child1>10</child1><child2>5</child2>" +
                "<child3>1</child3></xml-fragment>",
                testElt.xmlText());

        testElt.xsetChild2(
                XmlInteger.Factory.parse("<xml-fragment>3</xml-fragment>"));
        assertEquals("<xml-fragment><child1>10</child1><child2>3</child2>" +
                "<child3>1</child3></xml-fragment>",
                testElt.xmlText());
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

    /**
     * Mixed content is invalid for element only types
     */
    public void testInvalidContent() {
        EltTypeDocument doc = EltTypeDocument.Factory.newInstance();
        ElementT testElt
                = doc.getEltType();
        assertEquals(null, testElt);
        testElt = doc.addNewEltType();
        assertEquals(null, testElt.getChild1());
        assertEquals(null, testElt.xgetChild1());
        testElt.setChild1(new BigInteger("10"));
        testElt.setChild2(new BigInteger("5"));
        testElt.setChild3(new BigInteger("1"));
        assertTrue(testElt.validate());
        XmlCursor cur = testElt.newCursor();
        cur.toFirstContentToken();
        cur.insertChars("Random mixed content");
        System.out.println(testElt.xmlText());
        assertTrue(!testElt.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_ONLY_WITH_TEXT};
            assertTrue(compareErrorCodes(errExpected));

    }

}
