package com.appearnetworks.aiq.integrationframework.impl.integration;

import com.appearnetworks.aiq.integrationframework.integration.DocumentReference;

import java.util.Collection;

public final class ListDocumentsResponse {
  public final Collection<? extends DocumentReference> documentReferences;

  public ListDocumentsResponse(Collection<? extends DocumentReference> documentReferences) {
    this.documentReferences = documentReferences;
  }
}
