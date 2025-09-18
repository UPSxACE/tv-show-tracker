CREATE TABLE public.app_properties (
	id bigserial NOT NULL,
	"key" varchar NOT NULL,
	value varchar NOT NULL,
	CONSTRAINT app_properties_pk PRIMARY KEY (id),
	CONSTRAINT app_properties_unique UNIQUE ("key")
);

CREATE TABLE public.genres (
	id bigserial NOT NULL,
	tmdb_id bigint NULL,
	"name" varchar NOT NULL,
	CONSTRAINT genres_pk PRIMARY KEY (id)
);
CREATE INDEX genres_tmdb_id_idx ON public.genres (tmdb_id);