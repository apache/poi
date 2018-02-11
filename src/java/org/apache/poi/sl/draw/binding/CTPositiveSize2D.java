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
 * <p>Java class for CT_PositiveSize2D complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PositiveSize2D"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="cx" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" /&gt;
 *       &lt;attribute name="cy" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PositiveSize2D")
public class CTPositiveSize2D {

    @XmlAttribute(name = "cx", required = true)
    protected long cx;
    @XmlAttribute(name = "cy", required = true)
    protected long cy;

    /**
     * Gets the value of the cx property.
     * 
     */
    public long getCx() {
        return cx;
    }

    /**
     * Sets the value of the cx property.
     * 
     */
    public void setCx(long value) {
        this.cx = value;
    }

    public boolean isSetCx() {
        return true;
    }

    /**
     * Gets the value of the cy property.
     * 
     */
    public long getCy() {
        return cy;
    }

    /**
     * Sets the value of the cy property.
     * 
     */
    public void setCy(long value) {
        this.cy = value;
    }

    public boolean isSetCy() {
        return true;
    }

}
