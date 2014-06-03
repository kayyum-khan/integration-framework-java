package com.appearnetworks.aiq.integrationframework.impl.integration;

import com.appearnetworks.aiq.integrationframework.integration.DocumentReference;

import java.util.List;

public final class ListDocumentsResponse {
  public final List<DocumentReference> documentReferences;

  public ListDocumentsResponse(List<DocumentReference> documentReferences) {
    this.documentReferences = documentReferences;
  }
}
