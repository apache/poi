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

import java.math.BigInteger;
import java.util.*;
import java.util.List;

import org.apache.xmlbeans.impl.xb.xsdschema.*;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Schema;
import org.apache.xmlbeans.impl.xb.xsdschema.AnyDocument.Any;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.QNameSetBuilder;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.SchemaAttributeModel;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlNonNegativeInteger;
import org.apache.xmlbeans.SchemaLocalAttribute;
import org.apache.xmlbeans.SchemaLocalElement;
import org.apache.xmlbeans.QNameSetSpecification;

public class StscComplexTypeResolver
{
    public static Group getContentModel(ComplexType parseCt)
    {
        if (parseCt.getAll() != null)
            return parseCt.getAll();

        if (parseCt.getSequence() != null)
            return parseCt.getSequence();

        if (parseCt.getChoice() != null)
            return parseCt.getChoice();

        if (parseCt.getGroup() != null)
            return parseCt.getGroup();

        return null;
    }

    public static Group getContentModel(ComplexRestrictionType parseRest)
    {
        if (parseRest.getAll() != null)
            return parseRest.getAll();

        if (parseRest.getSequence() != null)
            return parseRest.getSequence();

        if (parseRest.getChoice() != null)
            return parseRest.getChoice();

        if (parseRest.getGroup() != null)
            return parseRest.getGroup();

        return null;

    }

    public static Group getContentModel(ExtensionType parseExt)
    {
        if (parseExt.getAll() != null)
            return parseExt.getAll();

        if (parseExt.getSequence() != null)
            return parseExt.getSequence();

        if (parseExt.getChoice() != null)
            return parseExt.getChoice();

        if (parseExt.getGroup() != null)
            return parseExt.getGroup();

        return null;

    }

    static Schema getSchema(XmlObject o)
    {
        XmlCursor c = o.newCursor();

        try
        {
            while ( c.toParent() )
            {
                o = c.getObject();

                if (o.schemaType().equals(Schema.type))
                    return (Schema) o;
            }
        }
        finally
        {
            c.dispose();
        }

        return null;
    }

    public static void resolveComplexType(SchemaTypeImpl sImpl)
    {
        ComplexType parseCt = (ComplexType)sImpl.getParseObject();
        StscState state = StscState.get();
        Schema schema = getSchema(parseCt);

        // Set abstract & final flags
        boolean abs = parseCt.isSetAbstract() ? parseCt.getAbstract() : false;
        boolean finalExt = false;
        boolean finalRest = false;
        boolean finalList = false;
        boolean finalUnion = false;

        Object ds = null;
        if (parseCt.isSetFinal())
        {
            ds = parseCt.getFinal();
        }
        // Inspect the final default attribute on the schema
        else if (schema != null && schema.isSetFinalDefault())
        {
            ds = schema.getFinalDefault();
        }

        if (ds != null)
        {
            if (ds instanceof String && ds.equals("#all"))
            {
                // #ALL value
                finalExt = finalRest = finalList = finalUnion = true;
            }
            else if (ds instanceof List)
            {
                if (((List)ds).contains("extension"))
                    finalExt = true;

                if (((List)ds).contains("restriction"))
                    finalRest = true;

// Since complex types don't participate in list and unions, these can remain
// false.  Perhaps we should throw an error.

//                if (((List)ds).contains("list"))
//                    finalList = true;
//
//                if (((List)ds).contains("union"))
//                    finalUnion = true;
            }
        }

        sImpl.setAbstractFinal(abs, finalExt, finalRest, finalList, finalUnion);

        // Set block flags
        boolean blockExt = false;
        boolean blockRest = false;
        Object block = null;

        if (parseCt.isSetBlock())
            block = parseCt.getBlock();
        else if (schema != null && schema.isSetBlockDefault())
            block = schema.getBlockDefault();

        if (block != null)
        {
            if (block instanceof String && block.equals("#all"))
            {
                // #ALL value
                blockExt = blockRest = true;
            }
            else if (block instanceof List)
            {
                if (((List)block).contains("extension"))
                    blockExt = true;
                if (((List)block).contains("restriction"))
                    blockRest = true;
            }
        }

        sImpl.setBlock(blockExt, blockRest);

        // Verify: have simpleContent, complexContent, or direct stuff
        ComplexContentDocument.ComplexContent parseCc = parseCt.getComplexContent();
        SimpleContentDocument.SimpleContent parseSc = parseCt.getSimpleContent();
        Group parseGroup = getContentModel(parseCt);
        int count =
                (parseCc != null ? 1 : 0) +
                (parseSc != null ? 1 : 0) +
                (parseGroup != null ? 1 : 0);
        if (count > 1)
        {
            // KHK: s4s should catch this?
            state.error("A complex type must define either a content model, " +
                      "or a simpleContent or complexContent derivation: " +
                      "more than one found.",
                    XmlErrorCodes.REDUNDANT_CONTENT_MODEL, parseCt);
            // recovery: treat it as the first of complexContent, simpleContent, model
            parseGroup = null;
            if (parseCc != null && parseSc != null)
                parseSc = null;
        }

        if (parseCc != null)
        {
            // KHK: s4s should catch this?
            if (parseCc.getExtension() != null && parseCc.getRestriction() != null)
                state.error("Restriction conflicts with extension", XmlErrorCodes.REDUNDANT_CONTENT_MODEL, parseCc.getRestriction());

            // Mixed can be specified in two places: the rules are that Cc wins over Ct if present
            // http://www.w3.org/TR/xmlschema-1/#c-mve
            boolean mixed = parseCc.isSetMixed() ? parseCc.getMixed() : parseCt.getMixed();

            if (parseCc.getExtension() != null)
                resolveCcExtension(sImpl, parseCc.getExtension(), mixed);
            else if (parseCc.getRestriction() != null)
                resolveCcRestriction(sImpl, parseCc.getRestriction(), mixed);
            else
            {
                // KHK: s4s should catch this?
                state.error("Missing restriction or extension", XmlErrorCodes.MISSING_RESTRICTION_OR_EXTENSION, parseCc);
                resolveErrorType(sImpl);
            }
            return;
        }
        else if (parseSc != null)
        {
            // KHK: s4s should catch this?
            if (parseSc.getExtension() != null && parseSc.getRestriction() != null)
                state.error("Restriction conflicts with extension", XmlErrorCodes.REDUNDANT_CONTENT_MODEL, parseSc.getRestriction());

            if (parseSc.getExtension() != null)
                resolveScExtension(sImpl, parseSc.getExtension());
            else if (parseSc.getRestriction() != null)
                resolveScRestriction(sImpl, parseSc.getRestriction());
            else
            {
                // KHK: s4s should catch this?
                state.error("Missing restriction or extension", XmlErrorCodes.MISSING_RESTRICTION_OR_EXTENSION, parseSc);
                resolveErrorType(sImpl);
            }
            return;
        }
        else
            resolveBasicComplexType(sImpl);
    }

    static void resolveErrorType(SchemaTypeImpl sImpl)
    {
        throw new RuntimeException("This type of error recovery not yet implemented.");
    }

    private static SchemaType.Ref[] makeRefArray(Collection typeList)
    {
        SchemaType.Ref[] result = new SchemaType.Ref[typeList.size()];
        int j = 0;
        for (Iterator i = typeList.iterator(); i.hasNext(); j++)
            result[j] = ((SchemaType)i.next()).getRef();
        return result;
    }


    static void resolveBasicComplexType(SchemaTypeImpl sImpl)
    {
        List anonymousTypes = new ArrayList();
        ComplexType parseTree = (ComplexType)sImpl.getParseObject();
        String targetNamespace = sImpl.getTargetNamespace();
        boolean chameleon = (sImpl.getChameleonNamespace() != null);
        Group parseGroup = getContentModel(parseTree);

        if (sImpl.isRedefinition())
        {
            StscState.get().error(XmlErrorCodes.SCHEMA_REDEFINE$EXTEND_OR_RESTRICT,
                new Object[] { "<complexType>" }, parseTree);
            // recovery: oh well.
        }

        int particleCode = translateParticleCode(parseGroup);

        // used to ensure consistency (doesn't become part of the result)
        Map elementModel = new LinkedHashMap();

        // build content model and anonymous types
        SchemaParticle contentModel = translateContentModel(sImpl,
            parseGroup, targetNamespace, chameleon,
            sImpl.getElemFormDefault(), sImpl.getAttFormDefault(),
            particleCode, anonymousTypes, elementModel, false, null);

        // detect the nonempty "all" case (empty <all> doesn't count - it needs to be eliminated to match XSD test cases)
        boolean isAll = contentModel != null && contentModel.getParticleType() == SchemaParticle.ALL;

        // build attr model and anonymous types
        SchemaAttributeModelImpl attrModel = new SchemaAttributeModelImpl();
        translateAttributeModel(parseTree, targetNamespace, chameleon, sImpl.getAttFormDefault(),
            anonymousTypes, sImpl, null, attrModel, null, true, null);

        // summarize wildcard information
        WildcardResult wcElt = summarizeEltWildcards(contentModel);
        WildcardResult wcAttr = summarizeAttrWildcards(attrModel);

        // build state machine and verify that content model is deterministic
        if (contentModel != null)
        {
            buildStateMachine(contentModel);
            if (!StscState.get().noUpa() && !((SchemaParticleImpl)contentModel).isDeterministic())
                StscState.get().error(XmlErrorCodes.UNIQUE_PARTICLE_ATTRIBUTION, null, parseGroup);
        }

        // build property model
        // emitDBG("Building content Model for " + sImpl);
        Map elementPropertyModel = buildContentPropertyModelByQName(contentModel, sImpl);

        // add attribute property model
        Map attributePropertyModel = buildAttributePropertyModelByQName(attrModel, sImpl);

        // figure out content type
        int complexVariety =
            parseTree.getMixed()
                ? SchemaType.MIXED_CONTENT
                : contentModel == null
                    ? SchemaType.EMPTY_CONTENT
                    : SchemaType.ELEMENT_CONTENT;

        // now fill in the actual schema type implementation
        sImpl.setBaseTypeRef(BuiltinSchemaTypeSystem.ST_ANY_TYPE.getRef());
        sImpl.setBaseDepth(BuiltinSchemaTypeSystem.ST_ANY_TYPE.getBaseDepth() + 1);
        sImpl.setDerivationType(SchemaType.DT_EXTENSION);
        sImpl.setComplexTypeVariety(complexVariety);
        sImpl.setContentModel(contentModel, attrModel, elementPropertyModel, attributePropertyModel, isAll);
        sImpl.setAnonymousTypeRefs(makeRefArray(anonymousTypes));
        sImpl.setWildcardSummary(wcElt.typedWildcards, wcElt.hasWildcards, wcAttr.typedWildcards, wcAttr.hasWildcards);
    }

    static void resolveCcRestriction(SchemaTypeImpl sImpl, ComplexRestrictionType parseTree, boolean mixed)
    {
        StscState state = StscState.get();
        String targetNamespace = sImpl.getTargetNamespace();
        boolean chameleon = (sImpl.getChameleonNamespace() != null);

        // BUGBUG: NOT YET REALLY IMPLEMENTED
        // throw new RuntimeException("Not yet implemented.");

        SchemaType baseType;
        if (parseTree.getBase() == null)
        {
            // KHK: s4s
            state.error("A complexContent must define a base type", XmlErrorCodes.MISSING_BASE, parseTree);
            baseType = null; // recovery: no inheritance.
        }
        else
        {
            if (sImpl.isRedefinition())
            {
                baseType = state.findRedefinedGlobalType(parseTree.getBase(), sImpl.getChameleonNamespace(), sImpl);
                if (baseType != null && !baseType.getName().equals(sImpl.getName()))
                {
                    state.error(XmlErrorCodes.SCHEMA_REDEFINE$SAME_TYPE,
                        new Object[] { "<complexType>",
                                       QNameHelper.pretty(baseType.getName()),
                                       QNameHelper.pretty(sImpl.getName())
                        },
                        parseTree);
                }
            }
            else
            {
                baseType = state.findGlobalType(parseTree.getBase(), sImpl.getChameleonNamespace(), targetNamespace);
            }

            if (baseType == null)
                state.notFoundError(parseTree.getBase(), SchemaType.TYPE, parseTree.xgetBase(), true);
        }

        if (baseType == null)
            baseType = BuiltinSchemaTypeSystem.ST_ANY_TYPE;

        if (baseType != null && baseType.finalRestriction())
        {
            state.error(XmlErrorCodes.COMPLEX_TYPE_RESTRICTION$FINAL,
                new Object[] { QNameHelper.pretty(baseType.getName()), QNameHelper.pretty(sImpl.getName()) },
                parseTree.xgetBase());
            // recovery: just keep going
        }

        // Recursion
        if (baseType != null)
        {
            if (!StscResolver.resolveType((SchemaTypeImpl)baseType))
                baseType = null; // circular dependency: no inheritance
        }

        List anonymousTypes = new ArrayList();
        Group parseEg = getContentModel(parseTree);

        // detect the "all" case
        int particleCode = translateParticleCode(parseEg);

        // used to ensure consistency (doesn't become part of the result)
        Map elementModel = new LinkedHashMap();

        // build content model and anonymous types
        SchemaParticle contentModel = translateContentModel( sImpl,
            parseEg, targetNamespace, chameleon,
            sImpl.getElemFormDefault(), sImpl.getAttFormDefault(),
            particleCode, anonymousTypes, elementModel, false, null);

        // detect the nonempty "all" case (empty <all> doesn't count - it needs to be eliminated to match XSD test cases)
        boolean isAll = contentModel != null && contentModel.getParticleType() == SchemaParticle.ALL;

        // build attr model and anonymous types
        SchemaAttributeModelImpl attrModel;
        if (baseType == null)
            attrModel = new SchemaAttributeModelImpl();
        else
            attrModel = new SchemaAttributeModelImpl(baseType.getAttributeModel());
        translateAttributeModel(parseTree, targetNamespace, chameleon, sImpl.getAttFormDefault(),
            anonymousTypes, sImpl, null, attrModel, baseType, false, null);

        // summarize wildcard information
        WildcardResult wcElt = summarizeEltWildcards(contentModel);
        WildcardResult wcAttr = summarizeAttrWildcards(attrModel);

        // build state machine and verify that content model is deterministic
        if (contentModel != null)
        {
            buildStateMachine(contentModel);
            if (!StscState.get().noUpa() && !((SchemaParticleImpl)contentModel).isDeterministic())
                StscState.get().error(XmlErrorCodes.UNIQUE_PARTICLE_ATTRIBUTION, null, parseEg);
        }

        // build property model
        // emitDBG("Building content Model for " + sImpl);
        Map elementPropertyModel = buildContentPropertyModelByQName(contentModel, sImpl);

        // add attribute property model
        Map attributePropertyModel = buildAttributePropertyModelByQName(attrModel, sImpl);

        // compute empty/element/mixed
        // fix for XMLBEANS-414
        int complexVariety = (mixed ? SchemaType.MIXED_CONTENT :
            (contentModel == null ? SchemaType.EMPTY_CONTENT : SchemaType.ELEMENT_CONTENT));

        // now fill in the actual schema type implementation
        sImpl.setBaseTypeRef(baseType.getRef());
        sImpl.setBaseDepth(((SchemaTypeImpl)baseType).getBaseDepth() + 1);
        sImpl.setDerivationType(SchemaType.DT_RESTRICTION);
        sImpl.setComplexTypeVariety(complexVariety);
        sImpl.setContentModel(contentModel, attrModel, elementPropertyModel, attributePropertyModel, isAll);
        sImpl.setAnonymousTypeRefs(makeRefArray(anonymousTypes));
        sImpl.setWildcardSummary(wcElt.typedWildcards, wcElt.hasWildcards, wcAttr.typedWildcards, wcAttr.hasWildcards);
    }

    static Map extractElementModel(SchemaType sType)
    {
        Map elementModel = new HashMap();
        if (sType != null)
        {
            SchemaProperty[] sProps = sType.getProperties();
            for (int i = 0; i < sProps.length; i++)
            {
                if (sProps[i].isAttribute())
                    continue;
                elementModel.put(sProps[i].getName(),
                                 sProps[i].getType());
            }
        }
        return elementModel;
    }

    static void resolveCcExtension(SchemaTypeImpl sImpl, ExtensionType parseTree, boolean mixed)
    {
        SchemaType baseType;
        StscState state = StscState.get();
        String targetNamespace = sImpl.getTargetNamespace();
        boolean chameleon = (sImpl.getChameleonNamespace() != null);

        if (parseTree.getBase() == null)
        {
            // KHK: s4s
            state.error("A complexContent must define a base type", XmlErrorCodes.MISSING_BASE, parseTree);
            baseType = null; // recovery: no inheritance.
        }
        else
        {
            if (sImpl.isRedefinition())
            {
                baseType = state.findRedefinedGlobalType(parseTree.getBase(), sImpl.getChameleonNamespace(), sImpl);
                if (baseType != null && !baseType.getName().equals(sImpl.getName()))
                {
                    state.error(XmlErrorCodes.SCHEMA_REDEFINE$SAME_TYPE,
                        new Object[] { "<complexType>",
                                       QNameHelper.pretty(baseType.getName()),
                                       QNameHelper.pretty(sImpl.getName())
                        },
                        parseTree);
                }
            }
            else
            {
                baseType = state.findGlobalType(parseTree.getBase(), sImpl.getChameleonNamespace(), targetNamespace);
            }
            if (baseType == null)
                state.notFoundError(parseTree.getBase(), SchemaType.TYPE, parseTree.xgetBase(), true);
        }

        // Recursion
        if (baseType != null)
        {
            if (!StscResolver.resolveType((SchemaTypeImpl)baseType))
                baseType = null; // circular dependency: no inheritance
        }

        if (baseType != null && baseType.isSimpleType())
        {
            state.recover(XmlErrorCodes.SCHEMA_COMPLEX_TYPE$COMPLEX_CONTENT,
                new Object[] { QNameHelper.pretty(baseType.getName()) },
                parseTree.xgetBase());
            baseType = null; // recovery: no inheritance.
        }

        if (baseType != null && baseType.finalExtension())
        {
            state.error(XmlErrorCodes.COMPLEX_TYPE_EXTENSION$FINAL,
                new Object[] { QNameHelper.pretty(baseType.getName()), QNameHelper.pretty(sImpl.getName()) },
                parseTree.xgetBase());
            // recovery: just keep going
        }

        // get base content model
        SchemaParticle baseContentModel = (baseType == null ? null : baseType.getContentModel());
        // TODO: attribute model also

        List anonymousTypes = new ArrayList();
        Map baseElementModel = extractElementModel(baseType);
        Group parseEg = getContentModel(parseTree);

        if (baseType != null &&
            (baseType.getContentType() == SchemaType.SIMPLE_CONTENT))
        if (parseEg != null)
        {
            // if this type has complexContent, baseType is complexType
            // but with non-empty simpleContent then this type cannot
            // add extra elements
            state.recover(XmlErrorCodes.COMPLEX_TYPE_EXTENSION$EXTENDING_SIMPLE_CONTENT,
                new Object[] { QNameHelper.pretty(baseType.getName()) },
                parseTree.xgetBase());
            baseType = null; // recovery: no inheritance.
        }
        else
        {
            // No extra elements, the type is a complex type with simple content
            resolveScExtensionPart2(sImpl, baseType, parseTree, targetNamespace, chameleon);
            return;
        }

        // build extension model
        SchemaParticle extensionModel = translateContentModel(sImpl,
            parseEg, targetNamespace, chameleon,
            sImpl.getElemFormDefault(), sImpl.getAttFormDefault(),
            translateParticleCode(parseEg), anonymousTypes, baseElementModel, false, null);

        // apply rule #2 near http://www.w3.org/TR/xmlschema-1/#c-mve: empty ext model -> mixed taken from base
        if (extensionModel == null && !mixed)
            mixed = (baseType != null && baseType.getContentType() == SchemaType.MIXED_CONTENT);

        // apply Derivation Valid (Extension) rule 1.4.2.2
        if (baseType != null && (baseType.getContentType() != SchemaType.EMPTY_CONTENT) &&
                ((baseType.getContentType() == SchemaType.MIXED_CONTENT) != mixed))
        {
            state.error(XmlErrorCodes.COMPLEX_TYPE_EXTENSION$BOTH_ELEMEMENT_OR_MIXED, null, parseTree.xgetBase());
            // recovery: just keep going
        }

        // detect the "all" base case
        if (baseType != null && baseType.hasAllContent() && extensionModel != null)
        {
            // KHK: which rule? cos-particle-extend.2 or cos-all-limited.1.2.  I think the limited one.
            state.error("Cannot extend a type with 'all' content model", XmlErrorCodes.CANNOT_EXTEND_ALL, parseTree.xgetBase());
            extensionModel = null; // recovery: drop extension
        }

        // build content model and anonymous types
        SchemaParticle contentModel = extendContentModel(baseContentModel, extensionModel, parseTree);

        // detect the nonempty "all" case (empty <all> doesn't count - it needs to be eliminated to match XSD test cases)
        boolean isAll = contentModel != null && contentModel.getParticleType() == SchemaParticle.ALL;

        // build attr model and anonymous types
        SchemaAttributeModelImpl attrModel;
        if (baseType == null)
            attrModel = new SchemaAttributeModelImpl();
        else
            attrModel = new SchemaAttributeModelImpl(baseType.getAttributeModel());
        translateAttributeModel(parseTree, targetNamespace, chameleon, sImpl.getAttFormDefault(),
            anonymousTypes, sImpl, null, attrModel, baseType, true, null);

        // summarize wildcard information
        WildcardResult wcElt = summarizeEltWildcards(contentModel);
        WildcardResult wcAttr = summarizeAttrWildcards(attrModel);

        // build state machine and verify that content model is deterministic
        if (contentModel != null)
        {
            buildStateMachine(contentModel);
            if (!StscState.get().noUpa() && !((SchemaParticleImpl)contentModel).isDeterministic())
                StscState.get().error(XmlErrorCodes.UNIQUE_PARTICLE_ATTRIBUTION, null, parseEg);
        }

        // build property model
        // emitDBG("Building content Model for " + sImpl);
        Map elementPropertyModel = buildContentPropertyModelByQName(contentModel, sImpl);

        // add attribute property model
        Map attributePropertyModel = buildAttributePropertyModelByQName(attrModel, sImpl);

        // compute empty/element/mixed
        int complexVariety;
        if (contentModel == null && baseType != null &&
            baseType.getContentType() == SchemaType.SIMPLE_CONTENT)
        {
            complexVariety = SchemaType.SIMPLE_CONTENT;
            sImpl.setContentBasedOnTypeRef(baseType.getContentBasedOnType().getRef());
        }
        else
            complexVariety = ( mixed ? SchemaType.MIXED_CONTENT :
            (contentModel == null ? SchemaType.EMPTY_CONTENT : SchemaType.ELEMENT_CONTENT));

        // now fill in the actual schema type implementation
        if (baseType == null)
            baseType = XmlObject.type;
        sImpl.setBaseTypeRef(baseType.getRef());
        sImpl.setBaseDepth(((SchemaTypeImpl)baseType).getBaseDepth() + 1);
        sImpl.setDerivationType(SchemaType.DT_EXTENSION);
        sImpl.setComplexTypeVariety(complexVariety);
        sImpl.setContentModel(contentModel, attrModel, elementPropertyModel, attributePropertyModel, isAll);
        sImpl.setAnonymousTypeRefs(makeRefArray(anonymousTypes));
        sImpl.setWildcardSummary(wcElt.typedWildcards, wcElt.hasWildcards, wcAttr.typedWildcards, wcAttr.hasWildcards);
    }

    static void resolveScRestriction(SchemaTypeImpl sImpl, SimpleRestrictionType parseTree)
    {
        SchemaType baseType;
        SchemaType contentType = null;
        StscState state = StscState.get();
        String targetNamespace = sImpl.getTargetNamespace();
        boolean chameleon = (sImpl.getChameleonNamespace() != null);
        List anonymousTypes = new ArrayList();
        if (parseTree.getSimpleType() != null)
        {
            LocalSimpleType typedef = parseTree.getSimpleType();
            SchemaTypeImpl anonType = StscTranslator.
                translateAnonymousSimpleType(typedef, targetNamespace, chameleon,
                    sImpl.getElemFormDefault(), sImpl.getAttFormDefault(),
                    anonymousTypes, sImpl);
            contentType = anonType;
        }
        if (parseTree.getBase() == null)
        {
            state.error("A simpleContent restriction must define a base type", XmlErrorCodes.MISSING_BASE, parseTree);
            // recovery: extends ANY_SIMPLE type
            baseType = BuiltinSchemaTypeSystem.ST_ANY_SIMPLE;
        }
        else
        {
            if (sImpl.isRedefinition())
            {
                baseType = state.findRedefinedGlobalType(parseTree.getBase(), sImpl.getChameleonNamespace(), sImpl);
                if (baseType != null && !baseType.getName().equals(sImpl.getName()))
                {
                    state.error(XmlErrorCodes.SCHEMA_REDEFINE$SAME_TYPE,
                        new Object[] { "<simpleType>",
                                       QNameHelper.pretty(baseType.getName()),
                                       QNameHelper.pretty(sImpl.getName())
                        },
                        parseTree);
                }
            }
            else
            {
                baseType = state.findGlobalType(parseTree.getBase(), sImpl.getChameleonNamespace(), targetNamespace);
            }
            if (baseType == null)
            {
                state.notFoundError(parseTree.getBase(), SchemaType.TYPE, parseTree.xgetBase(), true);
                // recovery: extends ANY_SIMPLE type
                baseType = BuiltinSchemaTypeSystem.ST_ANY_SIMPLE;
            }
        }

        // Recursion
        StscResolver.resolveType((SchemaTypeImpl)baseType);
        if (contentType != null)
            StscResolver.resolveType((SchemaTypeImpl)contentType);
        else
            contentType = baseType;

        if (baseType.isSimpleType())
        {
            // src-ct.2: complex types with simple content cannot restrict simple types
            state.recover(XmlErrorCodes.COMPLEX_TYPE_PROPERTIES$SIMPLE_TYPE_EXTENSION,
                new Object[] { QNameHelper.pretty(baseType.getName()) },
                parseTree);
            // recovery: extends ANY_SIMPLE type
            baseType = BuiltinSchemaTypeSystem.ST_ANY_SIMPLE;
        }
        else if (baseType.getContentType() != SchemaType.SIMPLE_CONTENT &&
                 contentType == null)
        {
            // recovery: extends ANY_SIMPLE type
            baseType = BuiltinSchemaTypeSystem.ST_ANY_SIMPLE;
        }

        if (baseType != null && baseType.finalRestriction())
        {
            state.error(XmlErrorCodes.COMPLEX_TYPE_RESTRICTION$FINAL,
                    new Object[] { QNameHelper.pretty(baseType.getName()), QNameHelper.pretty(sImpl.getName()) },
                    parseTree.xgetBase());
            // recovery: just keep going
        }

        // build attr model and anonymous types
        SchemaAttributeModelImpl attrModel;
        if (baseType == null)
            attrModel = new SchemaAttributeModelImpl();
        else
            attrModel = new SchemaAttributeModelImpl(baseType.getAttributeModel());
        translateAttributeModel(parseTree, targetNamespace, chameleon, sImpl.getAttFormDefault(),
            anonymousTypes, sImpl, null, attrModel, baseType, false, null);

        // summarize wildcard information
        WildcardResult wcAttr = summarizeAttrWildcards(attrModel);

        // add attribute property model
        Map attributePropertyModel = buildAttributePropertyModelByQName(attrModel, sImpl);

        // now fill in the actual schema type implementation
        sImpl.setBaseTypeRef(baseType.getRef());
        sImpl.setBaseDepth(((SchemaTypeImpl)baseType).getBaseDepth() + 1);
        sImpl.setContentBasedOnTypeRef(contentType.getRef());
        sImpl.setDerivationType(SchemaType.DT_RESTRICTION);
        sImpl.setAnonymousTypeRefs(makeRefArray(anonymousTypes));
        sImpl.setWildcardSummary(QNameSet.EMPTY, false, wcAttr.typedWildcards, wcAttr.hasWildcards);
        sImpl.setComplexTypeVariety(SchemaType.SIMPLE_CONTENT);
        sImpl.setContentModel(null, attrModel, null, attributePropertyModel, false);
        sImpl.setSimpleTypeVariety(contentType.getSimpleVariety());
        sImpl.setPrimitiveTypeRef(contentType.getPrimitiveType() == null ? null : contentType.getPrimitiveType().getRef());
        switch (sImpl.getSimpleVariety())
        {
            case SchemaType.LIST:
                sImpl.setListItemTypeRef(contentType.getListItemType().getRef());
                break;

            case SchemaType.UNION:
                sImpl.setUnionMemberTypeRefs(makeRefArray(Arrays.asList(contentType.getUnionMemberTypes())));
                break;
        }

        // deal with facets
        StscSimpleTypeResolver.resolveFacets(sImpl, parseTree, (SchemaTypeImpl) contentType);

        // now compute our intrinsic properties
        StscSimpleTypeResolver.resolveFundamentalFacets(sImpl);
    }

    static void resolveScExtension(SchemaTypeImpl sImpl, SimpleExtensionType parseTree)
    {
        SchemaType baseType;
        StscState state = StscState.get();
        String targetNamespace = sImpl.getTargetNamespace();
        boolean chameleon = (sImpl.getChameleonNamespace() != null);
        if (parseTree.getBase() == null)
        {
            state.error("A simpleContent extension must define a base type", XmlErrorCodes.MISSING_BASE, parseTree);
            // recovery: extends ANY_SIMPLE type
            baseType = BuiltinSchemaTypeSystem.ST_ANY_SIMPLE;
        }
        else
        {
            if (sImpl.isRedefinition())
            {
                baseType = state.findRedefinedGlobalType(parseTree.getBase(), sImpl.getChameleonNamespace(), sImpl);
                if (baseType != null && !baseType.getName().equals(sImpl.getName()))
                {
                    state.error(XmlErrorCodes.SCHEMA_REDEFINE$SAME_TYPE,
                        new Object[] { "<simpleType>",
                                       QNameHelper.pretty(baseType.getName()),
                                       QNameHelper.pretty(sImpl.getName())
                        },
                        parseTree);
                }
            }
            else
            {
                baseType = state.findGlobalType(parseTree.getBase(), sImpl.getChameleonNamespace(), targetNamespace);
            }
            if (baseType == null)
            {
                state.notFoundError(parseTree.getBase(), SchemaType.TYPE, parseTree.xgetBase(), true);
                // recovery: extends ANY_SIMPLE type
                baseType = BuiltinSchemaTypeSystem.ST_ANY_SIMPLE;
            }
        }

        // Recursion
        StscResolver.resolveType((SchemaTypeImpl)baseType);

        if (!baseType.isSimpleType() && baseType.getContentType() != SchemaType.SIMPLE_CONTENT)
        {
            // src-ct.2: complex types with simple content can only extend simple types
            state.error(XmlErrorCodes.SCHEMA_COMPLEX_TYPE$SIMPLE_CONTENT,
                new Object[] { QNameHelper.pretty(baseType.getName()) } ,
                parseTree);
            // recovery: extends ANY_SIMPLE type
            baseType = BuiltinSchemaTypeSystem.ST_ANY_SIMPLE;
        }

        if (baseType != null && baseType.finalExtension())
        {
            state.error(XmlErrorCodes.COMPLEX_TYPE_EXTENSION$FINAL,
                    new Object[] { QNameHelper.pretty(baseType.getName()), QNameHelper.pretty(sImpl.getName()) },
                    parseTree.xgetBase());
            // recovery: just keep going
        }

        resolveScExtensionPart2(sImpl, baseType, parseTree, targetNamespace, chameleon);
    }

    static void resolveScExtensionPart2(SchemaTypeImpl sImpl, SchemaType baseType, ExtensionType parseTree,
        String targetNamespace, boolean chameleon)
    {
        // build attr model and anonymous types
        List anonymousTypes = new ArrayList();
        SchemaAttributeModelImpl attrModel;
        attrModel = new SchemaAttributeModelImpl(baseType.getAttributeModel());
        translateAttributeModel(parseTree, targetNamespace, chameleon, sImpl.getAttFormDefault(), anonymousTypes, sImpl, null, attrModel, baseType, true, null);

        // summarize wildcard information
        WildcardResult wcAttr = summarizeAttrWildcards(attrModel);

        // add attribute property model
        Map attributePropertyModel = buildAttributePropertyModelByQName(attrModel, sImpl);

        // now fill in the actual schema type implementation
        sImpl.setBaseTypeRef(baseType.getRef());
        sImpl.setBaseDepth(((SchemaTypeImpl)baseType).getBaseDepth() + 1);
        sImpl.setContentBasedOnTypeRef(baseType.getRef());
        sImpl.setDerivationType(SchemaType.DT_EXTENSION);
        sImpl.setAnonymousTypeRefs(makeRefArray(anonymousTypes));
        sImpl.setWildcardSummary(QNameSet.EMPTY, false, wcAttr.typedWildcards, wcAttr.hasWildcards);
        sImpl.setComplexTypeVariety(SchemaType.SIMPLE_CONTENT);
        sImpl.setContentModel(null, attrModel, null, attributePropertyModel, false);
        sImpl.setSimpleTypeVariety(baseType.getSimpleVariety());
        sImpl.setPrimitiveTypeRef(baseType.getPrimitiveType() == null ? null : baseType.getPrimitiveType().getRef());
        switch (sImpl.getSimpleVariety())
        {
            case SchemaType.LIST:
                sImpl.setListItemTypeRef(baseType.getListItemType().getRef());
                break;

            case SchemaType.UNION:
                sImpl.setUnionMemberTypeRefs(makeRefArray(Arrays.asList(baseType.getUnionMemberTypes())));
                break;
        }

        // deal with facets
        StscSimpleTypeResolver.resolveFacets(sImpl, null, (SchemaTypeImpl)baseType);

        // now compute our intrinsic properties
        StscSimpleTypeResolver.resolveFundamentalFacets(sImpl);
    }

    static class WildcardResult
    {
        WildcardResult(QNameSet typedWildcards, boolean hasWildcards)
        {
            this.typedWildcards = typedWildcards;
            this.hasWildcards = hasWildcards;
        }
        QNameSet typedWildcards;
        boolean hasWildcards;
    }

    static WildcardResult summarizeAttrWildcards(SchemaAttributeModel attrModel)
    {
        if (attrModel.getWildcardProcess() == SchemaAttributeModel.NONE)
            return new WildcardResult(QNameSet.EMPTY, false);
        if (attrModel.getWildcardProcess() == SchemaAttributeModel.SKIP)
            return new WildcardResult(QNameSet.EMPTY, true);
        return new WildcardResult(attrModel.getWildcardSet(), true);
    }

    static WildcardResult summarizeEltWildcards(SchemaParticle contentModel)
    {
        if (contentModel == null)
        {
            return new WildcardResult(QNameSet.EMPTY, false);
        }

        switch (contentModel.getParticleType())
        {
            case SchemaParticle.ALL:
            case SchemaParticle.SEQUENCE:
            case SchemaParticle.CHOICE:
                QNameSetBuilder set = new QNameSetBuilder();
                boolean hasWildcards = false;
                for (int i = 0; i < contentModel.countOfParticleChild(); i++)
                {
                    WildcardResult inner = summarizeEltWildcards(contentModel.getParticleChild(i));
                    set.addAll(inner.typedWildcards);
                    hasWildcards |= inner.hasWildcards;
                }
                return new WildcardResult(set.toQNameSet(), hasWildcards);
            case SchemaParticle.WILDCARD:
                return new WildcardResult(
                    (contentModel.getWildcardProcess() == SchemaParticle.SKIP) ?
                    QNameSet.EMPTY : contentModel.getWildcardSet(), true);
                // otherwise fallthrough

            default:
                return new WildcardResult(QNameSet.EMPTY, false);
        }
    }

    static void translateAttributeModel(XmlObject parseTree,
            String targetNamespace, boolean chameleon, String formDefault,
            List anonymousTypes, SchemaType outerType,
            Set seenAttributes, SchemaAttributeModelImpl result,
            SchemaType baseType, boolean extension,
            SchemaAttributeGroupImpl redefinitionFor)
    {
        StscState state = StscState.get();
        if (seenAttributes == null)
            seenAttributes = new HashSet();
        boolean seenWildcard = false;
        boolean seenRedefinition = false;
        SchemaAttributeModel baseModel = null;
        if (baseType != null)
            baseModel = baseType.getAttributeModel();

        XmlCursor cur = parseTree.newCursor();

        for (boolean more = cur.toFirstChild(); more; more = cur.toNextSibling())
        {
            switch (translateAttributeCode(cur.getName()))
            {
                case ATTRIBUTE_CODE:
                {
                    Attribute xsdattr = (Attribute)cur.getObject();

                    SchemaLocalAttribute sAttr = StscTranslator.translateAttribute(xsdattr, targetNamespace, formDefault, chameleon, anonymousTypes, outerType, baseModel, true);
                    if (sAttr == null)
                        continue;

                    if (seenAttributes.contains(sAttr.getName()))
                    {
                        state.error(XmlErrorCodes.COMPLEX_TYPE_PROPERTIES$DUPLICATE_ATTRIBUTE,
                            new Object[] { QNameHelper.pretty(sAttr.getName()), QNameHelper.pretty(outerType.getName()) },
                            xsdattr.xgetName());
                        continue; // ignore the duplicate attr
                    }

                    seenAttributes.add(sAttr.getName());

                    if (baseModel != null)
                    {
                        SchemaLocalAttribute baseAttr = baseModel.getAttribute(sAttr.getName());
                        if (baseAttr == null)
                        {
                            if (!extension)
                            {
                                if (!baseModel.getWildcardSet().contains(sAttr.getName()))
                                    state.error(XmlErrorCodes.COMPLEX_TYPE_RESTRICTION$ATTR_IN_BASE_WILDCARD_SET,
                                        new Object[] { QNameHelper.pretty(sAttr.getName()), QNameHelper.pretty(outerType.getName()) }, xsdattr);
                            }
                        }
                        else
                        {
                            if (extension)
                            {
                                // KHK: cos-ct-extends.1.2?
                                if (sAttr.getUse() == SchemaLocalAttribute.PROHIBITED)
                                    state.error("An extension cannot prohibit an attribute from the base type; use restriction instead.", XmlErrorCodes.DUPLICATE_ATTRIBUTE_NAME, xsdattr.xgetUse());
                            }
                            else
                            {
                                if (sAttr.getUse() != SchemaLocalAttribute.REQUIRED)
                                {
                                    if (baseAttr.getUse() == SchemaLocalAttribute.REQUIRED)
                                        state.error(XmlErrorCodes.COMPLEX_TYPE_RESTRICTION$ATTR_REQUIRED,
                                            new Object[] { QNameHelper.pretty(sAttr.getName()), QNameHelper.pretty(outerType.getName()) }, xsdattr);

                                    if (sAttr.getUse() == SchemaLocalAttribute.PROHIBITED)
                                        result.removeProhibitedAttribute(sAttr.getName());
                                }
                            }
                        }
                    }

                    if (sAttr.getUse() != SchemaLocalAttribute.PROHIBITED)
                    {
                        result.addAttribute(sAttr);
                    }
                    else
                    {
                        // attribute is prohibited. If it has an anonymous type remove
                        // it from the list (this will prevent inclusion of any anonymous
                        // types defined within the prohibited attribute which would
                        // otherwise attempt to refer to the prohibited attribute at
                        // save() time)
                        SchemaType attrType = sAttr.getType();
                        if (anonymousTypes != null && anonymousTypes.contains(attrType))
                        {
                            anonymousTypes.remove(attrType);
                        }
                    }

                    if (sAttr.getDefaultText() != null && !sAttr.isFixed())
                    {
                        if (sAttr.getUse() != SchemaLocalAttribute.OPTIONAL)
                            state.error(XmlErrorCodes.SCHEMA_ATTR$DEFAULT_AND_USE_OPTIONAL,
                                new Object[] { QNameHelper.pretty(sAttr.getName()) }, xsdattr);
                    }


                    break;
                }
                case ANY_ATTRIBUTE_CODE:
                {
                    Wildcard xsdwc = (Wildcard)cur.getObject();
                    if (seenWildcard)
                    {
                        // KHK: ?
                        state.error("Only one attribute wildcard allowed", XmlErrorCodes.DUPLICATE_ANY_ATTRIBUTE, xsdwc);
                        continue; // ignore the extra wildcard
                    }
                    seenWildcard = true;
                    NamespaceList nsList = xsdwc.xgetNamespace();
                    String nsText;
                    if (nsList == null)
                        nsText = "##any";
                    else
                        nsText = nsList.getStringValue();
                    QNameSet wcset = QNameSet.forWildcardNamespaceString(nsText, targetNamespace);

                    if (baseModel != null && !extension)
                    {
                        if (baseModel.getWildcardSet() == null)
                        {
                            state.error(XmlErrorCodes.COMPLEX_TYPE_RESTRICTION$BASE_HAS_ATTR_WILDCARD, null, xsdwc);
                            continue; // ignore the extra wildcard
                        }
                        else if (!baseModel.getWildcardSet().containsAll(wcset))
                        {
                            state.error(XmlErrorCodes.COMPLEX_TYPE_RESTRICTION$ATTR_WILDCARD_SUBSET,
                                new Object[] { nsText }, xsdwc);
                            continue; // ignore the restriction
                        }
                    }

                    int wcprocess = translateWildcardProcess(xsdwc.xgetProcessContents());
                    if (result.getWildcardProcess() == SchemaAttributeModel.NONE)
                    {
                        result.setWildcardSet(wcset);
                        result.setWildcardProcess(wcprocess);
                    }
                    else
                    {
                        if (extension)
                        {
                            result.setWildcardSet(wcset.union(result.getWildcardSet()));
                            result.setWildcardProcess(wcprocess);
                        }
                        else
                        {
                            result.setWildcardSet(wcset.intersect(result.getWildcardSet()));
                            // keep old process
                        }
                    }
                    break;
                }
                case ATTRIBUTE_GROUP_CODE:
                {
                    AttributeGroupRef xsdag = (AttributeGroupRef)cur.getObject();
                    QName ref = xsdag.getRef();
                    if (ref == null)
                    {
                        // KHK: s4s
                        state.error("Attribute group reference must have a ref attribute", XmlErrorCodes.ATTRIBUTE_GROUP_MISSING_REF, xsdag);
                        continue;
                    }
                    SchemaAttributeGroupImpl group;
                    if (redefinitionFor != null)
                    {
                        group = state.findRedefinedAttributeGroup(ref, chameleon ? targetNamespace : null, redefinitionFor);
                        if (group != null &&
                            redefinitionFor.getName().equals(group.getName()))
                        {
                            if (seenRedefinition)
                                state.error(XmlErrorCodes.SCHEMA_REDEFINE$ATTR_GROUP_SELF_REF,
                                    new Object[] { QNameHelper.pretty(redefinitionFor.getName()) }, xsdag);
                            seenRedefinition = true;
                        }
                    }
                    else
                    {
                        group = state.findAttributeGroup(ref, chameleon ? targetNamespace : null, targetNamespace);
                    }
                    if (group == null)
                    {
                        state.notFoundError(ref, SchemaType.ATTRIBUTE_GROUP, xsdag.xgetRef(), true);
                        continue;
                    }
                    if (state.isProcessing(group))
                    {
                        state.error(XmlErrorCodes.SCHEMA_ATTR_GROUP$SELF_REF,
                            new Object[] { QNameHelper.pretty(group.getName()) },group.getParseObject());
                        continue;
                    }
                    String subTargetNamespace = targetNamespace;
                    if (group.getTargetNamespace() != null)
                    {
                        subTargetNamespace = group.getTargetNamespace();
                        chameleon = group.getChameleonNamespace() != null;
                    }

                    state.startProcessing(group);
                    SchemaAttributeGroupImpl nestedRedefinitionFor = null;
                    if (group.isRedefinition())
                        nestedRedefinitionFor = group;
                    translateAttributeModel(group.getParseObject(), subTargetNamespace, chameleon,
                        group.getFormDefault(),
                        anonymousTypes, outerType, seenAttributes, result, baseType,
                        extension, nestedRedefinitionFor);
                    state.finishProcessing(group);
                    break;
                }
                default:
                {
                    continue; // skip things that are not part of the attribute model.
                }
            }
        }
        // If this is restriction and no wildcard was present, then
        // we have to erase the inherited wildcards
        if (!extension && !seenWildcard)
        {
            result.setWildcardSet(null);
            result.setWildcardProcess(SchemaAttributeModel.NONE);
        }
    }

    static SchemaParticle extendContentModel(SchemaParticle baseContentModel, SchemaParticle extendedContentModel, XmlObject parseTree)
    {
        // http://www.w3.org/TR/xmlschema-1/#element-complexContent::extension

        // 2.1 If the explicit content is empty, then the {content type} of the type definition resolved to by the �actual value� of the base [attribute]
        if (extendedContentModel == null)
            return baseContentModel;

        // 2.2 If the type definition resolved to by the actual value of the base [attribute] has a {content type} of empty, then a pair of mixed or elementOnly (determined as per clause 1.2.1 above) and the explicit content itself;
        if (baseContentModel == null)
            return extendedContentModel;

        // 2.3 otherwise a pair of mixed or elementOnly (determined as per clause 1.2.1 above) and a particle whose properties are as follows:
        SchemaParticleImpl sPart = new SchemaParticleImpl();
        sPart.setParticleType(SchemaParticle.SEQUENCE);

        List accumulate = new ArrayList();
        addMinusPointlessParticles(accumulate, baseContentModel, SchemaParticle.SEQUENCE);
        addMinusPointlessParticles(accumulate, extendedContentModel, SchemaParticle.SEQUENCE);
        sPart.setMinOccurs(BigInteger.ONE);
        sPart.setMaxOccurs(BigInteger.ONE);
        sPart.setParticleChildren((SchemaParticle[])
                accumulate.toArray(new SchemaParticle[accumulate.size()]));

        return filterPointlessParticlesAndVerifyAllParticles(sPart, parseTree);
    }

    static BigInteger extractMinOccurs(XmlNonNegativeInteger nni)
    {
        if (nni == null)
            return BigInteger.ONE;
        BigInteger result = nni.getBigIntegerValue();
        if (result == null)
            return BigInteger.ONE;
        return result;
    }

    static BigInteger extractMaxOccurs(AllNNI allNNI)
    {
        if (allNNI == null)
            return BigInteger.ONE;

        if (allNNI.instanceType().getPrimitiveType().getBuiltinTypeCode() == SchemaType.BTC_DECIMAL)
            return ((XmlInteger)allNNI).getBigIntegerValue();
        else
            return null;
    }

    private static class RedefinitionForGroup
    {
        private SchemaModelGroupImpl group;
        private boolean seenRedefinition = false;

        public RedefinitionForGroup(SchemaModelGroupImpl group)
        {
            this.group = group;
        }

        public SchemaModelGroupImpl getGroup()
        {
            return group;
        }

        public boolean isSeenRedefinition()
        {
            return seenRedefinition;
        }

        public void setSeenRedefinition(boolean seenRedefinition)
        {
            this.seenRedefinition = seenRedefinition;
        }
    }

    static SchemaParticle translateContentModel(
            SchemaType outerType,
            XmlObject parseTree, String targetNamespace, boolean chameleon,
            String elemFormDefault, String attFormDefault,
            int particleCode, List anonymousTypes, Map elementModel,
            boolean allowElt, RedefinitionForGroup redefinitionFor)
    {
        if (parseTree == null || particleCode == 0)
            return null;

        StscState state = StscState.get();

        // emitDBG("Translating content model for " + outerType);
        // indentDBG();
        assert(particleCode != 0);

        boolean hasChildren = false;
        BigInteger minOccurs;
        BigInteger maxOccurs;
        SchemaModelGroupImpl group = null;

        SchemaParticleImpl sPart;

        if (particleCode == SchemaParticle.ELEMENT)
        {
            if (!allowElt)
                state.error("Must be a sequence, choice or all here", XmlErrorCodes.EXPLICIT_GROUP_NEEDED, parseTree);

            // TODO: detect substitution group for this element and construct a choice

            LocalElement parseElt = (LocalElement)parseTree;
            sPart = StscTranslator.translateElement(parseElt, targetNamespace, chameleon,
                elemFormDefault, attFormDefault, anonymousTypes, outerType);
            if (sPart == null)
                return null;
            minOccurs = extractMinOccurs(parseElt.xgetMinOccurs());
            maxOccurs = extractMaxOccurs(parseElt.xgetMaxOccurs());

            SchemaType oldType = (SchemaType)elementModel.get(sPart.getName());
            if (oldType == null)
            {
                elementModel.put(sPart.getName(), sPart.getType());
            }
            else if (!sPart.getType().equals(oldType))
            {
                state.error(XmlErrorCodes.ELEM_CONSISTANT, new Object[] { QNameHelper.pretty(sPart.getName()) }, parseTree);
                return null;
            }
        }
        else if (particleCode == SchemaParticle.WILDCARD)
        {
            if (!allowElt)
                state.error("Must be a sequence, choice or all here", XmlErrorCodes.EXPLICIT_GROUP_NEEDED, parseTree);
            Any parseAny = (Any)parseTree;
            sPart = new SchemaParticleImpl();
            sPart.setParticleType(SchemaParticle.WILDCARD);
            QNameSet wcset;
            NamespaceList nslist = parseAny.xgetNamespace();
            if (nslist == null)
                wcset = QNameSet.ALL;
            else
                wcset = QNameSet.forWildcardNamespaceString(nslist.getStringValue(), targetNamespace);
            sPart.setWildcardSet(wcset);
            sPart.setWildcardProcess(translateWildcardProcess(parseAny.xgetProcessContents()));
            minOccurs = extractMinOccurs(parseAny.xgetMinOccurs());
            maxOccurs = extractMaxOccurs(parseAny.xgetMaxOccurs());
        }
        else
        {
            Group parseGroup = (Group)parseTree;
            sPart = new SchemaParticleImpl();

            // grab min/maxOccurs before dereferencign group ref
            minOccurs = extractMinOccurs(parseGroup.xgetMinOccurs());
            maxOccurs = extractMaxOccurs(parseGroup.xgetMaxOccurs());

            if (particleCode == MODEL_GROUP_CODE)
            {
                QName ref = parseGroup.getRef();
                if (ref == null)
                {
                    // KHK: s4s
                    state.error("Group reference must have a ref attribute", XmlErrorCodes.GROUP_MISSING_REF, parseTree);
                    return null;
                }

                if (redefinitionFor != null)
                {
                    group = state.findRedefinedModelGroup(ref, chameleon ? targetNamespace : null, redefinitionFor.getGroup());
                    if (group != null && group.getName().equals(redefinitionFor.getGroup().getName()))
                    {
                        if (redefinitionFor.isSeenRedefinition())
                            state.error(XmlErrorCodes.SCHEMA_REDEFINE$GROUP_SELF_REF,
                                new Object[] { QNameHelper.pretty(group.getName()) }, parseTree);
                        if (!BigInteger.ONE.equals(maxOccurs) || !BigInteger.ONE.equals(minOccurs))
                            state.error(XmlErrorCodes.SCHEMA_REDEFINE$GROUP_SELF_REF_MIN_MAX_1,
                                new Object[] { QNameHelper.pretty(group.getName()) }, parseTree);
                        redefinitionFor.setSeenRedefinition(true);
                    }
                }
                else
                {
                    group = state.findModelGroup(ref, chameleon ? targetNamespace : null, targetNamespace);
                }
                if (group == null)
                {
                    state.notFoundError(ref, SchemaType.MODEL_GROUP, ((Group)parseTree).xgetRef(), true);
                    return null;
                }
                if (state.isProcessing(group))
                {
                    state.error(XmlErrorCodes.MODEL_GROUP_PROPERTIES$CIRCULAR,
                        new Object[] { QNameHelper.pretty(group.getName()) }, group.getParseObject());
                    return null;
                }

                // no go to the child.
                XmlCursor cur = group.getParseObject().newCursor();
                for (boolean more = cur.toFirstChild(); more; more = cur.toNextSibling())
                {
                    particleCode = translateParticleCode(cur.getName());
                    if (particleCode != 0)
                    {
                        parseTree = parseGroup = (Group)cur.getObject();
                        break;
                    }
                }
                if (particleCode == 0)
                {
                    // KHK: s4s
                    state.error("Model group " + QNameHelper.pretty(group.getName()) + " is empty", XmlErrorCodes.EXPLICIT_GROUP_NEEDED, group.getParseObject());
                    return null;
                }
                if (particleCode != SchemaParticle.ALL && particleCode != SchemaParticle.SEQUENCE && particleCode != SchemaParticle.CHOICE)
                {
                    // KHK: s4s
                    state.error("Model group " + QNameHelper.pretty(group.getName()) + " is not a sequence, all, or choice", XmlErrorCodes.EXPLICIT_GROUP_NEEDED, group.getParseObject());
                }

                String newTargetNamespace = group.getTargetNamespace();
                if (newTargetNamespace != null)
                    targetNamespace = newTargetNamespace;
                elemFormDefault = group.getElemFormDefault();
                attFormDefault = group.getAttFormDefault();
                chameleon = group.getChameleonNamespace() != null;
            }

            switch (particleCode)
            {
                case SchemaParticle.ALL:
                case SchemaParticle.SEQUENCE:
                case SchemaParticle.CHOICE:
                    sPart.setParticleType(particleCode);
                    hasChildren = true;
                    break;

                default:
                    assert(false);
                    throw new IllegalStateException();
            }
        }

        if (maxOccurs != null && minOccurs.compareTo(maxOccurs) > 0)
        {
            state.error(XmlErrorCodes.PARTICLE_PROPERTIES$MIN_LTE_MAX, null, parseTree);
            maxOccurs = minOccurs; // remedy: pin max up to min
        }

        if (maxOccurs != null && maxOccurs.compareTo(BigInteger.ONE) < 0)
        {
            state.warning(XmlErrorCodes.PARTICLE_PROPERTIES$MAX_GTE_1, null, parseTree);

            // remove from the list of anonymous types if it was added
            anonymousTypes.remove(sPart.getType());
            return null; // maxOccurs == minOccurs == 0, same as no particle at all.
        }

        sPart.setMinOccurs(minOccurs);
        sPart.setMaxOccurs(maxOccurs);

        if (group != null)
        {
            state.startProcessing(group);
            redefinitionFor = null;
            if (group.isRedefinition())
                redefinitionFor = new RedefinitionForGroup(group);
        }

        if (hasChildren)
        {
            XmlCursor cur = parseTree.newCursor();
            List accumulate = new ArrayList();
            for (boolean more = cur.toFirstChild(); more; more = cur.toNextSibling())
            {
                int code = translateParticleCode(cur.getName());
                if (code == 0)
                    continue;
                addMinusPointlessParticles(accumulate,
                        translateContentModel(outerType,
                            cur.getObject(), targetNamespace, chameleon,
                            elemFormDefault, attFormDefault, code,
                            anonymousTypes, elementModel, true, redefinitionFor),
                        sPart.getParticleType());
            }
            sPart.setParticleChildren((SchemaParticle[])
                    accumulate.toArray(new SchemaParticle[accumulate.size()]));
            cur.dispose();
        }
        

        SchemaParticle result = filterPointlessParticlesAndVerifyAllParticles(sPart, parseTree);

        if (group != null)
        {
            state.finishProcessing(group);
        }
        // outdentDBG();
        return result;
    }
    
    static int translateWildcardProcess(Any.ProcessContents process)
    {
        if (process == null)
            return SchemaParticle.STRICT;

        String processValue = process.getStringValue();

        if ("lax".equals(processValue))
            return SchemaParticle.LAX;

        if ("skip".equals(processValue))
            return SchemaParticle.SKIP;

        return SchemaParticle.STRICT;
    }

    static SchemaParticle filterPointlessParticlesAndVerifyAllParticles(SchemaParticle part, XmlObject parseTree)
    {
        if (part.getMaxOccurs() != null && part.getMaxOccurs().signum() == 0)
            return null;

        switch (part.getParticleType())
        {
            case SchemaParticle.SEQUENCE:
            case SchemaParticle.ALL:
                if (part.getParticleChildren().length == 0)
                    return null;
                if (part.isSingleton() && part.countOfParticleChild() == 1)
                    return part.getParticleChild(0);
                break;

            case SchemaParticle.CHOICE:
                if (part.getParticleChildren().length == 0 &&
                    part.getMinOccurs().compareTo(BigInteger.ZERO) == 0)
                    return null;
                if (part.isSingleton() && part.countOfParticleChild() == 1)
                    return part.getParticleChild(0);
                break;

            case SchemaParticle.ELEMENT:
            case SchemaParticle.WILDCARD:
                return part;

            default:
                assert(false);
                throw new IllegalStateException();
        }
        
        boolean isAll = part.getParticleType() == SchemaParticle.ALL;
        
        if (isAll)
        {
            // http://www.w3.org/TR/xmlschema-1/#cos-all-limited
            if (part.getMaxOccurs() == null || part.getMaxOccurs().compareTo(BigInteger.ONE) > 0)
            {
                // An all group must have maxOccurs <= 1
                // KHK: review
                StscState.get().error(XmlErrorCodes.ALL_GROUP_LIMITED$IN_MIN_MAX_1_PARTICLE, null, parseTree);
            }
        }
        
        for (int i = 0; i < part.countOfParticleChild(); i++)
        {
            SchemaParticle child = part.getParticleChild(i);
            if (child.getParticleType() == SchemaParticle.ALL)
            {
                // An all group is only allowed at the top level of the content model
                // KHK: review
                StscState.get().error(XmlErrorCodes.ALL_GROUP_LIMITED$IN_COMPLEX_TYPE_DEF_PARTICLE, null, parseTree);
            }
            else if (isAll && (child.getParticleType() != SchemaParticle.ELEMENT || child.getMaxOccurs() == null || child.getMaxOccurs().compareTo(BigInteger.ONE) > 0))
            {
                // An all group can contain only element particles with maxOccurs <= 1
                // KHK: review
                StscState.get().error(XmlErrorCodes.ALL_GROUP_LIMITED$CHILD_PARTICLES_MAX_LTE_1, null, parseTree);
            }
        }
        
        return part;
    }

    static void addMinusPointlessParticles(
            List list, SchemaParticle part, int parentParticleType)
    {
        if (part == null)
            return;

        switch (part.getParticleType())
        {
            case SchemaParticle.SEQUENCE:
                if (parentParticleType == SchemaParticle.SEQUENCE && part.isSingleton())
                {
                    // emitDBG("dropping redundant sequence");
                    list.addAll(Arrays.asList(part.getParticleChildren()));
                    return;
                }
                break;

            case SchemaParticle.CHOICE:
                if (parentParticleType == SchemaParticle.CHOICE && part.isSingleton())
                {
                    // emitDBG("dropping redundant choice");
                    list.addAll(Arrays.asList(part.getParticleChildren()));
                    return;
                }
                break;
                
            case SchemaParticle.ALL:
            default:                
        }
        list.add(part);
    }

    static Map buildAttributePropertyModelByQName(SchemaAttributeModel attrModel, SchemaType owner)
    {
        Map result = new LinkedHashMap();
        SchemaLocalAttribute[] attruses = attrModel.getAttributes();

        for (int i = 0; i < attruses.length; i++)
            result.put(attruses[i].getName(), buildUseProperty(attruses[i], owner));

        return result;
    }

    static Map buildContentPropertyModelByQName(SchemaParticle part, SchemaType owner)
    {
        if (part == null)
            return Collections.EMPTY_MAP;

        boolean asSequence = false;
        Map model = null;

        switch (part.getParticleType())
        {
            case SchemaParticle.ALL:
            case SchemaParticle.SEQUENCE:
                asSequence = true;
                break;
            case SchemaParticle.CHOICE:
                asSequence = false;
                break;
            case SchemaParticle.ELEMENT:
                model = buildElementPropertyModel((SchemaLocalElement)part, owner);
                break;
            case SchemaParticle.WILDCARD:
                model = Collections.EMPTY_MAP;
                break;
            default:
                assert(false);
                throw new IllegalStateException();
        }

        if (model == null)
        {
            // build model for children
            model = new LinkedHashMap();
            SchemaParticle[] children = part.getParticleChildren();

            for (int i = 0; i < children.length; i++)
            {
                // indentDBG();
                Map childModel = buildContentPropertyModelByQName(children[i], owner);
                // outdentDBG();
                for (Iterator j = childModel.values().iterator(); j.hasNext(); )
                {
                    SchemaProperty iProp = (SchemaProperty)j.next();
                    SchemaPropertyImpl oProp = (SchemaPropertyImpl)model.get(iProp.getName());
                    if (oProp == null)
                    {
                        if (!asSequence)
                            ((SchemaPropertyImpl)iProp).setMinOccurs(BigInteger.ZERO);
                        model.put(iProp.getName(), iProp);
                        continue;
                    }
                    // consistency verified in an earlier step
                    assert(oProp.getType().equals(iProp.getType()));

                    mergeProperties(oProp, iProp, asSequence);
                }
            }

            // finally deal with minOccurs, maxOccurs over whole group
            BigInteger min = part.getMinOccurs();
            BigInteger max = part.getMaxOccurs();

            for (Iterator j = model.values().iterator(); j.hasNext(); )
            {
                SchemaProperty oProp = (SchemaProperty)j.next();
                BigInteger minOccurs = oProp.getMinOccurs();
                BigInteger maxOccurs = oProp.getMaxOccurs();

                minOccurs = minOccurs.multiply(min);
                if (max != null && max.equals(BigInteger.ZERO))
                    maxOccurs = BigInteger.ZERO;
                else if (maxOccurs != null && !maxOccurs.equals(BigInteger.ZERO))
                    maxOccurs = max == null ? null : maxOccurs.multiply(max);

                ((SchemaPropertyImpl)oProp).setMinOccurs(minOccurs);
                ((SchemaPropertyImpl)oProp).setMaxOccurs(maxOccurs);
            }
        }

        return model;
    }

    static Map buildElementPropertyModel(SchemaLocalElement epart, SchemaType owner)
    {
        Map result = new HashMap(1);

        SchemaProperty sProp = buildUseProperty(epart, owner);
        result.put(sProp.getName(), sProp);
        return result;
    }

    static SchemaProperty buildUseProperty(SchemaField use, SchemaType owner)
    {
        SchemaPropertyImpl sPropImpl = new SchemaPropertyImpl();
        sPropImpl.setName(use.getName());
        sPropImpl.setContainerTypeRef(owner.getRef());
        sPropImpl.setTypeRef(use.getType().getRef());
        sPropImpl.setAttribute(use.isAttribute());
        sPropImpl.setDefault(use.isDefault() ? SchemaProperty.CONSISTENTLY : SchemaProperty.NEVER);
        sPropImpl.setFixed(use.isFixed() ? SchemaProperty.CONSISTENTLY : SchemaProperty.NEVER);
        sPropImpl.setNillable(use.isNillable() ? SchemaProperty.CONSISTENTLY : SchemaProperty.NEVER);
        sPropImpl.setDefaultText(use.getDefaultText());
        sPropImpl.setMinOccurs(use.getMinOccurs());
        sPropImpl.setMaxOccurs(use.getMaxOccurs());

        if (use instanceof SchemaLocalElementImpl)
        {
            SchemaLocalElementImpl elt = (SchemaLocalElementImpl)use;
            sPropImpl.setAcceptedNames(elt.acceptedStartNames());
        }

        return sPropImpl;
    }

    static void mergeProperties(SchemaPropertyImpl into, SchemaProperty from, boolean asSequence)
    {
        // minoccur, maxoccur
        BigInteger minOccurs = into.getMinOccurs();
        BigInteger maxOccurs = into.getMaxOccurs();
        if (asSequence)
        {
            minOccurs = minOccurs.add(from.getMinOccurs());
            if (maxOccurs != null)
                maxOccurs = (from.getMaxOccurs() == null ? null :
                                  maxOccurs.add(from.getMaxOccurs()));
        }
        else
        {
            minOccurs = minOccurs.min(from.getMinOccurs());
            if (maxOccurs != null)
                maxOccurs = (from.getMaxOccurs() == null ? null :
                                maxOccurs.max(from.getMaxOccurs()));
        }
        into.setMinOccurs(minOccurs);
        into.setMaxOccurs(maxOccurs);

        // nillable, default, fixed
        if (from.hasNillable() != into.hasNillable())
            into.setNillable(SchemaProperty.VARIABLE);
        if (from.hasDefault() != into.hasDefault())
            into.setDefault(SchemaProperty.VARIABLE);
        if (from.hasFixed() != into.hasFixed())
            into.setFixed(SchemaProperty.VARIABLE);

        // default value
        if (into.getDefaultText() != null)
        {
            if (from.getDefaultText() == null ||
                !into.getDefaultText().equals(from.getDefaultText()))
                into.setDefaultText(null);
        }
    }

    static SchemaParticle[] ensureStateMachine(SchemaParticle[] children)
    {
        for (int i = 0; i < children.length; i++)
        {
            buildStateMachine(children[i]);
        }
        return children;
    }

    static void buildStateMachine(SchemaParticle contentModel)
    {
        if (contentModel == null)
            return;

        SchemaParticleImpl partImpl = (SchemaParticleImpl)contentModel;
        if (partImpl.hasTransitionNotes())
            return;

        QNameSetBuilder start = new QNameSetBuilder();
        QNameSetBuilder excludenext = new QNameSetBuilder();
        boolean deterministic = true;
        SchemaParticle[] children = null;
        boolean canskip = (partImpl.getMinOccurs().signum() == 0);

        switch (partImpl.getParticleType())
        {
            case SchemaParticle.ELEMENT:
                // compute start and excludeNext; canskip is already correct
                if (partImpl.hasTransitionRules())
                    start.addAll(partImpl.acceptedStartNames());
                else
                    start.add(partImpl.getName());

                break;

            case SchemaParticle.WILDCARD:
                // compute start and excludeNext; canskip is already correct
                start.addAll(partImpl.getWildcardSet());
                break;

            case SchemaParticle.SEQUENCE:
                children = ensureStateMachine(partImpl.getParticleChildren());

                // adjust canskip if all children are skippable
                canskip = true;
                for (int i = 0; canskip && i < children.length; i++)
                {
                    if (!(children[i]).isSkippable())
                        canskip = false;
                }
                
                // bubble up nondeterministic bit
                for (int i = 0; deterministic && i < children.length; i++)
                {
                    if (!((SchemaParticleImpl)children[i]).isDeterministic())
                        deterministic = false;
                }
                
                // verify deterministic and compute excludeNext set
                for (int i = 1; i < children.length; i++)
                {
                    excludenext.addAll(((SchemaParticleImpl)children[i - 1]).getExcludeNextSet());
                    if (deterministic && !excludenext.isDisjoint((children[i]).acceptedStartNames()))
                        deterministic = false;
                    if ((children[i]).isSkippable())
                        excludenext.addAll((children[i]).acceptedStartNames());
                    else
                        excludenext.clear();
                }

                // next, compute start set
                for (int i = 0; i < children.length; i++)
                {
                    start.addAll((children[i]).acceptedStartNames());
                    if (!(children[i]).isSkippable())
                        break;
                }
                break;

            case SchemaParticle.CHOICE:
                children = ensureStateMachine(partImpl.getParticleChildren());

                // adjust canskip if any children are skippable
                canskip = false;
                for (int i = 0; !canskip && i < children.length; i++)
                {
                    if ((children[i]).isSkippable())
                        canskip = true;
                }

                // bubble up nondeterministic bit
                for (int i = 0; deterministic && i < children.length; i++)
                {
                    if (!((SchemaParticleImpl)children[i]).isDeterministic())
                        deterministic = false;
                }
                
                // compute start and excludeNext sets, verify deterministic
                for (int i = 0; i < children.length; i++)
                {
                    if (deterministic && !start.isDisjoint((children[i]).acceptedStartNames()))
                        deterministic = false;
                    start.addAll((children[i]).acceptedStartNames());
                    excludenext.addAll(((SchemaParticleImpl)children[i]).getExcludeNextSet());
                }

                break;

            case SchemaParticle.ALL:
                children = ensureStateMachine(partImpl.getParticleChildren());

                // adjust canskip if all children are skippable
                canskip = true;
                for (int i = 0; !canskip && i < children.length; i++)
                {
                    if (!(children[i]).isSkippable())
                        canskip = false;
                }

                // bubble up nondeterministic bit
                for (int i = 0; deterministic && i < children.length; i++)
                {
                    if (!((SchemaParticleImpl)children[i]).isDeterministic())
                        deterministic = false;
                }
                
                // compute start and excludeNext sets, verify deterministic
                for (int i = 0; i < children.length; i++)
                {
                    if (deterministic && !start.isDisjoint((children[i]).acceptedStartNames()))
                        deterministic = false;
                    start.addAll((children[i]).acceptedStartNames());
                    excludenext.addAll(((SchemaParticleImpl)children[i]).getExcludeNextSet());
                }
                if (canskip)
                    excludenext.addAll(start);

                break;

            default:
                throw new IllegalStateException("Unrecognized schema particle");
        }

        // apply looping logic

        BigInteger minOccurs = partImpl.getMinOccurs();
        BigInteger maxOccurs = partImpl.getMaxOccurs();
        boolean canloop = (maxOccurs == null || maxOccurs.compareTo(BigInteger.ONE) > 0);
        boolean varloop = (maxOccurs == null || minOccurs.compareTo(maxOccurs) < 0);

        if (canloop && deterministic && !excludenext.isDisjoint(start))
        {
            // we have a possible looping nondeterminism.
            // let's take some time now to see if it's actually caused
            // by non-unique-particle-attribute or not.
            QNameSet suspectSet = excludenext.intersect(start);
            
            // compute the set of all particles that could start this group
            Map startMap = new HashMap();
            particlesMatchingStart(partImpl, suspectSet, startMap, new QNameSetBuilder());
            
            // compute the set of all particles that could have been repeated rather than ending this group
            Map afterMap = new HashMap();
            particlesMatchingAfter(partImpl, suspectSet, afterMap, new QNameSetBuilder(), true);
            
            // see if we can find a member of after that is not a member of start
            // if we can, then particle attribution is not unique
            deterministic = afterMapSubsumedByStartMap(startMap, afterMap);
        }

        if (varloop)
            excludenext.addAll(start);

        canskip = canskip || minOccurs.signum() == 0;

        partImpl.setTransitionRules(start.toQNameSet(), canskip);
        partImpl.setTransitionNotes(excludenext.toQNameSet(), deterministic);
    }
    
    private static boolean afterMapSubsumedByStartMap(Map startMap, Map afterMap)
    {
        if (afterMap.size() > startMap.size())
            return false;
        
        if (afterMap.isEmpty())
            return true;
        
        for (Iterator i = startMap.keySet().iterator(); i.hasNext(); )
        {
            SchemaParticle part = (SchemaParticle)i.next();
            if (part.getParticleType() == SchemaParticle.WILDCARD)
            {
                if (afterMap.containsKey(part))
                {
                    QNameSet startSet = (QNameSet)startMap.get(part);
                    QNameSet afterSet = (QNameSet)afterMap.get(part);
                    if (!startSet.containsAll(afterSet))
                        return false;
                }
            }
            afterMap.remove(part);
            if (afterMap.isEmpty())
                return true;
        }
        return (afterMap.isEmpty());
    }
    
    private static void particlesMatchingStart(SchemaParticle part, QNameSetSpecification suspectSet, Map result, QNameSetBuilder eliminate)
    {
        switch (part.getParticleType())
        {
            case SchemaParticle.ELEMENT:
                if (!suspectSet.contains(part.getName()))
                    return;
                result.put(part, null);
                eliminate.add(part.getName());
                return;
                
            case SchemaParticle.WILDCARD:
                if (suspectSet.isDisjoint(part.getWildcardSet()))
                    return;
                result.put(part, part.getWildcardSet().intersect(suspectSet));
                eliminate.addAll(part.getWildcardSet());
                return;
                
            case SchemaParticle.CHOICE:
            case SchemaParticle.ALL:
                {
                    SchemaParticle[] children = part.getParticleChildren();
                    for (int i = 0; i < children.length; i++)
                        particlesMatchingStart(children[i], suspectSet, result, eliminate);
                    return;
                }
                
            case SchemaParticle.SEQUENCE:
                {
                    SchemaParticle[] children = part.getParticleChildren();
                    if (children.length == 0)
                        return;
                    if (!children[0].isSkippable())
                    {
                        particlesMatchingStart(children[0], suspectSet, result, eliminate);
                        return;
                    }
                    QNameSetBuilder remainingSuspects = new QNameSetBuilder(suspectSet);
                    QNameSetBuilder suspectsToEliminate = new QNameSetBuilder();
                    for (int i = 0; i < children.length; i++)
                    {
                        particlesMatchingStart(children[i], remainingSuspects, result, suspectsToEliminate);
                        eliminate.addAll(suspectsToEliminate);
                        if (!children[i].isSkippable())
                            return;
                        remainingSuspects.removeAll(suspectsToEliminate);
                        if (remainingSuspects.isEmpty())
                            return;
                        suspectsToEliminate.clear();
                    }
                    return;
                }
        }
    }
    
    private static void particlesMatchingAfter(SchemaParticle part, QNameSetSpecification suspectSet, Map result, QNameSetBuilder eliminate, boolean top)
    {
        recurse: switch (part.getParticleType())
        {
            case SchemaParticle.CHOICE:
            case SchemaParticle.ALL:
                {
                    SchemaParticle[] children = part.getParticleChildren();
                    for (int i = 0; i < children.length; i++)
                        particlesMatchingAfter(children[i], suspectSet, result, eliminate, false);
                    break recurse;
                }
                
            case SchemaParticle.SEQUENCE:
                {
                    SchemaParticle[] children = part.getParticleChildren();
                    if (children.length == 0)
                        break recurse;
                    if (!children[children.length - 1].isSkippable())
                    {
                        particlesMatchingAfter(children[0], suspectSet, result, eliminate, false);
                        break recurse;
                    }
                    QNameSetBuilder remainingSuspects = new QNameSetBuilder(suspectSet);
                    QNameSetBuilder suspectsToEliminate = new QNameSetBuilder();
                    for (int i = children.length - 1; i >= 0; i--)
                    {
                        particlesMatchingAfter(children[i], remainingSuspects, result, suspectsToEliminate, false);
                        eliminate.addAll(suspectsToEliminate);
                        if (!children[i].isSkippable())
                            break recurse;
                        remainingSuspects.removeAll(suspectsToEliminate);
                        if (remainingSuspects.isEmpty())
                            break recurse;
                        suspectsToEliminate.clear();
                    }
                    break recurse;
                }
        }

        if (!top)
        {
            BigInteger minOccurs = part.getMinOccurs();
            BigInteger maxOccurs = part.getMaxOccurs();
            boolean varloop = (maxOccurs == null || minOccurs.compareTo(maxOccurs) < 0);
            if (varloop)
            {
                particlesMatchingStart(part, suspectSet, result, eliminate);
            }
        }
    }

    private static class CodeForNameEntry
    {
        CodeForNameEntry(QName name, int code)
            { this.name = name; this.code = code; }
        public QName name;
        public int code;
    }

    private static final int MODEL_GROUP_CODE = 100;

    private static CodeForNameEntry[] particleCodes = new CodeForNameEntry[]
    {
        new CodeForNameEntry(QNameHelper.forLNS("all", "http://www.w3.org/2001/XMLSchema"), SchemaParticle.ALL),
        new CodeForNameEntry(QNameHelper.forLNS("sequence", "http://www.w3.org/2001/XMLSchema"), SchemaParticle.SEQUENCE),
        new CodeForNameEntry(QNameHelper.forLNS("choice", "http://www.w3.org/2001/XMLSchema"), SchemaParticle.CHOICE),
        new CodeForNameEntry(QNameHelper.forLNS("element", "http://www.w3.org/2001/XMLSchema"), SchemaParticle.ELEMENT),
        new CodeForNameEntry(QNameHelper.forLNS("any", "http://www.w3.org/2001/XMLSchema"), SchemaParticle.WILDCARD),
        new CodeForNameEntry(QNameHelper.forLNS("group", "http://www.w3.org/2001/XMLSchema"), MODEL_GROUP_CODE),
    };

    private static Map particleCodeMap = buildParticleCodeMap();

    private static Map buildParticleCodeMap()
    {
        Map result = new HashMap();
        for (int i = 0; i < particleCodes.length; i++)
            result.put(particleCodes[i].name,  new Integer(particleCodes[i].code));
        return result;
    }

    private static int translateParticleCode(Group parseEg)
    {
        if (parseEg == null)
            return 0;
        return translateParticleCode(parseEg.newCursor().getName());
    }

    private static int translateParticleCode(QName name)
    {
        Integer result = (Integer)particleCodeMap.get(name);
        if (result == null)
            return 0;
        return result.intValue();
    }

    private static final int ATTRIBUTE_CODE = 100;
    private static final int ATTRIBUTE_GROUP_CODE = 101;
    private static final int ANY_ATTRIBUTE_CODE = 102;

    private static CodeForNameEntry[] attributeCodes = new CodeForNameEntry[]
    {
        new CodeForNameEntry(QNameHelper.forLNS("attribute", "http://www.w3.org/2001/XMLSchema"), ATTRIBUTE_CODE),
        new CodeForNameEntry(QNameHelper.forLNS("attributeGroup", "http://www.w3.org/2001/XMLSchema"), ATTRIBUTE_GROUP_CODE),
        new CodeForNameEntry(QNameHelper.forLNS("anyAttribute", "http://www.w3.org/2001/XMLSchema"), ANY_ATTRIBUTE_CODE),
    };

    private static Map attributeCodeMap = buildAttributeCodeMap();

    private static Map buildAttributeCodeMap()
    {
        Map result = new HashMap();
        for (int i = 0; i < attributeCodes.length; i++)
            result.put(attributeCodes[i].name,  new Integer(attributeCodes[i].code));
        return result;
    }

    static int translateAttributeCode(QName currentName)
    {
        Integer result = (Integer)attributeCodeMap.get(currentName);
        if (result == null)
            return 0;
        return result.intValue();
    }


}
