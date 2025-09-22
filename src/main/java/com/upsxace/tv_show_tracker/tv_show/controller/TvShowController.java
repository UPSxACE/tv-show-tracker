package com.upsxace.tv_show_tracker.tv_show.controller;

import com.upsxace.tv_show_tracker.actor.entity.ActorCredit;
import com.upsxace.tv_show_tracker.actor.service.ActorService;
import com.upsxace.tv_show_tracker.tv_show.service.TvShowService;
import com.upsxace.tv_show_tracker.tv_show.graphql.AllTvShowsInput;
import com.upsxace.tv_show_tracker.tv_show.graphql.TvShowDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class TvShowController {
    private final TvShowService tvShowService;
    private final ActorService actorService;

    @QueryMapping
    public TvShowDto getTvShow(@Argument Long id){
        return tvShowService.getById(id);
    }

    @QueryMapping
    public Page<TvShowDto> allTvShows(@Argument AllTvShowsInput input){
        return tvShowService.getAll(input);
    }

    @BatchMapping(typeName = "TvShow")
    public List<List<ActorCredit>> actorCredits(List<TvShowDto> tvShows){
        var tvShowIds = tvShows.stream().map(TvShowDto::getId).toList();
        var allActorCredits = actorService.getActorCreditsByTvShowIds(tvShowIds);
        Map<Long, List<ActorCredit>> creditsByTvShowId = allActorCredits.stream()
                .collect(Collectors.groupingBy(ActorCredit::getTvShowId));
        return tvShows.stream().map(t -> creditsByTvShowId.getOrDefault(t.getId(), List.of())).toList();
    }
}
