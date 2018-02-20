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

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.common.PrefixResolver;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.XmlWhitespace;
import org.apache.xmlbeans.impl.inst2xsd.util.Attribute;
import org.apache.xmlbeans.impl.inst2xsd.util.Element;
import org.apache.xmlbeans.impl.inst2xsd.util.TypeSystemHolder;
import org.apache.xmlbeans.impl.inst2xsd.util.Type;
import org.apache.xmlbeans.impl.util.XsTypeConverter;
import org.apache.xmlbeans.impl.values.*;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * @author Cezar Andrei ( cezar.andrei at bea.com )
 * Date: Jul 26, 2004
 */
public class RussianDollStrategy
    implements XsdGenStrategy
{
    static final String _xsi         = "http://www.w3.org/2001/XMLSchema-instance";

    static final QName _xsiNil          = new QName( _xsi, "nil", "xsi" );
    static final QName _xsiType         = new QName( _xsi, "type", "xsi" );

    public void processDoc(XmlObject[] instances, Inst2XsdOptions options, TypeSystemHolder typeSystemHolder)
    {
        for (int i = 0; i < instances.length; i++)
        {
            XmlObject instance = instances[i];
            XmlCursor xc = instance.newCursor();
            // xc on start doc

            StringBuffer comment = new StringBuffer();

            while( !xc.isStart() )
            {
                xc.toNextToken();
                if( xc.isComment() )
                    comment.append(xc.getTextValue());
                else if (xc.isEnddoc())
                    return;
            }
            // xc now on the root element

            Element withElem = processElement(xc, comment.toString(), options, typeSystemHolder);
            withElem.setGlobal(true);

            addGlobalElement(withElem, typeSystemHolder, options);
        }
    }

    protected Element addGlobalElement(Element withElem, TypeSystemHolder typeSystemHolder, Inst2XsdOptions options)
    {
        assert withElem.isGlobal();
        Element intoElem = typeSystemHolder.getGlobalElement(withElem.getName());

        if (intoElem==null)
        {
            typeSystemHolder.addGlobalElement(withElem);
            return withElem;
        }
        else
        {
            combineTypes(intoElem.getType(), withElem.getType(), options);
            combineElementComments(intoElem, withElem);
            return intoElem;
        }
    }

    protected Element processElement(XmlCursor xc, String comment,
        Inst2XsdOptions options, TypeSystemHolder typeSystemHolder)
    {
        assert xc.isStart();
        Element element = new Element();
        element.setName(xc.getName());
        element.setGlobal(false);

        Type elemType = Type.createUnnamedType(Type.SIMPLE_TYPE_SIMPLE_CONTENT); //assume simple, set later
        element.setType(elemType);

        StringBuffer textBuff = new StringBuffer();
        StringBuffer commentBuff = new StringBuffer();
        List children = new ArrayList();
        List attributes = new ArrayList();

        loop: do
        {
            XmlCursor.TokenType tt = xc.toNextToken();
            switch (tt.intValue())
            {
                case XmlCursor.TokenType.INT_ATTR:
                    // todo check for xsi:type
                    // ignore xsi:... attributes other than xsi:nil
                    QName attName = xc.getName();
                    if (!_xsiNil.getNamespaceURI().equals(attName.getNamespaceURI()))
                        attributes.add(processAttribute(xc, options, element.getName().getNamespaceURI(), typeSystemHolder));
                    else if (_xsiNil.equals(attName))
                        element.setNillable(true);

                    break;

                case XmlCursor.TokenType.INT_START:
                    children.add(processElement(xc, commentBuff.toString(), options, typeSystemHolder));
                    commentBuff.delete(0, commentBuff.length());
                    break;

                case XmlCursor.TokenType.INT_TEXT:
                    textBuff.append(xc.getChars());
                    break;

                case XmlCursor.TokenType.INT_COMMENT:
                    commentBuff.append(xc.getTextValue());
                    break;

                case XmlCursor.TokenType.INT_NAMESPACE:
                    // ignore,
                    // each element and attribute will take care to define itself in the right targetNamespace
                    break;

                case XmlCursor.TokenType.INT_END:
                    break loop;

                case XmlCursor.TokenType.INT_PROCINST:
                    // ignore
                    break;

                case XmlCursor.TokenType.INT_ENDDOC:
                    break loop;

                case XmlCursor.TokenType.INT_NONE:
                    break loop;

                case XmlCursor.TokenType.INT_STARTDOC:
                    throw new IllegalStateException();

                default:
                    throw new IllegalStateException("Unknown TokenType.");
            }
        }
        while( true );

        String collapsedText =  XmlWhitespace.collapse(textBuff.toString(), XmlWhitespace.WS_COLLAPSE);

        String commnetStr = (comment == null ?
            ( commentBuff.length() == 0 ? null : commentBuff.toString() ) :
            ( commentBuff.length() == 0 ? comment : commentBuff.insert(0, comment).toString()) );
        element.setComment(commnetStr);

        if (children.size()>0)
        {
            // complex content
            if (collapsedText.length()>0)
            {
                elemType.setContentType(Type.COMPLEX_TYPE_MIXED_CONTENT);
            }
            else
            {
                elemType.setContentType(Type.COMPLEX_TYPE_COMPLEX_CONTENT);
            }
            processElementsInComplexType(elemType, children, element.getName().getNamespaceURI(), typeSystemHolder, options);
            processAttributesInComplexType(elemType, attributes);
        }
        else
        {
            // simple content
            // hack workaround for being able to call xc.getNamespaceForPrefix()
            XmlCursor xcForNamespaces = xc.newCursor();
            xcForNamespaces.toParent();

            if (attributes.size()>0)
            {
                elemType.setContentType(Type.COMPLEX_TYPE_SIMPLE_CONTENT);

                Type extendedType = Type.createNamedType(
                    processSimpleContentType(textBuff.toString(), options, xcForNamespaces), Type.SIMPLE_TYPE_SIMPLE_CONTENT);
                elemType.setExtensionType(extendedType);

                processAttributesInComplexType(elemType, attributes);
            }
            else
            {
                elemType.setContentType(Type.SIMPLE_TYPE_SIMPLE_CONTENT);
                elemType.setName(processSimpleContentType(textBuff.toString(), options, xcForNamespaces));

                // add enumeration value
                String enumValue = XmlString.type.getName().equals(elemType.getName()) ? textBuff.toString() : collapsedText;
                elemType.addEnumerationValue(enumValue, xcForNamespaces);
            }

            xcForNamespaces.dispose(); // end hack
        }

        checkIfReferenceToGlobalTypeIsNeeded( element, typeSystemHolder, options);

        return element;
    }

    protected void processElementsInComplexType(Type elemType, List children, String parentNamespace,
        TypeSystemHolder typeSystemHolder, Inst2XsdOptions options)
    {
        Map elemNamesToElements = new HashMap();
        Element currentElem = null;

        for (Iterator iterator = children.iterator(); iterator.hasNext();)
        {
            Element child = (Element) iterator.next();

            if (currentElem==null)
            {   // first element in this type
                checkIfElementReferenceIsNeeded(child, parentNamespace, typeSystemHolder, options);
                elemType.addElement(child);
                elemNamesToElements.put(child.getName(), child);
                currentElem = child;
                continue;
            }

            if (currentElem.getName()==child.getName())
            {   // same contiguos element
                combineTypes(currentElem.getType(), child.getType(), options); // unify types
                combineElementComments(currentElem, child);
                // minOcc=0 maxOcc=unbounded
                currentElem.setMinOccurs(0);
                currentElem.setMaxOccurs(Element.UNBOUNDED);
            }
            else
            {
                Element sameElem = (Element)elemNamesToElements.get(child.getName());
                if (sameElem==null)
                {   // new element name
                    checkIfElementReferenceIsNeeded(child, parentNamespace, typeSystemHolder, options);
                    elemType.addElement(child);
                    elemNamesToElements.put(child.getName(), child);
                }
                else
                {   //same non contiguos
                    combineTypes(currentElem.getType(), child.getType(), options);
                    combineElementComments(currentElem, child);
                    elemType.setTopParticleForComplexOrMixedContent(Type.PARTICLE_CHOICE_UNBOUNDED);
                }
                currentElem = child;
            }
        }
    }

    protected void checkIfElementReferenceIsNeeded(Element child, String parentNamespace,
        TypeSystemHolder typeSystemHolder, Inst2XsdOptions options)
    {
        if (!child.getName().getNamespaceURI().equals(parentNamespace))
        {
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

    protected void checkIfReferenceToGlobalTypeIsNeeded(Element elem, TypeSystemHolder typeSystemHolder,
        Inst2XsdOptions options)
    {
        // RussianDollDesign doesn't define global types
    }

    protected void processAttributesInComplexType(Type elemType, List attributes)
    {
        assert elemType.isComplexType();
        for (Iterator iterator = attributes.iterator(); iterator.hasNext();)
        {
            Attribute att = (Attribute) iterator.next();
            elemType.addAttribute(att);
        }
    }

    protected Attribute processAttribute(XmlCursor xc, Inst2XsdOptions options, String parentNamespace,
                                              TypeSystemHolder typeSystemHolder)
    {
        assert xc.isAttr() : "xc not on attribute";
        Attribute attribute = new Attribute();
        QName attName = xc.getName();

        attribute.setName(attName);

        XmlCursor parent = xc.newCursor();
        parent.toParent();

        Type simpleContentType = Type.createNamedType(
            processSimpleContentType(xc.getTextValue(), options, parent), Type.SIMPLE_TYPE_SIMPLE_CONTENT);

        parent.dispose();

        attribute.setType(simpleContentType);

        checkIfAttributeReferenceIsNeeded(attribute, parentNamespace, typeSystemHolder);

        return attribute;
    }

    protected void checkIfAttributeReferenceIsNeeded(Attribute attribute, String parentNamespace, TypeSystemHolder typeSystemHolder)
    {
        if (!attribute.getName().getNamespaceURI().equals("") &&
            !attribute.getName().getNamespaceURI().equals(parentNamespace))
        {
            // make attribute be a reference to a top level attribute in a different targetNamespace
            Attribute referencedAtt = new Attribute();
            referencedAtt.setGlobal(true);
            referencedAtt.setName(attribute.getName());
            referencedAtt.setType(attribute.getType());

            typeSystemHolder.addGlobalAttribute(referencedAtt);

            attribute.setRef(referencedAtt);
        }
    }

    protected class SCTValidationContext
        implements ValidationContext
    {
        protected boolean valid = true;

        public boolean isValid()
        {
            return valid;
        }

        public void resetToValid()
        {
            valid = true;
        }

        public void invalid(String message)
        {
            valid = false;
        }

        public void invalid(String code, Object[] args)
        {
            valid = false;
        }
    }

    private SCTValidationContext _validationContext = new SCTValidationContext();


    // List of precedence for smart simple primitive type determination
    // byte, short, int, long, integer, float, double, decimal,
    // boolean
    // date, dateTime, time, gDuration,
    // QName ?,
    // anyUri ? - triggered only for http:// or www. constructs,
    // list types ?
    // string
    protected QName processSimpleContentType(String lexicalValue, Inst2XsdOptions options, final XmlCursor xc)
    {
        // check options and return xsd:string or if smart is enabled, look for a better type
        if (options.getSimpleContentTypes()==Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING)
            return XmlString.type.getName();

        if (options.getSimpleContentTypes()!=Inst2XsdOptions.SIMPLE_CONTENT_TYPES_SMART)
            throw new IllegalArgumentException("Unknown value for Inst2XsdOptions.getSimpleContentTypes() :" + options.getSimpleContentTypes());

        // Inst2XsdOptions.SIMPLE_CONTENT_TYPES_SMART case


        try
        {
            XsTypeConverter.lexByte(lexicalValue);
            return XmlByte.type.getName();
        }
        catch (Exception e) {}

        try
        {
            XsTypeConverter.lexShort(lexicalValue);
            return XmlShort.type.getName();
        }
        catch (Exception e) {}

        try
        {
            XsTypeConverter.lexInt(lexicalValue);
            return XmlInt.type.getName();
        }
        catch (Exception e) {}

        try
        {
            XsTypeConverter.lexLong(lexicalValue);
            return XmlLong.type.getName();
        }
        catch (Exception e) {}

        try
        {
            XsTypeConverter.lexInteger(lexicalValue);
            return XmlInteger.type.getName();
        }
        catch (Exception e) {}

        try
        {
            XsTypeConverter.lexFloat(lexicalValue);
            return XmlFloat.type.getName();
        }
        catch (Exception e) {}

//        // this not needed because it's lexical space is covered by float
//        try
//        {
//            XsTypeConverter.lexDouble(lexicalValue);
//            return XmlDouble.type.getName();
//        }
//        catch (Exception e) {}
//
//        try
//        {
//            XsTypeConverter.lexDecimal(lexicalValue);
//            return XmlDecimal.type.getName();
//        }
//        catch (Exception e) {}

        XmlDateImpl.validateLexical(lexicalValue, XmlDate.type, _validationContext);
        if (_validationContext.isValid())
            return XmlDate.type.getName();
        _validationContext.resetToValid();

        XmlDateTimeImpl.validateLexical(lexicalValue, XmlDateTime.type, _validationContext);
        if (_validationContext.isValid())
            return XmlDateTime.type.getName();
        _validationContext.resetToValid();

        XmlTimeImpl.validateLexical(lexicalValue, XmlTime.type, _validationContext);
        if (_validationContext.isValid())
            return XmlTime.type.getName();
        _validationContext.resetToValid();

        XmlDurationImpl.validateLexical(lexicalValue, XmlDuration.type, _validationContext);
        if (_validationContext.isValid())
            return XmlDuration.type.getName();
        _validationContext.resetToValid();

        // check for uri
        if (lexicalValue.startsWith("http://") || lexicalValue.startsWith("www."))
        {
            XmlAnyUriImpl.validateLexical(lexicalValue, _validationContext);
            if (_validationContext.isValid())
                return XmlAnyURI.type.getName();
            _validationContext.resetToValid();
        }

        // check for QName
        int idx = lexicalValue.indexOf(':');
        if (idx>=0 && idx==lexicalValue.lastIndexOf(':') && idx+1<lexicalValue.length())
        {
            PrefixResolver prefixResolver = new PrefixResolver()
            {
                public String getNamespaceForPrefix(String prefix)
                {  return xc.namespaceForPrefix(prefix); }
            };

            QName qname = XmlQNameImpl.validateLexical(lexicalValue, _validationContext, prefixResolver);
            if (_validationContext.isValid())
                return XmlQName.type.getName();
            _validationContext.resetToValid();
        }

        //the check for lists is probably too expensive

        return XmlString.type.getName();
    }


    protected void combineTypes(Type into, Type with, Inst2XsdOptions options)
    {
        if (into==with)
            return;

        if (into.isGlobal() && with.isGlobal() && into.getName().equals(with.getName()))
            return;


        if (into.getContentType()==Type.SIMPLE_TYPE_SIMPLE_CONTENT &&
            with.getContentType()==Type.SIMPLE_TYPE_SIMPLE_CONTENT)
        {
            combineSimpleTypes(into, with, options);
            return;
        }

        if ((into.getContentType()==Type.SIMPLE_TYPE_SIMPLE_CONTENT ||
            into.getContentType()==Type.COMPLEX_TYPE_SIMPLE_CONTENT) &&
            (with.getContentType()==Type.SIMPLE_TYPE_SIMPLE_CONTENT ||
            with.getContentType()==Type.COMPLEX_TYPE_SIMPLE_CONTENT) )
        {
            // take the extension name if it's a complex type
            QName intoTypeName = into.isComplexType() ? into.getExtensionType().getName() : into.getName();
            QName withTypeName = with.isComplexType() ? with.getExtensionType().getName() : with.getName();

            //complex type simple content
            into.setContentType(Type.COMPLEX_TYPE_SIMPLE_CONTENT);

            QName moreGeneralTypeName = combineToMoreGeneralSimpleType(intoTypeName, withTypeName);
            if (into.isComplexType())
            {
                Type extendedType = Type.createNamedType(moreGeneralTypeName, Type.SIMPLE_TYPE_SIMPLE_CONTENT);
                into.setExtensionType(extendedType);
            }
            else
                into.setName(moreGeneralTypeName);

            combineAttributesOfTypes(into, with);
            return;
        }

        if (into.getContentType()==Type.COMPLEX_TYPE_COMPLEX_CONTENT &&
            with.getContentType()==Type.COMPLEX_TYPE_COMPLEX_CONTENT)
        {
            combineAttributesOfTypes(into, with);
            combineElementsOfTypes(into, with, false, options);
            return;
        }

        if (into.getContentType()==Type.SIMPLE_TYPE_SIMPLE_CONTENT ||
            into.getContentType()==Type.COMPLEX_TYPE_SIMPLE_CONTENT ||
            with.getContentType()==Type.SIMPLE_TYPE_SIMPLE_CONTENT ||
            with.getContentType()==Type.COMPLEX_TYPE_SIMPLE_CONTENT)
        {
            into.setContentType(Type.COMPLEX_TYPE_MIXED_CONTENT);
            combineAttributesOfTypes(into, with);
            combineElementsOfTypes(into, with, true, options);
            return;
        }

        if ((into.getContentType()==Type.SIMPLE_TYPE_SIMPLE_CONTENT ||
            into.getContentType()==Type.COMPLEX_TYPE_SIMPLE_CONTENT ||
            into.getContentType()==Type.COMPLEX_TYPE_COMPLEX_CONTENT ||
            into.getContentType()==Type.COMPLEX_TYPE_MIXED_CONTENT) &&
            (with.getContentType()==Type.SIMPLE_TYPE_SIMPLE_CONTENT ||
            with.getContentType()==Type.COMPLEX_TYPE_SIMPLE_CONTENT ||
            with.getContentType()==Type.COMPLEX_TYPE_COMPLEX_CONTENT ||
            with.getContentType()==Type.COMPLEX_TYPE_MIXED_CONTENT) )
        {
            into.setContentType(Type.COMPLEX_TYPE_MIXED_CONTENT);
            combineAttributesOfTypes(into, with);
            combineElementsOfTypes(into, with, false, options);
            return;
        }

        throw new IllegalArgumentException("Unknown content type.");
    }

    protected void combineSimpleTypes(Type into, Type with, Inst2XsdOptions options)
    {
        assert (into.getContentType()==Type.SIMPLE_TYPE_SIMPLE_CONTENT &&
            with.getContentType()==Type.SIMPLE_TYPE_SIMPLE_CONTENT) : "Invalid arguments";

        //simple type simple content
        into.setName(combineToMoreGeneralSimpleType(into.getName(), with.getName()));

        // take care of enumeration values
        if (options.isUseEnumerations())
        {
            into.addAllEnumerationsFrom(with);

            if (into.getEnumerationValues().size()>options.getUseEnumerations())
            {
                into.closeEnumeration();
            }
        }
    }

    protected QName combineToMoreGeneralSimpleType(QName t1, QName t2)
    {
        if (t1.equals(t2))
            return t1;

        if (t2.equals(XmlShort.type.getName()) && t1.equals(XmlByte.type.getName()))
            return t2;
        if (t1.equals(XmlShort.type.getName()) && t2.equals(XmlByte.type.getName()))
            return t1;

        if (t2.equals(XmlInt.type.getName()) &&
            (t1.equals(XmlShort.type.getName()) || t1.equals(XmlByte.type.getName())) )
            return t2;
        if (t1.equals(XmlInt.type.getName()) &&
            (t2.equals(XmlShort.type.getName()) || t2.equals(XmlByte.type.getName())) )
            return t1;

        if (t2.equals(XmlLong.type.getName()) &&
            (t1.equals(XmlInt.type.getName()) || t1.equals(XmlShort.type.getName()) || t1.equals(XmlByte.type.getName())) )
            return t2;
        if (t1.equals(XmlLong.type.getName()) &&
            (t2.equals(XmlInt.type.getName()) || t2.equals(XmlShort.type.getName()) || t2.equals(XmlByte.type.getName())) )
            return t1;

        if (t2.equals(XmlInteger.type.getName()) &&
            (t1.equals(XmlLong.type.getName()) || t1.equals(XmlInt.type.getName()) ||
            t1.equals(XmlShort.type.getName()) || t1.equals(XmlByte.type.getName())) )
            return t2;
        if (t1.equals(XmlInteger.type.getName()) &&
            (t2.equals(XmlLong.type.getName()) || t2.equals(XmlInt.type.getName()) ||
            t2.equals(XmlShort.type.getName()) || t2.equals(XmlByte.type.getName())) )
            return t1;

        if (t2.equals(XmlFloat.type.getName()) &&
            (t1.equals(XmlInteger.type.getName()) ||
            t1.equals(XmlLong.type.getName()) || t1.equals(XmlInt.type.getName()) ||
            t1.equals(XmlShort.type.getName()) || t1.equals(XmlByte.type.getName())) )
            return t2;
        if (t1.equals(XmlFloat.type.getName()) &&
            (t2.equals(XmlInteger.type.getName()) ||
            t2.equals(XmlLong.type.getName()) || t2.equals(XmlInt.type.getName()) ||
            t2.equals(XmlShort.type.getName()) || t2.equals(XmlByte.type.getName())) )
            return t1;

        //double, decimal will never get here since they don't get generated

        //the rest of the combinations are not compatible, so they will combine in xsd:string
        return XmlString.type.getName();
    }

    protected void combineAttributesOfTypes(Type into, Type from)
    {
        // loop through attributes: add fromAtt if they don't exist, combine them if they exist
        outterLoop:
        for (int i = 0; i < from.getAttributes().size(); i++)
        {
            Attribute fromAtt = (Attribute)from.getAttributes().get(i);
            for (int j = 0; j < into.getAttributes().size(); j++)
            {
                Attribute intoAtt = (Attribute)into.getAttributes().get(j);
                if (intoAtt.getName().equals(fromAtt.getName()))
                {
                    intoAtt.getType().setName(
                        combineToMoreGeneralSimpleType(intoAtt.getType().getName(), fromAtt.getType().getName()));
                    continue outterLoop;
                }
            }
            // fromAtt doesn't exist in into type, will add it right now
            into.addAttribute(fromAtt);
        }

        //optional attributes: if there are atts in into that are not in from, make them optional
        outterLoop:
        for (int i = 0; i < into.getAttributes().size(); i++)
        {
            Attribute intoAtt = (Attribute)into.getAttributes().get(i);
            for (int j = 0; j < from.getAttributes().size(); j++)
            {
                Attribute fromAtt = (Attribute)from.getAttributes().get(j);
                if (fromAtt.getName().equals(intoAtt.getName()))
                {
                    continue;
                }
            }
            // intoAtt doesn't exist in into type, will add it right now
            intoAtt.setOptional(true);
        }
    }

    protected void combineElementsOfTypes(Type into, Type from, boolean makeElementsOptional, Inst2XsdOptions options)
    {
        boolean needsUnboundedChoice = false;

        if (into.getTopParticleForComplexOrMixedContent()!=Type.PARTICLE_SEQUENCE ||
            from.getTopParticleForComplexOrMixedContent()!=Type.PARTICLE_SEQUENCE)
            needsUnboundedChoice = true;

        List res = new ArrayList();

        int fromStartingIndex = 0;
        int fromMatchedIndex = -1;
        int intoMatchedIndex = -1;

        // for each element in into
        for (int i = 0; !needsUnboundedChoice && i < into.getElements().size(); i++)
        {
            // try to find one with same name in from
            Element intoElement = (Element) into.getElements().get(i);
            for (int j = fromStartingIndex; j < from.getElements().size(); j++)
            {
                Element fromElement = (Element) from.getElements().get(j);
                if (intoElement.getName().equals(fromElement.getName()))
                {
                    fromMatchedIndex = j;
                    break;
                }
            }

            // if not found, it's safe to add this one to result 'res' (as optional) and continue
            if ( fromMatchedIndex < fromStartingIndex )
            {
                res.add(intoElement);
                intoElement.setMinOccurs(0);
                continue;
            }

            // else try out all from elemens between fromStartingIndex to fromMatchedIndex
            // to see if they match one of the into elements
            intoMatchingLoop:
            for (int j2 = fromStartingIndex; j2 < fromMatchedIndex; j2++)
            {
                Element fromCandidate = (Element) from.getElements().get(j2);

                for (int i2 = i+1; i2 < into.getElements().size(); i2++)
                {
                    Element intoCandidate = (Element) into.getElements().get(i2);
                    if (fromCandidate.getName().equals(intoCandidate.getName()))
                    {
                        intoMatchedIndex = i2;
                        break intoMatchingLoop;
                    }
                }
            }

            if (intoMatchedIndex<i)
            {
                // if none matched they are safe to be added to res as optional
                for (int j3 = fromStartingIndex; j3 < fromMatchedIndex; j3++)
                {
                    Element fromCandidate = (Element) from.getElements().get(j3);
                    res.add(fromCandidate);
                    fromCandidate.setMinOccurs(0);
                }
                // also since into[i] == from[fromMatchedIndex] add it only once
                res.add(intoElement);
                Element fromMatchedElement = (Element)from.getElements().get(fromMatchedIndex);

                if (fromMatchedElement.getMinOccurs()<=0)
                    intoElement.setMinOccurs(0);
                if (fromMatchedElement.getMaxOccurs()==Element.UNBOUNDED)
                    intoElement.setMaxOccurs(Element.UNBOUNDED);

                combineTypes(intoElement.getType(), fromMatchedElement.getType(), options);
                combineElementComments(intoElement, fromMatchedElement);

                fromStartingIndex = fromMatchedIndex + 1;
                continue;
            }
            else
            {
                // if matched it means into type will transform into a choice unbounded type
                needsUnboundedChoice = true;
            }
        }

        for (int j = fromStartingIndex; j < from.getElements().size(); j++)
        {
            Element remainingFromElement = (Element) from.getElements().get(j);
            res.add(remainingFromElement);
            remainingFromElement.setMinOccurs(0);
        }

        // if choice was detected
        if (needsUnboundedChoice)
        {
            into.setTopParticleForComplexOrMixedContent(Type.PARTICLE_CHOICE_UNBOUNDED);

            outterLoop:
            for (int j = 0; j < from.getElements().size(); j++)
            {
                Element fromElem = (Element) from.getElements().get(j);
                for (int i = 0; i < into.getElements().size(); i++)
                {
                    Element intoElem = (Element)into.getElements().get(i);
                    intoElem.setMinOccurs(1);
                    intoElem.setMaxOccurs(1);

                    if (intoElem==fromElem)
                        continue outterLoop;

                    if (intoElem.getName().equals(fromElem.getName()))
                    {
                        combineTypes(intoElem.getType(), fromElem.getType(), options);
                        combineElementComments(intoElem, fromElem);

                        continue outterLoop;
                    }
                }

                // fromElem doesn't exist in into type, will add it right now
                into.addElement(fromElem);
                fromElem.setMinOccurs(1);
                fromElem.setMaxOccurs(1);
            }
            return;
        }
        else
        {
            // into remains sequence but will contain the new list of elements res
            into.setElements(res);
            return;
        }
    }

    protected void combineElementComments(Element into, Element with)
    {
        if (with.getComment()!=null && with.getComment().length()>0)
        {
            if (into.getComment()==null)
                into.setComment(with.getComment());
            else
                into.setComment(into.getComment() + with.getComment());
        }
    }
}
