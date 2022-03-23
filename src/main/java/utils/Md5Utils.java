package utils;


import org.apache.commons.codec.digest.DigestUtils;

public class Md5Utils {
    public static String hash(String inputString){
        String md5Hex = DigestUtils.md5Hex(inputString).toLowerCase();
        return md5Hex;
    }
}
