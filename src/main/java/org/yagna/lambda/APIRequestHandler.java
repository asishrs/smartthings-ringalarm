package org.yagna.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.yagna.lambda.constants.RingConstants;
import org.yagna.lambda.model.RingInput;

import java.io.*;


/**
 * Created by asish on 8/25/18.
 * Main Class handles all API requests via API Gateway proxy
 */
public class APIRequestHandler implements RequestStreamHandler {

    JSONParser parser = new JSONParser();


    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {

        LambdaLogger logger = context.getLogger();
        logger.log("Loading Java Lambda handler of ProxyWithStream");


        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        JSONObject responseJson = new JSONObject();
        String responseCode = "200";
        String action = "";

        try {
            JSONObject event = (JSONObject) parser.parse(reader);

            if (event.get("pathParameters") != null) {
                JSONObject pps = (JSONObject) event.get("pathParameters");
                if (pps.get("ring-action") != null) {
                    action = (String) pps.get("ring-action");
                }
            }

            RingInput input = new RingInput();
            if (event.get("body") != null) {
                JSONObject body = (JSONObject) parser.parse((String) event.get("body"));
                if (body.get("user") != null) {
                    input.setUser((String) body.get("user"));
                }

                if (body.get("password") != null) {
                    input.setPassword((String) body.get("password"));
                }

                if (body.get("locationId") != null) {
                    input.setLocationId((String) body.get("locationId"));
                }

                if (body.get("zid") != null) {
                    input.setZid((String) body.get("zid"));
                }

            }

            JSONObject responseBody = new JSONObject();

            switch (action) {
                case "home":
                    setHome(input, responseBody);
                    break;
                case "away":
                    setAway(input, responseBody);
                    break;
                case "off":
                    disArmMode(input, responseBody);
                    break;
                case "status":
                    getMode(input, responseBody);
                    break;
                default:
                    responseBody.put("message", "Unknown");
            }

            responseJson.put("isBase64Encoded", false);
            responseJson.put("statusCode", responseCode);
            responseJson.put("body", responseBody.toString());

        } catch (ParseException pex) {
            responseJson.put("statusCode", "400");
            responseJson.put("exception", pex);
        }

        logger.log(responseJson.toJSONString());
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toJSONString());
        writer.close();
    }

    private void setHome(RingInput ringInput, JSONObject responseBody) {
        responseBody.put("message", RingUtil.instance(ringInput).setHomeMode().equals(RingConstants.SUCCESS) ?
                RingConstants.SUCCESS : RingConstants.FAILED);
    }

    private void disArmMode(RingInput ringInput, JSONObject responseBody) {
        responseBody.put("message", RingUtil.instance(ringInput).disArmMode().equals(RingConstants.SUCCESS) ?
                RingConstants.SUCCESS : RingConstants.FAILED);
    }

    private void setAway(RingInput ringInput, JSONObject responseBody) {
        responseBody.put("message", RingUtil.instance(ringInput).setAway().equals(RingConstants.SUCCESS) ?
                RingConstants.SUCCESS : RingConstants.FAILED);
    }

    private void getMode(RingInput ringInput, JSONObject responseBody) {
        responseBody.put("message", getAlarmStatus(RingUtil.instance(ringInput).getStatus()));
    }

    private String getAlarmStatus(String ringStatus) {
        switch (ringStatus) {
            case RingConstants.NONE:
                return "off";
            case RingConstants.SOME:
                return "home";
            case RingConstants.ALL:
                return "away";
            default:
                return "";
        }

    }
}
