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

package scomp.elements.detailed;

import scomp.common.BaseCase;
import xbean.scomp.element.globalEltNillable.*;
import org.apache.xmlbeans.impl.values.XmlValueNotNillableException;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlOptions;

/**
 *
 *
 */
public class GlobalEltNillable extends BaseCase {

    //xsi:nil illegal in instance if the elt is not nillable

    public void testNillableFalse() throws Exception {
        GlobalEltNotNillableDocument testElt = GlobalEltNotNillableDocument
                .Factory.parse("<GlobalEltNotNillable" +
                "   xmlns=\"http://xbean/scomp/element/GlobalEltNillable\"" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                " xsi:nil=\"false\"/>");
        assertTrue(!testElt.validate(validateOptions));
        assertEquals(1, errorList.size());
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_LOCALLY_VALID$NOT_NILLABLE};
             assertTrue(compareErrorCodes(errExpected));


    }

    /**
     * Try to set a non-nillable elt. to nill
     * CR CR192914:
     * Regardless of Schema definition,
     * setXXX(null) will clear the value of the
     * XXX attribute/element and if the container is an
     * element, will also add the "xsi:nil" attribute.

     *
     * @throws Exception
     */
    public void testNotNillable() throws Exception {

        // XmlValueNotNillableException should be thrown only when validateOnSet property is set
        XmlOptions options = new XmlOptions();
        options.setValidateOnSet();

        GlobalEltNotNillableDocument testElt = GlobalEltNotNillableDocument
                .Factory.newInstance(options);
        try {
            testElt.setNil();
            fail("Expected XmlValueNotNillableException");
        }
        catch (XmlValueNotNillableException e) {
        }

         try {
            testElt.set(null);
            fail("Expected XmlValueNotNillableException");
        }
        catch (XmlValueNotNillableException e) {
        }

        testElt.setGlobalEltNotNillable(null);
        //assert that value is cleared
        assertEquals("<glob:GlobalEltNotNillable " +
                "xsi:nil=\"true\" " +
                "xmlns:glob=\"http://xbean/scomp/element/GlobalEltNillable\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>"
                ,testElt.xmlText());
        assertTrue(!testElt.validate(validateOptions));
        assertEquals(1, errorList.size());
        showErrors();
        String[] errExpected =
                new String[]{
                    XmlErrorCodes.ELEM_LOCALLY_VALID$NOT_NILLABLE};
             assertTrue(compareErrorCodes(errExpected));
}

    //for nillable, fixed value cannot be specified (instance error) :
    // Walmsley p.137 footnote
    public void testNillableFixed() throws Exception {
        GlobalEltNillableFixedDocument testElt = GlobalEltNillableFixedDocument
                .Factory.parse("<GlobalEltNillableFixed" +
                "   xmlns=\"http://xbean/scomp/element/GlobalEltNillable\"" +
                "   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                "   xsi:nil=\"true\"" +
                "/>");
        assertFalse(testElt.validate(validateOptions));
        assertEquals(1, errorList.size());
        showErrors();
               String[] errExpected = new String[]{XmlErrorCodes.ELEM_LOCALLY_VALID$NIL_WITH_FIXED};
                    assertTrue(compareErrorCodes(errExpected));


    }

    public void testNillableInt() throws Exception {
        GlobalEltNillableIntDocument testElt = GlobalEltNillableIntDocument
                .Factory.parse("<GlobalEltNillableInt" +
                "   xmlns=\"http://xbean/scomp/element/GlobalEltNillable\"" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                " xsi:nil=\"true\"/>");

        try {
            assertTrue(testElt.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
        }
        assertTrue ( testElt.isNilGlobalEltNillableInt());
        assertEquals(0, testElt.getGlobalEltNillableInt());

        //after setting the value, the nil attribute should be gone
        testElt.setGlobalEltNillableInt(3);
        assertEquals(
                "<GlobalEltNillableInt" +
                " xmlns=\"http://xbean/scomp/element/GlobalEltNillable\"" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                ">3</GlobalEltNillableInt>",
                testElt.xmlText() );


    }


    //default value not filled in for nillable elts when xsi:nil=true
    // $TODO: check w/ Kevin--what is the value of a nillable attr if it's a primitive type????
    public void testNillableDefault() throws Exception {
        GlobalEltNillableDefaultDocument testElt = GlobalEltNillableDefaultDocument
                .Factory.parse("<GlobalEltNillableDefault" +
                "   xmlns=\"http://xbean/scomp/element/GlobalEltNillable\"" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                " xsi:nil=\"true\"/>");
        try {
            assertTrue(testElt.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
        }

        assertEquals(0, testElt.getGlobalEltNillableDefault());
    }

    // An element with xsi:nil="true" may not have any element content but it
    //  may still carry attributes.
    public void testComplexNillable() throws Throwable {
        GlobalEltComplexDocument testElt = GlobalEltComplexDocument
                .Factory.parse("<GlobalEltComplex" +
                "   xmlns=\"http://xbean/scomp/element/GlobalEltNillable\"" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                " xsi:nil=\"true\"><nestedElt/></GlobalEltComplex>");
        assertTrue(!testElt.validate(validateOptions));
        assertEquals(1, errorList.size());

        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_LOCALLY_VALID$NIL_WITH_CONTENT};
             assertTrue(compareErrorCodes(errExpected));

        testElt = GlobalEltComplexDocument
                .Factory.parse("<GlobalEltComplex" +
                "   xmlns=\"http://xbean/scomp/element/GlobalEltNillable\"" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                " xsi:nil=\"true\" testattribute=\"foobar\"/>");
        try {
            assertTrue(testElt.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

    }

    /** calling setNil should inserts
     * attr and delete value
     * @throws Throwable
     */
    public void testDelete() throws Throwable{
        GlobalEltComplexDocument  testElt = GlobalEltComplexDocument
                .Factory.parse("<pre:GlobalEltComplex" +
                "   xmlns:pre=\"http://xbean/scomp/element/GlobalEltNillable\"" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                " testattribute=\"foobar\">" +
                "<nestedElt>" +
                "foo</nestedElt></pre:GlobalEltComplex>");
        try {
            assertTrue(testElt.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        testElt.getGlobalEltComplex().setNil();
        assertEquals("<pre:GlobalEltComplex " +                 
                "testattribute=\"foobar\" " +
                "xsi:nil=\"true\" " +
                "xmlns:pre=\"http://xbean/scomp/element/GlobalEltNillable\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>",
                testElt.xmlText());
         try {
            assertTrue(testElt.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

    }

}
