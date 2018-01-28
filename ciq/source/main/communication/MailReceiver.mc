using Toybox.Application as App;
using Toybox.Communications as Comm;

class MailReceiver {
    function initialize() {
        Comm.setMailboxListener( method(:onMail) );

        // ------------------------
        // for testing purposes ...
        // ------------------------
        //var testLightsAndGroups = "";
        //var testLightsAndGroups = "4;Hue iris 1";
        //var testLightsAndGroups = "5;Hue bloom 2|4;Hue iris 1|1;Hue go 1|3;Hue bloom 1|6;Hue color lamp 1|2;Hue lightstrip 1";
        //var testLightsAndGroups = "5;Hue bloom 2|4;Hue iris 1|1;Hue go 1|3;Hue bloom 1|6;Hue color lamp 1|2;Hue lightstrip 1§1;Living Room|2;Office|3;Bedroom";
        //storeMailContent(testLightsAndGroups);
    }

    function onMail(mailIter) {
        try {
            var latestMail = null;
            var mail = mailIter.next();

            while (mail != null) {
                latestMail = mail;
                mail = mailIter.next();
            }

            if (latestMail != null) {
                storeMailContent(latestMail);
            }
        } catch( ex ) {
            // do nothing ...
        } finally {
            Comm.emptyMailbox();
        }
    }

    function storeMailContent(mail) {
        var lightsAndGroups = Stringz.split(mail, Constantz.LIGHT_GROUP_SEPARATOR);

        App.getApp().setProperty("known_lights", lightsAndGroups[0]);

        if (lightsAndGroups.size() > 1) {
            App.getApp().setProperty("known_groups", lightsAndGroups[1]);
        } else {
            App.getApp().setProperty("known_groups", "");
        }
    }
}
