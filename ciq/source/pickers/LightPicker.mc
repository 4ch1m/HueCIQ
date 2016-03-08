using Toybox.Application as App;
using Toybox.Graphics as Gfx;
using Toybox.WatchUi as Ui;

class LightPicker extends Ui.Picker {
    static const LIGHT_ITEM_SEPARATOR = "|";
    static const LIGHT_ID_SEPARATOR = ";";

    function initialize() {
        var knownLights = App.getApp().getProperty("known_lights");
        var allLightsItemArray = [Ui.loadResource(Rez.Strings.lightPickerAll)];

        var pickerItems;

        if (knownLights == null || "" == knownLights || knownLights.find(LIGHT_ID_SEPARATOR) == null) {
            pickerItems = allLightsItemArray;
        } else {
            var knownLightItems = Stringz.split(knownLights, LIGHT_ITEM_SEPARATOR);
            var knownLightIdAndName;

            var formattedLightItemsArray = new [knownLightItems.size()];

            for(var i = 0; i < knownLightItems.size(); i++) {
                knownLightIdAndName = Stringz.split(knownLightItems[i], LIGHT_ID_SEPARATOR);
                formattedLightItemsArray[i] = "#" + knownLightIdAndName[0] + "\n" + knownLightIdAndName[1];
            }

            pickerItems = Arrayz.join(allLightsItemArray, formattedLightItemsArray);
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
        var selectedLight = null;

        if(values[0].equals(Ui.loadResource(Rez.Strings.lightPickerAll))) {
            selectedLight = 0;
        } else {
            var splitted = Stringz.split(values[0], "\n");
            selectedLight = splitted[0].substring(1, splitted[0].length());
        }

        App.getApp().setProperty("selected_light", selectedLight);

        Ui.pushView(new ActionPicker(), new ActionPickerDelegate(), Ui.SLIDE_IMMEDIATE);
    }
}
