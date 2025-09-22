package com.upsxace.tv_show_tracker.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL controller for genre-related queries.
 */
@Controller
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    /**
     * Retrieves all genres.
     *
     * @return List of all Genre entities
     */
    @QueryMapping
    public List<Genre> allGenres() {
        return genreService.getAll();
    }
}
