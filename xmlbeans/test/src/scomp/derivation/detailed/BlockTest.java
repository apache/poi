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
package scomp.derivation.detailed;

import xbean.scomp.derivation.finalBlockDefault.EltNoBlockDocument;
import xbean.scomp.derivation.finalBlockDefault.EltDefaultBlockDocument;
import xbean.scomp.derivation.block.*;
import scomp.common.BaseCase;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlErrorCodes;

/**
 *
 *
 *
 */
public class BlockTest extends BaseCase {
    String restrContentValid = "<name>Bobby</name><age>20</age>";
    String restrContentInvalid = "<name>Bobby</name><age>40</age>";
    String extContent = "<name>Bobby</name><age>40</age><gender>f</gender>";

    public String getInstance(String elt,
                              String type,
                              boolean ext,
                              boolean valid) {
        StringBuffer sb = new StringBuffer();
        sb.append("<ns:" + elt +
                "  xmlns:ns=\"http://xbean/scomp/derivation/Block\"");
        sb.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        sb.append(" xsi:type=\"ns:" + type + "\">");
        if (ext)
            sb.append(extContent);
        else if (valid)
            sb.append(restrContentValid);
        else
            sb.append(restrContentInvalid);

        sb.append("</ns:" + elt + ">");
        return sb.toString();

    }

    public String getInstanceDefault(String elt, String type, boolean ext,
                                     boolean valid) {
        StringBuffer sb = new StringBuffer();
        sb.append("<ns:" + elt +
                "  xmlns:ns=\"http://xbean/scomp/derivation/FinalBlockDefault\"");
        sb.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        sb.append(" xsi:type=\"ns:" + type + "\">");
        if (ext)
            sb.append(extContent);
        else if (valid)
            sb.append(restrContentValid);
        else
            sb.append(restrContentInvalid);

        sb.append("</ns:" + elt + ">");
        return sb.toString();

    }

    public void testBlockAll() throws Throwable {
        //subst ext type: should not be possible
        EltAllBaseDocument doc = EltAllBaseDocument.Factory.parse(getInstance("EltAllBase", "extAllT", true, true));
        assertTrue(!doc.validate());

        //subst rest type:  should not be possible
        EltAllBaseDocument doc1 = EltAllBaseDocument.Factory.parse(getInstance("EltAllBase", "restAllT", false, false));

        assertTrue(!doc.validate());

        doc1 = EltAllBaseDocument.Factory.parse(getInstance("EltAllBase", "restAllT", false, true));
        assertTrue(!doc.validate(validateOptions));
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_EXTENSION};
        assertTrue(compareErrorCodes(errExpected));


    }

    public void testBlockExtension() throws Throwable {
        //subst ext type: should not be possible
        try {
            EltEBaseDocument doc = EltEBaseDocument.Factory.parse(getInstance("EltExtE", "extET", true, true));
            fail("Not a valid Substitution");
        } catch (XmlException e) {

        }
        //subst rest type: should work
        /**
         * base type: blocked="extension" so rest. type should be a vald subst
         */
        EltEBaseDocument doc1 = null;
        try {
            doc1 = EltEBaseDocument.Factory.parse(getInstance("EltEBase", "restET", false, false));
        } catch (XmlException e) {

        }

        String instance = getInstance("EltEBase", "restET", false, true);
        doc1 = EltEBaseDocument.Factory.parse(instance);
        validate(doc1);
    }

    public void testBlockRestriction() throws Throwable {
        //subst ext type: should work
        EltRBaseDocument doc = EltRBaseDocument.Factory.parse(getInstance("EltRBase", "extRT", true, true));
        validate(doc);
        //subst rest type:  should not be possible
        EltRBaseDocument doc1 = EltRBaseDocument.Factory.parse(getInstance("EltRBase", "restRT", false, false));
        assertTrue(!doc1.validate(validateOptions));
        String[] errExpected = new String[]{
            XmlErrorCodes
                .ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_RESTRICTION};
               assertTrue(compareErrorCodes(errExpected));

        doc1 = EltRBaseDocument.Factory
                .parse(getInstance("EltRBase", "restRT", false, true));
        clearErrors();
        assertTrue(!doc1.validate(validateOptions));
        errExpected = new String[]{
            XmlErrorCodes.ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_RESTRICTION};
        assertTrue(compareErrorCodes(errExpected));

    }

    //should be equivaluent to final="#all"
    public void testBlockRE_ER() throws Throwable {
        //subst ext type: should not be possible
        //ER
        EltERBaseDocument doc = EltERBaseDocument.Factory.parse(getInstance("EltERBase", "extERT", true, true));
        //RE
        EltREBaseDocument doc1 =
                EltREBaseDocument.Factory.parse(getInstance("EltREBase", "extRET", true, true));
        assertTrue(!doc1.validate(validateOptions));
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_EXTENSION};
        assertTrue(compareErrorCodes(errExpected));


        //subst rest type:  should not be possible
        //ER
        EltERBaseDocument doc2 = EltERBaseDocument.Factory
                .parse(getInstance("EltERBase",
                        "restERT", false, false));
        clearErrors();
        assertTrue(!doc2.validate(validateOptions));
        errExpected = new String[]{
            XmlErrorCodes
                .ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_RESTRICTION};
        assertTrue(compareErrorCodes(errExpected));

        doc2 = EltERBaseDocument.Factory.parse(getInstance("EltERBase", "restERT", false, true));
        clearErrors();
        assertTrue(!doc2.validate(validateOptions));
        errExpected = new String[]{
            XmlErrorCodes
                .ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_RESTRICTION};
        assertTrue(compareErrorCodes(errExpected));

        //RE
        EltREBaseDocument doc3 = EltREBaseDocument.Factory
                .parse(getInstance("EltREBase", "restRET", false, false));
       clearErrors();
        assertTrue(!doc3.validate(validateOptions));
        errExpected = new String[]{
            XmlErrorCodes
                .ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_RESTRICTION
        };
        assertTrue(compareErrorCodes(errExpected));

        doc3 = EltREBaseDocument.Factory
                .parse(getInstance("EltREBase", "restRET", false, true));
        clearErrors();
        assertTrue(!doc3.validate(validateOptions));
        errExpected = new String[]{
             XmlErrorCodes
                .ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_RESTRICTION
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    /**
     * blockDefault="#all"
     *
     * @throws Throwable
     */
    public void testBlockDefault() throws Throwable {
        EltDefaultBlockDocument doc =
                EltDefaultBlockDocument.Factory
                .parse(getInstanceDefault("EltDefaultBlock", "extNoneT", true, true));
        assertTrue(!doc.validate(validateOptions));
        String[] errExpected = new String[]{
            XmlErrorCodes
                .ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_EXTENSION};
        assertTrue(compareErrorCodes(errExpected));

        doc = EltDefaultBlockDocument.Factory
                .parse(getInstanceDefault("EltDefaultBlock", "restNoneT", false, false));
        clearErrors();
        assertTrue(!doc.validate(validateOptions));
        errExpected = new String[]{
            XmlErrorCodes
                .ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_RESTRICTION
        };
        assertTrue(compareErrorCodes(errExpected));

        doc = EltDefaultBlockDocument.Factory.parse(getInstanceDefault("EltDefaultBlock", "restNoneT", false, true));
       clearErrors();
        assertTrue(!doc.validate(validateOptions));
        errExpected = new String[]{
            XmlErrorCodes.ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_RESTRICTION
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    /**
     * blockDefault="#all"
     * local block=""
     *
     * @throws Throwable
     */
    public void testBlockNone() throws Throwable {
        String instance = getInstanceDefault("EltNoBlock", "extAllT", true, true);
        EltNoBlockDocument doc = EltNoBlockDocument.Factory.parse(instance);
        validate(doc);
        doc = EltNoBlockDocument.Factory.parse(getInstanceDefault("EltNoBlock", "restAllT", false, false));
        assertTrue(!doc.validate());
        doc = EltNoBlockDocument.Factory.parse(getInstanceDefault("EltNoBlock", "restAllT", false, true));
        validate(doc);
    }

    public void validate(XmlObject doc) throws Throwable {
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

}
