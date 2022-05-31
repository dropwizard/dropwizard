package io.dropwizard.jetty;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;

import static org.assertj.core.api.Assertions.assertThat;

class NetUtilTest {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @DisabledIf("isTcpBacklogSettingReadable")
    void testDefaultTcpBacklogForWindows() {
        assertThat(NetUtil.getTcpBacklog()).isEqualTo(NetUtil.DEFAULT_TCP_BACKLOG_WINDOWS);
    }

    @Test
    @DisabledIf("isTcpBacklogSettingReadable")
    @EnabledOnOs(OS.MAC)
    void testNonWindowsDefaultTcpBacklog() {
        assertThat(NetUtil.getTcpBacklog()).isEqualTo(NetUtil.DEFAULT_TCP_BACKLOG_LINUX);
    }

    @Test
    @DisabledIf("isTcpBacklogSettingReadable")
    @EnabledOnOs(OS.MAC)
    void testNonWindowsSpecifiedTcpBacklog() {
        assertThat(NetUtil.getTcpBacklog(100)).isEqualTo(100);
    }

    @Test
    @EnabledIf("isTcpBacklogSettingReadable")
    @EnabledOnOs(OS.LINUX)
    void testOsTcpBackloc() {
        assertThat(NetUtil.getTcpBacklog(-1)).isNotEqualTo(-1);
        assertThat(NetUtil.getTcpBacklog())
            .as("NetUtil should read more than the first character of somaxconn")
            .isGreaterThan(2);
    }

    private static boolean isTcpBacklogSettingReadable() {
        return AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> {
            try {
                File f = new File(NetUtil.TCP_BACKLOG_SETTING_LOCATION);
                return f.exists() && f.canRead();
            } catch (Exception e) {
                return false;
            }
        });
    }
}
