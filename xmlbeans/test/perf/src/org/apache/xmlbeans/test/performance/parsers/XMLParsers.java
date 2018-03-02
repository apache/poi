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
package org.apache.xmlbeans.test.performance.parsers;

import java.io.File;


/**
 * @author Cezar Andrei (cezar.andrei at bea.com)
 *         Date: Jul 11, 2005
 */
public class XMLParsers
{
    public static void main(String[] args)
        throws Exception
    {
        runJaxb2Tests();
        runXBTests();

        runPiccoloTests();
        runDefaultJDKParserTests();
        runXercesSaxParserTests();
    }

    public static void runPiccoloTests()
        throws Exception
    {
        Utils.ParseFile doNothingTest = new PiccoloPerfTests.DoNothingParseFile();
        runAllFiles(doNothingTest);

        Utils.ParseFile copyDataTest = new PiccoloPerfTests.CopyDataParseFile();
        runAllFiles(copyDataTest);

        Utils.ParseFile copyAndStoreDataTest = new PiccoloPerfTests.CopyAndStoreDataParseFile();
        runAllFiles(copyAndStoreDataTest);
    }

    public static void runDefaultJDKParserTests()
        throws Exception
    {
        Utils.ParseFile doNothingTest = new DefaultSaxParserPerfTests.DoNothingParseFile();
        runAllFiles(doNothingTest);

        Utils.ParseFile copyDataTest = new DefaultSaxParserPerfTests.CopyDataParseFile();
        runAllFiles(copyDataTest);

        Utils.ParseFile copyAndStoreDataTest = new DefaultSaxParserPerfTests.CopyAndStoreDataParseFile();
        runAllFiles(copyAndStoreDataTest);
    }

    public static void runXercesSaxParserTests()
        throws Exception
    {
        Utils.ParseFile doNothingTest = new XercesSaxParserPerfTests.DoNothingParseFile();
        runAllFiles(doNothingTest);

        Utils.ParseFile copyDataTest = new XercesSaxParserPerfTests.CopyDataParseFile();
        runAllFiles(copyDataTest);

        Utils.ParseFile copyAndStoreDataTest = new XercesSaxParserPerfTests.CopyAndStoreDataParseFile();
        runAllFiles(copyAndStoreDataTest);
    }

    public static void runJaxb2Tests()
        throws Exception
    {
        Utils.ParseFile loadOnly = new Jaxb2Tests.LoadOnly();
        runAllFiles(loadOnly);

        Utils.ParseFile loadAndTraverse = new Jaxb2Tests.LoadAndTraverse();
        runAllFiles(loadAndTraverse);

        Utils.ParseFile createonly = new Jaxb2Tests.CreateOnly();
        runAllFiles2(createonly);

        Utils.ParseFile createAndSave1 = new Jaxb2Tests.CreateAndSave1();
        runAllFiles2(createAndSave1);

        Utils.ParseFile createAndSave2 = new Jaxb2Tests.CreateAndSave2();
        runAllFiles2(createAndSave2);
    }

    public static void runXBTests()
        throws Exception
    {
        Utils.ParseFile loadOnly = new XmlBeansTests.LoadOnly();
        runAllFiles(loadOnly);

        Utils.ParseFile loadAndTraverseArray = new XmlBeansTests.LoadAndTraverseArray();
        runAllFiles(loadAndTraverseArray);

        Utils.ParseFile loadAndTraverseItem = new XmlBeansTests.LoadAndTraverseItem();
        runAllFiles(loadAndTraverseItem);


        Utils.ParseFile createOnly = new XmlBeansTests.CreateOnly();
        runAllFiles2(createOnly);

        Utils.ParseFile createAndSave = new XmlBeansTests.CreateAndSave();
        runAllFiles2(createAndSave);
    }

    private static void runAllFiles(Utils.ParseFile parseTest)
        throws Exception
    {
        parseTest.run(Utils.file1k);
        parseTest.run(Utils.file1k);
        parseTest.run(Utils.file10k);
        parseTest.run(Utils.file100k);
        parseTest.run(Utils.file1M);
        parseTest.run(Utils.file10M);
    }

    private static void runAllFiles2(Utils.ParseFile parseTest)
        throws Exception
    {
        parseTest.run("2");
        parseTest.run("2");
        parseTest.run("36");
        parseTest.run("377");
        parseTest.run("3863");
        parseTest.run("38630");
    }


}


