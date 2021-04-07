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

package org.apache.poi.poifs.crypt.dsig;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.xml.namespace.QName;

import com.microsoft.schemas.office.office.CTSignatureLine;
import com.microsoft.schemas.vml.CTGroup;
import com.microsoft.schemas.vml.CTImageData;
import com.microsoft.schemas.vml.CTShape;
import com.microsoft.schemas.vml.STExt;
import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.hpsf.ClassID;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.sl.draw.DrawPictureShape;
import org.apache.poi.sl.draw.ImageRenderer;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STTrueFalse;

/**
 * Base class for SignatureLines (XSSF,XWPF only)
 */
public abstract class SignatureLine {

    private static final String MS_OFFICE_URN = "urn:schemas-microsoft-com:office:office";
    protected static final QName QNAME_SIGNATURE_LINE = new QName(MS_OFFICE_URN, "signatureline");


    private ClassID setupId;
    private Boolean allowComments;
    private String signingInstructions = "Before signing the document, verify that the content you are signing is correct.";
    private String suggestedSigner;
    private String suggestedSigner2;
    private String suggestedSignerEmail;
    private String caption;
    private String invalidStamp = "invalid";
    private byte[] plainSignature;
    private String contentType;

    private CTShape signatureShape;

    public ClassID getSetupId() {
        return setupId;
    }

    public void setSetupId(ClassID setupId) {
        this.setupId = setupId;
    }

    public Boolean getAllowComments() {
        return allowComments;
    }

    public void setAllowComments(Boolean allowComments) {
        this.allowComments = allowComments;
    }

    public String getSigningInstructions() {
        return signingInstructions;
    }

    public void setSigningInstructions(String signingInstructions) {
        this.signingInstructions = signingInstructions;
    }

    public String getSuggestedSigner() {
        return suggestedSigner;
    }

    public void setSuggestedSigner(String suggestedSigner) {
        this.suggestedSigner = suggestedSigner;
    }

    public String getSuggestedSigner2() {
        return suggestedSigner2;
    }

    public void setSuggestedSigner2(String suggestedSigner2) {
        this.suggestedSigner2 = suggestedSigner2;
    }

    public String getSuggestedSignerEmail() {
        return suggestedSignerEmail;
    }

    public void setSuggestedSignerEmail(String suggestedSignerEmail) {
        this.suggestedSignerEmail = suggestedSignerEmail;
    }

    /**
     * The default caption
     * @return "[suggestedSigner] \n [suggestedSigner2] \n [suggestedSignerEmail]"
     */
    public String getDefaultCaption() {
        return suggestedSigner+"\n"+suggestedSigner2+"\n"+suggestedSignerEmail;
    }

    public String getCaption() {
        return caption;
    }

    /**
     * Set the caption - use maximum of three lines separated by "\n".
     * Defaults to {@link #getDefaultCaption()}
     * @param caption the signature caption
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getInvalidStamp() {
        return invalidStamp;
    }

    /**
     * Sets the text stamped over the signature image when the document got tampered with
     * @param invalidStamp the invalid stamp text
     */
    public void setInvalidStamp(String invalidStamp) {
        this.invalidStamp = invalidStamp;
    }

    /** the plain signature without caption */
    public byte[] getPlainSignature() {
        return plainSignature;
    }

    /**
     * Sets the plain signature
     * supported formats are PNG,GIF,JPEG,(SVG),EMF,WMF.
     * for SVG,EMF,WMF poi-scratchpad needs to be in the class-/modulepath
     *
     * @param plainSignature the plain signature - if {@code null}, the signature is not rendered
     *                       and only the caption is visible
     */
    public void setPlainSignature(byte[] plainSignature) {
        this.plainSignature = plainSignature;
        this.contentType = null;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public CTShape getSignatureShape() {
        return signatureShape;
    }

    public void setSignatureShape(CTShape signatureShape) {
        this.signatureShape = signatureShape;
    }

    public void setSignatureShape(CTSignatureLine signatureLine) {
        XmlCursor cur = signatureLine.newCursor();
        cur.toParent();
        this.signatureShape = (CTShape)cur.getObject();
        cur.dispose();
    }

    public void updateSignatureConfig(SignatureConfig config) throws IOException {
        if (plainSignature == null) {
            throw new IllegalStateException("Plain signature not initialized");
        }

        if (contentType == null) {
            determineContentType();
        }

        byte[] signValid = generateImage(true, false);
        byte[] signInvalid = generateImage(true, true);

        config.setSignatureImageSetupId(getSetupId());
        config.setSignatureImage(plainPng());
        config.setSignatureImageValid(signValid);
        config.setSignatureImageInvalid(signInvalid);
    }

    protected void parse() {
        if (signatureShape == null) {
            return;
        }

        CTSignatureLine signatureLine = signatureShape.getSignaturelineArray(0);

        setSetupId(new ClassID(signatureLine.getId()));
        setAllowComments(signatureLine.isSetAllowcomments() ? STTrueFalse.TRUE.equals(signatureLine.getAllowcomments()) : null);
        setSuggestedSigner(signatureLine.getSuggestedsigner());
        setSuggestedSigner2(signatureLine.getSuggestedsigner2());
        setSuggestedSignerEmail(signatureLine.getSuggestedsigneremail());
        XmlCursor cur = signatureLine.newCursor();
        try {
            // the signinginstructions are actually qualified, but our schema version is too old
            setSigningInstructions(cur.getAttributeText(new QName(MS_OFFICE_URN, "signinginstructions")));
        } finally {
            cur.dispose();
        }
    }

    protected interface AddPictureData {
        /**
         * Add picture data to the document
         * @param imageData the image bytes
         * @param pictureType the picture type - typically PNG
         * @return the relation id of the newly add picture
         */
        String addPictureData(byte[] imageData, PictureType pictureType) throws InvalidFormatException;
    }

    protected abstract void setRelationId(CTImageData imageData, String relId);

    protected void add(XmlObject signatureContainer, AddPictureData addPictureData) {
        byte[] inputImage;
        try {
            inputImage = generateImage(false, false);

            CTGroup grp = CTGroup.Factory.newInstance();
            grp.addNewShape();

            XmlCursor contCur = signatureContainer.newCursor();
            contCur.toEndToken();
            XmlCursor otherC = grp.newCursor();
            otherC.copyXmlContents(contCur);
            otherC.dispose();
            contCur.toPrevSibling();
            signatureShape = (CTShape)contCur.getObject();
            contCur.dispose();

            signatureShape.setAlt("Microsoft Office Signature Line...");
            signatureShape.setStyle("width:191.95pt;height:96.05pt");
//            signatureShape.setStyle("position:absolute;margin-left:100.8pt;margin-top:43.2pt;width:192pt;height:96pt;z-index:1");
            signatureShape.setType("rect");

            String relationId = addPictureData.addPictureData(inputImage, PictureType.PNG);
            CTImageData imgData = signatureShape.addNewImagedata();
            setRelationId(imgData, relationId);
            imgData.setTitle("");

            CTSignatureLine xsl = signatureShape.addNewSignatureline();
            if (suggestedSigner != null) {
                xsl.setSuggestedsigner(suggestedSigner);
            }
            if (suggestedSigner2 != null) {
                xsl.setSuggestedsigner2(suggestedSigner2);
            }
            if (suggestedSignerEmail != null) {
                xsl.setSuggestedsigneremail(suggestedSignerEmail);
            }
            if (setupId == null) {
                setupId = new ClassID("{"+ UUID.randomUUID().toString()+"}");
            }
            xsl.setId(setupId.toString());
            xsl.setAllowcomments(STTrueFalse.T);
            xsl.setIssignatureline(STTrueFalse.T);
            xsl.setProvid("{00000000-0000-0000-0000-000000000000}");
            xsl.setExt(STExt.EDIT);
            xsl.setSigninginstructionsset(STTrueFalse.T);
            XmlCursor cur = xsl.newCursor();
            cur.setAttributeText(new QName(MS_OFFICE_URN, "signinginstructions"), signingInstructions);
            cur.dispose();
        } catch (IOException | InvalidFormatException e) {
            // shouldn't happen ...
            throw new POIXMLException("Can't generate signature line image", e);
        }
    }

    protected void update() {

    }

    /**
     * Word and Excel a regenerating the valid and invalid signature line based on the
     * plain signature. Both are picky about the input format.
     * Especially EMF images need to a specific device dimension (dpi)
     * instead of fiddling around with the input image, we generate/register a bitmap image instead
     *
     * @return the converted PNG image
     */
    protected byte[] plainPng() throws IOException {
        byte[] plain = getPlainSignature();
        PictureType pictureType = PictureType.valueOf(FileMagic.valueOf(plain));
        if (pictureType == PictureType.UNKNOWN) {
            throw new IllegalArgumentException("Unsupported picture format");
        }

        ImageRenderer rnd = DrawPictureShape.getImageRenderer(null, pictureType.contentType);
        if (rnd == null) {
            throw new UnsupportedOperationException(pictureType + " can't be rendered - did you provide poi-scratchpad and its dependencies (batik et al.)");
        }
        rnd.loadImage(getPlainSignature(), pictureType.contentType);

        Dimension2D dim = rnd.getDimension();
        int defaultWidth = 300;
        int defaultHeight = (int)(defaultWidth * dim.getHeight() / dim.getWidth());
        BufferedImage bi = new BufferedImage(defaultWidth, defaultHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gfx = bi.createGraphics();
        gfx.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        gfx.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gfx.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        rnd.drawImage(gfx, new Rectangle2D.Double(0, 0, defaultWidth, defaultHeight));
        gfx.dispose();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bi, "PNG", bos);
        return bos.toByteArray();
    }


    /**
     * Generate the image for a signature line
     * @param showSignature show signature image - use {@code false} for placeholder images in to-be-signed documents
     * @param showInvalidStamp print invalid stamp over the signature
     * @return the signature image in PNG format as byte array
     */
    protected byte[] generateImage(boolean showSignature, boolean showInvalidStamp) throws IOException {
        BufferedImage bi = new BufferedImage(400, 150, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gfx = bi.createGraphics();
        gfx.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        gfx.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gfx.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String markX = "X\n";
        String lineX = (new String(new char[500]).replace("\0", " ")) +"\n";
        String cap = (getCaption() == null) ? getDefaultCaption() : getCaption();
        String text = markX+lineX+cap.replaceAll("(?m)^", "    ");

        AttributedString as = new AttributedString(text);
        as.addAttribute(TextAttribute.FAMILY, Font.SANS_SERIF);
        as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, markX.length(), text.indexOf('\n', markX.length()));

        as.addAttribute(TextAttribute.SIZE, 15, 0, markX.length());
        as.addAttribute(TextAttribute.SIZE, 12, markX.length(), text.length());

        gfx.setColor(Color.BLACK);

        AttributedCharacterIterator chIter = as.getIterator();
        FontRenderContext frc = gfx.getFontRenderContext();
        LineBreakMeasurer measurer = new LineBreakMeasurer(chIter, frc);
        float y = 80, x = 5;
        for (int lineNr = 0; measurer.getPosition() < chIter.getEndIndex(); lineNr++) {
            int mpos = measurer.getPosition();
            int limit = text.indexOf('\n', mpos);
            limit = (limit == -1) ? text.length() : limit+1;
            TextLayout textLayout = measurer.nextLayout(bi.getWidth()-10f, limit, false);
            if (lineNr != 1) {
                y += textLayout.getAscent();
            }
            textLayout.draw(gfx, x, y);
            y += textLayout.getDescent() + textLayout.getLeading();
        }

        if (showSignature && plainSignature != null && contentType != null) {

            ImageRenderer renderer = DrawPictureShape.getImageRenderer(gfx, contentType);

            renderer.loadImage(plainSignature, contentType);

            double targetX = 10;
            double targetY = 100;
            double targetWidth = bi.getWidth() - targetX;
            double targetHeight = targetY - 5;
            Dimension2D dim = renderer.getDimension();
            double scale = Math.min(targetWidth / dim.getWidth(), targetHeight / dim.getHeight());
            double effWidth = dim.getWidth() * scale;
            double effHeight = dim.getHeight() * scale;

            renderer.drawImage(gfx, new Rectangle2D.Double(targetX + ((bi.getWidth() - effWidth) / 2), targetY - effHeight, effWidth, effHeight));
        }

        if (showInvalidStamp && invalidStamp != null && !invalidStamp.isEmpty()) {
            gfx.setFont(new Font("Lucida Bright", Font.ITALIC, 60));
            gfx.rotate(Math.toRadians(-15), bi.getWidth()/2., bi.getHeight()/2.);
            TextLayout tl = new TextLayout(invalidStamp, gfx.getFont(), gfx.getFontRenderContext());
            Rectangle2D bounds = tl.getBounds();
            x = (float)((bi.getWidth()-bounds.getWidth())/2 - bounds.getX());
            y = (float)((bi.getHeight()-bounds.getHeight())/2 - bounds.getY());
            Shape outline = tl.getOutline(AffineTransform.getTranslateInstance(x+2, y+1));
            gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            gfx.setPaint(Color.RED);
            gfx.draw(outline);
            gfx.setPaint(new GradientPaint(0, 0, Color.RED, 30, 20, new Color(128, 128, 255), true));
            tl.draw(gfx, x, y);
        }

        gfx.dispose();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bi, "PNG", bos);
        return bos.toByteArray();
    }

    private void determineContentType() {
        FileMagic fm = FileMagic.valueOf(plainSignature);
        PictureType type = PictureType.valueOf(fm);
        if (type == PictureType.UNKNOWN) {
            throw new IllegalArgumentException("unknown image type");
        }
        contentType = type.contentType;
    }

}
