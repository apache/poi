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
import org.apache.poi.util.CodePageUtil;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.RLEDecompressingInputStream;

/**
 * <p>Finds all VBA Macros in an office file (OLE2/POIFS and OOXML/OPC),
 *  and returns them.
 * </p>
 * <p>
 * <b>NOTE:</b> This does not read macros from .ppt files.
 * See org.apache.poi.hslf.usermodel.TestBugs.getMacrosFromHSLF() in the scratchpad
 * module for an example of how to do this. Patches that make macro
 * extraction from .ppt more elegant are welcomed!
 * </p>
 * 
 * @since 3.15-beta2
 */
public class VBAMacroReader implements Closeable {
    protected static final String VBA_PROJECT_OOXML = "vbaProject.bin";
    protected static final String VBA_PROJECT_POIFS = "VBA";
    // FIXME: When minimum supported version is Java 7, replace with java.nio.charset.StandardCharsets.UTF_16LE
    private static final Charset UTF_16LE = Charset.forName("UTF-16LE");
    
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
        void read(InputStream in) throws IOException {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOUtils.copy(in, out);
            out.close();
            buf = out.toByteArray();
        }
    }
    protected static class ModuleMap extends HashMap<String, Module> {
        Charset charset = Charset.forName("Cp1252"); // default charset
    }
    
    /**
     * Recursively traverses directory structure rooted at <tt>dir</tt>.
     * For each macro module that is found, the module's name and code are
     * added to <tt>modules<tt>.
     *
     * @param dir The directory of entries to look at
     * @param modules The resulting map of modules
     * @throws IOException If reading the VBA module fails
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
     * @throws IOException If reading from the stream fails
     */
    private static String readString(InputStream stream, int length, Charset charset) throws IOException {
        byte[] buffer = new byte[length];
        int count = stream.read(buffer);
        return new String(buffer, 0, count, charset);
    }
    
    /**
     * reads module from DIR node in input stream and adds it to the modules map for decompression later
     * on the second pass through this function, the module will be decompressed
     * 
     * Side-effects: adds a new module to the module map or sets the buf field on the module
     * to the decompressed stream contents (the VBA code for one module)
     *
     * @param in the run-length encoded input stream to read from
     * @param streamName the stream name of the module
     * @param modules a map to store the modules
     * @throws IOException If reading data from the stream or from modules fails
     */
    private static void readModule(RLEDecompressingInputStream in, String streamName, ModuleMap modules) throws IOException {
        int moduleOffset = in.readInt();
        Module module = modules.get(streamName);
        if (module == null) {
            // First time we've seen the module. Add it to the ModuleMap and decompress it later
            module = new Module();
            module.offset = moduleOffset;
            modules.put(streamName, module);
            // Would adding module.read(in) here be correct?
        } else {
            // Decompress a previously found module and store the decompressed result into module.buf
            InputStream stream = new RLEDecompressingInputStream(
                    new ByteArrayInputStream(module.buf, moduleOffset, module.buf.length - moduleOffset)
            );
            module.read(stream);
            stream.close();
        }
    }
    
    private static void readModule(DocumentInputStream dis, String name, ModuleMap modules) throws IOException {
        Module module = modules.get(name);
        // TODO Refactor this to fetch dir then do the rest
        if (module == null) {
            // no DIR stream with offsets yet, so store the compressed bytes for later
            module = new Module();
            modules.put(name, module);
            module.read(dis);
        } else if (module.buf == null) { //if we haven't already read the bytes for the module keyed off this name...
            if (module.offset == null) {
                //This should not happen. bug 59858
                throw new IOException("Module offset for '" + name + "' was never read.");
            }
            // we know the offset already, so decompress immediately on-the-fly
            long skippedBytes = dis.skip(module.offset);
            if (skippedBytes != module.offset) {
                throw new IOException("tried to skip " + module.offset + " bytes, but actually skipped " + skippedBytes + " bytes");
            }
            InputStream stream = new RLEDecompressingInputStream(dis);
            module.read(stream);
            stream.close();
        }
        
    }

    /**
      * Skips <tt>n</tt> bytes in an input stream, throwing IOException if the
      * number of bytes skipped is different than requested.
      * @throws IOException If skipping would exceed the available data or skipping did not work.
      */
    private static void trySkip(InputStream in, long n) throws IOException {
        long skippedBytes = in.skip(n);
        if (skippedBytes != n) {
            if (skippedBytes < 0) {
                throw new IOException(
                    "Tried skipping " + n + " bytes, but no bytes were skipped. "
                    + "The end of the stream has been reached or the stream is closed.");
            } else {
                throw new IOException(
                    "Tried skipping " + n + " bytes, but only " + skippedBytes + " bytes were skipped. "
                    + "This should never happen.");
            }
        }
    }
    
    // Constants from MS-OVBA: https://msdn.microsoft.com/en-us/library/office/cc313094(v=office.12).aspx
    private static final int EOF = -1;
    private static final int VERSION_INDEPENDENT_TERMINATOR = 0x0010;
    @SuppressWarnings("unused")
    private static final int VERSION_DEPENDENT_TERMINATOR = 0x002B;
    private static final int PROJECTVERSION = 0x0009;
    private static final int PROJECTCODEPAGE = 0x0003;
    private static final int STREAMNAME = 0x001A;
    private static final int MODULEOFFSET = 0x0031;
    @SuppressWarnings("unused")
    private static final int MODULETYPE_PROCEDURAL = 0x0021;
    @SuppressWarnings("unused")
    private static final int MODULETYPE_DOCUMENT_CLASS_OR_DESIGNER = 0x0022;
    @SuppressWarnings("unused")
    private static final int PROJECTLCID = 0x0002;
    @SuppressWarnings("unused")
    private static final int MODULE_NAME = 0x0019;
    @SuppressWarnings("unused")
    private static final int MODULE_NAME_UNICODE = 0x0047;
    @SuppressWarnings("unused")
    private static final int MODULE_DOC_STRING = 0x001c;
    private static final int STREAMNAME_RESERVED = 0x0032;

    /**
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
            try {
                if ("dir".equalsIgnoreCase(name)) {
                    // process DIR
                    RLEDecompressingInputStream in = new RLEDecompressingInputStream(dis);
                    String streamName = null;
                    int recordId = 0;
                    try {
                        while (true) {
                            recordId = in.readShort();
                            if (EOF == recordId
                                    || VERSION_INDEPENDENT_TERMINATOR == recordId) {
                                break;
                            }
                            int recordLength = in.readInt();
                            switch (recordId) {
                            case PROJECTVERSION:
                                trySkip(in, 6);
                                break;
                            case PROJECTCODEPAGE:
                                int codepage = in.readShort();
                                modules.charset = Charset.forName(CodePageUtil.codepageToEncoding(codepage, true));
                                break;
                            case STREAMNAME:
                                streamName = readString(in, recordLength, modules.charset);
                                int reserved = in.readShort();
                                if (reserved != STREAMNAME_RESERVED) {
                                    throw new IOException("Expected x0032 after stream name before Unicode stream name, but found: "+
                                            Integer.toHexString(reserved));
                                }
                                int unicodeNameRecordLength = in.readInt();
                                readUnicodeString(in, unicodeNameRecordLength);
                                // do something with this at some point
                                break;
                            case MODULEOFFSET:
                                readModule(in, streamName, modules);
                                break;
                            default:
                                trySkip(in, recordLength);
                                break;
                            }
                        }
                    } catch (final IOException e) {
                        throw new IOException(
                                "Error occurred while reading macros at section id "
                                + recordId + " (" + HexDump.shortToHex(recordId) + ")", e);
                    }
                    finally {
                        in.close();
                    }
                } else if (!startsWithIgnoreCase(name, "__SRP")
                        && !startsWithIgnoreCase(name, "_VBA_PROJECT")) {
                    // process module, skip __SRP and _VBA_PROJECT since these do not contain macros
                    readModule(dis, name, modules);
                }
            }
            finally {
                dis.close();
            }
        }
    }

    private String readUnicodeString(RLEDecompressingInputStream in, int unicodeNameRecordLength) throws IOException {
        byte[] buffer = new byte[unicodeNameRecordLength];
        IOUtils.readFully(in, buffer);
        return new String(buffer, UTF_16LE);
    }
}
