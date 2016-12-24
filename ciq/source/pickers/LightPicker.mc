using Toybox.Application as App;
using Toybox.Graphics as Gfx;
using Toybox.WatchUi as Ui;

class LightPicker extends Ui.Picker {
    function initialize() {
        var knownLights = App.getApp().getProperty("known_lights");

        var pickerItems;

        if (knownLights == null || "" == knownLights || knownLights.find(Constantz.ID_NAME_SEPARATOR) == null) {
            pickerItems = [Ui.loadResource(Rez.Strings.lightPickerNA)];
        } else {
            var knownLightItems = Stringz.split(knownLights, Constantz.ITEM_SEPARATOR);
            var knownLightIdAndName;

            var formattedLightItemsArray = new [knownLightItems.size()];

            for(var i = 0; i < knownLightItems.size(); i++) {
                knownLightIdAndName = Stringz.split(knownLightItems[i], Constantz.ID_NAME_SEPARATOR);
                formattedLightItemsArray[i] = "#" + knownLightIdAndName[0] + "\n" + knownLightIdAndName[1];
            }

            pickerItems = formattedLightItemsArray;
        }

        var title = new Ui.Text({:text=>Rez.Strings.lightPickerTitle, :locX =>Ui.LAYOUT_HALIGN_CENTER, :locY=>Ui.LAYOUT_VALIGN_BOTTOM, :color=>Gfx.COLOR_WHITE});
        var factory = new WordPickerFactory(pickerItems, {:font=>Gfx.FONT_XTINY});

        Picker.initialize({:title=>title, :pattern=>[factory]});
    }

    function onUpdate(dc) {
        dc.setColor(Gfx.COLOR_BLACK, Gfx.COLOR_BLACK);
        dc.clear();

        Picker.onUpdate(dc);
    }
}

class LightPickerDelegate extends Ui.PickerDelegate {
    function initialize() {
        PickerDelegate.initialize();
    }

    function onCancel() {
        Ui.popView(Ui.SLIDE_IMMEDIATE);
    }

    function onAccept(values) {
        if(!values[0].equals(Ui.loadResource(Rez.Strings.lightPickerNA))) {
            var splitted = Stringz.split(values[0], "\n");
            var selectedLight = splitted[0].substring(1, splitted[0].length());

            App.getApp().setProperty("selected_id", selectedLight);

            Ui.pushView(new ActionPicker(), new ActionPickerDelegate(), Ui.SLIDE_IMMEDIATE);
        }
    }
}
