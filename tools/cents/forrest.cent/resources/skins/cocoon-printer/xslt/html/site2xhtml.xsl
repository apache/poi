<?xml version="1.0"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

	<xsl:param name="header"/>

<xsl:template match="/">
<html>
      <head>
        <title><xsl:value-of select="/site/document/title"/></title>
      </head>

      <body text="#000000" link="#039acc" vlink="#0086b2" alink="#cc0000"
            topmargin="4" leftmargin="4" marginwidth="4" marginheight="4"
            bgcolor="#ffffff">
        <!-- THE TOP BAR (HEADER) -->
        <table width="100%" cellspacing="0" cellpadding="0" border="0">
          <tr>
            <td width="135" height="60" rowspan="3" valign="top" align="left">
              <img width="135" height="60" src="images/logo.gif" hspace="0" vspace="0" border="0"/>
            </td>
            <td width="100%" height="5" valign="top" align="left" colspan="2" background="images/line.gif">
              <img width="1" height="5" src="images/line.gif" hspace="0" vspace="0" border="0" align="left"/>
            </td>
            <td width="29" height="60"  rowspan="3" valign="top" align="left">
              <img width="29" height="60" src="images/right.gif" hspace="0" vspace="0" border="0"/>
            </td>
          </tr>
          <tr>
            <!-- using svg 
            <td width="100%" height="35" valign="top" align="right" colspan="2" bgcolor="#0086b2">
              <img src="{$header}?label={/site/document/title}" hspace="0" vspace="0" border="0" alt="{/site/document/title}" align="right"/>           
            </td>
            -->
            <td width="100%" height="35" valign="top" align="right" colspan="2" bgcolor="#0086b2">
              <p align="right" style="color:white; font-family:arial; font-size:30px; font-style:italic">
                <xsl:value-of select="/site/document/title"/>
              </p>
            </td>
          </tr>
          <tr>
            <td width="100%" height="20" valign="top" align="left" bgcolor="#0086b2" background="images/bottom.gif">
              <img width="3" height="20" src="images/bottom.gif" hspace="0" vspace="0" border="0" align="left"/>
            </td>
            <td align="right" bgcolor="#0086b2" height="20" valign="top" width="288" background="images/bottom.gif">
              <table border="0" cellpadding="0" cellspacing="0" width="288">
                <tr>
                  <td width="96" height="20" valign="top" align="left">
                    <a href="http://xml.apache.org/" target="new">
                      <img alt="http://xml.apache.org/" width="96" height="20" src="images/button-xml-lo.gif"
                           name="xml" hspace="0" vspace="0" border="0"/>
                    </a>
                  </td>
                  <td width="96" height="20" valign="top" align="left">
                    <a href="http://www.apache.org/" target="new">
                      <img alt="http://www.apache.org/" width="96" height="20" src="images/button-asf-lo.gif"
                           name="asf" hspace="0" vspace="0" border="0"/>
                    </a>
                  </td>
                  <td width="96" height="20" valign="top" align="left">
                    <a href="http://www.w3.org/" target="new">
                      <img alt="http://www.w3.org/" width="96" height="20" src="images/button-w3c-lo.gif"
                           name="w3c" hspace="0" vspace="0" border="0"/>
                    </a>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>

<table border="0" cellpadding="0" cellspacing="0" width="100%">
	<tr width="100%">
		<td width="120" valign="top"> 
			<table border="0" cellpadding="0" cellspacing="0" width="120">
			      <tr>
					<td align="left" valign="top">
						<img border="0" height="14" hspace="0" src="images/join.gif" vspace="0" width="120"/>
						<br/>
					</td>
				</tr>
				<xsl:copy-of select="/site/menu/node()|@*"/>
<!--
				<tr>
					<td valign="top" align="left">
						<img border="0" height="14" hspace="0" src="images/close.gif" vspace="0" width="120"/>
						<br/>
					</td>
				</tr>
-->
			</table>
		</td>
		<td>
			<table border="0" cellpadding="0" cellspacing="0">
				<tr><td width="100%" height="10"/></tr>
				<tr><td><xsl:copy-of select="/site/document/body/node()|@*"/></td></tr>
			</table>
		</td>
	</tr>
 </table>
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr>
    <td bgcolor="#0086b2">
      <img height="1" src="images/dot.gif" width="1"/>
    </td>
  </tr>
  <tr>
    <td align="center">
      <font color="#0086b2" face="arial,helvetica,sanserif" size="-1">
        <i>Copyright &#169; @year@ The Apache Software Foundation. All Rights Reserved.</i>
      </font>
    </td>
  </tr>
</table>
</body>


</html>
</xsl:template>
</xsl:stylesheet>
