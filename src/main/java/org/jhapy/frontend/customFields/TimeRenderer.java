package org.jhapy.frontend.customFields;

import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.data.renderer.BasicRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.function.ValueProvider;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class TimeRenderer<SOURCE> extends BasicRenderer<SOURCE, Integer> {

    private DateTimeFormatter formatter;
    private String nullRepresentation;
    
    public TimeRenderer(ValueProvider<SOURCE, Integer> valueProvider) {
        super(valueProvider);
    }

    @Override
    protected String getFormattedValue(Integer timeValue) {
        if ( timeValue == null )
            return "";
        var minutes = timeValue;
        var hours = minutes / 60;
        return hours + ":" + String.format("%02d", minutes - hours * 60);
    }
}