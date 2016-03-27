using Toybox.Application as App;
using Toybox.Communications as Comm;

class MailReceiver {
    function initialize() {
        Comm.setMailboxListener( method(:onMail) );
    }

    function onMail(mailIter) {
        try {
            var latestKnownLights = null;

            var mail = mailIter.next();

            while (mail != null) {
                latestKnownLights = mail;
                mail = mailIter.next();
            }

            if (latestKnownLights != null) {
                App.getApp().setProperty("known_lights", latestKnownLights);
            }
        }
        catch( ex ) {
            // do nothing ...
        }
        finally {
            Comm.emptyMailbox();
        }
    }
}