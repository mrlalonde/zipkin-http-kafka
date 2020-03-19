# Zipkin HTTP Kafka Bridge

A simple REST service that collects Spans via the /api/v2/spans endpoint as if it was Zipkin

Supports JSON and GZipped JSON span lists.  Writes them to kafka without any validation (as raw bytes).

## Usage of Webflux
Implemented with Webflux so that it scales better.

## Error handling
Maps the CompletableFuture returned from the Kafka producer to an equivalent HTTP Response.

## Partitioning
None, since messages from different traces are combined.  This is consistent with zipkin.