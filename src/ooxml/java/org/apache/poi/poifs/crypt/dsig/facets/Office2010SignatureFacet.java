/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

/* ====================================================================
   This product contains an ASLv2 licensed version of the OOXML signer
   package from the eID Applet project
   http://code.google.com/p/eid-applet/source/browse/trunk/README.txt  
   Copyright (C) 2008-2014 FedICT.
   ================================================================= */ 

package org.apache.poi.poifs.crypt.dsig.facets;

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import javax.xml.crypto.MarshalException;

import org.apache.xmlbeans.XmlException;
import org.etsi.uri.x01903.v13.QualifyingPropertiesType;
import org.etsi.uri.x01903.v13.UnsignedPropertiesType;
import org.etsi.uri.x01903.v13.UnsignedSignaturePropertiesType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Work-around for Office2010 to accept the XAdES-BES/EPES signature.
 * 
 * xades:UnsignedProperties/xades:UnsignedSignatureProperties needs to be
 * present.
 * 
 * @author Frank Cornelis
 * 
 */
public class Office2010SignatureFacet extends SignatureFacet {

    @Override
    public void postSign(Document document)
    throws MarshalException {
        // check for XAdES-BES
        NodeList nl = document.getElementsByTagNameNS(XADES_132_NS, "QualifyingProperties");
        if (nl.getLength() != 1) {
            throw new MarshalException("no XAdES-BES extension present");
        }

        QualifyingPropertiesType qualProps;
        try {
            qualProps = QualifyingPropertiesType.Factory.parse(nl.item(0), DEFAULT_XML_OPTIONS);
        } catch (XmlException e) {
            throw new MarshalException(e);
        }
        
        // create basic XML container structure
        UnsignedPropertiesType unsignedProps = qualProps.getUnsignedProperties();
        if (unsignedProps == null) {
            unsignedProps = qualProps.addNewUnsignedProperties();
        }
        UnsignedSignaturePropertiesType unsignedSigProps = unsignedProps.getUnsignedSignatureProperties();
        if (unsignedSigProps == null) {
            /* unsignedSigProps = */ unsignedProps.addNewUnsignedSignatureProperties();
        }
        
        Node n = document.importNode(qualProps.getDomNode().getFirstChild(), true);
        nl.item(0).getParentNode().replaceChild(n, nl.item(0));
    }
}