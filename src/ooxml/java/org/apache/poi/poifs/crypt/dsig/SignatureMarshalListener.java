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

import static org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet.OO_DIGSIG_NS;
import static org.apache.poi.poifs.crypt.dsig.facets.SignatureFacet.XML_NS;

import org.apache.poi.poifs.crypt.dsig.SignatureConfig.SignatureConfigurable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;

/**
 * This listener class is used, to modify the to be digested xml document,
 * e.g. to register id attributes or set prefixes for registered namespaces
 */
public class SignatureMarshalListener implements EventListener, SignatureConfigurable {
    ThreadLocal<EventTarget> target = new ThreadLocal<>();
    SignatureConfig signatureConfig;
    public void setEventTarget(EventTarget target) {
        this.target.set(target);
    }
    
    @Override
    public void handleEvent(Event e) {
        if (!(e instanceof MutationEvent)) {
            return;
        }
        MutationEvent mutEvt = (MutationEvent)e;
        EventTarget et = mutEvt.getTarget();
        if (!(et instanceof Element)) {
            return;
        }
        handleElement((Element)et);
    }

    public void handleElement(Element el) {
        EventTarget target = this.target.get();

        if (el.hasAttribute("Id")) {
            el.setIdAttribute("Id", true);
        }

        setListener(target, this, false);
        if (OO_DIGSIG_NS.equals(el.getNamespaceURI())) {
            String parentNS = el.getParentNode().getNamespaceURI();
            if (!OO_DIGSIG_NS.equals(parentNS) && !el.hasAttributeNS(XML_NS, "mdssi")) {
                el.setAttributeNS(XML_NS, "xmlns:mdssi", OO_DIGSIG_NS);
            }
        }
        setPrefix(el);
        setListener(target, this, true);
    }

    // helper method to keep it in one place
    public static void setListener(EventTarget target, EventListener listener, boolean enabled) {
        String type = "DOMSubtreeModified";
        boolean useCapture = false;
        if (enabled) {
            target.addEventListener(type, listener, useCapture);
        } else {
            target.removeEventListener(type, listener, useCapture);
        }
    }
    
    protected void setPrefix(Node el) {
        String prefix = signatureConfig.getNamespacePrefixes().get(el.getNamespaceURI());
        if (prefix != null && el.getPrefix() == null) {
            el.setPrefix(prefix);
        }
        
        NodeList nl = el.getChildNodes();
        for (int i=0; i<nl.getLength(); i++) {
            setPrefix(nl.item(i));
        }
    }
    
    @Override
    public void setSignatureConfig(SignatureConfig signatureConfig) {
        this.signatureConfig = signatureConfig;
    }
}