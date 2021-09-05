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

package org.jhapy.frontend.views.admin.eureka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.jhapy.commons.security.oauth2.AuthorizationHeaderUtil;
import org.jhapy.dto.registry.*;
import org.jhapy.dto.registry.Caches.Cache;
import org.jhapy.dto.registry.Endpoints.Endpoint;
import org.jhapy.dto.registry.Thread;
import org.jhapy.dto.registry.HttpRequests.HttpRequest;
import org.jhapy.dto.registry.Thread.StackTrace;
import org.jhapy.frontend.components.Badge;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.ListItem;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Top;
import org.jhapy.frontend.utils.BoxShadowBorders;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.lumo.BadgeColor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.*;

import java.net.URI;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 07/06/2020
 */
@Tag("metricsTabContent")
@CssImport("./styles/views/metrics.css")
public class MetricsTabContent extends ActuatorBaseView {

  public static final String MAX_WIDTH = "1024px";
  protected FlexBoxLayout content;
  protected Component jvmMemoryComponent;
  protected Component garbageComponent;
  protected Component threadsComponent;
  protected Component endpointsComponent;
  protected Component requestsComponent;
  protected Component cachesComponent;
  protected Component databaseComponent;

  public MetricsTabContent(UI ui, String I18N_PREFIX,
      AuthorizationHeaderUtil authorizationHeaderUtil) {
    super(ui, I18N_PREFIX + "metrics.", authorizationHeaderUtil);
  }

  public Component getContent(EurekaInfo eurekaInfo) {
    this.eurekaInfo = eurekaInfo;
    content = new FlexBoxLayout(createHeader(VaadinIcon.SEARCH,
        getTranslation("element." + I18N_PREFIX + "title"),
        getEurekaInstancesList(true, eurekaInfo.getApplicationList(),
            this::getDetails)));
    content.setAlignItems(FlexComponent.Alignment.CENTER);
    content.setFlexDirection(FlexDirection.COLUMN);
    content.setSizeFull();

    if (UI.getCurrent().getSession().getAttribute(EurekaApplication.class) != null &&
        UI.getCurrent().getSession().getAttribute(EurekaApplicationInstance.class) != null) {
      currentEurekaApplication = UI.getCurrent().getSession()
          .getAttribute(EurekaApplication.class);
      currentEurekaApplicationInstance = UI.getCurrent().getSession()
          .getAttribute(EurekaApplicationInstance.class);
      getDetails(currentEurekaApplication, currentEurekaApplicationInstance);
    }

    return content;
  }

  @Override
  public void refresh() {
    var loggerPrefix = getLoggerPrefix("refresh");
    if (currentEurekaApplicationInstance != null && currentEurekaApplication != null) {
      logger().debug(loggerPrefix + "Refresh content");
      getDetails(currentEurekaApplication, currentEurekaApplicationInstance);
    } else {
      logger()
          .warn(loggerPrefix + "No application or application instance set, nothing to do");
    }
  }

  protected Component createJVMMemory(JSONObject jvmMetrics, JSONObject processMetrics,
      JSONObject garbageCollectorMetrics, Thread[] threads) {
    FlexBoxLayout payments = new FlexBoxLayout(
        createHeader(VaadinIcon.CREDIT_CARD,
            getTranslation("element." + I18N_PREFIX + "jvmMetrics")));
    payments.setFlexDirection(FlexDirection.COLUMN);
    //payments.setMargin(Top.L);
    //payments.setMaxWidth(MAX_WIDTH);
    payments.setPadding(Horizontal.RESPONSIVE_L);
    payments.setWidthFull();

    // payments.add(  createG1OldGen( jvmMetrics) );
    payments.add(getFirstRow(jvmMetrics, processMetrics));
    payments.add(getSecondRow(garbageCollectorMetrics, threads));
    return payments;
  }

  protected Component createGarbage(GarbageCollector garbageCollector) {
    FlexBoxLayout payments = new FlexBoxLayout(
        createHeader(VaadinIcon.CREDIT_CARD,
            getTranslation("element." + I18N_PREFIX + "garbageCollectorStatistics")));
    payments.setFlexDirection(FlexDirection.COLUMN);
    payments.setMargin(Top.M);
    //payments.setMaxWidth(MAX_WIDTH);
    payments.setPadding(Horizontal.RESPONSIVE_L);
    payments.setWidthFull();

    DecimalFormat numberFormat = new DecimalFormat("###.###");

    Grid<GarbageCollector.JvmGcPause> grid = new Grid<>();
    grid.setWidthFull();
    grid.setHeight("100px");

    grid.addColumn(gc -> "jvm.gc.pause").setWidth("200px");

    grid.addColumn(garbageCollector1 -> garbageCollector1.getCount().intValue())
        .setTextAlign(ColumnTextAlign.END).setKey("count");
    grid.addColumn(garbageCollector1 -> numberFormat.format(garbageCollector1.getMean()))
        .setTextAlign(ColumnTextAlign.END).setKey("mean");
    grid.addColumn(GarbageCollector.JvmGcPause::getP000).setTextAlign(ColumnTextAlign.END)
        .setKey("min");
    grid.addColumn(GarbageCollector.JvmGcPause::getP050).setTextAlign(ColumnTextAlign.END)
        .setKey("p50");
    grid.addColumn(GarbageCollector.JvmGcPause::getP075).setTextAlign(ColumnTextAlign.END)
        .setKey("p75");
    grid.addColumn(GarbageCollector.JvmGcPause::getP095).setTextAlign(ColumnTextAlign.END)
        .setKey("p95");
    grid.addColumn(GarbageCollector.JvmGcPause::getP099).setTextAlign(ColumnTextAlign.END)
        .setKey("p99");
    grid.addColumn(GarbageCollector.JvmGcPause::getMax).setTextAlign(ColumnTextAlign.END)
        .setKey("max");

    grid.getColumns().forEach(column -> {
      if (column.getKey() != null) {
        column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
        column.setResizable(true);
      }
    });

    grid.setItems(garbageCollector.getJvmGcPause());

    payments.add(grid);
    return payments;
  }

  protected Component createDataSource(Databases databases) {
    FlexBoxLayout payments = new FlexBoxLayout(
        createHeader(VaadinIcon.CREDIT_CARD,
            getTranslation("element." + I18N_PREFIX + "databases")));
    payments.setFlexDirection(FlexDirection.COLUMN);
    payments.setMargin(Top.M);
    //payments.setMaxWidth(MAX_WIDTH);
    payments.setPadding(Horizontal.RESPONSIVE_L);
    payments.setWidthFull();

    DecimalFormat numberFormat2 = new DecimalFormat("###.##");
    DecimalFormat numberFormat3 = new DecimalFormat("###.###");

    Label stats = UIUtils.createH5Label(
        getTranslation("element." + I18N_PREFIX + "connectionPoolUsage") + " : active: "
            + databases
            .getActive().getValue() + ", min: " + databases.getMin().getValue() + ", max: "
            + databases.getMax().getValue() + ", idle: " + databases.getIdle().getValue());
    payments.add(stats);

    Grid<Databases.Stats> grid = new Grid<>();
    grid.setWidthFull();

    grid.addColumn(Databases.Stats::getName).setKey("type");
    grid.addColumn(database -> database.getCount().intValue()).setTextAlign(ColumnTextAlign.END)
        .setKey("count");
    grid.addColumn(database -> numberFormat2.format(database.getMean()))
        .setTextAlign(ColumnTextAlign.END).setKey("mean");
    grid.addColumn(database -> numberFormat3.format(database.getP000()))
        .setTextAlign(ColumnTextAlign.END).setKey("min");
    grid.addColumn(database -> numberFormat3.format(database.getP050()))
        .setTextAlign(ColumnTextAlign.END).setKey("p50");
    grid.addColumn(database -> numberFormat3.format(database.getP075()))
        .setTextAlign(ColumnTextAlign.END).setKey("p75");
    grid.addColumn(database -> numberFormat3.format(database.getP095()))
        .setTextAlign(ColumnTextAlign.END).setKey("p95");
    grid.addColumn(database -> numberFormat3.format(database.getP099()))
        .setTextAlign(ColumnTextAlign.END).setKey("p99");
    grid.addColumn(database -> numberFormat2.format(database.getP100()))
        .setTextAlign(ColumnTextAlign.END).setKey("max");

    grid.getColumns().forEach(column -> {
      if (column.getKey() != null) {
        column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
        column.setResizable(true);
      }
    });

    grid.setItems(databases.getStats());

    payments.add(grid);
    return payments;
  }

  protected Component createEndpoints(Endpoints endpoints) {
    FlexBoxLayout payments = new FlexBoxLayout(
        createHeader(VaadinIcon.CREDIT_CARD,
            getTranslation("element." + I18N_PREFIX + "endpoints")));
    payments.setFlexDirection(FlexDirection.COLUMN);
    payments.setMargin(Top.M);
    //payments.setMaxWidth(MAX_WIDTH);
    payments.setPadding(Horizontal.RESPONSIVE_L);
    payments.setWidthFull();

    DecimalFormat numberFormat = new DecimalFormat("###.###");

    Grid<Endpoints.Endpoint> grid = new Grid<>();
    grid.setWidthFull();

    grid.addColumn(Endpoint::getMethod).setFlexGrow(0).setWidth("150px").setKey("method");
    grid.addColumn(Endpoint::getPath).setFlexGrow(1).setKey("path");
    grid.addColumn(endpoint1 -> endpoint1.getCount().intValue())
        .setTextAlign(ColumnTextAlign.END)
        .setWidth("50px").setKey("count");
    grid.addColumn(endpoint1 -> numberFormat.format(endpoint1.getMean()))
        .setTextAlign(ColumnTextAlign.END).setWidth("50px").setKey("mean");
    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    grid.getColumns().forEach(column -> {
      if (column.getKey() != null) {
        column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
        column.setResizable(true);
      }
    });

    grid.setItems(endpoints.getEndpointList());

    payments.add(grid);
    return payments;
  }

  protected Component createCache(Caches caches) {
    FlexBoxLayout payments = new FlexBoxLayout(
        createHeader(VaadinIcon.CREDIT_CARD,
            getTranslation("element." + I18N_PREFIX + "caches")));
    payments.setFlexDirection(FlexDirection.COLUMN);
    payments.setMargin(Top.M);
    //payments.setMaxWidth(MAX_WIDTH);
    payments.setPadding(Horizontal.RESPONSIVE_L);
    payments.setWidthFull();

    Grid<Caches.Cache> grid = new Grid<>();
    grid.setWidthFull();

    DecimalFormat numberFormat = new DecimalFormat("###.##");

    grid.addColumn(Caches.Cache::getName).setKey("name");

    grid.addColumn(cache -> cache.getGetsHit().intValue()).setTextAlign(ColumnTextAlign.END)
        .setKey("hits");
    grid.addColumn(cache -> cache.getGetsMissed().intValue()).setTextAlign(ColumnTextAlign.END)
        .setKey("misses");
    grid.addColumn(cache -> cache.getsHit + cache.getGetsMissed())
        .setTextAlign(ColumnTextAlign.END)
        .setKey("gets");
    grid.addColumn(cache -> cache.getPuts().intValue()).setTextAlign(ColumnTextAlign.END)
        .setKey("puts");
    grid.addColumn(cache -> cache.getRemoval().intValue()).setTextAlign(ColumnTextAlign.END)
        .setKey("removals");
    grid.addColumn(cache -> cache.getEvictions().intValue()).setTextAlign(ColumnTextAlign.END)
        .setKey("evictions");
    grid.addColumn(cache ->
        numberFormat
            .format((100 * cache.getGetsHit()) / (cache.getsHit + cache.getGetsMissed()))
            + " %").setTextAlign(ColumnTextAlign.END).setKey("hitPercent");
    grid.addColumn(cache ->
        numberFormat
            .format((100 * cache.getGetsMissed()) / (cache.getsHit + cache.getGetsMissed()))
            + " %").setTextAlign(ColumnTextAlign.END).setKey("missPercent");

    grid.getColumns().forEach(column -> {
      if (column.getKey() != null) {
        column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
        column.setResizable(true);
      }
    });

    grid.setItems(caches.getCaches());

    payments.add(grid);
    return payments;
  }

  protected Component createRequests(HttpRequests httpRequests) {
    FlexBoxLayout payments = new FlexBoxLayout(
        createHeader(VaadinIcon.CREDIT_CARD,
            getTranslation("element." + I18N_PREFIX + "httpRequests")));
    payments.setFlexDirection(FlexDirection.COLUMN);
    payments.setMargin(Top.M);
    //payments.setMaxWidth(MAX_WIDTH);
    payments.setPadding(Horizontal.RESPONSIVE_L);
    payments.setWidthFull();

    DecimalFormat decimalFormat = new DecimalFormat("###.###");
    DecimalFormat numberFormat = new DecimalFormat("###");
    Grid<HttpRequests.HttpRequest> grid = new Grid<>();
    grid.setWidthFull();

    grid.addColumn(HttpRequest::getCode).setFlexGrow(0).setWidth("150px").setKey("code");
    grid.addColumn(httpRequest -> httpRequest.getCount().intValue() + " (" + numberFormat
        .format((httpRequest.getCount() / httpRequests.getAll()) * 100) + "%)").setWidth("50px")
        .setKey("count");
    grid.addColumn(httpRequest -> decimalFormat.format(httpRequest.getMean()))
        .setTextAlign(ColumnTextAlign.END).setWidth("50px").setKey("mean");
    grid.addColumn(httpRequest -> decimalFormat.format(httpRequest.getMax()))
        .setTextAlign(ColumnTextAlign.END).setWidth("50px").setKey("max");
    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    grid.getColumns().forEach(column -> {
      if (column.getKey() != null) {
        column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
        column.setResizable(true);
      }
    });

    grid.setItems(httpRequests.getRequests());

    payments.add(grid);
    return payments;
  }

  protected Component createThreads(Thread[] threads) {
    FlexBoxLayout payments = new FlexBoxLayout(
        createHeader(VaadinIcon.CREDIT_CARD,
            getTranslation("element." + I18N_PREFIX + "threads")));
    payments.setFlexDirection(FlexDirection.COLUMN);
    payments.setMargin(Top.M);
    //payments.setMaxWidth(MAX_WIDTH);
    payments.setPadding(Horizontal.RESPONSIVE_L);
    payments.setWidthFull();

    Grid<Thread> grid = new Grid<>();
    grid.setWidthFull();

    ComponentRenderer<Badge, Thread> badgeRenderer = new ComponentRenderer<>(
        thread -> {
          return switch (thread.getThreadState()) {
            case "RUNNABLE" -> new Badge("Runnable", BadgeColor.SUCCESS);
            case "WAITING" -> new Badge("Waiting", BadgeColor.NORMAL);
            case "TIMED_WAITING" -> new Badge("Timed Waiting", BadgeColor.ERROR);
            case "BLOCKED" -> new Badge("Blocked", BadgeColor.ERROR_PRIMARY);
            default -> new Badge(thread.getThreadState(), BadgeColor.SUCCESS_PRIMARY);
          };
        }
    );
    grid.addColumn(badgeRenderer).setFlexGrow(0).setWidth("150px").setKey("threadState")
        .setComparator(
            Comparator.comparing(Thread::getThreadState));

    grid.addColumn(Thread::getThreadName).setWidth("200px").setKey("threadName");

    grid.addColumn(Thread::getBlockedTime).setTextAlign(ColumnTextAlign.END).setWidth("100px")
        .setKey("blockedTime");
    grid.addColumn(Thread::getBlockedCount).setTextAlign(ColumnTextAlign.END).setWidth("100px")
        .setKey("blockedCount");
    grid.addColumn(Thread::getWaitedTime).setTextAlign(ColumnTextAlign.END).setWidth("100px")
        .setKey("waitedTime");
    grid.addColumn(Thread::getWaitedCount).setTextAlign(ColumnTextAlign.END).setWidth("100px")
        .setKey("waitedCount");
    grid.addComponentColumn(thread -> {
      if (thread.getStackTrace() != null && thread.getStackTrace().size() > 0) {
        Button displayStackTrace = UIUtils
            .createButton("Stacktrace", ButtonVariant.LUMO_SMALL);
        displayStackTrace.addClickListener(buttonClickEvent -> grid
            .setDetailsVisible(thread, !grid.isDetailsVisible(thread)));
        return displayStackTrace;
      } else {
        return new Label("");
      }
    }).setTextAlign(ColumnTextAlign.CENTER).setWidth("150px");
    grid.addColumn(Thread::getLockName).setAutoWidth(true).setKey("lockName");
    grid.setItemDetailsRenderer(TemplateRenderer.<Thread>of(
        "<div inner-h-t-m-l='[[item.orderItems]]'></div>")
        .withProperty("orderItems", thread -> {
          Set<StackTrace> stackTraceSet = thread.getStackTrace();
          StringBuilder content = new StringBuilder();
          stackTraceSet.forEach(stackTrace -> content.append(stackTrace.getClassName()).append("(")
              .append("<span style=\"color:#e83e8c;\">").append(stackTrace.getFileName())
              .append(":").append(stackTrace.getLineNumber()).append("</span>)<br/>"));
          return content.toString();
        })
        // This is now how we open the details
        .withEventHandler("handleClick", person -> grid.getDataProvider().refreshItem(person)));
    grid.setDetailsVisibleOnClick(false);
    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    grid.getColumns().forEach(column -> {
      if (column.getKey() != null) {
        column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
        column.setResizable(true);
        column.setSortable(true);
      }
    });

    grid.setItems(threads);

    payments.add(grid);
    return payments;
  }

  protected Component getFirstRow(JSONObject jvmMetrics, JSONObject processMetrics) {
    Row docs = new Row(getMemory(jvmMetrics), getSystem(processMetrics));
    //docs.addClassName(LumoStyles.Margin.Top.XL);
    // UIUtils.setMaxWidth(MAX_WIDTH, docs);
    docs.setWidthFull();

    return docs;
  }

  protected Component getSecondRow(JSONObject garbageCollector, Thread[] threads) {
    Row docs = new Row(getThreads(threads), getGarbageCollector(garbageCollector));
    //docs.addClassName(LumoStyles.Margin.Top.XL);
    // UIUtils.setMaxWidth(MAX_WIDTH, docs);
    docs.setWidthFull();

    return docs;
  }

  protected Component getMemory(JSONObject jvmMetrics) {
    FlexBoxLayout content = new FlexBoxLayout();
    content.setFlexDirection(FlexDirection.COLUMN);
    //content.setMargin(Horizontal.AUTO, Vertical.RESPONSIVE_L);
    content.setWidthFull();

    Label header = UIUtils.createH3Label(getTranslation("element." + I18N_PREFIX + "memory"));
    header
        .addClassNames(LumoStyles.Margin.Vertical.L, LumoStyles.Margin.Responsive.Horizontal.L);
    content.add(header);

    Div items = new Div();
    items.addClassNames(BoxShadowBorders.BOTTOM, LumoStyles.Padding.Bottom.L);
    int idx = 0;
    for (Object key : jvmMetrics.keySet()) {
      JSONObject jsonObject = (JSONObject) jvmMetrics.get(key);
      ListItem item;
      if ((Double) jsonObject.get("max") == -1) {
        item = new ListItem(
            key.toString(),
            "Committed : " + getValue((Double) jsonObject.get("committed")),
            UIUtils.createH5Label(getValue((Double) jsonObject.get("used")))
        );
      } else {
        item = new ListItem(
            key.toString(),
            "Committed : " + getValue((Double) jsonObject.get("committed")) + " / Max : "
                + getValue(
                (Double) jsonObject.get("max")),
            UIUtils.createH5Label(getValue((Double) jsonObject.get("used")))
        );
      }
      item.setDividerVisible(++idx < jvmMetrics.size());
      items.add(item);
    }

    content.add(items);
    return content;
  }

  protected Component getThreads(Thread[] threads) {
    FlexBoxLayout content = new FlexBoxLayout();
    content.setFlexDirection(FlexDirection.COLUMN);
    //  content.setMargin(Horizontal.AUTO, Vertical.RESPONSIVE_L);
    content.setWidthFull();

    double runnable = 0;
    double timedWaiting = 0;
    double waiting = 0;
    double blocked = 0;
    double count = threads.length;

    for (Thread thread : threads) {
      switch (thread.getThreadState()) {
        case "RUNNABLE" -> runnable++;
        case "WAITING" -> waiting++;
        case "TIMED_WAITING" -> timedWaiting++;
        case "BLOCKED" -> blocked++;
        default -> logger().error("unknown : " + thread);
      }
    }
    DecimalFormat numberFormat = new DecimalFormat("###");

    Label header = UIUtils.createH3Label(getTranslation("element." + I18N_PREFIX + "threads"));
    header
        .addClassNames(LumoStyles.Margin.Vertical.L, LumoStyles.Margin.Responsive.Horizontal.L);
    content.add(header);

    Div items = new Div();
    items.addClassNames(BoxShadowBorders.BOTTOM, LumoStyles.Padding.Bottom.L);

    {
      ListItem item = new ListItem(
          "Runnable",
          UIUtils.createH5Label(
              numberFormat.format((runnable / count) * 100) + "% (" + (int) runnable + ")")
      );

      item.setDividerVisible(true);
      items.add(item);
    }
    {
      ListItem item = new ListItem(
          "Timed Waiting",
          UIUtils.createH5Label(
              numberFormat.format((timedWaiting / count) * 100) + "% (" + (int) timedWaiting
                  + ")")
      );

      item.setDividerVisible(true);
      items.add(item);
    }
    {
      ListItem item = new ListItem(
          "Waiting",
          UIUtils.createH5Label(
              numberFormat.format((waiting / count) * 100) + "% (" + (int) waiting + ")")
      );

      item.setDividerVisible(true);
      items.add(item);
    }
    {
      ListItem item = new ListItem(
          "Blocked",
          UIUtils.createH5Label(
              numberFormat.format((blocked / count) * 100) + "% (" + (int) blocked + ")")
      );

      item.setDividerVisible(false);
      items.add(item);
    }

    content.add(items);
    return content;
  }

  protected Component getSystem(JSONObject jvmMetrics) {
    FlexBoxLayout content = new FlexBoxLayout();
    content.setFlexDirection(FlexDirection.COLUMN);
    //  content.setMargin(Horizontal.AUTO, Vertical.RESPONSIVE_L);
    content.setWidthFull();

    Label header = UIUtils.createH3Label(getTranslation("element." + I18N_PREFIX + "system"));
    header
        .addClassNames(LumoStyles.Margin.Vertical.L, LumoStyles.Margin.Responsive.Horizontal.L);
    content.add(header);

    Div items = new Div();
    items.addClassNames(BoxShadowBorders.BOTTOM, LumoStyles.Padding.Bottom.L);
    Double upTime = (Double) jvmMetrics.get("process.uptime");
    Double startTime = (Double) jvmMetrics.get("process.start.time");
    Double processCpuUsage = (Double) jvmMetrics.get("process.cpu.usage");
    Double systemCpuUsage = (Double) jvmMetrics.get("system.cpu.usage");
    Double processCpuCount = (Double) jvmMetrics.get("system.cpu.count");
    Double loadAverage1m = (Double) jvmMetrics.get("system.load.average.1m");
    Double processFilesMax = (Double) jvmMetrics.get("process.files.max");
    Double processFilesOpen = (Double) jvmMetrics.get("process.files.open");

    DecimalFormat numberFormat = new DecimalFormat("###.##");
    {
      ListItem item = new ListItem(
          "process.uptime",
          DurationFormatUtils.formatDurationWords(upTime.longValue(), false, false)
      );

      item.setDividerVisible(true);
      items.add(item);
    }
    {
      ListItem item = new ListItem(
          "process.start.time",
          FastDateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG)
              .format(startTime.longValue())
      );

      item.setDividerVisible(true);
      items.add(item);
    }
    {
      ListItem item = new ListItem(
          "process.cpu.usage",
          UIUtils.createH5Label(numberFormat.format(processCpuUsage * 100) + "%")
      );

      item.setDividerVisible(true);
      items.add(item);
    }
    {
      ListItem item = new ListItem(
          "system.cpu.usage",
          UIUtils.createH5Label(numberFormat.format(systemCpuUsage * 100) + "%")
      );

      item.setDividerVisible(true);
      items.add(item);
    }
    {
      ListItem item = new ListItem(
          "system.cpu.count",
          UIUtils.createH5Label(numberFormat.format(processCpuCount))
      );

      item.setDividerVisible(true);
      items.add(item);
    }
    {
      ListItem item = new ListItem(
          "system.load.average.1m",
          UIUtils.createH5Label(numberFormat.format(loadAverage1m))
      );

      item.setDividerVisible(true);
      items.add(item);
    }
    {
      ListItem item = new ListItem(
          "process.files.max",
          UIUtils.createH5Label(numberFormat.format(processFilesMax))
      );

      item.setDividerVisible(true);
      items.add(item);
    }
    {
      ListItem item = new ListItem(
          "process.files.open",
          UIUtils.createH5Label(numberFormat.format(processFilesOpen))
      );

      item.setDividerVisible(false);
      items.add(item);
    }

    content.add(items);
    return content;
  }

  protected Component getGarbageCollector(JSONObject jvmMetrics) {
    FlexBoxLayout content = new FlexBoxLayout();
    content.setFlexDirection(FlexDirection.COLUMN);
    //   content.setMargin(Horizontal.AUTO, Vertical.RESPONSIVE_L);
    content.setWidthFull();

    Label header = UIUtils
        .createH3Label(getTranslation("element." + I18N_PREFIX + "garbageCollector"));
    header
        .addClassNames(LumoStyles.Margin.Vertical.L, LumoStyles.Margin.Responsive.Horizontal.L);
    content.add(header);

    Div items = new Div();
    items.addClassNames(BoxShadowBorders.BOTTOM, LumoStyles.Padding.Bottom.L);

    {
      Double gcLiveDateSize = (Double) jvmMetrics.get("jvm.gc.live.data.size");
      Double gcMaxDateSize = (Double) jvmMetrics.get("jvm.gc.max.data.size");
      ListItem item = new ListItem(
          "GC Live Data Size/GC Max Data Size",
          UIUtils.createH5Label(getValue(gcLiveDateSize) + " / " + getValue(gcMaxDateSize))
      );
      item.setDividerVisible(true);
      items.add(item);
    }
    {
      Double gcPromoted = (Double) jvmMetrics.get("jvm.gc.memory.promoted");
      Double gcAllocated = (Double) jvmMetrics.get("jvm.gc.memory.allocated");
      ListItem item = new ListItem(
          "GC Memory Promoted/GC Memory Allocated",
          UIUtils.createH5Label(getValue(gcPromoted) + " / " + getValue(gcAllocated))
      );
      item.setDividerVisible(true);
      items.add(item);
    }
    {
      Double classesLoaded = (Double) jvmMetrics.get("classesLoaded");
      ListItem item = new ListItem(
          "Class loaded",
          UIUtils.createH5Label(Integer.toString(classesLoaded.intValue()))
      );
      item.setDividerVisible(true);
      items.add(item);
    }
    {
      Double classesUnloaded = (Double) jvmMetrics.get("classesUnloaded");
      ListItem item = new ListItem(
          "Class unloaded",
          UIUtils.createH5Label(Integer.toString(classesUnloaded.intValue()))
      );
      item.setDividerVisible(false);
      items.add(item);
    }

    content.add(items);
    return content;
  }

  public static int getDigitGroup(double value) {
    if (value <= 0) {
      return -1;
    }
    return (int) (Math.log10(value) / Math.log10(1024));
  }

  public static String getUnit(double value) {
    if (value <= 0) {
      return "N/A";
    }
    final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
    int digitGroups = getDigitGroup(value);
    return units[digitGroups];
  }

  public static double getValue(double value, int digitGroups) {
    if (value <= 0) {
      return 0d;
    }
    return Double
        .parseDouble(new DecimalFormat("#,##0.##").format(value / Math.pow(1024, digitGroups)));
  }

  public static String getValue(double value) {
    if (value <= 0) {
      return "0";
    }
    final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
    int digitGroups = getDigitGroup(value);
    return new DecimalFormat("#,##0.##").format(value / Math.pow(1024, digitGroups)) + " "
        + units[digitGroups];
  }

  protected void getDetails(EurekaApplication eurekaApplication,
      EurekaApplicationInstance eurekaApplicationInstance) {
    var loggerPrefix = getLoggerPrefix("getDetails",
        eurekaApplicationInstance.getInstanceId());

    titleLabel.setText(
        getTranslation("element." + I18N_PREFIX + "title") + " - " + eurekaApplicationInstance
            .getInstanceId());

    try {
      final HttpHeaders httpHeaders;
      Optional<String> authorization = authorizationHeaderUtil
          .getAuthorizationHeader(authentication);
      if (authorization.isPresent()) {
        httpHeaders = new HttpHeaders() {{
          set("Authorization", authorization.get());
          setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        }};
      } else {
        logger().warn(loggerPrefix + "Cannot get Authorization headers... skip");
        return;
      }
      logger().debug(
          "Application : " + eurekaApplication.getName() + ", Metrics Url = "
              + eurekaApplicationInstance.getMetadata().get("management.url")
              + "/jhametrics");
      ResponseEntity<String> jhametricsResponseEntity = restTemplate.exchange(URI.create(
          eurekaApplicationInstance.getMetadata().get("management.url") + "/jhametrics"),
          HttpMethod.GET,
          new HttpEntity<>(httpHeaders), String.class);
      String jhametricsBody = jhametricsResponseEntity.getBody();

      JSONParser jsonParser = new JSONParser();
      JSONObject jhametricsObject = (JSONObject) jsonParser.parse(jhametricsBody);

      ResponseEntity<String> threadDumpResponseEntity = restTemplate.exchange(URI.create(
          eurekaApplicationInstance.getMetadata().get("management.url") + "/threaddump"),
          HttpMethod.GET,
          new HttpEntity<>(httpHeaders), String.class);
      String threaddumpBody = threadDumpResponseEntity.getBody();

      JSONObject threaddumpObject = (JSONObject) jsonParser.parse(threaddumpBody);

      ObjectMapper mapper = new ObjectMapper();
      Thread[] threads = mapper
          .readValue(threaddumpObject.get("threads").toString(), Thread[].class);

      ResponseEntity<String> metricsResponseEntity = restTemplate.exchange(URI.create(
          eurekaApplicationInstance.getMetadata().get("management.url") + "/metrics"),
          HttpMethod.GET,
          new HttpEntity<>(httpHeaders), String.class);
      String metricsBody = metricsResponseEntity.getBody();

//      logger().debug("Thread = " + threads);

      logger().debug("Metrics = " + jhametricsBody);

      //    logger().debug("ThreadDump = " + threaddumpObject);

      GarbageCollector garbageCollector = mapper
          .readValue(jhametricsObject.get("garbageCollector").toString(),
              GarbageCollector.class);

      JSONObject jvmMetrics = (JSONObject) jhametricsObject.get("jvm");
      JSONObject processMetrics = (JSONObject) jhametricsObject.get("processMetrics");
      JSONObject garbageMetrics = (JSONObject) jhametricsObject.get("garbageCollector");
      JSONObject endpointMetrics = (JSONObject) jhametricsObject.get("services");
      JSONObject cachesMetrics = (JSONObject) jhametricsObject.get("cache");
      JSONObject databasesMetrics = (JSONObject) jhametricsObject.get("databases");
      JSONObject httpRequestMetrics = (JSONObject) jhametricsObject
          .get("http.server.requests");

      Databases databases = null;
      if (databasesMetrics.size() > 0) {
        databases = mapper.readValue(databasesMetrics.toString(), Databases.class);
      }

      final Caches caches;
      if (cachesMetrics.size() > 0) {
        caches = new Caches();
        caches.setCaches(new ArrayList<>());
        cachesMetrics.keySet().forEach(o -> {
          JSONObject data = (JSONObject) cachesMetrics.get(o);
          Caches.Cache cache = new Cache();
          cache.setName(o.toString());
          cache.setMemoryEntry((Double) data.getOrDefault("cache.entry.memory", 0d));
          cache.setSize((Double) data.getOrDefault("cache.size", 0d));
          cache.setPuts((Double) data.getOrDefault("cache.puts", 0d));
          cache.setGetsHit((Double) data.getOrDefault("cache.entry.gets.hit", 0d));
          cache.setPartitionGets((Double) data.getOrDefault("cache.partition.gets", 0d));
          cache.setEntries((Double) data.getOrDefault("cache.entries", 0d));
          cache.setEvictions((Double) data.getOrDefault("cache.evictions", 0d));
          cache.setGetsMissed((Double) data.getOrDefault("cache.gets.miss", 0d));
          cache.setRemoval((Double) data.getOrDefault("cache.removals", 0d));
          caches.getCaches().add(cache);
        });
      } else {
        caches = null;
      }

      final Endpoints endpoints;
      if (endpointMetrics.size() > 0) {
        endpoints = new Endpoints();
        endpoints.setEndpointList(new ArrayList<>());
        endpointMetrics.keySet().forEach(o -> {
          JSONObject methods = (JSONObject) endpointMetrics.get(o);
          methods.keySet().forEach(o1 -> {
            JSONObject method = (JSONObject) methods.get(o1);
            Endpoint endpoint = new Endpoint();
            endpoint.setPath(o.toString());
            endpoint.setMethod(o1.toString());
            endpoint.setMax((Double) method.get("max"));
            endpoint.setMean((Double) method.get("mean"));
            endpoint.setCount((Long) method.get("count"));

            endpoints.getEndpointList().add(endpoint);
          });
        });
      } else {
        endpoints = null;
      }

      HttpRequests httpRequests = new HttpRequests();
      httpRequests.setRequests(new ArrayList<>());
      httpRequests.setAll((Long) ((JSONObject) httpRequestMetrics.get("all")).get("count"));
      JSONObject perCode = (JSONObject) httpRequestMetrics.get("percode");
      perCode.keySet().forEach(o -> {
        JSONObject code = (JSONObject) perCode.get(o);
        HttpRequests.HttpRequest httpRequest = new HttpRequest();
        httpRequest.setCode(Integer.parseInt(o.toString()));
        httpRequest.setMax((Double) code.get("max"));
        httpRequest.setMean((Double) code.get("mean"));
        httpRequest.setCount((Long) code.get("count"));
        httpRequests.getRequests().add(httpRequest);

      });

      if (content.getChildren().count() > 1) {
        logger().debug(loggerPrefix + "Remove previous content");
        if (jvmMemoryComponent != null) {
          content.remove(jvmMemoryComponent);
        }
        if (garbageComponent != null) {
          content.remove(garbageComponent);
        }
        if (threadsComponent != null) {
          content.remove(threadsComponent);
        }
        if (endpointsComponent != null) {
          content.remove(endpointsComponent);
        }
        if (requestsComponent != null) {
          content.remove(requestsComponent);
        }
        if (cachesComponent != null) {
          content.remove(cachesComponent);
        }
        if (databaseComponent != null) {
          content.remove(databaseComponent);
        }
      }
      jvmMemoryComponent = createJVMMemory(jvmMetrics, processMetrics, garbageMetrics,
          threads);
      content.add(jvmMemoryComponent);

      garbageComponent = createGarbage(garbageCollector);
      content.add(garbageComponent);
      content.setFlex("1", garbageComponent);

      threadsComponent = createThreads(threads);
      content.add(threadsComponent);
      content.setFlex("1", threadsComponent);

      if (endpoints != null) {
        endpointsComponent = createEndpoints(endpoints);
        content.add(endpointsComponent);
        content.setFlex("1", endpointsComponent);
      }

      requestsComponent = createRequests(httpRequests);
      content.add(requestsComponent);
      content.setFlex("1", requestsComponent);

      if (caches != null) {
        cachesComponent = createCache(caches);
        content.add(cachesComponent);
        content.setFlex("1", cachesComponent);
      }

      if (databases != null) {
        databaseComponent = createDataSource(databases);
        content.add(databaseComponent);
        content.setFlex("1", databaseComponent);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}