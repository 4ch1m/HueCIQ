using Toybox.Application as App;
using Toybox.Graphics as Gfx;
using Toybox.WatchUi as Ui;
using Toybox.Attention as Att;

class SwitchPicker extends Ui.Picker {
    function initialize() {
        var title = new Ui.Text({:text=>Rez.Strings.switchPickerTitle, :locX =>Ui.LAYOUT_HALIGN_CENTER, :locY=>Ui.LAYOUT_VALIGN_BOTTOM, :color=>Gfx.COLOR_WHITE});
        var factory = new WordPickerFactory([Rez.Strings.switchPickerOn, Rez.Strings.switchPickerOff], {:font=>Gfx.FONT_MEDIUM});

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
        Att.playTone(Att.TONE_KEY);

        var selectedLightId = App.getApp().getProperty(HueCIQApp.PROPERTY_SELECTED_LIGHT);

        if(values[0] == Rez.Strings.switchPickerOn) {
            Transmitter.switchOn(selectedLightId);
        }
        else if(values[0] == Rez.Strings.switchPickerOff) {
            Transmitter.switchOff(selectedLightId);
        }
    }
}
