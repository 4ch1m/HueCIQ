using Toybox.Graphics as Gfx;
using Toybox.WatchUi as Ui;

class ActionPicker extends Ui.Picker {
    static const ACTIONS = {"switch" => Stringz.wrap(Ui.loadResource(Rez.Strings.actionPickerSwitch)),
                            "brightness" => Stringz.wrap(Ui.loadResource(Rez.Strings.actionPickerBrightness)),
                            "color" => Stringz.wrap(Ui.loadResource(Rez.Strings.actionPickerColor))};

    function initialize() {
        var title = new Ui.Text({:text=>Ui.loadResource(Rez.Strings.actionPickerTitle), :locX =>Ui.LAYOUT_HALIGN_CENTER, :locY=>Ui.LAYOUT_VALIGN_BOTTOM, :color=>Gfx.COLOR_WHITE});
        var factory = new WordPickerFactory([ACTIONS["switch"], ACTIONS["brightness"], ACTIONS["color"]], {:font=>Gfx.FONT_XTINY});

        Picker.initialize({:title=>title, :pattern=>[factory]});
    }

    function onUpdate(dc) {
        dc.setColor(Gfx.COLOR_BLACK, Gfx.COLOR_BLACK);
        dc.clear();

        Picker.onUpdate(dc);
    }
}

class ActionPickerDelegate extends Ui.PickerDelegate {
    function initialize() {
        PickerDelegate.initialize();
    }

    function onCancel() {
        Ui.popView(Ui.SLIDE_IMMEDIATE);
    }

    function onAccept(values) {
        var acceptedValue = values[0];

        if (acceptedValue == ActionPicker.ACTIONS["switch"]) {
            Ui.pushView(new SwitchPicker(), new SwitchPickerDelegate(), Ui.SLIDE_IMMEDIATE);
        }
        else if (acceptedValue == ActionPicker.ACTIONS["brightness"]) {
            Ui.pushView(new BrightnessPicker(), new BrightnessPickerDelegate(), Ui.SLIDE_IMMEDIATE);
        }
        else if (acceptedValue == ActionPicker.ACTIONS["color"]) {
            Ui.pushView(new ColorPicker(), new ColorPickerDelegate(), Ui.SLIDE_IMMEDIATE);
        }
    }
}
