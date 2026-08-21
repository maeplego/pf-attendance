import { NextResponse } from "next/server";

import { getAttendanceSession } from "@/lib/session";

export async function GET() {
  const session = await getAttendanceSession();
  return NextResponse.json({
    oidc: session.oidc,
    loggedIn: session.loggedIn,
    sub: session.sub,
    displayName: session.displayName,
    devMode: session.devMode,
    orgId: session.orgId,
    organizations: session.organizations,
  });
}
