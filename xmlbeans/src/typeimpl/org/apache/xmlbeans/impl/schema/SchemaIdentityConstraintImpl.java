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

import org.apache.xmlbeans.SchemaIdentityConstraint;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SchemaAnnotation;
import org.apache.xmlbeans.SchemaComponent;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.common.XPath;
import javax.xml.namespace.QName;
import java.util.Map;
import java.util.Collections;

public class SchemaIdentityConstraintImpl implements SchemaIdentityConstraint
{
    private SchemaContainer _container;
    private String _selector;
    private String[] _fields;
    private SchemaIdentityConstraint.Ref _key;
    private QName _name;
    private int _type;
    private XmlObject _parse;
    private Object _userData;
    private SchemaAnnotation _annotation;
    private Map _nsMap = Collections.EMPTY_MAP;
    private String _parseTNS;
    private boolean _chameleon;
    private String _filename;

    // Lazily computed paths
    private volatile XPath _selectorPath;
    private volatile XPath[] _fieldPaths;

    public SchemaIdentityConstraintImpl(SchemaContainer c) {
        _container = c;
    }

    public void setFilename(String filename)
        { _filename = filename; }

    public String getSourceName()
        { return _filename; }


    public String getSelector() {
        return _selector;
    }

    public Object getSelectorPath() {
        XPath p = _selectorPath;
        if (p == null) {
            try {
                buildPaths();
                p = _selectorPath;
            }
            catch (XPath.XPathCompileException e) {
                assert false: "Failed to compile xpath. Should be caught by compiler " + e;
                return null;
            }
        }
        return p;
    }

    public void setAnnotation(SchemaAnnotation ann)
    {
        _annotation = ann;
    }

    public SchemaAnnotation getAnnotation()
    {
        return _annotation;
    }

    public void setNSMap(Map nsMap) {
        _nsMap = nsMap;
    }

    public Map getNSMap() {
        return Collections.unmodifiableMap(_nsMap);
    }

    public void setSelector(String selector) {
        assert selector != null;
        _selector = selector;
    }

    public void setFields(String[] fields) {
        assert fields != null && fields.length > 0;
        _fields = fields;
    }

    public String[] getFields() {
        String[] fields = new String[_fields.length];
        System.arraycopy(_fields, 0, fields, 0, fields.length);
        return fields;
    }

    public Object getFieldPath(int index) {
        XPath[] p = _fieldPaths;
        if (p == null) {
            try {
                buildPaths();
                p = _fieldPaths;
            }
            catch (XPath.XPathCompileException e) {
                assert false: "Failed to compile xpath. Should be caught by compiler " + e;
                return null;
            }
        }
        return p[index];
    }

    public void buildPaths() throws XPath.XPathCompileException {
        // TODO: Need the namespace map - requires store support
        _selectorPath = XPath.compileXPath(_selector, _nsMap);

        _fieldPaths = new XPath[_fields.length];
        for (int i = 0 ; i < _fieldPaths.length ; i++)
            _fieldPaths[i] = XPath.compileXPath(_fields[i], _nsMap);
    }

    public void setReferencedKey(SchemaIdentityConstraint.Ref key) {
        _key = key;
    }

    public SchemaIdentityConstraint getReferencedKey() {
        return _key.get();
    }

    public void setConstraintCategory(int type) {
        assert type >= CC_KEY && type <= CC_UNIQUE;
        _type = type;
    }

    public int getConstraintCategory() {
        return _type;
    }

    public void setName(QName name) {
        assert name != null;
        _name = name;
    }

    public QName getName() {
        return _name;
    }

    public int getComponentType() {
        return IDENTITY_CONSTRAINT;
    }

    public SchemaTypeSystem getTypeSystem() {
        return _container.getTypeSystem();
    }

    SchemaContainer getContainer() {
        return _container;
    }

    public void setParseContext(XmlObject o, String targetNamespace, boolean chameleon) {
        _parse = o;
        _parseTNS = targetNamespace;
        _chameleon = chameleon;
    }

    public XmlObject getParseObject() {
        return _parse;
    }

    public String getTargetNamespace() {
        return _parseTNS;
    }

    public String getChameleonNamespace() {
        return _chameleon ? _parseTNS : null;
    }


    /**
     * Only applicable to keyrefs. Other types are implicitly resolved.
     */
    public boolean isResolved() {
        return getConstraintCategory() != CC_KEYREF || _key != null;
    }

    private SchemaIdentityConstraint.Ref _selfref = new SchemaIdentityConstraint.Ref(this);

    public SchemaIdentityConstraint.Ref getRef()
        { return _selfref; }

    public SchemaComponent.Ref getComponentRef()
        { return getRef(); }

    public Object getUserData()
    {   return _userData; }

    public void setUserData(Object data)
    {   _userData = data; }
}
