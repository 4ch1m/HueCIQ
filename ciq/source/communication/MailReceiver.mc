using Toybox.Application as App;
using Toybox.Communications as Comm;

class MailReceiver extends App.AppBase {
    function initialize() {
        Comm.setMailboxListener( method(:onMail) );

        // ------------------------
        // for testing purposes ...
        // ------------------------
        //var testLights = "4;Hue iris 1";
        //var testLights = "5;Hue bloom 2|4;Hue iris 1|1;Hue go 1|3;Hue bloom 1|6;Hue color lamp 1|2;Hue lightstrip 1";
        //App.getApp().setProperty(HueCIQApp.PROPERTY_KNOWN_LIGHTS, testLights);
    }

    function onMail(mailIter)
    {
        var latestKnownLights = null;

        var mail = mailIter.next();

        while( mail != null )
        {
            latestKnownLights = mail;
            mail = mailIter.next();
        }

        if (latestKnownLights != null) {
            App.getApp().setProperty(HueCIQApp.PROPERTY_KNOWN_LIGHTS, latestKnownLights);
        }

        Comm.emptyMailbox();
    }
}