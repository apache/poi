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
package tools.inst2xsd.common;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.inst2xsd.Inst2Xsd;
import org.apache.xmlbeans.impl.inst2xsd.Inst2XsdOptions;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

import tools.xml.XmlComparator;
import common.Common;

public class Inst2XsdTestBase extends Common {

    public static tools.inst2xsd.common.Inst2XsdCommon common;
    public static boolean _verbose = true;

    public static final String fwroot = FWROOT;
    public static String caseroot = XBEAN_CASE_ROOT;
    //location of files under "cases folder"
    public static String miscDir = caseroot + P + "tools";
    public static String inst2xsdDir = miscDir + P + "inst2xsd" + P;
    public static String OPTION_CASES_DIR = inst2xsdDir + P + "options" + P;
    public static String SCHEMA_CASES_DIR = inst2xsdDir + P + "schema" + P;
    public static String VALIDATION_CASES_DIR = inst2xsdDir + P + "validation" + P;
    public static final String BASEXML = OPTION_CASES_DIR + "base.xml";
    public static final String EXPBASEXML = OPTION_CASES_DIR + "base0.xsd";


    private static String base_start = "<a xmlns=\"typeTests\">";
    private static String base_end = "</a>";

    private static String attr_base_start = "<a xmlns=\"attrTests\" a=\"";
    private static String attr_base_end = "\" />";


    public Inst2XsdTestBase(String name) {
        super(name);
    }

    public static final String test_getRootFilePath() throws IllegalStateException {
        String root = System.getProperty("xbean.rootdir");
        log("xbean.rootdir: " + root);
        if (root == null)
            throw new IllegalStateException("xbean.rootdir system property not found");

        return root;
    }


    public XmlObject getTypeXml(String val) throws Exception {
        return XmlObject.Factory.parse(setTypeVal(val));
    }

    public String setTypeVal(String val) {
        return base_start + val + base_end;
    }

    public XmlObject getAttrTypeXml(String val) throws Exception {
        return XmlObject.Factory.parse(setAttrVal(val));
    }

    public String setAttrVal(String val) {
        return attr_base_start + val + attr_base_end;
    }

    //attribute testing methods
    public String getAttrTypeXmlVenetian(String primType, String derType) {
        return "<schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
                "targetNamespace=\"attrTests\" xmlns=\"http://www.w3.org/2001/XMLSchema\">" +
                "<element name=\"a\" type=\"att:aType\" xmlns:att=\"attrTests\"/>" +
                "<complexType name=\"aType\">" +
                "<simpleContent>" +
                "<extension base=\"xs:" + primType + "\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
                "<attribute type=\"xs:" + derType + "\" name=\"a\"/>" +
                "</extension>" +
                "</simpleContent></complexType></schema>";
    }

    public String getAttrTypeXmlVenetian(String type) {
        return "<schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
                "targetNamespace=\"attrTests\" xmlns=\"http://www.w3.org/2001/XMLSchema\">" +
                "<element name=\"a\" type=\"att:aType\" xmlns:att=\"attrTests\"/>" +
                "<complexType name=\"aType\">" +
                "<simpleContent>" +
                "<extension base=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
                "<attribute type=\"xs:" + type + "\" name=\"a\"/>" +
                "</extension>" +
                "</simpleContent></complexType></schema>";
    }

    public String getAttrTypeXmlRDandSS(String primType, String derType) {
        return "<schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
                "targetNamespace=\"attrTests\" xmlns=\"http://www.w3.org/2001/XMLSchema\">" +
                "<element name=\"a\">" +
                "<complexType>" +
                "<simpleContent>" +
                "<extension base=\"xs:" + primType + "\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
                "<attribute type=\"xs:" + derType + "\" name=\"a\"/>" +
                "</extension>" +
                "</simpleContent>" +
                "</complexType></element></schema>";
    }

    public String getAttrTypeXmlRDandSS(String type) {
        return "<schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
                "targetNamespace=\"attrTests\" xmlns=\"http://www.w3.org/2001/XMLSchema\">" +
                "<element name=\"a\">" +
                "<complexType>" +
                "<simpleContent>" +
                "<extension base=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
                "<attribute type=\"xs:" + type + "\" name=\"a\"/>" +
                "</extension>" +
                "</simpleContent>" +
                "</complexType></element></schema>";
    }

    public void runAttrTypeChecking(XmlObject act, String expType) throws Exception {

        log("=== Venetian options ===");
        runAttrTypeChecking(act, expType, Inst2XsdCommon.getVenetianOptions());
        log("=== Russian options ===");
        runAttrTypeChecking(act, expType, Inst2XsdCommon.getRussianOptions());
        log("=== Salami options ===");
        runAttrTypeChecking(act, expType, Inst2XsdCommon.getSalamiOptions());
        log("=== Default options ===");
        runAttrTypeChecking(act, expType, Inst2XsdCommon.getDefaultInstOptions());
    }

    public void runAttrTypeChecking(XmlObject act, String primType, String derType) throws Exception {

        log("=== Venetian options ===");
        runAttrTypeChecking(act, primType, derType, Inst2XsdCommon.getVenetianOptions());
        log("=== Russian options ===");
        runAttrTypeChecking(act, primType, derType, Inst2XsdCommon.getRussianOptions());
        log("=== Salami options ===");
        runAttrTypeChecking(act, primType, derType, Inst2XsdCommon.getSalamiOptions());
        log("=== Default options ===");
        runAttrTypeChecking(act, primType, derType, Inst2XsdCommon.getDefaultInstOptions());
    }

    private void runAttrTypeChecking(XmlObject act, String primType, String derType, Inst2XsdOptions opt) throws Exception {
        SchemaDocument[] venetian = (SchemaDocument[]) runInst2Xsd(act, opt);
        checkLength(venetian, 1);

        if (opt.getDesign() == Inst2XsdOptions.DESIGN_RUSSIAN_DOLL ||
                opt.getDesign() == Inst2XsdOptions.DESIGN_SALAMI_SLICE)
            compare(venetian[0], XmlObject.Factory.parse(getAttrTypeXmlRDandSS(primType, derType)));
        else if (opt.getDesign() == Inst2XsdOptions.DESIGN_VENETIAN_BLIND)
            compare(venetian[0], XmlObject.Factory.parse(getAttrTypeXmlVenetian(primType, derType)));
        else
            throw new Exception("Design style was not found");

        checkInstance(venetian, new XmlObject[]{act});

    }

    private void runAttrTypeChecking(XmlObject act, String expType, Inst2XsdOptions opt) throws Exception {
        SchemaDocument[] venetian = (SchemaDocument[]) runInst2Xsd(act, opt);
        checkLength(venetian, 1);

        if (opt.getDesign() == Inst2XsdOptions.DESIGN_RUSSIAN_DOLL ||
                opt.getDesign() == Inst2XsdOptions.DESIGN_SALAMI_SLICE)
            compare(venetian[0], XmlObject.Factory.parse(getAttrTypeXmlRDandSS(expType)));
        else if (opt.getDesign() == Inst2XsdOptions.DESIGN_VENETIAN_BLIND)
            compare(venetian[0], XmlObject.Factory.parse(getAttrTypeXmlVenetian(expType)));
        else
            throw new Exception("Design style was not found");

        checkInstance(venetian, new XmlObject[]{act});

    }

    //element value test methods
    public void runTypeChecking(XmlObject act, String expType) throws Exception {
        log("=== Venetian options ===");
        runTypeChecking(act, expType, Inst2XsdCommon.getVenetianOptions());
        log("=== Russian options ===");
        runTypeChecking(act, expType, Inst2XsdCommon.getRussianOptions());
        log("=== Salami options ===");
        runTypeChecking(act, expType, Inst2XsdCommon.getSalamiOptions());
        log("=== Default options ===");
        runTypeChecking(act, expType, Inst2XsdCommon.getDefaultInstOptions());
    }


    private void runTypeChecking(XmlObject act, String expType, Inst2XsdOptions opt) throws Exception {
        SchemaDocument[] venetian = (SchemaDocument[]) runInst2Xsd(act, opt);
        checkLength(venetian, 1);
        log("actual: " + act);
        log("expType: " + expType);
        checkInstance(venetian, new XmlObject[]{act});
        compare(venetian[0], XmlObject.Factory.parse(getExpTypeXml(expType)));
    }

    public String getExpTypeXml(String type) {
        return "<xs:schema attributeFormDefault=\"unqualified\" " +
                "elementFormDefault=\"qualified\" targetNamespace=\"typeTests\"" +
                " xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >" +
                "<xs:element name=\"a\" type=\"xs:" + type + "\"" +
                " xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" />" +
                "</xs:schema>";
    }

    //type coercion/LCD test methods
    public void runLCDTypeCheckTest(String val1, String val2, String expType) throws Exception {
        log("=== Venetian options ===");
        runLCDTypeChecking(val1, val2, expType, Inst2XsdCommon.getVenetianOptions());
        log("=== Russian options ===");
        runLCDTypeChecking(val1, val2, expType, Inst2XsdCommon.getRussianOptions());
        log("=== Salami options ===");
        runLCDTypeChecking(val1, val2, expType, Inst2XsdCommon.getSalamiOptions());
        log("=== Default options ===");
        runLCDTypeChecking(val1, val2, expType, Inst2XsdCommon.getDefaultInstOptions());
    }

    private void runLCDTypeChecking(String val1, String val2, String expType, Inst2XsdOptions opt) throws Exception {
        XmlObject act = getTypeCoerceXml(val1, val2);
        SchemaDocument[] venetian = (SchemaDocument[]) runInst2Xsd(act, opt);
        checkLength(venetian, 1);
        log("instance: " + act);
        log("expType: " + expType);
        checkInstance(venetian, new XmlObject[]{act});

        if (opt.getDesign() == Inst2XsdOptions.DESIGN_VENETIAN_BLIND)
            compare(venetian[0], getExpLCDXml_vb(expType));
        else if (opt.getDesign() == Inst2XsdOptions.DESIGN_SALAMI_SLICE)
            compare(venetian[0], getExpLCDXml_ss(expType));
        else if (opt.getDesign() == Inst2XsdOptions.DESIGN_RUSSIAN_DOLL)
            compare(venetian[0], getExpLCDXml_rd(expType));
        else
            compare(venetian[0], getExpLCDXml_vb(expType));
    }

    public String getTypeCoerceXmlString(String val1, String val2) {
        return "<a xmlns=\"typeCoercion\">" +
                "    <b c=\"" + val1 + "\">" + val1 + "</b>" +
                "    <b c=\"" + val2 + "\">" + val2 + "</b>" +
                "</a>";
    }

    public XmlObject getTypeCoerceXml(String val1, String val2) throws XmlException {
        return XmlObject.Factory.parse(getTypeCoerceXmlString(val1, val2));
    }


    public XmlObject getExpLCDXml_vb(String type) throws XmlException {
        return XmlObject.Factory.parse("<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" targetNamespace=\"typeCoercion\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "  <xs:element name=\"a\" type=\"typ:aType\" xmlns:typ=\"typeCoercion\"/>\n" +
                "  <xs:complexType name=\"aType\">\n" +
                "    <xs:sequence>\n" +
                "      <xs:element type=\"typ:bType\" name=\"b\" maxOccurs=\"unbounded\" minOccurs=\"0\" xmlns:typ=\"typeCoercion\"/>\n" +
                "    </xs:sequence>\n" +
                "  </xs:complexType>\n" +
                "  <xs:complexType name=\"bType\">\n" +
                "    <xs:simpleContent>\n" +
                "      <xs:extension base=\"xs:" + type + "\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "        <xs:attribute type=\"xs:" + type + "\" name=\"c\" use=\"optional\"/>\n" +
                "      </xs:extension>\n" +
                "    </xs:simpleContent>\n" +
                "  </xs:complexType>\n" +
                "</xs:schema>");
    }

    public XmlObject getExpLCDXml_ss(String type) throws XmlException {
        return XmlObject.Factory.parse("<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" targetNamespace=\"typeCoercion\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "  <xs:element name=\"b\">\n" +
                "    <xs:complexType>\n" +
                "      <xs:simpleContent>\n" +
                "        <xs:extension base=\"xs:"+type+"\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "          <xs:attribute type=\"xs:"+type+"\" name=\"c\" use=\"optional\"/>\n" +
                "        </xs:extension>\n" +
                "      </xs:simpleContent>\n" +
                "    </xs:complexType>\n" +
                "  </xs:element>\n" +
                "  <xs:element name=\"a\">\n" +
                "    <xs:complexType>\n" +
                "      <xs:sequence>\n" +
                "        <xs:element ref=\"typ:b\" maxOccurs=\"unbounded\" minOccurs=\"0\" xmlns:typ=\"typeCoercion\"/>\n" +
                "      </xs:sequence>\n" +
                "    </xs:complexType>\n" +
                "  </xs:element>\n" +
                "</xs:schema>");
    }

    public XmlObject getExpLCDXml_rd(String type) throws XmlException {
        return XmlObject.Factory.parse("<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" targetNamespace=\"typeCoercion\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "  <xs:element name=\"a\">\n" +
                "    <xs:complexType>\n" +
                "      <xs:sequence>\n" +
                "        <xs:element name=\"b\" maxOccurs=\"unbounded\" minOccurs=\"0\">\n" +
                "          <xs:complexType>\n" +
                "            <xs:simpleContent>\n" +
                "              <xs:extension base=\"xs:" + type + "\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "                <xs:attribute type=\"xs:" + type + "\" name=\"c\" use=\"optional\"/>\n" +
                "              </xs:extension>\n" +
                "            </xs:simpleContent>\n" +
                "          </xs:complexType>\n" +
                "        </xs:element>\n" +
                "      </xs:sequence>\n" +
                "    </xs:complexType>\n" +
                "  </xs:element>\n" +
                "</xs:schema>");
    }


    public static XmlObject[] runInst2Xsd(String inst) throws XmlException {
        return runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(inst, Inst2XsdCommon.getXmlOptions())},
                Inst2XsdCommon.getDefaultInstOptions());
    }

    public static XmlObject[] runInst2Xsd(XmlObject inst) {
        return Inst2Xsd.inst2xsd(new XmlObject[]{inst}, Inst2XsdCommon.getDefaultInstOptions());
    }

    public static XmlObject[] runInst2Xsd(XmlObject[] inst) {
        return Inst2Xsd.inst2xsd(inst, Inst2XsdCommon.getDefaultInstOptions());
    }

    public static XmlObject[] runInst2Xsd(XmlObject inst, Inst2XsdOptions options) {
        return Inst2Xsd.inst2xsd(new XmlObject[]{inst}, options);
    }

    public static XmlObject[] runInst2Xsd(XmlObject[] inst, Inst2XsdOptions options) {
        return Inst2Xsd.inst2xsd(inst, options);
    }


    public static SchemaDocument[] getSchemaDoc(XmlObject[] inst) throws XmlException {
        SchemaDocument[] docs = new SchemaDocument[inst.length];
        for (int i = 0; i < docs.length; i++) {
            docs[i] = SchemaDocument.Factory.parse(inst[i].xmlText());
        }
        return docs;

    }

    public static void runSchemaBuild(XmlObject inst, Inst2XsdOptions opts, XmlObject exp) throws Exception {
        XmlObject[] genSchema = runInst2Xsd(inst, opts);
        log(genSchema);
        checkInstanceToAll(genSchema, inst, exp);
        checkLength(genSchema, 1);
        compare(genSchema[0], exp);

    }

    public static void runSchemaBuild(XmlObject inst, Inst2XsdOptions opts, XmlObject[] exp) throws Exception {
        XmlObject[] genSchema = runInst2Xsd(inst, opts);
        log(genSchema);
        checkInstanceToAll(genSchema, new XmlObject[]{inst}, exp);
        checkLength(genSchema, exp.length);
        compare(genSchema, exp);

    }

    public static void runSchemaBuild(XmlObject[] inst, Inst2XsdOptions opts, XmlObject[] exp) throws Exception {
        XmlObject[] genSchema = runInst2Xsd(inst, opts);
        checkInstanceToAll(genSchema, inst, exp);
        log(genSchema);
        compare(genSchema, exp);

    }

    //TODO: Make this error narrowed
    public static void checkLength(Object[] obj, int val) throws Exception {
        log("Length = " + obj.length + " exp: " + val);

        if (obj.length == val) {
            return;
        } else if (obj.length < val) {
            throw new Exception("Actual was smaller than expected");
        } else if (obj.length > val) {
            throw new Exception("Actual was larger than expected");
        } else {
            throw new Exception("Array Indexes did not compare correctly");
        }


    }


    public static void compare(XmlObject[] act, XmlObject[] exp) throws XmlException, Exception {
        checkLength(act, exp.length);
        //Arrays.sort(act);
        //Arrays.sort(exp);
        //if (Arrays.equals(act, exp)){
        //    return;
        //}else{
        for (int i = 0; i < act.length; i++) {
            compare(act[i], exp[i]);
        }

    }

    public static void compare(XmlObject act, XmlObject exp)
            throws XmlException, Exception {
        XmlComparator.Diagnostic diag = XmlComparator.lenientlyCompareTwoXmlStrings(act.xmlText(Inst2XsdCommon.getXmlOptions()),
                exp.xmlText(Inst2XsdCommon.getXmlOptions()));
        if (diag.hasMessage()) {
            log("Expected: \n" + exp.xmlText(Inst2XsdCommon.getXmlOptions()));
            log("Actual: \n" + act.xmlText(Inst2XsdCommon.getXmlOptions()));
            throw new Exception("Xml Comparison Failed:\n" + diag.toString());
        }
    }

    public static void log(XmlObject[] doc) {
        if (_verbose) {
            for (int i = 0; i < doc.length; i++) {
                log("Schema[" + i + "] - " + doc[i].xmlText(Inst2XsdCommon.getXmlOptions()));
            }
        }
    }

    public static void log(String msg) {
        if (_verbose)
            System.out.println(msg);
    }

    public static void log(XmlObject obj) {
        if (_verbose)
            System.out.println(obj.xmlText(Inst2XsdCommon.getXmlOptions()));
    }


    public static boolean checkInstanceToAll(XmlObject[] actSchemaDoc, XmlObject inst,
                                             XmlObject expSchemas) throws Exception {
        return checkInstanceToAll(getSchemaDoc(actSchemaDoc), new XmlObject[]{inst}, getSchemaDoc(new XmlObject[]{expSchemas}));
    }

    public static boolean checkInstanceToAll(XmlObject[] actSchemaDoc, XmlObject[] inst,
                                             XmlObject[] expSchemas) throws Exception {
        return checkInstanceToAll(getSchemaDoc(actSchemaDoc), inst, getSchemaDoc(expSchemas));
    }

    public static boolean checkInstanceToAll(XmlObject[] actSchemaDoc, XmlObject[] inst,
                                             XmlObject expSchemas) throws Exception {
        return checkInstanceToAll(getSchemaDoc(actSchemaDoc), inst, getSchemaDoc(new XmlObject[]{expSchemas}));
    }

    public static boolean checkInstanceToAll(SchemaDocument[] actSchemaDoc, XmlObject inst,
                                             SchemaDocument expSchemas) throws Exception {
        return checkInstanceToAll(actSchemaDoc, new XmlObject[]{inst}, getSchemaDoc(new XmlObject[]{expSchemas}));
    }

    public static boolean checkInstanceToAll(SchemaDocument[] actSchemaDoc, XmlObject inst,
                                             SchemaDocument[] expSchemas) throws Exception {
        return checkInstanceToAll(actSchemaDoc, new XmlObject[]{inst}, expSchemas);
    }

    public static boolean checkInstanceToAll(SchemaDocument[] actSchemaDoc, XmlObject[] inst,
                                             SchemaDocument[] expSchemas) throws Exception {
        log("-= Comparing Actual to instance=-");
        if (checkInstance(actSchemaDoc, inst))
            log("-= Instance validated actual =-");

        log("-= Comparing Expected to instance=-");
        if (checkInstance(expSchemas, inst))
            log("-= Instance validated Expected =-");

        return true;
    }

    public static boolean checkInstance(SchemaDocument[] sDocs, XmlObject[] inst) throws Exception {
        if (validateInstances(sDocs, inst)) {
            return true;
        } else {
            throw new Exception("Instance Failed to validate");
        }
    }

    /**
     * Copied from inst2Xsd as option may be removed
     *
     * @param sDocs
     * @param instances
     * @return
     */
    public static boolean validateInstances(SchemaDocument[] sDocs, XmlObject[] instances) {

        SchemaTypeLoader sLoader;
        Collection compErrors = new ArrayList();
        XmlOptions schemaOptions = new XmlOptions();
        schemaOptions.setErrorListener(compErrors);
        try {
            sLoader = XmlBeans.loadXsd(sDocs, schemaOptions);
        } catch (Exception e) {
            if (compErrors.isEmpty() || !(e instanceof XmlException)) {
                e.printStackTrace(System.out);
            }
            System.out.println("Schema invalid");
            for (Iterator errors = compErrors.iterator(); errors.hasNext();)
                System.out.println(errors.next());
            return false;
        }

        boolean result = true;

        for (int i = 0; i < instances.length; i++) {
            String instance = instances[i].toString();

            XmlObject xobj;

            try {
                xobj = sLoader.parse(instance, null, new XmlOptions().setLoadLineNumbers());
            } catch (XmlException e) {
                System.out.println("Error:\n" + instance + " not loadable: " + e);
                e.printStackTrace(System.out);
                result = false;
                continue;
            }

            Collection errors = new ArrayList();

            if (xobj.schemaType() == XmlObject.type) {
                System.out.println(instance + " NOT valid.  ");
                System.out.println("  Document type not found.");
                result = false;
            } else if (xobj.validate(new XmlOptions().setErrorListener(errors)))
                System.out.println("Instance[" + i + "] valid.");
            else {
                System.out.println("Instance[" + i + "] NOT valid.");
                for (Iterator it = errors.iterator(); it.hasNext();) {
                    System.out.println("    " + it.next());
                }
                result = false;
            }
        }

        return result;
    }

}
