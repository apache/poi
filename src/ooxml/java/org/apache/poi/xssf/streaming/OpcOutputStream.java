package org.apache.poi.xssf.streaming;


import org.apache.poi.xssf.streaming.Zip64Impl.Entry;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.*;

/**
 * ZIP64 OutputStream implementation compatible with MS Excel.
 * Drop in replacement for `java.util.ZipOutputStream`.
 *
 * For more information see https://github.com/rzymek/opczip
 *
 * @author Krzysztof Rzymkowski
 */
class OpcOutputStream extends DeflaterOutputStream {

    private final Zip64Impl spec;
    private final List<Entry> entries = new ArrayList<>();
    private final CRC32 crc = new CRC32();
    private Entry current;
    private int written = 0;
    private boolean finished = false;

    /**
     * Creates ZIP64 output stream
     *
     * @param out target stream to write compressed data to
     */
    public OpcOutputStream(OutputStream out) {
        super(out, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
        this.spec = new Zip64Impl(out);
    }

    /**
     * @see Deflater#setLevel(int)
     */
    public void setLevel(int level) {
        super.def.setLevel(level);
    }

    /**
     * @see ZipOutputStream#putNextEntry(ZipEntry)
     */
    public void putNextEntry(String name) throws IOException {
        if (current != null) {
            closeEntry();
        }
        current = new Entry(name);
        current.offset = written;
        written += spec.writeLFH(current);
        entries.add(current);
    }

    /**
     * @see ZipOutputStream#closeEntry()
     */
    public void closeEntry() throws IOException {
        if (current == null) {
            throw new IllegalStateException("not current zip current");
        }
        def.finish();
        while (!def.finished()) {
            deflate();
        }

        current.size = def.getBytesRead();
        current.compressedSize = (int) def.getBytesWritten();
        current.crc = crc.getValue();

        written += current.compressedSize;
        written += spec.writeDAT(current);
        current = null;
        def.reset();
        crc.reset();
    }


    /**
     * @see ZipOutputStream#finish()
     */
    @Override
    public void finish() throws IOException {
        if(finished){
            return;
        }
        if(current != null) {
            closeEntry();
        }
        int offset = written;
        for (Entry entry : entries) {
            written += spec.writeCEN(entry);
        }
        written += spec.writeEND(entries.size(), offset, written - offset);
        finished = true;
    }

    /**
     * @see ZipOutputStream#write(byte[], int, int)
     */
    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        super.write(b, off, len);
        crc.update(b, off, len);
    }

    /**
     * @see ZipOutputStream#close()
     */
    @Override
    public void close() throws IOException {
        finish();
        out.close();
    }
}
