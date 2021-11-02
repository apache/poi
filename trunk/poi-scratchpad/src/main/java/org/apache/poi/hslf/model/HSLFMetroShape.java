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

package org.apache.poi.hslf.model;

import java.io.IOException;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherComplexProperty;
import org.apache.poi.ddf.EscherPropertyTypes;
import org.apache.poi.ddf.EscherTertiaryOptRecord;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.sl.usermodel.MetroShapeProvider;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextRun;
import org.apache.poi.util.Internal;

/**
 * Experimental class for metro blobs, i.e. an alternative escher property
 * containing an ooxml representation of the shape
 */
@Internal
public class HSLFMetroShape<
    S extends Shape<S,P>,
    P extends TextParagraph<S,P,? extends TextRun>
> {
    private static final Logger LOGGER = LogManager.getLogger(HSLFMetroShape.class);

    private final HSLFShape shape;

    public HSLFMetroShape(HSLFShape shape) {
        this.shape = shape;
    }

    /**
     * @return the bytes of the metro blob, which are bytes of an OPCPackage, i.e. a zip stream
     */
    public byte[] getMetroBytes() {
        EscherComplexProperty ep = getMetroProp();
        return (ep == null) ? null : ep.getComplexData();
    }

    /**
     * @return if there's a metro blob to extract
     */
    public boolean hasMetroBlob() {
        return getMetroProp() != null;
    }

    private EscherComplexProperty getMetroProp() {
        AbstractEscherOptRecord opt = shape.getEscherChild(EscherTertiaryOptRecord.RECORD_ID);
        return (opt == null) ? null : (EscherComplexProperty)opt.lookup(EscherPropertyTypes.GROUPSHAPE__METROBLOB.propNumber);
    }

    /**
     * @return the metro blob shape or null if either there's no metro blob or the ooxml classes
     * aren't in the classpath
     */
    @SuppressWarnings("unchecked")
    public Shape<S,P> getShape() {
        byte[] metroBytes = getMetroBytes();
        if (metroBytes == null) {
            return null;
        }

        ClassLoader cl = HSLFMetroShape.class.getClassLoader();
        IOException lastError = null;
        for (MetroShapeProvider msp : ServiceLoader.load(MetroShapeProvider.class, cl)) {
            try {
                return (Shape<S, P>) msp.parseShape(metroBytes);
            } catch (IOException ex) {
                lastError = ex;
                break;
            }
        }

        LOGGER.atError().withThrowable(lastError).log("can't process metro blob, check if all dependencies for POI OOXML are in the classpath.");
        return null;
    }
}

