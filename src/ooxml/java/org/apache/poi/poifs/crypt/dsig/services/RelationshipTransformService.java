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

package org.apache.poi.poifs.crypt.dsig.services;

import static org.apache.poi.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.Provider;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.TransformService;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.XmlSort;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.xpackage.x2006.digitalSignature.CTRelationshipReference;
import org.openxmlformats.schemas.xpackage.x2006.digitalSignature.RelationshipReferenceDocument;
import org.openxmlformats.schemas.xpackage.x2006.relationships.CTRelationship;
import org.openxmlformats.schemas.xpackage.x2006.relationships.CTRelationships;
import org.openxmlformats.schemas.xpackage.x2006.relationships.RelationshipsDocument;
import org.openxmlformats.schemas.xpackage.x2006.relationships.STTargetMode;
import org.w3.x2000.x09.xmldsig.TransformDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * JSR105 implementation of the RelationshipTransform transformation.
 * 
 * <p>
 * Specs: http://openiso.org/Ecma/376/Part2/12.2.4#26
 * </p>
 */
public class RelationshipTransformService extends TransformService {

    public static final String TRANSFORM_URI = "http://schemas.openxmlformats.org/package/2006/RelationshipTransform";

    private final List<String> sourceIds;

    private static final POILogger LOG = POILogFactory.getLogger(RelationshipTransformService.class);
    
    /**
     * Relationship Transform parameter specification class.
     */
    public static class RelationshipTransformParameterSpec implements TransformParameterSpec {
        List<String> sourceIds = new ArrayList<String>();
        public void addRelationshipReference(String relationshipId) {
            sourceIds.add(relationshipId);
        }
        public boolean hasSourceIds() {
            return !sourceIds.isEmpty();
        }
    }
    
    
    public RelationshipTransformService() {
        super();
        LOG.log(POILogger.DEBUG, "constructor");
        this.sourceIds = new ArrayList<String>();
    }

    /**
     * Register the provider for this TransformService
     * 
     * @see javax.xml.crypto.dsig.TransformService
     */
    public static synchronized void registerDsigProvider() {
        // the xml signature classes will try to find a special TransformerService,
        // which is ofcourse unknown to JCE before ...
        final String dsigProvider = "POIXmlDsigProvider";
        if (Security.getProperty(dsigProvider) == null) {
            Provider p = new Provider(dsigProvider, 1.0, dsigProvider){
                static final long serialVersionUID = 1L;
            };
            p.put("TransformService." + TRANSFORM_URI, RelationshipTransformService.class.getName());
            p.put("TransformService." + TRANSFORM_URI + " MechanismType", "DOM");
            Security.addProvider(p);
        }
    }
    
    
    @Override
    public void init(TransformParameterSpec params) throws InvalidAlgorithmParameterException {
        LOG.log(POILogger.DEBUG, "init(params)");
        if (!(params instanceof RelationshipTransformParameterSpec)) {
            throw new InvalidAlgorithmParameterException();
        }
        RelationshipTransformParameterSpec relParams = (RelationshipTransformParameterSpec) params;
        for (String sourceId : relParams.sourceIds) {
            this.sourceIds.add(sourceId);
        }
    }

    @Override
    public void init(XMLStructure parent, XMLCryptoContext context) throws InvalidAlgorithmParameterException {
        LOG.log(POILogger.DEBUG, "init(parent,context)");
        LOG.log(POILogger.DEBUG, "parent java type: " + parent.getClass().getName());
        DOMStructure domParent = (DOMStructure) parent;
        Node parentNode = domParent.getNode();
        
        try {
            TransformDocument transDoc = TransformDocument.Factory.parse(parentNode, DEFAULT_XML_OPTIONS);
            XmlObject xoList[] = transDoc.getTransform().selectChildren(RelationshipReferenceDocument.type.getDocumentElementName());
            if (xoList.length == 0) {
                LOG.log(POILogger.WARN, "no RelationshipReference/@SourceId parameters present");
            }
            for (XmlObject xo : xoList) {
                String sourceId = ((CTRelationshipReference)xo).getSourceId();
                LOG.log(POILogger.DEBUG, "sourceId: ", sourceId);
                this.sourceIds.add(sourceId);
            }
        } catch (XmlException e) {
            throw new InvalidAlgorithmParameterException(e);
        }
    }

    @Override
    public void marshalParams(XMLStructure parent, XMLCryptoContext context) throws MarshalException {
        LOG.log(POILogger.DEBUG, "marshallParams(parent,context)");
        DOMStructure domParent = (DOMStructure) parent;
        Element parentNode = (Element)domParent.getNode();
        // parentNode.setAttributeNS(XML_NS, "xmlns:mdssi", XML_DIGSIG_NS);
        Document doc = parentNode.getOwnerDocument();
        
        for (String sourceId : this.sourceIds) {
            RelationshipReferenceDocument relRef = RelationshipReferenceDocument.Factory.newInstance();
            relRef.addNewRelationshipReference().setSourceId(sourceId);
            Node n = relRef.getRelationshipReference().getDomNode();
            n = doc.importNode(n, true);
            parentNode.appendChild(n);
        }
    }
    
    public AlgorithmParameterSpec getParameterSpec() {
        LOG.log(POILogger.DEBUG, "getParameterSpec");
        return null;
    }

    public Data transform(Data data, XMLCryptoContext context) throws TransformException {
        LOG.log(POILogger.DEBUG, "transform(data,context)");
        LOG.log(POILogger.DEBUG, "data java type: " + data.getClass().getName());
        OctetStreamData octetStreamData = (OctetStreamData) data;
        LOG.log(POILogger.DEBUG, "URI: " + octetStreamData.getURI());
        InputStream octetStream = octetStreamData.getOctetStream();
        
        RelationshipsDocument relDoc;
        try {
            relDoc = RelationshipsDocument.Factory.parse(octetStream, DEFAULT_XML_OPTIONS);
        } catch (Exception e) {
            throw new TransformException(e.getMessage(), e);
        }
        LOG.log(POILogger.DEBUG, "relationships document", relDoc);
        
        CTRelationships rels = relDoc.getRelationships();
        List<CTRelationship> relList = rels.getRelationshipList();
        Iterator<CTRelationship> relIter = rels.getRelationshipList().iterator();
        while (relIter.hasNext()) {
            CTRelationship rel = relIter.next();
            /*
             * See: ISO/IEC 29500-2:2008(E) - 13.2.4.24 Relationships Transform
             * Algorithm.
             */
            if (!this.sourceIds.contains(rel.getId())) {
                LOG.log(POILogger.DEBUG, "removing element: " + rel.getId());
                relIter.remove();
            } else {
                if (!rel.isSetTargetMode()) {
                    rel.setTargetMode(STTargetMode.INTERNAL);
                }
            }
        }
        
        // TODO: remove non element nodes ???
        LOG.log(POILogger.DEBUG, "# Relationship elements", relList.size());
        
        XmlSort.sort(rels, new Comparator<XmlCursor>(){
            public int compare(XmlCursor c1, XmlCursor c2) {
                String id1 = ((CTRelationship)c1.getObject()).getId();
                String id2 = ((CTRelationship)c2.getObject()).getId();
                return id1.compareTo(id2);
            }
        });

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            XmlOptions xo = new XmlOptions();
            xo.setSaveNoXmlDecl();
            relDoc.save(bos, xo);
            return new OctetStreamData(new ByteArrayInputStream(bos.toByteArray()));
        } catch (IOException e) {
            throw new TransformException(e.getMessage(), e);
        }
    }

    public Data transform(Data data, XMLCryptoContext context, OutputStream os) throws TransformException {
        LOG.log(POILogger.DEBUG, "transform(data,context,os)");
        return null;
    }

    public boolean isFeatureSupported(String feature) {
        LOG.log(POILogger.DEBUG, "isFeatureSupported(feature)");
        return false;
    }
}
