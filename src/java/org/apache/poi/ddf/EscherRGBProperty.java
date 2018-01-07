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

import org.apache.poi.util.HexDump;

/**
 * A color property.
 */
public class EscherRGBProperty
        extends EscherSimpleProperty
{

    public EscherRGBProperty( short propertyNumber, int rgbColor )
    {
        super( propertyNumber, rgbColor );
    }

    /**
     * @return the rgb color as int value
     */
    public int getRgbColor()
    {
        return getPropertyValue();
    }

    /**
     * @return the red part
     */
    public byte getRed()
    {
        return (byte) ( getRgbColor() & 0xFF );
    }

    /**
     * @return the green part
     */
    public byte getGreen()
    {
        return (byte) ( (getRgbColor() >> 8) & 0xFF );
    }

    /**
     * @return the blue part
     */
    public byte getBlue()
    {
        return (byte) ( (getRgbColor() >> 16) & 0xFF );
    }

    @Override
    public String toXml(String tab){
        StringBuilder builder = new StringBuilder();
        builder.append(tab).append("<").append(getClass().getSimpleName()).append(" id=\"0x").append(HexDump.toHex(getId()))
                .append("\" name=\"").append(getName()).append("\" blipId=\"")
                .append(isBlipId()).append("\" value=\"0x").append(HexDump.toHex(getRgbColor())).append("\"/>");
        return builder.toString();
    }
}
