package com.appearnetworks.aiq.integrationframework.platform;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.util.Date;

/**
 * Represents an active client session details and platform user associated with that session
 *
 * @see PlatformUser
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientSession {
    private PlatformUser user;
    private String deviceId;
    private Date created;
    private Date lastAccessed;
    private ObjectNode context;
    private String _id;
    private long _rev;
    private ClientSessionLinks links;

    /**
     * Needed for Jackson deserialization, do not use.
     */
    public ClientSession() {}

    /**
     * Client sessions can only be created on the platform and this constructor should only be used for unit testing.
     *
     * @param user platform user of the current session.
     * @param deviceId unique device id.
     * @param created timestamp when the session was created.
     * @param lastAccessed timestamp when the session was last accessed by the client.
     * @param context contents of the user's aggregated (both client and backend) context.
     * @param _id the session id.
     * @param _rev the session revision
     */
    public ClientSession(PlatformUser user, String deviceId, Date created,
                         Date lastAccessed, ObjectNode context, String _id, long _rev) {
        this.user = user;
        this.deviceId = deviceId;
        this.created = created;
        this.lastAccessed = lastAccessed;
        this.context = context;
        this._id = _id;
        this._rev = _rev;
    }

    /**
     * @return user of the current client session
     */
    public PlatformUser getUser() {
        return user;
    }

    /**
     * @return the unique ID of the device.
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * @return timestamp when the session was created.
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @return timestamp when the session was last accessed by the client.
     */
    public Date getLastAccessed() {
        return lastAccessed;
    }

    /**
     * @return contents of the user's aggregated (both client and backend) context (may be empty, but will not be absent).
     */
    public ObjectNode getContext() {
        return context;
    }

    /**
     * @return the session id
     */
    public String get_id() {
      return _id;
    }

    /**
     * @return the session revision
     */
    public long get_rev() {
        return _rev;
    }

    /**
     * @return the link to get this specific session (in the "self" key).
     */
    public ClientSessionLinks getLinks() {
        return links;
    }

    /**
     * @return the link to this specific session
     */
    public URI getURL() {
        return URI.create(links.getSelf());
    }
}
