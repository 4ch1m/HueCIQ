using Toybox.Application as App;
using Toybox.WatchUi as Ui;
using Toybox.Timer as Tmr;

const TIMER_VALUE = 2000;

class HueCIQApp extends App.AppBase {
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
    function initialize() {
        View.initialize();
    }

    function onLayout(dc) {
        setLayout(Rez.Layouts.MainLayout(dc));
    }

    function onShow() {
        var author = findDrawableById("author");
        author.setText(Ui.loadResource(Rez.Strings.authorBy) + " " + Stringz.reverse(Ui.loadResource(Rez.Strings.Author)));

        var timer = new Tmr.Timer();
        timer.start(method(:showActionPicker), TIMER_VALUE, false);
    }

    function onUpdate(dc) {
        View.onUpdate(dc);
    }

    function onHide() {
    }

    function showActionPicker() {
        Ui.pushView(new ActionPicker(), new ActionPickerDelegate(), Ui.SLIDE_IMMEDIATE);
    }
}

class HueCIQDelegate extends Ui.BehaviorDelegate {
    function initialize() {
        BehaviorDelegate.initialize();
    }

    function onSelect() {
        Ui.pushView(new ActionPicker(), new ActionPickerDelegate(), Ui.SLIDE_IMMEDIATE);
    }
}
