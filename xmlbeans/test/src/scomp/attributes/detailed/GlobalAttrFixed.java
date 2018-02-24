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


import xbean.scomp.attribute.globalAttrFixed.GlobalAttrFixedDocDocument;
import xbean.scomp.attribute.globalAttrFixed.TestattributeStrAttribute;
import xbean.scomp.attribute.globalAttrFixed.TestattributeIntAttribute;
import xbean.scomp.attribute.globalAttrFixed.GlobalAttrFixedT;
import scomp.common.BaseCase;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlErrorCodes;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 *
 *
 *
 */
public class GlobalAttrFixed extends BaseCase {

    /**
     * Missing OK
     */
    public void testValidMissing() throws Exception {
        GlobalAttrFixedT testDoc =
                GlobalAttrFixedDocDocument.Factory.parse("<pre:GlobalAttrFixedDoc" +
                " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrFixed\" " +
                "/>").getGlobalAttrFixedDoc();
        assertTrue(testDoc.validate());

    }

    public void testBothValid() throws Throwable {
        GlobalAttrFixedT testDoc =
                GlobalAttrFixedDocDocument.Factory.parse("<pre:GlobalAttrFixedDoc" +
                " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrFixed\" " +
                "pre:testattributeStr=\"XBeanAttrStr\" " +
                "pre:testattributeInt=\" 1 \"/>").getGlobalAttrFixedDoc();
        try {
            assertTrue(testDoc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }

        assertEquals("XBeanAttrStr", testDoc.getTestattributeStr());
        assertEquals(1, testDoc.getTestattributeInt().intValue());
    }

    /**
     * value does not match fixed
     */
    public void testStringInvalidSpace() throws Exception {
        GlobalAttrFixedT testAtt =
                GlobalAttrFixedDocDocument.Factory.parse("<pre:GlobalAttrFixedDoc" +
                " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrFixed\" " +
                "pre:testattributeStr=\" XBeanAttrStr \"/>").getGlobalAttrFixedDoc();
        String[] errExpected = new String[]{
             XmlErrorCodes.ATTR_LOCALLY_VALID$FIXED
        };
        assertTrue(!testAtt.validate(validateOptions));
        assertEquals(1, errorList.size());
        showErrors();
        assertTrue(compareErrorCodes(errExpected));


        //catch XmlExceptionHere;
    }

    public void testStringInvalidValue() throws Exception {
        GlobalAttrFixedT testAtt =
                GlobalAttrFixedDocDocument.Factory.parse("<pre:GlobalAttrFixedDoc" +
                " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrFixed\" " +
                "pre:testattributeStr=\" foobar \" />").getGlobalAttrFixedDoc();
        String[] errExpected = new String[]{
            XmlErrorCodes.ATTR_LOCALLY_VALID$FIXED
        };
        assertTrue(!testAtt.validate(validateOptions));
        assertEquals(1, errorList.size());
        showErrors();
        assertTrue(compareErrorCodes(errExpected));


    }

    /**
     * Test empty string: should be preserved
     */
    public void testIntInvalidType() throws XmlException {
        GlobalAttrFixedT testAtt =
                GlobalAttrFixedDocDocument.Factory.parse("<pre:GlobalAttrFixedDoc" +
                " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrFixed\" " +
                "pre:testattributeInt=\" foo \"/>").getGlobalAttrFixedDoc();

        assertTrue(!testAtt.validate(validateOptions));
        assertEquals(1, errorList.size());
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.DECIMAL
        };
        assertTrue(compareErrorCodes(errExpected));


    }

    public void testIntInvalidValue() throws XmlException {
        GlobalAttrFixedT testAtt =
                GlobalAttrFixedDocDocument.Factory.parse("<pre:GlobalAttrFixedDoc" +
                " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrFixed\" " +
                "pre:testattributeInt=\" 4 \"/>").getGlobalAttrFixedDoc();
        assertTrue(!testAtt.validate(validateOptions));
        assertEquals(1, errorList.size());
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ATTR_LOCALLY_VALID$FIXED
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testIntValidValue() throws Throwable {
        GlobalAttrFixedT testAtt =
                GlobalAttrFixedDocDocument.Factory.parse("<pre:GlobalAttrFixedDoc" +
                " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrFixed\" " +
                "pre:testattributeInt=\" +01 \"/>").getGlobalAttrFixedDoc();
        try {
            assertTrue(testAtt.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }

        //catch XmlExceptionHere;
    }

    public void testSetValue() throws XmlException {
        GlobalAttrFixedDocDocument testAtt =
                GlobalAttrFixedDocDocument.Factory.newInstance();
        GlobalAttrFixedT testDoc = testAtt.addNewGlobalAttrFixedDoc();
        testDoc.setTestattributeInt(new BigInteger("5"));
        //shouldn't this fail?
        assertEquals(5, testDoc.getTestattributeInt().intValue());
        assertTrue(!testDoc.validate(validateOptions));
        assertEquals(1, errorList.size());
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ATTR_LOCALLY_VALID$FIXED};
        assertTrue(compareErrorCodes(errExpected));

    }

}
