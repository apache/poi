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
package scomp.elements.detailed;

import scomp.common.BaseCase;
import xbean.scomp.element.nillTest.*;
import org.apache.xmlbeans.impl.values.XmlValueNotNillableException;
import org.apache.xmlbeans.XmlOptions;

import java.math.BigInteger;


/**
 * this test illustrates somewhat inconsistent behavior
 * of nillable:
 */
public class NillTest extends BaseCase {
    /**
     * Tests exceptions when setting values to
     * null for non-nillable elems
     *  * CR CR192914:
     * Regardless of Schema definition,
     * setXXX(null) will clear the value of the
     * XXX attribute/element and if the container is an
     * element, will also add the "xsi:nil" attribute.
     */
    // for all nillable tests, the validation falls thro only if the ValidateOnSet option is turned on
    public void testNotNillableLocalElem() {

        XmlOptions options = new XmlOptions();
        options.setValidateOnSet();

        // local element, not nillable. If setXXX is set to null & validateOnSet is true, it should throw XmlValueNotNillableException
        Contact contact = Contact.Factory.newInstance(options);
        try{
            contact.setFirstName(null);
            fail("XmlValueNotNillableException Expected here");
        }
        catch (XmlValueNotNillableException e) {
            e.printStackTrace();
        }

        // with validate turned off, this should to thro
        Contact contactWithValidateOff = Contact.Factory.newInstance();
        try{
            contactWithValidateOff.setFirstName(null);
        }
        catch (XmlValueNotNillableException e) {
            e.printStackTrace();
            fail("XmlValueNotNillableException NOT Expected here");
        }
        assertEquals("<firstName " +
               "xsi:nil=\"true\" " +
               "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>",
               contactWithValidateOff.xmlText());

    }

    public void testNotNillableGlobalElem() {
        XmlOptions options = new XmlOptions();
        options.setValidateOnSet();

        // global element, not nillable. If setXXX is set to null & validateOnSet is true, it should throw XmlValueNotNillableException
        CityNameDocument cityName = CityNameDocument.Factory.newInstance(options);
        try{
            cityName.setCityName(null);
            fail("XmlValueNotNillableException Expected here");
        }
        catch (XmlValueNotNillableException e) {
        }

        // with validate turned off, this should to thro
        CityNameDocument cityNameWithValidateOff = CityNameDocument.Factory.newInstance();
        try{
            cityNameWithValidateOff.setCityName(null);
        }
        catch (XmlValueNotNillableException e) {
            e.printStackTrace();
            fail("XmlValueNotNillableException NOT Expected here");
        }

        assertEquals("<nil:cityName " +
               "xsi:nil=\"true\" " +
               "xmlns:nil=\"http://xbean/scomp/element/NillTest\" " +
               "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>",
               cityNameWithValidateOff.xmlText());
    }

    public void testNillableGlobalElement()
    {
        XmlOptions options = new XmlOptions();
        options.setValidateOnSet();

        // global element, nillable. If setXXX is set to null & validateOnSet is true, it should NOT throw XmlValueNotNillableException
        GlobalEltNillableDocument testElt = GlobalEltNillableDocument
                .Factory.newInstance(options);
        try{
            testElt.setGlobalEltNillable(null);
        }
        catch (XmlValueNotNillableException e) {
            e.printStackTrace();
            fail("XmlValueNotNillableException Not Expected here");
        }
        assertEquals("<nil:GlobalEltNillable " +
               "xsi:nil=\"true\" " +
               "xmlns:nil=\"http://xbean/scomp/element/NillTest\" " +
               "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>",
               testElt.xmlText());

        // without the validateOnSet - no exception in this case either
        GlobalEltNillableDocument testEltWithValidateOff = GlobalEltNillableDocument
                .Factory.newInstance();
        try{
            testEltWithValidateOff.setGlobalEltNillable(null);
        }
        catch (XmlValueNotNillableException e) {
            e.printStackTrace();
            fail("XmlValueNotNillableException Not Expected here");
        }
        assertEquals("<nil:GlobalEltNillable " +
               "xsi:nil=\"true\" " +
               "xmlns:nil=\"http://xbean/scomp/element/NillTest\" " +
               "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>",
               testElt.xmlText());


    }

    public void testNillableLocalElement()
    {
        XmlOptions options = new XmlOptions();
        options.setValidateOnSet();

        // global element, nillable. If setXXX is set to null & validateOnSet is true, it should NOT throw XmlValueNotNillableException
        Contact contact = Contact
                .Factory.newInstance(options);
        try{
            contact.setLocalNillableElem(null);
        }
        catch (XmlValueNotNillableException e) {
            e.printStackTrace();
            fail("XmlValueNotNillableException Not Expected here");
        }
        assertEquals("<LocalNillableElem " +
               "xsi:nil=\"true\" " +
               "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>",
               contact.xmlText());

        // without the validateOnSet - no exception in this case either
        Contact contactWithValidationOff = Contact
                .Factory.newInstance();
        try{
            contactWithValidationOff.setLocalNillableElem(null);
        }
        catch (XmlValueNotNillableException e) {
            e.printStackTrace();
            fail("XmlValueNotNillableException Not Expected here");
        }
        assertEquals("<LocalNillableElem " +
               "xsi:nil=\"true\" " +
               "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>",
               contactWithValidationOff.xmlText());


    }

    public void testDefaultValElement()
    {
        XmlOptions options = new XmlOptions();
        options.setValidateOnSet();

        // default value element, not nillable. If setXXX is set to null & validateOnSet is true, it should
        // throw XmlValueNotNillableException and validation should fail
        GlobalEltDefaultDocument elt = GlobalEltDefaultDocument
                .Factory.newInstance(options);
        try{
            elt.setGlobalEltDefault(null);
            System.out.println("Elt Text:" + elt.xmlText());
            assertFalse(elt.validate());
            fail("XmlValueNotNillableException Expected here");
        }
        catch (XmlValueNotNillableException e) {
        }
    }

    public void testNotNillableFixedValueElement()
    {
        XmlOptions options = new XmlOptions();
        options.setValidateOnSet();

        // fixed value element, not nillable. If setXXX is set to null & validateOnSet is true, it should
        // throw XmlValueNotNillableException and validation should fail
        GlobalEltFixedDocument elt = GlobalEltFixedDocument
                .Factory.newInstance(options);
        try{
            elt.setGlobalEltFixed(null);
            System.out.println("Elt Text:" + elt.xmlText());
            assertFalse(elt.validate());
            fail("XmlValueNotNillableException Expected here");
        }
        catch (XmlValueNotNillableException e) {
            e.printStackTrace();
        }
    }

}
