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

package org.apache.poi.hwpf;

import org.apache.poi.hwpf.model.FileInformationBlock;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.POIDataSamples;

import java.io.IOException;


public final class HWPFDocFixture
{
  public static final String DEFAULT_TEST_FILE = "test.doc";
  
  public byte[] _tableStream;
  public byte[] _mainStream;
  public FileInformationBlock _fib;
  private final String _testFile;

  public HWPFDocFixture(Object obj, String testFile)
  {
    _testFile = testFile;
  }

  public void setUp() throws IOException {
      POIFSFileSystem filesystem = new POIFSFileSystem(
              POIDataSamples.getDocumentInstance().openResourceAsStream(_testFile));

      DocumentEntry documentProps =
        (DocumentEntry) filesystem.getRoot().getEntry("WordDocument");
      _mainStream = new byte[documentProps.getSize()];
      filesystem.createDocumentInputStream("WordDocument").read(_mainStream);

      // use the fib to determine the name of the table stream.
      _fib = new FileInformationBlock(_mainStream);

      String name = "0Table";
      if (_fib.getFibBase().isFWhichTblStm())
      {
        name = "1Table";
      }

      // read in the table stream.
      DocumentEntry tableProps =
        (DocumentEntry) filesystem.getRoot().getEntry(name);
      _tableStream = new byte[tableProps.getSize()];
      filesystem.createDocumentInputStream(name).read(_tableStream);

      _fib.fillVariableFields(_mainStream, _tableStream);
  }

  public void tearDown()
  {
  }

}
