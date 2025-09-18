package com.upsxace.tv_show_tracker.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    public List<Genre> getAll(){
        return genreRepository.findAll();
    }
}
