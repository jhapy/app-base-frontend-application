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

package org.jhapy.frontend.utils.messages;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-06
 */
public class Message {

  public static final String CONFIRM_CAPTION_DELETE = "Confirm Delete";
  public static final String CONFIRM_MESSAGE_DELETE =
      "Are you sure you want to delete the selected Item? This action cannot be undone.";
  public static final String BUTTON_CAPTION_DELETE = "Delete";
  public static final String BUTTON_CAPTION_CANCEL = "Cancel";

  public static final MessageSupplier UNSAVED_CHANGES =
      createMessage(
          "Unsaved Changes",
          "Discard",
          "Continue Editing",
          "There are unsaved modifications to the %s. Discard changes?");

  public static final MessageSupplier CONFIRM_DELETE =
      createMessage(
          CONFIRM_CAPTION_DELETE,
          BUTTON_CAPTION_DELETE,
          BUTTON_CAPTION_CANCEL,
          CONFIRM_MESSAGE_DELETE);

  private final String caption;
  private final String okText;
  private final String cancelText;
  private final String message;

  public Message(String caption, String okText, String cancelText, String message) {
    this.caption = caption;
    this.okText = okText;
    this.cancelText = cancelText;
    this.message = message;
  }

  private static MessageSupplier createMessage(
      String caption, String okText, String cancelText, String message) {
    return (parameters) ->
        new Message(caption, okText, cancelText, String.format(message, parameters));
  }

  public String getCaption() {
    return caption;
  }

  public String getOkText() {
    return okText;
  }

  public String getCancelText() {
    return cancelText;
  }

  public String getMessage() {
    return message;
  }

  @FunctionalInterface
  public interface MessageSupplier {

    Message createMessage(Object... parameters);
  }
}