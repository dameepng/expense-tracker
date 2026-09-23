# Navigation Transition Map & Architecture Audit

## 1. Project Navigation Architecture

- **Navigation Library**: `androidx.navigation:navigation-compose:2.9.8`
- **Compose BOM**: `2026.06.01` (`androidx.compose.animation:animation`, `androidx.compose.material3:material3`)
- **Activity Compose**: `1.13.0`
- **Kotlin**: `2.4.10`
- **Target SDK**: `37` (Compile SDK: `37`, Min SDK: `28`)
- **Architecture Paradigm**: Navigation Compose (Single Activity, Single NavHost with `SharedTransitionLayout`)
- **Predictive Back**: Enabled in manifest (`android:enableOnBackInvokedCallback="true"`), supported natively by Navigation Compose 2.9.8.

---

## 2. Navigation Hierarchy & Route Classification

```text
Root (MainActivity)
 │
 ├── Top-Level Peer Destinations (BottomNavBar / NavigationRail / PermanentDrawer)
 │    ├── [home] Home Screen (Dashboard, Recent Transactions, FABs)
 │    ├── [wallet] Wallet List Screen (Accounts, Balances, Transfers)
 │    ├── [summary] Financial Summary Screen (Charts, Categories, Monthly Report)
 │    └── [profile] User Profile & Settings (Preferences, AI Config, Security)
 │
 ├── Creation & Action Modal (Center Action FAB / Edit)
 │    └── [input?expenseId={expenseId}] Transaction Input Form (Amount, Category, Wallet)
 │
 ├── Hierarchical Child Screens (Parent → Child / Deeper Details)
 │    ├── [summary] → [category_detail/{categoryId}...] Category Breakdown Detail
 │    ├── [home] → [reminder_list] Scheduled Bill Reminders
 │    ├── [profile] → [help_faq] Help & FAQ Documentation
 │    └── [profile] → [privacy_policy] Privacy & Terms
 │
 ├── AI & Media Scanning Flows (Multi-step / Hero flows)
 │    ├── [home/profile] → [ai_input] Natural Language Expense Parser
 │    ├── [home/profile] → [receipt_picker] Receipt Scanner Camera / Gallery
 │    │         ↓ (Shared Element: "receipt_image")
 │    │    [receipt_review] Extracted Receipt Review & Confirmation
 │    └── [home/profile] → [chat] AI Financial Assistant Chatbot
 │
 └── First Run / Standalone
      └── [onboarding] Welcome & Initial Setup
```

---

## 3. Transition Classification & Policy Matrix

| Navigation Flow | Source | Destination | Relationship | Current Transition | Proposed Transition | Rationale |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Top-Level Tab Switch** | `home` ↔ `wallet` ↔ `summary` ↔ `profile` | Peer | Type A | `fadeIn` (250ms, 100ms delay) + `scaleIn` (0.96) / `fadeOut` (150ms) | **Subtle Fade (150ms, 0ms delay)** | Eliminates 100ms dead blank latency. Peer tabs should feel immediate and never bounce or scale. |
| **Transaction Creation / Edit** | `home` / `*` → `input` | Creation Modal | Type F / Modal | `EnterTransition.None` / `ExitTransition.None` (Hard cut) | **Vertical Slide-Up + Fade (200ms)** | Communicates modal creation sheet semantics; eliminates abrupt hard cut while keeping frame budget clean. |
| **Dismiss Input Screen** | `input` → Back | Dismiss Modal | Type F / Modal | `EnterTransition.None` / `ExitTransition.None` (Hard cut) | **Vertical Slide-Down + Fade (150ms)** | Matches natural modal dismiss motion; fully compatible with predictive back gesture. |
| **List → Detail** | `summary` → `category_detail` | Hierarchical Child | Type B | 300ms Shared Axis X + 250ms fade | **Spatial Slide-Start (220ms) + Fade (180ms)** | Crisp, forward spatial motion indicating deeper hierarchy level. Shorter duration respects perceived speed. |
| **Detail → Back** | `category_detail` → `summary` | Reverse Hierarchy | Type B | 300ms Pop Exit + 250ms Pop Enter | **Spatial Slide-End (200ms) + Fade (180ms)** | Reverse of forward motion; linear easing during gesture tracking ensures smooth Predictive Back. |
| **Sub-Settings / Documents** | `profile` → `help_faq` / `privacy_policy` | Hierarchical Child | Type B | 300ms Shared Axis X | **Spatial Slide-Start (220ms) + Fade (180ms)** | Consistent forward navigation standard for secondary screens. |
| **Reminders** | `home` → `reminder_list` | Hierarchical Child | Type B | 300ms Shared Axis X | **Spatial Slide-Start (220ms) + Fade (180ms)** | Clear forward push into reminder management. |
| **Receipt Scanner Flow** | `receipt_picker` → `receipt_review` | Shared Visual Element | Type E | Shared Element (`receipt_image`) + 300ms Slide | **Shared Element + Subtle Fade (200ms)** | Preserves spatial hero image continuity without redundant whole-screen horizontal slide. |
| **AI Input & Chat** | `home` / `profile` → `ai_input` / `chat` | Action Flow | Type B | 300ms Shared Axis X | **Spatial Slide-Start (220ms) + Fade (180ms)** | Responsive entry into conversational AI workflow. |

---

## 4. State & Back Stack Preservation Policy

- **Top-Level Tabs (`onNavigateTo`)**:
  ```kotlin
  navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) {
          saveState = true
      }
      launchSingleTop = true
      restoreState = true
  }
  ```
  - Saves and restores scroll state (`LazyListState`) across `Home`, `Wallet`, `Summary`, and `Profile`.
  - Prevents reloading data or re-running database queries when simply switching between main navigation sections.
