package org.jhapy.frontend.customFields;

import com.vaadin.flow.component.textfield.TextField;

public class TimeField extends TextField {
    public TimeField() {
       super();
        init();
    }

    public TimeField(String label) {
        super(label);
        init();
    }

    public TimeField(String label, String placeholder) {
        super(label, placeholder);
        init();
    }

    public TimeField(String label, String initialValue, String placeholder) {
        super(label, initialValue, placeholder);
        init();
    }

    public TimeField(ValueChangeListener<? super ComponentValueChangeEvent<TextField, String>> listener) {
        super(listener);
        init();
    }

    public TimeField(String label, ValueChangeListener<? super ComponentValueChangeEvent<TextField, String>> listener) {
        super(label, listener);
        init();
    }

    public TimeField(String label, String initialValue, ValueChangeListener<? super ComponentValueChangeEvent<TextField, String>> listener) {
        super(label, initialValue, listener);
        init();
    }

    public void setValue(String value) {
        if ( value == null )
            super.setValue("");
            else
            super.setValue(value);
    }

    protected void init() {
        setPattern("\\d*(:\\d{0,2})?");
        setClearButtonVisible(true);
    addValueChangeListener(
        event -> {
          if (event.getValue() != null) {
            String newValue;
            if (event.getValue().startsWith(":")) newValue = "0" + event.getValue();
            else if (event.getValue().endsWith(":")) newValue = event.getValue() + "00";
            else if (!event.getValue().contains(":")) newValue = event.getValue() + ":00";
            else newValue = null;

            if (newValue != null) {
              String[] valueSplit = newValue.split(":");
              int hours = Integer.parseInt(valueSplit[0]);
              int mins = Integer.parseInt(valueSplit[1]);
              if (mins < 0 || mins > 59|| hours<0 || hours > 24) {
                setInvalid(true);
                setErrorMessage("Invalid time");
              } else {
                  setInvalid(false);
                  setErrorMessage(null);
              }
            }
          }
        });
    }
}