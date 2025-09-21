CREATE TABLE public.users (
	id uuid NOT NULL,
	username varchar NOT NULL,
	"password" varchar NULL,
	email varchar NOT NULL,
	avatar_url varchar NULL,
	created_at timestamp NOT NULL,
	CONSTRAINT users_pk PRIMARY KEY (id),
	CONSTRAINT users_unique UNIQUE (username),
	CONSTRAINT users_unique_1 UNIQUE (email)
);
CREATE INDEX users_created_at_idx ON public.users (created_at);

CREATE TABLE public.user_favorite_tv_shows (
	id bigserial NOT NULL,
	user_id uuid NULL,
	tv_show_id bigint NOT NULL,
	CONSTRAINT user_favorite_tv_shows_pk PRIMARY KEY (id),
	CONSTRAINT user_favorite_tv_shows_unique UNIQUE (user_id,tv_show_id),
	CONSTRAINT user_favorite_tv_shows_users_fk FOREIGN KEY (user_id) REFERENCES public.users(id),
	CONSTRAINT user_favorite_tv_shows_tv_shows_fk FOREIGN KEY (tv_show_id) REFERENCES public.tv_shows(id)
);