package com.appearnetworks.aiq.integrationframework.integration;

/**
 * Revision for document and attachment.
 */
public final class DocumentAndAttachmentRevision {
  public final long documentRev;
  public final long attachmentRev;

  public DocumentAndAttachmentRevision(long documentRev, long attachmentRev) {
    this.documentRev = documentRev;
    this.attachmentRev = attachmentRev;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DocumentAndAttachmentRevision that = (DocumentAndAttachmentRevision) o;

    if (attachmentRev != that.attachmentRev) return false;
    if (documentRev != that.documentRev) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = (int) (documentRev ^ (documentRev >>> 32));
    result = 31 * result + (int) (attachmentRev ^ (attachmentRev >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "DocumentAndAttachmentRevision{" +
            "docRev=" + documentRev +
            ", attachmentRev=" + attachmentRev +
            '}';
  }
}
