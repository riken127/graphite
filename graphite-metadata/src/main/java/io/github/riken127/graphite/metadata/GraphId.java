package io.github.riken127.graphite.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks the application identity property of a mapped graph object. */
@Retention(RetentionPolicy.RUNTIME)
@Target({
  ElementType.RECORD_COMPONENT,
  ElementType.FIELD,
  ElementType.METHOD,
  ElementType.PARAMETER
})
public @interface GraphId {}
