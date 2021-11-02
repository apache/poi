/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xddf.usermodel.chart;

import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTView3D;

public class XDDFView3D {
    private final CTView3D view3D;

    @Internal
    protected XDDFView3D(CTView3D view3D) {
        this.view3D = view3D;
    }

    public Byte getXRotationAngle() {
        if (view3D.isSetRotX()) {
            return view3D.getRotX().getVal();
        } else {
            return null;
        }
    }

    public void setXRotationAngle(Byte rotation) {
        if (rotation == null) {
            if (view3D.isSetRotX()) {
                view3D.unsetRotX();
            }
        } else {
            if (rotation < -90 || 90 < rotation) {
                throw new IllegalArgumentException("rotation must be between -90 and 90");
            }
            if (view3D.isSetRotX()) {
                view3D.getRotX().setVal(rotation);
            } else {
                view3D.addNewRotX().setVal(rotation);
            }
        }
    }

    public Integer getYRotationAngle() {
        if (view3D.isSetRotY()) {
            return view3D.getRotY().getVal();
        } else {
            return null;
        }
    }

    public void setYRotationAngle(Integer rotation) {
        if (rotation == null) {
            if (view3D.isSetRotY()) {
                view3D.unsetRotY();
            }
        } else {
            if (rotation < 0 || 360 < rotation) {
                throw new IllegalArgumentException("rotation must be between 0 and 360");
            }
            if (view3D.isSetRotY()) {
                view3D.getRotY().setVal(rotation);
            } else {
                view3D.addNewRotY().setVal(rotation);
            }
        }
    }

    public Boolean hasRightAngleAxes() {
        if (view3D.isSetRAngAx()) {
            return view3D.getRAngAx().getVal();
        } else {
            return null;
        }
    }

    public void setRightAngleAxes(Boolean rightAngles) {
        if (rightAngles == null) {
            if (view3D.isSetRAngAx()) {
                view3D.unsetRAngAx();
            }
        } else {
            if (view3D.isSetRAngAx()) {
                view3D.getRAngAx().setVal(rightAngles);
            } else {
                view3D.addNewRAngAx().setVal(rightAngles);
            }
        }
    }

    public Short getPerspectiveAngle() {
        if (view3D.isSetPerspective()) {
            return view3D.getPerspective().getVal();
        } else {
            return null;
        }
    }

    public void setPerspectiveAngle(Short perspective) {
        if (perspective == null) {
            if (view3D.isSetPerspective()) {
                view3D.unsetPerspective();
            }
        } else {
            if (perspective < 0 || 240 < perspective) {
                throw new IllegalArgumentException("perspective must be between 0 and 240");
            }
            if (view3D.isSetPerspective()) {
                view3D.getPerspective().setVal(perspective);
            } else {
                view3D.addNewPerspective().setVal(perspective);
            }
        }
    }

    public Integer getDepthPercent() {
        return (view3D.isSetDepthPercent()) ? POIXMLUnits.parsePercent(view3D.getDepthPercent().xgetVal()) : null;
    }

    public void setDepthPercent(Integer percent) {
        if (percent == null) {
            if (view3D.isSetDepthPercent()) {
                view3D.unsetDepthPercent();
            }
        } else {
            if (percent < 20 || 2000 < percent) {
                throw new IllegalArgumentException("percent must be between 20 and 2000");
            }
            if (view3D.isSetDepthPercent()) {
                view3D.getDepthPercent().setVal(percent);
            } else {
                view3D.addNewDepthPercent().setVal(percent);
            }
        }
    }

    public Integer getHPercent() {
        return (view3D.isSetHPercent()) ? POIXMLUnits.parsePercent(view3D.getHPercent().xgetVal()) : null;
    }

    public void setHPercent(Integer percent) {
        if (percent == null) {
            if (view3D.isSetHPercent()) {
                view3D.unsetHPercent();
            }
        } else {
            if (percent < 5 || 500 < percent) {
                throw new IllegalArgumentException("percent must be between 5 and 500");
            }
            if (view3D.isSetHPercent()) {
                view3D.getHPercent().setVal(percent);
            } else {
                view3D.addNewHPercent().setVal(percent);
            }
        }
    }

}
