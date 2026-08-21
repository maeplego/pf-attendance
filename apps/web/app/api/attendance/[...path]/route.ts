import { NextRequest, NextResponse } from "next/server";

import { readCookie, readRequestCookie } from "@/lib/oidc/cookies";
import { attendanceApiBase, internalBase, oidcEnabled } from "@/lib/oidc/env";

async function resolveOrg(req: NextRequest, access?: string): Promise<string> {
  const fromClient = req.headers.get("x-dev-user-org")?.trim();
  if (fromClient) {
    return fromClient;
  }
  if (oidcEnabled() && access) {
    try {
      const res = await fetch(`${internalBase()}/userinfo`, {
        headers: { Authorization: `Bearer ${access}` },
        cache: "no-store",
      });
      if (res.ok) {
        const ui = (await res.json()) as { org_id?: string };
        if (ui.org_id?.trim()) {
          return ui.org_id.trim();
        }
      }
    } catch {
      // fall through to default
    }
  }
  return readRequestCookie(req, "dev_org") || (await readCookie("dev_org")) || "org-demo-a";
}

async function proxy(req: NextRequest, path: string[]) {
  const suffix = path.join("/");
  const url = new URL(`${attendanceApiBase()}/${suffix}`);
  url.search = req.nextUrl.search;
  const headers = new Headers();
  const contentType = req.headers.get("content-type");
  if (contentType) {
    headers.set("content-type", contentType);
  }
  let access: string | undefined;
  if (oidcEnabled()) {
    access = await readCookie("rp_access");
    if (!access) {
      return NextResponse.json({ error: { code: "unauthorized", message: "missing credentials" } }, { status: 401 });
    }
    headers.set("Authorization", `Bearer ${access}`);
  } else {
    const sub = req.headers.get("x-dev-user-sub");
    if (sub) {
      headers.set("X-Dev-User-Sub", sub);
    }
  }
  headers.set("X-Dev-User-Org", await resolveOrg(req, access));
  const init: RequestInit = { method: req.method, headers, cache: "no-store" };
  if (req.method !== "GET" && req.method !== "HEAD") {
    init.body = await req.arrayBuffer();
  }
  const upstream = await fetch(url, init);
  if (upstream.status === 204) {
    return new NextResponse(null, { status: 204 });
  }
  const body = await upstream.arrayBuffer();
  const out = new NextResponse(body, { status: upstream.status });
  const ct = upstream.headers.get("content-type");
  if (ct) {
    out.headers.set("content-type", ct);
  }
  const cd = upstream.headers.get("content-disposition");
  if (cd) {
    out.headers.set("content-disposition", cd);
  }
  return out;
}

type Ctx = { params: Promise<{ path: string[] }> };

export async function GET(req: NextRequest, ctx: Ctx) {
  return proxy(req, (await ctx.params).path);
}

export async function POST(req: NextRequest, ctx: Ctx) {
  return proxy(req, (await ctx.params).path);
}

export async function PUT(req: NextRequest, ctx: Ctx) {
  return proxy(req, (await ctx.params).path);
}

export async function PATCH(req: NextRequest, ctx: Ctx) {
  return proxy(req, (await ctx.params).path);
}

export async function DELETE(req: NextRequest, ctx: Ctx) {
  return proxy(req, (await ctx.params).path);
}
