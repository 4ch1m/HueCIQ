using Toybox.Graphics as Gfx;
using Toybox.WatchUi as Ui;

class TargetPicker extends Ui.Picker {
    static const TARGETS = {"individual_light" => Stringz.wrap(Ui.loadResource(Rez.Strings.targetPickerLight)),
                            "light_group" => Stringz.wrap(Ui.loadResource(Rez.Strings.targetPickerGroup))};

    function initialize() {
        var title = new Ui.Text({:text=>Ui.loadResource(Rez.Strings.targetPickerTitle), :locX =>Ui.LAYOUT_HALIGN_CENTER, :locY=>Ui.LAYOUT_VALIGN_BOTTOM, :color=>Gfx.COLOR_WHITE});
        var factory = new WordPickerFactory([TARGETS["individual_light"], TARGETS["light_group"]], {:font=>Gfx.FONT_XTINY});

        Picker.initialize({:title=>title, :pattern=>[factory]});
    }

    function onUpdate(dc) {
        dc.setColor(Gfx.COLOR_BLACK, Gfx.COLOR_BLACK);
        dc.clear();

        Picker.onUpdate(dc);
    }
}

class TargetPickerDelegate extends Ui.PickerDelegate {
    function initialize() {
        PickerDelegate.initialize();
    }

    function onCancel() {
        Ui.popView(Ui.SLIDE_IMMEDIATE);
    }

    function onAccept(values) {
        var acceptedValue = values[0];

        if (acceptedValue == TargetPicker.TARGETS["individual_light"]) {
            Ui.pushView(new LightPicker(), new LightPickerDelegate(), Ui.SLIDE_IMMEDIATE);
        }
        else if (acceptedValue == TargetPicker.TARGETS["light_group"]) {
            Ui.pushView(new GroupPicker(), new GroupPickerDelegate(), Ui.SLIDE_IMMEDIATE);
        }
    }
}
