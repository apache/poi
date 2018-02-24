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
 * <p>Java class for CT_Hyperlink complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Hyperlink"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="snd" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_EmbeddedWAVAudioFile" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}id"/&gt;
 *       &lt;attribute name="invalidUrl" type="{http://www.w3.org/2001/XMLSchema}string" default="" /&gt;
 *       &lt;attribute name="action" type="{http://www.w3.org/2001/XMLSchema}string" default="" /&gt;
 *       &lt;attribute name="tgtFrame" type="{http://www.w3.org/2001/XMLSchema}string" default="" /&gt;
 *       &lt;attribute name="tooltip" type="{http://www.w3.org/2001/XMLSchema}string" default="" /&gt;
 *       &lt;attribute name="history" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *       &lt;attribute name="highlightClick" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="endSnd" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Hyperlink", propOrder = {
    "snd",
    "extLst"
})
public class CTHyperlink {

    protected CTEmbeddedWAVAudioFile snd;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "id", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String id;
    @XmlAttribute(name = "invalidUrl")
    protected String invalidUrl;
    @XmlAttribute(name = "action")
    protected String action;
    @XmlAttribute(name = "tgtFrame")
    protected String tgtFrame;
    @XmlAttribute(name = "tooltip")
    protected String tooltip;
    @XmlAttribute(name = "history")
    protected Boolean history;
    @XmlAttribute(name = "highlightClick")
    protected Boolean highlightClick;
    @XmlAttribute(name = "endSnd")
    protected Boolean endSnd;

    /**
     * Gets the value of the snd property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmbeddedWAVAudioFile }
     *     
     */
    public CTEmbeddedWAVAudioFile getSnd() {
        return snd;
    }

    /**
     * Sets the value of the snd property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmbeddedWAVAudioFile }
     *     
     */
    public void setSnd(CTEmbeddedWAVAudioFile value) {
        this.snd = value;
    }

    public boolean isSetSnd() {
        return (this.snd!= null);
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public CTOfficeArtExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public void setExtLst(CTOfficeArtExtensionList value) {
        this.extLst = value;
    }

    public boolean isSetExtLst() {
        return (this.extLst!= null);
    }

    /**
     * Drawing Object Hyperlink Target
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    public boolean isSetId() {
        return (this.id!= null);
    }

    /**
     * Gets the value of the invalidUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInvalidUrl() {
        if (invalidUrl == null) {
            return "";
        } else {
            return invalidUrl;
        }
    }

    /**
     * Sets the value of the invalidUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInvalidUrl(String value) {
        this.invalidUrl = value;
    }

    public boolean isSetInvalidUrl() {
        return (this.invalidUrl!= null);
    }

    /**
     * Gets the value of the action property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAction() {
        if (action == null) {
            return "";
        } else {
            return action;
        }
    }

    /**
     * Sets the value of the action property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAction(String value) {
        this.action = value;
    }

    public boolean isSetAction() {
        return (this.action!= null);
    }

    /**
     * Gets the value of the tgtFrame property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTgtFrame() {
        if (tgtFrame == null) {
            return "";
        } else {
            return tgtFrame;
        }
    }

    /**
     * Sets the value of the tgtFrame property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTgtFrame(String value) {
        this.tgtFrame = value;
    }

    public boolean isSetTgtFrame() {
        return (this.tgtFrame!= null);
    }

    /**
     * Gets the value of the tooltip property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTooltip() {
        if (tooltip == null) {
            return "";
        } else {
            return tooltip;
        }
    }

    /**
     * Sets the value of the tooltip property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTooltip(String value) {
        this.tooltip = value;
    }

    public boolean isSetTooltip() {
        return (this.tooltip!= null);
    }

    /**
     * Gets the value of the history property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHistory() {
        if (history == null) {
            return true;
        } else {
            return history;
        }
    }

    /**
     * Sets the value of the history property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHistory(boolean value) {
        this.history = value;
    }

    public boolean isSetHistory() {
        return (this.history!= null);
    }

    public void unsetHistory() {
        this.history = null;
    }

    /**
     * Gets the value of the highlightClick property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHighlightClick() {
        if (highlightClick == null) {
            return false;
        } else {
            return highlightClick;
        }
    }

    /**
     * Sets the value of the highlightClick property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHighlightClick(boolean value) {
        this.highlightClick = value;
    }

    public boolean isSetHighlightClick() {
        return (this.highlightClick!= null);
    }

    public void unsetHighlightClick() {
        this.highlightClick = null;
    }

    /**
     * Gets the value of the endSnd property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEndSnd() {
        if (endSnd == null) {
            return false;
        } else {
            return endSnd;
        }
    }

    /**
     * Sets the value of the endSnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEndSnd(boolean value) {
        this.endSnd = value;
    }

    public boolean isSetEndSnd() {
        return (this.endSnd!= null);
    }

    public void unsetEndSnd() {
        this.endSnd = null;
    }

}
