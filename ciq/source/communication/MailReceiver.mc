using Toybox.Application as App;
using Toybox.Communications as Comm;

class MailReceiver extends App.AppBase {
    function initialize() {
        Comm.setMailboxListener( method(:onMail) );
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