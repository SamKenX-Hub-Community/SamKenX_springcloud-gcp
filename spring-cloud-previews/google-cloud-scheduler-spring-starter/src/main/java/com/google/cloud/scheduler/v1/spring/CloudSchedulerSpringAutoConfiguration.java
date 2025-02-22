/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.scheduler.v1.spring;

import com.google.api.core.BetaApi;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.retrying.RetrySettings;
import com.google.api.gax.rpc.HeaderProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.scheduler.v1.CloudSchedulerClient;
import com.google.cloud.scheduler.v1.CloudSchedulerSettings;
import com.google.cloud.spring.autoconfigure.core.GcpContextAutoConfiguration;
import com.google.cloud.spring.core.DefaultCredentialsProvider;
import com.google.cloud.spring.core.Retry;
import com.google.cloud.spring.core.util.RetryUtil;
import java.io.IOException;
import java.util.Collections;
import javax.annotation.Generated;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

// AUTO-GENERATED DOCUMENTATION AND CLASS.
/**
 * Auto-configuration for {@link CloudSchedulerClient}.
 *
 * <p>Provides auto-configuration for Spring Boot
 *
 * <p>The default instance has everything set to sensible defaults:
 *
 * <ul>
 *   <li>The default transport provider is used.
 *   <li>Credentials are acquired automatically through Application Default Credentials.
 *   <li>Retries are configured for idempotent methods but not for non-idempotent methods.
 * </ul>
 */
@Generated("by google-cloud-spring-generator")
@BetaApi("Autogenerated Spring autoconfiguration is not yet stable")
@AutoConfiguration
@AutoConfigureAfter(GcpContextAutoConfiguration.class)
@ConditionalOnClass(CloudSchedulerClient.class)
@ConditionalOnProperty(
    value = "com.google.cloud.scheduler.v1.cloud-scheduler.enabled",
    matchIfMissing = true)
@EnableConfigurationProperties(CloudSchedulerSpringProperties.class)
public class CloudSchedulerSpringAutoConfiguration {
  private final CloudSchedulerSpringProperties clientProperties;
  private final CredentialsProvider credentialsProvider;
  private static final Log LOGGER = LogFactory.getLog(CloudSchedulerSpringAutoConfiguration.class);

  protected CloudSchedulerSpringAutoConfiguration(
      CloudSchedulerSpringProperties clientProperties, CredentialsProvider credentialsProvider)
      throws IOException {
    this.clientProperties = clientProperties;
    if (this.clientProperties.getCredentials().hasKey()) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Using credentials from CloudScheduler-specific configuration");
      }
      this.credentialsProvider =
          ((CredentialsProvider) new DefaultCredentialsProvider(this.clientProperties));
    } else {
      this.credentialsProvider = credentialsProvider;
    }
  }

  /**
   * Provides a default transport channel provider bean. The default is gRPC and will default to it
   * unless the useRest option is supported and provided to use HTTP transport instead
   *
   * @return a default transport channel provider.
   */
  @Bean
  @ConditionalOnMissingBean(name = "defaultCloudSchedulerTransportChannelProvider")
  public TransportChannelProvider defaultCloudSchedulerTransportChannelProvider() {
    if (this.clientProperties.getUseRest()) {
      return CloudSchedulerSettings.defaultHttpJsonTransportProviderBuilder().build();
    }
    return CloudSchedulerSettings.defaultTransportChannelProvider();
  }

  /**
   * Provides a CloudSchedulerSettings bean configured to use a DefaultCredentialsProvider and the
   * client library's default transport channel provider
   * (defaultCloudSchedulerTransportChannelProvider()). It also configures the quota project ID and
   * executor thread count, if provided through properties.
   *
   * <p>Retry settings are also configured from service-level and method-level properties specified
   * in CloudSchedulerSpringProperties. Method-level properties will take precedence over
   * service-level properties if available, and client library defaults will be used if neither are
   * specified.
   *
   * @param defaultTransportChannelProvider TransportChannelProvider to use in the settings.
   * @return a {@link CloudSchedulerSettings} bean configured with {@link TransportChannelProvider}
   *     bean.
   */
  @Bean
  @ConditionalOnMissingBean
  public CloudSchedulerSettings cloudSchedulerSettings(
      @Qualifier("defaultCloudSchedulerTransportChannelProvider")
          TransportChannelProvider defaultTransportChannelProvider)
      throws IOException {
    CloudSchedulerSettings.Builder clientSettingsBuilder;
    if (this.clientProperties.getUseRest()) {
      clientSettingsBuilder = CloudSchedulerSettings.newHttpJsonBuilder();
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Using REST (HTTP/JSON) transport.");
      }
    } else {
      clientSettingsBuilder = CloudSchedulerSettings.newBuilder();
    }
    clientSettingsBuilder
        .setCredentialsProvider(this.credentialsProvider)
        .setTransportChannelProvider(defaultTransportChannelProvider)
        .setHeaderProvider(this.userAgentHeaderProvider());
    if (this.clientProperties.getQuotaProjectId() != null) {
      clientSettingsBuilder.setQuotaProjectId(this.clientProperties.getQuotaProjectId());
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace(
            "Quota project id set to "
                + this.clientProperties.getQuotaProjectId()
                + ", this overrides project id from credentials.");
      }
    }
    if (this.clientProperties.getExecutorThreadCount() != null) {
      ExecutorProvider executorProvider =
          CloudSchedulerSettings.defaultExecutorProviderBuilder()
              .setExecutorThreadCount(this.clientProperties.getExecutorThreadCount())
              .build();
      clientSettingsBuilder.setBackgroundExecutorProvider(executorProvider);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace(
            "Background executor thread count is "
                + this.clientProperties.getExecutorThreadCount());
      }
    }
    Retry serviceRetry = clientProperties.getRetry();
    if (serviceRetry != null) {
      RetrySettings listJobsRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.listJobsSettings().getRetrySettings(), serviceRetry);
      clientSettingsBuilder.listJobsSettings().setRetrySettings(listJobsRetrySettings);

      RetrySettings getJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.getJobSettings().getRetrySettings(), serviceRetry);
      clientSettingsBuilder.getJobSettings().setRetrySettings(getJobRetrySettings);

      RetrySettings createJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.createJobSettings().getRetrySettings(), serviceRetry);
      clientSettingsBuilder.createJobSettings().setRetrySettings(createJobRetrySettings);

      RetrySettings updateJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.updateJobSettings().getRetrySettings(), serviceRetry);
      clientSettingsBuilder.updateJobSettings().setRetrySettings(updateJobRetrySettings);

      RetrySettings deleteJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.deleteJobSettings().getRetrySettings(), serviceRetry);
      clientSettingsBuilder.deleteJobSettings().setRetrySettings(deleteJobRetrySettings);

      RetrySettings pauseJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.pauseJobSettings().getRetrySettings(), serviceRetry);
      clientSettingsBuilder.pauseJobSettings().setRetrySettings(pauseJobRetrySettings);

      RetrySettings resumeJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.resumeJobSettings().getRetrySettings(), serviceRetry);
      clientSettingsBuilder.resumeJobSettings().setRetrySettings(resumeJobRetrySettings);

      RetrySettings runJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.runJobSettings().getRetrySettings(), serviceRetry);
      clientSettingsBuilder.runJobSettings().setRetrySettings(runJobRetrySettings);

      RetrySettings listLocationsRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.listLocationsSettings().getRetrySettings(), serviceRetry);
      clientSettingsBuilder.listLocationsSettings().setRetrySettings(listLocationsRetrySettings);

      RetrySettings getLocationRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.getLocationSettings().getRetrySettings(), serviceRetry);
      clientSettingsBuilder.getLocationSettings().setRetrySettings(getLocationRetrySettings);

      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Configured service-level retry settings from properties.");
      }
    }
    Retry listJobsRetry = clientProperties.getListJobsRetry();
    if (listJobsRetry != null) {
      RetrySettings listJobsRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.listJobsSettings().getRetrySettings(), listJobsRetry);
      clientSettingsBuilder.listJobsSettings().setRetrySettings(listJobsRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Configured method-level retry settings for listJobs from properties.");
      }
    }
    Retry getJobRetry = clientProperties.getGetJobRetry();
    if (getJobRetry != null) {
      RetrySettings getJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.getJobSettings().getRetrySettings(), getJobRetry);
      clientSettingsBuilder.getJobSettings().setRetrySettings(getJobRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Configured method-level retry settings for getJob from properties.");
      }
    }
    Retry createJobRetry = clientProperties.getCreateJobRetry();
    if (createJobRetry != null) {
      RetrySettings createJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.createJobSettings().getRetrySettings(), createJobRetry);
      clientSettingsBuilder.createJobSettings().setRetrySettings(createJobRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Configured method-level retry settings for createJob from properties.");
      }
    }
    Retry updateJobRetry = clientProperties.getUpdateJobRetry();
    if (updateJobRetry != null) {
      RetrySettings updateJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.updateJobSettings().getRetrySettings(), updateJobRetry);
      clientSettingsBuilder.updateJobSettings().setRetrySettings(updateJobRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Configured method-level retry settings for updateJob from properties.");
      }
    }
    Retry deleteJobRetry = clientProperties.getDeleteJobRetry();
    if (deleteJobRetry != null) {
      RetrySettings deleteJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.deleteJobSettings().getRetrySettings(), deleteJobRetry);
      clientSettingsBuilder.deleteJobSettings().setRetrySettings(deleteJobRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Configured method-level retry settings for deleteJob from properties.");
      }
    }
    Retry pauseJobRetry = clientProperties.getPauseJobRetry();
    if (pauseJobRetry != null) {
      RetrySettings pauseJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.pauseJobSettings().getRetrySettings(), pauseJobRetry);
      clientSettingsBuilder.pauseJobSettings().setRetrySettings(pauseJobRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Configured method-level retry settings for pauseJob from properties.");
      }
    }
    Retry resumeJobRetry = clientProperties.getResumeJobRetry();
    if (resumeJobRetry != null) {
      RetrySettings resumeJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.resumeJobSettings().getRetrySettings(), resumeJobRetry);
      clientSettingsBuilder.resumeJobSettings().setRetrySettings(resumeJobRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Configured method-level retry settings for resumeJob from properties.");
      }
    }
    Retry runJobRetry = clientProperties.getRunJobRetry();
    if (runJobRetry != null) {
      RetrySettings runJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.runJobSettings().getRetrySettings(), runJobRetry);
      clientSettingsBuilder.runJobSettings().setRetrySettings(runJobRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Configured method-level retry settings for runJob from properties.");
      }
    }
    Retry listLocationsRetry = clientProperties.getListLocationsRetry();
    if (listLocationsRetry != null) {
      RetrySettings listLocationsRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.listLocationsSettings().getRetrySettings(), listLocationsRetry);
      clientSettingsBuilder.listLocationsSettings().setRetrySettings(listLocationsRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Configured method-level retry settings for listLocations from properties.");
      }
    }
    Retry getLocationRetry = clientProperties.getGetLocationRetry();
    if (getLocationRetry != null) {
      RetrySettings getLocationRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.getLocationSettings().getRetrySettings(), getLocationRetry);
      clientSettingsBuilder.getLocationSettings().setRetrySettings(getLocationRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Configured method-level retry settings for getLocation from properties.");
      }
    }
    return clientSettingsBuilder.build();
  }

  /**
   * Provides a CloudSchedulerClient bean configured with CloudSchedulerSettings.
   *
   * @param cloudSchedulerSettings settings to configure an instance of client bean.
   * @return a {@link CloudSchedulerClient} bean configured with {@link CloudSchedulerSettings}
   */
  @Bean
  @ConditionalOnMissingBean
  public CloudSchedulerClient cloudSchedulerClient(CloudSchedulerSettings cloudSchedulerSettings)
      throws IOException {
    return CloudSchedulerClient.create(cloudSchedulerSettings);
  }

  private HeaderProvider userAgentHeaderProvider() {
    String springLibrary = "spring-autogen-cloud-scheduler";
    String version = this.getClass().getPackage().getImplementationVersion();
    return () -> Collections.singletonMap("user-agent", springLibrary + "/" + version);
  }
}
