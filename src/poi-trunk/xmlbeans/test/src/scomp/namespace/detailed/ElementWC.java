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
import xbean.scomp.namespace.elementWC.*;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.*;

/**
 */
//TODO: no test on minOccurs maxOccurs here
public class ElementWC extends BaseCase {
    public void testAnyLaxLegal() throws Throwable {
        AnyLaxDocument doc = AnyLaxDocument.Factory
                .parse("<AnyLax " +
                "xmlns=\"http://xbean/scomp/namespace/ElementWC\" " +
                "xmlns:foobar=\"http://foo\">" +
                "<foobar:child/></AnyLax>");
        assertTrue(doc.validate(validateOptions));
    }

    /**
     * Is it possible to have an illegal LAX/Any--think not
     *
     * @throws Throwable

    public void testAnyLaxIllegal() throws Throwable {
        AnyLaxDocument doc = AnyLaxDocument.Factory
                .parse("<AnyLax " +
                "xmlns=\"http://xbean/scomp/namespace/ElementWC\" >" +
                "<child/></AnyLax>");
        assertTrue(!doc.validate(validateOptions));
        String[] errExpected = new String[]{"cvc-attribute"};
        assertTrue(compareErrorCodes(errExpected));

    }
      */

    public void testAnySkipLegal() throws Throwable {
        AnySkipDocument doc = AnySkipDocument.Factory
                .parse("<AnySkip " +
                "xmlns=\"http://xbean/scomp/namespace/ElementWC\" " +
                "xmlns:foobar=\"http://foo\">" +
                "<foobar:child/></AnySkip>");
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
                " xmlns:ns=\"http://xbean/scomp/namespace/ElementWC\"" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                " xmlns:elt=\"http://xbean/scomp/element/GlobalEltDefault\"" +
                // " xsi:schemaLocation=\"http://xbean/scomp/element/GlobalEltDefault " +
                //  "GlobalEltDefault.xsd\"
                "> " +
                "<elt:GlobalEltDefaultStr/></ns:AnyStrict>");
        if (!doc.validate(validateOptions))
            showErrors();
        assertTrue(doc.validate(validateOptions));
        XmlObject[] arr=doc.getAnyStrict()
                .selectChildren(new QName(
                        "http://xbean/scomp/element/GlobalEltDefault",
                        "GlobalEltDefaultStr","elt"));
        assertEquals(arr[0].schemaType(),XmlString.type);
    }

    public void testAnyStrictIllegal() throws Throwable {
        AnyStrictDocument doc = AnyStrictDocument.Factory
                .parse("<AnyStrict " +
                "xmlns=\"http://xbean/scomp/namespace/ElementWC\" " +
                "xmlns:foobar=\"http://foo\">" +
                "<foobar:child/></AnyStrict>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ASSESS_ELEM_SCHEMA_VALID$NOT_RESOLVED
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testOtherLaxLegal() throws Throwable {
        OtherLaxDocument doc = OtherLaxDocument.Factory
                .parse("<foo:OtherLax " +
                "xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\" " +
                "xmlns:foobar=\"http:apache.org\" >" +
                "<foobar:child/></foo:OtherLax>");
        assertTrue(doc.validate(validateOptions));
    }

    //can not be in target NS
    //cannot be in noNS
    public void testOtherLaxIllegal() throws Throwable {
        OtherLaxDocument doc = OtherLaxDocument.Factory
                .parse("<foo:OtherLax " +
                "xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"> " +
                "<foo:child/></foo:OtherLax>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        doc = OtherLaxDocument.Factory
                .parse("<foo:OtherLax " +
                "xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"> " +
                "<child/></foo:OtherLax>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
    }

    public void testOtherSkipLegal() throws Throwable {
        OtherSkipDocument doc = OtherSkipDocument.Factory
                .parse("<foo:OtherSkip " +
                "xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"" +
                "  xmlns:elt=\"http://xbean/scomp/attribute/GlobalEltDefault\">" +
                "<elt:child/></foo:OtherSkip>");
        assertTrue(doc.validate(validateOptions));
    }

    //no ns not allowed by the wc
    public void testOtherSkipIllegal() throws Throwable {
        OtherSkipDocument doc = OtherSkipDocument.Factory
                .parse("<foo:OtherSkip " +
                "xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\" >" +
                "<child/></foo:OtherSkip>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
    }
    //"http://xbean/scomp/element/GlobalEltDefault"
    public void testOtherStrictLegal() throws Throwable {
        OtherStrictDocument doc = OtherStrictDocument.Factory
                .parse("<foo:OtherStrict xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"" +
                "  xmlns:elt=\"http://xbean/scomp/element/GlobalEltDefault\">" +
                "<elt:GlobalEltDefaultStr/></foo:OtherStrict>");
        if (!doc.validate(validateOptions))
            showErrors();
        assertTrue(doc.validate(validateOptions));

    }

    public void testOtherStrictIllegal() throws Throwable {
        OtherStrictDocument doc = OtherStrictDocument.Factory
                .parse("<foo:OtherStrict " +
                "xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                " xmlns:elt=\"http://xbean/scomp/element/GlobalEltDefault\"" +
                " xsi:schemaLocation=\"http://xbean/scomp/element/GlobalEltDefault.xsd\"> " +
                "<elt:GlobalEltDefaultInt> foo " +
                "</elt:GlobalEltDefaultInt>" +
                "</foo:OtherStrict>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
    }

    //no declaration for this attr, no error on Lax
    public void testListLaxLegal() throws Throwable {
        ListLaxDocument doc = ListLaxDocument.Factory
                .parse("<foo:ListLax " +
                " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"" +
                "  xmlns:at=\"http://apache.org\">" +
                " <at:child/></foo:ListLax>");
        assertTrue(doc.validate(validateOptions));
    }

    public void testListLaxIllegal() throws Throwable {
        ListLaxDocument doc = ListLaxDocument.Factory
                .parse("<foo:ListLax " +
                " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"" +
                "  xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\">" +
                " <at:child/></foo:ListLax>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
    }


    public void testListSkipLegal() throws Throwable {
        ListSkipDocument doc = ListSkipDocument.Factory
                .parse("<foo:ListSkip " +
                " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"" +
                "  xmlns:at=\"http://apache.org\">" +
                " <at:child/></foo:ListSkip>");
        assertTrue(doc.validate(validateOptions));
    }

    public void testListSkipIllegal() throws Throwable {
        ListSkipDocument doc = ListSkipDocument.Factory
                .parse("<foo:ListSkip " +
                " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"" +
                "  xmlns:at=\"http://apache_org.org\">" +
                " <at:child/></foo:ListSkip>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
    }

    public void testListStrictLegal() throws Throwable {
        ListStrictDocument doc = ListStrictDocument.Factory
                .parse("<foo:ListStrict " +
                 "xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"" +
                " xmlns:elt=\"http://xbean/scomp/element/GlobalEltDefault\">" +
                " <elt:GlobalEltDefaultInt/>" +
                "</foo:ListStrict>");
        if (!doc.validate(validateOptions)) {
            showErrors();
            fail("testFailed");
        }

    }

   //element will not be found
    public void testListStrictIllegal() throws Throwable {
        ListStrictDocument doc = ListStrictDocument.Factory
                .parse("<foo:ListStrict " +
                " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"" +
                "  xmlns:at=\"http://apache.org\">" +
                " <at:child/></foo:ListStrict>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_ELEMENT
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    //replacement elements MUST be in the
    //  current target NS
    public void testTargetLaxLegal() throws Throwable {
        TargetLaxDocument doc = TargetLaxDocument.Factory
                .parse("<foo:TargetLax" +
                " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
                "<foo:LocalElt>2</foo:LocalElt></foo:TargetLax>");
        if (!doc.validate(validateOptions))
            showErrors();
        assertTrue(doc.validate(validateOptions));
        XmlObject[] arr=doc.getTargetLax().selectChildren("http://xbean/scomp/namespace/ElementWC","LocalElt");
        assertEquals(arr[0].schemaType(),XmlInt.type);
    }

    //no such element in the NS
    public void testTargetLaxIllegal() throws Throwable {
        TargetLaxDocument doc = TargetLaxDocument.Factory
                .parse("<foo:TargetLax " +
                " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"" +
                "  xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\">" +
                " <at:child/></foo:TargetLax>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
              XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_ELEMENT
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testTargetSkipLegal() throws Throwable {
        TargetSkipDocument doc = TargetSkipDocument.Factory
                .parse("<foo:TargetSkip " +
                " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
                " <foo:child/></foo:TargetSkip>");
        assertTrue(doc.validate(validateOptions));
    }

    /**
     * can a test ever be illegal here?
     */
    public void testTargetSkipIllegal() throws Throwable {
        TargetSkipDocument doc = TargetSkipDocument.Factory
                .parse("<foo:TargetSkip " +
                " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
                " <child/></foo:TargetSkip>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_ELEMENT
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testTargetStrictLegal() throws Throwable {
        TargetStrictDocument doc = TargetStrictDocument.Factory
                .parse("<foo:TargetStrict " +
                " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
                "<foo:LocalElt>2</foo:LocalElt></foo:TargetStrict>");
        try{
        assertTrue(doc.validate(validateOptions));
        }catch(Throwable t){
                showErrors();
                throw t;
            }
    }

    public void testTargetStrictIllegal() throws Throwable {
        TargetStrictDocument doc = TargetStrictDocument.Factory
                .parse("<foo:TargetStrict " +
                " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
                " <foo:child/></foo:TargetStrict>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ASSESS_ELEM_SCHEMA_VALID$NOT_RESOLVED
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testLocalLaxLegal() throws Throwable {
        LocalLaxDocument doc = LocalLaxDocument.Factory
                .parse("<foo:LocalLax " +
                " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
                " <child/></foo:LocalLax>");
        assertTrue(doc.validate(validateOptions));
    }

    //no such child in current NS
    public void testLocalLaxIllegal() throws Throwable {
        LocalLaxDocument doc = LocalLaxDocument.Factory
                .parse("<foo:LocalLax " +
                " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
                " <foo:child/></foo:LocalLax>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
              XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_ELEMENT
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testLocalSkipLegal() throws Throwable {
        LocalSkipDocument doc = LocalSkipDocument.Factory
                .parse("<foo:LocalSkip " +
                " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
                " <child/></foo:LocalSkip>");
        assertTrue(doc.validate(validateOptions));
    }

    /**
     * can a test ever be illegal here?

    public void testLocalSkipIllegal() throws Throwable {
        LocalSkipDocument doc = LocalSkipDocument.Factory
                .parse("<foo:LocalSkip " +
                " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
                " <child/></foo:LocalSkip>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{"cvc-attribute"};
        assertTrue(compareErrorCodes(errExpected));

    }
      */
    public void testLocalStrictIllegal() throws Throwable {
        LocalStrictDocument doc = LocalStrictDocument.Factory
                .parse("<foo:LocalStrict " +
                " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
                " <child/></foo:LocalStrict>");
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ASSESS_ELEM_SCHEMA_VALID$NOT_RESOLVED
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testLocalStrictLegal() throws Throwable {
        LocalStrictDocument doc = LocalStrictDocument.Factory
                .parse("<foo:LocalStrict " +
                " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
                "<NoNSElt>2</NoNSElt></foo:LocalStrict>");
        if (!doc.validate(validateOptions)) {
            showErrors();
            fail("test failed");
        }
    }
}

