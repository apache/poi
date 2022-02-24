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

import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.image.ImageHeaderPNG;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Removal;

/**
 * Represents a PNG picture data in a PPT file
 */
public final class PNG extends Bitmap {

    /**
     * @deprecated Use {@link HSLFSlideShow#addPicture(byte[], org.apache.poi.sl.usermodel.PictureData.PictureType)} or one of its overloads to create new
     *             {@link PNG}. This API led to detached {@link PNG} instances (See Bugzilla
     *             46122) and prevented adding additional functionality.
     */
    @Deprecated
    @Removal(version = "5.3")
    public PNG() {
        this(new EscherContainerRecord(), new EscherBSERecord());
    }

    /**
     * Creates a new instance.
     *
     * @param recordContainer Record tracking all pictures. Should be attached to the slideshow that this picture is
     *                        linked to.
     * @param bse Record referencing this picture. Should be attached to the slideshow that this picture is linked to.
     */
    @Internal
    public PNG(EscherContainerRecord recordContainer, EscherBSERecord bse) {
        super(recordContainer, bse);
    }

    @Override
    public byte[] getData() {
        return new ImageHeaderPNG(super.getData()).extractPNG();
    }

    @Override
    public PictureType getType(){
        return PictureType.PNG;
    }

    /**
     * PNG signature is {@code 0x6E00} or {@code 0x6E10}
     *
     * @return PNG signature ({@code 0x6E00} or {@code 0x6E10})
     */
    public int getSignature(){
        return (getUIDInstanceCount() == 1 ? 0x6E00 : 0x6E10);
    }

    /**
     * Sets the PNG signature - either {@code 0x6E00} or {@code 0x6E10}
     */
    public void setSignature(int signature) {
        switch (signature) {
            case 0x6E00:
                setUIDInstanceCount(1);
                break;
            case 0x6E10:
                setUIDInstanceCount(2);
                break;
            default:
                throw new IllegalArgumentException(signature+" is not a valid instance/signature value for PNG");
        }
    }
}
