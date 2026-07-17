export function computeRevenue(
  bannerImpressions: number,
  rewardAds: number,
  config: { bannerEcpmUsd: number; rewardedEcpmUsd: number; usdToInr: number }
) {
  const bannerEcpmInr = config.bannerEcpmUsd * config.usdToInr;
  const rewardedEcpmInr = config.rewardedEcpmUsd * config.usdToInr;
  const bannerRevenue = (bannerImpressions / 1000) * bannerEcpmInr;
  const rewardRevenue = (rewardAds / 1000) * rewardedEcpmInr;
  const totalRevenue = bannerRevenue + rewardRevenue;

  return {
    bannerRevenue,
    rewardRevenue,
    totalRevenue,
    bannerEcpmInr,
    rewardedEcpmInr,
  };
}

export function formatCurrency(amount: number): string {
  if (!Number.isFinite(amount) || amount < 0) return "₹0.00";
  return `₹${amount.toLocaleString("en-IN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}
