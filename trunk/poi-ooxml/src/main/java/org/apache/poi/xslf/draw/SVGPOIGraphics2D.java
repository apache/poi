/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xslf.draw;

import java.awt.RenderingHints;
import java.util.Map;

import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGeneratorContext.GraphicContextDefaults;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.poi.util.Internal;
import org.w3c.dom.Document;

/**
 * Wrapper class to pass changes to rendering hints down to the svg graphics context defaults
 * which can be read by the extension handlers
 */
@Internal
public class SVGPOIGraphics2D extends SVGGraphics2D {

    private final RenderingHints hints;

    public SVGPOIGraphics2D(Document document, boolean textAsShapes) {
        super(getCtx(document), textAsShapes);
        hints = getGeneratorContext().getGraphicContextDefaults().getRenderingHints();
        ((SVGRenderExtension)getGeneratorContext().getExtensionHandler()).setSvgGraphics2D(this);
    }

    private static SVGGeneratorContext getCtx(Document document) {
        SVGGeneratorContext context = SVGGeneratorContext.createDefault(document);
        context.setExtensionHandler(new SVGRenderExtension());
        GraphicContextDefaults defs = new GraphicContextDefaults();
        defs.setRenderingHints(new RenderingHints(null));
        context.setGraphicContextDefaults(defs);
        return context;
    }

    @Override
    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
        hints.put(hintKey, hintValue);
        super.setRenderingHint(hintKey, hintValue);
    }

    @Override
    public void setRenderingHints(Map hints) {
        this.hints.clear();
        this.hints.putAll(hints);
        super.setRenderingHints(hints);
    }

    @Override
    public void addRenderingHints(Map hints) {
        this.hints.putAll(hints);
        super.addRenderingHints(hints);
    }
}
