import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.apis.MicrosoftAzureActiveDirectoryApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.logging.log4j.core.util.JsonUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class MSALTest {

    private static final String NETWORK_NAME = "Microsoft Azure Active Directory";
    private static final String PROTECTED_RESOURCE_URL = "https://graph.microsoft.com/v1.0/me/events";

    private MSALTest() {

    }

    public static void main(String... args) throws IOException, InterruptedException, ExecutionException {
        // Replace these with your client id and secret
        final String clientId = "b1c8c70e-daf3-4bc9-ae1e-50b0f348dd58";
        MicrosoftAzureActiveDirectoryApi20 api = MicrosoftAzureActiveDirectoryApi20.instance();
        final OAuth20Service service = new ServiceBuilder(clientId)
                .scope("openid")
                .scope("Calendars.ReadWrite")
//                .apiSecret(clientSecret)
                .callback("https://login.microsoftonline.com/common/oauth2/nativeclient")
                .build(api);
        final Scanner in = new Scanner(System.in, "UTF-8");

        System.out.println("=== " + NETWORK_NAME + "'s OAuth Workflow ===");
        System.out.println();

        // Obtain the Authorization URL
        System.out.println("Fetching the Authorization URL...");
        final String authorizationUrl = service.getAuthorizationUrl();
        System.out.println("Got the Authorization URL!");
        System.out.println("Now go and authorize ScribeJava here:");
        System.out.println(authorizationUrl);
        System.out.println("And paste the authorization code here");
        System.out.print(">>");
        final String code = in.nextLine();
        System.out.println();

        // Trade the Request Token and Verfier for the Access Token
        System.out.println("Trading the Request Token for an Access Token...");
        final OAuth2AccessToken accessToken = service.getAccessToken(code);
        System.out.println("Got the Access Token!");
        System.out.println("(The raw response looks like this: " + accessToken.getRawResponse() + "')");
        System.out.println();

        // Now let's go and ask for a protected resource!
        System.out.println("Now we're going to access a protected resource...");
        final OAuthRequest request = new OAuthRequest(Verb.POST, PROTECTED_RESOURCE_URL);
        String load = "{" +
                "  \"Subject\": \"Discuss the Calendar REST API\"," +
                "  \"Body\": {" +
                "    \"ContentType\": \"HTML\"," +
                "    \"Content\": \"I think it will meet our requirements!hahahah2\"" +
                "  }," +
                "  \"Start\": {" +
                "      \"DateTime\": \"2018-10-05T18:00:00\"," +
                "      \"TimeZone\": \"Pacific Standard Time\"" +
                "  }," +
                "  \"End\": {" +
                "      \"DateTime\": \"2018-10-05T19:00:00\"," +
                "      \"TimeZone\": \"Pacific Standard Time\"" +
                "  }"+
                "}";

        request.addHeader("Content-Type","application/json;charset=UTF-8");
        request.setPayload(load);
        service.signRequest(accessToken, request);
        final Response response = service.execute(request);
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response.getCode());
        System.out.println(response.getBody());

        System.out.println();
        System.out.println("Thats it man! Go and build something awesome with ScribeJava! :)");
    }
}