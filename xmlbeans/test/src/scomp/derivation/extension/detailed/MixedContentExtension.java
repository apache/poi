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

package scomp.derivation.extension.detailed;

import scomp.common.BaseCase;
import xbean.scomp.derivation.complexExtension.ExtendedMixedEltDocument;
import xbean.scomp.derivation.complexExtension.ExtendedMixedT;

import java.math.BigInteger;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlErrorCodes;

/**
 *
 *
 *
 */
public class MixedContentExtension extends BaseCase {

    public void testMixedContentInvalid() throws Throwable {
        ExtendedMixedEltDocument doc = ExtendedMixedEltDocument.
                Factory.newInstance();
        ExtendedMixedT elt = doc.addNewExtendedMixedElt();
        elt.setExtendedAttr("FOOBAR_val");
        elt.setChild1(new BigInteger("10"));
        XmlCursor cur = elt.newCursor();
        cur.toFirstContentToken();
        cur.beginElement("Child2");
        cur.toNextToken();
        cur.insertChars("2");
        elt.setChild3(BigInteger.ONE);
        cur.toFirstContentToken();
        cur.toEndToken();
        cur.toNextToken();
         cur.toNextToken();
        cur.insertChars(" SOME CDATA HERE");
        String resultStr=
                "<com:ExtendedMixedElt extendedAttr=\"FOOBAR_val\" " +
                "xmlns:com=\"http://xbean/scomp/derivation/ComplexExtension\">" +
                "<Child2/>" +
                "2" +
                "<child1>10</child1>" +
                " SOME CDATA HERE" +
                "<child3>1</child3>" +
                "</com:ExtendedMixedElt>";
        assertEquals(resultStr, doc.xmlText());

        assertTrue(!doc.validate(validateOptions));
        showErrors();
        //TODO: Order check: is this the right errors? last one seems redundant given second-to-last
        //need ch1, ch2
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_ELEMENT
         };
        assertTrue(compareErrorCodes(errExpected));


    }


    public void testMixedContentValid() throws Throwable {
        ExtendedMixedEltDocument doc = ExtendedMixedEltDocument.
                Factory.newInstance();
        ExtendedMixedT elt = doc.addNewExtendedMixedElt();
        elt.setExtendedAttr("FOOBAR_val");
        elt.setChild1(new BigInteger("10"));
        XmlCursor cur = elt.newCursor();
        cur.toEndToken();
        cur.beginElement("child2");
        cur.insertChars("2");
        cur.toNextToken();

        cur.insertComment("My comment");
        elt.setChild3(BigInteger.ONE);
        cur.toFirstContentToken();
        cur.toEndToken();
         cur.insertChars("SOME CDATA HERE");
        try {
            assertTrue( doc.validate(validateOptions) );
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        assertEquals("<com:ExtendedMixedElt " +
                "extendedAttr=\"FOOBAR_val\" " +
                "xmlns:com=\"http://xbean/scomp/derivation/ComplexExtension\">" +
                "<child1>10</child1>" +
                "<child2>2</child2><!--My comment--><child3>1</child3>" +
                "SOME CDATA HERE</com:ExtendedMixedElt>", doc.xmlText());
    }

}
