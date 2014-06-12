package com.appearnetworks.aiq.integrationframework.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.util.Date;
import java.util.List;

/**
 * Represents the backend message returned from server and the message read reports if any.
 * The returned message will not contain recipients data.
 *
 * @see BackendMessageReadReport
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnrichedBackendMessage extends BackendMessage {
    private String _id;
    private long created;
    private EnrichedBackendMessageLinks links;
    private List<BackendMessageReadReport> readBy;

    public EnrichedBackendMessage() { }

    public EnrichedBackendMessage(String type, Date activeFrom, int timeToLive, boolean urgent, String _launchable, ObjectNode payload, BackendMessageNotification notification, String _id, long created, EnrichedBackendMessageLinks links, List<BackendMessageReadReport> readBy) {
        super(type, activeFrom, timeToLive, urgent, _launchable, payload, notification);
        this._id = _id;
        this.created = created;
        this.links = links;
        this.readBy = readBy;
    }

    /**
     * @return id of the backend message
     */
    public String get_id() {
        return _id;
    }

    /**
     * @return created timestamp of the backend message
     */
    public Date getCreated() {
        return new Date(created);
    }

    /**
     *
     * @return the link to get this specific backend message (in the "self" key).
     */
    public EnrichedBackendMessageLinks getLinks() {
        return links;
    }

    /**
     * @return the link to this specific backend message
     */
    public URI getURL() {
        return URI.create(links.getSelf());
    }

    /**
     * @return the list of backend message read reports
     */
    public List<BackendMessageReadReport> getReadBy() {
        return readBy;
    }

    @Override
    public BackendMessageRecipients getRecipients() {
        throw new UnsupportedOperationException("read recipients data from fetched backend message");
    }
}
