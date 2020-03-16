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

package org.apache.poi.ss.usermodel;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.util.ImageUtils;
import org.apache.poi.util.Units;
import org.junit.Test;

public abstract class BaseTestPicture {

    private final ITestDataProvider _testDataProvider;

    protected BaseTestPicture(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    public void baseTestResize(Picture input, Picture compare, double scaleX, double scaleY) {
        input.resize(scaleX, scaleY);

        ClientAnchor inpCA = input.getClientAnchor();
        ClientAnchor cmpCA = compare.getClientAnchor();

        Dimension inpDim = ImageUtils.getDimensionFromAnchor(input);
        Dimension cmpDim = ImageUtils.getDimensionFromAnchor(compare);

        double emuPX = Units.EMU_PER_PIXEL;

        assertEquals("the image height differs", inpDim.getHeight(), cmpDim.getHeight(), emuPX*6);
        assertEquals("the image width differs", inpDim.getWidth(),  cmpDim.getWidth(),  emuPX*6);
        assertEquals("the starting column differs", inpCA.getCol1(), cmpCA.getCol1());
        assertEquals("the column x-offset differs", inpCA.getDx1(), cmpCA.getDx1(), 1);
        assertEquals("the column y-offset differs", inpCA.getDy1(), cmpCA.getDy1(), 1);
        assertEquals("the ending columns differs", inpCA.getCol2(), cmpCA.getCol2());
        // can't compare row heights because of variable test heights

        input.resize();
        inpDim = ImageUtils.getDimensionFromAnchor(input);

        Dimension imgDim = input.getImageDimension();

        assertEquals("the image height differs", imgDim.getHeight(), inpDim.getHeight()/emuPX, 1);
        assertEquals("the image width differs",  imgDim.getWidth(), inpDim.getWidth()/emuPX,  1);
    }


    @Test
    public void testResizeNoColumns() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();

            Row row = sheet.createRow(0);

            handleResize(wb, sheet, row);
        }
    }

    @Test
    public void testResizeWithColumns() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();

            Row row = sheet.createRow(0);
            row.createCell(0);

            handleResize(wb, sheet, row);
        }
    }


    private void handleResize(Workbook wb, Sheet sheet, Row row) throws IOException {
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        CreationHelper createHelper = wb.getCreationHelper();

        final byte[] bytes = HSSFITestDataProvider.instance.getTestDataFileContent("logoKarmokar4.png");

        row.setHeightInPoints(getImageSize(bytes).y);

        int pictureIdx = wb.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);

        //add a picture shape
        ClientAnchor anchor = createHelper.createClientAnchor();
        //set top-left corner of the picture,
        //subsequent call of Picture#resize() will operate relative to it
        anchor.setCol1(0);
        anchor.setRow1(0);

        Picture pict = drawing.createPicture(anchor, pictureIdx);

        //auto-size picture relative to its top-left corner
        pict.resize();
    }

    private static Point getImageSize( byte [] image) throws IOException {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(image));

        assertNotNull(img);

        return new Point(img.getWidth(), img.getHeight());
    }

    @Test
    public void bug64213() throws IOException {
        int[] input = {
            200, 50 * 256, -1,
            400, 50 * 256, -1,
            200, 50 * 256, 200,
            400, 50 * 256, 200,
            400, 50 * 256, 400,
        };

        int[] expXSSF = {
            2, 952500, 2, 0, 2, 1905000, 7, 0,
            2, 952500, 2, 0, 2, 2857500, 12, 0,
            2, 952500, 2, 0, 2, 1905000, 2, 952500,
            2, 952500, 2, 0, 2, 2857500, 3, 0,
            2, 952500, 2, 0, 2, 2857500, 2, 1905000
        };

        int[] expHSSF = {
            2, 292, 2, 0, 2, 584, 7, 226,
            2, 292, 2, 0, 2, 877, 13, 196,
            2, 292, 2, 0, 2, 584, 2, 128,
            2, 292, 2, 0, 2, 877, 3, 0,
            2, 292, 2, 0, 2, 877, 2, 128
        };

        int[] expected = "xls".equals(_testDataProvider.getStandardFileNameExtension()) ? expHSSF : expXSSF;

        for (int i=0; i<5; i++) {
            int[] inp = Arrays.copyOfRange(input, i*3, (i+1)*3);
            int[] exp = Arrays.copyOfRange(expected, i*8, (i+1)*8);
            int[] act = bug64213Helper(inp[0], inp[1], inp[2]);
            assertArrayEquals(exp, act);
        }
    }

    private int[] bug64213Helper(int imgDim, int colWidth, int rowHeight) throws IOException {
        final int col1 = 2;
        final int dx1Pixel = 100;
        final int row1 = 2;
        final float scale = 0.5f;

        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");

            final byte[] bytes = createTestImage(imgDim, imgDim);
            int pictureIdx = wb.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);

            sheet.setColumnWidth(col1, colWidth);
            float col1px = sheet.getColumnWidthInPixels(col1);
            if (rowHeight > -1) {
                sheet.createRow(row1).setHeightInPoints((float) Units.pixelToPoints(rowHeight));
            }

            //create an anchor with upper left cell column/startRow, only one cell anchor since bottom right depends on resizing
            CreationHelper helper = wb.getCreationHelper();
            ClientAnchor anchor = helper.createClientAnchor();
            anchor.setCol1(col1);
            if (wb instanceof HSSFWorkbook) {
                anchor.setDx1((int)(dx1Pixel * 1024 / col1px));
            } else {
                anchor.setDx1(dx1Pixel * Units.EMU_PER_PIXEL);
            }
            anchor.setRow1(row1);


            //create a picture anchored to Col1 and Row1
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            Picture pict = drawing.createPicture(anchor, pictureIdx);

            //resize the picture to it's native size
            pict.resize();

            //resize the picture scaled proportional
            pict.resize(scale);

            ClientAnchor anc = (ClientAnchor)pict.getAnchor();
            return new int[]{
                anc.getCol1(), anc.getDx1(),
                anc.getRow1(), anc.getDy1(),
                anc.getCol2(), anc.getDx2(),
                anc.getRow2(), anc.getDy2()
            };
        }
    }

    private static byte[] createTestImage(double width, double height) throws IOException {
        BufferedImage bi = new BufferedImage((int)width, (int)height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        g.scale((width-1)/width, (height-1)/height);

        g.setStroke(new BasicStroke(1));

        double ellSize = 5/6d;
        Ellipse2D ell = new Ellipse2D.Double(width * (1-ellSize)/2d, height * (1-ellSize)/2d, width * ellSize, height * ellSize);
        Color[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW };
        Rectangle2D rect = new Rectangle2D.Double();
        for (int i=0; i<4; i++) {
            rect.setRect(width/2 * (i&1),height/2 * (i>>1&1), width/2, height/2);
            Area a = new Area(rect);
            a.subtract(new Area(ell));
            g.setPaint(colors[i]);
            g.fill(a);
            g.setColor(java.awt.Color.BLACK);
            g.draw(rect);
        }
        g.draw(ell);

        g.dispose();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(2000);
        ImageIO.write(bi, "PNG", bos);
        return bos.toByteArray();
    }
}
