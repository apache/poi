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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaAnnotation;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.xb.xsdschema.AppinfoDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.Annotated;
import org.apache.xmlbeans.impl.xb.xsdschema.AnnotationDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.DocumentationDocument;
import org.apache.xmlbeans.SchemaComponent;

public class SchemaAnnotationImpl implements SchemaAnnotation
{
    private SchemaContainer _container;
    private String[] _appInfoAsXml;
    private AppinfoDocument.Appinfo[] _appInfo;
    private String[] _documentationAsXml;
    private DocumentationDocument.Documentation[] _documentation;
    private Attribute[] _attributes;
    private String _filename;

    public void setFilename(String filename)
    {
        _filename = filename;
    }

    public String getSourceName()
    {
        return _filename;
    }

    public XmlObject[] getApplicationInformation()
    {
        if (_appInfo == null)
        {
            int n = _appInfoAsXml.length;
            _appInfo = new AppinfoDocument.Appinfo[n];
            for (int i = 0; i < n; i++)
            {
                String appInfo = _appInfoAsXml[i];
                try
                {
                    _appInfo[i] = AppinfoDocument.Factory.
                        parse(appInfo).getAppinfo();
                }
                catch(XmlException e)
                {
                    // problem in the classfile
                    _appInfo[i] = AppinfoDocument.Factory.
                        newInstance().getAppinfo();
                }
            }
        }
        return _appInfo;
    }

    public XmlObject[] getUserInformation()
    {
        if (_documentation == null)
        {
            int n = _documentationAsXml.length;
            _documentation = new DocumentationDocument.Documentation[n];
            for (int i = 0; i <  n; i++)
            {
                String doc = _documentationAsXml[i];
                try 
                {
                    _documentation[i] = DocumentationDocument.Factory.
                        parse(doc).getDocumentation();
                }
                catch (XmlException e)
                {
                    // problem in the classfile
                    _documentation[i] = DocumentationDocument.Factory.
                        newInstance().getDocumentation();
                }
            }
        }
        return _documentation;
    }

    public Attribute[] getAttributes()
    {   return _attributes; }

    public int getComponentType()
    {   return ANNOTATION; }

    public SchemaTypeSystem getTypeSystem()
    {   return _container != null ? _container.getTypeSystem() : null; }

    SchemaContainer getContainer()
    {   return _container; }

    public QName getName()
    {   return null; }

    public SchemaComponent.Ref getComponentRef()
    {   return null; }

    public static SchemaAnnotationImpl getAnnotation(SchemaContainer c,
        Annotated elem)
    {
        AnnotationDocument.Annotation ann = elem.getAnnotation();

        return getAnnotation(c, elem, ann);
    }

    public static SchemaAnnotationImpl getAnnotation(SchemaContainer c,
        XmlObject elem, AnnotationDocument.Annotation ann)
    {
        // Check option
        if (StscState.get().noAnn())
            return null;

        SchemaAnnotationImpl result = new SchemaAnnotationImpl(c);
        // Retrieving attributes, first attributes on the enclosing element
        ArrayList attrArray = new ArrayList(2);
        addNoSchemaAttributes(elem, attrArray);
        if (ann == null)
        {
            if (attrArray.size() == 0)
                return null; // no annotation present
            // no annotation element present, but attributes on the enclosing
            // element present, so we have an annotation component
            result._appInfo = new AppinfoDocument.Appinfo[0];
            result._documentation = new DocumentationDocument.Documentation[0];
        }
        else
        {
            result._appInfo = ann.getAppinfoArray();
            result._documentation = ann.getDocumentationArray();
            // Now the attributes on the annotation element
            addNoSchemaAttributes(ann, attrArray);
        }
        
        result._attributes =
            (AttributeImpl[]) attrArray.toArray(new AttributeImpl[attrArray.size()]);
        return result;
    }

    private static void addNoSchemaAttributes(XmlObject elem, List attrList)
    {
        XmlCursor cursor = elem.newCursor();
        boolean hasAttributes = cursor.toFirstAttribute();
        while (hasAttributes)
        {
            QName name = cursor.getName();
            String namespaceURI = name.getNamespaceURI();
            if ("".equals(namespaceURI) ||
                "http://www.w3.org/2001/XMLSchema".equals(namespaceURI))
                ; // no nothing
            else
            {
                String attValue = cursor.getTextValue();
                String valUri;
                String prefix;
                if (attValue.indexOf(':') > 0)
                    prefix = attValue.substring(0, attValue.indexOf(':'));
                else
                    prefix = "";
                cursor.push();
                cursor.toParent();
                valUri = cursor.namespaceForPrefix(prefix);
                cursor.pop();
                attrList.add(new AttributeImpl(name, attValue, valUri)); //add the attribute
            }
            hasAttributes = cursor.toNextAttribute();
        }
        cursor.dispose();
    }

    private SchemaAnnotationImpl(SchemaContainer c)
    {
        _container = c;
    }

    /*package*/ SchemaAnnotationImpl(SchemaContainer c,
        String[] aapStrings, String[] adocStrings,
        Attribute[] aat)
    {
        _container = c;
        _appInfoAsXml = aapStrings;
        _documentationAsXml = adocStrings;
        _attributes = aat;
    }

    /*package*/ static class AttributeImpl implements Attribute
    {
        private QName _name;
        private String _value;
        private String _valueUri;

        /*package*/ AttributeImpl(QName name, String value, String valueUri)
        {
            _name = name;
            _value = value;
            _valueUri = valueUri;
        }

        public QName getName()
        {   return _name; }

        public String getValue()
        {   return _value; }

        public String getValueUri()
        {   return _valueUri; }
    }
}
