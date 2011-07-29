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
package org.apache.poi.ddf;

/**
 * The opt record is used to store property values for a shape. It is the key to
 * determining the attributes of a shape. Properties can be of two types: simple
 * or complex. Simple types are fixed length. Complex properties are variable
 * length.
 * 
 * @author Glen Stampoultzis
 */
public class EscherOptRecord extends AbstractEscherOptRecord
{
    public static final short RECORD_ID = (short) 0xF00B;
    public static final String RECORD_DESCRIPTION = "msofbtOPT";

    /**
     * Automatically recalculate the correct option
     */
    public short getOptions()
    {
        setOptions( (short) ( ( properties.size() << 4 ) | 0x3 ) );
        return super.getOptions();
    }

    public String getRecordName()
    {
        return "Opt";
    }
}
