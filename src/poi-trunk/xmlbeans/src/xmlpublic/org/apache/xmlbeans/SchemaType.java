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

import javax.xml.namespace.QName;

/**
 * Represents a schema type.
 * <p>
 * SchemaType is the metadata "type" class for {@link XmlObject}, and it plays the
 * analogous role for {@link XmlObject} that {@link java.lang.Class} plays for
 * {@link java.lang.Object}.
 * <p>
 * Every XML Bean class corresponds to a singleton SchemaType object
 * obtainable by ClassName.type (e.g., {@link XmlNonPositiveInteger#type}), and
 * every XML Bean instance has an actual SchemaType, obtainable by
 * {@link XmlObject#schemaType}.  The ClassName.type and schemaType() mechanisms are
 * analogous to the ordinary Java ClassName.class and obj.getClass() mechanisms.
 * <p>
 * All schema types are represented by a SchemaType, this includes all types
 * regardless of whether they are built-in or user-defined, compiled or
 * uncompiled, simple or complex.
 * <p>
 * In addition, a compiled XML Bean type system includes special "document" schema types
 * each representing a document containing nothing but a single element for each
 * global element, and special "attribute" schema types each representing
 * a fragment containing nothing but a single global attribute for global
 * attribute.
 * <p>
 * Do not confuse Schema Types with other types of Schema Components such as
 * {@link SchemaGlobalElement Global Elements}, {@link SchemaGlobalAttribute Global Attributes},
 * {@link SchemaModelGroup Named Model Groups}, or {@link SchemaAttributeGroup Attribute Groups}.
 * SchemaType represents a Type component, not any of the other kinds of components.
 * There are different kinds of metadata objects for the different Schema components. 
 * <p>
 * The space of SchemaTypes is divided as follows:
 * 
 * <ul>
 * <li>First, there is the universal base type and the universal
 *     subtype.  These are {@link XmlObject#type}
 *     (corresponding to xs:anyType) and {@link XmlBeans#NO_TYPE},
 *     respectively.  The first type is a base type of all other
 *     types.  The other type indicates the absence of type information
 *     and, at least in set-theoretic terms, is a subtype of all other types.
 * <li>There is another universal base type that is the base type
 *     for all simple types.  This is the {@link XmlAnySimpleType#type},
 *     corresponding to xs:anySimpleType.   Only XmlObject.type and
 *     XmlAnySimpleType.type return <code>true</code> for {@link #isURType}, and
 *     only XmlBeans.NO_TYPE returns <code>true</code> for {@link #isNoType}.
 * <li>The two "special" kinds of types that are generated that
 *     do not formally exist in the actual Schema specification are
 *     document types and global attribute types (corresponding to
 *     documents that contain a global element, or fragments that
 *     contain a global attribute).  They can be detected using
 *     {@link #isDocumentType} and {@link #isAttributeType}. Other
 *     than their anonymity (lack of a type name) and their appearance
 *     only at the root of an instance, they are otherwise just like
 *     ordinary complex types.
 * <li>Simple types can be detected using {@link #isSimpleType}.
 *     Complex types are consdered to be all the types that are
 *     not simple.
 * <li>Simple types are divided into three varieties: atomic types,
 *     list types, and union types.  Which variety of simple type
 *     you have can be discoverd using {@link #getSimpleVariety}.
 *     It will return either {@link #ATOMIC}, {@link #LIST}, or
 *     {@link #UNION}.
 * <li>An {@link #ATOMIC} simple type is always based on one of the
 *     20 built-in primitive schema types.  You can determine
 *     the underlying primitive type for an atomic simple type
 *     by calling {@link #getPrimitiveType}.  An atomic type
 *     may add facet restrictions on top of the primitive type,
 *     and these facets can be explored using {@link #getFacet},
 *     {@link #getWhiteSpaceRule}, {@link #matchPatternFacet},
 *     {@link #getEnumerationValues}, and related methods.
 * <li>A {@link #LIST} simple type is always based on another non-list
 *     simple type.  The underlying list item type can be obtained
 *     by using {@link #getListItemType}.
 * <li>A {@link #UNION} simple type is always composed out of a number of
 *     other simple types.  The direct members of the union can
 *     be obtained by {@link #getUnionMemberTypes}.  When unions
 *     consist of other unions, it is useful to know useful to know
 *     the "leaves of the union tree", so the
 *     set of non-union types making up the union can be obtained
 *     by {@link #getUnionConstituentTypes}.  The closure of the
 *     entire "union tree" is {@link #getUnionSubTypes} (this includes
 *     the type itself).  For
 *     simple unions that do not consist of other unions, all three
 *     of these sets are the same.
 * <li>Complex types have nested structure.  They are divided into
 *     four content types: empty content, simple content, element-only
 *     content, and mixed content.  All kinds of complex types may
 *     have attributes.  The content type for a complex type can
 *     be dermined using {@link #getContentType}.  This will return
 *     {@link #EMPTY_CONTENT}, {@link #SIMPLE_CONTENT},
 *     {@link #ELEMENT_CONTENT}, or {@link #MIXED_CONTENT}.
 * <li>If a complex type has {@link #EMPTY_CONTENT}, the content model will be null.
 * <li>If a complex type has {@link #SIMPLE_CONTENT}, then it will extend the
 *     simple type that describes the content.  In addition, the type
 *     may impose additional simple type facet restrictions; these can 
 *     be determined in the same way they are for a simple type.
 * <li>If a complex type has {@link #ELEMENT_CONTENT} or {@link #MIXED_CONTENT}, then
 *     the detailed content model can be determined by examining
 *     the particle tree (which may be null for MIXED_CONTENT).
 *     The particle tree can be obtained via {@link #getContentModel}.
 * <li>When working with a complex type, most users will find it
 *     sufficient to discover the summarized shape of the content model
 *     and attribute model using {@link #getElementProperties},
 *     {@link #getAttributeProperties}, and related methods rather than
 *     examining the particle tree and attribute model directly.
 * </ul>
 * 
 * @see SchemaTypeLoader
 * @see XmlObject#schemaType
 * @see SimpleValue#instanceType
 */ 
public interface SchemaType extends SchemaComponent, SchemaAnnotated
{
    /**
     * The name used to describe the type in the schema.
     * Null if the type is anonymous (nested), or if it is a document type.
     */
    public abstract QName getName();

    /**
     * The parent schema element.
     * Null for top-level (named) types and document types.
     */
    public abstract SchemaField getContainerField();

    /**
     * True if this is a document type.
     * <p>
     * Document types are generated for every global element. A document
     * type is an unnamed complex type that contains exactly one element:
     * we define these types, because they are the types of the "documents"
     * which contain the defined global elements, and they all turn into
     * Java types. (Named ElementnameDocument.)
     */
    public boolean isDocumentType();

    /**
     * True if this is a attribute type.
     * <p>
     * Attribute types are generated for every global attribute. An attribute
     * type is an unnamed complex type that contains exactly one attribute:
     * we define these types, because they are the types of the "attribute documents"
     * which contain the defined global attribute, and they all turn into
     * Java types. (Named AttributenameAttribute.)
     */
    public boolean isAttributeType();


    /**
     * Returns the document element name if this is a document type,
     * or null otherwise.
     */
    public QName getDocumentElementName();

    /**
     * Returns the attribute qname if this is a attribute type,
     * or null otherwise.
     */
    public QName getAttributeTypeAttributeName();

    /**
     * The outer schema type.
     * Null for top-level (named) types.
     */
    public abstract SchemaType getOuterType();

    /**
     * True if this anonymous type has no corresponding Java type. True for
     * anonymous types nested within simple type restrictions.
     */
    public abstract boolean isSkippedAnonymousType();

    /**
     * True if this schema type was compiled to have a corresponding
     * Java class.
     */
    public abstract boolean isCompiled();

    /**
     * The fully-qualified Java type name of the class.
     */
    public abstract String getFullJavaName();

    /**
     * The short unqualfiied Java name for the class.
     */
    public abstract String getShortJavaName();

    /**
     * The fully-qualified Java type name of the implementation class.
     */
    public abstract String getFullJavaImplName();

    /**
     * The short unqualfiied Java name for the implementation class.
     */
    public abstract String getShortJavaImplName();

    /**
     * The Java class corresponding to this schema type.
     */
    public abstract Class getJavaClass();

    /**
     * The Java class corresponding to the enumeration type for this schema type,
     * if applicable (or null if not an enumeration).
     */
    public abstract Class getEnumJavaClass();

    /**
     * Returns user-specific information.
     * @see SchemaBookmark
     */
    public Object getUserData();

    /**
     * True if the Xsd type is anonymous (i.e., not top-level).
     */
    public abstract boolean isAnonymousType();

    /**
     * True for any of the 40+ built-in types.
     */
    public abstract boolean isBuiltinType();

    /**
     * True for the anySimpleType and any restrictions/unions/lists.
     */
    public abstract boolean isSimpleType();

    /**
     * Returns base restriction or extension type. Unions and lists
     * return the anySimpleType.
     */
    public abstract SchemaType getBaseType();

    /**
     * Returns common base type with the given type. The returned
     * type is the most specific declared base type of both types.
     */
    public abstract SchemaType getCommonBaseType(SchemaType type);

    /**
     * True if the specified type derives from this type (or if
     * it is the same type).
     *
     * Note that XmlObject.type (the anyType) is assignable
     * from all type, and the XmlBeans.noType (the absence of
     * a type) is assignable to all types.
     */
    public abstract boolean isAssignableFrom(SchemaType type);

    /**
     * Returns an integer for the derivation type, either 
     * {@link #DT_EXTENSION}, {@link #DT_RESTRICTION}, {@link #DT_NOT_DERIVED}.
     */
    public int getDerivationType();
    
    /** Not derived.  True for XmlObject.type only. See {@link #getDerivationType}. */
    public static final int DT_NOT_DERIVED = 0;
    /** Derived by restriction. See {@link #getDerivationType}. */
    public static final int DT_RESTRICTION = 1;
    /** Derived by extension. See {@link #getDerivationType}. */
    public static final int DT_EXTENSION = 2;

    /**
     * Returns an integer for builtin types that can be used
     * for quick comparison.
     */
    public abstract int getBuiltinTypeCode();

    /** Not a builtin type */ 
    public static final int BTC_NOT_BUILTIN = 0;
    /** xs:anyType, aka {@link XmlObject#type} */ 
    public static final int BTC_ANY_TYPE = 1;
    
    /** The primitive types have codes between BTC_FIRST_PRIMITIVE and BTC_LAST_PRIMITIVE inclusive */
    public static final int BTC_FIRST_PRIMITIVE = 2;

    /** xs:anySimpleType, aka {@link XmlAnySimpleType#type} */ 
    public static final int BTC_ANY_SIMPLE = 2;

    /** xs:boolean, aka {@link XmlBoolean#type} */ 
    public static final int BTC_BOOLEAN = 3;
    /** xs:base64Binary, aka {@link XmlBase64Binary#type} */ 
    public static final int BTC_BASE_64_BINARY = 4;
    /** xs:hexBinary, aka {@link XmlBase64Binary#type} */ 
    public static final int BTC_HEX_BINARY = 5;
    /** xs:anyURI, aka {@link XmlAnyURI#type} */ 
    public static final int BTC_ANY_URI = 6;
    /** xs:QName, aka {@link XmlQName#type} */ 
    public static final int BTC_QNAME = 7;
    /** xs:NOTATION, aka {@link XmlNOTATION#type} */ 
    public static final int BTC_NOTATION = 8;
    /** xs:float, aka {@link XmlFloat#type} */ 
    public static final int BTC_FLOAT = 9;
    /** xs:double, aka {@link XmlDouble#type} */ 
    public static final int BTC_DOUBLE = 10;
    /** xs:decimal, aka {@link XmlDecimal#type} */ 
    public static final int BTC_DECIMAL = 11;
    /** xs:string, aka {@link XmlString#type} */ 
    public static final int BTC_STRING = 12;

    /** xs:duration, aka {@link XmlDuration#type} */ 
    public static final int BTC_DURATION = 13;
    /** xs:dateTime, aka {@link XmlDateTime#type} */ 
    public static final int BTC_DATE_TIME = 14;
    /** xs:time, aka {@link XmlTime#type} */ 
    public static final int BTC_TIME = 15;
    /** xs:date, aka {@link XmlDate#type} */ 
    public static final int BTC_DATE = 16;
    /** xs:gYearMonth, aka {@link XmlGYearMonth#type} */ 
    public static final int BTC_G_YEAR_MONTH = 17;
    /** xs:gYear, aka {@link XmlGYear#type} */ 
    public static final int BTC_G_YEAR = 18;
    /** xs:gMonthDay, aka {@link XmlGMonthDay#type} */ 
    public static final int BTC_G_MONTH_DAY = 19;
    /** xs:gDay, aka {@link XmlGDay#type} */ 
    public static final int BTC_G_DAY = 20;
    /** xs:gMonth, aka {@link XmlGMonth#type} */ 
    public static final int BTC_G_MONTH = 21;

    /** The primitive types have codes between BTC_FIRST_PRIMITIVE and BTC_LAST_PRIMITIVE inclusive */
    public static final int BTC_LAST_PRIMITIVE = 21;

    // derived numerics
    /** xs:integer, aka {@link XmlInteger#type} */ 
    public static final int BTC_INTEGER = 22;
    /** xs:long, aka {@link XmlLong#type} */ 
    public static final int BTC_LONG = 23;
    /** xs:int, aka {@link XmlInt#type} */ 
    public static final int BTC_INT = 24;
    /** xs:short, aka {@link XmlShort#type} */ 
    public static final int BTC_SHORT = 25;
    /** xs:byte, aka {@link XmlByte#type} */ 
    public static final int BTC_BYTE = 26;
    /** xs:nonPositiveInteger, aka {@link XmlNonPositiveInteger#type} */ 
    public static final int BTC_NON_POSITIVE_INTEGER = 27;
    /** xs:NegativeInteger, aka {@link XmlNegativeInteger#type} */ 
    public static final int BTC_NEGATIVE_INTEGER = 28;
    /** xs:nonNegativeInteger, aka {@link XmlNonNegativeInteger#type} */ 
    public static final int BTC_NON_NEGATIVE_INTEGER = 29;
    /** xs:positiveInteger, aka {@link XmlPositiveInteger#type} */ 
    public static final int BTC_POSITIVE_INTEGER = 30;
    /** xs:unsignedLong, aka {@link XmlUnsignedLong#type} */
    public static final int BTC_UNSIGNED_LONG = 31;
    /** xs:unsignedInt, aka {@link XmlUnsignedInt#type} */
    public static final int BTC_UNSIGNED_INT = 32;
    /** xs:unsignedShort, aka {@link XmlUnsignedShort#type} */
    public static final int BTC_UNSIGNED_SHORT = 33;
    /** xs:unsignedByte, aka {@link XmlUnsignedByte#type} */
    public static final int BTC_UNSIGNED_BYTE = 34;

    // derived strings
    /** xs:normalizedString, aka {@link XmlNormalizedString#type} */
    public static final int BTC_NORMALIZED_STRING = 35;
    /** xs:token, aka {@link XmlToken#type} */
    public static final int BTC_TOKEN = 36;
    /** xs:Name, aka {@link XmlName#type} */
    public static final int BTC_NAME = 37;
    /** xs:NCName, aka {@link XmlNCName#type} */
    public static final int BTC_NCNAME = 38;
    /** xs:language, aka {@link XmlLanguage#type} */
    public static final int BTC_LANGUAGE = 39;
    /** xs:ID, aka {@link XmlID#type} */
    public static final int BTC_ID = 40;
    /** xs:IDREF, aka {@link XmlIDREF#type} */
    public static final int BTC_IDREF = 41;
    /** xs:IDREFS, aka {@link XmlIDREFS#type} */
    public static final int BTC_IDREFS = 42;
    /** xs:ENTITY, aka {@link XmlENTITY#type} */
    public static final int BTC_ENTITY = 43;
    /** xs:ENTITIES, aka {@link XmlENTITIES#type} */
    public static final int BTC_ENTITIES = 44;
    /** xs:NMTOKEN, aka {@link XmlNMTOKEN#type} */
    public static final int BTC_NMTOKEN = 45;
    /** xs:NMTOKENS, aka {@link XmlNMTOKENS#type} */
    public static final int BTC_NMTOKENS = 46;

    public static final int BTC_LAST_BUILTIN = 46;

    /**
     * True for anyType and anySimpleType.
     */
    public boolean isURType();

    /**
     * True for the type object that represents a the absence of a determined type.
     * XML Objects whose type isNoType() are never valid.
     */
    public boolean isNoType();

    /**
     * Returns the SchemaTypeLoader in which this type was defined.
     * Complex types are defined and used in exactly one schema type
     * system, but simple types are defined in one type system and can
     * be used in any number of type systems. The most common case is
     * the builtin types, which are defined in the builtin type system
     * and used elsewhere.
     */
    public SchemaTypeSystem getTypeSystem();

    /** True if this type cannot be used directly in instances */
    public boolean isAbstract();

    /** True if other types cannot extend this type (only for complex types) */
    public boolean finalExtension();

    /** True if other types cannot restrict this type */
    public boolean finalRestriction();

    /** True if list derivation of this type is prohibited (only for simple types) */
    public boolean finalList();

    /** True if union derivation of this type is prohibited (only for simple types) */
    public boolean finalUnion();

    /** True if extensions of this type cannot be substituted for this type */
    public boolean blockExtension();

    /** True if restrictions of this type cannot be substituted for this type */
    public boolean blockRestriction();

    /**
     * Returns {@link #EMPTY_CONTENT}, {@link #SIMPLE_CONTENT}, {@link #ELEMENT_CONTENT}, or
     * {@link #MIXED_CONTENT} for complex types. For noncomplex types, returns
     * {@link #NOT_COMPLEX_TYPE}.
     */
    public abstract int getContentType();
    
    /** Not a complex type.  See {@link #getContentType()}. */
    public static final int NOT_COMPLEX_TYPE = 0;
    /** Empty content.  See {@link #getContentType()}. */
    public static final int EMPTY_CONTENT = 1;
    /** Simple content.  See {@link #getContentType()}. */
    public static final int SIMPLE_CONTENT = 2;
    /** Element-only content.  See {@link #getContentType()}. */
    public static final int ELEMENT_CONTENT = 3;
    /** Mixed content.  See {@link #getContentType()}. */
    public static final int MIXED_CONTENT = 4;


    /**
     * For complex types with simple content returns the base type for this
     * type's content. In most cases, this is the same as the base type, but
     * it can also be an anonymous type.
     */
    SchemaType getContentBasedOnType();

    /**
     * Returns a {@link SchemaTypeElementSequencer} object, which can then
     * be used to validate complex content inside this element. This is useful
     * for example for trying out different names and see which one would be
     * valid as a child of this element.
     */
    SchemaTypeElementSequencer getElementSequencer();

    /**
     * The array of inner (anonymous) types defined
     * within this type.
     */
    public abstract SchemaType[] getAnonymousTypes();

    /**
     * Returns a SchemaProperty corresponding to an element within this
     * complex type by looking up the element name.
     */
    public abstract SchemaProperty getElementProperty(QName eltName);

    /**
     * Returns all the SchemaProperties corresponding to elements.
     */
    public abstract SchemaProperty[] getElementProperties();

    /**
     * Returns a SchemaProperty corresponding to an attribute within this
     * complex type by looking up the attribute name.
     */
    public abstract SchemaProperty getAttributeProperty(QName attrName);

    /**
     * Returns all the SchemaProperties corresponding to attributes.
     */
    public abstract SchemaProperty[] getAttributeProperties();

    /**
     * Returns all the SchemaProperties within this complex type,
     * elements followed by attributes.
     */
    public abstract SchemaProperty[] getProperties();

    /**
     * Returns the SchemaProperties defined by this complex type,
     * exclusive of the base type (if any).
     */
    SchemaProperty[] getDerivedProperties();

    /**
     * Returns the attribute model for this complex type (with simple or complex content).
     */
    public abstract SchemaAttributeModel getAttributeModel();

    /**
     * True if this type permits wildcard attributes. See the attribute model for
     * more information about which wildcards are allowed.
     */
    public abstract boolean hasAttributeWildcards();

    /**
     * Returns the complex content model for this complex type (with complex content).
     */
    public abstract SchemaParticle getContentModel();

    /**
     * True if this type permits element wildcards. See the content model for
     * more information about which wildcards are allowed, and where.
     */
    public abstract boolean hasElementWildcards();

    /**
     * For document types, true if the given name can be substituted for the
     * document element name.
     */
    public boolean isValidSubstitution(QName name);

    /**
     * True if the complex content model for this complex type is an "all" group.
     */
    public abstract boolean hasAllContent();

    /**
     * True if particles have same defaults, nillability, etc, that are
     * invariant when order changes. Computed only for Javaized types.
     */
    public abstract boolean isOrderSensitive();

    /**
     * Returns the type of a child element based on the element name and
     * an xsi:type attribute (and the type system within which names are
     * resolved).
     */
    public abstract SchemaType getElementType(QName eltName, QName xsiType, SchemaTypeLoader wildcardTypeLoader);

    /**
     * Returns the type of an attribute based on the attribute name and
     * the type system within which (wildcard) names are resolved.
     */
    public abstract SchemaType getAttributeType(QName eltName, SchemaTypeLoader wildcardTypeLoader);

    /*************************************************************/
    /* SIMPLE TYPE MODEL BELOW                                   */
    /*************************************************************/

    /** xs:length facet */
    public static final int FACET_LENGTH = 0;
    /** xs:minLength facet */
    public static final int FACET_MIN_LENGTH = 1;
    /** xs:maxLength facet */
    public static final int FACET_MAX_LENGTH = 2;
    /** xs:minExclusive facet */
    public static final int FACET_MIN_EXCLUSIVE = 3;
    /** xs:minInclusive facet */
    public static final int FACET_MIN_INCLUSIVE = 4;
    /** xs:maxInclusive facet */
    public static final int FACET_MAX_INCLUSIVE = 5;
    /** xs:maxExclusive facet */
    public static final int FACET_MAX_EXCLUSIVE = 6;
    /** xs:totalDigits facet */
    public static final int FACET_TOTAL_DIGITS = 7;
    /** xs:fractionDigits facet */
    public static final int FACET_FRACTION_DIGITS = 8;

    public static final int LAST_BASIC_FACET = 8;

    /** xs:whiteSpace facet - use {@link #getWhiteSpaceRule} instead */
    public static final int FACET_WHITE_SPACE = 9;
    /** xs:pattern facet - use {@link #matchPatternFacet} instead */
    public static final int FACET_PATTERN = 10;
    /** xs:enumeration facet - use {@link #getEnumerationValues} instead */
    public static final int FACET_ENUMERATION = 11;

    /** The last ordinary facet code */ 
    public static final int LAST_FACET = 11;
    
    /** @see #ordered */
    public static final int PROPERTY_ORDERED = 12;
    /** @see #isBounded */
    public static final int PROPERTY_BOUNDED = 13;
    /** @see #isFinite */
    public static final int PROPERTY_CARDINALITY = 14;
    /** @see #isNumeric */
    public static final int PROPERTY_NUMERIC = 15;
    
    /** The last property code */
    public static final int LAST_PROPERTY = 15;


    /**
     * Returns the value of the given facet, or null if
     * none is set.
     */
    public abstract XmlAnySimpleType getFacet(int facetCode);

    /**
     * True if the given facet is fixed.
     */
    public abstract boolean isFacetFixed(int facetCode);

    /**
     * True if ordered.  Returns either {@link #UNORDERED},
     * {@link #PARTIAL_ORDER}, or {@link #TOTAL_ORDER}.
     */
    public abstract int ordered();

    /** Unordered. See {@link #ordered}. */
    public static int UNORDERED = 0;
    /** Partially ordered. See {@link #ordered}. */
    public static int PARTIAL_ORDER = 1;
    /** Totally ordered. See {@link #ordered}. */
    public static int TOTAL_ORDER = 2;

    /**
     * True if bounded.
     */
    public abstract boolean isBounded();

    /**
     * True if finite.
     */
    public abstract boolean isFinite();

    /**
     * True if numeric.
     */
    public abstract boolean isNumeric();

    /**
     * True if there are regex pattern facents
     */
    public abstract boolean hasPatternFacet();
    
    /**
     * True 
     */
    public abstract String[] getPatterns();

    /**
     * True if the given string matches the pattern facets.
     * Always true if there are no pattern facets.
     */
    public abstract boolean matchPatternFacet(String s);

    /**
     * Returns the array of valid objects from the
     * enumeration facet, null if no enumeration defined.
     */
    public abstract XmlAnySimpleType[] getEnumerationValues();

    /**
     * True if this is a string enum where an integer
     * is assigned to each enumerated value.
     */
    public abstract boolean hasStringEnumValues();

    /**
     * If this is a string enumeration, returns the most basic base schema
     * type that this enuemration is based on. Otherwise returns null.
     */
    public abstract SchemaType getBaseEnumType();

    /**
     * Returns the array of SchemaStringEnumEntries for this type: this
     * array includes information about the java constant names used for
     * each string enum entry.
     */
    public SchemaStringEnumEntry[] getStringEnumEntries();

    /**
     * Returns the string enum entry corresponding to the given enumerated
     * string, or null if there is no match or this type is not
     * a string enumeration.
     */
    public SchemaStringEnumEntry enumEntryForString(String s);

    /**
     * Returns the string enum value corresponding to the given enumerated
     * string, or null if there is no match or this type is not
     * a string enumeration.
     */
    public abstract StringEnumAbstractBase enumForString(String s);

    /**
     * Returns the string enum value corresponding to the given enumerated
     * string, or null if there is no match or this type is not
     * a string enumeration.
     */
    public abstract StringEnumAbstractBase enumForInt(int i);

    /**
     * True for any of the 20 primitive types (plus anySimpleType)
     */
    public abstract boolean isPrimitiveType();

    /**
     * Returns whether the simple type is ATOMIC, UNION, or LIST.
     * Returns {@link #NOT_SIMPLE}, {@link #ATOMIC}, {@link #UNION},
     * or {@link #LIST}.
     */
    public abstract int getSimpleVariety();
    
    /** Not a simple type or simple content. See {@link #getSimpleVariety}. */
    public static final int NOT_SIMPLE = 0;
    /** Atomic type.  See {@link #getSimpleVariety} */
    public static final int ATOMIC = 1;
    /** Union type.  See {@link #getSimpleVariety} */
    public static final int UNION = 2;
    /** Simple list type.  See {@link #getSimpleVariety} */
    public static final int LIST = 3;


    /**
     * For atomic types only: get the primitive type underlying this one.
     * <p>
     * Returns null if this is not an atomic type.
     */
    public abstract SchemaType getPrimitiveType();

    /**
     * For atomic numeric restrictions of decimal only: the
     * numeric size category. Takes into account min and max
     * restrictions as well as totalDigits and fractionDigits
     * facets.
     * <p>
     * Returns either {@link #NOT_DECIMAL},
     * {@link #SIZE_BYTE}, {@link #SIZE_SHORT}, {@link #SIZE_INT},
     * {@link #SIZE_LONG}, {@link #SIZE_BIG_INTEGER}, or
     * {@link #SIZE_BIG_DECIMAL}.
     */
    public abstract int getDecimalSize();

    /** Not a decimal restriction. See {@link #getDecimalSize}. */
    public static final int NOT_DECIMAL = 0;
    /** Fits in a byte. See {@link #getDecimalSize}. */
    public static final int SIZE_BYTE = 8;
    /** Fits in a short. See {@link #getDecimalSize}. */
    public static final int SIZE_SHORT = 16;
    /** Fits in an int. See {@link #getDecimalSize}. */
    public static final int SIZE_INT = 32;
    /** Fits in a long. See {@link #getDecimalSize}. */
    public static final int SIZE_LONG = 64;
    /** Fits in a {@link java.math.BigInteger}. See {@link #getDecimalSize}. */
    public static final int SIZE_BIG_INTEGER = 1000000; // "millions"
    /** Fits in a {@link java.math.BigDecimal}. See {@link #getDecimalSize}. */
    public static final int SIZE_BIG_DECIMAL = 1000001; // "even more"

    /**
     * For union types only: get the shallow member types. This
     * returns the declared member types of the union, so, for
     * example if the type contains another union, the nested
     * members of that union are NOT returned here.
     * <p>
     * Returns null if this type is not a union.
     */
    public abstract SchemaType[] getUnionMemberTypes();

    /**
     * For union types only: gets the full tree of member types.
     * This computes the closure of the set returned by
     * getUnionMemberTypes(), so, for example, it returns
     * all the types nested within unions of unions as well
     * as the top-level members; the set also includes the
     * type itself. If you are seeking only the basic
     * non-union consituents, use getUnionConstituentTypes.
     * <p>
     * Returns null if this type is not a union.
     */
    public abstract SchemaType[] getUnionSubTypes();

    /**
     * For union types only: get the constituent member types. This
     * returns only non-union types, so, for example, for unions of
     * unions, this returns the flattened list of individual member
     * types within the innermost unions.
     * <p>
     * Returns null if this type is not a union.
     */
    public abstract SchemaType[] getUnionConstituentTypes();

    /**
     * For union types only: get the most specific common base
     * type of the constituent member types. May return a UR type.
     * <p>
     * Returns null if this type is not a union.
     */
    public abstract SchemaType getUnionCommonBaseType();

    /**
     * For anonymous types defined inside a union only: gets
     * the integer indicating the declaration order of this
     * type within the outer union type, or zero if this is
     * not applicable. The first anonymous union member within
     * a union type is numbered "1". Used to differentiate
     * between different anonymous types.
     */
    public abstract int getAnonymousUnionMemberOrdinal();

    /**
     * For list types only: get the item type. This is the atomic
     * or union type that is the type of every entry in the list.
     * <p>
     * Returns null if this type is not a list.
     */
    public abstract SchemaType getListItemType();

    /**
     * For nonunion simple types: get the whitespace rule. This is
     * either {@link #WS_PRESERVE}, {@link #WS_REPLACE}, or
     * {@link #WS_COLLAPSE}. Returns {@link #WS_UNSPECIFIED}
     * for unions and complex types.
     */
    public abstract int getWhiteSpaceRule();

    /** Whitespace rule unspecified.  See {@link #getWhiteSpaceRule}. */
    public static final int WS_UNSPECIFIED = 0;
    /** Whitespace preserved.  See {@link #getWhiteSpaceRule}. */
    public static final int WS_PRESERVE = 1;
    /** Whitespace replaced by ordinary space.  See {@link #getWhiteSpaceRule}. */
    public static final int WS_REPLACE = 2;
    /** Whitespace collapsed and trimmed.  See {@link #getWhiteSpaceRule}. */
    public static final int WS_COLLAPSE = 3;

    /**
     * Creates an immutable simple type value that does not reside in a tree.
     */
    public abstract XmlAnySimpleType newValue(Object v);
    

    /**
     * Used to allow on-demand loading of types.
     * 
     * @exclude
     */
    public final static class Ref extends SchemaComponent.Ref
    {
        public Ref(SchemaType type)
            { super(type); }

        public Ref(SchemaTypeSystem system, String handle)
            { super(system, handle); }

        public final int getComponentType()
            { return SchemaComponent.TYPE; }

        public final SchemaType get()
            { return (SchemaType)getComponent(); }
    }

    /**
     * Retruns a SchemaType.Ref pointing to this schema type itself.
     */
    public Ref getRef();

    /**
     * Returns a QNameSet of elements that may exist in wildcard
     * buchets and are not explicitly defined in this schema type.
     * Note: In this example:
     *  <xs:complexType name="exampleType">
     *    <xs:sequence>
     *      <xs:element name="someElement" type='xs:string' />
     *      <xs:any namespace="##targetNamespace" />
     *    </xs:sequence>
     *  </xs:complexType>
     *  the returned QNameSet will not contain the qname of 'someElement'.
     * @return the constructed QNameSet
     */
    public QNameSet qnameSetForWildcardElements();

    /**
     * Returns a QNameSet of attributes that may exist in wildcard
     * buchets and are not explicitly defined in this schema type.
     * Note: In this example:
     *  <xs:complexType name="exampleType">
     *    ...
     *    <xs:attribute name='someAttribute' type='xs:string' />
     *    <xs:anyAttribute namespace="##targetNamespace" />
     *  </xs:complexType>
     *  the returned QNameSet will not contain the qname of 'someAttribute'.
     * @return the constructed QNameSet
     */
    public QNameSet qnameSetForWildcardAttributes();
}
