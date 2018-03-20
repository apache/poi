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

package scomp.namespace.detailed;

import scomp.common.BaseCase;
import xbean.scomp.namespace.attributeWC.*;
import org.apache.xmlbeans.XmlErrorCodes;

/**
 *
 *
 * 
 */
public class AttributeWC extends BaseCase {
    public void testAnyLaxLegal() throws Throwable {
        AnyLaxDocument doc = AnyLaxDocument.Factory.parse("<AnyLax xmlns=\"http://xbean/scomp/namespace/AttributeWC\" " +
                "attr1=\"val1\"/>");
        assertTrue(doc.validate(validateOptions));
    }

    public void testAnyLaxIllegal() throws Throwable {
        AnyLaxDocument doc = AnyLaxDocument.Factory.parse("<AnyLax xmlns=\"http://xbean/scomp/namespace/AttributeWC\" " +
                "attr1=\"val1\"/>");
        assertTrue(doc.validate(validateOptions));
    }

    public void testAnySkipLegal() throws Throwable {
        AnySkipDocument doc = AnySkipDocument.Factory.parse("<AnySkip xmlns=\"http://xbean/scomp/namespace/AttributeWC\" " +
                "attr1=\"val1\"/>");
        assertTrue(doc.validate(validateOptions));
    }

    /**
     * Everything is legal here
     * public void testAnySkipIllegal() throws Throwable {
     * }
     */
    //no NS is legal too
    public void testAnyStrictLegal() throws Throwable {
        AnyStrictDocument doc = AnyStrictDocument.Factory
                .parse("<ns:AnyStrict" +
                " xmlns:ns=\"http://xbean/scomp/namespace/AttributeWC\" " +
                "  xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\" " +
                "at:testattribute=\"val1\"/>");
        assertTrue(doc.validate(validateOptions));
    }

    public void testAnyStrictIllegal() throws Throwable {
        AnyStrictDocument doc = AnyStrictDocument.Factory.parse("<AnyStrict xmlns=\"http://xbean/scomp/namespace/AttributeWC\" " +
                "attr1=\"val1\"/>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ASSESS_ATTR_SCHEMA_VALID$NOT_RESOLVED
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testOtherLaxLegal() throws Throwable {
        OtherLaxDocument doc = OtherLaxDocument.Factory
                .parse("<foo:OtherLax " +
                "xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\" " +
                "xmlns:foobar=\"http:apache.org\" " +
                "foobar:attr1=\"val1\"/>");
        assertTrue(doc.validate(validateOptions));
    }

    //can not be in target NS
    //cannot be in noNS
    public void testOtherLaxIllegal() throws Throwable {
        OtherLaxDocument doc = OtherLaxDocument.Factory
                .parse("<foo:OtherLax xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\" " +
                " foo:attr1=\"val1\"/>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
             XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NOT_WILDCARD_VALID
        };
        assertTrue(compareErrorCodes(errExpected));

        doc = OtherLaxDocument.Factory
                .parse("<foo:OtherLax xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\" " +
                "attr1=\"val1\"/>");
         clearErrors();
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        errExpected = new String[]{
             XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NOT_WILDCARD_VALID
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testOtherSkipLegal() throws Throwable {
        OtherSkipDocument doc = OtherSkipDocument.Factory
                .parse("<foo:OtherSkip xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\" " +
                "xmlns:foobar=\"http:apache.org\" " +
                "foobar:attr1=\"val1\"/>");
        assertTrue(doc.validate(validateOptions));
    }

    //ns not allowed by the wc
    public void testOtherSkipIllegal() throws Throwable {
        OtherSkipDocument doc = OtherSkipDocument.Factory
                .parse("<foo:OtherSkip xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\" " +
                " foo:attr1=\"val1\"/>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NOT_WILDCARD_VALID
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testOtherStrictLegal() throws Throwable {
        OtherStrictDocument doc = OtherStrictDocument.Factory
                .parse("<foo:OtherStrict xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
                "  xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\"" +
                " at:testattribute=\"val1\"/>");
        assertTrue(doc.validate(validateOptions));

    }

    public void testOtherStrictIllegal() throws Throwable {
        OtherStrictDocument doc = OtherStrictDocument.Factory
                .parse("<foo:OtherStrict xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
                "  xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\"" +
                " at:test=\"val1\"/>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ASSESS_ATTR_SCHEMA_VALID$NOT_RESOLVED
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    //no declaration for this attr, no error on Lax
    public void testListLaxLegal() throws Throwable {
        ListLaxDocument doc = ListLaxDocument.Factory
                .parse("<foo:ListLax " +
                " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
                "  xmlns:at=\"http://apache.org\"" +
                " at:testattribute=\"val1\"/>");
        assertTrue(doc.validate(validateOptions));
    }

    public void testListLaxIllegal() throws Throwable {
        ListLaxDocument doc = ListLaxDocument.Factory.parse("<foo:ListLax " +
                " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
                "  xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\"" +
                " at:test=\"val1\"/>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NOT_WILDCARD_VALID
        };
        assertTrue(compareErrorCodes(errExpected));

    }


    public void testListSkipLegal() throws Throwable {
        ListSkipDocument doc = ListSkipDocument.Factory
                .parse("<foo:ListSkip " +
                " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
                "  xmlns:at=\"http://apache.org\"" +
                " at:testattribute=\"val1\"/>");
        assertTrue(doc.validate(validateOptions));
    }

    public void testListSkipIllegal() throws Throwable {
        ListSkipDocument doc = ListSkipDocument.Factory
                .parse("<foo:ListSkip " +
                " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
                "  xmlns:at=\"http://apache_org.org\"" +
                " at:testattribute=\"val1\"/>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NOT_WILDCARD_VALID
        };
        assertTrue(compareErrorCodes(errExpected));

    }

   //  " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrDefault\"
    public void testListStrictLegal() throws Throwable {
        ListStrictDocument doc = ListStrictDocument.Factory
                .parse("<foo:ListStrict " +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
                "  xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\"" +
                " at:testattribute=\"val1\"/>");
        if (!doc.validate(validateOptions)) {
            showErrors();
            fail("testFailed");
        }

    }

    public void testListStrictIllegal() throws Throwable {
        ListStrictDocument doc = ListStrictDocument.Factory
                .parse("<foo:ListStrict " +
                " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
                "  xmlns:at=\"http://apache.org\"" +
                " at:testattribute=\"val1\"/>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
             XmlErrorCodes.ASSESS_ATTR_SCHEMA_VALID$NOT_RESOLVED
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testTargetLaxLegal() throws Throwable {
        TargetLaxDocument doc = TargetLaxDocument.Factory
                .parse("<foo:TargetLax " +
                " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
                " foo:testattribute=\"val1\"/>");
        assertTrue(doc.validate(validateOptions));
    }

    public void testTargetLaxIllegal() throws Throwable {
        TargetLaxDocument doc = TargetLaxDocument.Factory
                .parse("<foo:TargetLax " +
                " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
                "  xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\"" +
                " at:testattributeInt=\"foo\"/>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
              XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NOT_WILDCARD_VALID
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testTargetSkipLegal() throws Throwable {
        TargetSkipDocument doc = TargetSkipDocument.Factory
                .parse("<foo:TargetSkip " +
                " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
                " foo:undeclAttr=\"val1\"/>");
        assertTrue(doc.validate(validateOptions));
    }

    /**
     * can a test ever be illegal here?

    public void testTargetSkipIllegal() throws Throwable {
        TargetSkipDocument doc = TargetSkipDocument.Factory
                .parse("<foo:TargetSkip " +
                " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
                " foo:undeclAttr=\"val1\"/>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{"cvc-attribute"};
        assertTrue(compareErrorCodes(errExpected));

    }
     */

    public void testTargetStrictLegal() throws Throwable {
        TargetStrictDocument doc = TargetStrictDocument.Factory
                .parse("<foo:TargetStrict " +
                " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
                " foo:LocalAttr=\"3\"/>");
        assertTrue(doc.validate(validateOptions));
    }

    public void testTargetStrictIllegal() throws Throwable {
        TargetStrictDocument doc = TargetStrictDocument.Factory
                .parse("<foo:TargetStrict " +
                " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
                " foo:LocalAttr=\"foo\"/>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.DECIMAL
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testLocalLaxLegal() throws Throwable {
        LocalLaxDocument doc = LocalLaxDocument.Factory
                .parse("<foo:LocalLax " +
                " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
                " undeclAttr=\"val1\"/>");
        assertTrue(doc.validate(validateOptions));
    }

    public void testLocalLaxIllegal() throws Throwable {
        LocalLaxDocument doc = LocalLaxDocument.Factory
                .parse("<foo:LocalLax " +
                " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
                " foo:undeclAttr=\"val1\"/>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NOT_WILDCARD_VALID
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testLocalSkipLegal() throws Throwable {
        LocalSkipDocument doc = LocalSkipDocument.Factory
                .parse("<foo:LocalSkip " +
                " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
                " undeclAttr=\"val1\"/>");
        assertTrue(doc.validate(validateOptions));
    }

    /**
     * can a test ever be illegal here?

    public void testLocalSkipIllegal() throws Throwable {
        LocalSkipDocument doc = LocalSkipDocument.Factory
                .parse("<foo:LocalSkip " +
                " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
                " undeclAttr=\"val1\"/>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{"cvc-attribute"};
        assertTrue(compareErrorCodes(errExpected));

    }
    */
    public void testLocalStrictIllegal() throws Throwable {
        LocalStrictDocument doc = LocalStrictDocument.Factory
                .parse("<foo:LocalStrict " +
                " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
                " undeclAttr=\"val1\"/>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
              XmlErrorCodes.ASSESS_ATTR_SCHEMA_VALID$NOT_RESOLVED
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testLocalStrictLegal() throws Throwable {
        LocalStrictDocument doc = LocalStrictDocument.Factory
                .parse("<foo:LocalStrict " +
                " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
                " NoNSAttr=\"2\"/>");
        if (!doc.validate(validateOptions)) {
            showErrors();
            fail("test failed");
        }
    }
}
