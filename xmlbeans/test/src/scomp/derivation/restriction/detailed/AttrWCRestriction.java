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
import xbean.scomp.derivation.attributeWCRestriction.*;
import org.apache.xmlbeans.XmlErrorCodes;

/**
 *
 */
public class AttrWCRestriction extends BaseCase {
    String input;
    /**
     * Replace a wildcard with a concrete attribute
     * No other attr should be valid here
    */
    public void testAny2Instance() throws Throwable{
         input="<foo:Any2Concrete " +
                " xmlns:foo=\"http://xbean/scomp/derivation/AttributeWCRestriction\"" +
                 " xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\"" +
                " at:testattribute=\"XBean\"/>";
         Any2ConcreteDocument doc=
                 Any2ConcreteDocument.Factory.parse(input);
        assertTrue ( doc.validate(validateOptions));

          input="<foo:Any2Concrete " +
                " xmlns:foo=\"http://xbean/scomp/derivation/AttributeWCRestriction\"" +
                 " xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\"" +
                " at:testatt=\"3\"/>";
          doc=
                 Any2ConcreteDocument.Factory.parse(input);
        assertTrue ( !doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NO_WILDCARD
        };
                     assertTrue(compareErrorCodes(errExpected));

    }

    /**
     * should be able to replace the any w/ local...
     * attrs not in target ns are therefore illegal
     * Skip to strict
     * No namespace should be OK, any other namespace should be notOK
     * @throws Throwable
     */
    public void testAny2LocalStrict() throws Throwable{
          input="<foo:Any2Local " +
                " xmlns:foo=\"http://xbean/scomp/derivation/AttributeWCRestriction\"" +
                " testattribute=\"XBean\"/>";
         Any2LocalDocument doc=
                 Any2LocalDocument.Factory.parse(input);
        try{
        assertTrue ( doc.validate(validateOptions));
        }catch(Throwable t){
            showErrors();
            throw t;
        }

        //a diff ns is not OK
         input="<foo:Any2Local " +
                " xmlns:foo=\"http://xbean/scomp/derivation/AttributeWCRestriction\"" +
                 " xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\"" +
                " at:testattribute=\"XBean\"/>";
          doc=
                 Any2LocalDocument.Factory.parse(input);
        assertTrue (! doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NOT_WILDCARD_VALID};
                     assertTrue(compareErrorCodes(errExpected));

    }

    /**
     * should be able to replace list of ns w/ subset
     * lax to strict
     * @throws Throwable
     */
    public void testList2SubsetStrict()throws Throwable{
          input="<foo:List2Subset " +
                " xmlns:foo=\"http://xbean/scomp/derivation/AttributeWCRestriction\"" +
                 " xmlns:at=\"http://ap.org\"" +
                " at:testattribute=\"XBean\"/>";
         List2SubsetDocument doc=
                 List2SubsetDocument.Factory.parse(input);
        //this is a non-existing NS...strict should complain
        assertTrue ( !doc.validate(validateOptions));
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NOT_WILDCARD_VALID};
                     assertTrue(compareErrorCodes(errExpected));


        //invalid case: give the attr a bad value, make sure it's being validated
    }
    /**
     * should be able to ##other w/ spec. namespaces
     * skip to lax
     * @throws Throwable
     */
    public void testOther2ListLax()throws Throwable{
          input="<foo:Other2List" +
                " xmlns:foo=\"http://xbean/scomp/derivation/AttributeWCRestriction\"" +
                 " xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\"" +
                " at:testattribute=\"XBean\"/>";
         Other2ListDocument doc=
                 Other2ListDocument.Factory.parse(input);
        assertTrue ( doc.validate(validateOptions));

        //invalid case: a ns not in the list

         input="<foo:Other2List" +
                " xmlns:foo=\"http://xbean/scomp/derivation/AttributeWCRestriction\"" +
                 " xmlns:at=\"http://foobar\"" +
                " at:testattribute=\"XBean\"/>";
          doc=
                 Other2ListDocument.Factory.parse(input);
        assertTrue (! doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NOT_WILDCARD_VALID};
                     assertTrue(compareErrorCodes(errExpected));

    }
}
