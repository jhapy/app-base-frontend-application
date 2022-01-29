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

package org.jhapy.frontend.utils;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.binder.PropertySet;
import com.vaadin.flow.data.provider.Query;
import org.apache.commons.text.WordUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.BaseEntity;
import org.jhapy.dto.domain.resource.StoredFileDTO;
import org.jhapy.frontend.dataproviders.DefaultDataProvider;
import org.jhapy.frontend.dataproviders.DefaultFilter;
import org.jhapy.frontend.views.JHapyMainView3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 09/04/2020
 */
public class ExcelExporter<T extends BaseEntity, F extends DefaultFilter> implements HasLogger {

  private static final String TMP_FILE_NAME = "tmp";
  private final DefaultDataProvider<T, F> dataProvider;
  private final Grid<T> grid;
  private final Class<T> entityType;
  private File file;
  private PropertySet<T> propertySet;

  public ExcelExporter(Grid<T> grid, DefaultDataProvider<T, F> dataProvider, Class<T> entityType) {
    this(grid, dataProvider, entityType, null);
  }

  public ExcelExporter(
      Grid<T> grid,
      DefaultDataProvider<T, F> dataProvider,
      Class<T> entityType,
      List<String> excludedColumns) {
    this.dataProvider = dataProvider;
    this.entityType = entityType;
    this.grid = grid;
  }

  public InputStream build() {
    try {
      initTempFile();
      resetContent();
      buildFileContent();
      writeToFile();
      return new FileInputStream(file);
    } catch (Exception e) {
      throw new ExcelExporterException("An error happened during exporting your Grid", e);
    }
  }

  private void initTempFile() throws IOException {
    if (file == null || file.delete()) {
      file = createTempFile();
    }
  }

  private File createTempFile() throws IOException {
    return File.createTempFile(TMP_FILE_NAME, getFileExtension());
  }

  protected void resetContent() {}

  private void buildFileContent() {
    buildHeaderRow();
    buildRows();
    buildFooter();
  }

  private void buildHeaderRow() {
    onNewRow();

    Method[] methods = entityType.getDeclaredMethods();
    String className = WordUtils.uncapitalize(entityType.getSimpleName());
    for (Method method : methods) {
      if (method.getName().startsWith("get")
          && !Collection.class.isAssignableFrom(method.getReturnType())
          && !method.getReturnType().isArray()
          && !method.getReturnType().equals(StoredFileDTO.class)
          && !BaseEntity.class.isAssignableFrom(method.getReturnType())) {
        onNewCell();
        String attrName = WordUtils.uncapitalize(method.getName().substring(3));
        buildColumnHeaderCell("element." + className + "." + attrName);
      }
    }
  }

  void buildColumnHeaderCell(String header) {
    var loggerPrefix = getLoggerPrefix("buildColumnHeaderCell");

    logger()
        .debug(
            loggerPrefix
                + "Build header for column name "
                + header
                + " : "
                + JHapyMainView3.get().getTranslation(header));
  }

  private void buildRows() {
    var loggerPrefix = getLoggerPrefix("buildRows");
    Object filter = null;

    Query<T, F> streamQuery =
        new Query(
            0,
            dataProvider.size(dataProvider.getCurrentQuery()),
            grid.getDataCommunicator().getBackEndSorting(),
            grid.getDataCommunicator().getInMemorySorting(),
            dataProvider.getCurrentQuery().getFilter());
    Stream<T> dataStream = getDataStream(streamQuery);

    dataStream.forEach(this::buildRow);
  }

  void buildFooter() {}

  void writeToFile() {}

  private void buildRow(T item) {
    onNewRow();
    Map<String, Object> values = new HashMap();
    Method[] methods = item.getClass().getMethods();
    for (Method method : methods) {
      try {
        if (method.getName().startsWith("get")
            && !Collection.class.isAssignableFrom(method.getReturnType())
            && !method.getReturnType().isArray()
            && !method.getReturnType().equals(StoredFileDTO.class)) {
          String attrName = WordUtils.uncapitalize(method.getName().substring(3));
          values.put(attrName, method.invoke(item));
        }
      } catch (IllegalAccessException | InvocationTargetException e) {
        e.printStackTrace();
      }
    }

    values.forEach(
        (s, o) -> {
          onNewCell();
          buildCell(o);
        });
  }

  String getFileExtension() {
    return ".xlsx";
  }

  void buildCell(Object value) {
    var loggerPrefix = getLoggerPrefix("buildCell");

    logger().debug(loggerPrefix + "Build cell with value " + value);
  }

  void onNewRow() {
    var loggerPrefix = getLoggerPrefix("onNewRow");

    logger().debug(loggerPrefix + "New Row");
  }

  void onNewCell() {
    var loggerPrefix = getLoggerPrefix("onNewCell");

    logger().debug(loggerPrefix + "New Cell");
  }

  private Stream<T> getDataStream(Query newQuery) {
    var loggerPrefix = getLoggerPrefix("getDataStream");
    Stream<T> stream = grid.getDataProvider().fetch(newQuery);
    if (stream.isParallel()) {
      logger()
          .debug(
              "Data provider {} has returned " + "parallel stream on 'fetch' call",
              grid.getDataProvider().getClass());
      stream = stream.collect(Collectors.toList()).stream();
      assert !stream.isParallel();
    }
    return stream;
  }
}
