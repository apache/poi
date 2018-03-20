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

package org.apache.poi.sl.draw.binding;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Transform2D complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Transform2D"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="off" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Point2D" minOccurs="0"/&gt;
 *         &lt;element name="ext" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_PositiveSize2D" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="rot" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Angle" default="0" /&gt;
 *       &lt;attribute name="flipH" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="flipV" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Transform2D", propOrder = {
    "off",
    "ext"
})
public class CTTransform2D {

    protected CTPoint2D off;
    protected CTPositiveSize2D ext;
    @XmlAttribute(name = "rot")
    protected Integer rot;
    @XmlAttribute(name = "flipH")
    protected Boolean flipH;
    @XmlAttribute(name = "flipV")
    protected Boolean flipV;

    /**
     * Gets the value of the off property.
     * 
     * @return
     *     possible object is
     *     {@link CTPoint2D }
     *     
     */
    public CTPoint2D getOff() {
        return off;
    }

    /**
     * Sets the value of the off property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPoint2D }
     *     
     */
    public void setOff(CTPoint2D value) {
        this.off = value;
    }

    public boolean isSetOff() {
        return (this.off!= null);
    }

    /**
     * Gets the value of the ext property.
     * 
     * @return
     *     possible object is
     *     {@link CTPositiveSize2D }
     *     
     */
    public CTPositiveSize2D getExt() {
        return ext;
    }

    /**
     * Sets the value of the ext property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPositiveSize2D }
     *     
     */
    public void setExt(CTPositiveSize2D value) {
        this.ext = value;
    }

    public boolean isSetExt() {
        return (this.ext!= null);
    }

    /**
     * Gets the value of the rot property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getRot() {
        if (rot == null) {
            return  0;
        } else {
            return rot;
        }
    }

    /**
     * Sets the value of the rot property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRot(int value) {
        this.rot = value;
    }

    public boolean isSetRot() {
        return (this.rot!= null);
    }

    public void unsetRot() {
        this.rot = null;
    }

    /**
     * Gets the value of the flipH property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFlipH() {
        if (flipH == null) {
            return false;
        } else {
            return flipH;
        }
    }

    /**
     * Sets the value of the flipH property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFlipH(boolean value) {
        this.flipH = value;
    }

    public boolean isSetFlipH() {
        return (this.flipH!= null);
    }

    public void unsetFlipH() {
        this.flipH = null;
    }

    /**
     * Gets the value of the flipV property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFlipV() {
        if (flipV == null) {
            return false;
        } else {
            return flipV;
        }
    }

    /**
     * Sets the value of the flipV property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFlipV(boolean value) {
        this.flipV = value;
    }

    public boolean isSetFlipV() {
        return (this.flipV!= null);
    }

    public void unsetFlipV() {
        this.flipV = null;
    }

}
