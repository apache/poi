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

package org.apache.poi.hwmf.record;

import java.awt.Color;
import java.io.IOException;

import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

/**
 * A 32-bit ColorRef Object that defines the color value.
 * Red (1 byte):  An 8-bit unsigned integer that defines the relative intensity of red.
 * Green (1 byte):  An 8-bit unsigned integer that defines the relative intensity of green.
 * Blue (1 byte):  An 8-bit unsigned integer that defines the relative intensity of blue.
 * Reserved (1 byte):  An 8-bit unsigned integer that MUST be 0x00.
 */
public class HwmfColorRef implements Cloneable {
    private Color colorRef = Color.BLACK;
    
    public HwmfColorRef() {}
    
    public HwmfColorRef(Color colorRef) {
        this.colorRef = colorRef;
    }
    
    public int init(LittleEndianInputStream leis) throws IOException {
        int red = leis.readUByte();
        int green = leis.readUByte();
        int blue = leis.readUByte();
        /*int reserved =*/ leis.readUByte();

        colorRef = new Color(red, green, blue);
        return 4*LittleEndianConsts.BYTE_SIZE;
    }

    public Color getColor() {
        return colorRef;
    }

    /**
     * Creates a new object of the same class and with the
     * same contents as this object.
     * @return     a clone of this instance.
     * @exception  OutOfMemoryError            if there is not enough memory.
     * @see        java.lang.Cloneable
     */
    @Override
    public HwmfColorRef clone() {
        try {
            return (HwmfColorRef)super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }
}
