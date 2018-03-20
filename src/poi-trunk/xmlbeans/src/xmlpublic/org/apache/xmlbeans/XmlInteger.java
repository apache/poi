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

package org.apache.xmlbeans;

import javax.xml.stream.XMLStreamReader;


import java.math.BigInteger;

/**
 * Corresponds to the XML Schema
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#integer">xs:integer</a> type.
 * One of the derived types based on <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#decimal">xs:decimal</a>.
 * <p>
 * This type should not be confused with <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#int">xs:int</a>
 * or Java {@link Integer}.  This type represents an arbitrary-precision integer with
 * any number of digits, while a Java int or an xs:int is a 32-bit finite-precision integer.
 * <p>
 * Convertible to a Java {@link BigInteger}.
 */ 
public interface XmlInteger extends XmlDecimal
{
    /** The constant {@link SchemaType} object representing this schema type. */
    public static final SchemaType type = XmlBeans.getBuiltinTypeSystem().typeForHandle("_BI_integer");
    
    /** Returns this value as a {@link BigInteger} */
    BigInteger getBigIntegerValue();
    /** Sets this value as a {@link BigInteger} */
    void setBigIntegerValue(BigInteger bi);

    /**
     * Returns this value as a {@link BigInteger}
     * @deprecated replaced with {@link #getBigIntegerValue}
     **/
    BigInteger bigIntegerValue();
    /**
     * Sets this value as a {@link BigInteger}
     * @deprecated replaced with {@link #setBigIntegerValue}
     **/
    void set(BigInteger bi);

    /**
     * A class with methods for creating instances
     * of {@link XmlInteger}.
     */
    public static final class Factory
    {
        /** Creates an empty instance of {@link XmlInteger} */
        public static XmlInteger newInstance() {
          return (XmlInteger) XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        /** Creates an empty instance of {@link XmlInteger} */
        public static XmlInteger newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (XmlInteger) XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** Creates an immutable {@link XmlInteger} value */
        public static XmlInteger newValue(Object obj) {
          return (XmlInteger) type.newValue( obj ); }
        
        /** Parses a {@link XmlInteger} fragment from a String. For example: "<code>&lt;xml-fragment&gt;1234567890&lt;/xml-fragment&gt;</code>". */
        public static XmlInteger parse(java.lang.String s) throws org.apache.xmlbeans.XmlException {
          return (XmlInteger) XmlBeans.getContextTypeLoader().parse( s, type, null ); }
        
        /** Parses a {@link XmlInteger} fragment from a String. For example: "<code>&lt;xml-fragment&gt;1234567890&lt;/xml-fragment&gt;</code>". */
        public static XmlInteger parse(java.lang.String s, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (XmlInteger) XmlBeans.getContextTypeLoader().parse( s, type, options ); }
        
        /** Parses a {@link XmlInteger} fragment from a File. */
        public static XmlInteger parse(java.io.File f) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlInteger) XmlBeans.getContextTypeLoader().parse( f, type, null ); }
        
        /** Parses a {@link XmlInteger} fragment from a File. */
        public static XmlInteger parse(java.io.File f, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlInteger) XmlBeans.getContextTypeLoader().parse( f, type, options ); }
        
        /** Parses a {@link XmlInteger} fragment from a URL. */
        public static XmlInteger parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlInteger) XmlBeans.getContextTypeLoader().parse( u, type, null ); }

        /** Parses a {@link XmlInteger} fragment from a URL. */
        public static XmlInteger parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlInteger) XmlBeans.getContextTypeLoader().parse( u, type, options ); }

        /** Parses a {@link XmlInteger} fragment from an InputStream. */
        public static XmlInteger parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlInteger) XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        /** Parses a {@link XmlInteger} fragment from an InputStream. */
        public static XmlInteger parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlInteger) XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        /** Parses a {@link XmlInteger} fragment from a Reader. */
        public static XmlInteger parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlInteger) XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        /** Parses a {@link XmlInteger} fragment from a Reader. */
        public static XmlInteger parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlInteger) XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        /** Parses a {@link XmlInteger} fragment from a DOM Node. */
        public static XmlInteger parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (XmlInteger) XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        /** Parses a {@link XmlInteger} fragment from a DOM Node. */
        public static XmlInteger parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (XmlInteger) XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** Parses a {@link XmlInteger} fragment from an XMLInputStream.
         * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
         */
        public static XmlInteger parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (XmlInteger) XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** Parses a {@link XmlInteger} fragment from an XMLInputStream.
         * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
         */
        public static XmlInteger parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (XmlInteger) XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** Parses a {@link XmlInteger} fragment from an XMLStreamReader. */
        public static XmlInteger parse(javax.xml.stream.XMLStreamReader xsr) throws org.apache.xmlbeans.XmlException {
          return (XmlInteger) XmlBeans.getContextTypeLoader().parse( xsr, type, null ); }
        
        /** Parses a {@link XmlInteger} fragment from an XMLStreamReader. */
        public static XmlInteger parse(javax.xml.stream.XMLStreamReader xsr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException{
          return (XmlInteger) XmlBeans.getContextTypeLoader().parse( xsr, type, options ); }
        
        /** Returns a validating XMLInputStream.
         * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
         */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** Returns a validating XMLInputStream.
         * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
         */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}

