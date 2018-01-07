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

package org.apache.poi.hslf.blip;


/**
 * Represents a JPEG picture data in a PPT file
 */
public final class JPEG extends Bitmap {

    public enum ColorSpace { rgb, cymk }

    private ColorSpace colorSpace = ColorSpace.rgb;
    
    @Override
    public PictureType getType(){
        return PictureType.JPEG;
    }

    public ColorSpace getColorSpace() {
        return colorSpace;
    }
    
    public void setColorSpace(ColorSpace colorSpace) {
        this.colorSpace = colorSpace;
    }
    
    /**
     * JPEG signature is one of {@code 0x46A0, 0x46B0, 0x6E20, 0x6E30} 
     *
     * @return JPEG signature ({@code 0x46A0, 0x46B0, 0x6E20, 0x6E30})
     */
    public int getSignature(){
        return (colorSpace == ColorSpace.rgb)
            ? (getUIDInstanceCount() == 1 ? 0x46A0 :  0x46B0)
            : (getUIDInstanceCount() == 1 ? 0x6E20 :  0x6E30);
    }
    
    /**
     * Sets the PICT signature - either {@code 0x5420} or {@code 0x5430}
     */
    public void setSignature(int signature) {
        switch (signature) {
            case 0x46A0:
                setUIDInstanceCount(1);
                colorSpace = ColorSpace.rgb;
                break;
            case 0x46B0:
                setUIDInstanceCount(2);
                colorSpace = ColorSpace.rgb;
                break;
            case 0x6E20:
                setUIDInstanceCount(1);
                colorSpace = ColorSpace.cymk;
                break;
            case 0x6E30:
                setUIDInstanceCount(2);
                colorSpace = ColorSpace.cymk;
                break;
            default:
                throw new IllegalArgumentException(signature+" is not a valid instance/signature value for JPEG");
        }        
    }
}
