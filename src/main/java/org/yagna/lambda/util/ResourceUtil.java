package org.yagna.lambda.util;

import java.util.ResourceBundle;

/**
 * Created by asish on 8/25/18.
 */
public class ResourceUtil {
    private static ResourceUtil resourceUtil;

    private ResourceBundle resourceBundle;

    private ResourceUtil() {
        this.resourceBundle = ResourceBundle.getBundle("application");
    }

    public static ResourceUtil getInstance() {
        if (resourceUtil == null) {
            resourceUtil = new ResourceUtil();
        }

        return resourceUtil;
    }

    public String getRingClientId() {
        return this.resourceBundle.getString("ring.client.id");
    }

    public String getRingGrantType() {
        return this.resourceBundle.getString("ring.client.grant.type");
    }

    public String getRingClientScope() {
        return this.resourceBundle.getString("ring.client.scope");
    }

    public String getRingUrlOauth() {
        return this.resourceBundle.getString("ring.url.oauth");
    }

    public String getRingUrlExchange() {
        return this.resourceBundle.getString("ring.url.exchange");
    }

    public String getRingUrlConnection() {
        return this.resourceBundle.getString("ring.url.connection");
    }

    public String getRingUrlLocations() {
        return this.resourceBundle.getString("ring.url.locations");
    }

    public String getRingUrlWebSocket() {
        return this.resourceBundle.getString("ring.url.websocket");
    }

}
