package org.apache.poi.xssf.streaming;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.IOException;
import java.io.OutputStream;

class OpcZipArchiveOutputStream extends ZipArchiveOutputStream {
    private final OpcOutputStream out;

    OpcZipArchiveOutputStream(OutputStream out) {
        super(out);
        this.out = new OpcOutputStream(out);
    }

    @Override
    public void setLevel(int level) {
        out.setLevel(level);
    }


    @Override
    public void putArchiveEntry(ArchiveEntry archiveEntry) throws IOException {
        out.putNextEntry(archiveEntry.getName());
    }

    @Override
    public void closeArchiveEntry() throws IOException {
        out.closeEntry();
    }


    @Override
    public void finish() throws IOException {
        out.finish();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
    }
}
