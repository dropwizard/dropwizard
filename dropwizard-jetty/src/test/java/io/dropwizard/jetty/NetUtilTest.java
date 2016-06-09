package io.dropwizard.jetty;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class NetUtilTest {

    private static final String OS_NAME_PROPERTY = "os.name";
    public String osName;

    @Before
    public void setup() {
        osName = System.getProperty(OS_NAME_PROPERTY);
    }

    @After
    public void tearDown() {
        if (osName == null) {
            System.clearProperty(OS_NAME_PROPERTY);
        } else {
            System.setProperty(OS_NAME_PROPERTY, osName);
        }
    }

    @Test
    public void testDefaultTcpBacklogForWindows() {
        System.setProperty(OS_NAME_PROPERTY, "win");
        if (NetUtil.isWindows() && !isTcpBacklogSettingReadable()) {
            assertEquals(NetUtil.DEFAULT_TCP_BACKLOG_WINDOWS, NetUtil.getTcpBacklog());
        }
    }

    @Test
    public void testNonWindowsDefaultTcpBacklog() {
        System.setProperty(OS_NAME_PROPERTY, "lin");

        if (!NetUtil.isWindows() && !isTcpBacklogSettingReadable()) {
            assertEquals(NetUtil.DEFAULT_TCP_BACKLOG_LINUX, NetUtil.getTcpBacklog());
        }
    }

    @Test
    public void testNonWindowsSpecifiedTcpBacklog() {
        System.setProperty(OS_NAME_PROPERTY, "lin");

        if (!NetUtil.isWindows() && !isTcpBacklogSettingReadable()) {
            assertEquals(100, NetUtil.getTcpBacklog(100));
        }
    }

    @Test
    public void testOsSetting() {
        if(!NetUtil.isWindows() && isTcpBacklogSettingReadable()) {
            assertNotEquals(-1, NetUtil.getTcpBacklog(-1));
        }
    }


    public boolean isTcpBacklogSettingReadable() {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                try {
                    File f = new File(NetUtil.TCP_BACKLOG_SETTING_LOCATION);
                    return (f.exists() && f.canRead());
                } catch (Exception e) {
                    return false;
                }

            }
        });
    }
}
