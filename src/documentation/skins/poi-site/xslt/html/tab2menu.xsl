<?xml version="1.0"?>
<!--
   ====================================================================
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
   ====================================================================
-->
<!--
This stylesheet generates 'tabs' at the top left of the screen.
See the imported tab2menu.xsl for details.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:import href="../../../common/xslt/html/tab2menu.xsl"/>
  <xsl:param name="config-file" select="'../../../../skinconf.xml'"/>
  <xsl:variable name="config" select="document($config-file)/skinconfig"/>
  
  <xsl:param name="notoc"/>
  <xsl:param name="path"/>
  <!-- <xsl:include href="split.xsl"/> -->
  <xsl:include href="../../../common/xslt/html/dotdots.xsl"/>
  <xsl:include href="../../../common/xslt/html/pathutils.xsl"/>
  
  <!-- If true, a PDF link for this page will not be generated -->
  <xsl:variable name="disable-pdf-link" select="$config/disable-pdf-link"/>
  <!-- If true, a "print" link for this page will not be generated -->
  <xsl:variable name="disable-print-link" select="$config/disable-print-link"/>
  <!-- If true, an XML link for this page will not be generated -->
  <xsl:variable name="disable-xml-link" select="$config/disable-xml-link"/>  
  <!-- Get the section depth to use when generating the minitoc (default is 2) -->
  <xsl:variable name="config-max-depth" select="$config/toc/@level"/>
  <!-- Whether to obfuscate email links -->
  <xsl:variable name="obfuscate-mail-links" select="$config/obfuscate-mail-links"/>

  <!-- Path to site root, eg '../../' -->
  <xsl:variable name="root">
    <xsl:call-template name="dotdots">
      <xsl:with-param name="path" select="$path"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="filename-noext">
    <xsl:call-template name="filename-noext">
      <xsl:with-param name="path" select="$path"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:template name="pre-separator">
  </xsl:template>

  <xsl:template name="post-separator">

    <xsl:if test="not($config/disable-print-link) or $disable-print-link = 'false'">
    <xsl:text> | </xsl:text>
<script type="text/javascript" language="Javascript">
function printit() {  
if (window.print) {
    window.print() ;  
} else {
    var WebBrowser = '&lt;OBJECT ID="WebBrowser1" WIDTH="0" HEIGHT="0" CLASSID="CLSID:8856F961-340A-11D0-A96B-00C04FD705A2">&lt;/OBJECT>';
document.body.insertAdjacentHTML('beforeEnd', WebBrowser);
    WebBrowser1.ExecWB(6, 2);//Use a 1 vs. a 2 for a prompting dialog box    WebBrowser1.outerHTML = "";  
}
}
</script>

<script type="text/javascript" language="Javascript">
var NS = (navigator.appName == "Netscape");
var VERSION = parseInt(navigator.appVersion);
if (VERSION > 3) {
    document.write('  <a href="javascript:printit()" title="PRINT this page OUT">PRINT</a>');
}
</script>
   </xsl:if>

   <xsl:if test="not($config/disable-xml-link) or $disable-xml-link = 'false'"> 
    <xsl:text> | </xsl:text><a href="{$filename-noext}.xml" title="XML file of this page">XML</a>
   </xsl:if>


   <xsl:if test="not($config/disable-pdf-link) or $disable-pdf-link = 'false'"> 
    <xsl:text> | </xsl:text><a href="{$filename-noext}.pdf" title="PDF file of this page">PDF</a>
   </xsl:if>
  </xsl:template>

  <xsl:template name="separator">
    <xsl:text> | </xsl:text>
  </xsl:template>

  <xsl:template name="selected" mode="print">
    <span class="selectedTab">
      <xsl:call-template name="base-selected"/>
    </span>
  </xsl:template>

  <xsl:template name="not-selected" mode="print">
    <span class="unselectedTab">
    <!-- Called from 'not-selected' -->
     <a>
      <xsl:attribute name="href">
        <xsl:call-template name="calculate-tab-href">
          <xsl:with-param name="tab" select="."/>
          <xsl:with-param name="path" select="$path"/>
        </xsl:call-template>
      </xsl:attribute>
      <xsl:value-of select="@label"/>
     </a>
    </span>
  </xsl:template>
</xsl:stylesheet>
