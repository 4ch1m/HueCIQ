using Toybox.Application as App;
using Toybox.System as Sys;
using Toybox.WatchUi as Ui;
using Toybox.Timer as Tmr;

class HueCIQApp extends App.AppBase {
    function initialize() {
        AppBase.initialize();
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
        author.setText(Ui.loadResource(Rez.Strings.authorBy) + Constantz.BLANK + Stringz.reverse(Ui.loadResource(Rez.Strings.Author)));
        author.setLocation(author.locX, Sys.getDeviceSettings().screenHeight - 55);

        var version = findDrawableById("version");
        version.setLocation(version.locX, Sys.getDeviceSettings().screenHeight - 30);

        timer = new Tmr.Timer();
        timer.start(method(:showTargetPicker), 2000, false);
    }

    function onUpdate(dc) {
        View.onUpdate(dc);
    }

    function onHide() {
       timer.stop();
    }

    function showTargetPicker() {
        Ui.pushView(new TargetPicker(), new TargetPickerDelegate(), Ui.SLIDE_IMMEDIATE);
    }
}

class HueCIQDelegate extends Ui.BehaviorDelegate {
    function initialize() {
        BehaviorDelegate.initialize();
    }

    function onSelect() {
        Ui.pushView(new TargetPicker(), new TargetPickerDelegate(), Ui.SLIDE_IMMEDIATE);
    }
}
