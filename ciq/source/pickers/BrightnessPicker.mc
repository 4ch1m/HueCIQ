using Toybox.Graphics as Gfx;
using Toybox.WatchUi as Ui;

const MIN_BRIGHTNESS_VALUE = 10;
const MAX_BRIGHTNESS_VALUE = 100;
const STEP_BRIGHTNESS_VALUE = 10;

class BrightnessPicker extends Ui.Picker {
    function initialize() {
        var brightnessSteps = new [ MAX_BRIGHTNESS_VALUE / STEP_BRIGHTNESS_VALUE ];
        var brightnessStep = MIN_BRIGHTNESS_VALUE;
        for (var i=0; i < (MAX_BRIGHTNESS_VALUE / STEP_BRIGHTNESS_VALUE); i+=1) {
            brightnessSteps[i] = brightnessStep.toString();
            brightnessStep += STEP_BRIGHTNESS_VALUE;
        }
        var title = new Ui.Text({:text=>Rez.Strings.brightnessPickerTitle, :locX =>Ui.LAYOUT_HALIGN_CENTER, :locY=>Ui.LAYOUT_VALIGN_BOTTOM, :color=>Gfx.COLOR_WHITE});
        var factory = new WordPickerFactory(brightnessSteps, {:font=>Gfx.FONT_MEDIUM});

        Picker.initialize({:title=>title, :pattern=>[factory]});
    }

    function onUpdate(dc) {
        dc.setColor(Gfx.COLOR_BLACK, Gfx.COLOR_BLACK);
        dc.clear();

        Picker.onUpdate(dc);
    }
}

class BrightnessPickerDelegate extends Ui.PickerDelegate {
    function initialize() {
        PickerDelegate.initialize();
    }

    function onCancel() {
        Ui.popView(Ui.SLIDE_IMMEDIATE);
    }

    function onAccept(values) {
        Transmitter.setBrightness(values[0]);
    }
}
