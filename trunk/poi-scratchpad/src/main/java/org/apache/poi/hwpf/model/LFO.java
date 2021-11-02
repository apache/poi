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
package org.apache.poi.hwpf.model;

import org.apache.poi.util.Internal;

import org.apache.poi.hwpf.model.types.LFOAbstractType;

/**
 * "The LFO structure specifies the LSTF element that corresponds to a list that
 * contains a paragraph. An LFO can also specify formatting information that
 * overrides the LSTF element to which it corresponds."
 */
@Internal
public class LFO extends LFOAbstractType
{
    public LFO()
    {
    }

    public LFO( byte[] std, int offset )
    {
        fillFields( std, offset );
    }
}
