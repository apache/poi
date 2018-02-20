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
package tools.inst2xsd.detailed;

import tools.inst2xsd.common.Inst2XsdTestBase;

import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.inst2xsd.Inst2XsdOptions;

public class ComplexDetailedTest extends Inst2XsdTestBase {

    public ComplexDetailedTest(String name) {
        super(name);
    }

    public void test_complex_enum() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "enum.xml"));

        log("-= Russian Options =-");
        runSchemaBuild(inst, common.getRussianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "enum2_rd0.xsd")));
        log("-= Venetian Options =-");
        runSchemaBuild(inst, common.getVenetianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "enum2_vb0.xsd")));
        log("-= Salami Options =-");
        runSchemaBuild(inst, common.getSalamiOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "enum2_ss0.xsd")));
    }

    public void test_complex_enum_never() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "enum.xml"));

        Inst2XsdOptions opts = common.getVenetianOptions();
        opts.setUseEnumerations(opts.ENUMERATION_NEVER);
        log("-= Never Enum Options =-");
        runSchemaBuild(inst, opts,
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "enum_vb_eN0.xsd")));

        opts = null;
        opts = common.getVenetianOptions();
        opts.setSimpleContentTypes(opts.SIMPLE_CONTENT_TYPES_STRING);
        log("-= SCS Options =-");
        runSchemaBuild(inst, opts,
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "enum_vb_scs_enum0.xsd")));

    }

    public void test_complex_qname_enum() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "enumQName.xml"));

        Inst2XsdOptions opts = common.getVenetianOptions();
        log("-= Enum Options =-");
        runSchemaBuild(inst, opts,
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "enumQName_vb_enum0.xsd")));

        log("-= Enum Options =-");
        opts.setUseEnumerations(opts.ENUMERATION_NEVER);
        runSchemaBuild(inst, opts,
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "enumQName_vb_eN0.xsd")));
    }

    public void test_complex_nestedNSArray() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedNSArray.xml"));

        log("-= Russian Options =-");
        runSchemaBuild(inst, common.getRussianOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_rd0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_rd1.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_rd2.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_rd3.xsd"))
        });
        log("-= Venetian Options =-");
        runSchemaBuild(inst, common.getVenetianOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_vb0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_vb1.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_vb2.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_vb3.xsd"))
        });
        log("-= Salami Options =-");
        runSchemaBuild(inst, common.getSalamiOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_ss0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_ss1.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_ss2.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "nestedNSArray" + P + "nestedArray_ss3.xsd"))
        });
    }

    public void test_example_po() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "po" + P + "po.xml"));

        log("-= Russian Options =-");
        Inst2XsdOptions rdEN = common.getRussianOptions();
        rdEN.setUseEnumerations(rdEN.ENUMERATION_NEVER);
        runSchemaBuild(inst, rdEN,
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "po" + P + "po_rd_eN0.xsd")));

        log("-= Venetian Options =-");
        runSchemaBuild(inst, common.getVenetianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "po" + P + "po_vb0.xsd")));
        log("-= Salami Options =-");
        runSchemaBuild(inst, common.getSalamiOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "po" + P + "po_ss0.xsd")));

        Inst2XsdOptions opts = common.getVenetianOptions();
        opts.setUseEnumerations(opts.ENUMERATION_NEVER);
        log("-= Venetian Never Enum Options =-");
        runSchemaBuild(inst, opts,
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "po" + P + "po_vb_eN0.xsd")));

        opts.setDesign(opts.DESIGN_SALAMI_SLICE);
        log("-= Salami Never Enum Options =-");
        runSchemaBuild(inst, opts,
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "po" + P + "po_ss_eN0.xsd")));

        opts.setDesign(opts.DESIGN_VENETIAN_BLIND);
        opts.setSimpleContentTypes(opts.SIMPLE_CONTENT_TYPES_STRING);
        opts.setUseEnumerations(opts.ENUMERATION_NEVER);
        log("-= Venetian Never Enum SimpleContentString Options =-");
        runSchemaBuild(inst, opts,
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "po" + P + "po_vb_scs0.xsd")));
        opts = null;
        opts = common.getVenetianOptions();
        opts.setSimpleContentTypes(opts.SIMPLE_CONTENT_TYPES_STRING);
        log("-= Venetian SimpleContentString with Enum Options =-");
        runSchemaBuild(inst, opts,
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "po" + P + "po_vb_scs_enum0.xsd")));
    }

    /**
     * java.lang.IllegalStateException: Not on a container
     */
    public void test_complex_attrenum() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr"+P+"attrenum.xml"));

        log("-= Russian Options =-");
        runSchemaBuild(inst, common.getRussianOptions(),
                new XmlObject[]{
                    XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_rd0.xsd")),
                    XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_rd1.xsd")),
                    XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_rd2.xsd"))
                });

        log("-= Venetian Options =-");
        runSchemaBuild(inst, common.getVenetianOptions(),
                new XmlObject[]{
                    XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_vb0.xsd")),
                    XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_vb1.xsd")),
                    XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_vb2.xsd"))
                });


        log("-= Salami Options =-");
        runSchemaBuild(inst, common.getSalamiOptions(),
                new XmlObject[]{
                    XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_ss0.xsd")),
                    XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_ss1.xsd")),
                    XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_ss2.xsd"))
                });

        log("-= Russian enum Options =-");
        Inst2XsdOptions opts = common.getRussianOptions();
        opts.setUseEnumerations(opts.ENUMERATION_NEVER);
        runSchemaBuild(inst, opts,
                new XmlObject[]{
                    XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_rd_enum0.xsd")),
                    XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_rd_enum1.xsd")),
                    XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_rd_enum2.xsd"))
                });

        log("-= Venetian enum Options =-");
        opts = null;
        opts = common.getVenetianOptions();
        opts.setUseEnumerations(opts.ENUMERATION_NEVER);
        runSchemaBuild(inst, opts,
                new XmlObject[]{
                    XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_vb_enum0.xsd")),
                    XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_vb_enum1.xsd")),
                    XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "complex" + P + "enum" + P + "attr" + P + "attrenum_vb_enum2.xsd"))
                });
    }

}
