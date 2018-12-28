package com.seuic.zh.mydueros;

import java.io.Serializable;

public class MyList implements Serializable {
    private String type;
    private String contact;
    private String content;

    public MyList() {

    }

    public MyList (String type,String contact,String content) {
        this.type = type;
        this.contact = contact;
        this.content = content;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getContact() {
        return contact;
    }
    public void setContact(String contact) {
        this.contact = contact;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
}
