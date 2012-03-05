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

package org.apache.poi.xslf.usermodel;

import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.Beta;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * For now this class renders only images supported by the javax.imageio.ImageIO
 * framework. Subclasses can override this class to support other formats, for
 * example, Use Apache batik to render WMF:
 * 
 * <pre>
 * <code>
 * public class MyImageRendener extends XSLFImageRendener{
 * public boolean drawImage(Graphics2D graphics, XSLFPictureData data, Rectangle2D anchor){
 * 	boolean ok = super.drawImage(graphics, data, anchor);
 * 	if(!ok){
 * 		// see what type of image we are
 * 		String contentType = data.getPackagePart().getContentType();
 * 		if(contentType.equals("image/wmf")){
 * 			// use Apache Batik to handle WMF
 * 			// see http://xmlgraphics.apache.org/batik/
 * 		}
 * 		
 * 	}
 * 	return ok;
 * }
 * }
 * </code>
 * </pre>
 * 
 * and then pass this class to your instance of java.awt.Graphics2D:
 * 
 * <pre>
 * <code>
 * graphics.setRenderingHint(XSLFRenderingHint.IMAGE_RENDERER, new MyImageRendener());
 * </code>
 * </pre>
 * 
 * @author Yegor Kozlov
 */
@Beta
public class XSLFImageRenderer {

	/**
	 * Render picture data into the supplied graphics
	 * 
	 * @return true if the picture data was successfully rendered
	 */
	public boolean drawImage(Graphics2D graphics, XSLFPictureData data,
			Rectangle2D anchor) {
		try {
			BufferedImage img = ImageIO.read(data.getPackagePart().getInputStream());
            double sx = anchor.getWidth()/img.getWidth();
            double sy = anchor.getHeight()/img.getHeight();
            double tx = anchor.getX();
            double ty = anchor.getY();
            AffineTransform at = new AffineTransform(sx, 0, 0, sy, tx, ty) ;

            graphics.drawRenderedImage(img, at);

			return true;
		} catch (Exception e) {
			return false;
		}

	}

    /**
     * Create a buffered image from the supplied package part.
     * This method is called to create texture paints.
     *
     * @return a <code>BufferedImage</code> containing the decoded
     * contents of the input, or <code>null</code>.
     */
    public BufferedImage readImage(PackagePart packagePart) throws IOException {
        return ImageIO.read(packagePart.getInputStream());
    }
}