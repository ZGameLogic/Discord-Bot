package interfaces.atlassian.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Server {

    private String baseUrl;
    private String personalAccessToken;

}
