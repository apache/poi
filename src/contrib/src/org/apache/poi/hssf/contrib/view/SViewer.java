
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */


package org.apache.poi.hssf.contrib.view;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.applet.*;
import java.io.*;
import javax.swing.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFCell;

/**
 * Sheet Viewer - Views XLS files via HSSF.  Can be used as an applet with
 * filename="" or as a applications (pass the filename as the first parameter).
 * Or you can pass it a URL in a "url" parameter when run as an applet or just
 * that first parameter must start with http:// and it will guess its a url. I
 * only tested it as an applet though, so it probably won't work...you fix it.
 *
 * @author Andrew C. Oliver
 * @author Jason Height
 */
public class SViewer extends JApplet {
  private SViewerPanel panel;
  boolean isStandalone = false;
  String filename = null;

  /**Get a parameter value*/
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
      (getParameter(key) != null ? getParameter(key) : def);
  }

  /**Construct the applet*/
  public SViewer() {
  }

  /**Initialize the applet*/
  public void init() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**Component initialization*/
  private void jbInit() throws Exception {
    InputStream i = null;
    boolean isurl = false;
    if (filename == null) filename = getParameter("filename");

    if (filename == null || filename.substring(0,7).equals("http://")) {
      isurl = true;
      if (filename == null) filename = getParameter("url");
      i = getXLSFromURL(filename);
    }

    HSSFWorkbook wb = null;
    if (isurl) {
      wb = constructWorkbook(i);
    } else {
      wb = constructWorkbook(filename);
    }
    panel = new SViewerPanel(wb, false);
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(panel, BorderLayout.CENTER);
  }

  private HSSFWorkbook constructWorkbook(String filename) throws FileNotFoundException, IOException {
    HSSFWorkbook wb = null;
      FileInputStream in = new FileInputStream(filename);
      wb = new HSSFWorkbook(in);
      in.close();
    return wb;
  }

  private HSSFWorkbook constructWorkbook(InputStream in) throws IOException {
    HSSFWorkbook wb = null;

      wb = new HSSFWorkbook(in);
      in.close();
    return wb;
  }

  /**Start the applet*/
  public void start() {
  }
  /**Stop the applet*/
  public void stop() {
  }
  /**Destroy the applet*/
  public void destroy() {
  }
  /**Get Applet information*/
  public String getAppletInfo() {
    return "Applet Information";
  }
  /**Get parameter info*/
  public String[][] getParameterInfo() {
    return null;
  }

  /**
   * opens a url and returns an inputstream
   *
   */
  private InputStream getXLSFromURL(String urlstring) throws MalformedURLException, IOException {
    URL url = new URL(urlstring);
    URLConnection uc = url.openConnection();
    String field = uc.getHeaderField(0);
    for (int i=0;field != null; i++) {
      System.out.println(field);
      field = uc.getHeaderField(i);
  }
    BufferedInputStream is = new BufferedInputStream(uc.getInputStream());
    return is;
  }


  /**Main method*/
  public static void main(String[] args) {
    SViewer applet = new SViewer();
    applet.isStandalone = true;
    applet.filename = args[0];
    Frame frame;
    frame = new Frame() {
      protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
          System.exit(0);
        }
      }
      public synchronized void setTitle(String title) {
        super.setTitle(title);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      }
    };
    frame.setTitle("Applet Frame");
    frame.add(applet, BorderLayout.CENTER);
    applet.init();
    applet.start();
    frame.setSize(400,320);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
    frame.setVisible(true);
  }
}
