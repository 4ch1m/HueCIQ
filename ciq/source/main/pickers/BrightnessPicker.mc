using Toybox.Application as App;
using Toybox.Graphics as Gfx;
using Toybox.WatchUi as Ui;
using Toybox.Attention as Att;

class BrightnessPicker extends Ui.Picker {
    static const MIN_BRIGHTNESS_VALUE = 10;
    static const MAX_BRIGHTNESS_VALUE = 100;
    static const STEP_BRIGHTNESS_VALUE = 10;

    function initialize() {
        var brightnessSteps = new [ MAX_BRIGHTNESS_VALUE / STEP_BRIGHTNESS_VALUE ];
        var brightnessStep = MIN_BRIGHTNESS_VALUE;

        for (var i=0; i < (MAX_BRIGHTNESS_VALUE / STEP_BRIGHTNESS_VALUE); i+=1) {
            brightnessSteps[i] = brightnessStep.toString() + Constantz.PERCENT;
            brightnessStep += STEP_BRIGHTNESS_VALUE;
        }

        var title = new Ui.Text({:text=>Ui.loadResource(Rez.Strings.brightnessPickerTitle), :locX =>Ui.LAYOUT_HALIGN_CENTER, :locY=>Ui.LAYOUT_VALIGN_BOTTOM, :color=>Gfx.COLOR_WHITE});
        var factory = new WordPickerFactory(brightnessSteps, {:font=>Gfx.FONT_SMALL});

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
        var acceptedValue = values[0];

        if (Helperz.playTone()) {
            Att.playTone(Att.TONE_KEY);
        }

        Transmitter.setBrightness(App.getApp().getProperty("selected_id"), acceptedValue.substring(0, acceptedValue.length() - 1));
    }
}
