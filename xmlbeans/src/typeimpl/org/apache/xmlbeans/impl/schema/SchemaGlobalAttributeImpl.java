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

import org.apache.xmlbeans.SchemaGlobalAttribute;
import org.apache.xmlbeans.SchemaComponent;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlObject;

public class SchemaGlobalAttributeImpl extends SchemaLocalAttributeImpl
        implements SchemaGlobalAttribute
{
    SchemaContainer _container;
    String _filename;
    private String _parseTNS;
    private boolean _chameleon;

    public SchemaGlobalAttributeImpl(SchemaContainer container)
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


    public int getComponentType()
    {
        return SchemaComponent.ATTRIBUTE;
    }

    public String getSourceName()
    {
        return _filename;
    }

    public void setFilename(String filename)
    {
        _filename = filename;
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

    private SchemaGlobalAttribute.Ref _selfref = new SchemaGlobalAttribute.Ref(this);

    public SchemaGlobalAttribute.Ref getRef()
        { return _selfref; }

    public SchemaComponent.Ref getComponentRef()
        { return getRef(); }
}
