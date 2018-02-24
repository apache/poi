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

import org.apache.xmlbeans.impl.common.NameUtil;
import org.apache.xmlbeans.QNameSetBuilder;
import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.UserType;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.SchemaStringEnumEntry;
import org.apache.xmlbeans.XmlByte;
import org.apache.xmlbeans.XmlShort;
import org.apache.xmlbeans.InterfaceExtension;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.PrePostExtension;
import org.apache.xmlbeans.BindingConfig;

import java.util.*;
import java.math.BigInteger;

import javax.xml.namespace.QName;

public class StscJavaizer
{

    /**
     * XMLBEANS-307
     * if enumeration count is greater than 3668,
     * xmlbeans scomp will fail with a code too large error
     */
    private static final int MAX_ENUM_COUNT = 3668;
    /**
     * Does a topo walk of all the types to resolve them.
     */
    public static void javaizeAllTypes(boolean javaize)
    {
        StscState state = StscState.get();

        List allSeenTypes = new ArrayList();
        allSeenTypes.addAll(Arrays.asList(state.documentTypes()));
        allSeenTypes.addAll(Arrays.asList(state.attributeTypes()));
        allSeenTypes.addAll(Arrays.asList(state.globalTypes()));

        // First distribute the global names among the top entities.
        if (javaize)
            assignGlobalJavaNames(allSeenTypes);

        // now fully javaize everything deeply.
        for (int i = 0; i < allSeenTypes.size(); i++)
        {
            SchemaType gType = (SchemaType)allSeenTypes.get(i);
            if (javaize)
            {
                javaizeType((SchemaTypeImpl)gType);
                String className = gType.getFullJavaName();
                if (className != null)
                    state.addClassname(className.replace('$', '.'), gType);
            }
            else
                skipJavaizingType((SchemaTypeImpl)gType);
            allSeenTypes.addAll(Arrays.asList(gType.getAnonymousTypes()));
            // We need to javaize the anonymous types defined inside redefined types
            // since redefined type do not get a Java class of their own.
            // The exception is complex types derived by restriction, since in this case
            // anonymous types are not inherited
            addAnonymousTypesFromRedefinition(gType, allSeenTypes);
        }
    }

    static void assignGlobalJavaNames(Collection schemaTypes)
    {
        HashSet usedNames = new HashSet();
        StscState state = StscState.get();

        for (Iterator i = schemaTypes.iterator(); i.hasNext(); )
        {
            SchemaTypeImpl sImpl = (SchemaTypeImpl)i.next();
            QName topName = findTopName(sImpl);
            String pickedName = state.getJavaname(topName, sImpl.isDocumentType() ?
                BindingConfig.QNAME_DOCUMENT_TYPE : BindingConfig.QNAME_TYPE);
            if (sImpl.isUnjavaized())
            {
                sImpl.setFullJavaName(pickFullJavaClassName(usedNames, findTopName(sImpl), pickedName, sImpl.isDocumentType(), sImpl.isAttributeType()));
                sImpl.setFullJavaImplName(pickFullJavaImplName(usedNames, sImpl.getFullJavaName()));

                setUserTypes(sImpl, state);

                setExtensions(sImpl, state);
            }
        }

        verifyInterfaceNameCollisions(usedNames, state);
    }

    private static void verifyInterfaceNameCollisions(Set usedNames, StscState state)
    {
        BindingConfig config = state.getBindingConfig();
        if (config == null)
            return;

        InterfaceExtension[] exts = config.getInterfaceExtensions();
        for (int i = 0; i < exts.length; i++)
        {
            if (usedNames.contains(exts[i].getInterface().toLowerCase()))
                state.error("InterfaceExtension interface '" + exts[i].getInterface() + "' creates a name collision with one of the generated interfaces or classes.", XmlError.SEVERITY_ERROR, null);

            String handler = exts[i].getStaticHandler();
            if (handler != null && usedNames.contains(handler.toLowerCase()))
                state.error("InterfaceExtension handler class '" + handler + "' creates a name collision with one of the generated interfaces or classes.", XmlError.SEVERITY_ERROR, null);
        }

        PrePostExtension[] prepost = config.getPrePostExtensions();
        for (int i = 0; i < prepost.length; i++)
        {
            String handler = prepost[i].getStaticHandler();
            if (handler != null && usedNames.contains(handler.toLowerCase()))
                state.error("PrePostExtension handler class '" + handler + "' creates a name collision with one of the generated interfaces or classes.", XmlError.SEVERITY_ERROR, null);
        }
    }

    private static void setUserTypes(SchemaTypeImpl sImpl, StscState state)
    {
        BindingConfig config = state.getBindingConfig();

        if (config != null)
        {
            UserType utype = config.lookupUserTypeForQName(sImpl.getName());
            if (utype != null)
            {
                sImpl.setUserTypeName(utype.getJavaName());
                sImpl.setUserTypeHandlerName(utype.getStaticHandler());
            }
        }
    }

    private static void setExtensions(SchemaTypeImpl sImpl, StscState state)
    {
        String javaName = sImpl.getFullJavaName();
        BindingConfig config = state.getBindingConfig();

        if (javaName != null && config != null)
        {
            sImpl.setInterfaceExtensions(config.getInterfaceExtensions(javaName));
            sImpl.setPrePostExtension(config.getPrePostExtension(javaName));
        }
    }

    private static boolean isStringType(SchemaType type)
    {
        if (type == null || type.getSimpleVariety() != SchemaType.ATOMIC)
            return false;
        return (type.getPrimitiveType().getBuiltinTypeCode() == SchemaType.BTC_STRING);
    }

    static String pickConstantName(Set usedNames, String words)
    {
        String base = NameUtil.upperCaseUnderbar(words);

        if (base.length() == 0)
        {
            base = "X";
        }

        if (base.startsWith("INT_")) // reserved for int codes
        {
            base = "X_" + base;
        }

        String uniqName;
        int index = 1;
        for (uniqName = base; usedNames.contains(uniqName); )
        {
            index++;
            uniqName = base + "_" + index;
        }

        usedNames.add(uniqName);

        return uniqName;
    }

    static void skipJavaizingType(SchemaTypeImpl sImpl)
    {
        if (sImpl.isJavaized())
            return;
        
        SchemaTypeImpl baseType = (SchemaTypeImpl)sImpl.getBaseType();
        if (baseType != null)
            skipJavaizingType(baseType);
        
        sImpl.startJavaizing();
        secondPassProcessType(sImpl);
        sImpl.finishJavaizing();
    }

    static void secondPassProcessType(SchemaTypeImpl sImpl)
    {
        if (isStringType(sImpl))
        {
            XmlAnySimpleType[] enumVals = sImpl.getEnumerationValues();

            // if this is an enumerated string type, values are to be
            // javaized as constants.
            if (enumVals != null)
            {
                //ERROR is found at > 3668
                if (enumVals.length > MAX_ENUM_COUNT)
                {
                    StscState.get().warning("SchemaType Enumeration found with too many enumeration values " +
                        "to create a Java enumeration. The base SchemaType \"" +
                        sImpl.getBaseEnumType() + "\" will be used instead", XmlError.SEVERITY_WARNING, null);

                    sImpl = (SchemaTypeImpl) sImpl.getBaseEnumType();
                }
                else
                {
                    SchemaStringEnumEntry[] entryArray = new SchemaStringEnumEntry[enumVals.length];
                    SchemaType basedOn = sImpl.getBaseEnumType();
                    if (basedOn == sImpl)
                    {
                        Set usedNames = new HashSet();
                        for (int i = 0; i < enumVals.length; i++)
                        {
                            String val = enumVals[i].getStringValue();

                            entryArray[i] = new SchemaStringEnumEntryImpl(val, i + 1, pickConstantName(usedNames, val));
                        }
                    }
                    else
                    {
                        for (int i = 0; i < enumVals.length; i++)
                        {
                            String val = enumVals[i].getStringValue();
                            entryArray[i] = basedOn.enumEntryForString(val);
                        }
                    }
                    sImpl.setStringEnumEntries(entryArray);
                }
            }
        }
    }

    static void javaizeType(SchemaTypeImpl sImpl)
    {
        if (sImpl.isJavaized())
            return;

        SchemaTypeImpl baseType = (SchemaTypeImpl)sImpl.getBaseType();
        if (baseType != null)
            javaizeType(baseType);
        if (sImpl.getContentBasedOnType() != null && sImpl.getContentBasedOnType() != baseType)
            javaizeType((SchemaTypeImpl) sImpl.getContentBasedOnType());

        sImpl.startJavaizing();

        sImpl.setCompiled(true);

        secondPassProcessType(sImpl);

        if (!sImpl.isSimpleType())
        {
            SchemaProperty[] eltProps = sImpl.getElementProperties();
            SchemaProperty[] attrProps = sImpl.getAttributeProperties();

            // Handing out java names - this permits us to avoid collisions.
            Set usedPropNames = new HashSet();

            // First, copy all used property names from base, since these
            // cannnot be changed at this point and they may be arbitrary
            // because of derivation by restriction and the "nopvr" switch
            SchemaProperty[] baseProps = baseType.getProperties();
            for (int i = 0; i < baseProps.length; i++)
            {
                String name = baseProps[i].getJavaPropertyName();
                assert !usedPropNames.contains(name);
                usedPropNames.add(name);
            }

            // count in the methods from extension interfaces
            avoidExtensionMethods(usedPropNames, sImpl);

            // Assign names in two passes: first inherited names, then others.
            for (boolean doInherited = true; ; doInherited = false)
            {
                if (eltProps.length > 0)
                    assignJavaPropertyNames(usedPropNames, eltProps, baseType, doInherited);

                assignJavaPropertyNames(usedPropNames, attrProps, baseType, doInherited);

                if (doInherited == false)
                    break;
            }

            SchemaProperty[] allprops = sImpl.getProperties();

            // determine whether order insensitive
            boolean insensitive = isPropertyModelOrderInsensitive(allprops);

            // Fill in the java type codes now.
            // This depends on recursive type information, so it's done in typechecking
            assignJavaTypeCodes(allprops);

            sImpl.setOrderSensitive(!insensitive);
        }

        // assign java type names to anonymous types
        // for redefined types, this step was performed when javaizing the redefinition
        if (sImpl.getFullJavaName() != null || sImpl.getOuterType() != null)
            assignJavaAnonymousTypeNames(sImpl);

        sImpl.finishJavaizing();
    }

    private static final String[] PREFIXES = new String[]{"get", "xget", "isNil", "isSet", "sizeOf", "set",
                "xset", "addNew", "setNil", "unset", "insert", "add", "insertNew", "addNew", "remove"};

    private static void avoidExtensionMethods(Set usedPropNames, SchemaTypeImpl sImpl)
    {
        InterfaceExtension[] exts = sImpl.getInterfaceExtensions();
        if (exts != null) for (int i = 0; i < exts.length; i++)
        {
            InterfaceExtension ext = exts[i];
            InterfaceExtension.MethodSignature[] methods = ext.getMethods();
            for (int j = 0; j < methods.length; j++)
            {
                String methodName = methods[j].getName();
                for (int k = 0; k < PREFIXES.length; k++)
                {
                    String prefix = PREFIXES[k];
                    if (methodName.startsWith(prefix))
                        usedPropNames.add(methodName.substring(prefix.length()));
                }
            }
        }
    }

    static void assignJavaAnonymousTypeNames(SchemaTypeImpl outerType)
    {
        Set usedTypeNames = new HashSet();
        SchemaType[] anonymousTypes = outerType.getAnonymousTypes();
        StscState state = StscState.get();

        int nrOfAnonTypes = anonymousTypes.length;
        if (outerType.isRedefinition())
        {
            // We have to add the anonymous types for redefinitions to the list
            // since they don't have another outer class
            ArrayList list = new ArrayList();
            addAnonymousTypesFromRedefinition(outerType, list);
            if (list.size() > 0)
            {
                SchemaType[] temp = new SchemaType[nrOfAnonTypes + list.size()];
                list.toArray(temp);
                System.arraycopy(anonymousTypes, 0, temp, list.size(), nrOfAnonTypes);
                anonymousTypes = temp;
            }
        }

        // Because we generate nested java interfaces, and nested
        // interface names must not be the same as an ancestor, use up
        // the ancestors

        for ( SchemaType scanOuterType = outerType ;
              scanOuterType != null ;
              scanOuterType = scanOuterType.getOuterType() )
        {
            usedTypeNames.add( scanOuterType.getShortJavaName() );
        }

        for ( SchemaType scanOuterType = outerType ;
              scanOuterType != null ;
              scanOuterType = scanOuterType.getOuterType() )
        {
            usedTypeNames.add( scanOuterType.getShortJavaImplName() );
        }

        // and because things are problematic if an inner type name
        // is the same as a top-level package name, also get rid of that
        // collision
        usedTypeNames.add(getOutermostPackage(outerType.getFullJavaName()));

        // assign names
        for (int i = 0; i < anonymousTypes.length; i++)
        {
            SchemaTypeImpl sImpl = (SchemaTypeImpl)anonymousTypes[i];
            if (sImpl == null) // already handled in first pass
                continue;
            if (sImpl.isSkippedAnonymousType())
                continue;
            String localname = null;
            String javaname = null;

            SchemaField containerField = sImpl.getContainerField();
            if (containerField != null)
            {
                QName qname = sImpl.getContainerField().getName();
                localname = qname.getLocalPart();
                javaname = state.getJavaname(sImpl.getContainerField().getName(), BindingConfig.QNAME_TYPE);
            }
            else
            {
                // not defined inside an Elt or Attr: must be a nested simple type
                switch (sImpl.getOuterType().getSimpleVariety())
                {
                    case SchemaType.UNION:
                        javaname = "Member"; break;
                    case SchemaType.LIST:
                        javaname = "Item"; break;
                    case SchemaType.ATOMIC:
                    default:
                        assert(false) : "Weird type " + sImpl.toString();
                        javaname = "Base"; break;
                }
            }

            if (i < nrOfAnonTypes)
            {
                sImpl.setShortJavaName(
                    pickInnerJavaClassName(usedTypeNames, localname, javaname));
                sImpl.setShortJavaImplName(
                    pickInnerJavaImplName(usedTypeNames, localname, javaname == null ? null : javaname + "Impl"));
            }
            else
            {
                // This comes from redefined types, so we have to compute the
                // full name here
                sImpl.setFullJavaName(outerType.getFullJavaName() + "$" +
                    pickInnerJavaClassName(usedTypeNames, localname, javaname));
                sImpl.setFullJavaImplName(outerType.getFullJavaImplName() + "$" +
                    pickInnerJavaImplName(usedTypeNames, localname, javaname == null ? null : javaname + "Impl"));
            }

            // TODO(radup) why is this inside this loop here?
            setExtensions(sImpl, state);
        }
    }

    static void assignJavaPropertyNames(Set usedNames, SchemaProperty[] props, SchemaType baseType, boolean doInherited)
    {
        StscState state = StscState.get();

        // two passes: first deal with inherited properties, then with new ones.
        // this ensures that we match up with base class definitions cleanly
        // BUGBUG(radup) We have to look for particles that have been removed
        // in the derivation tree for this type using derivation by restriction,
        // because they have not been removed in Java and may collide with 
        // this type's properties.

        for (int i = 0; i < props.length; i++)
        {
            SchemaPropertyImpl sImpl = (SchemaPropertyImpl)props[i];

            SchemaProperty baseProp =
               (sImpl.isAttribute() ?
                    baseType.getAttributeProperty(sImpl.getName()) :
                    baseType.getElementProperty(sImpl.getName()));

            if ((baseProp != null) != doInherited)
                continue;

            QName propQName = sImpl.getName();

            String theName;

            if (baseProp == null)
                theName = pickJavaPropertyName(usedNames, propQName.getLocalPart(),
                    state.getJavaname(propQName, sImpl.isAttribute() ? BindingConfig.QNAME_ACCESSOR_ATTRIBUTE :
                    BindingConfig.QNAME_ACCESSOR_ELEMENT));
            else
                theName = baseProp.getJavaPropertyName();

            sImpl.setJavaPropertyName(theName);

            boolean isArray = (sImpl.getMaxOccurs() == null ||
                sImpl.getMaxOccurs().compareTo(BigInteger.ONE) > 0);
            boolean isSingleton = !isArray && (sImpl.getMaxOccurs().signum() > 0);
            boolean isOption = isSingleton && (sImpl.getMinOccurs().signum() == 0);
            SchemaType javaBasedOnType = sImpl.getType();

            if (baseProp != null)
            {
                if (baseProp.extendsJavaArray())
                {
                    isSingleton = false;
                    isOption = false;
                    isArray = true;
                }
                if (baseProp.extendsJavaSingleton())
                {
                    isSingleton = true;
                }
                if (baseProp.extendsJavaOption())
                {
                    isOption = true;
                }
                javaBasedOnType = baseProp.javaBasedOnType();
            }

            sImpl.setExtendsJava(javaBasedOnType.getRef(), isSingleton, isOption, isArray);
        }

    }

    static void assignJavaTypeCodes(SchemaProperty[] properties)
    {
        for (int i = 0; i < properties.length; i++)
        {
            SchemaPropertyImpl sImpl = (SchemaPropertyImpl)properties[i];
            SchemaType sType = sImpl.javaBasedOnType();
            sImpl.setJavaTypeCode(javaTypeCodeForType(sType));
        }
    }

    static int javaTypeCodeInCommon(SchemaType[] types)
    {
        if (types == null || types.length == 0)
            return SchemaProperty.XML_OBJECT;

        int code = javaTypeCodeForType(types[0]);
        if (code == SchemaProperty.JAVA_OBJECT)
            return code;
        for (int i = 1; i < types.length; i++)
        {
            // if any two are different, the answer is java.lang.Object
            if (code != javaTypeCodeForType(types[i]))
                return SchemaProperty.JAVA_OBJECT;
        }
        return code;
    }

    static int javaTypeCodeForType(SchemaType sType)
    {
        if (!sType.isSimpleType())
            return SchemaProperty.XML_OBJECT;

        if (((SchemaTypeImpl)sType).getUserTypeHandlerName() != null)
            return SchemaProperty.JAVA_USER;

        if (sType.getSimpleVariety() == SchemaType.UNION)
        {
            // see if we can find an interesting common base type, e.g., for string enums
            SchemaType baseType = sType.getUnionCommonBaseType();
            if (baseType != null && !baseType.isURType())
                sType = baseType;
            else
                return javaTypeCodeInCommon(sType.getUnionConstituentTypes());
        }

        if (sType.getSimpleVariety() == SchemaType.LIST)
            return SchemaProperty.JAVA_LIST;

        if (sType.isURType())
            return SchemaProperty.XML_OBJECT;

        switch (sType.getPrimitiveType().getBuiltinTypeCode())
        {
            case SchemaType.BTC_ANY_SIMPLE:
                // return SchemaProperty.XML_OBJECT;
                return SchemaProperty.JAVA_STRING;

            case SchemaType.BTC_BOOLEAN:
                return SchemaProperty.JAVA_BOOLEAN;

            case SchemaType.BTC_BASE_64_BINARY:
                return SchemaProperty.JAVA_BYTE_ARRAY;

            case SchemaType.BTC_HEX_BINARY:
                return SchemaProperty.JAVA_BYTE_ARRAY;

            case SchemaType.BTC_ANY_URI:
                return SchemaProperty.JAVA_STRING;

            case SchemaType.BTC_QNAME:
                return SchemaProperty.JAVA_QNAME;

            case SchemaType.BTC_NOTATION:
                return SchemaProperty.XML_OBJECT;

            case SchemaType.BTC_FLOAT:
                return SchemaProperty.JAVA_FLOAT;

            case SchemaType.BTC_DOUBLE:
                return SchemaProperty.JAVA_DOUBLE;

            case SchemaType.BTC_DECIMAL:
                switch (sType.getDecimalSize())
                {
                    case SchemaType.SIZE_BYTE:
                        return SchemaProperty.JAVA_BYTE;
                    case SchemaType.SIZE_SHORT:
                        return SchemaProperty.JAVA_SHORT;
                    case SchemaType.SIZE_INT:
                        return SchemaProperty.JAVA_INT;
                    case SchemaType.SIZE_LONG:
                        return SchemaProperty.JAVA_LONG;
                    case SchemaType.SIZE_BIG_INTEGER:
                        return SchemaProperty.JAVA_BIG_INTEGER;
                    case SchemaType.SIZE_BIG_DECIMAL:
                    default:
                        return SchemaProperty.JAVA_BIG_DECIMAL;
                }

            case SchemaType.BTC_STRING:
                if (isStringType(sType.getBaseEnumType()))
		{
                    // This is necessary for local types, etc.
                    // schema enums with > ~3668 cause a Java Src file to be created
                    // that cannot be compiled due to JVM restrictions
                    // FIXFIX: http://issues.apache.org/jira/browse/XMLBEANS-307
                    // FIXFIX: XMLBeans scomp throws error "code too large"
                    if (sType.getEnumerationValues() != null &&
                            sType.getEnumerationValues().length > MAX_ENUM_COUNT) 
                    {
                        return SchemaProperty.JAVA_STRING;
                    }
                    else
                    {
                        return SchemaProperty.JAVA_ENUM;
                    }
                }
                return SchemaProperty.JAVA_STRING;

            case SchemaType.BTC_DURATION:
                return SchemaProperty.JAVA_GDURATION;

            case SchemaType.BTC_DATE_TIME:
            case SchemaType.BTC_DATE:
                // return SchemaProperty.JAVA_DATE; // converted to calendar

            case SchemaType.BTC_TIME:
            case SchemaType.BTC_G_YEAR_MONTH:
            case SchemaType.BTC_G_YEAR:
            case SchemaType.BTC_G_MONTH_DAY:
            case SchemaType.BTC_G_DAY:
            case SchemaType.BTC_G_MONTH:
                // return SchemaProperty.JAVA_GDATE; // converted to calendar (JAX-B)
                return SchemaProperty.JAVA_CALENDAR;

            default:
                assert(false) : "unrecognized code " + sType.getPrimitiveType().getBuiltinTypeCode();
                throw new IllegalStateException("unrecognized code " + sType.getPrimitiveType().getBuiltinTypeCode() + " of " + sType.getPrimitiveType().getName());
        }
    }

    static boolean isPropertyModelOrderInsensitive(SchemaProperty[] properties)
    {
        for (int i = 0; i < properties.length; i++)
        {
            SchemaProperty prop = properties[i];
            if (prop.hasNillable() == SchemaProperty.VARIABLE)
                return false;
            if (prop.hasDefault() == SchemaProperty.VARIABLE)
                return false;
            if (prop.hasFixed() == SchemaProperty.VARIABLE)
                return false;
            if (prop.hasDefault() != SchemaProperty.NEVER &&
                prop.getDefaultText() == null)
                return false;
        }
        return true;
    }

    static boolean protectReservedGlobalClassNames(String name)
    {
        int i = name.lastIndexOf('.');
        String lastSegment = name.substring(i + 1);
        if (lastSegment.endsWith("Document") && !lastSegment.equals("Document"))
            return true;
        return false;
    }

    static boolean protectReservedInnerClassNames(String name)
    {
        return (name.equals("Enum") || name.equals("Factory"));
    }

    static String[] PROTECTED_PROPERTIES = {
        "StringValue",
        "BooleanValue",
        "ByteValue",
        "ShortValue",
        "IntValue",
        "LongValue",
        "BigIntegerValue",
        "BigDecimalValue",
        "FloatValue",
        "DoubleValue",
        "ByteArrayValue",
        "EnumValue",
        "CalendarValue",
        "DateValue",
        "GDateValue",
        "GDurationValue",
        "QNameValue",
        "ListValue",
        "ObjectValue",
        "Class",
    };
    static Set PROTECTED_PROPERTIES_SET = new HashSet(Arrays.asList(PROTECTED_PROPERTIES));

    static boolean protectReservedPropertyNames(String name)
    {
        return PROTECTED_PROPERTIES_SET.contains(name) ||
            (name.endsWith("Array") && !name.equals("Array"));
    }

    static String pickFullJavaClassName(Set usedNames, QName qName, String configname, boolean isDocument, boolean isAttrType)
    {
        String base;
        boolean protect;

        if (configname != null && configname.indexOf('.') >= 0)
        {
            // a configname with dots defines the fully qualified java class name
            base = configname;
            protect = protectReservedGlobalClassNames(base);
        }
        else
        {
            StscState state = StscState.get();
            String uri = qName.getNamespaceURI();

            base = NameUtil.getClassNameFromQName(qName);

            // Check to see if we have a mapping from namespace URI to Java package
            // name. If so, apply the mapped package prefix at the beginning of
            // the base name

            String pkgPrefix = state.getPackageOverride(uri);

            if (pkgPrefix != null)
            {
                // Form the new qualified class name from the new package name
                // and the old class name
                base = pkgPrefix + "." + base.substring(base.lastIndexOf('.') + 1);
            }

            // See if there is a prefix...
            String javaPrefix = state.getJavaPrefix(uri);
            if (javaPrefix != null)
                base = base.substring(0, base.lastIndexOf('.') + 1) + javaPrefix + base.substring(base.lastIndexOf('.') + 1);

            // a configname without dots may override the shortname part.
            if (configname != null)
            {
                base = base.substring(0, base.lastIndexOf('.') + 1) + configname;
            }

            protect = protectReservedGlobalClassNames(base);
            if (configname == null)
            {
                // add special suffix
                if (isDocument)
                    base = base + "Document";
                else if (isAttrType)
                    base = base + "Attribute";

                // add configured suffix
                String javaSuffix = state.getJavaSuffix(uri);
                if (javaSuffix != null)
                    base = base + javaSuffix;
            }
        }

        String outermostPkg = getOutermostPackage(base);

        int index = 1;
        String uniqName;
        if (protect)
            uniqName = base + index;
        else
            uniqName = base;
        while (usedNames.contains(uniqName.toLowerCase()) || uniqName.equals(outermostPkg))
        {
            index++;
            uniqName = base + index;
        }

        usedNames.add(uniqName.toLowerCase());

        return uniqName;
    }

    static String getOutermostPackage(String fqcn)
    {
        if (fqcn == null)
            return "";

        // remove class name
        int lastdot = fqcn.indexOf('.');
        if (lastdot < 0)
            return "";

        // remove outer package names
        return fqcn.substring(0, lastdot);
    }

    static String pickFullJavaImplName(Set usedNames, String intfName)
    {
        // Strip off the package from the class name so we can replace it
        String className = intfName;
        String pkgName = null;
        int index = intfName.lastIndexOf('.');
        if (index >= 0)
        {
            className = intfName.substring(index + 1);
            pkgName = intfName.substring(0, index);
        }

        // Form the new qualified class name from the new package name
        // and the old class name
        String base = pkgName + ".impl." + className + "Impl";

        index = 1;
        String uniqName = base;
        while (usedNames.contains(uniqName.toLowerCase()))
        {
            index++;
            uniqName = base + index;
        }

        usedNames.add(uniqName.toLowerCase());

        return uniqName;
    }

    static String pickJavaPropertyName(Set usedNames, String localName, String javaName)
    {
        if (javaName == null)
            javaName = NameUtil.upperCamelCase(localName);
        boolean protect = protectReservedPropertyNames(javaName);
        String uniqName;
        int index = 1;
        if (protect)
            uniqName = javaName + index;
        else
            uniqName = javaName;
        while (usedNames.contains(uniqName))
        {
            index++;
            uniqName = javaName + index;
        }

        usedNames.add(uniqName);

        return uniqName;
    }

    static String pickInnerJavaClassName(Set usedNames, String localName, String javaName)
    {
        if (javaName == null)
            javaName = NameUtil.upperCamelCase(localName);
        boolean protect = protectReservedInnerClassNames(javaName);
        String uniqName;
        int index = 1;
        if (protect)
            uniqName = javaName + index;
        else
            uniqName = javaName;
        while (usedNames.contains(uniqName))
        {
            index++;
            uniqName = javaName + index;
        }

        usedNames.add(uniqName);

        return uniqName;
    }

    static String pickInnerJavaImplName(Set usedNames, String localName, String javaName)
    {
        if (javaName == null)
            javaName = NameUtil.upperCamelCase(localName) + "Impl";
        String uniqName = javaName;
        int index = 1;
        while (usedNames.contains(uniqName))
        {
            index++;
            uniqName = javaName + index;
        }

        usedNames.add(uniqName);

        return uniqName;
    }

    static QName findTopName(SchemaType sType)
    {
        if (sType.getName() != null)
            return sType.getName();

        if (sType.isDocumentType())
        {
            // A document type must have a content model consisting of a single elt
            if (sType.getContentModel() == null || sType.getContentModel().getParticleType() != SchemaParticle.ELEMENT)
                throw new IllegalStateException();
            return (sType.getDocumentElementName());
        }

        if (sType.isAttributeType())
        {
            if (sType.getAttributeModel() == null || sType.getAttributeModel().getAttributes().length != 1)
                throw new IllegalStateException();
            return sType.getAttributeTypeAttributeName();
        }

        SchemaField sElt = sType.getContainerField();
        assert(sElt != null);
        assert(sType.getOuterType() == null);
        return sElt.getName();
    }

    static void addAnonymousTypesFromRedefinition(SchemaType sType, List result)
    {
        while (((SchemaTypeImpl)sType).isRedefinition() &&
                (sType.getDerivationType() == SchemaType.DT_EXTENSION ||
                        sType.isSimpleType()))
        {
            sType = sType.getBaseType();
            SchemaType[] newAnonTypes = sType.getAnonymousTypes();
            if (newAnonTypes.length > 0)
                result.addAll(Arrays.asList(newAnonTypes));
        }
    }
}
