package org.yagna.lambda.model;

import com.amazonaws.util.ValidationUtils;

public class RingInput {
    private String user;
    private String password;
    private String locationId;
    private String zid;

    public RingInput() {
    }

    public RingInput(String user, String password, String locationId, String zid) {
        super();
        setUser(user);
        setPassword(password);
        setLocationId(locationId);
        setZid(zid);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        ValidationUtils.assertStringNotEmpty(user, "User Name");
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        ValidationUtils.assertStringNotEmpty(password, "Password");
        this.password = password;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        ValidationUtils.assertStringNotEmpty(locationId, "LocationId");
        this.locationId = locationId;
    }

    public String getZid() {
        return zid;
    }

    public void setZid(String zid) {
        ValidationUtils.assertStringNotEmpty(zid, "ZID");
        this.zid = zid;
    }

    @Override
    public String toString() {
        return "RingInput{" +
                "user='" + user + '\'' +
                ", locationId='" + locationId + '\'' +
                ", zid='" + zid + '\'' +
                '}';
    }
}
