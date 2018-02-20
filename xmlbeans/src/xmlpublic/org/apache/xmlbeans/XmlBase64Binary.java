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
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#base64Binary">xs:base64Binary</a> type.
 * <p>
 * Convertible to a byte array. 
 */ 
public interface XmlBase64Binary extends XmlAnySimpleType
{
    /** The constant {@link SchemaType} object representing this schema type. */
    public static final SchemaType type = XmlBeans.getBuiltinTypeSystem().typeForHandle("_BI_base64Binary");
    
    /**
     * Returns this value as a byte array.
     * @deprecated replaced by {@link #getByteArrayValue}
     **/
    byte[] byteArrayValue();

    /**
     * Sets this value as a byte array.
     * @deprecated replaced by {@link #setByteArrayValue}
     **/
    void set(byte[] ba);

    /** Returns this value as a byte array. **/
    byte[] getByteArrayValue();
    /** Sets this value as a byte array. */
    void setByteArrayValue(byte[] ba);


    /**
     * A class with methods for creating instances
     * of {@link XmlBase64Binary}.
     */
    public static final class Factory
    {
        /** Creates an empty instance of {@link XmlBase64Binary} */
        public static XmlBase64Binary newInstance() {
          return (XmlBase64Binary) XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        /** Creates an empty instance of {@link XmlBase64Binary} */
        public static XmlBase64Binary newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (XmlBase64Binary) XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** Creates an immutable {@link XmlBase64Binary} value */
        public static XmlBase64Binary newValue(Object obj) {
          return (XmlBase64Binary) type.newValue( obj ); }
        
        /** Parses a {@link XmlBase64Binary} fragment from a String. For example: "<code>&lt;xml-fragment&gt;VGhpcyBzdHJp&lt;/xml-fragment&gt;</code>". */
        public static XmlBase64Binary parse(java.lang.String s) throws org.apache.xmlbeans.XmlException {
          return (XmlBase64Binary) XmlBeans.getContextTypeLoader().parse( s, type, null ); }
        
        /** Parses a {@link XmlBase64Binary} fragment from a String. For example: "<code>&lt;xml-fragment&gt;VGhpcyBzdHJp&lt;/xml-fragment&gt;</code>". */
        public static XmlBase64Binary parse(java.lang.String s, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (XmlBase64Binary) XmlBeans.getContextTypeLoader().parse( s, type, options ); }
        
        /** Parses a {@link XmlBase64Binary} fragment from a File. */
        public static XmlBase64Binary parse(java.io.File f) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlBase64Binary) XmlBeans.getContextTypeLoader().parse( f, type, null ); }
        
        /** Parses a {@link XmlBase64Binary} fragment from a File. */
        public static XmlBase64Binary parse(java.io.File f, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlBase64Binary) XmlBeans.getContextTypeLoader().parse( f, type, options ); }
        
        /** Parses a {@link XmlBase64Binary} fragment from a URL. */
        public static XmlBase64Binary parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlBase64Binary) XmlBeans.getContextTypeLoader().parse( u, type, null ); }

        /** Parses a {@link XmlBase64Binary} fragment from a URL. */
        public static XmlBase64Binary parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlBase64Binary) XmlBeans.getContextTypeLoader().parse( u, type, options ); }

        /** Parses a {@link XmlBase64Binary} fragment from an InputStream. */
        public static XmlBase64Binary parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlBase64Binary) XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        /** Parses a {@link XmlBase64Binary} fragment from an InputStream. */
        public static XmlBase64Binary parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlBase64Binary) XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        /** Parses a {@link XmlBase64Binary} fragment from a Reader. */
        public static XmlBase64Binary parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlBase64Binary) XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        /** Parses a {@link XmlBase64Binary} fragment from a Reader. */
        public static XmlBase64Binary parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlBase64Binary) XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        /** Parses a {@link XmlBase64Binary} fragment from a DOM Node. */
        public static XmlBase64Binary parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (XmlBase64Binary) XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        /** Parses a {@link XmlBase64Binary} fragment from a DOM Node. */
        public static XmlBase64Binary parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (XmlBase64Binary) XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** Parses a {@link XmlBase64Binary} fragment from an XMLInputStream.
         * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
         */
        public static XmlBase64Binary parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (XmlBase64Binary) XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** Parses a {@link XmlBase64Binary} fragment from an XMLInputStream.
         * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
         */
        public static XmlBase64Binary parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (XmlBase64Binary) XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** Parses a {@link XmlBase64Binary} fragment from an XMLStreamReader. */
        public static XmlBase64Binary parse(javax.xml.stream.XMLStreamReader xsr) throws org.apache.xmlbeans.XmlException {
          return (XmlBase64Binary) XmlBeans.getContextTypeLoader().parse( xsr, type, null ); }
        
        /** Parses a {@link XmlBase64Binary} fragment from an XMLStreamReader. */
        public static XmlBase64Binary parse(javax.xml.stream.XMLStreamReader xsr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException{
          return (XmlBase64Binary) XmlBeans.getContextTypeLoader().parse( xsr, type, options ); }
        
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

