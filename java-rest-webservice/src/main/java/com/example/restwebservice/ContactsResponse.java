package com.example.restwebservice;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

/**
 * Main body object of the rest response
 */
public class ContactsResponse {

    @ApiModelProperty(position = 1)
    private long total;
    @ApiModelProperty(position = 2)
    private List<Contact> contacts;
    @ApiModelProperty(position = 3)
    private String message;

    public ContactsResponse() {
        this.total = 0;
        this.contacts = new ArrayList<>();
        this.message = "";
    }

    public ContactsResponse(long total, List<Contact> contacts) {
        this.total = total;
        this.contacts = contacts;
        this.message = "";
    }

    public ContactsResponse(Contact contact) {
        this.total = 1;
        this.contacts = Arrays.asList(contact);
        this.message = "";
    }

    public ContactsResponse(String message) {
        this.total = 0;
        this.contacts = new ArrayList<>();
        this.message = message;
    }

    public long getTotal() {
        return total;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public String getMessage() {
        return message;
    }
}
