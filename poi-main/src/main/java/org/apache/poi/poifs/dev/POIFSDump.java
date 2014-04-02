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
package org.apache.poi.poifs.dev;

import org.apache.poi.poifs.filesystem.*;

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.Iterator;

/**
 *
 * Dump internal structure of a OLE2 file into file system
 *
 * @author Yegor Kozlov
 */
public class POIFSDump {

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            System.out.println("Dumping " + args[i]);
            FileInputStream is = new FileInputStream(args[i]);
            POIFSFileSystem fs = new POIFSFileSystem(is);
            is.close();

            DirectoryEntry root = fs.getRoot();
            File file = new File(root.getName());
            file.mkdir();

            dump(root, file);
        }
   }


    public static void dump(DirectoryEntry root, File parent) throws IOException {
        for(Iterator it = root.getEntries(); it.hasNext();){
            Entry entry = (Entry)it.next();
            if(entry instanceof DocumentNode){
                DocumentNode node = (DocumentNode)entry;
                DocumentInputStream is = new DocumentInputStream(node);
                byte[] bytes = new byte[node.getSize()];
                is.read(bytes);
                is.close();

                FileOutputStream out = new FileOutputStream(new File(parent, node.getName().trim()));
                out.write(bytes);
                out.close();
            } else if (entry instanceof DirectoryEntry){
                DirectoryEntry dir = (DirectoryEntry)entry;
                File file = new File(parent, entry.getName());
                file.mkdir();
                dump(dir, file);
            } else {
                System.err.println("Skipping unsupported POIFS entry: " + entry);
            }
        }
    }
}
