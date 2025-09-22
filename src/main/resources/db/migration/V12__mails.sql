CREATE TABLE public.emails (
	id bigserial NOT NULL,
	"type" varchar NOT NULL,
	user_id uuid NOT NULL,
	email varchar NOT NULL,
	success boolean DEFAULT false NOT NULL,
	sent_at timestamp NOT NULL,
	confirmed_at timestamp NULL,
	CONSTRAINT emails_pk PRIMARY KEY (id),
	CONSTRAINT emails_users_fk FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE INDEX emails_type_idx ON public.emails ("type");
CREATE INDEX emails_email_idx ON public.emails (email);

ALTER TABLE public.users ADD email_notifications boolean DEFAULT true NOT NULL;