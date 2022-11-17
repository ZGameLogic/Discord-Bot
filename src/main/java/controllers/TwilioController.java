package controllers;

import com.twilio.converter.Converter;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.Text;
import com.twilio.twiml.voice.Sms;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class TwilioController {

    @PostMapping(value = "/sms")
    private void receiveMessage(@RequestBody String body) throws URISyntaxException {
        List<NameValuePair> params = URLEncodedUtils.parse(new URI("?" + body), Charset.forName("UTF-8"));
        Map<String, String> mapped = new HashMap<>();
        for (NameValuePair param : params) {
            mapped.put(param.getName(), param.getValue());
        }
        log.info("Message from: " + mapped.get("From"));
        log.info("Body: " + mapped.get("Body"));
    }
}
