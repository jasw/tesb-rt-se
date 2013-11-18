package oauth2.poc;

import org.apache.cxf.rs.security.oauth2.common.OAuthAuthorizationData;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created by jasonwang on 17/11/13.
 */
@Path("/login")
public class LoginPageService {

    @GET
    @Path("get")
    public LoginPageResponse  getPage(LoginPageRequest loginPageRequest){

        LoginPageResponse response = new LoginPageResponse();
        response.setClientID(loginPageRequest.getClientID());
        response.setRedirectURL(loginPageRequest.getRedirectURL());
        return response;
    }

}
