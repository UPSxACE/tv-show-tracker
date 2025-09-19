ALTER TABLE public.tv_shows ALTER COLUMN id TYPE bigint;

CREATE TABLE public.seasons (
	id bigserial NOT NULL,
	tmdb_id bigint NULL,
	season_number int NOT NULL,
	"name" varchar NOT NULL,
	episode_count int NOT NULL,
	air_date date NOT NULL,
	tv_show_id bigint NOT NULL,
	CONSTRAINT seasons_pk PRIMARY KEY (id),
	CONSTRAINT seasons_tv_shows_fk FOREIGN KEY (tv_show_id) REFERENCES public.tv_shows(id)
);
