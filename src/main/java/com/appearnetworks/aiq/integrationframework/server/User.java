package com.appearnetworks.aiq.integrationframework.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents user details
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {
    private String _id;
    private String username;
    private String email; // optional
    private String fullName; // optional
    private Map<String,String> profile;
    private List<String> roles;

    /**
     * Needed for Jackson deserialization, do not use.
     */
    public User() { }

    /**
     * Users can only be created on the server and this constructor should only be used for unit testing.
     */
    public User(String _id, String username, String email, String fullName, Map<String, String> profile, List<String> roles) {
        this._id = _id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.profile = profile;
        this.roles = new ArrayList<>(roles.size());
        this.roles = roles;
    }

    /**
     * @return unique id of the user.
     */
    public String get_id() {
        return _id;
    }

    /**
     * @return username of the user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return email address of the user.
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return full name of the user.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @return user profile
     */
    public Map<String, String> getProfile() {
        return profile;
    }

    /**
     * @return list of roles assigned to the user
     */
    public List<String> getRoles() {
        return roles;
    }
}
