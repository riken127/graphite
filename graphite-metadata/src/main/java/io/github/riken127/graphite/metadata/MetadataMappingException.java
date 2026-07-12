package io.github.riken127.graphite.metadata;

/** Failure while constructing a mapped object from graph properties. */
public final class MetadataMappingException extends MetadataException {

  public MetadataMappingException(String message) {
    super(message);
  }

  public MetadataMappingException(String message, Throwable cause) {
    super(message, cause);
  }
}
