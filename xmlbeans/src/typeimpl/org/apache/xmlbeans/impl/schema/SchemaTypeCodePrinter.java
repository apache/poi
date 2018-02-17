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

import java.io.Writer;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.namespace.QName;
import org.apache.xmlbeans.impl.common.NameUtil;
import org.apache.xmlbeans.PrePostExtension;
import org.apache.xmlbeans.InterfaceExtension;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaStringEnumEntry;
import org.apache.xmlbeans.SystemProperties;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SchemaCodePrinter;

/**
 * Prints the java code for a single schema type
 */
public final class SchemaTypeCodePrinter implements SchemaCodePrinter
{
    Writer _writer;
    int    _indent;
    boolean _useJava15;


    static final String LINE_SEPARATOR =
        SystemProperties.getProperty("line.separator") == null
            ? "\n"
            : SystemProperties.getProperty("line.separator");

    static final String MAX_SPACES = "                                        ";
    static final int INDENT_INCREMENT = 4;

    public static final String INDEX_CLASSNAME = "TypeSystemHolder";

    public static void printTypeImpl ( Writer writer, SchemaType sType,
                                       XmlOptions opt )
        throws IOException
    {
        getPrinter(opt).printTypeImpl( writer, sType );
    }

    public static void printType ( Writer writer, SchemaType sType,
                                   XmlOptions opt )
        throws IOException
    {
        getPrinter(opt).printType( writer, sType );
    }

    /** @deprecated */
    public static void printLoader ( Writer writer, SchemaTypeSystem system,
                                     XmlOptions opt )
        throws IOException
    {
        getPrinter(opt).printLoader( writer, system );
    }

    private static SchemaCodePrinter getPrinter(XmlOptions opt)
    {
        Object printer = XmlOptions.safeGet
            (opt, XmlOptions.SCHEMA_CODE_PRINTER);
        if (printer == null || !(printer instanceof SchemaCodePrinter))
        {
            printer = new SchemaTypeCodePrinter(opt);
        }
        return (SchemaCodePrinter) printer;
    }

    public SchemaTypeCodePrinter (XmlOptions opt)
    {
        _indent = 0;

        String genversion = null;

        if (opt != null && XmlOptions.hasOption(opt, XmlOptions.GENERATE_JAVA_VERSION))
            genversion = (String)opt.get(XmlOptions.GENERATE_JAVA_VERSION);

        if (genversion == null)
            genversion = XmlOptions.GENERATE_JAVA_14;

        _useJava15 = XmlOptions.GENERATE_JAVA_15.equals(genversion);
    }

    void indent()
    {
        _indent += INDENT_INCREMENT;
    }

    void outdent()
    {
        _indent -= INDENT_INCREMENT;
    }

    String encodeString ( String s )
    {
        StringBuffer sb = new StringBuffer();

        sb.append( '"' );

        for ( int i = 0 ; i < s.length() ; i++ )
        {
            char ch = s.charAt( i );

            if (ch == '"')
            {
                sb.append( '\\' );
                sb.append( '\"' );
            }
            else if (ch == '\\')
            {
                sb.append( '\\' );
                sb.append( '\\' );
            }
            else if (ch == '\r')
            {
                sb.append( '\\' );
                sb.append( 'r' );
            }
            else if (ch == '\n')
            {
                sb.append( '\\' );
                sb.append( 'n' );
            }
            else if (ch == '\t')
            {
                sb.append( '\\' );
                sb.append( 't' );
            }
            else
                sb.append( ch );
        }

        sb.append( '"' );

        return sb.toString();
    }

    void emit(String s) throws IOException
    {
        int indent = _indent;
        
        if (indent > MAX_SPACES.length() / 2)
            indent = MAX_SPACES.length() / 4 + indent / 2;
        
        if (indent > MAX_SPACES.length())
            indent = MAX_SPACES.length();
        
        _writer.write(MAX_SPACES.substring(0, indent));
        try
        {
            _writer.write(s);
        }
        catch (CharacterCodingException cce)
        {
            _writer.write(makeSafe(s));
        }
        _writer.write(LINE_SEPARATOR);
        
        // System.out.print(MAX_SPACES.substring(0, indent));
        // System.out.println(s);
    }

    private static String makeSafe(String s)
    {
        Charset charset = Charset.forName(System.getProperty("file.encoding"));
        if (charset == null)
            throw new IllegalStateException("Default character set is null!");
        CharsetEncoder cEncoder = charset.newEncoder();
        StringBuffer result = new StringBuffer();
        int i;
        for (i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if (!cEncoder.canEncode(c))
                break;
        }
        for (; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if (cEncoder.canEncode(c))
                result.append(c);
            else
            {
                String hexValue = Integer.toHexString((int) c);
                switch (hexValue.length())
                {
                case 1:
                    result.append("\\u000").append(hexValue); break;
                case 2:
                    result.append("\\u00").append(hexValue); break;
                case 3:
                    result.append("\\u0").append(hexValue); break;
                case 4:
                    result.append("\\u").append(hexValue); break;
                default:
                    throw new IllegalStateException();
                }
            }
        }
        return result.toString();
    }

    public void printType(Writer writer, SchemaType sType) throws IOException
    {
        _writer = writer;
        printTopComment(sType);
        printPackage(sType, true);
        emit("");
        printInnerType(sType, sType.getTypeSystem());
        _writer.flush();
    }

    public void printTypeImpl(Writer writer, SchemaType sType) 
        throws IOException
    {
        _writer = writer;
        printTopComment(sType);
        printPackage(sType, false);
        printInnerTypeImpl(sType, sType.getTypeSystem(), false);
    }

    /**
     * Since not all schema types have java types, this skips
     * over any that don't and gives you the nearest java base type.
     */
    String findJavaType ( SchemaType sType )
    {
        while ( sType.getFullJavaName() == null )
            sType = sType.getBaseType();
        
        return sType.getFullJavaName();
    }

    static String prettyQName(QName qname)
    {
        String result = qname.getLocalPart();
        if (qname.getNamespaceURI() != null)
            result += "(@" + qname.getNamespaceURI() + ")";
        return result;
    }

    void printInnerTypeJavaDoc(SchemaType sType) throws IOException
    {
        QName name = sType.getName();
        if (name == null)
        {
            if (sType.isDocumentType())
                name = sType.getDocumentElementName();
            else if (sType.isAttributeType())
                name = sType.getAttributeTypeAttributeName();
            else if (sType.getContainerField() != null)
                name = sType.getContainerField().getName();
        }

        emit("/**");
        if (sType.isDocumentType())
            emit(" * A document containing one " + prettyQName(name) + " element.");
        else if (sType.isAttributeType())
            emit(" * A document containing one " + prettyQName(name) + " attribute.");
        else if (name != null)
            emit(" * An XML " + prettyQName(name) + ".");
        else
            emit(" * An anonymous inner XML type.");
        emit(" *");
        switch (sType.getSimpleVariety())
        {
            case SchemaType.NOT_SIMPLE:
                emit(" * This is a complex type.");
                break;
            case SchemaType.ATOMIC:
                emit(" * This is an atomic type that is a restriction of " + getFullJavaName(sType) + ".");
                break;
            case SchemaType.LIST:
                emit(" * This is a list type whose items are " + sType.getListItemType().getFullJavaName() + ".");
                break;
            case SchemaType.UNION:
                emit(" * This is a union type. Instances are of one of the following types:");
                SchemaType[] members = sType.getUnionConstituentTypes();
                for (int i = 0; i < members.length; i++)
                    emit(" *     " + members[i].getFullJavaName());
                break;
        }
        emit(" */");
    }

    private String getFullJavaName(SchemaType sType)
    {

        SchemaTypeImpl sTypeI = (SchemaTypeImpl) sType;
        String ret = sTypeI.getFullJavaName();

        while (sTypeI.isRedefinition())
        {
            ret = sTypeI.getFullJavaName();
            sTypeI = (SchemaTypeImpl) sTypeI.getBaseType();
        }
        return ret;
    }
    
    private String getUserTypeStaticHandlerMethod(boolean encode, SchemaTypeImpl stype)
    {
        String unqualifiedName = stype.getName().getLocalPart();
        if (unqualifiedName.length() < 2)
            unqualifiedName = unqualifiedName.toUpperCase();
        else
            unqualifiedName = unqualifiedName.substring(0, 1).toUpperCase() + unqualifiedName.substring(1);
        
        if (encode)
            return stype.getUserTypeHandlerName() + ".encode" + unqualifiedName;
        else
            return stype.getUserTypeHandlerName() + ".decode" + unqualifiedName;
    }


    public static String indexClassForSystem(SchemaTypeSystem system)
    {
        String name = system.getName();
        return name + "." + INDEX_CLASSNAME;
    }

    static String shortIndexClassForSystem(SchemaTypeSystem system)
    {
        return INDEX_CLASSNAME;
    }

    void printStaticTypeDeclaration(SchemaType sType, SchemaTypeSystem system) throws IOException
    {
        String interfaceShortName = sType.getShortJavaName();
        emit("public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)");
        indent();
        emit("org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(" +
             interfaceShortName + ".class.getClassLoader(), \"" + system.getName() + "\")" +
             ".resolveHandle(\"" +
             ((SchemaTypeSystemImpl)system).handleForType(sType) + "\");");
        outdent();
    }

    /** @deprecated */
    public void printLoader(Writer writer, SchemaTypeSystem system)
        throws IOException
    {
        // deprecated
    }

    void printInnerType(SchemaType sType, SchemaTypeSystem system) throws IOException
    {
        emit("");

        printInnerTypeJavaDoc(sType);

        startInterface(sType);

        printStaticTypeDeclaration(sType, system);

        if (sType.isSimpleType())
        {
            if (sType.hasStringEnumValues())
                printStringEnumeration(sType);
        }
        else
        {
            if (sType.getContentType() == SchemaType.SIMPLE_CONTENT && sType.hasStringEnumValues())
                printStringEnumeration(sType);

            SchemaProperty[] props = getDerivedProperties(sType);

            for (int i = 0; i < props.length; i++)
            {
                SchemaProperty prop = props[i];

                printPropertyGetters(
                    prop.getName(),
                    prop.isAttribute(),
                    prop.getJavaPropertyName(),
                    prop.getJavaTypeCode(),
                    javaTypeForProperty(prop),
                    xmlTypeForProperty(prop),
                    prop.hasNillable() != SchemaProperty.NEVER,
                    prop.extendsJavaOption(),
                    prop.extendsJavaArray(),
                    prop.extendsJavaSingleton()
                );

                if (!prop.isReadOnly())
                {
                    printPropertySetters(
                        prop.getName(),
                        prop.isAttribute(),
                        prop.getJavaPropertyName(),
                        prop.getJavaTypeCode(),
                        javaTypeForProperty(prop),
                        xmlTypeForProperty(prop),
                        prop.hasNillable() != SchemaProperty.NEVER,
                        prop.extendsJavaOption(),
                        prop.extendsJavaArray(),
                        prop.extendsJavaSingleton()
                    );
                }
            }

        }

        printNestedInnerTypes(sType, system);

        printFactory(sType);

        endBlock();
    }

    void printFactory(SchemaType sType) throws IOException
    {
        // Only need full factories for top-level types
        boolean fullFactory = true;
        if (sType.isAnonymousType() && ! sType.isDocumentType() && !sType.isAttributeType())
            fullFactory = false;

        String fullName = sType.getFullJavaName().replace('$', '.');

        emit("");
        emit("/**");
        emit(" * A factory class with static methods for creating instances");
        emit(" * of this type.");
        emit(" */");
        emit("");
        // BUGBUG - Can I use the name loader here?  could it be a
        // nested type name?  It is lower case!
        emit("public static final class Factory");
        emit("{");
        indent();

        if (sType.isSimpleType())
        {
            emit("public static " + fullName + " newValue(java.lang.Object obj) {");
            emit("  return (" + fullName + ") type.newValue( obj ); }");
            emit("");
        }

        // Only need newInstance() for non-abstract types
        if (sType.isAbstract()) {
            emit("/** @deprecated No need to be able to create instances of abstract types */");
            if (_useJava15)
                emit("@Deprecated");
        }
        emit("public static " + fullName + " newInstance() {");
        emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }");
        emit("");

        // Only need newInstance() for non-abstract types
        if (sType.isAbstract()) {
            emit("/** @deprecated No need to be able to create instances of abstract types */");
            if (_useJava15)
                emit("@Deprecated");
        }
        emit("public static " + fullName + " newInstance(org.apache.xmlbeans.XmlOptions options) {");
        emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }");
        emit("");

        if (fullFactory)
        {
            emit("/** @param xmlAsString the string value to parse */");
            emit("public static " + fullName + " parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }");
            emit("");

            emit("public static " + fullName + " parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }");
            emit("");

            emit("/** @param file the file from which to load an xml document */");
            emit("public static " + fullName + " parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }");
            emit("");

            emit("public static " + fullName + " parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }");
            emit("");

            emit("public static " + fullName + " parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }");
            emit("");

            emit("public static " + fullName + " parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }");
            emit("");

            emit("public static " + fullName + " parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }");
            emit("");

            emit("public static " + fullName + " parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }");
            emit("");

            emit("public static " + fullName + " parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }");
            emit("");

            emit("public static " + fullName + " parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }");
            emit("");

            emit("public static " + fullName + " parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }");
            emit("");

            emit("public static " + fullName + " parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }");
            emit("");

            emit("public static " + fullName + " parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }");
            emit("");

            emit("public static " + fullName + " parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }");
            emit("");

            emit("/** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */");
            if (_useJava15)
                emit("@Deprecated");
            emit("public static " + fullName + " parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }");
            emit("");

            emit("/** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */");
            if (_useJava15)
                emit("@Deprecated");
            emit("public static " + fullName + " parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {");
            emit("  return (" + fullName + ") org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }");
            emit("");

            // Don't have XMLInputStream anymore
            emit("/** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */");
            if (_useJava15)
                emit("@Deprecated");
            emit("public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {");
            emit("  return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }");
            emit("");

            // Don't have XMLInputStream anymore
            emit("/** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */");
            if (_useJava15)
                emit("@Deprecated");
            emit("public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {");
            emit("  return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }");
            emit("");
        }

        emit("private Factory() { } // No instance of this class allowed");
        outdent();
        emit("}");
    }

    void printNestedInnerTypes(SchemaType sType, SchemaTypeSystem system) throws IOException
    {
        boolean redefinition = sType.getName() != null &&
            sType.getName().equals(sType.getBaseType().getName());
        while (sType != null)
        {
            SchemaType[] anonTypes = sType.getAnonymousTypes();
            for (int i = 0; i < anonTypes.length; i++)
            {
                if (anonTypes[i].isSkippedAnonymousType())
                    printNestedInnerTypes(anonTypes[i], system);
                else
                    printInnerType(anonTypes[i], system);
            }
            // For redefinition other than by extension for complex types, go ahead and print
            // the anonymous types in the base
            if (!redefinition ||
                (sType.getDerivationType() != SchemaType.DT_EXTENSION && !sType.isSimpleType()))
                break;
            sType = sType.getBaseType();
        }
    }

    void printTopComment(SchemaType sType) throws IOException
    {
        emit("/*");
        if (sType.getName() != null)
        {
            emit(" * XML Type:  " + sType.getName().getLocalPart());
            emit(" * Namespace: " + sType.getName().getNamespaceURI());
        }
        else
        {
            QName thename = null;

            if (sType.isDocumentType())
            {
                thename = sType.getDocumentElementName();
                emit(" * An XML document type.");
            }
            else if (sType.isAttributeType())
            {
                thename = sType.getAttributeTypeAttributeName();
                emit(" * An XML attribute type.");
            }
            else
                assert false;

            assert( thename != null );
            
            emit(" * Localname: " + thename.getLocalPart());
            emit(" * Namespace: " + thename.getNamespaceURI());
        }
        emit(" * Java type: " + sType.getFullJavaName());
        emit(" *");
        emit(" * Automatically generated - do not modify.");
        emit(" */");
    }

    void printPackage(SchemaType sType, boolean intf) throws IOException
    {
        String fqjn;
        if (intf)
            fqjn = sType.getFullJavaName();
        else
            fqjn = sType.getFullJavaImplName();

        int lastdot = fqjn.lastIndexOf('.');
        if (lastdot < 0)
            return;
        String pkg = fqjn.substring(0, lastdot);
        emit("package " + pkg + ";");
    }

    void startInterface(SchemaType sType) throws IOException
    {
        String shortName = sType.getShortJavaName();
        
        String baseInterface = findJavaType(sType.getBaseType());

        /*
        StringBuffer specializedInterfaces = new StringBuffer();

        if (sType.getSimpleVariety() == SchemaType.ATOMIC &&
            sType.getPrimitiveType().getBuiltinTypeCode() == SchemaType.BTC_DECIMAL)
        {
            int bits = sType.getDecimalSize();
            if (bits == SchemaType.SIZE_BIG_INTEGER)
                specializedInterfaces.append(", org.apache.xmlbeans.BigIntegerValue");
            if (bits == SchemaType.SIZE_LONG)
                specializedInterfaces.append(", org.apache.xmlbeans.LongValue");
            if (bits <= SchemaType.SIZE_INT)
                specializedInterfaces.append(", org.apache.xmlbeans.IntValue");
        }
        if (sType.getSimpleVariety() == SchemaType.LIST)
            specializedInterfaces.append(", org.apache.xmlbeans.ListValue");

        if (sType.getSimpleVariety() == SchemaType.UNION)
        {
            SchemaType ctype = sType.getUnionCommonBaseType();
            String javaTypeHolder = javaTypeHolderForType(ctype);
            if (javaTypeHolder != null)
                specializedInterfaces.append(", " + javaTypeHolder);
        }
        */

        emit("public interface " + shortName + " extends " + baseInterface + getExtensionInterfaces(sType));
        emit("{");
        indent();
        emitSpecializedAccessors(sType);
    }

    private static String getExtensionInterfaces(SchemaType sType)
    {
        SchemaTypeImpl sImpl = getImpl(sType);
        if (sImpl == null)
            return "";

        StringBuffer sb = new StringBuffer();

        InterfaceExtension[] exts = sImpl.getInterfaceExtensions();
        if (exts != null) for (int i = 0; i < exts.length; i++)
            sb.append(", " + exts[i].getInterface());

        return sb.toString();
    }

    private static SchemaTypeImpl getImpl(SchemaType sType)
    {
        if (sType instanceof SchemaTypeImpl)
            return (SchemaTypeImpl) sType;
        else
            return null;
    }

    private void emitSpecializedAccessors(SchemaType sType) throws IOException
    {
        if (sType.getSimpleVariety() == SchemaType.ATOMIC &&
            sType.getPrimitiveType().getBuiltinTypeCode() == SchemaType.BTC_DECIMAL)
        {
            int bits = sType.getDecimalSize();
            int parentBits = sType.getBaseType().getDecimalSize();
            if (bits != parentBits || sType.getBaseType().getFullJavaName() == null)
            {
                if (bits == SchemaType.SIZE_BIG_INTEGER)
                {
                    emit("java.math.BigInteger getBigIntegerValue();");
                    emit("void setBigIntegerValue(java.math.BigInteger bi);");
                    emit("/** @deprecated */");
                    if (_useJava15)
                        emit("@Deprecated");
                    emit("java.math.BigInteger bigIntegerValue();");
                    emit("/** @deprecated */");
                    if (_useJava15)
                        emit("@Deprecated");
                    emit("void set(java.math.BigInteger bi);");
                }
                else if (bits == SchemaType.SIZE_LONG)
                {
                    emit("long getLongValue();");
                    emit("void setLongValue(long l);");
                    emit("/** @deprecated */");
                    if (_useJava15)
                        emit("@Deprecated");
                    emit("long longValue();");
                    emit("/** @deprecated */");
                    if (_useJava15)
                        emit("@Deprecated");
                    emit("void set(long l);");
                }
                else if (bits == SchemaType.SIZE_INT)
                {
                    emit("int getIntValue();");
                    emit("void setIntValue(int i);");
                    emit("/** @deprecated */");
                    if (_useJava15)
                        emit("@Deprecated");
                    emit("int intValue();");
                    emit("/** @deprecated */");
                    if (_useJava15)
                        emit("@Deprecated");
                    emit("void set(int i);");
                }
                else if (bits == SchemaType.SIZE_SHORT)
                {
                    emit("short getShortValue();");
                    emit("void setShortValue(short s);");
                    emit("/** @deprecated */");
                    if (_useJava15)
                        emit("@Deprecated");
                    emit("short shortValue();");
                    emit("/** @deprecated */");
                    if (_useJava15)
                        emit("@Deprecated");
                    emit("void set(short s);");
                }
                else if (bits == SchemaType.SIZE_BYTE)
                {
                    emit("byte getByteValue();");
                    emit("void setByteValue(byte b);");
                    emit("/** @deprecated */");
                    if (_useJava15)
                        emit("@Deprecated");
                    emit("byte byteValue();");
                    emit("/** @deprecated */");
                    if (_useJava15)
                        emit("@Deprecated");
                    emit("void set(byte b);");
                }
            }
        }

        if (sType.getSimpleVariety() == SchemaType.UNION)
        {
            emit("java.lang.Object getObjectValue();");
            emit("void setObjectValue(java.lang.Object val);");
            emit("/** @deprecated */");
            if (_useJava15)
                emit("@Deprecated");
            emit("java.lang.Object objectValue();");
            emit("/** @deprecated */");
            if (_useJava15)
                emit("@Deprecated");
            emit("void objectSet(java.lang.Object val);");
            emit("org.apache.xmlbeans.SchemaType instanceType();");
            SchemaType ctype = sType.getUnionCommonBaseType();
            if (ctype != null && ctype.getSimpleVariety() != SchemaType.UNION);
                emitSpecializedAccessors(ctype);
        }

        if (sType.getSimpleVariety() == SchemaType.LIST)
        {
            emit("java.util.List getListValue();");
            emit("java.util.List xgetListValue();");
            emit("void setListValue(java.util.List list);");
            emit("/** @deprecated */");
            if (_useJava15)
                emit("@Deprecated");
            emit("java.util.List listValue();");
            emit("/** @deprecated */");
            if (_useJava15)
                emit("@Deprecated");
            emit("java.util.List xlistValue();");
            emit("/** @deprecated */");
            if (_useJava15)
                emit("@Deprecated");
            emit("void set(java.util.List list);");
        }
    }

    void startBlock() throws IOException
    {
        emit("{");
        indent();
    }
    void endBlock() throws IOException
    {
        outdent();
        emit("}");
    }

    void printJavaDoc(String sentence) throws IOException
    {
        emit("");
        emit("/**");
        emit(" * " + sentence);
        emit(" */");
    }

    void printShortJavaDoc(String sentence) throws IOException
    {
        emit("/** " + sentence + " */");
    }

    public static String javaStringEscape(String str)
    {
        // forbidden: \n, \r, \", \\.
        test: {
            for (int i = 0; i < str.length(); i++)
            {
                switch (str.charAt(i))
                {
                    case '\n':
                    case '\r':
                    case '\"':
                    case '\\':
                        break test;
                }
            }
            return str;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++)
        {
            char ch = str.charAt(i);
            switch (ch)
            {
                default:
                    sb.append(ch);
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
            }
        }
        return sb.toString();
    }

    void printStringEnumeration(SchemaType sType) throws IOException
    {
        SchemaType baseEnumType = sType.getBaseEnumType();
        String baseEnumClass = baseEnumType.getFullJavaName();
        boolean hasBase = hasBase(sType);

        if (!hasBase)
        {
            emit("");
            emit("org.apache.xmlbeans.StringEnumAbstractBase enumValue();");
            emit("void set(org.apache.xmlbeans.StringEnumAbstractBase e);");
        }

        emit("");
        SchemaStringEnumEntry[] entries = sType.getStringEnumEntries();
        HashSet seenValues = new HashSet();
        HashSet repeatValues = new HashSet();
        for (int i = 0; i < entries.length; i++)
        {
            String enumValue = entries[i].getString();
            if (seenValues.contains(enumValue))
            {
                repeatValues.add(enumValue);
                continue;
            }
            else
                seenValues.add(enumValue);
            String constName = entries[i].getEnumName();
            if (hasBase)
                emit("static final " + baseEnumClass + ".Enum " + constName + " = " + baseEnumClass + "." + constName + ";");
            else
                emit("static final Enum " + constName + " = Enum.forString(\"" + javaStringEscape(enumValue) + "\");");
        }
        emit("");
        for (int i = 0; i < entries.length; i++)
        {
            if (repeatValues.contains(entries[i].getString()))
                continue;
            String constName = "INT_" + entries[i].getEnumName();
            if (hasBase)
                emit("static final int " + constName + " = " + baseEnumClass + "." + constName + ";");
            else
                emit("static final int " + constName + " = Enum." + constName + ";");
        }
        if (!hasBase)
        {
            emit("");
            emit("/**");
            emit(" * Enumeration value class for " + baseEnumClass + ".");
            emit(" * These enum values can be used as follows:");
            emit(" * <pre>");
            emit(" * enum.toString(); // returns the string value of the enum");
            emit(" * enum.intValue(); // returns an int value, useful for switches");
            if (entries.length > 0)
            emit(" * // e.g., case Enum.INT_" + entries[0].getEnumName());
            emit(" * Enum.forString(s); // returns the enum value for a string");
            emit(" * Enum.forInt(i); // returns the enum value for an int");
            emit(" * </pre>");
            emit(" * Enumeration objects are immutable singleton objects that");
            emit(" * can be compared using == object equality. They have no");
            emit(" * public constructor. See the constants defined within this");
            emit(" * class for all the valid values.");
            emit(" */");
            emit("static final class Enum extends org.apache.xmlbeans.StringEnumAbstractBase");
            emit("{");
            indent();
            emit("/**");
            emit(" * Returns the enum value for a string, or null if none.");
            emit(" */");
            emit("public static Enum forString(java.lang.String s)");
            emit("    { return (Enum)table.forString(s); }");
            emit("/**");
            emit(" * Returns the enum value corresponding to an int, or null if none.");
            emit(" */");
            emit("public static Enum forInt(int i)");
            emit("    { return (Enum)table.forInt(i); }");
            emit("");
            emit("private Enum(java.lang.String s, int i)");
            emit("    { super(s, i); }");
            emit("");
            for (int i = 0; i < entries.length; i++)
            {
                String constName = "INT_" + entries[i].getEnumName();
                int intValue = entries[i].getIntValue();
                emit("static final int " + constName + " = " + intValue + ";");
            }
            emit("");
            emit("public static final org.apache.xmlbeans.StringEnumAbstractBase.Table table =");
            emit("    new org.apache.xmlbeans.StringEnumAbstractBase.Table");
            emit("(");
            indent();
            emit("new Enum[]");
            emit("{");
            indent();
            for (int i = 0; i < entries.length; i++)
            {
                String enumValue = entries[i].getString();
                String constName = "INT_" + entries[i].getEnumName();
                emit("new Enum(\"" + javaStringEscape(enumValue) + "\", " + constName + "),");
            }
            outdent();
            emit("}");
            outdent();
            emit(");");
            emit("private static final long serialVersionUID = 1L;");
            emit("private java.lang.Object readResolve() { return forInt(intValue()); } ");
            outdent();
            emit("}");
        }
    }

    private boolean hasBase(SchemaType sType)
    {
        boolean hasBase;
       	SchemaType baseEnumType = sType.getBaseEnumType();
        if (baseEnumType.isAnonymousType() && baseEnumType.isSkippedAnonymousType())
        {
            if (sType.getContentBasedOnType() != null)
                hasBase = sType.getContentBasedOnType().getBaseType() != baseEnumType;
            else
                hasBase = sType.getBaseType() != baseEnumType;
        }
        else
            hasBase = baseEnumType != sType;
	return hasBase;
    }

    String xmlTypeForProperty(SchemaProperty sProp)
    {
        SchemaType sType = sProp.javaBasedOnType();
        return findJavaType(sType).replace('$', '.');
    }

    static boolean xmlTypeForPropertyIsUnion(SchemaProperty sProp)
    {
        SchemaType sType = sProp.javaBasedOnType();
        return (sType.isSimpleType() && sType.getSimpleVariety() == SchemaType.UNION);
    }

    static boolean isJavaPrimitive(int javaType)
    {
        return (javaType < SchemaProperty.JAVA_FIRST_PRIMITIVE ? false :
            (javaType > SchemaProperty.JAVA_LAST_PRIMITIVE ? false : true));
    }

    /** Returns the wrapped type for a java primitive. */
    static String javaWrappedType(int javaType)
    {
        switch (javaType)
        {
            case SchemaProperty.JAVA_BOOLEAN:
                return "java.lang.Boolean";
            case SchemaProperty.JAVA_FLOAT:
                return "java.lang.Float";
            case SchemaProperty.JAVA_DOUBLE:
                return "java.lang.Double";
            case SchemaProperty.JAVA_BYTE:
                return "java.lang.Byte";
            case SchemaProperty.JAVA_SHORT:
                return "java.lang.Short";
            case SchemaProperty.JAVA_INT:
                return "java.lang.Integer";
            case SchemaProperty.JAVA_LONG:
                return "java.lang.Long";

            // anything else is not a java primitive
            default:
                assert false;
                throw new IllegalStateException();
        }
    }

    String javaTypeForProperty(SchemaProperty sProp)
    {
        // The type to use is the XML object....
        if (sProp.getJavaTypeCode() == SchemaProperty.XML_OBJECT)
        {
            SchemaType sType = sProp.javaBasedOnType();
            return findJavaType(sType).replace('$', '.');
        }
        
        if (sProp.getJavaTypeCode() == SchemaProperty.JAVA_USER)
        {
               return ((SchemaTypeImpl)sProp.getType()).getUserTypeName();
        }

        switch (sProp.getJavaTypeCode())
        {
            case SchemaProperty.JAVA_BOOLEAN:
                return "boolean";
            case SchemaProperty.JAVA_FLOAT:
                return "float";
            case SchemaProperty.JAVA_DOUBLE:
                return "double";
            case SchemaProperty.JAVA_BYTE:
                return "byte";
            case SchemaProperty.JAVA_SHORT:
                return "short";
            case SchemaProperty.JAVA_INT:
                return "int";
            case SchemaProperty.JAVA_LONG:
                return "long";

            case SchemaProperty.JAVA_BIG_DECIMAL:
                return "java.math.BigDecimal";
            case SchemaProperty.JAVA_BIG_INTEGER:
                return "java.math.BigInteger";
            case SchemaProperty.JAVA_STRING:
                return "java.lang.String";
            case SchemaProperty.JAVA_BYTE_ARRAY:
                return "byte[]";
            case SchemaProperty.JAVA_GDATE:
                return "org.apache.xmlbeans.GDate";
            case SchemaProperty.JAVA_GDURATION:
                return "org.apache.xmlbeans.GDuration";
            case SchemaProperty.JAVA_DATE:
                return "java.util.Date";
            case SchemaProperty.JAVA_QNAME:
                return "javax.xml.namespace.QName";
            case SchemaProperty.JAVA_LIST:
                return "java.util.List";
            case SchemaProperty.JAVA_CALENDAR:
                return "java.util.Calendar";

            case SchemaProperty.JAVA_ENUM:
                SchemaType sType = sProp.javaBasedOnType();
                if (sType.getSimpleVariety() == SchemaType.UNION)
                    sType = sType.getUnionCommonBaseType();
                assert sType.getBaseEnumType() != null;
                if (hasBase(sType)) 
                    return findJavaType(sType.getBaseEnumType()).replace('$', '.') + ".Enum";
                else
                    return findJavaType(sType).replace('$', '.') + ".Enum";

            case SchemaProperty.JAVA_OBJECT:
                return "java.lang.Object";

            default:
                assert(false);
                throw new IllegalStateException();
        }
    }

    void printPropertyGetters(QName qName, boolean isAttr,
                       String propertyName, int javaType,
                       String type, String xtype,
                       boolean nillable, boolean optional,
                       boolean several, boolean singleton)
       throws IOException
    {
        String propdesc = "\"" + qName.getLocalPart() + "\"" + (isAttr ? " attribute" : " element");
        boolean xmltype = (javaType == SchemaProperty.XML_OBJECT);

        if (singleton)
        {
            printJavaDoc((several ? "Gets first " : "Gets the ") + propdesc);
            emit(type + " get" + propertyName + "();");

            if (!xmltype)
            {
                printJavaDoc((several ? "Gets (as xml) first " : "Gets (as xml) the ") + propdesc);
                emit(xtype + " xget" + propertyName + "();");
            }

            if (nillable)
            {
                printJavaDoc((several ? "Tests for nil first " : "Tests for nil ") + propdesc);
                emit("boolean isNil" + propertyName + "();");
            }
        }

        if (optional)
        {
            printJavaDoc((several ? "True if has at least one " : "True if has ") + propdesc);
            emit("boolean isSet" + propertyName + "();");
        }

        if (several)
        {
            String arrayName = propertyName + "Array";

            if (_useJava15)
            {
                String wrappedType = type;
                if (isJavaPrimitive(javaType))
                    wrappedType = javaWrappedType(javaType);

                printJavaDoc("Gets a List of " + propdesc + "s");
                emit("java.util.List<" + wrappedType + "> get" + propertyName + "List();");
            }

            if (_useJava15)
            {
                emit("");
                emit("/**");
                emit(" * Gets array of all " + propdesc + "s");
                emit(" * @deprecated");
                emit(" */");
                emit("@Deprecated");
            }
            else
                printJavaDoc("Gets array of all " + propdesc + "s");
            emit(type + "[] get" + arrayName + "();");

            printJavaDoc("Gets ith " + propdesc);
            emit(type + " get" + arrayName + "(int i);");

            if (!xmltype)
            {
                if (_useJava15)
                {
                    printJavaDoc("Gets (as xml) a List of " + propdesc + "s");
                    emit("java.util.List<" + xtype + "> xget" + propertyName + "List();");
                }

                if (_useJava15)
                {
                    emit("");
                    emit("/**");
                    emit(" * Gets (as xml) array of all " + propdesc + "s");
                    emit(" * @deprecated");
                    emit(" */");
                    emit("@Deprecated");
                }
                else
                    printJavaDoc("Gets (as xml) array of all " + propdesc + "s");
                emit(xtype + "[] xget" + arrayName + "();");

                printJavaDoc("Gets (as xml) ith " + propdesc);
                emit(xtype + " xget" + arrayName + "(int i);");
            }

            if (nillable)
            {
                printJavaDoc("Tests for nil ith " + propdesc);
                emit("boolean isNil" + arrayName + "(int i);");
            }

            printJavaDoc("Returns number of " + propdesc);
            emit("int sizeOf" + arrayName + "();");
        }
    }

    void printPropertySetters(QName qName, boolean isAttr,
                       String propertyName, int javaType, String type, String xtype,
                       boolean nillable, boolean optional,
                       boolean several, boolean singleton)
       throws IOException
    {
        String safeVarName = NameUtil.nonJavaKeyword(NameUtil.lowerCamelCase(propertyName));
        if (safeVarName.equals("i"))
            safeVarName = "iValue";
        boolean xmltype = (javaType == SchemaProperty.XML_OBJECT);

        String propdesc = "\"" + qName.getLocalPart() + "\"" + (isAttr ? " attribute" : " element");

        if (singleton)
        {
            printJavaDoc((several ? "Sets first " : "Sets the ") + propdesc);
            emit("void set" + propertyName + "(" + type + " " + safeVarName + ");");

            if (!xmltype)
            {
                printJavaDoc((several ? "Sets (as xml) first " : "Sets (as xml) the ") + propdesc);
                emit("void xset" + propertyName + "(" + xtype + " " + safeVarName + ");");
            }

            if (xmltype && !several)
            {
                printJavaDoc("Appends and returns a new empty " + propdesc);
                emit(xtype + " addNew" + propertyName + "();");
            }

            if (nillable)
            {
                printJavaDoc((several ? "Nils the first " : "Nils the ") + propdesc);
                emit("void setNil" + propertyName + "();");
            }
        }

        if (optional)
        {
            printJavaDoc((several ? "Removes first " : "Unsets the ") + propdesc);
            emit("void unset" + propertyName + "();");
        }

        if (several)
        {
            String arrayName = propertyName + "Array";

            printJavaDoc("Sets array of all " + propdesc);
            emit("void set" + arrayName + "(" + type + "[] " + safeVarName + "Array);");

            printJavaDoc("Sets ith " + propdesc);
            emit("void set" + arrayName + "(int i, " + type + " " + safeVarName + ");");

            if (!xmltype)
            {
                printJavaDoc("Sets (as xml) array of all " + propdesc);
                emit("void xset" + arrayName + "(" + xtype + "[] " + safeVarName + "Array);");

                printJavaDoc("Sets (as xml) ith " + propdesc);
                emit("void xset" + arrayName + "(int i, " + xtype + " " + safeVarName + ");");
            }

            if (nillable)
            {
                printJavaDoc("Nils the ith " + propdesc);
                emit("void setNil" + arrayName + "(int i);");
            }

            if (!xmltype)
            {
                printJavaDoc("Inserts the value as the ith " + propdesc);
                emit("void insert" + propertyName + "(int i, " + type + " " + safeVarName + ");");

                printJavaDoc("Appends the value as the last " + propdesc);
                emit("void add" + propertyName + "(" + type + " " + safeVarName + ");");
            }

            printJavaDoc("Inserts and returns a new empty value (as xml) as the ith " + propdesc);
            emit(xtype + " insertNew" + propertyName + "(int i);");

            printJavaDoc("Appends and returns a new empty value (as xml) as the last " + propdesc);
            emit(xtype + " addNew" + propertyName + "();");

            printJavaDoc("Removes the ith " + propdesc);
            emit("void remove" + propertyName + "(int i);");
        }
    }

    String getAtomicRestrictionType(SchemaType sType) {
        SchemaType pType = sType.getPrimitiveType();
        switch (pType.getBuiltinTypeCode())
        {
            case SchemaType.BTC_ANY_SIMPLE:
                return "org.apache.xmlbeans.impl.values.XmlAnySimpleTypeImpl";
            case SchemaType.BTC_BOOLEAN:
                return "org.apache.xmlbeans.impl.values.JavaBooleanHolderEx";
            case SchemaType.BTC_BASE_64_BINARY:
                return "org.apache.xmlbeans.impl.values.JavaBase64HolderEx";
            case SchemaType.BTC_HEX_BINARY:
                return "org.apache.xmlbeans.impl.values.JavaHexBinaryHolderEx";
            case SchemaType.BTC_ANY_URI:
                return "org.apache.xmlbeans.impl.values.JavaUriHolderEx";
            case SchemaType.BTC_QNAME:
                return "org.apache.xmlbeans.impl.values.JavaQNameHolderEx";
            case SchemaType.BTC_NOTATION:
                return "org.apache.xmlbeans.impl.values.JavaNotationHolderEx";
            case SchemaType.BTC_FLOAT:
                return "org.apache.xmlbeans.impl.values.JavaFloatHolderEx";
            case SchemaType.BTC_DOUBLE:
                return "org.apache.xmlbeans.impl.values.JavaDoubleHolderEx";
            case SchemaType.BTC_DECIMAL:
                switch (sType.getDecimalSize())
                {
                    default:
                        assert(false);
                    case SchemaType.SIZE_BIG_DECIMAL:
                        return "org.apache.xmlbeans.impl.values.JavaDecimalHolderEx";
                    case SchemaType.SIZE_BIG_INTEGER:
                        return "org.apache.xmlbeans.impl.values.JavaIntegerHolderEx";
                    case SchemaType.SIZE_LONG:
                        return "org.apache.xmlbeans.impl.values.JavaLongHolderEx";
                    case SchemaType.SIZE_INT:
                    case SchemaType.SIZE_SHORT:
                    case SchemaType.SIZE_BYTE:
                        return "org.apache.xmlbeans.impl.values.JavaIntHolderEx";
                }
            case SchemaType.BTC_STRING:
                if (sType.hasStringEnumValues())
                    return "org.apache.xmlbeans.impl.values.JavaStringEnumerationHolderEx";
                else
                    return "org.apache.xmlbeans.impl.values.JavaStringHolderEx";

            case SchemaType.BTC_DATE_TIME:
            case SchemaType.BTC_TIME:
            case SchemaType.BTC_DATE:
            case SchemaType.BTC_G_YEAR_MONTH:
            case SchemaType.BTC_G_YEAR:
            case SchemaType.BTC_G_MONTH_DAY:
            case SchemaType.BTC_G_DAY:
            case SchemaType.BTC_G_MONTH:
                return "org.apache.xmlbeans.impl.values.JavaGDateHolderEx";

            case SchemaType.BTC_DURATION:
                return "org.apache.xmlbeans.impl.values.JavaGDurationHolderEx";
            default:
                assert(false) : "unrecognized primitive type";
                return null;
        }
    }

    static SchemaType findBaseType(SchemaType sType)
    {
        while (sType.getFullJavaName() == null)
            sType = sType.getBaseType();
        return sType;
    }

    String getBaseClass(SchemaType sType) {
        SchemaType baseType = findBaseType(sType.getBaseType());

        switch (sType.getSimpleVariety())
        {
            case SchemaType.NOT_SIMPLE:
                // non-simple-content: inherit from base type impl
                if (!XmlObject.type.equals(baseType))
                    return baseType.getFullJavaImplName();
                return "org.apache.xmlbeans.impl.values.XmlComplexContentImpl";

            case SchemaType.ATOMIC:
                // We should only get called for restrictions
                assert(! sType.isBuiltinType());
                return getAtomicRestrictionType(sType);

            case SchemaType.LIST:
                return "org.apache.xmlbeans.impl.values.XmlListImpl";

            case SchemaType.UNION:
                return "org.apache.xmlbeans.impl.values.XmlUnionImpl";

            default:
                throw new IllegalStateException();
        }
    }

    void printConstructor(SchemaType sType, String shortName) throws IOException {
        emit("");
        emit("public " + shortName + "(org.apache.xmlbeans.SchemaType sType)");
        startBlock();
        emit("super(sType" + (sType.getSimpleVariety() == SchemaType.NOT_SIMPLE ?
                             "":
                             ", " + !sType.isSimpleType()) +
             ");");
        endBlock();

        if (sType.getSimpleVariety() != SchemaType.NOT_SIMPLE)
        {
            emit("");
            emit("protected " + shortName + "(org.apache.xmlbeans.SchemaType sType, boolean b)");
            startBlock();
            emit("super(sType, b);");
            endBlock();
        }
    }

    void startClass(SchemaType sType, boolean isInner) throws IOException
    {
        String shortName = sType.getShortJavaImplName();
        String baseClass = getBaseClass(sType);
        StringBuffer interfaces = new StringBuffer();
        interfaces.append(sType.getFullJavaName().replace('$', '.'));

        if (sType.getSimpleVariety() == SchemaType.UNION) {
            SchemaType[] memberTypes = sType.getUnionMemberTypes();
            for (int i = 0 ; i < memberTypes.length ; i++)
                interfaces.append(", " + memberTypes[i].getFullJavaName().replace('$', '.'));
        }

        emit("public " + ( isInner ? "static ": "" ) + "class " + shortName +
            " extends " + baseClass + " implements " + interfaces.toString());

        startBlock();

        emit("private static final long serialVersionUID = 1L;");
    }

    void makeAttributeDefaultValue(String jtargetType, SchemaProperty prop, String identifier) throws IOException
    {
        String fullName = jtargetType;
        if (fullName == null)
            fullName = prop.javaBasedOnType().getFullJavaName().replace('$', '.');

        emit("target = (" + fullName + ")get_default_attribute_value(" + identifier + ");");
    }

    void makeMissingValue(int javaType) throws IOException
    {
        switch (javaType)
        {
            case SchemaProperty.JAVA_BOOLEAN:
                emit("return false;"); break;

            case SchemaProperty.JAVA_FLOAT:
                emit("return 0.0f;"); break;

            case SchemaProperty.JAVA_DOUBLE:
                emit("return 0.0;"); break;

            case SchemaProperty.JAVA_BYTE:
            case SchemaProperty.JAVA_SHORT:
            case SchemaProperty.JAVA_INT:
                emit("return 0;"); break;

            case SchemaProperty.JAVA_LONG:
                emit("return 0L;"); break;

            default:
            case SchemaProperty.XML_OBJECT:
            case SchemaProperty.JAVA_BIG_DECIMAL:
            case SchemaProperty.JAVA_BIG_INTEGER:
            case SchemaProperty.JAVA_STRING:
            case SchemaProperty.JAVA_BYTE_ARRAY:
            case SchemaProperty.JAVA_GDATE:
            case SchemaProperty.JAVA_GDURATION:
            case SchemaProperty.JAVA_DATE:
            case SchemaProperty.JAVA_QNAME:
            case SchemaProperty.JAVA_LIST:
            case SchemaProperty.JAVA_CALENDAR:
            case SchemaProperty.JAVA_ENUM:
            case SchemaProperty.JAVA_OBJECT:
                emit("return null;"); break;
        }
    }

    void printJGetArrayValue(int javaType, String type, SchemaTypeImpl stype) throws IOException {
        switch (javaType)
        {
            case SchemaProperty.XML_OBJECT:
                emit(type + "[] result = new " + type + "[targetList.size()];");
                emit("targetList.toArray(result);");
                break;

            case SchemaProperty.JAVA_ENUM:
                emit(type + "[] result = new " + type + "[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = (" + type + ")((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getEnumValue();");
                break;

            case SchemaProperty.JAVA_BOOLEAN:
                emit("boolean[] result = new boolean[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getBooleanValue();");
                break;

            case SchemaProperty.JAVA_FLOAT:
                emit("float[] result = new float[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getFloatValue();");
                break;

            case SchemaProperty.JAVA_DOUBLE:
                emit("double[] result = new double[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getDoubleValue();");
                break;

            case SchemaProperty.JAVA_BYTE:
                emit("byte[] result = new byte[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getByteValue();");
                break;

            case SchemaProperty.JAVA_SHORT:
                emit("short[] result = new short[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getShortValue();");
                break;

            case SchemaProperty.JAVA_INT:
                emit("int[] result = new int[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getIntValue();");
                break;

            case SchemaProperty.JAVA_LONG:
                emit("long[] result = new long[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getLongValue();");
                break;

            case SchemaProperty.JAVA_BIG_DECIMAL:
                emit("java.math.BigDecimal[] result = new java.math.BigDecimal[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getBigDecimalValue();");
                break;

            case SchemaProperty.JAVA_BIG_INTEGER:
                emit("java.math.BigInteger[] result = new java.math.BigInteger[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getBigIntegerValue();");
                break;

            case SchemaProperty.JAVA_STRING:
                emit("java.lang.String[] result = new java.lang.String[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();");
                break;

            case SchemaProperty.JAVA_BYTE_ARRAY:
                emit("byte[][] result = new byte[targetList.size()][];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getByteArrayValue();");
                break;

            case SchemaProperty.JAVA_CALENDAR:
                emit("java.util.Calendar[] result = new java.util.Calendar[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getCalendarValue();");
                break;

            case SchemaProperty.JAVA_DATE:
                emit("java.util.Date[] result = new java.util.Date[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getDateValue();");
                break;

            case SchemaProperty.JAVA_GDATE:
                emit("org.apache.xmlbeans.GDate[] result = new org.apache.xmlbeans.GDate[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getGDateValue();");
                break;

            case SchemaProperty.JAVA_GDURATION:
                emit("org.apache.xmlbeans.GDuration[] result = new org.apache.xmlbeans.GDuration[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getGDurationValue();");
                break;

            case SchemaProperty.JAVA_QNAME:
                emit("javax.xml.namespace.QName[] result = new javax.xml.namespace.QName[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getQNameValue();");
                break;

            case SchemaProperty.JAVA_LIST:
                emit("java.util.List[] result = new java.util.List[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getListValue();");
                break;

            case SchemaProperty.JAVA_OBJECT:
                emit("java.lang.Object[] result = new java.lang.Object[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getObjectValue();");
                break;

            case SchemaProperty.JAVA_USER:
                emit(stype.getUserTypeName() + "[] result = new " + stype.getUserTypeName() + "[targetList.size()];");
                emit("for (int i = 0, len = targetList.size() ; i < len ; i++)");
                emit("    result[i] = " + getUserTypeStaticHandlerMethod(false, stype)
                        + "((org.apache.xmlbeans.SimpleValue)targetList.get(i));");
                break;
                
            default:
                throw new IllegalStateException();
        }
        emit("return result;");
    }
    void printJGetValue(int javaType, String type, SchemaTypeImpl stype) throws IOException {
        switch (javaType)
        {
        case SchemaProperty.XML_OBJECT:
            emit("return target;"); break;

        case SchemaProperty.JAVA_BOOLEAN:
            emit("return target.getBooleanValue();"); break;

        case SchemaProperty.JAVA_FLOAT:
            emit("return target.getFloatValue();"); break;

        case SchemaProperty.JAVA_DOUBLE:
            emit("return target.getDoubleValue();"); break;

        case SchemaProperty.JAVA_BYTE:
            emit("return target.getByteValue();"); break;

        case SchemaProperty.JAVA_SHORT:
            emit("return target.getShortValue();"); break;

        case SchemaProperty.JAVA_INT:
            emit("return target.getIntValue();"); break;

        case SchemaProperty.JAVA_LONG:
            emit("return target.getLongValue();"); break;

        case SchemaProperty.JAVA_BIG_DECIMAL:
            emit("return target.getBigDecimalValue();"); break;

        case SchemaProperty.JAVA_BIG_INTEGER:
            emit("return target.getBigIntegerValue();"); break;

        case SchemaProperty.JAVA_STRING:
            emit("return target.getStringValue();"); break;

        case SchemaProperty.JAVA_BYTE_ARRAY:
            emit("return target.getByteArrayValue();"); break;

        case SchemaProperty.JAVA_GDATE:
            emit("return target.getGDateValue();"); break;

        case SchemaProperty.JAVA_GDURATION:
            emit("return target.getGDurationValue();"); break;

        case SchemaProperty.JAVA_CALENDAR:
            emit("return target.getCalendarValue();"); break;

        case SchemaProperty.JAVA_DATE:
            emit("return target.getDateValue();"); break;

        case SchemaProperty.JAVA_QNAME:
            emit("return target.getQNameValue();"); break;

        case SchemaProperty.JAVA_LIST:
            emit("return target.getListValue();"); break;

        case SchemaProperty.JAVA_ENUM:
            emit("return (" + type + ")target.getEnumValue();"); break;

        case SchemaProperty.JAVA_OBJECT:
            emit("return target.getObjectValue();"); break;
            
        case SchemaProperty.JAVA_USER:
            emit("return " + getUserTypeStaticHandlerMethod(false, stype)
                    + "(target);");
            break;
            
        default:
            throw new IllegalStateException();
        }
    }
    void printJSetValue(int javaType, String safeVarName, SchemaTypeImpl stype) throws IOException {
        switch (javaType)
        {
            case SchemaProperty.XML_OBJECT:
                emit("target.set(" + safeVarName + ");"); break;

            case SchemaProperty.JAVA_BOOLEAN:
                emit("target.setBooleanValue(" + safeVarName + ");"); break;

            case SchemaProperty.JAVA_FLOAT:
                emit("target.setFloatValue(" + safeVarName + ");"); break;

            case SchemaProperty.JAVA_DOUBLE:
                emit("target.setDoubleValue(" + safeVarName + ");"); break;

            case SchemaProperty.JAVA_BYTE:
                emit("target.setByteValue(" + safeVarName + ");"); break;

            case SchemaProperty.JAVA_SHORT:
                emit("target.setShortValue(" + safeVarName + ");"); break;

            case SchemaProperty.JAVA_INT:
                emit("target.setIntValue(" + safeVarName + ");"); break;

            case SchemaProperty.JAVA_LONG:
                emit("target.setLongValue(" + safeVarName + ");"); break;

            case SchemaProperty.JAVA_BIG_DECIMAL:
                emit("target.setBigDecimalValue(" + safeVarName + ");"); break;

            case SchemaProperty.JAVA_BIG_INTEGER:
                emit("target.setBigIntegerValue(" + safeVarName + ");"); break;

            case SchemaProperty.JAVA_STRING:
                emit("target.setStringValue(" + safeVarName + ");"); break;

            case SchemaProperty.JAVA_BYTE_ARRAY:
                emit("target.setByteArrayValue(" + safeVarName + ");"); break;

            case SchemaProperty.JAVA_GDATE:
                emit("target.setGDateValue(" + safeVarName + ");"); break;

            case SchemaProperty.JAVA_GDURATION:
                emit("target.setGDurationValue(" + safeVarName + ");"); break;

            case SchemaProperty.JAVA_CALENDAR:
                emit("target.setCalendarValue(" + safeVarName + ");"); break;

            case SchemaProperty.JAVA_DATE:
                emit("target.setDateValue(" + safeVarName + ");"); break;

            case SchemaProperty.JAVA_QNAME:
                emit("target.setQNameValue(" + safeVarName + ");"); break;

            case SchemaProperty.JAVA_LIST:
                emit("target.setListValue(" + safeVarName + ");"); break;

            case SchemaProperty.JAVA_ENUM:
                emit("target.setEnumValue(" + safeVarName + ");"); break;

            case SchemaProperty.JAVA_OBJECT:
                emit("target.setObjectValue(" + safeVarName + ");"); break;
                
            case SchemaProperty.JAVA_USER:
                emit(getUserTypeStaticHandlerMethod(true, stype)
                        + "(" + safeVarName + ", target);");
                break;

            default:
                throw new IllegalStateException();
        }
    }

    String getIdentifier(Map qNameMap, QName qName) {
        return ((String[])qNameMap.get(qName))[0];
    }

    String getSetIdentifier(Map qNameMap, QName qName) {
        String[] identifiers = (String[])qNameMap.get(qName);
        return identifiers[1] == null ? identifiers[0] : identifiers[1];
    }

    Map printStaticFields(SchemaProperty[] properties) throws IOException {
        final Map results = new HashMap();

        emit("");
        for (int i = 0; i < properties.length; i++)
        {
            final String[] identifiers = new String[2];
            final SchemaProperty prop = properties[i];
            final QName name = prop.getName();
            results.put(name, identifiers);
            final String javaName = prop.getJavaPropertyName();
            identifiers[0] = (javaName + "$" + (i * 2)).toUpperCase();
            final String uriString =  "\"" + name.getNamespaceURI() + "\"";

            emit("private static final javax.xml.namespace.QName " + identifiers[0] +
                 " = " );
            indent();
            emit("new javax.xml.namespace.QName(" +
                 uriString + ", \"" + name.getLocalPart() + "\");");
            outdent();

            if (properties[i].acceptedNames() != null)
            {
                final QName[] qnames = properties[i].acceptedNames();

                if (qnames.length > 1)
                {
                    identifiers[1] = (javaName + "$" + (i*2+1)).toUpperCase();

                    emit("private static final org.apache.xmlbeans.QNameSet " + identifiers[1] +
                        " = org.apache.xmlbeans.QNameSet.forArray( new javax.xml.namespace.QName[] { " );
                    indent();
                    for (int j = 0 ; j < qnames.length ; j++)
                    {
                        emit("new javax.xml.namespace.QName(\"" + qnames[j].getNamespaceURI() +
                            "\", \"" + qnames[j].getLocalPart() + "\"),");
                    }

                    outdent();

                    emit("});");
                }
            }
        }
        emit("");
        return results;
    }

    void emitImplementationPreamble() throws IOException
    {
        emit("synchronized (monitor())");
        emit("{");
        indent();
        emit("check_orphaned();");
    }

    void emitImplementationPostamble() throws IOException
    {
        outdent();
        emit("}");
    }

    void emitDeclareTarget(boolean declareTarget, String xtype)
        throws IOException
    {
         if (declareTarget)
             emit(xtype + " target = null;");
  	}

    void emitAddTarget(String identifier, boolean isAttr, boolean declareTarget, String xtype)
        throws IOException
    {
        if (isAttr)
            emit("target = (" + xtype + ")get_store().add_attribute_user(" + identifier + ");");
        else
            emit("target = (" + xtype + ")get_store().add_element_user(" + identifier + ");");
    }

    void emitPre(SchemaType sType, int opType, String identifier, boolean isAttr) throws IOException
    {
        emitPre(sType, opType, identifier, isAttr, "-1");
    }

    void emitPre(SchemaType sType, int opType, String identifier, boolean isAttr, String index) throws IOException
    {
        SchemaTypeImpl sImpl = getImpl(sType);
        if (sImpl == null)
            return;

        PrePostExtension ext = sImpl.getPrePostExtension();
        if (ext != null)
        {
            if (ext.hasPreCall())
            {
                emit("if ( " + ext.getStaticHandler() + ".preSet(" + prePostOpString(opType) + ", this, " + identifier + ", " + isAttr + ", " + index + "))");
                startBlock();
            }
        }
    }

    void emitPost(SchemaType sType, int opType, String identifier, boolean isAttr) throws IOException
    {
        emitPost(sType, opType, identifier, isAttr, "-1");
    }

    void emitPost(SchemaType sType, int opType, String identifier, boolean isAttr, String index) throws IOException
    {
        SchemaTypeImpl sImpl = getImpl(sType);
        if (sImpl == null)
            return;

        PrePostExtension ext = sImpl.getPrePostExtension();
        if (ext != null)
        {
            if (ext.hasPreCall())
            {
                endBlock();
            }

            if (ext.hasPostCall())
                emit(ext.getStaticHandler() + ".postSet(" + prePostOpString(opType) + ", this, " + identifier + ", " + isAttr + ", " + index + ");");
        }
    }

    String prePostOpString(int opType)
    {
        switch (opType)
        {
            default:
                assert false;

            case PrePostExtension.OPERATION_SET:
                return "org.apache.xmlbeans.PrePostExtension.OPERATION_SET";

            case PrePostExtension.OPERATION_INSERT:
                return "org.apache.xmlbeans.PrePostExtension.OPERATION_INSERT";

            case PrePostExtension.OPERATION_REMOVE:
                return "org.apache.xmlbeans.PrePostExtension.OPERATION_REMOVE";
        }
    }

    private static final int NOTHING = 1;
    private static final int ADD_NEW_VALUE = 3;
    private static final int THROW_EXCEPTION = 4;

    void emitGetTarget(String setIdentifier,
                       String identifier,
                       boolean isAttr,
                       String index,
                       int nullBehaviour,
                       String xtype)
        throws IOException
    {
        assert setIdentifier != null && identifier != null;

        emit(xtype + " target = null;");

        if (isAttr)
            emit("target = (" + xtype + ")get_store().find_attribute_user(" + identifier + ");");
        else
            emit("target = (" + xtype + ")get_store().find_element_user(" + setIdentifier + ", " + index + ");");

        if (nullBehaviour == NOTHING)
            return;

        emit("if (target == null)");

        startBlock();

        switch (nullBehaviour)
        {
            case ADD_NEW_VALUE:
                // target already emited, no need for emitDeclareTarget(false, xtype);
                emitAddTarget(identifier, isAttr, false, xtype);
                break;

            case THROW_EXCEPTION:
                emit("throw new IndexOutOfBoundsException();");
                break;

            case NOTHING:
                break;

            default:
                assert false : "Bad behaviour type: " + nullBehaviour;
        }

        endBlock();
    }

    void printListGetter15Impl(String parentJavaName,
        String propdesc, String propertyName,
        String wrappedType, String xtype,
        boolean xmltype, boolean xget)
            throws IOException
    {
        String arrayName = propertyName + "Array";
        String listName = propertyName + "List";
        String parentThis = parentJavaName + ".this.";

        String xgetMethod = (xget ? "x" : "") + "get";
        String xsetMethod = (xget ? "x" : "") + "set";

        printJavaDoc("Gets " + (xget ? "(as xml) " : "") + "a List of " + propdesc + "s");

        emit("public java.util.List<" + wrappedType + "> " + xgetMethod + listName  + "()");
        startBlock();

        emit("final class " + listName + " extends java.util.AbstractList<" + wrappedType + ">");
        startBlock();

        // Object get(i)
        if (_useJava15)
            emit("@Override");
        emit("public " + wrappedType + " get(int i)");
        emit("    { return " + parentThis + xgetMethod + arrayName + "(i); }");
        emit("");

        // Object set(i, o)
        if (_useJava15)
            emit("@Override");
        emit("public " + wrappedType + " set(int i, " + wrappedType + " o)");
        startBlock();
        emit(wrappedType + " old = " + parentThis + xgetMethod + arrayName + "(i);");
        emit(parentThis + xsetMethod + arrayName + "(i, o);");
        emit("return old;");
        endBlock();
        emit("");

        // void add(i, o)
        if (_useJava15)
            emit("@Override");
        emit("public void add(int i, " + wrappedType +" o)");
        if (xmltype || xget)
            emit("    { " + parentThis + "insertNew" + propertyName + "(i).set(o); }");
        else
            emit("    { " + parentThis + "insert" + propertyName + "(i, o); }");
        emit("");

        // Object remove(i)
        if (_useJava15)
            emit("@Override");
        emit("public " + wrappedType +" remove(int i)");
        startBlock();
        emit(wrappedType + " old = " + parentThis + xgetMethod + arrayName + "(i);");
        emit(parentThis + "remove" + propertyName + "(i);");
        emit("return old;");
        endBlock();
        emit("");

        // int size()
        if (_useJava15)
            emit("@Override");
        emit("public int size()");
        emit("    { return " + parentThis + "sizeOf" + arrayName + "(); }");
        emit("");

        endBlock();

        emit("");

        emitImplementationPreamble();

        emit("return new " + listName + "();");

        emitImplementationPostamble();
        endBlock();
    }

    void printGetterImpls(String parentJavaName,
        SchemaProperty prop, QName qName, boolean isAttr, String propertyName,
        int javaType, String type, String xtype, boolean nillable,
        boolean optional, boolean several, boolean singleton,
        boolean isunion,
        String identifier, String setIdentifier )
            throws IOException
    {
        String propdesc = "\"" + qName.getLocalPart() + "\"" + (isAttr ? " attribute" : " element");
        boolean xmltype = (javaType == SchemaProperty.XML_OBJECT);
        String jtargetType = (isunion || !xmltype) ? "org.apache.xmlbeans.SimpleValue" : xtype;

        if (singleton)
        {
            // Value getProp()
            printJavaDoc((several ? "Gets first " : "Gets the ") + propdesc);
            emit("public " + type + " get" + propertyName + "()");
            startBlock();
            emitImplementationPreamble();

            emitGetTarget(setIdentifier, identifier, isAttr, "0", NOTHING, jtargetType);

            if (isAttr && (prop.hasDefault() == SchemaProperty.CONSISTENTLY ||
                    prop.hasFixed() == SchemaProperty.CONSISTENTLY))
            {
                emit("if (target == null)");
                startBlock();
                makeAttributeDefaultValue(jtargetType, prop, identifier);
                endBlock();
            }
            emit("if (target == null)");
            startBlock();
            makeMissingValue(javaType);
            endBlock();

            
            printJGetValue(javaType, type, (SchemaTypeImpl)prop.getType());    
            

            emitImplementationPostamble();

            endBlock();

            if (!xmltype)
            {
                // Value xgetProp()
                printJavaDoc((several ? "Gets (as xml) first " : "Gets (as xml) the ") + propdesc);
                emit("public " + xtype + " xget" + propertyName + "()");
                startBlock();
                emitImplementationPreamble();
                emitGetTarget(setIdentifier, identifier, isAttr, "0", NOTHING, xtype);

                if (isAttr && (prop.hasDefault() == SchemaProperty.CONSISTENTLY ||
                        prop.hasFixed() == SchemaProperty.CONSISTENTLY))
                {
                    emit("if (target == null)");
                    startBlock();
                    makeAttributeDefaultValue(xtype, prop, identifier);
                    endBlock();
                }

                emit("return target;");
                emitImplementationPostamble();
                endBlock();
            }

            if (nillable)
            {
                // boolean isNilProp()
                printJavaDoc((several ? "Tests for nil first " : "Tests for nil ") + propdesc);
                emit("public boolean isNil" + propertyName + "()");
                startBlock();
                emitImplementationPreamble();
                emitGetTarget(setIdentifier, identifier, isAttr, "0", NOTHING, xtype);

                emit("if (target == null) return false;");
                emit("return target.isNil();");
                emitImplementationPostamble();
                endBlock();
            }
        }

        if (optional)
        {
            // boolean isSetProp()
            printJavaDoc((several ? "True if has at least one " : "True if has ") + propdesc);
            emit("public boolean isSet" + propertyName + "()");

            startBlock();
            emitImplementationPreamble();

            if (isAttr)
                emit("return get_store().find_attribute_user(" + identifier +") != null;");
            else
                emit("return get_store().count_elements(" + setIdentifier + ") != 0;");

            emitImplementationPostamble();
            endBlock();
        }

        if (several)
        {
            String arrayName = propertyName + "Array";

            if (_useJava15)
            {
                // use boxed type if the java type is a primitive and jdk1.5
                // jdk1.5 will box/unbox for us
                String wrappedType = type;
                if (isJavaPrimitive(javaType))
                    wrappedType = javaWrappedType(javaType);

                printListGetter15Impl(parentJavaName, propdesc, propertyName, wrappedType, xtype, xmltype, false);
            }

            // Value[] getProp()
            if (_useJava15)
            {
                emit("");
                emit("/**");
                emit(" * Gets array of all " + propdesc + "s");
                emit(" * @deprecated");
                emit(" */");
                emit("@Deprecated");
            }
            else
                printJavaDoc("Gets array of all " + propdesc + "s");
            emit("public " + type + "[] get" + arrayName + "()");
            startBlock();
            emitImplementationPreamble();

            if (_useJava15)
                emit("java.util.List<" + xtype + "> targetList = new java.util.ArrayList<" + xtype + ">();");
            else
                emit("java.util.List targetList = new java.util.ArrayList();");
            emit("get_store().find_all_element_users(" + setIdentifier + ", targetList);");

            printJGetArrayValue(javaType, type, (SchemaTypeImpl)prop.getType());

            emitImplementationPostamble();
            endBlock();

            // Value getProp(int i)
            printJavaDoc("Gets ith " + propdesc);
            emit("public " + type + " get" + arrayName + "(int i)");
            startBlock();
            emitImplementationPreamble();

            emitGetTarget(setIdentifier, identifier, isAttr, "i", THROW_EXCEPTION, jtargetType);
            printJGetValue(javaType, type, (SchemaTypeImpl)prop.getType());

            emitImplementationPostamble();
            endBlock();

            if (!xmltype)
            {
                if (_useJava15)
                {
                    printListGetter15Impl(parentJavaName, propdesc, propertyName, xtype, xtype, xmltype, true);
                }

                // Value[] xgetProp()
                if (_useJava15)
                {
                    emit("");
                    emit("/**");
                    emit(" * Gets array of all " + propdesc + "s");
                    emit(" * @deprecated");
                    emit(" */");
                    emit("@Deprecated");
                }
                else
                    printJavaDoc("Gets (as xml) array of all " + propdesc + "s");
                emit("public " + xtype + "[] xget" + arrayName + "()");
                startBlock();
                emitImplementationPreamble();
                if (_useJava15)
                    emit("java.util.List<" + xtype +  "> targetList = new java.util.ArrayList<" + xtype +  ">();");
                else
                    emit("java.util.List targetList = new java.util.ArrayList();");
                emit("get_store().find_all_element_users(" + setIdentifier + ", targetList);");
                emit(xtype + "[] result = new " + xtype + "[targetList.size()];");
                emit("targetList.toArray(result);");
                emit("return result;");
                emitImplementationPostamble();
                endBlock();

                // Value xgetProp(int i)
                printJavaDoc("Gets (as xml) ith " + propdesc);
                emit("public " + xtype + " xget" + arrayName + "(int i)");
                startBlock();
                emitImplementationPreamble();
                emitGetTarget(setIdentifier, identifier, isAttr, "i", THROW_EXCEPTION, xtype);
                emit("return target;");
                emitImplementationPostamble();
                endBlock();

            }

            if (nillable)
            {
                // boolean isNil(int i);
                printJavaDoc("Tests for nil ith " + propdesc);
                emit("public boolean isNil" + arrayName + "(int i)");
                startBlock();
                emitImplementationPreamble();
                emitGetTarget(setIdentifier, identifier, isAttr, "i", THROW_EXCEPTION, xtype);
                emit("return target.isNil();");
                emitImplementationPostamble();
                endBlock();
            }

            // int countProp();
            printJavaDoc("Returns number of " + propdesc);
            emit("public int sizeOf" + arrayName + "()");
            startBlock();
            emitImplementationPreamble();
            emit("return get_store().count_elements(" + setIdentifier +");");
            emitImplementationPostamble();
            endBlock();
        }
    }

    void printSetterImpls(QName qName, SchemaProperty prop, boolean isAttr,
        String propertyName, int javaType, String type, String xtype,
        boolean nillable, boolean optional, boolean several, boolean singleton,
        boolean isunion, String identifier, String setIdentifier, SchemaType sType)
        throws IOException
    {
        String safeVarName = NameUtil.nonJavaKeyword(NameUtil.lowerCamelCase(propertyName));
        safeVarName = NameUtil.nonExtraKeyword(safeVarName);

        boolean xmltype = (javaType == SchemaProperty.XML_OBJECT);
        boolean isobj = (javaType == SchemaProperty.JAVA_OBJECT);
        boolean isSubstGroup = identifier != setIdentifier;
        String jtargetType = (isunion || !xmltype) ? "org.apache.xmlbeans.SimpleValue" : xtype;

        String propdesc = "\"" + qName.getLocalPart() + "\"" + (isAttr ? " attribute" : " element");

        if (singleton)
        {
            // void setProp(Value v);
            printJavaDoc((several ? "Sets first " : "Sets the ") + propdesc);
            emit("public void set" + propertyName + "(" + type + " " + safeVarName + ")");
            startBlock();
            if ( xmltype && !isSubstGroup )
            {
                emitPre(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, several ? "0" : "-1");
                emit("generatedSetterHelperImpl(" + safeVarName + ", " + setIdentifier + ", 0, " +
                        "org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);");
                emitPost(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, several ? "0" : "-1");
            }
            else
            {
                emitImplementationPreamble();
                emitPre(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, several ? "0" : "-1");
                emitGetTarget(setIdentifier, identifier, isAttr, "0", ADD_NEW_VALUE, jtargetType);
                printJSetValue(javaType, safeVarName, (SchemaTypeImpl)prop.getType());
                emitPost(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, several ? "0" : "-1");
                emitImplementationPostamble();
            }
            endBlock();

            if (!xmltype)
            {
                // void xsetProp(Value v)
                printJavaDoc((several ? "Sets (as xml) first " : "Sets (as xml) the ") + propdesc);
                emit("public void xset" + propertyName + "(" + xtype + " " + safeVarName + ")");
                startBlock();
                emitImplementationPreamble();
                emitPre(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, several ? "0" : "-1");
                emitGetTarget(setIdentifier, identifier, isAttr, "0", ADD_NEW_VALUE, xtype);
                emit("target.set(" + safeVarName + ");");
                emitPost(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, several ? "0" : "-1");
                emitImplementationPostamble();
                endBlock();

            }

            if (xmltype && !several)
            {
                // Value addNewProp()
                printJavaDoc("Appends and returns a new empty " + propdesc);
                emit("public " + xtype + " addNew" + propertyName + "()");
                startBlock();
                emitImplementationPreamble();
                emitDeclareTarget(true, xtype);
  	            emitPre(sType, PrePostExtension.OPERATION_INSERT, identifier, isAttr);
                emitAddTarget(identifier, isAttr, true, xtype);
                emitPost(sType, PrePostExtension.OPERATION_INSERT, identifier, isAttr);
                emit("return target;");
                emitImplementationPostamble();
                endBlock();
            }

            if (nillable)
            {
                printJavaDoc((several ? "Nils the first " : "Nils the ") + propdesc);
                emit("public void setNil" + propertyName + "()");
                startBlock();
                emitImplementationPreamble();
                emitPre(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, several ? "0" : "-1");
                emitGetTarget(setIdentifier, identifier, isAttr, "0", ADD_NEW_VALUE, xtype);
                emit("target.setNil();");
                emitPost(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, several ? "0" : "-1");
                emitImplementationPostamble();
                endBlock();
            }
        }

        if (optional)
        {
            printJavaDoc((several ? "Removes first " : "Unsets the ") + propdesc);
            emit("public void unset" + propertyName + "()");
            startBlock();
            emitImplementationPreamble();
            emitPre(sType, PrePostExtension.OPERATION_REMOVE, identifier, isAttr, several ? "0" : "-1");
            if (isAttr)
                emit("get_store().remove_attribute(" + identifier + ");");
            else
                emit("get_store().remove_element(" + setIdentifier + ", 0);");
            emitPost(sType, PrePostExtension.OPERATION_REMOVE, identifier, isAttr, several ? "0" : "-1");
            emitImplementationPostamble();
            endBlock();
        }

        if (several)
        {
            String arrayName = propertyName + "Array";

            if ( xmltype )
            {
                printJavaDoc("Sets array of all " + propdesc + "  WARNING: This method is not atomicaly synchronized.");
                emit("public void set" + arrayName + "(" + type + "[] " + safeVarName + "Array)");
                startBlock();
                // do not use synchronize (monitor()) {  and GlobalLock inside  } !!! deadlock
                //emitImplementationPreamble();
                emit("check_orphaned();");
                emitPre(sType, PrePostExtension.OPERATION_SET, identifier, isAttr);

                if (isobj)
                {
                    if (!isSubstGroup)
                        emit("unionArraySetterHelper(" + safeVarName + "Array" + ", " + identifier + ");" );
                    else
                        emit("unionArraySetterHelper(" + safeVarName + "Array" + ", " + identifier + ", " + setIdentifier + ");" );
                }
                else
                {
                    if (!isSubstGroup)
                        emit("arraySetterHelper(" + safeVarName + "Array" + ", " + identifier + ");" );
                    else
                        emit("arraySetterHelper(" + safeVarName + "Array" + ", " + identifier + ", " + setIdentifier + ");" );
                }

                emitPost(sType, PrePostExtension.OPERATION_SET, identifier, isAttr);
                //emitImplementationPostamble();  to avoid deadlock
                endBlock();
            }
            else
            {
                printJavaDoc("Sets array of all " + propdesc );
                emit("public void set" + arrayName + "(" + type + "[] " + safeVarName + "Array)");
                startBlock();
                emitImplementationPreamble();
                emitPre(sType, PrePostExtension.OPERATION_SET, identifier, isAttr);

                if (isobj)
                {
                    if (!isSubstGroup)
                        emit("unionArraySetterHelper(" + safeVarName + "Array" + ", " + identifier + ");" );
                    else
                        emit("unionArraySetterHelper(" + safeVarName + "Array" + ", " + identifier + ", " + setIdentifier + ");" );
                }
                else if (prop.getJavaTypeCode() == SchemaProperty.JAVA_USER)
                {
                    if (!isSubstGroup)
                    {
                        emit("org.apache.xmlbeans.SimpleValue[] dests = arraySetterHelper(" + safeVarName + "Array.length" + ", " + identifier + ");" );
                        emit("for ( int i = 0 ; i < dests.length ; i++ ) {");
                        emit("    " + getUserTypeStaticHandlerMethod(true, (SchemaTypeImpl)prop.getType())
                                + "(" + safeVarName + "Array[i], dests[i]);");
                        emit("}");
                    }
                    else
                    {
                        emit("org.apache.xmlbeans.SimpleValue[] dests = arraySetterHelper(" + safeVarName + "Array.length" + ", " + identifier + ", " + setIdentifier + ");" );
                        emit("for ( int i = 0 ; i < dests.length ; i++ ) {");
                        emit("    " + getUserTypeStaticHandlerMethod(true, (SchemaTypeImpl)prop.getType())
                                + "(" + safeVarName + "Array[i], dests[i]);");
                        emit("}");
                    }
                }
                else
                {
                    if (!isSubstGroup)
                        emit("arraySetterHelper(" + safeVarName + "Array" + ", " + identifier + ");" );
                    else
                        emit("arraySetterHelper(" + safeVarName + "Array" + ", " + identifier + ", " + setIdentifier + ");" );
                }

                emitPost(sType, PrePostExtension.OPERATION_SET, identifier, isAttr);
                emitImplementationPostamble();
                endBlock();
            }

            printJavaDoc("Sets ith " + propdesc);
            emit("public void set" + arrayName + "(int i, " + type + " " + safeVarName + ")");
            startBlock();
            if ( xmltype && !isSubstGroup )
            {
                emitPre(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, "i");
                emit("generatedSetterHelperImpl(" + safeVarName + ", " + setIdentifier + ", i, " +
                        "org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_ARRAYITEM);");
                emitPost(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, "i");
            }
            else
            {
                emitImplementationPreamble();
                emitPre(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, "i");
                emitGetTarget(setIdentifier, identifier, isAttr, "i", THROW_EXCEPTION, jtargetType);
                printJSetValue(javaType, safeVarName, (SchemaTypeImpl)prop.getType());
                emitPost(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, "i");
                emitImplementationPostamble();
            }
            endBlock();

            if (!xmltype)
            {
                printJavaDoc("Sets (as xml) array of all " + propdesc);
                emit("public void xset" + arrayName + "(" + xtype + "[]" + safeVarName + "Array)");
                startBlock();
                emitImplementationPreamble();
                emitPre(sType, PrePostExtension.OPERATION_SET, identifier, isAttr);
                emit("arraySetterHelper(" + safeVarName + "Array" + ", " + identifier + ");" );
                emitPost(sType, PrePostExtension.OPERATION_SET, identifier, isAttr);
                emitImplementationPostamble();
                endBlock();

                printJavaDoc("Sets (as xml) ith " + propdesc);
                emit("public void xset" + arrayName + "(int i, " + xtype + " " + safeVarName + ")");
                startBlock();
                emitImplementationPreamble();
                emitPre(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, "i");
                emitGetTarget(setIdentifier, identifier, isAttr, "i", THROW_EXCEPTION, xtype);
                emit("target.set(" + safeVarName + ");");
                emitPost(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, "i");
                emitImplementationPostamble();
                endBlock();
            }

            if (nillable)
            {
                printJavaDoc("Nils the ith " + propdesc);
                emit("public void setNil" + arrayName + "(int i)");
                startBlock();
                emitImplementationPreamble();
                emitPre(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, "i");
                emitGetTarget(setIdentifier, identifier, isAttr, "i", THROW_EXCEPTION, xtype);
                emit("target.setNil();");
                emitPost(sType, PrePostExtension.OPERATION_SET, identifier, isAttr, "i");
                emitImplementationPostamble();
                endBlock();
            }

            if (!xmltype)
            {
                printJavaDoc("Inserts the value as the ith " + propdesc);
                emit("public void insert" + propertyName + "(int i, " + type + " " + safeVarName + ")");
                startBlock();
                emitImplementationPreamble();
                emitPre(sType, PrePostExtension.OPERATION_INSERT, identifier, isAttr, "i");
                emit(jtargetType + " target = ");
                indent();
                if (!isSubstGroup)
                    emit("(" + jtargetType + ")get_store().insert_element_user(" + identifier + ", i);");
                else // This is a subst group case
                    emit ("(" + jtargetType +")get_store().insert_element_user(" + setIdentifier + ", " +
                            identifier + ", i);");
                outdent();
                printJSetValue(javaType, safeVarName, (SchemaTypeImpl)prop.getType());
                emitPost(sType, PrePostExtension.OPERATION_INSERT, identifier, isAttr, "i");
                emitImplementationPostamble();
                endBlock();

                printJavaDoc("Appends the value as the last " + propdesc);
                emit("public void add" + propertyName + "(" + type + " " + safeVarName + ")");
                startBlock();
                emitImplementationPreamble();
                emitDeclareTarget(true, jtargetType);
  	            emitPre(sType, PrePostExtension.OPERATION_INSERT, identifier, isAttr);
                emitAddTarget(identifier, isAttr, true, jtargetType);
                printJSetValue(javaType, safeVarName, (SchemaTypeImpl)prop.getType());
                emitPost(sType, PrePostExtension.OPERATION_INSERT, identifier, isAttr);
                emitImplementationPostamble();
                endBlock();
            }

            printJavaDoc("Inserts and returns a new empty value (as xml) as the ith " + propdesc);
            emit("public " + xtype + " insertNew" + propertyName + "(int i)");
            startBlock();
            emitImplementationPreamble();
            emitDeclareTarget(true, xtype);
            emitPre(sType, PrePostExtension.OPERATION_INSERT, identifier, isAttr, "i");
            if (!isSubstGroup)
            {
                emit("target = (" + xtype + ")get_store().insert_element_user(" + identifier + ", i);");
            }
            else // This is a subst group case
            {
                emit("target = (" + xtype + ")get_store().insert_element_user(" +
                        setIdentifier + ", " + identifier + ", i);");
            }
            emitPost(sType, PrePostExtension.OPERATION_INSERT, identifier, isAttr, "i");
            emit("return target;");
            emitImplementationPostamble();
            endBlock();

            printJavaDoc("Appends and returns a new empty value (as xml) as the last " + propdesc);
            emit("public " + xtype + " addNew" + propertyName + "()");
            startBlock();
            emitImplementationPreamble();
            emitDeclareTarget(true, xtype);
            emitPre(sType, PrePostExtension.OPERATION_INSERT, identifier, isAttr);
            emitAddTarget(identifier, isAttr, true, xtype);
            emitPost(sType, PrePostExtension.OPERATION_INSERT, identifier, isAttr);
            emit("return target;");
            emitImplementationPostamble();
            endBlock();

            printJavaDoc("Removes the ith " + propdesc);
            emit("public void remove" + propertyName + "(int i)");
            startBlock();
            emitImplementationPreamble();
            emitPre(sType, PrePostExtension.OPERATION_REMOVE, identifier, isAttr, "i");
            emit("get_store().remove_element(" + setIdentifier + ", i);");
            emitPost(sType, PrePostExtension.OPERATION_REMOVE, identifier, isAttr, "i");
            emitImplementationPostamble();
            endBlock();
        }
    }

    static void getTypeName(Class c, StringBuffer sb) {
        int arrayCount = 0;
        while (c.isArray()) {
            c = c.getComponentType();
            arrayCount++;
        }

        sb.append(c.getName());

        for (int i = 0 ; i < arrayCount; i++)
            sb.append("[]");

    }

    void printInnerTypeImpl(
        SchemaType sType, SchemaTypeSystem system, boolean isInner ) throws IOException
    {
        String shortName = sType.getShortJavaImplName();

        printInnerTypeJavaDoc(sType);

        startClass(sType, isInner);

        printConstructor(sType, shortName);

        printExtensionImplMethods(sType);

        if (!sType.isSimpleType())
        {
            SchemaProperty[] properties;

            if (sType.getContentType() == SchemaType.SIMPLE_CONTENT)
            {
                // simple content types impls derive directly from "holder" impls
                // in order to handle the case (for ints or string enums e.g.) where
                // there is a simple type restriction.  So property getters need to
                // be implemented "from scratch" for each derived complex type
                // Moreover, attribute or element properties can be removed via restriction,
                // but we still need to implement them because this class is supposed to
                // also implement all the interfaces
                SchemaType baseType = sType.getBaseType();
                List extraProperties = null;
                while (!baseType.isSimpleType() && !baseType.isBuiltinType())
                {
                    SchemaProperty[] baseProperties = baseType.getDerivedProperties();
                    for (int i = 0; i < baseProperties.length; i++)
                        if (!(baseProperties[i].isAttribute() &&
                                sType.getAttributeProperty(baseProperties[i].getName()) != null))
                        {
                            if (extraProperties == null)
                                extraProperties = new ArrayList();
                            extraProperties.add(baseProperties[i]);
                        }
                    baseType = baseType.getBaseType();
                }

                properties = sType.getProperties();
                if (extraProperties != null)
                {
                    for (int i = 0; i < properties.length; i++)
                        extraProperties.add(properties[i]);
                    properties = (SchemaProperty[]) extraProperties.
                        toArray(new SchemaProperty[extraProperties.size()]);
                }
            }
            else
            {
                // complex content type implementations derive from base type impls
                // so derived property impls can be reused

                properties = getDerivedProperties(sType);
            }

            Map qNameMap = printStaticFields(properties);

            for (int i = 0; i < properties.length; i++)
            {
                SchemaProperty prop = properties[i];

                QName name = prop.getName();
                String xmlType = xmlTypeForProperty( prop );

                printGetterImpls(
                    shortName,
                    prop,
                    name,
                    prop.isAttribute(),
                    prop.getJavaPropertyName(),
                    prop.getJavaTypeCode(),
                    javaTypeForProperty(prop),
                    xmlType,
                    prop.hasNillable() != SchemaProperty.NEVER,
                    prop.extendsJavaOption(),
                    prop.extendsJavaArray(),
                    prop.extendsJavaSingleton(),
                    xmlTypeForPropertyIsUnion(prop),
                    getIdentifier(qNameMap, name),
                    getSetIdentifier(qNameMap, name)
                );

                if (!prop.isReadOnly())
                {
                    printSetterImpls(
                        name,
                        prop,
                        prop.isAttribute(),
                        prop.getJavaPropertyName(),
                        prop.getJavaTypeCode(),
                        javaTypeForProperty(prop),
                        xmlType,
                        prop.hasNillable() != SchemaProperty.NEVER,
                        prop.extendsJavaOption(),
                        prop.extendsJavaArray(),
                        prop.extendsJavaSingleton(),
                        xmlTypeForPropertyIsUnion(prop),
                        getIdentifier(qNameMap, name),
                        getSetIdentifier(qNameMap, name),
  	                    sType
                    );
                }
            }
        }

        printNestedTypeImpls(sType, system);

        endBlock();
    }

    private SchemaProperty[] getDerivedProperties(SchemaType sType)
    {
        // We have to see if this is redefined, because if it is we have
        // to include all properties associated to its supertypes
        QName name = sType.getName();
        if (name != null && name.equals(sType.getBaseType().getName()))
        {
            SchemaType sType2 = sType.getBaseType();
            // Walk all the redefined types and record any properties
            // not present in sType, because the redefined types do not
            // have a generated class to represent them
            SchemaProperty[] props = sType.getDerivedProperties();
            Map propsByName = new LinkedHashMap();
            for (int i = 0; i < props.length; i++)
                propsByName.put(props[i].getName(), props[i]);
            while (sType2 != null && name.equals(sType2.getName()))
            {
                props = sType2.getDerivedProperties();
                for (int i = 0; i < props.length; i++)
                    if (!propsByName.containsKey(props[i].getName()))
                        propsByName.put(props[i].getName(), props[i]);
                sType2 = sType2.getBaseType();
            }
            return (SchemaProperty[]) propsByName.values().toArray(new SchemaProperty[0]);
        }
        else
            return sType.getDerivedProperties();
    }

    private void printExtensionImplMethods(SchemaType sType) throws IOException
    {
        SchemaTypeImpl sImpl = getImpl(sType);
        if (sImpl == null)
            return;

        InterfaceExtension[] exts = sImpl.getInterfaceExtensions();
        if (exts != null) for (int i = 0; i < exts.length; i++)
        {
            InterfaceExtension.MethodSignature[] methods = exts[i].getMethods();
            if (methods != null)
            {
                for (int j = 0; j < methods.length; j++)
                {
                    printJavaDoc("Implementation method for interface " + exts[i].getStaticHandler());
                    printInterfaceMethodDecl(methods[j]);
                    startBlock();
                    printInterfaceMethodImpl(exts[i].getStaticHandler(), methods[j]);
                    endBlock();
                }
            }
        }
    }

    void printInterfaceMethodDecl(InterfaceExtension.MethodSignature method) throws IOException
    {
        StringBuffer decl = new StringBuffer(60);

        decl.append("public ").append(method.getReturnType());
        decl.append(" ").append(method.getName()).append("(");

        String[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++)
        {
            if (i != 0)
                decl.append(", ");
            decl.append(paramTypes[i]).append(" p").append(i);
        }

        decl.append(")");

        String[] exceptions = method.getExceptionTypes();
        for (int i = 0; i < exceptions.length; i++)
            decl.append((i == 0 ? " throws " : ", ") + exceptions[i]);

        emit(decl.toString());
    }

    void printInterfaceMethodImpl(String handler, InterfaceExtension.MethodSignature method) throws IOException
    {
        StringBuffer impl = new StringBuffer(60);

        if (!method.getReturnType().equals("void"))
            impl.append("return ");

        impl.append(handler).append(".").append(method.getName()).append("(this");

        String[] params = method.getParameterTypes();
        for (int i = 0; i < params.length; i++)
            impl.append(", p" + i);

        impl.append(");");

        emit(impl.toString());
    }

    void printNestedTypeImpls(SchemaType sType, SchemaTypeSystem system) throws IOException
    {
        boolean redefinition = sType.getName() != null &&
            sType.getName().equals(sType.getBaseType().getName());
        while (sType != null)
        {
            SchemaType[] anonTypes = sType.getAnonymousTypes();
            for (int i = 0; i < anonTypes.length; i++)
            {
                if (anonTypes[i].isSkippedAnonymousType())
                    printNestedTypeImpls(anonTypes[i], system);
                else
                    printInnerTypeImpl(anonTypes[i], system, true);
            }
            // For redefinition by extension, go ahead and print the anonymous
            // types in the base
            if (!redefinition ||
                (sType.getDerivationType() != SchemaType.DT_EXTENSION && !sType.isSimpleType()))
                break;
            sType = sType.getBaseType();
        }
    }
}
