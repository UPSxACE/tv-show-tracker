package com.upsxace.tv_show_tracker.common.app_property;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppPropertyService {
    private final AppPropertyRepository appPropertyRepository;

    @Transactional
    public void upsertProperty(String key, String value){
        var model = AppProperty.builder().key(key).build();

        var property = appPropertyRepository.findOne(
                Example.of(AppProperty.builder().key(key).build())
        ).orElse(model);
        property.setValue(value);
        appPropertyRepository.save(property);
    }

    public Optional<String> readProperty(String key) {
        return appPropertyRepository
                .findOne(
                    Example.of(AppProperty.builder().key(key).build())
                )
                .map(AppProperty::getValue);
    }
}
