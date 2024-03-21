package services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioService {

    @Value("${twilio.sid}")
    private String twilioSID;
    @Value("${twilio.token}")
    private String twilioToken;
    @Value("${twilio.number}")
    private String twilioNumber;

    public void sendMessage(String to, String message){
        Twilio.init(twilioSID, twilioToken);
//        Message.creator(
//                new PhoneNumber(to),
//                new PhoneNumber(twilioNumber),
//                message
//        ).create();
    }
}
