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


import javax.xml.namespace.QName;


/**
 * Corresponds to the XML Schema
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#QName">xs:QName</a> type.
 * <p>
 * A QName is the logical combination of an XML namespace URI and a localName.
 * Although in an XML instance document, a QName appears as "prefix:localName",
 * the logical value of a QName does NOT contain any information about the
 * prefix, only the namespace URI to which the prefix maps.  For example,
 * two QNames "a:hello" and "b:hello" are perfectly equivalent if "a:" in
 * the first instance maps to the same URI as "b:" in the second instance.
 * <p>
 * Convertible to {@link javax.xml.namespace.QName}.
 */ 
public interface XmlQName extends XmlAnySimpleType
{
    /** The constant {@link SchemaType} object representing this schema type. */
    public static final SchemaType type = XmlBeans.getBuiltinTypeSystem().typeForHandle("_BI_QName");
    
    /** Returns this value as a {@link QName} */
    QName getQNameValue();
    /** Sets this value as a {@link QName} */
    void setQNameValue(QName name);

    /**
     * Returns this value as a {@link QName}
     * @deprecated replaced with {@link #getQNameValue}
     **/
    QName qNameValue();
    /**
     * Sets this value as a {@link QName}
     * @deprecated replaced with {@link #setQNameValue}
     **/
    void set(QName name);
    
    /**
     * A class with methods for creating instances
     * of {@link XmlQName}.
     */
    public static final class Factory
    {
        /** Creates an empty instance of {@link XmlQName} */
        public static XmlQName newInstance() {
          return (XmlQName) XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        /** Creates an empty instance of {@link XmlQName} */
        public static XmlQName newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (XmlQName) XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** Creates an immutable {@link XmlQName} value */
        public static XmlQName newValue(Object obj) {
          return (XmlQName) type.newValue( obj ); }
        
        /** Parses a {@link XmlQName} fragment from a String. For example: "<code>&lt;xml-fragment xmlns:x="http://openuri.org/"&gt;x:sample&lt;/xml-fragment&gt;</code>". */
        public static XmlQName parse(java.lang.String s) throws org.apache.xmlbeans.XmlException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( s, type, null ); }
        
        /** Parses a {@link XmlQName} fragment from a String. For example: "<code>&lt;xml-fragment xmlns:x="http://openuri.org/"&gt;x:sample&lt;/xml-fragment&gt;</code>". */
        public static XmlQName parse(java.lang.String s, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( s, type, options ); }
        
        /** Parses a {@link XmlQName} fragment from a File. */
        public static XmlQName parse(java.io.File f) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( f, type, null ); }
        
        /** Parses a {@link XmlQName} fragment from a File. */
        public static XmlQName parse(java.io.File f, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( f, type, options ); }
        
        /** Parses a {@link XmlQName} fragment from a URL. */
        public static XmlQName parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( u, type, null ); }

        /** Parses a {@link XmlQName} fragment from a URL. */
        public static XmlQName parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( u, type, options ); }

        /** Parses a {@link XmlQName} fragment from an InputStream. */
        public static XmlQName parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        /** Parses a {@link XmlQName} fragment from an InputStream. */
        public static XmlQName parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        /** Parses a {@link XmlQName} fragment from a Reader. */
        public static XmlQName parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        /** Parses a {@link XmlQName} fragment from a Reader. */
        public static XmlQName parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        /** Parses a {@link XmlQName} fragment from a DOM Node. */
        public static XmlQName parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        /** Parses a {@link XmlQName} fragment from a DOM Node. */
        public static XmlQName parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** Parses a {@link XmlQName} fragment from an XMLInputStream.
         * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
         */
        public static XmlQName parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** Parses a {@link XmlQName} fragment from an XMLInputStream.
         * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
         */
        public static XmlQName parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** Parses a {@link XmlQName} fragment from an XMLStreamReader. */
        public static XmlQName parse(javax.xml.stream.XMLStreamReader xsr) throws org.apache.xmlbeans.XmlException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( xsr, type, null ); }
        
        /** Parses a {@link XmlQName} fragment from an XMLStreamReader. */
        public static XmlQName parse(javax.xml.stream.XMLStreamReader xsr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException{
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( xsr, type, options ); }
        
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

