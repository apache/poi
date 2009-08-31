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
package org.apache.poi.xwpf.extractor;

import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.model.XWPFCommentsDecorator;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.model.XWPFHyperlinkDecorator;
import org.apache.poi.xwpf.model.XWPFParagraphDecorator;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;

/**
 * Helper class to extract text from an OOXML Word file
 */
public class XWPFWordExtractor extends POIXMLTextExtractor {
	private XWPFDocument document;
	private boolean fetchHyperlinks = false;
	
	public XWPFWordExtractor(OPCPackage container) throws XmlException, OpenXML4JException, IOException {
		this(new XWPFDocument(container));
	}
	public XWPFWordExtractor(XWPFDocument document) {
		super(document);
		this.document = document;
	}

	/**
	 * Should we also fetch the hyperlinks, when fetching 
	 *  the text content? Default is to only output the
	 *  hyperlink label, and not the contents
	 */
	public void setFetchHyperlinks(boolean fetch) {
		fetchHyperlinks = fetch;
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length < 1) {
			System.err.println("Use:");
			System.err.println("  HXFWordExtractor <filename.docx>");
			System.exit(1);
		}
		POIXMLTextExtractor extractor = 
			new XWPFWordExtractor(POIXMLDocument.openPackage(
					args[0]
			));
		System.out.println(extractor.getText());
	}
	
	public String getText() {
		StringBuffer text = new StringBuffer();
		XWPFHeaderFooterPolicy hfPolicy = document.getHeaderFooterPolicy();

		// Start out with all headers
                extractHeaders(text, hfPolicy);
		
		// First up, all our paragraph based text
		Iterator<XWPFParagraph> i = document.getParagraphsIterator();
		while(i.hasNext()) {
                        XWPFParagraph paragraph = i.next();


                        try {
                                CTSectPr ctSectPr = null;
                                if (paragraph.getCTP().getPPr()!=null) {
                                        ctSectPr = paragraph.getCTP().getPPr().getSectPr();
                                }

                                XWPFHeaderFooterPolicy headerFooterPolicy = null;

                                if (ctSectPr!=null) {
                                        headerFooterPolicy = new XWPFHeaderFooterPolicy(document, ctSectPr);

                                        extractHeaders(text, headerFooterPolicy);
                                }

                                XWPFParagraphDecorator decorator = new XWPFCommentsDecorator(
                                                new XWPFHyperlinkDecorator(paragraph, null, fetchHyperlinks));
                                text.append(decorator.getText()).append('\n');

                                if (ctSectPr!=null) {
                                        extractFooters(text, headerFooterPolicy);
                                }
                        } catch (IOException e) {
                                throw new POIXMLException(e);
                        } catch (XmlException e) {
                                throw new POIXMLException(e);
                        }
                }

		// Then our table based text
		Iterator<XWPFTable> j = document.getTablesIterator();
		while(j.hasNext()) {
                        text.append(j.next().getText()).append('\n');
		}
		
		// Finish up with all the footers
                extractFooters(text, hfPolicy);
		
		return text.toString();
	}

        private void extractFooters(StringBuffer text, XWPFHeaderFooterPolicy hfPolicy) {
                if(hfPolicy.getFirstPageFooter() != null) {
                        text.append( hfPolicy.getFirstPageFooter().getText() );
                }
                if(hfPolicy.getEvenPageFooter() != null) {
                        text.append( hfPolicy.getEvenPageFooter().getText() );
                }
                if(hfPolicy.getDefaultFooter() != null) {
                        text.append( hfPolicy.getDefaultFooter().getText() );
                }
        }

        private void extractHeaders(StringBuffer text, XWPFHeaderFooterPolicy hfPolicy) {
                if(hfPolicy.getFirstPageHeader() != null) {
                        text.append( hfPolicy.getFirstPageHeader().getText() );
                }
                if(hfPolicy.getEvenPageHeader() != null) {
                        text.append( hfPolicy.getEvenPageHeader().getText() );
                }
                if(hfPolicy.getDefaultHeader() != null) {
                        text.append( hfPolicy.getDefaultHeader().getText() );
                }
        }
}
