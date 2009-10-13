
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

/*
 * Based on the eID Applet Project code.
 * Original Copyright (C) 2008-2009 FedICT.
 */

package org.apache.poi.ooxml.signature.service.signer;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignatureFactory;

import org.w3c.dom.Document;

/**
 * JSR105 Signature Aspect interface.
 */
public interface SignatureAspect {

    /**
     * This method is being invoked by the XML signature service engine during
     * pre-sign phase. Via this method a signature aspect implementation can add
     * signature aspects to an XML signature.
     * 
     * @param signatureFactory
     * @param document
     * @param signatureId
     * @param references
     * @param objects
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     */
    void preSign(XMLSignatureFactory signatureFactory, Document document, String signatureId, List<Reference> references, List<XMLObject> objects)
                                    throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;
}
