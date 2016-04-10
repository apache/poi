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

package org.apache.poi.poifs.macros;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.RLEDecompressingInputStream;

/**
 * This class is able to extract the source of all VBA Modules of an Excel file.
 */
public class VBAMacroExtractor {

    /**
     * Extract macros from XLSM or XLS file. Automatically detects ZIP (XLSM, DOCX, etc) files.
     * 
     * @param in
     * @return
     * @throws IOException
     */
    public Map<String, String> extractMacros(InputStream in) throws IOException {
        PushbackInputStream bpin = new PushbackInputStream(in, 2);
        byte[] header = new byte[2];
        if (bpin.read(header) != 2) {
            throw new IllegalArgumentException("Invalid InputStream: cannot read 2 bytes");
        }
        bpin.unread(header);
        if (header[0] == 'P' && header[1] == 'K') {
            ZipInputStream zis = new ZipInputStream(bpin);
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if ("xl/vbaProject.bin".equals(zipEntry.getName())) {
                    try {
                        return extractMacrosFromPOIFSInputStream(zis);
                    } finally {
                        zis.closeEntry();
                    }
                }
            }
            return null;
        } else {
            return extractMacrosFromPOIFSInputStream(bpin);
        }
    }

    /**
     * Extracts all macros from all modules of the provided input stream. The stream is assumed to be in POIFS format (i.e. XLS file itself or
     * vbaProject.bin from OOXML files)
     * 
     * @param in
     * @return
     * @throws IOException
     */
    public Map<String, String> extractMacrosFromPOIFSInputStream(InputStream in) throws IOException {
        class Module {

            Integer offset;
            byte[] buf;
        }
        class ModuleMap extends HashMap<String, Module> {

            Charset charset = Charset.forName("Cp1252"); // default charset
        }
        try {
            final ModuleMap modules = new ModuleMap();
            POIFSReader dirReader = new POIFSReader();
            dirReader.registerListener(new POIFSReaderListener() {

                public void processPOIFSReaderEvent(POIFSReaderEvent event) {
                    try {
                        String name = event.getName();
                        if (event.getPath().toString().endsWith("\\VBA")) {
                            if ("dir".equals(name)) {
                                // process DIR
                                RLEDecompressingInputStream in = new RLEDecompressingInputStream(event.getStream());
                                String streamName = null;
                                while (true) {
                                    int id = in.readShort();
                                    if (id == -1 || id == 0x0010) {
                                        break; // EOF or TERMINATOR
                                    }
                                    int len = in.readInt();
                                    switch (id) {
                                        case 0x0009: // PROJECTVERSION
                                            in.skip(6);
                                            break;
                                        case 0x0003: // PROJECTCODEPAGE
                                            int codepage = in.readShort();
                                            modules.charset = Charset.forName("Cp" + codepage);
                                            break;
                                        case 0x001A: // STREAMNAME
                                            byte[] streamNameBuf = new byte[len];
                                            int count = in.read(streamNameBuf);
                                            streamName = new String(streamNameBuf, 0, count, modules.charset);
                                            break;
                                        case 0x0031: // MODULEOFFSET
                                            int moduleOffset = in.readInt();
                                            Module module = modules.get(streamName);
                                            if (module != null) {
                                                ByteArrayOutputStream out = new ByteArrayOutputStream();
                                                RLEDecompressingInputStream stream = new RLEDecompressingInputStream(new ByteArrayInputStream(
                                                        module.buf, moduleOffset, module.buf.length - moduleOffset));
                                                IOUtils.copy(stream, out);
                                                stream.close();
                                                out.close();
                                                module.buf = out.toByteArray();
                                            } else {
                                                module = new Module();
                                                module.offset = moduleOffset;
                                                modules.put(streamName, module);
                                            }
                                            break;
                                        default:
                                            in.skip(len);
                                            break;
                                    }
                                }
                            } else if (!name.startsWith("__SRP") && !name.startsWith("_VBA_PROJECT")) {
                                // process module, skip __SRP and _VBA_PROJECT since these do not contain macros
                                Module module = modules.get(name);
                                final DocumentInputStream stream = event.getStream();
                                final InputStream in;
                                if (module == null) {
                                    // no DIR stream with offsets yet, so store the compressed bytes for later
                                    module = new Module();
                                    modules.put(name, module);
                                    in = stream;
                                } else {
                                    // we know the offset already, so decompress immediately on-the-fly
                                    stream.skip(module.offset);
                                    in = new RLEDecompressingInputStream(stream);
                                }
                                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                                IOUtils.copy(in, out);
                                in.close();
                                out.close();
                                module.buf = out.toByteArray();
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            dirReader.read(in);
            Map<String, String> moduleSources = new HashMap<String, String>();
            for (Map.Entry<String, Module> entry : modules.entrySet()) {
                Module module = entry.getValue();
                if (module.buf != null && module.buf.length > 0) { // Skip empty modules
                    moduleSources.put(entry.getKey(), new String(module.buf, modules.charset));
                }
            }
            return moduleSources;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
