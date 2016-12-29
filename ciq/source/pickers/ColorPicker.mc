using Toybox.Application as App;
using Toybox.Graphics as Gfx;
using Toybox.WatchUi as Ui;
using Toybox.Attention as Att;

class ColorPicker extends Ui.Picker {
    var factory;
    var title;

    function initialize() {
        title = new Ui.Text({:text=>Ui.loadResource(Rez.Strings.colorPickerTitle), :locX=>Ui.LAYOUT_HALIGN_CENTER, :locY=>Ui.LAYOUT_VALIGN_BOTTOM, :color=>Gfx.COLOR_WHITE});
        factory = new ColorPickerFactory([Gfx.COLOR_RED, Gfx.COLOR_GREEN, Gfx.COLOR_BLUE, Gfx.COLOR_ORANGE, Gfx.COLOR_YELLOW, Gfx.COLOR_PURPLE]);

        var nextArrow = new Ui.Bitmap({:rezId=>Rez.Drawables.nextArrow});
        var previousArrow = new Ui.Bitmap({:rezId=>Rez.Drawables.previousArrow});
        var brush = new Ui.Bitmap({:rezId=>Rez.Drawables.brush});

        Picker.initialize({:title=>title, :pattern=>[factory], :defaults=>null, :nextArrow=>nextArrow, :previousArrow=>previousArrow, :confirm=>brush});
    }

    function onUpdate(dc) {
        dc.setColor(Gfx.COLOR_BLACK, Gfx.COLOR_BLACK);
        dc.clear();
        Picker.onUpdate(dc);
    }
}

class ColorPickerDelegate extends Ui.PickerDelegate {
    function initialize() {
        PickerDelegate.initialize();
    }

    function onCancel() {
        Ui.popView(Ui.SLIDE_IMMEDIATE);
    }

    function onAccept(values) {
        if (Helperz.playTone()) {
            Att.playTone(Att.TONE_KEY);
        }

        Transmitter.setColor(App.getApp().getProperty("selected_id"), values[0]);
    }
}
