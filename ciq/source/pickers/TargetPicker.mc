using Toybox.Graphics as Gfx;
using Toybox.WatchUi as Ui;

class TargetPicker extends Ui.Picker {
    function initialize() {
        var title = new Ui.Text({:text=>Ui.loadResource(Rez.Strings.targetPickerTitle), :locX =>Ui.LAYOUT_HALIGN_CENTER, :locY=>Ui.LAYOUT_VALIGN_BOTTOM, :color=>Gfx.COLOR_WHITE});
        var factory = new ImagePickerFactory([Rez.Drawables.singleBulb, Rez.Drawables.bulbGroup]);

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

        if (acceptedValue == Rez.Drawables.singleBulb) {
            Ui.pushView(new LightPicker(), new LightPickerDelegate(), Ui.SLIDE_IMMEDIATE);
        }
        else if (acceptedValue == Rez.Drawables.bulbGroup) {
            Ui.pushView(new GroupPicker(), new GroupPickerDelegate(), Ui.SLIDE_IMMEDIATE);
        }
    }
}
