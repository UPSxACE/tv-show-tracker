package com.upsxace.tv_show_tracker.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for handling genre-related operations, including mapping external TMDB IDs to internal database IDs.
 */
@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    /** Cache mapping TMDB IDs to database IDs to avoid repeated database lookups */
    private final Map<Long, Long> tmdbToDbIdMap = new HashMap<>();

    /**
     * Retrieves the internal database ID for a given TMDB ID, caching the result.
     *
     * @param tmdbId The external TMDB genre ID
     * @return The corresponding internal database ID
     * @throws IllegalStateException if the TMDB ID does not exist in the database
     */
    private Long getDbId(Long tmdbId) {
        if (tmdbToDbIdMap.containsKey(tmdbId)) {
            return tmdbToDbIdMap.get(tmdbId);
        }

        var genre = genreRepository.findByTmdbId(tmdbId)
                .orElseThrow(IllegalStateException::new);

        tmdbToDbIdMap.put(genre.getTmdbId(), genre.getId());
        return genre.getId();
    }

    /**
     * Maps a list of TMDB genre IDs to their corresponding database IDs.
     *
     * @param tmdbIds List of TMDB genre IDs
     * @return List of internal database genre IDs
     */
    public List<Long> mapToDbId(List<Long> tmdbIds) {
        return tmdbIds.stream()
                .map(this::getDbId)
                .toList();
    }

    /**
     * Retrieves all genres from the database.
     *
     * @return List of all Genre entities
     */
    public List<Genre> getAll() {
        return genreRepository.findAll();
    }
}
