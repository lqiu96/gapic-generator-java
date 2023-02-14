/*
 * Copyright 2021 Google LLC
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
package com.google.api.gax.httpjson;

public class HttpJsonLongRunningClientTest {
  //
  //  private OperationSnapshotFactory<Option, Field> operationSnapFact;
  //  private PollingRequestFactory<Option> pollReqFact;
  //
  //  @Before
  //  public void init() {
  //    operationSnapFact =
  //        new OperationSnapshotFactory<Option, Field>() {
  //          @Override
  //          public OperationSnapshot create(final Option request, final Field response) {
  //            OperationSnapshot mockOpSnap = mock(OperationSnapshot.class);
  //            when(mockOpSnap.getName()).thenReturn(response.getName());
  //            return mockOpSnap;
  //          }
  //        };
  //    pollReqFact =
  //        new PollingRequestFactory<Option>() {
  //          @Override
  //          public Option create(String compoundOperationId) {
  //            return Option.newBuilder().setName(compoundOperationId).build();
  //          }
  //        };
  //  }
  //
  //  @Test
  //  public void getOperationCallableTest() {
  //    UnaryCallable<Option, Field> operationCallable =
  //        new UnaryCallable<Option, Field>() {
  //          @Override
  //          public ApiFuture<Field> futureCall(Option request, ApiCallContext context) {
  //            return ApiFutures.immediateFuture(Field.newBuilder().setName("Miami").build());
  //          }
  //        };
  //
  //    HttpJsonLongRunningClient<Option, Field> lroClient =
  //        new HttpJsonLongRunningClient<Option, Field>(
  //            operationCallable, operationSnapFact, pollReqFact);
  //    UnaryCallable<String, OperationSnapshot> operationCall = lroClient.getOperationCallable();
  //    assertThat(operationCall.call("Chicago").getName()).isEqualTo("Miami");
  //  }
  //
  //  @Test
  //  public void getOperationCallableFailTest() {
  //    UnaryCallable<Option, Field> operationCallable =
  //        new UnaryCallable<Option, Field>() {
  //          @Override
  //          public ApiFuture<Field> futureCall(Option request, ApiCallContext context) {
  //            return ApiFutures.immediateFailedFuture(new RuntimeException("Prague"));
  //          }
  //        };
  //
  //    HttpJsonLongRunningClient<Option, Field> lroClient =
  //        new HttpJsonLongRunningClient<Option, Field>(
  //            operationCallable, operationSnapFact, pollReqFact);
  //    UnaryCallable<String, OperationSnapshot> operationCall = lroClient.getOperationCallable();
  //    try {
  //      operationCall.call("Chicago");
  //      Assert.fail("Exception should have been thrown");
  //    } catch (RuntimeException e) {
  //      Truth.assertThat(e).hasMessageThat().contains("Prague");
  //    }
  //  }
  //
  //  @Test
  //  public void cancelOperationCallableTest() {
  //    UnaryCallable<Option, Field> operationCallable =
  //        new UnaryCallable<Option, Field>() {
  //          @Override
  //          public ApiFuture<Field> futureCall(Option request, ApiCallContext context) {
  //            return ApiFutures.immediateFuture(Field.newBuilder().setName("Miami").build());
  //          }
  //        };
  //
  //    HttpJsonLongRunningClient<Option, Field> lroClient =
  //        new HttpJsonLongRunningClient<Option, Field>(
  //            operationCallable, operationSnapFact, pollReqFact);
  //    UnaryCallable<String, Void> cancel = lroClient.cancelOperationCallable();
  //    assertNull(cancel);
  //  }
  //
  //  @Test
  //  public void deleteOperationCallableTest() {
  //    UnaryCallable<Option, Field> operationCallable =
  //        new UnaryCallable<Option, Field>() {
  //          @Override
  //          public ApiFuture<Field> futureCall(Option request, ApiCallContext context) {
  //            return ApiFutures.immediateFuture(Field.newBuilder().setName("Miami").build());
  //          }
  //        };
  //
  //    HttpJsonLongRunningClient<Option, Field> lroClient =
  //        new HttpJsonLongRunningClient<Option, Field>(
  //            operationCallable, operationSnapFact, pollReqFact);
  //    UnaryCallable<String, Void> delete = lroClient.cancelOperationCallable();
  //    assertNull(delete);
  //  }
}
