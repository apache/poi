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

package org.apache.poi.xslf.usermodel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlipFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTCustomGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTEffectContainer;
import org.openxmlformats.schemas.drawingml.x2006.main.CTEffectList;
import org.openxmlformats.schemas.drawingml.x2006.main.CTFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGroupFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNoFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPatternFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTStyleMatrixReference;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableCellProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.presentationml.x2006.main.CTBackgroundProperties;

/**
 * Internal helper class to unify property access.
 * 
 * This class is experimental and not (yet) supposed for public usage.
 * Maybe the xml schemas might be enhanced with interfaces to make this class superfluous
 * 
 * @since POI 3.15-beta2
 */
@Internal
/* package */ class XSLFPropertiesDelegate {
    private static final Logger LOG = LogManager.getLogger(XSLFPropertiesDelegate.class);


    public static XSLFFillProperties getFillDelegate(XmlObject props) {
        return getDelegate(XSLFFillProperties.class, props);
    }

    public static XSLFGeometryProperties getGeometryDelegate(XmlObject props) {
        return getDelegate(XSLFGeometryProperties.class, props);
    }
    
    public static XSLFEffectProperties getEffectDelegate(XmlObject props) {
        return getDelegate(XSLFEffectProperties.class, props);
    }
    
    public interface XSLFFillProperties {
        /**
         * Gets the "noFill" element
         */
        CTNoFillProperties getNoFill();
        
        /**
         * True if has "noFill" element
         */
        boolean isSetNoFill();
        
        /**
         * Sets the "noFill" element
         */
        void setNoFill(CTNoFillProperties noFill);
        
        /**
         * Appends and returns a new empty "noFill" element
         */
        CTNoFillProperties addNewNoFill();
        
        /**
         * Unsets the "noFill" element
         */
        void unsetNoFill();
        
        /**
         * Gets the "solidFill" element
         */
        CTSolidColorFillProperties getSolidFill();
        
        /**
         * True if has "solidFill" element
         */
        boolean isSetSolidFill();
        
        /**
         * Sets the "solidFill" element
         */
        void setSolidFill(CTSolidColorFillProperties solidFill);
        
        /**
         * Appends and returns a new empty "solidFill" element
         */
        CTSolidColorFillProperties addNewSolidFill();
        
        /**
         * Unsets the "solidFill" element
         */
        void unsetSolidFill();
        
        /**
         * Gets the "gradFill" element
         */
        CTGradientFillProperties getGradFill();
        
        /**
         * True if has "gradFill" element
         */
        boolean isSetGradFill();
        
        /**
         * Sets the "gradFill" element
         */
        void setGradFill(CTGradientFillProperties gradFill);
        
        /**
         * Appends and returns a new empty "gradFill" element
         */
        CTGradientFillProperties addNewGradFill();
        
        /**
         * Unsets the "gradFill" element
         */
        void unsetGradFill();
        
        /**
         * Gets the "blipFill" element
         */
        CTBlipFillProperties getBlipFill();
        
        /**
         * True if has "blipFill" element
         */
        boolean isSetBlipFill();
        
        /**
         * Sets the "blipFill" element
         */
        void setBlipFill(CTBlipFillProperties blipFill);
        
        /**
         * Appends and returns a new empty "blipFill" element
         */
        CTBlipFillProperties addNewBlipFill();
        
        /**
         * Unsets the "blipFill" element
         */
        void unsetBlipFill();
        
        /**
         * Gets the "pattFill" element
         */
        CTPatternFillProperties getPattFill();
        
        /**
         * True if has "pattFill" element
         */
        boolean isSetPattFill();
        
        /**
         * Sets the "pattFill" element
         */
        void setPattFill(CTPatternFillProperties pattFill);
        
        /**
         * Appends and returns a new empty "pattFill" element
         */
        CTPatternFillProperties addNewPattFill();
        
        /**
         * Unsets the "pattFill" element
         */
        void unsetPattFill();
        
        /**
         * Gets the "grpFill" element
         */
        CTGroupFillProperties getGrpFill();
        
        /**
         * True if has "grpFill" element
         */
        boolean isSetGrpFill();
        
        /**
         * Sets the "grpFill" element
         */
        void setGrpFill(CTGroupFillProperties grpFill);
        
        /**
         * Appends and returns a new empty "grpFill" element
         */
        CTGroupFillProperties addNewGrpFill();
        
        /**
         * Unsets the "grpFill" element
         */
        void unsetGrpFill();
        
        /**
         * Helper method to unify other properties with style matrix references
         * @return true, if this is a matrix style delegate
         */
        boolean isSetMatrixStyle();
        
        /**
         * Helper method to unify other properties with style matrix references
         */
        CTStyleMatrixReference getMatrixStyle();
        
        /**
         * Helper method to choose between fill and line style
         *
         * @return true, if this applies to a line
         */
        boolean isLineStyle();
    }

    public interface XSLFGeometryProperties {
        /**
         * Gets the "custGeom" element
         */
        CTCustomGeometry2D getCustGeom();
        
        /**
         * True if has "custGeom" element
         */
        boolean isSetCustGeom();
        
        /**
         * Sets the "custGeom" element
         */
        void setCustGeom(CTCustomGeometry2D custGeom);
        
        /**
         * Appends and returns a new empty "custGeom" element
         */
        CTCustomGeometry2D addNewCustGeom();
        
        /**
         * Unsets the "custGeom" element
         */
        void unsetCustGeom();
        
        /**
         * Gets the "prstGeom" element
         */
        CTPresetGeometry2D getPrstGeom();
        
        /**
         * True if has "prstGeom" element
         */
        boolean isSetPrstGeom();
        
        /**
         * Sets the "prstGeom" element
         */
        void setPrstGeom(CTPresetGeometry2D prstGeom);
        
        /**
         * Appends and returns a new empty "prstGeom" element
         */
        CTPresetGeometry2D addNewPrstGeom();
        
        /**
         * Unsets the "prstGeom" element
         */
        void unsetPrstGeom();
    }

    public interface XSLFEffectProperties {
        /**
         * Gets the "effectLst" element
         */
        CTEffectList getEffectLst();
        
        /**
         * True if has "effectLst" element
         */
        boolean isSetEffectLst();
        
        /**
         * Sets the "effectLst" element
         */
        void setEffectLst(CTEffectList effectLst);
        
        /**
         * Appends and returns a new empty "effectLst" element
         */
        CTEffectList addNewEffectLst();
        
        /**
         * Unsets the "effectLst" element
         */
        void unsetEffectLst();
        
        /**
         * Gets the "effectDag" element
         */
        CTEffectContainer getEffectDag();
        
        /**
         * True if has "effectDag" element
         */
        boolean isSetEffectDag();
        
        /**
         * Sets the "effectDag" element
         */
        void setEffectDag(CTEffectContainer effectDag);
        
        /**
         * Appends and returns a new empty "effectDag" element
         */
        CTEffectContainer addNewEffectDag();
        
        /**
         * Unsets the "effectDag" element
         */
        void unsetEffectDag();
    }
    
    private static class ShapeDelegate implements XSLFFillProperties, XSLFGeometryProperties, XSLFEffectProperties {
        final CTShapeProperties props;
        
        ShapeDelegate(CTShapeProperties props) {
            this.props = props;
        }

        @Override
        public CTNoFillProperties getNoFill() {
            return props.getNoFill();
        }

        @Override
        public boolean isSetNoFill() {
            return props.isSetNoFill();
        }

        @Override
        public void setNoFill(CTNoFillProperties noFill) {
            props.setNoFill(noFill);
        }

        @Override
        public CTNoFillProperties addNewNoFill() {
            return props.addNewNoFill();
        }

        @Override
        public void unsetNoFill() {
            props.unsetNoFill();
        }

        @Override
        public CTSolidColorFillProperties getSolidFill() {
            return props.getSolidFill();
        }

        @Override
        public boolean isSetSolidFill() {
            return props.isSetSolidFill();
        }

        @Override
        public void setSolidFill(CTSolidColorFillProperties solidFill) {
            props.setSolidFill(solidFill);
        }

        @Override
        public CTSolidColorFillProperties addNewSolidFill() {
            return props.addNewSolidFill();
        }

        @Override
        public void unsetSolidFill() {
            props.unsetSolidFill();
        }

        @Override
        public CTGradientFillProperties getGradFill() {
            return props.getGradFill();
        }

        @Override
        public boolean isSetGradFill() {
            return props.isSetGradFill();
        }

        @Override
        public void setGradFill(CTGradientFillProperties gradFill) {
            props.setGradFill(gradFill);
        }

        @Override
        public CTGradientFillProperties addNewGradFill() {
            return props.addNewGradFill();
        }

        @Override
        public void unsetGradFill() {
            props.unsetGradFill();
        }

        @Override
        public CTBlipFillProperties getBlipFill() {
            return props.getBlipFill();
        }

        @Override
        public boolean isSetBlipFill() {
            return props.isSetBlipFill();
        }

        @Override
        public void setBlipFill(CTBlipFillProperties blipFill) {
            props.setBlipFill(blipFill);
        }

        @Override
        public CTBlipFillProperties addNewBlipFill() {
            return props.addNewBlipFill();
        }

        @Override
        public void unsetBlipFill() {
            props.unsetBlipFill();
        }

        @Override
        public CTPatternFillProperties getPattFill() {
            return props.getPattFill();
        }

        @Override
        public boolean isSetPattFill() {
            return props.isSetPattFill();
        }

        @Override
        public void setPattFill(CTPatternFillProperties pattFill) {
            props.setPattFill(pattFill);
        }

        @Override
        public CTPatternFillProperties addNewPattFill() {
            return props.addNewPattFill();
        }

        @Override
        public void unsetPattFill() {
            props.unsetPattFill();
        }

        @Override
        public CTGroupFillProperties getGrpFill() {
            return props.getGrpFill();
        }

        @Override
        public boolean isSetGrpFill() {
            return props.isSetGrpFill();
        }

        @Override
        public void setGrpFill(CTGroupFillProperties grpFill) {
            props.setGrpFill(grpFill);
        }

        @Override
        public CTGroupFillProperties addNewGrpFill() {
            return props.addNewGrpFill();
        }

        @Override
        public void unsetGrpFill() {
            props.unsetGrpFill();
        }

        @Override
        public CTCustomGeometry2D getCustGeom() {
            return props.getCustGeom();
        }

        @Override
        public boolean isSetCustGeom() {
            return props.isSetCustGeom();
        }

        @Override
        public void setCustGeom(CTCustomGeometry2D custGeom) {
            props.setCustGeom(custGeom);
        }

        @Override
        public CTCustomGeometry2D addNewCustGeom() {
            return props.addNewCustGeom();
        }

        @Override
        public void unsetCustGeom() {
            props.unsetCustGeom();
        }

        @Override
        public CTPresetGeometry2D getPrstGeom() {
            return props.getPrstGeom();
        }

        @Override
        public boolean isSetPrstGeom() {
            return props.isSetPrstGeom();
        }

        @Override
        public void setPrstGeom(CTPresetGeometry2D prstGeom) {
            props.setPrstGeom(prstGeom);
        }

        @Override
        public CTPresetGeometry2D addNewPrstGeom() {
            return props.addNewPrstGeom();
        }

        @Override
        public void unsetPrstGeom() {
            props.unsetPrstGeom();
        }

        @Override
        public CTEffectList getEffectLst() {
            return props.getEffectLst();
        }

        @Override
        public boolean isSetEffectLst() {
            return props.isSetEffectLst();
        }

        @Override
        public void setEffectLst(CTEffectList effectLst) {
            props.setEffectLst(effectLst);
        }

        @Override
        public CTEffectList addNewEffectLst() {
            return props.addNewEffectLst();
        }

        @Override
        public void unsetEffectLst() {
            props.unsetEffectLst();
        }

        @Override
        public CTEffectContainer getEffectDag() {
            return props.getEffectDag();
        }

        @Override
        public boolean isSetEffectDag() {
            return props.isSetEffectDag();
        }

        @Override
        public void setEffectDag(CTEffectContainer effectDag) {
            props.setEffectDag(effectDag);
        }

        @Override
        public CTEffectContainer addNewEffectDag() {
            return props.addNewEffectDag();
        }

        @Override
        public void unsetEffectDag() {
            props.unsetEffectDag();
        }

        @Override
        public boolean isSetMatrixStyle() {
            return false;
        }

        @Override
        public CTStyleMatrixReference getMatrixStyle() {
            return null;
        }
        
        @Override
        public boolean isLineStyle() {
            return false;
        }
    }

    private static class BackgroundDelegate implements XSLFFillProperties, XSLFEffectProperties {
        final CTBackgroundProperties props;
        
        BackgroundDelegate(CTBackgroundProperties props) {
            this.props = props;
        }

        @Override
        public CTNoFillProperties getNoFill() {
            return props.getNoFill();
        }

        @Override
        public boolean isSetNoFill() {
            return props.isSetNoFill();
        }

        @Override
        public void setNoFill(CTNoFillProperties noFill) {
            props.setNoFill(noFill);
        }

        @Override
        public CTNoFillProperties addNewNoFill() {
            return props.addNewNoFill();
        }

        @Override
        public void unsetNoFill() {
            props.unsetNoFill();
        }

        @Override
        public CTSolidColorFillProperties getSolidFill() {
            return props.getSolidFill();
        }

        @Override
        public boolean isSetSolidFill() {
            return props.isSetSolidFill();
        }

        @Override
        public void setSolidFill(CTSolidColorFillProperties solidFill) {
            props.setSolidFill(solidFill);
        }

        @Override
        public CTSolidColorFillProperties addNewSolidFill() {
            return props.addNewSolidFill();
        }

        @Override
        public void unsetSolidFill() {
            props.unsetSolidFill();
        }

        @Override
        public CTGradientFillProperties getGradFill() {
            return props.getGradFill();
        }

        @Override
        public boolean isSetGradFill() {
            return props.isSetGradFill();
        }

        @Override
        public void setGradFill(CTGradientFillProperties gradFill) {
            props.setGradFill(gradFill);
        }

        @Override
        public CTGradientFillProperties addNewGradFill() {
            return props.addNewGradFill();
        }

        @Override
        public void unsetGradFill() {
            props.unsetGradFill();
        }

        @Override
        public CTBlipFillProperties getBlipFill() {
            return props.getBlipFill();
        }

        @Override
        public boolean isSetBlipFill() {
            return props.isSetBlipFill();
        }

        @Override
        public void setBlipFill(CTBlipFillProperties blipFill) {
            props.setBlipFill(blipFill);
        }

        @Override
        public CTBlipFillProperties addNewBlipFill() {
            return props.addNewBlipFill();
        }

        @Override
        public void unsetBlipFill() {
            props.unsetBlipFill();
        }

        @Override
        public CTPatternFillProperties getPattFill() {
            return props.getPattFill();
        }

        @Override
        public boolean isSetPattFill() {
            return props.isSetPattFill();
        }

        @Override
        public void setPattFill(CTPatternFillProperties pattFill) {
            props.setPattFill(pattFill);
        }

        @Override
        public CTPatternFillProperties addNewPattFill() {
            return props.addNewPattFill();
        }

        @Override
        public void unsetPattFill() {
            props.unsetPattFill();
        }

        @Override
        public CTGroupFillProperties getGrpFill() {
            return props.getGrpFill();
        }

        @Override
        public boolean isSetGrpFill() {
            return props.isSetGrpFill();
        }

        @Override
        public void setGrpFill(CTGroupFillProperties grpFill) {
            props.setGrpFill(grpFill);
        }

        @Override
        public CTGroupFillProperties addNewGrpFill() {
            return props.addNewGrpFill();
        }

        @Override
        public void unsetGrpFill() {
            props.unsetGrpFill();
        }

        @Override
        public CTEffectList getEffectLst() {
            return props.getEffectLst();
        }

        @Override
        public boolean isSetEffectLst() {
            return props.isSetEffectLst();
        }

        @Override
        public void setEffectLst(CTEffectList effectLst) {
            props.setEffectLst(effectLst);
        }

        @Override
        public CTEffectList addNewEffectLst() {
            return props.addNewEffectLst();
        }

        @Override
        public void unsetEffectLst() {
            props.unsetEffectLst();
        }

        @Override
        public CTEffectContainer getEffectDag() {
            return props.getEffectDag();
        }

        @Override
        public boolean isSetEffectDag() {
            return props.isSetEffectDag();
        }

        @Override
        public void setEffectDag(CTEffectContainer effectDag) {
            props.setEffectDag(effectDag);
        }

        @Override
        public CTEffectContainer addNewEffectDag() {
            return props.addNewEffectDag();
        }

        @Override
        public void unsetEffectDag() {
            props.unsetEffectDag();
        }

        @Override
        public boolean isSetMatrixStyle() {
            return false;
        }

        @Override
        public CTStyleMatrixReference getMatrixStyle() {
            return null;
        }
        
        @Override
        public boolean isLineStyle() {
            return false;
        }
    }

    private static class TableCellDelegate implements XSLFFillProperties {
        final CTTableCellProperties props;
        
        TableCellDelegate(CTTableCellProperties props) {
            this.props = props;
        }

        @Override
        public CTNoFillProperties getNoFill() {
            return props.getNoFill();
        }

        @Override
        public boolean isSetNoFill() {
            return props.isSetNoFill();
        }

        @Override
        public void setNoFill(CTNoFillProperties noFill) {
            props.setNoFill(noFill);
        }

        @Override
        public CTNoFillProperties addNewNoFill() {
            return props.addNewNoFill();
        }

        @Override
        public void unsetNoFill() {
            props.unsetNoFill();
        }

        @Override
        public CTSolidColorFillProperties getSolidFill() {
            return props.getSolidFill();
        }

        @Override
        public boolean isSetSolidFill() {
            return props.isSetSolidFill();
        }

        @Override
        public void setSolidFill(CTSolidColorFillProperties solidFill) {
            props.setSolidFill(solidFill);
        }

        @Override
        public CTSolidColorFillProperties addNewSolidFill() {
            return props.addNewSolidFill();
        }

        @Override
        public void unsetSolidFill() {
            props.unsetSolidFill();
        }

        @Override
        public CTGradientFillProperties getGradFill() {
            return props.getGradFill();
        }

        @Override
        public boolean isSetGradFill() {
            return props.isSetGradFill();
        }

        @Override
        public void setGradFill(CTGradientFillProperties gradFill) {
            props.setGradFill(gradFill);
        }

        @Override
        public CTGradientFillProperties addNewGradFill() {
            return props.addNewGradFill();
        }

        @Override
        public void unsetGradFill() {
            props.unsetGradFill();
        }

        @Override
        public CTBlipFillProperties getBlipFill() {
            return props.getBlipFill();
        }

        @Override
        public boolean isSetBlipFill() {
            return props.isSetBlipFill();
        }

        @Override
        public void setBlipFill(CTBlipFillProperties blipFill) {
            props.setBlipFill(blipFill);
        }

        @Override
        public CTBlipFillProperties addNewBlipFill() {
            return props.addNewBlipFill();
        }

        @Override
        public void unsetBlipFill() {
            props.unsetBlipFill();
        }

        @Override
        public CTPatternFillProperties getPattFill() {
            return props.getPattFill();
        }

        @Override
        public boolean isSetPattFill() {
            return props.isSetPattFill();
        }

        @Override
        public void setPattFill(CTPatternFillProperties pattFill) {
            props.setPattFill(pattFill);
        }

        @Override
        public CTPatternFillProperties addNewPattFill() {
            return props.addNewPattFill();
        }

        @Override
        public void unsetPattFill() {
            props.unsetPattFill();
        }

        @Override
        public CTGroupFillProperties getGrpFill() {
            return props.getGrpFill();
        }

        @Override
        public boolean isSetGrpFill() {
            return props.isSetGrpFill();
        }

        @Override
        public void setGrpFill(CTGroupFillProperties grpFill) {
            props.setGrpFill(grpFill);
        }

        @Override
        public CTGroupFillProperties addNewGrpFill() {
            return props.addNewGrpFill();
        }

        @Override
        public void unsetGrpFill() {
            props.unsetGrpFill();
        }

        @Override
        public boolean isSetMatrixStyle() {
            return false;
        }

        @Override
        public CTStyleMatrixReference getMatrixStyle() {
            return null;
        }
        
        @Override
        public boolean isLineStyle() {
            return false;
        }
    }

    private static class StyleMatrixDelegate implements XSLFFillProperties {
        final CTStyleMatrixReference props;
        
        StyleMatrixDelegate(CTStyleMatrixReference props) {
            this.props = props;
        }

        @Override
        public CTNoFillProperties getNoFill() {
            return null;
        }

        @Override
        public boolean isSetNoFill() {
            return false;
        }

        @Override
        public void setNoFill(CTNoFillProperties noFill) {}

        @Override
        public CTNoFillProperties addNewNoFill() {
            return null;
        }

        @Override
        public void unsetNoFill() {}

        @Override
        public CTSolidColorFillProperties getSolidFill() {
            return null;
        }

        @Override
        public boolean isSetSolidFill() {
            return false;
        }

        @Override
        public void setSolidFill(CTSolidColorFillProperties solidFill) {}

        @Override
        public CTSolidColorFillProperties addNewSolidFill() {
            return null;
        }

        @Override
        public void unsetSolidFill() {}

        @Override
        public CTGradientFillProperties getGradFill() {
            return null;
        }

        @Override
        public boolean isSetGradFill() {
            return false;
        }

        @Override
        public void setGradFill(CTGradientFillProperties gradFill) {}

        @Override
        public CTGradientFillProperties addNewGradFill() {
            return null;
        }

        @Override
        public void unsetGradFill() {}

        @Override
        public CTBlipFillProperties getBlipFill() {
            return null;
        }

        @Override
        public boolean isSetBlipFill() {
            return false;
        }

        @Override
        public void setBlipFill(CTBlipFillProperties blipFill) {}

        @Override
        public CTBlipFillProperties addNewBlipFill() {
            return null;
        }

        @Override
        public void unsetBlipFill() {}

        @Override
        public CTPatternFillProperties getPattFill() {
            return null;
        }

        @Override
        public boolean isSetPattFill() {
            return false;
        }

        @Override
        public void setPattFill(CTPatternFillProperties pattFill) {}

        @Override
        public CTPatternFillProperties addNewPattFill() {
            return null;
        }

        @Override
        public void unsetPattFill() {}

        @Override
        public CTGroupFillProperties getGrpFill() {
            return null;
        }

        @Override
        public boolean isSetGrpFill() {
            return false;
        }

        @Override
        public void setGrpFill(CTGroupFillProperties grpFill) {}

        @Override
        public CTGroupFillProperties addNewGrpFill() {
            return null;
        }

        @Override
        public void unsetGrpFill() {}

    
        @Override
        public boolean isSetMatrixStyle() {
            return true;
        }

        @Override
        public CTStyleMatrixReference getMatrixStyle() {
            return props;
        }
        
        @Override
        public boolean isLineStyle() {
            try (XmlCursor cur = props.newCursor()) {
                String name = cur.getName().getLocalPart();
                return "lnRef".equals(name);
            }
        }
    }

    private static class FillDelegate implements XSLFFillProperties {
        final CTFillProperties props;
        
        FillDelegate(CTFillProperties props) {
            this.props = props;
        }

        @Override
        public CTNoFillProperties getNoFill() {
            return props.getNoFill();
        }

        @Override
        public boolean isSetNoFill() {
            return props.isSetNoFill();
        }

        @Override
        public void setNoFill(CTNoFillProperties noFill) {
            props.setNoFill(noFill);
        }

        @Override
        public CTNoFillProperties addNewNoFill() {
            return props.addNewNoFill();
        }

        @Override
        public void unsetNoFill() {
            props.unsetNoFill();
        }

        @Override
        public CTSolidColorFillProperties getSolidFill() {
            return props.getSolidFill();
        }

        @Override
        public boolean isSetSolidFill() {
            return props.isSetSolidFill();
        }

        @Override
        public void setSolidFill(CTSolidColorFillProperties solidFill) {
            props.setSolidFill(solidFill);
        }

        @Override
        public CTSolidColorFillProperties addNewSolidFill() {
            return props.addNewSolidFill();
        }

        @Override
        public void unsetSolidFill() {
            props.unsetSolidFill();
        }

        @Override
        public CTGradientFillProperties getGradFill() {
            return props.getGradFill();
        }

        @Override
        public boolean isSetGradFill() {
            return props.isSetGradFill();
        }

        @Override
        public void setGradFill(CTGradientFillProperties gradFill) {
            props.setGradFill(gradFill);
        }

        @Override
        public CTGradientFillProperties addNewGradFill() {
            return props.addNewGradFill();
        }

        @Override
        public void unsetGradFill() {
            props.unsetGradFill();
        }

        @Override
        public CTBlipFillProperties getBlipFill() {
            return props.getBlipFill();
        }

        @Override
        public boolean isSetBlipFill() {
            return props.isSetBlipFill();
        }

        @Override
        public void setBlipFill(CTBlipFillProperties blipFill) {
            props.setBlipFill(blipFill);
        }

        @Override
        public CTBlipFillProperties addNewBlipFill() {
            return props.addNewBlipFill();
        }

        @Override
        public void unsetBlipFill() {
            props.unsetBlipFill();
        }

        @Override
        public CTPatternFillProperties getPattFill() {
            return props.getPattFill();
        }

        @Override
        public boolean isSetPattFill() {
            return props.isSetPattFill();
        }

        @Override
        public void setPattFill(CTPatternFillProperties pattFill) {
            props.setPattFill(pattFill);
        }

        @Override
        public CTPatternFillProperties addNewPattFill() {
            return props.addNewPattFill();
        }

        @Override
        public void unsetPattFill() {
            props.unsetPattFill();
        }

        @Override
        public CTGroupFillProperties getGrpFill() {
            return props.getGrpFill();
        }

        @Override
        public boolean isSetGrpFill() {
            return props.isSetGrpFill();
        }

        @Override
        public void setGrpFill(CTGroupFillProperties grpFill) {
            props.setGrpFill(grpFill);
        }

        @Override
        public CTGroupFillProperties addNewGrpFill() {
            return props.addNewGrpFill();
        }

        @Override
        public void unsetGrpFill() {
            props.unsetGrpFill();
        }

        @Override
        public boolean isSetMatrixStyle() {
            return false;
        }

        @Override
        public CTStyleMatrixReference getMatrixStyle() {
            return null;
        }
        
        @Override
        public boolean isLineStyle() {
            return false;
        }
    }

    private static class FillPartDelegate implements XSLFFillProperties {
        final XmlObject props;
        
        FillPartDelegate(XmlObject props) {
            this.props = props;
        }

        @Override
        public CTNoFillProperties getNoFill() {
            return isSetNoFill() ? (CTNoFillProperties)props : null;
        }

        @Override
        public boolean isSetNoFill() {
            return (props instanceof CTNoFillProperties);
        }

        @Override
        public void setNoFill(CTNoFillProperties noFill) {}

        @Override
        public CTNoFillProperties addNewNoFill() {
            return null;
        }

        @Override
        public void unsetNoFill() {}

        @Override
        public CTSolidColorFillProperties getSolidFill() {
            return isSetSolidFill() ? (CTSolidColorFillProperties)props : null;
        }

        @Override
        public boolean isSetSolidFill() {
            return (props instanceof CTSolidColorFillProperties);
        }

        @Override
        public void setSolidFill(CTSolidColorFillProperties solidFill) {}

        @Override
        public CTSolidColorFillProperties addNewSolidFill() {
            return null;
        }

        @Override
        public void unsetSolidFill() {}

        @Override
        public CTGradientFillProperties getGradFill() {
            return isSetGradFill() ? (CTGradientFillProperties)props : null;
        }

        @Override
        public boolean isSetGradFill() {
            return (props instanceof CTGradientFillProperties);
        }

        @Override
        public void setGradFill(CTGradientFillProperties gradFill) {}

        @Override
        public CTGradientFillProperties addNewGradFill() {
            return null;
        }

        @Override
        public void unsetGradFill() {}

        @Override
        public CTBlipFillProperties getBlipFill() {
            return isSetBlipFill() ? (CTBlipFillProperties)props : null;
        }

        @Override
        public boolean isSetBlipFill() {
            return (props instanceof CTBlipFillProperties);
        }

        @Override
        public void setBlipFill(CTBlipFillProperties blipFill) {}

        @Override
        public CTBlipFillProperties addNewBlipFill() {
            return null;
        }

        @Override
        public void unsetBlipFill() {}

        @Override
        public CTPatternFillProperties getPattFill() {
            return isSetPattFill() ? (CTPatternFillProperties)props : null;
        }

        @Override
        public boolean isSetPattFill() {
            return (props instanceof CTPatternFillProperties);
        }

        @Override
        public void setPattFill(CTPatternFillProperties pattFill) {}

        @Override
        public CTPatternFillProperties addNewPattFill() {
            return null;
        }

        @Override
        public void unsetPattFill() {}

        @Override
        public CTGroupFillProperties getGrpFill() {
            return isSetGrpFill() ? (CTGroupFillProperties)props : null;
        }

        @Override
        public boolean isSetGrpFill() {
            return (props instanceof CTGroupFillProperties);
        }

        @Override
        public void setGrpFill(CTGroupFillProperties grpFill) {}

        @Override
        public CTGroupFillProperties addNewGrpFill() {
            return null;
        }

        @Override
        public void unsetGrpFill() {}

        @Override
        public boolean isSetMatrixStyle() {
            return false;
        }

        @Override
        public CTStyleMatrixReference getMatrixStyle() {
            return null;
        }
        
        @Override
        public boolean isLineStyle() {
            return false;
        }
    }
    
    private static class LineStyleDelegate implements XSLFFillProperties {
        final CTLineProperties props;
        
        LineStyleDelegate(CTLineProperties props) {
            this.props = props;
        }

        @Override
        public CTNoFillProperties getNoFill() {
            return props.getNoFill();
        }

        @Override
        public boolean isSetNoFill() {
            return props.isSetNoFill();
        }

        @Override
        public void setNoFill(CTNoFillProperties noFill) {
            props.setNoFill(noFill);
        }

        @Override
        public CTNoFillProperties addNewNoFill() {
            return props.addNewNoFill();
        }

        @Override
        public void unsetNoFill() {
            props.unsetNoFill();
        }

        @Override
        public CTSolidColorFillProperties getSolidFill() {
            return props.getSolidFill();
        }

        @Override
        public boolean isSetSolidFill() {
            return props.isSetSolidFill();
        }

        @Override
        public void setSolidFill(CTSolidColorFillProperties solidFill) {
            props.setSolidFill(solidFill);
        }

        @Override
        public CTSolidColorFillProperties addNewSolidFill() {
            return props.addNewSolidFill();
        }

        @Override
        public void unsetSolidFill() {
            props.unsetSolidFill();
        }

        @Override
        public CTGradientFillProperties getGradFill() {
            return props.getGradFill();
        }

        @Override
        public boolean isSetGradFill() {
            return props.isSetGradFill();
        }

        @Override
        public void setGradFill(CTGradientFillProperties gradFill) {
            props.setGradFill(gradFill);
        }

        @Override
        public CTGradientFillProperties addNewGradFill() {
            return props.addNewGradFill();
        }

        @Override
        public void unsetGradFill() {
            props.unsetGradFill();
        }

        @Override
        public CTBlipFillProperties getBlipFill() {
            return null;
        }

        @Override
        public boolean isSetBlipFill() {
            return false;
        }

        @Override
        public void setBlipFill(CTBlipFillProperties blipFill) {}

        @Override
        public CTBlipFillProperties addNewBlipFill() {
            return null;
        }

        @Override
        public void unsetBlipFill() {}

        @Override
        public CTPatternFillProperties getPattFill() {
            return props.getPattFill();
        }

        @Override
        public boolean isSetPattFill() {
            return props.isSetPattFill();
        }

        @Override
        public void setPattFill(CTPatternFillProperties pattFill) {
            props.setPattFill(pattFill);
        }

        @Override
        public CTPatternFillProperties addNewPattFill() {
            return props.addNewPattFill();
        }

        @Override
        public void unsetPattFill() {
            props.unsetPattFill();
        }

        @Override
        public CTGroupFillProperties getGrpFill() {
            return null;
        }

        @Override
        public boolean isSetGrpFill() {
            return false;
        }

        @Override
        public void setGrpFill(CTGroupFillProperties grpFill) {}

        @Override
        public CTGroupFillProperties addNewGrpFill() {
            return null;
        }

        @Override
        public void unsetGrpFill() {}

        @Override
        public boolean isSetMatrixStyle() {
            return false;
        }

        @Override
        public CTStyleMatrixReference getMatrixStyle() {
            return null;
        }
        
        @Override
        public boolean isLineStyle() {
            return true;
        }
    }
    
    private static class TextCharDelegate implements XSLFFillProperties {
        final CTTextCharacterProperties props;
        
        TextCharDelegate(CTTextCharacterProperties props) {
            this.props = props;
        }

        @Override
        public CTNoFillProperties getNoFill() {
            return props.getNoFill();
        }

        @Override
        public boolean isSetNoFill() {
            return props.isSetNoFill();
        }

        @Override
        public void setNoFill(CTNoFillProperties noFill) {
            props.setNoFill(noFill);
        }

        @Override
        public CTNoFillProperties addNewNoFill() {
            return props.addNewNoFill();
        }

        @Override
        public void unsetNoFill() {
            props.unsetNoFill();
        }

        @Override
        public CTSolidColorFillProperties getSolidFill() {
            return props.getSolidFill();
        }

        @Override
        public boolean isSetSolidFill() {
            return props.isSetSolidFill();
        }

        @Override
        public void setSolidFill(CTSolidColorFillProperties solidFill) {
            props.setSolidFill(solidFill);
        }

        @Override
        public CTSolidColorFillProperties addNewSolidFill() {
            return props.addNewSolidFill();
        }

        @Override
        public void unsetSolidFill() {
            props.unsetSolidFill();
        }

        @Override
        public CTGradientFillProperties getGradFill() {
            return props.getGradFill();
        }

        @Override
        public boolean isSetGradFill() {
            return props.isSetGradFill();
        }

        @Override
        public void setGradFill(CTGradientFillProperties gradFill) {
            props.setGradFill(gradFill);
        }

        @Override
        public CTGradientFillProperties addNewGradFill() {
            return props.addNewGradFill();
        }

        @Override
        public void unsetGradFill() {
            props.unsetGradFill();
        }

        @Override
        public CTBlipFillProperties getBlipFill() {
            return props.getBlipFill();
        }

        @Override
        public boolean isSetBlipFill() {
            return props.isSetBlipFill();
        }

        @Override
        public void setBlipFill(CTBlipFillProperties blipFill) {
            props.setBlipFill(blipFill);
        }

        @Override
        public CTBlipFillProperties addNewBlipFill() {
            return props.addNewBlipFill();
        }

        @Override
        public void unsetBlipFill() {
            props.unsetBlipFill();
        }

        @Override
        public CTPatternFillProperties getPattFill() {
            return props.getPattFill();
        }

        @Override
        public boolean isSetPattFill() {
            return props.isSetPattFill();
        }

        @Override
        public void setPattFill(CTPatternFillProperties pattFill) {
            props.setPattFill(pattFill);
        }

        @Override
        public CTPatternFillProperties addNewPattFill() {
            return props.addNewPattFill();
        }

        @Override
        public void unsetPattFill() {
            props.unsetPattFill();
        }

        @Override
        public CTGroupFillProperties getGrpFill() {
            return props.getGrpFill();
        }

        @Override
        public boolean isSetGrpFill() {
            return props.isSetGrpFill();
        }

        @Override
        public void setGrpFill(CTGroupFillProperties grpFill) {
            props.setGrpFill(grpFill);
        }

        @Override
        public CTGroupFillProperties addNewGrpFill() {
            return props.addNewGrpFill();
        }

        @Override
        public void unsetGrpFill() {
            props.unsetGrpFill();
        }

        @Override
        public boolean isSetMatrixStyle() {
            return false;
        }

        @Override
        public CTStyleMatrixReference getMatrixStyle() {
            return null;
        }
        
        @Override
        public boolean isLineStyle() {
            return false;
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T getDelegate(Class<T> clazz, XmlObject props) {
        Object obj = null;
        if (props == null) {
            return null;
        } else if (props instanceof CTShapeProperties) {
            obj = new ShapeDelegate((CTShapeProperties)props);
        } else if (props instanceof CTBackgroundProperties) {
            obj = new BackgroundDelegate((CTBackgroundProperties)props);
        } else if (props instanceof CTStyleMatrixReference) {
            obj = new StyleMatrixDelegate((CTStyleMatrixReference)props);
        } else if (props instanceof CTTableCellProperties) {
            obj = new TableCellDelegate((CTTableCellProperties)props);
        } else if (props instanceof CTNoFillProperties
            || props instanceof CTSolidColorFillProperties
            || props instanceof CTGradientFillProperties
            || props instanceof CTBlipFillProperties
            || props instanceof CTPatternFillProperties
            || props instanceof CTGroupFillProperties) {
            obj = new FillPartDelegate(props);
        } else if (props instanceof CTFillProperties) {
            obj = new FillDelegate((CTFillProperties)props);
        } else if (props instanceof CTLineProperties) {
            obj = new LineStyleDelegate((CTLineProperties)props);
        } else if (props instanceof CTTextCharacterProperties) {
            obj = new TextCharDelegate((CTTextCharacterProperties)props);
        } else {
            LOG.atError().log("{} is an unknown properties type", props.getClass());
            return null;
        }

        if (clazz.isInstance(obj)) {
            return (T)obj;
        }

        LOG.atWarn().log("{} doesn't implement {}", obj.getClass(), clazz);
        return null;
    }
}
