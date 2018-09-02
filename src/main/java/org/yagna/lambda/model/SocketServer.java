package org.yagna.lambda.model;

/**
 * Created by asish on 8/25/18.
 */
public class SocketServer {

    private String server;
    private String authCode;

    public SocketServer(String server, String authCode) {
        this.server = server;
        this.authCode = authCode;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    @Override
    public String toString() {
        return "SocketServer{" +
                "server='" + server + '\'' +
                ", authCode='" + authCode + '\'' +
                '}';
    }
}
