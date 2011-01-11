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

package org.apache.poi.hsmf.dev;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.poi.hsmf.datatypes.MAPIAttribute;
import org.apache.poi.hsmf.datatypes.Types;

/**
 * Lists the different MAPI types
 */
public class TypesLister {
   public TypesLister() {}
   
   public void listByName(PrintStream out) {
      ArrayList<MAPIAttribute> all = new ArrayList<MAPIAttribute>(MAPIAttribute.getAll());
      Collections.sort(all, new Comparator<MAPIAttribute>() {
         public int compare(MAPIAttribute a, MAPIAttribute b) {
            return a.name.compareTo(b.name);
         }
      });
      list(all, out);
   }
   public void listById(PrintStream out) {
      ArrayList<MAPIAttribute> all = new ArrayList<MAPIAttribute>(MAPIAttribute.getAll());
      Collections.sort(all, new Comparator<MAPIAttribute>() {
         public int compare(MAPIAttribute a, MAPIAttribute b) {
            if(a.id < b.id) return -1;
            if(a.id > b.id) return +1;
            return 0;
         }
      });
      list(all, out);
   }
   private void list(ArrayList<MAPIAttribute> list, PrintStream out) {
      for(MAPIAttribute attr : list) {
         String id = Integer.toHexString(attr.id);
         while(id.length() < 4) { id = "0"+id; }
         
         out.println("0x" + id + " - " + attr.name);
         out.println("   " + attr.id + " - " + Types.asName(attr.usualType) + 
               " (" + attr.usualType + ") - " + attr.mapiProperty);
      }
   }
   
   public static void main(String[] args) {
      TypesLister lister = new TypesLister();
      
      lister.listByName(System.out);
      System.out.println();
      lister.listById(System.out);
   }
}
