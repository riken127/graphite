package io.github.riken127.graphite.metadata;

/** Invalid metadata declaration or unsupported mapped Java type. */
public class MetadataException extends RuntimeException {

  public MetadataException(String message) {
    super(message);
  }

  public MetadataException(String message, Throwable cause) {
    super(message, cause);
  }
}
