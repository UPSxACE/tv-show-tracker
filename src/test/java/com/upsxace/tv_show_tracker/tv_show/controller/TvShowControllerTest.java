package com.upsxace.tv_show_tracker.tv_show.controller;

import com.upsxace.tv_show_tracker.actor.entity.ActorCredit;
import com.upsxace.tv_show_tracker.actor.service.ActorService;
import com.upsxace.tv_show_tracker.tv_show.graphql.AllTvShowsInput;
import com.upsxace.tv_show_tracker.tv_show.graphql.TvShowDto;
import com.upsxace.tv_show_tracker.tv_show.service.TvShowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TvShowControllerTest {

    @InjectMocks
    private TvShowController controller;

    @Mock
    private TvShowService tvShowService;

    @Mock
    private ActorService actorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getTvShow_delegatesToService() {
        TvShowDto dto = new TvShowDto(1L, null, null, null, null, null, null, null, null, null, null, null, null, null);
        when(tvShowService.getById(1L)).thenReturn(dto);

        TvShowDto result = controller.getTvShow(1L);

        assertSame(dto, result);
        verify(tvShowService).getById(1L);
    }

    @Test
    void allTvShows_delegatesToService() {
        AllTvShowsInput input = new AllTvShowsInput(null, null, null);
        Page<TvShowDto> page = Page.empty();
        when(tvShowService.getAll(input)).thenReturn(page);

        Page<TvShowDto> result = controller.allTvShows(input);

        assertSame(page, result);
        verify(tvShowService).getAll(input);
    }

    @Test
    void actorCredits_groupsByTvShowIdCorrectly() {
        TvShowDto tv1 = new TvShowDto(1L, null, null, null, null, null, null, null, null, null, null, null, null, null);
        TvShowDto tv2 = new TvShowDto(2L, null, null, null, null, null, null, null, null, null, null, null, null, null);

        ActorCredit c1 = new ActorCredit(); c1.setTvShowId(1L);
        ActorCredit c2 = new ActorCredit(); c2.setTvShowId(2L);
        ActorCredit c3 = new ActorCredit(); c3.setTvShowId(1L);

        when(actorService.getActorCreditsByTvShowIds(List.of(1L, 2L)))
                .thenReturn(List.of(c1, c2, c3));

        List<List<ActorCredit>> result = controller.actorCredits(List.of(tv1, tv2));

        assertEquals(2, result.size());
        assertEquals(2, result.get(0).size()); // tv1 has c1 and c3
        assertEquals(1, result.get(1).size()); // tv2 has c2
    }
}
