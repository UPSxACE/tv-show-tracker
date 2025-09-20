package com.upsxace.tv_show_tracker.tv_show.mapper;

import com.upsxace.tv_show_tracker.genre.Genre;
import com.upsxace.tv_show_tracker.tv_show.TvShow;
import com.upsxace.tv_show_tracker.tv_show.TvShowGenre;
import com.upsxace.tv_show_tracker.tv_show.graphql.TvShowDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface TvShowMapper {
    @Mapping(source = "tvShowGenres", target = "genres", qualifiedByName = "flattenGenres")
    TvShowDto toDto(TvShow entity);
    List<TvShowDto> toDtos(List<TvShow> entities);

    @Named("flattenGenres")
    default List<Genre> flattenGenres(Set<TvShowGenre> tvShowGenres){
        return tvShowGenres.stream().map(TvShowGenre::getGenre).toList();
    }

    default Page<TvShowDto> toDtoPage(Page<TvShow> page){
        List<TvShowDto> dtoList = toDtos(page.getContent());
        return new PageImpl<>(dtoList, page.getPageable(), page.getTotalElements());
    }
}
