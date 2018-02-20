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
package scomp.attributes.detailed;

import scomp.common.BaseCase;
import xbean.scomp.attribute.localAttrFixedDefault.LocalAttrFixedDefaultT;
import xbean.scomp.attribute.localAttrFixedDefault.LocalAttrFixedDefaultDocument;
import org.apache.xmlbeans.XmlErrorCodes;

/**
 *
 *
 *
 */
public class LocalAttrFixedDefault extends BaseCase {
    /**
     * Verify that a local attribute can add a fixed value but can not overwrite
     * an existing fixed val
     * inspired by Walmsley 13.6.2
     */
    //ensure default val is shadowed locally
    //fixed can not be...
     public void testDefault() throws Throwable{
         LocalAttrFixedDefaultT testDoc =
            LocalAttrFixedDefaultDocument.Factory.newInstance()
                 .addNewLocalAttrFixedDefault();
         assertTrue(testDoc.validate());
        assertEquals(2,testDoc.getAttDefault().intValue());
        //second fixed value is ignored
        testDoc.setAttFixed("NEWXBeanAttrStr");
        assertTrue(!testDoc.validate(validateOptions));
        showErrors();
               String[] errExpected = new String[]{
                   XmlErrorCodes.ATTR_LOCALLY_VALID$FIXED             
               };
                    assertTrue(compareErrorCodes(errExpected));


        testDoc.setAttFixed("XBeanAttrStr");
          try {
            assertTrue(testDoc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

    }
}
