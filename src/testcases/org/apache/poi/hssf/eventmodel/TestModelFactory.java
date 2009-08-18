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

package org.apache.poi.hssf.eventmodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.model.Model;
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import junit.framework.TestCase;

/**
 * Tests the ModelFactory.
 *
 * @author Andrew C. Oliver acoliver@apache.org
 */
public class TestModelFactory extends TestCase {
    private ModelFactory factory;
    private HSSFWorkbook book;
    private InputStream  in;
    private List         models;

    protected void setUp() throws Exception
    {
        ModelFactory mf = new ModelFactory();
        assertTrue("listeners member cannot be null", mf.listeners != null);
        models = new ArrayList(3);
        factory = new ModelFactory();
        book = new HSSFWorkbook();
        ByteArrayOutputStream stream = setupRunFile(book);
        POIFSFileSystem fs = new POIFSFileSystem(
                                   new ByteArrayInputStream(stream.toByteArray())
                                   );
        in = fs.createDocumentInputStream("Workbook");
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        factory = null;
        book = null;
        in = null;
    }

    /**
     * tests that listeners can be registered
     */
    public void testRegisterListener()
    {
        if (factory.listeners.size() != 0) {
         factory = new ModelFactory();
        }

        factory.registerListener(new MFListener(null));
        factory.registerListener(new MFListener(null));
        assertTrue("Factory listeners should be two, was="+
                                  factory.listeners.size(),
                    factory.listeners.size() == 2);
    }

    /**
     * tests that given a simple input stream with one workbook and sheet
     * that those models are processed and returned.
     */
    public void testRun()
    {
        Model temp = null;
        Iterator mi = null;

        if (factory.listeners.size() != 0) {
         factory = new ModelFactory();
        }

        factory.registerListener(new MFListener(models));
        factory.run(in);

        assertTrue("Models size must be 2 was = "+models.size(),
                                             models.size() == 2);
        mi = models.iterator();
        temp = (Model)mi.next();

        assertTrue("First model is Workbook was " + temp.getClass().getName(),
                    temp instanceof Workbook);

        temp = (Model)mi.next();

        assertTrue("Second model is Sheet was " + temp.getClass().getName(),
                    temp instanceof Sheet);

    }

    /**
     * Sets up a test file
     */
    private ByteArrayOutputStream setupRunFile(HSSFWorkbook book) throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        HSSFSheet sheet = book.createSheet("Test");
        HSSFRow   row   = sheet.createRow(0);
        HSSFCell  cell  = row.createCell(0);
        cell.setCellValue(10.5);
        book.write(stream);
        return stream;
    }

}

/**
 * listener for use in the test
 */
class MFListener implements ModelFactoryListener {
    private List mlist;
    public MFListener(List mlist) {
      this.mlist = mlist;
    }

    public boolean process(Model model)
    {
        mlist.add(model);
        return true;
    }

    public Iterator models() {
        return mlist.iterator();
    }
}
