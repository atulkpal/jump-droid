export const metadata = {
  title: "Account Deletion | Jump Droid",
};

export default function AccountDeletionPage() {
  return (
    <main className="mx-auto max-w-3xl px-6 py-16">
      <h1 className="text-4xl font-bold mb-6">
        Account Deletion
      </h1>

      <p className="mb-6">
        Jump Droid can be played without creating an account. Connecting your
        Google account is completely optional.
      </p>

      <p className="mb-6">
        If you have connected your Google account and would like your account
        and associated data deleted, please email us.
      </p>

      <h2 className="text-2xl font-semibold mt-8 mb-4">
        Email
      </h2>

      <p>
        <strong>hi.jumpdroid@gmail.com</strong>
      </p>

      <p className="mt-2">
        Subject: <strong>Account Deletion Request</strong>
      </p>

      <h2 className="text-2xl font-semibold mt-10 mb-4">
        Data that will be deleted
      </h2>

      <ul className="list-disc pl-6 space-y-2">
        <li>Connected account information</li>
        <li>Cloud save data</li>
        <li>Player profile</li>
        <li>Associated online data</li>
      </ul>

      <h2 className="text-2xl font-semibold mt-10 mb-4">
        Processing time
      </h2>

      <p>
        We will process verified account deletion requests within 30 days.
      </p>

      <p className="mt-8">
        For more information, please see our Privacy Policy.
      </p>
    </main>
  );
}