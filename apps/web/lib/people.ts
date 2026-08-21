export const DEMO_PEOPLE = [
  { sub: "aoki.haru", label: "青木 陽（一般）", role: "member" },
  { sub: "sato.mei", label: "佐藤 芽衣（上長）", role: "manager" },
  { sub: "kondo.minato", label: "近藤 湊（一般）", role: "member" },
  { sub: "fujii.an", label: "藤井 杏（一般）", role: "member" },
  { sub: "murakami.hayate", label: "村上 颯（一般）", role: "member" },
  { sub: "okada.ritsu", label: "岡田 律（一般）", role: "member" },
  { sub: "nakamura.nagi", label: "中村 凪（一般）", role: "member" },
  { sub: "takahashi.saku", label: "高橋 朔（一般）", role: "member" },
] as const;

/** Asia/Tokyo calendar date YYYY-MM-DD for demo defaults. */
export function tokyoToday(): string {
  return new Intl.DateTimeFormat("en-CA", { timeZone: "Asia/Tokyo" }).format(new Date());
}

export function tokyoYearMonth(): string {
  return tokyoToday().slice(0, 7);
}
