package com.kaya.yatang.dto.recipe;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

/**
 * OpenAI가 ingredients/missingIngredients 항목을 객체 대신 문자열로 줄 때가 있어
 * 둘 다 허용한다. (예: "파슬리" → name=파슬리)
 */
public class RecipeIngredientAmountDtoDeserializer extends JsonDeserializer<RecipeIngredientAmountDto> {

    @Override
    public RecipeIngredientAmountDto deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node == null || node.isNull()) {
            return null;
        }
        RecipeIngredientAmountDto dto = new RecipeIngredientAmountDto();
        if (node.isTextual()) {
            dto.setName(node.asText("").trim());
            dto.setAmount("");
            dto.setNote("");
            dto.setSubstitute("");
            return dto;
        }
        if (node.isObject()) {
            dto.setName(text(node, "name"));
            dto.setAmount(text(node, "amount"));
            dto.setNote(text(node, "note"));
            dto.setSubstitute(text(node, "substitute"));
            return dto;
        }
        return null;
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            return "";
        }
        if (v.isNumber() || v.isBoolean()) {
            return v.asText();
        }
        return v.asText("").trim();
    }
}
