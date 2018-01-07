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

import org.apache.poi.hwpf.model.types.TBDAbstractType;
import org.apache.poi.hwpf.usermodel.ParagraphProperties;

/**
 * Tab descriptor. Part of {@link ParagraphProperties}.
 * 
 * @author vlsergey
 */
public class TabDescriptor extends TBDAbstractType
{

    public TabDescriptor()
    {
    }

    public TabDescriptor( byte[] bytes, int offset )
    {
        fillFields( bytes, offset );
    }

    public byte[] toByteArray()
    {
        byte[] buf = new byte[getSize()];
        serialize( buf, 0 );
        return buf;
    }

}
