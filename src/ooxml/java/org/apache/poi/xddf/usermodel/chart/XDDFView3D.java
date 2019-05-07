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

import org.openxmlformats.schemas.drawingml.x2006.chart.CTView3D;

/**
 * @since 4.1
 */

public class XDDFView3D {
    private final CTView3D view3D;

    public XDDFView3D(CTView3D view3D) {
        this.view3D = view3D;
    }
    
    public int getXRotationAngle() {
        return view3D.getRotX().getVal();
    }
    
   public void setXRotationAngle(int val) {
      if (view3D.isSetRotY()) {
         view3D.getRotY().setVal(val);
      } else {
         view3D.addNewRotY().setVal(val);
      }
   }
    
    public int getYRotationAngle() {
        return view3D.getRotY().getVal();
    }
    
   public void setYRotationAngle(int val) {
      if (view3D.isSetRotY()) {
         view3D.getRotY().setVal(val);
      } else {
         view3D.addNewRotY().setVal(val);
      }
   }
    
    public boolean getRightAngleAxes() {
        return view3D.getRAngAx().getVal();
    }
    
   public void setRightAngleAxes(boolean val) {
      if (view3D.isSetRAngAx()) {
         view3D.getRAngAx().setVal(val);
      } else {
         view3D.addNewRAngAx().setVal(val);
      }
   }
    
    public short getPerspectiveAngle() {
        return view3D.getPerspective().getVal();
    }
    
   public void setPerspectiveAngle(short val) {
      if (view3D.isSetPerspective()) {
         view3D.getPerspective().setVal(val);
      } else {
         view3D.addNewPerspective().setVal(val);
      }
   }
    
    public int getDepthPercentVal() {
        return view3D.getDepthPercent().getVal();
    }
    
   public void setDepthPercent(int val) {
      if (view3D.isSetDepthPercent()) {
         view3D.getDepthPercent().setVal(val);
      } else {
         view3D.addNewDepthPercent().setVal(val);
      }
   }
    
    public int getHeightPercent() {
        return view3D.getHPercent().getVal();
    }
    
   public void setHeightPercent(int val) {
      if (view3D.isSetHPercent()) {
         view3D.getHPercent().setVal(val);
      } else {
         view3D.addNewHPercent().setVal(val);
      }
   }
}
