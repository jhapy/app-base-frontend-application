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

package org.jhapy.frontend.views;

import com.vaadin.flow.data.binder.ValidationException;

/**
 * A master / detail view for entities of the type <code>T</code>. The view has a list of entities
 * (the 'master' part) and a dialog to show a single entity (the 'detail' part). The dialog has two
 * modes: a view mode and an edit mode.
 *
 * <p>The view can also show notifications, error messages, and confirmation requests.
 *
 * @param <T> the entity type
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-06
 */
public interface EntityView<T> extends HasConfirmation, HasNotifications {

  /**
   * Shows an error notification with a given text.
   *
   * @param message a user-friendly error message
   * @param isPersistent if <code>true</code> the message requires a user action to disappear (if
   *     <code>false</code> it disappears automatically after some time)
   */
  default void showError(String message, boolean isPersistent) {
    showNotification(message, isPersistent);
  }

  /**
   * Returns the current dirty state of the entity dialog.
   *
   * @return <code>true</code> if the entity dialog is open in the 'edit' mode and has unsaved
   *     changes
   */
  boolean isDirty();

  /** Remove the reference to the entity and reset dirty status. */
  void clear();

  /**
   * Writes the changes from the entity dialog into the given entity instance (see {@link
   * com.vaadin.flow.data.binder.Binder#writeBean(Object)})
   *
   * @param entity the entity instance to save the changes into
   * @throws ValidationException if the values entered into the entity dialog cannot be converted
   *     into entity properties
   */
  void write(T entity) throws ValidationException;

  String getEntityName();

  default void showCreatedNotification() {
    showNotification(getEntityName() + " was created");
  }

  default void showUpdatedNotification() {
    showNotification(getEntityName() + " was updated");
  }

  default void showDeletedNotification() {
    showNotification(getEntityName() + " was deleted");
  }
}
