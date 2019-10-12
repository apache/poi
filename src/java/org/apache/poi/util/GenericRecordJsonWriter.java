/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.util;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.util.GenericRecordUtil.AnnotatedFlag;

@Beta
public class GenericRecordJsonWriter implements Closeable {
    private static final String TABS;
    private static final String ZEROS = "0000000000000000";
    private static final Pattern ESC_CHARS = Pattern.compile("[\"\\p{Cntrl}\\\\]");

    private static final List<Map.Entry<Class,BiConsumer<GenericRecordJsonWriter,Object>>> handler = new ArrayList<>();

    static {
        char[] t = new char[255];
        Arrays.fill(t, '\t');
        TABS = new String(t);
        handler(String.class, GenericRecordJsonWriter::printObject);
        handler(Number.class, GenericRecordJsonWriter::printNumber);
        handler(Boolean.class, GenericRecordJsonWriter::printBoolean);
        handler(List.class, GenericRecordJsonWriter::printList);
        handler(GenericRecord.class, GenericRecordJsonWriter::printGenericRecord);
        handler(AnnotatedFlag.class, GenericRecordJsonWriter::printAnnotatedFlag);
        handler(byte[].class, GenericRecordJsonWriter::printBytes);
        handler(Point2D.class, GenericRecordJsonWriter::printPoint);
        handler(Dimension2D.class, GenericRecordJsonWriter::printDimension);
        handler(Rectangle2D.class, GenericRecordJsonWriter::printRectangle);
        handler(Path2D.class, GenericRecordJsonWriter::printPath);
        handler(AffineTransform.class, GenericRecordJsonWriter::printAffineTransform);
        handler(Color.class, GenericRecordJsonWriter::printColor);
        handler(Array.class, GenericRecordJsonWriter::printArray);
        handler(Object.class, GenericRecordJsonWriter::printObject);
    }

    private static void handler(Class c, BiConsumer<GenericRecordJsonWriter,Object> printer) {
        handler.add(new AbstractMap.SimpleEntry<>(c,printer));
    }

    private final PrintWriter fw;
    private int indent = 0;
    private boolean withComments = true;
    private int childIndex = 0;

    public GenericRecordJsonWriter(File fileName) throws IOException {
        OutputStream os = ("null".equals(fileName.getName())) ? new NullOutputStream() : new FileOutputStream(fileName);
        fw = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
    }

    public GenericRecordJsonWriter(Appendable buffer) {
        fw = new PrintWriter(new AppendableWriter(buffer));
    }

    public static String marshal(GenericRecord record) {
        return marshal(record, true);
    }

    public static String marshal(GenericRecord record, boolean withComments) {
        final StringBuilder sb = new StringBuilder();
        try (GenericRecordJsonWriter w = new GenericRecordJsonWriter(sb)) {
            w.setWithComments(withComments);
            w.write(record);
            return sb.toString();
        } catch (IOException e) {
            return "{}";
        }
    }

    public void setWithComments(boolean withComments) {
        this.withComments = withComments;
    }

    @Override
    public void close() throws IOException {
        fw.close();
    }

    private String tabs() {
        return TABS.substring(0, Math.min(indent, TABS.length()));
    }

    public void write(GenericRecord record) {
        final String tabs = tabs();
        Enum type = record.getGenericRecordType();
        String recordName = (type != null) ? type.name() : record.getClass().getSimpleName();
        fw.append(tabs);
        fw.append("{");
        if (withComments) {
            fw.append("   /* ");
            fw.append(recordName);
            if (childIndex > 0) {
                fw.append(" - index: ");
                fw.print(childIndex);
            }
            fw.append(" */");
        }
        fw.println();

        Map<String, Supplier<?>> prop = record.getGenericProperties();
        if (prop != null) {
            final int oldChildIndex = childIndex;
            childIndex = 0;
            prop.forEach(this::writeProp);
            childIndex = oldChildIndex;
        }

        fw.println();
        List<? extends GenericRecord> list = record.getGenericChildren();
        if (list != null && !list.isEmpty()) {
            indent++;
            fw.append(tabs());
            if (prop != null && !prop.isEmpty()) {
                fw.append(", ");
            }
            fw.append("children: [");
            final int oldChildIndex = childIndex;
            childIndex = 0;
            list.forEach(l -> { writeValue(l); childIndex++; });
            childIndex = oldChildIndex;
            fw.println();
            fw.append(tabs());
            fw.append("]");
            fw.println();
            indent--;
        }

        fw.append(tabs);
        fw.append("}");
    }

    public void writeError(String errorMsg) {
        fw.append("{ error: ");
        printObject(errorMsg);
        fw.append(" }");
    }

    private void writeProp(String k, Supplier<?> v) {
        final boolean isNext = (childIndex++>0);
        if (isNext) {
            fw.println();
        }
        fw.write(tabs());
        fw.write('\t');
        fw.write(isNext ? ", " : "  ");
        fw.write(k);
        fw.write(": ");
        final int oldChildIndex = childIndex;
        childIndex = 0;
        writeValue(v.get());
        childIndex = oldChildIndex;
    }

    private void writeValue(Object o) {
        if (childIndex > 0) {
            fw.println(',');
        }
        if (o == null) {
            fw.write("null");
        } else {
            handler.stream().
                filter(h -> matchInstanceOrArray(h.getKey(), o)).
                findFirst().
                ifPresent(h -> h.getValue().accept(this, o));
        }
    }

    private static boolean matchInstanceOrArray(Class key, Object instance) {
        return key.isInstance(instance) || (Array.class.equals(key) && instance.getClass().isArray());
    }

    private void printNumber(Object o) {
        Number n = (Number)o;
        fw.print(n.toString());

        final int size;
        if (n instanceof Byte) {
            size = 2;
        } else if (n instanceof Short) {
            size = 4;
        } else if (n instanceof Integer) {
            size = 8;
        } else if (n instanceof Long) {
            size = 16;
        } else {
            size = -1;
        }

        long l = n.longValue();
        if (withComments && size > 0 && (l < 0 || l > 9)) {
            fw.write(" /* 0x");
            fw.write(trimHex(l, size));
            fw.write(" */");
        }
    }

    private void printBoolean(Object o) {
        fw.write(((Boolean)o).toString());
    }

    private void printList(Object o) {
        fw.println('[');
        int oldChildIndex = childIndex;
        childIndex = 0;
        //noinspection unchecked
        ((List)o).forEach(e -> { writeValue(e); childIndex++; });
        childIndex = oldChildIndex;
        fw.write(']');
    }

    private void printGenericRecord(Object o) {
        fw.println();
        this.indent++;
        write((GenericRecord) o);
        this.indent--;
    }

    private void printAnnotatedFlag(Object o) {
        AnnotatedFlag af = (AnnotatedFlag) o;
        fw.write("0x");
        fw.write(Long.toHexString(af.getValue().get().longValue()));
        if (withComments) {
            fw.write(" /* ");
            fw.write(af.getDescription());
            fw.write(" */ ");
        }
    }

    private void printBytes(Object o) {
        fw.write('"');
        fw.write(DatatypeConverter.printBase64Binary((byte[]) o));
        fw.write('"');
    }

    private void printPoint(Object o) {
        Point2D p = (Point2D)o;
        fw.write("{ x: "+p.getX()+", y: "+p.getY()+" }");
    }

    private void printDimension(Object o) {
        Dimension2D p = (Dimension2D)o;
        fw.write("{ width: "+p.getWidth()+", height: "+p.getHeight()+" }");
    }

    private void printRectangle(Object o) {
        Rectangle2D p = (Rectangle2D)o;
        fw.write("{ x: "+p.getX()+", y: "+p.getY()+", width: "+p.getWidth()+", height: "+p.getHeight()+" }");
    }

    private void printPath(Object o) {
        final PathIterator iter = ((Path2D)o).getPathIterator(null);
        final double[] pnts = new double[6];
        fw.print("[");

        indent += 2;
        String t = tabs();
        indent -= 2;

        boolean isNext = false;
        while (!iter.isDone()) {
            fw.println(isNext ? ", " : "");
            fw.print(t);
            isNext = true;
            final int segType = iter.currentSegment(pnts);
            fw.append("{ type: ");
            switch (segType) {
                case PathIterator.SEG_MOVETO:
                    fw.write("'move', x: "+pnts[0]+", y: "+pnts[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    fw.write("'lineto', x: "+pnts[0]+", y: "+pnts[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    fw.write("'quad', x1: "+pnts[0]+", y1: "+pnts[1]+", x2: "+pnts[2]+", y2: "+pnts[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    fw.write("'cubic', x1: "+pnts[0]+", y1: "+pnts[1]+", x2: "+pnts[2]+", y2: "+pnts[3]+", x3: "+pnts[4]+", y3: "+pnts[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    fw.write("'close'");
                    break;
            }
            fw.append(" }");
            iter.next();
        }

        fw.write("]");
    }

    private void printObject(Object o) {
        fw.write('"');

        final Matcher m = ESC_CHARS.matcher(o.toString());
        final StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String repl;
            String match = m.group();
            switch (match) {
                case "\n":
                    repl = "\\\\n";
                    break;
                case "\r":
                    repl = "\\\\r";
                    break;
                case "\t":
                    repl = "\\\\t";
                    break;
                case "\b":
                    repl = "\\\\b";
                    break;
                case "\f":
                    repl = "\\\\f";
                    break;
                case "\\":
                    repl = "\\\\\\\\";
                    break;
                case "\"":
                    repl = "\\\\\"";
                    break;
                default:
                    repl = "\\\\u" + trimHex(match.charAt(0), 4);
                    break;
            }
            m.appendReplacement(sb, repl);
        }
        m.appendTail(sb);
        fw.write(sb.toString());

        fw.write('"');
    }

    private void printAffineTransform(Object o) {
        AffineTransform xForm = (AffineTransform)o;
        fw.write(
            "{ scaleX: "+xForm.getScaleX()+
            ", shearX: "+xForm.getShearX()+
            ", transX: "+xForm.getTranslateX()+
            ", scaleY: "+xForm.getScaleY()+
            ", shearY: "+xForm.getShearY()+
            ", transY: "+xForm.getTranslateY()+" }");
    }

    private void printColor(Object o) {
        final int rgb = ((Color)o).getRGB();
        fw.print(rgb);

        if (withComments) {
            fw.write(" /* 0x");
            fw.write(trimHex(rgb, 8));
            fw.write(" */");
        }
    }

    private void printArray(Object o) {
        fw.println('[');
        int length = Array.getLength(o);
        final int oldChildIndex = childIndex;
        for (childIndex=0; childIndex<length; childIndex++) {
            writeValue(Array.get(o, childIndex));
        }
        childIndex = oldChildIndex;
        fw.write(']');
    }

    static String trimHex(final long l, final int size) {
        final String b = Long.toHexString(l);
        int len = b.length();
        return ZEROS.substring(0, Math.max(0,size-len)) + b.substring(Math.max(0,len-size), len);
    }

    static class NullOutputStream extends OutputStream {
        NullOutputStream() {
        }

        @Override
        public void write(byte[] b, int off, int len) {
        }

        @Override
        public void write(int b) {
        }

        @Override
        public void write(byte[] b) {
        }
    }

    static class AppendableWriter extends Writer {
        private Appendable buffer;

        AppendableWriter(Appendable buffer) {
            super(buffer);
            this.buffer = buffer;
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            buffer.append(String.valueOf(cbuf), off, len);
        }

        @Override
        public void flush() throws IOException {
            if (buffer instanceof Flushable) {
                ((Flushable)buffer).flush();
            }
        }

        @Override
        public void close() throws IOException {
            flush();
            if (buffer instanceof Closeable) {
                ((Closeable)buffer).close();
            }
        }
    }
}
