package com.github.mrlalonde.zipkinwebbridge;

import zipkin2.codec.SpanBytesDecoder;

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

public class ZipkinCollector {
    public void acceptSpans(ByteBuffer nioBuffer, SpanBytesDecoder decoder, ZipkinWebBridgeController.CompletableCallback result,
                            Executor executor) {

    }
}
