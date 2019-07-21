package com.example.liew.idelivery.Model;

/**
 * Created by Liew on 4/30/2018.
 */

public class User {

    private String name;
    private String password;
    private String phone;
    private String isstaff;
    private String secureCode;
    private String homeAddress;
    private String images;

    public User(){

    }

    public User(String Pname, String Ppassword, String PsecureCode,String image ){
        name = Pname;
        password = Ppassword;
        isstaff = "false";
        secureCode = PsecureCode;
        images = image;
    }


    public String getSecureCode() {
        return secureCode;
    }

    public void setSecureCode(String secureCode) {
        this.secureCode = secureCode;
    }

    public String getIsstaff() {
        return isstaff;
    }

    public void setIsstaff(String isstaff) {
        this.isstaff = isstaff;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String Pphone) {
        phone = Pphone;
    }

    public String getName(){
        return name;
    }

    public String getImage() {
        return images;
    }

    public void setImage(String image) {
        images = image;
    }

    public String setname(String Pname){
        name= Pname;
        return name;
    }

    public String getpassword(){
        return password;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }
}
