package com.ritesh.scalablefileupload.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class User {
    @Id
    private int userid;
    private String username;
    private String useremail;
    private String userpaswword;

    public User() {}

    public User(int userid, String userpaswword, String username, String useremail) {
        this.userid = userid;
        this.userpaswword = userpaswword;
        this.username = username;
        this.useremail = useremail;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getUserpaswword() {
        return userpaswword;
    }

    public void setUserpaswword(String userpaswword) {
        this.userpaswword = userpaswword;
    }

    public String getUseremail() {
        return useremail;
    }

    public void setUseremail(String useremail) {
        this.useremail = useremail;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    @Override
    public String toString() {
        return "User{" +
                "userid=" + userid +
                ", username='" + username + '\'' +
                ", useremail='" + useremail + '\'' +
                ", userpaswword='" + userpaswword + '\'' +
                '}';
    }

}

