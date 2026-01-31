# 🎮 Level Up Life - RPG Habit Tracker

**Android app that turns habits into quests. Complete tasks → earn XP → level up your character. Inspired by Solo Leveling.**

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)] [![Java](https://img.shields.io/badge/Language-Java-orange.svg)] [![Status](https://img.shields.io/badge/Status-WIP-yellow.svg)] [![License](https://img.shields.io/badge/License-MIT-blue.svg)]

## What it does

Daily tasks become RPG quests. Gym? +Strength. Running? +Agility. Reading? +Intelligence. See your character grow as you get shit done.

**Why?** Backend dev who loves anime/gaming. Regular habit apps bored me. This one feels like playing Dark Souls with my to-do list.

## MVP Features (Week 1-4)

✅ **Week 1 DONE**: Tasks CRUD + XP system  
⏳ **Week 2**: Character screen + 4 attributes (STR/AGI/INT/CHA)  
⏳ **Week 3**: Daily quests + gold currency  
⏳ **Week 4**: Streaks + achievements + Play Store beta  

**Future**: Cloud sync, Telegram bot, multiplayer guilds, iOS (KMM?)

## Tech Stack

Frontend: Java 11 + Android Studio (API 24+)
Data: Room Database (SQLite)
Architecture: MVVM + LiveData + Repository
UI: Material Design 3 + RecyclerView
Backend (future): Spring Boot + PostgreSQL

Plans after MVP
 Play Store launch (beta)

 Telegram bot (shared business logic)

 Spring Boot backend (cloud sync)

 Multiplayer guilds/leaderboards

 iOS port (KMM or Swift rewrite)

 📱 Features (v0.1.0 MVP)
Task Management
CRUD Operations: Create, Read, Update, and Delete tasks seamlessly

Task Completion: CheckBox toggle with instant LiveData updates

Swipe to Delete: Swipe right to remove tasks with Snackbar UNDO action

Long Press to Edit: Intuitive gesture to modify existing tasks

Progression System
XP Tracking: Displays total experience points earned from completed tasks

Player Stats Header: Shows Total XP and completion counter (Completed X/Y)

Rewards: Each task grants XP (1-999 range) and gold

UI/UX
Material Design: Modern interface with dialogs, FAB, TextInputLayout, and Snackbar

Task Creation Dialog: Input validation (default title "New Task", XP clamped to 1-999)

Task Edit Dialog: Pre-filled fields for seamless editing

Color Scheme: Purple accent (#BB86FC) for gamification elements
