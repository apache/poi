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
package org.apache.poi.stress;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("unused")
public enum FileHandlerKnown {
    HDGF,
    HMEF,
    HPBF,
    HPSF,
    HSLF,
    HSMF,
    HSSF,
    HWPF,
    OPC,
    POIFS,
    XDGF,
    XSLF,
    XSSFB,
    XSSF,
    XWPF,
    OWPF,
    NULL
    ;

    public FileHandler getHandler() {
        try {
            // Because of no-scratchpad handling, we need to resort to reflection here
            String n = name().replace("NULL", "Null");
            return (FileHandler)Class.forName("org.apache.poi.stress." + n + "FileHandler").getDeclaredConstructor().newInstance();
        } catch (RuntimeException | ClassNotFoundException | NoSuchMethodException | InstantiationException |
                IllegalAccessException | InvocationTargetException e) {
            return new NullFileHandler();
        }
    }

    private static class NullFileHandler implements FileHandler {
        @Override
        public void handleFile(InputStream stream, String path) {}

        @Override
        public void handleExtracting(File file) {}

        @Override
        public void handleAdditional(File file) {}
    }
}
