package com.upsxace.tv_show_tracker.tv_show.service;

import com.upsxace.tv_show_tracker.data_collector.http.TmdbService;
import com.upsxace.tv_show_tracker.tv_show.entity.TvShow;
import com.upsxace.tv_show_tracker.tv_show.graphql.AllTvShowsFilterInput;
import com.upsxace.tv_show_tracker.tv_show.graphql.AllTvShowsInput;
import com.upsxace.tv_show_tracker.tv_show.graphql.TvShowDto;
import com.upsxace.tv_show_tracker.tv_show.mapper.TvShowMapper;
import com.upsxace.tv_show_tracker.tv_show.repository.TvShowRepository;
import com.upsxace.tv_show_tracker.user.repository.UserFavoriteTvShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TvShowServiceTest {

    @InjectMocks
    private TvShowService tvShowService;

    @Mock
    private TvShowRepository tvShowRepository;
    @Mock
    private TvShowMapper tvShowMapper;
    @Mock
    private TmdbService tmdbService;
    @Mock
    private UserFavoriteTvShowRepository userFavoriteTvShowRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAll_withGenreFilter_returnsMappedDtos() {
        AllTvShowsInput input = new AllTvShowsInput(new AllTvShowsFilterInput(1L), null, null);
        Pageable pageable = PageRequest.of(0, 20);

        Page<Long> idsPage = new PageImpl<>(List.of(1L, 2L), pageable, 2);
        when(tvShowRepository.findAllIdsByGenreId(any(), eq(1L))).thenReturn(idsPage);

        TvShow tv1 = new TvShow();
        TvShow tv2 = new TvShow();
        tv1.setSeasons(Collections.emptyList());
        tv2.setSeasons(Collections.emptyList());
        when(tvShowRepository.findAllByIdIn(anyList(), any(Sort.class))).thenReturn(List.of(tv1, tv2));

        TvShowDto dto1 = new TvShowDto(1L, null, null, null, null, null, null, null, null, null, null, null, null, null);
        TvShowDto dto2 = new TvShowDto(2L, null, null, null, null, null, null, null, null, null, null, null, null, null);
        when(tvShowMapper.toDtos(anyList())).thenReturn(List.of(dto1, dto2));

        Page<TvShowDto> result = tvShowService.getAll(input);

        assertEquals(2, result.getContent().size());
        verify(tvShowRepository).findAllIdsByGenreId(any(), eq(1L));
        verify(tvShowMapper).toDtos(anyList());
    }

    @Test
    void getAll_noGenreFilter_returnsMappedDtos() {
        AllTvShowsInput input = new AllTvShowsInput(null, null, null);

        Pageable pageable = PageRequest.of(0, 20);
        Page<Long> idsPage = new PageImpl<>(List.of(1L), pageable, 1);
        when(tvShowRepository.findAllIds(pageable)).thenReturn(idsPage);

        TvShow tv1 = new TvShow();
        tv1.setSeasons(Collections.emptyList());
        when(tvShowRepository.findAllByIdIn(anyList(), any(Sort.class))).thenReturn(List.of(tv1));

        TvShowDto dto1 = new TvShowDto(1L, null, null, null, null, null, null, null, null, null, null, null, null, null);
        when(tvShowMapper.toDtos(anyList())).thenReturn(List.of(dto1));

        Page<TvShowDto> result = tvShowService.getAll(input);

        assertEquals(1, result.getContent().size());
        verify(tvShowRepository).findAllIds(pageable);
        verify(tvShowMapper).toDtos(anyList());
    }

    @Test
    void getById_existingTvShow_returnsDtoAndInitializesSeasons() {
        TvShow tvShow = new TvShow();
        tvShow.setSeasons(Collections.emptyList());
        tvShow.setActorCredits(Collections.emptyList());
        when(tvShowRepository.findById(1L)).thenReturn(Optional.of(tvShow));

        TvShowDto dto = new TvShowDto(1L, null, null, null, null, null, null, null, null, null, null, null, null, null);
        when(tvShowMapper.toDto(tvShow)).thenReturn(dto);

        TvShowDto result = tvShowService.getById(1L);

        assertNotNull(result);
        verify(tmdbService).fillTvShowCredits(tvShow);
        verify(tvShowMapper).toDto(tvShow);
    }

    @Test
    void getById_nonExistingTvShow_returnsNull() {
        when(tvShowRepository.findById(1L)).thenReturn(Optional.empty());
        assertNull(tvShowService.getById(1L));
    }

    @Test
    void getAllById_returnsDtos() {
        TvShow tv1 = new TvShow();
        TvShow tv2 = new TvShow();
        when(tvShowRepository.findAllByIdIn(List.of(1L, 2L))).thenReturn(List.of(tv1, tv2));

        TvShowDto dto1 = new TvShowDto(1L, null, null, null, null, null, null, null, null, null, null, null, null, null);
        TvShowDto dto2 = new TvShowDto(2L, null, null, null, null, null, null, null, null, null, null, null, null, null);
        when(tvShowMapper.toDtos(List.of(tv1, tv2))).thenReturn(List.of(dto1, dto2));

        List<TvShowDto> result = tvShowService.getAllById(List.of(1L, 2L));

        assertEquals(2, result.size());
        verify(tvShowMapper).toDtos(anyList());
    }
}
