# Set Card Game

Welcome to the README for the **Set Card Game** project! This Java-based card game is a digital adaptation of the popular card game called Set. The project incorporates threads and concurrent programming techniques to allow for both single-player mode against a computer bot and a two-player mode where players can compete using keyboard inputs.

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Thread and Synchronization](#thread-and-synchronization)
- [Single-Player Mode](#single-player-mode)
- [Multiplayer Mode](#multiplayer-mode)

## Introduction

The **Set Card Game** is a Java application that brings the classic Set card game to the digital realm. The game involves identifying sets of three cards that satisfy a specific set of conditions. The project goes beyond just implementing the game logic – it also employs threads and synchronization to manage concurrent gameplay scenarios, enabling both single-player and multiplayer modes.

## Features

- Play the classic Set card game on your computer.
- Enjoy the game in single-player mode against a computer bot.
- Compete with a friend in multiplayer mode using keyboard inputs.
- Utilize Java threads and synchronization techniques for smooth concurrent gameplay.

## Installation

1. Ensure you have Java Development Kit (JDK) installed on your system.
2. Clone this repository to your local machine using:
   ```
   git clone https://github.com/BacharMetar/Set_Card_Game.git
   ```
3. Navigate to the project directory:
   ```
   cd Set_Card_Game
   ```

## Usage

run the game using the following commands:

```bash
java -jar Set_Card_Game.jar
```

## Thread and Synchronization

The project demonstrates a strong understanding of threads and synchronization in Java. This is achieved through careful design of thread management, ensuring that concurrent actions do not lead to conflicts or unexpected behavior. The game's logic is encapsulated within synchronized blocks to prevent race conditions and ensure consistent gameplay.

## Single-Player Mode

In single-player mode, you can challenge a computer bot to a game of Set. The bot is programmed to make intelligent decisions based on the available cards and the rules of the game. This mode serves as a great practice ground to improve your Set game skills.

## Multiplayer Mode

The multiplayer mode allows two players to compete against each other on the same computer. Player 1 and Player 2 can take turns selecting sets using their respective keyboard inputs. The game manages the synchronization of turns to prevent conflicts and ensure a fair gameplay experience.

---
