package com.upsxace.tv_show_tracker.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genreService;

    @QueryMapping(name = "allGenres")
    public List<Genre> allGenres(){
        return genreService.getAll();
    }
}
