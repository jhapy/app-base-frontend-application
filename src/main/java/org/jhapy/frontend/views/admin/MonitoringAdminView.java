package org.jhapy.frontend.views.admin;

import com.hazelcast.core.HazelcastInstance;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.router.RouterLayout;
import java.time.LocalDateTime;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.ListItem;
import org.jhapy.frontend.layout.ViewFrame;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Top;
import org.jhapy.frontend.layout.size.Vertical;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.FontSize;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.BoxSizing;
import org.jhapy.frontend.utils.i18n.DateTimeFormatter;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.views.JHapyMainView3;
import org.springframework.security.access.annotation.Secured;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 17/09/2020
 */
@I18NPageTitle(messageKey = AppConst.TITLE_ACTUAL_SESSIONS_ADMIN)
@Secured(SecurityConst.ROLE_ADMIN)
public class MonitoringAdminView extends ViewFrame implements RouterLayout, HasLogger {

  private final HazelcastInstance hazelcastInstance;

  public MonitoringAdminView(HazelcastInstance hazelcastInstance) {
    this.hazelcastInstance = hazelcastInstance;
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    JHapyMainView3.get().getAppBar().setTitle(getTranslation("element.dashboard.title"));
    setViewContent(createContent());
  }

  private Component createContent() {
    FlexBoxLayout content = new FlexBoxLayout();
    content.setFlexDirection(FlexDirection.COLUMN);
    content.setMargin(Horizontal.AUTO, Vertical.RESPONSIVE_L);
    content.setWidthFull();

    content.add(UIUtils.createLabel(FontSize.L, "Current sessions :"));

    ConcurrentMap<String, LocalDateTime> userSession = retrieveMap();
    userSession.forEach((userEmail, localDateTime) -> {
      ListItem item = new ListItem(userEmail, DateTimeFormatter.format(localDateTime, getLocale()));
      content.add(item);
    });

    return content;
  }

  private ConcurrentMap<String, LocalDateTime> retrieveMap() {
    return hazelcastInstance.getMap("userSessions");
  }
}
