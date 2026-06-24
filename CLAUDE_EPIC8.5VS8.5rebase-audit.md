# Changes on epic8.5 but NOT on epic8.5-rebased

Both branches split from 575ee89 (epic7-complete). epic8.5 has 6 implementation commits (plus 2 docs commits) absent from epic8.5-rebased:

| # | Commit | Change | On rebased? | Classification |
|---|--------|--------|-------------|----------------|
| 1 | `b9e8a72` | Delete MissionRow.kt (dead code, 133 lines, zero imports) | No — file still present | Keep |
| 2 | `5cdec23` | Remove Mission.isNew zombie field | No — isNew still at Mission.kt:71 | Keep |
| 3 | `bc19bf7` | Remove unused CeremonyStage values (COMPLETED_TEXT, REWARD_SPAWNED) | No — still 5 values | Keep |
| 4 | `d9a9c14` | Make Mission.checkCompletion() pure (move ceremonyStage side effect to callers) | No | Keep |
| 5 | `26ead44` | Remove MissionType.COMBO + toIcon mapping | No — COMBO still at MissionType.kt:12 | Keep |
| 6 | `5421592` | Extract PowerUpManager from GameScreen.kt (auto-spawn, physics, collection) | No — no PowerUpManager.kt | Reimplement later |

(The 2 docs-only commits — 74c12ea baseline report, 8b55b6c playtest report — are not implementation changes, so excluded per your scope.)

## Rationale

**Keep (commits 1–5)** — the entire Sprint 8.5.1 cleanup. These are exactly the zero-risk, zero-behavior-change items the blueprint defines for Sprint 8.5.1 (FINDING-06, -07, -14, plus COMBO removal). They are already done correctly on epic8.5. Re-deriving them on epic8.5-rebased is pure wasted effort — these commits should be cherry-picked over rather than reimplemented.

**Reimplement later (commit 6)** — PowerUpManager extraction. This is real structural work (Sprint 8.5.2-class extraction), and there's a conflict: the two branches reconstructed EPIC 8's mission/progression layer differently (rebased's afbc562+f1ec85f vs epic8.5's parallel history). PowerUpManager was extracted against epic8.5's version of GameScreen, so it won't cherry-pick cleanly onto rebased's diverged GameScreen. Better to re-extract it on the chosen baseline after the branch identity is settled — don't graft it blindly.

Nothing classified Discard — none of the 6 is wrong or superseded; the split is "5 trivially portable cleanups" vs "1 extraction that needs redoing in context."

---

⚠️ **Important caveat:** This is built from commit metadata + the file-state evidence from the previous audit, not from reading the actual diffs (classifier outage blocked that). The classifications are sound at the commit level, but if you want me to verify line-level content — especially the checkCompletion() caller changes (commit 4) and the PowerUpManager extraction surface (commit 6) — run the Option A command above and I'll confirm against the real diffs. Want to do that?