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

package org.jhapy.frontend.client.reference;

import com.vaadin.flow.spring.SpringServlet;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-02
 */
@Service
public class ReferenceServices {

  public static CountryService getCountryService() {
    return getApplicationContext().getBean(CountryService.class);
  }

  public static CountryTrlService getCountryTrlService() {
    return getApplicationContext().getBean(CountryTrlService.class);
  }

  public static IntermediateRegionService getIntermediateRegionService() {
    return getApplicationContext().getBean(IntermediateRegionService.class);
  }

  public static IntermediateRegionTrlService getIntermediateRegionTrlService() {
    return getApplicationContext().getBean(IntermediateRegionTrlService.class);
  }

  public static RegionService getRegionService() {
    return getApplicationContext().getBean(RegionService.class);
  }

  public static RegionTrlService getRegionTrlService() {
    return getApplicationContext().getBean(RegionTrlService.class);
  }

  public static SubRegionService getSubRegionService() {
    return getApplicationContext().getBean(SubRegionService.class);
  }

  public static SubRegionTrlService getSubRegionTrlService() {
    return getApplicationContext().getBean(SubRegionTrlService.class);
  }

  public static ApplicationContext getApplicationContext() {
    ServletContext servletContext = SpringServlet.getCurrent().getServletContext();
    return WebApplicationContextUtils.getWebApplicationContext(servletContext);
  }
}
