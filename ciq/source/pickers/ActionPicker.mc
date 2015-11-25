using Toybox.Graphics as Gfx;
using Toybox.WatchUi as Ui;

class ActionPicker extends Ui.Picker {
    function initialize() {
        var title = new Ui.Text({:text=>Rez.Strings.actionPickerTitle, :locX =>Ui.LAYOUT_HALIGN_CENTER, :locY=>Ui.LAYOUT_VALIGN_BOTTOM, :color=>Gfx.COLOR_WHITE});
        var factory = new WordPickerFactory([Rez.Strings.actionPickerSwitch, Rez.Strings.actionPickerBrightness, Rez.Strings.actionPickerColor], {:font=>Gfx.FONT_XTINY});

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
        if(values[0] == Rez.Strings.actionPickerSwitch) {
            Ui.pushView(new SwitchPicker(), new SwitchPickerDelegate(), Ui.SLIDE_IMMEDIATE);
        }
        else if(values[0] == Rez.Strings.actionPickerBrightness) {
            Ui.pushView(new BrightnessPicker(), new BrightnessPickerDelegate(), Ui.SLIDE_IMMEDIATE);
        }
        else if(values[0] == Rez.Strings.actionPickerColor) {
            Ui.pushView(new ColorPicker(), new ColorPickerDelegate(), Ui.SLIDE_IMMEDIATE);
        }
    }
}
