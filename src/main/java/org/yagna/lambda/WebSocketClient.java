package org.yagna.lambda;

import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.handshake.HandshakeImpl1Client;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.protocols.IProtocol;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by asish on 8/25/18.
 */
public class WebSocketClient extends org.java_websocket.client.WebSocketClient {
    private static Draft_6455 draftOcppOnly;

    static {
        HandshakeImpl1Client handshakedataExtension = new HandshakeImpl1Client();
        handshakedataExtension.put("Upgrade", "websocket");
        handshakedataExtension.put("Connection", "Upgrade");
        handshakedataExtension.put("Sec-WebSocket-Version", "13");
        handshakedataExtension.put("Sec-WebSocket-Extension", "permessage-deflate");

        ArrayList<IProtocol> protocols = new ArrayList<IProtocol>();
        protocols.add(new org.java_websocket.protocols.Protocol("ocpp2.0"));
        protocols.add(new org.java_websocket.protocols.Protocol(""));
        draftOcppOnly = new Draft_6455(Collections.<IExtension>emptyList(), protocols);
        try {
            draftOcppOnly.acceptHandshakeAsServer(handshakedataExtension);
        } catch (InvalidHandshakeException e) {
            e.printStackTrace();
        }
    }

    LinkedList<String> response;

    public WebSocketClient(URI serverUri, Draft draft, LinkedList<String> response) {
        super(serverUri, draft);
        this.response = response;
    }

    public WebSocketClient(URI serverURI, LinkedList<String> response) {
        super(serverURI, draftOcppOnly);
        this.response = response;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("opened connection");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("received: " + message);
        this.response.add(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}
