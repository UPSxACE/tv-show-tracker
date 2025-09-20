package com.upsxace.tv_show_tracker.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;
    private final Map<Long, Long> tmdbToDbIdMap = new HashMap<>();

    private Long getDbId(Long tmdbId){
        if(tmdbToDbIdMap.containsKey(tmdbId))
            return tmdbToDbIdMap.get(tmdbId);

        var genre = genreRepository.findByTmdbId(tmdbId).orElseThrow(IllegalStateException::new);
        tmdbToDbIdMap.put(genre.getTmdbId(), genre.getId());
        return genre.getId();
    }

    public List<Long> mapToDbId(List<Long> tmdbIds){
        return tmdbIds.stream().map(this::getDbId).toList();
    }

    public List<Genre> getAll(){
        return genreRepository.findAll();
    }
}
