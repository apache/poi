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

package org.apache.poi.hwpf.usermodel;

import org.apache.poi.hwpf.model.types.SHDAbstractType;

/**
 * The SHD is a substructure of the CHP, PAP, and TC for Word 2000.
 * 
 * @author vlsergey
 */
public final class ShadingDescriptor extends SHDAbstractType implements
        Cloneable
{

    public ShadingDescriptor()
    {
    }

    public ShadingDescriptor( byte[] buf, int offset )
    {
        super();
        fillFields( buf, offset );
    }

    public ShadingDescriptor clone() throws CloneNotSupportedException
    {
        return (ShadingDescriptor) super.clone();
    }

    public boolean isEmpty()
    {
        return field_3_ipat == 0;
    }

    public byte[] serialize()
    {
        byte[] result = new byte[getSize()];
        serialize( result, 0 );
        return result;
    }

    @Override
    public String toString()
    {
        if ( isEmpty() )
            return "[SHD] EMPTY";

        return "[SHD] (cvFore: " + getCvFore() + "; cvBack: " + getCvBack()
                + "; iPat: " + getIpat() + ")";
    }

}
