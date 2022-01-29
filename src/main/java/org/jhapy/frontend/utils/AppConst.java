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

import org.jhapy.dto.utils.DirectionEnum;

import java.util.Locale;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-26
 */
public interface AppConst {

  // Todo: Change to a method
  Locale APP_LOCALE = Locale.ENGLISH;

  String PAGE_ROOT = "";
  String PAGE_LOGIN = "login";
  String PAGE_LOGIN_2 = "login2";
  String PAGE_POST = "posts";
  String PAGE_PLACES = "places";
  String PAGE_PLACES2 = "places2";
  String PAGE_FRIENDS = "friends";
  String PAGE_EVENTS = "events";
  String PAGE_NOTIFICATIONS = "notifications";
  String PAGE_USERS_ADMIN = "usersAdmin";
  String PAGE_USER_ADMIN = "userAdmin";
  String PAGE_MAIL_TEMPLATES_ADMIN = "notifications/mailTemplatesAdmin";
  String PAGE_SMS_TEMPLATES_ADMIN = "notifications/smsTemplatesAdmin";
  String PAGE_MAIL_TEMPLATE_ADMIN = "notifications/mailTemplateAdmin";
  String PAGE_SMS_TEMPLATE_ADMIN = "notifications/smsTemplateAdmin";
  String PAGE_MONITORING = "monitoring";
  String PAGE_SESSIONS_ADMIN = "monitoring/sessionsAdmin";
  String PAGE_ACTUAL_SESSIONS_ADMIN = "monitoring/actualSessionsAdmin";
  String PAGE_EUREKA_ADMIN = "monitoring/eurekaAdmin";
  String PAGE_CLOUD_CONFIG_ADMIN = "cloudConfigAdmin";
  String PAGE_SMS_ADMIN = "notifications/smsAdmin";
  String PAGE_MAILS_ADMIN = "notifications/mailsAdmin";
  String PAGE_USER_SETTINGS = "userSettings";
  String PAGE_SETTINGS = "settings";
  String PAGE_SECURITY = "security";
  String PAGE_USERS = "security/users";
  String PAGE_ROLES = "security/roles";
  String PAGE_GROUPS = "security/groups";
  String PAGE_SESSIONS = "security/sessions";
  String PAGE_ELEMENTS = "settings/i18n/elements";
  String PAGE_REFERENCES = "settings/reference";
  String PAGE_LANGUAGES = "settings/reference/languages";
  String PAGE_I18N = "settings/i18n";
  String PAGE_ACTIONS = "settings/i18n/actions";
  String PAGE_MESSAGES = "settings/i18n/messages";
  String PAGE_SOCIAL_MEDIAS = "settings/places/socialMedia";
  String PAGE_ENTRANCE_TYPE = "settings/places/entranceType";
  String PAGE_RATING_TYPE = "settings/places/ratingType";
  String PAGE_PLACE_TYPE = "settings/places/placeType";
  String PAGE_CLIENTS = "settings/security/clients";
  String PAGE_DB_TABLES = "settings/security/dbTables";
  String PAGE_SECURITY_USERS = "settings/security/securityUsers";
  String PAGE_SECURITY_ROLES = "settings/security/securityRoles";
  String PAGE_SECURITY_GROUPS = "settings/security/securityGroups";
  String PAGE_ADMIN_SWAGGERS = "settings/swaggers";
  String PAGE_ADMIN_SWAGGER_DETAILS = "settings/swagger/detail";
  String PAGE_COUNTRIES = "settings/reference/countries";
  String PAGE_ACTIVITIES = "settings/activities/activities";
  String PAGE_ACTIVITIES_ADMIN = "settings/activities/activitiesAdmin";
  String PAGE_ACTIVITY_ADMIN = "settings/activities/activitiyAdmin";

  String PAGE_PLACE_ADMIN = "placeAdmin";
  String PAGE_PLACES_ADMIN = "placesAdmin";

  // Todo: I18N titles
  String TITLE_LOGIN = "element.page.title.login";
  String TITLE_POST = "element.page.title.posts";
  String TITLE_PLACES = "element.page.title.places";
  String TITLE_FRIENDS = "element.page.title.friends";
  String TITLE_EVENTS = "element.page.title.events";
  String TITLE_NOTIFICATIONS = "element.page.title.notifications";
  String TITLE_SETTINGS = "element.page.title.settings";
  String TITLE_USERS_ADMIN = "element.page.title.usersAdmin";
  String TITLE_MAILS_ADMIN = "element.page.title.mailsAdmin";
  String TITLE_ACTUAL_SESSIONS_ADMIN = "element.page.title.actualSessionsAdmin";
  String TITLE_SWAGGER_ADMIN = "element.page.title.swagger";
  String TITLE_SWAGGERS_ADMIN = "element.page.title.swaggers";
  String TITLE_SESSIONS_ADMIN = "element.page.title.sessionsAdmin";
  String TITLE_EUREKA_ADMIN = "element.page.title.eurekaAdmin";
  String TITLE_CLOUD_CONFIG_ADMIN = "element.page.title.cloudConfigAdmin";
  String TITLE_SMS_ADMIN = "element.page.title.smsAdmin";
  String TITLE_NOTIFICATION_ADMIN = "element.page.title.notificationAdmin";
  String TITLE_MAIL_TEMPLATES_ADMIN = "element.page.title.mailTemplatesAdmin";
  String TITLE_SMS_TEMPLATES_ADMIN = "element.page.title.smsTemplatesAdmin";
  String TITLE_MAIL_TEMPLATE_ADMIN = "element.page.title.mailTemplateAdmin";
  String TITLE_SMS_TEMPLATE_ADMIN = "element.page.title.smsTemplateAdmin";
  String TITLE_USER_ADMIN = "element.page.title.userAdmin";
  String TITLE_USER_SETTINGS = "element.page.title.userSettings";
  String TITLE_SECURITY = "element.page.title.security";
  String TITLE_MONITORING = "element.page.title.monitoring";
  String TITLE_ACTIVITIES_ADMIN = "element.page.title.activitiesAdmin";
  String TITLE_ACTIVITY_ADMIN = "element.page.title.activityAdmin";
  String TITLE_PLACES_ADMIN = "element.page.title.placesAdmin";
  String TITLE_PLACE_ADMIN = "element.page.title.placeAdmin";
  String TITLE_CLIENTS = "element.page.title.clients";
  String TITLE_DB_TABLES = "element.page.title.dbTables";
  String TITLE_SECURITY_USERS = "element.page.title.securityUsers";
  String TITLE_SECURITY_ROLES = "element.page.title.securityRoles";
  String TITLE_SECURITY_GROUPS = "element.page.title.securityGroups";
  String TITLE_I18N = "element.page.title.i18n";
  String TITLE_ACTIVITIES = "element.page.title.activities";
  String TITLE_REFERENCES = "element.page.title.references";
  String TITLE_ELEMENTS = "element.page.title.elements";
  String TITLE_LANGUAGES = "element.page.title.languages";
  String TITLE_COUNTRIES = "element.page.title.countries";
  String TITLE_ACTIONS = "element.page.title.actions";
  String TITLE_SOCIAL_MEDIAS = "element.page.title.socialMedia";
  String TITLE_ENTRANCE_TYPE = "element.page.title.entranceType";
  String TITLE_PLACE_TYPE = "element.page.title.placeType";
  String TITLE_RATING_TYPE = "element.page.title.ratingType";
  String TITLE_MESSAGES = "element.page.title.messages";
  String TITLE_MAILS = "element.page.title.mail";
  String TITLE_SMS = "element.page.title.sms";
  String TITLE_LOGOUT = "element.page.title.logout";
  String TITLE_NOT_FOUND = "element.page.title.notFound";
  String TITLE_ACCESS_DENIED = "element.page.accessDenied";

  String[] SECURITY_USER_SORT_FIELDS = {"username", "id"};
  String[] DEFAULT_SORT_FIELDS = {"name", "id"};
  String[] DEFAULT_EXECUTIONS_SORT_FIELDS = {"created"};
  String[] DEFAULT_ASSET_TYPE_FIELDS = {"displayName"};
  String[] DEFAULT_USER_SORT_FIELDS = {"lastName"};
  String[] DEFAULT_GROUP_SORT_FIELDS = {"n.name"};
  String[] DEFAULT_RECORD_STATUS_SORT_FIELDS = {"seq"};
  String[] DEFAULT_SESSION_SORT_FIELDS = {"sessionStart"};
  String[] DEFAULT_ASSET_SORT_FIELDS = {"a.name", "a.id"};
  String[] DEFAULT_ID_SORT_FIELDS = {"id"};
  String[] COUNTRY_SORT_FIELDS = {"m.name"};

  DirectionEnum DEFAULT_SORT_DIRECTION = DirectionEnum.ASC;
  DirectionEnum DEFAULT_EXECUTIONS_SORT_DIRECTION = DirectionEnum.DESC;

  DirectionEnum DEFAULT_SESSION_SORT_DIRECTION = DirectionEnum.DESC;

  String ICON_OK = "/images/iconfinder_ok.png";
  String ICON_KO = "/images/iconfinder_ko.png";
  String ICON_BLANK = "/images/blank.gif";
  String ICON_GREEN = "/images/iconfinder_green.png";
  String ICON_RED = "/images/iconfinder_red.png";
  String NO_PICTURE = "/images/NoPicture.jpg";
  String VIEWPORT = "width=device-width, minimum-scale=1, initial-scale=1, user-scalable=yes";

  // Mutable for testing.
  int NOTIFICATION_DURATION = 4000;

  String AVATAR_ATTRIBUTE = "AVATAR";
  String CLIENT_ID_ATTRIBUTE = "CLIENT_ID";
  String MAILBOX_ATTRIBUTE = "MAILBOX";
  String MAILBOX_FULL_NAME_ATTRIBUTE = "MAILBOX_FULL_NAME";
  String SECURITY_USER_ATTRIBUTE = "SECURITY_USER";
  String SECURITY_USER_ID_ATTRIBUTE = "SECURITY_USER_ID";
  String USER_ATTRIBUTE = "USER";
  String THEME_ATTRIBUTE = "THEME";

  String USER_ID_ATTRIBUTE = "USER_ID";
  String NICKNAME_ATTRIBUTE = "NICKNAME";
}
