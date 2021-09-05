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

package org.jhapy.frontend.views.admin.configServer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.security.oauth2.AuthorizationHeaderUtil;
import org.jhapy.dto.registry.EurekaApplication;
import org.jhapy.dto.registry.EurekaApplicationInstance;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.utils.UIUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.vaadin.tabs.PagedTabs;

import java.net.URI;
import java.util.Collections;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 07/06/2020
 */
@Tag("apiTabContent")
public class EncryptionTabContent extends CloudConfigBaseView {

  protected FlexBoxLayout content;
  protected Component component;

  public EncryptionTabContent(
      Environment env, UI ui, String I18N_PREFIX, AuthorizationHeaderUtil authorizationHeaderUtil) {
    super(env, ui, I18N_PREFIX + "encryption.", authorizationHeaderUtil);
  }

  public Component getContent() {
    content =
        new FlexBoxLayout(
            createHeader(VaadinIcon.SEARCH, getTranslation("element." + I18N_PREFIX + "title")));
    content.setAlignItems(FlexComponent.Alignment.CENTER);
    content.setFlexDirection(FlexDirection.COLUMN);
    content.setSizeFull();

    PagedTabs tabs = new PagedTabs(content);
    content.add(tabs);

    tabs.add(getTranslation("element." + I18N_PREFIX + "tab.encrypt"), getEncrypt(), false);
    tabs.add(getTranslation("element." + I18N_PREFIX + "tab.decrypt"), getDecrypt(), false);

    return content;
  }

  protected Component getEncrypt() {
    FlexBoxLayout layout = new FlexBoxLayout();
    layout.setAlignItems(FlexComponent.Alignment.CENTER);
    layout.setFlexDirection(FlexDirection.COLUMN);
    layout.setSizeFull();

    TextArea inputTextArea = new TextArea();
    inputTextArea.setSizeFull();

    TextArea outputTextArea = new TextArea();
    outputTextArea.setReadOnly(true);
    outputTextArea.setSizeFull();

    Button encryptButton =
        UIUtils.createPrimaryButton(getTranslation("action." + I18N_PREFIX + "encrypt"));
    encryptButton.addClickListener(
        event -> {
          if (StringUtils.isNotBlank(inputTextArea.getValue())) {
            final HttpHeaders httpHeaders =
                new HttpHeaders() {
                  {
                    set("Authorization", authorizationHeaderUtil.getAuthorizationHeader().get());
                    setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));
                  }
                };

            String url = env.getProperty("spring.cloud.config.uri") + "/encrypt";
            logger().debug("Encrypt Url = " + url);
            ResponseEntity<String> configprops =
                restTemplate.exchange(
                    URI.create(url),
                    HttpMethod.POST,
                    new HttpEntity<>(inputTextArea.getValue(), httpHeaders),
                    String.class);
            String configpropsBody = configprops.getBody();
            logger().debug("Config YAML = " + configpropsBody);
            outputTextArea.setValue("{cipher}" + configpropsBody);
          }
        });

    layout.add(inputTextArea, encryptButton, outputTextArea);

    return layout;
  }

  protected Component getDecrypt() {
    FlexBoxLayout layout = new FlexBoxLayout();
    layout.setAlignItems(FlexComponent.Alignment.CENTER);
    layout.setFlexDirection(FlexDirection.COLUMN);
    layout.setSizeFull();

    TextArea inputTextArea = new TextArea();
    inputTextArea.setSizeFull();

    TextArea outputTextArea = new TextArea();
    outputTextArea.setReadOnly(true);
    outputTextArea.setSizeFull();

    Button encryptButton =
        UIUtils.createPrimaryButton(getTranslation("action." + I18N_PREFIX + "decrypt"));
    encryptButton.addClickListener(
        event -> {
          if (StringUtils.isNotBlank(inputTextArea.getValue())) {
            final HttpHeaders httpHeaders =
                new HttpHeaders() {
                  {
                    set("Authorization", authorizationHeaderUtil.getAuthorizationHeader().get());
                    setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));
                  }
                };

            String url = env.getProperty("spring.cloud.config.uri") + "/decrypt";
            logger().debug("Decrypt Url = " + url);
            ResponseEntity<String> configprops =
                restTemplate.exchange(
                    URI.create(url),
                    HttpMethod.POST,
                    new HttpEntity<>(inputTextArea.getValue().replace("{cipher}", ""), httpHeaders),
                    String.class);
            String configpropsBody = configprops.getBody();
            logger().debug("Config YAML = " + configpropsBody);
            outputTextArea.setValue(configpropsBody);
          }
        });

    layout.add(inputTextArea, encryptButton, outputTextArea);

    return layout;
  }

  protected void getDetails(
      EurekaApplication eurekaApplication, EurekaApplicationInstance eurekaApplicationInstance) {}
}