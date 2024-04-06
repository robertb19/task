package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.RandomStringUtils;

public final class TestUtils {

    private TestUtils(){}

    public static String generateRandomCharacterString(final int length) {
        return RandomStringUtils.randomAlphabetic(length);
    }

    public static ObjectMapper objectMapperWithTimeModule() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

}
