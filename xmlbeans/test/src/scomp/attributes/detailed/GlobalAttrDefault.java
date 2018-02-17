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

import xbean.scomp.attribute.globalAttrDefault.GlobalAttrDefaultDocDocument;
import xbean.scomp.attribute.globalAttrDefault.GlobalAttrDefaultT;
import scomp.common.BaseCase;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlErrorCodes;

/**
 *
 *
 *
 */
public class GlobalAttrDefault extends BaseCase {
    /**
     * If value is missing default should appear
     */
    public void testMissing() {
        GlobalAttrDefaultT testDoc =
                GlobalAttrDefaultDocDocument.Factory.newInstance()
                .addNewGlobalAttrDefaultDoc();
        assertEquals("XBeanAttr", testDoc.getTestattribute());
    }

    /**
     * Test val preservation
     */
    public void testPresent() {
        GlobalAttrDefaultT testDoc =
                GlobalAttrDefaultDocDocument.Factory.newInstance()
                .addNewGlobalAttrDefaultDoc();
        testDoc.setTestattribute("Existing");
        assertEquals("Existing", testDoc.getTestattribute());
    }

    /**
     * Test empty string: should be preserved
     */
    public void testPresentEmpty() throws Throwable {
        GlobalAttrDefaultT testDoc =
                GlobalAttrDefaultDocDocument.Factory.parse("<pre:GlobalAttrDefaultDoc" +
                " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrDefault\" " +
                " pre:testattribute=\"\"/>").getGlobalAttrDefaultDoc();
        assertEquals("", testDoc.getTestattribute());
        try {
                  assertTrue(testDoc.validate(validateOptions));
              }
              catch (Throwable t) {
                  showErrors();
                  throw t;
              }

    }


    /**
     * Type mismatch
     */
    public void testBadType() throws XmlException {
        GlobalAttrDefaultT testDoc =
                GlobalAttrDefaultDocDocument.Factory.parse("<pre:GlobalAttrDefaultDoc" +
                " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrDefault\" " +
                "pre:testattributeInt=\"\"/>").getGlobalAttrDefaultDoc();
        String[] errExpected=new String[]{
            XmlErrorCodes.DECIMAL
        };
        assertTrue(!testDoc.validate(validateOptions));
        assertEquals(1, errorList.size());
        showErrors();
        assertTrue(compareErrorCodes(errExpected));


    }


}
