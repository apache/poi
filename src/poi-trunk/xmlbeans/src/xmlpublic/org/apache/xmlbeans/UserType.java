package org.apache.xmlbeans;

import javax.xml.namespace.QName;

/**
 * The UserType class represents a mapping between an XML Schema QName and
 * a custom Java class type. It is used during code generation to determine
 * how to convert user-defined simple types to user defined Java classes.
 */
public interface UserType
{
    /**
     * The QName of the simple value that will be converted to a Java class.
     */
    QName getName();

    /**
     * The class name the simple value will be converted to.
     */
    String getJavaName();

    /**
     * A class which provides public static methods to convert {@link SimpleValue}
     * objects to and from the Java type specified by {@link #getJavaName()}.
     */
    String getStaticHandler();
}
