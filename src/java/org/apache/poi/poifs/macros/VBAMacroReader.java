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

import static org.apache.poi.util.StringUtil.startsWithIgnoreCase;
import static org.apache.poi.util.StringUtil.endsWithIgnoreCase;

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
 *  and returns them.
 * 
 * @since 3.15-beta2
 */
public class VBAMacroReader implements Closeable {
    protected static final String VBA_PROJECT_OOXML = "vbaProject.bin";
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
            if (endsWithIgnoreCase(zipEntry.getName(), VBA_PROJECT_OOXML)) {
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
     *
     * @since 3.15-beta2
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
    
    /**
     * Recursively traverses directory structure rooted at <tt>dir</tt>.
     * For each macro module that is found, the module's name and code are
     * added to <tt>modules<tt>.
     *
     * @param dir
     * @param modules
     * @throws IOException
     * @since 3.15-beta2
     */
    protected void findMacros(DirectoryNode dir, ModuleMap modules) throws IOException {
        if (VBA_PROJECT_POIFS.equalsIgnoreCase(dir.getName())) {
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
    
    /**
     * Read <tt>length</tt> bytes of MBCS (multi-byte character set) characters from the stream
     *
     * @param stream the inputstream to read from
     * @param length number of bytes to read from stream
     * @param charset the character set encoding of the bytes in the stream
     * @return a java String in the supplied character set
     * @throws IOException
     */
    private static String readString(InputStream stream, int length, Charset charset) throws IOException {
        byte[] buffer = new byte[length];
        int count = stream.read(buffer);
        return new String(buffer, 0, count, charset);
    }

    /**
      * Skips <tt>n</tt> bytes in an input stream, throwing IOException if the
      * number of bytes skipped is different than requested.
      * @throws IOException
      */
    private static void trySkip(InputStream in, long n) throws IOException {
        long skippedBytes = in.skip(n);
        if (skippedBytes != n) {
            throw new IOException(
                "Skipped only " + skippedBytes + " while trying to skip " + n + " bytes. " +
                    " This should never happen.");
        }
    }

    /*
     * Reads VBA Project modules from a VBA Project directory located at
     * <tt>macroDir</tt> into <tt>modules</tt>.
     *
     * @since 3.15-beta2
     */    
    protected void readMacros(DirectoryNode macroDir, ModuleMap modules) throws IOException {
        for (Entry entry : macroDir) {
            if (! (entry instanceof DocumentNode)) { continue; }
            
            String name = entry.getName();
            DocumentNode document = (DocumentNode)entry;
            DocumentInputStream dis = new DocumentInputStream(document);
            if ("dir".equalsIgnoreCase(name)) {
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
                        trySkip(in, 6);
                        break;
                    case 0x0003: // PROJECTCODEPAGE
                        int codepage = in.readShort();
                        modules.charset = Charset.forName("Cp" + codepage);
                        break;
                    case 0x001A: // STREAMNAME
                        streamName = readString(in, len, modules.charset);
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
                        trySkip(in, len);
                        break;
                    }
                }
                in.close();
            } else if (!startsWithIgnoreCase(name, "__SRP")
                    && !startsWithIgnoreCase(name, "_VBA_PROJECT")) {
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
