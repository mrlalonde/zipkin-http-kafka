package com.github.mrlalonde.zipkinwebbridge;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;
import static org.springframework.util.MimeTypeUtils.APPLICATION_OCTET_STREAM;

final class SpanCodec implements Decoder<List<Span>> {
    private static final Logger LOG = LoggerFactory.getLogger(SpanCodec.class);

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

        try {
            ByteBuffer byteBuffer = APPLICATION_OCTET_STREAM.equals(mimeType) ? gzipDecompress(buffer) :
                    ByteBuffer.wrap(StreamUtils.copyToByteArray(buffer.asInputStream()));

            // not sure if it was an issue - try using the DataBuffer directly without turning it to InputStream
            LOG.trace("Decoding mime type {} and size {}", mimeType, byteBuffer.remaining());

            return SpanBytesDecoder.JSON_V2.decodeList(byteBuffer);
        } catch (IOException | IllegalArgumentException e) {

            throw new DecodingException("Failed on decode " + buffer.toString(Charset.defaultCharset()), e);
        }

    }

    ByteBuffer gzipDecompress(DataBuffer dataBuffer) {
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(dataBuffer.asInputStream())) {
            return ByteBuffer.wrap(StreamUtils.copyToByteArray(gzipInputStream));
        } catch (IOException e) {
            throw new DecodingException("Couldn't unzip data", e);
        }
    }
}
