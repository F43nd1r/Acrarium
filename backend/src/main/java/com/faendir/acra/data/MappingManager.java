package com.faendir.acra.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Lukas
 * @since 19.05.2017
 */
@Component
public class MappingManager {
    private final MappingRepository mappingRepository;

    @Autowired
    public MappingManager(MappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
    }

    public void addMapping(String app, int version, String mappings) {
        mappingRepository.save(new ProguardMapping(app, version, mappings));
    }

    public ProguardMapping getMapping(String app, int version) {
        return mappingRepository.findOne(new ProguardMapping.MetaData(app, version));
    }

    public List<ProguardMapping> getMappings(String app) {
        return mappingRepository.findAll(Example.of(new ProguardMapping(app, -1, null), ExampleMatcher.matchingAny()));
    }
}
