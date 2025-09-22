package com.upsxace.tv_show_tracker.common.app_property;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for managing application properties stored in the database.
 * Provides methods to read and upsert key-value properties.
 */
@Service
@RequiredArgsConstructor
public class AppPropertyService {
    private final AppPropertyRepository appPropertyRepository;

    /**
     * Inserts a new property or updates an existing property with the given key and value.
     *
     * @param key the property key
     * @param value the property value
     */
    @Transactional
    public void upsertProperty(String key, String value){
        var model = AppProperty.builder().key(key).build();

        // find existing property by key, or use a new model if none exists
        var property = appPropertyRepository.findOne(
                Example.of(model)
        ).orElse(model);

        property.setValue(value);
        appPropertyRepository.save(property);
    }

    /**
     * Reads the value of a property by its key.
     *
     * @param key the property key
     * @return an Optional containing the property value if present, otherwise empty
     */
    public Optional<String> readProperty(String key) {
        return appPropertyRepository
                .findOne(
                        Example.of(AppProperty.builder().key(key).build())
                )
                .map(AppProperty::getValue);
    }
}
