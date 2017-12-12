package com.faendir.acra.mongod;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @author Lukas
 * @since 13.05.2017
 */
@Configuration
public class MongoConfiguration {

    @NotNull
    @Bean
    public MappingMongoConverter mongoConverter(@NotNull MongoDbFactory mongoFactory, @NotNull MongoMappingContext mongoMappingContext) throws Exception {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoFactory);
        MappingMongoConverter mongoConverter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
        mongoConverter.setMapKeyDotReplacement("%&&%");
        mongoConverter.setCustomConversions(new MongoCustomConversions(Arrays.asList(new StacktraceElementReadConverter(), new StacktraceElementWriteConverter())));
        mongoConverter.afterPropertiesSet();
        return mongoConverter;
    }

    @NotNull
    @Bean
    public GridFsTemplate gridFsTemplate(@NotNull MongoDbFactory mongoDbFactory, @NotNull MappingMongoConverter mappingMongoConverter) {
        return new GridFsTemplate(mongoDbFactory, mappingMongoConverter);
    }

    private static class StacktraceElementReadConverter implements Converter<DBObject, StackTraceElement> {

        @Nullable
        @Override
        public StackTraceElement convert(@NotNull DBObject source) {
            try {
                StackTraceElement element = new StackTraceElement("","","", -1);
                for (Field field : StackTraceElement.class.getDeclaredFields()) {
                    field.setAccessible(true);
                    field.set(element, source.get(field.getName()));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static class StacktraceElementWriteConverter implements Converter<StackTraceElement, DBObject> {

        @Nullable
        @Override
        public DBObject convert(@NotNull StackTraceElement source) {
            try {
                BasicDBObject dbObject = new BasicDBObject();
                for (Field field : StackTraceElement.class.getDeclaredFields()) {
                    field.setAccessible(true);
                    dbObject = dbObject.append(field.getName(), field.get(source));
                }
                return dbObject;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
