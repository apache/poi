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

import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaComponent;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlObject;
import javax.xml.namespace.QName;

import java.util.LinkedHashSet;
import java.util.Set;

public class SchemaGlobalElementImpl extends SchemaLocalElementImpl
        implements SchemaGlobalElement
{
    private Set _sgMembers = new LinkedHashSet();
    private static final QName[] _namearray = new QName[0];
    private boolean _finalExt;
    private boolean _finalRest;
    private SchemaContainer _container;
    private String _filename;
    // private XmlObject _parseObject; now inherited from base
    private String _parseTNS;
    private boolean _chameleon;
    private SchemaGlobalElement.Ref _sg;

    public SchemaGlobalElementImpl(SchemaContainer container)
    {
        _container = container;
    }

    public SchemaTypeSystem getTypeSystem()
    {
        return _container.getTypeSystem();
    }

    SchemaContainer getContainer()
    {
        return _container;
    }

    public String getSourceName()
    {
        return _filename;
    }

    public void setFilename(String filename)
    {
        _filename = filename;
    }

    void setFinal(boolean finalExt, boolean finalRest)
    {
        mutate(); _finalExt = finalExt; _finalRest = finalRest;
    }

    public int getComponentType()
    {
        return SchemaComponent.ELEMENT;
    }

    public SchemaGlobalElement substitutionGroup()
    {
        return _sg == null ? null : _sg.get();
    }

    public void setSubstitutionGroup(SchemaGlobalElement.Ref sg) 
    {
        _sg = sg;
    }

    public QName[] substitutionGroupMembers()
    {
        return (QName[])_sgMembers.toArray(_namearray);
    }

    public void addSubstitutionGroupMember(QName name)
    {
        mutate(); _sgMembers.add(name);
    }


    public boolean finalExtension()
    {
        return _finalExt;
    }

    public boolean finalRestriction()
    {
        return _finalRest;
    }

    public void setParseContext(XmlObject parseObject, String targetNamespace, boolean chameleon)
    {
        _parseObject = parseObject;
        _parseTNS = targetNamespace;
        _chameleon = chameleon;
    }

    public XmlObject getParseObject()
        { return _parseObject; }

    public String getTargetNamespace()
        { return _parseTNS; }

    public String getChameleonNamespace()
        { return _chameleon ? _parseTNS : null; }

    private SchemaGlobalElement.Ref _selfref = new SchemaGlobalElement.Ref(this);

    public SchemaGlobalElement.Ref getRef()
        { return _selfref; }

    public SchemaComponent.Ref getComponentRef()
        { return getRef(); }
}
