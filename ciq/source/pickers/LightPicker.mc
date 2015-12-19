using Toybox.Application as App;
using Toybox.Graphics as Gfx;
using Toybox.WatchUi as Ui;

const NEW_LINE = "\n";

class LightPicker extends Ui.Picker {
    const LIGHT_ITEM_SEPARATOR = "|";
    const LIGHT_ID_SEPARATOR = ";";

    function initialize() {
        var knownLights = App.getApp().getProperty(HueCIQApp.PROPERTY_KNOWN_LIGHTS);
        var allLightsItemArray = [Ui.loadResource(Rez.Strings.lightPickerAll)];

        var pickerItems;

        if (knownLights == null || "" == knownLights) {
            pickerItems = allLightsItemArray;
        } else {
            var knownLightItems = Stringz.split(knownLights, LIGHT_ITEM_SEPARATOR);
            var knownLightIdAndName;

            var formattedLightItemsArray = new [knownLightItems.size()];

            for(var i = 0; i < knownLightItems.size(); i++) {
                knownLightIdAndName = Stringz.split(knownLightItems[i], LIGHT_ID_SEPARATOR);
                formattedLightItemsArray[i] = "#" + knownLightIdAndName[0] + NEW_LINE + knownLightIdAndName[1];
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
            var splitted = Stringz.split(values[0], NEW_LINE);
            selectedLight = splitted[0].substring(1, splitted[0].length());
        }

        App.getApp().setProperty(HueCIQApp.PROPERTY_SELECTED_LIGHT, selectedLight);

        Ui.pushView(new ActionPicker(), new ActionPickerDelegate(), Ui.SLIDE_IMMEDIATE);
    }
}
