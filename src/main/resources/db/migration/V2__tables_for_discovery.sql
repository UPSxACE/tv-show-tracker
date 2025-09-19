CREATE TABLE public.tv_show (
	id bigserial NOT NULL,
	tmdb_id bigint NOT NULL,
	name varchar NOT NULL,
	overview varchar NULL,
	poster_url varchar NOT NULL,
	popularity double precision NOT NULL,
	vote_average double precision NOT NULL,
	number_of_seasons int NOT NULL,
	number_of_episodes int NOT NULL,
	first_air_date date NOT NULL,
	last_air_date date NULL,
	in_production boolean NOT NULL,
	CONSTRAINT tv_show_pk PRIMARY KEY (id)
);
CREATE INDEX tv_show_tmdb_id_idx ON public.tv_show (tmdb_id);
CREATE INDEX tv_show_popularity_idx ON public.tv_show (popularity);
CREATE INDEX tv_show_vote_average_idx ON public.tv_show (vote_average);

CREATE TABLE public.actor (
	id bigserial NOT NULL,
	tmdb_id bigint NOT NULL,
	"name" varchar NOT NULL,
	popularity double precision NOT NULL,
	profile_url varchar NOT NULL,
	"character" varchar NOT NULL,
	CONSTRAINT actor_pk PRIMARY KEY (id)
);
CREATE INDEX actor_tmdb_id_idx ON public.actor (tmdb_id);
CREATE INDEX actor_popularity_idx ON public.actor (popularity);

CREATE TABLE public.actor_credit (
	id bigserial NOT NULL,
	tmdb_id bigint NOT NULL,
	actor_id bigint NOT NULL,
	tv_show_id bigint NULL,
	tv_show_tmdb_id bigint NOT NULL,
	"name" varchar NOT NULL,
	overview varchar NULL,
	popularity double precision NOT NULL,
	"character" varchar NOT NULL,
	first_air_date date NOT NULL,
	first_credit_air_date date NOT NULL,
	CONSTRAINT actor_credit_pk PRIMARY KEY (id),
	CONSTRAINT actor_credit_actor_fk FOREIGN KEY (actor_id) REFERENCES public.actor(id),
	CONSTRAINT actor_credit_tv_show_fk FOREIGN KEY (tv_show_id) REFERENCES public.tv_show(id)
);
CREATE INDEX actor_credit_tmdb_id_idx ON public.actor_credit (tmdb_id);
CREATE INDEX actor_credit_tv_show_tmdb_id_idx ON public.actor_credit (tv_show_tmdb_id);
CREATE INDEX actor_credit_popularity_idx ON public.actor_credit (popularity);
