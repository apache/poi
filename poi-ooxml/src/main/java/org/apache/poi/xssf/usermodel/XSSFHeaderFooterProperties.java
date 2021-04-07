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

package org.apache.poi.xssf.usermodel;

import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTHeaderFooter;

/**
 * <p>
 * All Header/Footer properties for a sheet are scoped to the sheet. This includes Different First Page,
 * and Different Even/Odd. These properties can be set or unset explicitly in this class. Note that while
 * Scale With Document and Align With Margins default to unset, Different First, and Different Even/Odd
 * are updated automatically as headers and footers are added and removed. 
 * </p>
 */
public class XSSFHeaderFooterProperties {
	private CTHeaderFooter headerFooter;

	/**
	 * Create an instance of XSSFHeaderFooterProperties from the supplied XML bean
	 *
	 * @param headerFooter
	 */
	public XSSFHeaderFooterProperties(CTHeaderFooter headerFooter) {
        this.headerFooter = headerFooter;
	}

	/**
	 * Returns the underlying CTHeaderFooter xml bean
	 *
	 * @return the underlying CTHeaderFooter xml bean
	 */
    @Internal
	public CTHeaderFooter getHeaderFooter() {
		return this.headerFooter;
	}

    /**
	 * returns alignWithMargins attribute
	 */
	public boolean getAlignWithMargins() {
	  return getHeaderFooter().isSetAlignWithMargins() ? getHeaderFooter().getAlignWithMargins() : false;
	}
	
	/**
	 * returns differentFirst attribute
	 */
	public boolean getDifferentFirst() {
	    return getHeaderFooter().isSetDifferentFirst() ? getHeaderFooter().getDifferentFirst() : false;
	}
	
	/**
	 * returns differentOddEven attribute
	 */
	public boolean getDifferentOddEven() {
	    return getHeaderFooter().isSetDifferentOddEven() ? getHeaderFooter().getDifferentOddEven() : false;
	}
	
	/**
	 * returns scaleWithDoc attribute
	 */
	public boolean getScaleWithDoc() {
	    return getHeaderFooter().isSetScaleWithDoc() ? getHeaderFooter().getScaleWithDoc() : false;
	}
	
	/**
	 * set alignWithMargins attribute
	 */
	public void setAlignWithMargins(boolean flag) {
	    getHeaderFooter().setAlignWithMargins(flag);
	}
	   
    /**
     * set differentFirst attribute
     */
	public void setDifferentFirst(boolean flag) {
        getHeaderFooter().setDifferentFirst(flag);
    }
    
    /**
     * set differentOddEven attribute
     */
	public void setDifferentOddEven(boolean flag) {
        getHeaderFooter().setDifferentOddEven(flag);
    }
    
    /**
     * set scaleWithDoc attribute
     */
	public void setScaleWithDoc(boolean flag) {
        getHeaderFooter().setScaleWithDoc(flag);
    }
    
    /**
     * remove alignWithMargins attribute
     */
	public void removeAlignWithMargins() {
        if (getHeaderFooter().isSetAlignWithMargins()) {
            getHeaderFooter().unsetAlignWithMargins();
        }
    }
       
    /**
     * remove differentFirst attribute
     */
	public void removeDifferentFirst() {
        if (getHeaderFooter().isSetDifferentFirst()) {
            getHeaderFooter().unsetDifferentFirst();
        }
    }
    
    /**
     * remove differentOddEven attribute
     */
	public void removeDifferentOddEven() {
        if (getHeaderFooter().isSetDifferentOddEven()) {
            getHeaderFooter().unsetDifferentOddEven();
        }
    }
    
    /**
     * remove scaleWithDoc attribute
     */
	public void removeScaleWithDoc() {
        if (getHeaderFooter().isSetScaleWithDoc()) {
            getHeaderFooter().unsetScaleWithDoc();
        }
    }
}
