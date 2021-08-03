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

package org.jhapy.frontend.components.events;

import com.vaadin.flow.component.HasValue;
import org.jhapy.dto.domain.EntityTranslationV2;
import org.jhapy.frontend.customFields.DefaultCustomListFieldForTranslationV2;

import java.util.Map;

public class CustomListFieldValueChangeEventForTranslationV2<C extends EntityTranslationV2> implements
    HasValue.ValueChangeEvent<Map<String,C>> {

  private final Map<String,C> oldValues;
  private final Map<String,C> newValues;
  private final DefaultCustomListFieldForTranslationV2 src;

  public CustomListFieldValueChangeEventForTranslationV2(Map<String,C> oldValues, Map<String,C> newValues,
                                                         DefaultCustomListFieldForTranslationV2 src) {
    this.oldValues = oldValues;
    this.newValues = newValues;
    this.src = src;
  }

  @Override
  public HasValue<?, Map<String,C>> getHasValue() {
    return src;
  }

  @Override
  public boolean isFromClient() {
    return true;
  }

  @Override
  public Map<String,C> getOldValue() {
    return oldValues;
  }

  @Override
  public Map<String,C> getValue() {
    return newValues;
  }
}