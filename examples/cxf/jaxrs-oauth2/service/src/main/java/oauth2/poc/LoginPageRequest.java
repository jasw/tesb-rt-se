package oauth2.poc;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by jasonwang on 17/11/13.
 */
@XmlRootElement

public class LoginPageRequest {

    private String redirectURL;
    private String scope;
    private String state;
    private String clientID;
    private String uuid;

    public LoginPageRequest() {
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
