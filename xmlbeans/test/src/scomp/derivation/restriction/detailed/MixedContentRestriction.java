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

import xbean.scomp.derivation.mixedContentRestriction.*;
import scomp.common.BaseCase;

import java.math.BigInteger;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.impl.values.XmlValueNotSupportedException;

/**
 *
 */
public class MixedContentRestriction extends BaseCase{

    public void testRestrictedMixed() throws Throwable{
        MixedEltDocument doc=MixedEltDocument.Factory.newInstance();
        RestrictedMixedT elt=doc.addNewMixedElt();
        assertTrue( !elt.isSetChild1());
        elt.setChild1(new BigInteger("10"));
        elt.setChild2(BigInteger.ZERO);
        //insert text b/n the 2 elements
        XmlCursor cur=elt.newCursor();
        cur.toFirstContentToken();
        assertTrue(cur.toNextSibling());
        cur.insertChars("My chars");
          try {
            assertTrue( doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        assertEquals("<xml-fragment>" +
                "<child1>10</child1>My chars<child2>0</child2>" +
                "</xml-fragment>", elt.xmlText());

    }
    public void testRestrictedEltOnly() throws Throwable{
       ElementOnlyEltDocument doc=ElementOnlyEltDocument.Factory.newInstance();
        RestrictedEltT elt=doc.addNewElementOnlyElt();
        assertTrue( !elt.isSetChild1());
        elt.setChild1(new BigInteger("10"));
        elt.setChild2(BigInteger.ZERO);
        //insert text b/n the 2 elements
        XmlCursor cur=elt.newCursor();
       cur.toFirstContentToken();
        assertTrue(cur.toNextSibling());
        cur.insertChars("My chars");
        assertTrue( !doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_ONLY_WITH_TEXT};
                            assertTrue(compareErrorCodes(errExpected));

        //should be valid w/o the Text there
        cur.toPrevToken();
         assertEquals("<xml-fragment>" +
                "<child1>10</child1>My chars<child2>0</child2>" +
                "</xml-fragment>", elt.xmlText());
       assertTrue(cur.removeXml());
        try {
            assertTrue( doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        assertEquals("<xml-fragment>" +
                "<child1>10</child1><child2>0</child2>" +
                "</xml-fragment>", elt.xmlText());


    }
    //seems that this is not a valid example p.329 top
    //public void testRestrictedMixedToSimple() throws Throwable{}
    public void testRestrictedMixedToEmpty() throws Throwable{
         Mixed2EmptyEltDocument doc=Mixed2EmptyEltDocument.Factory.newInstance();
         Mixed2EmptyT elt=doc.addNewMixed2EmptyElt();
        assertEquals(null,elt.xgetChild1());

        // ok this gets a little tricky. Due to the restriction extension, the setter method is now
        // 'removed'. So the schema is actually an XmlAnyType while the method sets it to a BigInteger.
        // This will fail irrespective of the setValidateOnset XmlOption
        boolean vneThrown = false;
        try
        {
        elt.setChild1(new BigInteger("10"));
         assertTrue( !doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{"cvc-attribute"};
                            assertTrue(compareErrorCodes(errExpected));
        }

        catch (XmlValueNotSupportedException vns) {
            vneThrown = true;
        }
        finally {
            if(!vneThrown)
                fail("Expected XmlValueNotSupportedException here");
        }

    }
}
