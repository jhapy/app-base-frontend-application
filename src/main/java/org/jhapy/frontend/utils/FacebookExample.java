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

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class FacebookExample {

  private static final String NETWORK_NAME = "Facebook";
  private static final String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/v3.2/me";

  private FacebookExample() {}

  @SuppressWarnings("PMD.SystemPrintln")
  public static void main(String... args)
      throws IOException, InterruptedException, ExecutionException {
    // Replace these with your client id and secret
    final String clientId = "287555422129327";
    final String clientSecret = "4371c4df775b7af8b2e3e3d23e619abb";
    final String secretState = "secret" + new Random().nextInt(999_999);
    final OAuth20Service service =
        new ServiceBuilder(clientId)
            .apiSecret(clientSecret)
            .callback("http://localhost:9002/frontend/oauth2/popupCallback.html")
            .build(FacebookApi.instance());

    final Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);

    System.out.println("=== " + NETWORK_NAME + "'s OAuth Workflow ===");
    System.out.println();

    // Obtain the Authorization URL
    System.out.println("Fetching the Authorization URL...");
    final String authorizationUrl = service.getAuthorizationUrl(secretState);
    System.out.println("Got the Authorization URL!");
    System.out.println("Now go and authorize ScribeJava here:");
    System.out.println(authorizationUrl);
    System.out.println("And paste the authorization code here");
    System.out.print(">>");
    final String code = in.nextLine();
    System.out.println();

    System.out.println(
        "And paste the state from server here. We have set 'secretState'='" + secretState + "'.");
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
    final OAuth2AccessToken accessToken = service.getAccessToken(code);
    System.out.println("Got the Access Token!");
    System.out.println("(The raw response looks like this: " + accessToken.getRawResponse() + "')");
    System.out.println();

    // Now let's go and ask for a protected resource!
    System.out.println("Now we're going to access a protected resource...");
    final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
    service.signRequest(accessToken, request);
    final Response response = service.execute(request);
    System.out.println("Got it! Lets see what we found...");
    System.out.println();
    System.out.println(response.getCode());
    System.out.println(response.getBody());

    System.out.println();
    System.out.println("Thats it man! Go and build something awesome with ScribeJava! :)");
  }
}
