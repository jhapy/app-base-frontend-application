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
public final class CrudErrorMessage {

  public static final String ENTITY_NOT_FOUND = "The selected entity was not found.";

  public static final String CONCURRENT_UPDATE =
      "Somebody else might have updated the data. Please refresh and try again.";

  public static final String OPERATION_PREVENTED_BY_REFERENCES =
      "The operation can not be executed as there are references to entity in the database.";

  public static final String REQUIRED_FIELDS_MISSING =
      "Please fill out all required fields before proceeding.";

  private CrudErrorMessage() {}
}
