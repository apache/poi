<?xml version="1.0"?>

<html xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xsl:version="1.0">
  <head><title><xsl:value-of select="/site/body/title"/></title></head>
  <body text="#000000" link="#525D76" vlink="#023264" alink="#023264"
        topmargin="4" leftmargin="4" marginwidth="4" marginheight="4"
	bgcolor="#ffffff">
    <table width="100%" cellspacing="0" cellpadding="0" border="0">
      <tr>
        <td valign="top" align="left">
          <a href="http://jakarta.apache.org/index.html">
            <img hspace="0" vspace="0" border="0" src="images/jakarta-logo.gif"/>
          </a>
        </td>
        <td width="100%" valign="top" align="left" bgcolor="#ffffff">
          <img hspace="0"
               vspace="0"
               border="0"
               align="right"
               src="images/header.gif"/>
        </td>
      </tr>
      <tr>
        <td width="100%" height="2" colspan="2"><hr noshade="" size="1"/></td>
      </tr>
    </table>
 
    <table width="100%" cellspacing="0" cellpadding="0" border="0">
      <tr>
        <td width="1%" valign="top"/>
        <td width="14%" valign="top" nowrap="1">
          <br/>
          <font face="arial,helvetica,sanserif">
            <br/>
            <xsl:copy-of select="/site/menu/node()|@*"/>
            <br/>
          </font>
        </td>
        <td width="*" valign="top" align="left">
          <xsl:copy-of select="/site/body/node()|@*"/>
        </td>
      </tr>
    </table>
    <br/>
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr><td><hr noshade="" size="1"/></td></tr>
      <tr>
        <td align="center">
          <font face="arial,helvetica,sanserif" size="-1" color="#525D76">
            <i>
              Copyright &#169;2002 Apache Software Foundation
            </i>
          </font>
        </td>
        <td align="right" width="5%">
          <img hspace="0"
               vspace="0"
               border="0"
               align="right"
               src="images/cocoon2-small.jpg"/>
        </td>        
      </tr>
    </table>
  </body>
</html>

