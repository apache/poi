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

package org.apache.xmlbeans.impl.tool;

import java.util.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.io.*;
import java.math.BigInteger;


import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.util.HexBin;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.SchemaLocalAttribute;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.soap.SOAPArrayType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import javax.xml.namespace.QName;

public class XsbDumper
{
    public static void printUsage()
    {
        System.out.println("Prints the contents of an XSB file in human-readable form.");
        System.out.println("An XSB file contains schema meta information needed to ");
        System.out.println("perform tasks such as binding and validation.");
        System.out.println("Usage: dumpxsb myfile.xsb");
        System.out.println("    myfile.xsb - Path to an XSB file.");
        System.out.println();
    }

    public static void main(String[] args)
    {
        if (args.length == 0) {
            printUsage();
            System.exit(0);
            return;
        }

        for (int i = 0; i < args.length; i++)
        {
            dump(new File(args[i]), true);
        }
    }

    private static void dump(File file, boolean force)
    {
        if (file.isDirectory())
        {
            File[] files = file.listFiles(
                new FileFilter()
                {
                    public boolean accept(File file)
                        { return file.isDirectory() || file.isFile() && file.getName().endsWith(".xsb"); }
                }
            );
            for (int i = 0; i < files.length; i++)
            {
                dump(files[i], false);
            }
        }
        else if (file.getName().endsWith(".jar") || file.getName().endsWith(".zip"))
        {
            dumpZip(file);
        }
        else if (force || file.getName().endsWith(".xsb"))
        {
            try
            {
                System.out.println(file.toString());
                dump(new FileInputStream(file), "  ");
                System.out.println();
            }
            catch (FileNotFoundException e)
            {
                System.out.println(e.toString());
            }
        }
    }

    public static void dumpZip(File file)
    {
        try
        {
            ZipFile zipFile = new ZipFile(file);
            Enumeration e = zipFile.entries();
            while (e.hasMoreElements())
            {
                ZipEntry entry = (ZipEntry)e.nextElement();
                if (entry.getName().endsWith(".xsb"))
                {
                    System.out.println(entry.getName());
                    dump(zipFile.getInputStream(entry), "  ");
                    System.out.println();
                }
            }
        }
        catch (IOException e)
        {
            System.out.println(e.toString());
        }
    }

    public static void dump(InputStream input)
    {
        dump(input, "", System.out);
    }

    public static void dump(InputStream input, String indent)
    {
        dump(input, indent, System.out);
    }

    public static void dump(InputStream input, String indent, PrintStream output)
    {
        XsbDumper dumper = new XsbDumper(input, indent, output);
        dumper.dumpAll();
    }

    private XsbDumper(InputStream stream, String indent, PrintStream ostream)
    {
        _input = new DataInputStream(stream);
        _indent = indent;
        _out = ostream;
    }

    void flush() { _out.flush(); }
    void emit(String str) { _out.println(_indent + str); flush(); }
    void emit() { _out.println(); flush(); }
    void error(Exception e) { _out.println(e.toString()); flush(); IllegalStateException e2 = new IllegalStateException( e.getMessage() ); e2.initCause( e ); throw e2; }
    void error(String str) { _out.println(str); flush(); IllegalStateException e2 = new IllegalStateException( str ); throw e2; }
    private String _indent;
    private PrintStream _out;
    void indent() { _indent += "  "; }
    void outdent() { _indent = _indent.substring(0, _indent.length() - 2); }

    public static final int DATA_BABE = 0xDA7ABABE;
    public static final int MAJOR_VERSION = 2;
    public static final int MINOR_VERSION = 24;

    public static final int FILETYPE_SCHEMAINDEX = 1;
    public static final int FILETYPE_SCHEMATYPE = 2;
    public static final int FILETYPE_SCHEMAELEMENT = 3;
    public static final int FILETYPE_SCHEMAATTRIBUTE = 4;
    public static final int FILETYPE_SCHEMAPOINTER = 5;
    public static final int FILETYPE_SCHEMAMODELGROUP = 6;
    public static final int FILETYPE_SCHEMAATTRIBUTEGROUP = 7;

    static String filetypeString(int code)
    {
        switch (code)
        {
            case FILETYPE_SCHEMAINDEX: return "FILETYPE_SCHEMAINDEX";
            case FILETYPE_SCHEMATYPE: return "FILETYPE_SCHEMATYPE";
            case FILETYPE_SCHEMAELEMENT: return "FILETYPE_SCHEMAELEMENT";
            case FILETYPE_SCHEMAATTRIBUTE: return "FILETYPE_SCHEMAATTRIBUTE";
            case FILETYPE_SCHEMAPOINTER: return "FILETYPE_SCHEMAPOINTER";
            case FILETYPE_SCHEMAMODELGROUP: return "FILETYPE_SCHEMAMODELGROUP";
            case FILETYPE_SCHEMAATTRIBUTEGROUP: return "FILETYPE_SCHEMAATTRIBUTEGROUP";
            default:
                return "Unknown FILETYPE (" + code + ")";
        }
    }

    public static final int FLAG_PART_SKIPPABLE = 1;
    public static final int FLAG_PART_FIXED = 4;
    public static final int FLAG_PART_NILLABLE = 8;
    public static final int FLAG_PART_BLOCKEXT = 16;
    public static final int FLAG_PART_BLOCKREST = 32;
    public static final int FLAG_PART_BLOCKSUBST = 64;
    public static final int FLAG_PART_ABSTRACT = 128;
    public static final int FLAG_PART_FINALEXT = 256;
    public static final int FLAG_PART_FINALREST = 512;

    static String particleflagsString(int flags)
    {
        StringBuffer result = new StringBuffer();
        if ((flags & FLAG_PART_SKIPPABLE) != 0) result.append("FLAG_PART_SKIPPABLE | ");
        if ((flags & FLAG_PART_FIXED) != 0) result.append("FLAG_PART_FIXED | ");
        if ((flags & FLAG_PART_NILLABLE) != 0) result.append("FLAG_PART_NILLABLE | ");
        if ((flags & FLAG_PART_BLOCKEXT) != 0) result.append("FLAG_PART_BLOCKEXT | ");
        if ((flags & FLAG_PART_BLOCKREST) != 0) result.append("FLAG_PART_BLOCKREST | ");
        if ((flags & FLAG_PART_BLOCKSUBST) != 0) result.append("FLAG_PART_BLOCKSUBST | ");
        if ((flags & FLAG_PART_ABSTRACT) != 0) result.append("FLAG_PART_ABSTRACT | ");
        if ((flags & FLAG_PART_FINALEXT) != 0) result.append("FLAG_PART_FINALEXT | ");
        if ((flags & FLAG_PART_FINALREST) != 0) result.append("FLAG_PART_FINALREST | ");
        if (result.length() == 0) result.append("0 | ");
        return result.substring(0, result.length() - 3);
    }

    public static final int FLAG_PROP_ISATTR = 1;
    public static final int FLAG_PROP_JAVASINGLETON = 2;
    public static final int FLAG_PROP_JAVAOPTIONAL = 4;
    public static final int FLAG_PROP_JAVAARRAY = 8;

    static String propertyflagsString(int flags)
    {
        StringBuffer result = new StringBuffer();
        if ((flags & FLAG_PROP_ISATTR) != 0) result.append("FLAG_PROP_ISATTR | ");
        if ((flags & FLAG_PROP_JAVASINGLETON) != 0) result.append("FLAG_PROP_JAVASINGLETON | ");
        if ((flags & FLAG_PROP_JAVAOPTIONAL) != 0) result.append("FLAG_PROP_JAVAOPTIONAL | ");
        if ((flags & FLAG_PROP_JAVAARRAY) != 0) result.append("FLAG_PROP_JAVAARRAY | ");
        if (result.length() == 0) result.append("0 | ");
        return result.substring(0, result.length() - 3);
    }

    public static final int FIELD_NONE = 0;
    public static final int FIELD_GLOBAL = 1;
    public static final int FIELD_LOCALATTR = 2;
    public static final int FIELD_LOCALELT = 3;

    static String containerfieldTypeString(int code)
    {
        switch (code)
        {
            case FIELD_NONE: return "FIELD_NONE";
            case FIELD_GLOBAL: return "FIELD_GLOBAL";
            case FIELD_LOCALATTR: return "FIELD_LOCALATTR";
            case FIELD_LOCALELT: return "FIELD_LOCALELT";
            default:
                return "Unknown container field type (" + code + ")";
        }
    }

    // type flags
    static final int FLAG_SIMPLE_TYPE     = 0x1;
    static final int FLAG_DOCUMENT_TYPE   = 0x2;
    static final int FLAG_ORDERED         = 0x4;
    static final int FLAG_BOUNDED         = 0x8;
    static final int FLAG_FINITE          = 0x10;
    static final int FLAG_NUMERIC         = 0x20;
    static final int FLAG_STRINGENUM      = 0x40;
    static final int FLAG_UNION_OF_LISTS  = 0x80;
    static final int FLAG_HAS_PATTERN     = 0x100;
    static final int FLAG_ORDER_SENSITIVE = 0x200;
    static final int FLAG_TOTAL_ORDER     = 0x400;
    static final int FLAG_COMPILED        = 0x800;
    static final int FLAG_BLOCK_EXT       = 0x1000;
    static final int FLAG_BLOCK_REST      = 0x2000;
    static final int FLAG_FINAL_EXT       = 0x4000;
    static final int FLAG_FINAL_REST      = 0x8000;
    static final int FLAG_FINAL_UNION     = 0x10000;
    static final int FLAG_FINAL_LIST      = 0x20000;
    static final int FLAG_ABSTRACT        = 0x40000;
    static final int FLAG_ATTRIBUTE_TYPE  = 0x80000;

    static String typeflagsString(int flags)
    {
        StringBuffer result = new StringBuffer();
        if ((flags & FLAG_SIMPLE_TYPE) != 0) result.append("FLAG_SIMPLE_TYPE | ");
        if ((flags & FLAG_DOCUMENT_TYPE) != 0) result.append("FLAG_DOCUMENT_TYPE | ");
        if ((flags & FLAG_ATTRIBUTE_TYPE) != 0) result.append("FLAG_ATTRIBUTE_TYPE | ");
        if ((flags & FLAG_ORDERED) != 0) result.append("FLAG_ORDERED | ");
        if ((flags & FLAG_BOUNDED) != 0) result.append("FLAG_BOUNDED | ");
        if ((flags & FLAG_FINITE) != 0) result.append("FLAG_FINITE | ");
        if ((flags & FLAG_NUMERIC) != 0) result.append("FLAG_NUMERIC | ");
        if ((flags & FLAG_STRINGENUM) != 0) result.append("FLAG_STRINGENUM | ");
        if ((flags & FLAG_UNION_OF_LISTS) != 0) result.append("FLAG_UNION_OF_LISTS | ");
        if ((flags & FLAG_HAS_PATTERN) != 0) result.append("FLAG_HAS_PATTERN | ");
        if ((flags & FLAG_TOTAL_ORDER) != 0) result.append("FLAG_TOTAL_ORDER | ");
        if ((flags & FLAG_COMPILED) != 0) result.append("FLAG_COMPILED | ");
        if ((flags & FLAG_BLOCK_EXT) != 0) result.append("FLAG_BLOCK_EXT | ");
        if ((flags & FLAG_BLOCK_REST) != 0) result.append("FLAG_BLOCK_REST | ");
        if ((flags & FLAG_FINAL_EXT) != 0) result.append("FLAG_FINAL_EXT | ");
        if ((flags & FLAG_FINAL_REST) != 0) result.append("FLAG_FINAL_REST | ");
        if ((flags & FLAG_FINAL_UNION) != 0) result.append("FLAG_FINAL_UNION | ");
        if ((flags & FLAG_FINAL_LIST) != 0) result.append("FLAG_FINAL_LIST | ");
        if ((flags & FLAG_ABSTRACT) != 0) result.append("FLAG_ABSTRACT | ");
        if (result.length() == 0) result.append("0 | ");
        return result.substring(0, result.length() - 3);
    }

    void dumpAll()
    {
        int filetype = dumpHeader();
        switch (filetype)
        {
            case FILETYPE_SCHEMAINDEX:
                dumpIndexData();
                return;
            case FILETYPE_SCHEMATYPE:
                dumpTypeFileData();
                break;
            case FILETYPE_SCHEMAELEMENT:
                dumpParticleData(true);
                break;
            case FILETYPE_SCHEMAATTRIBUTE:
                dumpAttributeData(true);
                break;
            case FILETYPE_SCHEMAPOINTER:
                dumpPointerData();
                break;
            case FILETYPE_SCHEMAMODELGROUP:
                dumpModelGroupData();
                break;
            case FILETYPE_SCHEMAATTRIBUTEGROUP:
                dumpAttributeGroupData();
                break;
        }
        readEnd();
    }

    static String hex32String(int i)
    {
        return Integer.toHexString(i);
    }

    protected int dumpHeader()
    {
        int magic = readInt();
        emit("Magic cookie: " + hex32String(magic));

        if (magic != DATA_BABE)
        {
            emit("Wrong magic cookie.");
            return 0;
        }

        _majorver = readShort();
        _minorver = readShort();
        if (atLeast(2, 18, 0))
            _releaseno = readShort();

        emit("Major version: " + _majorver);
        emit("Minor version: " + _minorver);
        emit("Release number: " + _releaseno);

        if (_majorver != MAJOR_VERSION || _minorver > MINOR_VERSION)
        {
            emit("Incompatible version.");
            return 0;
        }

        int actualfiletype = readShort();
        emit("Filetype: " + filetypeString(actualfiletype));

        _stringPool = new StringPool();
        _stringPool.readFrom(_input);

        return actualfiletype;
    }

    void dumpPointerData()
    {
        emit("Type system: " + readString());
    }

    protected void dumpIndexData()
    {
        // has a handle pool (count, handle/type, handle/type...)
        int size = readShort();
        emit("Handle pool (" + size + "):");
        indent();
        for (int i = 0; i < size; i++)
        {
            String handle = readString();
            int code = readShort();
            emit(handle + " (" + filetypeString(code) + ")");
        }
        outdent();

        dumpQNameMap("Global elements");

        // qname map of global attributes
        dumpQNameMap("Global attributes");

        // attr groups and model groups
        dumpQNameMap("Model groups");
        dumpQNameMap("Attribute groups");

        dumpQNameMap("Identity constraints");

        // qname map of global types
        dumpQNameMap("Global types");

        // qname map of document types, by the qname of the contained element
        dumpQNameMap("Document types");

        // qname map of attribute types, by the qname of the contained attribute
        dumpQNameMap("Attribute types");

        // all the types indexed by classname
        dumpClassnameIndex("All types by classname");

        // all the namespaces
        dumpStringArray("Defined namespaces");

        // version 15 stuff for redefines
        if (atLeast(2, 15, 0))
        {
            dumpQNameMap("Redefined global types");
            dumpQNameMap("Redfined model groups");
            dumpQNameMap("Redfined attribute groups");
        }

        // version 19 annotations
        if (atLeast(2, 19, 0))
            dumpAnnotations();

        readEnd();
    }


    class StringPool
    {
        private List intsToStrings = new ArrayList();
        private Map stringsToInts = new HashMap();

        StringPool()
        {
            intsToStrings.add(null);
        }

        String stringForCode(int code)
        {
            if (code == 0)
                return null;
            return (String)intsToStrings.get(code);
        }

        int codeForString(String str)
        {
            if (str == null)
                return 0;
            Integer result = (Integer)stringsToInts.get(str);
            if (result == null)
            {
                result = new Integer(intsToStrings.size());
                intsToStrings.add(str);
                stringsToInts.put(str, result);
            }
            return result.intValue();
        }

        void readFrom(DataInputStream input)
        {
            if (intsToStrings.size() != 1 || stringsToInts.size() != 0)
                throw new IllegalStateException();

            try
            {
                int size = input.readShort();
                emit("String pool (" + size + "):");
                indent();
                for (int i = 1; i < size; i++)
                {
                    String str = input.readUTF();
                    int code = codeForString(str);
                    if (code != i)
                        throw new IllegalStateException();
                    emit(code + " = \"" + str + "\"");
                }
                outdent();
            }
            catch (IOException e)
            {
                emit(e.toString());
            }
        }
    }

    // active while loading one type.
    DataInputStream _input;
    StringPool _stringPool;

    int readShort()
    {
        try
        {
            return _input.readUnsignedShort();
        }
        catch (IOException e)
        {
            error(e);
            return 0;
        }
    }

    int readInt()
    {
        try
        {
            return _input.readInt();
        }
        catch (IOException e)
        {
            error(e);
            return 0;
        }
    }

    String readString()
    {
        return _stringPool.stringForCode(readShort());
    }

    QName readQName()
    {
        String namespace = readString();
        String localname = readString();
        if (localname == null)
            return null;
        return new QName(namespace, localname);
    }

    String readHandle()
    {
        return readString();
    }

    String readType()
    {
        return readHandle();
    }

    static String qnameString(QName qname)
    {
        if (qname == null)
            return "(null)";
        if (qname.getNamespaceURI() != null)
            return qname.getLocalPart() + "@" + qname.getNamespaceURI();
        else
            return qname.getLocalPart();
    }

    static String qnameSetString(QNameSet set)
    {
        return set.toString();
    }

    void dumpQNameMap(String fieldname)
    {
        int size = readShort();
        emit(fieldname + " (" + size + "):");
        indent();
        for (int i = 0; i < size; i++)
        {
            emit(qnameString(readQName()) + " = " + readHandle());
        }
        outdent();
    }

    void dumpTypeArray(String fieldname)
    {
        int size = readShort();
        emit(fieldname + " (" + size + "):");
        indent();
        for (int i = 0; i < size; i++)
        {
            emit(i + " = " + readType());
        }
        outdent();
    }

    void dumpClassnameIndex(String fieldname)
    {
        int size = readShort();
        emit(fieldname + " (" + size + "):");
        indent();
        for (int i = 0; i < size; i++)
        {
            emit(readString() + " = " + readType());
        }
        outdent();
    }

    void dumpStringArray(String fieldname)
    {
        int size = readShort();
        emit(fieldname + " (" + size + "):");
        indent();
        for (int i = 0; i < size; i++)
        {
            emit(readString());
        }
        outdent();
    }

    void readEnd()
    {
        try
        {
            _input.close();
        }
        catch (IOException e)
        {
            // oh, well.
        }
        _input = null;
        _stringPool = null;
    }

    static String particleTypeString(int spt)
    {
        switch (spt)
        {
            case SchemaParticle.ALL: return "ALL";
            case SchemaParticle.CHOICE: return "CHOICE";
            case SchemaParticle.ELEMENT: return "ELEMENT";
            case SchemaParticle.SEQUENCE: return "SEQUENCE";
            case SchemaParticle.WILDCARD: return "WILDCARD";
            default:
                return "Unknown particle type (" + spt + ")";
        }
    }

    static String bigIntegerString(BigInteger bigint)
    {
        if (bigint == null)
            return "(null)";
        return bigint.toString();
    }

    static String wcprocessString(int code)
    {
        switch (code)
        {
            case SchemaParticle.STRICT: return "STRICT";
            case SchemaParticle.SKIP: return "SKIP";
            case SchemaParticle.LAX: return "LAX";
            case 0: return "NOT_WILDCARD";
            default:
                return "Unknown process type (" + code + ")";
        }
    }

    void dumpAnnotation()
    {
        if (!atLeast(2, 19, 0))
            return; // no annotations in this version of the file

        int n = readInt();
        if (n == -1)
            return; // no annotation present
        emit("Annotation");
        boolean empty = true;
        indent();
        if (n > 0)
        {
            emit("Attributes (" + n + "):");
            indent();
            for (int i = 0; i < n; i++)
            {
                if (atLeast(2, 24, 0))
                    emit("Name: " + qnameString(readQName()) +
                        ", Value: " + readString() +
                        ", ValueURI: " + readString());
                else
                    emit("Name: " + qnameString(readQName()) +
                        ", Value: " + readString());
            }
            outdent();
            empty = false;
        }

        n = readInt();
        if (n > 0)
        {
            emit("Documentation elements (" + n + "):");
            indent();
            for (int i = 0; i < n; i++)
                emit(readString());
            outdent();
            empty = false;
        }

        n = readInt();
        if (n > 0)
        {
            emit("Appinfo elements (" + n + "):");
            indent();
            for (int i = 0; i < n; i++)
                emit(readString());
            outdent();
            empty = false;
        }
        if (empty)
            emit("<empty>");
        outdent();
    }

    void dumpAnnotations()
    {
        int n = readInt();
        if (n > 0)
        {
            emit("Top-level annotations (" + n + "):");
            indent();
            for (int i = 0; i < n; i++)
                dumpAnnotation();
            outdent();
        }
    }

    void dumpParticleData(boolean global)
    {
        int particleType = readShort();
        emit(particleTypeString(particleType) + ":");
        indent();
        int particleFlags = readShort();
        emit("Flags: " + particleflagsString(particleFlags));

        emit("MinOccurs: " + bigIntegerString(readBigInteger()));
        emit("MaxOccurs: " + bigIntegerString(readBigInteger()));

        emit("Transition: " + qnameSetString(readQNameSet()));

        switch (particleType)
        {
            case SchemaParticle.WILDCARD:
                emit("Wildcard set: " + qnameSetString(readQNameSet()));
                emit("Wildcard process: " + wcprocessString(readShort()));
                break;

            case SchemaParticle.ELEMENT:
                emit("Name: " + qnameString(readQName()));
                emit("Type: " + readType());
                emit("Default: " + readString());
                if (atLeast(2, 16, 0))
                    emit("Default value: " + readXmlValueObject());
                emit("WsdlArrayType: " + SOAPArrayTypeString(readSOAPArrayType()));
                dumpAnnotation();
                if (global)
                {
                    if (atLeast(2, 17, 0))
                        emit("Substitution group ref: " + readHandle());
                    int substGroupCount = readShort();
                    emit("Substitution group members (" + substGroupCount + ")");
                    indent();
                    for (int i = 0; i < substGroupCount; i++)
                    {
                        emit(qnameString(readQName()));
                    }
                    outdent();
                }
                int count = readShort();
                emit("Identity constraints (" + count + "):");
                indent();
                for (int i = 0; i < count; i++)
                {
                    emit(readHandle());
                }
                outdent();
                if (global)
                    emit("Filename: " + readString());
                break;

            case SchemaParticle.ALL:
            case SchemaParticle.SEQUENCE:
            case SchemaParticle.CHOICE:
                dumpParticleArray("Particle children");
                break;

            default:
                error("Unrecognized schema particle type");
        }
        outdent();
    }

    void dumpParticleArray(String fieldname)
    {
        int count = readShort();
        emit(fieldname + "(" + count + "):");
        indent();
        for (int i = 0; i < count; i++)
            dumpParticleData(false);
        outdent();
    }

    static String complexVarietyString(int code)
    {
        switch (code)
        {
            case SchemaType.EMPTY_CONTENT: return "EMPTY_CONTENT";
            case SchemaType.SIMPLE_CONTENT: return "SIMPLE_CONTENT";
            case SchemaType.ELEMENT_CONTENT: return "ELEMENT_CONTENT";
            case SchemaType.MIXED_CONTENT: return "MIXED_CONTENT";
            default:
                return "Unknown complex variety (" + code + ")";
        }
    }

    static String simpleVarietyString(int code)
    {
        switch (code)
        {
            case SchemaType.ATOMIC: return "ATOMIC";
            case SchemaType.LIST: return "LIST";
            case SchemaType.UNION: return "UNION";
            default:
                return "Unknown simple variety (" + code + ")";
        }
    }

    String facetCodeString(int code)
    {
        switch (code)
        {
            case SchemaType.FACET_LENGTH: return "FACET_LENGTH";
            case SchemaType.FACET_MIN_LENGTH: return "FACET_MIN_LENGTH";
            case SchemaType.FACET_MAX_LENGTH: return "FACET_MAX_LENGTH";
            case SchemaType.FACET_MIN_EXCLUSIVE: return "FACET_MIN_EXCLUSIVE";
            case SchemaType.FACET_MIN_INCLUSIVE: return "FACET_MIN_INCLUSIVE";
            case SchemaType.FACET_MAX_INCLUSIVE: return "FACET_MAX_INCLUSIVE";
            case SchemaType.FACET_MAX_EXCLUSIVE: return "FACET_MAX_EXCLUSIVE";
            case SchemaType.FACET_TOTAL_DIGITS: return "FACET_TOTAL_DIGITS";
            case SchemaType.FACET_FRACTION_DIGITS: return "FACET_FRACTION_DIGITS";
            default:
                return "Unknown facet code (" + code + ")";
        }
    }

    String whitespaceCodeString(int code)
    {
        switch (code)
        {
            case SchemaType.WS_COLLAPSE: return "WS_COLLAPSE";
            case SchemaType.WS_PRESERVE: return "WS_PRESERVE";
            case SchemaType.WS_REPLACE: return "WS_REPLACE";
            case SchemaType.WS_UNSPECIFIED: return "WS_UNSPECIFIED";
            default:
                return "Unknown whitespace code (" + code + ")";
        }
    }

    String derivationTypeString(int code)
    {
        switch (code)
        {
            case SchemaType.DT_NOT_DERIVED: return "DT_NOT_DERIVED";
            case SchemaType.DT_RESTRICTION: return "DT_RESTRICTION";
            case SchemaType.DT_EXTENSION: return "DT_EXTENSION";
            default:
                return "Unknown derivation code (" + code + ")";
        }
    }

    void dumpTypeFileData()
    {
        emit("Name: " + qnameString(readQName()));
        emit("Outer type: " + readType());
        emit("Depth: " + readShort());
        emit("Base type: " + readType());
        emit("Derivation type: " + derivationTypeString(readShort()));
        dumpAnnotation();

        emit("Container field:");
        indent();
        int containerfieldtype = readShort();
        emit("Reftype: " + containerfieldTypeString(containerfieldtype));
        switch (containerfieldtype)
        {
            case FIELD_GLOBAL:
                emit("Handle: " + readHandle());
                break;
            case FIELD_LOCALATTR:
                emit("Index: " + readShort());
                break;
            case FIELD_LOCALELT:
                emit("Index: " + readShort());
                break;
        }
        outdent();
        emit("Java class name: " + readString());
        emit("Java impl class name: " + readString());

        dumpTypeArray("Anonymous types");

        emit("Anonymous union member ordinal: " + readShort());

        int flags;
        flags = readInt();
        emit("Flags: " + typeflagsString(flags));
        boolean isComplexType = ((flags & FLAG_SIMPLE_TYPE) == 0);

        int complexVariety = SchemaType.NOT_COMPLEX_TYPE;
        if (isComplexType)
        {
            complexVariety = readShort();
            emit("Complex variety: " + complexVarietyString(complexVariety));

            if (atLeast(2, 23, 0))
                emit("Content based on type: " + readType());

            int attrCount = readShort();
            emit("Attribute model (" + attrCount + "):");
            indent();
            for (int i = 0; i < attrCount; i++)
                dumpAttributeData(false);

            emit("Wildcard set: " + qnameSetString(readQNameSet()));
            emit("Wildcard process: " + wcprocessString(readShort()));
            outdent();

            // Attribute Property Table
            int attrPropCount = readShort();
            emit("Attribute properties (" + attrPropCount + "):");
            indent();
            for (int i = 0; i < attrPropCount; i++)
            {
                dumpPropertyData();
            }
            outdent();

            if (complexVariety == SchemaType.ELEMENT_CONTENT || complexVariety == SchemaType.MIXED_CONTENT)
            {
                emit("IsAll: " + readShort());

                // Content model tree
                dumpParticleArray("Content model");

                // Element Property Table
                int elemPropCount = readShort();
                emit("Element properties (" + elemPropCount + "):");
                indent();
                for (int i = 0; i < elemPropCount; i++)
                {
                    dumpPropertyData();
                }
                outdent();
            }
        }

        if (!isComplexType || complexVariety == SchemaType.SIMPLE_CONTENT)
        {
            int simpleVariety = readShort();
            emit("Simple type variety: " + simpleVarietyString(simpleVariety));

            boolean isStringEnum = ((flags & FLAG_STRINGENUM) != 0);

            int facetCount = readShort();
            emit("Facets (" + facetCount + "):");
            indent();
            for (int i = 0; i < facetCount; i++)
            {
                emit(facetCodeString(readShort()));
                emit("Value: " + readXmlValueObject());
                emit("Fixed: " + readShort());
            }
            outdent();

            emit("Whitespace rule: " + whitespaceCodeString(readShort()));

            int patternCount = readShort();
            emit("Patterns (" + patternCount + "):");
            indent();
            for (int i = 0; i < patternCount; i++)
            {
                emit(readString());
            }
            outdent();

            int enumCount = readShort();
            emit("Enumeration values (" + enumCount + "):");
            indent();
            for (int i = 0; i < enumCount; i++)
            {
                emit(readXmlValueObject());
            }
            outdent();

            emit("Base enum type: " + readType());
            if (isStringEnum)
            {
                int seCount = readShort();
                emit("String enum entries (" + seCount + "):");
                indent();
                for (int i = 0; i < seCount; i++)
                {
                    emit("\"" + readString() + "\" -> " + readShort() + " = " + readString());
                }
                outdent();
            }

            switch (simpleVariety)
            {
                case SchemaType.ATOMIC:
                    emit("Primitive type: " + readType());
                    emit("Decimal size: " + readInt());
                    break;

                case SchemaType.LIST:
                    emit("List item type: " + readType());
                    break;

                case SchemaType.UNION:
                    dumpTypeArray("Union members");
                    break;

                default:
                    error("Unknown simple type variety");
            }
        }

        emit("Filename: " + readString());
    }

    static String attruseCodeString(int code)
    {
        switch (code)
        {
            case SchemaLocalAttribute.OPTIONAL: return "OPTIONAL";
            case SchemaLocalAttribute.REQUIRED: return "REQUIRED";
            case SchemaLocalAttribute.PROHIBITED: return "PROHIBITED";
            default:
                return "Unknown use code (" + code + ")";
        }
    }

    void dumpAttributeData(boolean global)
    {
        emit("Name: " + qnameString(readQName()));
        emit("Type: " + readType());
        emit("Use: " + attruseCodeString(readShort()));
        emit("Default: " + readString());
        if (atLeast(2, 16, 0))
            emit("Default value: " + readXmlValueObject());
        emit("Fixed: " + readShort());
        emit("WsdlArrayType: " + SOAPArrayTypeString(readSOAPArrayType()));
        dumpAnnotation();
        if (global)
            emit("Filename: " + readString());
    }

    private static final XmlOptions prettyOptions =
        new XmlOptions().setSavePrettyPrint();

    void dumpXml()
    {
        String xml = readString();
        try
        {
            emit( XmlObject.Factory.parse( xml ).xmlText( prettyOptions ) );
        }
        catch ( XmlException x )
        {
            emit( "!!!!!! BAD XML !!!!!" );
            emit( xml );
        }
    }

    void dumpModelGroupData()
    {
        emit("Name: " + qnameString(readQName()));
        emit("Target namespace: " + readString());
        emit("Chameleon: " + readShort());
        if (atLeast(2, 22, 0))
            emit("Element form default: " + readString());
        if (atLeast(2, 22, 0))
            emit("Attribute form default: " + readString());
        if (atLeast(2, 15, 0))
            emit("Redefine: " + readShort());
        emit("Model Group Xml: ");
        dumpXml();
        dumpAnnotation();
        if (atLeast(2, 21, 0))
            emit("Filename: " + readString());
    }

    void dumpAttributeGroupData()
    {
        emit("Name: " + qnameString(readQName()));
        emit("Target namespace: " + readString());
        emit("Chameleon: " + readShort());
        if (atLeast(2, 22, 0))
            emit("Form default: " + readString());
        if (atLeast(2, 15, 0))
            emit("Redefine: " + readShort());
        emit("Attribute Group Xml: ");
        dumpXml();
        dumpAnnotation();
        if (atLeast(2, 21, 0))
            emit("Filename: " + readString());
    }

    static String alwaysString(int code)
    {
        switch (code)
        {
            case SchemaProperty.CONSISTENTLY: return "CONSISTENTLY";
            case SchemaProperty.NEVER: return "NEVER";
            case SchemaProperty.VARIABLE: return "VARIABLE";
            default:
                return "Unknown frequency code (" + code + ")";
        }
    }

    static String jtcString(int code)
    {
        switch (code)
        {
            case SchemaProperty.XML_OBJECT: return "XML_OBJECT";
            case SchemaProperty.JAVA_BOOLEAN: return "JAVA_BOOLEAN";
            case SchemaProperty.JAVA_FLOAT: return "JAVA_FLOAT";
            case SchemaProperty.JAVA_DOUBLE: return "JAVA_DOUBLE";
            case SchemaProperty.JAVA_BYTE: return "JAVA_BYTE";
            case SchemaProperty.JAVA_SHORT: return "JAVA_SHORT";
            case SchemaProperty.JAVA_INT: return "JAVA_INT";
            case SchemaProperty.JAVA_LONG: return "JAVA_LONG";

            case SchemaProperty.JAVA_BIG_DECIMAL: return "JAVA_BIG_DECIMAL";
            case SchemaProperty.JAVA_BIG_INTEGER: return "JAVA_BIG_INTEGER";
            case SchemaProperty.JAVA_STRING: return "JAVA_STRING";
            case SchemaProperty.JAVA_BYTE_ARRAY: return "JAVA_BYTE_ARRAY";
            case SchemaProperty.JAVA_GDATE: return "JAVA_GDATE";
            case SchemaProperty.JAVA_GDURATION: return "JAVA_GDURATION";
            case SchemaProperty.JAVA_DATE: return "JAVA_DATE";
            case SchemaProperty.JAVA_QNAME: return "JAVA_QNAME";
            case SchemaProperty.JAVA_CALENDAR: return "JAVA_CALENDAR";
            case SchemaProperty.JAVA_LIST: return "JAVA_LIST";

            case SchemaProperty.JAVA_ENUM: return "JAVA_ENUM";
            case SchemaProperty.JAVA_OBJECT: return "JAVA_OBJECT";

            default:
                return "Unknown java type code (" + code + ")";
        }
    }

    void dumpPropertyData()
    {
        emit("Property");
        indent();
        emit("Name: " + qnameString(readQName()));
        emit("Type: " + readType());
        int propflags = readShort();
        emit("Flags: " + propertyflagsString(propflags));
        emit("Container type: " + readType());
        emit("Min occurances: " + bigIntegerString(readBigInteger()));
        emit("Max occurances: " + bigIntegerString(readBigInteger()));
        emit("Nillable: " + alwaysString(readShort()));
        emit("Default: " + alwaysString(readShort()));
        emit("Fixed: " + alwaysString(readShort()));
        emit("Default text: " + readString());
        emit("Java prop name: " + readString());
        emit("Java type code: " + jtcString(readShort()));
        emit("Type for java signature: " + readType());
        if (atMost(2, 19, 0))
            emit("Java setter delimiter: " + qnameSetString(readQNameSet()));
        if (atLeast(2, 16, 0))
            emit("Default value: " + readXmlValueObject());
        if (((propflags & FLAG_PROP_ISATTR) == 0) && atLeast(2, 17, 0))
        {
            int size = readShort();
            emit("Accepted substitutions (" + size + "):");
            for (int i = 0 ; i < size ; i++)
                emit("  Accepted name " + readQName());
        }
        outdent();
    }

    String readXmlValueObject()
    {
        String type = readType();
        if (type == null)
            return "null";

        int btc = readShort();
        String value;
        switch (btc)
        {
            default:
                assert(false);
            case 0:
                value = "nil";
                break;

            case SchemaType.BTC_ANY_SIMPLE:
            case SchemaType.BTC_ANY_URI:
            case SchemaType.BTC_STRING:
            case SchemaType.BTC_DURATION:
            case SchemaType.BTC_DATE_TIME:
            case SchemaType.BTC_TIME:
            case SchemaType.BTC_DATE:
            case SchemaType.BTC_G_YEAR_MONTH:
            case SchemaType.BTC_G_YEAR:
            case SchemaType.BTC_G_MONTH_DAY:
            case SchemaType.BTC_G_DAY:
            case SchemaType.BTC_G_MONTH:
            case SchemaType.BTC_DECIMAL:
            case SchemaType.BTC_BOOLEAN:
                value = readString();
                break;

            case SchemaType.BTC_BASE_64_BINARY:
            case SchemaType.BTC_HEX_BINARY:
                {
                    value = new String(HexBin.encode(readByteArray()));
                    if (value.length() > 19)
                        value = value.subSequence(0, 16) + "...";
                    break;
                }

            case SchemaType.BTC_QNAME:
            case SchemaType.BTC_NOTATION:
                value = QNameHelper.pretty(readQName());
                break;

            case SchemaType.BTC_FLOAT:
            case SchemaType.BTC_DOUBLE:
                value = Double.toString(readDouble());
                break;
        }
        return value + " (" + type + ": " + btc +")";
    }

    double readDouble()
    {
        try
        {
            return _input.readDouble();
        }
        catch (IOException e)
        {
            error(e);
            return 0.0;
        }
    }

    String SOAPArrayTypeString(SOAPArrayType t)
    {
        if (t == null)
            return "null";
        return QNameHelper.pretty(t.getQName()) + t.soap11DimensionString();
    }

    SOAPArrayType readSOAPArrayType()
    {
        QName qName = readQName();
        String dimensions = readString();
        if (qName == null)
            return null;
        return new SOAPArrayType(qName, dimensions);
    }

    QNameSet readQNameSet()
    {
        int flag = readShort();

        Set uriSet = new HashSet();
        int uriCount = readShort();
        for (int i = 0; i < uriCount; i++)
            uriSet.add(readString());

        Set qnameSet1 = new HashSet();
        int qncount1 = readShort();
        for (int i = 0; i < qncount1; i++)
            qnameSet1.add(readQName());

        Set qnameSet2 = new HashSet();
        int qncount2 = readShort();
        for (int i = 0; i < qncount2; i++)
            qnameSet2.add(readQName());

        if (flag == 1)
            return QNameSet.forSets(uriSet, null, qnameSet1, qnameSet2);
        else
            return QNameSet.forSets(null, uriSet, qnameSet2, qnameSet1);
    }

    byte[] readByteArray()
    {
        try
        {
            int len = _input.readShort();
            byte[] result = new byte[len];
            _input.readFully(result);
            return result;
        }
        catch (IOException e)
        {
            error(e);
            return null;
        }
    }

    BigInteger readBigInteger()
    {
        byte[] result = readByteArray();
        if (result.length == 0)
            return null;
        if (result.length == 1 && result[0] == 0)
            return BigInteger.ZERO;
        if (result.length == 1 && result[0] == 1)
            return BigInteger.ONE;
        return new BigInteger(result);
    }

    static final byte[] SINGLE_ZERO_BYTE = new byte[] { (byte)0 };

    private int _majorver;
    private int _minorver;
    private int _releaseno;


    protected boolean atLeast(int majorver, int minorver, int releaseno)
    {
        if (_majorver > majorver)
            return true;
        if (_majorver < majorver)
            return false;
        if (_minorver > minorver)
            return true;
        if (_minorver < minorver)
            return false;
        return (_releaseno >= releaseno);
    }

    protected boolean atMost(int majorver, int minorver, int releaseno)
    {
        if (_majorver > majorver)
            return false;
        if (_majorver < majorver)
            return true;
        if (_minorver > minorver)
            return false;
        if (_minorver < minorver)
            return true;
        return (_releaseno <= releaseno);
    }

}
