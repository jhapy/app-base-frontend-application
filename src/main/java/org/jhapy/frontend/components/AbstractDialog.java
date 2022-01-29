package org.jhapy.frontend.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.Lumo;

import java.util.List;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 16/08/2020
 */
@CssImport("./styles/my-dialog.css")
public abstract class AbstractDialog extends Dialog {

  public final String DOCK = "dock";
  public final String FULLSCREEN = "fullscreen";
  protected Button saveButton = null;
  private boolean isDocked = false;
  private boolean isFullScreen = false;
  private Header header;
  private Button max;
  private VerticalLayout content;
  private Footer footer;
  private H2 titleField;

  public AbstractDialog() {}

  public void open() {
    super.setDraggable(isDraggable());
    super.setModal(isModal());
    super.setResizable(isResizable());
    super.setCloseOnOutsideClick(false);
    // Dialog theming
    getElement().getThemeList().add("my-dialog");
    // setWidth("600px");

    // Accessibility
    getElement().setAttribute("aria-labelledby", "dialog-title");

    // Header
    titleField = new H2(getTitle());
    titleField.addClassName("dialog-title");

    max = new Button(VaadinIcon.EXPAND_SQUARE.create());
    max.addClickListener(event -> maximise());

    Button close = new Button(VaadinIcon.CLOSE_SMALL.create());

    header = new Header(titleField);
    if (canMaximize()) {
      header.add(max);
    }
    header.add(close);

    header.getElement().getThemeList().add(Lumo.DARK);
    add(header);

    content = new VerticalLayout(getContent());
    content.addClassName("dialog-content");
    content.setAlignItems(Alignment.STRETCH);
    add(content);

    close.addClickListener(
        event -> {
          if (onClose()) {
            close();
          }
        });

    // Footer
    Button cancelButton = new Button(getTranslation("action.global.cancel"));
    cancelButton.addClickListener(
        event -> {
          if (onClose()) {
            close();
          }
        });

    if (hasSaveButton()) {
      saveButton = new Button(getSaveButtonLabel());
      saveButton.addClickListener(
          event -> {
            if (onSave()) {
              close();
            }
          });
      saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }
    footer = new Footer(cancelButton);
    List<Component> buttons = getButtons();
    if (buttons != null && buttons.size() > 0) {
      footer.add(getButtons().toArray(new Component[0]));
    }
    if (hasSaveButton()) {
      footer.add(saveButton);
    }
    add(footer);

    // Button theming
    for (Button button : new Button[] {max, close}) {
      button.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_TERTIARY);
    }

    addResizeListener(this::onDialogResized);

    super.open();

    afterOpen();
  }

  protected boolean hasSaveButton() {
    return true;
  }

  protected void afterOpen() {}

  protected abstract String getTitle();

  public void setTitle(String value) {
    titleField.setText(value);
  }

  protected String getSaveButtonLabel() {
    return getTranslation("action.global.save");
  }

  protected abstract Component getContent();

  protected void onDialogResized(DialogResizeEvent event) {}

  protected boolean onClose() {
    return true;
  }

  protected boolean onSave() {
    return true;
  }

  protected List<Component> getButtons() {
    return null;
  }

  public boolean canMaximize() {
    return true;
  }

  public boolean isDraggable() {
    return true;
  }

  public boolean isModal() {
    return true;
  }

  public boolean isResizable() {
    return true;
  }

  private void initialSize() {
    getElement().getThemeList().remove(DOCK);
    max.setIcon(VaadinIcon.EXPAND_SQUARE.create());
    getElement().getThemeList().remove(FULLSCREEN);
    setHeight("auto");
    setWidth("auto");
  }

  private void maximise() {
    if (isFullScreen) {
      initialSize();
    } else {
      if (isDocked) {
        initialSize();
      }
      max.setIcon(VaadinIcon.COMPRESS_SQUARE.create());
      getElement().getThemeList().add(FULLSCREEN);
      setSizeFull();
      content.setVisible(true);
      footer.setVisible(true);
    }
    isFullScreen = !isFullScreen;
    isDocked = false;
  }
}
