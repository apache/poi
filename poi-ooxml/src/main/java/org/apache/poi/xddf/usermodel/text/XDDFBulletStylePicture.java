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

package org.apache.poi.xddf.usermodel.text;

import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xddf.usermodel.XDDFPicture;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBlipBullet;

@Beta
public class XDDFBulletStylePicture implements XDDFBulletStyle {
    private CTTextBlipBullet style;

    @Internal
    protected XDDFBulletStylePicture(CTTextBlipBullet style) {
        this.style = style;
    }

    @Internal
    protected CTTextBlipBullet getXmlObject() {
        return style;
    }

    public XDDFPicture getPicture() {
        return new XDDFPicture(style.getBlip());
    }

    public void setPicture(XDDFPicture picture) {
        if (picture != null) {
            style.setBlip(picture.getXmlObject());
        }
    }
}
