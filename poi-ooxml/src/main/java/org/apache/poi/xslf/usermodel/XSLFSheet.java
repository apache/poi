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

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Beta;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.officeDocument.x2006.relationships.STRelationshipId;
import org.openxmlformats.schemas.presentationml.x2006.main.CTCommonSlideData;
import org.openxmlformats.schemas.presentationml.x2006.main.CTConnector;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPicture;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;

import javax.xml.namespace.QName;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Beta
public abstract class XSLFSheet extends POIXMLDocumentPart implements XSLFShapeContainer {
    private XSLFCommonSlideData _commonSlideData;
    private XSLFDrawing _drawing;
    private List<XSLFShape> _shapes;
    private CTGroupShape _spTree;

    private List<XSLFTextShape>_placeholders;
    private Map<Integer, XSLFSimpleShape> _placeholderByIdMap;
    private Map<Integer, XSLFSimpleShape> _placeholderByTypeMap;

    public XSLFSheet() {
        super();
    }

    public XSLFSheet(PackagePart part, PackageRelationship rel){
        super(part, rel);
    }

    /**
     *
     * @return the XMLSlideShow this sheet belongs to
     */
    public XMLSlideShow getSlideShow() {
        POIXMLDocumentPart p = getParent();
        while(p != null) {
            if(p instanceof XMLSlideShow){
                return (XMLSlideShow)p;
            }
            p = p.getParent();
        }
        throw new IllegalStateException("SlideShow was not found");
    }

    protected List<XSLFShape> buildShapes(CTGroupShape spTree){
        List<XSLFShape> shapes = new ArrayList<XSLFShape>();
        for(XmlObject ch : spTree.selectPath("*")){
            if(ch instanceof CTShape){ // simple shape
                XSLFAutoShape shape = XSLFAutoShape.create((CTShape)ch, this);
                shapes.add(shape);
            } else if (ch instanceof CTGroupShape){
                shapes.add(new XSLFGroupShape((CTGroupShape)ch, this));
            } else if (ch instanceof CTConnector){
                shapes.add(new XSLFConnectorShape((CTConnector)ch, this));
            } else if (ch instanceof CTPicture){
                shapes.add(new XSLFPictureShape((CTPicture)ch, this));
            } else if (ch instanceof CTGraphicalObjectFrame){
                XSLFGraphicFrame shape = XSLFGraphicFrame.create((CTGraphicalObjectFrame)ch, this);
                shapes.add(shape);
            }
        }
        return shapes;
    }

    /**
     * @return top-level Xml bean representing this sheet
     */
    public abstract XmlObject getXmlObject();

    @Internal
    public XSLFCommonSlideData getCommonSlideData() {
       return _commonSlideData;
    }

    protected void setCommonSlideData(CTCommonSlideData data) {
       if(data == null) {
          _commonSlideData = null;
       } else {
          _commonSlideData = new XSLFCommonSlideData(data);
       }
    }

    private XSLFDrawing getDrawing(){
        if(_drawing == null) {
            _drawing = new XSLFDrawing(this, getSpTree());
        }
        return _drawing;
    }

    private List<XSLFShape> getShapeList(){
        if(_shapes == null){
            _shapes = buildShapes(getSpTree());
        }
        return _shapes;
    }

    // shape factory methods

    public XSLFAutoShape createAutoShape(){
        List<XSLFShape> shapes = getShapeList();
        XSLFAutoShape sh = getDrawing().createAutoShape();
        shapes.add(sh);
        return sh;
    }

    public XSLFFreeformShape createFreeform(){
        List<XSLFShape> shapes = getShapeList();
        XSLFFreeformShape sh = getDrawing().createFreeform();
        shapes.add(sh);
        return sh;
    }

    public XSLFTextBox createTextBox(){
        List<XSLFShape> shapes = getShapeList();
        XSLFTextBox sh = getDrawing().createTextBox();
        shapes.add(sh);
        return sh;
    }

    public XSLFConnectorShape createConnector(){
        List<XSLFShape> shapes = getShapeList();
        XSLFConnectorShape sh = getDrawing().createConnector();
        shapes.add(sh);
        return sh;
    }

    public XSLFGroupShape createGroup(){
        List<XSLFShape> shapes = getShapeList();
        XSLFGroupShape sh = getDrawing().createGroup();
        shapes.add(sh);
        return sh;
    }

    public XSLFPictureShape createPicture(int pictureIndex){
        List<PackagePart>  pics = getPackagePart().getPackage()
                .getPartsByName(Pattern.compile("/ppt/media/image" + (pictureIndex + 1) + ".*?"));

        if(pics.size() == 0) {
            throw new IllegalArgumentException("Picture with index=" + pictureIndex + " was not found");
        }

        PackagePart pic = pics.get(0);

        PackageRelationship rel = getPackagePart().addRelationship(
                pic.getPartName(), TargetMode.INTERNAL, XSLFRelation.IMAGES.getRelation());
        addRelation(rel.getId(), new XSLFPictureData(pic, rel));

        XSLFPictureShape sh = getDrawing().createPicture(rel.getId());
        sh.resize();

        getShapeList().add(sh);
        return sh;
    }

    public XSLFTable createTable(){
        List<XSLFShape> shapes = getShapeList();
        XSLFTable sh = getDrawing().createTable();
        shapes.add(sh);
        return sh;
    }

    /**
     * Returns an array containing all of the shapes in this sheet
     *
     * @return an array of all shapes in this sheet
     */
    public XSLFShape[] getShapes(){
        return getShapeList().toArray(new XSLFShape[_shapes.size()]);
    }

    /**
     * Returns an iterator over the shapes in this sheet
     *
     * @return an iterator over the shapes in this sheet
     */
    public Iterator<XSLFShape> iterator(){
        return getShapeList().iterator();
    }

    /**
     * Removes the specified shape from this sheet, if it is present
     * (optional operation).  If this sheet does not contain the element,
     * it is unchanged.
     *
     * @param xShape shape to be removed from this sheet, if present
     * @return <tt>true</tt> if this sheet contained the specified element
     * @throws IllegalArgumentException if the type of the specified shape
     *         is incompatible with this sheet (optional)
     */
    public boolean removeShape(XSLFShape xShape) {
        XmlObject obj = xShape.getXmlObject();
        CTGroupShape spTree = getSpTree();
        if(obj instanceof CTShape){
            spTree.getSpList().remove(obj);
        } else if (obj instanceof CTGroupShape){
            spTree.getGrpSpList().remove(obj);
        } else if (obj instanceof CTConnector){
            spTree.getCxnSpList().remove(obj);
        } else {
            throw new IllegalArgumentException("Unsupported shape: " + xShape);
        }
        return getShapeList().remove(xShape);
    }

    /**
     * Removes all of the elements from this container (optional operation).
     * The container will be empty after this call returns.
     */
    public void clear() {
        for(XSLFShape shape : getShapes()){
            removeShape(shape);
        }
    }

    protected abstract String getRootElementName();

    protected CTGroupShape getSpTree(){
        if(_spTree == null) {
            XmlObject root = getXmlObject();
            XmlObject[] sp = root.selectPath(
                    "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' .//*/p:spTree");
            if(sp.length == 0) throw new IllegalStateException("CTGroupShape was not found");
            _spTree = (CTGroupShape)sp[0];
        }
        return _spTree;
    }

    protected final void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);

        Map<String, String> map = new HashMap<String, String>();
        map.put(STRelationshipId.type.getName().getNamespaceURI(), "r");
        map.put("http://schemas.openxmlformats.org/drawingml/2006/main", "a");
        map.put("http://schemas.openxmlformats.org/presentationml/2006/main", "p");
        xmlOptions.setSaveSuggestedPrefixes(map);
        String docName = getRootElementName();
        if(docName != null) {
            xmlOptions.setSaveSyntheticDocumentElement(
                    new QName("http://schemas.openxmlformats.org/presentationml/2006/main", docName));
        }

        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        getXmlObject().save(out, xmlOptions);
        out.close();
    }

    /**
     * Set the contents of this sheet to be a copy of the source sheet.
     * This method erases any existing shapes and replaces them with
     * object from the source sheet.
     *
     * @param src the source sheet to copy data from
     * @return modified 'this'
     */
    public XSLFSheet importContent(XSLFSheet src){
        _shapes = null;
        _spTree = null;
        _drawing = null;
        _spTree = null;
        _placeholders = null;

        // first copy the source xml
        getSpTree().set(src.getSpTree());

        // recursively update each shape
        List<XSLFShape> tgtShapes = getShapeList();
        List<XSLFShape> srcShapes = src.getShapeList();
        for(int i = 0; i < tgtShapes.size(); i++){
            XSLFShape s1 = srcShapes.get(i);
            XSLFShape s2 = tgtShapes.get(i);

            s2.copy(s1);
        }
        return this;
    }

    /**
     * Append content to this sheet.
     *
     * @param src the source sheet
     * @return modified <code>this</code>.
     */
    public XSLFSheet appendContent(XSLFSheet src){
        CTGroupShape spTree = getSpTree();
        int numShapes = getShapeList().size();

        CTGroupShape srcTree = src.getSpTree();
        for(XmlObject ch : srcTree.selectPath("*")){
            if(ch instanceof CTShape){ // simple shape
                spTree.addNewSp().set(ch);
            } else if (ch instanceof CTGroupShape){
                spTree.addNewGrpSp().set(ch);
            } else if (ch instanceof CTConnector){
                spTree.addNewCxnSp().set(ch);
            } else if (ch instanceof CTPicture){
                spTree.addNewPic().set(ch);
            } else if (ch instanceof CTGraphicalObjectFrame){
                spTree.addNewGraphicFrame().set(ch);
            }
        }

        _shapes = null;
        _spTree = null;
        _drawing = null;
        _spTree = null;
        _placeholders = null;

        // recursively update each shape
        List<XSLFShape> tgtShapes = getShapeList();
        List<XSLFShape> srcShapes = src.getShapeList();
        for(int i = 0; i < srcShapes.size(); i++){
            XSLFShape s1 = srcShapes.get(i);
            XSLFShape s2 = tgtShapes.get(numShapes + i);

            s2.copy(s1);
        }
        return this;
    }

   /**
     * @return theme (shared styles) associated with this theme.
     *  By default returns <code>null</code> which means that this sheet is theme-less.
     *  Sheets that support the notion of themes (slides, masters, layouts, etc.) should override this
     *  method and return the corresponding package part.
     */
    XSLFTheme getTheme(){
    	return null;
    }

    /**
     *
     * @return master of this sheet.
     */
    public abstract XSLFSheet getMasterSheet();

    protected XSLFTextShape getTextShapeByType(Placeholder type){
        for(XSLFShape shape : this.getShapes()){
            if(shape instanceof XSLFTextShape) {
               XSLFTextShape txt = (XSLFTextShape)shape;
                if(txt.getTextType() == type) {
                    return txt;
                }
            }
        }
        return null;
    }

    XSLFSimpleShape getPlaceholder(CTPlaceholder ph) {
        XSLFSimpleShape shape = null;
        if(ph.isSetIdx()) shape = getPlaceholderById((int)ph.getIdx());

        if (shape == null && ph.isSetType()) {
            shape = getPlaceholderByType(ph.getType().intValue());
        }
        return shape;
    }

    void initPlaceholders() {
        if(_placeholders == null) {
            _placeholders = new ArrayList<XSLFTextShape>();
            _placeholderByIdMap = new HashMap<Integer, XSLFSimpleShape>();
            _placeholderByTypeMap = new HashMap<Integer, XSLFSimpleShape>();

            for(XSLFShape sh : getShapes()){
                if(sh instanceof XSLFTextShape){
                    XSLFTextShape sShape = (XSLFTextShape)sh;
                    CTPlaceholder ph = sShape.getCTPlaceholder();
                    if(ph != null) {
                        _placeholders.add(sShape);
                        if(ph.isSetIdx()) {
                            int idx = (int)ph.getIdx();
                            _placeholderByIdMap.put(idx, sShape);
                        }
                        if(ph.isSetType()){
                            _placeholderByTypeMap.put(ph.getType().intValue(), sShape);
                        }
                    }
                }
            }
        }
    }

    XSLFSimpleShape getPlaceholderById(int id) {
        initPlaceholders();
        return _placeholderByIdMap.get(id);
    }

    XSLFSimpleShape getPlaceholderByType(int ordinal) {
        initPlaceholders();
        return _placeholderByTypeMap.get(ordinal);
    }

    /**
     *
     * @param idx 0-based index of a placeholder in the sheet
     * @return placeholder
     */
    public XSLFTextShape getPlaceholder(int idx) {
        initPlaceholders();
        return _placeholders.get(idx);
    }

    /**
     *
     * @return all placeholder shapes in this sheet
     */
    public XSLFTextShape[] getPlaceholders() {
        initPlaceholders();
        return _placeholders.toArray(new XSLFTextShape[_placeholders.size()]);
    }

    /**
     * Checks if this <code>sheet</code> displays the specified shape.
     *
     * Subclasses can override it and skip certain shapes from drawings,
     * for instance, slide masters and layouts don't display placeholders
     */
    protected boolean canDraw(XSLFShape shape){
        return true;
    }

    /**
     *
     * @return whether shapes on the master sheet should be shown. By default master graphics is turned off.
     * Sheets that support the notion of master (slide, slideLayout) should override it and
     * check this setting in the sheet XML
     */
    public boolean getFollowMasterGraphics(){
        return false;
    }

    /**
     *
     * @return  background for this sheet
     */
    public XSLFBackground getBackground() {
        return null;
    }

    /**
     * Render this sheet into the supplied graphics object
     *
     * @param graphics
     */
    public void draw(Graphics2D graphics){
        XSLFSheet master = getMasterSheet();
        if(getFollowMasterGraphics() && master != null) master.draw(graphics);

        graphics.setRenderingHint(XSLFRenderingHint.GROUP_TRANSFORM, new AffineTransform());
        for(XSLFShape shape : getShapeList()) {
            if(!canDraw(shape)) continue;

        	// remember the initial transform and restore it after we are done with drawing
        	AffineTransform at = graphics.getTransform();

            // concrete implementations can make sense of this hint,
            // for example PSGraphics2D or PDFGraphics2D would call gsave() / grestore
            graphics.setRenderingHint(XSLFRenderingHint.GSAVE, true);

            // apply rotation and flipping
            shape.applyTransform(graphics);
            // draw stuff
            shape.draw(graphics);

            // restore the coordinate system
            graphics.setTransform(at);

            graphics.setRenderingHint(XSLFRenderingHint.GRESTORE, true);

        }
    }

    /**
     * Import a picture data from another document.
     *
     * @param blipId        ID of the package relationship to retrieve.
     * @param packagePart   package part containing the data to import
     * @return ID of the created relationship
     */
    String importBlip(String blipId, PackagePart packagePart) {
        PackageRelationship blipRel = packagePart.getRelationship(blipId);
        PackagePart blipPart;
        try {
            blipPart = packagePart.getRelatedPart(blipRel);
        } catch (InvalidFormatException e){
            throw new POIXMLException(e);
        }
        XSLFPictureData data = new XSLFPictureData(blipPart, null);

        XMLSlideShow ppt = getSlideShow();
        int pictureIdx = ppt.addPicture(data.getData(), data.getPictureType());
        PackagePart pic = ppt.getAllPictures().get(pictureIdx).getPackagePart();

        PackageRelationship rel = getPackagePart().addRelationship(
                pic.getPartName(), TargetMode.INTERNAL, blipRel.getRelationshipType());
        addRelation(rel.getId(), new XSLFPictureData(pic, rel));

        return rel.getId();
    }

    /**
     * Import a package part into this sheet.
     */
    PackagePart importPart(PackageRelationship srcRel, PackagePart srcPafrt) {

        OPCPackage pkg = getPackagePart().getPackage();
        if(!pkg.containPart(srcPafrt.getPartName())){
            PackageRelationship rel = getPackagePart().addRelationship(
                    srcPafrt.getPartName(), TargetMode.INTERNAL, srcRel.getRelationshipType());

            PackagePart part = pkg.createPart(srcPafrt.getPartName(), srcPafrt.getContentType());
            OutputStream out = part.getOutputStream();
            try {
                InputStream is = srcPafrt.getInputStream();
                IOUtils.copy(is, out);
                out.close();
            } catch (IOException e){
                throw new POIXMLException(e);
            }
            return part;
        }  else {
            // already exists
            return pkg.getPart(srcPafrt.getPartName());
        }
    }
}