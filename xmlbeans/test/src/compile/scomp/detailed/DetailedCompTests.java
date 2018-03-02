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

package compile.scomp.detailed;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;

import java.io.File;
import java.util.*;

import compile.scomp.common.CompileCommon;

import javax.xml.namespace.QName;


public class DetailedCompTests extends TestCase
{
    public DetailedCompTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(DetailedCompTests.class); }


    /**
     * This test requires laxDoc.xsd to be compiled and
     * on the classpath ahead of time, otherwise documentation
     * element processing would not occur
     * @throws Exception
     */
    public void testLaxDocProcessing() throws Exception
    {
        QName q = new QName("urn:lax.Doc.Compilation", "ItemRequest");
        ArrayList err = new ArrayList();
        XmlOptions xm_opt = new XmlOptions().setErrorListener(err);
        xm_opt.setSavePrettyPrint();

        XmlObject xObj = XmlObject.Factory.parse(
                new File(CompileCommon.fileLocation+"/detailed/laxDoc.xsd"));
        XmlObject[] schemas = new XmlObject[]{xObj};


        // ensure exception is thrown when
        // xmloptions flag is not set
        boolean valDocEx = false;
        try{
            SchemaTypeSystem sts = XmlBeans.compileXmlBeans(null, null,
                schemas, null, XmlBeans.getBuiltinTypeSystem(), null, xm_opt);
            Assert.assertTrue("STS was null", sts != null);
        }catch(XmlException xmlEx){
            valDocEx = true;
            System.err.println("Expected Error: "+xmlEx.getMessage());
        } catch(Exception e){
            throw e;
        }

        //check exception was thrown
        if(!valDocEx)
            throw new Exception("Documentation processing " +
                    "should have thrown and error");
        // validate error code
        valDocEx = false;
        for (Iterator iterator = err.iterator(); iterator.hasNext();) {
            XmlError xErr = (XmlError)iterator.next();
            //System.out.println("ERROR: '"+ xErr+"'");
            //any one of these are possible
            if(xErr.getErrorCode().compareTo("cvc-complex-type.4") == 0 ||
                    xErr.getErrorCode().compareTo("cvc-complex-type.2.3") == 0 ||
                    xErr.getErrorCode().compareTo("cvc-complex-type.2.4c") == 0)
                valDocEx = true;
        }

        if (!valDocEx)
            throw new Exception("Expected Error code did not validate");

        //reset errors
        err.clear();

        //ensure no exception when error
        xm_opt = xm_opt.setCompileNoValidation();
        try {
            SchemaTypeSystem sts = XmlBeans.compileXmlBeans(null, null,
                    schemas, null, XmlBeans.getBuiltinTypeSystem(), null,
                    xm_opt);

            if(!err.isEmpty())
                throw new Exception("Error listener should be empty");

            for (Iterator iterator = err.iterator(); iterator.hasNext();) {
                System.out.println(iterator.next());
            }

            SchemaGlobalElement sge = sts.findElement(q);
            System.out.println("QName: " + sge.getName());
            System.out.println("Type: " + sge.getType());


        } catch (Exception e) {
            throw e;
        }

    }

    private static final String schema_begin = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n";
    private static final String root_decl    = "<xs:element name=\"root\">\n  <xs:complexType>\n";
    private static final String att_decl     = "    <xs:attribute name=\"att\" type=\"simpleNotType\"/>\n";
    private static final String root_end     = "  </xs:complexType>\n</xs:element>\n";
    private static final String schema_end   = "</xs:schema>\n";

    private static final String notation1    = "    <xs:attribute name=\"att\" type=\"xs:NOTATION\"/>\n";
    private static final String notation2    = "<xs:simpleType name=\"simpleNotType\">\n" +
                                               "  <xs:restriction base=\"xs:NOTATION\">\n" +
                                               "    <xs:pattern value=\"ns:.*\"/>\n" +
                                               "  </xs:restriction>\n</xs:simpleType>\n";
    private static final String notation3    = "    <xs:sequence>\n      " +
                                               "<xs:element name=\"elem\" type=\"xs:ID\"/>\n" +
                                               "    </xs:sequence>\n";
    private static final String notation4    = " targetNamespace=\"scomp.detailed.CompilationTests\" " +
                                               "xmlns=\"scomp.detailed.CompilationTests\">\n";
    private static final String simpleTypeDef= "<xs:simpleType name=\"simpleNotType\">\n" +
                                               "  <xs:restriction base=\"enumDef\">\n";
    private static final String notation6    = "    <xs:pattern value=\"ns:.*\"/>\n";
    private static final String notation5    = "    <xs:length value=\"6\"/>\n";
    private static final String enumDef      = "  </xs:restriction>\n</xs:simpleType>\n" +
                                               "<xs:simpleType name=\"enumDef\">\n" +
                                               "  <xs:restriction base=\"xs:NOTATION\" xmlns:ns=\"namespace.notation\">\n" +
                                               "    <xs:enumeration value=\"ns:app1\"/>\n" +
                                               "    <xs:enumeration value=\"ns:app2\"/>\n" +
                                               "  </xs:restriction>\n</xs:simpleType>\n";

    private static final String doc_begin    = "<root xmlns:ns=\"namespace.notation\" " +
                                               "xmlns:app=\"namespace.notation\" att=\"";
    private static final String doc_end      = "\"/>";
    private static final String notation7    = "ns1:app1";
    private static final String notation8    = "ns:app";
    private static final String notation9    = "app:app1";
    private static final String notation10   = "ns:app1";

    /**
     * This tests usage of the xs:NOTATION type
     * @throws Exception
     */
    public void testNotation() throws Exception
    {
        String schema;
        String xml;
        SchemaTypeSystem typeSystem;
        XmlObject[] parsedSchema = new XmlObject[1];
        XmlObject parsedDoc;
        XmlOptions opts = new XmlOptions();
        ArrayList errors = new ArrayList();
        opts.setErrorListener(errors);
        opts.put("COMPILE_PARTIAL_TYPESYSTEM");

        // 1. Negative test - Error if xs:NOTATION used directly
        schema = schema_begin + root_decl + notation1 + root_end + schema_end;
//        System.out.println(schema);
        parsedSchema[0] = SchemaDocument.Factory.parse(schema);
        errors.clear();
        XmlBeans.compileXsd(parsedSchema, null, opts);
        assertTrue("Expected error: NOTATION type cannot be used directly", errors.size() == 1);
        assertEquals("Expected error: NOTATION type cannot be used directly",
            XmlErrorCodes.ATTR_NOTATION_TYPE_FORBIDDEN, ((XmlError)errors.get(0)).getErrorCode());
        assertEquals(XmlError.SEVERITY_ERROR, ((XmlError)errors.get(0)).getSeverity());

        // 2. Negative test - Error if xs:NOTATION restricted without enumeration
        schema = schema_begin + root_decl + att_decl + root_end + notation2 + schema_end;
//        System.out.println(schema);
        parsedSchema[0] = SchemaDocument.Factory.parse(schema);
        errors.clear();
        XmlBeans.compileXsd(parsedSchema, null, opts);
        assertTrue("Expected error: restriction of NOTATION must use enumeration facet", errors.size() == 1);
        assertEquals("Expected error: restriction of NOTATION must use enumeration facet",
            XmlErrorCodes.DATATYPE_ENUM_NOTATION, ((XmlError)errors.get(0)).getErrorCode());
        assertEquals(XmlError.SEVERITY_ERROR, ((XmlError)errors.get(0)).getSeverity());

        // 3. Warning if xs:NOTATION used as type of an element
        final String correctTypes = simpleTypeDef + notation6 + enumDef;
        schema = schema_begin + root_decl + notation3 + root_end + correctTypes + schema_end;
//        System.out.println(schema);
        parsedSchema[0] = SchemaDocument.Factory.parse(schema);
        errors.clear();
        XmlBeans.compileXsd(parsedSchema, null, opts);
        assertTrue("Expected warning: NOTATION-derived type should not be used on elements", errors.size() == 1);
        assertEquals("Expected warning: NOTATION-derived type should not be used on elements",
            XmlErrorCodes.ELEM_COMPATIBILITY_TYPE, ((XmlError)errors.get(0)).getErrorCode());
        assertEquals(XmlError.SEVERITY_WARNING, ((XmlError)errors.get(0)).getSeverity());

        // 4. Warning if xs:NOTATION is used in a Schema with target namespace
        schema = schema_begin.substring(0, schema_begin.length() - 2) + notation4 + root_decl +
            att_decl + root_end + correctTypes + schema_end;
//        System.out.println(schema);
        parsedSchema[0] = SchemaDocument.Factory.parse(schema);
        errors.clear();
        XmlBeans.compileXsd(parsedSchema, null, opts);
        assertTrue("Expected warning: NOTATION-derived type should not be used in a Schema with target namespace", errors.size() == 1);
        assertEquals("Expected warning: NOTATION-derived type should not be used in a Schema with target namespace",
            XmlErrorCodes.ATTR_COMPATIBILITY_TARGETNS, ((XmlError)errors.get(0)).getErrorCode());
        assertEquals(XmlError.SEVERITY_WARNING, ((XmlError)errors.get(0)).getSeverity());

        // 5. Warning - Deprecation of minLength, maxLength and length facets
        schema = schema_begin + root_decl + att_decl + root_end + simpleTypeDef + notation5 +
            enumDef + schema_end;
//        System.out.println(schema);
        parsedSchema[0] = SchemaDocument.Factory.parse(schema);
        errors.clear();
        XmlBeans.compileXsd(parsedSchema, null, opts);
        assertTrue("Expected warning: length facet cannot be used on a type derived from NOTATION", errors.size() == 1);
        assertEquals("Expected warning: length facet cannot be used on a type derived from NOTATION",
            XmlErrorCodes.FACETS_DEPRECATED_NOTATION, ((XmlError)errors.get(0)).getErrorCode());
        assertEquals(XmlError.SEVERITY_WARNING, ((XmlError)errors.get(0)).getSeverity());

        // 6. Positive test - Test restriction via enumeration, then same as 2
        schema = schema_begin + root_decl + att_decl + root_end + correctTypes + schema_end;
//        System.out.println(schema);
        parsedSchema[0] = SchemaDocument.Factory.parse(schema);
        errors.clear();
        typeSystem = XmlBeans.compileXsd(parsedSchema, null, opts);
        assertTrue("Expected no errors or warnings", errors.size() == 0);
        SchemaType docType = typeSystem.findDocumentType(new QName("", "root"));
        SchemaType type = docType.getElementProperty(new QName("", "root")).getType().
            getAttributeProperty(new QName("", "att")).getType();
        assertEquals(type.getPrimitiveType().getBuiltinTypeCode(), SchemaType.BTC_NOTATION);

        SchemaTypeLoader loader = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[] {typeSystem,
            XmlBeans.getBuiltinTypeSystem()});

        // 7. Validation negative - Test error if QName has bad prefix
        xml = doc_begin + notation7 + doc_end;
        parsedDoc = loader.parse(xml, null, opts);
        assertEquals("Did not find the root element in the Schema", docType, parsedDoc.schemaType());
        errors.clear();
        parsedDoc.validate(opts);
        // Both "prefix not found" and "pattern doesn't match" errors
        assertTrue("Expected validation errors", errors.size() == 2);
        // Unfortunately, can't get the error code because it is logged via an intermediate exception
        assertTrue("Expected prefix not found error", ((XmlError) errors.get(0)).getMessage().
            indexOf("Invalid QName") >= 0);
        assertEquals(XmlError.SEVERITY_ERROR, ((XmlError) errors.get(0)).getSeverity());
//        System.out.println(xml);

        // 8. Validation negative - Test error if QName has correct prefix but not in enumeration
        xml = doc_begin + notation8 + doc_end;
        parsedDoc = loader.parse(xml, null, opts);
        assertEquals("Did not find the root element in the Schema", docType, parsedDoc.schemaType());
        errors.clear();
        parsedDoc.validate(opts);
        assertTrue("Expected validation errors", errors.size() == 1);
        assertEquals("Expected prefix not found error", XmlErrorCodes.DATATYPE_ENUM_VALID,
            ((XmlError) errors.get(0)).getErrorCode());
        assertEquals(XmlError.SEVERITY_ERROR, ((XmlError) errors.get(0)).getSeverity());
//        System.out.println(xml);

        // 9. Validation negative - Test error if QName doesn't match the extra facet
        xml = doc_begin + notation9 + doc_end;
        parsedDoc = loader.parse(xml, null, opts);
        assertEquals("Did not find the root element in the Schema", docType, parsedDoc.schemaType());
        errors.clear();
        parsedDoc.validate(opts);
        assertTrue("Expected validation errors", errors.size() == 1);
        assertEquals("Expected prefix not found error", XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID,
            ((XmlError) errors.get(0)).getErrorCode());
        assertEquals(XmlError.SEVERITY_ERROR, ((XmlError) errors.get(0)).getSeverity());
//        System.out.println(xml);

        // 10. Validation positive - Test that validation can be performed correctly
        xml = doc_begin + notation10 + doc_end;
        parsedDoc = loader.parse(xml, null, opts);
        assertEquals("Did not find the root element in the Schema", docType, parsedDoc.schemaType());
        errors.clear();
        parsedDoc.validate(opts);
        assertTrue("Expected no validation errors", errors.size() == 0);
//        System.out.println(xml);
    }
}
