/** 
 * (C) Copyright 2012 Hal Hildebrand, All Rights Reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.yammer.dropwizard.config.validation.tests;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.File;

import org.junit.Test;

import com.google.common.io.Resources;
import com.yammer.dropwizard.config.ConfigurationException;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.validation.Validator;

/**
 * @author hhildebrand
 * 
 */
public class PortRangeTest {

    @Test
    public void testValidPortRange() throws Exception {
        PortRangeConfiguration config = ConfigurationFactory.forClass(PortRangeConfiguration.class,
                                                                      new Validator()).build(new File(
                                                                                                      Resources.getResource("yaml/validPortRange.yml").getFile()));
        assertNotNull(config);
        assertEquals(2048, config.testPort);
    }

    @Test
    public void testDyamicdPortRange() throws Exception {
        PortRangeConfiguration config = ConfigurationFactory.forClass(PortRangeConfiguration.class,
                                                                      new Validator()).build(new File(
                                                                                                      Resources.getResource("yaml/dynamicPortRange.yml").getFile()));
        assertNotNull(config);
        assertEquals(0, config.testPort);
    }

    @Test
    public void testInvalidPortRange() throws Exception {
        try {
            ConfigurationFactory.forClass(PortRangeConfiguration.class,
                                          new Validator()).build(new File(
                                                                          Resources.getResource("yaml/invalidPortRange.yml").getFile()));
            fail("should have thrown a configuration exception for an invalid port range");
        } catch (ConfigurationException e) {
            assertTrue(e.getMessage().endsWith("invalidPortRange.yml has the following errors:\n  * testPort port range must be between 1025 and 65535 (was 80)\n"));
        }
    }

}
