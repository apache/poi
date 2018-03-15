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
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#positiveInteger">xs:positiveInteger</a> type.
 * One of the derived types based on <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#decimal">xs:decimal</a>.
 * <p>
 * Verified to be positive when validating.
 * <p>
 * Convertible to {@link java.math.BigInteger}.
 */ 
public interface XmlPositiveInteger extends XmlNonNegativeInteger
{
    /** The constant {@link SchemaType} object representing this schema type. */
    public static final SchemaType type = XmlBeans.getBuiltinTypeSystem().typeForHandle("_BI_positiveInteger");
    
    /**
     * A class with methods for creating instances
     * of {@link XmlPositiveInteger}.
     */
    public static final class Factory
    {
        /** Creates an empty instance of {@link XmlPositiveInteger} */
        public static XmlPositiveInteger newInstance() {
          return (XmlPositiveInteger) XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        /** Creates an empty instance of {@link XmlPositiveInteger} */
        public static XmlPositiveInteger newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (XmlPositiveInteger) XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** Creates an immutable {@link XmlPositiveInteger} value */
        public static XmlPositiveInteger newValue(Object obj) {
          return (XmlPositiveInteger) type.newValue( obj ); }
        
        /** Parses a {@link XmlPositiveInteger} fragment from a String. For example: "<code>&lt;xml-fragment&gt;1234567890&lt;/xml-fragment&gt;</code>". */
        public static XmlPositiveInteger parse(java.lang.String s) throws org.apache.xmlbeans.XmlException {
          return (XmlPositiveInteger) XmlBeans.getContextTypeLoader().parse( s, type, null ); }
        
        /** Parses a {@link XmlPositiveInteger} fragment from a String. For example: "<code>&lt;xml-fragment&gt;1234567890&lt;/xml-fragment&gt;</code>". */
        public static XmlPositiveInteger parse(java.lang.String s, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (XmlPositiveInteger) XmlBeans.getContextTypeLoader().parse( s, type, options ); }
        
        /** Parses a {@link XmlPositiveInteger} fragment from a File. */
        public static XmlPositiveInteger parse(java.io.File f) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlPositiveInteger) XmlBeans.getContextTypeLoader().parse( f, type, null ); }
        
        /** Parses a {@link XmlPositiveInteger} fragment from a File. */
        public static XmlPositiveInteger parse(java.io.File f, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlPositiveInteger) XmlBeans.getContextTypeLoader().parse( f, type, options ); }
        
        /** Parses a {@link XmlPositiveInteger} fragment from a URL. */
        public static XmlPositiveInteger parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlPositiveInteger) XmlBeans.getContextTypeLoader().parse( u, type, null ); }

        /** Parses a {@link XmlPositiveInteger} fragment from a URL. */
        public static XmlPositiveInteger parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlPositiveInteger) XmlBeans.getContextTypeLoader().parse( u, type, options ); }

        /** Parses a {@link XmlPositiveInteger} fragment from an InputStream. */
        public static XmlPositiveInteger parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlPositiveInteger) XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        /** Parses a {@link XmlPositiveInteger} fragment from an InputStream. */
        public static XmlPositiveInteger parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlPositiveInteger) XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        /** Parses a {@link XmlPositiveInteger} fragment from a Reader. */
        public static XmlPositiveInteger parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlPositiveInteger) XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        /** Parses a {@link XmlPositiveInteger} fragment from a Reader. */
        public static XmlPositiveInteger parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlPositiveInteger) XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        /** Parses a {@link XmlPositiveInteger} fragment from a DOM Node. */
        public static XmlPositiveInteger parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (XmlPositiveInteger) XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        /** Parses a {@link XmlPositiveInteger} fragment from a DOM Node. */
        public static XmlPositiveInteger parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (XmlPositiveInteger) XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** Parses a {@link XmlPositiveInteger} fragment from an XMLInputStream.
         * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
         */
        public static XmlPositiveInteger parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (XmlPositiveInteger) XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** Parses a {@link XmlPositiveInteger} fragment from an XMLInputStream.
         * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
         */
        public static XmlPositiveInteger parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (XmlPositiveInteger) XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** Parses a {@link XmlPositiveInteger} fragment from an XMLStreamReader. */
        public static XmlPositiveInteger parse(javax.xml.stream.XMLStreamReader xsr) throws org.apache.xmlbeans.XmlException {
          return (XmlPositiveInteger) XmlBeans.getContextTypeLoader().parse( xsr, type, null ); }
        
        /** Parses a {@link XmlPositiveInteger} fragment from an XMLStreamReader. */
        public static XmlPositiveInteger parse(javax.xml.stream.XMLStreamReader xsr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException{
          return (XmlPositiveInteger) XmlBeans.getContextTypeLoader().parse( xsr, type, options ); }
        
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

