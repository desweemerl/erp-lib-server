package com.erp.lib.server.json;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;

public class JacksonObjectMapper extends ObjectMapper {

    public JacksonObjectMapper() {
        AnnotationIntrospector introspector = new JacksonAnnotationIntrospector();
        getDeserializationConfig().setAnnotationIntrospector(introspector);
        getSerializationConfig().setAnnotationIntrospector(introspector);
        super.configure(Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        super.configure(Feature.FAIL_ON_EMPTY_BEANS, false);
        super.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
