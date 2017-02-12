using Toybox.Application as App;
using Toybox.System as Sys;

class Helperz {
    static function playTone() {
        var playTones = App.getApp().getProperty("play_tones");
        var tonesOn = Sys.getDeviceSettings().tonesOn;

        return playTones && tonesOn;
    }
}
