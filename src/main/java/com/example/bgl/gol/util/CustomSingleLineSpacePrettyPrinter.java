package com.example.bgl.gol.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;

import java.io.IOException;
import org.springframework.stereotype.Component;

/**
 * Custom PrettyPrinter that formats JSON in a single line with spaces after commas;
 * To fulfill the requirement of having spaces after commas in JSON arrays.
 * Example: {"key1": "value1", "key2": "value2"}
 */
@Component
public class CustomSingleLineSpacePrettyPrinter extends MinimalPrettyPrinter {

    public CustomSingleLineSpacePrettyPrinter() {
        super();
    }

    // This is the key override for your requirement
    @Override
    public void writeArrayValueSeparator(JsonGenerator g) throws IOException {
        g.writeRaw(',');
        g.writeRaw(' ');
    }

    // You can also add a space for object field value separators if needed
    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator g) throws IOException {
        g.writeRaw(':');
        g.writeRaw(' ');
    }
}