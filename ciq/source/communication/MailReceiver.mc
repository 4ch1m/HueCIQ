using Toybox.Application as App;
using Toybox.Communications as Comm;

class MailReceiver {
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
            App.getApp().setProperty("known_lights", latestKnownLights);
        }

        Comm.emptyMailbox();
    }
}