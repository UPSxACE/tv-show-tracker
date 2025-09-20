CREATE TABLE public.tv_show_genres (
	id bigserial NOT NULL,
	tv_show_id bigint NOT NULL,
	genre_id bigint NOT NULL,
	CONSTRAINT tv_show_genres_pk PRIMARY KEY (id),
	CONSTRAINT tv_show_genres_unique UNIQUE (tv_show_id,genre_id),
	CONSTRAINT tv_show_genres_tv_shows_fk FOREIGN KEY (tv_show_id) REFERENCES public.tv_shows(id),
	CONSTRAINT tv_show_genres_genres_fk FOREIGN KEY (genre_id) REFERENCES public.genres(id)
);
