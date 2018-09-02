package org.yagna.lambda;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.yagna.lambda.constants.RingConstants;
import org.yagna.lambda.model.RingInput;

public class RingUtilTest {

    private RingInput ringInput;

    @Before
    public void setUp() {
        Assert.assertTrue(StringUtils.isNoneBlank(System.getProperty("user")));
        Assert.assertTrue(StringUtils.isNoneBlank(System.getProperty("password")));
        Assert.assertTrue(StringUtils.isNoneBlank(System.getProperty("locationId")));
        Assert.assertTrue(StringUtils.isNoneBlank(System.getProperty("zid")));
        ringInput = new RingInput(System.getProperty("user"), System.getProperty("password"),
                System.getProperty("locationId"), System.getProperty("zid"));
    }

    @Test
    public void disArmMode() {
        Assert.assertEquals(RingConstants.SUCCESS, RingUtil.instance(ringInput).disArmMode());
    }

    @Test
    public void setHomeMode() {
    }

    @Test
    public void setAway() {
    }

    @Test
    public void getStatus() {
        Assert.assertNotEquals(RingConstants.ERROR, RingUtil.instance(ringInput).getStatus());
    }

    @Test
    public void getZid() {
        Assert.assertNotEquals(RingConstants.ERROR, RingUtil.instance(ringInput).getZid());
    }
}