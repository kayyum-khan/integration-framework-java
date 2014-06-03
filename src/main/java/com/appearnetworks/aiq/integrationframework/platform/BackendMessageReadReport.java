package com.appearnetworks.aiq.integrationframework.platform;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

/**
 * Represents the read report submitted by a platform user for a specific backend message
 *
 * @see BackendMessage
 * @see PlatformUser
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BackendMessageReadReport {
    private PlatformUser user;
    private long revision;
    private long readTimestamp;

    /**
     * Needed for Jackson deserialization, do not use.
     */
    public BackendMessageReadReport() { }

    /**
     * Backend message read reports can only be created on the platform and this constructor should only be used for unit testing.
     *
     * @param user user who read the message
     * @param revision revision of the read report
     * @param readTimestamp timestamp when the message was read by user
     */
    public BackendMessageReadReport(PlatformUser user, long revision, long readTimestamp) {
        this.user = user;
        this.revision = revision;
        this.readTimestamp = readTimestamp;
    }

    /**
     * @return user who read the message
     */
    public PlatformUser getUser() {
        return user;
    }

    /**
     * @return revision of the read report
     */
    public long getRevision() {
        return revision;
    }

    /**
     * @return timestamp when the message was read by user
     */
    public Date getReadTimestamp() {
        return new Date(readTimestamp);
    }
}
