package org.apache.poi.hslf.model;

import org.apache.poi.ddf.*;
import org.apache.poi.hslf.usermodel.PictureData;
import org.apache.poi.hslf.usermodel.SlideShow;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;


/**
 * Represents a picture in a PowerPoint document.
 * <p>
 * The information about an image in PowerPoint document is stored in
 * two places:
 *  <li> EscherBSE container in the Document keeps information about image
 *    type, image index to refer by slides etc.
 *  <li> "Pictures" OLE stream holds the actual data of the image.
 * </p>
 * <p>
 *  Data in the "Pictures" OLE stream is organized as follows:<br>
 *  For each image there is an entry: 25 byte header + image data.
 *  Image data is the exact content of the JPEG file, i.e. PowerPoint
 *  puts the whole jpeg file there without any modifications.<br>
 *   Header format:
 *    <li> 2 byte: image type. For JPEGs it is 0x46A0, for PNG it is 0x6E00.
 *    <li> 2 byte: unknown.
 *    <li> 4 byte : image size + 17. Looks like shift from the end of
 *          header but why to add it to the image  size?
 *    <li> next 16 bytes. Unique identifier of this image which is used by
 *          EscherBSE record.
 *  </p>
 *
 * @author Yegor Kozlov
 */
public class Picture extends SimpleShape {

    /**
    *  Windows Metafile
    *  ( NOT YET SUPPORTED )
    */
    public static final int WMF = 3;

    /**
    * Macintosh PICT
     *  ( NOT YET SUPPORTED )
    */
    public static final int PICT = 4;

    /**
    *  JPEG
    */
    public static final int JPEG = 5;

    /**
    *  PNG
    */
    public static final int PNG = 6;

    /**
    * Windows DIB (BMP)
    */
    public static final int DIB = 7;

    /**
     * Create a new <code>Picture</code>
     *
    * @param idx the index of the picture
     */
    public Picture(int idx){
        super(null, null);
        _escherContainer = createSpContainer(idx);
    }

    /**
      * Create a <code>Picture</code> object
      *
      * @param escherRecord the <code>EscherSpContainer</code> record which holds information about
      *        this picture in the <code>Slide</code>
      * @param parent the parent shape of this picture
      */
     protected Picture(EscherContainerRecord escherRecord, Shape parent){
        super(escherRecord, parent);
    }

    /**
     * Returns index associated with this picture.
     * Index starts with 1 and points to a EscherBSE record which
     * holds information about this picture.
     *
     * @return the index to this picture (1 based).
     */
    public int getPictureIndex(){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        EscherSimpleProperty prop = (EscherSimpleProperty)getEscherProperty(opt, EscherProperties.BLIP__BLIPTODISPLAY + 0x4000);
        return prop.getPropertyValue();
    }

    /**
     * Create a new Picture and populate the inital structure of the <code>EscherSp</code> record which holds information about this picture.

     * @param idx the index of the picture which referes to <code>EscherBSE</code> container.
     * @return the create Picture object
     */
    protected EscherContainerRecord createSpContainer(int idx) {
        EscherContainerRecord spContainer = super.createSpContainer(false);
        spContainer.setOptions((short)15);

        EscherSpRecord spRecord = spContainer.getChildById(EscherSpRecord.RECORD_ID);
        spRecord.setOptions((short)((ShapeTypes.PictureFrame << 4) | 0x2));

        //set default properties for a picture
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(spContainer, EscherOptRecord.RECORD_ID);
        setEscherProperty(opt, EscherProperties.PROTECTION__LOCKAGAINSTGROUPING, 8388736);

        //another weird feature of powerpoint: for picture id we must add 0x4000.
        setEscherProperty(opt, (short)(EscherProperties.BLIP__BLIPTODISPLAY + 0x4000), idx);

        return spContainer;
    }

    /**
     * Set default size of the picture
     *
     * @param ppt presentation which holds the picture
     */
    public void setDefaultSize(SlideShow ppt) throws IOException {
        int idx = getPictureIndex();

        PictureData pict = ppt.getPictures()[idx-1];
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(pict.getData()));

        setAnchor(new java.awt.Rectangle(0, 0, img.getWidth()*6, img.getHeight()*6));
    }
}
