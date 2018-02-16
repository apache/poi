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

import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.QNameSetBuilder;
import org.apache.xmlbeans.SchemaAnnotation;
import org.apache.xmlbeans.SchemaAttributeModel;
import org.apache.xmlbeans.SchemaComponent;
import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.SchemaGlobalAttribute;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaLocalAttribute;
import org.apache.xmlbeans.SchemaLocalElement;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaStringEnumEntry;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeElementSequencer;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.StringEnumAbstractBase;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.InterfaceExtension;
import org.apache.xmlbeans.PrePostExtension;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.values.*;

import javax.xml.namespace.QName;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SchemaTypeImpl implements SchemaType, TypeStoreUserFactory
{
    // global types have names
    private QName _name;

    // annotation on the type
    private SchemaAnnotation _annotation;

    // compilation support
    private int _resolvePhase;
    private static final int UNRESOLVED = 0;
    private static final int RESOLVING_SGS = 1; // For document types only
    private static final int RESOLVED_SGS = 2;  // For document types only
    private static final int RESOLVING = 3;
    private static final int RESOLVED = 4;
    private static final int JAVAIZING = 5;
    private static final int JAVAIZED = 6;

    // anonymous type support
    private SchemaType.Ref _outerSchemaTypeRef;
    private volatile SchemaComponent.Ref _containerFieldRef;
    private volatile SchemaField _containerField;
    private volatile int _containerFieldCode;
    private volatile int _containerFieldIndex;
    private volatile QName[] _groupReferenceContext;
    private SchemaType.Ref[] _anonymousTyperefs;
    private boolean _isDocumentType;
    private boolean _isAttributeType;
    // private boolean _skippedAnonymousType;

    // compiletime java type support
    private boolean _isCompiled;
    private String _shortJavaName;
    private String _fullJavaName;
    private String _shortJavaImplName;
    private String _fullJavaImplName;
    private InterfaceExtension[] _interfaces;
    private PrePostExtension _prepost;

    // runtime java type support: volatile because they're cached
    private volatile Class _javaClass;
    private volatile Class _javaEnumClass;
    private volatile Class _javaImplClass;
    private volatile Constructor _javaImplConstructor;
    private volatile Constructor _javaImplConstructor2;
    private volatile boolean _implNotAvailable;
    private volatile Class _userTypeClass;
    private volatile Class _userTypeHandlerClass;

    // user data objects not persisted
    private volatile Object _userData;

    private final Object[] _ctrArgs = new Object[] { this };

    // reflective support
    private SchemaContainer _container;
    private String _filename;

    // complex content support
    private SchemaParticle _contentModel;
    private volatile SchemaLocalElement[] _localElts; // lazily computed
    private volatile Map _eltToIndexMap; // lazily computed
    private volatile Map _attrToIndexMap; // lazily computed
    private Map _propertyModelByElementName;
    private Map _propertyModelByAttributeName;
    private boolean _hasAllContent;
    private boolean _orderSensitive;
    private QNameSet _typedWildcardElements;
    private QNameSet _typedWildcardAttributes;
    private boolean _hasWildcardElements;
    private boolean _hasWildcardAttributes;
    // set of valid QNames that can be substituted for a property
    private Set _validSubstitutions = Collections.EMPTY_SET;

    // simple content support
    private int _complexTypeVariety;
    private SchemaAttributeModel _attributeModel;

    // simple type support
    private int _builtinTypeCode;

    private int _simpleTypeVariety;
    private boolean _isSimpleType;

    private SchemaType.Ref _baseTyperef; // via restriction or extension
    private int _baseDepth; // how many inheritance steps to AnyType
    private int _derivationType;

    // user type support
    private String _userTypeName;
    private String _userTypeHandler;
    
    // for complex types with simple content
    private SchemaType.Ref _contentBasedOnTyperef;

    // facets
    private XmlValueRef[] _facetArray;
    private boolean[] _fixedFacetArray;

    // fundamental facets
    private int _ordered;
    private boolean _isFinite;
    private boolean _isBounded;
    private boolean _isNumeric;

    private boolean _abs;
    private boolean _finalExt;
    private boolean _finalRest;
    private boolean _finalList;
    private boolean _finalUnion;
    private boolean _blockExt;
    private boolean _blockRest;

    // whitespace facet
    private int _whiteSpaceRule;

    // regex patterns
    private boolean _hasPatterns; // also takes into account base classes
    private org.apache.xmlbeans.impl.regex.RegularExpression[] _patterns;

    // enumerated values
    private XmlValueRef[] _enumerationValues;
    private SchemaType.Ref _baseEnumTyperef;
    private boolean _stringEnumEnsured;
    private volatile Map _lookupStringEnum;
    private volatile List _listOfStringEnum;
    private volatile Map _lookupStringEnumEntry;
    private SchemaStringEnumEntry[] _stringEnumEntries;

    // for lists only
    private SchemaType.Ref _listItemTyperef;

    // for unions only
    private boolean _isUnionOfLists;
    private SchemaType.Ref[] _unionMemberTyperefs;
    private int _anonymousUnionMemberOrdinal;
    private volatile SchemaType[] _unionConstituentTypes;
    private volatile SchemaType[] _unionSubTypes;
    private volatile SchemaType _unionCommonBaseType;

    // for atomic types only
    private SchemaType.Ref _primitiveTypeRef;

    // for decimal restrictions only
    private int _decimalSize;

    // lazy loading support
    private volatile boolean _unloaded;

    // for document types only - only valid during compilation
    private QName _sg;
    private List _sgMembers = new ArrayList();

    public boolean isUnloaded()
    {
        return _unloaded;
    }

    public void finishLoading()
    {
        _unloaded = false;
    }



    SchemaTypeImpl(SchemaContainer container)
    {
        _container = container;
    }

    SchemaTypeImpl(SchemaContainer container, boolean unloaded)
    {
        _container = container;
        _unloaded = unloaded;
        if (unloaded)
            finishQuick();
    }

    public boolean isSGResolved()
        { return _resolvePhase >= RESOLVED_SGS; }

    public boolean isSGResolving()
        { return _resolvePhase >= RESOLVING_SGS; }

    public boolean isResolved()
        { return _resolvePhase >= RESOLVED; }

    public boolean isResolving()
        { return _resolvePhase == RESOLVING; }

    public boolean isUnjavaized()
        { return _resolvePhase < JAVAIZED; }

    public boolean isJavaized()
        { return _resolvePhase == JAVAIZED; }

    public void startResolvingSGs()
    {
        if (_resolvePhase != UNRESOLVED)
            throw new IllegalStateException();
        _resolvePhase = RESOLVING_SGS;
    }

    public void finishResolvingSGs()
    {
        if (_resolvePhase != RESOLVING_SGS)
            throw new IllegalStateException();
        _resolvePhase = RESOLVED_SGS;
    }

    public void startResolving()
    {
        if ( (_isDocumentType && _resolvePhase != RESOLVED_SGS) ||
             (!_isDocumentType && _resolvePhase != UNRESOLVED))
            throw new IllegalStateException();
        _resolvePhase = RESOLVING;
    }

    public void finishResolving()
    {
        if (_resolvePhase != RESOLVING)
            throw new IllegalStateException();
        _resolvePhase = RESOLVED;
    }

    public void startJavaizing()
    {
        if (_resolvePhase != RESOLVED)
            throw new IllegalStateException();
        _resolvePhase = JAVAIZING;
    }

    public void finishJavaizing()
    {
        if (_resolvePhase != JAVAIZING)
            throw new IllegalStateException();
        _resolvePhase = JAVAIZED;
    }

    private void finishQuick()
    {
        _resolvePhase = JAVAIZED;
    }

    private void assertUnresolved()
    {
        if (_resolvePhase != UNRESOLVED && !_unloaded)
            throw new IllegalStateException();
    }

    private void assertSGResolving()
    {
        if (_resolvePhase != RESOLVING_SGS && !_unloaded)
            throw new IllegalStateException();
    }

    private void assertSGResolved()
    {
        if (_resolvePhase != RESOLVED_SGS && !_unloaded)
            throw new IllegalStateException();
    }

    private void assertResolving()
    {
        if (_resolvePhase != RESOLVING && !_unloaded)
            throw new IllegalStateException();
    }

    private void assertResolved()
    {
        if (_resolvePhase != RESOLVED && !_unloaded)
            throw new IllegalStateException();
    }

    private void assertJavaizing()
    {
        if (_resolvePhase != JAVAIZING && !_unloaded)
            throw new IllegalStateException();
    }

    public QName getName()
        { return _name; }

    public void setName(QName name)
        { assertUnresolved(); _name = name; }

    public String getSourceName()
    {
        if (_filename != null)
            return _filename;
        if (getOuterType() != null)
            return getOuterType().getSourceName();

        SchemaField field = getContainerField();
        if (field != null)
        {
            if (field instanceof SchemaGlobalElement)
                return ((SchemaGlobalElement)field).getSourceName();
            if (field instanceof SchemaGlobalAttribute)
                return ((SchemaGlobalAttribute)field).getSourceName();
        }
        return null;
    }

    public void setFilename(String filename)
        { assertUnresolved(); _filename = filename; }

    public int getComponentType()
        { return SchemaComponent.TYPE; }

    public boolean isAnonymousType()
        { return _name == null; }

    public boolean isDocumentType()
        { return _isDocumentType; }

    public boolean isAttributeType()
        { return _isAttributeType; }

    public QName getDocumentElementName()
    {
        if (_isDocumentType)
        {
            SchemaParticle sp = getContentModel();
            if (sp != null)
                return sp.getName();
        }

        return null;
    }

    public QName getAttributeTypeAttributeName()
    {
        if (_isAttributeType)
        {
            SchemaAttributeModel sam = getAttributeModel();
            if (sam != null)
            {
                SchemaLocalAttribute[] slaArray = sam.getAttributes();
                if (slaArray != null && slaArray.length > 0)
                {
                    SchemaLocalAttribute sla = slaArray[0];
                    return sla.getName();
                }
            }
        }

        return null;
    }

    public void setAnnotation(SchemaAnnotation ann)
        { assertUnresolved(); _annotation = ann; }

    public SchemaAnnotation getAnnotation()
        { return _annotation; }

    public void setDocumentType(boolean isDocument)
        { assertUnresolved(); _isDocumentType = isDocument; }

    public void setAttributeType(boolean isAttribute)
        { assertUnresolved(); _isAttributeType = isAttribute; }

    public int getContentType()
        { return _complexTypeVariety; }

    public void setComplexTypeVariety(int complexTypeVariety)
        { assertResolving(); _complexTypeVariety = complexTypeVariety; }

    public SchemaTypeElementSequencer getElementSequencer()
    {
        if (_complexTypeVariety == NOT_COMPLEX_TYPE)
            return new SequencerImpl(null);

        return new SequencerImpl(new SchemaTypeVisitorImpl(_contentModel));
    }

    /** Set the abstract and final flags for a complex type */
    void setAbstractFinal(
        boolean abs, boolean finalExt, boolean finalRest, boolean finalList, boolean finalUnion)
    {
        assertResolving();
        _abs = abs;
        _finalExt = finalExt; _finalRest = finalRest;
        _finalList = finalList; _finalUnion = finalUnion;
    }

    /** Set the final flags for a simple type */
    void setSimpleFinal(boolean finalRest, boolean finalList, boolean finalUnion)
    {
        assertResolving(); _finalRest = finalRest; _finalList = finalList ; _finalUnion = finalUnion;
    }

    void setBlock(boolean blockExt, boolean blockRest)
    {
        assertResolving(); _blockExt = blockExt ; _blockRest = blockRest;
    }

    public boolean blockRestriction()
    { return _blockRest; }

    public boolean blockExtension()
    { return _blockExt; }

    public boolean isAbstract()
    { return _abs; }

    public boolean finalExtension()
    { return _finalExt; }

    public boolean finalRestriction()
    { return _finalRest; }

    public boolean finalList()
    { return _finalList; }

    public boolean finalUnion()
    { return _finalUnion; }

    public synchronized SchemaField getContainerField()
    {
        if (_containerFieldCode != -1)
        {
            SchemaType outer = getOuterType();
            if (_containerFieldCode == 0)
                _containerField = _containerFieldRef == null ? null : (SchemaField)_containerFieldRef.getComponent();
            else if (_containerFieldCode == 1)
                _containerField = outer.getAttributeModel().getAttributes()[_containerFieldIndex];
            else
                _containerField = ((SchemaTypeImpl)outer).getLocalElementByIndex(_containerFieldIndex);
            _containerFieldCode = -1;
        }
        return _containerField;
    }

    public void setContainerField(SchemaField field)
    {
        assertUnresolved();
        _containerField = field;
        _containerFieldCode = -1;
    }

    public void setContainerFieldRef(SchemaComponent.Ref ref)
    {
        assertUnresolved();
        _containerFieldRef = ref;
        _containerFieldCode = 0;
    }

    public void setContainerFieldIndex(short code, int index)
    {
        assertUnresolved();
        _containerFieldCode = code;
        _containerFieldIndex = index;
    }

    /* package */ void setGroupReferenceContext(QName[] groupNames)
    {
        assertUnresolved();
        _groupReferenceContext = groupNames;
    }

    /* package */ QName[] getGroupReferenceContext()
    { return _groupReferenceContext; }

    public SchemaType getOuterType()
        { return _outerSchemaTypeRef == null ? null : _outerSchemaTypeRef.get(); }

    public void setOuterSchemaTypeRef(SchemaType.Ref typeref)
        { assertUnresolved(); _outerSchemaTypeRef = typeref; }

    public boolean isCompiled()
        { return _isCompiled; }

    public void setCompiled(boolean f)
        { assertJavaizing(); _isCompiled = f; }

    public boolean isSkippedAnonymousType()
    {
        SchemaType outerType = getOuterType();
        return ((outerType == null) ? false :
                (outerType.getBaseType() == this ||
                 outerType.getContentBasedOnType() == this));
    }

    public String getShortJavaName()
        { return _shortJavaName; }

    public void setShortJavaName(String name)
    {
        assertResolved();
        _shortJavaName = name;
        SchemaType outer = _outerSchemaTypeRef.get();
        while (outer.getFullJavaName() == null)
            outer = outer.getOuterType();

        _fullJavaName = outer.getFullJavaName() + "$" + _shortJavaName;
    }

    public String getFullJavaName()
        { return _fullJavaName; }

    public void setFullJavaName(String name)
    {
        assertResolved();
        _fullJavaName = name;
        int index = Math.max(_fullJavaName.lastIndexOf('$'),
                             _fullJavaName.lastIndexOf('.')) + 1;
        _shortJavaName = _fullJavaName.substring(index);
    }

    public void setShortJavaImplName(String name)
    {
        assertResolved();
        _shortJavaImplName = name;
        SchemaType outer = _outerSchemaTypeRef.get();
        while (outer.getFullJavaImplName() == null)
            outer = outer.getOuterType();

        _fullJavaImplName = outer.getFullJavaImplName() + "$" + _shortJavaImplName;
    }

    public void setFullJavaImplName(String name)
    {
        assertResolved();
        _fullJavaImplName = name;
        int index = Math.max(_fullJavaImplName.lastIndexOf('$'),
                             _fullJavaImplName.lastIndexOf('.')) + 1;
        _shortJavaImplName = _fullJavaImplName.substring(index);
    }

    public String getFullJavaImplName() { return _fullJavaImplName;}
    public String getShortJavaImplName() { return _shortJavaImplName;}

    public String getUserTypeName() 
    {
        return _userTypeName;
    }
    
    public void setUserTypeName(String userTypeName)
    {
        _userTypeName = userTypeName;
    }

    public String getUserTypeHandlerName() 
    {
        return _userTypeHandler;
    }

    public void setUserTypeHandlerName(String typeHandler) 
    {
        _userTypeHandler = typeHandler;
    }

    public void setInterfaceExtensions(InterfaceExtension[] interfaces)
    {
        assertResolved();
        _interfaces = interfaces;
    }

    public InterfaceExtension[] getInterfaceExtensions()
    {
        return _interfaces;
    }

    public void setPrePostExtension(PrePostExtension prepost)
    {
        assertResolved();
        _prepost = prepost;
    }

    public PrePostExtension getPrePostExtension()
    {
        return _prepost;
    }

    public Object getUserData()
    {   return _userData; }

    public void setUserData(Object data)
    {   _userData = data; }

    /* Only used for asserts */
    SchemaContainer getContainer()
    {   return _container; }

    void setContainer(SchemaContainer container)
    {   _container = container; }

    public SchemaTypeSystem getTypeSystem()
    {   return _container.getTypeSystem(); }

    public SchemaParticle getContentModel()
    {   return _contentModel; }

    private static void buildEltList(List eltList, SchemaParticle contentModel)
    {
        if (contentModel == null)
            return;

        switch (contentModel.getParticleType())
        {
            case SchemaParticle.ELEMENT:
                eltList.add(contentModel);
                return;
            case SchemaParticle.ALL:
            case SchemaParticle.CHOICE:
            case SchemaParticle.SEQUENCE:
                for (int i = 0; i < contentModel.countOfParticleChild(); i++)
                    buildEltList(eltList, contentModel.getParticleChild(i));
                return;
            default:
                return;
        }
    }

    private void buildLocalElts()
    {
        List eltList = new ArrayList();
        buildEltList(eltList, _contentModel);
        _localElts = (SchemaLocalElement[])eltList.toArray(new SchemaLocalElement[eltList.size()]);
    }

    public SchemaLocalElement getLocalElementByIndex(int i)
    {
        SchemaLocalElement[] elts = _localElts;
        if (elts == null)
        {
            buildLocalElts();
            elts = _localElts;
        }
        return elts[i];
    }

    public int getIndexForLocalElement(SchemaLocalElement elt)
    {
        Map localEltMap = _eltToIndexMap;
        if (localEltMap == null)
        {
            if (_localElts == null)
                buildLocalElts();
            localEltMap = new HashMap();
            for (int i = 0; i < _localElts.length; i++)
            {
                localEltMap.put(_localElts[i], new Integer(i));
            }
            _eltToIndexMap = localEltMap;
        }
        return ((Integer)localEltMap.get(elt)).intValue();
    }

    public int getIndexForLocalAttribute(SchemaLocalAttribute attr)
    {
        Map localAttrMap = _attrToIndexMap;
        if (localAttrMap == null)
        {
            localAttrMap = new HashMap();
            SchemaLocalAttribute[] attrs = this._attributeModel.getAttributes();
            for (int i = 0; i < attrs.length; i++)
            {
                localAttrMap.put(attrs[i], new Integer(i));
            }
            _attrToIndexMap = localAttrMap;
        }
        return ((Integer)localAttrMap.get(attr)).intValue();
    }

    public SchemaAttributeModel getAttributeModel()
        { return _attributeModel; }

    public SchemaProperty[] getProperties()
    {
        if (_propertyModelByElementName == null)
            return getAttributeProperties();

        if (_propertyModelByAttributeName == null)
            return getElementProperties();

        List list = new ArrayList();
        list.addAll(_propertyModelByElementName.values());
        list.addAll(_propertyModelByAttributeName.values());
        return (SchemaProperty[])list.toArray(new SchemaProperty[list.size()]);
    }

    private static final SchemaProperty[] NO_PROPERTIES = new SchemaProperty[0];

    public SchemaProperty[] getDerivedProperties()
    {
        SchemaType baseType = getBaseType();
        if (baseType == null)
            return getProperties();

        List results = new ArrayList();

        if (_propertyModelByElementName != null)
            results.addAll(_propertyModelByElementName.values());

        if (_propertyModelByAttributeName != null)
            results.addAll(_propertyModelByAttributeName.values());

        for (Iterator it = results.iterator() ; it.hasNext() ; )
        {
            SchemaProperty prop = (SchemaProperty)it.next();
            SchemaProperty baseProp = prop.isAttribute() ?
                baseType.getAttributeProperty(prop.getName()) :
                baseType.getElementProperty(prop.getName());


            // Remove the derived property from the results if it is
            // A) present in the base type and
            // B) all the details are the same (cardinality, nillability, default)

            if (baseProp != null)
            {
                if ( eq(prop.getMinOccurs(), baseProp.getMinOccurs()) &&
                     eq(prop.getMaxOccurs(), baseProp.getMaxOccurs()) &&
                     prop.hasNillable() == baseProp.hasNillable() &&
                     eq(prop.getDefaultText(), baseProp.getDefaultText()))
                {
                    it.remove();
                }

            }

        }

        return (SchemaProperty[])results.toArray(new SchemaProperty[results.size()]);
    }

    private static boolean eq(BigInteger a, BigInteger b)
    {
        if (a == null && b == null)
            return true;
        if (a== null || b == null)
            return false;
        return a.equals(b);
    }

    private static boolean eq(String a, String b)
    {
        if (a == null && b == null)
            return true;
        if (a== null || b == null)
            return false;
        return a.equals(b);
    }

    public SchemaProperty[] getElementProperties()
    {
        if (_propertyModelByElementName == null)
            return NO_PROPERTIES;

        return (SchemaProperty[])
                _propertyModelByElementName.values().toArray(new SchemaProperty[_propertyModelByElementName.size()]);
    }

    public SchemaProperty[] getAttributeProperties()
    {
        if (_propertyModelByAttributeName == null)
            return NO_PROPERTIES;

        return (SchemaProperty[])
                _propertyModelByAttributeName.values().toArray(new SchemaProperty[_propertyModelByAttributeName.size()]);
    }

    public SchemaProperty getElementProperty(QName eltName)
        { return _propertyModelByElementName == null ? null : (SchemaProperty)_propertyModelByElementName.get(eltName); }

    public SchemaProperty getAttributeProperty(QName attrName)
        { return _propertyModelByAttributeName == null ? null : (SchemaProperty)_propertyModelByAttributeName.get(attrName); }

    public boolean hasAllContent()
        { return _hasAllContent; }

    public boolean isOrderSensitive()
        { return _orderSensitive; }

    public void setOrderSensitive(boolean sensitive)
        { assertJavaizing(); _orderSensitive = sensitive; }

    public void setContentModel(
            SchemaParticle contentModel,
            SchemaAttributeModel attrModel,
            Map propertyModelByElementName,
            Map propertyModelByAttributeName,
            boolean isAll)
    {
        assertResolving();
        _contentModel = contentModel;
        _attributeModel = attrModel;
        _propertyModelByElementName = propertyModelByElementName;
        _propertyModelByAttributeName = propertyModelByAttributeName;
        _hasAllContent = isAll;


        // Add entries for each element property for substitution group members
        if (_propertyModelByElementName != null)
        {
            _validSubstitutions = new LinkedHashSet();
            Collection eltProps = _propertyModelByElementName.values();
            for (Iterator it = eltProps.iterator() ; it.hasNext() ; )
            {
                SchemaProperty prop = (SchemaProperty)it.next();
                QName[] names = prop.acceptedNames();
                for (int i = 0 ; i < names.length ; i++)
                {
                    if (! _propertyModelByElementName.containsKey(names[i]))
                        _validSubstitutions.add(names[i]);
                }
            }
        }
    }

    private boolean containsElements()
    {
        return getContentType() == ELEMENT_CONTENT ||
               getContentType() == MIXED_CONTENT;
    }

    public boolean hasAttributeWildcards()
    {
        return _hasWildcardAttributes;
    }

    public boolean hasElementWildcards()
    {
        return _hasWildcardElements;
    }

    public boolean isValidSubstitution(QName name)
    {
        return _validSubstitutions.contains(name);
    }

    public SchemaType getElementType(QName eltName, QName xsiType, SchemaTypeLoader wildcardTypeLoader)
    {
        if (isSimpleType() || !containsElements() || isNoType())
            return BuiltinSchemaTypeSystem.ST_NO_TYPE;

        SchemaType type = null;
        SchemaProperty prop = (SchemaProperty)_propertyModelByElementName.get(eltName);
        if (prop != null)
        {
            type = prop.getType();
        }
        else
        {
            if (wildcardTypeLoader == null)
                return BuiltinSchemaTypeSystem.ST_NO_TYPE;

            if (_typedWildcardElements.contains(eltName) ||
                _validSubstitutions.contains(eltName))
            {
                SchemaGlobalElement elt = wildcardTypeLoader.findElement(eltName);
                if (elt == null)
                    return BuiltinSchemaTypeSystem.ST_NO_TYPE;
                // According to http://www.w3.org/TR/xmlschema-1/#key-lva,
                // the line above should return ST_ANY_TYPE.
                type = elt.getType();
            }
            else
            {
                // Substitution groups
                // Actually, better not enable this yet
                /*SchemaGlobalElement elt = wildcardTypeLoader.findElement(eltName);
                SchemaGlobalElement sghead = elt == null ? null : elt.substitutionGroup();
                while (sghead != null)
                {
                    prop = (SchemaProperty)_propertyModelByElementName.get(sghead.getName());
                    if (prop != null)
                    {
                        type = elt.getType();
                        break;
                    }
                    sghead = sghead.substitutionGroup();
                }
                */
                if (type == null)
                    return BuiltinSchemaTypeSystem.ST_NO_TYPE;
            }
        }

        if (xsiType != null && wildcardTypeLoader != null)
        {
            SchemaType itype = wildcardTypeLoader.findType(xsiType);

            // NOTE: a previous version of XMLBeans used ST_NO_TYPE if the
            // xsiType was not derived from 'type', but this results in a
            // ClassCastException.  Instead we ignore xsi:type if it's not
            // found or a derived type.
            if (itype != null && type.isAssignableFrom(itype)) {
                return itype;
            }
        }

        return type;
    }

    public SchemaType getAttributeType(QName attrName, SchemaTypeLoader wildcardTypeLoader)
    {
        if (isSimpleType() || isNoType())
            return BuiltinSchemaTypeSystem.ST_NO_TYPE;

        if (isURType())
            return BuiltinSchemaTypeSystem.ST_ANY_SIMPLE;

        SchemaProperty prop = (SchemaProperty)_propertyModelByAttributeName.get(attrName);
        if (prop != null)
            return prop.getType();

        if (!_typedWildcardAttributes.contains(attrName) || wildcardTypeLoader == null)
            return BuiltinSchemaTypeSystem.ST_NO_TYPE;
        // For symmetry with the element case (as well as with URType), perhaps
        // the above line should be returning ST_ANY_SIMPLE

        SchemaGlobalAttribute attr = wildcardTypeLoader.findAttribute(attrName);
        if (attr == null)
            return BuiltinSchemaTypeSystem.ST_NO_TYPE;
        return attr.getType();
    }

    /* These two methods, createElementType and getElementType have to stay
     * synchronized, because they create an XmlObject and return the type
     * for that object, respectively. But since they do slightly different
     * things, they can't be refactored to share code, so exercise caution
     */
    public XmlObject createElementType(QName eltName, QName xsiType, SchemaTypeLoader wildcardTypeLoader)
    {
        SchemaType type = null;
        SchemaProperty prop = null;
        if (isSimpleType() || !containsElements() || isNoType())
        {
            type = BuiltinSchemaTypeSystem.ST_NO_TYPE;
        }
        else
        {
            prop = (SchemaProperty)_propertyModelByElementName.get(eltName);
            if (prop != null)
            {
                type = prop.getType();
            }
            else if (_typedWildcardElements.contains(eltName) ||
                     _validSubstitutions.contains(eltName))
            {
                SchemaGlobalElement elt = wildcardTypeLoader.findElement(eltName);
                if (elt != null)
                {
                    type = elt.getType();
                    SchemaType docType = wildcardTypeLoader.findDocumentType(eltName);
                    if (docType != null)
                        prop = docType.getElementProperty(eltName);
                }
                else
                    type = BuiltinSchemaTypeSystem.ST_NO_TYPE;
            }
            else
            {
                // Check if the requested element isn't by any chance part of a
                // substitution group headed by one of the declared elements
                // Better not enable this yet
                /*
                SchemaGlobalElement elt = wildcardTypeLoader.findElement(eltName);
                SchemaGlobalElement sghead = elt == null ? null : elt.substitutionGroup();
                while (sghead != null)
                {
                    if (_propertyModelByElementName.containsKey(sghead.getName()))
                    {
                        type = elt.getType();
                        SchemaType docType = wildcardTypeLoader.findDocumentType(elt.getName());
                        if (docType != null)
                            prop = docType.getElementProperty(elt.getName());
                        break;
                    }
                    sghead = sghead.substitutionGroup();
                }
                */
                if (type == null)
                    type = BuiltinSchemaTypeSystem.ST_NO_TYPE;
            }

            if (xsiType != null)
            {
                SchemaType itype = wildcardTypeLoader.findType(xsiType);

                // NOTE: a previous version of XMLBeans used ST_NO_TYPE if the
                // xsiType was not derived from 'type', but this results in a
                // ClassCastException.  Instead we ignore xsi:type if it's not
                // found or a derived type.
                if (itype != null && type.isAssignableFrom(itype)) {
                    type = itype;
                }
            }
        }

        if (type != null)
            return ((SchemaTypeImpl)type).createUnattachedNode(prop);
        return null;
    }

    public XmlObject createAttributeType(QName attrName, SchemaTypeLoader wildcardTypeLoader)
    {
        SchemaTypeImpl type = null;
        SchemaProperty prop = null;
        if (isSimpleType() || isNoType())
        {
            type = BuiltinSchemaTypeSystem.ST_NO_TYPE;
        }
        else if (isURType())
        {
            type = BuiltinSchemaTypeSystem.ST_ANY_SIMPLE;
        }
        else
        {
            prop = (SchemaProperty)_propertyModelByAttributeName.get(attrName);
            if (prop != null)
            {
                type = (SchemaTypeImpl)prop.getType();
            }
            else if (!_typedWildcardAttributes.contains(attrName))
            {
                type = BuiltinSchemaTypeSystem.ST_NO_TYPE;
            }
            else
            {
                SchemaGlobalAttribute attr = wildcardTypeLoader.findAttribute(attrName);
                if (attr != null)
                    type = (SchemaTypeImpl)attr.getType();
                else
                    type = BuiltinSchemaTypeSystem.ST_NO_TYPE;
            }
        }

        if (type != null)
            return type.createUnattachedNode(prop);

        return null;
    }

    public void setWildcardSummary(QNameSet elementSet, boolean haswcElt, QNameSet attributeSet, boolean haswcAtt)
    {
        assertResolving(); _typedWildcardElements = elementSet; _hasWildcardElements = haswcElt; _typedWildcardAttributes = attributeSet; _hasWildcardAttributes = haswcAtt;
    }

    public SchemaType[] getAnonymousTypes()
    {
        SchemaType[] result = new SchemaType[_anonymousTyperefs.length];
        for (int i = 0; i < result.length; i++)
            result[i] = _anonymousTyperefs[i].get();
        return result;
    }

    public void setAnonymousTypeRefs(SchemaType.Ref[] anonymousTyperefs)
    {
        _anonymousTyperefs = anonymousTyperefs;
    }


    private static SchemaType[] staCopy(SchemaType[] a)
    {
        if (a == null)
            return null;

        SchemaType[] result = new SchemaType[a.length];
        System.arraycopy(a, 0, result, 0, a.length);
        return result;
    }

    private static boolean[] boaCopy(boolean[] a)
    {
        if (a == null)
            return null;

        boolean[] result = new boolean[a.length];
        System.arraycopy(a, 0, result, 0, a.length);
        return result;
    }



    public void setSimpleTypeVariety(int variety)
        { assertResolving(); _simpleTypeVariety = variety; }

    public int getSimpleVariety()
        { return _simpleTypeVariety; }

    public boolean isURType()
        { return _builtinTypeCode == BTC_ANY_TYPE || _builtinTypeCode == BTC_ANY_SIMPLE; }

    public boolean isNoType()
        { return this == BuiltinSchemaTypeSystem.ST_NO_TYPE; }

    public boolean isSimpleType()
        { return _isSimpleType; }

    public void setSimpleType(boolean f)
        { assertUnresolved(); _isSimpleType = f; }

    public boolean isUnionOfLists()
        { return _isUnionOfLists; }

    public void setUnionOfLists(boolean f)
        { assertResolving(); _isUnionOfLists = f; }

    public SchemaType getPrimitiveType()
        { return _primitiveTypeRef == null ? null : _primitiveTypeRef.get(); }

    public void setPrimitiveTypeRef(SchemaType.Ref typeref)
        { assertResolving(); _primitiveTypeRef = typeref; }

    public int getDecimalSize()
        { return _decimalSize; }

    public void setDecimalSize(int bits)
        { assertResolving(); _decimalSize = bits; }

    public SchemaType getBaseType()
        { return _baseTyperef == null ? null : _baseTyperef.get(); }

    public void setBaseTypeRef(SchemaType.Ref typeref)
        { assertResolving(); _baseTyperef = typeref; }

    public int getBaseDepth()
        { return _baseDepth; }

    public void setBaseDepth(int depth)
        { assertResolving(); _baseDepth = depth; }

    public SchemaType getContentBasedOnType()
        { return _contentBasedOnTyperef == null ? null : _contentBasedOnTyperef.get(); }

    public void setContentBasedOnTypeRef(SchemaType.Ref typeref)
        { assertResolving(); _contentBasedOnTyperef = typeref; }

    public int getDerivationType()
        { return _derivationType; }

    public void setDerivationType(int type)
        { assertResolving(); _derivationType = type; }

    public SchemaType getListItemType()
        { return _listItemTyperef == null ? null : _listItemTyperef.get(); }

    public void setListItemTypeRef(SchemaType.Ref typeref)
        { assertResolving(); _listItemTyperef = typeref; }

    public SchemaType[] getUnionMemberTypes()
    {
        SchemaType[] result = new SchemaType[_unionMemberTyperefs ==null ? 0 : _unionMemberTyperefs.length];
        for (int i = 0; i < result.length; i++)
            result[i] = _unionMemberTyperefs[i].get();
        return result;
    }

    public void setUnionMemberTypeRefs(SchemaType.Ref[] typerefs)
        { assertResolving(); _unionMemberTyperefs = typerefs; }

    public int getAnonymousUnionMemberOrdinal()
        { return _anonymousUnionMemberOrdinal; }

    public void setAnonymousUnionMemberOrdinal(int i)
        { assertUnresolved(); _anonymousUnionMemberOrdinal = i; }

    public synchronized SchemaType[] getUnionConstituentTypes()
    {
        if (_unionCommonBaseType == null)
            computeFlatUnionModel();
        return staCopy(_unionConstituentTypes);
    }

    private void setUnionConstituentTypes(SchemaType[] types)
        { _unionConstituentTypes = types; }

    public synchronized SchemaType[] getUnionSubTypes()
    {
        if (_unionCommonBaseType == null)
            computeFlatUnionModel();
        return staCopy(_unionSubTypes);
    }

    private void setUnionSubTypes(SchemaType[] types)
        { _unionSubTypes = types; }

    public synchronized SchemaType getUnionCommonBaseType()
    {
        if (_unionCommonBaseType == null)
            computeFlatUnionModel();
        return _unionCommonBaseType;
    }

    private void setUnionCommonBaseType(SchemaType type)
        { _unionCommonBaseType = type; }

    private void computeFlatUnionModel()
    {
        if (getSimpleVariety() != SchemaType.UNION)
            throw new IllegalStateException("Operation is only supported on union types");
        Set constituentMemberTypes = new LinkedHashSet();
        Set allSubTypes = new LinkedHashSet();
        SchemaType commonBaseType = null;

        allSubTypes.add(this);

        for (int i = 0; i < _unionMemberTyperefs.length; i++)
        {
            SchemaTypeImpl mImpl = (SchemaTypeImpl)_unionMemberTyperefs[i].get();

            switch (mImpl.getSimpleVariety())
            {
                case SchemaType.LIST:
                    constituentMemberTypes.add(mImpl);
                    allSubTypes.add(mImpl);
                    commonBaseType = mImpl.getCommonBaseType(commonBaseType);
                    break;
                case SchemaType.UNION:
                    constituentMemberTypes.addAll(Arrays.asList(mImpl.getUnionConstituentTypes()));
                    allSubTypes.addAll(Arrays.asList(mImpl.getUnionSubTypes()));
                    SchemaType otherCommonBaseType = mImpl.getUnionCommonBaseType();
                    if (otherCommonBaseType != null)
                        commonBaseType = otherCommonBaseType.getCommonBaseType(commonBaseType);
                    break;
                case SchemaType.ATOMIC:
                    constituentMemberTypes.add(mImpl);
                    allSubTypes.add(mImpl);
                    commonBaseType = mImpl.getCommonBaseType(commonBaseType);
                    break;
                default:
                    assert(false);
            }
        }

        setUnionConstituentTypes((SchemaType[])
                constituentMemberTypes.toArray(StscState.EMPTY_ST_ARRAY));
        setUnionSubTypes((SchemaType[])
                allSubTypes.toArray(StscState.EMPTY_ST_ARRAY));
        setUnionCommonBaseType(commonBaseType);
    }

    public QName getSubstitutionGroup()
        { return _sg; }

    public void setSubstitutionGroup(QName sg)
        { assertSGResolving(); _sg = sg; }

    public void addSubstitutionGroupMember(QName member)
        { assertSGResolved(); _sgMembers.add(member); }

    public QName[] getSubstitutionGroupMembers()
    {
        QName[] result = new QName[_sgMembers.size()];
        return (QName[])_sgMembers.toArray(result);
    }

    public int getWhiteSpaceRule()
        { return _whiteSpaceRule; }

    public void setWhiteSpaceRule(int ws)
        { assertResolving(); _whiteSpaceRule = ws; }

    public XmlAnySimpleType getFacet(int facetCode)
    {
        if (_facetArray == null)
            return null;
        XmlValueRef ref = _facetArray[facetCode];
        if (ref == null)
            return null;
        return ref.get();
    }

    public boolean isFacetFixed(int facetCode)
    {
        return _fixedFacetArray[facetCode];
    }

    public XmlAnySimpleType[] getBasicFacets()
    {
        XmlAnySimpleType[] result = new XmlAnySimpleType[SchemaType.LAST_FACET + 1];
        for (int i = 0; i <= SchemaType.LAST_FACET; i++)
        {
            result[i] = getFacet(i);
        }
        return result;
    }

    public boolean[] getFixedFacets()
    {
        return boaCopy(_fixedFacetArray);
    }

    public void setBasicFacets(XmlValueRef[] values, boolean[] fixed)
    {
        assertResolving();
        _facetArray = values;
        _fixedFacetArray = fixed;
    }

    public int ordered()
        { return _ordered; }

    public void setOrdered(int ordering)
        { assertResolving(); _ordered = ordering; }

    public boolean isBounded()
        { return _isBounded; }

    public void setBounded(boolean f)
        { assertResolving(); _isBounded = f; }

    public boolean isFinite()
        { return _isFinite; }

    public void setFinite(boolean f)
        { assertResolving(); _isFinite = f; }

    public boolean isNumeric()
        { return _isNumeric; }

    public void setNumeric(boolean f)
        { assertResolving(); _isNumeric = f; }


    public boolean hasPatternFacet()
        { return _hasPatterns; }

    public void setPatternFacet(boolean hasPatterns)
        { assertResolving(); _hasPatterns = hasPatterns; }

    public boolean matchPatternFacet(String s)
    {
        if (!_hasPatterns)
            return true;

        if (_patterns != null && _patterns.length > 0)
        {
            int i;
            for (i = 0; i < _patterns.length; i++)
            {
                if (_patterns[i].matches(s))
                    break;
            }
            if (i >= _patterns.length)
                return false;
        }

        return getBaseType().matchPatternFacet(s);
    }

    public String[] getPatterns()
    {
        if (_patterns == null)
            return new String[0];
        String[] patterns = new String[_patterns.length];
        for (int i=0; i< _patterns.length; i++)
            patterns[i] = _patterns[i].getPattern();
        return patterns;
    }

    public org.apache.xmlbeans.impl.regex.RegularExpression[] getPatternExpressions()
    {
        if (_patterns == null)
            return new org.apache.xmlbeans.impl.regex.RegularExpression[0];
        org.apache.xmlbeans.impl.regex.RegularExpression[] result = new org.apache.xmlbeans.impl.regex.RegularExpression[_patterns.length];
        System.arraycopy(_patterns, 0, result, 0, _patterns.length);
        return result;
    }

    public void setPatterns(org.apache.xmlbeans.impl.regex.RegularExpression[] list)
        { assertResolving(); _patterns = list; }

    public XmlAnySimpleType[] getEnumerationValues()
    {
        if (_enumerationValues == null)
            return null;
        XmlAnySimpleType[] result = new XmlAnySimpleType[_enumerationValues.length];
        for (int i = 0; i < result.length; i++)
        {
            XmlValueRef ref = _enumerationValues[i];
            result[i] = (ref == null ? null : ref.get());

        }
        return result;
    }

    public void setEnumerationValues(XmlValueRef[] a)
        { assertResolving(); _enumerationValues = a; }

    public StringEnumAbstractBase enumForString(String s)
    {
        ensureStringEnumInfo();
        if (_lookupStringEnum == null)
            return null;
        return (StringEnumAbstractBase)_lookupStringEnum.get(s);
    }

    public StringEnumAbstractBase enumForInt(int i)
    {
        ensureStringEnumInfo();
        if (_listOfStringEnum == null || i < 0 || i >= _listOfStringEnum.size())
            return null;
        return (StringEnumAbstractBase)_listOfStringEnum.get(i);
    }

    public SchemaStringEnumEntry enumEntryForString(String s)
    {
        ensureStringEnumInfo();
        if (_lookupStringEnumEntry == null)
            return null;
        return (SchemaStringEnumEntry)_lookupStringEnumEntry.get(s);
    }

    public SchemaType getBaseEnumType()
    {
        return _baseEnumTyperef == null ? null : _baseEnumTyperef.get();
    }

    public void setBaseEnumTypeRef(SchemaType.Ref baseEnumTyperef)
    {
        _baseEnumTyperef = baseEnumTyperef;
    }

    public SchemaStringEnumEntry[] getStringEnumEntries()
    {
        if (_stringEnumEntries == null)
            return null;
        SchemaStringEnumEntry[] result = new SchemaStringEnumEntry[_stringEnumEntries.length];
        System.arraycopy(_stringEnumEntries, 0, result, 0, result.length);
        return result;
    }

    public void setStringEnumEntries(SchemaStringEnumEntry sEnums[])
    {
        assertJavaizing();
        _stringEnumEntries = sEnums;
    }

    private void ensureStringEnumInfo()
    {
        if (_stringEnumEnsured)
            return;

        SchemaStringEnumEntry[] sEnums = _stringEnumEntries;
        if (sEnums == null)
        {
            _stringEnumEnsured = true;
            return;
        }

        Map lookupStringEnum = new HashMap(sEnums.length);
        List listOfStringEnum = new ArrayList(sEnums.length + 1);
        Map lookupStringEnumEntry = new HashMap(sEnums.length);

        for (int i = 0; i < sEnums.length; i++)
        {
            lookupStringEnumEntry.put(sEnums[i].getString(), sEnums[i]);
        }

        Class jc = _baseEnumTyperef.get().getEnumJavaClass();
        if (jc != null)
        {
            try
            {
                StringEnumAbstractBase.Table table = (StringEnumAbstractBase.Table)jc.getField("table").get(null);
                for (int i = 0; i < sEnums.length; i++)
                {
                    int j = sEnums[i].getIntValue();
                    StringEnumAbstractBase enumVal = table.forInt(j);
                    lookupStringEnum.put(sEnums[i].getString(), enumVal);
                    while (listOfStringEnum.size() <= j)
                        listOfStringEnum.add(null);
                    listOfStringEnum.set(j, enumVal);
                }
            }
            catch (Exception e)
            {
                System.err.println("Something wrong: could not locate enum table for " + jc);
                jc = null;
                lookupStringEnum.clear();
                listOfStringEnum.clear();
            }
        }

        if (jc == null)
        {
            for (int i = 0; i < sEnums.length; i++)
            {
                int j = sEnums[i].getIntValue();
                String s = sEnums[i].getString();
                StringEnumAbstractBase enumVal = new StringEnumValue(s, j);
                lookupStringEnum.put(s, enumVal);
                while (listOfStringEnum.size() <= j)
                    listOfStringEnum.add(null);
                listOfStringEnum.set(j, enumVal);
            }
        }

        synchronized (this)
        {
            if (!_stringEnumEnsured)
            {
                _lookupStringEnum = lookupStringEnum;
                _listOfStringEnum = listOfStringEnum;
                _lookupStringEnumEntry = lookupStringEnumEntry;
            }
        }
        // HACKHACK: two syncrhonized blocks force a memory barrier:
        // BUGBUG: this behavior is likely to change in future VMs
        synchronized (this)
        {
            _stringEnumEnsured = true;
        }
    }

    public boolean hasStringEnumValues()
    {
        return _stringEnumEntries != null;
    }

    public void copyEnumerationValues(SchemaTypeImpl baseImpl)
    {
        assertResolving();
        _enumerationValues = baseImpl._enumerationValues;
        _baseEnumTyperef = baseImpl._baseEnumTyperef;
    }

    public int getBuiltinTypeCode()
        { return _builtinTypeCode; } // special: set up pre-init

    public void setBuiltinTypeCode(int builtinTypeCode)
        { assertResolving(); _builtinTypeCode = builtinTypeCode; }

    synchronized void assignJavaElementSetterModel()
    {
        // To compute the element setter model, we need to compute the
        // delimiting elements for each element.

        SchemaProperty[] eltProps = getElementProperties();
        SchemaParticle contentModel = getContentModel();
        Map state = new HashMap();
        QNameSet allContents = computeAllContainedElements(contentModel, state);

        for (int i = 0; i < eltProps.length; i++)
        {
            SchemaPropertyImpl sImpl = (SchemaPropertyImpl)eltProps[i];
            QNameSet nde = computeNondelimitingElements(sImpl.getName(), contentModel, state);
            QNameSetBuilder builder = new QNameSetBuilder(allContents);
            builder.removeAll(nde);
            sImpl.setJavaSetterDelimiter(builder.toQNameSet());
        }
    }

    /**
     * Used to compute setter model.
     *
     * Returns the QNameSet of all elements that can possibly come before an
     * element whose name is given by the target in a valid instance of the
     * contentModel.  When appending an element, it comes before the first
     * one that is not in this set.
     */
    private static QNameSet computeNondelimitingElements(QName target, SchemaParticle contentModel, Map state)
    {
        QNameSet allContents = computeAllContainedElements(contentModel, state);
        if (!allContents.contains(target))
            return QNameSet.EMPTY;

        // If iterated, then all contents are delimiting.
        if (contentModel.getMaxOccurs() == null ||
            contentModel.getMaxOccurs().compareTo(BigInteger.ONE) > 0)
            return allContents;

        QNameSetBuilder builder;

        switch (contentModel.getParticleType())
        {
            case SchemaParticle.ALL:
            case SchemaParticle.ELEMENT:
            default:
                return allContents;

            case SchemaParticle.WILDCARD:
                return QNameSet.singleton(target);

            case SchemaParticle.CHOICE:
                builder = new QNameSetBuilder();
                for (int i = 0; i < contentModel.countOfParticleChild(); i++)
                {
                    QNameSet childContents = computeAllContainedElements(contentModel.getParticleChild(i), state);
                    if (childContents.contains(target))
                        builder.addAll(computeNondelimitingElements(target, contentModel.getParticleChild(i), state));
                }
                return builder.toQNameSet();

            case SchemaParticle.SEQUENCE:
                builder = new QNameSetBuilder();
                boolean seenTarget = false;
                for (int i = contentModel.countOfParticleChild(); i > 0; )
                {
                    i--;
                    QNameSet childContents = computeAllContainedElements(contentModel.getParticleChild(i), state);
                    if (seenTarget)
                    {
                        builder.addAll(childContents);
                    }
                    else if (childContents.contains(target))
                    {
                        builder.addAll(computeNondelimitingElements(target, contentModel.getParticleChild(i), state));
                        seenTarget = true;
                    }
                }
                return builder.toQNameSet();
        }
    }

    /**
     * Used to compute the setter model.
     *
     * Returns the set of all QNames of elements that could possibly be
     * contained in the given contentModel. The state variable is used
     * to record the results, so that if they are needed again later,
     * they do not need to be recomputed.
     */
    private static QNameSet computeAllContainedElements(SchemaParticle contentModel, Map state)
    {
        // Remember previously computed results to avoid complexity explosion
        QNameSet result = (QNameSet)state.get(contentModel);
        if (result != null)
            return result;

        QNameSetBuilder builder;

        switch (contentModel.getParticleType())
        {
            case SchemaParticle.ALL:
            case SchemaParticle.CHOICE:
            case SchemaParticle.SEQUENCE:
            default:
                builder = new QNameSetBuilder();
                for (int i = 0; i < contentModel.countOfParticleChild(); i++)
                {
                    builder.addAll(computeAllContainedElements(contentModel.getParticleChild(i), state));
                }
                result = builder.toQNameSet();
                break;

            case SchemaParticle.WILDCARD:
                result = contentModel.getWildcardSet();
                break;

            case SchemaParticle.ELEMENT:
                // Fix for XMLBEANS-228
                result = ((SchemaLocalElementImpl) contentModel).acceptedStartNames();
                break;
        }
        state.put(contentModel, result);
        return result;
    }

    public Class getJavaClass()
    {
        // This field is declared volatile and Class is immutable so this is allowed.
        if (_javaClass == null && getFullJavaName() != null)
        {
            try
                { _javaClass = Class.forName(getFullJavaName(), false, getTypeSystem().getClassLoader()); }
            catch (ClassNotFoundException e)
            {
//                This is a legitimate use, when users get a SchemaTypeSystem without compiling classes
//                System.err.println("Could not find class name " + getFullJavaName());
//                System.err.println("Searched in classloader " + getTypeSystem().getClassLoader());
//                e.printStackTrace(System.err);
                _javaClass = null;
            }
        }

        return _javaClass;
    }

    public Class getJavaImplClass() {
        if (_implNotAvailable)
            return null;

        if (_javaImplClass == null)
        {
            try {
                if (getFullJavaImplName() != null)
                    _javaImplClass = Class.forName(getFullJavaImplName(), false, getTypeSystem().getClassLoader());
                else
                    _implNotAvailable = true;
            }
            catch (ClassNotFoundException e) {
                _implNotAvailable = true;
            }
        }

        return _javaImplClass;
    }

    public Class getUserTypeClass() 
    {
        // This field is declared volatile and Class is immutable so this is allowed.
        if (_userTypeClass == null && getUserTypeName() != null) 
        {
            try 
            {
                _userTypeClass = Class.forName(_userTypeName, false,
                    getTypeSystem().getClassLoader());
            } 
            catch (ClassNotFoundException e) 
            {
                _userTypeClass = null;
            }
        }

        return _userTypeClass;
    }

    public Class getUserTypeHandlerClass() 
    {
        // This field is declared volatile and Class is immutable so this is allowed.
        if (_userTypeHandlerClass == null && getUserTypeHandlerName() != null) 
        {
            try 
            {
                _userTypeHandlerClass = Class.forName(_userTypeHandler, false,
                    getTypeSystem().getClassLoader());
            } 
            catch (ClassNotFoundException e) 
            {
                _userTypeHandlerClass = null;
            }
        }

        return _userTypeHandlerClass;
    }

    public Constructor getJavaImplConstructor()
    {
        if (_javaImplConstructor == null && !_implNotAvailable)
        {
            final Class impl = getJavaImplClass();
            if (impl == null) return null;
            try
            {
                _javaImplConstructor = impl.getConstructor(new Class[] { SchemaType.class });
            }
            catch (NoSuchMethodException e)
            {
                e.printStackTrace();
            }
        }

        return _javaImplConstructor;

    }

    public Constructor getJavaImplConstructor2()
    {
        if (_javaImplConstructor2 == null && !_implNotAvailable)
        {
            final Class impl = getJavaImplClass();
            if (impl == null) return null;
            try
            {
                _javaImplConstructor2 = impl.getDeclaredConstructor(new Class[] { SchemaType.class, boolean.class });
            }
            catch (NoSuchMethodException e)
            {
                e.printStackTrace();
            }
        }

        return _javaImplConstructor2;

    }

    public Class getEnumJavaClass()
    {
        // This field is declared volatile and Class is immutable so this is allowed.
        if (_javaEnumClass == null)
        {
            if ( getBaseEnumType() != null )
            {
                try
                {
                    _javaEnumClass = Class.forName(getBaseEnumType().getFullJavaName() + "$Enum", false, getTypeSystem().getClassLoader());
                }
                catch (ClassNotFoundException e)
                { 
                    _javaEnumClass = null;
                }
            }
        }

        return _javaEnumClass;
    }

    public void setJavaClass(Class javaClass)
    {
        assertResolved();
        _javaClass = javaClass;
        setFullJavaName(javaClass.getName());
    }

    public boolean isPrimitiveType()
    {
        return (getBuiltinTypeCode() >= BTC_FIRST_PRIMITIVE &&
                getBuiltinTypeCode() <= BTC_LAST_PRIMITIVE);
    }

    public boolean isBuiltinType()
    {
        return getBuiltinTypeCode() != 0;
    }

    public XmlObject createUnwrappedNode()
    {
        // Todo: attach a new xml store!
        XmlObject result = createUnattachedNode(null);
        return result;
    }

    /**
     * TypeStoreUserFactory implementation
     */
    public TypeStoreUser createTypeStoreUser()
    {
        return (TypeStoreUser)createUnattachedNode(null);
    }


    public XmlAnySimpleType newValidatingValue(Object obj)
    {
        return newValue(obj, true);
    }
    /**
     * Creates an immutable simple value.
     */
    public XmlAnySimpleType newValue(Object obj)
    {
        return newValue(obj, false);
    }

    public XmlAnySimpleType newValue(Object obj, boolean validateOnSet)
    {
        if (!isSimpleType() && getContentType() != SchemaType.SIMPLE_CONTENT)
            throw new XmlValueOutOfRangeException(); // values must be simple

        XmlObjectBase result = (XmlObjectBase)createUnattachedNode(null);
        if (validateOnSet)
            result.setValidateOnSet();

        // In the case of tree copy, need to call a specla setter to avoid
        // set(XmlObject)
        if (obj instanceof XmlObject)
            result.set_newValue((XmlObject)obj);
        else
            result.objectSet(obj);

        result.check_dated();
        result.setImmutable();

        return (XmlAnySimpleType) result;
    }

    /**
     * Creates an instance of this type.
     */
    private XmlObject createUnattachedNode(SchemaProperty prop)
    {
        XmlObject result = null;

        if (!isBuiltinType() && !isNoType())
        {
            // System.out.println("Attempting to load impl class: " + getFullJavaImplName());
            Constructor ctr = getJavaImplConstructor();
            if (ctr != null)
            {
                try
                {
                    // System.out.println("Succeeded!");
                    return (XmlObject)ctr.newInstance(_ctrArgs);
                }
                catch (Exception e)
                {
                    System.out.println("Exception trying to instantiate impl class.");
                    e.printStackTrace();
                }
            }
        }
        else
        {
            result = createBuiltinInstance();
        }

        // if no result, we must be a restriction of some compiled type
        for (SchemaType sType = this; result == null; sType = sType.getBaseType())
            result = ((SchemaTypeImpl)sType).createUnattachedSubclass(this);

        ((XmlObjectBase)result).init_flags(prop);
        return result;
    }

    private XmlObject createUnattachedSubclass(SchemaType sType)
    {
        if (!isBuiltinType() && !isNoType())
        {
            // System.out.println("Attempting to load impl class: " + getFullJavaImplName());
            Constructor ctr = getJavaImplConstructor2();
            if (ctr != null)
            {
                boolean accessible = ctr.isAccessible();
                try
                {
                    ctr.setAccessible(true);
                    // System.out.println("Succeeded!");
                    try
                    {
                        return (XmlObject)ctr.newInstance(new Object[] { sType, sType.isSimpleType() ? Boolean.FALSE : Boolean.TRUE });
                    }
                    catch (Exception e)
                    {
                        System.out.println("Exception trying to instantiate impl class.");
                        e.printStackTrace();
                    }
                    finally
                    {
                        // Make a best-effort try to set the accessibility back to what it was
                        try
                        {   ctr.setAccessible(accessible); }
                        catch (SecurityException se)
                        { }
                    }
                }
                catch (Exception e)
                {
                    System.out.println("Exception trying to instantiate impl class.");
                    e.printStackTrace();
                }
            }
            return null;
        }
        else
        {
            return createBuiltinSubclass(sType);
        }
    }

    private XmlObject createBuiltinInstance()
    {
        switch (getBuiltinTypeCode())
        {
            case BTC_NOT_BUILTIN:
                return new XmlAnyTypeImpl(BuiltinSchemaTypeSystem.ST_NO_TYPE);
            case BTC_ANY_TYPE:
                return new XmlAnyTypeImpl();
            case BTC_ANY_SIMPLE:
                return new XmlAnySimpleTypeImpl();
            case BTC_BOOLEAN:
                return new XmlBooleanImpl();
            case BTC_BASE_64_BINARY:
                return new XmlBase64BinaryImpl();
            case BTC_HEX_BINARY:
                return new XmlHexBinaryImpl();
            case BTC_ANY_URI:
                return new XmlAnyUriImpl();
            case BTC_QNAME:
                return new XmlQNameImpl();
            case BTC_NOTATION:
                return new XmlNotationImpl();
            case BTC_FLOAT:
                return new XmlFloatImpl();
            case BTC_DOUBLE:
                return new XmlDoubleImpl();
            case BTC_DECIMAL:
                return new XmlDecimalImpl();
            case BTC_STRING:
                return new XmlStringImpl();
            case BTC_DURATION:
                return new XmlDurationImpl();
            case BTC_DATE_TIME:
                return new XmlDateTimeImpl();
            case BTC_TIME:
                return new XmlTimeImpl();
            case BTC_DATE:
                return new XmlDateImpl();
            case BTC_G_YEAR_MONTH:
                return new XmlGYearMonthImpl();
            case BTC_G_YEAR:
                return new XmlGYearImpl();
            case BTC_G_MONTH_DAY:
                return new XmlGMonthDayImpl();
            case BTC_G_DAY:
                return new XmlGDayImpl();
            case BTC_G_MONTH:
                return new XmlGMonthImpl();
            case BTC_INTEGER:
                return new XmlIntegerImpl();
            case BTC_LONG:
                return new XmlLongImpl();
            case BTC_INT:
                return new XmlIntImpl();
            case BTC_SHORT:
                return new XmlShortImpl();
            case BTC_BYTE:
                return new XmlByteImpl();
            case BTC_NON_POSITIVE_INTEGER:
                return new XmlNonPositiveIntegerImpl();
            case BTC_NEGATIVE_INTEGER:
                return new XmlNegativeIntegerImpl();
            case BTC_NON_NEGATIVE_INTEGER:
                return new XmlNonNegativeIntegerImpl();
            case BTC_POSITIVE_INTEGER:
                return new XmlPositiveIntegerImpl();
            case BTC_UNSIGNED_LONG:
                return new XmlUnsignedLongImpl();
            case BTC_UNSIGNED_INT:
                return new XmlUnsignedIntImpl();
            case BTC_UNSIGNED_SHORT:
                return new XmlUnsignedShortImpl();
            case BTC_UNSIGNED_BYTE:
                return new XmlUnsignedByteImpl();
            case BTC_NORMALIZED_STRING:
                return new XmlNormalizedStringImpl();
            case BTC_TOKEN:
                return new XmlTokenImpl();
            case BTC_NAME:
                return new XmlNameImpl();
            case BTC_NCNAME:
                return new XmlNCNameImpl();
            case BTC_LANGUAGE:
                return new XmlLanguageImpl();
            case BTC_ID:
                return new XmlIdImpl();
            case BTC_IDREF:
                return new XmlIdRefImpl();
            case BTC_IDREFS:
                return new XmlIdRefsImpl();
            case BTC_ENTITY:
                return new XmlEntityImpl();
            case BTC_ENTITIES:
                return new XmlEntitiesImpl();
            case BTC_NMTOKEN:
                return new XmlNmTokenImpl();
            case BTC_NMTOKENS:
                return new XmlNmTokensImpl();
            default:
                throw new IllegalStateException("Unrecognized builtin type: " + getBuiltinTypeCode());
        }
    }

    private XmlObject createBuiltinSubclass(SchemaType sType)
    {
        boolean complex = !sType.isSimpleType();
        switch (getBuiltinTypeCode())
        {
            case BTC_NOT_BUILTIN:
                return new XmlAnyTypeImpl(BuiltinSchemaTypeSystem.ST_NO_TYPE);
            case BTC_ANY_TYPE:
            case BTC_ANY_SIMPLE:
                switch (sType.getSimpleVariety())
                {
                    case NOT_SIMPLE:
                        return new XmlComplexContentImpl(sType);
                    case ATOMIC:
                        return new XmlAnySimpleTypeRestriction(sType, complex);
                    case LIST:
                        return new XmlListImpl(sType, complex);
                    case UNION:
                        return new XmlUnionImpl(sType, complex);
                    default:
                        throw new IllegalStateException();
                }
            case BTC_BOOLEAN:
                return new XmlBooleanRestriction(sType, complex);
            case BTC_BASE_64_BINARY:
                return new XmlBase64BinaryRestriction(sType, complex);
            case BTC_HEX_BINARY:
                return new XmlHexBinaryRestriction(sType, complex);
            case BTC_ANY_URI:
                return new XmlAnyUriRestriction(sType, complex);
            case BTC_QNAME:
                return new XmlQNameRestriction(sType, complex);
            case BTC_NOTATION:
                return new XmlNotationRestriction(sType, complex);
            case BTC_FLOAT:
                return new XmlFloatRestriction(sType, complex);
            case BTC_DOUBLE:
                return new XmlDoubleRestriction(sType, complex);
            case BTC_DECIMAL:
                return new XmlDecimalRestriction(sType, complex);
            case BTC_STRING:
                if (sType.hasStringEnumValues())
                    return new XmlStringEnumeration(sType, complex);
                else
                    return new XmlStringRestriction(sType, complex);
            case BTC_DURATION:
                return new XmlDurationImpl(sType, complex);
            case BTC_DATE_TIME:
                return new XmlDateTimeImpl(sType, complex);
            case BTC_TIME:
                return new XmlTimeImpl(sType, complex);
            case BTC_DATE:
                return new XmlDateImpl(sType, complex);
            case BTC_G_YEAR_MONTH:
                return new XmlGYearMonthImpl(sType, complex);
            case BTC_G_YEAR:
                return new XmlGYearImpl(sType, complex);
            case BTC_G_MONTH_DAY:
                return new XmlGMonthDayImpl(sType, complex);
            case BTC_G_DAY:
                return new XmlGDayImpl(sType, complex);
            case BTC_G_MONTH:
                return new XmlGMonthImpl(sType, complex);
            case BTC_INTEGER:
                return new XmlIntegerRestriction(sType, complex);
            case BTC_LONG:
                return new XmlLongRestriction(sType, complex);
            case BTC_INT:
                return new XmlIntRestriction(sType, complex);
            case BTC_SHORT:
                return new XmlShortImpl(sType, complex);
            case BTC_BYTE:
                return new XmlByteImpl(sType, complex);
            case BTC_NON_POSITIVE_INTEGER:
                return new XmlNonPositiveIntegerImpl(sType, complex);
            case BTC_NEGATIVE_INTEGER:
                return new XmlNegativeIntegerImpl(sType, complex);
            case BTC_NON_NEGATIVE_INTEGER:
                return new XmlNonNegativeIntegerImpl(sType, complex);
            case BTC_POSITIVE_INTEGER:
                return new XmlPositiveIntegerImpl(sType, complex);
            case BTC_UNSIGNED_LONG:
                return new XmlUnsignedLongImpl(sType, complex);
            case BTC_UNSIGNED_INT:
                return new XmlUnsignedIntImpl(sType, complex);
            case BTC_UNSIGNED_SHORT:
                return new XmlUnsignedShortImpl(sType, complex);
            case BTC_UNSIGNED_BYTE:
                return new XmlUnsignedByteImpl(sType, complex);
            case BTC_NORMALIZED_STRING:
                return new XmlNormalizedStringImpl(sType, complex);
            case BTC_TOKEN:
                return new XmlTokenImpl(sType, complex);
            case BTC_NAME:
                return new XmlNameImpl(sType, complex);
            case BTC_NCNAME:
                return new XmlNCNameImpl(sType, complex);
            case BTC_LANGUAGE:
                return new XmlLanguageImpl(sType, complex);
            case BTC_ID:
                return new XmlIdImpl(sType, complex);
            case BTC_IDREF:
                return new XmlIdRefImpl(sType, complex);
            case BTC_IDREFS:
                return new XmlIdRefsImpl(sType, complex);
            case BTC_ENTITY:
                return new XmlEntityImpl(sType, complex);
            case BTC_ENTITIES:
                return new XmlEntitiesImpl(sType, complex);
            case BTC_NMTOKEN:
                return new XmlNmTokenImpl(sType, complex);
            case BTC_NMTOKENS:
                return new XmlNmTokensImpl(sType, complex);
            default:
                throw new IllegalStateException("Unrecognized builtin type: " + getBuiltinTypeCode());
        }
    }

    public SchemaType getCommonBaseType(SchemaType type)
    {
        // null type is treated as the no-type
        if (this == BuiltinSchemaTypeSystem.ST_ANY_TYPE || type == null || type.isNoType())
            return this;

        // any type is the universal base type; noType is the universal derived type
        if (type == BuiltinSchemaTypeSystem.ST_ANY_TYPE || isNoType())
            return type;

        // the regular case:
        SchemaTypeImpl sImpl1 = (SchemaTypeImpl)type;
        while (sImpl1.getBaseDepth() > getBaseDepth())
            sImpl1 = (SchemaTypeImpl)sImpl1.getBaseType();
        SchemaTypeImpl sImpl2 = this;
        while (sImpl2.getBaseDepth() > sImpl1.getBaseDepth())
            sImpl2 = (SchemaTypeImpl)sImpl2.getBaseType();
        for (;;)
        {
            if (sImpl1.equals(sImpl2))
                break;
            sImpl1 = (SchemaTypeImpl)sImpl1.getBaseType();
            sImpl2 = (SchemaTypeImpl)sImpl2.getBaseType();
            assert(sImpl1 != null && sImpl2 != null); // must meet at anyType
        }
        return sImpl1;
    }

    public boolean isAssignableFrom(SchemaType type)
    {
        if (type == null || type.isNoType())
            return true;

        if (isNoType())
            return false;

        if (getSimpleVariety() == UNION)
        {
            SchemaType[] members = getUnionMemberTypes();
            for (int i = 0; i < members.length; i++)
                if (members[i].isAssignableFrom(type))
                    return true;
        }

        int depth = ((SchemaTypeImpl)type).getBaseDepth() - getBaseDepth();
        if (depth < 0)
            return false;
        while (depth > 0)
        {
            type = type.getBaseType();
            depth -= 1;
        }
        return (type.equals(this));
    }


    public String toString()
    {
        if (getName() != null)
            return "T=" + QNameHelper.pretty(getName());

        if (isDocumentType())
            return "D=" + QNameHelper.pretty(getDocumentElementName());

        if (isAttributeType())
            return "R=" + QNameHelper.pretty(getAttributeTypeAttributeName());

        String prefix;

        if (getContainerField() != null)
        {
            prefix = (getContainerField().getName().getNamespaceURI().length() > 0 ?
                            (getContainerField().isAttribute() ? "Q=" : "E=") :
                            (getContainerField().isAttribute() ? "A=" : "U="))
                    + getContainerField().getName().getLocalPart();
            if (getOuterType() == null)
                return prefix + "@" + getContainerField().getName().getNamespaceURI();
        }
        else if (isNoType())
            return "N=";
        else if (getOuterType() == null)
            return "noouter";
        else if (getOuterType().getBaseType() == this)
            prefix = "B=";
        else if (getOuterType().getContentBasedOnType() == this)
             prefix = "S=";
        else if (getOuterType().getSimpleVariety() == SchemaType.LIST)
            prefix = "I=";
        else if (getOuterType().getSimpleVariety() == SchemaType.UNION)
            prefix = "M=" + getAnonymousUnionMemberOrdinal();
        else
            prefix = "strange=";
        
        return prefix + "|" + getOuterType().toString();
    }

    private XmlObject _parseObject;
    private String _parseTNS;
    private String _elemFormDefault;
    private String _attFormDefault;
    private boolean _chameleon;
    private boolean _redefinition;

    public void setParseContext(XmlObject parseObject, String targetNamespace, boolean chameleon, String elemFormDefault, String attFormDefault, boolean redefinition)
    {
        _parseObject = parseObject;
        _parseTNS = targetNamespace;
        _chameleon = chameleon;
        _elemFormDefault = elemFormDefault;
        _attFormDefault = attFormDefault;
        _redefinition = redefinition;
    }

    public XmlObject getParseObject()
        { return _parseObject; }

    public String getTargetNamespace()
        { return _parseTNS; }

    public boolean isChameleon ( )
        { return _chameleon; }

    public String getElemFormDefault()
        { return _elemFormDefault; }

    public String getAttFormDefault()
        { return _attFormDefault; }

    public String getChameleonNamespace()
        { return _chameleon ? _parseTNS : null; }

    public boolean isRedefinition()
        { return _redefinition; }

    private SchemaType.Ref _selfref = new SchemaType.Ref(this);

    public SchemaType.Ref getRef()
        { return _selfref; }

    public SchemaComponent.Ref getComponentRef()
        { return getRef(); }

    /**
     * Gives access to the internals of element validation
     */
    private static class SequencerImpl implements SchemaTypeElementSequencer
    {
        private SchemaTypeVisitorImpl _visitor;

        private SequencerImpl(SchemaTypeVisitorImpl visitor)
        {
            _visitor = visitor;
        }

        public boolean next(QName elementName)
        {
            if (_visitor == null)
                return false;

            return _visitor.visit(elementName);
        }

        public boolean peek(QName elementName)
        {
            if (_visitor == null)
                return false;

            return _visitor.testValid(elementName);
        }
    }

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
    public QNameSet qnameSetForWildcardElements()
    {
        SchemaParticle model = this.getContentModel();
        QNameSetBuilder wildcardSet = new QNameSetBuilder();
        computeWildcardSet(model, wildcardSet);

        QNameSetBuilder qnsb = new QNameSetBuilder( wildcardSet );
        SchemaProperty[] props = this.getElementProperties();

        for (int i = 0; i < props.length; i++)
        {
            SchemaProperty prop = props[i];
            qnsb.remove(prop.getName());
        }

        return qnsb.toQNameSet();
    }

    private static void computeWildcardSet(SchemaParticle model, QNameSetBuilder result)
    {
        if (model.getParticleType() == SchemaParticle.WILDCARD)
        {
            QNameSet cws = model.getWildcardSet();
            result.addAll(cws);
            return;
        }
        for (int i = 0; i < model.countOfParticleChild(); i++)
        {
            SchemaParticle child = model.getParticleChild(i);
            computeWildcardSet(child, result);
        }
    }

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
    public QNameSet qnameSetForWildcardAttributes()
    {
        SchemaAttributeModel model = this.getAttributeModel();
        QNameSet wildcardSet = model.getWildcardSet();

        if (wildcardSet==null)
            return QNameSet.EMPTY;

        QNameSetBuilder qnsb = new QNameSetBuilder( wildcardSet );

        SchemaProperty[] props = this.getAttributeProperties();

        for (int i = 0; i < props.length; i++)
        {
            SchemaProperty prop = props[i];
            qnsb.remove(prop.getName());
        }

        return qnsb.toQNameSet();
    }
}
