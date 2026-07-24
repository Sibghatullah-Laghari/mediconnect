# Setup Guide – MediConnect

This guide describes how to configure and run the MediConnect project for local development using Docker or a manual setup.

---

> **📌 Quick start (Docker):** If you're in a hurry, jump straight to [Running with Docker](#-running-with-docker-recommended) – it takes just a few commands to get everything up and running.

---

## 🛠️ Prerequisites

Before starting, ensure the following software is installed on your system:

* **Docker & Docker Compose** (recommended for a quick setup).
* **Java Development Kit (JDK 21)** for running the backend locally
* **Node.js 18 or later** with **npm** for the frontend
* **PostgreSQL 16** if you intend to use a local database instead of Docker
.
> 💡 *Tip:* Use `java -version` and `node -v` to verify your installed versions before proceeding.

---

## 🚀 Running with Docker (Recommended)

Docker offers the fastest and most convenient approach to launch the complete application.

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/mediconnect.git
cd mediconnect.
..
