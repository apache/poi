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
import java.util.Collections;
import java.util.List;

import org.apache.xmlbeans.SchemaAnnotation;
import org.apache.xmlbeans.SchemaAttributeGroup;
import org.apache.xmlbeans.SchemaComponent;
import org.apache.xmlbeans.SchemaGlobalAttribute;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaIdentityConstraint;
import org.apache.xmlbeans.SchemaModelGroup;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;

/*package*/ class SchemaContainer
{
    // The namespace that this is the container for
    // TODO(radup) In the future, I think the right approach is one container
    // per file instead of per namespace, but per namespace is easier for now
    private String _namespace;

    SchemaContainer(String namespace)
    {
        _namespace = namespace;
    }

    String getNamespace()
    {
        return _namespace;
    }

    // The SchemaTypeSystem on behalf of which this acts
    private SchemaTypeSystem _typeSystem;

    // This is the whole idea of the containers
    // By synchronizing getter/setters on this field, we allow
    // both SchemaTypeSystems and SchemaTypes to be immutable
    // at the same time providing the mechanism through which
    // we can "move" SchemaTypes from one SchemaTypeSystem to another
    // via incremental compilation
    synchronized SchemaTypeSystem getTypeSystem()
    {
        return _typeSystem;
    }

    synchronized void setTypeSystem(SchemaTypeSystem typeSystem)
    {
        _typeSystem = typeSystem;
    }

    // Immutability refers to the content of the container
    // Once the container has been initialized, one cannot add/remove
    // SchemaComponents from it. Instead, one has to blow it away
    // and build a new one.
    // Immutability does not mean that one cannot move this container
    // between typesystems.
    boolean _immutable;

    synchronized void setImmutable()
    {
        _immutable = true;
    }

    synchronized void unsetImmutable()
    {
        _immutable = false;
    }

    private void check_immutable()
    {
        if (_immutable)
            throw new IllegalStateException("Cannot add components to immutable SchemaContainer");
    }

    // Data
    // TODO(radup) unmodifiableList() is not really necessary, since this
    // is package-level access and code in this package should do the "right thing"
    // Global Elements
    private List/*<SchemaGlobalElement.Ref>*/ _globalElements = new ArrayList();

    void addGlobalElement(SchemaGlobalElement.Ref e)
    {   check_immutable(); _globalElements.add(e); }

    List globalElements()
    {   return getComponentList(_globalElements); }

    // Global Attributes
    private List/*<SchemaGlobalAttribute.Ref>*/ _globalAttributes = new ArrayList();

    void addGlobalAttribute(SchemaGlobalAttribute.Ref a)
    {   check_immutable(); _globalAttributes.add(a); }

    List globalAttributes()
    {   return getComponentList(_globalAttributes); }

    // Model Groups
    private List/*<SchemaModelGroup.Ref>*/ _modelGroups = new ArrayList();

    void addModelGroup(SchemaModelGroup.Ref g)
    {   check_immutable(); _modelGroups.add(g); }

    List modelGroups()
    {   return getComponentList(_modelGroups); }

    // Redefined Model Groups
    private List/*<SchemaModelGroup.Ref>*/ _redefinedModelGroups = new ArrayList();

    void addRedefinedModelGroup(SchemaModelGroup.Ref g)
    {   check_immutable(); _redefinedModelGroups.add(g); }

    List redefinedModelGroups()
    {   return getComponentList(_redefinedModelGroups); }

    // Attribute Groups
    private List/*<SchemaAttributeGroup.Ref>*/ _attributeGroups = new ArrayList();

    void addAttributeGroup(SchemaAttributeGroup.Ref g)
    {   check_immutable(); _attributeGroups.add(g); }

    List attributeGroups()
    {   return getComponentList(_attributeGroups); }

    // Redefined Attribute Groups
    private List/*<SchemaAttributeGroup.Ref>*/ _redefinedAttributeGroups = new ArrayList();

    void addRedefinedAttributeGroup(SchemaAttributeGroup.Ref g)
    {   check_immutable(); _redefinedAttributeGroups.add(g); }

    List redefinedAttributeGroups()
    {   return getComponentList(_redefinedAttributeGroups); }

    // Global Types
    private List/*<SchemaType.Ref>*/ _globalTypes = new ArrayList();

    void addGlobalType(SchemaType.Ref t)
    {   check_immutable(); _globalTypes.add(t); }

    List globalTypes()
    {   return getComponentList(_globalTypes); }

    // Redefined Global Types
    private List/*<SchemaType.Ref>*/ _redefinedGlobalTypes = new ArrayList();

    void addRedefinedType(SchemaType.Ref t)
    {   check_immutable(); _redefinedGlobalTypes.add(t); }

    List redefinedGlobalTypes()
    {   return getComponentList(_redefinedGlobalTypes); }

    // Document Types
    private List/*<SchemaType.Ref>*/ _documentTypes = new ArrayList();

    void addDocumentType(SchemaType.Ref t)
    {   check_immutable(); _documentTypes.add(t); }

    List documentTypes()
    {   return getComponentList(_documentTypes); }

    // Attribute Types
    private List/*<SchemaType.Ref>*/ _attributeTypes = new ArrayList();

    void addAttributeType(SchemaType.Ref t)
    {   check_immutable(); _attributeTypes.add(t); }

    List attributeTypes()
    {   return getComponentList(_attributeTypes); }

    // Identity Constraints
    private List/*<SchemaIdentityConstraint.Ref>*/ _identityConstraints = new ArrayList();

    void addIdentityConstraint(SchemaIdentityConstraint.Ref c)
    {   check_immutable(); _identityConstraints.add(c); }

    List identityConstraints()
    {   return getComponentList(_identityConstraints); }

    // Annotations
    private List/*<SchemaAnnotation>*/ _annotations = new ArrayList();

    void addAnnotation(SchemaAnnotation a)
    {   check_immutable(); _annotations.add(a); }

    List annotations()
    {   return Collections.unmodifiableList(_annotations); }

    private List getComponentList(List referenceList)
    {
        List result = new ArrayList();
        for (int i = 0; i < referenceList.size(); i ++)
            result.add(((SchemaComponent.Ref) referenceList.get(i)).getComponent());
        return Collections.unmodifiableList(result);
    }
}
