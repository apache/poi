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

package org.apache.poi.xddf.usermodel;

import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLinearShadeProperties;

@Beta
public class XDDFLinearShadeProperties {
    private CTLinearShadeProperties props;

    protected XDDFLinearShadeProperties(CTLinearShadeProperties properties) {
        this.props = properties;
    }

    @Internal
    protected CTLinearShadeProperties getXmlObject() {
        return props;
    }

    public Double getAngle() {
        if (props.isSetAng()) {
            return Angles.attributeToDegrees(props.getAng());
        } else {
            return null;
        }
    }

    public void setAngle(Double angle) {
        if (angle == null) {
            if (props.isSetAng()) {
                props.unsetAng();
            }
        } else {
            if (angle < 0.0 || 360.0 <= angle) {
                throw new IllegalArgumentException("angle must be in the range [0, 360).");
            }
            props.setAng(Angles.degreesToAttribute(angle));
        }
    }

    public Boolean isScaled() {
        if (props.isSetScaled()) {
            return props.getScaled();
        } else {
             return false;
        }
    }

    public void setScaled(Boolean scaled) {
        if (scaled == null) {
            if (props.isSetScaled()) {
                props.unsetScaled();
            }
        } else {
            props.setScaled(scaled);
        }
    }
}
