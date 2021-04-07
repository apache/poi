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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndianByteArrayInputStream;

import static org.apache.logging.log4j.util.Unbox.box;

@Internal
public class VariantBool {
    private static final Logger LOG = LogManager.getLogger(VariantBool.class);

    static final int SIZE = 2;

    private boolean _value;

    public void read( LittleEndianByteArrayInputStream lei ) {
        short value = lei.readShort();
        switch (value) {
            case 0:
                _value = false;
                break;
            case -1:
                _value = true;
                break;
            default:
                LOG.atWarn().log("VARIANT_BOOL value '{}' is incorrect", box(value));
                _value = true;
                break;
        }
    }

    public boolean getValue() {
        return _value;
    }

    public void setValue( boolean value ) {
        this._value = value;
    }
}
