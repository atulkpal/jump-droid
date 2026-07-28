# Jump Droid Website — Production Compliance & Legal Pages Implementation Plan

This plan outlines the steps to prepare the Jump Droid website for Google Play production release, ensuring legal compliance and visual consistency.

## User Review Required

> [!IMPORTANT]
> The Account Deletion and Privacy Policy pages will be styled to match the "Terminal/Protocol" aesthetic of the existing site, rather than plain HTML documents.

## Proposed Changes

### Website Configuration & Cleanup

#### [MODIFY] [constants.ts](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/lib/constants.ts)
- Update `SOCIAL_LINKS.email` to `hi.jumpdroid@gmail.com`.

#### [MODIFY] [SignalSource.tsx](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/app/transmission/SignalSource.tsx)
- Update the hardcoded email in the copy-to-clipboard handler.

#### [MODIFY] [route.ts](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/app/api/campaign/send-now/route.ts)
- Update default sender email.

---

### Account Deletion Page

#### [MODIFY] [page.tsx](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/app/account-deletion/page.tsx)
- Complete rewrite to match the "Data Handling Protocol" (Privacy) page style.
- Include sections:
    - Overview (Optional accounts)
    - Data to be deleted (Google ID, Cloud save, Profile)
    - Retained data (Legal/Fraud prevention)
    - Deletion process (Email hi.jumpdroid@gmail.com)
    - Guest play (Continue playing without account)

---

### Privacy Policy Update

#### [MODIFY] [page.tsx](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/app/privacy/page.tsx)
- Update content to reflect:
    - Optional Google Sign-In.
    - Cloud save and online features.
    - Direct link to Account Deletion page.
- Replace all occurrences of old email.
- Update "Last Updated" date.

---

### Navigation & Footer

#### [MODIFY] [Footer.tsx](file:///C:/Users/Atul/AndroidStudioProjects/Jump_droid/website/site/app/components/screens/Footer.tsx)
- Add "Account Deletion" link.
- Ensure "Privacy Policy" and "Contact" are clearly accessible.

---

### Production Review

- Search for and remove `TODO`, `Coming Soon`, and placeholder text.
- Verify all legal links resolve correctly.

## Verification Plan

### Automated Tests
- Run `npm run build` in the `website/site` directory to ensure no regressions in routing or types.

### Manual Verification
- Visual inspection of `/privacy` and `/account-deletion` for branding consistency.
- Verify all footer links navigate correctly.
- Check mobile responsiveness for the new pages.
