/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.dropwizard.jetty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;

/**
 * This class is taken from the Netty project, and all credit goes to them.
 * It has been modified, to remove dependencies on other classes, and to convert to methods, rather than a
 * static value.
 */
class NetUtil {
    static final int DEFAULT_TCP_BACKLOG_WINDOWS = 200;
    static final int DEFAULT_TCP_BACKLOG_LINUX = 128;
    static final String TCP_BACKLOG_SETTING_LOCATION = "/proc/sys/net/core/somaxconn";

    /**
     * The SOMAXCONN value of the current machine.  If failed to get the value,  {@code 200}  is used as a
     * default value for Windows or {@code 128} for others.
     */
    public static int getTcpBacklog() {
        return getTcpBacklog(getDefaultTcpBacklog());
    }

    static int getTcpBacklog(int tcpBacklog) {
        // Taken from netty.

        // As a SecurityManager may prevent reading the somaxconn file we wrap this in a privileged block.
        //
        // See https://github.com/netty/netty/issues/3680
        return AccessController.doPrivileged((PrivilegedAction<Integer>) () -> {
            // Determine the default somaxconn (server socket backlog) value of the platform.
            // The known defaults:
            // - Windows NT Server 4.0+: 200
            // - Linux and Mac OS X: 128
            final File file = new File(TCP_BACKLOG_SETTING_LOCATION);
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                return Integer.parseInt(in.readLine().trim());
            } catch (SecurityException | IOException | NumberFormatException | NullPointerException e) {
                return tcpBacklog;
            }
        });
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.US).contains("win");
    }

    private static int getDefaultTcpBacklog() {
        return isWindows() ? DEFAULT_TCP_BACKLOG_WINDOWS : DEFAULT_TCP_BACKLOG_LINUX;
    }
}
