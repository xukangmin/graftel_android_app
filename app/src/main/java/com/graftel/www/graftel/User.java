package com.graftel.www.graftel;

import android.os.StrictMode;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by Shorabh on 6/20/2016.
 */
public class User {

    private static String UID;
    private static String isTempUser;
    private static String loginEmail;
    private static String password;
    private static String companyName;
    private static String contactPersonName;

    public static FTPClient getmFtpClient() {
        try
        {
       //     StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            //    StrictMode.setThreadPolicy(policy);
            mFtpClient = new FTPClient();
            mFtpClient.setConnectTimeout(10 * 1000);
            mFtpClient.connect("port.magneticflowmetercalibration.com",48701);
            mFtpClient.login("FTPoutside", "brUth7#tAb");
            mFtpClient.setFileType(FTP.BINARY_FILE_TYPE);
//            mFtpClient.execPBSZ(0);
//            mFtpClient.execPROT("P");
            mFtpClient.enterLocalPassiveMode();
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return mFtpClient;
    }

    public static void setmFtpClient(FTPClient mFtpClient) {
        User.mFtpClient = mFtpClient;
    }

    private static String shippingAddress;
    private static String phone;
    private static String fax;
    private static String addEmail1;
    private static String addEmail2;
    private static String addEmail3;
    private static String addEmail4;
    private static FTPClient mFtpClient;

    public User(JSONObject json) throws JSONException
    {
        setUID(json.getString("UID"));
        setIsTempUser(json.getString("IsTempUser"));
        setLoginEmail(json.getString("LoginEmail"));
        setPassword(json.getString("Password"));
        setCompanyName(json.getString("CompanyName"));
        setContactPersonName(json.getString("ContactPersonName"));
        setShippingAddress(json.getString("ShippingAddress"));
        setPhone(json.getString("Phone"));
        setFax(json.getString("Fax"));
        setAddEmail1(json.getString("AdditionalEmail1"));
        setAddEmail2(json.getString("AdditionalEmail2"));
        setAddEmail3(json.getString("AdditionalEmail3"));
        setAddEmail4(json.getString("AdditionalEmail4"));
    }

    public static String getIsTempUser() {
        return isTempUser;
    }

    public static void setIsTempUser(String isTempUser) {
        User.isTempUser = isTempUser;
    }

    public static String getLoginEmail() {
        return loginEmail;
    }

    public static void setLoginEmail(String loginEmail) {
        User.loginEmail = loginEmail;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        User.password = password;
    }

    public static String getCompanyName() {
        return companyName;
    }

    public static void setCompanyName(String companyName) {
        if(!companyName.equals(null))
            User.companyName = companyName;
        else
            User.companyName = "";
    }

    public static String getContactPersonName() {
        return contactPersonName;
    }

    public static void setContactPersonName(String contactPersonName) {
        if(!contactPersonName.equals(null))
            User.contactPersonName = contactPersonName;
        else
            User.contactPersonName = "";
    }

    public static String getShippingAddress() {
        return shippingAddress;
    }

    public static void setShippingAddress(String shippingAddress) {
        if(!shippingAddress.equals(null))
            User.shippingAddress = shippingAddress;
        else
            User.shippingAddress = "";
    }

    public static String getPhone() {
        return phone;
    }

    public static void setPhone(String phone) {
        if(!phone.equals(null))
            User.phone = phone;
        else
            User.phone = "";
    }

    public static String getFax() {
        return fax;
    }

    public static void setFax(String fax) {
        if(!fax.equals("null"))
            User.fax = fax;
        else
            User.fax = "";
    }

    public static String getAddEmail1() {
        return addEmail1;
    }

    public static void setAddEmail1(String addEmail1) {

        if(!addEmail1.equals("null"))
            User.addEmail1 = addEmail1;
        else
            User.addEmail1 = "";
    }

    public static String getAddEmail2() {
        return addEmail2;
    }

    public static void setAddEmail2(String addEmail2) {
        if(!addEmail2.equals("null"))
            User.addEmail2 = addEmail2;
        else
            User.addEmail2 = "";
    }

    public static String getAddEmail3() {
        return addEmail3;
    }

    public static void setAddEmail3(String addEmail3) {
        if(!addEmail3.equals("null"))
            User.addEmail3 = addEmail3;
        else
            User.addEmail3 = "";
    }

    public static String getAddEmail4() {
        return addEmail4;
    }

    public static void setAddEmail4(String addEmail4) {
        if(!addEmail4.equals("null"))
            User.addEmail4 = addEmail4;
        else
            User.addEmail4 = "";
    }

    public static String getUID() {
        return UID;
    }

    public static void setUID(String UID) {
        User.UID = UID;
    }

}
