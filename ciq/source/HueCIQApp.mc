using Toybox.Application as App;
using Toybox.System as Sys;
using Toybox.WatchUi as Ui;
using Toybox.Timer as Tmr;

const SPLASH_TIMER_VALUE = 2000;

class HueCIQApp extends App.AppBase {
    static const PROPERTY_KNOWN_LIGHTS = "known_lights";
    static const PROPERTY_SELECTED_LIGHT = "selected_light";

    function initialize() {
        AppBase.initialize();
    }

    function onStart() {
    }

    function onStop() {
    }

    function getInitialView() {
        return [ new HueCIQView(), new HueCIQDelegate() ];
    }
}

class HueCIQView extends Ui.View {
    var mailReceiver = null;
    var timer = null;

    function initialize() {
        View.initialize();
    }

    function onLayout(dc) {
        setLayout(Rez.Layouts.MainLayout(dc));
    }

    function onShow() {
        if (mailReceiver == null) {
            mailReceiver = new MailReceiver();
        }

        var author = findDrawableById("author");
        author.setText(Ui.loadResource(Rez.Strings.authorBy) + " " + Stringz.reverse(Ui.loadResource(Rez.Strings.Author)));
        author.setLocation(author.locX, Sys.getDeviceSettings().screenHeight - 55);

        var version = findDrawableById("version");
        version.setLocation(version.locX, Sys.getDeviceSettings().screenHeight - 30);

        timer = new Tmr.Timer();
        timer.start(method(:showLightPicker), HueCIQApp.SPLASH_TIMER_VALUE, false);
    }

    function onUpdate(dc) {
        View.onUpdate(dc);
    }

    function onHide() {
       timer.stop();
    }

    function showLightPicker() {
        Ui.pushView(new LightPicker(), new LightPickerDelegate(), Ui.SLIDE_IMMEDIATE);
    }
}

class HueCIQDelegate extends Ui.BehaviorDelegate {
    function initialize() {
        BehaviorDelegate.initialize();
    }

    function onSelect() {
        Ui.pushView(new LightPicker(), new LightPickerDelegate(), Ui.SLIDE_IMMEDIATE);
    }
}
