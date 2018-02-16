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
import xbean.scomp.derivation.complexExtension.ChoiceExtensionEltDocument;
import xbean.scomp.derivation.complexExtension.ChoiceExtensionT;
import xbean.scomp.derivation.complexExtension.SequenceExtensionEltDocument;
import xbean.scomp.derivation.complexExtension.SequenceExtensionT;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.apache.xmlbeans.*;

/**
 *
 *
 *
 */
public class ComplexContentExtensionTest extends BaseCase {


    public void testSequenceExtension() throws Throwable {
        SequenceExtensionEltDocument doc = SequenceExtensionEltDocument.Factory
                .newInstance();
        SequenceExtensionT elt = doc.addNewSequenceExtensionElt();
        elt.setChild1(BigInteger.ONE);
        elt.addChild2("foobar");
        elt.setChild3Array(new BigInteger[]{BigInteger.ONE, BigInteger.ZERO});
        elt.addExtraEltInt(3);
        elt.setExtraEltStrArray(new String[]{"extra1", "extra2"});

        assertEquals(BigInteger.ONE, elt.getChild1());
        assertEquals("foobar", elt.getChild2Array()[0]);
        elt.insertChild2(0, "newfoobar");
        assertEquals("newfoobar", elt.getChild2Array()[0]);

        assertTrue(!doc.validate(validateOptions));
              showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED
        };
                     assertTrue(compareErrorCodes(errExpected));


        elt.removeExtraEltInt(0);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        elt.removeExtraEltStr(1);
        assertTrue(elt.validate());

    }

    /**
     *  result type is a sequence of 2 choices
     * Valid sets: { (child1 xor child2 xor child3 )(extraEltStr xor extraEltStr) }
     * @throws Throwable
     */
    public void testChoiceExtension() throws Throwable {
        ChoiceExtensionEltDocument doc = ChoiceExtensionEltDocument.Factory
                .newInstance();
        ChoiceExtensionT elt = doc.addNewChoiceExtensionElt();
        elt.setChild1(new BigInteger("10"));
        elt.addChild2("foobar");
        elt.setChild3Array(new BigInteger[]{BigInteger.ONE, BigInteger.ZERO});
        elt.addExtraEltInt(3);
        elt.setExtraEltStrArray(new String[]{"extra1", "extra2"});
        assertTrue(!doc.validate(validateOptions));
                    showErrors();
             //TODO: child 2 and child3 not allowed
             //extraEltStr not allowed
              String[] errExpected = new String[]{
                  XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT,
                   XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT,
                  XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED
              };
                           assertTrue(compareErrorCodes(errExpected));

        assertEquals(new BigInteger("10"), elt.getChild1());
        assertEquals("foobar", elt.getChild2Array()[0]);
        elt.unsetChild1();

        XmlInteger expected = XmlInteger.Factory.newInstance();
        expected.setBigIntegerValue(BigInteger.ONE);
        assertTrue( expected.valueEquals(elt.xgetChild3Array()[0]) );
        elt.removeExtraEltInt(0);
        elt.removeChild2(0);
        clearErrors();
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            System.out.println(" test failed :");
            showErrors();
            throw t;
        }



         elt.addExtraEltInt(3);
        expected.setBigIntegerValue(BigInteger.ZERO);
        assertTrue( expected.valueEquals(elt.xgetChild3Array()[1]) );
          assertEquals(3, elt.getExtraEltIntArray()[0]);
        assertEquals(3, elt.getExtraEltIntArray(0));
        clearErrors();
        assertTrue(!doc.validate(validateOptions));
                           showErrors();
                    errExpected = new String[]{
                         XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED
                        };
                                  assertTrue(compareErrorCodes(errExpected));
        elt.removeExtraEltInt(0);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        XmlString expected1 = XmlString.Factory.newInstance();
        expected1.setStringValue("extra1");
        assertTrue( expected1.valueEquals(elt.xgetExtraEltStrArray(0)) );
        expected1.setStringValue("extra2");
        assertTrue( expected1.valueEquals(elt.xgetExtraEltStrArray()[1]) );

    }

    /**
     * The follwing are test for the 'final' attribute used in a base type that affects extenstion/restriction
     * They are negative tests and test for #all, restriction, extenstion and 'extenstion restriction' values
     */
    public void testFinalAll() {
        String inputXsd = "    <xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "    <xsd:complexType name=\"BaseProductTypeFinalAll\" final=\"#all\">\n" +
                "        <xsd:sequence>\n" +
                "            <xsd:element name=\"number\" type=\"xsd:integer\" />\n" +
                "            <xsd:element name=\"name\" type=\"xsd:string\" minOccurs=\"0\" />\n" +
                "        </xsd:sequence>\n" +
                "    </xsd:complexType>\n" +
                "\n" +
                "    <xsd:complexType name=\"ProductTypeExtension\">\n" +
                "        <xsd:complexContent>\n" +
                "            <xsd:extension base=\"BaseProductTypeFinalAll\">\n" +
                "                <xsd:sequence>\n" +
                "                    <xsd:element name=\"subcategory\" type=\"xsd:string\"/>\n" +
                "                </xsd:sequence>\n" +
                "            </xsd:extension>\n" +
                "        </xsd:complexContent>\n" +
                "    </xsd:complexType>    \n" +
                "\n" +
                "    <xsd:complexType name=\"ProductTypeRestriction\">\n" +
                "        <xsd:complexContent>\n" +
                "            <xsd:restriction base=\"BaseProductTypeFinalAll\">\n" +
                "                <xsd:sequence>\n" +
                "                    <xsd:element name=\"number\" type=\"xsd:integer\"/>\n" +
                "                </xsd:sequence>\n" +
                "            </xsd:restriction>\n" +
                "        </xsd:complexContent>\n" +
                "    </xsd:complexType>    \n" +
                "\n" +
                "    </xsd:schema>";

        XmlOptions options = new XmlOptions();
        List errors = new ArrayList();
        options.setErrorListener(errors);

        try {
            XmlObject xobj = XmlObject.Factory.parse(inputXsd);
            XmlObject[] compInput = new XmlObject[]{xobj};
            XmlBeans.compileXmlBeans(null, null, compInput, null, XmlBeans.getBuiltinTypeSystem(), null, options);
        }
        catch (XmlException xme) {
            // The convention is that the XmlException that gets thrown form XmlBeans.compile* methods always contains
            // just the first error and if you need to see all errors you use XmlOptions.
            // hence checking for error codes is now done with XmlOptions.
            assertEquals(1, xme.getErrors().size());
        }

        assertEquals(2, errors.size());

        Iterator itr = errors.iterator();
        XmlError eacherr = (XmlError) itr.next();
        System.out.println("Err:" + eacherr.getMessage() + "," + eacherr.getErrorCode());
        assertNotNull(eacherr.getErrorCode());
        assertEquals(XmlErrorCodes.COMPLEX_TYPE_EXTENSION$FINAL, eacherr.getErrorCode());

        eacherr = (XmlError) itr.next();
        System.out.println("Err:" + eacherr.getMessage());
        assertNotNull(eacherr.getErrorCode());
        assertEquals(XmlErrorCodes.COMPLEX_TYPE_RESTRICTION$FINAL, eacherr.getErrorCode());

    }

    public void testFinalExtension() {
        String inputXsd = "    <xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "    <xsd:complexType name=\"BaseProductTypeFinalExtension\" final=\"extension\">\n" +
                "        <xsd:sequence>\n" +
                "            <xsd:element name=\"number\" type=\"xsd:integer\" />\n" +
                "            <xsd:element name=\"name\" type=\"xsd:string\" minOccurs=\"0\" />\n" +
                "        </xsd:sequence>\n" +
                "    </xsd:complexType>\n" +
                "\n" +
                "    <xsd:complexType name=\"ProductTypeExtension\">\n" +
                "        <xsd:complexContent>\n" +
                "            <xsd:extension base=\"BaseProductTypeFinalExtension\">\n" +
                "                <xsd:sequence>\n" +
                "                    <xsd:element name=\"subcategory\" type=\"xsd:string\"/>\n" +
                "                </xsd:sequence>\n" +
                "            </xsd:extension>\n" +
                "        </xsd:complexContent>\n" +
                "    </xsd:complexType>    \n" +
                "\n" +
                "    </xsd:schema>";

        try {
            XmlObject xobj = XmlObject.Factory.parse(inputXsd);
            XmlObject[] compInput = new XmlObject[]{xobj};
            XmlBeans.compileXmlBeans(null, null, compInput, null, XmlBeans.getBuiltinTypeSystem(), null, null);
        }
        catch (XmlException xme) {
            assertEquals(xme.getErrors().size(), 1);

            XmlError eacherr = (XmlError) xme.getErrors().iterator().next();
            System.out.println("error:" + eacherr.getErrorCode());
            assertNotNull(eacherr.getErrorCode());
            assertEquals(XmlErrorCodes.COMPLEX_TYPE_EXTENSION$FINAL, eacherr.getErrorCode());
        }
    }

    public void testFinalRestriction() {
        String inputXsd = "    <xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "    <xsd:complexType name=\"BaseProductTypeFinalRestriction\" final=\"restriction\">\n" +
                "        <xsd:sequence>\n" +
                "            <xsd:element name=\"number\" type=\"xsd:integer\" />\n" +
                "            <xsd:element name=\"name\" type=\"xsd:string\" />\n" +
                "        </xsd:sequence>\n" +
                "    </xsd:complexType>\n" +
                "\n" +
                "    <xsd:complexType name=\"ProductTypeRestriction\">\n" +
                "        <xsd:complexContent>\n" +
                "            <xsd:restriction base=\"BaseProductTypeFinalRestriction\">\n" +
                "                <xsd:sequence>\n" +
                "                    <xsd:element name=\"number\" type=\"xsd:integer\"/>\n" +
                "                </xsd:sequence>\n" +
                "            </xsd:restriction>\n" +
                "        </xsd:complexContent>\n" +
                "    </xsd:complexType>    " +
                "    </xsd:schema>";

        try {
            XmlObject xobj = XmlObject.Factory.parse(inputXsd);
            XmlObject[] compInput = new XmlObject[]{xobj};
            XmlBeans.compileXmlBeans(null, null, compInput, null, XmlBeans.getBuiltinTypeSystem(), null, null);
        }
        catch (XmlException xme) {
            assertEquals(1, xme.getErrors().size());

            XmlError eacherr = (XmlError) xme.getErrors().iterator().next();
            System.out.println("Err:" + eacherr.getMessage());
            assertNotNull(eacherr.getErrorCode());
            assertEquals(XmlErrorCodes.COMPLEX_TYPE_RESTRICTION$FINAL, eacherr.getErrorCode());
        }
    }

    public void testFinalRestrExt() {
        String inputXsd = "    <xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "    <xsd:complexType name=\"BaseProductTypeFinalAll\" final=\"restriction extension\">\n" +
                "        <xsd:sequence>\n" +
                "            <xsd:element name=\"number\" type=\"xsd:integer\" />\n" +
                "            <xsd:element name=\"name\" type=\"xsd:string\" minOccurs=\"0\" />\n" +
                "        </xsd:sequence>\n" +
                "    </xsd:complexType>\n" +
                "\n" +
                "    <xsd:complexType name=\"ProductTypeExtension\">\n" +
                "        <xsd:complexContent>\n" +
                "            <xsd:extension base=\"BaseProductTypeFinalAll\">\n" +
                "                <xsd:sequence>\n" +
                "                    <xsd:element name=\"subcategory\" type=\"xsd:string\"/>\n" +
                "                </xsd:sequence>\n" +
                "            </xsd:extension>\n" +
                "        </xsd:complexContent>\n" +
                "    </xsd:complexType>    \n" +
                "\n" +
                "    <xsd:complexType name=\"ProductTypeRestriction\">\n" +
                "        <xsd:complexContent>\n" +
                "            <xsd:restriction base=\"BaseProductTypeFinalAll\">\n" +
                "                <xsd:sequence>\n" +
                "                    <xsd:element name=\"number\" type=\"xsd:integer\"/>\n" +
                "                </xsd:sequence>\n" +
                "            </xsd:restriction>\n" +
                "        </xsd:complexContent>\n" +
                "    </xsd:complexType>    \n" +
                "\n" +
                "    </xsd:schema>";

        XmlOptions options = new XmlOptions();
        List errors = new ArrayList();
        options.setErrorListener(errors);

        try {
            XmlObject xobj = XmlObject.Factory.parse(inputXsd);
            XmlObject[] compInput = new XmlObject[]{xobj};
            XmlBeans.compileXmlBeans(null, null, compInput, null, XmlBeans.getBuiltinTypeSystem(), null, options);
        }
        catch (XmlException xme) {
            // The convention is that the XmlException that gets thrown form XmlBeans.compile* methods always contains
            // just the first error and if you need to see all errors you use XmlOptions.
            // hence checking for error codes is now done with XmlOptions.
            assertEquals(1, xme.getErrors().size());
        }

        assertEquals(2, errors.size());

        Iterator itr = errors.iterator();
        XmlError eacherr = (XmlError) itr.next();
        System.out.println("Err:" + eacherr.getMessage() + "," + eacherr.getErrorCode());
        assertNotNull(eacherr.getErrorCode());
        assertEquals(XmlErrorCodes.COMPLEX_TYPE_EXTENSION$FINAL, eacherr.getErrorCode());

        eacherr = (XmlError) itr.next();
        System.out.println("Err:" + eacherr.getMessage());
        assertNotNull(eacherr.getErrorCode());
        assertEquals(XmlErrorCodes.COMPLEX_TYPE_RESTRICTION$FINAL, eacherr.getErrorCode());

    }

}
