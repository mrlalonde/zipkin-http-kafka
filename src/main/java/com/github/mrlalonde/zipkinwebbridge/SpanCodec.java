package com.github.mrlalonde.zipkinwebbridge;

import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.util.MimeType;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zipkin2.Span;
import zipkin2.codec.SpanBytesDecoder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;
import static org.springframework.util.MimeTypeUtils.APPLICATION_OCTET_STREAM;

final class SpanCodec implements Decoder<List<Span>> {

    @Override
    public boolean canDecode(ResolvableType resolvableType, MimeType mimeType) {
        return resolvableType.getRawClass().equals(List.class)
                && (mimeType.equals(APPLICATION_JSON) || mimeType.equals(APPLICATION_OCTET_STREAM));
    }

    @Override
    public Flux<List<Span>> decode(Publisher<DataBuffer> publisher, ResolvableType resolvableType, MimeType mimeType, Map<String, Object> map) {
        return Flux.from(publisher).map(dataBuffer -> decode(dataBuffer, resolvableType, mimeType, map));
    }

    @Override
    public Mono<List<Span>> decodeToMono(Publisher<DataBuffer> publisher, ResolvableType resolvableType, MimeType mimeType, Map<String, Object> map) {
        return Mono.from(publisher).map(dataBuffer -> decode(dataBuffer, resolvableType, mimeType, map));
    }

    @Override
    public List<MimeType> getDecodableMimeTypes() {
        return Arrays.asList(APPLICATION_JSON, APPLICATION_OCTET_STREAM);
    }

    @Override
    public List<Span> decode(DataBuffer buffer, ResolvableType targetType, MimeType mimeType, Map<String, Object> hints) throws DecodingException {
        ByteBuffer byteBuffer = APPLICATION_OCTET_STREAM.equals(mimeType) ? gzipDecompress(buffer) : buffer.asByteBuffer();
        List<Span> spans = SpanBytesDecoder.JSON_V2.decodeList(byteBuffer);
        return spans;
    }

    ByteBuffer gzipDecompress(DataBuffer dataBuffer) {
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(dataBuffer.asInputStream())) {
            return ByteBuffer.wrap(StreamUtils.copyToByteArray(gzipInputStream));
        } catch (IOException e) {
            throw new DecodingException("Couldn't unzip data", e);
        }
    }
}
