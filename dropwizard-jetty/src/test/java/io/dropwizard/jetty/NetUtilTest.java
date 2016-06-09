package io.dropwizard.jetty;

import org.junit.Test;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assume.assumeThat;

public class NetUtilTest {

    private static final String OS_NAME_PROPERTY = "os.name";

    /**
     * Assuming Windows
     */
    @Test
    public void testDefaultTcpBacklogForWindows() {
        assumeThat(System.getProperty(OS_NAME_PROPERTY),containsString("win"));
        assumeThat(isTcpBacklogSettingReadable(),is(false));
        assertEquals(NetUtil.DEFAULT_TCP_BACKLOG_WINDOWS, NetUtil.getTcpBacklog());
    }

    /**
     * Assuming Mac (which does not have /proc)
     */
    @Test
    public void testNonWindowsDefaultTcpBacklog() {
        assumeThat(System.getProperty(OS_NAME_PROPERTY), containsString("Mac OS X"));
        assumeThat(isTcpBacklogSettingReadable(),is(false));
        assertEquals(NetUtil.DEFAULT_TCP_BACKLOG_LINUX, NetUtil.getTcpBacklog());

    }

    /**
     * Assuming Mac (which does not have /proc)
     */
    @Test
    public void testNonWindowsSpecifiedTcpBacklog() {
        assumeThat(System.getProperty(OS_NAME_PROPERTY), containsString("Mac OS X"));
        assumeThat(isTcpBacklogSettingReadable(),is(false));
        assertEquals(100, NetUtil.getTcpBacklog(100));
    }

    /**
     * Assuming Linux (which has /proc)
     */
    @Test
    public void testOsSetting() {
        assumeThat(System.getProperty(OS_NAME_PROPERTY),containsString("Linux"));
        assumeThat(isTcpBacklogSettingReadable(),is(true));
        assertNotEquals(-1, NetUtil.getTcpBacklog(-1));
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
