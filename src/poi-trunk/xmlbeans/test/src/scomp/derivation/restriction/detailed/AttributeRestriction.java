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

import xbean.scomp.derivation.attributeRestriction.AttrEltDocument;
import xbean.scomp.derivation.attributeRestriction.RestrictedAttrT;

import java.math.BigInteger;

import scomp.common.BaseCase;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.XmlErrorCodes;

/**
 *
 *
 */
public class AttributeRestriction extends BaseCase {
   /**
    * A should be positive
    * B should be there by default
    * @throws Throwable
    */
    public void testAttributeABC() throws Throwable {
        AttrEltDocument doc = AttrEltDocument.Factory.newInstance();
        RestrictedAttrT elt = doc.addNewAttrElt();
        elt.setA(new BigInteger("-3"));
        assertTrue(!doc.validate(validateOptions));
        showErrors();
       String[] errExpected = new String[]{
           "cvc-attribute",
           XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_REQUIRED_ATTRIBUTE
       };
//                    assertTrue(compareErrorCodes(errExpected));

        assertEquals("b", elt.getB());
       XmlString expected=XmlString.Factory.newInstance();
       expected.setStringValue("c2");
        assertTrue ( expected.valueEquals(elt.xgetC()) );
    }

    public void testAttributeDEF() throws Throwable {
        AttrEltDocument doc = AttrEltDocument.Factory.newInstance();
        RestrictedAttrT elt = doc.addNewAttrElt();
        XmlString expected=XmlString.Factory.newInstance();
        expected.setStringValue("a");
        elt.xsetD(expected);
        assertEquals("a", elt.getD());
        assertTrue(!doc.validate(validateOptions));
       // showErrors();
        //D invalid, F missing
        String[] errExpected = new String[]{
            XmlErrorCodes.ATTR_LOCALLY_VALID$FIXED,
              XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_REQUIRED_ATTRIBUTE
        };
        assertTrue(compareErrorCodes(errExpected));

        elt.setD("d");
        elt.setE("e");
        elt.setF("foobar");
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }
  /**
   * G is prohibited, X can appear even though not explicit in type
   * @throws Throwable
   */
    public void testAttributeGX() throws Throwable {
        AttrEltDocument doc = AttrEltDocument.Factory.newInstance();
        RestrictedAttrT elt = doc.addNewAttrElt();
        elt.setG("foobar");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
      //g prohibited, f missing
      String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NO_WILDCARD,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_REQUIRED_ATTRIBUTE
      };
                   assertTrue(compareErrorCodes(errExpected));

        elt.setX("myval");
        elt.unsetG();
        elt.setF("foobar");
         try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

    }
}
