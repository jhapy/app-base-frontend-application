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

package org.jhapy.frontend.security;

import org.jhapy.dto.domain.security.SecurityUser;
import org.jhapy.dto.serviceQuery.security.securityUser.GetSecurityUserByUsernameQuery;
import org.jhapy.frontend.client.security.SecurityUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Implements the {@link UserDetailsService}.
 *
 * <p>This implementation searches for {@link User} entities by the e-mail address supplied in the
 * login screen.
 *
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-26
 */
// @Service
// @Primary
public class UserDetailsServiceImpl implements UserDetailsService {

  private final SecurityUserService securityUserService;

  @Autowired
  public UserDetailsServiceImpl(SecurityUserService securityUserService) {
    this.securityUserService = securityUserService;
  }

  /**
   * Recovers the {@link SecurityUser} from the database using the e-mail address supplied in the
   * login screen. If the user is found, returns a {@link
   * org.springframework.security.core.userdetails.User}.
   *
   * @param username User's e-mail address
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    SecurityUser securityUser =
        securityUserService
            .getSecurityUserByUsername(new GetSecurityUserByUsernameQuery(username))
            .getData();

    if (securityUser != null) {
      return securityUser;
    } else {
      throw new UsernameNotFoundException("No user present with username: " + username);
    }
  }
}
