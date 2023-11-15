/*
 * Copyright 2023 Google LLC
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google LLC nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.google.api.gax.rpc;

import com.google.api.core.InternalApi;
import com.google.api.gax.rpc.mtls.MtlsProvider;
import com.google.auth.Credentials;
import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import javax.annotation.Nullable;

@InternalApi
@AutoValue
public abstract class EndpointContext {
  public static final String INVALID_UNIVERSE_DOMAIN_ERROR_MESSAGE =
      "The configured universe domain (%s) does not match the universe domain found in the credentials (%s). If you haven't configured the universe domain explicitly, `googleapis.com` is the default.";
  private static final String GOOGLE_DEFAULT_UNIVERSE = "googleapis.com";
  private static final String DEFAULT_PORT = "443";
  private static final String ENDPOINT_TEMPLATE = "https://SERVICE_NAME.UNIVERSE_DOMAIN:PORT";

  public abstract String hostServiceName();

  @Nullable
  public abstract String clientSettingsEndpoint();

  @Nullable
  public abstract String transportChannelEndpoint();

  @Nullable
  public abstract String mtlsEndpoint();

  public abstract boolean switchToMtlsEndpointAllowed();

  @Nullable
  public abstract String universeDomain();

  @VisibleForTesting
  @Nullable
  public abstract MtlsProvider mtlsProvider();

  public abstract Builder toBuilder();

  private String resolvedEndpoint;
  private String resolvedUniverseDomain;

  public static Builder newBuilder() {
    return new AutoValue_EndpointContext.Builder()
        .setHostServiceName("")
        .setSwitchToMtlsEndpointAllowed(false);
  }

  @VisibleForTesting
  void determineEndpoint() throws IOException {
    if (resolvedEndpoint != null && resolvedUniverseDomain != null) {
      return;
    }
    MtlsProvider mtlsProvider = mtlsProvider() == null ? new MtlsProvider() : mtlsProvider();
    String customEndpoint =
        transportChannelEndpoint() != null ? transportChannelEndpoint() : clientSettingsEndpoint();
    if (universeDomain() == null && customEndpoint == null) {
      String defaultEndpoint = buildEndpoint(hostServiceName());
      resolvedEndpoint =
          mtlsEndpointResolver(
              mtlsProvider, defaultEndpoint, mtlsEndpoint(), switchToMtlsEndpointAllowed());
      resolvedUniverseDomain = GOOGLE_DEFAULT_UNIVERSE;
    } else if (universeDomain() != null && universeDomain().isEmpty()) {
      throw new IllegalStateException("Universe Domain cannot be set as an empty string");
    } else if (universeDomain() != null && !universeDomain().isEmpty() && customEndpoint == null) {
      resolvedEndpoint =
          mtlsEndpointResolver(
              mtlsProvider,
              buildEndpoint(hostServiceName(), universeDomain(), DEFAULT_PORT),
              mtlsEndpoint(),
              switchToMtlsEndpointAllowed());
      if (resolvedEndpoint.contains("mtls") && !universeDomain().equals(GOOGLE_DEFAULT_UNIVERSE)) {
        throw new RuntimeException(
            "mTLS is not supported in any universe other than googleapis.com");
      } else {
        resolvedUniverseDomain = universeDomain();
      }
    } else if (customEndpoint != null && !customEndpoint.isEmpty()) {
      if (customEndpoint.contains("https://")) {
        customEndpoint = customEndpoint.substring(8);
      }
      if (customEndpoint.contains("localhost")) {
        resolvedEndpoint = customEndpoint;
        resolvedUniverseDomain = GOOGLE_DEFAULT_UNIVERSE;
        return;
      }
      // Parse the custom customEndpoint for the universe domain
      int periodIndex = customEndpoint.indexOf('.');
      int colonIndex = customEndpoint.indexOf(':');
      String universeDomain;
      if (colonIndex != -1) {
        universeDomain = customEndpoint.substring(periodIndex + 1, colonIndex);
      } else {
        universeDomain = customEndpoint.substring(periodIndex + 1);
      }
      resolvedEndpoint =
          mtlsEndpointResolver(
              mtlsProvider,
              buildEndpoint(hostServiceName(), universeDomain, DEFAULT_PORT),
              mtlsEndpoint(),
              switchToMtlsEndpointAllowed());
      if (resolvedEndpoint.contains("mtls") && !universeDomain.equals(GOOGLE_DEFAULT_UNIVERSE)) {
        throw new RuntimeException(
            "mTLS is not supported in any universe other than googleapis.com");
      } else {
        resolvedUniverseDomain = universeDomain;
      }
    } else {
      throw new IllegalStateException("Unable to resolve the endpoint");
    }
  }

  private String mtlsEndpointResolver(
      MtlsProvider mtlsProvider,
      String endpoint,
      String mtlsEndpoint,
      boolean switchToMtlsEndpointAllowed)
      throws IOException {
    if (switchToMtlsEndpointAllowed && mtlsProvider != null) {
      switch (mtlsProvider.getMtlsEndpointUsagePolicy()) {
        case ALWAYS:
          return mtlsEndpoint;
        case NEVER:
          return endpoint;
        default:
          if (mtlsProvider.useMtlsClientCertificate() && mtlsProvider.getKeyStore() != null) {
            return mtlsEndpoint;
          }
          return endpoint;
      }
    }
    return endpoint;
  }

  private String buildEndpoint(String hostServiceName) {
    return buildEndpoint(hostServiceName, GOOGLE_DEFAULT_UNIVERSE, DEFAULT_PORT);
  }

  private String buildEndpoint(String hostServiceName, String universeDomain, String port) {
    return ENDPOINT_TEMPLATE
        .replace("SERVICE_NAME", hostServiceName)
        .replace("UNIVERSE_DOMAIN", universeDomain)
        .replace("PORT", port);
  }

  public String resolveEndpoint() throws IOException {
    if (resolvedEndpoint == null) {
      determineEndpoint();
    }
    return resolvedEndpoint;
  }

  public String resolveUniverseDomain() throws IOException {
    if (resolvedUniverseDomain == null) {
      determineEndpoint();
    }
    return resolvedUniverseDomain;
  }

  public boolean isValidUniverseDomain(Credentials credentials) throws IOException {
    if (resolvedUniverseDomain == null) {
      determineEndpoint();
    }
    return true;
    //    return resolvedUniverseDomain.equals(credentials.getUniverseDomain());
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setHostServiceName(String hostServiceName);

    public abstract Builder setClientSettingsEndpoint(String clientSettingsEndpoint);

    public abstract Builder setTransportChannelEndpoint(String transportChannelEndpoint);

    public abstract Builder setMtlsEndpoint(String mtlsEndpoint);

    public abstract Builder setSwitchToMtlsEndpointAllowed(boolean switchToMtlsEndpointAllowed);

    public abstract Builder setUniverseDomain(String universeDomain);

    @VisibleForTesting
    public abstract Builder setMtlsProvider(MtlsProvider mtlsProvider);

    public abstract EndpointContext build();
  }
}
