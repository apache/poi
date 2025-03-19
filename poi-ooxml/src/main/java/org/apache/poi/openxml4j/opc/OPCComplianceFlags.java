/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.openxml4j.opc;

/**
 * Allows disabling specific OPC compliance rules.
 * By default, rules M4.2, M4.3, M4.4, and M4.5 are all enforced which will prevent
 * non-compliant documents from being parsed.
 *
 * Consumers may disable these compliance checks individually or as a whole at their
 * own discretion to allow certain non-compliant documents to be parsed.
 * @since POI 5.4.1
 */
public class OPCComplianceFlags {

    /*
     * Rule M4.2: A format consumer shall consider the use of the Markup
     * Compatibility namespace to be an error.
     */
    protected boolean ENFORCE_M4_2_FORBID_MARKUP_COMPATIBILITY_NAMESPACE;

    /**
     * Rule M4.3: Producers shall not create a document element that contains
     * refinements to the Dublin Core elements, except for the two specified in
     * the schema: &lt;dcterms:created&gt; and &lt;dcterms:modified&gt; Consumers shall
     * consider a document element that violates this constraint to be an error.
     */
    protected boolean ENFORCE_M4_3_FORBID_REFINING_DUBLIN_CORE_ELEMENTS;

    /**
     * Rule M4.4: Producers shall not create a document element that contains
     * the xml:lang attribute. Consumers shall consider a document element that
     * violates this constraint to be an error.
     */
    protected boolean ENFORCE_M4_4_FORBID_XML_LANG_ATTRIBUTE;

    /*
     * Rule M4.5: Producers shall not create a document element that contains
     * the xsi:type attribute, except for a &lt;dcterms:created&gt; or
     * &lt;dcterms:modified&gt; element where the xsi:type attribute shall be present
     * and shall hold the value dcterms:W3CDTF, where dcterms is the namespace
     * prefix of the Dublin Core namespace. Consumers shall consider a document
     * element that violates this constraint to be an error.
     */
    protected boolean ENFORCE_M4_5_RESTRICT_XSI_TYPE_ATTRIBUTE;

    private OPCComplianceFlags(
            boolean forbidMarkupCompatibilityNamespace,
            boolean forbidRefiningDublinCoreElements,
            boolean forbidXmlLangAttribute,
            boolean restrictXsiTypeAttribute
    ) {
        this.ENFORCE_M4_2_FORBID_MARKUP_COMPATIBILITY_NAMESPACE = forbidMarkupCompatibilityNamespace;
        this.ENFORCE_M4_3_FORBID_REFINING_DUBLIN_CORE_ELEMENTS = forbidRefiningDublinCoreElements;
        this.ENFORCE_M4_4_FORBID_XML_LANG_ATTRIBUTE = forbidXmlLangAttribute;
        this.ENFORCE_M4_5_RESTRICT_XSI_TYPE_ATTRIBUTE = restrictXsiTypeAttribute;
    }

    public static OPCComplianceFlags enforceAll() {
        return new OPCComplianceFlags(true, true, true, true);
    }

    public static OPCComplianceFlags disableAll() {
        return new OPCComplianceFlags(false, false, false, false);
    }

    public OPCComplianceFlags setForbidMarkupCompatibilityNamespace(boolean flag) {
        ENFORCE_M4_2_FORBID_MARKUP_COMPATIBILITY_NAMESPACE = flag;
        return this;
    }

    public OPCComplianceFlags setForbidRefiningDublinCoreElements(boolean flag) {
        ENFORCE_M4_3_FORBID_REFINING_DUBLIN_CORE_ELEMENTS = flag;
        return this;
    }

    public OPCComplianceFlags setForbidXmlLangAttribute(boolean flag) {
        ENFORCE_M4_4_FORBID_XML_LANG_ATTRIBUTE = flag;
        return this;
    }

    public OPCComplianceFlags setRestrictXsiTypeAttribute(boolean flag) {
        ENFORCE_M4_5_RESTRICT_XSI_TYPE_ATTRIBUTE = flag;
        return this;
    }

    public boolean getForbidMarkupCompatibilityNamespace() {
        return ENFORCE_M4_2_FORBID_MARKUP_COMPATIBILITY_NAMESPACE;
    }

    public boolean getForbidRefiningDublinCoreElements() {
        return ENFORCE_M4_3_FORBID_REFINING_DUBLIN_CORE_ELEMENTS;
    }

    public boolean getForbidXmlLangAttributes() {
        return ENFORCE_M4_4_FORBID_XML_LANG_ATTRIBUTE;
    }

    public boolean getRestrictXsiTypeAttribute() {
        return ENFORCE_M4_5_RESTRICT_XSI_TYPE_ATTRIBUTE;
    }

}
