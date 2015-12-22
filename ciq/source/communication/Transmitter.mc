using Toybox.Application as App;
using Toybox.Communications as Comm;
using Toybox.Graphics as Gfx;
using Toybox.Attention as Att;

class Transmitter {
    static const SWITCH_COMMAND_PREFIX = "switch_";
    static const SWITCH_COMMAND_ON = "on";
    static const SWITCH_COMMAND_OFF = "off";

    static const BRIGHTNESS_COMMAND_PREFIX = "brightness_";

    static const COLOR_COMMAND_PREFIX = "color_";
    static const COLOR_COMMAND_RED = "red";
    static const COLOR_COMMAND_BLUE = "blue";
    static const COLOR_COMMAND_GREEN = "green";
    static const COLOR_COMMAND_YELLOW = "yellow";
    static const COLOR_COMMAND_ORANGE = "orange";
    static const COLOR_COMMAND_PURPLE = "purple";

    static const COMMAND_LIGHT_ID_SEPARATOR = "-";

    static function switchOn(lightId) {
        Comm.transmit(SWITCH_COMMAND_PREFIX + SWITCH_COMMAND_ON + COMMAND_LIGHT_ID_SEPARATOR + lightId, null, new TransmitListener());
    }

    static function switchOff(lightId) {
        Comm.transmit(SWITCH_COMMAND_PREFIX + SWITCH_COMMAND_OFF + COMMAND_LIGHT_ID_SEPARATOR + lightId, null, new TransmitListener());
    }

    static function setBrightness(lightId, value) {
        Comm.transmit(BRIGHTNESS_COMMAND_PREFIX + value + COMMAND_LIGHT_ID_SEPARATOR + lightId, null, new TransmitListener());
    }

    static function setColor(lightId, value) {
        var color = "";

        if (value == Gfx.COLOR_GREEN) {
            color = COLOR_COMMAND_GREEN;
        }
        else if (value == Gfx.COLOR_BLUE) {
            color = COLOR_COMMAND_BLUE;
        }
        else if (value == Gfx.COLOR_ORANGE) {
            color = COLOR_COMMAND_ORANGE;
        }
        else if (value == Gfx.COLOR_YELLOW) {
            color = COLOR_COMMAND_YELLOW;
        }
        else if (value == Gfx.COLOR_PURPLE) {
            color = COLOR_COMMAND_PURPLE;
        }
        else {
            color = COLOR_COMMAND_RED;
        }

        Comm.transmit(COLOR_COMMAND_PREFIX + color + COMMAND_LIGHT_ID_SEPARATOR + lightId, null, new TransmitListener());
    }
}

class TransmitListener extends Comm.ConnectionListener {
    function initialize() {
        ConnectionListener.initialize();
    }

    function onComplete() {
        Att.playTone(Att.TONE_START);
    }

    function onError() {
        Att.playTone(Att.TONE_ALERT_LO);
    }
}
