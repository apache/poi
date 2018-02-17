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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Scale2D complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Scale2D"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="sx" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Ratio"/&gt;
 *         &lt;element name="sy" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Ratio"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Scale2D", propOrder = {
    "sx",
    "sy"
})
public class CTScale2D {

    @XmlElement(required = true)
    protected CTRatio sx;
    @XmlElement(required = true)
    protected CTRatio sy;

    /**
     * Gets the value of the sx property.
     * 
     * @return
     *     possible object is
     *     {@link CTRatio }
     *     
     */
    public CTRatio getSx() {
        return sx;
    }

    /**
     * Sets the value of the sx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRatio }
     *     
     */
    public void setSx(CTRatio value) {
        this.sx = value;
    }

    public boolean isSetSx() {
        return (this.sx!= null);
    }

    /**
     * Gets the value of the sy property.
     * 
     * @return
     *     possible object is
     *     {@link CTRatio }
     *     
     */
    public CTRatio getSy() {
        return sy;
    }

    /**
     * Sets the value of the sy property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRatio }
     *     
     */
    public void setSy(CTRatio value) {
        this.sy = value;
    }

    public boolean isSetSy() {
        return (this.sy!= null);
    }

}
