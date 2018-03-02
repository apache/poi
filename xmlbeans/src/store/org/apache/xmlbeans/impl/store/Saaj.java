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

package org.apache.xmlbeans.impl.store;

import javax.xml.namespace.QName;

import java.util.Iterator;
import java.util.Locale;


import javax.xml.transform.Source;

import org.apache.xmlbeans.impl.soap.Detail;
import org.apache.xmlbeans.impl.soap.DetailEntry;
import org.apache.xmlbeans.impl.soap.MimeHeaders;
import org.apache.xmlbeans.impl.soap.Name;
import org.apache.xmlbeans.impl.soap.SOAPBody;
import org.apache.xmlbeans.impl.soap.SOAPBodyElement;
import org.apache.xmlbeans.impl.soap.SOAPElement;
import org.apache.xmlbeans.impl.soap.SOAPEnvelope;
import org.apache.xmlbeans.impl.soap.SOAPException;
import org.apache.xmlbeans.impl.soap.SOAPFactory;
import org.apache.xmlbeans.impl.soap.SOAPFault;
import org.apache.xmlbeans.impl.soap.SOAPHeader;
import org.apache.xmlbeans.impl.soap.SOAPHeaderElement;
import org.apache.xmlbeans.impl.soap.SOAPPart;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public interface Saaj
{
    // Use in XmlOptions to enable SAAJ support in store
    
    public static final String SAAJ_IMPL =  "SAAJ_IMPL";
    
    public interface SaajCallback
    {
        void   setSaajData ( Node n, Object o );
        Object getSaajData ( Node n );

        Element createSoapElement ( QName name, QName parentName );

        Element importSoapElement ( Document doc, Element elem, boolean deep, QName parentName );
    }

    void setCallback ( SaajCallback callback );

    Class identifyElement ( QName name, QName parentName );
    
    void        soapNode_detachNode       ( org.apache.xmlbeans.impl.soap.Node soapNode );
    void        soapNode_recycleNode      ( org.apache.xmlbeans.impl.soap.Node node );
    String      soapNode_getValue         ( org.apache.xmlbeans.impl.soap.Node node );
    void        soapNode_setValue         ( org.apache.xmlbeans.impl.soap.Node node, String value );
    SOAPElement soapNode_getParentElement ( org.apache.xmlbeans.impl.soap.Node node );
    void        soapNode_setParentElement ( org.apache.xmlbeans.impl.soap.Node node, SOAPElement soapElement );

    void        soapElement_removeContents              ( SOAPElement soapElement );
    String      soapElement_getEncodingStyle            ( SOAPElement soapElement );
    void        soapElement_setEncodingStyle            ( SOAPElement soapElement, String encodingStyle );
    boolean     soapElement_removeNamespaceDeclaration  ( SOAPElement soapElement, String prefix );
    Iterator    soapElement_getAllAttributes            ( SOAPElement soapElement );
    Iterator    soapElement_getChildElements            ( SOAPElement parent );
    Iterator    soapElement_getNamespacePrefixes        ( SOAPElement soapElement );
    SOAPElement soapElement_addAttribute                ( SOAPElement soapElement, Name name, String value ) throws SOAPException;
    SOAPElement soapElement_addChildElement             ( SOAPElement parent, SOAPElement oldChild ) throws SOAPException;
    SOAPElement soapElement_addChildElement             ( SOAPElement soapElement, Name name ) throws SOAPException;
    SOAPElement soapElement_addChildElement             ( SOAPElement soapElement, String localName ) throws SOAPException;
    SOAPElement soapElement_addChildElement             ( SOAPElement soapElement, String localName, String prefix ) throws SOAPException;
    SOAPElement soapElement_addChildElement             ( SOAPElement soapElement, String localName, String prefix, String uri ) throws SOAPException;
    SOAPElement soapElement_addNamespaceDeclaration     ( SOAPElement soapElement, String prefix, String uri );
    SOAPElement soapElement_addTextNode                 ( SOAPElement soapElement, String data );
    String      soapElement_getAttributeValue           ( SOAPElement soapElement, Name name );
    Iterator    soapElement_getChildElements            ( SOAPElement parent, Name name );
    Name        soapElement_getElementName              ( SOAPElement soapElement );
    String      soapElement_getNamespaceURI             ( SOAPElement soapElement, String prefix );
    Iterator    soapElement_getVisibleNamespacePrefixes ( SOAPElement soapElement );
    boolean     soapElement_removeAttribute             ( SOAPElement soapElement, Name name );

    SOAPBody   soapEnvelope_addBody    ( SOAPEnvelope soapEnvelope ) throws SOAPException;
    SOAPBody   soapEnvelope_getBody    ( SOAPEnvelope soapEnvelope ) throws SOAPException;
    SOAPHeader soapEnvelope_getHeader  ( SOAPEnvelope soapEnvelope ) throws SOAPException;
    SOAPHeader soapEnvelope_addHeader  ( SOAPEnvelope soapEnvelope ) throws SOAPException;
    Name       soapEnvelope_createName ( SOAPEnvelope soapEnvelope, String localName );
    Name       soapEnvelope_createName ( SOAPEnvelope soapEnvelope, String localName, String prefix, String namespaceURI );

    Iterator          soapHeader_examineAllHeaderElements            ( SOAPHeader soapHeader );
    Iterator          soapHeader_extractAllHeaderElements            ( SOAPHeader soapHeader );
    Iterator          soapHeader_examineHeaderElements               ( SOAPHeader soapHeader, String actor );
    Iterator          soapHeader_examineMustUnderstandHeaderElements ( SOAPHeader soapHeader, String mustUnderstandString );
    Iterator          soapHeader_extractHeaderElements               ( SOAPHeader soapHeader, String actor );
    SOAPHeaderElement soapHeader_addHeaderElement                    ( SOAPHeader soapHeader, Name name );

    void         soapPart_removeAllMimeHeaders      ( SOAPPart soapPart );
    void         soapPart_removeMimeHeader          ( SOAPPart soapPart, String name );
    Iterator     soapPart_getAllMimeHeaders         ( SOAPPart soapPart );
    SOAPEnvelope soapPart_getEnvelope               ( SOAPPart soapPart );
    Source       soapPart_getContent                ( SOAPPart soapPart );
    void         soapPart_setContent                ( SOAPPart soapPart, Source source );
    String[]     soapPart_getMimeHeader             ( SOAPPart soapPart, String name );
    void         soapPart_addMimeHeader             ( SOAPPart soapPart, String name, String value );
    void         soapPart_setMimeHeader             ( SOAPPart soapPart, String name, String value );
    Iterator     soapPart_getMatchingMimeHeaders    ( SOAPPart soapPart, String[] names );
    Iterator     soapPart_getNonMatchingMimeHeaders ( SOAPPart soapPart, String[] names );

    boolean         soapBody_hasFault       ( SOAPBody soapBody );
    SOAPFault       soapBody_addFault       ( SOAPBody soapBody ) throws SOAPException;
    SOAPFault       soapBody_getFault       ( SOAPBody soapBody );
    SOAPBodyElement soapBody_addBodyElement ( SOAPBody soapBody, Name name );
    SOAPBodyElement soapBody_addDocument    ( SOAPBody soapBody, Document document );
    SOAPFault       soapBody_addFault       ( SOAPBody soapBody, Name name, String s ) throws SOAPException;
    SOAPFault       soapBody_addFault       ( SOAPBody soapBody, Name faultCode, String faultString, Locale locale ) throws SOAPException;

    Detail   soapFault_addDetail            ( SOAPFault soapFault ) throws SOAPException;
    Detail   soapFault_getDetail            ( SOAPFault soapFault );
    String   soapFault_getFaultActor        ( SOAPFault soapFault );
    String   soapFault_getFaultCode         ( SOAPFault soapFault );
    Name     soapFault_getFaultCodeAsName   ( SOAPFault soapFault );
    String   soapFault_getFaultString       ( SOAPFault soapFault );
    Locale   soapFault_getFaultStringLocale ( SOAPFault soapFault );
    void     soapFault_setFaultActor        ( SOAPFault soapFault, String faultActorString );
    void     soapFault_setFaultCode         ( SOAPFault soapFault, Name faultCodeName ) throws SOAPException;
    void     soapFault_setFaultCode         ( SOAPFault soapFault, String faultCode ) throws SOAPException;
    void     soapFault_setFaultString       ( SOAPFault soapFault, String faultString );
    void     soapFault_setFaultString       ( SOAPFault soapFault, String faultString, Locale locale );

    void    soapHeaderElement_setMustUnderstand ( SOAPHeaderElement soapHeaderElement, boolean mustUnderstand );
    boolean soapHeaderElement_getMustUnderstand ( SOAPHeaderElement soapHeaderElement );
    void    soapHeaderElement_setActor          ( SOAPHeaderElement soapHeaderElement, String actor );
    String  soapHeaderElement_getActor          ( SOAPHeaderElement soapHeaderElement );

    boolean soapText_isComment ( org.apache.xmlbeans.impl.soap.Text text );

    DetailEntry detail_addDetailEntry   ( Detail detail, Name name );
    Iterator    detail_getDetailEntries ( Detail detail );
} 