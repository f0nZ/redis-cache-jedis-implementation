package com.bosch.bt.indego.redis.jedis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.args.GeoUnit;
import redis.clients.jedis.params.GeoRadiusParam;
import redis.clients.jedis.resps.GeoRadiusResponse;

public class JedisServiceImpl<T> implements JedisService<T> {

  private final UnifiedJedis client;
  private final ObjectMapper objectMapper;
  private final ObjectMapper objectJsonMapper;
  private static Gson gson;

  public JedisServiceImpl(final String host, final String key, final Boolean ssl) {
    this.client = new UnifiedJedis(HostAndPort.from(host),
        DefaultJedisClientConfig.builder().password(key).ssl(ssl).build());
    gson = new GsonBuilder()
        .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeTypeAdapter())
        .create();
    this.objectMapper = new ObjectMapper(new MessagePackFactory()).registerModule(new JavaTimeModule());
    this.objectJsonMapper = new ObjectMapper().registerModule(new JavaTimeModule());
  }

  //----------------- String -----------------

  @Override
  public String add(final String key, final String value) {
    return client.set(key, value);
  }

  @Override
  public boolean add(final String key, final String value, final int expirationInSeconds) {
    try {
      client.setex(key, expirationInSeconds, value);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public Optional<String> get(final String key) {
    if (key == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(client.get(key));
  }

  @Override
  public Long delete(final String... keys) {
    return client.del(keys);
  }

  // ----------------- Byte -----------------
  @Override
  public String add(final byte[] key, final byte[] value) {
    return client.set(key, value);
  }

  @Override
  public T get(final byte[] key, final Class<T> clazz) throws IOException {
    byte[] bytes = client.get(key);

    return objectMapper.readValue(bytes, clazz);
  }

  // ----------------- JSON -----------------
  @Override
  public boolean jadd(final String key, final T value) {
    validate(key);
    try {
      client.jsonSet(key, gson.toJson(value));
    } catch (final Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public boolean jadd(final String key, final byte[] value) {
    validate(key);
    try {
      client.jsonSet(key, gson.toJson(value));
    } catch (final Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public boolean jadd(final String key, final byte[] value, final long expirationInSeconds) {
    validate(key);
    try {
      Transaction multi = client.multi();
      multi.jsonSet(key, gson.toJson(value));
      multi.expire(key, expirationInSeconds);
      multi.exec();
    } catch (final Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public boolean jadd(final UUID key, final T value) {
    validate(key);
    return jadd(key.toString(), value);
  }

  @Override
  public boolean jadd(final UUID key, final byte[] value) {
    return jadd(key.toString(), value);
  }

  @Override
  public T jget(final UUID key, final Class<T> clazz) {
    validate(key);
    Object o = client.jsonGet(key.toString());
    return gson.fromJson(gson.toJson(o), clazz);
  }

  //----------------- Geo -----------------

  @Override
  public boolean geoAdd(final String key, final GeoCoordinate geoCoordinate, final Map<String, T> value) {
    String serializedValue = serialize(value);
    long result = client.geoadd(key, geoCoordinate.getLongitude(), geoCoordinate.getLatitude(), serializedValue);
    return result == 1;
  }

  @Override
  public boolean geoAdd(final String key, final GeoCoordinate geoCoordinate, final T value, final Integer ttl) {
    try {
      Transaction tx = client.multi();
      String serializedValue = serialize(value);
      tx.geoadd(key, geoCoordinate.getLongitude(), geoCoordinate.getLatitude(), serializedValue);
      tx.expire(key, ttl);
      tx.exec();
    } catch (final Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public SortedMap<String, T> geoSearch(final String key, final GeoCoordinate geoCoordinate, final double radius,
      final Class<T> valueType) {
    List<GeoRadiusResponse> responses = client.georadius(key, geoCoordinate.getLongitude(), geoCoordinate.getLatitude(),
        radius, GeoUnit.KM);

    SortedMap<String, T> resultMap = new TreeMap<>();
    for (GeoRadiusResponse response : responses) {
      String serializedValue = response.getMemberByString();
      Map<String, T> deserializedMap = deserialize(serializedValue, valueType);
      resultMap.putAll(deserializedMap);
    }

    return resultMap;
  }

  @Override
  public List<T> geoSearch(final String key, final GeoCoordinate geoCoordinate, final Double radius,
      final Class<T> valueType, final Integer limit) {

    final GeoRadiusParam param = new GeoRadiusParam().count(limit);

    List<GeoRadiusResponse> responses = client.georadius(key, geoCoordinate.getLongitude(), geoCoordinate.getLatitude(),
        radius, GeoUnit.KM, param);

    List<T> result = new ArrayList<>();
    for (GeoRadiusResponse response : responses) {
      String serializedValue = response.getMemberByString();
      result.add(deserializeToList(serializedValue, valueType));
    }

    return result;
  }

  //----------------- Private -----------------

  private String serialize(final Map<String, T> map) {
    try {
      return objectJsonMapper.writeValueAsString(map);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize map", e);
    }
  }

  private String serialize(final T map) {
    try {
      return objectJsonMapper.writeValueAsString(map);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize map", e);
    }
  }

  private Map<String, T> deserialize(final String serializedValue, final Class<T> valueType) {
    try {
      JavaType type = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, valueType);
      return objectMapper.readValue(serializedValue, type);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to deserialize map", e);
    }
  }

  private T deserializeToList(final String serializedValue, final Class<T> valueType) {
    try {
      return objectMapper.readValue(serializedValue, valueType);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to deserialize map", e);
    }
  }


  private void validate(final Object o) {
    if (o == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }
  }


}


