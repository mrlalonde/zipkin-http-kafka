package com.github.mrlalonde.zipkinwebbridge;

import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.util.MimeType;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zipkin2.Span;
import zipkin2.codec.SpanBytesDecoder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;
import static org.springframework.util.MimeTypeUtils.APPLICATION_OCTET_STREAM;

final class SpanCodec implements Decoder<List<Span>> {
    private static final List<MimeType> DECODABLE_TYPES = Arrays.asList(APPLICATION_JSON, APPLICATION_OCTET_STREAM);

    @Override
    public boolean canDecode(ResolvableType resolvableType, MimeType mimeType) {
        return isSpanList(resolvableType) && DECODABLE_TYPES.contains(mimeType);
    }

    private boolean isSpanList(ResolvableType resolvableType) {
        return resolvableType.getRawClass().equals(List.class);
    }

    @Override
    public Flux<List<Span>> decode(Publisher<DataBuffer> publisher, ResolvableType resolvableType, MimeType mimeType, Map<String, Object> map) {
        return Flux.from(publisher).map(dataBuffer -> decode(dataBuffer, resolvableType, mimeType, map));
    }

    @Override
    public Mono<List<Span>> decodeToMono(Publisher<DataBuffer> publisher, ResolvableType resolvableType, MimeType mimeType, Map<String, Object> map) {
        return DataBufferUtils.join(publisher).map(dataBuffer -> decode(dataBuffer, resolvableType, mimeType, map));
    }

    @Override
    public List<MimeType> getDecodableMimeTypes() {
        return DECODABLE_TYPES;
    }

    @Override
    public List<Span> decode(DataBuffer dataBuffer, ResolvableType targetType, MimeType mimeType, Map<String, Object> hints) throws DecodingException {
        try {
            ByteBuffer byteBuffer = isGzipped(mimeType) ?
                    gunzip(dataBuffer) :
                    dataBuffer.asByteBuffer();

            return SpanBytesDecoder.JSON_V2.decodeList(byteBuffer);
        } finally {
            DataBufferUtils.release(dataBuffer);
        }
    }

    private boolean isGzipped(MimeType mimeType) {
        return APPLICATION_OCTET_STREAM.equals(mimeType);
    }

    ByteBuffer gunzip(DataBuffer dataBuffer) {
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(dataBuffer.asInputStream())) {
            return ByteBuffer.wrap(StreamUtils.copyToByteArray(gzipInputStream));
        } catch (IOException e) {
            throw new DecodingException("Couldn't unzip data", e);
        }
    }
}
