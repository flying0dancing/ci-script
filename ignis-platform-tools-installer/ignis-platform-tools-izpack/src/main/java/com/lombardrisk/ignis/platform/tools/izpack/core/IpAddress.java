package com.lombardrisk.ignis.platform.tools.izpack.core;

import com.izforge.izpack.api.exception.IzPackException;
import lombok.experimental.UtilityClass;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

@UtilityClass
public final class IpAddress {

    public static boolean isCurrentIpAddress(final InetAddress addr) {
        if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()) {
            return true;
        }
        try {
            return NetworkInterface.getByInetAddress(addr) != null;
        } catch (SocketException e) {
            throw new IzPackException(e);
        }
    }

    public static boolean isSameIpAddress(final String host1, final String host2) {
        if (host1.equals(host2)) {
            return true;
        }
        try {
            InetAddress host1Address = InetAddress.getByName(host1);
            InetAddress host2Address = InetAddress.getByName(host2);

            return host1Address != null
                    && host2Address != null
                    && host1Address.getHostAddress().equals(host2Address.getHostAddress());
        } catch (@SuppressWarnings("squid:S1166") UnknownHostException e) {
            return false;
        }
    }
}
