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
package org.apache.xmlbeans.impl.inst2xsd;

import org.apache.xmlbeans.impl.inst2xsd.util.Element;
import org.apache.xmlbeans.impl.inst2xsd.util.TypeSystemHolder;

/**
 * @author Cezar Andrei ( cezar.andrei at bea.com )
 * Date: Jul 26, 2004
 */
public class SalamiSliceStrategy
    extends RussianDollStrategy
    implements XsdGenStrategy
{
    protected void checkIfElementReferenceIsNeeded(Element child, String parentNamespace,
        TypeSystemHolder typeSystemHolder, Inst2XsdOptions options)
    {
        // always add element references
        Element referencedElem = new Element();
        referencedElem.setGlobal(true);
        referencedElem.setName(child.getName());
        referencedElem.setType(child.getType());

        if (child.isNillable())
        {
            referencedElem.setNillable(true);
            child.setNillable(false);
        }

        referencedElem = addGlobalElement(referencedElem, typeSystemHolder, options);

        child.setRef(referencedElem); // clears child's type
    }
}
