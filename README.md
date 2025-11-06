# 🐍 Snake Game (Java Swing)

[![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=java&logoColor=white)](https://www.oracle.com/java/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![OOP](https://img.shields.io/badge/Paradigm-OOP-green)]()
[![Data Structures](https://img.shields.io/badge/Data%20Structure-Singly%20Linked%20List-lightgrey)]()

> 🎮 A standalone **Snake Game built in Java Swing**, featuring a **custom Singly Linked List (SLL)** implementation and a **strict OOP design**.  
> No Java Collection Frameworks. 100% manual logic.

---

## 🧩 Overview

This project reimagines the classic *Snake* arcade game to highlight **Object-Oriented Programming (OOP)** and **custom data structure management**.

The snake’s moving body is **manually implemented** as a **Singly Linked List**, avoiding any `java.util` classes such as `LinkedList` or `ArrayList`.

---

## ⚙️ Core Technical Features

### 🚫 No Built-In Collections
All core operations use **custom pointers and nodes**, emphasizing low-level data handling and algorithmic control.

### 🧱 Snake Structure
- Each segment is a `SegmentNode` with:
  - Grid coordinates `(x, y)`
  - A reference to the next node `next`
- The snake’s body is dynamically adjusted using **linked list operations**.

### 🏃 Movement Logic
Each game tick:
1. **Prepends a new head** (`O(1)` operation).  
2. **Traverses and removes the tail** (`O(N)` operation).  

This process produces smooth, continuous motion — mimicking the “slithering” effect.

### 🍎 Growth Mechanism
When food is eaten:
- A **growth flag** is activated.
- On the next move, tail removal is skipped.
- The snake’s length increases by one segment immediately.

---

## 🧠 Architecture (MVC Design)

| Component | Role | Description |
|------------|------|-------------|
| **`AASnakeGame`** | View / Controller | Manages the GUI, keyboard input (WASD / Arrow keys), and game states (*Start*, *Countdown*, *Game Over*). |
| **`CustomSnakeLogic`** | Model | Encapsulates all game logic, including movement, collision detection, food generation, and scoring. |

---

## 🕹️ Gameplay Features

✅ Adjustable difficulty levels  
✅ Dynamic speed scaling based on score  
✅ Smooth animations with Swing timers  
✅ 180° turn prevention  
✅ Clean UI and responsive controls  

**Controls:**
- Move Up: `W` / `↑`
- Move Down: `S` / `↓`
- Move Left: `A` / `←`
- Move Right: `D` / `→`

---

## 👥 Collaborators

| Name                           | GitHub Profile                                       |
| ------------------------------ | ---------------------------------------------------- |
| **Alby Mathew Biju**               | [@albymathewbiju](https://github.com/Cyberspidey617)       |
| **Anusree Babu**           | [@anusreebabu](https://github.com/AnusreeBabu02) |
