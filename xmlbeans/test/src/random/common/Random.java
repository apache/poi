/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package random.common;


import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.tool.CommandLine;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Arrays;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.xml.stream.XMLInputStream;

import com.easypo.XmlPurchaseOrderDocumentBean.PurchaseOrder;
import com.easypo.XmlCustomerBean;
import com.easypo.XmlLineItemBean;
import com.easypo.XmlShipperBean;

public class Random implements Runnable {

   static long seed;
   static int iterations;
   static int threads;
   static int docs;


    public static void runTest(CommandLine cl) {
        if (cl.getOpt("?") != null || cl.getOpt("help") != null ||
                cl.args().length != 0)
            System.out.println(
                    "Usage: random [-seed #] [-i iterations] [-t threads] [-docs docs] [-readonly] [-nosave]");
        else {
            boolean readonly = false;
            boolean nosave = false;
            boolean noquery = false;

            if (cl.getOpt("seed") != null)
                seed = Long.parseLong(cl.getOpt("seed"));
            if (cl.getOpt("i") != null)
                iterations = Integer.parseInt(cl.getOpt("i"));
            if (cl.getOpt("t") != null)
                threads = Integer.parseInt(cl.getOpt("t"));
            if (cl.getOpt("docs") != null)
                docs = Integer.parseInt(cl.getOpt("docs"));
            noquery = (cl.getOpt("noquery") != null);
            readonly = (cl.getOpt("readonly") != null);
            nosave = (cl.getOpt("nosave") != null);

            System.out.println("seed=" + seed);
            System.out.println("iterations=" + iterations);
            System.out.println("threads=" + threads);
            System.out.println("docs=" + docs);
            System.out.println(readonly ? "readonly" : "read/write");
            System.out.println(nosave ? "nosave" : "with save");
            System.out.println(noquery ? "noquery" : "with query");
            doTests(seed, iterations, threads, docs, readonly, nosave, noquery);
        }
    }

    private static void doTests(long seed, int iterations, int threadCount,
                                int docCount, boolean readonly, boolean nosave,
                                boolean noquery) {
        long start = System.currentTimeMillis();

        XmlObject[] sharedDocs = new XmlObject[docCount];
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < iterations; i++) {
            for (int j = 0; j < threadCount; j++) {
                Random runnable = new Random(seed, sharedDocs, readonly,
                        nosave, noquery, threadCount > 1);
                threads[j] = new Thread(runnable);
                threads[j].start();
                seed++;
            }
            for (int j = 0; j < threadCount; j++) {
                try {
                    threads[j].join();
                }
                catch (InterruptedException e) {
                    System.err.println("Thread interrupted");
                }
            }
        }

        long end = System.currentTimeMillis();

        System.err.println();
        System.err.println(
                "Seconds to run random tests: " + (end - start) / 1000);
    }

    public void run() {
        System.err.print("\rSeed: " + _seed);

        try {
            for (int d = 0; d < _docs.length; d++)
                _docs[d] = XmlObject.Factory.newInstance();

            _cursors = new ArrayList();

            int nIterations = rnd(60000) + 5000;

            for (int i = 0; i < nIterations; i++) {
                boolean good = false;
                try {
                    _iter++;
                    iterate();
                    good = true;
                }
                finally {
                    if (!good) {
                        System.err.println();
                        System.err.println("Error on iteration " + _iter);
                        System.err.println();
                    }
                }
            }
        }
        catch (Throwable e) {
            System.err.println("Error on seed " + _seed);
            e.printStackTrace(System.err);
        }
    }

    private java.util.Random _rnd;

    private XmlObject[] _docs; // shared among threads!!
    private ArrayList _cursors;
    private long _seed;
    private int _iter;
    private boolean _readonly;
    private boolean _nosave;
    private boolean _noquery;
    private boolean _interference;


    private int rnd(int n) {
        return _rnd.nextInt(n);
    }

    private int rnd(int min, int maxPlusOne) {
        return _rnd.nextInt(maxPlusOne - min) + min;
    }

    private XmlCursor getCursor() {
        int n = _cursors.size();

        if (n == 0 || (n < _docs.length * 3) && rnd(4) == 0) {
            XmlCursor c = _docs[rnd(_docs.length)].newCursor();
            _cursors.add(c);
            return c;
        }

        return (XmlCursor) _cursors.get(rnd(n));
    }

    private void iterate() throws Exception {
        try {
            switch (rnd(6)) {
                case 0:
                    interateHigh();
                    break;
                case 1:
                    interateHigh();
                    break;
                case 2:
                    interateHigh();
                    break;
                case 3:
                    interateMedium();
                    break;
                case 4:
                    interateMedium();
                    break;
                case 5:
                    interateLow();
                    break;
            }
        }
        catch (IllegalStateException e) {
            if (!_interference)
                throw e;
        }
        catch (IllegalArgumentException e) {
            if (!_interference)
                throw e;
        }
        catch (XmlValueDisconnectedException e) {

        }
    }

    private void interateHigh() throws Exception {
        switch (rnd(2)) {
            case 0:
                moveCursorRightOneToken();
                break;
            case 1:
                moveCursorLeftOneToken();
                break;
        }
    }

    private void interateMedium() throws Exception {
        switch (_readonly ? rnd(4) : rnd(24)) {
            case 0:
                getChars();
                break;
            case 1:
                getTextValue();
                break;
            case 2:
                compareValue();
                break;
            case 3:
                prevTokenType();
                break;

            case 4:
                insertText();
                break;
            case 5:
                moveCharsRight();
                break;
            case 6:
                moveCharsLeft();
                break;
            case 7:
                insertElem();
                break;
            case 8:
                insertAttr();
                break;
            case 9:
                removeXml();
                break;
            case 10:
                removeXmlContents();
                break;
            case 11:
                copyXml();
                break;
            case 12:
                copyXmlContents();
                break;
            case 13:
                moveXml();
                break;
            case 14:
                moveXmlContents();
                break;
            case 15:
                removeText();
                break;
            case 16:
                moveText();
                break;
            case 17:
                copyText();
                break;
            case 18:
                insertComment();
                break;
            case 19:
                insertProcinst();
                break;
            case 20:
                setTextValue();
                break;
            case 21:
                setStrong();
                break;
            case 22:
                objectSet();
                break;
            case 23:
                setName();
                break;
        }
    }

    private void interateLow() throws Exception {
        switch (rnd(_readonly ? 1 : 0, _nosave ? 8 : 17)) {
            case 0:
                changeType();
                break;
            case 1:
                createBookmark();
                break;
            case 2:
                clearBookmark();
                break;

            case 3:
                loadDoc();
                break;
            case 4:
                loadSchemadDoc();
                break;
            case 5:
                compareCursors();
                break;
            case 6:
                getObject();
                break;
            case 7:
                newCursor();
                break;

            case 8:
                validate();
                break;
            case 9:
                execQuery();
                break;
            case 10:
                xmlInputStream();
                break;
            case 11:
                docBytes();
                break;
            case 12:
                cursorBytes();
                break;
            case 13:
                newDomNode();
                break;
            case 14:
                objectString();
                break;
            case 15:
                cursorString();
                break;
            case 16:
                prettyObject();
                break;
        }
    }

    private XmlObject findObject() {
        XmlCursor c = getCursor();
        c.push();

        while (!(c.isContainer() || c.isAttr()))
            if (c.toNextToken().isNone())
                break;

        if (!c.isEnddoc()) {
            XmlObject x = c.getObject();
            c.pop();
            if (x == null)
                throw new IllegalStateException(
                        "getObject returned null - content must have changed");
            return x;
        }

        c.pop();
        c.push();

        while (!(c.isContainer() || c.isAttr()))
            if (c.toPrevToken().isNone())
                break;

        XmlObject x = c.getObject();
        c.pop();
        if (x == null)
            throw new IllegalStateException(
                    "getObject returned null - content must have changed");
        return x;
    }

    private void prettyObject() {
        XmlOptions options = new XmlOptions();
        options.setSavePrettyPrint();
        findObject().xmlText(options);
    }

    private void objectString() {
        findObject().toString();
    }

    private void cursorString() {
        getCursor().toString();
    }

    private void changeType() {
        XmlObject o, n;

        try {
            o = findObject();
            SchemaType type = findObject().schemaType();
            n = o.changeType(type);
        }
        catch (IllegalArgumentException e) {
            return;
        }
        catch (IllegalStateException e) {
            return;
        }

        for (int i = 0; i < _docs.length; i++) {
            if (o == _docs[i]) {
                _docs[i] = n;
                break;
            }
        }
    }

    private void prevTokenType() {
        getCursor().prevTokenType();
    }

    private void newCursor() {
        findObject().newCursor();
    }

    private void setName() {
        XmlCursor c = findObject().newCursor();

        if (!c.isStartdoc())
            c.setName(getQName());

        c.dispose();
    }

    private void newDomNode() {
        if (rnd(5) != 0)
            return;

        try {
            getCursor().newDomNode();
        }
        catch (IllegalStateException e) {
        }
    }

    private void xmlInputStream() throws Exception {
        if (rnd(5) != 0)
            return;

        XMLInputStream xis;

        try {
            xis = getCursor().newXMLInputStream();
        }
        catch (IllegalStateException e) {
            return;
        }

        while (xis.next() != null)
            ;
    }

    private void objectSet() {
        findObject().set(findObject());
    }

    private void setStrong() {
        XmlObject x = findObject();

        if (x instanceof PurchaseOrder) {
            PurchaseOrder o = (PurchaseOrder) x;
            o.setDate(Calendar.getInstance());
        } else if (x instanceof XmlCustomerBean) {
            XmlCustomerBean o = (XmlCustomerBean) x;
            o.setName("Bob");

            if (rnd(2) == 0)
                o.setAge(23);

            if (rnd(2) == 0)
                o.setMoo(24);

            if (rnd(2) == 0)
                o.setPoo(200);
        } else if (x instanceof XmlLineItemBean) {
            XmlLineItemBean o = (XmlLineItemBean) x;
            o.setPerUnitOunces(new BigDecimal(122.44));
            o.setPrice(new BigDecimal(555.33));
            o.setQuantity(BigInteger.valueOf(111));
        } else if (x instanceof XmlShipperBean) {
            XmlShipperBean o = (XmlShipperBean) x;
            o.setPerOunceRate(new BigDecimal(3.14159));
            o.setName("Eric");
        }
    }

    private void compareValue() {
        findObject().compareValue(findObject());
    }

    private void validate() {
        findObject().validate();
    }

    private void execQuery() {
        if (_noquery)
            return;

        if (rnd(20) > 0)
            return;

        QName name = getQName();

        String query =
                "declare namespace xxx='" + name.getNamespaceURI() + "' " +
                ".//xxx:" + name.getLocalPart();

        XmlObject x = getCursor().execQuery(query).getObject();

        if (rnd(3) == 0)
            _docs[rnd(_docs.length)] = x;
    }

    private void getObject() {
        getCursor().getObject();
    }

    private void getChars() {
        getCursor().getChars();
    }

    private void compareCursors() {
        try {
            getCursor().isInSameDocument(getCursor());
            getCursor().comparePosition(getCursor());
            getCursor().isAtSamePositionAs(getCursor());
        }
        catch (IllegalArgumentException e) {
        }
    }

    private String[] _xmls =
            {
                "<a/>",
            };

    private String[] _schema_xmls =
            {
                "<po:purchase-order xmlns:po='http://openuri.org/easypo'>\n" +
            "<po:customer age='31' poo='200'>\n" +
            "<po:name>David Bau</po:name>\n" +
            "<po:address>Gladwyne, PA</po:address>\n" +
            "</po:customer>\n" +
            "<po:customer age='37'>\n" +
            "<po:name>Eric Vasilik</po:name>\n" +
            "<po:address>Redmond, WA</po:address>\n" +
            "</po:customer>\n" +
            "<po:date>2002-09-30T14:16:00-05:00</po:date>\n" +
            "<po:line-item>\n" +
            "<po:description>Burnham's Celestial Handbook, Vol 1</po:description>\n" +
            "<po:per-unit-ounces>5</po:per-unit-ounces>\n" +
            "<po:price>21.79</po:price>\n" +
            "<po:quantity>2</po:quantity>\n" +
            "</po:line-item>\n" +
            "<po:line-item>\n" +
            "<po:description>Burnham's Celestial Handbook, Vol 2</po:description>\n" +
            "<po:per-unit-ounces>5</po:per-unit-ounces>\n" +
            "<po:price>19.89</po:price>\n" +
            "<po:quantity>2</po:quantity>\n" +
            "</po:line-item>\n" +
            "<po:line-item>\n" +
            "<po:description>Burnham's Celestial Handbook, Vol 3</po:description>\n" +
            "<po:per-unit-ounces>5</po:per-unit-ounces>\n" +
            "<po:price>19.89</po:price>\n" +
            "<po:quantity>1</po:quantity>\n" +
            "</po:line-item>\n" +
            "<po:shipper>\n" +
            "<po:name>UPS</po:name>\n" +
            "<po:per-ounce-rate>0.74</po:per-ounce-rate>\n" +
            "</po:shipper>\n" +
            "</po:purchase-order>\n" +
            "",
            };

    private void loadDoc() throws Exception {
        if (rnd(15) == 0) {
            _docs[rnd(_docs.length)] =
                    XmlObject.Factory.parse(_xmls[rnd(_xmls.length)]);
        }
    }

    private void loadSchemadDoc() throws Exception {
        if (rnd(4) == 0) {
            _docs[rnd(_docs.length)] =
                    XmlObject.Factory.parse(
                            _schema_xmls[rnd(_schema_xmls.length)]);
        }
    }

    private void moveCursorLeftOneToken() {
        getCursor().toPrevToken();
    }

    private void moveCursorRightOneToken() {
        getCursor().toNextToken();
    }

    private char[] _chars =
            {
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                ' ', '<', '>', '&', '-', '?'
            };

    private void getTextValue() {
        XmlCursor c = getCursor();

        if (c.isFinish() || c.isNamespace() || c.isText())
            return;

        c.getTextValue();
    }

    private void setTextValue() {
        XmlCursor c = getCursor();

        if (c.isFinish() || c.isNamespace() || c.isText())
            return;

        StringBuffer sb = new StringBuffer();

        for (int i = rnd(10); i >= 0; i--)
            sb.append(_chars[rnd(_chars.length)]);

        c.setTextValue(sb.toString());
    }

    private void moveText() {
        try {
            getCursor().moveChars(rnd(10), getCursor());
        }
        catch (IllegalArgumentException e) {
        }
        catch (IllegalStateException e) {
        }
    }

    private void copyText() {
        try {
            getCursor().copyChars(rnd(10), getCursor());
        }
        catch (IllegalArgumentException e) {
        }
        catch (IllegalStateException e) {
        }
    }

    private void removeText() {
        getCursor().removeChars(rnd(6));
    }

    private void insertComment() {
        try {
            getCursor().insertComment("poo");
        }
        catch (IllegalArgumentException e) {
        }
        catch (IllegalStateException e) {
        }
    }

    private void insertProcinst() {
        try {
            getCursor().insertProcInst("target", "val");
        }
        catch (IllegalArgumentException e) {
        }
        catch (IllegalStateException e) {
        }
    }

    private void insertText() {
        XmlCursor c = getCursor();

        if (c.isAnyAttr() || c.isStartdoc())
            return;

        StringBuffer sb = new StringBuffer();

        for (int i = rnd(10); i >= 0; i--)
            sb.append(_chars[rnd(_chars.length)]);

        c.insertChars(sb.toString());
    }

    private void docChars() {
        _docs[rnd(_docs.length)].xmlText();
    }

    private void cursorChars() {
        getCursor().xmlText();
    }

    private void docBytes() throws Exception {
        _docs[rnd(_docs.length)].save(new ByteArrayOutputStream());
    }

    private void cursorBytes() throws Exception {
        getCursor().save(new ByteArrayOutputStream());
    }

    private void moveCharsRight() throws Exception {
        getCursor().toNextChar(rnd(10));
    }

    private void moveCharsLeft() throws Exception {
        getCursor().toPrevChar(rnd(10));
    }

    private QName getQName() {
        QName name = null;

        switch (rnd(3)) {
            case 0:
                name = XmlBeans.getQName("foo.com", "foo");
                break;
            case 1:
                name = XmlBeans.getQName("bar.com", "bar");
                break;
            case 2:
                name = XmlBeans.getQName("moo");
                break;
        }

        return name;
    }

    private void insertElem() throws Exception {
        XmlCursor c = getCursor();

        if (c.isAnyAttr() || c.isStartdoc())
            return;

        c.insertElement(getQName());
    }

    public void insertAttr() {
        XmlCursor c = getCursor();

        while (!c.isEnddoc() && !c.isContainer())
            c.toNextToken();

        if (c.isEnddoc())
            return;

        c.toNextToken();

        c.insertAttribute(getQName());
    }

    public void removeXmlContents() {
        XmlCursor c = getCursor();

        c.removeXmlContents();
    }

    public void copyXmlContents() {
        try {
            getCursor().copyXmlContents(getCursor());
        }
        catch (IllegalArgumentException e) {
        }
        catch (IllegalStateException e) {
        }
    }

    public void copyXml() {
        try {
            getCursor().copyXml(getCursor());
        }
        catch (IllegalArgumentException e) {
        }
        catch (IllegalStateException e) {
        }
    }

    public void moveXmlContents() {
        try {
            getCursor().moveXmlContents(getCursor());
        }
        catch (IllegalArgumentException e) {
        }
        catch (IllegalStateException e) {
        }
    }

    public void moveXml() {
        try {
            getCursor().moveXml(getCursor());
        }
        catch (IllegalArgumentException e) {
        }
        catch (IllegalStateException e) {
        }
    }

    public void removeXml() {
        XmlCursor c = getCursor();

        if (!c.isStartdoc() && !c.isFinish())
            c.removeXml();
    }

    public static class Bookmark extends XmlCursor.XmlBookmark {
    }

    public void createBookmark() throws Exception {
        getCursor().setBookmark(new Bookmark());
    }

    public void clearBookmark() throws Exception {
        getCursor().clearBookmark(Bookmark.class);
    }

    public Random(long seed, XmlObject[] sharedDocs, boolean readonly,
                  boolean nosave, boolean noquery, boolean interference) {
        _seed = seed;
        _rnd = new java.util.Random(seed);
        _docs = sharedDocs;
        _readonly = readonly;
        _nosave = nosave;
        _noquery = noquery;
        _interference = interference;
    }

}

