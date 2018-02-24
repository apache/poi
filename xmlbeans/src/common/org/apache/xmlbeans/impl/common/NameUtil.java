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

package org.apache.xmlbeans.impl.common;

import javax.xml.namespace.QName;

import java.util.*;

public class NameUtil
{
    // punctuation characters
    public final static char HYPHEN = '\u002D';
    public final static char PERIOD = '\u002E';
    public final static char COLON  = '\u003A';
    public final static char USCORE = '\u005F';
    public final static char DOT    = '\u00B7';
    public final static char TELEIA = '\u0387';
    public final static char AYAH   = '\u06DD';
    public final static char ELHIZB = '\u06DE';

    private final static boolean DEBUG = false;

    private final static Set javaWords = new HashSet(Arrays.asList(
        new String[]
        {
              "assert",
              "abstract",
              "boolean",
              "break",
              "byte",
              "case",
              "catch",
              "char",
              "class",
              "const",
              "continue",
              "default",
              "do",
              "double",
              "else",
              "enum", // since JDK1.5
              "extends",
              "false", // not a keyword
              "final",
              "finally",
              "float",
              "for",
              "goto",
              "if",
              "implements",
              "import",
              "instanceof",
              "int",
              "interface",
              "long",
              "native",
              "new",
              "null", // not a keyword
              "package",
              "private",
              "protected",
              "public",
              "return",
              "short",
              "static",
              "strictfp",
              "super",
              "switch",
              "synchronized",
              "this",
              "threadsafe",
              "throw",
              "throws",
              "transient",
              "true", // not a keyword
              "try",
              "void",
              "volatile",
              "while",
        }
    ));

    private final static Set extraWords = new HashSet(Arrays.asList(
        new String[]
        {
              "i",          // used for indexes
              "target",     // used for parameter
              "org",        // used for package names
              "com",        // used for package names
        }
    ));

    /*
    private final static Set javaNames = new HashSet(Arrays.asList(
        new String[]
        {
            "CharSequence",
            "Cloneable",
            "Comparable",
            "Runnable",

            "Boolean",
            "Byte",
            "Character",
            "Class",
            "ClassLoader",
            "Compiler",
            "Double",
            "Float",
            "InheritableThreadLocal",
            "Integer",
            "Long",
            "Math",
            "Number",
            "Object",
            "Package",
            "Process",
            "Runtime",
            "RuntimePermission",
            "SecurityManager",
            "Short",
            "StackTraceElement",
            "StrictMath",
            "String",
            "StringBuffer",
            "System",
            "Thread",
            "ThreadGroup",
            "ThreadLocal",
            "Throwable",
            "Void",

            "ArithmeticException",
            "ArrayIndexOutOfBoundsException",
            "ArrayStoreException",
            "ClassCastException",
            "ClassNotFoundException",
            "CloneNotSupportedException",
            "Exception",
            "IllegalAccessException",
            "IllegalArgumentException",
            "IllegalMonitorStateException",
            "IllegalStateException",
            "IllegalThreadStateException",
            "IndexOutOfBoundsException",
            "InstantiationException",
            "InterruptedException",
            "NegativeArraySizeException",
            "NoSuchFieldException",
            "NoSuchMethodException",
            "NullPointerException",
            "NumberFormatException",
            "RuntimeException",
            "SecurityException",
            "StringIndexOutOfBoundsException",
            "UnsupportedOperationException",

            "AbstractMethodError",
            "AssertionError",
            "ClassCircularityError",
            "ClassFormatError",
            "Error",
            "ExceptionInInitializerError",
            "IllegalAccessError",
            "IncompatibleClassChangeError",
            "InstantiationError",
            "InternalError",
            "LinkageError",
            "NoClassDefFoundError",
            "NoSuchFieldError",
            "NoSuchMethodError",
            "OutOfMemoryError",
            "StackOverflowError",
            "ThreadDeath",
            "UnknownError",
            "UnsatisfiedLinkError",
            "UnsupportedClassVersionError",
            "VerifyError",
            "VirtualMachineError",
        }
    ));
    */

    private final static Set javaNames = new HashSet(Arrays.asList(
        new String[]
        {
            // 1. all the Java.lang classes [1.4.1 JDK].
            "CharSequence",
            "Cloneable",
            "Comparable",
            "Runnable",

            "Boolean",
            "Byte",
            "Character",
            "Class",
            "ClassLoader",
            "Compiler",
            "Double",
            "Float",
            "InheritableThreadLocal",
            "Integer",
            "Long",
            "Math",
            "Number",
            "Object",
            "Package",
            "Process",
            "Runtime",
            "RuntimePermission",
            "SecurityManager",
            "Short",
            "StackTraceElement",
            "StrictMath",
            "String",
            "StringBuffer",
            "System",
            "Thread",
            "ThreadGroup",
            "ThreadLocal",
            "Throwable",
            "Void",

            "ArithmeticException",
            "ArrayIndexOutOfBoundsException",
            "ArrayStoreException",
            "ClassCastException",
            "ClassNotFoundException",
            "CloneNotSupportedException",
            "Exception",
            "IllegalAccessException",
            "IllegalArgumentException",
            "IllegalMonitorStateException",
            "IllegalStateException",
            "IllegalThreadStateException",
            "IndexOutOfBoundsException",
            "InstantiationException",
            "InterruptedException",
            "NegativeArraySizeException",
            "NoSuchFieldException",
            "NoSuchMethodException",
            "NullPointerException",
            "NumberFormatException",
            "RuntimeException",
            "SecurityException",
            "StringIndexOutOfBoundsException",
            "UnsupportedOperationException",

            "AbstractMethodError",
            "AssertionError",
            "ClassCircularityError",
            "ClassFormatError",
            "Error",
            "ExceptionInInitializerError",
            "IllegalAccessError",
            "IncompatibleClassChangeError",
            "InstantiationError",
            "InternalError",
            "LinkageError",
            "NoClassDefFoundError",
            "NoSuchFieldError",
            "NoSuchMethodError",
            "OutOfMemoryError",
            "StackOverflowError",
            "ThreadDeath",
            "UnknownError",
            "UnsatisfiedLinkError",
            "UnsupportedClassVersionError",
            "VerifyError",
            "VirtualMachineError",

            // 2. other classes used as primitive types by xml beans
            "BigInteger",
            "BigDecimal",
            "Enum",
            "Date",
            "GDate",
            "GDuration",
            "QName",
            "List",

            // 3. the top few org.apache.xmlbeans names
            "XmlObject",
            "XmlCursor",
            "XmlBeans",
            "SchemaType",
        }
    ));

    public static boolean isValidJavaIdentifier(String id)
    {
        if (id == null)
            throw new IllegalArgumentException("id cannot be null");

        int len = id.length();
        if (len == 0)
            return false;

        if (javaWords.contains(id))
            return false;

        if (!Character.isJavaIdentifierStart(id.charAt(0)))
            return false;

        for (int i = 1; i < len; i++)
        {
            if (!Character.isJavaIdentifierPart(id.charAt(i)))
                return false;
        }

        return true;
    }

    public static String getClassNameFromQName(QName qname)
    {
        return getClassNameFromQName(qname, false);
    }

    public static String getClassNameFromQName(QName qname, boolean useJaxRpcRules)
    {
        String java_type = upperCamelCase(qname.getLocalPart(), useJaxRpcRules);

        String uri = qname.getNamespaceURI();
        String java_pkg = null;

        java_pkg = getPackageFromNamespace(uri, useJaxRpcRules);

        if (java_pkg != null)
            return java_pkg + "." + java_type;
        else
            return java_type;
    }

    private static final String JAVA_NS_PREFIX="java:";
    private static final String LANG_PREFIX = "java.";

    public static String getNamespaceFromPackage(final Class clazz)
    {
        Class curr_clazz = clazz;

        while (curr_clazz.isArray())
            curr_clazz = curr_clazz.getComponentType();

        String fullname = clazz.getName();
        int lastdot = fullname.lastIndexOf('.');
        String pkg_name = lastdot < 0 ? "" : fullname.substring(0, lastdot);

        //special case for builtin types
        /*
        if (curr_clazz.isPrimitive())
        {
            pkg_name = c.getJavaLanguageNamespaceUri();
        }
        else if (pkg_name.startsWith(LANG_PREFIX))
        {
            final String rem_str = pkg_name.substring(LANG_PREFIX.length());
            pkg_name = c.getJavaLanguageNamespaceUri() + "." + rem_str;
        }
        */
        return JAVA_NS_PREFIX + pkg_name;
    }

    private static boolean isUriSchemeChar(char ch)
    {
        return (ch >= 'a' && ch <='z' ||
            ch >= 'A' && ch <='Z' ||
            ch >= '0' && ch <='9' ||
            ch == '-' || ch == '.' || ch == '+');
    }

    private static boolean isUriAlphaChar(char ch)
    {
        return (ch >= 'a' && ch <='z' || ch >= 'A' && ch <= 'Z');
    }

    private static int findSchemeColon(String uri)
    {
        int len = uri.length();
        if (len == 0)
            return -1;
        if (!isUriAlphaChar(uri.charAt(0)))
            return -1;
        int i;
        for (i = 1; i < len; i++)
            if (!isUriSchemeChar(uri.charAt(i)))
                break;
        if (i == len)
            return -1;
        if (uri.charAt(i) != ':')
            return -1;
        // consume consecutive colons
        for (; i < len; i++)
            if (uri.charAt(i) != ':')
                break;
        // for the "scheme:::" case, return len-1
        return i-1;
    }

    private static String jls77String(String name)
    {
        StringBuffer buf = new StringBuffer(name);
        for (int i = 0; i < name.length(); i++)
        {
            // We need to also make sure that our package names don't contain the
            // "$" character in them, which, although a valid Java identifier part,
            // would create confusion when trying to generate fully-qualified names
            if (!Character.isJavaIdentifierPart(buf.charAt(i)) || '$' == buf.charAt(i))
                buf.setCharAt(i, '_');
        }
        if (buf.length() == 0 || !Character.isJavaIdentifierStart(buf.charAt(0)))
            buf.insert(0, '_');
        if (isJavaReservedWord(name))
            buf.append('_');
        return buf.toString();
    }

    private static List splitDNS(String dns)
    {
        // JAXB says: only split+reverse DNS if TLD matches known TLDs or ISO 3166
        // We are ignoring this now (TH)

        List result = new ArrayList();

        int end = dns.length();
        int begin = dns.lastIndexOf('.');
        for ( ; begin != -1 ; begin--)
        {
            if (dns.charAt(begin) == '.') {
                result.add(jls77String(dns.substring(begin + 1, end)));
                end = begin;
            }
        }
        result.add(jls77String(dns.substring(0, end)));

        // JAXB draft example implies removal of www
        if (result.size() >= 3 &&
                ((String)result.get(result.size() - 1)).toLowerCase().equals("www"))
            result.remove(result.size() - 1);

        return result;
    }

    private static String processFilename(String filename)
    {
        // JAXB says: strip 2 or 3 letter extension or ".html"

        int i = filename.lastIndexOf('.');
        if (i > 0 && (
                i + 1 + 2 == filename.length() ||
                i + 1 + 3 == filename.length() ||
                filename.substring(i + 1).toLowerCase() == "html"))
        {
            return filename.substring(0, i);
        }

        return filename;
    }

    public static String getPackageFromNamespace(String uri)
    {
        return getPackageFromNamespace(uri, false);
    }

    public static String getPackageFromNamespace(String uri, boolean useJaxRpcRules)
    {
        // special case: no namespace -> package "noNamespace"
        if (uri == null || uri.length() == 0)
            return "noNamespace";

        // apply draft JAXB rules
        int len = uri.length();
        int i = findSchemeColon(uri);
        List result = null;

        if (i == len-1)
        {
            // XMLBEANS-57: colon is at end so just use scheme as the package name
            result = new ArrayList();
            result.add(uri.substring(0, i));
        }
        else if (i >= 0 && uri.substring(0, i).equals("java"))
        {
            result =  Arrays.asList(uri.substring(i + 1).split("\\."));
        }
        else {
            result = new ArrayList();
            outer: for (i = i + 1; i < len; )
            {
                while (uri.charAt(i) == '/')
                    if (++i >= len) break outer;
                int start = i;
                while (uri.charAt(i) != '/')
                    if (++i >= len) break;
                int end = i;
                result.add(uri.substring(start, end));
            }
            if (result.size() > 1)
                result.set(result.size() - 1, processFilename((String)result.get(result.size() - 1)));

            if (result.size() > 0)
            {
                List splitdns = splitDNS((String)result.get(0));
                result.remove(0);
                result.addAll(0, splitdns);
            }
        }

        StringBuffer buf = new StringBuffer();
        for (Iterator it = result.iterator(); it.hasNext(); )
        {
            String part = nonJavaKeyword(lowerCamelCase((String)it.next(), useJaxRpcRules, true));
            if (part.length() > 0)
            {
                buf.append(part);
                buf.append('.');
            }
        }
        if (buf.length() == 0)
            return "noNamespace";
        if (useJaxRpcRules)
            return buf.substring(0, buf.length() - 1).toLowerCase();
        return buf.substring(0, buf.length() - 1); // chop off extra dot
    }

    public static void main(String[] args)
    {
        for (int i = 0; i < args.length; i++)
            System.out.println(upperCaseUnderbar(args[i]));
    }

    /**
     * Returns a upper-case-and-underbar string using the JAXB rules.
     * Always starts with a capital letter that is a valid
     * java identifier start. (If JAXB rules don't produce
     * one, then "X_" is prepended.)
     */
    public static String upperCaseUnderbar(String xml_name)
    {
        StringBuffer buf = new StringBuffer();
        List words = splitWords(xml_name, false);

        final int sz = words.size() - 1;
        if (sz >= 0 && !Character.isJavaIdentifierStart(((String)words.get(0)).charAt(0)))
            buf.append("X_");

        for(int i = 0 ; i < sz ; i++)
        {
            buf.append((String)words.get(i));
            buf.append(USCORE);
        }

        if (sz >= 0)
        {
            buf.append((String)words.get(sz));
        }

        //upcase entire buffer
        final int len = buf.length();
        for(int j = 0 ; j < len ; j++)
        {
            char c = buf.charAt(j);
            buf.setCharAt(j, Character.toUpperCase(c));
        }

        return buf.toString();
    }

    /**
     * Returns a camel-cased string using the JAXB rules.
     * Always starts with a capital letter that is a valid
     * java identifier start. (If JAXB rules don't produce
     * one, then "X" is prepended.)
     */
    public static String upperCamelCase(String xml_name)
    {
        return upperCamelCase(xml_name, false);
    }

    /**
     * Returns a camel-cased string, but either JAXB or JAX-RPC rules
     * are used
     */
    public static String upperCamelCase(String xml_name, boolean useJaxRpcRules)
    {
        StringBuffer buf = new StringBuffer();
        List words = splitWords(xml_name, useJaxRpcRules);

        if (words.size() > 0)
        {
            if (!Character.isJavaIdentifierStart(((String)words.get(0)).charAt(0)))
                buf.append("X");

            Iterator itr = words.iterator();
            while(itr.hasNext())
                buf.append((String)itr.next());
        }
        return buf.toString();
    }

    /**
     * Returns a camel-cased string using the JAXB rules,
     * where the first component is lowercased. Note that
     * if the first component is an acronym, the whole
     * thigns gets lowercased.
     * Always starts with a lowercase letter that is a valid
     * java identifier start. (If JAXB rules don't produce
     * one, then "x" is prepended.)
     */
    public static String lowerCamelCase(String xml_name)
    {
        return lowerCamelCase(xml_name, false, true);
    }

    /**
     * Returns a camel-cased string using the JAXB or JAX-RPC rules
     */
    public static String lowerCamelCase(String xml_name, boolean useJaxRpcRules,
                                        boolean fixGeneratedName)
    {
        StringBuffer buf = new StringBuffer();
        List words = splitWords(xml_name, useJaxRpcRules);

        if (words.size() > 0)
        {
            String first = ((String)words.get(0)).toLowerCase();
            char f = first.charAt(0);
            if (!Character.isJavaIdentifierStart(f) && fixGeneratedName)
                buf.append("x");
            buf.append(first);

            Iterator itr = words.iterator();
            itr.next(); // skip already-lowercased word
            while(itr.hasNext())
                buf.append((String)itr.next());
        }
        return buf.toString();
    }

    public static String upperCaseFirstLetter(String s)
    {
        if (s.length() == 0 || Character.isUpperCase(s.charAt(0)))
            return s;

        StringBuffer buf = new StringBuffer(s);
        buf.setCharAt(0, Character.toUpperCase(buf.charAt(0)));
        return buf.toString();
    }


    /**
       split an xml name into words via JAXB approach, upcasing first
       letter of each word as needed, if upcase is true

       ncname is xml ncname (i.e. no colons).
    */
    private static void addCapped(List list, String str)
    {
        if (str.length() > 0)
            list.add(upperCaseFirstLetter(str));
    }

    public static List splitWords(String name, boolean useJaxRpcRules)
    {
        List list = new ArrayList();
        int len = name.length();
        int start = 0;
        int prefix = START;
        for (int i = 0; i < len; i++)
        {
            int current = getCharClass(name.charAt(i), useJaxRpcRules);
            if (prefix != PUNCT && current == PUNCT)
            {
                addCapped(list, name.substring(start, i));
                while ((current = getCharClass(name.charAt(i), useJaxRpcRules)) == PUNCT)
                    if (++i >= len) return list;
                start = i;
            }
            else if ((prefix == DIGIT) != (current == DIGIT) ||
                     (prefix == LOWER && current != LOWER) ||
                     (isLetter(prefix) != isLetter(current)))
            {
                addCapped(list, name.substring(start, i));
                start = i;
            }
            else if (prefix == UPPER && current == LOWER && i > start + 1)
            {
                addCapped(list, name.substring(start, i - 1));
                start = i - 1;
            }
            prefix = current;
        }
        addCapped(list, name.substring(start));
        return list;
    }

    //char classes
    private final static int START = 0;
    private final static int PUNCT = 1;
    private final static int DIGIT = 2;
    private final static int MARK = 3;
    private final static int UPPER = 4;
    private final static int LOWER= 5;
    private final static int NOCASE= 6;

    public static int getCharClass(char c, boolean useJaxRpcRules)
    {
        //ordering is important here.
        if (isPunctuation(c, useJaxRpcRules))
            return PUNCT;
        else if (Character.isDigit(c))
            return DIGIT;
        else if (Character.isUpperCase(c))
            return UPPER;
        else if (Character.isLowerCase(c))
            return LOWER;
        else if (Character.isLetter(c))
            return NOCASE;
        else if (Character.isJavaIdentifierPart(c))
            return MARK;
        else
            return PUNCT; // not covered by JAXB: treat it as punctuation
    }

    private static boolean isLetter(int state)
    {
        return (state==UPPER
              || state==LOWER
              || state==NOCASE);
    }

    public static boolean isPunctuation(char c, boolean useJaxRpcRules)
    {
        return (c == HYPHEN
              || c == PERIOD
              || c == COLON
              || c == DOT
              || (c == USCORE && !useJaxRpcRules)
              || c == TELEIA
              || c == AYAH
              || c == ELHIZB);
    }

    /**
     * Intended to be applied to a lowercase-starting identifier that
     * may collide with a Java keyword.  If it does collide, this
     * prepends the letter "x".
     */
    public static String nonJavaKeyword(String word)
    {
        if (isJavaReservedWord(word))
            return 'x' + word;
        return word;
    }

    /**
     * Intended to be applied to a lowercase-starting identifier that
     * may collide with a Java keyword.  If it does collide, this
     * prepends the letter "x".
     */
    public static String nonExtraKeyword(String word)
    {
        if (isExtraReservedWord(word, true))
            return word + "Value";
        return word;
    }

    /**
     * Intended to be applied to an uppercase-starting identifier that
     * may collide with a java.lang.* classname.  If it does collide, this
     * prepends the letter "X".
     */
    public static String nonJavaCommonClassName(String name)
    {
        if (isJavaCommonClassName(name))
            return "X" + name;
        return name;
    }

    private static boolean isJavaReservedWord(String word)
    {
        return isJavaReservedWord(word, true);
    }

    private static boolean isJavaReservedWord(String word, boolean ignore_case)
    {
        if (ignore_case)
            word = word.toLowerCase();
        return javaWords.contains(word);
    }

    private static boolean isExtraReservedWord(String word, boolean ignore_case)
    {
        if (ignore_case)
            word = word.toLowerCase();
        return extraWords.contains(word);
    }

    public static boolean isJavaCommonClassName(String word)
    {
        return javaNames.contains(word);
    }
}
