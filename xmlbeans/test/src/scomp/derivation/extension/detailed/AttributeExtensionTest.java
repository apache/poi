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
import xbean.scomp.derivation.attributeExtension.ExtendedT;
import xbean.scomp.derivation.attributeExtension.ExtendedElementDocument;

import java.math.BigInteger;

/**
 *
 *
 *
 */
public class AttributeExtensionTest extends BaseCase{
    /**
     * Attribute w/ same LN but diff NS in base type
     * Other scenarious are compile time errors
     */
    public void testAttribute()throws Throwable{
       ExtendedElementDocument doc=ExtendedElementDocument.Factory.newInstance();
        ExtendedT elt=doc.addNewExtendedElement();
         try{
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
           throw t;
        }
        elt.setTestattribute("foo");
        elt.setTestattribute2("bar");
        elt.setTestattributeInt(new BigInteger("10"));
          try{
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
           throw t;
        }
        //make sure attr w/ value foo is in the imported NS
       assertEquals("<att:ExtendedElement glob:testattribute=\"foo\" " +
               "testattribute=\"bar\" glob:testattributeInt=\"10\" " +
               "xmlns:att=\"http://xbean/scomp/derivation/AttributeExtension\" " +
               "xmlns:glob=\"http://xbean/scomp/attribute/GlobalAttrDefault\"/>"
               ,doc.xmlText());
    }

}
