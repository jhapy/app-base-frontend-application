package org.jhapy.frontend.components;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 26/07/2020
 */
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.IconFactory;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.function.Consumer;

public class InputDialog extends Dialog {

  private final VerticalLayout rootLayout;
  private final Div header = new Div();
  private final FlexBoxLayout content = new FlexBoxLayout();
  private final Div footer = new Div();
  private final ButtonBar buttonBar = new ButtonBar();

  public InputDialog() {
    setCloseOnEsc(false);
    setCloseOnOutsideClick(false);

    getElement().getStyle().set("background-color", "var(--lumo-error-color)");

    content.setWidthFull();
    content.setFlexDirection(FlexDirection.COLUMN);
    header.setVisible(false);

    rootLayout = new VerticalLayout();
    add(rootLayout);
    rootLayout.setPadding(false);

    // make the footer break out of the content area of the dialog so it covers the whole bottom
    // area
    footer.getStyle().set("margin", "calc(var(--lumo-space-l) * -1)");
    footer.getStyle().set("margin-top", "var(--lumo-space-l)");
    footer.getStyle().set("width", "calc(100% + (2 * var(--lumo-space-l)))");
    footer.getStyle().set("box-sizing", "border-box");

    footer.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
    footer.getStyle().set("padding", "var(--lumo-space-s) var(--lumo-space-m)");

    rootLayout.add(header, content, footer);

    footer.add(buttonBar);
  }

  /**
   * Sets the given text as title in the header area. Previous content of the header area is
   * removed.
   *
   * @param title the title
   * @return this dialog
   */
  public InputDialog setTitle(String title) {
    setTitle(title, null);
    return this;
  }

  /**
   * Sets the given icon and text as title in the header area. Previous content of the header area
   * is removed.
   *
   * @param title the title
   * @param icon the icon
   * @return this dialog
   */
  public InputDialog setTitle(String title, Component icon) {
    getHeader().removeAll();

    getHeader().getStyle().set("display", "flex");
    getHeader().getStyle().set("align-items", "center");

    if (icon != null) {
      icon.getElement().getStyle().set("width", "var(--lumo-size-l)");
      icon.getElement().getStyle().set("height", "var(--lumo-size-l)");
      icon.getElement().getStyle().set("margin-right", "var(--lumo-space-m)");
      getHeader().add(icon);
    }

    H3 header = new H3(title);
    header.getStyle().set("margin", "0");
    header.getElement().getStyle().set("max-width", "25rem");
    header.getElement().getStyle().set("display", "inline");
    getHeader().add(header);

    return this;
  }

  /**
   * Sets the given text as message in the content area. Previous content of the content area is
   * removed.
   *
   * @param message the message
   * @return this dialog
   */
  public InputDialog setMessage(String message) {
    getContent().removeAll();

    Div content = new Div();
    content.setText(message);
    content.getElement().getStyle().set("max-width", "30rem");

    getContent().add(content);

    return this;
  }

  /**
   * Adds the header area and returns it.
   *
   * @return the header area
   */
  public Div getHeader() {
    header.setVisible(true);
    return header;
  }

  /**
   * Returns the content area.
   *
   * @return the content area
   */
  public FlexBoxLayout getContent() {
    return content;
  }

  public InputDialog setContent(Component... content) {
    getContent().removeAll();

    getContent().add(content);

    return this;
  }

  /**
   * Returns the footer area which contains the button bar, a separator and a details area by
   * default; the latter two initially invisible.
   *
   * @return the footer area
   */
  public Div getFooter() {
    return footer;
  }

  /**
   * Returns the button bar.
   *
   * @return the button bar
   */
  public InputDialog.ButtonBar getButtonBar() {
    if (buttonBar.getParent().isEmpty()) {
      throw new IllegalStateException("Button bar has been removed.");
    }

    return buttonBar;
  }

  /**
   * Adds a button to the right side of the button bar and returns it.
   *
   * @return the button
   * @see InputDialog.ButtonBar#addButton()
   * @see InputDialog.ButtonBar#add(Component)
   */
  public InputDialog.FluentButton addButton() {
    return getButtonBar().addButton();
  }

  /**
   * Adds a button to the left side of the button bar and returns it.
   *
   * @return the button
   * @see InputDialog.ButtonBar#addButtonToLeft()
   * @see InputDialog.ButtonBar#addToLeft(Component)
   */
  public InputDialog.FluentButton addButtonToLeft() {
    return getButtonBar().addButtonToLeft();
  }

  /**
   * Adds a button to the middle of the button bar and returns it.
   *
   * @return the button
   * @see InputDialog.ButtonBar#addButtonToMiddle()
   * @see InputDialog.ButtonBar#addToMiddle(Component)
   */
  public InputDialog.FluentButton addButtonToMiddle() {
    return getButtonBar().addButtonToMiddle();
  }

  public class ButtonBar extends Composite<FlexBoxLayout> {

    private final Div leftSpacer = new Div();

    private final Div rightSpacer = new Div();

    @Override
    protected FlexBoxLayout initContent() {
      FlexBoxLayout content = new FlexBoxLayout();
      content.setFlexDirection(FlexDirection.ROW);
      content.setWidthFull();
      leftSpacer.setMinWidth("var(--lumo-space-m)");
      rightSpacer.setMinWidth("var(--lumo-space-m)");
      content.add(leftSpacer, rightSpacer);
      content.setFlexGrow(1, leftSpacer);
      content.setFlexGrow(1, rightSpacer);
      return content;
    }

    /**
     * Adds the component to the right side of the button bar.
     *
     * @param <C> the component type
     * @param component the component to add
     * @return the component
     */
    public <C extends Component> C add(C component) {
      getContent().add(component);
      return component;
    }

    /**
     * Adds the component to the left side of the button bar.
     *
     * @param <C> the component type
     * @param component the component to add
     * @return the component
     */
    public <C extends Component> C addToLeft(C component) {
      getContent().addComponentAtIndex(getContent().indexOf(leftSpacer), component);
      return component;
    }

    /**
     * Adds the component to the middle of the button bar.
     *
     * @param <C> the component type
     * @param component the component to add
     * @return the component
     */
    public <C extends Component> C addToMiddle(C component) {
      getContent().addComponentAtIndex(getContent().indexOf(rightSpacer), component);
      return component;
    }

    /**
     * Adds a button to the right side of the button bar and returns it.
     *
     * @return the button
     */
    public InputDialog.FluentButton addButton() {
      return new InputDialog.FluentButton(add(new Button()));
    }

    /**
     * Adds a button to the left side of the button bar and returns it.
     *
     * @return the button
     */
    public InputDialog.FluentButton addButtonToLeft() {
      return new InputDialog.FluentButton(addToLeft(new Button()));
    }

    /**
     * Adds a button to the middle of the button bar and returns it.
     *
     * @return the button
     */
    public InputDialog.FluentButton addButtonToMiddle() {
      return new InputDialog.FluentButton(addToMiddle(new Button()));
    }
  }

  public class FluentButton {

    private final Button button;

    public FluentButton(Button button) {
      this.button = button;
    }

    public Button getButton() {
      return button;
    }

    public InputDialog.FluentButton text(String text) {
      button.setText(text);
      return this;
    }

    public InputDialog.FluentButton icon(Component icon) {
      button.setIcon(icon);
      return this;
    }

    public InputDialog.FluentButton icon(IconFactory icon) {
      button.setIcon(icon.create());
      return this;
    }

    public InputDialog.FluentButton title(String title) {
      button.getElement().setAttribute("title", title);
      return this;
    }

    public InputDialog.FluentButton variant(ButtonVariant variant) {
      button.addThemeVariants(variant);
      return this;
    }

    public InputDialog.FluentButton primary() {
      return variant(ButtonVariant.LUMO_PRIMARY);
    }

    public InputDialog.FluentButton tertiary() {
      return variant(ButtonVariant.LUMO_TERTIARY);
    }

    public InputDialog.FluentButton error() {
      return variant(ButtonVariant.LUMO_ERROR);
    }

    public InputDialog.FluentButton success() {
      return variant(ButtonVariant.LUMO_SUCCESS);
    }

    public InputDialog.FluentButton contrast() {
      return variant(ButtonVariant.LUMO_CONTRAST);
    }

    public InputDialog.FluentButton onClick(
        ComponentEventListener<ClickEvent<Button>> clickListener) {
      button.addClickListener(clickListener);
      return this;
    }

    public InputDialog.FluentButton clickShortcut(Key key, KeyModifier... keyModifiers) {
      button.addClickShortcut(key, keyModifiers);
      return this;
    }

    public InputDialog.FluentButton clickShortcutEscape() {
      return clickShortcut(Key.ESCAPE);
    }

    public InputDialog.FluentButton clickShortcutEnter() {
      return clickShortcut(Key.ENTER);
    }

    public InputDialog.FluentButton closeOnClick() {
      button.addClickListener(e -> close());
      return this;
    }

    public InputDialog.FluentButton with(Consumer<Button> configurator) {
      configurator.accept(button);
      return this;
    }
  }
}