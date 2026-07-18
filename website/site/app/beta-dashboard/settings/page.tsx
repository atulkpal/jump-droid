import { redirect } from "next/navigation";

export default function SettingsPage() {
  redirect("/beta-dashboard/settings/email-accounts");
}
