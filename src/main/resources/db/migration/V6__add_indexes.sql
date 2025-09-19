ALTER TABLE public.actor_credits ALTER COLUMN tmdb_id TYPE varchar USING tmdb_id::varchar;
ALTER TABLE public.actors DROP COLUMN "character";

CREATE INDEX actors_name_idx ON public.actors ("name");
CREATE INDEX tv_shows_first_air_date_idx ON public.tv_shows (first_air_date);
CREATE INDEX tv_shows_name_idx ON public.tv_shows ("name");
