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
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.DatatypeConverter;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.util.GenericRecordJsonWriter.AppendableWriter;
import org.apache.poi.util.GenericRecordJsonWriter.NullOutputStream;

public class GenericRecordXmlWriter implements Closeable {
    private static final String TABS;
    private static final String ZEROS = "0000000000000000";
    private static final Pattern ESC_CHARS = Pattern.compile("[<>&'\"\\p{Cntrl}]");

    private static final List<Map.Entry<Class, BiConsumer<GenericRecordXmlWriter,Object>>> handler = new ArrayList<>();

    static {
        char[] t = new char[255];
        Arrays.fill(t, '\t');
        TABS = new String(t);
        handler(String.class, GenericRecordXmlWriter::printObject);
        handler(Number.class, GenericRecordXmlWriter::printNumber);
        handler(Boolean.class, GenericRecordXmlWriter::printBoolean);
        handler(List.class, GenericRecordXmlWriter::printList);
        // handler(GenericRecord.class, GenericRecordXmlWriter::printGenericRecord);
        handler(GenericRecordUtil.AnnotatedFlag.class, GenericRecordXmlWriter::printAnnotatedFlag);
        handler(byte[].class, GenericRecordXmlWriter::printBytes);
        handler(Point2D.class, GenericRecordXmlWriter::printPoint);
        handler(Dimension2D.class, GenericRecordXmlWriter::printDimension);
        handler(Rectangle2D.class, GenericRecordXmlWriter::printRectangle);
        handler(Path2D.class, GenericRecordXmlWriter::printPath);
        handler(AffineTransform.class, GenericRecordXmlWriter::printAffineTransform);
        handler(Color.class, GenericRecordXmlWriter::printColor);
        handler(BufferedImage.class, GenericRecordXmlWriter::printBufferedImage);
        handler(Array.class, GenericRecordXmlWriter::printArray);
        handler(Object.class, GenericRecordXmlWriter::printObject);
    }

    private static void handler(Class c, BiConsumer<GenericRecordXmlWriter,Object> printer) {
        handler.add(new AbstractMap.SimpleEntry<>(c, printer));
    }

    private final PrintWriter fw;
    private int indent = 0;
    private boolean withComments = true;
    private int childIndex = 0;
    private boolean attributePhase = true;

    public GenericRecordXmlWriter(File fileName) throws IOException {
        OutputStream os = ("null".equals(fileName.getName())) ? new NullOutputStream() : new FileOutputStream(fileName);
        fw = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
    }

    public GenericRecordXmlWriter(Appendable buffer) {
        fw = new PrintWriter(new AppendableWriter(buffer));
    }

    public static String marshal(GenericRecord record) {
        return marshal(record, true);
    }

    public static String marshal(GenericRecord record, boolean withComments) {
        final StringBuilder sb = new StringBuilder();
        try (GenericRecordXmlWriter w = new GenericRecordXmlWriter(sb)) {
            w.setWithComments(withComments);
            w.write(record);
            return sb.toString();
        } catch (IOException e) {
            return "<record/>";
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
        write(record, "record");
    }

    private void write(GenericRecord record, final String name) {
        final String tabs = tabs();
        Enum type = record.getGenericRecordType();
        String recordName = (type != null) ? type.name() : record.getClass().getSimpleName();
        fw.append(tabs);
        fw.append("<"+name+" type=\"");
        fw.append(recordName);
        fw.append("\"");
        if (childIndex > 0) {
            fw.append(" index=\"");
            fw.print(childIndex);
            fw.append("\"");
        }

        boolean hasChildren = false;

        Map<String, Supplier<?>> prop = record.getGenericProperties();
        if (prop != null) {
            final int oldChildIndex = childIndex;
            childIndex = 0;
            attributePhase = true;
            List<Map.Entry<String,Supplier<?>>> complex = prop.entrySet().stream().flatMap(this::writeProp).collect(Collectors.toList());
            attributePhase = false;
            if (!complex.isEmpty()) {
                hasChildren = true;
                fw.println(">");
                indent++;
                complex.forEach(this::writeProp);
                indent--;
            }
            childIndex = oldChildIndex;
        } else {
            fw.print(">");
        }

        attributePhase = false;

        List<? extends GenericRecord> list = record.getGenericChildren();
        if (list != null && !list.isEmpty()) {
            hasChildren = true;
            indent++;
            fw.println();
            fw.append(tabs());
            fw.println("<children>");
            indent++;
            final int oldChildIndex = childIndex;
            childIndex = 0;
            list.forEach(l -> { writeValue("record", l); childIndex++; });
            childIndex = oldChildIndex;
            fw.println();
            indent--;
            fw.append(tabs());
            fw.println("</children>");
            indent--;
        }

        if (hasChildren) {
            fw.append(tabs);
            fw.println("</" + name + ">");
        } else {
            fw.println("/>");
        }
    }

    public void writeError(String errorMsg) {
        fw.append("<error>");
        printObject(errorMsg);
        fw.append("</error>");
    }

    private Stream<Map.Entry<String,Supplier<?>>> writeProp(Map.Entry<String,Supplier<?>> me) {
        Object obj = me.getValue().get();
        if (obj == null) {
            return Stream.empty();
        }

        final boolean isComplex = isComplex(obj);
        if (attributePhase == isComplex) {
            return isComplex ? Stream.of(new AbstractMap.SimpleEntry<>(me.getKey(), () -> obj)) : Stream.empty();
        }

        final int oldChildIndex = childIndex;
        childIndex = 0;
        writeValue(me.getKey(), obj);
        childIndex = oldChildIndex;

        return Stream.empty();
    }

    private static boolean isComplex(Object obj) {
        return !(
            obj instanceof Number ||
            obj instanceof Boolean ||
            obj instanceof Character ||
            obj instanceof String ||
            obj instanceof Color ||
            obj instanceof Enum);
    }

    private void writeValue(String key, Object o) {
        assert(key != null);
        if (o instanceof GenericRecord) {
            printGenericRecord((GenericRecord)o, key);
        } else if (o != null) {
            if (key.endsWith(">")) {
                fw.print("\t");
            }

            fw.print(attributePhase ? " " + key + "=\"" : tabs()+"<" + key);
            if (key.endsWith(">")) {
                fw.println();
            }

            handler.stream().
                filter(h -> matchInstanceOrArray(h.getKey(), o)).
                findFirst().
                ifPresent(h -> h.getValue().accept(this, o));

            if (attributePhase) {
                fw.append("\"");
            }

            if (key.endsWith(">")) {
                fw.println(tabs()+"\t</"+key);
            } else if (o instanceof List || o.getClass().isArray()) {
                fw.println(tabs()+"</"+key+">");
            }
        }
    }

    private static boolean matchInstanceOrArray(Class key, Object instance) {
        return key.isInstance(instance) || (Array.class.equals(key) && instance.getClass().isArray());
    }
    private void printNumber(Object o) {
        assert(attributePhase);
        Number n = (Number)o;
        fw.print(n.toString());

        if (attributePhase) {
            return;
        }

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
        assert (!attributePhase);
        fw.println(">");
        int oldChildIndex = childIndex;
        childIndex = 0;
        //noinspection unchecked
        ((List)o).forEach(e -> { writeValue("item>", e); childIndex++; });
        childIndex = oldChildIndex;
    }

    private void printArray(Object o) {
        assert (!attributePhase);
        fw.println(">");
        int length = Array.getLength(o);
        final int oldChildIndex = childIndex;
        for (childIndex=0; childIndex<length; childIndex++) {
            writeValue("item>", Array.get(o, childIndex));
        }
        childIndex = oldChildIndex;
    }

    private void printGenericRecord(Object o, String name) {
        write((GenericRecord) o, name);
    }

    private void printAnnotatedFlag(Object o) {
        assert (!attributePhase);
        GenericRecordUtil.AnnotatedFlag af = (GenericRecordUtil.AnnotatedFlag) o;
        Number n = af.getValue().get();
        int len;
        if (n instanceof Byte) {
            len = 2;
        } else if (n instanceof Short) {
            len = 4;
        } else if (n instanceof Integer) {
            len = 8;
        } else {
            len = 16;
        }

        fw.print(" flag=\"0x");
        fw.print(trimHex(n.longValue(), len));
        fw.print('"');
        if (withComments) {
            fw.print(" description=\"");
            fw.print(af.getDescription());
            fw.print("\"");
        }
        fw.println("/>");
    }

    private void printBytes(Object o) {
        assert (!attributePhase);
        fw.write(">");
        fw.write(DatatypeConverter.printBase64Binary((byte[]) o));
    }

    private void printPoint(Object o) {
        assert (!attributePhase);
        Point2D p = (Point2D)o;
        fw.println(" x=\""+p.getX()+"\" y=\""+p.getY()+"\"/>");
    }

    private void printDimension(Object o) {
        assert (!attributePhase);
        Dimension2D p = (Dimension2D)o;
        fw.println(" width=\""+p.getWidth()+"\" height=\""+p.getHeight()+"\"/>");
    }

    private void printRectangle(Object o) {
        assert (!attributePhase);
        Rectangle2D p = (Rectangle2D)o;
        fw.println(" x=\""+p.getX()+"\" y=\""+p.getY()+"\" width=\""+p.getWidth()+"\" height=\""+p.getHeight()+"\"/>");
    }

    private void printPath(Object o) {
        assert (!attributePhase);
        final PathIterator iter = ((Path2D)o).getPathIterator(null);
        final double[] pnts = new double[6];

        indent += 2;
        String t = tabs();
        indent -= 2;

        boolean isNext = false;
        while (!iter.isDone()) {
            fw.print(t);
            isNext = true;
            final int segType = iter.currentSegment(pnts);
            fw.print("<pathelement ");
            switch (segType) {
                case PathIterator.SEG_MOVETO:
                    fw.print("type=\"move\" x=\""+pnts[0]+"\" y=\""+pnts[1]+"\"");
                    break;
                case PathIterator.SEG_LINETO:
                    fw.print("type=\"lineto\" x=\""+pnts[0]+"\" y=\""+pnts[1]+"\"");
                    break;
                case PathIterator.SEG_QUADTO:
                    fw.print("type=\"quad\" x1=\""+pnts[0]+"\" y1=\""+pnts[1]+"\" x2=\""+pnts[2]+"\" y2=\""+pnts[3]+"\"");
                    break;
                case PathIterator.SEG_CUBICTO:
                    fw.print("type=\"cubic\" x1=\""+pnts[0]+"\" y1=\""+pnts[1]+"\" x2=\""+pnts[2]+"\" y2=\""+pnts[3]+"\" x3=\""+pnts[4]+"\" y3=\""+pnts[5]+"\"");
                    break;
                case PathIterator.SEG_CLOSE:
                    fw.print("type=\"close\"");
                    break;
            }
            fw.println("/>");
            iter.next();
        }

    }

    private void printObject(Object o) {
        final Matcher m = ESC_CHARS.matcher(o.toString());
        final StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String repl;
            String match = m.group();
            switch (match) {
                case "<":
                    repl = "&lt;";
                    break;
                case ">":
                    repl = "&gt;";
                    break;
                case "&":
                    repl = "&amp;";
                    break;
                case "\'":
                    repl = "&apos;";
                    break;
                case "\"":
                    repl = "&quot;";
                    break;
                default:
                    repl = "&#x" + Long.toHexString(match.codePointAt(0)) + ";";
                    break;
            }
            m.appendReplacement(sb, repl);
        }
        m.appendTail(sb);
        fw.write(sb.toString());
    }

    private void printAffineTransform(Object o) {
        assert (!attributePhase);
        AffineTransform xForm = (AffineTransform)o;
        fw.write(
            " scaleX=\""+xForm.getScaleX()+"\" "+
            "shearX=\""+xForm.getShearX()+"\" "+
            "transX=\""+xForm.getTranslateX()+"\" "+
            "scaleY=\""+xForm.getScaleY()+"\" "+
            "shearY=\""+xForm.getShearY()+"\" "+
            "transY=\""+xForm.getTranslateY()+"\"/>");
    }

    private void printColor(Object o) {
        assert (attributePhase);
        final int rgb = ((Color)o).getRGB();
        fw.print("0x");
        fw.print(trimHex(rgb, 8));
    }

    private void printBufferedImage(Object o) {
        assert (!attributePhase);
        BufferedImage bi = (BufferedImage)o;
        fw.println(" width=\""+bi.getWidth()+"\" height=\""+bi.getHeight()+"\" bands=\""+bi.getColorModel().getNumComponents()+"\"/>");
    }

    private String trimHex(final long l, final int size) {
        final String b = Long.toHexString(l);
        int len = b.length();
        return ZEROS.substring(0, Math.max(0,size-len)) + b.substring(Math.max(0,len-size), len);
    }

}
