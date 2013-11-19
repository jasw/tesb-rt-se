/**
 * Copyright (C) 2011 Talend Inc. - www.talend.com
 */
package oauth2.thirdparty;

import java.net.URI;

import oauth2.common.OAuthConstants;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.rs.security.oauth2.client.OAuthClientUtils;
import org.apache.cxf.rs.security.oauth2.client.OAuthClientUtils.Consumer;
import org.apache.cxf.rs.security.oauth2.common.ClientAccessToken;
import org.apache.cxf.rs.security.oauth2.grants.code.AuthorizationCodeGrant;
import org.apache.cxf.rs.security.oauth2.provider.OAuthServiceException;

import javax.ws.rs.core.UriBuilder;

public class OAuthClientManager {

	private static final String DEFAULT_CLIENT_ID = "123456789";
	private static final String DEFAULT_CLIENT_SECRET = "987654321";
	
	private WebClient accessTokenService;
    private String authorizationServiceURI;
    private Consumer consumer = new Consumer(DEFAULT_CLIENT_ID, DEFAULT_CLIENT_SECRET);
    
	public OAuthClientManager() {
		
	}
	
	public URI getAuthorizationServiceURI(ReservationRequest request,
			                              URI redirectUri,
			                              String reservationRequestKey) {
		String scope = OAuthConstants.UPDATE_CALENDAR_SCOPE + request.getHour();
        String clientId = consumer.getKey();
        String state = reservationRequestKey;
        UriBuilder ub = UriBuilder.fromUri(authorizationServiceURI);
        if (clientId != null) {
            ub.queryParam(org.apache.cxf.rs.security.oauth2.utils.OAuthConstants.CLIENT_ID, clientId);
        }
        if (scope != null) {
            ub.queryParam(org.apache.cxf.rs.security.oauth2.utils.OAuthConstants.SCOPE, scope);
        }
        ub.queryParam(org.apache.cxf.rs.security.oauth2.utils.OAuthConstants.RESPONSE_TYPE, org.apache.cxf.rs.security.oauth2.utils.OAuthConstants.TOKEN_RESPONSE_TYPE);

        if (redirectUri != null) {
            ub.queryParam(org.apache.cxf.rs.security.oauth2.utils.OAuthConstants.REDIRECT_URI, redirectUri);
        }
        if (state != null) {
            ub.queryParam(org.apache.cxf.rs.security.oauth2.utils.OAuthConstants.STATE, state);
        }

	   return ub.build();
	}
	public ClientAccessToken getAccessToken(AuthorizationCodeGrant codeGrant) {
	    try {
	        return OAuthClientUtils.getAccessToken(accessTokenService, consumer, codeGrant);
	    } catch (OAuthServiceException ex) {
	        return null;
	    }
	}
	
	public String createAuthorizationHeader(ClientAccessToken token) {
		return OAuthClientUtils.createAuthorizationHeader(consumer, token);
	}
	
	public void setAccessTokenService(WebClient ats) {
		// The timeout is set to simplify debugging on the server side;
		// otherwise the connection may time-out too early
		WebClient.getConfig(ats).getHttpConduit().getClient().setReceiveTimeout(1000000);
		this.accessTokenService = ats;
	}
    
    public void setAuthorizationURI(String uri) {
		this.authorizationServiceURI = uri;
	}
	
}
