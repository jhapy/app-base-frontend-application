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

package org.jhapy.frontend.config;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.jhapy.commons.config.AppProperties;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.commons.utils.PrefixedKeyGenerator;
import org.jhapy.commons.utils.SpringProfileConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import javax.annotation.PreDestroy;

@Configuration
@EnableCaching
public class CacheConfiguration implements HasLogger {

  private final Environment env;
  private final ServerProperties serverProperties;
  private final DiscoveryClient discoveryClient;
  private GitProperties gitProperties;
  private BuildProperties buildProperties;
  private Registration registration;

  public CacheConfiguration(
      Environment env, ServerProperties serverProperties, DiscoveryClient discoveryClient) {
    var loggerPrefix = getLoggerPrefix("CacheConfiguration");
    logger().info(loggerPrefix + "Startup");
    this.env = env;
    this.serverProperties = serverProperties;
    this.discoveryClient = discoveryClient;
  }

  @Autowired(required = false)
  public void setRegistration(Registration registration) {
    this.registration = registration;
  }

  @PreDestroy
  public void destroy() throws Exception {
    var loggerPrefix = getLoggerPrefix("destroy");
    logger().info(loggerPrefix + "Closing Cache Manager");
    Hazelcast.shutdownAll();
  }

  @Bean
  public CacheManager cacheManager(HazelcastInstance hazelcastInstance) {
    var loggerPrefix = getLoggerPrefix("cacheManager");
    logger().info(loggerPrefix + "Starting HazelcastCacheManager");
    return new com.hazelcast.spring.cache.HazelcastCacheManager(hazelcastInstance);
  }

  @Bean
  public HazelcastInstance hazelcastInstance(AppProperties appProperties) {
    var loggerPrefix = getLoggerPrefix("hazelcastInstance");

    logger().info(loggerPrefix + "Configuring Hazelcast");
    HazelcastInstance hazelCastInstance =
        Hazelcast.getHazelcastInstanceByName(env.getProperty("spring.application.name"));
    if (hazelCastInstance != null) {
      logger().info(loggerPrefix + "Hazelcast already initialized");
      return hazelCastInstance;
    }
    Config config = new Config();
    MetricsConfig metricsConfig = new MetricsConfig();
    metricsConfig.setEnabled(false);
    config.setMetricsConfig(metricsConfig);
    config.setInstanceName(env.getProperty("spring.application.name"));
    config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
    if (this.registration == null) {
      logger()
          .warn(
              loggerPrefix + "No discovery service is set up, Hazelcast cannot create a cluster.");
    } else {
      // The serviceId is by default the application's name,
      // see the "spring.application.name" standard Spring property
      String serviceId = registration.getServiceId();
      logger()
          .debug(loggerPrefix + "Configuring Hazelcast clustering for instanceId: {}", serviceId);
      // In development, everything goes through 127.0.0.1, with a different port
      if (env.acceptsProfiles(Profiles.of(SpringProfileConstants.SPRING_PROFILE_K8S))) {
        logger()
            .debug(
                loggerPrefix
                    + "Application is running with the \"k8s\" profile, Hazelcast cluster will use Kubernetes Client");

        config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getKubernetesConfig().setEnabled(true);
        config
            .getNetworkConfig()
            .getJoin()
            .getKubernetesConfig()
            .setProperty("namespace", "thothee-test")
            .setProperty("service-name", "app-i18n-server");

      } else if (env.acceptsProfiles(
          Profiles.of(
              SpringProfileConstants.SPRING_PROFILE_TEST,
              SpringProfileConstants.SPRING_PROFILE_DEVELOPMENT,
              SpringProfileConstants.SPRING_PROFILE_STAGING,
              SpringProfileConstants.SPRING_PROFILE_PRODUCTION))) {
        logger()
            .debug(
                loggerPrefix
                    + "Application is running with the \"docker swarm\" profile, Hazelcast cluster will use Eureka Client");

        config.getNetworkConfig().setPort(5701);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
        for (ServiceInstance instance : discoveryClient.getInstances(serviceId)) {
          String clusterMember = instance.getHost() + ":5701";
          logger()
              .debug(loggerPrefix + "Adding Hazelcast (deploy) cluster member {}", clusterMember);
          config.getNetworkConfig().getJoin().getTcpIpConfig().addMember(clusterMember);
        }
      } else {
        logger()
            .debug(
                loggerPrefix
                    + "Application is running with the \"local\" profile, Hazelcast "
                    + "cluster will only work with localhost instances");

        config.getNetworkConfig().setPort(serverProperties.getPort() + 5701);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
        for (ServiceInstance instance : discoveryClient.getInstances(serviceId)) {
          String clusterMember = "127.0.0.1:" + (instance.getPort() + 5701);
          logger().debug(loggerPrefix + "Adding Hazelcast (dev) cluster member {}", clusterMember);
          config.getNetworkConfig().getJoin().getTcpIpConfig().addMember(clusterMember);
        }
      }
    }
    config.setManagementCenterConfig(new ManagementCenterConfig());
    config.addMapConfig(initializeDefaultMapConfig(appProperties));
    config.addMapConfig(initializeDomainMapConfig(appProperties));
    return Hazelcast.newHazelcastInstance(config);
  }

  private MapConfig initializeDefaultMapConfig(AppProperties appProperties) {
    MapConfig mapConfig = new MapConfig("default");

    /*
    Number of backups. If 1 is set as the backup-count for example,
    then all entries of the map will be copied to another JVM for
    fail-safety. Valid numbers are 0 (no backup), 1, 2, 3.
    */
    mapConfig.setBackupCount(appProperties.getHazelcast().getBackupCount());

    /*
    Valid values are:
    NONE (no eviction),
    LRU (Least Recently Used),
    LFU (Least Frequently Used).
    NONE is the default.
    */
    mapConfig.getEvictionConfig().setEvictionPolicy(EvictionPolicy.LRU);

    /*
    Maximum size of the map. When max size is reached,
    map is evicted based on the policy defined.
    Any integer between 0 and Integer.MAX_VALUE. 0 means
    Integer.MAX_VALUE. Default is 0.
    */
    mapConfig.getEvictionConfig().setMaxSizePolicy(MaxSizePolicy.USED_HEAP_SIZE);

    return mapConfig;
  }

  private MapConfig initializeDomainMapConfig(AppProperties appProperties) {
    MapConfig mapConfig = new MapConfig("org.jhapy.frontend.*");
    mapConfig.setTimeToLiveSeconds(appProperties.getHazelcast().getTimeToLiveSeconds());
    return mapConfig;
  }

  @Autowired(required = false)
  public void setGitProperties(GitProperties gitProperties) {
    this.gitProperties = gitProperties;
  }

  @Autowired(required = false)
  public void setBuildProperties(BuildProperties buildProperties) {
    this.buildProperties = buildProperties;
  }

  @Bean
  public KeyGenerator keyGenerator() {
    return new PrefixedKeyGenerator(this.gitProperties, this.buildProperties);
  }
}
