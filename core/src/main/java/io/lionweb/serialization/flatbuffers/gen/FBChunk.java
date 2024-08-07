// automatically generated by the FlatBuffers compiler, do not modify

package io.lionweb.serialization.flatbuffers.gen;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class FBChunk extends Table {
  public static void ValidateVersion() {
    Constants.FLATBUFFERS_24_3_25();
  }

  public static FBChunk getRootAsFBChunk(ByteBuffer _bb) {
    return getRootAsFBChunk(_bb, new FBChunk());
  }

  public static FBChunk getRootAsFBChunk(ByteBuffer _bb, FBChunk obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
  }

  public void __init(int _i, ByteBuffer _bb) {
    __reset(_i, _bb);
  }

  public FBChunk __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }

  public String serializationFormatVersion() {
    int o = __offset(4);
    return o != 0 ? __string(o + bb_pos) : null;
  }

  public ByteBuffer serializationFormatVersionAsByteBuffer() {
    return __vector_as_bytebuffer(4, 1);
  }

  public ByteBuffer serializationFormatVersionInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 4, 1);
  }

  public io.lionweb.serialization.flatbuffers.gen.FBLanguage languages(int j) {
    return languages(new io.lionweb.serialization.flatbuffers.gen.FBLanguage(), j);
  }

  public io.lionweb.serialization.flatbuffers.gen.FBLanguage languages(
      io.lionweb.serialization.flatbuffers.gen.FBLanguage obj, int j) {
    int o = __offset(6);
    return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null;
  }

  public int languagesLength() {
    int o = __offset(6);
    return o != 0 ? __vector_len(o) : 0;
  }

  public io.lionweb.serialization.flatbuffers.gen.FBLanguage.Vector languagesVector() {
    return languagesVector(new io.lionweb.serialization.flatbuffers.gen.FBLanguage.Vector());
  }

  public io.lionweb.serialization.flatbuffers.gen.FBLanguage.Vector languagesVector(
      io.lionweb.serialization.flatbuffers.gen.FBLanguage.Vector obj) {
    int o = __offset(6);
    return o != 0 ? obj.__assign(__vector(o), 4, bb) : null;
  }

  public io.lionweb.serialization.flatbuffers.gen.FBNode nodes(int j) {
    return nodes(new io.lionweb.serialization.flatbuffers.gen.FBNode(), j);
  }

  public io.lionweb.serialization.flatbuffers.gen.FBNode nodes(
      io.lionweb.serialization.flatbuffers.gen.FBNode obj, int j) {
    int o = __offset(8);
    return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null;
  }

  public int nodesLength() {
    int o = __offset(8);
    return o != 0 ? __vector_len(o) : 0;
  }

  public io.lionweb.serialization.flatbuffers.gen.FBNode.Vector nodesVector() {
    return nodesVector(new io.lionweb.serialization.flatbuffers.gen.FBNode.Vector());
  }

  public io.lionweb.serialization.flatbuffers.gen.FBNode.Vector nodesVector(
      io.lionweb.serialization.flatbuffers.gen.FBNode.Vector obj) {
    int o = __offset(8);
    return o != 0 ? obj.__assign(__vector(o), 4, bb) : null;
  }

  public static int createFBChunk(
      FlatBufferBuilder builder,
      int serializationFormatVersionOffset,
      int languagesOffset,
      int nodesOffset) {
    builder.startTable(3);
    FBChunk.addNodes(builder, nodesOffset);
    FBChunk.addLanguages(builder, languagesOffset);
    FBChunk.addSerializationFormatVersion(builder, serializationFormatVersionOffset);
    return FBChunk.endFBChunk(builder);
  }

  public static void startFBChunk(FlatBufferBuilder builder) {
    builder.startTable(3);
  }

  public static void addSerializationFormatVersion(
      FlatBufferBuilder builder, int serializationFormatVersionOffset) {
    builder.addOffset(0, serializationFormatVersionOffset, 0);
  }

  public static void addLanguages(FlatBufferBuilder builder, int languagesOffset) {
    builder.addOffset(1, languagesOffset, 0);
  }

  public static int createLanguagesVector(FlatBufferBuilder builder, int[] data) {
    builder.startVector(4, data.length, 4);
    for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]);
    return builder.endVector();
  }

  public static void startLanguagesVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(4, numElems, 4);
  }

  public static void addNodes(FlatBufferBuilder builder, int nodesOffset) {
    builder.addOffset(2, nodesOffset, 0);
  }

  public static int createNodesVector(FlatBufferBuilder builder, int[] data) {
    builder.startVector(4, data.length, 4);
    for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]);
    return builder.endVector();
  }

  public static void startNodesVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(4, numElems, 4);
  }

  public static int endFBChunk(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static void finishFBChunkBuffer(FlatBufferBuilder builder, int offset) {
    builder.finish(offset);
  }

  public static void finishSizePrefixedFBChunkBuffer(FlatBufferBuilder builder, int offset) {
    builder.finishSizePrefixed(offset);
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) {
      __reset(_vector, _element_size, _bb);
      return this;
    }

    public FBChunk get(int j) {
      return get(new FBChunk(), j);
    }

    public FBChunk get(FBChunk obj, int j) {
      return obj.__assign(__indirect(__element(j), bb), bb);
    }
  }
}
