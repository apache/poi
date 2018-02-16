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

package org.apache.xmlbeans;

public abstract class XmlErrorCodes
{

    public static final int MISMATCHED_TARGET_NAMESPACE       =   4;
    public static final int CANNOT_FIND_RESOURCE              =  56;
    public static final int REDUNDANT_NESTED_TYPE             =   7;
    public static final int ELEMENT_MISSING_NAME              =   8;
    public static final int REDUNDANT_DEFAULT_FIXED           =   9;
    public static final int MODEL_GROUP_MISSING_NAME          =  10;
    public static final int ATTRIBUTE_GROUP_MISSING_NAME      =  12;
    public static final int CYCLIC_DEPENDENCY                 =  13;
    public static final int FACET_DUPLICATED                  =  19;
    public static final int FACET_VALUE_MALFORMED             =  20;
    public static final int MALFORMED_NUMBER                  =  21;
    public static final int REDUNDANT_CONTENT_MODEL           =  26;
    public static final int MISSING_RESTRICTION_OR_EXTENSION  =  27;
    public static final int MISSING_BASE                      =  28;
    public static final int ELEMENT_EXTRA_REF                 =  30;
    public static final int EXPLICIT_GROUP_NEEDED             =  32;
    public static final int GROUP_MISSING_REF                 =  33;
    public static final int DUPLICATE_ATTRIBUTE_NAME          =  37;
    public static final int DUPLICATE_ANY_ATTRIBUTE           =  38;
    public static final int ATTRIBUTE_GROUP_MISSING_REF       =  39;
    public static final int CANNOT_EXTEND_ALL                 =  42;
    public static final int INVALID_SCHEMA                    =  46;
    public static final int DUPLICATE_GLOBAL_ELEMENT          =  47;
    public static final int DUPLICATE_GLOBAL_ATTRIBUTE        =  48;
    public static final int DUPLICATE_GLOBAL_TYPE             =  49;
    public static final int MALFORMED_SIMPLE_TYPE_DEFN        =  52;
    public static final int INVALID_NAME                      =  53;
    public static final int CANNOT_DERIVE_FINAL               =  54;
    public static final int IDC_NOT_FOUND                     =  55;
    public static final int NONDETERMINISTIC_MODEL            =  57;
    public static final int XPATH_COMPILATION_FAILURE         =  58;
    public static final int DUPLICATE_IDENTITY_CONSTRAINT     =  59;
    public static final int ILLEGAL_RESTRICTION               =  45;
    public static final int INCONSISTENT_TYPE                 =  50;

    /* general */

    public static final int UNSUPPORTED_FEATURE               =  51;
    public static final int GENERIC_ERROR                     =  60;


    //
    // xmlbeans errors
    //

    public static final String INVALID_DOCUMENT_TYPE = "invalid.document.type";
    public static final String CANNOT_LOAD_FILE = "cannot.load.file";
    public static final String EXCEPTION_EXCEEDED_ENTITY_BYTES = "exceeded-entity-bytes";
    public static final String EXCEPTION_LOADING_URL = "exception.loading.url";
    public static final String EXCEPTION_VALUE_NOT_SUPPORTED_J2S = "exception.value.not.supported.j2s";
    public static final String EXCEPTION_VALUE_NOT_SUPPORTED_S2J = "exception.value.not.supported.s2j";

    public static final String EXCEPTION_XQRL_XPATH_NOT_VALID = "exception.xqrl.xpath.not.valid";
    public static final String EXCEPTION_XQRL_EXCEPTION = "exception.xqrl.exception";

    //
    // xml errors
    //

    /**
     * uniqattspec: See
     * <a href="http://www.w3.org/TR/REC-xml/#uniqattspec">XML 1.0: Unique Att Spec</a>
     */
    public static final String XML_DUPLICATE_ATTRIBUTE = "uniqattspec";

    //
    // schema errors
    //

    /**
     * cvc-assess-attr: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-assess-attr">XMLSchema Structures 1.0: Schema-Validity Assessment (Attribute)</a>
     */
    public static final String ASSESS_ATTR_SCHEMA_VALID = "cvc-assess-attr";

    /**
     * cvc-assess-attr.1.2: See clause 1.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-assess-attr">XMLSchema Structures 1.0: Schema-Validity Assessment (Attribute)</a>
     */
    public static final String ASSESS_ATTR_SCHEMA_VALID$NOT_RESOLVED = "cvc-assess-attr.1.2";

    /**
     * cvc-assess-elt: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-assess-elt">XMLSchema Structures 1.0: Schema-Validity Assessment (Element)</a>
     */
    public static final String ASSESS_ELEM_SCHEMA_VALID = "cvc-assess-elt";

    /**
     * cvc-assess-elt.1.1.1.3.2: See clause 1.1.1.3.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-assess-elt">XMLSchema Structures 1.0: Schema-Validity Assessment (Element)</a>
     */
    public static final String ASSESS_ELEM_SCHEMA_VALID$NOT_RESOLVED = "cvc-assess-elt.1.1.1.3.2";

    /**
     * cvc-attribute: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-attribute">XMLSchema Structures 1.0: Attribute Locally Valid</a>
     */
    public static final String ATTR_LOCALLY_VALID = "cvc-attribute";

    /**
     * cvc-attribute.1: See clause 1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-attribute">XMLSchema Structures 1.0: Attribute Locally Valid</a>
     */
    public static final String ATTR_LOCALLY_VALID$NO_TYPE = "cvc-attribute.1";

    /**
     * cvc-attribute.4: See clause 4 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-attribute">XMLSchema Structures 1.0: Attribute Locally Valid</a>
     */
    public static final String ATTR_LOCALLY_VALID$FIXED = "cvc-attribute.4";

    /**
     * cvc-au: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-au">XMLSchema Structures 1.0: Attribute Locally Valid (Use)</a>
     */
    public static final String ATTR_USE_LOCALLY_VALID = "cvc-au";

    /**
     * cvc-complex-type: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-complex-type">XMLSchema Structures 1.0: Element Locally Valid (Complex Type)</a>
     */
    public static final String ELEM_COMPLEX_TYPE_LOCALLY_VALID = "cvc-complex-type";

    /**
     * cvc-complex-type.2.1: See clause 2.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-complex-type">XMLSchema Structures 1.0: Element Locally Valid (Complex Type)</a>
     */
    public static final String ELEM_COMPLEX_TYPE_LOCALLY_VALID$EMPTY_WITH_CONTENT = "cvc-complex-type.2.1";

    /**
     * cvc-complex-type.2.2: See clause 2.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-complex-type">XMLSchema Structures 1.0: Element Locally Valid (Complex Type)</a>
     */
    public static final String ELEM_COMPLEX_TYPE_LOCALLY_VALID$SIMPLE_TYPE_WITH_CONTENT = "cvc-complex-type.2.2";

    /**
     * cvc-complex-type.2.4a: See clause 2.4 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-complex-type">XMLSchema Structures 1.0: Element Locally Valid (Complex Type)</a>
     */
    public static final String ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT = "cvc-complex-type.2.4a";

    /**
     * cvc-complex-type.2.4b: See clause 2.4 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-complex-type">XMLSchema Structures 1.0: Element Locally Valid (Complex Type)</a>
     */
    public static final String ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED = "cvc-complex-type.2.4b";

    /**
     * cvc-complex-type.2.4c: See clause 2.4 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-complex-type">XMLSchema Structures 1.0: Element Locally Valid (Complex Type)</a>
     */
    public static final String ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_ELEMENT = "cvc-complex-type.2.4c";

    /**
     * cvc-complex-type.2.4d: See clause 2.4 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-complex-type">XMLSchema Structures 1.0: Element Locally Valid (Complex Type)</a>
     */
    public static final String ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_ELEMENT = "cvc-complex-type.2.4d";

    /**
     * cvc-complex-type.2.3: See clause 2.3 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-complex-type">XMLSchema Structures 1.0: Element Locally Valid (Complex Type)</a>
     */
    public static final String ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_ONLY_WITH_TEXT = "cvc-complex-type.2.3";

    /**
     * cvc-complex-type.3.1: See clause 3.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-complex-type">XMLSchema Structures 1.0: Element Locally Valid (Complex Type)</a>
     */
    public static final String ELEM_COMPLEX_TYPE_LOCALLY_VALID$ATTRIBUTE_VALID = "cvc-complex-type.3.1";

    /**
     * cvc-complex-type.3.2.1: See clause 3.2.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-complex-type">XMLSchema Structures 1.0: Element Locally Valid (Complex Type)</a>
     */
    public static final String ELEM_COMPLEX_TYPE_LOCALLY_VALID$NO_WILDCARD = "cvc-complex-type.3.2.1";

    /**
     * cvc-complex-type.3.2.2: See clause 3.2.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-complex-type">XMLSchema Structures 1.0: Element Locally Valid (Complex Type)</a>
     */
    public static final String ELEM_COMPLEX_TYPE_LOCALLY_VALID$NOT_WILDCARD_VALID = "cvc-complex-type.3.2.2";

    /**
     * cvc-complex-type.4: See clause 4 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-complex-type">XMLSchema Structures 1.0: Element Locally Valid (Complex Type)</a>
     */
    public static final String ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_REQUIRED_ATTRIBUTE = "cvc-complex-type.4";

    /**
     * cvc-datatype-valid: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-datatype-valid">XMLSchema Datatypes 1.0: Datatype Valid</a>
     */
    public static final String DATATYPE_VALID = "cvc-datatype-valid";

    /**
     * cvc-datatype-valid.1.1: See clause 1.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-datatype-valid">XMLSchema Datatypes 1.0: Datatype Valid</a>
     */
    public static final String DATATYPE_VALID$PATTERN_VALID = "cvc-datatype-valid.1.1";

    /**
     * cvc-datatype-valid.1.1b: See clause 1.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-datatype-valid">XMLSchema Datatypes 1.0: Datatype Valid</a>
     */
    public static final String DATATYPE_VALID$PATTERN_VALID$NO_VALUE = "cvc-datatype-valid.1.1b";

    /**
     * cvc-datatype-valid.1.2.3: See clause 1.2.3 of
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-datatype-valid">XMLSchema Datatypes 1.0: Datatype Valid</a>
     */
    public static final String DATATYPE_VALID$UNION = "cvc-datatype-valid.1.2.3";

    /**
     * cvc-elt: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-elt">XMLSchema Structures 1.0: Element Locally Valid (Element)</a>
     */
    public static final String ELEM_LOCALLY_VALID = "cvc-elt";

    /**
     * cvc-elt.1: See clause 1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-elt">XMLSchema Structures 1.0: Element Locally Valid (Element)</a>
     */
    public static final String ELEM_LOCALLY_VALID$NO_TYPE = "cvc-elt.1";

    /**
     * cvc-elt.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-elt">XMLSchema Structures 1.0: Element Locally Valid (Element)</a>
     */
    public static final String ELEM_LOCALLY_VALID$ABSTRACT = "cvc-elt.2";

    /**
     * cvc-elt.3.1: See clause 3.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-elt">XMLSchema Structures 1.0: Element Locally Valid (Element)</a>
     */
    public static final String ELEM_LOCALLY_VALID$NOT_NILLABLE = "cvc-elt.3.1";

    /**
     * cvc-elt.3.2.1: See clause 3.2.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-elt">XMLSchema Structures 1.0: Element Locally Valid (Element)</a>
     */
    public static final String ELEM_LOCALLY_VALID$NIL_WITH_CONTENT = "cvc-elt.3.2.1";

    /**
     * cvc-elt.3.2.2: See clause 3.2.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-elt">XMLSchema Structures 1.0: Element Locally Valid (Element)</a>
     */
    public static final String ELEM_LOCALLY_VALID$NIL_WITH_FIXED = "cvc-elt.3.2.2";

    /**
     * cvc-elt.4.1: See clause 4.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-elt">XMLSchema Structures 1.0: Element Locally Valid (Element)</a>
     */
    public static final String ELEM_LOCALLY_VALID$XSI_TYPE_INVALID_QNAME = "cvc-elt.4.1";

    /**
     * cvc-elt.4.2: See clause 4.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-elt">XMLSchema Structures 1.0: Element Locally Valid (Element)</a>
     */
    public static final String ELEM_LOCALLY_VALID$XSI_TYPE_NOT_FOUND = "cvc-elt.4.2";

    /**
     * cvc-elt.4.3a: See clause 4.3 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-elt">XMLSchema Structures 1.0: Element Locally Valid (Element)</a>
     */
    public static final String ELEM_LOCALLY_VALID$XSI_TYPE_NOT_DERIVED = "cvc-elt.4.3a";

    /**
     * cvc-elt.4.3b: See clause 4.3 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-elt">XMLSchema Structures 1.0: Element Locally Valid (Element)</a>
     */
    public static final String ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_EXTENSION = "cvc-elt.4.3b";

    /**
     * cvc-elt.4.3c: See clause 4.3 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-elt">XMLSchema Structures 1.0: Element Locally Valid (Element)</a>
     */
    public static final String ELEM_LOCALLY_VALID$XSI_TYPE_BLOCK_RESTRICTION = "cvc-elt.4.3c";

    /**
     * cvc-elt.5.2.2.1: See clause 5.2.2.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-elt">XMLSchema Structures 1.0: Element Locally Valid (Element)</a>
     */
    public static final String ELEM_LOCALLY_VALID$FIXED_WITH_CONTENT = "cvc-elt.5.2.2.1";

    /**
     * cvc-elt.5.2.2.2.1: See clause 5.2.2.2.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-elt">XMLSchema Structures 1.0: Element Locally Valid (Element)</a>
     */
    public static final String ELEM_LOCALLY_VALID$FIXED_VALID_MIXED_CONTENT = "cvc-elt.5.2.2.2.1";

    /**
     * cvc-elt.5.2.2.2.2: See clause 5.2.2.2.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-elt">XMLSchema Structures 1.0: Element Locally Valid (Element)</a>
     */
    public static final String ELEM_LOCALLY_VALID$FIXED_VALID_SIMPLE_TYPE = "cvc-elt.5.2.2.2.2";

    /**
     * cvc-elt.4.3d: See clause 4.3 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-elt">XMLSchema Structures 1.0: Element Locally Valid (Element)</a>
     */
    public static final String ELEM_LOCALLY_VALID$XSI_TYPE_PROHIBITED_SUBST = "cvc-elt.4.3d";

    /**
     * cvc-enumeration-valid: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-enumeration-valid">XMLSchema Datatypes 1.0: enumeration valid</a>
     */
    public static final String DATATYPE_ENUM_VALID = "cvc-enumeration-valid";

    /**
     * cvc-enumeration-valid.b: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-enumeration-valid">XMLSchema Datatypes 1.0: enumeration valid</a>
     */
    public static final String DATATYPE_ENUM_VALID$NO_VALUE = "cvc-enumeration-valid.b";

    /**
     * cvc-facet-valid: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-facet-valid">XMLSchema Datatypes 1.0: Facet Valid</a>
     */
    public static final String DATATYPE_FACET_VALID = "cvc-facet-valid";

    /**
     * cvc-fractionDigits-valid: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-fractionDigits-valid">XMLSchema Datatypes 1.0: fractionDigits Valid</a>
     */
    public static final String DATATYPE_FRACTION_DIGITS_VALID = "cvc-fractionDigits-valid";

    /**
     * cvc-id.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-id">XMLSchema Structures 1.0: Validation Root Valid (ID/IDREF)</a>
     */
    public static final String ID_VALID$DUPLICATE = "cvc-id.2";

    /**
     * cvc-identity-constraint: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-identity-constraint">XMLSchema Structures 1.0: Identity-constraint Satisfied</a>
     */
    public static final String IDENTITY_CONSTRAINT_VALID = "cvc-identity-constraint";

    /**
     * cvc-identity-constraint.4.1: See clause 4.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-identity-constraint">XMLSchema Structures 1.0: Identity-constraint Satisfied</a>
     */
    public static final String IDENTITY_CONSTRAINT_VALID$DUPLICATE_UNIQUE = "cvc-identity-constraint.4.1";

    /**
     * cvc-identity-constraint.4.2.2: See clause 4.2.2
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-identity-constraint">XMLSchema Structures 1.0: Identity-constraint Satisfied</a>
     */
    public static final String IDENTITY_CONSTRAINT_VALID$DUPLICATE_KEY = "cvc-identity-constraint.4.2.2";

    /**
     * cvc-identity-constraint.4.3: See clause 4.3
     * <a href="http://www.w3.org/TR/xmlschema-1/#cvc-identity-constraint">XMLSchema Structures 1.0: Identity-constraint Satisfied</a>
     */
    public static final String IDENTITY_CONSTRAINT_VALID$KEYREF_KEY_NOT_FOUND = "cvc-identity-constraint.4.3";

    /**
     * cvc-length-valid: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-length-valid">XMLSchema Datatypes 1.0: Length Valid</a>
     */
    public static final String DATATYPE_LENGTH_VALID = "cvc-length-valid";

    /**
     * cvc-length-valid.1.1: See clause 1.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-length-valid">XMLSchema Datatypes 1.0: Length Valid</a>
     */
    public static final String DATATYPE_LENGTH_VALID$STRING = "cvc-length-valid.1.1";

    /**
     * cvc-length-valid.1.2: See clause 1.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-length-valid">XMLSchema Datatypes 1.0: Length Valid</a>
     */
    public static final String DATATYPE_LENGTH_VALID$BINARY = "cvc-length-valid.1.2";

    /**
     * cvc-length-valid.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-length-valid">XMLSchema Datatypes 1.0: Length Valid</a>
     */
    public static final String DATATYPE_LENGTH_VALID$LIST_LENGTH = "cvc-length-valid.2";

    /**
     * cvc-maxExclusive-valid: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-maxExclusive-valid">XMLSchema Datatypes 1.0: maxExclusive Valid</a>
     */
    public static final String DATATYPE_MAX_EXCLUSIVE_VALID = "cvc-maxExclusive-valid";

    /**
     * cvc-maxInclusive-valid: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-maxInclusive-valid">XMLSchema Datatypes 1.0: maxInclusive Valid</a>
     */
    public static final String DATATYPE_MAX_INCLUSIVE_VALID = "cvc-maxInclusive-valid";

    /**
     * cvc-maxLength-valid: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-maxLength-valid">XMLSchema Datatypes 1.0: maxLength Valid</a>
     */
    public static final String DATATYPE_MAX_LENGTH_VALID = "cvc-maxLength-valid";

    /**
     * cvc-maxLength-valid.1.1: See clause 1.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-maxLength-valid">XMLSchema Datatypes 1.0: maxLength Valid</a>
     */
    public static final String DATATYPE_MAX_LENGTH_VALID$STRING = "cvc-maxLength-valid.1.1";

    /**
     * cvc-maxLength-valid.1.2: See clause 1.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-maxLength-valid">XMLSchema Datatypes 1.0: maxLength Valid</a>
     */
    public static final String DATATYPE_MAX_LENGTH_VALID$BINARY = "cvc-maxLength-valid.1.2";

    /**
     * cvc-maxLength-valid.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-maxLength-valid">XMLSchema Datatypes 1.0: maxLength Valid</a>
     */
    public static final String DATATYPE_MAX_LENGTH_VALID$LIST_LENGTH = "cvc-maxLength-valid.2";

    /**
     * cvc-minExclusive-valid: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-minExclusive-valid">XMLSchema Datatypes 1.0: minExclusive Valid</a>
     */
    public static final String DATATYPE_MIN_EXCLUSIVE_VALID = "cvc-minExclusive-valid";

    /**
     * cvc-minInclusive-valid: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-minInclusive-valid">XMLSchema Datatypes 1.0: minInclusive Valid</a>
     */
    public static final String DATATYPE_MIN_INCLUSIVE_VALID = "cvc-minInclusive-valid";

    /**
     * cvc-minLength-valid: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-minLength-valid">XMLSchema Datatypes 1.0: minLength Valid</a>
     */
    public static final String DATATYPE_MIN_LENGTH_VALID = "cvc-minLength-valid";

    /**
     * cvc-minLength-valid.1.1: See clause 1.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-minLength-valid">XMLSchema Datatypes 1.0: minLength Valid</a>
     */
    public static final String DATATYPE_MIN_LENGTH_VALID$STRING = "cvc-minLength-valid.1.1";

    /**
     * cvc-minLength-valid.1.2: See clause 1.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-minLength-valid">XMLSchema Datatypes 1.0: minLength Valid</a>
     */
    public static final String DATATYPE_MIN_LENGTH_VALID$BINARY = "cvc-minLength-valid.1.2";

    /**
     * cvc-minLength-valid.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-minLength-valid">XMLSchema Datatypes 1.0: minLength Valid</a>
     */
    public static final String DATATYPE_MIN_LENGTH_VALID$LIST_LENGTH = "cvc-minLength-valid.2";

    /**
     * cvc-model-group: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-model-group">XMLSchema Structures 1.0: Element Sequence Valid</a>
     */
    public static final String MODEL_GROUP_VALID = "cvc-model-group";

    /**
     * cvc-particle: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-particle">XMLSchema Structures 1.0: Element Sequence Locally Valid (Particle)</a>
     */
    public static final String PARTICLE_VALID = "cvc-particle";

    /**
     * cvc-particle.1.3: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-particle">XMLSchema Structures 1.0: Element Sequence Locally Valid (Particle)</a>
     */
    public static final String PARTICLE_VALID$NOT_WILDCARD_VALID = "cvc-particle.1.3";

    /**
     * cvc-particle.2.3.3a: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-particle">XMLSchema Structures 1.0: Element Sequence Locally Valid (Particle)</a>
     */
    public static final String PARTICLE_VALID$BLOCK_SUBSTITUTION = "cvc-particle.2.3.3a";

    /**
     * cvc-pattern-valid: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-pattern-valid">XMLSchema Datatypes 1.0: pattern valid</a>
     */
    public static final String DATATYPE_PATTERN_VALID = "cvc-pattern-valid";

    /**
     * cvc-resolve-instance: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-resolve-instance">XMLSchema Structures 1.0: QName resolution (Instance)</a>
     */
    public static final String RESOLVE_QNAME_INSTANCE_VALID = "cvc-resolve-instance"; // KHK: ? is the name good

    /**
     * cvc-simple-type: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-simple-type">XMLSchema Structures 1.0: String Valid</a>
     */
    public static final String SIMPLE_TYPE_STRING_VALID = "cvc-simple-type";

    /**
     * cvc-totalDigits-valid: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cvc-totalDigits-valid">XMLSchema Datatypes 1.0: totalDigits Valid</a>
     */
    public static final String DATATYPE_TOTAL_DIGITS_VALID = "cvc-totalDigits-valid";

    /**
     * cvc-type: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-type">XMLSchema Structures 1.0: Element Locally Valid (Type)</a>
     */
    public static final String ELEM_TYPE_LOCALLY_VALID = "cvc-type";

    /**
     * cvc-type.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-type">XMLSchema Structures 1.0: Element Locally Valid (Type)</a>
     */
    public static final String ELEM_TYPE_LOCALLY_VALID$ABSTRACT = "cvc-type.2";

    /**
     * cvc-wildcard: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-wildcard">XMLSchema Structures 1.0: Item Valid (Wildcard)</a>
     */
    public static final String WILDCARD_ITEM_VALID = "cvc-wildcard";

    /**
     * cvc-wildcard-namespace: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cvc-wildcard-namespace">XMLSchema Structures 1.0: Wildcard allows Namespace Name</a>
     */
    public static final String WILDCARD_NAMESPACE_NAME_VALID = "cvc-wildcard-namespace";

    /**
     * schema_reference: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#schema_reference">XMLSchema Structures 1.0: Schema Document Location Strategy</a>
     */
    public static final String SCHEMA_REFERENCE = "schema_reference";

    /**
     * src-annotation: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-annotation">XMLSchema Structures 1.0: Annotation Definition Representation OK</a>
     */
    public static final String SCHEMA_ANNOTATION = "src-annotation";

    /**
     * src-attribute: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-attribute">XMLSchema Structures 1.0: Attribute Declaration Representation OK</a>
     */
    public static final String SCHEMA_ATTR = "src-attribute";

    /**
     * src-attribute.1: See clause 1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-attribute">XMLSchema Structures 1.0: Attribute Declaration Representation OK</a>
     */
    public static final String SCHEMA_ATTR$DEFAULT_OR_FIXED = "src-attribute.1";

    /**
     * au-value_constraint: See description for
     * <a href="http://www.w3.org/TR/xmlschema-1/#au-value_constraint">XMLSchema Structures 1.0: The Attribute Use Schema Component</a>
     */
    public static final String SCHEMA_ATTR$FIXED_NOT_MATCH = "au-value_constraint";

    /**
     * src-attribute.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-attribute">XMLSchema Structures 1.0: Attribute Declaration Representation OK</a>
     */
    public static final String SCHEMA_ATTR$DEFAULT_AND_USE_OPTIONAL = "src-attribute.2";

    /**
     * src-attribute.3.1a: See clause 3.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-attribute">XMLSchema Structures 1.0: Attribute Declaration Representation OK</a>
     */
    public static final String SCHEMA_ATTR$REF_OR_NAME_HAS_BOTH = "src-attribute.3.1a";

    /**
     * src-attribute.3.1b: See clause 3.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-attribute">XMLSchema Structures 1.0: Attribute Declaration Representation OK</a>
     */
    public static final String SCHEMA_ATTR$REF_OR_NAME_HAS_NEITHER = "src-attribute.3.1b";

    /**
     * src-attribute.3.2: See clause 3.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-attribute">XMLSchema Structures 1.0: Attribute Declaration Representation OK</a>
     */
    public static final String SCHEMA_ATTR$REF_FEATURES = "src-attribute.3.2";

    /**
     * src-attribute.4: See clause 4 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-attribute">XMLSchema Structures 1.0: Attribute Declaration Representation OK</a>
     */
    public static final String SCHEMA_ATTR$TYPE_ATTR_OR_NESTED_TYPE = "src-attribute.4";

    /**
     * src-attribute_group: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-attribute_group">XMLSchema Structures 1.0: Attribute Group Definition Representation OK</a>
     */
    public static final String SCHEMA_ATTR_GROUP = "src-attribute_group";

    /**
     * src-attribute_group.3: See clause 3 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-attribute_group">XMLSchema Structures 1.0: Attribute Group Definition Representation OK</a>
     */
    public static final String SCHEMA_ATTR_GROUP$SELF_REF = "src-attribute_group.3";

    /**
     * src-ct: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-ct">XMLSchema Structures 1.0: Complex Type Definition Representation OK</a>
     */
    public static final String SCHEMA_COMPLEX_TYPE = "src-ct";

    /**
     * src-ct.1: See clause 1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-ct">XMLSchema Structures 1.0: Complex Type Definition Representation OK</a>
     */
    public static final String SCHEMA_COMPLEX_TYPE$COMPLEX_CONTENT = "src-ct.1";

    /**
     * src-ct.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-ct">XMLSchema Structures 1.0: Complex Type Definition Representation OK</a>
     */
    public static final String SCHEMA_COMPLEX_TYPE$SIMPLE_CONTENT = "src-ct.2";

    /**
     * src-element: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-element">XMLSchema Structures 1.0: Element Declaration Representation OK</a>
     */
    public static final String SCHEMA_ELEM = "src-element";

    /**
     * src-element.1: See clause 1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-element">XMLSchema Structures 1.0: Element Declaration Representation OK</a>
     */
    public static final String SCHEMA_ELEM$DEFAULT_OR_FIXED = "src-element.1";

    /**
     * src-element.2.1a: See clause 2.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-element">XMLSchema Structures 1.0: Element Declaration Representation OK</a>
     */
    public static final String SCHEMA_ELEM$REF_OR_NAME_HAS_BOTH = "src-element.2.1a";

    /**
     * src-element.2.1b: See clause 2.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-element">XMLSchema Structures 1.0: Element Declaration Representation OK</a>
     */
    public static final String SCHEMA_ELEM$REF_OR_NAME_HAS_NEITHER = "src-element.2.1b";

    /**
     * src-element.2.2: See clause 2.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-element">XMLSchema Structures 1.0: Element Declaration Representation OK</a>
     */
    public static final String SCHEMA_ELEM$REF_FEATURES = "src-element.2.2";

    /**
     * src-element.3: See clause 3 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-element">XMLSchema Structures 1.0: Element Declaration Representation OK</a>
     */
    public static final String SCHEMA_ELEM$TYPE_ATTR_OR_NESTED_TYPE = "src-element.3";

    /**
     * src-expredef: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-expredef">XMLSchema Structures 1.0: Individual Component Redefinition</a>
     */
    public static final String SCHEMA_REDEFINITION = "src-expredef";

    /**
     * src-identity-constraint: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-identity-constraint">XMLSchema Structures 1.0: Identity-constraint Definition Representation OK</a>
     */
    public static final String SCHEMA_IDENTITY_CONSTRAINT = "src-identity-constraint";

    /**
     * src-import: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-import">XMLSchema Structures 1.0: Import Constraints and Semantics</a>
     */
    public static final String SCHEMA_IMPORT = "src-import";

    /**
     * src-include: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-include">XMLSchema Structures 1.0: Inclusion Constraints and Semantics</a>
     */
    public static final String SCHEMA_INCLUDE = "src-include";

    /**
     * src-list-itemType-or-simpleType: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#src-list-itemType-or-simpleType">XMLSchema Datatypes 1.0: itemType attribute or simpleType child</a>
     */
    public static final String SCHEMA_SIMPLE_TYPE$LIST_ITEM_TYPE_OR_SIMPLE_TYPE = "src-list-itemType-or-simpleType";

    /**
     * src-model_group: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-model_group">XMLSchema Structures 1.0: Model Group Representation OK</a>
     */
    public static final String SCHEMA_MODEL_GROUP = "src-model_group";

    /**
     * src-model_group_defn: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-model_group_defn">XMLSchema Structures 1.0: Model Group Definition Representation OK</a>
     */
    public static final String SCHEMA_MODEL_GROUP_DEFN = "src-model_group_defn";

    /**
     * src-multiple-enumerations: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#src-multiple-enumerations">XMLSchema Datatypes 1.0: Multiple enumerations</a>
     */
    public static final String SCHEMA_MULTIPLE_ENUMS = "src-multiple-enumerations";

    /**
     * src-multiple-patterns: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#src-multiple-patterns">XMLSchema Datatypes 1.0: Multiple patterns</a>
     */
    public static final String SCHEMA_MULTIPLE_PATTERNS = "src-multiple-patterns";

    /**
     * src-notation: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-notation">XMLSchema Structures 1.0: Notation Definition Representation OK</a>
     */
    public static final String SCHEMA_NOTATION = "src-notation";

    /**
     * src-qname: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-qname">XMLSchema Structures 1.0: QName Interpretation</a>
     */
    public static final String SCHEMA_QNAME = "src-qname";

    /**
     * src-redefine: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-redefine">XMLSchema Structures 1.0: Redefinition Constraints and Semantics</a>
     */
    public static final String SCHEMA_REDEFINE = "src-redefine";

    /**
     * src-redefine.5a: See clause 5 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-redefine">XMLSchema Structures 1.0: Redefinition Constraints and Semantics</a>
     */
    public static final String SCHEMA_REDEFINE$EXTEND_OR_RESTRICT = "src-redefine.5a";

    /**
     * src-redefine.5b: See clause 5 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-redefine">XMLSchema Structures 1.0: Redefinition Constraints and Semantics</a>
     */
    public static final String SCHEMA_REDEFINE$SAME_TYPE = "src-redefine.5b";

    /**
     * src-redefine.6.1.1: See clause 6.1.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-redefine">XMLSchema Structures 1.0: Redefinition Constraints and Semantics</a>
     */
    public static final String SCHEMA_REDEFINE$GROUP_SELF_REF = "src-redefine.6.1.1";

    /**
     * src-redefine.6.1.2: See clause 6.1.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-redefine">XMLSchema Structures 1.0: Redefinition Constraints and Semantics</a>
     */
    public static final String SCHEMA_REDEFINE$GROUP_SELF_REF_MIN_MAX_1 = "src-redefine.6.1.2";

    /**
     * src-redefine.7.1: See clause 7.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-redefine">XMLSchema Structures 1.0: Redefinition Constraints and Semantics</a>
     */
    public static final String SCHEMA_REDEFINE$ATTR_GROUP_SELF_REF = "src-redefine.7.1";

    /**
     * src-resolve: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-resolve">XMLSchema Structures 1.0: QName resolution (Schema Document)</a>
     */
    public static final String SCHEMA_QNAME_RESOLVE = "src-resolve";

    /**
     * src-resolve: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-resolve">XMLSchema Structures 1.0: QName resolution (Schema Document)</a>
     */
    public static final String SCHEMA_QNAME_RESOLVE$HELP = "src-resolve.a";

    /**
     * src-restriction-base-or-simpleType: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#src-restriction-base-or-simpleType">XMLSchema Datatypes 1.0: base attribute or simpleType child</a>
     */
    public static final String SCHEMA_SIMPLE_TYPE$RESTICTION_HAS_BASE_OR_SIMPLE_TYPE = "src-restriction-base-or-simpleType";

    /**
     * src-simple-type: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-simple-type">XMLSchema Structures 1.0: Simple Type Definition Representation OK</a>
     */
    public static final String SCHEMA_SIMPLE_TYPE = "src-simple-type";

    /**
     * src-simple-type.2a: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-simple-type">XMLSchema Structures 1.0: Simple Type Definition Representation OK</a>
     */
    public static final String SCHEMA_SIMPLE_TYPE$RESTRICTION_HAS_BOTH_BASE_OR_SIMPLE_TYPE = "src-simple-type.2a";

    /**
     * src-simple-type.2b: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-simple-type">XMLSchema Structures 1.0: Simple Type Definition Representation OK</a>
     */
    public static final String SCHEMA_SIMPLE_TYPE$RESTRICTION_HAS_NEITHER_BASE_OR_SIMPLE_TYPE = "src-simple-type.2b";

    /**
     * src-simple-type.3a: See clause 3 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-simple-type">XMLSchema Structures 1.0: Simple Type Definition Representation OK</a>
     */
    public static final String SCHEMA_SIMPLE_TYPE$LIST_HAS_BOTH_ITEM_OR_SIMPLE_TYPE = "src-simple-type.3a";

    /**
     * src-simple-type.3b: See clause 3 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-simple-type">XMLSchema Structures 1.0: Simple Type Definition Representation OK</a>
     */
    public static final String SCHEMA_SIMPLE_TYPE$LIST_HAS_NEITHER_ITEM_OR_SIMPLE_TYPE = "src-simple-type.3b";

    /**
     * src-simple-type.4: See clause 4 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-simple-type">XMLSchema Structures 1.0: Simple Type Definition Representation OK</a>
     */
    public static final String SCHEMA_SIMPLE_TYPE$CYCLIC_UNION = "src-simple-type.4";

    /**
     * src-single-facet-value: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#src-single-facet-value">XMLSchema Datatypes 1.0: Single Facet Value</a>
     */
    public static final String DATATYPE_SINGLE_FACET_VALUE = "src-single-facet-value";

    /**
     * src-union-memberTypes-or-simpleTypes: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#src-union-memberTypes-or-simpleTypes">XMLSchema Datatypes 1.0: memberTypes attribute or simpleType children</a>
     */
    public static final String SCHEMA_SIMPLE_TYPE$UNION_HAS_MEMBER_TYPES_OR_SIMPLE_TYPES = "src-union-memberTypes-or-simpleTypes";

    /**
     * src-wildcard: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#src-wildcard">XMLSchema Structures 1.0: Wildcard Representation OK</a>
     */
    public static final String SCHEMA_WILDCARD = "src-wildcard";

    /**
     * a-props-correct: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#a-props-correct">XMLSchema Structures 1.0: Attribute Declaration Properties Correct</a>
     */
    public static final String ATTR_PROPERTIES = "a-props-correct";

    /**
     * a-props-correct.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#a-props-correct">XMLSchema Spec: Attribute Declaration Properties</a>
     */
    public static final String ATTR_PROPERTIES$CONSTRAINT_VALID = "a-props-correct.2";

    /**
     * a-props-correct.3: See clause 3 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#a-props-correct">XMLSchema Spec: Attribute Declaration Properties</a>
     */
    public static final String ATTR_PROPERTIES$ID_FIXED_OR_DEFAULT = "a-props-correct.3";

    /**
     * ag-props-correct: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#ag-props-correct">XMLSchema Structures 1.0: Attribute Group Definition Properties Correct</a>
     */
    public static final String ATTR_GROUP_PROPERTIES = "ag-props-correct";

    /**
     * ag-props-correct.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#ag-props-correct">XMLSchema Structures 1.0: Attribute Group Definition Properties Correct</a>
     */
    public static final String ATTR_GROUP_PROPERTIES$2 = "ag-props-correct.2";

    /**
     * ag-props-correct.3: See clause 3 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#ag-props-correct">XMLSchema Structures 1.0: Attribute Group Definition Properties Correct</a>
     */
    public static final String ATTR_GROUP_PROPERTIES$TWO_IDS = "ag-props-correct.3";

    /**
     * an-props-correct: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#an-props-correct">XMLSchema Structures 1.0: Annotation Correct</a>
     */
    public static final String ANNO_PROPERTIES = "an-props-correct";

    /**
     * au-props-correct: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#au-props-correct">XMLSchema Structures 1.0: Attribute Use Correct</a>
     */
    public static final String ATTR_USE_PROPERTIES = "au-props-correct";

    /**
     * c-fields-xpaths: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#c-fields-xpaths">XMLSchema Structures 1.0: Fields Value OK</a>
     */
    public static final String FIELDS_XPATH = "c-fields-xpaths";

    /**
     * c-props-correct: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#c-props-correct">XMLSchema Structures 1.0: Identity-constraint Definition Properties Correct</a>
     */
    public static final String IDENTITY_CONSTRAINT_PROPERTIES = "c-props-correct";

    /**
     * c-props-correct.1: See clause 1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#c-props-correct">XMLSchema Structures 1.0: Identity-constraint Definition Properties Correct</a>
     */
    public static final String IDENTITY_CONSTRAINT_PROPERTIES$KEYREF_REFERS_TO_KEYREF = "c-props-correct.1";

    /**
     * c-props-correct.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#c-props-correct">XMLSchema Structures 1.0: Identity-constraint Definition Properties Correct</a>
     */
    public static final String IDENTITY_CONSTRAINT_PROPERTIES$KEY_KEYREF_FIELD_COUNT_EQ = "c-props-correct.2";

    /**
     * c-selector-xpath: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#c-selector-xpath">XMLSchema Structures 1.0: Selector Value OK</a>
     */
    public static final String SELECTOR_XPATH = "c-selector-xpath";

    /**
     * cos-all-limited: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-all-limited">XMLSchema Structures 1.0: All Group Limited</a>
     */
    public static final String ALL_GROUP_LIMITED = "cos-all-limited";

    /**
     * cos-all-limited.1.2a: See clause 1.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-all-limited">XMLSchema Structures 1.0: All Group Limited</a>
     */
    public static final String ALL_GROUP_LIMITED$IN_MIN_MAX_1_PARTICLE = "cos-all-limited.1.2a";

    /**
     * cos-all-limited.1.2b: See clause 1.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-all-limited">XMLSchema Structures 1.0: All Group Limited</a>
     */
    public static final String ALL_GROUP_LIMITED$IN_COMPLEX_TYPE_DEF_PARTICLE = "cos-all-limited.1.2b";

    /**
     * cos-all-limited.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-all-limited">XMLSchema Structures 1.0: All Group Limited</a>
     */
    public static final String ALL_GROUP_LIMITED$CHILD_PARTICLES_MAX_LTE_1 = "cos-all-limited.2";

    /**
     * cos-applicable-facets: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cos-applicable-facets">XMLSchema Datatypes 1.0: applicable facets</a>
     */
    public static final String FACETS_APPLICABLE = "cos-applicable-facets"; // KHK: name ok?

    /**
     * notation-facets: See
     * http://www.w3.org/TR/xmlschema-2/#NOTATION-facets
     */
    public static final String FACETS_DEPRECATED_NOTATION = "notation-facets";

    /**
     * cos-aw-intersect: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-aw-intersect">XMLSchema Structures 1.0: Attribute Wildcard Intersection</a>
     */
    public static final String ATTR_WILDCARD_INTERSECT = "cos-aw-intersect";

    /**
     * cos-aw-union: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-aw-union">XMLSchema Structures 1.0: Attribute Wildcard Union</a>
     */
    public static final String ATTR_WILDCARD_UNION = "cos-aw-union";

    /**
     * enumeration-required-notation: See
     * <a href="http://www.w3.org/TR/xmlschema-2/#enumeration-required-notation">XMLSchema Datatypes 1.0: Schema Component Constraint: enumeration facet value required for NOTATION</a>
     */
    public static final String ATTR_NOTATION_TYPE_FORBIDDEN = "enumeration-required-notation-attr";

    /**
     * <a href="http://www.w3c.org/TR/xmlschema-2/#NOTATION">XMLSchema Datatypes 1.0: Definition of type NOTATION</>
     */
    public static final String ATTR_COMPATIBILITY_TARGETNS = "notation-targetns-attr";

    /**
     * cos-choice-range: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-choice-range">XMLSchema Structures 1.0: Effective Total Range (choice)</a>
     */
    public static final String CHOICE_RANGE = "cos-choice-range";

    /**
     * cos-ct-derived-ok: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-ct-derived-ok">XMLSchema Structures 1.0: Type Derivation OK (Complex)</a>
     */
    public static final String COMPLEX_TYPE_DERIVATION = "cos-ct-derived-ok";

    /**
     * cos-ct-extends: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-ct-extends">XMLSchema Structures 1.0: Derivation Valid (Extension)</a>
     */
    public static final String COMPLEX_TYPE_EXTENSION = "cos-ct-extends";

    /**
     * cos-ct-extends.1.4.1: See clause 1.4.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-ct-extends">XMLSchema Structures 1.0: Derivation Valid (Extension)</a>
     */
    public static final String COMPLEX_TYPE_EXTENSION$EXTENDING_SIMPLE_CONTENT = "cos-ct-extends.1.4.1";

    /**
     * cos-ct-extends.1.4.2.2: See clause 1.4.2.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-ct-extends">XMLSchema Structures 1.0: Derivation Valid (Extension)</a>
     */
    public static final String COMPLEX_TYPE_EXTENSION$BOTH_ELEMEMENT_OR_MIXED = "cos-ct-extends.1.4.2.2";

    /**
     * derivation-ok-restriction.1.1: See clause 1.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-ct-extends">XMLSchema Structures 1.0: Derivation Valid (Extension)</a>
     */
    public static final String COMPLEX_TYPE_EXTENSION$FINAL = "cos-ct-extends.1.1";

    /**
     * cos-element-consistant: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-element-consistent">XMLSchema Structures 1.0: Element Declarations Consistent</a>
     */
    public static final String ELEM_CONSISTANT = "cos-element-consistent";

    /**
     * cos-equiv-class: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-equiv-class">XMLSchema Structures 1.0: Substitution Group</a>
     */
    public static final String SUBST_GROUP = "cos-equiv-class";

    /**
     * cos-equiv-derived-ok-rec: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-equiv-derived-ok-rec">XMLSchema Structures 1.0: Substitution Group OK (Transitive)</a>
     */
    public static final String SUBST_GROUP_DERIVED = "cos-equiv-derived-ok-rec";

    /**
     * cos-group-emptiable: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-group-emptiable">XMLSchema Structures 1.0: Particle Emptiable</a>
     */
    public static final String PARTICLE_EMPTIABLE = "cos-group-emptiable";

    /**
     * cos-list-of-atomic: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cos-list-of-atomic">XMLSchema Datatypes 1.0: list of atomic</a>
     */
    public static final String DATATYPE_LIST_OF_ATOMIC = "cos-list-of-atomic";

    /**
     * cos-no-circular-unions: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#cos-no-circular-unions">XMLSchema Datatypes 1.0: no circular unions</a>
     */
    public static final String DATATYPE_CIRCULAR_UNION = "cos-no-circular-unions";

    /**
     * cos-nonambig: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-nonambig">XMLSchema Structures 1.0: Unique Particle Attribution</a>
     */
    public static final String UNIQUE_PARTICLE_ATTRIBUTION = "cos-nonambig";

    /**
     * cos-ns-subset: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-ns-subset">XMLSchema Structures 1.0: Wildcard Subset</a>
     */
    public static final String WILDCARD_SUBSET = "cos-ns-subset";

    /**
     * cos-particle-extend: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-particle-extend">XMLSchema Structures 1.0: Particle Valid (Extension)</a>
     */
    public static final String PARTICLE_EXTENSION = "cos-particle-extend";

    /**
     * cos-particle-restrict: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-particle-restrict">XMLSchema Structures 1.0: Particle Valid (Restriction)</a>
     */
    public static final String PARTICLE_RESTRICTION = "cos-particle-restrict";

    /**
     * cos-particle-restrict.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-particle-restrict">XMLSchema Structures 1.0: Particle Valid (Restriction)</a>
     */
    public static final String PARTICLE_RESTRICTION$INVALID_RESTRICTION = "cos-particle-restrict.2";

    /**
     * cos-seq-range: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-seq-range">XMLSchema Structures 1.0: Effective Total Range (all and sequence)</a>
     */
    public static final String SEQUENCE_RANGE = "cos-seq-range";

    /**
     * cos-st-derived-ok: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-st-derived-ok">XMLSchema Structures 1.0: Type Derivation OK (Simple)</a>
     */
    public static final String SIMPLE_TYPE_DERIVATION = "cos-st-derived-ok";

    /**
     * cos-st-restricts: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-st-restricts">XMLSchema Structures 1.0: Derivation Valid (Restriction, Simple)</a>
     */
    public static final String SIMPLE_TYPE_RESTRICTION = "cos-st-restricts";

    /**
     * cos-st-restricts.1.1: See clause 1.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-st-restricts">XMLSchema Structures 1.0: Derivation Valid (Restriction, Simple)</a>
     */
    public static final String SIMPLE_TYPE_RESTRICTION$ATOMIC_NOT_SIMPLE = "cos-st-restricts.1.1";

    /**
     * cos-st-restricts.2.1a: See clause 2.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-st-restricts">XMLSchema Structures 1.0: Derivation Valid (Restriction, Simple)</a>
     */
    public static final String SIMPLE_TYPE_RESTRICTION$LIST_ITEM_NOT_SIMPLE = "cos-st-restricts.2.1a";

    /**
     * cos-st-restricts.2.1a: See clause 2.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-st-restricts">XMLSchema Structures 1.0: Derivation Valid (Restriction, Simple)</a>
     */
    public static final String SIMPLE_TYPE_RESTRICTION$LIST_ITEM_IS_LIST = "cos-st-restricts.2.1b";

    /**
     * cos-st-restricts.2.1a: See clause 2.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-st-restricts">XMLSchema Structures 1.0: Derivation Valid (Restriction, Simple)</a>
     */
    public static final String SIMPLE_TYPE_RESTRICTION$LIST_ITEM_IS_UNION_OF_LIST = "cos-st-restricts.2.1c";

    /**
     * cos-st-restricts.3.1: See clause 3.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-st-restricts">XMLSchema Structures 1.0: Derivation Valid (Restriction, Simple)</a>
     */
    public static final String SIMPLE_TYPE_RESTRICTION$UNION_MEMBER_NOT_SIMPLE = "cos-st-restricts.3.1";

    /**
     * cos-valid-default: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-valid-default">XMLSchema Structures 1.0: Element Default Valid (Immediate)</a>
     */
    public static final String ELEM_DEFAULT_VALID = "cos-valid-default";

    /**
     * cos-valid-default.2.1: See clause 2.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-valid-default">XMLSchema Structures 1.0: Element Default Valid (Immediate)</a>
     */
    public static final String ELEM_DEFAULT_VALID$SIMPLE_TYPE_OR_MIXED = "cos-valid-default.2.1";

    /**
     * cos-valid-default.2.2.2: See clause 2.2.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#cos-valid-default">XMLSchema Structures 1.0: Element Default Valid (Immediate)</a>
     */
    public static final String ELEM_DEFAULT_VALID$MIXED_AND_EMPTIABLE = "cos-valid-default.2.2.2";

    /**
     * <a href="http://www.w3c.org/TR/xmlschema-2/#ID">XMLSchema Datatypes 1.0: Definitions of types ID, IDREF, IDREFS, ENTITY, ENTITIES, NOTATION</>
     */
    public static final String ELEM_COMPATIBILITY_TYPE = "id-idref-idrefs-entity-entities-notation";

    /**
     * <a href="http://www.w3c.org/TR/xmlschema-2/#NOTATION">XMLSchema Datatypes 1.0: Definition of type NOTATION</>
     */
    public static final String ELEM_COMPATIBILITY_TARGETNS = "notation-targetns-elem";

    /**
     * ct-props-correct: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#ct-props-correct">XMLSchema Structures 1.0: Complex Type Definition Properties Correct</a>
     */
    public static final String COMPLEX_TYPE_PROPERTIES = "ct-props-correct";

    /**
     * ct-props-correct.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#ct-props-correct">XMLSchema Structures 1.0: Complex Type Definition Properties Correct</a>
     */
    public static final String COMPLEX_TYPE_PROPERTIES$SIMPLE_TYPE_EXTENSION = "ct-props-correct.2";

    /**
     * ct-props-correct.4: See clause 4 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#ct-props-correct">XMLSchema Structures 1.0: Complex Type Definition Properties Correct</a>
     */
    public static final String COMPLEX_TYPE_PROPERTIES$DUPLICATE_ATTRIBUTE = "ct-props-correct.4";

    /**
     * derivation-ok-restriction: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#derivation-ok-restriction">XMLSchema Structures 1.0: Derivation Valid (Restriction, Complex)</a>
     */
    public static final String COMPLEX_TYPE_RESTRICTION = "derivation-ok-restriction";

    /**
     * derivation-ok-restriction.1: See clause 1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#derivation-ok-restriction">XMLSchema Structures 1.0: Derivation Valid (Restriction, Complex)</a>
     */
    public static final String COMPLEX_TYPE_RESTRICTION$FINAL = "derivation-ok-restriction.1";

    /**
     * derivation-ok-restriction.2.1.1: See clause 2.1.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#derivation-ok-restriction">XMLSchema Structures 1.0: Derivation Valid (Restriction, Complex)</a>
     */
    public static final String COMPLEX_TYPE_RESTRICTION$ATTR_REQUIRED = "derivation-ok-restriction.2.1.1";

    /**
     * derivation-ok-restriction.2.2: See clause 2.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#derivation-ok-restriction">XMLSchema Structures 1.0: Derivation Valid (Restriction, Complex)</a>
     */
    public static final String COMPLEX_TYPE_RESTRICTION$ATTR_IN_BASE_WILDCARD_SET = "derivation-ok-restriction.2.2";

    /**
     * derivation-ok-restriction.4.1: See clause 4.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#derivation-ok-restriction">XMLSchema Structures 1.0: Derivation Valid (Restriction, Complex)</a>
     */
    public static final String COMPLEX_TYPE_RESTRICTION$BASE_HAS_ATTR_WILDCARD = "derivation-ok-restriction.4.1";

    /**
     * derivation-ok-restriction.4.2: See clause 4.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#derivation-ok-restriction">XMLSchema Structures 1.0: Derivation Valid (Restriction, Complex)</a>
     */
    public static final String COMPLEX_TYPE_RESTRICTION$ATTR_WILDCARD_SUBSET = "derivation-ok-restriction.4.2";

    /**
     * derivation-ok-restriction.5.1: See clause 5.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#derivation-ok-restriction">XMLSchema Structures 1.0: Derivation Valid (Restriction, Complex)</a>
     */
    public static final String COMPLEX_TYPE_RESTRICTION$SC_AND_SIMPLE_TYPE_OR_MIXED = "derivation-ok-restriction.5.1";

    /**
     * derivation-ok-restriction.5.1.2: See clause 5.1.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#derivation-ok-restriction">XMLSchema Structures 1.0: Derivation Valid (Restriction, Complex)</a>
     */
    public static final String COMPLEX_TYPE_RESTRICTION$SC_AND_MIXED_EMPTIABLE = "derivation-ok-restriction.5.1.2";

    /**
     * derivation-ok-restriction.5.2.2.1: See clause 5.2.2.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#derivation-ok-restriction">XMLSchema Structures 1.0: Derivation Valid (Restriction, Complex)</a>
     */
    public static final String COMPLEX_TYPE_RESTRICTION$SC_NOT_DERIVED = "derivation-ok-restriction.5.2.2.1";

    /**
     * derivation-ok-restriction.5.2: See clause 5.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#derivation-ok-restriction">XMLSchema Structures 1.0: Derivation Valid (Restriction, Complex)</a>
     */
    public static final String COMPLEX_TYPE_RESTRICTION$EMPTY_AND_NOT_SIMPLE = "derivation-ok-restriction.5.2";

    /**
     * derivation-ok-restriction.5.2.2: See clause 5.2.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#derivation-ok-restriction">XMLSchema Structures 1.0: Derivation Valid (Restriction, Complex)</a>
     */
    public static final String COMPLEX_TYPE_RESTRICTION$EMPTY_AND_ELEMENT_OR_MIXED_EMPTIABLE = "derivation-ok-restriction.5.2.2";

    /**
     * derivation-ok-restriction.5.3: See clause 5.3 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#derivation-ok-restriction">XMLSchema Structures 1.0: Derivation Valid (Restriction, Complex)</a>
     */
    public static final String COMPLEX_TYPE_RESTRICTION$ELEMENT_OR_MIXED_AND_VALID = "derivation-ok-restriction.5.3";

    /**
     * derivation-ok-restriction.5.3a: See clause 5.3 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#derivation-ok-restriction">XMLSchema Structures 1.0: Derivation Valid (Restriction, Complex)</a>
     */
    public static final String COMPLEX_TYPE_RESTRICTION$ELEMENT_OR_MIXED_AND_MIXED = "derivation-ok-restriction.5.3a";

    /**
     * derivation-ok-restriction.5.3b: See clause 5.3 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#derivation-ok-restriction">XMLSchema Structures 1.0: Derivation Valid (Restriction, Complex)</a>
     */
    public static final String COMPLEX_TYPE_RESTRICTION$ELEMENT_OR_MIXED_AND_EMPTY = "derivation-ok-restriction.5.3b";

    /**
     * derivation-ok-restriction.5.3c: See clause 5.3 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#derivation-ok-restriction">XMLSchema Structures 1.0: Derivation Valid (Restriction, Complex)</a>
     */
    public static final String COMPLEX_TYPE_RESTRICTION$ELEMENT_OR_MIXED_AND_SIMPLE  = "derivation-ok-restriction.5.3c";

    /**
     * e-props-correct: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#e-props-correct">XMLSchema Structures 1.0: Element Declaration Properties Correct</a>
     */
    public static final String ELEM_PROPERTIES = "e-props-correct";

    /**
     * e-props-correct.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#e-props-correct">XMLSchema Structures 1.0: Element Declaration Properties Correct</a>
     */
    public static final String ELEM_PROPERTIES$CONSTRAINT_VALID = "e-props-correct.2";

    /**
     * e-props-correct.4: See clause 4 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#e-props-correct">XMLSchema Structures 1.0: Element Declaration Properties Correct</a>
     */
    public static final String ELEM_PROPERTIES$SUBSTITUTION_VALID = "e-props-correct.4";

    /**
     * e-props-correct.4a: See clause 4 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#e-props-correct">XMLSchema Structures 1.0: Element Declaration Properties Correct</a>
     */
    public static final String ELEM_PROPERTIES$SUBSTITUTION_FINAL = "e-props-correct.4a";

    /**
     * enumeration-required-notation: See
     * <a href="http://www.w3.org/TR/xmlschema-2/#enumeration-required-notation">XMLSchema Datatypes 1.0: Schema Component Constraint: enumeration facet value required for NOTATION</a>
     */
    public static final String ELEM_NOTATION_TYPE_FORBIDDEN = "enumeration-required-notation-elem";

    /**
     * enumeration-required-notation: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#enumeration-required-notation">XMLSchema Datatypes 1.0: enumeration facet value required for NOTATION</a>
     */
    public static final String DATATYPE_ENUM_NOTATION = "enumeration-required-notation";

    /**
     * enumeration-valid-restriction: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#enumeration-valid-restriction">XMLSchema Datatypes 1.0: enumeration valid restriction</a>
     */
    public static final String DATATYPE_ENUM_RESTRICTION = "enumeration-valid-restriction";

    /**
     * fractionDigits-totalDigits: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#fractionDigits-totalDigits">XMLSchema Datatypes 1.0: fractionDigits less than or equal to totalDigits</a>
     */
    public static final String DATATYPE_FRACTION_DIGITS_LE_TOTAL_DIGITS = "fractionDigits-totalDigits";

    /**
     * fractionDigits-valid-restriction: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#fractionDigits-valid-restriction">XMLSchema Datatypes 1.0: fractionDigits valid restriction</a>
     */
    public static final String DATATYPE_FRACTION_DIGITS_RESTRICTION = "fractionDigits-valid-restriction";

    /**
     * length-minLength-maxLength: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#length-minLength-maxLength">XMLSchema Datatypes 1.0: length and minLength or maxLength</a>
     */
    public static final String DATATYPE_LENGTH = "length-minLength-maxLength";

    /**
     * length-valid-restriction: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#length-valid-restriction">XMLSchema Datatypes 1.0: length valid restriction</a>
     */
    public static final String DATATYPE_LENGTH_RESTRICTION = "length-valid-restriction";

    /**
     * maxExclusive-valid-restriction: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#maxExclusive-valid-restriction">XMLSchema Datatypes 1.0: maxExclusive valid restriction</a>
     */
    public static final String DATATYPE_MAX_EXCLUSIVE_RESTRICTION = "maxExclusive-valid-restriction";

    /**
     * maxInclusive-maxExclusive: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#maxInclusive-maxExclusive">XMLSchema Datatypes 1.0: maxInclusive and maxExclusive</a>
     */
    public static final String DATATYPE_MIN_INCLUSIVE_MAX_EXCLUSIVE = "maxInclusive-maxExclusive";

    /**
     * maxInclusive-valid-restriction: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#maxInclusive-valid-restriction">XMLSchema Datatypes 1.0: maxInclusive valid restriction</a>
     */
    public static final String DATATYPE_MAX_INCLUSIVE_RESTRICTION = "maxInclusive-valid-restriction";

    /**
     * maxLength-valid-restriction: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#maxLength-valid-restriction">XMLSchema Datatypes 1.0: maxLength valid restriction</a>
     */
    public static final String DATATYPE_MAX_LENGTH_RESTRICTION = "maxLength-valid-restriction";

    /**
     * mg-props-correct: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#mg-props-correct">XMLSchema Structures 1.0: Model Group Correct</a>
     */
    public static final String MODEL_GROUP_PROPERTIES = "mg-props-correct";

    /**
     * mg-props-correct.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#mg-props-correct">XMLSchema Structures 1.0: Model Group Correct</a>
     */
    public static final String MODEL_GROUP_PROPERTIES$CIRCULAR = "mg-props-correct.2";

    /**
     * mgd-props-correct: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#mgd-props-correct">XMLSchema Structures 1.0: Model Group Definition Properties Correct</a>
     */
    public static final String MODEL_GROUP_DEFN_PROPERTIES = "mgd-props-correct";

    /**
     * minExclusive-less-than-equal-to-maxExclusive: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#minExclusive-less-than-equal-to-maxExclusive">XMLSchema Datatypes 1.0: minExclusive &lt;= maxExclusive</a>
     */
    public static final String DATATYPE_MIN_EXCLUSIVE_LE_MAX_EXCLUSIVE = "minExclusive-less-than-equal-to-maxExclusive";

    /**
     * minExclusive-less-than-maxInclusive: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#minExclusive-less-than-maxInclusive">XMLSchema Datatypes 1.0: minExclusive &lt; maxInclusive</a>
     */
    public static final String DATATYPE_MIN_EXCLUSIVE_LE_MAX_INCLUSIVE = "minExclusive-less-than-maxInclusive";

    /**
     * minExclusive-valid-restriction: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#minExclusive-valid-restriction">XMLSchema Datatypes 1.0: minExclusive valid restriction</a>
     */
    public static final String DATATYPE_MIN_EXCLUSIVE_RESTRICTION = "minExclusive-valid-restriction";

    /**
     * minInclusive-less-than-equal-to-maxInclusive: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#minInclusive-less-than-equal-to-maxInclusive">XMLSchema Datatypes 1.0: minInclusive &lt;= maxInclusive</a>
     */
    public static final String DATATYPE_MIN_INCLUSIVE_LTE_MAX_INCLUSIVE = "minInclusive-less-than-equal-to-maxInclusive";

    /**
     * minInclusive-less-than-maxExclusive: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#minInclusive-less-than-maxExclusive">XMLSchema Datatypes 1.0: minInclusive &lt; maxExclusive</a>
     */
    public static final String DATATYPE_MIN_INCLUSIVE_LE_MAX_EXCLUSIVE = "minInclusive-less-than-maxExclusive";

    /**
     * minInclusive-minExclusive: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#minInclusive-minExclusive">XMLSchema Datatypes 1.0: minInclusive and minExclusive</a>
     */
    public static final String DATATYPE_MIN_INCLUSIVE_MIN_EXCLUSIVE = "minInclusive-minExclusive";

    /**
     * minInclusive-valid-restriction: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#minInclusive-valid-restriction">XMLSchema Datatypes 1.0: minInclusive valid restriction</a>
     */
    public static final String DATATYPE_MIN_INCLUSIVE_RESTRICTION = "minInclusive-valid-restriction";

    /**
     * minLength-less-than-equal-to-maxLength: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#minLength-less-than-equal-to-maxLength">XMLSchema Datatypes 1.0: minLength &lt;= maxLength</a>
     */
    public static final String DATATYPE_MIN_LENGTH_LE_MAX_LENGTH = "minLength-less-than-equal-to-maxLength";

    /**
     * minLength-valid-restriction: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#minLength-valid-restriction">XMLSchema Datatypes 1.0: minLength valid restriction</a>
     */
    public static final String DATATYPE_MIN_LENGTH_RESTRICTION = "minLength-valid-restriction";

    /**
     * n-props-correct: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#n-props-correct">XMLSchema Structures 1.0: Notation Declaration Correct</a>
     */
    public static final String NOTATION_PROPERTIES = "n-props-correct";

    /**
     * no-xmlns: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#no-xmlns">XMLSchema Structures 1.0: xmlns Not Allowed</a>
     */
    public static final String NO_XMLNS = "no-xmlns";

    /**
     * no-xsi: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#no-xsi">XMLSchema Structures 1.0: xsi: Not Allowed</a>
     */
    public static final String NO_XSI = "no-xsi";

    /**
     * p-props-correct: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#p-props-correct">XMLSchema Structures 1.0: Particle Correct</a>
     */
    public static final String PARTICLE_PROPERTIES = "p-props-correct";

    /**
     * p-props-correct.2.1: See clause 2.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#p-props-correct">XMLSchema Structures 1.0: Particle Correct</a>
     */
    public static final String PARTICLE_PROPERTIES$MIN_LTE_MAX = "p-props-correct.2.1";

    /**
     * p-props-correct.2.2: See clause 2.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#p-props-correct">XMLSchema Structures 1.0: Particle Correct</a>
     */
    public static final String PARTICLE_PROPERTIES$MAX_GTE_1 = "p-props-correct.2.2";

    /**
     * range-ok: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#range-ok">XMLSchema Structures 1.0: Occurrence Range OK</a>
     */
    public static final String OCCURRENCE_RANGE = "range-ok";

    /**
     * range-ok.1: See clause 1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#range-ok">XMLSchema Structures 1.0: Occurrence Range OK</a>
     */
    public static final String OCCURRENCE_RANGE$MIN_GTE_MIN = "range-ok.1";

    /**
     * range-ok.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#range-ok">XMLSchema Structures 1.0: Occurrence Range OK</a>
     */
    public static final String OCCURRENCE_RANGE$MAX_LTE_MAX = "range-ok.2";

    /**
     * rcase-MapAndSum: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-MapAndSum">XMLSchema Structures 1.0: Particle Derivation OK (Sequence:Choice -- MapAndSum)</a>
     */
    public static final String PARTICLE_DERIVATION_MAP_AND_SUM = "rcase-MapAndSum";

    /**
     * rcase-MapAndSum.1: See clause 1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-MapAndSum">XMLSchema Structures 1.0: Particle Derivation OK (Sequence:Choice -- MapAndSum)</a>
     */
    public static final String PARTICLE_DERIVATION_MAP_AND_SUM$MAP = "rcase-MapAndSum.1";

    /**
     * rcase-MapAndSum.2a: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-MapAndSum">XMLSchema Structures 1.0: Particle Derivation OK (Sequence:Choice -- MapAndSum)</a>
     */
    public static final String PARTICLE_DERIVATION_MAP_AND_SUM$SUM_MIN_OCCURS_GTE_MIN_OCCURS = "rcase-MapAndSum.2a";

    /**
     * rcase-MapAndSum.2b: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-MapAndSum">XMLSchema Structures 1.0: Particle Derivation OK (Sequence:Choice -- MapAndSum)</a>
     */
    public static final String PARTICLE_DERIVATION_MAP_AND_SUM$SUM_MAX_OCCURS_LTE_MAX_OCCURS = "rcase-MapAndSum.2b";

    /**
     * rcase-NameAndTypeOK: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-NameAndTypeOK">XMLSchema Structures 1.0: Particle Restriction OK (Elt:Elt -- NameAndTypeOK)</a>
     */
    public static final String PARTICLE_RESTRICTION_NAME_AND_TYPE = "rcase-NameAndTypeOK";

    /**
     * rcase-NameAndTypeOK.1: See clause 1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-NameAndTypeOK">XMLSchema Structures 1.0: Particle Restriction OK (Elt:Elt -- NameAndTypeOK)</a>
     */
    public static final String PARTICLE_RESTRICTION_NAME_AND_TYPE$NAME = "rcase-NameAndTypeOK.1";

    /**
     * rcase-NameAndTypeOK.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-NameAndTypeOK">XMLSchema Structures 1.0: Particle Restriction OK (Elt:Elt -- NameAndTypeOK)</a>
     */
    public static final String PARTICLE_RESTRICTION_NAME_AND_TYPE$NILLABLE = "rcase-NameAndTypeOK.2";

    /**
     * rcase-NameAndTypeOK.3: See clause 3 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-NameAndTypeOK">XMLSchema Structures 1.0: Particle Restriction OK (Elt:Elt -- NameAndTypeOK)</a>
     */
    //public static final String PARTICLE_RESTRICTION_NAME_AND_TYPE$NAME = "rcase-NameAndTypeOK.3";

    /**
     * rcase-NameAndTypeOK.4: See clause 4 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-NameAndTypeOK">XMLSchema Structures 1.0: Particle Restriction OK (Elt:Elt -- NameAndTypeOK)</a>
     */
    public static final String PARTICLE_RESTRICTION_NAME_AND_TYPE$FIXED = "rcase-NameAndTypeOK.4";

    /**
     * rcase-NameAndTypeOK.5: See clause 5 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-NameAndTypeOK">XMLSchema Structures 1.0: Particle Restriction OK (Elt:Elt -- NameAndTypeOK)</a>
     */
    public static final String PARTICLE_RESTRICTION_NAME_AND_TYPE$IDENTITY_CONSTRAINTS = "rcase-NameAndTypeOK.5";

    /**
     * rcase-NameAndTypeOK.6: See clause 6 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-NameAndTypeOK">XMLSchema Structures 1.0: Particle Restriction OK (Elt:Elt -- NameAndTypeOK)</a>
     */
    public static final String PARTICLE_RESTRICTION_NAME_AND_TYPE$DISALLOWED_SUBSTITUTIONS = "rcase-NameAndTypeOK.6";

    /**
     * rcase-NameAndTypeOK.7a: See clause 7 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-NameAndTypeOK">XMLSchema Structures 1.0: Particle Restriction OK (Elt:Elt -- NameAndTypeOK)</a>
     */
    public static final String PARTICLE_RESTRICTION_NAME_AND_TYPE$TYPE_VALID = "rcase-NameAndTypeOK.7a";

    /**
     * rcase-NameAndTypeOK.7b: See clause 7 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-NameAndTypeOK">XMLSchema Structures 1.0: Particle Restriction OK (Elt:Elt -- NameAndTypeOK)</a>
     */
    public static final String PARTICLE_RESTRICTION_NAME_AND_TYPE$TYPE_RESTRICTED = "rcase-NameAndTypeOK.7b";

    /**
     * rcase-NSCompat: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-NSCompat">XMLSchema Structures 1.0: Particle Derivation OK (Elt:Any -- NSCompat)</a>
     */
    public static final String PARTICLE_DERIVATION_NS_COMPAT = "rcase-NSCompat";

    /**
     * rcase-NSCompat.1: See clause 1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-NSCompat">XMLSchema Structures 1.0: Particle Derivation OK (Elt:Any -- NSCompat)</a>
     */
    public static final String PARTICLE_DERIVATION_NS_COMPAT$WILDCARD_VALID = "rcase-NSCompat.1";

    /**
     * rcase-NSRecurseCheckCardinality: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-NSRecurseCheckCardinality">XMLSchema Structures 1.0: Particle Derivation OK (All/Choice/Sequence:Any -- NSRecurseCheckCardinality)</a>
     */
    public static final String PARTICLE_DERIVATION_NS_RECURSE = "rcase-NSRecurseCheckCardinality";

    /**
     * rcase-NSSubset: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-NSSubset">XMLSchema Structures 1.0: Particle Derivation OK (Any:Any -- NSSubset)</a>
     */
    public static final String PARTICLE_DERIVATION_NS_SUBST = "rcase-NSSubset";

    /**
     * rcase-NSSubset.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-NSSubset">XMLSchema Structures 1.0: Particle Derivation OK (Any:Any -- NSSubset)</a>
     */
    public static final String PARTICLE_DERIVATION_NS_SUBST$WILDCARD_SUBSET = "rcase-NSSubset.2";

    /**
     * rcase-Recurse: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-Recurse">XMLSchema Structures 1.0: Particle Derivation OK (All:All,Sequence:Sequence -- Recurse)</a>
     */
    public static final String PARTICLE_DERIVATION_RECURSE = "rcase-Recurse";

    /**
     * rcase-Recurse.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-Recurse">XMLSchema Structures 1.0: Particle Derivation OK (All:All,Sequence:Sequence -- Recurse)</a>
     */
    public static final String PARTICLE_DERIVATION_RECURSE$MAP = "rcase-Recurse.2";

    /**
     * rcase-Recurse.2.1: See clause 2.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-Recurse">XMLSchema Structures 1.0: Particle Derivation OK (All:All,Sequence:Sequence -- Recurse)</a>
     */
    public static final String PARTICLE_DERIVATION_RECURSE$MAP_VALID = "rcase-Recurse.2.1";

    /**
     * rcase-Recurse.2.2: See clause 2.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-Recurse">XMLSchema Structures 1.0: Particle Derivation OK (All:All,Sequence:Sequence -- Recurse)</a>
     */
    public static final String PARTICLE_DERIVATION_RECURSE$UNMAPPED_ARE_EMPTIABLE = "rcase-Recurse.2.2";

    /**
     * rcase-RecurseAsIfGroup: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-RecurseAsIfGroup">XMLSchema Structures 1.0: Particle Derivation OK (Elt:All/Choice/Sequence -- RecurseAsIfGroup)</a>
     */
    public static final String PARTICLE_DERIVATION_RECURSE_GROUP = "rcase-RecurseAsIfGroup";

    /**
     * rcase-RecurseLax: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-RecurseLax">XMLSchema Structures 1.0: Particle Derivation OK (Choice:Choice -- RecurseLax)</a>
     */
    public static final String PARTICLE_DERIVATION_RECURSE_LAX = "rcase-RecurseLax";

    /**
     * rcase-RecurseLax.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-RecurseLax">XMLSchema Structures 1.0: Particle Derivation OK (Choice:Choice -- RecurseLax)</a>
     */
    public static final String PARTICLE_DERIVATION_RECURSE_LAX$MAP = "rcase-RecurseLax.2";

    /**
     * rcase-RecurseUnordered: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-RecurseUnordered">XMLSchema Structures 1.0: Particle Derivation OK (Sequence:All -- RecurseUnordered)</a>
     */
    public static final String PARTICLE_DERIVATION_RECURSE_UNORDERED = "rcase-RecurseUnordered";

    /**
     * rcase-RecurseUnordered.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-RecurseUnordered">XMLSchema Structures 1.0: Particle Derivation OK (Sequence:All -- RecurseUnordered)</a>
     */
    public static final String PARTICLE_DERIVATION_RECURSE_UNORDERED$MAP = "rcase-RecurseUnordered.2";

    /**
     * rcase-RecurseUnordered.2.1: See clause 2.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-RecurseUnordered">XMLSchema Structures 1.0: Particle Derivation OK (Sequence:All -- RecurseUnordered)</a>
     */
    public static final String PARTICLE_DERIVATION_RECURSE_UNORDERED$MAP_UNIQUE = "rcase-RecurseUnordered.2.1";

    /**
     * rcase-RecurseUnordered.2.2: See clause 2.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-RecurseUnordered">XMLSchema Structures 1.0: Particle Derivation OK (Sequence:All -- RecurseUnordered)</a>
     */
    public static final String PARTICLE_DERIVATION_RECURSE_UNORDERED$MAP_VALID = "rcase-RecurseUnordered.2.2";

    /**
     * rcase-RecurseUnordered.2.2: See clause 2.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-RecurseUnordered">XMLSchema Structures 1.0: Particle Derivation OK (Sequence:All -- RecurseUnordered)</a>
     */
    public static final String PARTICLE_DERIVATION_RECURSE_UNORDERED$MAP_MAX_OCCURS_1 = "rcase-RecurseUnordered.2.2a";

    /**
     * rcase-RecurseUnordered.2.3: See clause 2.3 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#rcase-RecurseUnordered">XMLSchema Structures 1.0: Particle Derivation OK (Sequence:All -- RecurseUnordered)</a>
     */
    public static final String PARTICLE_DERIVATION_RECURSE_UNORDERED$UNMAPPED_ARE_EMPTIABLE = "rcase-RecurseUnordered.2.3";

    /**
     * sch-props-correct: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#sch-props-correct">XMLSchema Structures 1.0: Schema Properties Correct</a>
     */
    public static final String SCHEMA_PROPERTIES = "sch-props-correct";

    /**
     * sch-props-correct.2: See clause 2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#sch-props-correct">XMLSchema Structures 1.0: Schema Properties Correct</a>
     */
    public static final String SCHEMA_PROPERTIES$DUPLICATE = "sch-props-correct.2";

    /**
     * st-props-correct: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#st-props-correct">XMLSchema Structures 1.0: Simple Type Definition Properties Correct</a>
     */
    public static final String SIMPLE_TYPE_PROPERTIES = "st-props-correct";

    /**
     * st-props-correct.3: See clause 3 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#st-props-correct">XMLSchema Structures 1.0: Simple Type Definition Properties Correct</a>
     */
    public static final String SIMPLE_TYPE_PROPERTIES$RESTRICTION_FINAL = "st-props-correct.3";

    /**
     * st-props-correct.4.2.1: See clause 4.2.1 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#st-props-correct">XMLSchema Structures 1.0: Simple Type Definition Properties Correct</a>
     */
    public static final String SIMPLE_TYPE_PROPERTIES$LIST_FINAL = "st-props-correct.4.2.1";

    /**
     * st-props-correct.4.2.2: See clause 4.2.2 of
     * <a href="http://www.w3c.org/TR/xmlschema-1/#st-props-correct">XMLSchema Structures 1.0: Simple Type Definition Properties Correct</a>
     */
    public static final String SIMPLE_TYPE_PROPERTIES$UNION_FINAL = "st-props-correct.4.2.2";

    /**
     * st-restrict-facets: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#st-restrict-facets">XMLSchema Structures 1.0: Simple Type Restriction (Facets)</a>
     */
    public static final String SIMPLE_TYPE_RESTRICTION_FACETS = "st-restrict-facets";

    /**
     * totalDigits-valid-restriction: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#totalDigits-valid-restriction">XMLSchema Datatypes 1.0: totalDigits valid restriction</a>
     */
    public static final String DATATYPE_TOTAL_DIGITS_RESTRICTION = "totalDigits-valid-restriction";

    /**
     * w-props-correct: See
     * <a href="http://www.w3c.org/TR/xmlschema-1/#w-props-correct">XMLSchema Structures 1.0: Wildcard Properties Correct</a>
     */
    public static final String WILDCARD_PROPERTIES = "w-props-correct";

    /**
     * whiteSpace-valid-restriction: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#whiteSpace-valid-restriction">XMLSchema Datatypes 1.0: whiteSpace valid restriction</a>
     */
    public static final String DATATYPE_WHITESPACE_RESTRICTION = "whiteSpace-valid-restriction";

    //
    // datatypes
    //

    /**
     * anyURI: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#anyURI">XMLSchema Datatypes 1.0: anyURI</a>
     */
    public static final String ANYURI = "anyURI";

    /**
     * base64Binary: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#base64Binary">XMLSchema Datatypes 1.0: base64Binary</a>
     */
    public static final String BASE64BINARY = "base64Binary";

    /**
     * boolean: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#boolean">XMLSchema Datatypes 1.0: boolean</a>
     */
    public static final String BOOLEAN = "boolean";

    /**
     * date: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#date">XMLSchema Datatypes 1.0: date</a>
     */
    public static final String DATE = "date";

    /**
     * decimal: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#decimal">XMLSchema Datatypes 1.0: decimal</a>
     */
    public static final String DECIMAL = "decimal";

    /**
     * double: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#double">XMLSchema Datatypes 1.0: double</a>
     */
    public static final String DOUBLE = "double";

    /**
     * duration: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#duration">XMLSchema Datatypes 1.0: duration</a>
     */
    public static final String DURATION = "duration";

    /**
     * float: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#float">XMLSchema Datatypes 1.0: float</a>
     */
    public static final String FLOAT = "float";

    /**
     * hexBinary: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#hexBinary">XMLSchema Datatypes 1.0: hexBinary</a>
     */
    public static final String HEXBINARY = "hexBinary";

    /**
     * int: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#int">XMLSchema Datatypes 1.0: int</a>
     */
    public static final String INT = "int";

    /**
     * integer: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#integer">XMLSchema Datatypes 1.0: integer</a>
     */
    public static final String INTEGER = "integer";

    /**
     * list: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#list-datatypes">XMLSchema Datatypes 1.0: list</a>
     */
    public static final String LIST = "list";

    /**
     * long: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#long">XMLSchema Datatypes 1.0: long</a>
     */
    public static final String LONG = "long";

    /**
     * NCName: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#NCName">XMLSchema Datatypes 1.0: NCName</a>
     */
    public static final String NCNAME = "NCName";

    /**
     * NMTOKEN: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#NMTOKEN">XMLSchema Datatypes 1.0: NMTOKEN</a>
     */
    public static final String NMTOKEN = "NMTOKEN";

    /**
     * QName: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#QName">XMLSchema Datatypes 1.0: QName</a>
     */
    public static final String QNAME = "QName";

    /**
     * union: See
     * <a href="http://www.w3c.org/TR/xmlschema-2/#union-datatypes">XMLSchema Datatypes 1.0: union</a>
     */
    public static final String UNION = "union";

    //
    // following error codes aren't defined by the XMLSchema spec
    //

    /**
     * Found an attribute during validation that was declared use='prohibited':
     * See second NOTE at the end of <a href="http://www.w3c.org/TR/xmlschema-1/#declare-type">3.4.2 XML Representation of Complex Type Definitions</a>.
     *
     * <blockquote>
     * <b>NOTE: </b>
     * The <i>only</i> substantive function of the value <i>prohibited</i> for the
     * <code>use</code> attribute of an <a href="http://www.w3c.org/TR/xmlschema-1/#element-attribute" class="eltref">&lt;attribute&gt;</a> is in establishing
     * the correspondence between a complex type defined by restriction and its XML
     * representation.  It serves to prevent inheritance of an identically named attribute
     * use from the <a href="http://www.w3c.org/TR/xmlschema-1/#ct-base_type_definition" class="propref">{base type definition}</a>.
     * Such an <a href="http://www.w3c.org/TR/xmlschema-1/#element-attribute" class="eltref">&lt;attribute&gt;</a>
     * does not correspond to any component, and hence there is no interaction with either explicit
     * or inherited wildcards in the operation of <a href="http://www.w3c.org/TR/xmlschema-1/#formal-complex-type">Complex Type Definition Validation Rules (3.4.4)</a>
     * or <a href="#coss-ct">Constraints on Complex Type Definition Schema Components (3.4.6)</a>.
     * </blockquote>
     */
    public static final String ELEM_COMPLEX_TYPE_LOCALLY_VALID$PROHIBITED_ATTRIBUTE = "cvc-complex-type.prohibited-attribute";

    /**
     *
     */
    public static final String SOAPARRAY = "soaparray";

    /**
     *
     */
    public static final String FACET_FIXED = "facet-fixed";

    /**
     *
     */
    public static final String PATTERN_REGEX = "pattern-regex";

    /**
     *
     */
    public static final String MISSING_NAME = "missing-name";

    /**
     *
     */
    public static final String RESERVED_TYPE_NAME = "reserved-type-name";

    /**
     * Error code for an invalid attribute value.
     */
    public static final String INVALID_VALUE = "invalid-value";

    /**
     * Error code for an invalid attribute value.
     */
    public static final String INVALID_VALUE_DETAIL = "invalid-value-detail";

    /**
     *
     */
    public static final String INVALID_XPATH = "invalid-xpath";
}
