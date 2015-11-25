using Toybox.Application as App;
using Toybox.Communications as Comm;
using Toybox.Graphics as Gfx;
//using Toybox.System as Sys;

const SWITCH_COMMAND_PREFIX = "switch_";
const SWITCH_COMMAND_ON = "on";
const SWITCH_COMMAND_OFF = "off";

const BRIGHTNESS_COMMAND_PREFIX = "brightness_";

const COLOR_COMMAND_PREFIX = "color_";
const COLOR_COMMAND_RED = "red";
const COLOR_COMMAND_BLUE = "blue";
const COLOR_COMMAND_GREEN = "green";
const COLOR_COMMAND_YELLOW = "yellow";
const COLOR_COMMAND_ORANGE = "orange";
const COLOR_COMMAND_PURPLE = "purple";

class Transmitter {
    static function switchOn() {
        Comm.transmit(SWITCH_COMMAND_PREFIX + SWITCH_COMMAND_ON, null, new TransmitListener());
    }

    static function switchOff() {
        Comm.transmit(SWITCH_COMMAND_PREFIX + SWITCH_COMMAND_OFF, null, new TransmitListener());
    }

    static function setBrightness(value) {
        Comm.transmit(BRIGHTNESS_COMMAND_PREFIX + value, null, new TransmitListener());
    }

    static function setColor(value) {
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

        Comm.transmit(COLOR_COMMAND_PREFIX + color, null, new TransmitListener());
    }
}

class TransmitListener extends Comm.ConnectionListener {
    function initialize() {
        ConnectionListener.initialize();
    }

    function onComplete() {
        //Sys.println("Transmit Complete");
    }

    function onError() {
        //Sys.println("Transmit Failed");
    }
}
