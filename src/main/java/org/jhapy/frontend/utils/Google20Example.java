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

import com.github.scribejava.apis.GoogleApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class Google20Example {

  private static final String NETWORK_NAME = "Google";
  private static final String PROTECTED_RESOURCE_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

  private Google20Example() {
  }

  @SuppressWarnings("PMD.SystemPrintln")
  public static void main(String... args)
      throws IOException, InterruptedException, ExecutionException {
    // Replace these with your client id and secret
    final String clientId = "522878527467-0jr9v8mbis4ud9l592pcp7psusq62vfa.apps.googleusercontent.com";
    final String clientSecret = "rjwKy0f17NMkls80uvD3hkRZ";
    final String secretState = "secret" + new Random().nextInt(999_999);
    final OAuth20Service service = new ServiceBuilder(clientId)
        .apiSecret(clientSecret)
        .defaultScope("email") // replace with desired scope
        .callback("http://localhost:9002/frontend/oauth2/popupCallback.html")
        .build(GoogleApi20.instance());
    final Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);

    System.out.println("=== " + NETWORK_NAME + "'s OAuth Workflow ===");
    System.out.println();

    // Obtain the Authorization URL
    System.out.println("Fetching the Authorization URL...");
    //pass access_type=offline to get refresh token
    //https://developers.google.com/identity/protocols/OAuth2WebServer#preparing-to-start-the-oauth-20-flow
    final Map<String, String> additionalParams = new HashMap<>();
    additionalParams.put("access_type", "offline");
    //force to reget refresh token (if user are asked not the first time)
    additionalParams.put("prompt", "consent");
    final String authorizationUrl = service.createAuthorizationUrlBuilder()
        .state(secretState)
        .additionalParams(additionalParams)
        .build();
    System.out.println("Got the Authorization URL!");
    System.out.println("Now go and authorize ScribeJava here:");
    System.out.println(authorizationUrl);
    System.out.println("And paste the authorization code here");
    System.out.print(">>");
    final String code = in.nextLine();
    System.out.println();

    System.out.println(
        "And paste the state from server here. We have set 'secretState'='" + secretState
            + "'.");
    System.out.print(">>");
    final String value = in.nextLine();
    if (secretState.equals(value)) {
      System.out.println("State value does match!");
    } else {
      System.out.println("Ooops, state value does not match!");
      System.out.println("Expected = " + secretState);
      System.out.println("Got      = " + value);
      System.out.println();
    }

    System.out.println("Trading the Authorization Code for an Access Token...");
    OAuth2AccessToken accessToken = service.getAccessToken(code);
    System.out.println("Got the Access Token!");
    System.out
        .println("(The raw response looks like this: " + accessToken.getRawResponse() + "')");

    System.out.println("Refreshing the Access Token...");
    accessToken = service.refreshAccessToken(accessToken.getRefreshToken());
    System.out.println("Refreshed the Access Token!");
    System.out
        .println("(The raw response looks like this: " + accessToken.getRawResponse() + "')");
    System.out.println();

    // Now let's go and ask for a protected resource!
    System.out.println("Now we're going to access a protected resource...");
    while (true) {
      System.out.println(
          "Paste fieldnames to fetch (leave empty to get profile, 'exit' to stop example)");
      System.out.print(">>");
      final String query = in.nextLine();
      System.out.println();

      final String requestUrl;
      if ("exit".equals(query)) {
        break;
      } else if (query == null || query.isEmpty()) {
        requestUrl = PROTECTED_RESOURCE_URL;
      } else {
        requestUrl = PROTECTED_RESOURCE_URL + "?fields=" + query;
      }

      final OAuthRequest request = new OAuthRequest(Verb.GET, requestUrl);
      service.signRequest(accessToken, request);
      final Response response = service.execute(request);
      System.out.println();
      System.out.println(response.getCode());
      System.out.println(response.getBody());

      System.out.println();
    }
  }
}
