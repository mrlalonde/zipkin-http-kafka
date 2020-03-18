package com.github.mrlalonde.zipkinwebbridge;

import com.linecorp.armeria.client.encoding.StreamDecoderFactory;
import com.linecorp.armeria.common.*;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.annotation.Post;
import io.netty.buffer.ByteBufHolder;
import io.netty.util.ReferenceCountUtil;
import org.springframework.stereotype.Component;
import zipkin2.Callback;
import zipkin2.SpanBytesDecoderDetector;
import zipkin2.codec.SpanBytesDecoder;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static zipkin2.Call.propagateIfFatal;

@Component
public class ZipkinWebBridgeController {

    private ZipkinCollector collector;

    @Post("/api/v2/spans")
    public HttpResponse uploadSpans(ServiceRequestContext ctx, HttpRequest req) {
        return validateAndStoreSpans(SpanBytesDecoder.JSON_V2, ctx, req);
    }

    HttpResponse validateAndStoreSpans(SpanBytesDecoder decoder, ServiceRequestContext ctx,
                                       HttpRequest req) {
        CompletableCallback result = new CompletableCallback();

        req.aggregateWithPooledObjects(ctx.contextAwareEventLoop(), ctx.alloc()).handle((msg, t) -> {
            if (t != null) {
                result.onError(t);
                return null;
            }

            final HttpData content;
            try {
                content = UnzippingBytesRequestConverter.convertRequest(ctx, msg);
            } catch (Throwable t1) {
                propagateIfFatal(t1);
                result.onError(t1);
                return null;
            }

            try {
                // logging already handled upstream in UnzippingBytesRequestConverter where request context exists
                if (content.isEmpty()) {
                    result.onSuccess(null);
                    return null;
                }

                final ByteBuffer nioBuffer;
                if (content instanceof ByteBufHolder) {
                    nioBuffer = ((ByteBufHolder) content).content().nioBuffer();
                } else {
                    nioBuffer = ByteBuffer.wrap(content.array());
                }

                try {
                    SpanBytesDecoderDetector.decoderForListMessage(nioBuffer);
                } catch (IllegalArgumentException e) {
                    result.onError(new IllegalArgumentException("Expected a " + decoder + " encoded list\n"));
                    return null;
                } catch (Throwable t1) {
                    result.onError(t1);
                    return null;
                }

                // collector.accept might block so need to move off the event loop. We make sure the
                // callback is context aware to continue the trace.
                Executor executor = ctx.makeContextAware(ctx.blockingTaskExecutor());
                try {
                    collector.acceptSpans(nioBuffer, decoder, result, executor);
                } catch (Throwable t1) {
                    result.onError(t1);
                    return null;
                }
            } finally {
                ReferenceCountUtil.release(content);
            }

            return null;
        });

        return HttpResponse.from(result);
    }

    static final class CompletableCallback extends CompletableFuture<HttpResponse>
            implements Callback<Void> {

        static final ResponseHeaders ACCEPTED_RESPONSE = ResponseHeaders.of(HttpStatus.ACCEPTED);

        @Override
        public void onSuccess(Void value) {
            complete(HttpResponse.of(ACCEPTED_RESPONSE));
        }

        @Override
        public void onError(Throwable t) {
            completeExceptionally(t);
        }
    }


    static final class UnzippingBytesRequestConverter {

        static HttpData convertRequest(ServiceRequestContext ctx, AggregatedHttpRequest request) {
            String encoding = request.headers().get(HttpHeaderNames.CONTENT_ENCODING);
            HttpData content = request.content();
            if (!content.isEmpty() && encoding != null && encoding.contains("gzip")) {
                content = StreamDecoderFactory.gzip().newDecoder(ctx.alloc()).decode(content);
                // The implementation of the armeria decoder is to return an empty body on failure
                if (content.isEmpty()) {
                    ReferenceCountUtil.release(content);
                    throw new IllegalArgumentException("Cannot gunzip spans");
                }
            }

            //if (content.isEmpty()) ZipkinHttpCollector.maybeLog("Empty POST body", ctx, request);
            if (content.length() == 2 && "[]".equals(content.toStringAscii())) {
                //ZipkinHttpCollector.maybeLog("Empty JSON list POST body", ctx, request);
                ReferenceCountUtil.release(content);
                content = HttpData.EMPTY_DATA;
            }

            return content;
        }
    }

}
