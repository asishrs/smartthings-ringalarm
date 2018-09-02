package org.yagna.lambda;

import com.amazonaws.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.java_websocket.WebSocketImpl;
import org.json.JSONArray;
import org.json.JSONObject;
import org.yagna.lambda.constants.RingConstants;
import org.yagna.lambda.model.RingInput;
import org.yagna.lambda.model.SocketServer;
import org.yagna.lambda.util.HttpUtil;
import org.yagna.lambda.util.ResourceUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by asish on 8/25/18.
 */
public class RingUtil {

    private static RingUtil ringUtil;

    private String deviceInfoData = "42[\"message\",{\"msg\":\"DeviceInfoDocGetList\",\"seq\":1}]";

    private String ringWSSData = "42[\n" +
            "    \"message\",\n" +
            "    {\n" +
            "        \"msg\": \"DeviceInfoSet\",\n" +
            "        \"datatype\": \"DeviceInfoSetType\",\n" +
            "        \"body\": [\n" +
            "            {\n" +
            "                \"zid\": \"%s\",\n" +
            "                \"command\": {\n" +
            "                    \"v1\": [\n" +
            "                        {\n" +
            "                            \"commandType\": \"security-panel.switch-mode\",\n" +
            "                            \"data\": {\n" +
            "                                \"mode\": \"%s\"\n" +
            "                            }\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            }\n" +
            "        ],\n" +
            "        \"seq\": 2\n" +
            "    }\n" +
            "]";

    private RingInput ringInput;

    private RingUtil(RingInput ringInput) {
        this.ringInput = ringInput;
    }

    public static RingUtil instance(RingInput ringInput) {
        if (ringUtil == null) {
            ringUtil = new RingUtil(ringInput);
        }

        return ringUtil;
    }

    private String prepareAuthenticationInput() {
        return new JSONObject()
                .put(RingConstants.CLIENT_ID, ResourceUtil.getInstance().getRingClientId())
                .put(RingConstants.GRANT_TYPE, ResourceUtil.getInstance().getRingGrantType())
                .put(RingConstants.PASSWORD, this.ringInput.getPassword())
                .put(RingConstants.SCOPE, ResourceUtil.getInstance().getRingClientScope())
                .put(RingConstants.USERNAME, this.ringInput.getUser())
                .toString();
    }

    private String prepareOauthInput(String accessToken) {
        return new JSONObject()
                .put(RingConstants.ACCESS_TOKEN, accessToken)
                .toString();
    }

    private String getAuthenticationToken() throws IOException {
        String response = HttpUtil.getInstance().postJsonData(ResourceUtil.getInstance().getRingUrlOauth(),
                prepareAuthenticationInput());
        JSONObject auth = new JSONObject(response);
        return auth.getString(RingConstants.ACCESS_TOKEN);
    }

    private String getOauthToken(String input) throws IOException {
        String response = HttpUtil.getInstance().postJsonData(ResourceUtil.getInstance().getRingUrlExchange(), input);
        JSONObject auth = new JSONObject(response);
        return auth.getString(RingConstants.ACCESS_TOKEN);
    }

//    private String getLocationId(String authToken) throws IOException {
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Authorization", "Bearer " + authToken);
//        String response = HttpUtil.getInstance().get(ResourceUtil.getInstance().getRingUrlLocations(), headers);
//        JSONObject loc = new JSONObject(response);
//
//        JSONArray jsonArray = loc.getJSONArray("user_locations");
//        if(jsonArray == null && jsonArray.length() == 0) {
//            throw new IOException("Unable to find location");
//        }
//        return jsonArray.getJSONObject(0).getString("location_id");
//    }

    private SocketServer getWebSockerServer(String token, String locationId) throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(RingConstants.CONTENT_TYPE, RingConstants.APPLICATION_X_WWW_FORM_URLENCODE);
        headers.put(RingConstants.AUTHORIZATION, RingConstants.BEARER + " " + token);

        String response = HttpUtil.getInstance().postRawData(ResourceUtil.getInstance().getRingUrlConnection(),
                "accountId=" + locationId, headers);
        JSONObject con = new JSONObject(response);

        return new SocketServer(con.getString(RingConstants.SERVER), con.getString(RingConstants.AUTH_CODE));
    }

    private String prepareModeData(String mode) {
        return String.format(ringWSSData, this.ringInput.getZid(), mode);
    }

    public String disArmMode() {
        return callWebSocket(prepareModeData(RingConstants.NONE), 1000);
    }

    public String setHomeMode() {
        return callWebSocket(prepareModeData(RingConstants.SOME), 1000);
    }

    public String setAway() {
        return callWebSocket(prepareModeData(RingConstants.ALL), 1000);
    }

    public String getStatus() {
        return callWebSocketForStatus(3000);
    }

    public String getZid() {
        return callWebSocketForDeviceZid(3000);
    }

    private String callWebSocket(String data, int delayInMillis) {
        LinkedList<String> response = new LinkedList<>();
        return runWebSocketRequest(data, delayInMillis, response);
    }

    private String runWebSocketRequest(String data, int delayInMillis, LinkedList<String> response) {
        String socResponse = RingConstants.ERROR;
        String accessToken = null;
        try {
            accessToken = getAuthenticationToken();
        } catch (IOException e) {
            e.printStackTrace();
            return socResponse;
        }

        String authToken = null;
        try {
            authToken = getOauthToken(prepareOauthInput(accessToken));
        } catch (IOException e) {
            e.printStackTrace();
            return socResponse;
        }

//        String locationId = null;
//        try{
//            locationId = getLocationId(authToken);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return socResponse;
//        }

        SocketServer socketServer = null;
        try {
            socketServer = getWebSockerServer(authToken, this.ringInput.getLocationId());
        } catch (IOException e) {
            e.printStackTrace();
            return socResponse;
        }

        WebSocketImpl.DEBUG = true;
        WebSocketClient c = null;
        try {
            c = new WebSocketClient(new URI(MessageFormat.format(ResourceUtil.getInstance().getRingUrlWebSocket(),
                    socketServer.getServer(), socketServer.getAuthCode())), response);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return socResponse;
        }

        try {
            c.connectBlocking();
            c.send(data);
            Thread.sleep(delayInMillis);
            c.closeBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return socResponse;
        }
        return RingConstants.SUCCESS;
    }

    private String callWebSocketForStatus(int delayInMillis) {
        String statusResponse = RingConstants.ERROR;
        LinkedList<String> response = new LinkedList<>();
        String wsResponse = runWebSocketRequest(this.deviceInfoData, delayInMillis, response);
        if (wsResponse.equals(RingConstants.SUCCESS)) {
            if (CollectionUtils.isNullOrEmpty(response)) {
                return statusResponse;
            } else {
                String message = StringUtils.removeEnd(StringUtils.substringAfter(response.getLast(), "message\","), "]");
                JSONObject messageJsonObject = new JSONObject(message);
                JSONArray bodyJsonArray = messageJsonObject.getJSONArray("body");
                for (Object object : bodyJsonArray) {
                    JSONObject deviceJsonObject = ((JSONObject) object).getJSONObject("device");
                    if (deviceJsonObject != null && deviceJsonObject.getJSONObject("v1") != null && !deviceJsonObject.getJSONObject("v1").isNull("mode")) {
                        statusResponse = deviceJsonObject.getJSONObject("v1").getString("mode");
                        break;
                    }
                }
            }
        }

        return statusResponse;
    }

    private String callWebSocketForDeviceZid(int delayInMillis) {
        String statusResponse = RingConstants.ERROR;
        LinkedList<String> response = new LinkedList<>();
        String wsResponse = runWebSocketRequest(this.deviceInfoData, delayInMillis, response);
        if (wsResponse.equals(RingConstants.SUCCESS)) {
            if (CollectionUtils.isNullOrEmpty(response)) {
                return statusResponse;
            } else {
                String message = StringUtils.removeEnd(StringUtils.substringAfter(response.getLast(), "message\","), "]");
                JSONObject messageJsonObject = new JSONObject(message);
                JSONArray bodyJsonArray = messageJsonObject.getJSONArray("body");
                for (Object object : bodyJsonArray) {
                    JSONObject deviceJsonObject = ((JSONObject) object).getJSONObject("general");
                    if (deviceJsonObject != null && deviceJsonObject.getJSONObject("v2") != null
                            && !deviceJsonObject.getJSONObject("v2").isNull("deviceType")
                            && deviceJsonObject.getJSONObject("v2").getString("deviceType").equals("access-code")) {
                        statusResponse = deviceJsonObject.getJSONObject("v2").getString("adapterZid");
                        break;
                    }
                }
            }
        }

        return statusResponse;
    }
}
