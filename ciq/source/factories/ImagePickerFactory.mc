using Toybox.Graphics as Gfx;
using Toybox.WatchUi as Ui;

class ImagePickerFactory extends Ui.PickerFactory {
    var mImages;

    function initialize(images) {
        PickerFactory.initialize();

        mImages = images;
    }

    function getIndex(value) {
        for(var i = 0; i < mImages.size(); ++i) {
            if(mImages[i].equals(value)) {
                return i;
            }
        }

        return 0;
    }

    function getSize() {
        return mImages.size();
    }

    function getValue(index) {
        return mImages[index];
    }

    function getDrawable(index, selected) {
        return new Ui.Bitmap({:rezId=>mImages[index], :locX => Ui.LAYOUT_HALIGN_CENTER, :locY => Ui.LAYOUT_VALIGN_CENTER});
    }
}
