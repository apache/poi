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

package org.apache.poi.xdgf.geom;

import java.awt.geom.Dimension2D;

public class Dimension2dDouble extends Dimension2D {

    double width;
    double height;

    public Dimension2dDouble() {
        width = 0d;
        height = 0d;
    }

    public Dimension2dDouble(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Dimension2dDouble) {
            Dimension2dDouble other = (Dimension2dDouble) obj;
            return width == other.width && height == other.height;
        }

        return false;
    }

    @Override
    public int hashCode() {
        double sum = width + height;
        return (int) Math.ceil(sum * (sum + 1) / 2 + width);
    }

    @Override
    public String toString() {
        return "Dimension2dDouble[" + width + ", " + height + "]";
    }
}
