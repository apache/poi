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

package org.apache.poi.xslf.draw;

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.ext.awt.image.renderable.ClipRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.draw.ImageRenderer;
import org.apache.poi.sl.usermodel.PictureData;
import org.w3c.dom.Document;

public class SVGImageRenderer implements ImageRenderer {
    private final GVTBuilder builder = new GVTBuilder();
    private final BridgeContext context;
    private final SAXSVGDocumentFactory svgFact;
    private GraphicsNode svgRoot;
    private double alpha = 1.0;

    public SVGImageRenderer() {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        // TOOO: tell the batik guys to use secure parsing feature
        svgFact = new SAXSVGDocumentFactory(parser);

        UserAgent agent = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(agent);
        context = new BridgeContext(agent, loader);
        context.setDynamic(true);
    }


    @Override
    public void loadImage(InputStream data, String contentType) throws IOException {
        Document document = svgFact.createDocument("", data);
        svgRoot = builder.build(context, document);
    }

    @Override
    public void loadImage(byte[] data, String contentType) throws IOException {
        loadImage(new ByteArrayInputStream(data), contentType);
    }

    @Override
    public Rectangle2D getBounds() {
        return svgRoot.getPrimitiveBounds();
    }

    @Override
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    @Override
    public BufferedImage getImage() {
        return getImage(getDimension());
    }

    @Override
    public BufferedImage getImage(Dimension2D dim) {
        BufferedImage bi = new BufferedImage((int)dim.getWidth(), (int)dim.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) bi.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHintsKeyExt.KEY_BUFFERED_IMAGE, new WeakReference(bi));
        Dimension2D dimSVG = getDimension();

        double scaleX = dim.getWidth() / dimSVG.getWidth();
        double scaleY = dim.getHeight() / dimSVG.getHeight();
        g2d.scale(scaleX, scaleY);

        svgRoot.paint(g2d);
        g2d.dispose();

        return bi;
    }

    @Override
    public boolean drawImage(Graphics2D graphics, Rectangle2D anchor) {
        return drawImage(graphics, anchor, null);
    }

    @Override
    public boolean drawImage(Graphics2D graphics, Rectangle2D anchor, Insets clip) {
        graphics.setRenderingHint(RenderingHintsKeyExt.KEY_BUFFERED_IMAGE, graphics.getRenderingHint(Drawable.BUFFERED_IMAGE));

        Dimension2D bounds = getDimension();

        AffineTransform at = new AffineTransform();
        at.translate(anchor.getX(), anchor.getY());
        at.scale(anchor.getWidth()/bounds.getWidth(), anchor.getHeight()/bounds.getHeight());
        svgRoot.setTransform(at);

        if (clip == null) {
            svgRoot.setClip(null);
        } else {
            Rectangle2D clippedRect = new Rectangle2D.Double(
                anchor.getX()+clip.left,
                anchor.getY()+clip.top,
                anchor.getWidth()-(clip.left+clip.right),
                anchor.getHeight()-(clip.top+clip.bottom)
            );
            svgRoot.setClip(new ClipRable8Bit(null, clippedRect));
        }

        svgRoot.paint(graphics);

        return true;
    }

    @Override
    public boolean canRender(String contentType) {
        return PictureData.PictureType.SVG.contentType.equalsIgnoreCase(contentType);
    }

    @Override
    public Rectangle2D getNativeBounds() {
        return svgRoot.getPrimitiveBounds();
    }
}
