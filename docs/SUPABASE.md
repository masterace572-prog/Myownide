# Supabase backend

Forge uses Supabase for auth, settings sync, recent-project metadata and
diagnostics. Source code never leaves the device.

## What you must provide

Only two values are needed to build the Android app against a real project:

1. **Project URL** — `https://<your-project-ref>.supabase.co`
2. **anon public key** — the `public` (anon) key from
   Supabase → Project Settings → API.

The **service role key** is **not** needed in the app and should never be
committed or placed in `local.properties`. It is only used inside edge
functions (by Supabase) and by the server/database operator.

## Where to place the values

### Local build

Put them in `local.properties` at the repo root (this file is git-ignored):

```properties
SUPABASE_URL=https://xxxx.supabase.co
SUPABASE_ANON_KEY=eyJ...
```

### GitHub Actions build

Set the repository secrets `SUPABASE_URL` and `SUPABASE_ANON_KEY` (needed
only once the workflow reads them; until then the workflow builds with
placeholder config).

## Create the schema

Open Supabase → SQL Editor and run [`supabase/migrations/20260906000000_init.sql`](../supabase/migrations/20260906000000_init.sql).
This creates:

- `profiles`, `user_settings`, `recent_projects`, `keymaps`, `snippets`,
  `device_installs`, `toolchain_manifests`
- RLS policies scoped to `auth.uid()`
- a trigger that creates a profile on signup
- the public `avatars` storage bucket

## Edge functions

- [`delete-account`](../supabase/functions/delete-account/index.ts):
  server-side cascade delete of user data + storage. Requires the
  `SUPABASE_SERVICE_ROLE_KEY` env var in the edge function.
- [`manifest-latest`](../supabase/functions/manifest-latest/index.ts):
  returns the latest signed toolchain manifest. Uses the service role under the
  hood; the client still calls it with the anon key.

Deploy with:

```bash
supabase functions deploy delete-account
supabase functions deploy manifest-latest
```

## Google sign-in

To enable **Continue with Google**, add the Google OAuth provider in the
Supabase dashboard, then configure the Android client ID in the app
(`credentialManager`) later. This is the one place where a Google OAuth client
ID is needed in addition to the URL and anon key.

## Security rules

- Only the anon key is shipped to devices.
- Edge functions receive the anon key, not the service role key.
- All client Postgres access goes through RLS.
- Do not put the service role key in `local.properties`, Gradle, or the repo.
