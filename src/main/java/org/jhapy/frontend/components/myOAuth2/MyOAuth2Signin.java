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

package org.jhapy.frontend.components.myOAuth2;

import com.github.scribejava.core.model.Response;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.templatemodel.TemplateModel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

@Tag("sign-in-2")
@JsModule("./myOAuth2/signin.js")
public abstract class MyOAuth2Signin extends PolymerTemplate<TemplateModel> {

  private final List<Consumer<String>> loginListeners = new ArrayList<>();
  private String accessToken;

  @Id("signin-button")
  private Button signinButton;

  protected MyOAuth2Signin(String authUrl, String redirectUri) {

    requireNonNull(authUrl);
    requireNonNull(redirectUri);

    addListener(AccessTokenReceivedEvent.class, e -> onAccessTokenReceived(e.getAccessToken()));

    getElement().setAttribute("auth-url", authUrl);
    getElement().setAttribute("redirect-uri", redirectUri);
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    configureButton(signinButton);
  }

  protected abstract void configureButton(Button button);

  private void onAccessTokenReceived(String accessToken) {
    requireNonNull(accessToken);
    this.accessToken = accessToken;
    loginListeners.forEach(listener -> listener.accept(accessToken));
  }

  /**
   * An error-response was returned from the auth-provider. The default implementation will silently
   * return.
   *
   * @param response the response
   */
  protected void onResponseError(Response response) {}

  public String getAccessToken() {
    return accessToken;
  }

  public Registration addLoginListener(Consumer<String> listener) {
    requireNonNull(listener);

    loginListeners.add(listener);
    return () -> loginListeners.remove(listener);
  }
}