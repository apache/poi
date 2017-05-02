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

package org.apache.poi.hpsf;

import java.io.UnsupportedEncodingException;

import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.apache.poi.util.Removal;

/**
 * <p>Adds writing capability to the {@link Property} class.</p>
 *
 * <p>Please be aware that this class' functionality will be merged into the
 * {@link Property} class at a later time, so the API will change.</p>
 * 
 * @deprecated POI 3.16 - use Property as base class instead
 */
@Deprecated
@Removal(version="3.18")
public class MutableProperty extends Property {
    public MutableProperty() {}

    public MutableProperty(final Property p) {
    	super(p);
    }

    public MutableProperty(final long id, final long type, final Object value) {
        super(id, type, value);
    }
    
    public MutableProperty(final long id, final byte[] src, final long offset, final int length, final int codepage)
    throws UnsupportedEncodingException {
        super(id, src, offset, length, codepage);
    }
    
    public MutableProperty(final long id, LittleEndianByteArrayInputStream leis, final int length, final int codepage)
    throws UnsupportedEncodingException {
        super(id, leis, length, codepage);
    }
}
