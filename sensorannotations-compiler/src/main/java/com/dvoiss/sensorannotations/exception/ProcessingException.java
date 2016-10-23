package com.dvoiss.sensorannotations.exception;

import javax.lang.model.element.Element;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Processor exception class for errors that occur during processing.
 */
public class ProcessingException extends Exception {
    @Nullable private final Element mElement;

    public ProcessingException(@Nullable Element element, @Nullable String message) {
        super(message);
        this.mElement = element;
    }

    @Nullable
    public Element getElement() {
        return mElement;
    }
}
