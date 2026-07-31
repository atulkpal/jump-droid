# Jump Droid — OpenCode Skills Integration

This file configures the **skill-driven execution model** for the Jump Droid project.
Skills are located in `skills/<skill-name>/SKILL.md` — 24 curated workflows that guide agent behavior.

---

## Core Rules

- If a task matches a skill, the agent MUST invoke it.
- Skills are located in `skills/<skill-name>/SKILL.md`.
- Never implement directly if a skill applies.
- Always follow the skill instructions exactly (do not partially apply them).

---

## Intent → Skill Mapping

The agent should automatically map user intent to skills:

| Intent | Skills |
|--------|--------|
| Feature / new functionality | `spec-driven-development`, then `incremental-implementation`, `test-driven-development` |
| Planning / breakdown | `planning-and-task-breakdown` |
| Bug / failure / unexpected behavior | `debugging-and-error-recovery` |
| Code review | `code-review-and-quality` |
| Refactoring / simplification | `code-simplification` |
| API or interface design | `api-and-interface-design` |
| UI work | `frontend-ui-engineering` |

---

## Lifecycle Mapping

The agent must internally follow this lifecycle:

| Phase | Skill |
|-------|-------|
| DEFINE | `spec-driven-development` |
| PLAN | `planning-and-task-breakdown` |
| BUILD | `incremental-implementation` + `test-driven-development` |
| VERIFY | `debugging-and-error-recovery` |
| REVIEW | `code-review-and-quality` |
| SHIP | `shipping-and-launch` |

---

## Execution Model

For every request:

1. Determine if any skill applies (even 1% chance).
2. Invoke the appropriate skill using the `skill` tool.
3. Follow the skill workflow strictly.
4. Only proceed to implementation after required steps (spec, plan, etc.) are complete.

---

## Anti-Rationalization

The following thoughts are incorrect and must be ignored:

- "This is too small for a skill"
- "I can just quickly implement this"
- "I'll gather context first"

**Correct behavior:** Always check for and use skills first.

---

## Orchestration: Personas, Skills, and Commands

Three composable layers with different jobs:

| Layer | Location | Role |
|-------|----------|------|
| **Skills** | `skills/<name>/SKILL.md` | Workflows with steps and exit criteria. The *how*. Mandatory when an intent matches. |
| **Personas** | `agents/<role>.md` | Roles with a perspective and output format. The *who*. |
| **Slash commands** | `.claude/commands/*.md` | User-facing entry points. The *when*. Orchestration layer. |

Composition rule: **the user (or a slash command) is the orchestrator.** Personas do not invoke other personas. A persona may invoke skills. The only multi-persona pattern endorsed is parallel fan-out with a merge step (e.g., run reviewer + security + test concurrently, synthesize reports).

---

## Available Skills

24 skills are available in `skills/`:

- `api-and-interface-design/`
- `browser-testing-with-devtools/`
- `ci-cd-and-automation/`
- `code-review-and-quality/`
- `code-simplification/`
- `context-engineering/`
- `debugging-and-error-recovery/`
- `deprecation-and-migration/`
- `documentation-and-adrs/`
- `doubt-driven-development/`
- `frontend-ui-engineering/`
- `git-workflow-and-versioning/`
- `idea-refine/`
- `incremental-implementation/`
- `interview-me/`
- `observability-and-instrumentation/`
- `performance-optimization/`
- `planning-and-task-breakdown/`
- `security-and-hardening/`
- `shipping-and-launch/`
- `source-driven-development/`
- `spec-driven-development/`
- `test-driven-development/`
- `using-agent-skills/`
