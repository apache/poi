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

package org.apache.poi.xslf.util;

import java.awt.Graphics2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;

public class DummyFormat implements OutputFormat {

    private final UnsynchronizedByteArrayOutputStream bos;
    private final DummyGraphics2d dummy2d;

    public DummyFormat() {
        try {
            bos = new UnsynchronizedByteArrayOutputStream();
            dummy2d = new DummyGraphics2d(new PrintStream(bos, true, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Graphics2D addSlide(double width, double height) {
        bos.reset();
        return dummy2d;
    }

    @Override
    public void writeSlide(MFProxy proxy, File outFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            bos.writeTo(fos);
            bos.reset();
        }
    }

    @Override
    public void writeDocument(MFProxy proxy, File outFile) {

    }

    @Override
    public void close() throws IOException {
        bos.reset();
    }
}
