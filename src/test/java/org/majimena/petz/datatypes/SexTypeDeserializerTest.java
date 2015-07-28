package org.majimena.petz.datatypes;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by todoken on 2015/07/27.
 */
public class SexTypeDeserializerTest {

    private SexTypeDeserializer sut = new SexTypeDeserializer();

    @Mocked
    private JsonParser jp;

    @Mocked
    private DeserializationContext context;

    @Test
    public void 性別型にデシリアライズできること() throws Exception {
        new NonStrictExpectations() {{
            jp.getCurrentToken();
            result = JsonToken.START_OBJECT;
            jp.readValueAs(HashMap.class);
            Map<String, String> map = new HashMap<>();
            map.put("name", "オス");
            map.put("value", "MALE");
            result = map;
        }};

        SexType result = sut.deserialize(jp, context);
        assertThat(result, is(SexType.MALE));
    }

    @Test(expected = JsonMappingException.class)
    public void 文字列以外の場合は変換できないこと() throws Exception {
        new NonStrictExpectations() {{
            jp.getCurrentToken();
            result = JsonToken.VALUE_NUMBER_INT;
            context.mappingException(LocalDate.class);
            result = new JsonMappingException("test");
        }};

        sut.deserialize(jp, context);
    }
}
