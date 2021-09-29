package com.ec.quickbooks.model.controller;

import com.intuit.oauth2.client.DiscoveryAPIClient;
import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.config.Environment;
import com.intuit.oauth2.config.OAuth2Config;
import com.intuit.oauth2.config.Scope;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.data.DiscoveryAPIResponse;
import com.intuit.oauth2.exception.ConnectionException;
import com.intuit.oauth2.exception.InvalidRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;


@PropertySource(value = "classpath:/application.properties", ignoreResourceNotFound = true)
@Controller
public class HomeController {
    private static final Logger logger = Logger.getLogger(HomeController.class);


    @Autowired
    org.springframework.core.env.Environment env;

    @RequestMapping("/connectToQuickbooks")
    public View connectToQuickBooks() {

        //initialize the config (set client id, secret)
        OAuth2Config oauth2Config = new OAuth2Config.OAuth2ConfigBuilder(env.getProperty("OAuth2AppClientId"), env.getProperty("OAuth2AppClientSecret"))
                .callDiscoveryAPI(Environment.SANDBOX)
                .buildConfig();
        //generate csrf token
        String csrf = oauth2Config.generateCSRFToken();
        String redirectUri = env.getProperty("OAuth2AppRedirectUri");

        try {
            //prepare scopes
            List<Scope> scopes = new ArrayList<Scope>();
            scopes.add(Scope.Accounting);

            DiscoveryAPIResponse discoveryAPIResponse = new DiscoveryAPIClient().callDiscoveryAPI(Environment.SANDBOX);

            System.out.println("ENPOINTS DOSCPVERY: "+discoveryAPIResponse.getAuthorizationEndpoint());
            //prepare OAuth2Platform client
            OAuth2PlatformClient client  = new OAuth2PlatformClient(oauth2Config);

            //retrieve access token by calling the token endpoint
           // BearerTokenResponse bearerTokenResponse = client.retrieveBearerTokens(authCode, redirectUri);

            //prepare authorization url to intiate the oauth handshake
            return new RedirectView(oauth2Config.prepareUrl(scopes, redirectUri, csrf), true, true, false);
        } catch (InvalidRequestException | ConnectionException e) {
            logger.error("Exception calling connectToQuickbooks ", e);
            logger.error("intuit_tid: " + e.getIntuit_tid());
            logger.error("More info: " + e.getResponseContent());
        }
        return null;
    }
}
