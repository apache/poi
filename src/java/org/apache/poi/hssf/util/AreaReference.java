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


package org.apache.poi.hssf.util;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class AreaReference {


private CellReference [] cells;
private int dim;

    /**
     * Create an area ref from a string representation.
     * The area reference must be contiguous
     */
    public AreaReference(String reference) {
        if(! isContiguous(reference)) {
            throw new IllegalArgumentException("References passed to the AreaReference must be contiguous, use generateContiguous(ref) if you have non-contiguous references");
        }

        String[] refs = seperateAreaRefs(reference);
        dim = refs.length;
        cells = new CellReference[dim];
        for (int i=0;i<dim;i++) {
            cells[i]=new CellReference(refs[i]);
        }
    }

    /**
     * Is the reference for a contiguous (i.e.
     *  unbroken) area, or is it made up of
     *  several different parts?
     * (If it is, you will need to call
     *  ....
     */
    public static boolean isContiguous(String reference) {
        if(reference.indexOf(',') == -1) {
            return true;
        }
        return false;
    }

    /**
     * Takes a non-contiguous area reference, and
     *  returns an array of contiguous area references.
     */
    public static AreaReference[] generateContiguous(String reference) {
        ArrayList refs = new ArrayList();
        StringTokenizer st = new StringTokenizer(reference, ",");
        while(st.hasMoreTokens()) {
            refs.add(
                    new AreaReference(st.nextToken())
            );
        }
        return (AreaReference[])refs.toArray(new AreaReference[refs.size()]);
    }

    //not sure if we need to be flexible here!
    /** return the dimensions of this area
     **/
    public int getDim() {
        return dim;
    }
    /** return the cell references that define this area */
    public CellReference[] getCells() {
        return cells;
    }

    public String toString() {
        StringBuffer retval = new StringBuffer();
        for (int i=0;i<dim;i++){
            retval.append(':');
            retval.append(cells[i].toString());
        }
        retval.deleteCharAt(0);
        return retval.toString();
    }

    /**
     * seperates Area refs in two parts and returns them as seperate elements in a
     * String array
     */
    private String[] seperateAreaRefs(String reference) {
        String[] retval = null;

        int length = reference.length();

        int loc = reference.indexOf(':',0);
        if(loc == -1){
           retval = new String[1];
           retval[0] = reference;
        }
        else{
           retval = new String[2];
           int sheetStart = reference.indexOf("!");

           retval[0] = reference.substring(0, sheetStart+1) + reference.substring(sheetStart + 1,loc);
           retval[1] = reference.substring(0, sheetStart+1) + reference.substring(loc+1);
        }
        return retval;
    }
}