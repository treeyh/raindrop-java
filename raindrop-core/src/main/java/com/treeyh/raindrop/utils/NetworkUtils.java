package com.treeyh.raindrop.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Objects;

/**
 * @author: Treeyh
 * @version: 1.0
 * @description: 网络帮助类
 * @create: 2023-04-07 09:20
 * @email: tree@ejyi.com
 **/
@Slf4j
public class NetworkUtils {

    private final static String loopIp = "127.0.0.1";

    /**
     * 获取内网IP地址和Mac
     * @return 字符串数组，索引0 为IP；索引1 为 mac地址
     */
    public static String[] getLocalIpAndMac() {
        String[] ss = new String[]{loopIp, ""};
        String ip = "";
        String mac = "";
        try {
            InetAddress candidateAddress = null;

            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface iface = networkInterfaces.nextElement();
                // 该网卡接口下的ip会有多个，也需要一个个的遍历，找到自己所需要的
                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = inetAddrs.nextElement();
                    // 排除loopback回环类型地址（不管是IPv4还是IPv6 只要是回环地址都会返回true）
                    candidateAddress = inetAddr;
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了 就是我们要找的
                            // ~~~~~~~~~~~~~绝大部分情况下都会在此处返回你的ip地址值~~~~~~~~~~~~~
                            ip = inetAddr.getHostAddress();
                            mac = getLocalInetMac(inetAddr);

                            if(StrUtils.isEmpty(ip) || StrUtils.isEmpty(mac)) {
                                continue;
                            }
                            ss[0] = ip;
                            ss[1] = mac;
                            return ss;
                        }
                    }
                }
            }

            ip = candidateAddress.getHostAddress();
            mac = getLocalInetMac(candidateAddress);
            if(StrUtils.isNotEmpty(ip)) {
                ss[0] = ip;
            }
            if (StrUtils.isNotEmpty(mac)) {
                ss[1] = mac;
            } else {
                ss[1] = StrUtils.isNotEmpty(candidateAddress.getHostName()) ? candidateAddress.getHostName() : ss[1];
            }
            return ss;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ss;
    }

    /**
     * 获取InetAddress的mac地址
     * @return
     */
    public static String getLocalInetMac(InetAddress candidateAddress) {
        if (null == candidateAddress) {
            return "";
        }
        byte[] mac = new byte[0];
        try {
            // NetworkInterface.getByInetAddress(ia) 根据ip信息获取网卡信息
            mac = NetworkInterface.getByInetAddress(candidateAddress).getHardwareAddress();
        } catch (SocketException e) {
            log.error(e.getMessage(), e);
        }
        if (Objects.equals(null, mac)) {
            return "";
        }

        StringBuilder sb = new StringBuilder("");
        for(int i=0; i<mac.length; i++) {
            if(i!=0) {
                sb.append(":");
            }
            //字节转换为整数
            int temp = mac[i]&0xff;
            // 把无符号整数参数所表示的值转换成以十六进制表示的字符串
            String str = Integer.toHexString(temp);
            if(str.length()==1) {
                sb.append("0"+str);
            }else {
                sb.append(str);
            }
        }

        return sb.toString();
    }

//    /**
//     * 获取内网IP
//     * @return
//     */
//    public static String getLocalIp() {
//        InetAddress candidateAddress = getLocalInet();
//
//        return null == candidateAddress ? loopIp : candidateAddress.getHostAddress();
//    }
//
//
//    /**
//     * 获取内网ip的mac地址
//     * @return
//     */
//    public static String getLocalIpMac() {
//        InetAddress candidateAddress = getLocalInet();
//        if (null == candidateAddress) {
//            return "";
//        }
//        byte[] mac = new byte[0];
//        try {
//            // NetworkInterface.getByInetAddress(ia) 根据ip信息获取网卡信息
//            mac = NetworkInterface.getByInetAddress(candidateAddress).getHardwareAddress();
//        } catch (SocketException e) {
//            log.error(e.getMessage(), e);
//        }
//        if (Objects.equals(null, mac)) {
//            return "";
//        }
//
//        StringBuilder sb = new StringBuilder("");
//        for(int i=0; i<mac.length; i++) {
//            if(i!=0) {
//                sb.append(":");
//            }
//            //字节转换为整数
//            int temp = mac[i]&0xff;
//            // 把无符号整数参数所表示的值转换成以十六进制表示的字符串
//            String str = Integer.toHexString(temp);
//            if(str.length()==1) {
//                sb.append("0"+str);
//            }else {
//                sb.append(str);
//            }
//        }
//
//        return sb.toString();
//    }
//
//    private static InetAddress getLocalInet() {
//
//        try {
//            InetAddress candidateAddress = null;
//
//            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
//            while (networkInterfaces.hasMoreElements()) {
//                NetworkInterface iface = networkInterfaces.nextElement();
//                // 该网卡接口下的ip会有多个，也需要一个个的遍历，找到自己所需要的
//                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
//                    InetAddress inetAddr = inetAddrs.nextElement();
//                    // 排除loopback回环类型地址（不管是IPv4还是IPv6 只要是回环地址都会返回true）
//                    if (!inetAddr.isLoopbackAddress()) {
//                        if (inetAddr.isSiteLocalAddress()) {
//                            // 如果是site-local地址，就是它了 就是我们要找的
//                            // ~~~~~~~~~~~~~绝大部分情况下都会在此处返回你的ip地址值~~~~~~~~~~~~~
//                            return inetAddr;
//                        }
//
//                        // 若不是site-local地址 那就记录下该地址当作候选
//                        if (candidateAddress == null) {
//                            candidateAddress = inetAddr;
//                        }
//
//                    }
//                }
//            }
//
//            // 如果出去loopback回环地之外无其它地址了，那就回退到原始方案吧
//            return candidateAddress == null ? InetAddress.getLocalHost() : candidateAddress;
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
//        return null;
//    }
}
