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

package scomp.idConstraint.detailed;

import scomp.common.BaseCase;
import xbean.scomp.idConstraint.constraint.*;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.XmlErrorCodes;

/**
 *
 *
 * 
 */
public class KeyKeyref extends BaseCase {

    public void testUnique() throws Throwable {
        String input =
                "<con:productList xmlns:con=\"http://xbean/scomp/idConstraint/Constraint\">" +
                " <con:product>" +
                " <con:department con:name=\"Marketing\"/>" +
                " <con:id>FooBarChart</con:id>" +
                "</con:product>" +
                "<con:product>" +
                " <con:department con:name=\"7345\"/>" +
                " <con:id>FooBarChart</con:id>" +
                "</con:product>" +
                "</con:productList>";

        ProductListDocument doc = ProductListDocument.Factory.parse(input);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }

    }

    /**
     * field combo not unique in instance  (a and c are the same)
     *
     * @throws Throwable
     */
    public void testUniqueIllegal() throws Throwable {
        ProductListDocument doc = ProductListDocument.Factory.newInstance();
        ProductListType products = ProductListType.Factory.newInstance();
        ProductType a = products.addNewProduct();
        ProductType.Department dep = ProductType.Department.Factory.newInstance();
        XmlString val = XmlString.Factory.newInstance();
        val.setStringValue("FooDep");
        dep.setName(val);
        a.setDepartmentArray(new ProductType.Department[]{dep});
        a.setId("0");

        ProductType b = products.addNewProduct();
        b.setDepartmentArray(new ProductType.Department[]{dep});
        b.setId("1");

        ProductType c = products.addNewProduct();
        c.setDepartmentArray(new ProductType.Department[]{dep});
        c.setId("0");
        doc.setProductList(products);

        System.out.println(doc.xmlText());

        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.IDENTITY_CONSTRAINT_VALID$DUPLICATE_UNIQUE
        };
        assertTrue(compareErrorCodes(errExpected));


    }

    /**
     * Selector selects a non-unique field in instance
     * Only one dept can appear in the first product
     *
     * @throws Throwable
     */
    public void testUniqueIllegal2() throws Throwable {
        String input =
                "<con:productList xmlns:con=\"http://xbean/scomp/idConstraint/Constraint\">" +
                " <con:product>" +
                " <con:department con:name=\"Marketing\"/>" +
                " <con:department con:name=\"Management\"/>" +
                " <con:id>FooBarChart</con:id>" +
                "</con:product>" +
                "<con:product>" +
                " <con:department con:name=\"7345\"/>" +
                " <con:id>FooBarChart</con:id>" +
                "</con:product>" +
                "</con:productList>";

        ProductListDocument doc = ProductListDocument
                .Factory.parse(input);

        assertTrue(!doc.validate(validateOptions));
        showErrors();
    }


    public void testKey() throws Throwable {
        String input =
                "<con:KeyProductList xmlns:con=\"http://xbean/scomp/idConstraint/Constraint\">" +
                " <con:product>" +
                " <con:department con:name=\"Marketing\"/>" +
                " <con:id>FooBarChart</con:id>" +
                "</con:product>" +
                "<con:product>" +
                " <con:department con:name=\"7345\"/>" +
                " <con:id>FooBarChart</con:id>" +
                "</con:product>" +
                "</con:KeyProductList>";

        KeyProductListDocument doc = KeyProductListDocument.Factory.parse(input);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

    /**
     * null key in instance:missing dept in first product
     *
     * @throws Throwable
     */
    public void testKeyIllegal() throws Throwable {
        String input =
                "<xs:KeyProductList xmlns:xs=\"http://xbean/scomp/idConstraint/Constraint\">" +
                " <xs:product>" +
                " <xs:id>FooBarChart</xs:id>" +
                "</xs:product>" +
                "<xs:product>" +
                " <xs:department xs:name=\"7345\"/>" +
                " <xs:id>FooBarChart</xs:id>" +
                "</xs:product>" +
                "</xs:KeyProductList>";

        KeyProductListDocument doc = KeyProductListDocument.Factory.parse(input);

        assertTrue(!doc.validate(validateOptions));
        showErrors();
        //TODO: is this the right error
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT,
           XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_ELEMENT
        };
        assertTrue(compareErrorCodes(errExpected));

    }


    public void testKeyRef() throws Throwable {
        String input =
                "<con:CompanyDB xmlns:con=\"http://xbean/scomp/idConstraint/Constraint\">" +
                "<con:KeyProductList >" +
                " <con:product>" +
                " <con:department con:name=\"Marketing\"/>" +
                " <con:id>FooBarChart</con:id>" +
                "</con:product>" +
                "<con:product>" +
                " <con:department con:name=\"7345\"/>" +
                " <con:id>FooBarChart</con:id>" +
                "</con:product>" +
                "</con:KeyProductList>" +
                "<con:order>" +
                "   <con:customerName>Bozo</con:customerName>" +
                "   <con:item con:deptId=\"7345\"" +
                "   con:SKU='FooBarChart'>" +
                "  </con:item>" +
                "</con:order>" +
                "<con:order>" +
                "   <con:customerName>Bozo</con:customerName>" +
                "   <con:item con:deptId=\"7345\"" +
                "        con:SKU=\"FooBarChart\">" +
                "  </con:item>" +
                "</con:order>" +
                "</con:CompanyDB>";

        CompanyDBDocument doc = CompanyDBDocument.Factory.parse(input);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }


    }

    // Invalid xml instance with 2 problems :
    // a) the values for the key & key ref elems are not the same
    // b) The keyref/key elems are duplicated
    public void testKeyRefIllegal() throws Throwable {

        String input =
                "<con:CompanyDB xmlns:con=\"http://xbean/scomp/idConstraint/Constraint\">" +
                "<con:KeyProductList >" +
                " <con:product>" +
                " <con:department con:name=\"Marketing\"/>" +
                " <con:id>FooBarChart</con:id>" +
                "</con:product>" +
                " <con:product>" +
                " <con:department con:name=\"Marketing\"/>" +
                " <con:id>FooBarChart</con:id>" +
                "</con:product>" +
                "</con:KeyProductList>" +
                "<con:order>" +
                "   <con:customerName>Bozo</con:customerName>" +
                "   <con:item con:deptId=\"7345\"" +
                "   con:SKU=\"ID1\">" +
                "  </con:item>" +
                "</con:order>" +
                "<con:order>" +
                "   <con:customerName>Bozo</con:customerName>" +
                "   <con:item con:deptId=\"7345\"" +
                "   con:SKU=\"ID1\">" +
                "  </con:item>" +
                "</con:order>" +
                "</con:CompanyDB>";

        CompanyDBDocument doc = CompanyDBDocument.Factory.parse(input);
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.IDENTITY_CONSTRAINT_VALID$DUPLICATE_KEY,
            XmlErrorCodes.IDENTITY_CONSTRAINT_VALID$KEYREF_KEY_NOT_FOUND
        };
        assertTrue(compareErrorCodes(errExpected));


    }

}
