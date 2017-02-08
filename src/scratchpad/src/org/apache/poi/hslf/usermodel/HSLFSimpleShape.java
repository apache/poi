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
import java.util.List;

import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherChildAnchorRecord;
import org.apache.poi.ddf.EscherClientAnchorRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.ddf.EscherProperty;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.hslf.record.HSLFEscherClientDataRecord;
import org.apache.poi.hslf.record.OEPlaceholderAtom;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RoundTripHFPlaceholder12;
import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.draw.geom.CustomGeometry;
import org.apache.poi.sl.draw.geom.Guide;
import org.apache.poi.sl.draw.geom.PresetGeometries;
import org.apache.poi.sl.usermodel.LineDecoration;
import org.apache.poi.sl.usermodel.LineDecoration.DecorationShape;
import org.apache.poi.sl.usermodel.LineDecoration.DecorationSize;
import org.apache.poi.sl.usermodel.MasterSheet;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PaintStyle.SolidPaint;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.sl.usermodel.Shadow;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.SimpleShape;
import org.apache.poi.sl.usermodel.StrokeStyle;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCap;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCompound;
import org.apache.poi.sl.usermodel.StrokeStyle.LineDash;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Units;

/**
 *  An abstract simple (non-group) shape.
 *  This is the parent class for all primitive shapes like Line, Rectangle, etc.
 */
public abstract class HSLFSimpleShape extends HSLFShape implements SimpleShape<HSLFShape,HSLFTextParagraph> {
    private static final POILogger LOG = POILogFactory.getLogger(HSLFSimpleShape.class);

    public final static double DEFAULT_LINE_WIDTH = 0.75;

    /**
     * Hyperlink
     */
    protected HSLFHyperlink _hyperlink;
    
    /**
     * Create a SimpleShape object and initialize it from the supplied Record container.
     *
     * @param escherRecord    <code>EscherSpContainer</code> container which holds information about this shape
     * @param parent    the parent of the shape
     */
    protected HSLFSimpleShape(EscherContainerRecord escherRecord, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super(escherRecord, parent);
    }

    /**
     * Create a new Shape
     *
     * @param isChild   <code>true</code> if the Line is inside a group, <code>false</code> otherwise
     * @return the record container which holds this shape
     */
    @Override
    protected EscherContainerRecord createSpContainer(boolean isChild) {
        EscherContainerRecord ecr = super.createSpContainer(isChild);
        ecr.setRecordId( EscherContainerRecord.SP_CONTAINER );

        EscherSpRecord sp = new EscherSpRecord();
        int flags = EscherSpRecord.FLAG_HAVEANCHOR | EscherSpRecord.FLAG_HASSHAPETYPE;
        if (isChild) {
            flags |= EscherSpRecord.FLAG_CHILD;
        }
        sp.setFlags(flags);
        ecr.addChildRecord(sp);

        AbstractEscherOptRecord opt = new EscherOptRecord();
        opt.setRecordId(EscherOptRecord.RECORD_ID);
        ecr.addChildRecord(opt);

        EscherRecord anchor;
        if(isChild) {
            anchor = new EscherChildAnchorRecord();
        } else {
            anchor = new EscherClientAnchorRecord();

            //hack. internal variable EscherClientAnchorRecord.shortRecord can be
            //initialized only in fillFields(). We need to set shortRecord=false;
            byte[] header = new byte[16];
            LittleEndian.putUShort(header, 0, 0);
            LittleEndian.putUShort(header, 2, 0);
            LittleEndian.putInt(header, 4, 8);
            anchor.fillFields(header, 0, null);
        }
        ecr.addChildRecord(anchor);

        return ecr;
    }

    /**
     *  Returns width of the line in in points
     */
    public double getLineWidth(){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherProperties.LINESTYLE__LINEWIDTH);
        double width = (prop == null) ? DEFAULT_LINE_WIDTH : Units.toPoints(prop.getPropertyValue());
        return width;
    }

    /**
     *  Sets the width of line in in points
     *  @param width  the width of line in in points
     */
    public void setLineWidth(double width){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        setEscherProperty(opt, EscherProperties.LINESTYLE__LINEWIDTH, Units.toEMU(width));
    }

    /**
     * Sets the color of line
     *
     * @param color new color of the line
     */
    public void setLineColor(Color color){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        if (color == null) {
            setEscherProperty(opt, EscherProperties.LINESTYLE__NOLINEDRAWDASH, 0x80000);
        } else {
            int rgb = new Color(color.getBlue(), color.getGreen(), color.getRed(), 0).getRGB();
            setEscherProperty(opt, EscherProperties.LINESTYLE__COLOR, rgb);
            setEscherProperty(opt, EscherProperties.LINESTYLE__NOLINEDRAWDASH, 0x180018);
        }
    }

    /**
     * @return color of the line. If color is not set returns {@code null}
     */
    public Color getLineColor(){
        AbstractEscherOptRecord opt = getEscherOptRecord();

        EscherSimpleProperty p = getEscherProperty(opt, EscherProperties.LINESTYLE__NOLINEDRAWDASH);
        if(p != null && (p.getPropertyValue() & 0x8) == 0) {
            return null;
        }

        Color clr = getColor(EscherProperties.LINESTYLE__COLOR, EscherProperties.LINESTYLE__OPACITY, -1);
        return clr == null ? null : clr;
    }

    /**
     * @return background color of the line. If color is not set returns {@code null}
     */
    public Color getLineBackgroundColor(){
        AbstractEscherOptRecord opt = getEscherOptRecord();

        EscherSimpleProperty p = getEscherProperty(opt, EscherProperties.LINESTYLE__NOLINEDRAWDASH);
        if(p != null && (p.getPropertyValue() & 0x8) == 0) {
            return null;
        }

        Color clr = getColor(EscherProperties.LINESTYLE__BACKCOLOR, EscherProperties.LINESTYLE__OPACITY, -1);
        return clr == null ? null : clr;
    }

    /**
     * Sets the background color of line
     *
     * @param color new background color of the line
     */
    public void setLineBackgroundColor(Color color){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        if (color == null) {
            setEscherProperty(opt, EscherProperties.LINESTYLE__NOLINEDRAWDASH, 0x80000);
            opt.removeEscherProperty(EscherProperties.LINESTYLE__BACKCOLOR);
        } else {
            int rgb = new Color(color.getBlue(), color.getGreen(), color.getRed(), 0).getRGB();
            setEscherProperty(opt, EscherProperties.LINESTYLE__BACKCOLOR, rgb);
            setEscherProperty(opt, EscherProperties.LINESTYLE__NOLINEDRAWDASH, 0x180018);
        }
    }

    /**
     * Gets line cap.
     *
     * @return cap of the line.
     */
    public LineCap getLineCap(){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherProperties.LINESTYLE__LINEENDCAPSTYLE);
        return (prop == null) ? LineCap.FLAT : LineCap.fromNativeId(prop.getPropertyValue());
    }

    /**
     * Sets line cap.
     *
     * @param pen new style of the line.
     */
    public void setLineCap(LineCap pen){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        setEscherProperty(opt, EscherProperties.LINESTYLE__LINEENDCAPSTYLE, pen == LineCap.FLAT ? -1 : pen.nativeId);
    }

    /**
     * Gets line dashing.
     *
     * @return dashing of the line.
     */
    public LineDash getLineDash(){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherProperties.LINESTYLE__LINEDASHING);
        return (prop == null) ? LineDash.SOLID : LineDash.fromNativeId(prop.getPropertyValue());
    }

    /**
     * Sets line dashing.
     *
     * @param pen new style of the line.
     */
    public void setLineDash(LineDash pen){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        setEscherProperty(opt, EscherProperties.LINESTYLE__LINEDASHING, pen == LineDash.SOLID ? -1 : pen.nativeId);
    }

    /**
     * Gets the line compound style
     *
     * @return the compound style of the line.
     */
    public LineCompound getLineCompound() {
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherProperties.LINESTYLE__LINESTYLE);
        return (prop == null) ? LineCompound.SINGLE : LineCompound.fromNativeId(prop.getPropertyValue());
    }

    /**
     * Sets the line compound style
     *
     * @param style new compound style of the line.
     */
    public void setLineCompound(LineCompound style){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        setEscherProperty(opt, EscherProperties.LINESTYLE__LINESTYLE, style == LineCompound.SINGLE ? -1 : style.nativeId);
    }

    /**
     * Returns line style. One of the constants defined in this class.
     *
     * @return style of the line.
     */
    @Override
    public StrokeStyle getStrokeStyle(){
        return new StrokeStyle() {
            @Override
            public PaintStyle getPaint() {
                return DrawPaint.createSolidPaint(HSLFSimpleShape.this.getLineColor());
            }

            @Override
            public LineCap getLineCap() {
                return null;
            }

            @Override
            public LineDash getLineDash() {
                return HSLFSimpleShape.this.getLineDash();
            }

            @Override
            public LineCompound getLineCompound() {
                return HSLFSimpleShape.this.getLineCompound();
            }

            @Override
            public double getLineWidth() {
                return HSLFSimpleShape.this.getLineWidth();
            }

        };
    }

    @Override
    public Color getFillColor() {
        return getFill().getForegroundColor();
    }

    @Override
    public void setFillColor(Color color) {
        getFill().setForegroundColor(color);
    }

    @Override
    public Guide getAdjustValue(String name) {
        if (name == null || !name.matches("adj([1-9]|10)?")) {
            LOG.log(POILogger.INFO, "Adjust value '"+name+"' not supported. Using default value.");
            return null;
        }

        name = name.replace("adj", "");
        if ("".equals(name)) {
            name = "1";
        }

        short escherProp;
        switch (Integer.parseInt(name)) {
            case 1: escherProp = EscherProperties.GEOMETRY__ADJUSTVALUE; break;
            case 2: escherProp = EscherProperties.GEOMETRY__ADJUST2VALUE; break;
            case 3: escherProp = EscherProperties.GEOMETRY__ADJUST3VALUE; break;
            case 4: escherProp = EscherProperties.GEOMETRY__ADJUST4VALUE; break;
            case 5: escherProp = EscherProperties.GEOMETRY__ADJUST5VALUE; break;
            case 6: escherProp = EscherProperties.GEOMETRY__ADJUST6VALUE; break;
            case 7: escherProp = EscherProperties.GEOMETRY__ADJUST7VALUE; break;
            case 8: escherProp = EscherProperties.GEOMETRY__ADJUST8VALUE; break;
            case 9: escherProp = EscherProperties.GEOMETRY__ADJUST9VALUE; break;
            case 10: escherProp = EscherProperties.GEOMETRY__ADJUST10VALUE; break;
            default: throw new HSLFException();
        }

        // TODO: the adjust values need to be corrected somehow depending on the shape width/height
        // see https://social.msdn.microsoft.com/Forums/en-US/3f69ebb3-62a0-4fdd-b367-64790dfb2491/presetshapedefinitionsxml-does-not-specify-width-and-height-form-some-autoshapes?forum=os_binaryfile
        
        // the adjust value can be format dependent, e.g. hexagon has different values,
        // other shape types have the same adjust values in OOXML and native
        int adjval = getEscherProperty(escherProp, -1);

        return (adjval == -1) ? null : new Guide(name, "val "+adjval);
    }

    @Override
    public CustomGeometry getGeometry() {
        PresetGeometries dict = PresetGeometries.getInstance();
        ShapeType st = getShapeType();
        String name = (st != null) ? st.getOoxmlName() : null;
        CustomGeometry geom = dict.get(name);
        if (geom == null) {
            if (name == null) {
                name = (st != null) ? st.toString() : "<unknown>";
            }
            LOG.log(POILogger.WARN, "No preset shape definition for shapeType: "+name);
        }

        return geom;
    }


    public double getShadowAngle() {
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherProperties.SHADOWSTYLE__OFFSETX);
        int offX = (prop == null) ? 0 : prop.getPropertyValue();
        prop = getEscherProperty(opt, EscherProperties.SHADOWSTYLE__OFFSETY);
        int offY = (prop == null) ? 0 : prop.getPropertyValue();
        return Math.toDegrees(Math.atan2(offY, offX));
    }

    public double getShadowDistance() {
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherProperties.SHADOWSTYLE__OFFSETX);
        int offX = (prop == null) ? 0 : prop.getPropertyValue();
        prop = getEscherProperty(opt, EscherProperties.SHADOWSTYLE__OFFSETY);
        int offY = (prop == null) ? 0 : prop.getPropertyValue();
        return Units.toPoints((long)Math.hypot(offX, offY));
    }

    /**
     * @return color of the line. If color is not set returns <code>java.awt.Color.black</code>
     */
    public Color getShadowColor(){
        Color clr = getColor(EscherProperties.SHADOWSTYLE__COLOR, EscherProperties.SHADOWSTYLE__OPACITY, -1);
        return clr == null ? Color.black : clr;
    }

    @Override
    public Shadow<HSLFShape,HSLFTextParagraph> getShadow() {
        AbstractEscherOptRecord opt = getEscherOptRecord();
        if (opt == null) {
            return null;
        }
        EscherProperty shadowType = opt.lookup(EscherProperties.SHADOWSTYLE__TYPE);
        if (shadowType == null) {
            return null;
        }

        return new Shadow<HSLFShape,HSLFTextParagraph>(){
            @Override
            public SimpleShape<HSLFShape,HSLFTextParagraph> getShadowParent() {
                return HSLFSimpleShape.this;
            }

            @Override
            public double getDistance() {
                return getShadowDistance();
            }

            @Override
            public double getAngle() {
                return getShadowAngle();
            }

            @Override
            public double getBlur() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public SolidPaint getFillStyle() {
                return DrawPaint.createSolidPaint(getShadowColor());
            }

        };
    }

    public DecorationShape getLineHeadDecoration(){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherProperties.LINESTYLE__LINESTARTARROWHEAD);
        return (prop == null) ? null : DecorationShape.fromNativeId(prop.getPropertyValue());
    }

    public void setLineHeadDecoration(DecorationShape decoShape){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        setEscherProperty(opt, EscherProperties.LINESTYLE__LINESTARTARROWHEAD, decoShape == null ? -1 : decoShape.nativeId);
    }

    public DecorationSize getLineHeadWidth(){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherProperties.LINESTYLE__LINESTARTARROWWIDTH);
        return (prop == null) ? null : DecorationSize.fromNativeId(prop.getPropertyValue());
    }

    public void setLineHeadWidth(DecorationSize decoSize){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        setEscherProperty(opt, EscherProperties.LINESTYLE__LINESTARTARROWWIDTH, decoSize == null ? -1 : decoSize.nativeId);
    }

    public DecorationSize getLineHeadLength(){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherProperties.LINESTYLE__LINESTARTARROWLENGTH);
        return (prop == null) ? null : DecorationSize.fromNativeId(prop.getPropertyValue());
    }

    public void setLineHeadLength(DecorationSize decoSize){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        setEscherProperty(opt, EscherProperties.LINESTYLE__LINESTARTARROWLENGTH, decoSize == null ? -1 : decoSize.nativeId);
    }

    public DecorationShape getLineTailDecoration(){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherProperties.LINESTYLE__LINEENDARROWHEAD);
        return (prop == null) ? null : DecorationShape.fromNativeId(prop.getPropertyValue());
    }

    public void setLineTailDecoration(DecorationShape decoShape){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        setEscherProperty(opt, EscherProperties.LINESTYLE__LINEENDARROWHEAD, decoShape == null ? -1 : decoShape.nativeId);
    }

    public DecorationSize getLineTailWidth(){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherProperties.LINESTYLE__LINEENDARROWWIDTH);
        return (prop == null) ? null : DecorationSize.fromNativeId(prop.getPropertyValue());
    }

    public void setLineTailWidth(DecorationSize decoSize){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        setEscherProperty(opt, EscherProperties.LINESTYLE__LINEENDARROWWIDTH, decoSize == null ? -1 : decoSize.nativeId);
    }

    public DecorationSize getLineTailLength(){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherProperties.LINESTYLE__LINEENDARROWLENGTH);
        return (prop == null) ? null : DecorationSize.fromNativeId(prop.getPropertyValue());
    }

    public void setLineTailLength(DecorationSize decoSize){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        setEscherProperty(opt, EscherProperties.LINESTYLE__LINEENDARROWLENGTH, decoSize == null ? -1 : decoSize.nativeId);
    }



    @Override
    public LineDecoration getLineDecoration() {
        return new LineDecoration() {

            @Override
            public DecorationShape getHeadShape() {
                return HSLFSimpleShape.this.getLineHeadDecoration();
            }

            @Override
            public DecorationSize getHeadWidth() {
                return HSLFSimpleShape.this.getLineHeadWidth();
            }

            @Override
            public DecorationSize getHeadLength() {
                return HSLFSimpleShape.this.getLineHeadLength();
            }

            @Override
            public DecorationShape getTailShape() {
                return HSLFSimpleShape.this.getLineTailDecoration();
            }

            @Override
            public DecorationSize getTailWidth() {
                return HSLFSimpleShape.this.getLineTailWidth();
            }

            @Override
            public DecorationSize getTailLength() {
                return HSLFSimpleShape.this.getLineTailLength();
            }
        };
    }

    @Override
    public Placeholder getPlaceholder() {
        List<? extends Record> clRecords = getClientRecords();
        if (clRecords == null) {
            return null;
        }
        int phSource;
        HSLFSheet sheet = getSheet();
        if (sheet instanceof HSLFSlideMaster) {
            phSource = 1;
        } else if (sheet instanceof HSLFNotes) {
            phSource = 2;
        } else if (sheet instanceof MasterSheet) {
            // notes master aren't yet supported ...
            phSource = 3;
        } else {
            phSource = 0;
        }
        
        for (Record r : clRecords) {
            int phId;
            if (r instanceof OEPlaceholderAtom) {
                phId = ((OEPlaceholderAtom)r).getPlaceholderId();
            } else if (r instanceof RoundTripHFPlaceholder12) {
                //special case for files saved in Office 2007
                phId = ((RoundTripHFPlaceholder12)r).getPlaceholderId();
            } else {
                continue;
            }

            switch (phSource) {
            case 0:
                return Placeholder.lookupNativeSlide(phId);
            default:
            case 1:
                return Placeholder.lookupNativeSlideMaster(phId);
            case 2:
                return Placeholder.lookupNativeNotes(phId);
            case 3:
                return Placeholder.lookupNativeNotesMaster(phId);
            }
        }

        return null;
    }

    @Override
    public void setPlaceholder(Placeholder placeholder) {
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        int flags = spRecord.getFlags();
        if (placeholder == null) {
            flags ^= EscherSpRecord.FLAG_HAVEMASTER;
        } else {
            flags |= EscherSpRecord.FLAG_HAVEANCHOR | EscherSpRecord.FLAG_HAVEMASTER;
        }
        spRecord.setFlags(flags);

        // Placeholders can't be grouped
        setEscherProperty(EscherProperties.PROTECTION__LOCKAGAINSTGROUPING, (placeholder == null ? -1 : 262144));

        HSLFEscherClientDataRecord clientData = getClientData(false);
        if (placeholder == null) {
            if (clientData != null) {
                clientData.removeChild(OEPlaceholderAtom.class);
                clientData.removeChild(RoundTripHFPlaceholder12.class);
                // remove client data if the placeholder was the only child to be carried
                if (clientData.getChildRecords().isEmpty()) {
                    getSpContainer().removeChildRecord(clientData);
                }
            }
            return;
        }

        if (clientData == null) {
            clientData = getClientData(true);
        }

        // OEPlaceholderAtom tells powerpoint that this shape is a placeholder
        OEPlaceholderAtom oep = null;
        RoundTripHFPlaceholder12 rtp = null;
        for (Record r : clientData.getHSLFChildRecords()) {
            if (r instanceof OEPlaceholderAtom) {
                oep = (OEPlaceholderAtom)r;
                break;
            }
            if (r instanceof RoundTripHFPlaceholder12) {
                rtp = (RoundTripHFPlaceholder12)r;
                break;
            }
        }

        /**
         * Extract from MSDN:
         *
         * There is a special case when the placeholder does not have a position in the layout.
         * This occurs when the user has moved the placeholder from its original position.
         * In this case the placeholder ID is -1.
         */
        byte phId;
        HSLFSheet sheet = getSheet();
        // TODO: implement/switch NotesMaster
        if (sheet instanceof HSLFSlideMaster) {
            phId = (byte)placeholder.nativeSlideMasterId;
        } else if (sheet instanceof HSLFNotes) {
            phId = (byte)placeholder.nativeNotesId;
        } else {
            phId = (byte)placeholder.nativeSlideId;
        }

        if (phId == -2) {
            throw new HSLFException("Placeholder "+placeholder.name()+" not supported for this sheet type ("+sheet.getClass()+")");
        }

        switch (placeholder) {
            case HEADER:
            case FOOTER:
                if (rtp == null) {
                    rtp = new RoundTripHFPlaceholder12();
                    rtp.setPlaceholderId(phId);
                    clientData.addChild(rtp);
                }
                if (oep != null) {
                    clientData.removeChild(OEPlaceholderAtom.class);
                }
                break;
            default:
                if (rtp != null) {
                    clientData.removeChild(RoundTripHFPlaceholder12.class);
                }
                if (oep == null) {
                    oep = new OEPlaceholderAtom();
                    oep.setPlaceholderSize((byte)OEPlaceholderAtom.PLACEHOLDER_FULLSIZE);
                    // TODO: placement id only "SHOULD" be unique ... check other placeholders on sheet for unique id
                    oep.setPlacementId(-1);
                    oep.setPlaceholderId(phId);
                    clientData.addChild(oep);
                }
                break;
        }
    }


    @Override
    public void setStrokeStyle(Object... styles) {
        if (styles.length == 0) {
            // remove stroke
            setLineColor(null);
            return;
        }

        // TODO: handle PaintStyle
        for (Object st : styles) {
            if (st instanceof Number) {
                setLineWidth(((Number)st).doubleValue());
            } else if (st instanceof LineCap) {
                setLineCap((LineCap)st);
            } else if (st instanceof LineDash) {
                setLineDash((LineDash)st);
            } else if (st instanceof LineCompound) {
                setLineCompound((LineCompound)st);
            } else if (st instanceof Color) {
                setLineColor((Color)st);
            }
        }
    }

    @Override
    public HSLFHyperlink getHyperlink(){
        return _hyperlink;
    }
    
    @Override
    public HSLFHyperlink createHyperlink() {
        if (_hyperlink == null) {
            _hyperlink = HSLFHyperlink.createHyperlink(this);
        }
        return _hyperlink;
    }
    
    /**
     * Sets the hyperlink - used when the document is parsed
     *
     * @param link the hyperlink
     */
    protected void setHyperlink(HSLFHyperlink link) {
        _hyperlink = link;
    }
}