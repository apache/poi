<?xml version="1.0"?>

<html xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xsl:version="1.0">
  <head>
    <meta content="text/html; charset=ISO-8859-1"/>
    <title><xsl:value-of select="/site/body/title"/></title>
<style type="text/css">
<![CDATA[ body { background-color: white; font-size: normal; color: black ; }
 a { color: #525d76; }
 a.black { color: #000000;} 
 table {border-width: 0; width: 100%}
 table.centered {text-align: center}
 table.title {text-align: center; width: 80%} 
 img{border-width: 0;} 
 span.s1 {font-family: Helvetica, Arial, sans-serif; font-weight: bold; color: #000000; }
 span.s1_white { font-family: Helvetica, Arial, sans-serif; font-weight: bold; color: #ffffff; } 
 span.title {font-family: Helvetica, Arial, sans-serif; font-weight: bold; color: #000000; }
 span.c1 {color: #000000; font-family: Helvetica, Arial, sans-serif}
 tr.left {text-align: left}
 hr { width: 100%; size: 2} ]]>
</style>    
  </head>
  <body>
  
    <!-- header --> 
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tr>
<td align="left" valign="top"><a href="http://jakarta.apache.org/index.html"><img src="images/jakarta-logo.gif" border="0" vspace="0" hspace="0"/></a></td><td bgcolor="#ffffff" align="left" valign="top" width="100%"><img src="images/header.gif" align="right" border="0" vspace="0" hspace="0"/></td>
</tr>
<tr>
<td bgcolor="#525d76" colspan="2"><span class="c1"><a href="http://www.apache.org/" class="black">www.apache.org &gt;</a><a href="http://jakarta.apache.org/" class="black">jakarta.apache.org &gt;</a><a class="black" href="http://jakarta.apache.org/poi/">jakarta.apache.org/poi</a></span></td>
</tr>
<tr>
<td height="8"></td>
</tr>
</table>
    
    <!-- main --> 
    <table width="100%" cellspacing="0" cellpadding="0" border="0">
      <tr>
        <!-- left menu --> 
        <td width="1%"><br/></td>
        <td width="14%" valign="top" nowrap="1">
          <xsl:copy-of select="/site/menu/node()|@*"/>
        </td>
        <td width="1%"><br/></td>  
            
        <!-- contents --> 
        <td width="*" valign="top" align="left">
          <xsl:copy-of select="/site/body/node()|@*"/>
        </td>
      </tr>
    </table>
    <br/>
    
    <!-- footer --> 
   <table cellpadding="0" cellspacing="0" border="0" width="100%">
      <tbody>
        <tr>
          <td>
            <hr size="1" noshade=""/>
          </td>
        </tr>
        <tr>
          <td align="center">
            <i>Copyright &#x00A9; 2002 Apache Software Foundation</i>
          </td>
        </tr>
        <tr>
          <td width="100%" align="right">
			<br/>
          </td>
        </tr>        
        <tr>
          <td width="100%" align="right">
            <a href="http://krysalis.org/"><img src="images/krysalis-compatible.jpg" alt="Krysalis Logo"/></a> 
            <a href="http://xml.apache.org/cocoon/"><img src="images/built-with-cocoon.gif" alt="Cocoon Logo"/></a> 
          </td>
        </tr>
      </tbody>
    </table>
    
  </body>
</html>

