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

package org.apache.poi.hslf.usermodel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherChildAnchorRecord;
import org.apache.poi.ddf.EscherClientAnchorRecord;
import org.apache.poi.ddf.EscherClientDataRecord;
import org.apache.poi.ddf.EscherColorRef;
import org.apache.poi.ddf.EscherColorRef.SysIndexProcedure;
import org.apache.poi.ddf.EscherColorRef.SysIndexSource;
import org.apache.poi.ddf.EscherComplexProperty;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherProperty;
import org.apache.poi.ddf.EscherPropertyTypes;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherRecordTypes;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.ddf.EscherTextboxRecord;
import org.apache.poi.hslf.record.ColorSchemeAtom;
import org.apache.poi.hslf.record.HSLFEscherClientDataRecord;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.usermodel.FillStyle;
import org.apache.poi.sl.usermodel.PresetColor;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Removal;
import org.apache.poi.util.StringUtil;
import org.apache.poi.util.Units;

/**
 *  <p>
  * Represents a Shape which is the elemental object that composes a drawing.
 *  This class is a wrapper around EscherSpContainer which holds all information
 *  about a shape in PowerPoint document.
 *  </p>
 *  <p>
 *  When you add a shape, you usually specify the dimensions of the shape and the position
 *  of the upper'left corner of the bounding box for the shape relative to the upper'left
 *  corner of the page, worksheet, or slide. Distances in the drawing layer are measured
 *  in points (72 points = 1 inch).
 *  </p>
 * <p>
 */
public abstract class HSLFShape implements Shape<HSLFShape,HSLFTextParagraph> {
    private static final POILogger LOG = POILogFactory.getLogger(HSLFShape.class);

    /**
     * Either EscherSpContainer or EscheSpgrContainer record
     * which holds information about this shape.
     */
    private EscherContainerRecord _escherContainer;

    /**
     * Parent of this shape.
     * <code>null</code> for the topmost shapes.
     */
    private ShapeContainer<HSLFShape,HSLFTextParagraph> _parent;

    /**
     * The <code>Sheet</code> this shape belongs to
     */
    private HSLFSheet _sheet;

    /**
     * Fill
     */
    private HSLFFill _fill;
    
    /**
     * Create a Shape object. This constructor is used when an existing Shape is read from from a PowerPoint document.
     *
     * @param escherRecord       <code>EscherSpContainer</code> container which holds information about this shape
     * @param parent             the parent of this Shape
     */
      protected HSLFShape(EscherContainerRecord escherRecord, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        _escherContainer = escherRecord;
        _parent = parent;
     }

    /**
     * Create and assign the lower level escher record to this shape
     */
    protected EscherContainerRecord createSpContainer(boolean isChild) {
        if (_escherContainer == null) {
            _escherContainer = new EscherContainerRecord();
            _escherContainer.setOptions((short)15);
        }
        return _escherContainer;
    }

    /**
     *  @return the parent of this shape
     */
    @Override
    public ShapeContainer<HSLFShape,HSLFTextParagraph> getParent(){
        return _parent;
    }

    /**
     * @return name of the shape.
     */
    @Override
    public String getShapeName(){
        final EscherComplexProperty ep = getEscherProperty(getEscherOptRecord(), EscherPropertyTypes.GROUPSHAPE__SHAPENAME);
        if (ep != null) {
            final byte[] cd = ep.getComplexData();
            return StringUtil.getFromUnicodeLE0Terminated(cd, 0, cd.length/2);
        } else {
            return getShapeType().nativeName+" "+getShapeId();
        }
    }

    public ShapeType getShapeType(){
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        return ShapeType.forId(spRecord.getShapeType(), false);
    }

    public void setShapeType(ShapeType type){
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        spRecord.setShapeType( (short) type.nativeId );
        spRecord.setVersion( (short) 0x2 );
    }

    /**
     * Returns the anchor (the bounding box rectangle) of this shape.
     * All coordinates are expressed in points (72 dpi).
     *
     * @return the anchor of this shape
     */
    @Override
    public Rectangle2D getAnchor() {
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        int flags = spRecord.getFlags();
        int x1,y1,x2,y2;
        EscherChildAnchorRecord childRec = getEscherChild(EscherChildAnchorRecord.RECORD_ID);
        boolean useChildRec = ((flags & EscherSpRecord.FLAG_CHILD) != 0);
        if (useChildRec && childRec != null){
            x1 = childRec.getDx1();
            y1 = childRec.getDy1();
            x2 = childRec.getDx2();
            y2 = childRec.getDy2();
        } else {
            if (useChildRec) {
                LOG.log(POILogger.WARN, "EscherSpRecord.FLAG_CHILD is set but EscherChildAnchorRecord was not found");
            }
            EscherClientAnchorRecord clientRec = getEscherChild(EscherClientAnchorRecord.RECORD_ID);
            x1 = clientRec.getCol1();
            y1 = clientRec.getFlag();
            x2 = clientRec.getDx1();
            y2 = clientRec.getRow1();
        }

        // TODO: find out where this -1 value comes from at #57820 (link to ms docs?)

        return new Rectangle2D.Double(
            (x1 == -1 ? -1 : Units.masterToPoints(x1)),
            (y1 == -1 ? -1 : Units.masterToPoints(y1)),
            (x2 == -1 ? -1 : Units.masterToPoints(x2-x1)),
            (y2 == -1 ? -1 : Units.masterToPoints(y2-y1))
        );
    }

    /**
     * Sets the anchor (the bounding box rectangle) of this shape.
     * All coordinates should be expressed in points (72 dpi).
     *
     * @param anchor new anchor
     */
    public void setAnchor(Rectangle2D anchor){
        int x = Units.pointsToMaster(anchor.getX());
        int y = Units.pointsToMaster(anchor.getY());
        int w = Units.pointsToMaster(anchor.getWidth() + anchor.getX());
        int h = Units.pointsToMaster(anchor.getHeight() + anchor.getY());
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        int flags = spRecord.getFlags();
        if ((flags & EscherSpRecord.FLAG_CHILD) != 0){
            EscherChildAnchorRecord rec = getEscherChild(EscherChildAnchorRecord.RECORD_ID);
            rec.setDx1(x);
            rec.setDy1(y);
            rec.setDx2(w);
            rec.setDy2(h);
        } else {
            EscherClientAnchorRecord rec = getEscherChild(EscherClientAnchorRecord.RECORD_ID);
            rec.setCol1((short)x);
            rec.setFlag((short)y);
            rec.setDx1((short)w);
            rec.setRow1((short)h);
        }

    }

    /**
     * Moves the top left corner of the shape to the specified point.
     *
     * @param x the x coordinate of the top left corner of the shape
     * @param y the y coordinate of the top left corner of the shape
     */
    public final void moveTo(double x, double y) {
        // This convenience method should be implemented via setAnchor in subclasses
        // see HSLFGroupShape.setAnchor() for a reference
        Rectangle2D anchor = getAnchor();
        anchor.setRect(x, y, anchor.getWidth(), anchor.getHeight());
        setAnchor(anchor);
    }

    /**
     * Helper method to return escher child by record ID
     *
     * @return escher record or <code>null</code> if not found.
     */
    public static <T extends EscherRecord> T getEscherChild(EscherContainerRecord owner, int recordId){
        return owner.getChildById((short)recordId);
    }
    
    /**
     * @since POI 3.14-Beta2
     */
    public static <T extends EscherRecord> T getEscherChild(EscherContainerRecord owner, EscherRecordTypes recordId){
        return getEscherChild(owner, recordId.typeID);
    }

    public <T extends EscherRecord> T getEscherChild(int recordId){
        return _escherContainer.getChildById((short)recordId);
    }
    
    /**
     * @since POI 3.14-Beta2
     */
    public <T extends EscherRecord> T getEscherChild(EscherRecordTypes recordId){
        return getEscherChild(recordId.typeID);
    }
    
    /**
     * Returns  escher property by id.
     *
     * @return escher property or <code>null</code> if not found.
     *
     * @deprecated use {@link #getEscherProperty(EscherPropertyTypes)} instead
     */
     @Deprecated
     @Removal(version = "5.0.0")
     public static <T extends EscherProperty> T getEscherProperty(AbstractEscherOptRecord opt, int propId){
         return (opt == null) ? null : opt.lookup(propId);
     }

    /**
     * Returns  escher property by type.
     *
     * @return escher property or <code>null</code> if not found.
     */
    public static <T extends EscherProperty> T getEscherProperty(AbstractEscherOptRecord opt, EscherPropertyTypes type){
        return (opt == null) ? null : opt.lookup(type);
    }

    /**
     * Set an escher property for this shape.
     *
     * @param opt       The opt record to set the properties to.
     * @param propId    The id of the property. One of the constants defined in EscherOptRecord.
     * @param value     value of the property. If value = -1 then the property is removed.
     *
     * @deprecated use {@link #setEscherProperty(AbstractEscherOptRecord, EscherPropertyTypes, int)}
     */
    @Deprecated
    @Removal(version = "5.0.0")
    public static void setEscherProperty(AbstractEscherOptRecord opt, short propId, int value){
        List<EscherProperty> props = opt.getEscherProperties();
        for ( Iterator<EscherProperty> iterator = props.iterator(); iterator.hasNext(); ) {
            if (iterator.next().getPropertyNumber() == propId){
                iterator.remove();
                break;
            }
        }
        if (value != -1) {
            opt.addEscherProperty(new EscherSimpleProperty(propId, value));
            opt.sortProperties();
        }
    }

    /**
     * Set an escher property for this shape.
     *
     * @param opt       The opt record to set the properties to.
     * @param propType  The type of the property.
     * @param value     value of the property. If value = -1 then the property is removed.
     */
    public static void setEscherProperty(AbstractEscherOptRecord opt, EscherPropertyTypes propType, int value){
        setEscherProperty(opt, propType, false, value);
    }

    public static void setEscherProperty(AbstractEscherOptRecord opt, EscherPropertyTypes propType, boolean isBlipId, int value){
        List<EscherProperty> props = opt.getEscherProperties();
        for ( Iterator<EscherProperty> iterator = props.iterator(); iterator.hasNext(); ) {
            if (iterator.next().getPropertyNumber() == propType.propNumber){
                iterator.remove();
                break;
            }
        }
        if (value != -1) {
            opt.addEscherProperty(new EscherSimpleProperty(propType, false, isBlipId, value));
            opt.sortProperties();
        }
    }



    /**
     * Set an simple escher property for this shape.
     *
     * @param propId    The id of the property. One of the constants defined in EscherOptRecord.
     * @param value     value of the property. If value = -1 then the property is removed.
     *
     * @deprecated use {@link #setEscherProperty(EscherPropertyTypes, int)}
     */
    @Deprecated
    @Removal(version = "5.0.0")
    public void setEscherProperty(short propId, int value){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        setEscherProperty(opt, propId, value);
    }

    /**
     * Set an simple escher property for this shape.
     *
     * @param propType  The type of the property.
     * @param value     value of the property. If value = -1 then the property is removed.
     */
    public void setEscherProperty(EscherPropertyTypes propType, int value){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        setEscherProperty(opt, propType, value);
    }

    /**
     * Get the value of a simple escher property for this shape.
     *
     * @param propId    The id of the property. One of the constants defined in EscherOptRecord.
     */
    public int getEscherProperty(short propId){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, propId);
        return prop == null ? 0 : prop.getPropertyValue();
    }

    /**
     * Get the value of a simple escher property for this shape.
     *
     * @param propType    The type of the property. One of the constants defined in EscherOptRecord.
     */
    public int getEscherProperty(EscherPropertyTypes propType){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, propType);
        return prop == null ? 0 : prop.getPropertyValue();
    }

    /**
     * Get the value of a simple escher property for this shape.
     *
     * @param propId    The id of the property. One of the constants defined in EscherOptRecord.
     *
     * @deprecated use {@link #getEscherProperty(EscherPropertyTypes, int)} instead
     */
    @Deprecated
    @Removal(version = "5.0.0")
    public int getEscherProperty(short propId, int defaultValue){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, propId);
        return prop == null ? defaultValue : prop.getPropertyValue();
    }

    /**
     * Get the value of a simple escher property for this shape.
     *
     * @param type    The type of the property.
     */
    public int getEscherProperty(EscherPropertyTypes type, int defaultValue){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, type);
        return prop == null ? defaultValue : prop.getPropertyValue();
    }

    /**
     * @return  The shape container and it's children that can represent this
     *          shape.
     */
    public EscherContainerRecord getSpContainer(){
        return _escherContainer;
    }

    /**
     * Event which fires when a shape is inserted in the sheet.
     * In some cases we need to propagate changes to upper level containers.
     * <br>
     * Default implementation does nothing.
     *
     * @param sh - owning shape
     */
    protected void afterInsert(HSLFSheet sh){
        if(_fill != null) {
            _fill.afterInsert(sh);
        }
    }

    /**
     *  @return the <code>SlideShow</code> this shape belongs to
     */
    @Override
    public HSLFSheet getSheet(){
        return _sheet;
    }

    /**
     * Assign the <code>SlideShow</code> this shape belongs to
     *
     * @param sheet owner of this shape
     */
    public void setSheet(HSLFSheet sheet){
        _sheet = sheet;
    }

    Color getColor(EscherPropertyTypes colorProperty, EscherPropertyTypes opacityProperty){
        final AbstractEscherOptRecord opt = getEscherOptRecord();
        final EscherSimpleProperty colProp = getEscherProperty(opt, colorProperty);
        final Color col;
        if (colProp == null) {
            col = Color.WHITE;
        } else {
            EscherColorRef ecr = new EscherColorRef(colProp.getPropertyValue());
            col = getColor(ecr);
            if (col == null) {
                return null;
            }
        }

        double alpha = getAlpha(opacityProperty);
        return new Color(col.getRed(), col.getGreen(), col.getBlue(), (int)(alpha*255.0));
    }

    Color getColor(EscherColorRef ecr) {
        boolean fPaletteIndex = ecr.hasPaletteIndexFlag();
        boolean fPaletteRGB = ecr.hasPaletteRGBFlag();
        boolean fSystemRGB = ecr.hasSystemRGBFlag();
        boolean fSchemeIndex = ecr.hasSchemeIndexFlag();
        boolean fSysIndex = ecr.hasSysIndexFlag();

        int[] rgb = ecr.getRGB();

        HSLFSheet sheet = getSheet();
        if (fSchemeIndex && sheet != null) {
            //red is the index to the color scheme
            ColorSchemeAtom ca = sheet.getColorScheme();
            int schemeColor = ca.getColor(ecr.getSchemeIndex());

            rgb[0] = (schemeColor >> 0) & 0xFF;
            rgb[1] = (schemeColor >> 8) & 0xFF;
            rgb[2] = (schemeColor >> 16) & 0xFF;
        } else if (fPaletteIndex) {
            //TODO
        } else if (fPaletteRGB) {
            //TODO
        } else if (fSystemRGB) {
            //TODO
        } else if (fSysIndex) {
            Color col = getSysIndexColor(ecr);
            col = applySysIndexProcedure(ecr, col);
            return col;
        }
        
        return new Color(rgb[0], rgb[1], rgb[2]);
    }
    
    private Color getSysIndexColor(EscherColorRef ecr) {
        SysIndexSource sis = ecr.getSysIndexSource();
        if (sis == null) {
            int sysIdx = ecr.getSysIndex();
            PresetColor pc = PresetColor.valueOfNativeId(sysIdx);
            return (pc != null) ? pc.color : null;
        }
        
        // TODO: check for recursive loops, when color getter also reference
        // a different color type
        switch (sis) {
            case FILL_COLOR: {
                return getColor(EscherPropertyTypes.FILL__FILLCOLOR, EscherPropertyTypes.FILL__FILLOPACITY);
            }
            case LINE_OR_FILL_COLOR: {
                Color col = null;
                if (this instanceof HSLFSimpleShape) {
                    col = getColor(EscherPropertyTypes.LINESTYLE__COLOR, EscherPropertyTypes.LINESTYLE__OPACITY);
                }
                if (col == null) {
                    col = getColor(EscherPropertyTypes.FILL__FILLCOLOR, EscherPropertyTypes.FILL__FILLOPACITY);
                }
                return col;
            }
            case LINE_COLOR: {
                if (this instanceof HSLFSimpleShape) {
                    return getColor(EscherPropertyTypes.LINESTYLE__COLOR, EscherPropertyTypes.LINESTYLE__OPACITY);
                }
                break;
            }
            case SHADOW_COLOR: {
                if (this instanceof HSLFSimpleShape) {
                    return ((HSLFSimpleShape)this).getShadowColor();
                }
                break;
            }
            case CURRENT_OR_LAST_COLOR: {
                // TODO ... read from graphics context???
                break;
            }
            case FILL_BACKGROUND_COLOR: {
                return getColor(EscherPropertyTypes.FILL__FILLBACKCOLOR, EscherPropertyTypes.FILL__FILLOPACITY);
            }
            case LINE_BACKGROUND_COLOR: {
                if (this instanceof HSLFSimpleShape) {
                    return ((HSLFSimpleShape)this).getLineBackgroundColor();
                }
                break;
            }
            case FILL_OR_LINE_COLOR: {
                Color col = getColor(EscherPropertyTypes.FILL__FILLCOLOR, EscherPropertyTypes.FILL__FILLOPACITY);
                if (col == null && this instanceof HSLFSimpleShape) {
                    col = getColor(EscherPropertyTypes.LINESTYLE__COLOR, EscherPropertyTypes.LINESTYLE__OPACITY);
                }
                return col;
            }
            default:
                break;
        }
            
        return null;
    }
        
    private Color applySysIndexProcedure(EscherColorRef ecr, Color col) {
        
        final SysIndexProcedure sip = ecr.getSysIndexProcedure();
        if (col == null || sip == null) {
            return col;
        }
        
        switch (sip) {
            case DARKEN_COLOR: {
                // see java.awt.Color#darken()
                double FACTOR = (ecr.getRGB()[2])/255.;
                int r = (int)Math.rint(col.getRed()*FACTOR);
                int g = (int)Math.rint(col.getGreen()*FACTOR);
                int b = (int)Math.rint(col.getBlue()*FACTOR);
                return new Color(r,g,b);                
            }
            case LIGHTEN_COLOR: {
                double FACTOR = (0xFF-ecr.getRGB()[2])/255.;
                               
                int r = col.getRed();
                int g = col.getGreen();
                int b = col.getBlue();
                
                r += Math.rint((0xFF-r)*FACTOR);
                g += Math.rint((0xFF-g)*FACTOR);
                b += Math.rint((0xFF-b)*FACTOR);
                
                return new Color(r,g,b);
            }
            default:
                // TODO ...
                break;
        }
        
        return col;
    }
    
    double getAlpha(EscherPropertyTypes opacityProperty) {
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty op = getEscherProperty(opt, opacityProperty);
        int defaultOpacity = 0x00010000;
        int opacity = (op == null) ? defaultOpacity : op.getPropertyValue();
        return Units.fixedPointToDouble(opacity);
    }
    
    Color toRGB(int val){
        int a = (val >> 24) & 0xFF;
        int b = (val >> 16) & 0xFF;
        int g = (val >> 8) & 0xFF;
        int r = (val >> 0) & 0xFF;

        if(a == 0xFE){
            // Color is an sRGB value specified by red, green, and blue fields.
        } else if (a == 0xFF){
            // Color is undefined.
        } else {
            // index in the color scheme
            ColorSchemeAtom ca = getSheet().getColorScheme();
            int schemeColor = ca.getColor(a);

            r = (schemeColor >> 0) & 0xFF;
            g = (schemeColor >> 8) & 0xFF;
            b = (schemeColor >> 16) & 0xFF;
        }
        return new Color(r, g, b);
    }

    @Override
    public int getShapeId(){
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        return spRecord == null ? 0 : spRecord.getShapeId();
    }

    /**
     * Sets shape ID
     *
     * @param id of the shape
     */
    public void setShapeId(int id){
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        if(spRecord != null) spRecord.setShapeId(id);
    }

    /**
     * Fill properties of this shape
     *
     * @return fill properties of this shape
     */
    public HSLFFill getFill(){
        if(_fill == null) {
            _fill = new HSLFFill(this);
        }
        return _fill;
    }

    public FillStyle getFillStyle() {
        return getFill().getFillStyle();
    }

    @Override
    public void draw(Graphics2D graphics, Rectangle2D bounds){
        DrawFactory.getInstance(graphics).drawShape(graphics, this, bounds);
    }

    public AbstractEscherOptRecord getEscherOptRecord() {
        AbstractEscherOptRecord opt = getEscherChild(EscherRecordTypes.OPT);
        if (opt == null) {
            opt = getEscherChild(EscherRecordTypes.USER_DEFINED);
        }
        return opt;
    }
    
    public boolean getFlipHorizontal(){
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        return (spRecord.getFlags()& EscherSpRecord.FLAG_FLIPHORIZ) != 0;
    }
     
    public void setFlipHorizontal(boolean flip) {
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        int flag = spRecord.getFlags() | EscherSpRecord.FLAG_FLIPHORIZ;
        spRecord.setFlags(flag);
    }

    public boolean getFlipVertical(){
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        return (spRecord.getFlags()& EscherSpRecord.FLAG_FLIPVERT) != 0;
    }
    
    public void setFlipVertical(boolean flip) {
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        int flag = spRecord.getFlags() | EscherSpRecord.FLAG_FLIPVERT;
        spRecord.setFlags(flag);
    }

    public double getRotation(){
        int rot = getEscherProperty(EscherPropertyTypes.TRANSFORM__ROTATION);
        return Units.fixedPointToDouble(rot);
    }
    
    public void setRotation(double theta){
        int rot = Units.doubleToFixedPoint(theta % 360.0);
        setEscherProperty(EscherPropertyTypes.TRANSFORM__ROTATION, rot);
    }

    public boolean isPlaceholder() {
        return false;
    }

    /**
     *  Find a record in the underlying EscherClientDataRecord
     *
     * @param recordType type of the record to search
     */
    @SuppressWarnings("unchecked")
    public <T extends Record> T getClientDataRecord(int recordType) {

        List<? extends Record> records = getClientRecords();
        if (records != null) for (org.apache.poi.hslf.record.Record r : records) {
            if (r.getRecordType() == recordType){
                return (T)r;
            }
        }
        return null;
    }

    /**
     * Search for EscherClientDataRecord, if found, convert its contents into an array of HSLF records
     *
     * @return an array of HSLF records contained in the shape's EscherClientDataRecord or <code>null</code>
     */
    protected List<? extends Record> getClientRecords() {
        HSLFEscherClientDataRecord clientData = getClientData(false);
        return (clientData == null) ? null : clientData.getHSLFChildRecords();
    }

    /**
     * Create a new HSLF-specific EscherClientDataRecord
     *
     * @param create if true, create the missing record 
     * @return the client record or null if it was missing and create wasn't activated
     */
    protected HSLFEscherClientDataRecord getClientData(boolean create) {
        HSLFEscherClientDataRecord clientData = getEscherChild(EscherClientDataRecord.RECORD_ID);
        if (clientData == null && create) {
            clientData = new HSLFEscherClientDataRecord();
            clientData.setOptions((short)15);
            clientData.setRecordId(EscherClientDataRecord.RECORD_ID);
            getSpContainer().addChildBefore(clientData, EscherTextboxRecord.RECORD_ID);
        }
        return clientData;
    }
}
