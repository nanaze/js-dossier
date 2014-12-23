package com.github.jleyba.dossier.soy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.template.soy.data.SoyListData;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.data.SoyValue;
import com.google.template.soy.data.restricted.BooleanData;
import com.google.template.soy.data.restricted.IntegerData;
import com.google.template.soy.data.restricted.NullData;
import com.google.template.soy.data.restricted.StringData;
import com.google.template.soy.types.aggregate.ListType;
import com.google.template.soy.types.primitive.BoolType;
import com.google.template.soy.types.primitive.IntType;
import com.google.template.soy.types.primitive.StringType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Map;

@RunWith(JUnit4.class)
public class ProtoMessageSoyTypeTest {

  @Test
  public void testGetName() {
    ProtoMessageSoyType type = ProtoMessageSoyType.get(TestProto.Order.getDescriptor());
    assertEquals("test.Order", type.getName());
    assertEquals("test.Order", type.getNameForBackend(null));
  }

  @Test
  public void testGetName_nestedMessage() {
    ProtoMessageSoyType type = ProtoMessageSoyType.get(
        TestProto.Top.NestedMessage.getDescriptor());

    assertEquals("test.Top.NestedMessage", type.getName());
    assertEquals("test.Top.NestedMessage", type.getNameForBackend(null));
  }

  @Test
  public void testGetFieldType() {
    ProtoMessageSoyType type = ProtoMessageSoyType.get(TestProto.Order.getDescriptor());

    assertNull(type.getFieldType("unknownField"));
    assertNull(type.getFieldType("bool_field"));

    assertEquals(BoolType.getInstance(), type.getFieldType("boolField"));
    assertEquals(IntType.getInstance(), type.getFieldType("intField"));
    assertEquals(StringType.getInstance(), type.getFieldType("stringField"));
    assertEquals(
        ProtoEnumSoyType.get(TestProto.Fruit.getDescriptor()),
        type.getFieldType("fruit"));
    assertEquals(
        ListType.of(IntType.getInstance()),
        type.getFieldType("repeatedInt"));
    assertEquals(
        ListType.of(ProtoEnumSoyType.get(TestProto.Color.getDescriptor())),
        type.getFieldType("repeatedColor"));

    ProtoMessageSoyType topType = ProtoMessageSoyType.get(TestProto.Top.getDescriptor());
    assertEquals(ListType.of(type), topType.getFieldType("order"));
  }

  @Test
  public void testEquality() {
    ProtoMessageSoyType type = ProtoMessageSoyType.get(TestProto.Order.getDescriptor());
    ProtoMessageSoyType topType = ProtoMessageSoyType.get(TestProto.Top.getDescriptor());

    assertEquals(type, type);
    assertNotEquals(type, topType);
    assertEquals(type, ((ListType) topType.getFieldType("order")).getElementType());
    assertTrue(type.isAssignableFrom(type));
    assertFalse(type.isAssignableFrom(topType));
    assertFalse(topType.isAssignableFrom(type));
  }

  @Test
  public void testIsInstance() {
    ProtoMessageSoyType type = ProtoMessageSoyType.get(TestProto.Order.getDescriptor());

    SoyValue orderValue = ProtoMessageSoyType.toSoyValue(TestProto.Order.newBuilder().build());
    SoyValue topValue = ProtoMessageSoyType.toSoyValue(TestProto.Top.newBuilder().build());

    assertTrue(type.isInstance(orderValue));
    assertFalse(type.isInstance(topValue));
  }

  @Test
  public void convertMessageToSoyValue_defaults() {
    SoyValue value = ProtoMessageSoyType.toSoyValue(TestProto.Order.newBuilder().build());
    assertTrue(value instanceof SoyMapData);

    Map<String, ? extends SoyValue> map = ((SoyMapData) value).asResolvedJavaStringMap();
    assertEquals(
        ImmutableSet.of("boolField", "intField", "stringField", "fruit", "color", "repeatedInt",
            "repeatedColor"),
        map.keySet());

    assertEquals(NullData.INSTANCE, map.get("boolField"));
    assertEquals(NullData.INSTANCE, map.get("intField"));
    assertEquals(NullData.INSTANCE, map.get("stringField"));
    assertEquals(NullData.INSTANCE, map.get("fruit"));
    assertEquals(NullData.INSTANCE, map.get("color"));
    assertEquals(0, ((SoyListData) map.get("repeatedInt")).length());
    assertEquals(0, ((SoyListData) map.get("repeatedColor")).length());
  }

  @Test
  public void convertMessageToSoyValue_valuesSet() {
    SoyValue value = ProtoMessageSoyType.toSoyValue(TestProto.Order.newBuilder()
        .setBoolField(true)
        .setIntField(1234)
        .setStringField("hello")
        .setFruit(TestProto.Fruit.ORANGE)
        .setColor(TestProto.Color.RED)
        .addRepeatedInt(678)
        .addRepeatedInt(90)
        .addRepeatedColor(TestProto.Color.RED)
        .addRepeatedColor(TestProto.Color.GREEN)
        .build());
    assertTrue(value instanceof SoyMapData);

    Map<String, ? extends SoyValue> map = ((SoyMapData) value).asResolvedJavaStringMap();
    assertEquals(
        ImmutableSet.of("boolField", "intField", "stringField", "fruit", "color", "repeatedInt",
            "repeatedColor"),
        map.keySet());

    assertEquals(BooleanData.TRUE, map.get("boolField"));
    assertEquals(IntegerData.forValue(1234), map.get("intField"));
    assertEquals(StringData.forValue("hello"), map.get("stringField"));
    assertEquals(ProtoEnumSoyValue.get(TestProto.Fruit.ORANGE), map.get("fruit"));
    assertEquals(ProtoEnumSoyValue.get(TestProto.Color.RED), map.get("color"));

    assertEquals(ImmutableList.of(
            IntegerData.forValue(678),
            IntegerData.forValue(90)),
        ((SoyListData) map.get("repeatedInt")).asResolvedJavaList());
    assertEquals(ImmutableList.of(
            ProtoEnumSoyValue.get(TestProto.Color.RED),
            ProtoEnumSoyValue.get(TestProto.Color.GREEN)),
        ((SoyListData) map.get("repeatedColor")).asResolvedJavaList());
  }
}