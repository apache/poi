rolloverImagesOn=new Array();
rolloverImagesOff=new Array();

function rolloverOn(name) {
  if(document.images[name] && rolloverImagesOn[name]) document.images[name].src=rolloverImagesOn[name].src;
}

function rolloverOff(name) {
 if(document.images[name] && rolloverImagesOff[name]) document.images[name].src=rolloverImagesOff[name].src;
}


function rolloverLoad(name,on,off) {
  rolloverImagesOn[name]=new Image();
  rolloverImagesOn[name].src=mangle(on);
  rolloverImagesOff[name]=new Image();
  rolloverImagesOff[name].src=mangle(off);
}

function mangle(name) {

  name = name.replace(/:/g, "_");
  name = name.replace(/\?/g, "_");
  name = name.replace(/\"/g, "\'");

  return name;
}
