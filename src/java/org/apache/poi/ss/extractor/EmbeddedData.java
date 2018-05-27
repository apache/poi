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

package org.apache.poi.ss.extractor;

import org.apache.poi.ss.usermodel.Shape;

/**
 * A collection of embedded object informations and content
 */
public class EmbeddedData {
    private String filename;
    private byte[] embeddedData;
    private Shape shape;
    private String contentType = "binary/octet-stream";

    public EmbeddedData(String filename, byte[] embeddedData, String contentType) {
        setFilename(filename);
        setEmbeddedData(embeddedData);
        setContentType(contentType);
    }
    
    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }
    
    /**
     * Sets the filename 
     *
     * @param filename the filename
     */
    public void setFilename(String filename) {
        if (filename == null) {
            this.filename = "unknown.bin";
        } else {
            this.filename = filename.replaceAll("[^/\\\\]*[/\\\\]", "").trim();
        }
    }
    
    /**
     * @return the embedded object byte array
     */
    public byte[] getEmbeddedData() {
        return embeddedData;
    }

    /**
     * Sets the embedded object as byte array
     *
     * @param embeddedData the embedded object byte array
     */
    public void setEmbeddedData(byte[] embeddedData) {
        this.embeddedData = (embeddedData == null) ? null : embeddedData.clone();
    }

    /**
     * @return the shape which links to the embedded object
     */
    public Shape getShape() {
        return shape;
    }

    /**
     * Sets the shape which links to the embedded object
     *
     * @param shape the shape
     */
    public void setShape(Shape shape) {
        this.shape = shape;
    }

    /**
     * @return the content-/mime-type of the embedded object, the default (if unknown) is {@code binary/octet-stream} 
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content-/mime-type
     *
     * @param contentType the content-type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}