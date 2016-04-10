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
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.RLEDecompressingInputStream;

/**
 * Finds all VBA Macros in an office file (OLE2/POIFS and OOXML/OPC),
 *  and returns them
 */
public class VBAMacroReader implements Closeable {
    protected static final String VBA_PROJECT_OOXML = "xl/vbaProject.bin";
    protected static final String VBA_PROJECT_POIFS = "VBA";
    
    private NPOIFSFileSystem fs;
    
    public VBAMacroReader(InputStream rstream) throws IOException {
        PushbackInputStream stream = new PushbackInputStream(rstream, 8);
        byte[] header8 = IOUtils.peekFirst8Bytes(stream);

        if (NPOIFSFileSystem.hasPOIFSHeader(header8)) {
            fs = new NPOIFSFileSystem(stream);
        } else {
            openOOXML(stream);
        }
    }
    
    public VBAMacroReader(File file) throws IOException {
        try {
            this.fs = new NPOIFSFileSystem(file);
        } catch (OfficeXmlFileException e) {
            openOOXML(new FileInputStream(file));
        }
    }
    public VBAMacroReader(NPOIFSFileSystem fs) {
        this.fs = fs;
    }
    
    private void openOOXML(InputStream zipFile) throws IOException {
        ZipInputStream zis = new ZipInputStream(zipFile);
        ZipEntry zipEntry;
        while ((zipEntry = zis.getNextEntry()) != null) {
            if (VBA_PROJECT_OOXML.equals(zipEntry.getName())) {
                try {
                    // Make a NPOIFS from the contents, and close the stream
                    this.fs = new NPOIFSFileSystem(zis);
                    return;
                } catch (IOException e) {
                    // Tidy up
                    zis.close();
                    
                    // Pass on
                    throw e;
                }
            }
        }
        zis.close();
        throw new IllegalArgumentException("No VBA project found");
    }
    
    public void close() throws IOException {
        fs.close();
        fs = null;
    }

    /**
     * Reads all macros from all modules of the opened office file. 
     * @return All the macros and their contents
     */
    public Map<String, String> readMacros() throws IOException {
        final ModuleMap modules = new ModuleMap();
        findMacros(fs.getRoot(), modules);
        
        Map<String, String> moduleSources = new HashMap<String, String>();
        for (Map.Entry<String, Module> entry : modules.entrySet()) {
            Module module = entry.getValue();
            if (module.buf != null && module.buf.length > 0) { // Skip empty modules
                moduleSources.put(entry.getKey(), new String(module.buf, modules.charset));
            }
        }
        return moduleSources;
    }
    
    protected static class Module {
        Integer offset;
        byte[] buf;
    }
    protected static class ModuleMap extends HashMap<String, Module> {
        Charset charset = Charset.forName("Cp1252"); // default charset
    }
    
    protected void findMacros(DirectoryNode dir, ModuleMap modules) throws IOException {
        if (VBA_PROJECT_POIFS.equals(dir.getName())) {
            // VBA project directory, process
            readMacros(dir, modules);
        } else {
            // Check children
            for (Entry child : dir) {
                if (child instanceof DirectoryNode) {
                    findMacros((DirectoryNode)child, modules);
                }
            }
        }
    }
    protected void readMacros(DirectoryNode macroDir, ModuleMap modules) throws IOException {
        for (Entry entry : macroDir) {
            if (! (entry instanceof DocumentNode)) { continue; }
            
            String name = entry.getName();
            DocumentNode document = (DocumentNode)entry;
            DocumentInputStream dis = new DocumentInputStream(document);
            if ("dir".equals(name)) {
                // process DIR
                RLEDecompressingInputStream in = new RLEDecompressingInputStream(dis);
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
                in.close();
            } else if (!name.startsWith("__SRP") && !name.startsWith("_VBA_PROJECT")) {
                // process module, skip __SRP and _VBA_PROJECT since these do not contain macros
                Module module = modules.get(name);
                final InputStream in;
                // TODO Refactor this to fetch dir then do the rest
                if (module == null) {
                    // no DIR stream with offsets yet, so store the compressed bytes for later
                    module = new Module();
                    modules.put(name, module);
                    in = dis;
                } else {
                    // we know the offset already, so decompress immediately on-the-fly
                    dis.skip(module.offset);
                    in = new RLEDecompressingInputStream(dis);
                }
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                IOUtils.copy(in, out);
                in.close();
                out.close();
                module.buf = out.toByteArray();
            }
        }
    }
}
