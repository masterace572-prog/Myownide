// Forge IDE: delete-account edge function.
//
// Called by the client with the user's access token in the Authorization
// header. Uses the service role to cascade-delete user data and the user row.
//
// This must run server-side. The user is only ever sent to Supabase through
// the anon key from the client; the service role lives in the Supabase project
// environment, not in the app.

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

Deno.serve(async (req) => {
  if (req.method !== "POST") {
    return new Response("Method Not Allowed", { status: 405 });
  }

  const authHeader = req.headers.get("Authorization");
  const token = authHeader?.replace(/^Bearer\s+/i, "");
  if (!token) {
    return Response.json({ error: "Missing access token" }, { status: 401 });
  }

  const supabase = createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
    {
      global: {
        headers: { Authorization: `Bearer ${token}` },
      },
    },
  );

  const { data: { user }, error: userError } = await supabase.auth.getUser(token);
  if (userError || !user) {
    return Response.json({ error: "Invalid access token" }, { status: 401 });
  }

  const userId = user.id;

  // Remove storage objects owned by the user (avatars).
  const { data: avatar, error: avatarError } = await supabase
    .storage
    .from("avatars")
    .list(userId);

  if (!avatarError && avatar && avatar.length > 0) {
    await supabase.storage.from("avatars").remove(avatar.map((f) => `${userId}/${f.name}`));
  }

  // Delete app data. RLS-aware client calls would normally restrict these to
  // the caller, but this function runs with the service role so it can clean
  // up all related rows regardless of policies.
  const tables = [
    "user_settings",
    "recent_projects",
    "keymaps",
    "snippets",
    "device_installs",
  ];

  for (const table of tables) {
    await supabase.from(table).delete().eq("user_id", userId);
  }

  // Delete the account. profiles and all owned rows cascade on delete.
  const { error: deleteError } = await supabase.auth.admin.deleteUser(userId);
  if (deleteError) {
    return Response.json({ error: deleteError.message }, { status: 500 });
  }

  return Response.json({ ok: true });
});
