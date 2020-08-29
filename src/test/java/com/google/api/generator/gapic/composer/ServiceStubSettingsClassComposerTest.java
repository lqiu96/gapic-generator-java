// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.api.generator.gapic.composer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import com.google.api.generator.engine.writer.JavaWriterVisitor;
import com.google.api.generator.gapic.model.GapicClass;
import com.google.api.generator.gapic.model.Message;
import com.google.api.generator.gapic.model.ResourceName;
import com.google.api.generator.gapic.model.Service;
import com.google.api.generator.gapic.protoparser.Parser;
import com.google.api.generator.gapic.protoparser.ServiceConfigParser;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import com.google.showcase.v1beta1.EchoOuterClass;
import io.grpc.serviceconfig.ServiceConfig;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class ServiceStubSettingsClassComposerTest {
  private static final String JSON_DIRECTORY =
      "src/test/java/com/google/api/generator/gapic/testdata/";

  private ServiceDescriptor echoService;
  private FileDescriptor echoFileDescriptor;

  @Before
  public void setUp() {
    echoFileDescriptor = EchoOuterClass.getDescriptor();
    echoService = echoFileDescriptor.getServices().get(0);
    assertEquals(echoService.getName(), "Echo");
  }

  @Test
  public void generateServiceClasses() {
    Map<String, Message> messageTypes = Parser.parseMessages(echoFileDescriptor);
    Map<String, ResourceName> resourceNames = Parser.parseResourceNames(echoFileDescriptor);
    Set<ResourceName> outputResourceNames = new HashSet<>();
    List<Service> services =
        Parser.parseService(echoFileDescriptor, messageTypes, resourceNames, outputResourceNames);

    String jsonFilename = "showcase_grpc_service_config.json";
    Path jsonPath = Paths.get(JSON_DIRECTORY, jsonFilename);
    Optional<ServiceConfig> configOpt = ServiceConfigParser.parseFile(jsonPath.toString());
    assertTrue(configOpt.isPresent());
    ServiceConfig config = configOpt.get();

    Service echoProtoService = services.get(0);
    GapicClass clazz =
        ServiceStubSettingsClassComposer.instance()
            .generate(echoProtoService, config, messageTypes);

    JavaWriterVisitor visitor = new JavaWriterVisitor();
    clazz.classDefinition().accept(visitor);
    assertEquals(EXPECTED_CLASS_STRING, visitor.write());
  }

  // TODO(miraleung): Update this when a file-diffing test mechanism is in place.
  private static final String EXPECTED_CLASS_STRING =
      "package com.google.showcase.v1beta1.stub;\n"
          + "\n"
          + "import static com.google.showcase.v1beta1.EchoClient.PagedExpandPagedResponse;\n"
          + "\n"
          + "import com.google.api.core.ApiFuture;\n"
          + "import com.google.api.core.BetaApi;\n"
          + "import com.google.api.gax.core.GaxProperties;\n"
          + "import com.google.api.gax.core.GoogleCredentialsProvider;\n"
          + "import com.google.api.gax.core.InstantiatingExecutorProvider;\n"
          + "import com.google.api.gax.grpc.GaxGrpcProperties;\n"
          + "import com.google.api.gax.grpc.GrpcTransportChannel;\n"
          + "import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;\n"
          + "import com.google.api.gax.rpc.ApiCallContext;\n"
          + "import com.google.api.gax.rpc.ApiClientHeaderProvider;\n"
          + "import com.google.api.gax.rpc.ClientContext;\n"
          + "import com.google.api.gax.rpc.OperationCallSettings;\n"
          + "import com.google.api.gax.rpc.PageContext;\n"
          + "import com.google.api.gax.rpc.PagedCallSettings;\n"
          + "import com.google.api.gax.rpc.PagedListDescriptor;\n"
          + "import com.google.api.gax.rpc.PagedListResponseFactory;\n"
          + "import com.google.api.gax.rpc.ServerStreamingCallSettings;\n"
          + "import com.google.api.gax.rpc.StreamingCallSettings;\n"
          + "import com.google.api.gax.rpc.StubSettings;\n"
          + "import com.google.api.gax.rpc.TransportChannelProvider;\n"
          + "import com.google.api.gax.rpc.UnaryCallSettings;\n"
          + "import com.google.api.gax.rpc.UnaryCallable;\n"
          + "import com.google.common.collect.ImmutableList;\n"
          + "import com.google.longrunning.Operation;\n"
          + "import com.google.showcase.v1beta1.BlockRequest;\n"
          + "import com.google.showcase.v1beta1.BlockResponse;\n"
          + "import com.google.showcase.v1beta1.EchoRequest;\n"
          + "import com.google.showcase.v1beta1.EchoResponse;\n"
          + "import com.google.showcase.v1beta1.ExpandRequest;\n"
          + "import com.google.showcase.v1beta1.PagedExpandRequest;\n"
          + "import com.google.showcase.v1beta1.PagedExpandResponse;\n"
          + "import com.google.showcase.v1beta1.WaitMetadata;\n"
          + "import com.google.showcase.v1beta1.WaitRequest;\n"
          + "import com.google.showcase.v1beta1.WaitResponse;\n"
          + "import java.io.IOException;\n"
          + "import java.util.List;\n"
          + "import javax.annotation.Generated;\n"
          + "\n"
          + "@BetaApi\n"
          + "@Generated(\"by gapic-generator-java\")\n"
          + "public class EchoStubSettings extends StubSettings<EchoStubSettings> {\n"
          + "  private static final ImmutableList<String> DEFAULT_SERVICE_SCOPES =\n"
          + "     "
          + " ImmutableList.<String>builder().add(\"https://www.googleapis.com/auth/cloud-platform\").build();\n"
          + "  private final UnaryCallSettings<EchoRequest, EchoResponse> echoSettings;\n"
          + "  private final ServerStreamingCallSettings<ExpandRequest, EchoResponse>"
          + " expandSettings;\n"
          + "  private final StreamingCallSettings<EchoRequest, EchoResponse> collectSettings;\n"
          + "  private final StreamingCallSettings<EchoRequest, EchoResponse> chatSettings;\n"
          + "  private final StreamingCallSettings<EchoRequest, EchoResponse> chatAgainSettings;\n"
          + "  private final PagedCallSettings<PagedExpandRequest, PagedExpandResponse,"
          + " PagedExpandPagedResponse>\n"
          + "      pagedExpandSettings;\n"
          + "  private final UnaryCallSettings<WaitRequest, Operation> waitSettings;\n"
          + "  private final OperationCallSettings<WaitRequest, WaitResponse, WaitMetadata>\n"
          + "      waitOperationSettings;\n"
          + "  private final UnaryCallSettings<BlockRequest, BlockResponse> blockSettings;\n"
          + "  private static final PagedListResponseFactory<\n"
          + "          PagedExpandRequest, PagedExpandResponse, PagedExpandPagedResponse>\n"
          + "      PAGED_EXPAND_PAGE_STR_FACT =\n"
          + "          new PagedListResponseFactory<\n"
          + "              PagedExpandRequest, PagedExpandResponse, PagedExpandPagedResponse>()"
          + " {\n"
          + "            @Override\n"
          + "            public ApiFuture<PagedExpandPagedResponse> getFuturePagedResponse(\n"
          + "                UnaryCallable<PagedExpandRequest, PagedExpandResponse> callable,\n"
          + "                PagedExpandRequest request,\n"
          + "                ApiCallContext context,\n"
          + "                ApiFuture<PagedExpandResponse> futureResponse) {\n"
          + "              PageContext<PagedExpandRequest, PagedExpandResponse, EchoResponse>"
          + " pageContext =\n"
          + "                  PageContext.create(callable, PAGED_EXPAND_PAGE_STR_DESC, request,"
          + " context);\n"
          + "              return PagedExpandPagedResponse.createAsync(pageContext,"
          + " futureResponse);\n"
          + "            }\n"
          + "          };\n"
          + "\n"
          + "  public UnaryCallSettings<EchoRequest, EchoResponse> echoSettings() {\n"
          + "    return echoSettings;\n"
          + "  }\n"
          + "\n"
          + "  public ServerStreamingCallSettings<ExpandRequest, EchoResponse> expandSettings()"
          + " {\n"
          + "    return expandSettings;\n"
          + "  }\n"
          + "\n"
          + "  public StreamingCallSettings<EchoRequest, EchoResponse> collectSettings() {\n"
          + "    return collectSettings;\n"
          + "  }\n"
          + "\n"
          + "  public StreamingCallSettings<EchoRequest, EchoResponse> chatSettings() {\n"
          + "    return chatSettings;\n"
          + "  }\n"
          + "\n"
          + "  public StreamingCallSettings<EchoRequest, EchoResponse> chatAgainSettings() {\n"
          + "    return chatAgainSettings;\n"
          + "  }\n"
          + "\n"
          + "  public PagedCallSettings<PagedExpandRequest, PagedExpandResponse,"
          + " PagedExpandPagedResponse>\n"
          + "      pagedExpandSettings() {\n"
          + "    return pagedExpandSettings;\n"
          + "  }\n"
          + "\n"
          + "  public UnaryCallSettings<WaitRequest, Operation> waitSettings() {\n"
          + "    return waitSettings;\n"
          + "  }\n"
          + "\n"
          + "  public OperationCallSettings<WaitRequest, WaitResponse, WaitMetadata>"
          + " waitOperationSettings() {\n"
          + "    return waitOperationSettings;\n"
          + "  }\n"
          + "\n"
          + "  public UnaryCallSettings<BlockRequest, BlockResponse> blockSettings() {\n"
          + "    return blockSettings;\n"
          + "  }\n"
          + "\n"
          + "  @BetaApi(\"A restructuring of stub classes is planned, so this may break in the"
          + " future\")\n"
          + "  public EchoStub createStub() throws IOException {\n"
          + "    if (getTransportChannelProvider()\n"
          + "        .getTransportName()\n"
          + "        .equals(GrpcTransportChannel.getGrpcTransportName())) {\n"
          + "      return GrpcEchoStub.create(this);\n"
          + "    }\n"
          + "    throw new UnsupportedOperationException(\n"
          + "        String.format(\n"
          + "            \"Transport not supported: %s\","
          + " getTransportChannelProvider().getTransportName()));\n"
          + "  }\n"
          + "\n"
          + "  public static InstantiatingExecutorProvider.Builder"
          + " defaultExecutorProviderBuilder() {\n"
          + "    return InstantiatingExecutorProvider.newBuilder();\n"
          + "  }\n"
          + "\n"
          + "  public static String getDefaultEndpoint() {\n"
          + "    return \"localhost:7469\";\n"
          + "  }\n"
          + "\n"
          + "  public static List<String> getDefaultServiceScopes() {\n"
          + "    return DEFAULT_SERVICE_SCOPES;\n"
          + "  }\n"
          + "\n"
          + "  public static GoogleCredentialsProvider.Builder defaultCredentialsProviderBuilder()"
          + " {\n"
          + "    return"
          + " GoogleCredentialsProvider.newBuilder().setScopesToApply(DEFAULT_SERVICE_SCOPES);\n"
          + "  }\n"
          + "\n"
          + "  public static InstantiatingGrpcChannelProvider.Builder"
          + " defaultGrpcTransportProviderBuilder() {\n"
          + "    return InstantiatingGrpcChannelProvider.newBuilder()\n"
          + "        .setMaxInboundMessageSize(Integer.MAX_VALUE);\n"
          + "  }\n"
          + "\n"
          + "  public static TransportChannelProvider defaultTransportChannelProvider() {\n"
          + "    return defaultGrpcTransportProviderBuilder().build();\n"
          + "  }\n"
          + "\n"
          + "  @BetaApi(\"The surface for customizing headers is not stable yet and may change in"
          + " the future.\")\n"
          + "  public static ApiClientHeaderProvider.Builder"
          + " defaultApiClientHeaderProviderBuilder() {\n"
          + "    return ApiClientHeaderProvider.newBuilder()\n"
          + "        .setGeneratedLibToken(\"gapic\","
          + " GaxProperties.getLibraryVersion(EchoStubSettings.class))\n"
          + "        .setTransportToken(\n"
          + "            GaxGrpcProperties.getGrpcTokenName(),"
          + " GaxGrpcProperties.getGrpcVersion());\n"
          + "  }\n"
          + "\n"
          + "  public static Builder newBuilder() {\n"
          + "    return Builder.createDefault();\n"
          + "  }\n"
          + "\n"
          + "  public static Builder newBuilder(ClientContext clientContext) {\n"
          + "    return new Builder(clientContext);\n"
          + "  }\n"
          + "\n"
          + "  public Builder toBuilder() {\n"
          + "    return new Builder(this);\n"
          + "  }\n"
          + "\n"
          + "  protected EchoStubSettings(Builder settingsBuilder) throws IOException {\n"
          + "    super(settingsBuilder);\n"
          + "    echoSettings = settingsBuilder.echoSettings().build();\n"
          + "    expandSettings = settingsBuilder.expandSettings().build();\n"
          + "    collectSettings = settingsBuilder.collectSettings().build();\n"
          + "    chatSettings = settingsBuilder.chatSettings().build();\n"
          + "    chatAgainSettings = settingsBuilder.chatAgainSettings().build();\n"
          + "    pagedExpandSettings = settingsBuilder.pagedExpandSettings().build();\n"
          + "    waitSettings = settingsBuilder.waitSettings().build();\n"
          + "    waitOperationSettings = settingsBuilder.waitOperationSettings().build();\n"
          + "    blockSettings = settingsBuilder.blockSettings().build();\n"
          + "  }\n"
          + "\n"
          + "  public static class Builder {}\n"
          + "}\n";
}