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

import org.apache.xmlbeans.SchemaAnnotation;
import org.apache.xmlbeans.SchemaLocalElement;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaIdentityConstraint;
import org.apache.xmlbeans.soap.SOAPArrayType;
import org.apache.xmlbeans.soap.SchemaWSDLArrayType;

public class SchemaLocalElementImpl extends SchemaParticleImpl
        implements SchemaLocalElement, SchemaWSDLArrayType
{
    private boolean _blockExt;
    private boolean _blockRest;
    private boolean _blockSubst;
    protected boolean _abs;
    private SchemaAnnotation _annotation;
    private SOAPArrayType _wsdlArrayType;
    private SchemaIdentityConstraint.Ref[] _constraints = new SchemaIdentityConstraint.Ref[0];


    public SchemaLocalElementImpl()
    {
        setParticleType(SchemaParticle.ELEMENT);
    }

    public boolean blockExtension()
    {
        return _blockExt;
    }

    public boolean blockRestriction()
    {
        return _blockRest;
    }

    public boolean blockSubstitution()
    {
        return _blockSubst;
    }

    public boolean isAbstract()
    {
        return _abs;
    }

    public void setAbstract(boolean abs)
    {
        _abs = abs;
    }

    public void setBlock(boolean extension, boolean restriction, boolean substitution)
    {
        mutate();
        _blockExt = extension;
        _blockRest = restriction;
        _blockSubst = substitution;
    }

    public void setAnnotation(SchemaAnnotation ann)
    {
        _annotation = ann;
    }

    public void setWsdlArrayType(SOAPArrayType arrayType)
    {
        _wsdlArrayType = arrayType;
    }

    public SchemaAnnotation getAnnotation()
    {
        return _annotation;
    }

    public SOAPArrayType getWSDLArrayType()
    {
        return _wsdlArrayType;
    }

    public void setIdentityConstraints(SchemaIdentityConstraint.Ref[] constraints) {
        mutate();
        _constraints = constraints;
    }

    public SchemaIdentityConstraint[] getIdentityConstraints() {
        SchemaIdentityConstraint[] result = new SchemaIdentityConstraint[_constraints.length];
        for (int i = 0 ; i < result.length ; i++)
            result[i] = _constraints[i].get();
        return result;
    }

    public SchemaIdentityConstraint.Ref[] getIdentityConstraintRefs() {
        SchemaIdentityConstraint.Ref[] result = new SchemaIdentityConstraint.Ref[_constraints.length];
        System.arraycopy(_constraints, 0, result, 0, result.length);
        return result;
    }

}
