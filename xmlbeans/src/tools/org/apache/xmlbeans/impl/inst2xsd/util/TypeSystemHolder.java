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
package org.apache.xmlbeans.impl.inst2xsd.util;

import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.XmlQName;
import org.apache.xmlbeans.XmlCursor;

import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashMap;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com) Date: Jul 16, 2004
 */
public class TypeSystemHolder
{
    Map _globalElements;   // QName -> Element
    Map _globalAttributes; // QName -> Attribute
    Map _globalTypes;      // QName -> Type

    public TypeSystemHolder()
    {
        _globalElements = new LinkedHashMap();
        _globalAttributes = new LinkedHashMap();
        _globalTypes = new LinkedHashMap();
    }

    public void addGlobalElement(Element element)
    {
        assert element.isGlobal() && !element.isRef();
        _globalElements.put(element.getName(), element);
    }

    public Element getGlobalElement(QName name)
    {
        return (Element)_globalElements.get(name);
    }

    public Element[] getGlobalElements()
    {
        Collection col = _globalElements.values();
        return (Element[])col.toArray(new Element[col.size()]);
    }

    public void addGlobalAttribute(Attribute attribute)
    {
        assert attribute.isGlobal() && !attribute.isRef();
        _globalAttributes.put(attribute.getName(), attribute);
    }

    public Attribute getGlobalAttribute(QName name)
    {
        return (Attribute)_globalAttributes.get(name);
    }

    public Attribute[] getGlobalAttributes()
    {
        Collection col = _globalAttributes.values();
        return (Attribute[])col.toArray(new Attribute[col.size()]);
    }

    public void addGlobalType(Type type)
    {
        assert type.isGlobal() && type.getName()!=null : "type must be a global type before being added.";
        _globalTypes.put(type.getName(), type);
    }

    public Type getGlobalType(QName name)
    {
        return (Type)_globalTypes.get(name);
    }

    public Type[] getGlobalTypes()
    {
        Collection col = _globalTypes.values();
        return (Type[])col.toArray(new Type[col.size()]);
    }

    public SchemaDocument[] getSchemaDocuments()
    {
        // recompute everything, should cache it and track changes
        Map nsToSchemaDocs = new LinkedHashMap();

        for (Iterator iterator = _globalElements.keySet().iterator(); iterator.hasNext();)
        {
            QName globalElemName = (QName) iterator.next();
            String tns = globalElemName.getNamespaceURI();
            SchemaDocument schDoc = getSchemaDocumentForTNS(nsToSchemaDocs, tns);

            fillUpGlobalElement((Element)_globalElements.get(globalElemName), schDoc, tns);
        }

        for (Iterator iterator = _globalAttributes.keySet().iterator(); iterator.hasNext();)
        {
            QName globalAttName = (QName) iterator.next();
            String tns = globalAttName.getNamespaceURI();
            SchemaDocument schDoc = getSchemaDocumentForTNS(nsToSchemaDocs, tns);

            fillUpGlobalAttribute((Attribute)_globalAttributes.get(globalAttName), schDoc, tns);
        }

        for (Iterator iterator = _globalTypes.keySet().iterator(); iterator.hasNext();)
        {
            QName globalTypeName = (QName) iterator.next();
            String tns = globalTypeName.getNamespaceURI();
            SchemaDocument schDoc = getSchemaDocumentForTNS(nsToSchemaDocs, tns);

            fillUpGlobalType((Type)_globalTypes.get(globalTypeName), schDoc, tns);
        }

        Collection schDocColl = nsToSchemaDocs.values();
        return (SchemaDocument[])schDocColl.toArray(new SchemaDocument[schDocColl.size()]);
    }

    private static SchemaDocument getSchemaDocumentForTNS(Map nsToSchemaDocs, String tns)
    {
        SchemaDocument schDoc = (SchemaDocument)nsToSchemaDocs.get(tns);
        if (schDoc==null)
        {
            schDoc = SchemaDocument.Factory.newInstance();
            nsToSchemaDocs.put(tns, schDoc);
        }
        return schDoc;
    }

    private static org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Schema getTopLevelSchemaElement(SchemaDocument schDoc,
        String tns)
    {
        org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Schema sch = schDoc.getSchema();
        if (sch==null)
        {
            sch = schDoc.addNewSchema();
            sch.setAttributeFormDefault(org.apache.xmlbeans.impl.xb.xsdschema.FormChoice.Enum.forString("unqualified"));
            sch.setElementFormDefault(org.apache.xmlbeans.impl.xb.xsdschema.FormChoice.Enum.forString("qualified"));
            if (!tns.equals(""))
                sch.setTargetNamespace(tns);
        }
        return sch;
    }

    // Global Elements
    private void fillUpGlobalElement(Element globalElement, SchemaDocument schDoc, String tns)
    {
        assert tns.equals(globalElement.getName().getNamespaceURI());

        org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Schema sch = getTopLevelSchemaElement(schDoc, tns);

        org.apache.xmlbeans.impl.xb.xsdschema.TopLevelElement topLevelElem = sch.addNewElement();
        topLevelElem.setName(globalElement.getName().getLocalPart());

        if (globalElement.isNillable())
            topLevelElem.setNillable(globalElement.isNillable());

        fillUpElementDocumentation(topLevelElem, globalElement.getComment());

        Type elemType = globalElement.getType();
        fillUpTypeOnElement(elemType, topLevelElem, tns);
    }

    protected void fillUpLocalElement(Element element, org.apache.xmlbeans.impl.xb.xsdschema.LocalElement localSElement, String tns)
    {
        fillUpElementDocumentation(localSElement, element.getComment());
        if (!element.isRef())
        {
            assert element.getName().getNamespaceURI().equals(tns) ||
                element.getName().getNamespaceURI().length() == 0;
            fillUpTypeOnElement(element.getType(), localSElement, tns);
            localSElement.setName(element.getName().getLocalPart());
        }
        else
        {
            localSElement.setRef(element.getName());
            assert !element.isNillable();
        }

        if (element.getMaxOccurs()==Element.UNBOUNDED)
        {
            localSElement.setMaxOccurs("unbounded");
        }
        if (element.getMinOccurs()!=1)
        {
            localSElement.setMinOccurs(new BigInteger("" + element.getMinOccurs()));
        }

        if (element.isNillable())
            localSElement.setNillable(element.isNillable());
    }

    private void fillUpTypeOnElement(Type elemType, org.apache.xmlbeans.impl.xb.xsdschema.Element parentSElement, String tns)
    {
        if (elemType.isGlobal())
        {
            assert elemType.getName()!=null : "Global type must have a name.";
            parentSElement.setType(elemType.getName());
        }
        else if (elemType.getContentType()==Type.SIMPLE_TYPE_SIMPLE_CONTENT)
        {
            if (elemType.isEnumeration())
                fillUpEnumeration(elemType, parentSElement);
            else
                parentSElement.setType(elemType.getName());
        }
        else
        {
            org.apache.xmlbeans.impl.xb.xsdschema.LocalComplexType localComplexType = parentSElement.addNewComplexType();
            fillUpContentForComplexType(elemType, localComplexType, tns);
        }
    }

    private void fillUpEnumeration(Type type, org.apache.xmlbeans.impl.xb.xsdschema.Element parentSElement)
    {
        assert type.isEnumeration() && !type.isComplexType() : "Enumerations must be on simple types only.";
        org.apache.xmlbeans.impl.xb.xsdschema.RestrictionDocument.Restriction restriction = parentSElement.addNewSimpleType().addNewRestriction();
        restriction.setBase(type.getName());
        if (type.isQNameEnumeration())
        {
            for (int i = 0; i < type.getEnumerationQNames().size(); i++)
            {
                QName value = (QName) type.getEnumerationQNames().get(i);
                XmlQName xqname = XmlQName.Factory.newValue(value);

                org.apache.xmlbeans.impl.xb.xsdschema.NoFixedFacet enumSElem = restriction.addNewEnumeration();
                XmlCursor xc  = enumSElem.newCursor();

                String newPrefix = xc.prefixForNamespace(value.getNamespaceURI());
                xc.dispose();

                enumSElem.setValue( XmlQName.Factory.newValue(
                    new QName(value.getNamespaceURI(), value.getLocalPart(), newPrefix)));
            }
        }
        else
        {
            for (int i = 0; i < type.getEnumerationValues().size(); i++)
            {
                String value = (String) type.getEnumerationValues().get(i);
                restriction.addNewEnumeration().setValue(XmlString.Factory.newValue(value));
            }
        }
    }

    private void fillUpAttributesInComplexTypesSimpleContent(Type elemType,
        org.apache.xmlbeans.impl.xb.xsdschema.SimpleExtensionType sExtension, String tns)
    {
        for (int i = 0; i < elemType.getAttributes().size(); i++)
        {
            Attribute att = (Attribute) elemType.getAttributes().get(i);
            org.apache.xmlbeans.impl.xb.xsdschema.Attribute sAttribute = sExtension.addNewAttribute();
            fillUpLocalAttribute(att, sAttribute, tns);
        }
    }

    private void fillUpAttributesInComplexTypesComplexContent(Type elemType,
        org.apache.xmlbeans.impl.xb.xsdschema.ComplexType localSComplexType, String tns)
    {
        for (int i = 0; i < elemType.getAttributes().size(); i++)
        {
            Attribute att = (Attribute) elemType.getAttributes().get(i);
            org.apache.xmlbeans.impl.xb.xsdschema.Attribute sAttribute = localSComplexType.addNewAttribute();
            fillUpLocalAttribute(att, sAttribute, tns);
        }
    }

    protected void fillUpLocalAttribute(Attribute att, org.apache.xmlbeans.impl.xb.xsdschema.Attribute sAttribute, String tns)
    {
        if (att.isRef())
        {
            sAttribute.setRef(att.getRef().getName());
        }
        else
        {
            assert att.getName().getNamespaceURI()==tns || att.getName().getNamespaceURI().equals("");
            sAttribute.setType(att.getType().getName());
            sAttribute.setName(att.getName().getLocalPart());
            if (att.isOptional())
                sAttribute.setUse(org.apache.xmlbeans.impl.xb.xsdschema.Attribute.Use.OPTIONAL);
        }
    }

    protected void fillUpContentForComplexType(Type type, org.apache.xmlbeans.impl.xb.xsdschema.ComplexType sComplexType, String tns)
    {
        if (type.getContentType()==Type.COMPLEX_TYPE_SIMPLE_CONTENT)
        {
            org.apache.xmlbeans.impl.xb.xsdschema.SimpleContentDocument.SimpleContent simpleContent = sComplexType.addNewSimpleContent();

            assert type.getExtensionType()!=null && type.getExtensionType().getName()!=null : "Extension type must exist and be named for a COMPLEX_TYPE_SIMPLE_CONTENT";

            org.apache.xmlbeans.impl.xb.xsdschema.SimpleExtensionType ext = simpleContent.addNewExtension();
            ext.setBase(type.getExtensionType().getName());

            fillUpAttributesInComplexTypesSimpleContent(type, ext, tns);
        }
        else
        {
            if (type.getContentType()==Type.COMPLEX_TYPE_MIXED_CONTENT)
            {
                sComplexType.setMixed(true);
            }

            org.apache.xmlbeans.impl.xb.xsdschema.ExplicitGroup explicitGroup;
            if (type.getContentType()==Type.COMPLEX_TYPE_EMPTY_CONTENT)
                explicitGroup = null;
            else if (type.getTopParticleForComplexOrMixedContent()==Type.PARTICLE_SEQUENCE)
            {
                explicitGroup = sComplexType.addNewSequence();
            }
            else if (type.getTopParticleForComplexOrMixedContent()==Type.PARTICLE_CHOICE_UNBOUNDED)
            {
                explicitGroup = sComplexType.addNewChoice();
                explicitGroup.setMaxOccurs("unbounded");
                explicitGroup.setMinOccurs(new BigInteger("0"));
            }
            else { throw new IllegalStateException("Unknown particle type in complex and mixed content"); }

            for (int i = 0; i < type.getElements().size(); i++)
            {
                Element child = (Element) type.getElements().get(i);
                assert !child.isGlobal();
                org.apache.xmlbeans.impl.xb.xsdschema.LocalElement childLocalElement = explicitGroup.addNewElement();
                fillUpLocalElement(child, childLocalElement, tns);
            }

            fillUpAttributesInComplexTypesComplexContent(type, sComplexType, tns);
        }
    }

    // Global Attributes
    private void fillUpGlobalAttribute(Attribute globalAttribute, SchemaDocument schDoc, String tns)
    {
        assert tns.equals(globalAttribute.getName().getNamespaceURI());
        org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Schema sch = getTopLevelSchemaElement(schDoc, tns);

        org.apache.xmlbeans.impl.xb.xsdschema.TopLevelAttribute topLevelAtt = sch.addNewAttribute();
        topLevelAtt.setName(globalAttribute.getName().getLocalPart());

        Type elemType = globalAttribute.getType();

        if (elemType.getContentType()==Type.SIMPLE_TYPE_SIMPLE_CONTENT)
        {
            topLevelAtt.setType(elemType.getName());
        }
        else
        {
            //org.apache.xmlbeans.impl.xb.xsdschema.LocalSimpleType localSimpleType = topLevelAtt.addNewSimpleType();
            throw new IllegalStateException();
        }
    }

    private static void fillUpElementDocumentation(org.apache.xmlbeans.impl.xb.xsdschema.Element element, String comment)
    {
        if (comment!=null && comment.length()>0)
        {
            org.apache.xmlbeans.impl.xb.xsdschema.DocumentationDocument.Documentation documentation = element.addNewAnnotation().addNewDocumentation();
            documentation.set(org.apache.xmlbeans.XmlString.Factory.newValue(comment));
        }
    }

    // Global Types
    private void fillUpGlobalType(Type globalType, SchemaDocument schDoc, String tns)
    {
        assert tns.equals(globalType.getName().getNamespaceURI());
        org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Schema sch = getTopLevelSchemaElement(schDoc, tns);

        org.apache.xmlbeans.impl.xb.xsdschema.TopLevelComplexType topLevelComplexType = sch.addNewComplexType();
        topLevelComplexType.setName(globalType.getName().getLocalPart());

        fillUpContentForComplexType(globalType, topLevelComplexType, tns);
    }

    public String toString()
    {
        return "TypeSystemHolder{" +
            "\n\n_globalElements=" + _globalElements +
            "\n\n_globalAttributes=" + _globalAttributes +
            "\n\n_globalTypes=" + _globalTypes +
            "\n}";
    }
}
