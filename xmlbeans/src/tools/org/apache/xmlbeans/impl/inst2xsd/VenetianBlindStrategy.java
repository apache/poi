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
import org.apache.xmlbeans.impl.inst2xsd.util.Type;

import javax.xml.namespace.QName;

/**
 * @author Cezar Andrei ( cezar.andrei at bea.com )
 * Date: Jul 26, 2004
 */
public class VenetianBlindStrategy
    extends RussianDollStrategy
    implements XsdGenStrategy
{
    protected void checkIfReferenceToGlobalTypeIsNeeded(Element elem,
        TypeSystemHolder typeSystemHolder, Inst2XsdOptions options)
    {
        // VenetianBlindDesign defines global complex types
        Type elemType = elem.getType();
        QName elemName = elem.getName();

        if (elemType.isGlobal())
            // is already global, do nothing
            return;

        if (elemType.isComplexType())
        {
            for (int i = 0; ; i++)
            {
                elemType.setName(new QName(elemName.getNamespaceURI(), elemName.getLocalPart() + "Type" + (i==0 ? "" : "" + i)));

                Type candidate = typeSystemHolder.getGlobalType(elemType.getName());
                if (candidate==null)
                {
                    elemType.setGlobal(true);
                    typeSystemHolder.addGlobalType(elemType);
                    break;
                }
                else
                {
                    if (compatibleTypes(candidate, elemType))
                    {
                        combineTypes(candidate, elemType, options);
                        elem.setType(candidate);
                        break;
                    }
                }
            }
        }
    }

    private boolean compatibleTypes(Type elemType, Type candidate)
    {
        // when two types look like they are the same ?

        if (elemType==candidate)
            return true;

//        if (typeIsReferencedInside(elemType, candidate) || typeIsReferencedInside(candidate, elemType))
//            return false;
//
//        if (!elemType.isComplexType() && !candidate.isComplexType())
//            return true;
//
//        if (elemType.isComplexType() && !candidate.isComplexType())
//            return false;
//        if (!elemType.isComplexType() && candidate.isComplexType())
//            return false;
//
//        // both complex after this point
//
//        //todo: be smarter: look at att and elem names and types - compute a difference index

        return true;
    }

//    private boolean typeIsReferencedInside(Type entity, Type container)
//    {
//        for (int i = 0; i < container.getElements().size(); i++)
//        {
//            Element element = (Element) container.getElements().get(i);
//            if (entity==element.getType())
//                return true;
//
//            if (typeIsReferencedInside(entity, element.getType()))
//                return true;
//        }
//
//        for (int i = 0; i < container.getAttributes().size(); i++)
//        {
//            Attribute attribute = (Attribute) container.getAttributes().get(i);
//            if (entity==attribute.getType())
//                return true;
//
//            if (typeIsReferencedInside(entity, attribute.getType()))
//                return true;
//        }
//        return false;
//    }
}