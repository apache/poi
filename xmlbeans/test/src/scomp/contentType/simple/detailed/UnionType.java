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
import xbean.scomp.contentType.union.*;

import java.util.List;
import java.util.ArrayList;

import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;

/**
 *
 *
 *
 */
public class UnionType extends BaseCase {
    /**
     * should be a bunch of negative cases at compile time
     */
    public void testUnionType() throws Throwable {
        UnionEltDocument doc = UnionEltDocument.Factory.newInstance();
        assertEquals(null, doc.getUnionElt());
        doc.setUnionElt("small");
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc.setUnionElt(new Integer(2));
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc.setUnionElt(new Integer(-2));
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc.setUnionElt(new Integer(5));
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_VALID$UNION};
                    assertTrue(compareErrorCodes(errExpected));

    }

    /**
     * valid instance w/ xsi:type hint
     *
     * @throws Throwable
     */
    public void testParseInstanceValid() throws Throwable {
        String input =
                "<UnionElt xmlns=\"http://xbean/scomp/contentType/Union\"" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                " xsi:type=\"GlobalSimpleT2\">" +
                "-2" +
                "</UnionElt>";
        UnionEltDocument doc = UnionEltDocument.Factory.parse(input);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

    /**
     * invalid instance w/ xsi:type hint
     *
     * @throws Throwable
     */
    public void testParseInstanceInvalid() throws Throwable {
        String input =
                "<UnionElt xmlns=\"http://xbean/scomp/contentType/Union\"" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                " xsi:type=\"GlobalSimpleT1\">" +
                "-2" +
                "</UnionElt>";
        UnionEltDocument doc = UnionEltDocument.Factory.parse(input);
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_MIN_EXCLUSIVE_VALID};
                    assertTrue(compareErrorCodes(errExpected));

    }

    /**
     * Specifiying value for a union that is not part of the consitituent types. The constituent types in this schema
     * are enumerations and not basic XmlSchema types and hence get translated into enum types in the XmlObjects
     */
    public void testUnionOfUnions() throws Throwable {
        UnionOfUnionsDocument doc = UnionOfUnionsDocument.Factory.newInstance();
        doc.setUnionOfUnions("large");
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        UnionOfUnionsT elt = UnionOfUnionsT.Factory.newInstance();
        elt.setObjectValue(new Integer(-3));
        doc.xsetUnionOfUnions(elt);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc.setUnionOfUnions("addVal1");
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc.setUnionOfUnions("addVal2");
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc.setUnionOfUnions("addVal4");
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        // setting a value outside of the union should throw an exception as
        // type inside the Xmlobject is an enumeration and has a fixed number of constants in the type
        // This will fail irrespective of the setValidateOnSet() option
        boolean voeThrown = false;
        try
        {
            doc.setUnionOfUnions("foobar");

            assertTrue(!doc.validate(validateOptions));

            showErrors();
            String[] errExpected = new String[]{"cvc-attribute"};
                        assertTrue(compareErrorCodes(errExpected));
        }
        catch (XmlValueOutOfRangeException voe)
        {
            voeThrown = true;
        }

        finally
        {
            if(!voeThrown)
                fail("Expected XmlValueOutOfRangeException here");
        }


    }

    // for the above test (testUnionOfUnions), if the value set for the union type is AnyType (in the schema)
    // but the Java type defined as say Integer or Date then an Exception should be thrown only if
    // validateOnSet XmlOption is set and not otherwise.
    public void UnionOfUnions2()
    {
        UnionOfUnionsDocument doc = UnionOfUnionsDocument.Factory.newInstance();
        doc.setUnionOfUnions("4");

        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
        }

        // now validate with setValidateOnSetoption
        XmlOptions optWithValidateOnSet = new XmlOptions();
        optWithValidateOnSet.setValidateOnSet();

        UnionOfUnionsDocument doc2 = UnionOfUnionsDocument.Factory.newInstance(optWithValidateOnSet);
        boolean voeThrown = false;
        try {
            doc2.setUnionOfUnions("4");
        }
        catch (XmlValueOutOfRangeException voe) {
            voeThrown = true;
        }
        finally{
            if(!voeThrown)
                fail("Expected XmlValueOutOfRangeException..");
        }
    }

  /**
     * values allolwed here are either a list of (small, med, large, 1-3,-1,-2,-3}
     * or     (lstsmall, lstmed, lstlarge)
     */

    public void testUnionOfLists() throws Throwable {
        UnionOfListsDocument doc = UnionOfListsDocument.Factory.newInstance();
        List vals = new ArrayList();
        vals.add("small");
        vals.add(new Integer(-1));
        vals.add(new Integer(-2));
        vals.add(new Integer(-3));
        vals.add(new Integer(3));
        vals.add("medium");
        doc.setUnionOfLists(vals);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

        vals.clear();
        vals.add("lstsmall");
        vals.add("lstlarge");

        doc.setUnionOfLists(vals);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

        vals.clear();

        //mixing and matching should not be allowed
        //the list shoudl have exactly one of the 2 union types
        vals.add("lstsmall");
        vals.add(new Integer(-1));

        // if the type in a union and cannot be converted into any of the union types, and in this case
        // since the list have enumerations, an exception is expected irrespective of validateOnSet XmlOption
        // being set. Refer testUnionOfUnions comment also
        boolean voeThrown = false;
        try{
            doc.setUnionOfLists( vals );
        }
        catch (XmlValueOutOfRangeException voe){
            voeThrown = true;
        }
        finally{
            if(!voeThrown)
                fail("Expected XmlValueOutOfRangeException here");
        }

    }

}

