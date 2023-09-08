/*
 * Copyright (c) 2023 - Bosch Security Systems - GmbH
 * Estrada Nacional 109/IC 1 | Zona Industrial de Ovar, Pardala | 3880-728 S. João | Portugal
 * (www.bosch.com)
 *
 * This software is the proprietary information of BOSCH.
 * Use is subject to license terms.
 */

package com.bosch.bt.indego.redis.jedis;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A Gson TypeAdapter for serializing and deserializing ZonedDateTime objects. This class provides custom serialization
 * and deserialization logic for ZonedDateTime objects when using Gson library for JSON parsing and serialization.
 */
public class ZonedDateTimeTypeAdapter extends TypeAdapter<ZonedDateTime> {

  /**
   * The DateTimeFormatter used for serializing and deserializing ZonedDateTime objects. It follows the ISO-8601 format
   * with timezone information.
   */
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

  /**
   * Serializes a ZonedDateTime object into its JSON representation.
   *
   * @param out   The JsonWriter to write the JSON representation to.
   * @param value The ZonedDateTime object to be serialized.
   * @throws IOException If an I/O error occurs during the serialization process.
   */
  @Override
  public void write(JsonWriter out, ZonedDateTime value) throws IOException {
    out.value(value == null ? null : DATE_TIME_FORMATTER.format(value));
  }

  /**
   * Deserializes a JSON representation into a ZonedDateTime object.
   *
   * @param in The JsonReader from which the JSON representation is read.
   * @return The deserialized ZonedDateTime object.
   * @throws IOException If an I/O error occurs during the deserialization process.
   */
  @Override
  public ZonedDateTime read(JsonReader in) throws IOException {
    String stringValue = in.nextString();
    return ZonedDateTime.parse(stringValue, DATE_TIME_FORMATTER);
  }
}

