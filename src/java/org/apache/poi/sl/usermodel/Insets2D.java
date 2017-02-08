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

package org.apache.poi.sl.usermodel;

/**
 * This is a replacement for {@link java.awt.Insets} which works on doubles
 * instead of ints
 */
public final class Insets2D implements Cloneable {

    /**
     * The inset from the top.
     * This value is added to the Top of the rectangle
     * to yield a new location for the Top.
     */
    public double top;

    /**
     * The inset from the left.
     * This value is added to the Left of the rectangle
     * to yield a new location for the Left edge.
     */
    public double left;

    /**
     * The inset from the bottom.
     * This value is subtracted from the Bottom of the rectangle
     * to yield a new location for the Bottom.
     */
    public double bottom;

    /**
     * The inset from the right.
     * This value is subtracted from the Right of the rectangle
     * to yield a new location for the Right edge.
     */
    public double right;
    
    /**
     * Creates and initializes a new <code>Insets</code> object with the 
     * specified top, left, bottom, and right insets. 
     * @param       top   the inset from the top.
     * @param       left   the inset from the left.
     * @param       bottom   the inset from the bottom.
     * @param       right   the inset from the right.
     */
    public Insets2D(double top, double left, double bottom, double right) {
    this.top = top;
    this.left = left;
    this.bottom = bottom;
    this.right = right;
    }

    /**
     * Set top, left, bottom, and right to the specified values
     *
     * @param       top   the inset from the top.
     * @param       left   the inset from the left.
     * @param       bottom   the inset from the bottom.
     * @param       right   the inset from the right.
     * @since 1.5
     */
    public void set(double top, double left, double bottom, double right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    /**
     * Checks whether two insets objects are equal. Two instances 
     * of <code>Insets</code> are equal if the four integer values
     * of the fields <code>top</code>, <code>left</code>, 
     * <code>bottom</code>, and <code>right</code> are all equal.
     * @return      <code>true</code> if the two insets are equal;
     *                          otherwise <code>false</code>.
     * @since       JDK1.1
     */
    public boolean equals(Object obj) {
    if (obj instanceof Insets2D) {
        Insets2D insets = (Insets2D)obj;
        return ((top == insets.top) && (left == insets.left) &&
            (bottom == insets.bottom) && (right == insets.right));
    }
    return false;
    }

    /**
     * Returns the hash code for this Insets.
     *
     * @return    a hash code for this Insets.
     */
    public int hashCode() {
        double sum1 = left + bottom;
        double sum2 = right + top;
        double val1 = sum1 * (sum1 + 1)/2 + left;
        double val2 = sum2 * (sum2 + 1)/2 + top;
        double sum3 = val1 + val2;
        return (int)(sum3 * (sum3 + 1)/2 + val2);
    }

    /**
     * Returns a string representation of this <code>Insets</code> object. 
     * This method is intended to be used only for debugging purposes, and 
     * the content and format of the returned string may vary between 
     * implementations. The returned string may be empty but may not be 
     * <code>null</code>.
     * 
     * @return  a string representation of this <code>Insets</code> object.
     */
    public String toString() {
    return getClass().getName() + "[top="  + top + ",left=" + left + ",bottom=" + bottom + ",right=" + right + "]";
    }

    /**
     * Create a copy of this object.
     * @return     a copy of this <code>Insets2D</code> object.
     */
    @Override
    public Insets2D clone() {
        return new Insets2D(top, left, bottom, right);
    }
    

}
