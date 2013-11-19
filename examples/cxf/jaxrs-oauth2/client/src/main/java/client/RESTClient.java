/**
 * Copyright (C) 2011 Talend Inc. - www.talend.com
 */
package client;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.client.ClientException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import oauth2.common.Calendar;
import oauth2.common.OAuthConstants;
import oauth2.common.ReservationConfirmation;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;
import org.apache.cxf.rs.security.oauth2.client.OAuthClientUtils;
import org.apache.cxf.rs.security.oauth2.common.ClientAccessToken;
import org.apache.cxf.rs.security.oauth2.common.OAuthAuthorizationData;
import org.apache.cxf.rs.security.oauth2.grants.owner.ResourceOwnerGrant;
import org.apache.cxf.rs.security.oauth2.provider.OAuthJSONProvider;
import org.apache.cxf.rs.security.oauth2.provider.OAuthServiceException;
import org.apache.cxf.rs.security.oauth2.utils.OAuthUtils;

/**
 * OAuth demo client
 */
public final class RESTClient {

    private static final String PORT_PROPERTY = "http.port";
    private static final int DEFAULT_PORT_VALUE = 8080;

    private static final String HTTP_PORT;
    static {
        Properties props = new Properties();
        try {
            props.load(RESTClient.class.getResourceAsStream("/client.properties"));
        } catch (Exception ex) {
            throw new RuntimeException("client.properties resource is not available");
        }
        HTTP_PORT = props.getProperty(PORT_PROPERTY);
    }

    int port;

    public RESTClient() {
        this(getPort());
    }

    public RESTClient(int port) {
        this.port = port;
    }

    public void registerClientApplication() throws Exception {
    	WebClient rs = WebClient.create("http://localhost:" + port + "/services/oauth/registerProvider");
    	WebClient.getConfig(rs).getHttpConduit().getClient().setReceiveTimeout(10000000L);
    	rs.form(new Form().set("appName", "Restaurant Reservations")
    			          .set("appURI", "http://localhost:" + port + "/services/reservations/reserve")
    			          .set("appRedirectURI", "http://localhost:" + port + "/services/reservations/reserve/complete")
    			          );
    }
    
    public void createUserAccount() throws Exception {
    	WebClient rs = WebClient.create("http://localhost:" + port + "/services/register/registerUser");
    	WebClient.getConfig(rs).getHttpConduit().getClient().setReceiveTimeout(10000000L);
    	rs.form(new Form().set("user", "barry@social.com").set("password", "1234"));
    	
    	//printUserCalendar();
    }
    
    private void printUserCalendar() {
    	WebClient client = createClient("http://localhost:" + port + "/services/social/accounts/calendar", 
    			"barry@social.com", "1234");
    	Calendar calendar = client.get(Calendar.class);
    	System.out.println(calendar.toString());
    }
    
    private void updateAndGetUserCalendar(int hour, String event) {
    	WebClient client = createClient("http://localhost:" + port + "/services/social/accounts/calendar", 
    			"barry@social.com", "1234");
    	Form form = new Form().set("hour", hour).set("event", event);
    	client.form(form);
    	printUserCalendar();
    }
    
    public void reserveTable() throws Exception {
    	WebClient rs = createClient("http://localhost:" + port + "/services/reservations/reserve/table", 
    			"barry@restaurant.com", "5678");
    	Response r = rs.form(new Form().set("name", "Barry")
    			                       .set("phone", "12345678")
    			                       .set("hour", "7"));
    	
    	int status = r.getStatus();
    	Object locationHeader = r.getMetadata().getFirst("Location");
    	if (status != 303 || locationHeader == null) {
    		System.out.println("OAuth flow is broken");
    	}
    	WebClient authorizeClient = createClient(locationHeader.toString(), 
    			"barry@social.com", "1234");
    	OAuthAuthorizationData data = authorizeClient.get(OAuthAuthorizationData.class);    	
    	Object authenticityCookie = authorizeClient.getResponse().getMetadata().getFirst("Set-Cookie");
    	    	
    	Form authorizationResult = getAuthorizationResult(data);   //user clicked on "allow" authorization
    	authorizeClient.reset();
    	authorizeClient.to(data.getReplyTo(), false);
    	if (authenticityCookie != null) {
    		authorizeClient.header("Cookie", (String)authenticityCookie);
    	}
    	Response r2 = authorizeClient.form(authorizationResult);
    	
    	int status2 = r2.getStatus();
    	Object locationHeader2 = r2.getMetadata().getFirst("Location");
    	if (status2 != 303 || locationHeader2 == null) {
    		System.out.println("OAuth flow is broken");
    	}
    	
    	WebClient finalClient = createClient(locationHeader2.toString(), 
    			"barry@restaurant.com", "5678");
    	finalClient.accept("application/xml");
    	ReservationConfirmation confirm = finalClient.get(ReservationConfirmation.class);
    	
    	if (confirm != null) {
    		updateAndGetUserCalendar(7, "Dinner at " + confirm.getAddress());
    	} else {
    		System.out.println("Reservation failed");
    	}
    }
    
    private WebClient createClient(String address, String userName, String password) {
    	JAXRSClientFactoryBean bean = new JAXRSClientFactoryBean();
    	bean.setAddress(address);
    	bean.setUsername(userName);
    	bean.setPassword(password);
    	
    	bean.getOutInterceptors().add(new LoggingOutInterceptor());
    	bean.getInInterceptors().add(new LoggingInInterceptor());
    	
    	return bean.createWebClient();
    }
    
    private Form getAuthorizationResult(OAuthAuthorizationData data) {
        Form form = new Form();
        form.set("client_id", data.getClientId());
        form.set("state", data.getState());
        form.set("scope", data.getProposedScope());
        form.set("redirect_uri", data.getRedirectUri());
        // TODO: get the user confirmation, using a popup window or a blocking cmd input
        form.set("oauthDecision", "allow");
        form.set("session_authenticity_token", data.getAuthenticityToken());
        return form;
    }
    
    public static void main(String[] args) throws Exception {

        RESTClient client = new RESTClient();

        //client.registerClientApplication();
        //client.createUserAccount();
        client.mytest();
        //registration steps are done now.
        //client.
        //client.reserveTable();
    }

    private void mytest() {
        WebClient rs = WebClient.create("http://localhost:" + port + "/services/oauth/token");
        MultivaluedHashMap multivaluedHashMap = new MultivaluedHashMap();
        //fake stuff but must have, not validating against client id.
        multivaluedHashMap.add(org.apache.cxf.rs.security.oauth2.utils.OAuthConstants.CLIENT_ID,"barry@social.com");

        multivaluedHashMap.add(org.apache.cxf.rs.security.oauth2.utils.OAuthConstants.RESOURCE_OWNER_NAME,"abc");
        multivaluedHashMap.add(org.apache.cxf.rs.security.oauth2.utils.OAuthConstants.RESOURCE_OWNER_PASSWORD,"password");
        multivaluedHashMap.add(org.apache.cxf.rs.security.oauth2.utils.OAuthConstants.GRANT_TYPE,org.apache.cxf.rs.security.oauth2.utils.OAuthConstants.RESOURCE_OWNER_GRANT);

        Response response = rs.form(multivaluedHashMap);
                Map<String, String> map = null;
        try {
            map = new OAuthJSONProvider().readJSONResponse((InputStream)response.getEntity());
        } catch (IOException ex) {
            throw new ClientException(ex);
        }
        ClientAccessToken token = null;
        if (200 == response.getStatus()) {
            token = OAuthClientUtils.fromMapToClientToken(map);

        }
        WebClient calendar =  WebClient.create("http://localhost:" + port + "/services/thirdPartyAccess/calendar");
        String authHeader = OAuthClientUtils.createAuthorizationHeader(token);
        calendar.replaceHeader("Authorization", authHeader);
        Calendar c = calendar.get(Calendar.class);
        System.out.println(c);
        /*Map extraParams = new HashMap();
        extraParams.put(org.apache.cxf.rs.security.oauth2.utils.OAuthConstants.GRANT_TYPE,org.apache.cxf.rs.security.oauth2.utils.OAuthConstants.RESOURCE_OWNER_GRANT);

        ClientAccessToken accessToken = OAuthClientUtils.getAccessToken(rs, new OAuthClientUtils.Consumer("public",null),
                new ResourceOwnerGrant("abc", "password"),
                extraParams,false);
        System.out.println(accessToken);
*/
    }

    private static int getPort() {
        try {
            return Integer.valueOf(HTTP_PORT);
        } catch (NumberFormatException ex) {
            // ignore
        }
        return DEFAULT_PORT_VALUE;
    }
}
