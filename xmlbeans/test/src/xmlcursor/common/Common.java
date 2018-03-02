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



package xmlcursor.common;



/**
 *
 */
public class Common {
    public final static String XMLFRAG_BEGINTAG = "<xml-fragment>";
    public final static String XMLFRAG_ENDTAG = "</xml-fragment>";

    public final static String XML_FOO = "<foo></foo>";
    public final static String XML_FOO_1ATTR = "<foo attr0=\"val0\"></foo>";
    public final static String XML_FOO_TEXT = "<foo>text</foo>";
    public final static String XML_FOO_1ATTR_TEXT = "<foo attr0=\"val0\">text</foo>";
    public final static String XML_FOO_2ATTR = "<foo attr0=\"val0\" attr1=\"val1\"></foo>";
    public final static String XML_FOO_2ATTR_TEXT = "<foo attr0=\"val0\" attr1=\"val1\">text</foo>";
    public final static String XML_FOO_5ATTR_TEXT = "<foo attr0=\"val0\" attr1=\"val1\"  attr2=\"val2\"  attr3=\"val3\"  attr4=\"val4\">text</foo>";
    public final static String XML_FOO_BAR = "<foo><bar></bar></foo>";
    public final static String XML_FOO_BAR_TEXT = "<foo><bar>text</bar></foo>";
    public final static String XML_FOO_BAR_TEXT_EXT = "<foo><bar>text</bar>extended</foo>";
    public final static String XML_FOO_BAR_WS_TEXT = "<foo><bar> text </bar> ws \\r\\n </foo>";
    public final static String XML_FOO_BAR_WS_ONLY = "<foo> <bar> </bar> </foo>";
    public final static String XML_FOO_NS = "<foo xmlns=\"http://www.foo.org\"></foo>";
    public final static String XML_FOO_NS_PREFIX = "<foo xmlns:edi='http://ecommerce.org/schema'><!-- the 'price' element's namespace is http://ecommerce.org/schema -->  <edi:price units='Euro'>32.18</edi:price></foo>";
    public final static String XML_FOO_BAR_SIBLINGS = "<foo><bar>text0</bar><bar>text1</bar></foo>";
    public final static String XML_FOO_BAR_NESTED_SIBLINGS = "<foo attr0=\"val0\"><bar>text0<zed>nested0</zed></bar><bar>text1<zed>nested1</zed></bar></foo>";
    public final static String XML_FOO_PROCINST = "<?xml-stylesheet type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"?><foo>text</foo>";
    public final static String XML_FOO_COMMENT = "<!-- comment text --><foo>text</foo>";
    public final static String XML_FOO_DIGITS = "<foo xmlns=\"http://www.foo.org\" attr0=\"val0\">01234</foo>";
    public final static String XML_TEXT_MIDDLE = "<foo><bar>text</bar>extended<goo>text1</goo></foo>";

    public final static String XML_ATTR_TEXT = "<foo x=\"y\">ab</foo> ";



    public final static String TRANXML_DIR = "tranxml/Version4.0/";
    public final static String TRANXML_SCHEMAS_DIR = TRANXML_DIR + "schemas/StandAlone/";
    public final static String TRANXML_EXAMPLEDOCS_DIR = TRANXML_DIR + "ExampleDocs/";
    public final static String CLM_NS = "http://www.tranxml.org/TranXML/Version4.0";
    public final static String CLM_XSI_NS = "xmlns:xsi=\"http://www.w3.org/2000/10/XMLSchema-instance\"";
    public final static String CLM_NS_XQUERY_DEFAULT = "declare default element namespace \"" + CLM_NS + "\"; ";

    public final static String TRANXML_FILE_CLM = TRANXML_EXAMPLEDOCS_DIR + "CarLocationMessage.xml";
    public final static String TRANXML_FILE_RBL = TRANXML_EXAMPLEDOCS_DIR + "RailBillOfLading.xml";
    public final static String TRANXML_FILE_SRCWBI = TRANXML_EXAMPLEDOCS_DIR + "SimpleRailCarrierWaybillInterchange.xml";
    public final static String TRANXML_FILE_TOAIRA = TRANXML_EXAMPLEDOCS_DIR + "TerminalOperationsAndIntermodalRampActivity.xml";
    public final static String TRANXML_FILE_XMLCURSOR_PO = "xbean/xmlcursor/po.xml";


    public final static String XML_SCHEMA_TYPE_SUFFIX = "http://www.w3.org/2001/XMLSchema";
    public final static String TRANXML_SCHEMA_TYPE_SUFFIX = CLM_NS;

    public static final String XMLCASES_JAR = "xmlcases.jar";
    public static final String XSDCASES_JAR = "xsdcases.jar";

    public static final String XMLCURSOR_JAR = "xmlcursor.jar";
    public static final String CARLOCATIONMESSAGE_JAR = "CarLocationMessage_40_LX.xsd.jar";
    public static final String RAILBILLOFLADING_JAR = "RailBillOfLading_V40_LX.xsd.jar";
    public static final String SIMPLERAILCARRIERWAYBILLINTERCHANGE_JAR = "SimpleRailCarrierWaybillInterchange_V40_LX.xsd.jar";
    public static final String TERMINALOPERATIONSANDINTERMODALRAMPACTIVITY_JAR = "TerminalOperationsAndIntermodalRampActivity_V40_LX.xsd.jar";

    public static String wrapInXmlFrag(String text) {
        return XMLFRAG_BEGINTAG + text + XMLFRAG_ENDTAG;
    }

}

