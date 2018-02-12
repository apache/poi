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
 * <p>Java class for CT_RelativeRect complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_RelativeRect"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="l" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" default="0" /&gt;
 *       &lt;attribute name="t" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" default="0" /&gt;
 *       &lt;attribute name="r" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" default="0" /&gt;
 *       &lt;attribute name="b" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" default="0" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_RelativeRect")
public class CTRelativeRect {

    @XmlAttribute(name = "l")
    protected Integer l;
    @XmlAttribute(name = "t")
    protected Integer t;
    @XmlAttribute(name = "r")
    protected Integer r;
    @XmlAttribute(name = "b")
    protected Integer b;

    /**
     * Gets the value of the l property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getL() {
        if (l == null) {
            return  0;
        } else {
            return l;
        }
    }

    /**
     * Sets the value of the l property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setL(int value) {
        this.l = value;
    }

    public boolean isSetL() {
        return (this.l!= null);
    }

    public void unsetL() {
        this.l = null;
    }

    /**
     * Gets the value of the t property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getT() {
        if (t == null) {
            return  0;
        } else {
            return t;
        }
    }

    /**
     * Sets the value of the t property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setT(int value) {
        this.t = value;
    }

    public boolean isSetT() {
        return (this.t!= null);
    }

    public void unsetT() {
        this.t = null;
    }

    /**
     * Gets the value of the r property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getR() {
        if (r == null) {
            return  0;
        } else {
            return r;
        }
    }

    /**
     * Sets the value of the r property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setR(int value) {
        this.r = value;
    }

    public boolean isSetR() {
        return (this.r!= null);
    }

    public void unsetR() {
        this.r = null;
    }

    /**
     * Gets the value of the b property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getB() {
        if (b == null) {
            return  0;
        } else {
            return b;
        }
    }

    /**
     * Sets the value of the b property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setB(int value) {
        this.b = value;
    }

    public boolean isSetB() {
        return (this.b!= null);
    }

    public void unsetB() {
        this.b = null;
    }

}
