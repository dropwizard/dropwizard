/**
 * Copyright 2013-2014 The Apache Software Foundation (Curator Project)
 *
 * The Apache Software Foundation licenses this file to you under the Apache
 * License, version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

/**
 * @see https://github.com/apache/curator/blob/master/curator-x-discovery/src/main/java/org/apache/curator/x/discovery/LocalIpFilter.java
 */
public interface LocalIpFilter {

    public boolean use(NetworkInterface networkInterface,
                       InetAddress address) throws SocketException;
}