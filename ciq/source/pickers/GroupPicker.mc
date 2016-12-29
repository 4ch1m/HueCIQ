using Toybox.Application as App;
using Toybox.Graphics as Gfx;
using Toybox.WatchUi as Ui;

class GroupPicker extends Ui.Picker {
    function initialize() {
        var knownGroups = App.getApp().getProperty("known_groups");
        var allGroupsItemArray = [Ui.loadResource(Rez.Strings.groupPickerAll)];

        var pickerItems;

        if (knownGroups == null || "" == knownGroups || knownGroups.find(Constantz.ID_NAME_SEPARATOR) == null) {
            pickerItems = allGroupsItemArray;
        } else {
            var knownGroupItems = Stringz.split(knownGroups, Constantz.ITEM_SEPARATOR);
            var knownGroupIdAndName;

            var formattedGroupItemsArray = new [knownGroupItems.size()];

            for (var i=0; i < knownGroupItems.size(); i++) {
                knownGroupIdAndName = Stringz.split(knownGroupItems[i], Constantz.ID_NAME_SEPARATOR);
                formattedGroupItemsArray[i] = "#" + knownGroupIdAndName[0] + Constantz.NEW_LINE + Stringz.wrap(knownGroupIdAndName[1]);
            }

            pickerItems = Arrayz.join(allGroupsItemArray, formattedGroupItemsArray);
        }

        var title = new Ui.Text({:text=>Ui.loadResource(Rez.Strings.groupPickerTitle), :locX =>Ui.LAYOUT_HALIGN_CENTER, :locY=>Ui.LAYOUT_VALIGN_BOTTOM, :color=>Gfx.COLOR_WHITE});
        var factory = new WordPickerFactory(pickerItems, {:font=>Gfx.FONT_XTINY});

        Picker.initialize({:title=>title, :pattern=>[factory]});
    }

    function onUpdate(dc) {
        dc.setColor(Gfx.COLOR_BLACK, Gfx.COLOR_BLACK);
        dc.clear();

        Picker.onUpdate(dc);
    }
}

class GroupPickerDelegate extends Ui.PickerDelegate {
    function initialize() {
        PickerDelegate.initialize();
    }

    function onCancel() {
        Ui.popView(Ui.SLIDE_IMMEDIATE);
    }

    function onAccept(values) {
        var selectedGroup = null;

        if (values[0].equals(Ui.loadResource(Rez.Strings.groupPickerAll))) {
            selectedGroup = 0;
        } else {
            var splitted = Stringz.split(values[0], Constantz.NEW_LINE);
            selectedGroup = splitted[0].substring(1, splitted[0].length());
        }

        App.getApp().setProperty("selected_id", Constantz.GROUP_ID_PREFIX + selectedGroup);

        Ui.pushView(new ActionPicker(), new ActionPickerDelegate(), Ui.SLIDE_IMMEDIATE);
    }
}
