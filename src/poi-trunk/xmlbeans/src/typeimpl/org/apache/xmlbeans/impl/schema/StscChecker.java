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

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SchemaLocalElement;
import org.apache.xmlbeans.SchemaIdentityConstraint;
import org.apache.xmlbeans.SchemaAttributeModel;
import org.apache.xmlbeans.SchemaLocalAttribute;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.XmlID;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlNOTATION;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.impl.common.XBeanDebug;
import org.apache.xmlbeans.impl.common.QNameHelper;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.math.BigInteger;

public class StscChecker
{
    public static void checkAll()
    {
        // walk the tree of types
        StscState state = StscState.get();

        List allSeenTypes = new ArrayList();
        allSeenTypes.addAll(Arrays.asList(state.documentTypes()));
        allSeenTypes.addAll(Arrays.asList(state.attributeTypes()));
        allSeenTypes.addAll(Arrays.asList(state.redefinedGlobalTypes()));
        allSeenTypes.addAll(Arrays.asList(state.globalTypes()));

        for (int i = 0; i < allSeenTypes.size(); i++)
        {
            SchemaType gType = (SchemaType)allSeenTypes.get(i);
            if (!state.noPvr() &&  // option to turn off particle restriction checking
                !gType.isDocumentType()) // Don't check doc types for restriction. 
            {
                checkRestriction((SchemaTypeImpl)gType);
            }
            checkFields((SchemaTypeImpl)gType);
            allSeenTypes.addAll(Arrays.asList(gType.getAnonymousTypes()));
        }

        checkSubstitutionGroups(state.globalElements());
    }
    
    /**
     * The following code checks rule #5 of http://www.w3.org/TR/xmlschema-1/#coss-ct
     * as well as attribute + element default/fixed validity. <p/>
     * Checks that xs:NOTATION is not used directly
     */
    public static void checkFields(SchemaTypeImpl sType)
    {
        if (sType.isSimpleType())
            return;
        
        XmlObject location = sType.getParseObject();
        
        SchemaAttributeModel sAttrModel = sType.getAttributeModel();
        if (sAttrModel != null)
        {
            SchemaLocalAttribute[] sAttrs  = sAttrModel.getAttributes();
            QName idAttr = null;
            for (int i = 0; i < sAttrs.length; i++)
            {
                XmlObject attrLocation = ((SchemaLocalAttributeImpl)sAttrs[i])._parseObject;
                if (XmlID.type.isAssignableFrom(sAttrs[i].getType()))
                {
                    if (idAttr == null)
                    {
                        idAttr = sAttrs[i].getName();
                    }
                    else
                    {
                        StscState.get().error(XmlErrorCodes.ATTR_GROUP_PROPERTIES$TWO_IDS,
                            new Object[]{ QNameHelper.pretty(idAttr), sAttrs[i].getName() },
                            attrLocation != null ? attrLocation : location);
                    }
                    if (sAttrs[i].getDefaultText() != null)
                    {
                        StscState.get().error(XmlErrorCodes.ATTR_PROPERTIES$ID_FIXED_OR_DEFAULT,
                            null, attrLocation != null ? attrLocation : location);
                    }
                }
                else if (XmlNOTATION.type.isAssignableFrom(sAttrs[i].getType()))
                {
                    if (sAttrs[i].getType().getBuiltinTypeCode() == SchemaType.BTC_NOTATION)
                    {
                        StscState.get().recover(XmlErrorCodes.ATTR_NOTATION_TYPE_FORBIDDEN,
                            new Object[]{ QNameHelper.pretty(sAttrs[i].getName()) },
                            attrLocation != null ? attrLocation : location);
                    }
                    else
                    {
                        if (sAttrs[i].getType().getSimpleVariety() == SchemaType.UNION)
                        {
                            SchemaType[] members = sAttrs[i].getType().getUnionConstituentTypes();
                            for (int j = 0; j < members.length; j++)
                                if (members[j].getBuiltinTypeCode() == SchemaType.BTC_NOTATION)
                                    StscState.get().recover(XmlErrorCodes.ATTR_NOTATION_TYPE_FORBIDDEN,
                                        new Object[]{ QNameHelper.pretty(sAttrs[i].getName()) },
                                        attrLocation != null ? attrLocation : location);
                        }
                        // Check that the Schema in which this is present doesn't have a targetNS
                        boolean hasNS;
                        if (sType.isAttributeType())
                            hasNS = sAttrs[i].getName().getNamespaceURI().length() > 0;
                        else
                        {
                            SchemaType t = sType;
                            while (t.getOuterType() != null)
                                t = t.getOuterType();
                            if (t.isDocumentType())
                                hasNS = t.getDocumentElementName().getNamespaceURI().length() > 0;
                            else hasNS = t.getName().getNamespaceURI().length() > 0;
                        }
                        if (hasNS)
                            StscState.get().warning(XmlErrorCodes.ATTR_COMPATIBILITY_TARGETNS,
                                new Object[] {QNameHelper.pretty(sAttrs[i].getName()) },
                                attrLocation != null ? attrLocation : location);
                    }
                }
                else
                {
                    String valueConstraint = sAttrs[i].getDefaultText();
                    if (valueConstraint != null)
                    {
                        try
                        {
                            XmlAnySimpleType val = sAttrs[i].getDefaultValue();
                            if (!val.validate())
                                throw new Exception();
                            
                            SchemaPropertyImpl sProp = (SchemaPropertyImpl)sType.getAttributeProperty(sAttrs[i].getName());
                            if (sProp != null && sProp.getDefaultText() != null)
                            {
                                sProp.setDefaultValue(new XmlValueRef(val));
                            }
                        }
                        catch (Exception e)
                        {
                            // move to 'fixed' or 'default' attribute on the attribute definition
                            String constraintName = (sAttrs[i].isFixed() ? "fixed" : "default");
                            XmlObject constraintLocation = location;
                            if (attrLocation != null)
                            {
                                constraintLocation = attrLocation.selectAttribute("", constraintName);
                                if (constraintLocation == null)
                                    constraintLocation = attrLocation;
                            }

                            StscState.get().error(XmlErrorCodes.ATTR_PROPERTIES$CONSTRAINT_VALID,
                                new Object[] { QNameHelper.pretty(sAttrs[i].getName()), 
                                               constraintName,
                                               valueConstraint,
                                               QNameHelper.pretty(sAttrs[i].getType().getName()) },
                                constraintLocation);
                        }
                    }
                }
            }
        }
        
        checkElementDefaults(sType.getContentModel(), location, sType);
    }

    /**
     * Checks the default values of elements.<p/>
     * Also checks that the type of elements is not one of ID, IDREF, IDREFS, ENTITY, ENTITIES or
     * NOTATION as per XMLSchema part 2.
     * @param model
     * @param location
     * @param parentType
     */
    private static void checkElementDefaults(SchemaParticle model, XmlObject location, SchemaType parentType)
    {
        if (model == null)
            return;
        switch (model.getParticleType())
        {
            case SchemaParticle.SEQUENCE:
            case SchemaParticle.CHOICE:
            case SchemaParticle.ALL:
                SchemaParticle[] children = model.getParticleChildren();
                for (int i = 0; i < children.length; i++)
                {
                    checkElementDefaults(children[i], location, parentType);
                }
                break;
           case SchemaParticle.ELEMENT:
                String valueConstraint = model.getDefaultText();
                if (valueConstraint != null)
                {
                    if (model.getType().isSimpleType() || model.getType().getContentType() == SchemaType.SIMPLE_CONTENT)
                    {
                        try
                        {
                            XmlAnySimpleType val = model.getDefaultValue();
                            XmlOptions opt = new XmlOptions();
                            opt.put(XmlOptions.VALIDATE_TEXT_ONLY);
                            if (!val.validate(opt))
                                throw new Exception();

                            SchemaPropertyImpl sProp = (SchemaPropertyImpl)parentType.getElementProperty(model.getName());
                            if (sProp != null && sProp.getDefaultText() != null)
                            {
                                sProp.setDefaultValue(new XmlValueRef(val));
                            }
                        }
                        catch (Exception e)
                        {
                            // move to 'fixed' or 'default' attribute on the element definition
                            String constraintName = (model.isFixed() ? "fixed" : "default");
                            XmlObject constraintLocation = location.selectAttribute("", constraintName);

                            StscState.get().error(XmlErrorCodes.ELEM_PROPERTIES$CONSTRAINT_VALID,
                                new Object[] { QNameHelper.pretty(model.getName()),
                                               constraintName,
                                               valueConstraint,
                                               QNameHelper.pretty(model.getType().getName()) },
                                (constraintLocation==null ? location : constraintLocation));
                        }
                    }
                    else if (model.getType().getContentType() == SchemaType.MIXED_CONTENT)
                    {
                        if (!model.getType().getContentModel().isSkippable())
                        {
                            String constraintName = (model.isFixed() ? "fixed" : "default");
                            XmlObject constraintLocation = location.selectAttribute("", constraintName);

                            StscState.get().error(XmlErrorCodes.ELEM_DEFAULT_VALID$MIXED_AND_EMPTIABLE,
                                new Object[] { QNameHelper.pretty(model.getName()),
                                               constraintName,
                                               valueConstraint },
                                (constraintLocation==null ? location : constraintLocation));
                        }
                        else
                        {
                            // Element Default Valid (Immediate): cos-valid-default.2.2.2
                            // no need to validate the value; type is a xs:string
                            SchemaPropertyImpl sProp = (SchemaPropertyImpl)parentType.getElementProperty(model.getName());
                            if (sProp != null && sProp.getDefaultText() != null)
                            {
                                sProp.setDefaultValue(new XmlValueRef(XmlString.type.newValue(valueConstraint)));
                            }
                        }
                    }
                    else if (model.getType().getContentType() == SchemaType.ELEMENT_CONTENT)
                    {
                        XmlObject constraintLocation = location.selectAttribute("", "default");
                        StscState.get().error(XmlErrorCodes.ELEM_DEFAULT_VALID$SIMPLE_TYPE_OR_MIXED,
                            new Object[] { QNameHelper.pretty(model.getName()),
                                           valueConstraint,
                                           "element" },
                            (constraintLocation==null ? location : constraintLocation));
                    }
                    else if (model.getType().getContentType() == SchemaType.EMPTY_CONTENT)
                    {
                        XmlObject constraintLocation = location.selectAttribute("", "default");
                        StscState.get().error(XmlErrorCodes.ELEM_DEFAULT_VALID$SIMPLE_TYPE_OR_MIXED,
                            new Object[] { QNameHelper.pretty(model.getName()),
                                           valueConstraint,
                                           "empty" },
                            (constraintLocation==null ? location : constraintLocation));
                    }
                }
                // Checks if the type is one of the "attribute-specific" types
                String warningType = null;
                if (BuiltinSchemaTypeSystem.ST_ID.isAssignableFrom(model.getType()))
                    warningType = BuiltinSchemaTypeSystem.ST_ID.getName().getLocalPart();
                else if (BuiltinSchemaTypeSystem.ST_IDREF.isAssignableFrom(model.getType()))
                    warningType = BuiltinSchemaTypeSystem.ST_IDREF.getName().getLocalPart();
                else if (BuiltinSchemaTypeSystem.ST_IDREFS.isAssignableFrom(model.getType()))
                    warningType = BuiltinSchemaTypeSystem.ST_IDREFS.getName().getLocalPart();
                else if (BuiltinSchemaTypeSystem.ST_ENTITY.isAssignableFrom(model.getType()))
                    warningType = BuiltinSchemaTypeSystem.ST_ENTITY.getName().getLocalPart();
                else if (BuiltinSchemaTypeSystem.ST_ENTITIES.isAssignableFrom(model.getType()))
                    warningType = BuiltinSchemaTypeSystem.ST_ENTITIES.getName().getLocalPart();
                else if (BuiltinSchemaTypeSystem.ST_NOTATION.isAssignableFrom(model.getType()))
                {
                    if (model.getType().getBuiltinTypeCode() == SchemaType.BTC_NOTATION)
                    {
                        StscState.get().recover(XmlErrorCodes.ELEM_NOTATION_TYPE_FORBIDDEN,
                            new Object[]{ QNameHelper.pretty(model.getName()) },
                            ((SchemaLocalElementImpl) model)._parseObject == null ? location :
                            ((SchemaLocalElementImpl) model)._parseObject.selectAttribute("", "type"));
                    }
                    else
                    {
                        if (model.getType().getSimpleVariety() == SchemaType.UNION)
                        {
                            SchemaType[] members = model.getType().getUnionConstituentTypes();
                            for (int i = 0; i < members.length; i++)
                                if (members[i].getBuiltinTypeCode() == SchemaType.BTC_NOTATION)
                                    StscState.get().recover(XmlErrorCodes.ELEM_NOTATION_TYPE_FORBIDDEN,
                                        new Object[]{ QNameHelper.pretty(model.getName()) },
                                        ((SchemaLocalElementImpl) model)._parseObject == null ? location :
                                        ((SchemaLocalElementImpl) model)._parseObject.selectAttribute("", "type"));
                        }
                        warningType = BuiltinSchemaTypeSystem.ST_NOTATION.getName().getLocalPart();
                    }

                    // Check that the Schema in which this is present doesn't have a targetNS
                    boolean hasNS;
                    SchemaType t = parentType;
                    while (t.getOuterType() != null)
                        t = t.getOuterType();
                    if (t.isDocumentType())
                        hasNS = t.getDocumentElementName().getNamespaceURI().length() > 0;
                    else
                        hasNS = t.getName().getNamespaceURI().length() > 0;
                    if (hasNS)
                        StscState.get().warning(XmlErrorCodes.ELEM_COMPATIBILITY_TARGETNS,
                            new Object[] {QNameHelper.pretty(model.getName()) },
                            ((SchemaLocalElementImpl) model)._parseObject == null ? location :
                            ((SchemaLocalElementImpl) model)._parseObject);
                }

                if (warningType != null)
                    StscState.get().warning(XmlErrorCodes.ELEM_COMPATIBILITY_TYPE, new Object[]
                        { QNameHelper.pretty(model.getName()), warningType },
                        ((SchemaLocalElementImpl) model)._parseObject == null ? location :
                        ((SchemaLocalElementImpl) model)._parseObject.selectAttribute("", "type"));

                break;

           default:
                // nothing to do.
                break;
        }
    }
    
    /**
     * The following code only checks rule #5 of http://www.w3.org/TR/xmlschema-1/#derivation-ok-restriction
     *  (Everything else can and should be done in StscResolver, because we can give more detailed line # info there
     */
    public static boolean checkRestriction(SchemaTypeImpl sType)
    {
        if (sType.getDerivationType() == SchemaType.DT_RESTRICTION && !sType.isSimpleType())
        {
            StscState state = StscState.get();
            
            // we don't remember very precise line number information, but it's better than nothin.
            XmlObject location = sType.getParseObject();
        
            SchemaType baseType = sType.getBaseType();
            if (baseType.isSimpleType())
            {
                state.error(XmlErrorCodes.SCHEMA_COMPLEX_TYPE$COMPLEX_CONTENT,
                    new Object[] { QNameHelper.pretty(baseType.getName()) },
                    location);
                return false;
            }
            
            // 5 The appropriate case among the following must be true:
            switch (sType.getContentType())
            {
                case SchemaType.SIMPLE_CONTENT:
                    // 5.1 If the {content type} of the complex type definition is a simple type definition, then one of the following must be true:
                    switch (baseType.getContentType())
                    {
                        case SchemaType.SIMPLE_CONTENT:
                            // 5.1.1 The {content type} of the {base type definition} must be a simple type definition of which the {content type} is a �valid restriction� as defined in Derivation Valid (Restriction, Simple) (�3.14.6).
                            SchemaType cType = sType.getContentBasedOnType();
                            if (cType != baseType)
                            {
                                // We have to check that the contentType is legally derived
                                // from the base simple type in the hierarchy
                                SchemaType bType = baseType;
                                while (bType != null && !bType.isSimpleType())
                                    bType = bType.getContentBasedOnType();
                                if (bType != null && !bType.isAssignableFrom(cType))
                                {
                                    state.error(XmlErrorCodes.COMPLEX_TYPE_RESTRICTION$SC_NOT_DERIVED,
                                        null, location);
                                    return false;
                                }
                            }
                            break;
                            
                        case SchemaType.MIXED_CONTENT:
                            // 5.1.2 The {base type definition} must be mixed and have a particle which is �emptiable� as defined in Particle Emptiable (�3.9.6).
                            if (baseType.getContentModel() != null && !baseType.getContentModel().isSkippable())
                            {
                                state.error(XmlErrorCodes.COMPLEX_TYPE_RESTRICTION$SC_AND_MIXED_EMPTIABLE,
                                    null, location);
                                return false;
                            }
                            break;
                            
                        default:
                            state.error(XmlErrorCodes.COMPLEX_TYPE_RESTRICTION$SC_AND_SIMPLE_TYPE_OR_MIXED,
                                null, location);
                            return false;
                    }
                    break;
                    
                case SchemaType.EMPTY_CONTENT:
                    // 5.2 If the {content type} of the complex type itself is empty , then one of the following must be true:
                    switch (baseType.getContentType())
                    {
                        case SchemaType.EMPTY_CONTENT:
                            // 5.2.1 The {content type} of the {base type definition} must also be empty.
                            break;
                        case SchemaType.MIXED_CONTENT:
                        case SchemaType.ELEMENT_CONTENT:
                            // 5.2.2 The {content type} of the {base type definition} must be elementOnly or mixed and have a particle which is �emptiable� as defined in Particle Emptiable (�3.9.6).
                            if (baseType.getContentModel() != null && !baseType.getContentModel().isSkippable())
                            {
                                state.error(XmlErrorCodes.COMPLEX_TYPE_RESTRICTION$EMPTY_AND_ELEMENT_OR_MIXED_EMPTIABLE,
                                    null, location);
                                return false;
                            }
                            break;
                        default:                            
                            state.error(XmlErrorCodes.COMPLEX_TYPE_RESTRICTION$EMPTY_AND_NOT_SIMPLE,
                                null, location);
                            return false;
                    }
                    break;
                    
                case SchemaType.MIXED_CONTENT:
                    // 5.3 If the {content type} of the {base type definition} is mixed...
                    if (baseType.getContentType() != SchemaType.MIXED_CONTENT)
                    {
                        state.error(XmlErrorCodes.COMPLEX_TYPE_RESTRICTION$ELEMENT_OR_MIXED_AND_MIXED,
                            null, location);
                        return false;
                    }
                    
                    // FALLTHROUGH
                case SchemaType.ELEMENT_CONTENT:
                    // 5.3 ... or the {content type} of the complex type definition itself is element-only,...
                    if (baseType.getContentType() == SchemaType.EMPTY_CONTENT)
                    {
                        state.error(XmlErrorCodes.COMPLEX_TYPE_RESTRICTION$ELEMENT_OR_MIXED_AND_EMPTY,
                            null, location);
                        return false;
                    }
                    if (baseType.getContentType() == SchemaType.SIMPLE_CONTENT)
                    {
                        state.error(XmlErrorCodes.COMPLEX_TYPE_RESTRICTION$ELEMENT_OR_MIXED_AND_SIMPLE,
                            null, location);
                        return false;
                    }
                    
                    // 5.3 ... then the particle of the complex type definition itself must be a �valid restriction� of the particle of the {content type} of the {base type definition}
                    SchemaParticle baseModel = baseType.getContentModel();
                    SchemaParticle derivedModel = sType.getContentModel();
                    
                    if ( derivedModel == null && sType.getDerivationType()==SchemaType.DT_RESTRICTION )
                    {
                        // it is ok to have an empty contentModel if it's a restriction
                        // see Particle Valid (Restriction) (3.9.6) all three bulets 2.2.1
                        return true;
                    }
                    else if (baseModel == null || derivedModel == null)
                    {
                        XBeanDebug.logStackTrace("Null models that weren't caught by EMPTY_CONTENT: " + baseType + " (" + baseModel + "), " + sType + " (" + derivedModel + ")");
                        state.error(XmlErrorCodes.COMPLEX_TYPE_RESTRICTION$ELEMENT_OR_MIXED_AND_VALID, null, location);
                        return false;
                    }
                    
                    // 5.3 ...  as defined in Particle Valid (Restriction) (�3.9.6).
                    List errors = new ArrayList();
                    boolean isValid = isParticleValidRestriction(baseModel, derivedModel, errors, location);
                    if (!isValid)
                    {
                        // we only add the last error, because isParticleValidRestriction may add errors
                        // to the collection that it later changes its mind about, or it may (inadvertently)
                        // forget to describe an error into the collection....
                        if (errors.size() == 0)
                            state.error(XmlErrorCodes.COMPLEX_TYPE_RESTRICTION$ELEMENT_OR_MIXED_AND_VALID, null, location);
                        else
                            state.getErrorListener().add(errors.get(errors.size() - 1));
                            //state.getErrorListener().addAll(errors);
                        return false; // KHK: should return false, right?
                    }
            }
        }
        return true;
    }
    
    /**
     * This function takes in two schema particle types, a baseModel, and a derived model and returns true if the
     * derivedModel can be egitimately be used for restriction.  Errors are put into the errors collections.
     * @param baseModel - The base schema particle
     * @param derivedModel - The derived (restricted) schema particle
     * @param errors - Invalid restriction errors are put into this collection
     * @param context
     * @return boolean, true if valid restruction, false if invalid restriction
     * @
     */
    public static boolean isParticleValidRestriction(SchemaParticle baseModel, SchemaParticle derivedModel, Collection errors, XmlObject context)  {
        boolean restrictionValid = false;
        // 1 They are the same particle.
        if (baseModel.equals(derivedModel)) {
            restrictionValid = true;
        } else {
            // Implement table defined in schema spec on restrictions at:
            //   http://www.w3.org/TR/xmlschema-1/#cos-particle-restrict
            switch (baseModel.getParticleType()) {
                case SchemaParticle.ELEMENT:
                    switch (derivedModel.getParticleType()) {
                        case SchemaParticle.ELEMENT:
                            restrictionValid = nameAndTypeOK((SchemaLocalElement) baseModel, (SchemaLocalElement) derivedModel, errors, context);
                            break;
                        case SchemaParticle.WILDCARD:
                        case SchemaParticle.ALL:
                        case SchemaParticle.CHOICE:
                        case SchemaParticle.SEQUENCE:
                            errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_RESTRICTION$INVALID_RESTRICTION,
                                new Object[] { printParticle(derivedModel), printParticle(baseModel) }, context));
                            restrictionValid = false;
                            break;
                        default:
                            assert false : XBeanDebug.logStackTrace("Unknown schema type for Derived Type");
                    }
                    break;
                case SchemaParticle.WILDCARD:
                    switch (derivedModel.getParticleType()) {
                        case SchemaParticle.ELEMENT:
                            restrictionValid = nsCompat(baseModel, (SchemaLocalElement) derivedModel, errors, context);
                            break;
                        case SchemaParticle.WILDCARD:
                            restrictionValid = nsSubset(baseModel, derivedModel, errors, context);
                            break;
                        case SchemaParticle.ALL:
                            restrictionValid = nsRecurseCheckCardinality(baseModel, derivedModel, errors, context);
                            break;
                        case SchemaParticle.CHOICE:
                            restrictionValid = nsRecurseCheckCardinality(baseModel, derivedModel, errors, context);
                            break;
                        case SchemaParticle.SEQUENCE:
                            restrictionValid = nsRecurseCheckCardinality(baseModel, derivedModel, errors, context);
                            break;
                        default:
                            assert false : XBeanDebug.logStackTrace("Unknown schema type for Derived Type");
                    }
                    break;
                case SchemaParticle.ALL:
                    switch (derivedModel.getParticleType()) {
                        case SchemaParticle.ELEMENT:
                            restrictionValid = recurseAsIfGroup(baseModel, derivedModel, errors, context);
                            break;
                        case SchemaParticle.WILDCARD:
                        case SchemaParticle.CHOICE:
                            errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_RESTRICTION$INVALID_RESTRICTION,
                                new Object[] { printParticle(derivedModel), printParticle(baseModel) }, context));
                            restrictionValid = false;
                            break;
                        case SchemaParticle.ALL:
                            restrictionValid = recurse(baseModel, derivedModel, errors, context);
                            break;
                        case SchemaParticle.SEQUENCE:
                            restrictionValid = recurseUnordered(baseModel, derivedModel, errors, context);
                            break;
                        default:
                            assert false : XBeanDebug.logStackTrace("Unknown schema type for Derived Type");
                    }
                    break;
                case SchemaParticle.CHOICE:
                    switch (derivedModel.getParticleType()) {
                        case SchemaParticle.ELEMENT:
                            restrictionValid = recurseAsIfGroup(baseModel, derivedModel, errors, context);
                            break;
                        case SchemaParticle.WILDCARD:
                        case SchemaParticle.ALL:
                            errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_RESTRICTION$INVALID_RESTRICTION,
                                new Object[] { printParticle(derivedModel), printParticle(baseModel) }, context));
                            restrictionValid = false;
                            break;
                        case SchemaParticle.CHOICE:
                            restrictionValid = recurseLax(baseModel, derivedModel, errors, context);
                            break;
                        case SchemaParticle.SEQUENCE:
                            restrictionValid = mapAndSum(baseModel, derivedModel, errors, context);
                            break;
                        default:
                            assert false : XBeanDebug.logStackTrace("Unknown schema type for Derived Type");
                    }
                    break;
                case SchemaParticle.SEQUENCE:
                    switch (derivedModel.getParticleType()) {
                        case SchemaParticle.ELEMENT:
                            restrictionValid = recurseAsIfGroup(baseModel, derivedModel, errors, context);
                            break;
                        case SchemaParticle.WILDCARD:
                        case SchemaParticle.ALL:
                        case SchemaParticle.CHOICE:
                            errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_RESTRICTION$INVALID_RESTRICTION,
                                new Object[] { printParticle(derivedModel), printParticle(baseModel) }, context));
                            restrictionValid = false;
                            break;
                        case SchemaParticle.SEQUENCE:
                            restrictionValid = recurse(baseModel, derivedModel, errors, context);
                            break;
                        default:
                            assert false : XBeanDebug.logStackTrace("Unknown schema type for Derived Type");
                    }
                    break;
                default:
                    assert false : XBeanDebug.logStackTrace("Unknown schema type for Base Type");

            }
        }

        return restrictionValid;
    }

    private static boolean mapAndSum(SchemaParticle baseModel, SchemaParticle derivedModel, Collection errors, XmlObject context)  {
        // mapAndSum is call if base: CHOICE, derived: SEQUENCE
        assert baseModel.getParticleType() == SchemaParticle.CHOICE;
        assert derivedModel.getParticleType() == SchemaParticle.SEQUENCE;
        boolean mapAndSumValid = true;
        // Schema Component Constraint: Particle Derivation OK (Sequence:Choice -- MapAndSum)
        // For a sequence group particle to be a �valid restriction� of a choice group particle all of the following
        // must be true:
        // 1 There is a complete functional mapping from the particles in the {particles} of R to the particles in the
        // {particles} of B such that each particle in the {particles} of R is a �valid restriction� of the particle in
        // the {particles} of B it maps to as defined by Particle Valid (Restriction) (�3.9.6).
        // interpretation:  each particle child in derived should have a match in base.
        // 2 The pair consisting of the product of the {min occurs} of R and the length of its {particles} and unbounded
        // if {max occurs} is unbounded otherwise the product of the {max occurs} of R and the length of its {particles}
        // is a valid restriction of B's occurrence range as defined by Occurrence Range OK (�3.9.6).
        // NOTE: This clause is in principle more restrictive than absolutely necessary, but in practice will cover
        // all the likely cases, and is much easier to specify than the fully general version.
        // NOTE: This case allows the "unfolding" of iterated disjunctions into sequences. It may be particularly useful
        // when the disjunction is an implicit one arising from the use of substitution groups.

        // Map step - for each member of the derived model's particle children search base model's particle children
        //  for match
        SchemaParticle[] derivedParticleArray = derivedModel.getParticleChildren();
        SchemaParticle[] baseParticleArray = baseModel.getParticleChildren();
        for (int i = 0; i < derivedParticleArray.length; i++) {
            SchemaParticle derivedParticle = derivedParticleArray[i];
            boolean foundMatch = false;
            for (int j = 0; j < baseParticleArray.length; j++) {
                SchemaParticle baseParticle = baseParticleArray[j];
                // recurse to check if there is a match
                if (isParticleValidRestriction(baseParticle, derivedParticle, errors, context)) {
                    // if there is a match then no need to check base particles anymore
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) {
                mapAndSumValid = false;
                errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_DERIVATION_MAP_AND_SUM$MAP,
                    new Object[] { printParticle(derivedParticle) },
                    context));
                // KHK: if we don't return false now, this error may get swallowed by an error produced below
                return false;
                //break;
            }
        }

        // Sum step
        BigInteger derivedRangeMin = derivedModel.getMinOccurs().multiply(BigInteger.valueOf(derivedModel.getParticleChildren().length));
        BigInteger derivedRangeMax = null;
        BigInteger UNBOUNDED = null;
        if (derivedModel.getMaxOccurs() == UNBOUNDED) {
            derivedRangeMax = null;
        } else {
            derivedRangeMax = derivedModel.getMaxOccurs().multiply(BigInteger.valueOf(derivedModel.getParticleChildren().length));
        }

        // Now check derivedRange (derivedRangeMin and derivedRangeMax) against base model occurrence range
        // Schema Component Constraint: Occurrence Range OK
        // For a particle's occurrence range to be a valid restriction of another's occurrence range all of the following must be true:
        // 1 Its {min occurs} is greater than or equal to the other's {min occurs}.
        // 2 one of the following must be true:
        //   2.1 The other's {max occurs} is unbounded.
        //   2.2 Both {max occurs} are numbers, and the particle's is less than or equal to the other's.

        if (derivedRangeMin.compareTo(baseModel.getMinOccurs()) < 0) {
            mapAndSumValid = false;
            errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_DERIVATION_MAP_AND_SUM$SUM_MIN_OCCURS_GTE_MIN_OCCURS,
                new Object[] { derivedRangeMin.toString(), baseModel.getMinOccurs().toString() },
                context));
        } else if (baseModel.getMaxOccurs() != UNBOUNDED && (derivedRangeMax == UNBOUNDED || derivedRangeMax.compareTo(baseModel.getMaxOccurs()) > 0)) {
            mapAndSumValid = false;
            errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_DERIVATION_MAP_AND_SUM$SUM_MAX_OCCURS_LTE_MAX_OCCURS,
                    new Object[] { derivedRangeMax == UNBOUNDED ? "unbounded" : derivedRangeMax.toString(), baseModel.getMaxOccurs().toString() },
                context));
        }

        return mapAndSumValid;
    }

    private static boolean recurseAsIfGroup(SchemaParticle baseModel, SchemaParticle derivedModel, Collection errors, XmlObject context) {
        // recurseAsIfGroup is called if:
        // base: ALL, derived: ELEMENT
        // base: CHOICE, derived: ELEMENT
        // base: SEQUENCE, derived: ELEMENT
        assert (baseModel.getParticleType() == SchemaParticle.ALL && derivedModel.getParticleType() == SchemaParticle.ELEMENT)
                || (baseModel.getParticleType() == SchemaParticle.CHOICE && derivedModel.getParticleType() == SchemaParticle.ELEMENT)
                || (baseModel.getParticleType() == SchemaParticle.SEQUENCE && derivedModel.getParticleType() == SchemaParticle.ELEMENT);
        // Schema Component Constraint: Particle Derivation OK (Elt:All/Choice/Sequence -- RecurseAsIfGroup)

        // For an element declaration particle to be a �valid restriction� of a group particle
        // (all, choice or sequence) a group particle of the variety corresponding to B's, with {min occurs} and
        // {max occurs} of 1 and with {particles} consisting of a single particle the same as the element declaration
        // must be a �valid restriction� of the group as defined by Particle Derivation OK
        // (All:All,Sequence:Sequence -- Recurse) (�3.9.6), Particle Derivation OK (Choice:Choice -- RecurseLax)
        // (�3.9.6) or Particle Derivation OK (All:All,Sequence:Sequence -- Recurse) (�3.9.6), depending on whether
        // the group is all, choice or sequence

        // interpretation:  make a fake group of the right type, with min occurs and max occurs of 1
        SchemaParticleImpl asIfPart = new SchemaParticleImpl();
        asIfPart.setParticleType(baseModel.getParticleType());
        asIfPart.setMinOccurs(BigInteger.ONE);
        asIfPart.setMaxOccurs(BigInteger.ONE);
        asIfPart.setParticleChildren(new SchemaParticle[] { derivedModel });
        
        // the recurse
        return isParticleValidRestriction(baseModel, asIfPart, errors, context); 
    }

    private static boolean recurseLax(SchemaParticle baseModel, SchemaParticle derivedModel, Collection errors, XmlObject context)  {
        // recurseLax is called if base: CHOICE, derived: CHOICE
        assert baseModel.getParticleType() == SchemaParticle.CHOICE && derivedModel.getParticleType() == SchemaParticle.CHOICE;
        boolean recurseLaxValid = true;
        //Schema Component Constraint: Particle Derivation OK (Choice:Choice -- RecurseLax)
        // For a choice group particle to be a �valid restriction� of another choice group particle all of the
        // following must be true:
        // 1 R's occurrence range is a valid restriction of B's occurrence range as defined by Occurrence
        // Range OK (�3.9.6);
        // 2 There is a complete �order-preserving� functional mapping from the particles in the {particles} of R
        // to the particles in the {particles} of B such that each particle in the {particles} of R is a
        // �valid restriction� of the particle in the {particles} of B it maps to as defined by
        // Particle Valid (Restriction) (�3.9.6).
        // NOTE: Although the �validation� semantics of a choice group does not depend on the order of its particles,
        // derived choice groups are required to match the order of their base in order to simplify
        // checking that the derivation is OK.
        // interpretation:  check derived choices for match in order, must get an in order match on a base particle,
        //                  don't need to check if base particles are skippable.  a lot like recurse

        if (!occurrenceRangeOK(baseModel, derivedModel, errors, context)) {
            return false;
        }
        // cycle thru both derived particle children and base particle children looking for matches
        //  if the derived particle does not match the base particle then base particle can be skipped

        SchemaParticle[] derivedParticleArray = derivedModel.getParticleChildren();
        SchemaParticle[] baseParticleArray = baseModel.getParticleChildren();
        int i = 0, j = 0;
        for (; i < derivedParticleArray.length && j < baseParticleArray.length;) {
            SchemaParticle derivedParticle = derivedParticleArray[i];
            SchemaParticle baseParticle = baseParticleArray[j];
            // try to match the two particles by recursing
            if (isParticleValidRestriction(baseParticle, derivedParticle, errors, context)) {
                // cool found a match, increment both indexes
                i++;
                j++;
            } else {
                // did not match, increment the base particle array index only
                // Ok, let's skip this base particle, increment base particle array index only
                j++;
            }
        }

        // ok, got to the end of one of the arrays
        // if at end of base particle array and not at the end of derived particle array then remaining derived
        //  particles must not match
        if (i < derivedParticleArray.length) {
            recurseLaxValid = false;
            //String message = "Found derived particles that are not matched in the base content model.";
            //errors.add(XmlError.forObject(formatDerivedMappingError(message, baseModel, derivedModel), context));
            errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_DERIVATION_RECURSE_LAX$MAP,
                new Object[] { printParticles(baseParticleArray, i) },
                context));
        }


        return recurseLaxValid;
    }

    private static boolean recurseUnordered(SchemaParticle baseModel, SchemaParticle derivedModel, Collection errors, XmlObject context) {
        // recurseUnorder is called when base: ALL and derived: SEQ
        assert baseModel.getParticleType() == SchemaParticle.ALL && derivedModel.getParticleType() == SchemaParticle.SEQUENCE;
        boolean recurseUnorderedValid = true;
        // Schema Component Constraint: Particle Derivation OK (Sequence:All -- RecurseUnordered)
        // For a sequence group particle to be a �valid restriction� of an all group particle all of the
        // following must be true:
        // 1 R's occurrence range is a valid restriction of B's occurrence range as defined by
        // Occurrence Range OK (�3.9.6).
        // 2 There is a complete functional mapping from the particles in the {particles} of R to the particles
        // in the {particles} of B such that all of the following must be true:
        // 2.1 No particle in the {particles} of B is mapped to by more than one of the particles in
        // the {particles} of R;
        // 2.2 Each particle in the {particles} of R is a �valid restriction� of the particle in the {particles} of B
        // it maps to as defined by Particle Valid (Restriction) (�3.9.6);
        // 2.3 All particles in the {particles} of B which are not mapped to by any particle in the {particles}
        // of R are �emptiable� as defined by Particle Emptiable (�3.9.6).
        // NOTE: Although this clause allows reordering, because of the limits on the contents of all groups the
        // checking process can still be deterministic.
        // 1, 2.2, and 2.3 are the same as recurse, so do 2.1 and then call recurse

        if (!occurrenceRangeOK(baseModel, derivedModel, errors, context)) {
            return false;
        }

        // read baseParticle array QNames into hashmap
        SchemaParticle[] baseParticles = baseModel.getParticleChildren();
        HashMap baseParticleMap = new HashMap(10);
        Object MAPPED = new Object();
        // Initialize the hashmap
        for (int i = 0; i < baseParticles.length; i++)
            baseParticleMap.put(baseParticles[i].getName(), baseParticles[i]);
        
        // go thru the sequence (derived model's children) and check off from base particle map
        SchemaParticle[] derivedParticles = derivedModel.getParticleChildren();
        for (int i = 0; i < derivedParticles.length; i++) {
            Object baseParticle = baseParticleMap.get(derivedParticles[i].getName());
            if (baseParticle == null) {
                recurseUnorderedValid = false;
                errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_DERIVATION_RECURSE_UNORDERED$MAP,
                    new Object[] { printParticle(derivedParticles[i]) }, context ));
                break;
            } else {
                // got a match
                if (baseParticle == MAPPED) {
                    // whoa, this base particle has already been matched (see 2.1 above)
                    recurseUnorderedValid = false;
                    errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_DERIVATION_RECURSE_UNORDERED$MAP_UNIQUE,
                        new Object[] { printParticle(derivedParticles[i]) }, context ));
                    break;
                } else {
                    SchemaParticle matchedBaseParticle = (SchemaParticle)baseParticle;
                    if (derivedParticles[i].getMaxOccurs() == null ||
                            derivedParticles[i].getMaxOccurs().compareTo(BigInteger.ONE) > 0) {
                        // no derived particles can have a max occurs greater than 1
                        recurseUnorderedValid = false;
                        errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_DERIVATION_RECURSE_UNORDERED$MAP_MAX_OCCURS_1,
                            new Object[] { printParticle(derivedParticles[i]), printMaxOccurs(derivedParticles[i].getMinOccurs()) },
                            context));
                        break;
                    }
                    if (!isParticleValidRestriction(matchedBaseParticle, derivedParticles[i], errors, context))
                    {
                        // already have an error
                        recurseUnorderedValid = false;
                        break;
                    }
                    // everything is cool, got a match, update to MAPPED
                    baseParticleMap.put(derivedParticles[i].getName(), MAPPED);
                }
            }
        }

        // if everything is cool so far then check to see if any base particles are not matched
        if (recurseUnorderedValid) {
            // get all the hashmap keys and loop thru looking for NOT_MAPPED
            Set baseParticleCollection = baseParticleMap.keySet();
            for (Iterator iterator = baseParticleCollection.iterator(); iterator.hasNext();) {
                QName baseParticleQName = (QName) iterator.next();
                if (baseParticleMap.get(baseParticleQName) != MAPPED && !((SchemaParticle)baseParticleMap.get(baseParticleQName)).isSkippable()) {
                    // this base particle was not mapped and is not "particle emptiable" (skippable)
                    recurseUnorderedValid = false;
                    errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_DERIVATION_RECURSE_UNORDERED$UNMAPPED_ARE_EMPTIABLE,
                        new Object[] { printParticle((SchemaParticle)baseParticleMap.get(baseParticleQName)) },
                        context));
                }
            }
        }

        return recurseUnorderedValid;
    }

    private static boolean recurse(SchemaParticle baseModel, SchemaParticle derivedModel, Collection errors, XmlObject context) {
        // recurse is called when base: ALL derived: ALL or base: SEQUENCE derived: SEQUENCE
        boolean recurseValid = true;
        // For an all or sequence group particle to be a �valid restriction� of another group particle with the same
        // {compositor} all of the following must be true:
        // 1 R's occurrence range is a valid restriction of B's occurrence range as defined by
        // Occurrence Range OK (�3.9.6).
        // 2 There is a complete �order-preserving� functional mapping from the particles in the {particles} of R to
        // the particles in the {particles} of B such that all of the following must be true:
        //   2.1 Each particle in the {particles} of R is a �valid restriction� of the particle in the {particles}
        //   of B it maps to as defined by Particle Valid (Restriction) (�3.9.6).
        //   2.2 All particles in the {particles} of B which are not mapped to by any particle in the {particles}
        //   of R are �emptiable� as defined by Particle Emptiable (�3.9.6).
        // NOTE: Although the �validation� semantics of an all group does not depend on the order of its particles,
        // derived all groups are required to match the order of their base in order to simplify checking that
        // the derivation is OK.
        // [Definition:]  A complete functional mapping is order-preserving if each particle r in the domain R maps
        // to a particle b in the range B which follows (not necessarily immediately) the particle in the range B
        // mapped to by the predecessor of r, if any, where "predecessor" and "follows" are defined with respect to
        // the order of the lists which constitute R and B.

        if (!occurrenceRangeOK(baseModel, derivedModel, errors, context)) {
            // error message is formatted in occurrencRangeOK ...
            return false;
        }
        // cycle thru both derived particle children and base particle children looking for matches
        //  if the derived particle does not match the base particle then base particle can be skipped if it is
        //  skippable (same as "particle emptiable") otherwise is an invalid restriction.
        // after the derived particles have been cycled if there are any base particles left over then they
        //  must be skippable or invalid restriction

        SchemaParticle[] derivedParticleArray = derivedModel.getParticleChildren();
        SchemaParticle[] baseParticleArray = baseModel.getParticleChildren();
        int i = 0, j = 0;
        for (; i < derivedParticleArray.length && j < baseParticleArray.length;) {
            SchemaParticle derivedParticle = derivedParticleArray[i];
            SchemaParticle baseParticle = baseParticleArray[j];
            // try to match the two particles by recursing
            if (isParticleValidRestriction(baseParticle, derivedParticle, errors, context)) {
                // cool found a match, increment both indexes
                i++;
                j++;
            } else {
                // did not match, increment the base particle array index only
                //  that's ok if the base particle is skippable
                if (baseParticle.isSkippable()) {
                    // Ok, let's skip this base particle, increment base particle array index only
                    j++;
                } else {
                    // whoa, particles are not valid restrictions and base is not skippable - ERROR
                    recurseValid = false;
                    errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_DERIVATION_RECURSE$MAP_VALID,
                        new Object[] { printParticle(derivedParticle), printParticle(derivedModel), 
                                       printParticle(baseParticle), printParticle(baseModel) },
                        context));
                    break;
                }
            }
        }

        // ok, got to the end of one of the arrays
        // if at end of base particle array and not at the end of derived particle array then remaining derived
        //  particles must not match
        if (i < derivedParticleArray.length) {
            recurseValid = false;
            errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_DERIVATION_RECURSE$MAP,
                new Object[] { printParticle(derivedModel), printParticle(baseModel), printParticles(derivedParticleArray, i) },
                context));
        } else {
            // if at end of derived particle array and not at end of base particle array then chck remaining
            //  base particles to assure they are skippable
            if (j < baseParticleArray.length) {
                ArrayList particles = new ArrayList(baseParticleArray.length);
                for (int k = j; k < baseParticleArray.length; k++) {
                    if (!baseParticleArray[k].isSkippable()) {
                        particles.add(baseParticleArray[k]);
                    }
                }
                if (particles.size() > 0)
                {
                    recurseValid = false;
                    errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_DERIVATION_RECURSE$UNMAPPED_ARE_EMPTIABLE,
                        new Object[] { printParticle(baseModel), printParticle(derivedModel), printParticles(particles)}, context));
                }
            }
        }

        return recurseValid;
    }

    private static boolean nsRecurseCheckCardinality(SchemaParticle baseModel, SchemaParticle derivedModel, Collection errors, XmlObject context) {
        // nsRecurseCheckCardinality is called when:
        // base: ANY, derived: ALL
        // base: ANY, derived: CHOICE
        // base: ANY, derived: SEQUENCE
        assert baseModel.getParticleType() == SchemaParticle.WILDCARD;
        assert (derivedModel.getParticleType() == SchemaParticle.ALL)
                || (derivedModel.getParticleType() == SchemaParticle.CHOICE)
                || (derivedModel.getParticleType() == SchemaParticle.SEQUENCE);
        boolean nsRecurseCheckCardinality = true;
        // For a group particle to be a �valid restriction� of a wildcard particle all of the following must be true:
        // 1 Every member of the {particles} of the group is a �valid restriction� of the wildcard as defined by Particle Valid (Restriction) (�3.9.6).
        //  Note:  not positive what this means.  Interpreting to mean that every particle of the group must adhere to wildcard derivation rules
        //         in a recursive manner
        //  Loop thru the children particles of the group and invoke the appropriate function to check for wildcard restriction validity
        
        // BAU - an errata should be submitted on this clause of the spec, because the
        // spec makes no sense, as the xstc particlesR013.xsd test exemplifies.
        // what we _should_ so is an "as if" on the wildcard, allowing it minOccurs="0" maxOccurs="unbounded"
        // before recursing
        SchemaParticleImpl asIfPart = new SchemaParticleImpl();
        asIfPart.setParticleType(baseModel.getParticleType());
        asIfPart.setWildcardProcess(baseModel.getWildcardProcess());
        asIfPart.setWildcardSet(baseModel.getWildcardSet());
        asIfPart.setMinOccurs(BigInteger.ZERO);
        asIfPart.setMaxOccurs(null);
        asIfPart.setTransitionRules(baseModel.getWildcardSet(), true);
        asIfPart.setTransitionNotes(baseModel.getWildcardSet(), true);

        SchemaParticle[] particleChildren = derivedModel.getParticleChildren();
        for (int i = 0; i < particleChildren.length; i++) {
            SchemaParticle particle = particleChildren[i];
            switch (particle.getParticleType()) {
                case SchemaParticle.ELEMENT:
                    // Check for valid Wildcard/Element derivation
                    nsRecurseCheckCardinality = nsCompat(asIfPart, (SchemaLocalElement) particle, errors, context);
                    break;
                case SchemaParticle.WILDCARD:
                    // Check for valid Wildcard/Wildcard derivation
                    nsRecurseCheckCardinality = nsSubset(asIfPart, particle, errors, context);
                    break;
                case SchemaParticle.ALL:
                case SchemaParticle.CHOICE:
                case SchemaParticle.SEQUENCE:
                    // Check for valid Wildcard/Group derivation
                    nsRecurseCheckCardinality = nsRecurseCheckCardinality(asIfPart, particle, errors, context);
                    break;
            }
            // If any particle is invalid then break the loop
            if (!nsRecurseCheckCardinality) {
                break;
            }
        }

        // 2 The effective total range of the group, as defined by Effective Total Range (all and sequence) (�3.8.6)
        // (if the group is all or sequence) or Effective Total Range (choice) (�3.8.6) (if it is choice) is a valid
        // restriction of B's occurrence range as defined by Occurrence Range OK (�3.9.6).

        if (nsRecurseCheckCardinality) {
            nsRecurseCheckCardinality = checkGroupOccurrenceOK(baseModel, derivedModel, errors, context);
        }

        return nsRecurseCheckCardinality;
    }

    private static boolean checkGroupOccurrenceOK(SchemaParticle baseModel, SchemaParticle derivedModel, Collection errors, XmlObject context) {
        boolean groupOccurrenceOK = true;
        BigInteger minRange = BigInteger.ZERO;
        BigInteger maxRange = BigInteger.ZERO;
        switch (derivedModel.getParticleType()) {
            case SchemaParticle.ALL:
            case SchemaParticle.SEQUENCE:
                minRange = getEffectiveMinRangeAllSeq(derivedModel);
                maxRange = getEffectiveMaxRangeAllSeq(derivedModel);
                break;
            case SchemaParticle.CHOICE:
                minRange = getEffectiveMinRangeChoice(derivedModel);
                maxRange = getEffectiveMaxRangeChoice(derivedModel);
                break;
        }

        // Check min occurs for validity
        // derived min occurs is valid if its {min occurs} is greater than or equal to the other's {min occurs}.
        if (minRange.compareTo(baseModel.getMinOccurs()) < 0) {
            groupOccurrenceOK = false;
            errors.add(XmlError.forObject(XmlErrorCodes.OCCURRENCE_RANGE$MIN_GTE_MIN,
                new Object[] { printParticle(derivedModel), printParticle(baseModel) },
                context));
        }
        // Check max occurs for validity
        // one of the following must be true:
        // The base model's {max occurs} is unbounded.
        // or both {max occurs} are numbers, and the particle's is less than or equal to the other's
        BigInteger UNBOUNDED = null;
        if (baseModel.getMaxOccurs() != UNBOUNDED) {
            if (maxRange == UNBOUNDED) {
                groupOccurrenceOK = false;
                errors.add(XmlError.forObject(XmlErrorCodes.OCCURRENCE_RANGE$MAX_LTE_MAX,
                    new Object[] { printParticle(derivedModel), printParticle(baseModel) },
                    context));
            } else {
                if (maxRange.compareTo(baseModel.getMaxOccurs()) > 0) {
                    groupOccurrenceOK = false;
                    errors.add(XmlError.forObject(XmlErrorCodes.OCCURRENCE_RANGE$MAX_LTE_MAX,
                        new Object[] { printParticle(derivedModel), printParticle(baseModel) },
                        context));
                }
            }
        }
        return groupOccurrenceOK;
    }
    
    private static BigInteger getEffectiveMaxRangeChoice(SchemaParticle derivedModel) {
        BigInteger maxRange = BigInteger.ZERO;
        BigInteger UNBOUNDED = null;
        // Schema Component Constraint: Effective Total Range (choice)
        // The effective total range of a particle whose {term} is a group whose {compositor} is choice
        // is a pair of minimum and maximum, as follows:
        // MAXIMUM
        // 1) unbounded if the {max occurs} of any wildcard or element declaration particle in the group's {particles} or
        // the maximum part of the effective total range of any of the group particles in the group's {particles} is
        // unbounded (note: seems to be the same as Max Range All or Sequence),
        // or 2) if any of those is non-zero and the {max occurs} of the particle itself is unbounded,
        // otherwise 3) the product of the particle's {max occurs} and the maximum of the {max occurs} of every
        // wildcard or element declaration particle in the group's {particles} and the *maximum* (note: this is the difference
        // between MaxRange Choice ans MaxRange All or Sequence) part of the
        // effective total range of each of the group particles in the group's {particles}
        // (or 0 if there are no {particles}).

        boolean nonZeroParticleChildFound = false;
        BigInteger maxOccursInWildCardOrElement = BigInteger.ZERO;
        BigInteger maxOccursInGroup = BigInteger.ZERO;
        SchemaParticle[] particleChildren = derivedModel.getParticleChildren();
        for (int i = 0; i < particleChildren.length; i++) {
            SchemaParticle particle = particleChildren[i];
            switch (particle.getParticleType()) {
                case SchemaParticle.WILDCARD:
                case SchemaParticle.ELEMENT:
                    // if unbounded then maxoccurs will be null
                    if (particle.getMaxOccurs() == UNBOUNDED) {
                        maxRange = UNBOUNDED;
                    } else {
                        if (particle.getIntMaxOccurs() > 0) {
                            // show tht at least one non-zero particle is found for later test
                            nonZeroParticleChildFound = true;
                            if (particle.getMaxOccurs().compareTo(maxOccursInWildCardOrElement) > 0) {
                                maxOccursInWildCardOrElement = particle.getMaxOccurs();
                            }
                        }
                    }
                    break;
                case SchemaParticle.ALL:
                case SchemaParticle.SEQUENCE:
                    maxRange = getEffectiveMaxRangeAllSeq(particle);
                    if (maxRange != UNBOUNDED) {
                        // keep highest maxoccurs found
                        if (maxRange.compareTo(maxOccursInGroup) > 0) {
                            maxOccursInGroup = maxRange;
                        }
                    }
                    break;
                case SchemaParticle.CHOICE:
                    maxRange = getEffectiveMaxRangeChoice(particle);
                    if (maxRange != UNBOUNDED) {
                        // keep highest maxoccurs found
                        if (maxRange.compareTo(maxOccursInGroup) > 0) {
                            maxOccursInGroup = maxRange;
                        }
                    }
                    break;
            }
            // if an unbounded has been found then we are done
            if (maxRange == UNBOUNDED) {
                break;
            }
        }

        // 1) unbounded if the {max occurs} of any wildcard or element declaration particle in the group's {particles} or
        // the maximum part of the effective total range of any of the group particles in the group's {particles} is
        // unbounded
        if (maxRange != UNBOUNDED) {
            // 2) if any of those is non-zero and the {max occurs} of the particle itself is unbounded
            if (nonZeroParticleChildFound && derivedModel.getMaxOccurs() == UNBOUNDED) {
                maxRange = UNBOUNDED;
            } else {
                // 3) the product of the particle's {max occurs} and the maximum of the {max occurs} of every
                // wildcard or element declaration particle in the group's {particles} and the *maximum*
                // part of the effective total range of each of the group particles in the group's {particles}
                maxRange = derivedModel.getMaxOccurs().multiply(maxOccursInWildCardOrElement.add(maxOccursInGroup));
            }
        }

        return maxRange;
    }

    private static BigInteger getEffectiveMaxRangeAllSeq(SchemaParticle derivedModel) {
        BigInteger maxRange = BigInteger.ZERO;
        BigInteger UNBOUNDED = null;
        // Schema Component Constraint: Effective Total Range (all and sequence)
        // The effective total range of a particle whose {term} is a group whose {compositor} is all or sequence is a
        // pair of minimum and maximum, as follows:
        // MAXIMUM
        // 1) unbounded if the {max occurs} of any wildcard or element declaration particle in the group's {particles} or
        // the maximum part of the effective total range of any of the group particles in the group's {particles} is
        // unbounded, or 2) if any of those is non-zero and the {max occurs} of the particle itself is unbounded, otherwise
        // 3) the product of the particle's {max occurs} and the *sum* of the {max occurs} of every wildcard or element
        // declaration particle in the group's {particles} and the maximum part of the effective total range of each of
        // the group particles in the group's {particles} (or 0 if there are no {particles}).

        boolean nonZeroParticleChildFound = false;
        BigInteger maxOccursTotal = BigInteger.ZERO;
        BigInteger maxOccursInGroup = BigInteger.ZERO;
        SchemaParticle[] particleChildren = derivedModel.getParticleChildren();
        for (int i = 0; i < particleChildren.length; i++) {
            SchemaParticle particle = particleChildren[i];
            switch (particle.getParticleType()) {
                case SchemaParticle.WILDCARD:
                case SchemaParticle.ELEMENT:
                    // if unbounded then maxoccurs will be null
                    if (particle.getMaxOccurs() == UNBOUNDED) {
                        maxRange = UNBOUNDED;
                    } else {
                        if (particle.getIntMaxOccurs() > 0) {
                            // show tht at least one non-zero particle is found for later test
                            nonZeroParticleChildFound = true;
                            maxOccursTotal = maxOccursTotal.add(particle.getMaxOccurs());
                        }
                    }
                    break;
                case SchemaParticle.ALL:
                case SchemaParticle.SEQUENCE:
                    maxRange = getEffectiveMaxRangeAllSeq(particle);
                    if (maxRange != UNBOUNDED) {
                        // keep highest maxoccurs found
                        if (maxRange.compareTo(maxOccursInGroup) > 0) {
                            maxOccursInGroup = maxRange;
                        }
                    }
                    break;
                case SchemaParticle.CHOICE:
                    maxRange = getEffectiveMaxRangeChoice(particle);
                    if (maxRange != UNBOUNDED) {
                        // keep highest maxoccurs found
                        if (maxRange.compareTo(maxOccursInGroup) > 0) {
                            maxOccursInGroup = maxRange;
                        }
                    }
                    break;
            }
            // if an unbounded has been found then we are done
            if (maxRange == UNBOUNDED) {
                break;
            }
        }

        // 1) unbounded if the {max occurs} of any wildcard or element declaration particle in the group's {particles} or
        // the maximum part of the effective total range of any of the group particles in the group's {particles} is
        // unbounded
        if (maxRange != UNBOUNDED) {
            // 2) if any of those is non-zero and the {max occurs} of the particle itself is unbounded
            if (nonZeroParticleChildFound && derivedModel.getMaxOccurs() == UNBOUNDED) {
                maxRange = UNBOUNDED;
            } else {
                // 3) the product of the particle's {max occurs} and the sum of the {max occurs} of every wildcard or element
                // declaration particle in the group's {particles} and the maximum part of the effective total range of each of
                // the group particles in the group's {particles}
                maxRange = derivedModel.getMaxOccurs().multiply(maxOccursTotal.add(maxOccursInGroup));
            }
        }

        return maxRange;

    }

    private static BigInteger getEffectiveMinRangeChoice(SchemaParticle derivedModel) {
        // Schema Component Constraint: Effective Total Range (choice)
        // The effective total range of a particle whose {term} is a group whose {compositor} is choice is a pair of
        // minimum and maximum, as follows:
        // MINIMUM
        // The product of the particle's {min occurs}
        // and the *minimum* of the {min occurs} of every wildcard or element
        // declaration particle in the group's {particles} and the minimum part of the effective total range of each of
        // the group particles in the group's {particles} (or 0 if there are no {particles}).
        SchemaParticle[] particleChildren = derivedModel.getParticleChildren();
        if (particleChildren.length == 0)
            return BigInteger.ZERO;
        BigInteger minRange = null;
        // get the minimum of every wildcard or element
        // total up the effective total range for each group
        for (int i = 0; i < particleChildren.length; i++) {
            SchemaParticle particle = particleChildren[i];
            switch (particle.getParticleType()) {
                case SchemaParticle.WILDCARD:
                case SchemaParticle.ELEMENT:
                    if (minRange == null || minRange.compareTo(particle.getMinOccurs()) > 0) {
                        minRange = particle.getMinOccurs();
                    }
                    break;
                case SchemaParticle.ALL:
                case SchemaParticle.SEQUENCE:
                    BigInteger mrs = getEffectiveMinRangeAllSeq(particle);
                    if (minRange == null || minRange.compareTo(mrs) > 0) {
                        minRange = mrs;
                    }
                    break;
                case SchemaParticle.CHOICE:
                    BigInteger mrc = getEffectiveMinRangeChoice(particle);
                    if (minRange == null || minRange.compareTo(mrc) > 0) {
                        minRange = mrc;
                    }
                    break;
            }
        }
        if (minRange == null)
            minRange = BigInteger.ZERO;

        // calculate the total
        minRange = derivedModel.getMinOccurs().multiply(minRange);
        return minRange;
    }

    private static BigInteger getEffectiveMinRangeAllSeq(SchemaParticle derivedModel) {
        BigInteger minRange = BigInteger.ZERO;
        // Schema Component Constraint: Effective Total Range (all and sequence)
        // The effective total range of a particle whose {term} is a group whose {compositor} is all or sequence is a
        // pair of minimum and maximum, as follows:
        // MINIMUM
        // The product of the particle's {min occurs}
        // and the *sum* of the {min occurs} of every wildcard or element
        // declaration particle in the group's {particles}
        // and the minimum part of the effective total range of each
        // of the group particles in the group's {particles} (or 0 if there are no {particles}).
        SchemaParticle[] particleChildren = derivedModel.getParticleChildren();
        BigInteger particleTotalMinOccurs = BigInteger.ZERO;
        for (int i = 0; i < particleChildren.length; i++) {
            SchemaParticle particle = particleChildren[i];
            switch (particle.getParticleType()) {
                case SchemaParticle.WILDCARD:
                case SchemaParticle.ELEMENT:
                    particleTotalMinOccurs = particleTotalMinOccurs.add(particle.getMinOccurs());
                    break;
                case SchemaParticle.ALL:
                case SchemaParticle.SEQUENCE:
                    particleTotalMinOccurs = particleTotalMinOccurs.add(getEffectiveMinRangeAllSeq(particle));
                    break;
                case SchemaParticle.CHOICE:
                    particleTotalMinOccurs = particleTotalMinOccurs.add(getEffectiveMinRangeChoice(particle));
                    break;
            }
        }

        minRange = derivedModel.getMinOccurs().multiply(particleTotalMinOccurs);

        return minRange;
    }

    private static boolean nsSubset(SchemaParticle baseModel, SchemaParticle derivedModel, Collection errors, XmlObject context) {
        // nsSubset is called when base: ANY, derived: ANY
        assert baseModel.getParticleType() == SchemaParticle.WILDCARD;
        assert derivedModel.getParticleType() == SchemaParticle.WILDCARD;
        boolean nsSubset = false;
        // For a wildcard particle to be a �valid restriction� of another wildcard particle all of the following must be true:
        // 1 R's occurrence range must be a valid restriction of B's occurrence range as defined by Occurrence Range OK (�3.9.6).
        if (occurrenceRangeOK(baseModel, derivedModel, errors, context)) {
            // 2 R's {namespace constraint} must be an intensional subset of B's {namespace constraint} as defined
            // by Wildcard Subset (�3.10.6).
            if (baseModel.getWildcardSet().inverse().isDisjoint(derivedModel.getWildcardSet())) {
                nsSubset = true;
            } else {
                nsSubset = false;
                errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_DERIVATION_NS_SUBST$WILDCARD_SUBSET,
                    new Object[] { printParticle(derivedModel), printParticle(baseModel) }, context));
            }
        } else {
            nsSubset = false;
            // error already produced by occurrenceRangeOK
            //errors.add(XmlError.forObject(formatNSIsNotSubsetError(baseModel, derivedModel), context));
        }


        return nsSubset;
    }

    private static boolean nsCompat(SchemaParticle baseModel, SchemaLocalElement derivedElement, Collection errors, XmlObject context) {
        // nsCompat is called when base: ANY, derived: ELEMENT
        assert baseModel.getParticleType() == SchemaParticle.WILDCARD;
        boolean nsCompat = false;
        // For an element declaration particle to be a �valid restriction� of a wildcard particle all of the following must be true:
        // 1 The element declaration's {target namespace} is �valid� with respect to the wildcard's {namespace constraint}
        // as defined by Wildcard allows Namespace Name (�3.10.4).
        if (baseModel.getWildcardSet().contains(derivedElement.getName())) {
            // 2 R's occurrence range is a valid restriction of B's occurrence range as defined by Occurrence Range OK (�3.9.6).
            if (occurrenceRangeOK(baseModel, (SchemaParticle) derivedElement, errors, context)) {
                nsCompat = true;
            } else {
                // error already produced by occurrenceRangeOK
                //errors.add(XmlError.forObject(formatOccurenceRangeMinError(baseModel, (SchemaParticle) derivedElement), context));
            }
        } else {
            nsCompat = false;
            errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_DERIVATION_NS_COMPAT$WILDCARD_VALID,
                new Object[] { printParticle((SchemaParticle)derivedElement), printParticle(baseModel) },
                context));
        }


        return nsCompat;
    }

    private static boolean nameAndTypeOK(SchemaLocalElement baseElement, SchemaLocalElement derivedElement, Collection errors, XmlObject context) {
        // nameAndTypeOK called when base: ELEMENT and derived: ELEMENT
        
        // Schema Component Constraint: Particle Restriction OK (Elt:Elt -- NameAndTypeOK)
        // 1 The declarations' {name}s and {target namespace}s are the same.
        if (!((SchemaParticle)baseElement).canStartWithElement(derivedElement.getName())) {
            errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_RESTRICTION_NAME_AND_TYPE$NAME,
                new Object[] { printParticle((SchemaParticle)derivedElement), printParticle((SchemaParticle)baseElement) }, context));
            return false;
        }
        
        // 2 Either B's {nillable} is true or R's {nillable} is false.
        if (!baseElement.isNillable() && derivedElement.isNillable()) {
            errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_RESTRICTION_NAME_AND_TYPE$NILLABLE,
                new Object[] { printParticle((SchemaParticle)derivedElement), printParticle((SchemaParticle)baseElement) }, context));
            return false;
        }
        
        // 3 R's occurrence range is a valid restriction of B's occurrence range as defined by Occurrence Range OK (�3.9.6).
        if (!occurrenceRangeOK((SchemaParticle) baseElement, (SchemaParticle) derivedElement, errors, context)) {
            // error already produced
            return false;
        }
        
        // 4 either B's declaration's {value constraint} is absent, or is not fixed,
        // or R's declaration's {value constraint} is fixed with the same value.
        if (!checkFixed(baseElement, derivedElement, errors, context))
        {
            // error already produced
            return false;
        }
        
        // 5 R's declaration's {identity-constraint definitions} is a subset of B's declaration's {identity-constraint definitions}, if any.
        if (!checkIdentityConstraints(baseElement, derivedElement, errors, context))
        {
            // error already produced
            return false;
        }
        
        // 7 R's {type definition} is validly derived given {extension, list, union} from B's {type definition} as
        // defined by Type Derivation OK (Complex) (�3.4.6) or Type Derivation OK (Simple) (�3.14.6), as appropriate.
        if (!typeDerivationOK(baseElement.getType(), derivedElement.getType(), errors, context))
        {
            // error already produced
            return false;
        }
        
        // 6 R's declaration's {disallowed substitutions} is a superset of B's declaration's {disallowed substitutions}.
        if (!blockSetOK(baseElement, derivedElement, errors, context))
        {
            // error already produced
            return false;    
        }

        return true;
    }
    
    private static boolean blockSetOK(SchemaLocalElement baseElement, SchemaLocalElement derivedElement, Collection errors, XmlObject context)
    {
        if (baseElement.blockRestriction() && !derivedElement.blockRestriction())
        {
            errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_RESTRICTION_NAME_AND_TYPE$DISALLOWED_SUBSTITUTIONS,
                new Object[] { printParticle((SchemaParticle)derivedElement), "restriction", printParticle((SchemaParticle)baseElement) },
                context));
            return false;
        }
        if (baseElement.blockExtension() && !derivedElement.blockExtension())
        {
            errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_RESTRICTION_NAME_AND_TYPE$DISALLOWED_SUBSTITUTIONS,
                new Object[] { printParticle((SchemaParticle)derivedElement), "extension", printParticle((SchemaParticle)baseElement) },
                context));
            return false;
        }
        if (baseElement.blockSubstitution() && !derivedElement.blockSubstitution())
        {
            errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_RESTRICTION_NAME_AND_TYPE$DISALLOWED_SUBSTITUTIONS,
                new Object[] { printParticle((SchemaParticle)derivedElement), "substitution", printParticle((SchemaParticle)baseElement) },
                context));
            return false;
        }
        return true;    
    }

    private static boolean typeDerivationOK(SchemaType baseType, SchemaType derivedType, Collection errors, XmlObject context){
        boolean typeDerivationOK = false;
        // 1 If B and D are not the same type definition, then the {derivation method} of D must not be in the subset.
        // 2 One of the following must be true:
        // 2.1 B and D must be the same type definition.
        // 2.2 B must be D's {base type definition}.
        // 2.3 All of the following must be true:
        // 2.3.1 D's {base type definition} must not be the �ur-type definition�.
        // 2.3.2 The appropriate case among the following must be true:
        // 2.3.2.1 If D's {base type definition} is complex, then it must be validly derived from B given the subset as defined by this constraint.
        // 2.3.2.2 If D's {base type definition} is simple, then it must be validly derived from B given the subset as defined in Type Derivation OK (Simple) (�3.14.6).
        //   This line will check if derivedType is a subType of baseType (should handle all of the 2.xx checks above)
        if (baseType.isAssignableFrom(derivedType)) {
            // Ok derived type is subtype but need to make sure that all of the derivations between the two types are by
            // Restriction.
            typeDerivationOK = checkAllDerivationsForRestriction(baseType, derivedType, errors, context);
        } else {
            // derived type is not a sub-type of base type
            typeDerivationOK = false;
            errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_RESTRICTION_NAME_AND_TYPE$TYPE_VALID,
                new Object[] { printType(derivedType), printType(baseType) }, context));
        }

        return typeDerivationOK;
    }

    private static boolean checkAllDerivationsForRestriction(SchemaType baseType, SchemaType derivedType, Collection errors, XmlObject context) {
        boolean allDerivationsAreRestrictions = true;
        SchemaType currentType = derivedType;

        // XMLBEANS-66: if baseType is a union, check restriction is of one of the constituant types
        Set possibleTypes = null;
        if (baseType.getSimpleVariety() == SchemaType.UNION)
            possibleTypes = new HashSet(Arrays.asList(baseType.getUnionConstituentTypes()));

        // run up the types hierarchy from derived Type to base Type and make sure that all are derived by
        //   restriction.  If any are not then this is not a valid restriction.
        while (!baseType.equals(currentType) &&
            possibleTypes != null && !possibleTypes.contains(currentType)) {
            if (currentType.getDerivationType() == SchemaType.DT_RESTRICTION) {
                currentType = currentType.getBaseType();
            } else {
                allDerivationsAreRestrictions = false;
                errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_RESTRICTION_NAME_AND_TYPE$TYPE_RESTRICTED,
                    new Object[] { printType(derivedType), printType(baseType), printType(currentType) }, context));
                break;
            }
        }
        return allDerivationsAreRestrictions;
    }

    private static boolean checkIdentityConstraints(SchemaLocalElement baseElement, SchemaLocalElement derivedElement, Collection errors, XmlObject context) {
        // 5 R's declaration's {identity-constraint definitions} is a subset of B's declaration's {identity-constraint definitions}, if any.
        boolean identityConstraintsOK = true;

        SchemaIdentityConstraint[] baseConstraints = baseElement.getIdentityConstraints();
        SchemaIdentityConstraint[] derivedConstraints = derivedElement.getIdentityConstraints();
        // cycle thru derived's identity constraints and check each to assure they in the array of base constraints
        for (int i = 0; i < derivedConstraints.length; i++) {
            SchemaIdentityConstraint derivedConstraint = derivedConstraints[i];
            if (checkForIdentityConstraintExistence(baseConstraints, derivedConstraint)) {
                identityConstraintsOK = false;
                errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_RESTRICTION_NAME_AND_TYPE$IDENTITY_CONSTRAINTS,
                    new Object[] { printParticle((SchemaParticle)derivedElement), printParticle((SchemaParticle)baseElement) },
                    context));
                break;
            }
        }
        return identityConstraintsOK;
    }

    private static boolean checkForIdentityConstraintExistence(SchemaIdentityConstraint[] baseConstraints, SchemaIdentityConstraint derivedConstraint) {
        // spin thru the base identity constraints check to see if derived constraint exists
        boolean identityConstraintExists = false;
        for (int i = 0; i < baseConstraints.length; i++) {
            SchemaIdentityConstraint baseConstraint = baseConstraints[i];
            if (baseConstraint.getName().equals(derivedConstraint.getName())) {
                identityConstraintExists = true;
                break;
            }
        }
        return identityConstraintExists;
    }


    private static boolean checkFixed(SchemaLocalElement baseModel, SchemaLocalElement derivedModel, Collection errors, XmlObject context) {
        // 4 either B's declaration's {value constraint} is absent, or is not fixed,
        // or R's declaration's {value constraint} is fixed with the same value.
        boolean checkFixed = false;
        if (baseModel.isFixed()) {
            if (baseModel.getDefaultText().equals(derivedModel.getDefaultText())) {
                //  R's declaration's {value constraint} is fixed with the same value.
                checkFixed = true;
            } else {
                // The derived element has a fixed value that is different than the base element
                errors.add(XmlError.forObject(XmlErrorCodes.PARTICLE_RESTRICTION_NAME_AND_TYPE$FIXED,
                    new Object[] { printParticle((SchemaParticle)derivedModel), derivedModel.getDefaultText(),
                                   printParticle((SchemaParticle)baseModel), baseModel.getDefaultText() },
                    context));
                checkFixed = false;
            }
        } else {
            //  B's declaration's {value constraint} is absent, or is not fixed,
            checkFixed = true;
        }
        return checkFixed;
    }

    private static boolean occurrenceRangeOK(SchemaParticle baseParticle, SchemaParticle derivedParticle, Collection errors, XmlObject context) {
        boolean occurrenceRangeOK = false;
        // Note: in the following comments (from the schema spec) other is the baseModel
        // 1 Its {min occurs} is greater than or equal to the other's {min occurs}.
        if (derivedParticle.getMinOccurs().compareTo(baseParticle.getMinOccurs()) >= 0) {
            // 2 one of the following must be true:
            // 2.1 The other's {max occurs} is unbounded.
            if (baseParticle.getMaxOccurs() == null) {
                occurrenceRangeOK = true;
            } else {
                // 2.2 Both {max occurs} are numbers, and the particle's is less than or equal to the other's.
                if (derivedParticle.getMaxOccurs() != null && baseParticle.getMaxOccurs() != null &&
                        derivedParticle.getMaxOccurs().compareTo(baseParticle.getMaxOccurs()) <= 0) {
                    occurrenceRangeOK = true;
                } else {
                    occurrenceRangeOK = false;
                    errors.add(XmlError.forObject(XmlErrorCodes.OCCURRENCE_RANGE$MAX_LTE_MAX,
                        new Object[] { printParticle(derivedParticle), printMaxOccurs(derivedParticle.getMaxOccurs()),
                                       printParticle(baseParticle), printMaxOccurs(baseParticle.getMaxOccurs()) },
                        context));
                }
            }
        } else {
            occurrenceRangeOK = false;
            errors.add(XmlError.forObject(XmlErrorCodes.OCCURRENCE_RANGE$MIN_GTE_MIN,
                new Object[] { printParticle(derivedParticle), derivedParticle.getMinOccurs().toString(),
                               printParticle(baseParticle), baseParticle.getMinOccurs().toString() },
                context));
        }
        return occurrenceRangeOK;
    }

    private static String printParticles(List parts)
    {
        return printParticles((SchemaParticle[])parts.toArray(new SchemaParticle[parts.size()]));
    }
    
    private static String printParticles(SchemaParticle[] parts)
    {
        return printParticles(parts, 0, parts.length);
    }
    
    private static String printParticles(SchemaParticle[] parts, int start)
    {
        return printParticles(parts, start, parts.length);
    }
    
    private static String printParticles(SchemaParticle[] parts, int start, int end)
    {
        StringBuffer buf = new StringBuffer(parts.length * 30);
        for (int i = start; i < end; )
        {
            buf.append(printParticle(parts[i]));
            if (++i != end)
                buf.append(", ");
        }
        return buf.toString();
    }
    
    private static String printParticle(SchemaParticle part)
    {
        switch (part.getParticleType()) {
            case SchemaParticle.ALL:
                return "<all>";
            case SchemaParticle.CHOICE:
                return "<choice>";
            case SchemaParticle.ELEMENT:
                return "<element name=\"" + QNameHelper.pretty(part.getName()) + "\">";
            case SchemaParticle.SEQUENCE:
                return "<sequence>";
            case SchemaParticle.WILDCARD:
                return "<any>";
            default :
                return "??";
        }
    }
    
    private static String printMaxOccurs(BigInteger bi)
    {
        if (bi == null)
            return "unbounded";
        return bi.toString();
    }
    
    private static String printType(SchemaType type)
    {
        if (type.getName() != null)
            return QNameHelper.pretty(type.getName());
        return type.toString();
    }

    private static void checkSubstitutionGroups(SchemaGlobalElement[] elts)
    {
        StscState state = StscState.get();

        for (int i = 0 ; i < elts.length ; i++)
        {
            SchemaGlobalElement elt = elts[i];
            SchemaGlobalElement head = elt.substitutionGroup();

            if (head != null)
            {
                SchemaType headType = head.getType();
                SchemaType tailType = elt.getType();
                XmlObject parseTree = ((SchemaGlobalElementImpl)elt)._parseObject;

                if (! headType.isAssignableFrom(tailType))
                {
                    state.error(XmlErrorCodes.ELEM_PROPERTIES$SUBSTITUTION_VALID,
                        new Object[] {QNameHelper.pretty(elt.getName()),
                                      QNameHelper.pretty(head.getName())},
                        parseTree);
                }
                else if (head.finalExtension() && head.finalRestriction())
                {
                    state.error(XmlErrorCodes.ELEM_PROPERTIES$SUBSTITUTION_FINAL,
                        new Object[] {QNameHelper.pretty(elt.getName()),
                                      QNameHelper.pretty(head.getName()),
                                      "#all"}, parseTree);
                }
                else if (! headType.equals(tailType))
                {
                    if (head.finalExtension() && 
                             tailType.getDerivationType() == SchemaType.DT_EXTENSION)
                    {
                        state.error(XmlErrorCodes.ELEM_PROPERTIES$SUBSTITUTION_FINAL,
                            new Object[] {QNameHelper.pretty(elt.getName()),
                                          QNameHelper.pretty(head.getName()),
                                          "extension"}, parseTree);
                    }
                    else if (head.finalRestriction() &&
                             tailType.getDerivationType() == SchemaType.DT_RESTRICTION)
                    {
                        state.error(XmlErrorCodes.ELEM_PROPERTIES$SUBSTITUTION_FINAL,
                            new Object[] {QNameHelper.pretty(elt.getName()),
                                          QNameHelper.pretty(head.getName()),
                                          "restriction"}, parseTree);
                    }
                }
            }

        }
    }
}
