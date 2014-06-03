package com.appearnetworks.aiq.integrationframework.integration;

/**
 * Document reference.
 *
 * Document id and type can only contain characters {@code a-zA-Z0-9.~-_} (corresponds to the unreserved characters of an URI
 * according to <a href="http://tools.ietf.org/html/rfc3986#section-2.3">RFC-3986</a>) and must be between 1 and 250 characters long.
 */
public final class DocumentReference {
    public final String _id;
    public final String _type;
    public final long _rev;

    /**
     * @param _id    document id
     * @param _type  document type
     * @param _rev   document revision
     */
    public DocumentReference(String _id, String _type, long _rev) {
        this._id = _id;
        this._type = _type;
        this._rev = _rev;
    }

    /**
     * @param document  business document
     */
    public DocumentReference(BusinessDocument document) {
        this(document.get_id(), document.get_type(), document.get_rev());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocumentReference that = (DocumentReference) o;

        if (_rev != that._rev) return false;
        if (_id != null ? !_id.equals(that._id) : that._id != null) return false;
        if (_type != null ? !_type.equals(that._type) : that._type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _id != null ? _id.hashCode() : 0;
        result = 31 * result + (_type != null ? _type.hashCode() : 0);
        result = 31 * result + (int) (_rev ^ (_rev >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "DocumentReference{" +
                "_id='" + _id + '\'' +
                ", _type='" + _type + '\'' +
                ", _rev=" + _rev +
                '}';
    }
}
