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

package org.apache.xmlbeans.impl.schema;

import java.util.*;
import java.util.List;
import java.math.BigInteger;

import javax.xml.namespace.QName;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
import org.apache.xmlbeans.impl.regex.RegularExpression;
import org.apache.xmlbeans.impl.regex.ParseException;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlByte;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlNonNegativeInteger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlPositiveInteger;
import org.apache.xmlbeans.XmlShort;
import org.apache.xmlbeans.XmlUnsignedByte;
import org.apache.xmlbeans.impl.xb.xsdschema.*;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Schema;

public class StscSimpleTypeResolver
{

    /**************************************************************************
     * SIMPLE TYPE RESOLUTION HERE
     *
     * Simple types can be declared as lists, unions, or restrictions.
     * These three cases are treated separately in resolveListType,
     * resolveUnionType, and resolveSimpleRestrictionType.
     *
     * The intricate work with facets is done in the restriction case,
     * using method called resolveFacets (the union and list cases have
     * trivial facet rules). Then all simple types call resolveProperties
     * in the end to have their "fundamental facets" resolved.
     */

    public static void resolveSimpleType(SchemaTypeImpl sImpl)
    {
        SimpleType parseSt = (SimpleType)sImpl.getParseObject();
        
        assert sImpl.isSimpleType();

        Schema schema = StscComplexTypeResolver.getSchema(parseSt);

        // Verify: have list, union, or restriction, but not more than one
        int count =
                (parseSt.isSetList() ? 1 : 0) +
                (parseSt.isSetUnion() ? 1 : 0) +
                (parseSt.isSetRestriction() ? 1 : 0);
        if (count > 1)
        {
            StscState.get().error(
                    "A simple type must define either a list, a union, or a restriction: more than one found.",
                    XmlErrorCodes.MALFORMED_SIMPLE_TYPE_DEFN,
                    parseSt);
            // recovery: treat it as the first of list, union, restr
        }
        else if (count < 1)
        {
            StscState.get().error("A simple type must define either a list, a union, or a restriction: none was found.",
                    XmlErrorCodes.MALFORMED_SIMPLE_TYPE_DEFN,
                    parseSt);
            // recovery: treat it as restriction of anySimpleType
            resolveErrorSimpleType(sImpl);
            return;
        }

        // Set final flags
        boolean finalRest = false;
        boolean finalList = false;
        boolean finalUnion = false;

        Object finalValue = null;
        if (parseSt.isSetFinal())
        {
            finalValue = parseSt.getFinal();
        }
        // Inspect the finalDefault attribute on the schema
        else if (schema != null && schema.isSetFinalDefault())
        {
            finalValue = schema.getFinalDefault();
        }

        if (finalValue != null)
        {
            if (finalValue instanceof String)
            {
                if ("#all".equals((String)finalValue))
                {
                    finalRest = finalList = finalUnion = true;
                }
            }
            else if (finalValue instanceof List)
            {
                List lFinalValue = (List) finalValue;
                if (lFinalValue.contains("restriction"))
                    finalRest = true;

                if (lFinalValue.contains("list"))
                    finalList = true;

                if (lFinalValue.contains("union"))
                    finalUnion= true;
            }
        }

        sImpl.setSimpleFinal(finalRest, finalList, finalUnion);

        List anonTypes = new ArrayList();

        if (parseSt.getList() != null)
            resolveListType(sImpl, parseSt.getList(), anonTypes);
        else if (parseSt.getUnion() != null)
            resolveUnionType(sImpl, parseSt.getUnion(), anonTypes);
        else if (parseSt.getRestriction() != null)
            resolveSimpleRestrictionType(sImpl, parseSt.getRestriction(), anonTypes);

        sImpl.setAnonymousTypeRefs(makeRefArray(anonTypes));
    }

    private static SchemaType.Ref[] makeRefArray(Collection typeList)
    {
        SchemaType.Ref[] result = new SchemaType.Ref[typeList.size()];
        int j = 0;
        for (Iterator i = typeList.iterator(); i.hasNext(); j++)
            result[j] = ((SchemaType)i.next()).getRef();
        return result;
    }

    static void resolveErrorSimpleType(SchemaTypeImpl sImpl)
    {
        sImpl.setSimpleTypeVariety(SchemaType.ATOMIC);
        sImpl.setBaseTypeRef(BuiltinSchemaTypeSystem.ST_ANY_SIMPLE.getRef());
        sImpl.setBaseDepth(BuiltinSchemaTypeSystem.ST_ANY_SIMPLE.getBaseDepth() + 1);
        sImpl.setPrimitiveTypeRef(BuiltinSchemaTypeSystem.ST_ANY_SIMPLE.getRef());
    }

    static void resolveListType(SchemaTypeImpl sImpl, org.apache.xmlbeans.impl.xb.xsdschema.ListDocument.List parseList, List anonTypes)
    {
        StscState state = StscState.get();

        sImpl.setSimpleTypeVariety(SchemaType.LIST);
        sImpl.setBaseTypeRef(BuiltinSchemaTypeSystem.ST_ANY_SIMPLE.getRef());
        sImpl.setBaseDepth(BuiltinSchemaTypeSystem.ST_ANY_SIMPLE.getBaseDepth() + 1);
        sImpl.setDerivationType(SchemaType.DT_RESTRICTION);

        if (sImpl.isRedefinition())
        {
            state.error(XmlErrorCodes.SCHEMA_REDEFINE$EXTEND_OR_RESTRICT,
                new Object[] { "list" }, parseList);
            // recovery: oh well.
        }
        
        QName itemName = parseList.getItemType();
        LocalSimpleType parseInner = parseList.getSimpleType();

        if (itemName != null && parseInner != null)
        {
            state.error(XmlErrorCodes.SCHEMA_SIMPLE_TYPE$LIST_HAS_BOTH_ITEM_OR_SIMPLE_TYPE, null, parseList);
            // recovery: ignore the inner simple type.
            parseInner = null;
        }

        SchemaTypeImpl itemImpl;
        XmlObject errorLoc;

        if (itemName != null)
        {
            itemImpl = state.findGlobalType(itemName, sImpl.getChameleonNamespace(), sImpl.getTargetNamespace());
            errorLoc = parseList.xgetItemType();
            if (itemImpl == null)
            {
                state.notFoundError(itemName, SchemaType.TYPE, parseList.xgetItemType(), true);
                // recovery: treat it as a list of anySimpleType
                itemImpl = BuiltinSchemaTypeSystem.ST_ANY_SIMPLE;
            }
        }
        else if (parseInner != null)
        {
            itemImpl = StscTranslator.translateAnonymousSimpleType(parseInner,
                sImpl.getTargetNamespace(), sImpl.getChameleonNamespace() != null,
                sImpl.getElemFormDefault(), sImpl.getAttFormDefault(), anonTypes, sImpl);
            errorLoc = parseInner;
        }
        else
        {
            state.error(XmlErrorCodes.SCHEMA_SIMPLE_TYPE$LIST_HAS_NEITHER_ITEM_OR_SIMPLE_TYPE, null, parseList);
            // recovery: treat it as an extension of anySimpleType
            resolveErrorSimpleType(sImpl);
            return;
        }

        // Verify final restrictions
        if (itemImpl.finalList())
            state.error(XmlErrorCodes.SIMPLE_TYPE_PROPERTIES$LIST_FINAL, null, parseList);

        // Recursion...
        StscResolver.resolveType(itemImpl);

        if (!itemImpl.isSimpleType())
        {
            state.error(XmlErrorCodes.SIMPLE_TYPE_RESTRICTION$LIST_ITEM_NOT_SIMPLE, null, errorLoc);
            // recovery: treat the item type as anySimpleType
            sImpl = BuiltinSchemaTypeSystem.ST_ANY_SIMPLE;
        }

        switch (itemImpl.getSimpleVariety())
        {
            case SchemaType.LIST:
                state.error(XmlErrorCodes.SIMPLE_TYPE_RESTRICTION$LIST_ITEM_IS_LIST, null, errorLoc);
                // recovery: treat the list as an anySimpleType
                resolveErrorSimpleType(sImpl);
                return;
            case SchemaType.UNION:
                if (itemImpl.isUnionOfLists())
                {
                    state.error(XmlErrorCodes.SIMPLE_TYPE_RESTRICTION$LIST_ITEM_IS_UNION_OF_LIST, null, errorLoc);
                    resolveErrorSimpleType(sImpl);
                    return;
                }
                // fallthrough: nonlist unions are just like atomic items
            case SchemaType.ATOMIC:
                sImpl.setListItemTypeRef(itemImpl.getRef());
                // Check that the item type is not a plan NOTATION
                if (sImpl.getBuiltinTypeCode() == SchemaType.BTC_NOTATION)
                    state.recover(XmlErrorCodes.DATATYPE_ENUM_NOTATION, null, errorLoc);
                break;
            default:
                assert(false);
                sImpl.setListItemTypeRef(BuiltinSchemaTypeSystem.ST_ANY_SIMPLE.getRef());
        }

        // now deal with facets
        sImpl.setBasicFacets(StscState.FACETS_LIST, StscState.FIXED_FACETS_LIST);
        sImpl.setWhiteSpaceRule( SchemaType.WS_COLLAPSE );

        // now compute our intrinsic properties
        resolveFundamentalFacets(sImpl);
    }




    static void resolveUnionType(SchemaTypeImpl sImpl, UnionDocument.Union parseUnion, List anonTypes)
    {
        sImpl.setSimpleTypeVariety(SchemaType.UNION);
        sImpl.setBaseTypeRef(BuiltinSchemaTypeSystem.ST_ANY_SIMPLE.getRef());
        sImpl.setBaseDepth(BuiltinSchemaTypeSystem.ST_ANY_SIMPLE.getBaseDepth() + 1);
        sImpl.setDerivationType(SchemaType.DT_RESTRICTION);

        StscState state = StscState.get();
        
        if (sImpl.isRedefinition())
        {
            state.error(XmlErrorCodes.SCHEMA_REDEFINE$EXTEND_OR_RESTRICT,
                new Object[] { "union" }, parseUnion);
            // recovery: oh well.
        }
        
        List memberTypes = parseUnion.getMemberTypes();
        SimpleType[] simpleTypes = parseUnion.getSimpleTypeArray();

        List memberImplList = new ArrayList();
        
        if (simpleTypes.length == 0 && (memberTypes == null || memberTypes.size() == 0))
        {
            state.error(XmlErrorCodes.SCHEMA_SIMPLE_TYPE$UNION_HAS_MEMBER_TYPES_OR_SIMPLE_TYPES, null, parseUnion);
            // recovery: oh well, zero member types is fine.
        }

        if (memberTypes != null)
        {
            for (Iterator mNames = memberTypes.iterator(); mNames.hasNext(); )
            {
                QName mName = (QName)mNames.next();
                SchemaTypeImpl memberImpl = state.findGlobalType(mName, sImpl.getChameleonNamespace(), sImpl.getTargetNamespace());
                if (memberImpl == null)
                    // recovery: skip member
                    state.notFoundError(mName, SchemaType.TYPE, parseUnion.xgetMemberTypes(), true);
                else
                    memberImplList.add(memberImpl);
            }
        }

        for (int i = 0; i < simpleTypes.length; i++)
        {
            // BUGBUG: see if non<simpleType> children can leak through
            SchemaTypeImpl mImpl = StscTranslator.translateAnonymousSimpleType(simpleTypes[i],
                sImpl.getTargetNamespace(), sImpl.getChameleonNamespace() != null,
                sImpl.getElemFormDefault(), sImpl.getAttFormDefault(), anonTypes, sImpl);
            memberImplList.add(mImpl);
            mImpl.setAnonymousUnionMemberOrdinal(i + 1);
        }

        // Recurse and resolve all member types
        for (Iterator mImpls = memberImplList.iterator(); mImpls.hasNext(); )
        {
            SchemaTypeImpl mImpl = (SchemaTypeImpl)mImpls.next();
            if (!StscResolver.resolveType(mImpl))
            {
                // KHK: review
                String memberName = "";
                XmlObject errorLoc;
                if (mImpl.getOuterType().equals(sImpl))
                {
                    errorLoc = mImpl.getParseObject();
                }
                else
                {
                    memberName = QNameHelper.pretty(mImpl.getName()) + " ";
                    errorLoc = parseUnion.xgetMemberTypes();
                }

                state.error(XmlErrorCodes.SCHEMA_SIMPLE_TYPE$CYCLIC_UNION, new Object[] { memberName }, errorLoc);

                // recovery: ignore the errant union member
                mImpls.remove();
                continue;
            }
        }

        // Now verify members
        boolean isUnionOfLists = false;

        for (Iterator mImpls = memberImplList.iterator(); mImpls.hasNext(); )
        {
            SchemaTypeImpl mImpl = (SchemaTypeImpl)mImpls.next();

            if (!mImpl.isSimpleType())
            {
                // KHK: review
                String memberName = "";
                XmlObject errorLoc;
                if (mImpl.getOuterType() != null && mImpl.getOuterType().equals(sImpl))
                {
                    errorLoc = mImpl.getParseObject();
                }
                else
                {
                    memberName = QNameHelper.pretty(mImpl.getName()) + " ";
                    errorLoc = parseUnion.xgetMemberTypes();
                }

                state.error(XmlErrorCodes.SIMPLE_TYPE_RESTRICTION$UNION_MEMBER_NOT_SIMPLE, new Object[] { memberName }, errorLoc);

                // recovery: ignore the errant union member
                mImpls.remove();
                continue;
            }

            if (mImpl.getSimpleVariety() == SchemaType.LIST ||
                mImpl.getSimpleVariety() == SchemaType.UNION && mImpl.isUnionOfLists())
                isUnionOfLists = true;
        }

        // Verify any final restrictions
        for (int i = 0 ; i < memberImplList.size() ; i++)
        {
            SchemaTypeImpl mImpl = (SchemaTypeImpl)memberImplList.get(i);
            if (mImpl.finalUnion())
                state.error(XmlErrorCodes.SIMPLE_TYPE_PROPERTIES$UNION_FINAL, null, parseUnion);
        }

        sImpl.setUnionOfLists(isUnionOfLists);

        sImpl.setUnionMemberTypeRefs(makeRefArray(memberImplList));

        // now deal with facets
        sImpl.setBasicFacets(StscState.FACETS_UNION, StscState.FIXED_FACETS_UNION);

        // now compute our intrinsic properties
        resolveFundamentalFacets(sImpl);
    }

    static void resolveSimpleRestrictionType(SchemaTypeImpl sImpl, RestrictionDocument.Restriction parseRestr, List anonTypes)
    {
        QName baseName = parseRestr.getBase();
        SimpleType parseInner = parseRestr.getSimpleType();
        StscState state = StscState.get();

        if (baseName != null && parseInner != null)
        {
            state.error(XmlErrorCodes.SCHEMA_SIMPLE_TYPE$RESTRICTION_HAS_BOTH_BASE_OR_SIMPLE_TYPE, null, parseRestr);
            // recovery: ignore the inner simple type.
            parseInner = null;
        }

        SchemaTypeImpl baseImpl;

        if (baseName != null)
        {
            if (sImpl.isRedefinition())
            {
                baseImpl = state.findRedefinedGlobalType(parseRestr.getBase(), sImpl.getChameleonNamespace(), sImpl);
                if (baseImpl != null && !baseImpl.getName().equals(sImpl.getName()))
                {
                    state.error(XmlErrorCodes.SCHEMA_REDEFINE$SAME_TYPE,
                        new Object[] { "<simpleType>",
                                       QNameHelper.pretty(baseName), 
                                       QNameHelper.pretty(sImpl.getName())
                        },
                        parseRestr);
                }
            }
            else
            {
                baseImpl = state.findGlobalType(baseName, sImpl.getChameleonNamespace(), sImpl.getTargetNamespace());
            }
            if (baseImpl == null)
            {
                state.notFoundError(baseName, SchemaType.TYPE, parseRestr.xgetBase(), true);
                // recovery: treat it as an extension of anySimpleType
                baseImpl = BuiltinSchemaTypeSystem.ST_ANY_SIMPLE;
            }
        }
        else if (parseInner != null)
        {
            if (sImpl.isRedefinition())
            {
                StscState.get().error(XmlErrorCodes.SCHEMA_REDEFINE$EXTEND_OR_RESTRICT,
                    new Object[] { "<simpleType>" }, parseInner);
                // recovery: oh well.
            }
            
            baseImpl = StscTranslator.translateAnonymousSimpleType(parseInner,
                sImpl.getTargetNamespace(), sImpl.getChameleonNamespace() != null,
                sImpl.getElemFormDefault(), sImpl.getAttFormDefault(), anonTypes, sImpl);
        }
        else
        {
            state.error(XmlErrorCodes.SCHEMA_SIMPLE_TYPE$RESTRICTION_HAS_NEITHER_BASE_OR_SIMPLE_TYPE, null, parseRestr);
            // recovery: treat it as an extension of anySimpleType
            baseImpl = BuiltinSchemaTypeSystem.ST_ANY_SIMPLE;
        }

        // Recursion!
        if (!StscResolver.resolveType(baseImpl))
        {
            // cyclic dependency recovery: treat it as an extension of anySimpleType
            baseImpl = BuiltinSchemaTypeSystem.ST_ANY_SIMPLE;
        }

        if (baseImpl.finalRestriction())
            state.error(XmlErrorCodes.SIMPLE_TYPE_PROPERTIES$RESTRICTION_FINAL, null, parseRestr);

        sImpl.setBaseTypeRef(baseImpl.getRef());
        sImpl.setBaseDepth(baseImpl.getBaseDepth() + 1);
        sImpl.setDerivationType(SchemaType.DT_RESTRICTION);

        if (!baseImpl.isSimpleType())
        {
            state.error(XmlErrorCodes.SIMPLE_TYPE_RESTRICTION$ATOMIC_NOT_SIMPLE, null, parseRestr.xgetBase());
            // recovery: treat it as a restriction of anySimpleType
            resolveErrorSimpleType(sImpl);
            return;
        }

        sImpl.setSimpleTypeVariety(baseImpl.getSimpleVariety());

        // copy variety-specific properties
        switch (baseImpl.getSimpleVariety())
        {
            case SchemaType.ATOMIC:
                sImpl.setPrimitiveTypeRef(baseImpl.getPrimitiveType().getRef());
                break;
            case SchemaType.UNION:
                sImpl.setUnionOfLists(baseImpl.isUnionOfLists());
                sImpl.setUnionMemberTypeRefs(makeRefArray(Arrays.asList(baseImpl.getUnionMemberTypes())));
                break;
            case SchemaType.LIST:
                sImpl.setListItemTypeRef(baseImpl.getListItemType().getRef());
                break;
        }

        // deal with facets
        resolveFacets(sImpl, parseRestr, baseImpl);

        // now compute our intrinsic properties
        resolveFundamentalFacets(sImpl);
    }

    static int translateWhitespaceCode(XmlAnySimpleType value)
    {
        // BUGBUG: add whitespace rule to textvalue.
        String textval = value.getStringValue();

        if (textval.equals("collapse"))
            return SchemaType.WS_COLLAPSE;

        if (textval.equals("preserve"))
            return SchemaType.WS_PRESERVE;

        if (textval.equals("replace"))
            return SchemaType.WS_REPLACE;

        // KHK: s4s
        StscState.get().error("Unrecognized whitespace value \"" + textval + "\"", XmlErrorCodes.FACET_VALUE_MALFORMED, value);
        return SchemaType.WS_UNSPECIFIED;
    }

    static boolean isMultipleFacet(int facetcode)
    {
        return (facetcode == SchemaType.FACET_ENUMERATION ||
                facetcode == SchemaType.FACET_PATTERN);
    }

    static boolean facetAppliesToType(int facetCode, SchemaTypeImpl baseImpl)
    {
        switch (baseImpl.getSimpleVariety())
        {
            case SchemaType.LIST:
                switch (facetCode)
                {
                    case SchemaType.FACET_LENGTH:
                    case SchemaType.FACET_MIN_LENGTH:
                    case SchemaType.FACET_MAX_LENGTH:
                    case SchemaType.FACET_ENUMERATION:
                    case SchemaType.FACET_PATTERN:
                    case SchemaType.FACET_WHITE_SPACE:
                        return true;
                }
                return false;

            case SchemaType.UNION:
                switch (facetCode)
                {
                    case SchemaType.FACET_ENUMERATION:
                    case SchemaType.FACET_PATTERN:
                        return true;
                }
                return false;
        }

        // the atomic case

        switch (baseImpl.getPrimitiveType().getBuiltinTypeCode())
        {
            case SchemaType.BTC_ANY_SIMPLE:
                return false;

            case SchemaType.BTC_BOOLEAN:
                switch (facetCode)
                {
                    case SchemaType.FACET_PATTERN:
                    case SchemaType.FACET_WHITE_SPACE:
                        return true;
                }
                return false;

            case SchemaType.BTC_FLOAT:
            case SchemaType.BTC_DOUBLE:
            case SchemaType.BTC_DURATION:
            case SchemaType.BTC_DATE_TIME:
            case SchemaType.BTC_TIME:
            case SchemaType.BTC_DATE:
            case SchemaType.BTC_G_YEAR_MONTH:
            case SchemaType.BTC_G_YEAR:
            case SchemaType.BTC_G_MONTH_DAY:
            case SchemaType.BTC_G_DAY:
            case SchemaType.BTC_G_MONTH:
                switch (facetCode)
                {
                    case SchemaType.FACET_MIN_EXCLUSIVE:
                    case SchemaType.FACET_MIN_INCLUSIVE:
                    case SchemaType.FACET_MAX_INCLUSIVE:
                    case SchemaType.FACET_MAX_EXCLUSIVE:
                    case SchemaType.FACET_ENUMERATION:
                    case SchemaType.FACET_PATTERN:
                    case SchemaType.FACET_WHITE_SPACE:
                        return true;
                }
                return false;

            case SchemaType.BTC_DECIMAL:
                switch (facetCode)
                {
                    case SchemaType.FACET_MIN_EXCLUSIVE:
                    case SchemaType.FACET_MIN_INCLUSIVE:
                    case SchemaType.FACET_MAX_INCLUSIVE:
                    case SchemaType.FACET_MAX_EXCLUSIVE:
                    case SchemaType.FACET_TOTAL_DIGITS:
                    case SchemaType.FACET_FRACTION_DIGITS:
                    case SchemaType.FACET_ENUMERATION:
                    case SchemaType.FACET_PATTERN:
                    case SchemaType.FACET_WHITE_SPACE:
                        return true;
                }
                return false;

            case SchemaType.BTC_BASE_64_BINARY:
            case SchemaType.BTC_HEX_BINARY:
            case SchemaType.BTC_ANY_URI:
            case SchemaType.BTC_QNAME:
            case SchemaType.BTC_NOTATION:
            case SchemaType.BTC_STRING:
                switch (facetCode)
                {
                    case SchemaType.FACET_LENGTH:
                    case SchemaType.FACET_MIN_LENGTH:
                    case SchemaType.FACET_MAX_LENGTH:
                    case SchemaType.FACET_ENUMERATION:
                    case SchemaType.FACET_PATTERN:
                    case SchemaType.FACET_WHITE_SPACE:
                        return true;
                }
                return false;

            default:
                assert(false);
                return false;
        }
    }

    private static int other_similar_limit(int facetcode)
    {
        switch (facetcode)
        {
            case SchemaType.FACET_MIN_EXCLUSIVE:
                return SchemaType.FACET_MIN_INCLUSIVE;
            case SchemaType.FACET_MIN_INCLUSIVE:
                return SchemaType.FACET_MIN_EXCLUSIVE;
            case SchemaType.FACET_MAX_INCLUSIVE:
                return SchemaType.FACET_MAX_EXCLUSIVE;
            case SchemaType.FACET_MAX_EXCLUSIVE:
                return SchemaType.FACET_MAX_INCLUSIVE;
            default:
                assert(false);
                throw new IllegalStateException();
        }
    }

    static void resolveFacets(SchemaTypeImpl sImpl, XmlObject restriction, SchemaTypeImpl baseImpl)
    {
        StscState state = StscState.get();

        boolean[] seenFacet = new boolean[SchemaType.LAST_FACET + 1];
        XmlAnySimpleType[] myFacets = baseImpl.getBasicFacets(); // makes a copy
        boolean[] fixedFacets = baseImpl.getFixedFacets();
        int wsr = SchemaType.WS_UNSPECIFIED;
        List enumeratedValues = null;
        List patterns = null;

        if (restriction != null)
        {
            XmlCursor cur = restriction.newCursor();
            for (boolean more = cur.toFirstChild(); more; more = cur.toNextSibling())
            {
                QName facetQName = cur.getName();
                String facetName = facetQName.getLocalPart();
                int code = translateFacetCode(facetQName);
                if (code == -1)
                    continue;

                Facet facet = (Facet)cur.getObject();

                if (!facetAppliesToType(code, baseImpl))
                {
                    state.error(XmlErrorCodes.FACETS_APPLICABLE,
                        new Object[] { facetName, QNameHelper.pretty(baseImpl.getName()) }, facet);
                    continue;
                }
                else if (baseImpl.getSimpleVariety() == SchemaType.ATOMIC &&
                    baseImpl.getPrimitiveType().getBuiltinTypeCode() == SchemaType.BTC_NOTATION
                    && (code == SchemaType.FACET_LENGTH || code == SchemaType.FACET_MIN_LENGTH ||
                    code == SchemaType.FACET_MAX_LENGTH))
                {
                    state.warning(XmlErrorCodes.FACETS_DEPRECATED_NOTATION,
                        new Object[] {facetName, QNameHelper.pretty(baseImpl.getName()) }, facet);
                }
                if (seenFacet[code] && !isMultipleFacet(code))
                {
                    state.error(XmlErrorCodes.DATATYPE_SINGLE_FACET_VALUE, null, facet);
                    continue;
                }
                seenFacet[code] = true;

                switch (code)
                {
                    case SchemaType.FACET_LENGTH:
//                        if (myFacets[SchemaType.FACET_MIN_LENGTH] != null ||
//                            myFacets[SchemaType.FACET_MAX_LENGTH] != null)
//                        {
//                            state.error(XmlErrorCodes.DATATYPE_LENGTH, null, facet);
//                            continue;
//                        }
                        XmlInteger len = StscTranslator.buildNnInteger(facet.getValue());
                        if (len == null)
                        {
                            state.error("Must be a nonnegative integer", XmlErrorCodes.FACET_VALUE_MALFORMED, facet);
                            continue;
                        }
                        if (fixedFacets[code] && !myFacets[code].valueEquals(len))
                        {
                            state.error(XmlErrorCodes.FACET_FIXED, new Object[] { facetName }, facet);
                            continue;
                        }
                        if (myFacets[SchemaType.FACET_MIN_LENGTH] != null)
                        {
                            // An error for 'length' and 'minLength' to be specified at the same time
                            // except if the base type had the same value for 'minLength' also
                            XmlAnySimpleType baseMinLength = baseImpl.getFacet(SchemaType.FACET_MIN_LENGTH);
                            if (!(baseMinLength != null &&
                                baseMinLength.valueEquals(myFacets[SchemaType.FACET_MIN_LENGTH]) &&
                                baseMinLength.compareValue(len) <= 0))
                            {
                                state.error(XmlErrorCodes.DATATYPE_LENGTH, null, facet);
                                continue;
                            }
                        }
                        if (myFacets[SchemaType.FACET_MAX_LENGTH] != null)
                        {
                            // An error for 'length' and 'maxLength' to be specified at the same time
                            // except if the base type had the same value for 'maxLength' also
                            XmlAnySimpleType baseMaxLength = baseImpl.getFacet(SchemaType.FACET_MAX_LENGTH);
                            if (!(baseMaxLength != null &&
                                baseMaxLength.valueEquals(myFacets[SchemaType.FACET_MAX_LENGTH]) &&
                                baseMaxLength.compareValue(len) >= 0))
                            {
                                state.error(XmlErrorCodes.DATATYPE_LENGTH, null, facet);
                                continue;
                            }
                        }
                        myFacets[code] = len;
                        break;

                    case SchemaType.FACET_MIN_LENGTH:
                    case SchemaType.FACET_MAX_LENGTH:
                        XmlInteger mlen = StscTranslator.buildNnInteger(facet.getValue());
                        if (mlen == null)
                        {
                            state.error("Must be a nonnegative integer", XmlErrorCodes.FACET_VALUE_MALFORMED, facet);
                            continue;
                        }
                        if (fixedFacets[code] && !myFacets[code].valueEquals(mlen))
                        {
                            state.error(XmlErrorCodes.FACET_FIXED, new Object[] { facetName }, facet);
                            continue;
                        }
                        if (myFacets[SchemaType.FACET_LENGTH] != null)
                        {
                            // It's an error for 'length' and 'minLength'/'maxLength' to be
                            // specified at the same time, except for the case when
                            // the base type had the same value for 'minLength'/'maxLength'
                            // and the two values are consistent
                            XmlAnySimpleType baseMinMaxLength = baseImpl.getFacet(code);
                            if (!(baseMinMaxLength != null &&
                                baseMinMaxLength.valueEquals(mlen) &&
                                (code == SchemaType.FACET_MIN_LENGTH ?
                                    baseMinMaxLength.compareTo(myFacets[SchemaType.FACET_LENGTH]) <= 0 :
                                    baseMinMaxLength.compareTo(myFacets[SchemaType.FACET_LENGTH]) >= 0)))
                            {
                                state.error(XmlErrorCodes.DATATYPE_LENGTH, null, facet);
                                continue;
                            }
                        }
                        if (myFacets[SchemaType.FACET_MAX_LENGTH] != null)
                        {
                            if (mlen.compareValue(myFacets[SchemaType.FACET_MAX_LENGTH]) > 0)
                            {
                                state.error(XmlErrorCodes.DATATYPE_MAX_LENGTH_RESTRICTION, null, facet);
                                continue;
                            }
                        }
                        if (myFacets[SchemaType.FACET_MIN_LENGTH] != null)
                        {
                            if (mlen.compareValue(myFacets[SchemaType.FACET_MIN_LENGTH]) < 0)
                            {
                                state.error(XmlErrorCodes.DATATYPE_MIN_LENGTH_RESTRICTION, null, facet);
                                continue;
                            }
                        }
                        myFacets[code] = mlen;
                        break;

                    case SchemaType.FACET_TOTAL_DIGITS:
                        XmlPositiveInteger dig = StscTranslator.buildPosInteger(facet.getValue());
                        if (dig == null)
                        {
                            state.error("Must be a positive integer", XmlErrorCodes.FACET_VALUE_MALFORMED, facet);
                            break;
                        }
                        if (fixedFacets[code] && !myFacets[code].valueEquals(dig))
                        {
                            state.error(XmlErrorCodes.FACET_FIXED, new Object[] { facetName }, facet);
                            continue;
                        }
                        if (myFacets[SchemaType.FACET_TOTAL_DIGITS] != null)
                        {
                            if (dig.compareValue(myFacets[SchemaType.FACET_TOTAL_DIGITS]) > 0)
                                state.error(XmlErrorCodes.DATATYPE_TOTAL_DIGITS_RESTRICTION, null, facet);
                        }
                        myFacets[code] = dig;
                        break;

                    case SchemaType.FACET_FRACTION_DIGITS:
                        XmlNonNegativeInteger fdig = StscTranslator.buildNnInteger(facet.getValue());
                        if (fdig == null)
                        {
                            state.error("Must be a nonnegative integer", XmlErrorCodes.FACET_VALUE_MALFORMED, facet);
                            break;
                        }
                        if (fixedFacets[code] && !myFacets[code].valueEquals(fdig))
                        {
                            state.error(XmlErrorCodes.FACET_FIXED, new Object[] { facetName }, facet);
                            continue;
                        }
                        if (myFacets[SchemaType.FACET_FRACTION_DIGITS] != null)
                        {
                            if (fdig.compareValue(myFacets[SchemaType.FACET_FRACTION_DIGITS]) > 0)
                                state.error(XmlErrorCodes.DATATYPE_FRACTION_DIGITS_RESTRICTION, null, facet);
                        }
                        if (myFacets[SchemaType.FACET_TOTAL_DIGITS] != null)
                        {
                            if (fdig.compareValue(myFacets[SchemaType.FACET_TOTAL_DIGITS]) > 0)
                                state.error(XmlErrorCodes.DATATYPE_FRACTION_DIGITS_LE_TOTAL_DIGITS, null, facet);
                        }
                        myFacets[code] = fdig;
                        break;

                    case SchemaType.FACET_MIN_EXCLUSIVE:
                    case SchemaType.FACET_MIN_INCLUSIVE:
                    case SchemaType.FACET_MAX_INCLUSIVE:
                    case SchemaType.FACET_MAX_EXCLUSIVE:

                        if (seenFacet[other_similar_limit(code)])
                        {
                            state.error("Cannot define both inclusive and exclusive limit in the same restriciton", XmlErrorCodes.FACET_DUPLICATED, facet);
                            continue;
                        }
                        boolean ismin = (code == SchemaType.FACET_MIN_EXCLUSIVE || code == SchemaType.FACET_MIN_INCLUSIVE);
                        boolean isexclusive = (code == SchemaType.FACET_MIN_EXCLUSIVE || code == SchemaType.FACET_MAX_EXCLUSIVE);

                        XmlAnySimpleType limit;
                        try
                        {
                            limit = baseImpl.newValue(facet.getValue(), true);
                        }
                        catch (XmlValueOutOfRangeException e)
                        {
                            // note: this guarantees that the limit is a valid number in the
                            // base data type!!
                            switch (code)
                            {
                            case SchemaType.FACET_MIN_EXCLUSIVE:
                                state.error(XmlErrorCodes.DATATYPE_MIN_EXCLUSIVE_RESTRICTION,
                                    new Object[] {e.getMessage()}, facet);
                                break;
                            case SchemaType.FACET_MIN_INCLUSIVE:
                                state.error(XmlErrorCodes.DATATYPE_MIN_INCLUSIVE_RESTRICTION,
                                    new Object[] {e.getMessage()}, facet);
                                break;
                            case SchemaType.FACET_MAX_INCLUSIVE:
                                state.error(XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_RESTRICTION,
                                    new Object[] {e.getMessage()}, facet);
                                break;
                            case SchemaType.FACET_MAX_EXCLUSIVE:
                                state.error(XmlErrorCodes.DATATYPE_MAX_EXCLUSIVE_RESTRICTION,
                                    new Object[] {e.getMessage()}, facet);
                                break;
                            }

                            // BUGBUG: if there are actual schemas that redefine min/maxExclusive,
                            // they will need this rule relaxed for them!!
                            continue;
                        }
                        if (fixedFacets[code] && !myFacets[code].valueEquals(limit))
                        {
                            state.error(XmlErrorCodes.FACET_FIXED, new Object[] { facetName }, facet);
                            continue;
                        }
                        if (myFacets[code] != null)
                        {
                            SchemaType limitSType = limit.schemaType();
                            if ( limitSType!=null && !limitSType.isSimpleType() &&
                                    limitSType.getContentType()==SchemaType.SIMPLE_CONTENT)
                            {
                                // in the case of complex types with simple content that has facets
                                // we need to compare values based on the content type
                                limit = baseImpl.getContentBasedOnType().newValue(facet.getValue());
                            }

                            int comparison = limit.compareValue(myFacets[code]);
                            if (comparison == 2 || comparison == (ismin ? -1 : 1))
                            {
                                state.error(ismin ?
                                        (isexclusive ?
                                            "Must be greater than or equal to previous minExclusive" :
                                            "Must be greater than or equal to previous minInclusive") :
                                        (isexclusive ?
                                            "Must be less than or equal to previous maxExclusive" :
                                            "Must be less than or equal to previous maxInclusive"),
                                        XmlErrorCodes.FACET_VALUE_MALFORMED, facet);
                                continue;
                            }
                        }
                        myFacets[code] = limit;
                        myFacets[other_similar_limit(code)] = null;
                        break;

                    case SchemaType.FACET_WHITE_SPACE:
                        wsr = translateWhitespaceCode(facet.getValue());
                        if (baseImpl.getWhiteSpaceRule() > wsr)
                        {
                            wsr = SchemaType.WS_UNSPECIFIED;
                            state.error(XmlErrorCodes.DATATYPE_WHITESPACE_RESTRICTION, null, facet);
                            continue;
                        }
                        myFacets[code] = StscState.build_wsstring(wsr).get();
                        break;

                    case SchemaType.FACET_ENUMERATION:
                        XmlObject enumval;
                        try
                        {
                            enumval = baseImpl.newValue(facet.getValue(), true);
                            // enumval.set(facet.getValue());
                            // ((XmlObjectBase)enumval).setImmutable();
                        }
                        catch (XmlValueOutOfRangeException e)
                        {
                            state.error(XmlErrorCodes.DATATYPE_ENUM_RESTRICTION, new Object[] { facet.getValue().getStringValue(), e.getMessage() }, facet);
                            continue;
                        }
                        if (enumeratedValues == null)
                            enumeratedValues = new ArrayList();
                        enumeratedValues.add(enumval);
                        break;

                    case SchemaType.FACET_PATTERN:
                        RegularExpression p;
                        try { p = new RegularExpression(facet.getValue().getStringValue(), "X"); }
                        catch (ParseException e)
                        {
                            state.error(XmlErrorCodes.PATTERN_REGEX, new Object[] { facet.getValue().getStringValue(), e.getMessage() }, facet);
                            continue;
                        }
                        if (patterns == null)
                            patterns = new ArrayList();
                        patterns.add(p);
                        break;
                }

                if (facet.getFixed())
                    fixedFacets[code] = true;
            }
        }

        // Store the array of basic facets

        sImpl.setBasicFacets(makeValueRefArray(myFacets), fixedFacets);

        // Update the numeric whitespace rule
        if (wsr == SchemaType.WS_UNSPECIFIED)
            wsr = baseImpl.getWhiteSpaceRule();
        sImpl.setWhiteSpaceRule(wsr);

        // store away the enumerated values
        if (enumeratedValues != null)
        {
            sImpl.setEnumerationValues(makeValueRefArray((XmlAnySimpleType[])
                enumeratedValues.toArray(new XmlAnySimpleType[enumeratedValues.size()])));

            SchemaType beType = sImpl;
            if ( ((SchemaTypeImpl)sImpl).isRedefinition() ){
                beType = sImpl.getBaseType().getBaseEnumType();
                if( beType == null || sImpl.getBaseType() == beType ){
                    beType = sImpl;
                }
            }
            else if (sImpl.getBaseType().getBaseEnumType() != null)
                beType = sImpl.getBaseType().getBaseEnumType();
            sImpl.setBaseEnumTypeRef(beType.getRef());
        }
        else
        {
            sImpl.copyEnumerationValues(baseImpl);
        }

        // store the pattern list
        org.apache.xmlbeans.impl.regex.RegularExpression[] patternArray;
        if (patterns != null)
            patternArray = (org.apache.xmlbeans.impl.regex.RegularExpression[])patterns.toArray(EMPTY_REGEX_ARRAY);
        else
            patternArray = EMPTY_REGEX_ARRAY;
        sImpl.setPatternFacet((patternArray.length > 0 || baseImpl.hasPatternFacet()));
        sImpl.setPatterns(patternArray);

        // Check that, if the base type is NOTATION, there is an enumeration facet
        // http://www.w3.org/TR/xmlschema-2/#NOTATION
        if (baseImpl.getBuiltinTypeCode() == SchemaType.BTC_NOTATION)
            if (sImpl.getEnumerationValues() == null)
                state.recover(XmlErrorCodes.DATATYPE_ENUM_NOTATION, null, restriction);
    }

    private static XmlValueRef[] makeValueRefArray(XmlAnySimpleType[] source)
    {
        XmlValueRef[] result = new XmlValueRef[source.length];
        for (int i = 0; i < result.length; i++)
            result[i] = (source[i] == null ? null : new XmlValueRef(source[i]));
        return result;
    }

    private static final org.apache.xmlbeans.impl.regex.RegularExpression[] EMPTY_REGEX_ARRAY = new org.apache.xmlbeans.impl.regex.RegularExpression[0];

    private static boolean isDiscreteType(SchemaTypeImpl sImpl)
    {
        if (sImpl.getFacet(SchemaType.FACET_FRACTION_DIGITS) != null)
            return true;
        // BUGBUG: spec is silent on enumerations; they're finite too.
        switch (sImpl.getPrimitiveType().getBuiltinTypeCode())
        {
            case SchemaType.BTC_DATE:
            case SchemaType.BTC_G_YEAR_MONTH:
            case SchemaType.BTC_G_YEAR:
            case SchemaType.BTC_G_MONTH_DAY:
            case SchemaType.BTC_G_DAY:
            case SchemaType.BTC_G_MONTH:
            case SchemaType.BTC_BOOLEAN:
                return true;
        }
        return false;
    }

    private static boolean isNumericPrimitive(SchemaType sImpl)
    {
        switch (sImpl.getBuiltinTypeCode())
        {
            case SchemaType.BTC_DECIMAL:
            case SchemaType.BTC_FLOAT:
            case SchemaType.BTC_DOUBLE:
                return true;
        }
        return false;
    }

    private static int decimalSizeOfType(SchemaTypeImpl sImpl)
    {
        int size = mathematicalSizeOfType(sImpl);
        
        // byte and short are inconvenient, because setByte((byte)4) requires a cast.
        // So use "int" unless you're really a xs:byte, xs:short, or an xs:unsignedByte
        // (the last case is included for alignment with JAXB)
        
        if (size == SchemaType.SIZE_BYTE && !XmlByte.type.isAssignableFrom(sImpl))
            size = SchemaType.SIZE_SHORT;
        if (size == SchemaType.SIZE_SHORT && !XmlShort.type.isAssignableFrom(sImpl) && !XmlUnsignedByte.type.isAssignableFrom(sImpl))
            size = SchemaType.SIZE_INT;
        
        return size;
    }
    
    private static int mathematicalSizeOfType(SchemaTypeImpl sImpl)
    {
        if (sImpl.getPrimitiveType().getBuiltinTypeCode() != SchemaType.BTC_DECIMAL)
            return SchemaType.NOT_DECIMAL;

        if (sImpl.getFacet(SchemaType.FACET_FRACTION_DIGITS) == null ||
            ((SimpleValue)sImpl.getFacet(SchemaType.FACET_FRACTION_DIGITS)).getBigIntegerValue().signum() != 0)
            return SchemaType.SIZE_BIG_DECIMAL;

        BigInteger min = null;
        BigInteger max = null;

        if (sImpl.getFacet(SchemaType.FACET_MIN_EXCLUSIVE) != null)
            min = ((SimpleValue)sImpl.getFacet(SchemaType.FACET_MIN_EXCLUSIVE)).getBigIntegerValue(); // .add(BigInteger.ONE);
        if (sImpl.getFacet(SchemaType.FACET_MIN_INCLUSIVE) != null)
            min = ((SimpleValue)sImpl.getFacet(SchemaType.FACET_MIN_INCLUSIVE)).getBigIntegerValue();
        if (sImpl.getFacet(SchemaType.FACET_MAX_INCLUSIVE) != null)
            max = ((SimpleValue)sImpl.getFacet(SchemaType.FACET_MAX_INCLUSIVE)).getBigIntegerValue();
        if (sImpl.getFacet(SchemaType.FACET_MAX_EXCLUSIVE) != null)
            max = ((SimpleValue)sImpl.getFacet(SchemaType.FACET_MAX_EXCLUSIVE)).getBigIntegerValue(); // .subtract(BigInteger.ONE);

        if (sImpl.getFacet(SchemaType.FACET_TOTAL_DIGITS) != null)
        {
            BigInteger peg = null;
            try
            {
                BigInteger totalDigits = ((SimpleValue)sImpl.getFacet(SchemaType.FACET_TOTAL_DIGITS)).getBigIntegerValue();

                switch (totalDigits.intValue())
                {
                    case 0: case 1: case 2:
                         peg = BigInteger.valueOf(99L); // BYTE size
                         break;
                    case 3: case 4:
                         peg = BigInteger.valueOf(9999L); // SHORT size
                         break;
                    case 5: case 6: case 7: case 8: case 9:
                         peg = BigInteger.valueOf(999999999L); // INT size
                         break;
                    case 10: case 11: case 12: case 13: case 14:
                    case 15: case 16: case 17: case 18:
                         peg = BigInteger.valueOf(999999999999999999L); // LONG size
                         break;
                }
            }
            catch (XmlValueOutOfRangeException e) {}
            if (peg != null)
            {
                min = (min == null ? peg.negate() : min.max(peg.negate()));
                max = (max == null ? peg : max.min(peg));
            }
        }

        if (min != null && max != null)
        {
            // find the largest "absolute value" number that must be dealt with
            if (min.signum() < 0)
                min = min.negate().subtract(BigInteger.ONE);
            if (max.signum() < 0)
                max = max.negate().subtract(BigInteger.ONE);

            max = max.max(min);
            if (max.compareTo(BigInteger.valueOf(Byte.MAX_VALUE)) <= 0)
                return SchemaType.SIZE_BYTE;
            if (max.compareTo(BigInteger.valueOf(Short.MAX_VALUE)) <= 0)
                return SchemaType.SIZE_SHORT;
            if (max.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0)
                return SchemaType.SIZE_INT;
            if (max.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0)
                return SchemaType.SIZE_LONG;
        }

        return SchemaType.SIZE_BIG_INTEGER;
    }


    static void resolveFundamentalFacets(SchemaTypeImpl sImpl)
    {
        // deal with, isOrdered, isBounded, isFinite, isNumeric
        // also deal with
        switch (sImpl.getSimpleVariety())
        {
            case SchemaType.ATOMIC:
                SchemaTypeImpl baseImpl = (SchemaTypeImpl)sImpl.getBaseType();
                sImpl.setOrdered(baseImpl.ordered());
                sImpl.setBounded(
                    (sImpl.getFacet(SchemaType.FACET_MIN_EXCLUSIVE) != null ||
                     sImpl.getFacet(SchemaType.FACET_MIN_INCLUSIVE) != null) &&
                    (sImpl.getFacet(SchemaType.FACET_MAX_INCLUSIVE) != null ||
                     sImpl.getFacet(SchemaType.FACET_MAX_EXCLUSIVE) != null));
                sImpl.setFinite(baseImpl.isFinite() ||
                                sImpl.isBounded() && isDiscreteType(sImpl));
                sImpl.setNumeric(baseImpl.isNumeric() ||
                                isNumericPrimitive(sImpl.getPrimitiveType()));
                sImpl.setDecimalSize(decimalSizeOfType(sImpl));
                break;
            case SchemaType.UNION:
                SchemaType[] mTypes = sImpl.getUnionMemberTypes();
                int ordered = SchemaType.UNORDERED;
                boolean isBounded = true;
                boolean isFinite = true;
                boolean isNumeric = true;
                // ordered if any is ordered, bounded if all are bounded.
                for (int i = 0; i < mTypes.length; i++)
                {
                    if (mTypes[i].ordered() != SchemaType.UNORDERED)
                        ordered = SchemaType.PARTIAL_ORDER;
                    if (!mTypes[i].isBounded())
                        isBounded = false;
                    if (!mTypes[i].isFinite())
                        isFinite = false;
                    if (!mTypes[i].isNumeric())
                        isNumeric = false;
                }
                sImpl.setOrdered(ordered);
                sImpl.setBounded(isBounded);
                sImpl.setFinite(isFinite);
                sImpl.setNumeric(isNumeric);
                sImpl.setDecimalSize(SchemaType.NOT_DECIMAL);
                break;
            case SchemaType.LIST:
                sImpl.setOrdered(SchemaType.UNORDERED);
                // BUGBUG: the schema spec is wrong here: MIN_LENGTH is not needed, beause len >=0
                sImpl.setBounded(sImpl.getFacet(SchemaType.FACET_LENGTH) != null ||
                    sImpl.getFacet(SchemaType.FACET_MAX_LENGTH) != null);
                // BUGBUG: the schema spec is wrong here: finite cardinality requires item type is finite
                sImpl.setFinite(sImpl.getListItemType().isFinite() && sImpl.isBounded());
                sImpl.setNumeric(false);
                sImpl.setDecimalSize(SchemaType.NOT_DECIMAL);
                break;
        }
    }

    private static class CodeForNameEntry
    {
        CodeForNameEntry(QName name, int code)
            { this.name = name; this.code = code; }
        public QName name;
        public int code;
    }

    private static CodeForNameEntry[] facetCodes = new CodeForNameEntry[]
    {
        new CodeForNameEntry(QNameHelper.forLNS("length", "http://www.w3.org/2001/XMLSchema"), SchemaType.FACET_LENGTH),
        new CodeForNameEntry(QNameHelper.forLNS("minLength", "http://www.w3.org/2001/XMLSchema"), SchemaType.FACET_MIN_LENGTH),
        new CodeForNameEntry(QNameHelper.forLNS("maxLength", "http://www.w3.org/2001/XMLSchema"), SchemaType.FACET_MAX_LENGTH),
        new CodeForNameEntry(QNameHelper.forLNS("pattern", "http://www.w3.org/2001/XMLSchema"), SchemaType.FACET_PATTERN),
        new CodeForNameEntry(QNameHelper.forLNS("enumeration", "http://www.w3.org/2001/XMLSchema"), SchemaType.FACET_ENUMERATION),
        new CodeForNameEntry(QNameHelper.forLNS("whiteSpace", "http://www.w3.org/2001/XMLSchema"), SchemaType.FACET_WHITE_SPACE),
        new CodeForNameEntry(QNameHelper.forLNS("maxInclusive", "http://www.w3.org/2001/XMLSchema"), SchemaType.FACET_MAX_INCLUSIVE),
        new CodeForNameEntry(QNameHelper.forLNS("maxExclusive", "http://www.w3.org/2001/XMLSchema"), SchemaType.FACET_MAX_EXCLUSIVE),
        new CodeForNameEntry(QNameHelper.forLNS("minInclusive", "http://www.w3.org/2001/XMLSchema"), SchemaType.FACET_MIN_INCLUSIVE),
        new CodeForNameEntry(QNameHelper.forLNS("minExclusive", "http://www.w3.org/2001/XMLSchema"), SchemaType.FACET_MIN_EXCLUSIVE),
        new CodeForNameEntry(QNameHelper.forLNS("totalDigits", "http://www.w3.org/2001/XMLSchema"), SchemaType.FACET_TOTAL_DIGITS),
        new CodeForNameEntry(QNameHelper.forLNS("fractionDigits", "http://www.w3.org/2001/XMLSchema"), SchemaType.FACET_FRACTION_DIGITS),
    };

    private static final Map facetCodeMap = buildFacetCodeMap();

    private static Map buildFacetCodeMap()
    {
        Map result = new HashMap();
        for (int i = 0; i < facetCodes.length; i++)
            result.put(facetCodes[i].name,  new Integer(facetCodes[i].code));
        return result;
    }

    private static int translateFacetCode(QName name)
    {
        Integer result = ((Integer)facetCodeMap.get(name));
        if (result == null)
            return -1;
        return result.intValue();
    }
}
