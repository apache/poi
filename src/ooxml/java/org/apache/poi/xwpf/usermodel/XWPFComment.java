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
package org.apache.poi.xwpf.usermodel;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTComment;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;

/**
 * Sketch of XWPF comment class
 * 
* @author Yury Batrakov (batrakov at gmail.com)
 * 
 */
public class XWPFComment
{
    protected String id;
    protected String author;
    protected StringBuffer text;
    
    public XWPFComment(CTComment comment)
    {
        text = new StringBuffer();
        id = comment.getId().toString();
        author = comment.getAuthor();
        
        for(CTP ctp : comment.getPList())
        {
            XWPFParagraph p = new XWPFParagraph(ctp, null);
            text.append(p.getText());
        }
    }
    
    public String getId()
    {
        return id;
    }
    
    public String getAuthor()
    {
        return author;
    }
    
    public String getText()
    {
        return text.toString();
    }
}
