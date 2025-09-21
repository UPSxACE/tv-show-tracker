ALTER TABLE public.user_favorite_tv_shows ADD favorited_at timestamp NOT NULL;
CREATE INDEX user_favorite_tv_shows_favorited_at_idx ON public.user_favorite_tv_shows (favorited_at);
