
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


package org.apache.poi.hssf.view;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JApplet;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

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
    boolean isStandalone;
    String filename;

    /**
     * Get a parameter value
     */
    public String getParameter(String key, String def) {
        return isStandalone ? System.getProperty(key, def) :
                (getParameter(key) != null ? getParameter(key) : def);
    }

    /**
     * Construct the applet
     */
    public SViewer() {
    }

    /**
     * Initialize the applet
     */
    @Override
    public void init() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Component initialization
     */
    private void jbInit() throws Exception {
        boolean isurl = false;
        if (filename == null) {
            filename = getParameter("filename");
        }

        if (filename == null || filename.contains("://")) {
            isurl = true;
            if (filename == null) {
                filename = getParameter("url");
            }
        }

        final HSSFWorkbook wb;
        try (InputStream is = isurl ? getXLSFromURL(filename) : new FileInputStream(filename)) {
            wb = new HSSFWorkbook(is);
        }

        panel = new SViewerPanel(wb, false);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
    }

    /**
     * Start the applet
     */
    @Override
    public void start() {
    }

    /**
     * Stop the applet
     */
    @Override
    public void stop() {
    }

    /**
     * Destroy the applet
     */
    @Override
    public void destroy() {
    }

    /**
     * Get Applet information
     */
    @Override
    public String getAppletInfo() {
        return "Applet Information";
    }

    /**
     * Get parameter info
     */
    @Override
    public String[][] getParameterInfo() {
        return null;
    }

    /**
     * opens a url and returns an inputstream
     */
    private InputStream getXLSFromURL(String urlstring) throws MalformedURLException, IOException {
        URL url = new URL(urlstring);
        URLConnection uc = url.openConnection();
        String field = uc.getHeaderField(0);
        for (int i = 0; field != null; i++) {
            System.out.println(field);
            field = uc.getHeaderField(i);
        }
        return new BufferedInputStream(uc.getInputStream());
    }


    /**
     * Main method
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("A filename to view must be supplied as the first argument, but none was given");
        }

        SViewer applet = new SViewer();
        applet.isStandalone = true;
        applet.filename = args[0];
        Frame frame;
        frame = new Frame() {
            @Override
            protected void processWindowEvent(WindowEvent e) {
                super.processWindowEvent(e);
                if (e.getID() == WindowEvent.WINDOW_CLOSING) {
                    System.exit(0);
                }
            }

            @Override
            public synchronized void setTitle(String title) {
                super.setTitle(title);
                enableEvents(AWTEvent.WINDOW_EVENT_MASK);
            }
        };
        frame.setTitle("Applet Frame");
        frame.add(applet, BorderLayout.CENTER);
        applet.init();
        applet.start();
        frame.setSize(400, 320);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
        frame.setVisible(true);
    }
}
