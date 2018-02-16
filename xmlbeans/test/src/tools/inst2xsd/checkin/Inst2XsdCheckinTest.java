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
package tools.inst2xsd.checkin;

import tools.inst2xsd.common.Inst2XsdTestBase;

import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.inst2xsd.Inst2XsdOptions;

public class Inst2XsdCheckinTest extends Inst2XsdTestBase {

    public Inst2XsdCheckinTest(String name) {
        super(name);
    }

    public void test_simpleName() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "simpleName.xml"));
        runSchemaBuild(inst, common.getRussianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "simpleName_rd0.xsd")));
        runSchemaBuild(inst, common.getSalamiOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "simpleName_ss0.xsd")));
        runSchemaBuild(inst, common.getVenetianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "simpleName_vb0.xsd")));

    }

    public void test_ns_duplicate_russian() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "duplicatedNS.xml"));
        runSchemaBuild(inst, common.getRussianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "duplicatedNS_rd0.xsd")));

    }

    public void test_ns_multipleNested_russian() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNestedNS.xml"));
        runSchemaBuild(inst, common.getRussianOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNestedNS_rd0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNestedNS_rd1.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNestedNS_rd2.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNestedNS_rd3.xsd"))
        });
    }

    public void test_ns_multipleNested_salami() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNestedNS.xml"));

        runSchemaBuild(inst, common.getSalamiOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNestedNS_ss0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNestedNS_ss1.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNestedNS_ss2.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNestedNS_ss3.xsd"))
        });
    }

    public void test_ns_multipleNested_venetian() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNestedNS.xml"));

        runSchemaBuild(inst, common.getVenetianOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNestedNS_vb0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNestedNS_vb1.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNestedNS_vb2.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNestedNS_vb3.xsd"))
        });
    }

    public void test_ns_multiple_russian() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNS.xml"));

        runSchemaBuild(inst, common.getRussianOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNS_rd0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNS_rd1.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNS_rd2.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNS_rd3.xsd"))
        });
    }

    public void test_ns_multiple_salami() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNS.xml"));

        runSchemaBuild(inst, common.getSalamiOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNS_ss0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNS_ss1.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNS_ss2.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNS_ss3.xsd"))
        });
    }


    public void test_ns_multiple_venetian() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNS.xml"));

        runSchemaBuild(inst, common.getVenetianOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNS_vb0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNS_vb1.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNS_vb2.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "multipleNS_vb3.xsd"))
        });
    }

    public void test_ns_simple_russian() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "simple.xml"));

        runSchemaBuild(inst, common.getRussianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "simple_rd0.xsd")));
    }

    public void test_ns_simple_salami() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "simple.xml"));

        runSchemaBuild(inst, common.getSalamiOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "simple_ss0.xsd")));
    }

    public void test_ns_simple_venetian() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "simple.xml"));

        runSchemaBuild(inst, common.getVenetianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "simple_vb0.xsd")));
    }

    public void test_ns_must_venetian() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "must.xml"));

        runSchemaBuild(inst, common.getVenetianOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "must_vb0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "must_vb1.xsd"))});
    }

    public void test_ns_must_russian() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "must.xml"));

        runSchemaBuild(inst, common.getRussianOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "must_rd0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "must_rd1.xsd"))});
    }

    public void test_ns_must_salami() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "must.xml"));

        runSchemaBuild(inst, common.getSalamiOptions(), new XmlObject[]{
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "must_ss0.xsd")),
            XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "namespaces" + P + "must_ss1.xsd"))});
    }

    public void test_examples_cd_catalog() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "cd_catalog.xml"));

        log("-= Venetian Options =-");
        runSchemaBuild(inst, common.getVenetianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "cd_catalog_vb0.xsd")));

        log("-= Russian Options =-");
        runSchemaBuild(inst, common.getRussianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "cd_catalog_rd0.xsd")));
        log("-= Salami Options =-");
        runSchemaBuild(inst, common.getSalamiOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "cd_catalog_ss0.xsd")));
    }


    public void test_examples_cdcatalog() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "cdcatalog.xml"));

        log("-= Venetian Options =-");
        runSchemaBuild(inst, common.getVenetianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "cdcatalog_vb0.xsd")));
        log("-= Russian Options =-");
        runSchemaBuild(inst, common.getRussianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "cdcatalog_rd0.xsd")));
        log("-= Salami Options =-");
        runSchemaBuild(inst, common.getSalamiOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "cdcatalog_ss0.xsd")));
    }

    //component
    public void test_examples_component() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "component.xml"));

        log("-= Venetian Options =-");
        runSchemaBuild(inst, common.getVenetianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "component_vb0.xsd")));
        log("-= Russian Options =-");
        runSchemaBuild(inst, common.getRussianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "component_rd0.xsd")));
        log("-= Salami Options =-");
        runSchemaBuild(inst, common.getSalamiOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "component_ss0.xsd")));
    }


    public void test_examples_htmlexample() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "html_example.xml"));

        log("-= Russian Options =-");
        runSchemaBuild(inst, common.getRussianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "html_example_rd0.xsd")));
        log("-= Venetian Options =-");
        runSchemaBuild(inst, common.getVenetianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "html_example_vb0.xsd")));
        log("-= Salami Options =-");
        runSchemaBuild(inst, common.getSalamiOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "html_example_ss0.xsd")));
    }


    public void test_examples_slashdotxml_neverenum() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "slashdot.xml"));
        Inst2XsdOptions opt = common.getSalamiOptions();
        opt.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);

        runSchemaBuild(inst, opt,
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "slashdotxml_neverenum_ss0.xsd")));
    }


    public void test_examples_slashdotxml() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "slashdot.xml"));

        log("-= Russian Options =-");
        runSchemaBuild(inst, common.getRussianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "slashdotxml_rd0.xsd")));
        log("-= Venetian Options =-");
        runSchemaBuild(inst, common.getVenetianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "slashdotxml_vb0.xsd")));
        log("-= Salami Options =-");
        runSchemaBuild(inst, common.getSalamiOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "slashdotxml_ss0.xsd")));
    }






    public void test_examples_rss2_vb_contentsmart_4enum() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "rss2sample.xml"));
        Inst2XsdOptions opt = common.getVenetianOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_SMART);
        opt.setUseEnumerations(4);

        runSchemaBuild(inst, opt,
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "rss2sample_contentsmart_4enum_vb0.xsd")));
    }

    public void test_examples_rss2_vb_contentsmart_nevereum() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "rss2sample.xml"));
        Inst2XsdOptions opt = common.getVenetianOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_SMART);
        opt.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);

        runSchemaBuild(inst, opt,
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "rss2sample_contentsmart_neverenum_vb0.xsd")));
    }

    public void test_examples_rss2_vb_contentstring_nevereum() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "rss2sample.xml"));
        Inst2XsdOptions opt = common.getVenetianOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
        opt.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);

        runSchemaBuild(inst, opt,
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "rss2sample_contentstring_neverenum_vb0.xsd")));
    }

    public void test_examples_rss2_vb_contentstring_4enum() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "rss2sample.xml"));
        Inst2XsdOptions opt = common.getVenetianOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
        opt.setUseEnumerations(4);

        runSchemaBuild(inst, opt,
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "rss2sample_contentstring_4enum_vb0.xsd")));
    }

    public void test_examples_rss2_vb_contentstring() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "rss2sample.xml"));
        Inst2XsdOptions opt = common.getVenetianOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);

        runSchemaBuild(inst, opt,
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "rss2sample_contentstring_vb0.xsd")));
    }


    public void test_examples_rss2_vb_neverenum() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "rss2sample.xml"));
        Inst2XsdOptions opt = common.getVenetianOptions();
        opt.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);

        runSchemaBuild(inst, opt,
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "rss2sample_neverenum_vb0.xsd")));
    }

    public void test_examples_rss2() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "rss2sample.xml"));

        log("-= Russian Options =-");
        runSchemaBuild(inst, common.getRussianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "rss2sample_rd0.xsd")));
        log("-= Venetian Options =-");
        runSchemaBuild(inst, common.getVenetianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "rss2sample_vb0.xsd")));
        log("-= Salami Options =-");
        runSchemaBuild(inst, common.getSalamiOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "rss2sample_ss0.xsd")));
    }

    public void test_examples_rss091() throws Exception {
        XmlObject inst = XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "sampleRss091.xml"));

        log("-= Russian Options =-");
        runSchemaBuild(inst, common.getRussianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "sampleRss091_rd0.xsd")));
        log("-= Venetian Options =-");
        runSchemaBuild(inst, common.getVenetianOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "sampleRss091_vb0.xsd")));
        log("-= Salami Options =-");
        runSchemaBuild(inst, common.getSalamiOptions(),
                XmlObject.Factory.parse(new File(SCHEMA_CASES_DIR + "examples" + P + "sampleRss091_ss0.xsd")));
    }
}
