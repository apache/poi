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
package scomp.substGroup.detailed;

import xbean.scomp.substGroup.deep.*;

import java.math.BigInteger;

import scomp.common.BaseCase;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlErrorCodes;

/**
 */
public class Deep extends BaseCase {

    /**
     * QName for casualBShirt is incorrect:
     * err. message seems bad to me
     * TODO: seems like the first err is enuf
     */
    public void testErrorMessage() throws XmlException {
        String input =
                "<pre:items xmlns:pre=\"http://xbean/scomp/substGroup/Deep\">" +
                "<casualBShirt>" +
                " <number>SKU25</number>" +
                " <name>Oxford Shirt</name>" +
                " <size>12</size>" +
                " <color>blue</color>" +
                "<pokadotColor>yellow</pokadotColor>" +
                "</casualBShirt>" +
                "</pre:items>";
        ItemsDocument doc = ItemsDocument.Factory.parse(input);
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_ELEMENT
        };
             assertTrue(compareErrorCodes(errExpected));
        
    }


    public void testValidSubstParse() throws Throwable {
        String input =
                "<pre:items xmlns:pre=\"http://xbean/scomp/substGroup/Deep\">" +
                "<pre:casualBShirt>" +
                " <number>SKU25</number>" +
                " <name>Oxford Shirt</name>" +
                " <size>12</size>" +
                " <color>blue</color>" +
                "<pokadotColor>yellow</pokadotColor>" +
                "</pre:casualBShirt>" +
                "<pre:product>" +
                " <number>SKU45</number>" +
                "   <name>Accessory</name>" +
                "</pre:product>" +
                "<pre:umbrella>" +
                " <number>SKU15</number>" +
                "   <name>Umbrella</name>" +
                "</pre:umbrella>" +
                "</pre:items>";
        ItemsDocument doc = ItemsDocument.Factory.parse(input);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

    }

    /**
     * Test error message. 1 product too many
     *
     * @throws Throwable
     */
    public void testValidSubstParseInvalid() throws Throwable {
        String input =
                "<root:items xmlns:root=\"http://xbean/scomp/substGroup/Deep\">" +
                "<root:shirt>" +
                " <number>SKU25</number>" +
                " <name>Oxford Shirt</name>" +
                " <size>12</size>" +
                " <color>blue</color>" +
                "</root:shirt>" +
                "<root:product>" +
                " <number>SKU45</number>" +
                "   <name>Accessory</name>" +
                "</root:product>" +
                "<root:umbrella>" +
                " <number>SKU15</number>" +
                "   <name>Umbrella</name>" +
                "</root:umbrella>" +
                "<root:casualBShirt>" +
                " <number>SKU25</number>" +
                " <name>Oxford Shirt</name>" +
                " <size>12</size>" +
                " <color>blue</color>" +
                "<pokadotColor>yellow</pokadotColor>" +
                "</root:casualBShirt>" +
                "</root:items>";
        ItemsDocument doc = ItemsDocument.Factory.parse(input);

            assertTrue(!doc.validate(validateOptions));
            showErrors();
            String[] expErrors=new String[]{
                XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED
            };
            assertTrue(compareErrorCodes(expErrors));
    }

    public void testValidSubstBuild() throws Throwable {
        ItemsDocument doc = ItemsDocument.Factory.newInstance();
        ItemType items = doc.addNewItems();
        BusinessCasualShirtType bShirt = BusinessCasualShirtType.Factory.newInstance();
        bShirt.setName("Funny Shirt");
        bShirt.setNumber("SKU84");
        bShirt.setSize(new BigInteger("10"));
        bShirt.setPokadotColor("yellow");
        bShirt.setColor("blue");
/*   This doesn't work
ProductType hat = elt.addNewProduct();
((HatType)hat).setNumber(3);
ShirtType shirt = (ShirtType) elt.addNewProduct();
*/
        ShirtType shirt = ShirtType.Factory.newInstance();
        shirt.setName("Funny Shirt");
        shirt.setNumber("SKU54");
        shirt.setColor("green");
        shirt.setSize(new BigInteger("10"));
        ProductType genericProd = ProductType.Factory.newInstance();
        genericProd.setName("Pants");
        genericProd.setNumber("32");

        items.setProductArray(new ProductType[]{bShirt, shirt, genericProd});
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

//TODO: what to do with the umbrella here???
    }
}
