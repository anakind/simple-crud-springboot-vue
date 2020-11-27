package com.example.restwebservice;

import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nullable;

/**
 * Contact place holder Object
 */
public class Contact {
    @ApiModelProperty(position = 1, required = false, hidden = true)
    private String id;
    @ApiModelProperty(position = 2, required = true)
    private String firstName;
    @ApiModelProperty(position = 3, required = true)
    private String lastName;
    @ApiModelProperty(position = 4, required = true)
    private String country;
    @ApiModelProperty(position = 5, required = true)
    private String phone;
    @ApiModelProperty(position = 6, required = false, hidden = true)
    private Long lastModified;

    /**
     * Constructor to create Contact instance
     *
     * @param id            id of the contact, nullable
     * @param firstName     first name of contanct
     * @param lastName      last name of contact
     * @param country       country resident of the contact
     * @param phone         phone number of the contact
     * @param lastModified  last modified timestamp, nullable
     */
    public Contact(@Nullable String id,
                   String firstName, String lastName,
                   String country, String phone,
                   @Nullable Long lastModified) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.country = country;
        this.phone = phone;
        this.lastModified = lastModified;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }
}
