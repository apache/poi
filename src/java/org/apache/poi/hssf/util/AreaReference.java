/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */


package org.apache.poi.hssf.util;

public class AreaReference {


private CellReference [] cells;
private int dim;

    /** Create an area ref from a string representation
     */
    public AreaReference(String reference) {
        String[] refs = seperateAreaRefs(reference);
        dim = refs.length;
        cells = new CellReference[dim];
        for (int i=0;i<dim;i++) {
            cells[i]=new CellReference(refs[i]);
        }
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