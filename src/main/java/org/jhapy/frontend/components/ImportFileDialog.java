/*
 * Copyright 2020-2020 the original author or authors from the JHapy project.
 *
 * This file is part of the JHapy project, see https://www.jhapy.org/ for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jhapy.frontend.components;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.shared.Registration;
import java.io.Serializable;
import java.util.function.Consumer;
import org.jhapy.frontend.components.fileUpload.MemoryBuffer;

/**
 * A generic dialog for importing a file
 *
 * @param <T> The type of the action's subject
 */
public class ImportFileDialog<T extends Serializable> extends Dialog {

    private static final Runnable NO_OP = () -> {
    };
    private final H3 titleField = new H3();
    private final Div messageLabel = new Div();
    private final Div extraMessageLabel = new Div();
    private final Upload fileUpload;
    private final MemoryBuffer buffer = new MemoryBuffer();
    private final Button confirmButton = new Button();
    private final Button cancelButton = new Button("Cancel");
    private Registration registrationForConfirm;
    private Registration registrationForCancel;
    private Registration shortcutRegistrationForConfirm;

    /**
     * Constructor.
     */
    public ImportFileDialog() {
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);

        confirmButton.addClickListener(e -> close());
        confirmButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        confirmButton.setAutofocus(true);
        confirmButton.setEnabled(false);
        cancelButton.addClickListener(e -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttonBar = new HorizontalLayout(confirmButton,
            cancelButton);
        buttonBar.setClassName("buttons confirm-buttons");

        Div labels = new Div(messageLabel, extraMessageLabel);
        labels.setClassName("confirm-text");

        titleField.setClassName("confirm-title");

        fileUpload = new Upload(buffer);
        fileUpload.addSucceededListener(event -> {
            confirmButton.setEnabled(true);
        });

        add(titleField, labels, fileUpload, buttonBar);
    }

    /**
     * Opens the confirmation dialog.
     *
     * The dialog will display the given title and message(s), then call
     * <code>confirmHandler</code> if the Confirm button is clicked, or
     * <code>cancelHandler</code> if the Cancel button is clicked.
     *
     * @param title The title text
     * @param message Detail message (optional, may be empty)
     * @param additionalMessage Additional message (optional, may be empty)
     * @param actionName The action name to be shown on the Confirm button
     * @param confirmHandler The confirmation handler function
     * @param cancelHandler The cancellation handler function
     */
    public void open(String title, String message, String additionalMessage,
        String actionName,
        Consumer<byte[]> confirmHandler, Runnable cancelHandler) {
        titleField.setText(title);
        messageLabel.setText(message);
        extraMessageLabel.setText(additionalMessage);
        confirmButton.setText(actionName);

        shortcutRegistrationForConfirm = confirmButton
            .addClickShortcut(Key.ENTER);

        Runnable cancelAction = cancelHandler == null ? NO_OP : cancelHandler;

        if (registrationForConfirm != null) {
            registrationForConfirm.remove();
        }
        registrationForConfirm = confirmButton
            .addClickListener(e -> confirmHandler.accept(buffer.getBuf()));
        if (registrationForCancel != null) {
            registrationForCancel.remove();
        }
        registrationForCancel = cancelButton
            .addClickListener(e -> cancelAction.run());
        this.addOpenedChangeListener(e -> {
            if (!e.isOpened()) {
                cancelAction.run();
            }
        });
        confirmButton.removeThemeVariants(ButtonVariant.LUMO_ERROR);

        open();
    }

    @Override
    public void close() {
        super.close();
        if (shortcutRegistrationForConfirm != null) {
            shortcutRegistrationForConfirm.remove();
        }
    }
}
