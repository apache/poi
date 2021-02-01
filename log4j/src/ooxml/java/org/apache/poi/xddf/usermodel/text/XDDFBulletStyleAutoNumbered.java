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
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextAutonumberBullet;

@Beta
public class XDDFBulletStyleAutoNumbered implements XDDFBulletStyle {
    private CTTextAutonumberBullet style;

    @Internal
    protected XDDFBulletStyleAutoNumbered(CTTextAutonumberBullet style) {
        this.style = style;
    }

    @Internal
    protected CTTextAutonumberBullet getXmlObject() {
        return style;
    }

    public AutonumberScheme getType() {
        return AutonumberScheme.valueOf(style.getType());
    }

    public void setType(AutonumberScheme scheme) {
        style.setType(scheme.underlying);
    }

    public int getStartAt() {
        if (style.isSetStartAt()) {
            return style.getStartAt();
        } else {
            return 1;
        }
    }

    public void setStartAt(Integer value) {
        if (value == null) {
            if (style.isSetStartAt()) {
                style.unsetStartAt();
            }
        } else {
            style.setStartAt(value);
        }
    }
}
