package jeroenfl.csvprocessor;
 
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.kstream.internals.KStreamImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.*;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.clients.CommonClientConfigs;
 
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
 
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.common.utils.Bytes;
 

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
 
    public static void main(String[] args) {
        final String inputTopic  = envOrProp("INPUT_TOPIC",  "local-input-topic");
        final String outputTopic = envOrProp("OUTPUT_TOPIC", "local-stream-topic");
        final String updateTopic = envOrProp("UPDATE_TOPIC", "local-updates-only-topic");
 
        final String stateTopic = envOrProp("STATE_TOPIC", "local-state-topic");
 
        final boolean emitSnapshotOnBootstrap = Boolean.parseBoolean("true");
 

        log.info("kafka topic: {}", inputTopic);
 
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, envOrProp("APP_ID", "cos-csv-expander-app"));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, envOrProp("BOOTSTRAP_SERVERS", "localhost:9093"));
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, envOrProp("SSL_TRUSTSTORE_LOCATION", "truststore.jks"));
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, envOrProp("SSL_TRUSTSTORE_PASSWORD", "changeit"));
        props.put("ssl.endpoint.identification.algorithm", "");
 

        // Optional: if you want EOS
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
 
        final S3Client s3 = buildS3ClientFromEnv();
        final ObjectMapper mapper = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
 
        StreamsBuilder builder = new StreamsBuilder();
 
        KStream<String, String> source = builder.stream(
                inputTopic,
                Consumed.with(Serdes.String(), Serdes.String())
        );
 
        // Expand each COS path value into (key=functional key _-joined, value=json of row) records
        KStream<String, String> expanded = source
            .peek((k, v) -> log.info("IN  -> {}", v))
            .flatMap((k, pathValue) -> {
                List<KeyValue<String, String>> out = new ArrayList<>();
                if (pathValue == null || pathValue.isBlank()) return out;
 
                try {
                    S3Location loc = S3Location.parse(pathValue.trim());
                    GetObjectRequest req = GetObjectRequest.builder()
                            .bucket(loc.bucket).key(loc.key).build();
 
                    try (ResponseInputStream<GetObjectResponse> is = s3.getObject(req);
                         BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
 
                        // ---- Header ----
                        String headerLine = reader.readLine();
                        if (headerLine == null) {
                            log.warn("File {} is empty", pathValue);
                            return out;
                        }
                        // Strip BOM if present
                        if (!headerLine.isEmpty() && headerLine.charAt(0) == '\uFEFF') {
                            headerLine = headerLine.substring(1);
                        }
 
                        // Split headers and drop trailing empty headers caused by trailing ';'
                        List<String> headers = new ArrayList<>(Arrays.asList(headerLine.split(";", -1)));
                        while (!headers.isEmpty() && headers.get(headers.size() - 1).isBlank()) {
                            headers.remove(headers.size() - 1);
                        }
                        log.info("Headers: {}", headers);
 
                        final int keyIndex = 10;
                        if (headers.size() <= keyIndex) {
                            log.warn("Header has only {} columns (need at least 3). Skipping {}", headers.size(), pathValue);
                            return out;
                        }
                        String[] ordersKeyHeaders = {"column keys"};
                        ArrayList<Integer> ordersKeyHeadersIdx = new ArrayList<>();
 
                        for (String h : ordersKeyHeaders){
                            ordersKeyHeadersIdx.add(headers.indexOf(h));
                        }
 
                        log.info("--------> header indexes: {}", ordersKeyHeadersIdx);
 
                        // ---- Rows ----
                        String line;
                        int count = 0;
                        while ((line = reader.readLine()) != null) {
                            if (line.isBlank()) continue;
 
                            String[] parts = line.split(";", -1); // keep trailing empties
                            if (parts.length <= keyIndex) continue;
                           
                            ArrayList<String> compositeValues = new ArrayList<>();
                            for (int p: ordersKeyHeadersIdx){
                                compositeValues.add(parts[p]);
                            }
 
                            // String recordKey = parts[keyIndex];
                            String compositeRecordKey = String.join("_", compositeValues);
                            log.info("-----------------> The REAL KEY: {}", compositeRecordKey);
                           
 
                            if (compositeRecordKey == null || compositeRecordKey.isBlank()) continue;
 
                            Map<String, String> valueMap = new LinkedHashMap<>();
                            for (int i = 0; i < headers.size(); i++) {
                                if (i == keyIndex) continue; // exclude key column
                                String h = headers.get(i);
                                if (h == null || h.isBlank()) continue;
                                String v = (i < parts.length) ? parts[i] : "";
                                valueMap.put(h, v);
                            }
                            valueMap.put("input_filename", pathValue.trim());
                            valueMap.put("imported", getCurrentTimestamp());
 
                            String json = mapper.writeValueAsString(valueMap);
                            out.add(KeyValue.pair(compositeRecordKey, json));
                            count++;
                        }
                        log.info("Emitted {} records from {}", count, pathValue);
                    }
                } catch (NoSuchKeyException e) {
                    log.error("COS key not found for path '{}': {}", pathValue, e.getMessage());
                } catch (Exception e) {
                    log.error("Failed to process {}: {}", pathValue, e.toString());
                }
                return out;
            });
 
        KTable<String, String> stateTable = builder.table(
            stateTopic,
            Consumed.with(Serdes.String(), Serdes.String()),
            Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("rows-state-store")
                .withKeySerde(Serdes.String())
                .withValueSerde(Serdes.String())
 
        );
 
       KStream<String, String> updatesOnly = expanded.leftJoin(
            stateTable,
            (newV, oldV) -> {
                if (Objects.equals(newV, oldV)) return null;
                // if (!emitSnapshotOnBootstrap && oldV == null) return null;
 
                // only emit if extractionts is newer
                Long newTs = tsLong(newV, mapper, "ExtractionTS");
                Long oldTs = tsLong(oldV, mapper, "ExtractionTS");
                log.info("--------------------> oldTs: {}, newTs: {}", oldTs, newTs);
 
                oldTs = (oldTs == null) ? 0 : oldTs;
 
                return (newTs > oldTs) ? newV : null;
            }
        ).filter((k, v) -> v != null);
 
        updatesOnly.peek((k, v) -> log.info("UPDATE -> key='{}'", k))
                .to(updateTopic, Produced.with(Serdes.String(), Serdes.String()));
 
        updatesOnly.to(stateTopic, Produced.with(Serdes.String(), Serdes.String()));
 
        expanded.peek((k, v) -> log.info("OUT -> key='{}' value='{}'", k, v))
                .to(outputTopic, Produced.with(Serdes.String(), Serdes.String()));
        // expanded.peek((k, v) -> log.info("OUT -> key='{}' value='{}'", k, v))
        //         .to(updateTopic, Produced.with(Serdes.String(), Serdes.String()));
 
       
 
        Topology topology = builder.build();
        log.info("Topology:\n{}", topology.describe());
 
        KafkaStreams streams = new KafkaStreams(topology, props);
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        streams.start();
    }
    //     KStream<String, String> expanded =
    //             source.peek((k,v) -> log.info("IN: {}", v))
    //             .flatTransform(new CosCsvFlatTransformerSupplier(s3));
 
    //     expanded.peek((k, v) -> log.info("OUT: key: {} value={}", k, v))
    //             .to(outputTopic, Produced.with(Serdes.String(), Serdes.String()));
 
    //     Topology topology = builder.build();
    //     log.info("Topology:\n{}", topology.describe());
 
    //     KafkaStreams streams = new KafkaStreams(topology, props);
    //     Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    //     streams.start();
    // }
 
    static String envOrProp(String key, String def) {
        String v = System.getenv(key);
        return (v != null && !v.isEmpty()) ? v : def;
    }
 
    static S3Client buildS3ClientFromEnv() {
        String endpoint = mustGetEnv("COS_ENDPOINT"); // e.g., https://s3.eu-de.cloud-object-storage.appdomain.cloud
        String region   = envOrDefault("COS_REGION", "eu-fr2");
        String access = mustGetEnv("COS_ACCESS_KEY_ID");
        String secret = mustGetEnv("COS_SECRET_ACCESS_KEY");
        boolean pathStyle = Boolean.parseBoolean(envOrDefault("COS_PATH_STYLE", "false")); // true for some custom endpoints
 
        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(access, secret)))
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(pathStyle).build())
                .build();
    }
 
    static String envOrDefault(String key, String def) {
        String v = System.getenv(key);
        return (v != null && !v.isEmpty()) ? v : def;
    }
 
    static String mustGetEnv(String key) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) {
            throw new IllegalStateException("Missing env var: " + key);
        }
        return v;
    }
 
    /** Transformer that turns each path message into many (key,value) row messages. */
    static class CosCsvFlatTransformerSupplier implements TransformerSupplier<String, String, Iterable<KeyValue<String, String>>> {
        private final S3Client s3;
 
        CosCsvFlatTransformerSupplier(S3Client s3) {
            this.s3 = s3;
        }
 
        @Override
        public Transformer<String, String, Iterable<KeyValue<String, String>>> get() {
            return new CosCsvFlatTransformer(s3);
        }
    }
 
    static class CosCsvFlatTransformer implements Transformer<String, String, Iterable<KeyValue<String, String>>> {
        private final S3Client s3;
        private final ObjectMapper mapper;
 
        CosCsvFlatTransformer(S3Client s3) {
            this.s3 = s3;
            this.mapper = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        }
 
        @Override
        public void init(ProcessorContext context) {}
 
       @Override
        public Iterable<KeyValue<String, String>> transform(String ignoredKey, String pathValue) {
            if (pathValue == null || pathValue.isBlank()) {
                return List.of();
            }
            try {
                S3Location loc = S3Location.parse(pathValue.trim());
 
                GetObjectRequest req = GetObjectRequest.builder()
                        .bucket(loc.bucket)
                        .key(loc.key)
                        .build();
 
                try (ResponseInputStream<GetObjectResponse> is = s3.getObject(req);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
 
                    CSVFormat format = CSVFormat.DEFAULT.builder()
                            .setDelimiter(';')
                            .setSkipHeaderRecord(true)
                            .setTrim(true)
                            .setIgnoreEmptyLines(true)
                            .build();
 
                    CSVParser parser = new CSVParser(reader, format);
 
                    final List<String> headers = parser.getHeaderNames();
                    if (headers == null || headers.isEmpty()) {
                        return List.of();
                    }
                   // Drop a trailing empty column if header name is blank (typical for lines ending with ';')
                    final boolean dropLast = !headers.isEmpty() && headers.get(headers.size() - 1).isBlank();
                    final int keyIndex = 10; // 0-based -> third column OrdId for orders
 
                    List<KeyValue<String, String>> out = new ArrayList<>(1024);
 
                    for (CSVRecord rec : parser) {
                        // Defensive: skip short/empty lines
                        if (rec.size() == 0 || rec.size() <= keyIndex) continue;
 
                        String recordKey = rec.get(keyIndex);
                        log.info("record: {}", recordKey);
                        if (recordKey == null || recordKey.isBlank()) {
                            // skip rows with missing key
                            continue;
                        }
 
                        // Build JSON map excluding the key column and trailing empty column (if any)
                        Map<String, String> valueMap = new LinkedHashMap<>();
                        int lastIdx = headers.size() - 1;
 
                        for (int i = 0; i < headers.size(); i++) {
                            if (i == keyIndex) continue;                 // exclude key column
                            if (dropLast && i == lastIdx) continue;       // drop artificial empty column
 
                            String h = headers.get(i);
                            if (h == null || h.isBlank()) continue;       // ignore blank header names
                            String v = safeGet(rec, i);
                            valueMap.put(h, v);
                        }
 
                        String json = mapper.writeValueAsString(valueMap);
                        out.add(KeyValue.pair(recordKey, json));
                    }
                    parser.close();
                    return out;
                }
            } catch (NoSuchKeyException e) {
                log.error("COS key not found for path '{}': {}", pathValue, e.getMessage());
                return List.of();
            } catch (Exception e) {
                log.error("Failed to process path '{}': {}", pathValue, e.toString());
                return List.of();
            }
        }
 
        private static String safeGet(CSVRecord rec, int idx) {
            try {
                String v = rec.get(idx);
                return v == null ? "" : v;
            } catch (Exception e) {
                return "";
            }
        }
 
        @Override
        public void close() {}
    }
 
    /** Simple parser for s3:// or cos:// URIs, or "bucket/key..." fallback. */
    static class S3Location {
        final String bucket;
        final String key;
 
        private S3Location(String bucket, String key) {
            this.bucket = bucket;
            this.key = key.startsWith("/") ? key.substring(1) : key;
        }
 
        static S3Location parse(String path) {
            String p = path;
            if (p.startsWith("s3://") || p.startsWith("cos://")) {
                String rest = p.substring(p.indexOf("://") + 3);
                int slash = rest.indexOf('/');
                if (slash < 0) throw new IllegalArgumentException("Invalid S3/COS URI (no key): " + path);
                String bucket = rest.substring(0, slash);
                String key = rest.substring(slash + 1);
                return new S3Location(bucket, key);
            }
            // fallback: "bucket/key..."
            int slash = p.indexOf('/');
            if (slash < 0) throw new IllegalArgumentException("Path must be s3://, cos://, or bucket/key: " + path);
            return new S3Location(p.substring(0, slash), p.substring(slash + 1));
        }
    }
 
    static Long tsLong(String json, com.fasterxml.jackson.databind.ObjectMapper mapper, String field) {
        try {
            com.fasterxml.jackson.databind.JsonNode n = mapper.readTree(json).get(field);
            if (n == null || n.isNull()) return null;
            if (n.isNumber()) return n.longValue();
            String s = n.asText().trim();
            if (s.isEmpty()) return null;
            // ensure digits only, then parse
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (ch < '0' || ch > '9') return null;
            }
            return Long.parseLong(s);
        } catch (Exception e) {
            return null;
        }
    }
 
    static String getCurrentTimestamp() {
        // Get the current time in UTC
        ZonedDateTime now = ZonedDateTime.now(java.time.ZoneOffset.UTC);
 
        // Format the time as YYYYmmddHHMMSS
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return now.format(formatter);
 
    }
}
 


