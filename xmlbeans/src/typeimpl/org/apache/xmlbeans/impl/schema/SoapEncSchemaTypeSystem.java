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

import java.io.InputStream;
import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Collections;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.SchemaAnnotation;
import org.apache.xmlbeans.SchemaAttributeGroup;
import org.apache.xmlbeans.SchemaAttributeGroup;
import org.apache.xmlbeans.SchemaAttributeModel;
import org.apache.xmlbeans.SchemaComponent;
import org.apache.xmlbeans.SchemaGlobalAttribute;
import org.apache.xmlbeans.SchemaGlobalAttribute;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaIdentityConstraint;
import org.apache.xmlbeans.SchemaLocalAttribute;
import org.apache.xmlbeans.SchemaModelGroup;
import org.apache.xmlbeans.SchemaModelGroup;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.Filer;

public class SoapEncSchemaTypeSystem extends SchemaTypeLoaderBase
    implements SchemaTypeSystem
{
    public static final String SOAPENC = "http://schemas.xmlsoap.org/soap/encoding/";
    public static final String SOAP_ARRAY = "Array";
    public static final String ARRAY_TYPE = "arrayType";
    private static final String ATTR_ID = "id";
    private static final String ATTR_HREF = "href";
    private static final String ATTR_OFFSET = "offset";

    private static final SchemaType[] EMPTY_SCHEMATYPE_ARRAY = new SchemaType[0];
    private static final SchemaGlobalElement[] EMPTY_SCHEMAELEMENT_ARRAY = new SchemaGlobalElement[0];
    private static final SchemaModelGroup[] EMPTY_SCHEMAMODELGROUP_ARRAY = new SchemaModelGroup[0];
    private static final SchemaAttributeGroup[] EMPTY_SCHEMAATTRIBUTEGROUP_ARRAY = new SchemaAttributeGroup[0];
    private static final SchemaAnnotation[] EMPTY_SCHEMAANNOTATION_ARRAY = new SchemaAnnotation[0];

    // The global builtin type system
    public static SchemaTypeSystem get()
    {   return _global; }

    private static SoapEncSchemaTypeSystem _global = new SoapEncSchemaTypeSystem();

    private SchemaTypeImpl soapArray;
    private SchemaGlobalAttributeImpl arrayType;
    private Map _handlesToObjects = new HashMap();
    private String soapArrayHandle;
    private SchemaContainer _container = new SchemaContainer(SOAPENC);

    private SoapEncSchemaTypeSystem()
    {
        // soapenc:Array
        _container.setTypeSystem(this);
        soapArray = new SchemaTypeImpl(_container, true);
        _container.addGlobalType(soapArray.getRef());
        soapArray.setName(new QName(SOAPENC, SOAP_ARRAY));
        soapArrayHandle = SOAP_ARRAY.toLowerCase() + "type";
        soapArray.setComplexTypeVariety(SchemaType.ELEMENT_CONTENT);
        soapArray.setBaseTypeRef(BuiltinSchemaTypeSystem.ST_ANY_TYPE.getRef());
        soapArray.setBaseDepth(1);
        soapArray.setDerivationType(SchemaType.DT_EXTENSION);
        soapArray.setSimpleTypeVariety(SchemaType.NOT_SIMPLE);
        SchemaParticleImpl contentModel = new SchemaParticleImpl();
        contentModel.setParticleType(SchemaParticle.SEQUENCE);
        contentModel.setMinOccurs(BigInteger.ZERO);
        contentModel.setMaxOccurs(BigInteger.ONE);
        contentModel.setTransitionRules(QNameSet.ALL, true);
        SchemaParticleImpl[] children = new SchemaParticleImpl[1];
        contentModel.setParticleChildren(children);
        SchemaParticleImpl contentModel2 = new SchemaParticleImpl();
        contentModel2.setParticleType(SchemaParticle.WILDCARD);
        contentModel2.setWildcardSet(QNameSet.ALL);
        contentModel2.setWildcardProcess(SchemaParticle.LAX);
        contentModel2.setMinOccurs(BigInteger.ZERO);
        contentModel2.setMaxOccurs(null);
        contentModel2.setTransitionRules(QNameSet.ALL, true);
        children[0] = contentModel2;

        SchemaAttributeModelImpl attrModel = new SchemaAttributeModelImpl();
        attrModel.setWildcardProcess(SchemaAttributeModel.LAX);
        HashSet excludedURI = new HashSet();
        excludedURI.add(SOAPENC);
        attrModel.setWildcardSet(QNameSet.forSets(excludedURI, null, Collections.EMPTY_SET,
                Collections.EMPTY_SET));
        SchemaLocalAttributeImpl attr = new SchemaLocalAttributeImpl();
        attr.init(new QName("", ATTR_ID), BuiltinSchemaTypeSystem.ST_ID.getRef(),
            SchemaLocalAttribute.OPTIONAL, null, null, null, false, null, null, null);
        attrModel.addAttribute(attr);
        attr = new SchemaLocalAttributeImpl();
        attr.init(new QName("", ATTR_HREF), BuiltinSchemaTypeSystem.ST_ANY_URI.getRef(),
            SchemaLocalAttributeImpl.OPTIONAL, null, null, null, false, null, null, null);
        attrModel.addAttribute(attr);
        attr = new SchemaLocalAttributeImpl();
        attr.init(new QName(SOAPENC, ARRAY_TYPE), BuiltinSchemaTypeSystem.ST_STRING.getRef(),
            SchemaLocalAttributeImpl.OPTIONAL, null, null, null, false, null, null, null);
        attrModel.addAttribute(attr);
        attr = new SchemaLocalAttributeImpl();
        attr.init(new QName(SOAPENC, ATTR_OFFSET), BuiltinSchemaTypeSystem.ST_STRING.getRef(),
            SchemaLocalAttributeImpl.OPTIONAL, null, null, null, false, null, null, null);
        attrModel.addAttribute(attr);
        soapArray.setContentModel(contentModel, attrModel, Collections.EMPTY_MAP, Collections.EMPTY_MAP, false);

        // soapenc:arrayType
        arrayType = new SchemaGlobalAttributeImpl(_container);
        _container.addGlobalAttribute(arrayType.getRef());
        arrayType.init(new QName(SOAPENC, ARRAY_TYPE), BuiltinSchemaTypeSystem.ST_STRING.getRef(),
            SchemaLocalAttributeImpl.OPTIONAL, null, null, null, false, null, null, null);
        _handlesToObjects.put(soapArrayHandle, soapArray);
        _handlesToObjects.put(ARRAY_TYPE.toLowerCase() + "attribute", arrayType);
        _container.setImmutable();
    }

    /**
     * Returns the name of this loader.
     */
    public String getName()
    {
        return "schema.typesystem.soapenc.builtin";
    }

    public SchemaType findType(QName qName)
    {
        if (SOAPENC.equals(qName.getNamespaceURI()) &&
            SOAP_ARRAY.equals(qName.getLocalPart()))
            return soapArray;
        else
            return null;
    }

    public SchemaType findDocumentType(QName qName)
    {
        return null;
    }

    public SchemaType findAttributeType(QName qName)
    {
        return null;
    }

    public SchemaGlobalElement findElement(QName qName)
    {
        return null;
    }

    public SchemaGlobalAttribute findAttribute(QName qName)
    {
        if (SOAPENC.equals(qName.getNamespaceURI()) &&
            ARRAY_TYPE.equals(qName.getLocalPart()))
            return arrayType;
        else
            return null;
    }

    public SchemaModelGroup findModelGroup(QName qName)
    {
        return null;
    }

    public SchemaAttributeGroup findAttributeGroup(QName qName)
    {
        return null;
    }

    public boolean isNamespaceDefined(String string)
    {
        return SOAPENC.equals(string);
    }

    public SchemaType.Ref findTypeRef(QName qName)
    {
        SchemaType type = findType(qName);
        return (type == null ? null : type.getRef());
    }

    public SchemaType.Ref findDocumentTypeRef(QName qName)
    {
        return null;
    }

    public SchemaType.Ref findAttributeTypeRef(QName qName)
    {
        return null;
    }

    public SchemaGlobalElement.Ref findElementRef(QName qName)
    {
        return null;
    }

    public SchemaGlobalAttribute.Ref findAttributeRef(QName qName)
    {
        SchemaGlobalAttribute attr = findAttribute(qName);
        return (attr == null ? null : attr.getRef());
    }

    public SchemaModelGroup.Ref findModelGroupRef(QName qName)
    {
        return null;
    }

    public SchemaAttributeGroup.Ref findAttributeGroupRef(QName qName)
    {
        return null;
    }

    public SchemaIdentityConstraint.Ref findIdentityConstraintRef(QName qName)
    {
        return null;
    }

    public SchemaType typeForClassname(String string)
    {
        return null;
    }

    public InputStream getSourceAsStream(String string)
    {
        return null;            // no source
    }

    /**
     * Returns the classloader used by this loader for resolving types.
     */
    public ClassLoader getClassLoader()
    {
        return SoapEncSchemaTypeSystem.class.getClassLoader();
    }

    /**
     * Describe <code>resolve</code> method here.
     *
     */
    public void resolve()
    {
                                // don't need to do anything; already resolved
    }

    /**
     * @return an array consisting of a single type
     */
    public SchemaType[] globalTypes()
    {
        return new SchemaType[] {soapArray};
    }

    public SchemaType[] documentTypes()
    {
        return EMPTY_SCHEMATYPE_ARRAY;
    }

    public SchemaType[] attributeTypes()
    {
        return EMPTY_SCHEMATYPE_ARRAY;
    }

    public SchemaGlobalElement[] globalElements()
    {
        return EMPTY_SCHEMAELEMENT_ARRAY;
    }

    public SchemaGlobalAttribute[] globalAttributes()
    {
        return new SchemaGlobalAttribute[] {arrayType};
    }

    public SchemaModelGroup[] modelGroups()
    {
        return EMPTY_SCHEMAMODELGROUP_ARRAY;
    }

    public SchemaAttributeGroup[] attributeGroups()
    {
        return EMPTY_SCHEMAATTRIBUTEGROUP_ARRAY;
    }

    public SchemaAnnotation[] annotations()
    {
        return EMPTY_SCHEMAANNOTATION_ARRAY;
    }

    /**
     * Returns the handle for the given type within this loader.
     */
    public String handleForType(SchemaType type)
    {
        if (soapArray.equals(type))
            return soapArrayHandle;
        else
            return null;
    }

    /**
     * 
     */
    public SchemaComponent resolveHandle(String string)
    {
        return (SchemaComponent) _handlesToObjects.get(string);
    }

    /**
     *
     */
    public SchemaType typeForHandle(String string)
    {
        return (SchemaType) _handlesToObjects.get(string);
    }

    /**
     * Describe <code>saveToDirectory</code> method here.
     *
     * @param file a <code>File</code> value
     */
    public void saveToDirectory(File file)
    {
        throw new UnsupportedOperationException("The builtin soap encoding schema type system cannot be saved.");
    }

    public void save(Filer filer)
    {
        throw new UnsupportedOperationException("The builtin soap encoding schema type system cannot be saved.");
    }
}
