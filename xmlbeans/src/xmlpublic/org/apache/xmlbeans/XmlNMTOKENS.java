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


import java.util.List;


/**
 * Corresponds to the XML Schema
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#NMTOKENS">xs:NMTOKENS</a> type,
 * a list type.
 * <p>
 * Convertible to {@link List}.
 */ 
public interface XmlNMTOKENS extends XmlAnySimpleType
{
    /** The constant {@link SchemaType} object representing this schema type. */
    public static final SchemaType type = XmlBeans.getBuiltinTypeSystem().typeForHandle("_BI_NMTOKENS");
    
    /** Returns the value as a {@link List} of {@link String} values */
    List getListValue();
    /** Returns the value as a {@link List} of {@link XmlNMTOKEN} values */
    List xgetListValue();
    /** Sets the value as a {@link List} */
    void setListValue(List l);

    /**
     * Returns the value as a {@link List} of {@link String} values
     * @deprecated replaced by {@link #getListValue}
     **/
    List listValue();
    /**
     * Returns the value as a {@link List} of {@link XmlNMTOKEN} values
     * @deprecated replaced by {@link #getListValue}
     **/
    List xlistValue();
    /**
     * Sets the value as a {@link List}
     * @deprecated replaced by {@link #getListValue}
     **/
    void set(List l);


    /**
     * A class with methods for creating instances
     * of {@link XmlNMTOKENS}.
     */
    public static final class Factory
    {
        /** Creates an empty instance of {@link XmlNMTOKENS} */
        public static XmlNMTOKENS newInstance() {
          return (XmlNMTOKENS) XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        /** Creates an empty instance of {@link XmlNMTOKENS} */
        public static XmlNMTOKENS newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (XmlNMTOKENS) XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** Creates an immutable {@link XmlNMTOKENS} value */
        public static XmlNMTOKENS newValue(Object obj) {
          return (XmlNMTOKENS) type.newValue( obj ); }
        
        /** Parses a {@link XmlNMTOKENS} fragment from a String. For example: "<code>&lt;xml-fragment&gt;sample-1.1 sample-1.2 sample-1.3&lt;/xml-fragment&gt;</code>". */
        public static XmlNMTOKENS parse(java.lang.String s) throws org.apache.xmlbeans.XmlException {
          return (XmlNMTOKENS) XmlBeans.getContextTypeLoader().parse( s, type, null ); }
        
        /** Parses a {@link XmlNMTOKENS} fragment from a String. For example: "<code>&lt;xml-fragment&gt;sample-1.1 sample-1.2 sample-1.3&lt;/xml-fragment&gt;</code>". */
        public static XmlNMTOKENS parse(java.lang.String s, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (XmlNMTOKENS) XmlBeans.getContextTypeLoader().parse( s, type, options ); }
        
        /** Parses a {@link XmlNMTOKENS} fragment from a File. */
        public static XmlNMTOKENS parse(java.io.File f) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlNMTOKENS) XmlBeans.getContextTypeLoader().parse( f, type, null ); }
        
        /** Parses a {@link XmlNMTOKENS} fragment from a File. */
        public static XmlNMTOKENS parse(java.io.File f, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlNMTOKENS) XmlBeans.getContextTypeLoader().parse( f, type, options ); }
        
        /** Parses a {@link XmlNMTOKENS} fragment from a URL. */
        public static XmlNMTOKENS parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlNMTOKENS) XmlBeans.getContextTypeLoader().parse( u, type, null ); }

        /** Parses a {@link XmlNMTOKENS} fragment from a URL. */
        public static XmlNMTOKENS parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlNMTOKENS) XmlBeans.getContextTypeLoader().parse( u, type, options ); }

        /** Parses a {@link XmlNMTOKENS} fragment from an InputStream. */
        public static XmlNMTOKENS parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlNMTOKENS) XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        /** Parses a {@link XmlNMTOKENS} fragment from an InputStream. */
        public static XmlNMTOKENS parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlNMTOKENS) XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        /** Parses a {@link XmlNMTOKENS} fragment from a Reader. */
        public static XmlNMTOKENS parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlNMTOKENS) XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        /** Parses a {@link XmlNMTOKENS} fragment from a Reader. */
        public static XmlNMTOKENS parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlNMTOKENS) XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        /** Parses a {@link XmlNMTOKENS} fragment from a DOM Node. */
        public static XmlNMTOKENS parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (XmlNMTOKENS) XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        /** Parses a {@link XmlNMTOKENS} fragment from a DOM Node. */
        public static XmlNMTOKENS parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (XmlNMTOKENS) XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** Parses a {@link XmlNMTOKENS} fragment from an XMLInputStream.
         * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
         */
        public static XmlNMTOKENS parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (XmlNMTOKENS) XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** Parses a {@link XmlNMTOKENS} fragment from an XMLInputStream.
         * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
         */
        public static XmlNMTOKENS parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (XmlNMTOKENS) XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** Parses a {@link XmlNMTOKENS} fragment from an XMLStreamReader. */
        public static XmlNMTOKENS parse(javax.xml.stream.XMLStreamReader xsr) throws org.apache.xmlbeans.XmlException {
          return (XmlNMTOKENS) XmlBeans.getContextTypeLoader().parse( xsr, type, null ); }
        
        /** Parses a {@link XmlNMTOKENS} fragment from an XMLStreamReader. */
        public static XmlNMTOKENS parse(javax.xml.stream.XMLStreamReader xsr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException{
          return (XmlNMTOKENS) XmlBeans.getContextTypeLoader().parse( xsr, type, options ); }
        
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

