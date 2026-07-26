export function todayStr() {
  return new Date().toISOString().slice(0, 10);
}

export function monthAgo() {
  const d = new Date();
  d.setMonth(d.getMonth() - 1);
  return d.toISOString().slice(0, 10);
}
