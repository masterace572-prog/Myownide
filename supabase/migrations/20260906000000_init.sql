-- Forge IDE: Supabase schema
-- Run this in the Supabase SQL Editor or via the Supabase CLI.
-- All tables use RLS with auth.uid() = <owner column>.

-- Extensions
create extension if not exists "pgcrypto";

-- profiles -------------------------------------------------------------
create table if not exists public.profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    display_name text,
    avatar_url text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

alter table public.profiles enable row level security;

create policy "profiles_select_own" on public.profiles
    for select using (auth.uid() = id);

create policy "profiles_update_own" on public.profiles
    for update using (auth.uid() = id);

-- auto-create profile on signup
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer set search_path = public
as $$
begin
    insert into public.profiles (id, display_name, avatar_url)
    values (
        new.id,
        coalesce(new.raw_user_meta_data ->> 'full_name', new.raw_user_meta_data ->> 'name', new.email),
        new.raw_user_meta_data ->> 'avatar_url'
    )
    on conflict (id) do nothing;
    return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute procedure public.handle_new_user();

-- user_settings ---------------------------------------------------------
create table if not exists public.user_settings (
    user_id uuid primary key references auth.users(id) on delete cascade,
    settings jsonb not null default '{}'::jsonb,
    updated_at timestamptz not null default now()
);

alter table public.user_settings enable row level security;

create policy "user_settings_select_own" on public.user_settings
    for select using (auth.uid() = user_id);

create policy "user_settings_insert_own" on public.user_settings
    for insert with check (auth.uid() = user_id);

create policy "user_settings_update_own" on public.user_settings
    for update using (auth.uid() = user_id);

create policy "user_settings_delete_own" on public.user_settings
    for delete using (auth.uid() = user_id);

-- recent_projects --------------------------------------------------------
create table if not exists public.recent_projects (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    name text not null,
    remote_url text,
    last_opened_at timestamptz not null default now(),
    agp_version text,
    created_at timestamptz not null default now()
);

create index if not exists recent_projects_user_idx on public.recent_projects(user_id);
alter table public.recent_projects enable row level security;

create policy "recent_projects_select_own" on public.recent_projects
    for select using (auth.uid() = user_id);

create policy "recent_projects_insert_own" on public.recent_projects
    for insert with check (auth.uid() = user_id);

create policy "recent_projects_update_own" on public.recent_projects
    for update using (auth.uid() = user_id);

create policy "recent_projects_delete_own" on public.recent_projects
    for delete using (auth.uid() = user_id);

-- keymaps ----------------------------------------------------------------
create table if not exists public.keymaps (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    name text not null,
    bindings jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists keymaps_user_idx on public.keymaps(user_id);
alter table public.keymaps enable row level security;

create policy "keymaps_select_own" on public.keymaps
    for select using (auth.uid() = user_id);

create policy "keymaps_insert_own" on public.keymaps
    for insert with check (auth.uid() = user_id);

create policy "keymaps_update_own" on public.keymaps
    for update using (auth.uid() = user_id);

create policy "keymaps_delete_own" on public.keymaps
    for delete using (auth.uid() = user_id);

-- snippets ---------------------------------------------------------------
create table if not exists public.snippets (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    language text not null,
    title text not null,
    body text not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists snippets_user_idx on public.snippets(user_id);
alter table public.snippets enable row level security;

create policy "snippets_select_own" on public.snippets
    for select using (auth.uid() = user_id);

create policy "snippets_insert_own" on public.snippets
    for insert with check (auth.uid() = user_id);

create policy "snippets_update_own" on public.snippets
    for update using (auth.uid() = user_id);

create policy "snippets_delete_own" on public.snippets
    for delete using (auth.uid() = user_id);

-- device_installs (opt-in diagnostics) -----------------------------------
create table if not exists public.device_installs (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    device_model text,
    os_version text,
    toolchain_versions jsonb,
    last_seen_at timestamptz not null default now()
);

create index if not exists device_installs_user_idx on public.device_installs(user_id);
alter table public.device_installs enable row level security;

create policy "device_installs_select_own" on public.device_installs
    for select using (auth.uid() = user_id);

create policy "device_installs_insert_own" on public.device_installs
    for insert with check (auth.uid() = user_id);

create policy "device_installs_update_own" on public.device_installs
    for update using (auth.uid() = user_id);

create policy "device_installs_delete_own" on public.device_installs
    for delete using (auth.uid() = user_id);

-- toolchain_manifests (public read, service_role write) --------------------
create table if not exists public.toolchain_manifests (
    version text not null primary key,
    published_at timestamptz not null default now(),
    manifest jsonb not null,
    signature text not null
);

alter table public.toolchain_manifests enable row level security;

create policy "toolchain_manifests_public_read" on public.toolchain_manifests
    for select using (true);

-- Storage bucket for avatars (512 KB limit is enforced by the client upload
-- and can be tightened with a storage policy/function if desired).
insert into storage.buckets (id, name, public)
values ('avatars', 'avatars', true)
on conflict (id) do nothing;

create policy "avatars_public_read" on storage.objects
    for select using (bucket_id = 'avatars');

create policy "avatars_owner_write" on storage.objects
    for insert with check (bucket_id = 'avatars' and owner = auth.uid());

create policy "avatars_owner_delete" on storage.objects
    for delete using (bucket_id = 'avatars' and owner = auth.uid());
