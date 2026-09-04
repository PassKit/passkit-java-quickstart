package com.passkit.quickstart;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AppConfigTest {
    @Test
    public void defaultsAreDeveloperFriendly() throws Exception {
        AppConfig config = AppConfig.load();
        assertTrue(config.port() > 0);
        assertTrue(config.poolSize() >= 1);
        assertTrue(!config.host().isBlank());
    }
}
