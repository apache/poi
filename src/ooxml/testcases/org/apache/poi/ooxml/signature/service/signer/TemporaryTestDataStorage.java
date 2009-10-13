
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ooxml.signature.service.signer.TemporaryDataStorage;



class TemporaryTestDataStorage implements TemporaryDataStorage {

    private ByteArrayOutputStream outputStream;

    private Map<String, Serializable> attributes;

    public TemporaryTestDataStorage() {
        this.outputStream = new ByteArrayOutputStream();
        this.attributes = new HashMap<String, Serializable>();
    }

    public InputStream getTempInputStream() {
        byte[] data = this.outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        return inputStream;
    }

    public OutputStream getTempOutputStream() {
        return this.outputStream;
    }

    public Serializable getAttribute(String attributeName) {
        return this.attributes.get(attributeName);
    }

    public void setAttribute(String attributeName, Serializable attributeValue) {
        this.attributes.put(attributeName, attributeValue);
    }
}