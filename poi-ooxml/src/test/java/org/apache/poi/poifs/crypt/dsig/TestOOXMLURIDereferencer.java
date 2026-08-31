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

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class TestOOXMLURIDereferencer {

    private static URIReference uriRef(final String uri) {
        return new URIReference() {
            @Override
            public String getURI() {
                return uri;
            }

            @Override
            public String getType() {
                return null;
            }
        };
    }

    /**
     * A signature reference is expected to resolve to a part inside the package (or be a
     * same-document reference). A reference whose URI carries a scheme points outside the package
     * and must not be resolved.
     */
    @Test
    void absoluteUriIsNotResolved() throws Exception {
        try (OPCPackage pkg = OPCPackage.create(UnsynchronizedByteArrayOutputStream.builder().get())) {
            SignatureInfo si = new SignatureInfo();
            si.setOpcPackage(pkg);
            si.setSignatureFactory(XMLSignatureFactory.getInstance("DOM"));

            OOXMLURIDereferencer der = new OOXMLURIDereferencer();
            der.setSignatureInfo(si);

            Document doc = DocumentHelper.createDocument();
            Element el = doc.createElement("r");
            XMLCryptoContext ctx = new DOMValidateContext(new SecretKeySpec(new byte[16], "AES"), el);

            for (String uri : new String[]{
                    "http://poi.apache.org/",
                    "https://poi.apache.org/x",
                    "file:///etc/hostname",
                    "ftp://example.invalid/x"}) {
                URIReference ref = uriRef(uri);
                assertThrows(URIReferenceException.class, () -> der.dereference(ref, ctx),
                        "reference outside the package should not be resolved: " + uri);
            }

            pkg.revert();
        }
    }
}
