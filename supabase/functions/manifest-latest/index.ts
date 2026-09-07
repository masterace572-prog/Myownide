// Forge IDE: manifest-latest edge function.
//
// Returns the latest signed toolchain manifest. The manifest endpoint should be
// cached by a CDN in front of Supabase to keep egress low on the free tier.

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const CACHE_SECONDS = 3600;

Deno.serve(async (_req) => {
  const supabase = createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
  );

  const { data, error } = await supabase
    .from("toolchain_manifests")
    .select("version, published_at, manifest, signature")
    .order("published_at", { ascending: false })
    .limit(1)
    .single();

  if (error || !data) {
    return Response.json({ error: "No manifest published" }, { status: 404 });
  }

  return new Response(JSON.stringify(data), {
    status: 200,
    headers: {
      "Content-Type": "application/json",
      "Cache-Control": `public, max-age=${CACHE_SECONDS}`,
    },
  });
});
