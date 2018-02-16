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
import xbean.scomp.contentType.complexTypeTest.MixedTypeDocument;
import xbean.scomp.contentType.complexTypeTest.MixedT;
import xbean.scomp.contentType.complexTypeTest.MixedFixedEltDocument;

import java.math.BigInteger;

import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlException;
import scomp.common.BaseCase;

/**
 *
 *
 *
 */
public class MixedContentTest extends BaseCase {

    public void testElementsOnly() throws Throwable {

        testElt = doc.addNewMixedType();
        assertEquals(null, testElt.getChild1());
        assertEquals(null, testElt.xgetChild1());
        testElt.setChild2(new BigInteger("5"));
        testElt.setChild3(new BigInteger("1"));
        testElt.setChild1(new BigInteger("0"));
        assertEquals("<xml-fragment><child1>0</child1><child2>5</child2>" +
                "<child3>1</child3></xml-fragment>",
                testElt.xmlText());

        testElt.xsetChild2(
                XmlInteger.Factory.parse("<xml-fragment>3</xml-fragment>"));
        assertEquals("<xml-fragment><child1>0</child1><child2>3</child2>" +
                "<child3>1</child3></xml-fragment>",
                testElt.xmlText());
        try {
            assertTrue(testElt.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

    /**
     * Note that the mixed model in XML Schema differs fundamentally from the
     * mixed model in XML 1.0. Under the XML Schema mixed model, the order and
     *  number of child elements appearing in an instance must agree with the
     * order and number of child elements specified in the model
     * @throws Throwable
     */
    public void testTextOnly() throws Throwable {

        testElt = doc.addNewMixedType();
        assertEquals(null, testElt.getChild1());
        assertEquals(null, testElt.xgetChild1());
        XmlCursor cur = testElt.newCursor();
        cur.insertChars("Random mixed content");
        assertTrue( !testElt.validate(validateOptions) );
        showErrors();
        String[] errExpected = new String[]{
             XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_ELEMENT
        };
            assertTrue(compareErrorCodes(errExpected));


    }

    public void testMixed() throws Throwable {
         testElt = doc.addNewMixedType();
        assertEquals(null, testElt.getChild1());
        assertEquals(null, testElt.xgetChild1());
        testElt.setChild2(new BigInteger("5"));
        testElt.setChild3(new BigInteger("1"));
        testElt.setChild1(new BigInteger("0"));
         try {
            assertTrue(testElt.validate());
        }
        catch (Throwable t) {
            testElt.validate(validateOptions);
            showErrors();
            throw t;
        }
        XmlCursor cur = testElt.newCursor();
        cur.toFirstContentToken();
        cur.insertChars("Random mixed content");
        //move past child1
        cur.toNextToken();
        cur.toNextToken();
         cur.toNextToken();
        cur.insertChars("Random mixed content1");
        try {
            assertTrue(testElt.validate());
        }
        catch (Throwable t) {
            testElt.validate(validateOptions);
            showErrors();
            throw t;
        }
        assertEquals("<xml-fragment>Random mixed content" +
                "<child1>0</child1>Random mixed content1<child2>5</child2>" +
                "<child3>1</child3></xml-fragment>",testElt.xmlText() );
    }
    public void testInsertDelete() throws Throwable{
        testElt = doc.addNewMixedType();
        testElt.setChild2(new BigInteger("5"));
        testElt.setChild3(new BigInteger("1"));
        testElt.setChild1(new BigInteger("0"));
        XmlCursor cur = testElt.newCursor();
        cur.toFirstContentToken();
        cur.insertChars("Random mixed content");
        //move past child1
        cur.toNextToken();
        cur.toNextToken();
         cur.toNextToken();
        cur.insertChars("Random mixed content1");
        try {
            assertTrue(testElt.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        assertEquals("<xml-fragment>Random mixed content" +
                "<child1>0</child1>Random mixed content1<child2>5</child2>" +
                "<child3>1</child3></xml-fragment>",testElt.xmlText() );
        //to child1
        cur.toPrevToken();
        cur.toPrevToken();
         cur.toPrevToken();
         cur.toPrevToken();
        assertEquals(XmlCursor.TokenType.START, cur.currentTokenType());
        assertTrue(cur.removeXml());
         assertEquals(null,testElt.getChild1());

       assertEquals("<xml-fragment>Random mixed content" +
                "Random mixed content1<child2>5</child2>" +
                "<child3>1</child3></xml-fragment>",testElt.xmlText() );
       

    }

 /**
  * see CR related to CR194159:

clause 5.2.2.1 of
  "Validation Rule: Element Locally Valid (Element)" says
  if there is a fixed value constraint, the element may not have element children.
  * @throws XmlException
  */
    public void testMixedFixed() throws XmlException{
        MixedFixedEltDocument doc=
              MixedFixedEltDocument.Factory
        .parse("<pre:MixedFixedElt " +
                " xmlns:pre=\"http://xbean/scomp/contentType/ComplexTypeTest\">" +
                "<a/>abc</pre:MixedFixedElt>");

        assertTrue (! doc.validate(validateOptions));
        showErrors();
        String[] expected=new String[]
        {XmlErrorCodes.ELEM_LOCALLY_VALID$FIXED_WITH_CONTENT};
        assertTrue(compareErrorCodes(expected));

    }
    public void setUp() {
        doc = MixedTypeDocument.Factory.newInstance();
        testElt
                = doc.getMixedType();
        assertEquals(null, testElt);
        super.setUp();
    }

    private MixedTypeDocument doc;
    private MixedT testElt;

}
