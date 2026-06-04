CREATE TABLE public.app_config (
                                   key     TEXT PRIMARY KEY,
                                   value   TEXT NOT NULL,
                                   updated_at TIMESTAMPTZ DEFAULT NOW()
);

INSERT INTO public.app_config (key, value) VALUES
    ('current_split_start', '1741168800');