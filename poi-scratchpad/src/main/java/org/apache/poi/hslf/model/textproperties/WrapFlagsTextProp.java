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

package org.apache.poi.hslf.model.textproperties;

public class WrapFlagsTextProp extends BitMaskTextProp {
    public static final int CHAR_WRAP_IDX = 0;
    public static final int WORD_WRAO_IDX = 1;
    public static final int OVERFLOW_IDX = 2;

    public static final String NAME = "wrapFlags";

    public WrapFlagsTextProp() {
        super(2, 0xE0000, NAME, "charWrap", "wordWrap", "overflow");
    }

    public WrapFlagsTextProp(WrapFlagsTextProp other) {
        super(other);
    }

    @Override
    public WrapFlagsTextProp copy() {
        return new WrapFlagsTextProp(this);
    }
}
