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

package org.jhapy.frontend.crud;

import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.BaseEntity;
import org.jhapy.dto.domain.exception.DataIntegrityViolationException;
import org.jhapy.dto.domain.exception.EntityNotFoundException;
import org.jhapy.dto.domain.exception.OptimisticLockingFailureException;
import org.jhapy.dto.domain.exception.UserFriendlyDataException;
import org.jhapy.frontend.client.CrudService;
import org.jhapy.frontend.utils.messages.CrudErrorMessage;
import org.jhapy.frontend.views.HasNotifications;

import javax.validation.ConstraintViolationException;
import java.util.function.Consumer;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-26
 */
public class CrudEntityPresenter<E extends BaseEntity> implements HasLogger {

  private final CrudService<E> crudService;

  private final HasNotifications view;

  public CrudEntityPresenter(CrudService<E> crudService, HasNotifications view) {
    this.crudService = crudService;
    this.view = view;
  }

  public void delete(E entity, Consumer<E> onSuccess, Consumer<E> onFail) {
    if (executeOperation(() -> crudService.delete(entity))) {
      onSuccess.accept(entity);
    } else {
      onFail.accept(entity);
    }
  }

  public void save(E entity, Consumer<E> onSuccess, Consumer<E> onFail) {
    if (executeOperation(() -> saveEntity(entity))) {
      onSuccess.accept(entity);
    } else {
      onFail.accept(entity);
    }
  }

  private boolean executeOperation(Runnable operation) {
    try {
      operation.run();
      return true;
    } catch (UserFriendlyDataException e) {
      // Commit failed because of application-level data constraints
      consumeError(e, e.getMessage(), true);
    } catch (DataIntegrityViolationException e) {
      // Commit failed because of validation errors
      consumeError(e, CrudErrorMessage.OPERATION_PREVENTED_BY_REFERENCES, true);
    } catch (OptimisticLockingFailureException e) {
      consumeError(e, CrudErrorMessage.CONCURRENT_UPDATE, true);
    } catch (EntityNotFoundException e) {
      consumeError(e, CrudErrorMessage.ENTITY_NOT_FOUND, false);
    } catch (ConstraintViolationException e) {
      consumeError(e, CrudErrorMessage.REQUIRED_FIELDS_MISSING, false);
    }
    return false;
  }

  private void consumeError(Exception e, String message, boolean isPersistent) {
    logger().debug(message, e);
    view.showNotification(message, isPersistent);
  }

  private void saveEntity(E entity) {
    crudService.save(entity);
  }

  public boolean loadEntity(Long id, Consumer<E> onSuccess) {
    return executeOperation(() -> onSuccess.accept(crudService.load(id)));
  }
}