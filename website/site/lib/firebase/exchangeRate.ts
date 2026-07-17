const API_URL = "https://api.frankfurter.app/latest?from=USD&to=INR";

export async function fetchUsdToInr(): Promise<number | null> {
  try {
    const res = await fetch(API_URL);
    if (!res.ok) return null;
    const data = await res.json();
    const rate = data?.rates?.INR;
    return typeof rate === "number" && rate > 0 ? rate : null;
  } catch {
    return null;
  }
}
