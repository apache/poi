/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ==================================================================== 
 */

package org.apache.poi.xslf.usermodel;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Merge multiple pptx presentations together
 */
public final class MergePresentations {

    public static void main(String[] args) throws Exception {
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            for (String arg : args) {
                try (FileInputStream is = new FileInputStream(arg);
                     XMLSlideShow src = new XMLSlideShow(is)) {
                    for (XSLFSlide srcSlide : src.getSlides()) {
                        ppt.createSlide().importContent(srcSlide);
                    }
                }
            }

            try (FileOutputStream out = new FileOutputStream("merged.pptx")) {
                ppt.write(out);
            }
        }
    }
}
