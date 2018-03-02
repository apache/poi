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
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlErrorCodes;

import java.math.BigInteger;

import java.util.*;

import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelElement;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelAttribute;
import org.apache.xmlbeans.impl.xb.xsdschema.SimpleType;
import org.apache.xmlbeans.impl.xb.xsdschema.Attribute;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.apache.xmlbeans.impl.xb.xsdschema.KeyrefDocument.Keyref;
import javax.xml.namespace.QName;

public class StscResolver
{
    /**
     * Does a topo walk of all the types to resolve them.
     */
    public static void resolveAll()
    {
        // resolve tree of types
        StscState state = StscState.get();

        SchemaType[] documentTypes = state.documentTypes();
        for (int i = 0 ; i < documentTypes.length ; i++)
            resolveSubstitutionGroup((SchemaTypeImpl)documentTypes[i]);

        List allSeenTypes = new ArrayList();
        allSeenTypes.addAll(Arrays.asList(state.documentTypes()));
        allSeenTypes.addAll(Arrays.asList(state.attributeTypes()));
        allSeenTypes.addAll(Arrays.asList(state.redefinedGlobalTypes()));
        allSeenTypes.addAll(Arrays.asList(state.globalTypes()));

        for (int i = 0; i < allSeenTypes.size(); i++)
        {
            SchemaType gType = (SchemaType)allSeenTypes.get(i);
            resolveType((SchemaTypeImpl)gType);
            allSeenTypes.addAll(Arrays.asList(gType.getAnonymousTypes()));
        }

        // Resolve all keyref refs
        resolveIdentityConstraints();
    }

    public static boolean resolveType(SchemaTypeImpl sImpl)
    {
        if (sImpl.isResolved())
            return true;
        if (sImpl.isResolving())
        {
            StscState.get().error("Cyclic dependency error", XmlErrorCodes.CYCLIC_DEPENDENCY, sImpl.getParseObject());
            return false; // cyclic dependency error
        }
        // System.out.println("Resolving " + sImpl);

        sImpl.startResolving();

        if (sImpl.isDocumentType())
            resolveDocumentType(sImpl);
        else if (sImpl.isAttributeType())
            resolveAttributeType(sImpl);
        else if (sImpl.isSimpleType())
            StscSimpleTypeResolver.resolveSimpleType(sImpl);
        else
            StscComplexTypeResolver.resolveComplexType(sImpl);

        sImpl.finishResolving();
        // System.out.println("Finished resolving " + sImpl);
        return true;
    }

    public static boolean resolveSubstitutionGroup(SchemaTypeImpl sImpl)
    {
        assert sImpl.isDocumentType();

        if (sImpl.isSGResolved())
            return true;
        if (sImpl.isSGResolving())
        {
            StscState.get().error("Cyclic dependency error", XmlErrorCodes.CYCLIC_DEPENDENCY, sImpl.getParseObject());
            return false; // cyclic dependency error
        }

        sImpl.startResolvingSGs();

        // Resolve substitution group

        TopLevelElement elt = (TopLevelElement)sImpl.getParseObject();
        SchemaTypeImpl substitutionGroup = null;
        QName eltName = new QName(sImpl.getTargetNamespace(), elt.getName());

        // BUG: How do I tell if the type is in this compilation unit?
        if (elt.isSetSubstitutionGroup())
        {
            substitutionGroup = StscState.get().findDocumentType(elt.getSubstitutionGroup(), 
                sImpl.getChameleonNamespace(), sImpl.getTargetNamespace());

            if (substitutionGroup == null)
                StscState.get().notFoundError(elt.getSubstitutionGroup(), SchemaType.ELEMENT, elt.xgetSubstitutionGroup(), true);
                // recovery - ignore substitution group
            else if (! resolveSubstitutionGroup(substitutionGroup) )
                substitutionGroup = null;
            else
                sImpl.setSubstitutionGroup(elt.getSubstitutionGroup());
        }

        // Walk up the chain of subtitution groups adding this schematype to each head's
        // member list
        while (substitutionGroup != null)
        {

            substitutionGroup.addSubstitutionGroupMember(eltName);

            if (substitutionGroup.getSubstitutionGroup() == null)
                break;

            substitutionGroup = StscState.get().findDocumentType(
                substitutionGroup.getSubstitutionGroup(), substitutionGroup.getChameleonNamespace(), null/*no dependency added*/);

            assert substitutionGroup != null : "Could not find document type for: " + substitutionGroup.getSubstitutionGroup();

            if (! resolveSubstitutionGroup(substitutionGroup) )
                substitutionGroup = null; // cyclic dependency - no subst group

        }

        sImpl.finishResolvingSGs();
        return true;

    }

    public static void resolveDocumentType ( SchemaTypeImpl sImpl )
    {
        assert sImpl.isResolving();
        
        assert sImpl.isDocumentType();
        

        // translate the global element associated with this document type
        // and construct a content model which allows just that element
        
        List anonTypes = new ArrayList();

        SchemaGlobalElementImpl element =
            (SchemaGlobalElementImpl)
                StscTranslator.translateElement(
                    (Element) sImpl.getParseObject(),
                    sImpl.getTargetNamespace(), sImpl.isChameleon(), null, null,
                    anonTypes, sImpl );

        SchemaLocalElementImpl contentModel = null;

        if (element != null)
        {
            StscState.get().addGlobalElement( element );
                    
            contentModel = new SchemaLocalElementImpl();
        
            contentModel.setParticleType( SchemaParticle.ELEMENT );
            StscTranslator.copyGlobalElementToLocalElement( element, contentModel );
            contentModel.setMinOccurs( BigInteger.ONE );
            contentModel.setMaxOccurs( BigInteger.ONE );

            contentModel.setTransitionNotes(QNameSet.EMPTY, true);
        }

        Map elementPropertyModel =
            StscComplexTypeResolver.buildContentPropertyModelByQName(
                contentModel, sImpl );

        SchemaTypeImpl baseType = sImpl.getSubstitutionGroup() == null ?
            BuiltinSchemaTypeSystem.ST_ANY_TYPE :
            StscState.get().findDocumentType(sImpl.getSubstitutionGroup(), 
                sImpl.isChameleon() ? sImpl.getTargetNamespace() : null, null/*already added*/)
            ;

        sImpl.setBaseTypeRef( baseType.getRef() );
        sImpl.setBaseDepth( baseType.getBaseDepth() + 1 );
        sImpl.setDerivationType( SchemaType.DT_RESTRICTION );
        sImpl.setComplexTypeVariety( SchemaType.ELEMENT_CONTENT );

        sImpl.setContentModel(
            contentModel, new SchemaAttributeModelImpl(),
            elementPropertyModel, Collections.EMPTY_MAP, false );
        
        sImpl.setWildcardSummary(
            QNameSet.EMPTY, false, QNameSet.EMPTY, false );

        sImpl.setAnonymousTypeRefs( makeRefArray( anonTypes ) );



    }
    
    public static void resolveAttributeType ( SchemaTypeImpl sImpl )
    {
        assert sImpl.isResolving();

        assert sImpl.isAttributeType();
        
        List anonTypes = new ArrayList();

        SchemaGlobalAttributeImpl attribute =
            (SchemaGlobalAttributeImpl) StscTranslator.translateAttribute(
                (Attribute) sImpl.getParseObject(), sImpl.getTargetNamespace(), null,
                sImpl.isChameleon(), anonTypes, sImpl, null, false );

        SchemaAttributeModelImpl attributeModel = new SchemaAttributeModelImpl();

        if (attribute != null)
        {
            StscState.get().addGlobalAttribute( attribute );
            
            SchemaLocalAttributeImpl attributeCopy = new SchemaLocalAttributeImpl();
            StscTranslator.copyGlobalAttributeToLocalAttribute( attribute, attributeCopy );
            attributeModel.addAttribute( attributeCopy );
        }

        sImpl.setBaseTypeRef( BuiltinSchemaTypeSystem.ST_ANY_TYPE.getRef() );
        sImpl.setBaseDepth( sImpl.getBaseDepth() + 1 );
        sImpl.setDerivationType( SchemaType.DT_RESTRICTION );
        sImpl.setComplexTypeVariety( SchemaType.EMPTY_CONTENT );
        
        Map attributePropertyModel =
            StscComplexTypeResolver.buildAttributePropertyModelByQName(
                attributeModel, sImpl );

        sImpl.setContentModel(
            null, attributeModel, Collections.EMPTY_MAP, attributePropertyModel, false );

        sImpl.setWildcardSummary(
            QNameSet.EMPTY, false, QNameSet.EMPTY, false );
        
        sImpl.setAnonymousTypeRefs( makeRefArray( anonTypes ) );
    }
    
    private static SchemaType.Ref[] makeRefArray(Collection typeList)
    {
        SchemaType.Ref[] result = new SchemaType.Ref[typeList.size()];
        int j = 0;
        for (Iterator i = typeList.iterator(); i.hasNext(); j++)
            result[j] = ((SchemaType)i.next()).getRef();
        return result;
    }


    public static void resolveIdentityConstraints()
    {
        StscState state = StscState.get();
        SchemaIdentityConstraintImpl[] idcs = state.idConstraints();

        for (int i = 0 ; i < idcs.length ; i++)
        {
            if (!idcs[i].isResolved())
            {
                Keyref xsdkr = (Keyref)idcs[i].getParseObject();
                QName keyName = xsdkr.getRefer();
                SchemaIdentityConstraintImpl key = null;

                key = state.findIdConstraint(keyName, idcs[i].getChameleonNamespace(), idcs[i].getTargetNamespace());
                if (key == null)
                {
                    state.notFoundError(keyName, SchemaType.IDENTITY_CONSTRAINT, xsdkr, true);
                }
                else 
                {
                    if (key.getConstraintCategory() == SchemaIdentityConstraintImpl.CC_KEYREF)
                        state.error(XmlErrorCodes.IDENTITY_CONSTRAINT_PROPERTIES$KEYREF_REFERS_TO_KEYREF,
                            null, idcs[i].getParseObject());

                    if (key.getFields().length != idcs[i].getFields().length)
                        state.error(XmlErrorCodes.IDENTITY_CONSTRAINT_PROPERTIES$KEY_KEYREF_FIELD_COUNT_EQ,
                            null, idcs[i].getParseObject());

                    idcs[i].setReferencedKey(key.getRef());
                }
            }
        }
    }

}
