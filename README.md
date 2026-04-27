# 🎮 Level Up Life

**An Android RPG habit tracker where real-life discipline becomes character progression.**

Complete tasks, earn XP and gold, level up your hero, unlock achievements, and turn everyday consistency into something that feels closer to an RPG grind than a boring checklist app.

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com/)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue.svg)](#-architecture)
[![Database](https://img.shields.io/badge/Database-Room-purple.svg)](#-tech-stack)
[![Status](https://img.shields.io/badge/Status-Active%20WIP-yellow.svg)](#-roadmap)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](#-license)

---

## ⚔️ Concept

Most productivity apps feel sterile.  
They track tasks, but they do not create momentum.

**Level Up Life** is my attempt to turn habits, workouts, reading, coding, and everyday discipline into a progression loop: quests, XP, level-ups, stats, achievements, hero identity, and long-term growth. The current verified build already goes well beyond a simple CRUD prototype and includes the core gameplay loop in working form. [file:140][file:139]

---

## ✨ Features

### Quest-based task system
- Create, edit, complete, and delete tasks. [file:114]
- Filter tasks by `ALL`, `DAILY`, `TODO`, and `HABIT`. [file:114]
- Swipe right to delete with **UNDO** Snackbar. [file:114]
- Long press to edit existing tasks. [file:114]

### RPG progression
- Complete tasks to earn XP and gold. [file:114]
- Level up through a persistent player system with animated XP progress updates. [file:114][file:140]
- Level-up flow includes a dedicated dialog and Konfetti celebration. [file:114][file:139]
- Talent points are awarded on level-up. [file:140]

### Hero system
- Custom hero name support is implemented. [file:140]
- Class selection unlocks at level 10. [file:138][file:116]
- Current classes:
  - **Warrior** — strength-focused progression and class bonus identity. [file:138][file:139]
  - **Mage** — intelligence-focused progression and bonus identity. [file:138][file:139]
  - **Ranger** — consistency-focused progression and daily-oriented bonus identity. [file:138][file:139]

### Daily discipline loop
- Daily reset is scheduled automatically with WorkManager. [file:114][file:139][file:140]
- Missed dailies increase XP penalty up to 50 percent. [file:139][file:140]
- Completed dailies are saved into history for statistics and streak-related tracking. [file:140][file:136]

### Achievements
- 14 achievements are seeded and implemented. [file:116][file:139][file:140]
- Achievements grant bonus XP and gold. [file:140]
- Includes progression, class, economy, and stat-based milestones. [file:116][file:140]

### Shop
- Gold can be spent on gameplay effects. [file:140][file:116]
- Current effects:
  - Remove penalty
  - XP boost
  - HP potion
  - Mana potion
  - Gem pack [file:139][file:140][file:116]

### Reminders and settings
- Per-task reminders are supported and checked periodically in the background. [file:140][file:114]
- Android 13+ notification permission handling is present. [file:114]
- Settings screen includes sound toggle, notifications toggle, and full progress reset. [file:133]

### Statistics
- Weekly completed-task chart is implemented. [file:136]
- Total completed tasks, weekly activity, total XP earned, and best streak are displayed. [file:136]
- Completion breakdown by `DAILY`, `TODO`, and `HABIT` is supported. [file:136]

---

## 🧠 Core Loop

```text
Create Task
   ↓
Complete Task
   ↓
Gain XP + Gold
   ↓
Level Up Hero
   ↓
Earn Talent Points / Unlock Class Identity
   ↓
Stay Consistent With Dailies
   ↓
Avoid XP Penalty / Build History / Unlock Achievements
```

That loop is already visible in the current build through task completion, reward claiming, level-up flow, daily reset, hero progression, achievements, reminders, and statistics. [file:139][file:140][file:114][file:136]

---

## 📱 Screens

Currently implemented and verified screens include: [file:114][file:133][file:136][file:138][file:140]

- Main screen
- Statistics
- Talents
- Achievements
- Shop
- Hero
- Settings
- Onboarding

### Screenshot placeholders
> Replace with real screenshots later.

| Screen | File |
|---|---|
| Main | `docs/screenshots/main.png` |
| Hero | `docs/screenshots/hero.png` |
| Statistics | `docs/screenshots/statistics.png` |
| Achievements | `docs/screenshots/achievements.png` |
| Shop | `docs/screenshots/shop.png` |
| Settings | `docs/screenshots/settings.png` |

---

## 🏗️ Architecture

```text
┌────────────────────┐
│      UI Layer      │
│ Activities/Dialogs │
└─────────┬──────────┘
          ↓
┌────────────────────┐
│    ViewModels      │
│ Task / Player /    │
│ CompletedTask etc. │
└─────────┬──────────┘
          ↓
┌────────────────────┐
│ Repositories / DAO │
└─────────┬──────────┘
          ↓
┌────────────────────┐
│   Room Database    │
│ levelup_database   │
└────────────────────┘
```

The verified stack uses Java, Android SDK, MVVM with `AndroidViewModel`, Room, LiveData observers in `MainActivity`, WorkManager for background jobs, RecyclerView for lists, and SoundPool-based audio feedback. [file:140][file:139]

---

## 🛠️ Tech Stack

- **Language:** Java [file:139]
- **Architecture:** MVVM + Repository + LiveData [file:139][file:140]
- **Database:** Room / SQLite [file:116][file:140]
- **Database name:** `levelup_database` [file:116][file:139]
- **Schema version:** `10` [file:116][file:139]
- **Background jobs:** WorkManager [file:139][file:140]
- **UI:** RecyclerView, dialogs, bottom navigation, Android UI components [file:114]
- **Charts:** MPAndroidChart [file:136]
- **Audio:** SoundPool via `SoundManager` [file:140]

---

## 🗃️ Data Model

Current Room entities: [file:116][file:140]

- `Task`
- `Player`
- `CompletedTask`
- `Achievement`
- `ShopItem`

The migration chain already covers stats, talent points, streak tracking, completed task history, reward claim state, XP penalty, reminders, achievements, shop content, hero name, hero class, and class achievements. [file:139][file:140]

---

## 🧭 Navigation

The main flow currently includes navigation to Statistics, Talents, Achievements, Shop, Hero, and Settings, plus onboarding gate handling on app start. [file:114][file:140]

`MainActivity` also handles task filtering, task creation, task editing, swipe deletion, reminder scheduling, daily reset scheduling, level-up dialog display, and achievement unlock observation. [file:114][file:139][file:140]

---

## 🧪 Current Status

This project is already beyond “basic MVP CRUD”.  
The verified build includes tasks, progression, hero classes, achievements, shop effects, reminders, daily reset, XP penalty, task history, sounds, and statistics. [file:139][file:140]

It is still an active work in progress, mainly in terms of balancing, polish, and architectural cleanup. Dungeon mode is part of the planned direction, but it is **not implemented yet** in the currently verified code. [file:140]

---

## ⚠️ Known Issues

### Double XP penalty
There is a likely verified bug where XP penalty is applied twice: once in `MainActivity` before `addXp(finalXp)`, and again inside repository-level XP handling. [file:139][file:140][file:114]

### Split completion logic
Task completion logic currently exists in both `MainActivity` and `TaskViewModel.toggleTaskCompleted()`, which can create reward inconsistencies and should be unified into a single source of truth. [file:139][file:140]

These issues matter because the reward path now includes class bonus, XP boost, penalty math, completion history, and reward-claim state. [file:139][file:140]

---

## 🗺️ Roadmap

### Near term
- Fix double XP penalty logic. [file:139][file:140]
- Unify task completion into one clear reward pipeline. [file:139][file:140]
- Improve balancing for rewards, class feel, and progression pacing. [file:140]
- Continue polishing UI and overall RPG identity. [file:140]

### Planned
- Dungeon / PvE mode. [file:140]
- Equipment and loot systems. [file:140]
- More combat flavor and stronger class fantasy. [file:140]
- Cloud sync / backend support. [file:140]

---

## 🎨 Direction

The app is meant to feel more like a dark RPG overlay on top of self-improvement than a neutral task manager. The hero system, XP penalties, stat growth, class identity, achievements, and future dungeon ideas all push the project toward a more game-first product direction. [file:138][file:139][file:140]

---

## 📄 License

MIT
