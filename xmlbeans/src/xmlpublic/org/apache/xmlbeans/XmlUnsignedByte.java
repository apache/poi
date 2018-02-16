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


/**
 * Corresponds to the XML Schema
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#unsignedByte">xs:unsignedByte</a> type.
 * One of the derived types based on <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#decimal">xs:decimal</a>.
 * <p>
 * Verified to be in the range 0..255 when validating.
 * <p>
 * As suggested by JAXB, convertible to Java short.
 */ 
public interface XmlUnsignedByte extends XmlUnsignedShort
{
    /** The constant {@link SchemaType} object representing this schema type. */
    public static final SchemaType type = XmlBeans.getBuiltinTypeSystem().typeForHandle("_BI_unsignedByte");
    
    /** Returns this value as a short */
    public short getShortValue();
    /** Sets this value as a short */
    public void setShortValue(short s);

    /**
     * Returns this value as a short
     * @deprecated replaced with {@link #getShortValue}
     **/
    public short shortValue();
    /**
     * Sets this value as a short
     * @deprecated replaced with {@link #setShortValue}
     **/
    public void set(short s);

    /**
     * A class with methods for creating instances
     * of {@link XmlUnsignedByte}.
     */
    public static final class Factory
    {
        /** Creates an empty instance of {@link XmlUnsignedByte} */
        public static XmlUnsignedByte newInstance() {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        /** Creates an empty instance of {@link XmlUnsignedByte} */
        public static XmlUnsignedByte newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** Creates an immutable {@link XmlUnsignedByte} value */
        public static XmlUnsignedByte newValue(Object obj) {
          return (XmlUnsignedByte) type.newValue( obj ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from a String. For example: "<code>&lt;xml-fragment&gt;123&lt;/xml-fragment&gt;</code>". */
        public static XmlUnsignedByte parse(java.lang.String s) throws org.apache.xmlbeans.XmlException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( s, type, null ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from a String. For example: "<code>&lt;xml-fragment&gt;123&lt;/xml-fragment&gt;</code>". */
        public static XmlUnsignedByte parse(java.lang.String s, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( s, type, options ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from a File. */
        public static XmlUnsignedByte parse(java.io.File f) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( f, type, null ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from a File. */
        public static XmlUnsignedByte parse(java.io.File f, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( f, type, options ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from a URL. */
        public static XmlUnsignedByte parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( u, type, null ); }

        /** Parses a {@link XmlUnsignedByte} fragment from a URL. */
        public static XmlUnsignedByte parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( u, type, options ); }

        /** Parses a {@link XmlUnsignedByte} fragment from an InputStream. */
        public static XmlUnsignedByte parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from an InputStream. */
        public static XmlUnsignedByte parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from a Reader. */
        public static XmlUnsignedByte parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from a Reader. */
        public static XmlUnsignedByte parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from a DOM Node. */
        public static XmlUnsignedByte parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from a DOM Node. */
        public static XmlUnsignedByte parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from an XMLInputStream.
         * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
         */
        public static XmlUnsignedByte parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from an XMLInputStream.
         * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
         */
        public static XmlUnsignedByte parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from an XMLStreamReader. */
        public static XmlUnsignedByte parse(javax.xml.stream.XMLStreamReader xsr) throws org.apache.xmlbeans.XmlException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( xsr, type, null ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from an XMLStreamReader. */
        public static XmlUnsignedByte parse(javax.xml.stream.XMLStreamReader xsr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException{
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( xsr, type, options ); }
        
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

