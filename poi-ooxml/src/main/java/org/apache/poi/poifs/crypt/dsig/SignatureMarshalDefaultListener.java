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

package org.apache.poi.poifs.crypt.dsig;

import static org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet.MS_DIGSIG_NS;
import static org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet.OO_DIGSIG_NS;
import static org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet.XML_DIGSIG_NS;
import static org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet.XML_NS;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.xml.XMLConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

/**
 * This listener class is used, to modify the to be digested xml document,
 * e.g. to register id attributes or set prefixes for registered namespaces
 */
public class SignatureMarshalDefaultListener implements SignatureMarshalListener {
    private static final String OBJECT_TAG = "Object";
    private static final Set<String> IGNORE_NS = new HashSet<>(Arrays.asList(null, XML_NS, XML_DIGSIG_NS));

    private static final List<String> DIRECT_NS = Arrays.asList(OO_DIGSIG_NS, MS_DIGSIG_NS);

    @Override
    public void handleElement(SignatureInfo signatureInfo, Document doc, EventTarget target, EventListener parentListener) {
        // see POI #63712 : because of Santuario change r1853805 in XmlSec 2.1.3,
        // we have to deal with the whole document now

        final DocumentTraversal traversal = (DocumentTraversal) doc;
        final Map<String, String> prefixCfg = signatureInfo.getSignatureConfig().getNamespacePrefixes();
        final Map<String, String> prefixUsed = new HashMap<>();

        forEachElement(doc.getElementsByTagName(OBJECT_TAG), (o) -> {
           forEachElement(o.getChildNodes(), (c) -> {
               getAllNamespaces(traversal, c, prefixCfg, prefixUsed);
               prefixUsed.forEach((ns, prefix) -> setXmlns(c, prefix, ns));
           });
        });
    }

    private static void forEachElement(NodeList nl, Consumer<Element> consumer) {
        int len = nl.getLength();
        for(int i=0; i<len; i++) {
            Node n = nl.item(i);
            if (n instanceof Element) {
                consumer.accept((Element)n);
            }
        }
    }

    private void getAllNamespaces(DocumentTraversal traversal, Element objNode, Map<String, String> prefixCfg, Map<String, String> prefixUsed) {
        prefixUsed.clear();
        final NodeIterator iter = traversal.createNodeIterator(objNode, NodeFilter.SHOW_ELEMENT, null, false);
        try {
            for (Element node; (node = (Element)iter.nextNode()) != null; ) {
                setPrefix(node, prefixCfg, prefixUsed);
                NamedNodeMap nnm = node.getAttributes();
                final int nnmLen = nnm.getLength();
                for (int j=0; j<nnmLen; j++) {
                    setPrefix(nnm.item(j), prefixCfg, prefixUsed);
                }
            }
        } finally {
            iter.detach();
        }
    }

    private void setPrefix(Node node, Map<String,String> prefixCfg, Map<String,String> prefixUsed) {
        String ns = node.getNamespaceURI();
        String prefix = prefixCfg.get(ns);
        if (IGNORE_NS.contains(ns)) {
            return;
        }
        if (prefix != null) {
            node.setPrefix(prefix);
        }
        if (DIRECT_NS.contains(ns)) {
            setXmlns(node, prefix, ns);
        } else {
            prefixUsed.put(ns, prefix);
        }
    }

    private static void setXmlns(Node node, String prefix, String ns) {
        if (node instanceof Element && !ns.equals(node.getParentNode().getNamespaceURI())) {
            ((Element)node).setAttributeNS(XML_NS, XMLConstants.XMLNS_ATTRIBUTE + (prefix == null ? "" : ":"+prefix), ns);
        }
    }
}
