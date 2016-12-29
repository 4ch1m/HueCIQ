using Toybox.Application as App;
using Toybox.Graphics as Gfx;
using Toybox.WatchUi as Ui;
using Toybox.Attention as Att;

class SwitchPicker extends Ui.Picker {
    static const SWITCHES = {"on" => Stringz.wrap(Ui.loadResource(Rez.Strings.switchPickerOn)),
                             "off" => Stringz.wrap(Ui.loadResource(Rez.Strings.switchPickerOff))};

    function initialize() {
        var title = new Ui.Text({:text=>Ui.loadResource(Rez.Strings.switchPickerTitle), :locX =>Ui.LAYOUT_HALIGN_CENTER, :locY=>Ui.LAYOUT_VALIGN_BOTTOM, :color=>Gfx.COLOR_WHITE});
        var factory = new WordPickerFactory([SWITCHES["on"], SWITCHES["off"]], {:font=>Gfx.FONT_MEDIUM});

        Picker.initialize({:title=>title, :pattern=>[factory]});
    }

    function onUpdate(dc) {
        dc.setColor(Gfx.COLOR_BLACK, Gfx.COLOR_BLACK);
        dc.clear();

        Picker.onUpdate(dc);
    }
}

class SwitchPickerDelegate extends Ui.PickerDelegate {
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

        var selectedId = App.getApp().getProperty("selected_id");

        if (values[0] == SwitchPicker.SWITCHES["on"]) {
            Transmitter.switchOn(selectedId);
        } else {
            Transmitter.switchOff(selectedId);
        }
    }
}
