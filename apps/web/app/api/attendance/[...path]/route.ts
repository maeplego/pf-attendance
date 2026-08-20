import { NextRequest, NextResponse } from "next/server";

import { readCookie } from "@/lib/oidc/cookies";
import { attendanceApiBase, oidcEnabled } from "@/lib/oidc/env";

async function proxy(req: NextRequest, path: string[]) {
  const suffix = path.join("/");
  const url = new URL(`${attendanceApiBase()}/${suffix}`);
  url.search = req.nextUrl.search;
  const headers = new Headers();
  const contentType = req.headers.get("content-type");
  if (contentType) {
    headers.set("content-type", contentType);
  }
  if (oidcEnabled()) {
    const access = await readCookie("rp_access");
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
  const init: RequestInit = { method: req.method, headers, cache: "no-store" };
  if (req.method !== "GET" && req.method !== "HEAD") {
    init.body = await req.arrayBuffer();
  }
  const upstream = await fetch(url, init);
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
