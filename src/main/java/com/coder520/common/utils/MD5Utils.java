package com.coder520.common.utils;

import com.alibaba.druid.util.Base64;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {
    public static String encrptyPassword(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5=MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder=new BASE64Encoder();//处理乱码问题
        String result=base64Encoder.encode(md5.digest(password.getBytes("utf-8")));
        return result;
    }

    public static boolean checkPassword(String inputPwd,String dbPwd) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String result=encrptyPassword(inputPwd);
        if(result.equals(dbPwd)){
            return true;
        }else {
            return false;
        }

    }
}
