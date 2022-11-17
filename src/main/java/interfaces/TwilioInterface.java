package interfaces;

import application.App;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public abstract class TwilioInterface {

    public static void sendMessage(String to, String message){
        Twilio.init(App.config.getTwilioSID(), App.config.getTwilioToken());
        Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(App.config.getTwilioNumber()),
                message
        ).create();
    }
}
