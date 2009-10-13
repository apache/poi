
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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Input Stream proxy that doesn't close the underlying input stream.
 */
public class NoCloseInputStream extends ProxyInputStream {

    private static final Log LOG = LogFactory.getLog(NoCloseInputStream.class);

    /**
     * Main constructor.
     * 
     * @param proxy
     */
    public NoCloseInputStream(InputStream proxy) {
        super(proxy);
    }

    @Override
    public void close() throws IOException {
        LOG.debug("close");
    }
}