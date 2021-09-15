package org.jhapy.frontend.components;

import com.vaadin.flow.component.textfield.AbstractNumberField;
import com.vaadin.flow.function.SerializableFunction;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

public class FloatField extends AbstractNumberField<FloatField, Float> {
  public FloatField() {
    this(new FloatField.Formatter());
  }

  public FloatField(String label) {
    this();
    this.setLabel(label);
  }

  public FloatField(String label, String placeholder) {
    this(label);
    this.setPlaceholder(placeholder);
  }

  public FloatField(
      ValueChangeListener<? super ComponentValueChangeEvent<FloatField, Float>> listener) {
    this();
    this.addValueChangeListener(listener);
  }

  public FloatField(
      String label,
      ValueChangeListener<? super ComponentValueChangeEvent<FloatField, Float>> listener) {
    this(label);
    this.addValueChangeListener(listener);
  }

  public FloatField(
      String label,
      Float initialValue,
      ValueChangeListener<? super ComponentValueChangeEvent<FloatField, Float>> listener) {
    this(label);
    this.setValue(initialValue);
    this.addValueChangeListener(listener);
  }

  private FloatField(FloatField.Formatter formatter) {
    super(formatter::parse, formatter, -1.0F / 0.0, 1.0F / 0.0);
  }

  public void setMin(double min) {
    super.setMin(min);
  }

  public double getMin() {
    return this.getMinDouble();
  }

  public void setMax(double max) {
    super.setMax(max);
  }

  public double getMax() {
    return this.getMaxDouble();
  }

  public void setStep(double step) {
    if (step <= 0.0D) {
      throw new IllegalArgumentException("The step cannot be less or equal to zero.");
    } else {
      super.setStep(step);
    }
  }

  public double getStep() {
    return this.getStepDouble();
  }

  private static class Formatter implements SerializableFunction<Float, String> {
    private final DecimalFormat decimalFormat;

    private Formatter() {
      this.decimalFormat =
          new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    }

    public String apply(Float valueFromModel) {
      return valueFromModel == null ? "" : this.decimalFormat.format(valueFromModel);
    }

    private Float parse(String valueFromClient) {
      try {
        return valueFromClient != null && !valueFromClient.isEmpty()
            ? this.decimalFormat.parse(valueFromClient).floatValue()
            : null;
      } catch (ParseException var3) {
        throw new NumberFormatException(valueFromClient);
      }
    }
  }
}