/*
 * Copyright 2023 Maestro Cloud Control LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.maestro3.agent.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class OsIpUtils {

    private static final Logger LOG = LogManager.getLogger(OsIpUtils.class);

    private static final Pattern CIDR_NOTATION = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+\\/\\d+");
    private static final Pattern BASE_NOTATION = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+");

    private OsIpUtils() {
    }

    public static boolean isCidrNotation(String addressPrefix) {
        return CIDR_NOTATION.matcher(addressPrefix).matches();
    }

    public static boolean isIpNotation(String addressPrefix) {
        return BASE_NOTATION.matcher(addressPrefix).matches();
    }

    public static String getCidrIp(String addressPrefix) {
        if (OsIpUtils.isIpNotation(addressPrefix)) {
            return addressPrefix + "/32";
        } else if (OsIpUtils.isCidrNotation(addressPrefix)) {
            return addressPrefix;
        } else {
            return null;
        }
    }

    public static List<String> ipRangeToCidr(long start, long end) {
        ArrayList<String> result = new ArrayList<>();
        while (end >= start) {
            byte maxSize = 32;
            while (maxSize > 0) {
                long mask = iMask(maxSize - 1);
                long maskBase = start & mask;

                if (maskBase != start) {
                    break;
                }

                maxSize--;
            }
            double x = Math.log(end - start + 1) / Math.log(2);
            byte maxDiff = (byte) (32 - Math.floor(x));
            if (maxSize < maxDiff) {
                maxSize = maxDiff;
            }
            String ip = longToIp(start);
            result.add(ip + "/" + maxSize);
            start += Math.pow(2, (32 - maxSize));
        }
        return result;
    }

    private static long iMask(int s) {
        return Math.round(Math.pow(2, 32) - Math.pow(2, (32 - s)));
    }

    public static long ipToLong(String ipString) {
        String[] ipAddressInArray = ipString.split("\\.");
        long num = 0;
        long ip;
        for (int x = 3; x >= 0; x--) {
            ip = Long.parseLong(ipAddressInArray[3 - x]);
            num |= ip << (x << 3);
        }
        return num;
    }

    private static String longToIp(long longIP) {
        return new StringBuffer("")
            .append(String.valueOf(longIP >>> 24))
            .append(".")
            .append(String.valueOf((longIP & 0x00FFFFFF) >>> 16))
            .append(".")
            .append(String.valueOf((longIP & 0x0000FFFF) >>> 8))
            .append(".")
            .append(String.valueOf(longIP & 0x000000FF))
            .toString();
    }

    public static int countIpInRange(String ip1, String ip2) throws IllegalArgumentException {
        int result = 0;
        String[] str1 = StringUtils.split(ip1, ".");
        String[] str2 = StringUtils.split(ip2, ".");

        int length1 = str1.length;
        int length2 = str2.length;
        if (length1 != length2) {
            throw new IllegalArgumentException();
        }

        Integer[] int1 = new Integer[length1];
        Integer[] int2 = new Integer[length2];

        for (int i = 0; i < length1; i++) {
            int1[i] = Integer.parseInt(str1[i]);
            int2[i] = Integer.parseInt(str2[i]);
        }

        for (int i = 0; i < int1.length; i++) {
            Integer range1 = int1[i];
            Integer range2 = int2[i];
            int diff = range2 - range1;
            diff = new Double(diff * Math.pow(256, int1.length - i - 1)).intValue();
            result += diff;
        }
        result++;
        return result;
    }

    public static boolean onlyLocalAddresses(List<String> securityGroupRanges) {
        boolean onlyLocalAddresses = true;
        try {
            for (String securityGroupRange : securityGroupRanges) {
                if (securityGroupRange.contains("/0"))
                    return false;
                SubnetUtils utils = new SubnetUtils(securityGroupRange);
                String ip = utils.getInfo().getAddress();
                InetAddress inetAddress = InetAddress.getByName(ip);
                if (!inetAddress.isSiteLocalAddress()) {
                    return false;
                }
            }
        } catch (Exception e) {
            LOG.error("Error occurred: " + e.getMessage(), e);
            onlyLocalAddresses = false;
        }
        return onlyLocalAddresses;
    }

    //to convert ip addr from '192.168.0.1' to '192.168.0.1/32'
    public static String convertSingleIpAddr(String addr) {
        if (OsIpUtils.isIpNotation(addr)) {
            return addr + "/32";
        }
        return addr;
    }

    public static boolean isV4Address(String addr) {
        return isIpNotation(addr) || isCidrNotation(addr);
    }
}
