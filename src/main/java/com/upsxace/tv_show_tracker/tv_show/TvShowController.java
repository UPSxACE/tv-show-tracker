package com.upsxace.tv_show_tracker.tv_show;

import com.upsxace.tv_show_tracker.tv_show.graphql.AllTvShowsInput;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class TvShowController {
    private final TvShowService tvShowService;

    @QueryMapping
    public Page<TvShow> allTvShows(@Argument AllTvShowsInput input){
        return tvShowService.getAll(input);
    }
}
