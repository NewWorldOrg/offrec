# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Offrec** is a Discord bot application written in Scala that provides message lifecycle management and archiving functionality. The bot monitors Discord channels, queues messages for deletion with configurable TTL, and processes deletion requests through background daemons.

## Development Commands

### Build Commands
- `sbt compile` - Compile all modules
- `sbt test` - Run all tests
- `sbt assembly` - Create deployable fat JAR
- `sbt bot/run` - Run the bot application locally

### Code Quality
- Code formatting is automatic on compile via scalafmt
- Manual formatting: `sbt scalafmtAll`
- Linting: `sbt scalafix`

### Database Operations
- Set up development database: Use MariaDB with Docker
- Environment file: `docker/dev.env` 
- Database migrations: Located in `db/migration/`
- Flyway configuration: `db/flyway.sample.conf` (copy and configure for local use)

## Architecture

### Multi-Module Project Structure
- **bot/** - Main Discord bot application with daemon processes
- **logging/** - Shared logging utilities module  
- **db/** - Database migrations and configuration

### Core Components
- **HubDaemon** - Main Discord event listener and command processor
- **MessageDeleteDaemon** - Background queue processor (runs every 15 seconds)

### Database Schema
Two primary tables:
- `channel` - Tracks monitored Discord channels (guild_id, channel_id)
- `message_delete_queue` - Manages deletion queue with TTL and status tracking

### Technology Stack
- **Scala 2.13.14** with Cats Effect for functional I/O
- **JDA 5.4.0** for Discord API integration
- **ScalikeJDBC 4.3.0** for database access
- **MariaDB** for data persistence
- **Circe** for JSON processing

## Configuration

### Environment Variables
- `OFFREC_DISCORD_TOKEN` - Discord bot token (required)
- Database connection configured via application.conf

### Key Files
- `bot/src/main/scala/offrec/bot/Main.scala` - Application entry point
- `bot/src/main/resources/reference.conf` - Application configuration
- `project/Dependencies.scala` - Dependency management

## Development Notes

- Uses functional programming patterns with Cats Effect
- Fork is enabled for bot module to handle Discord connections properly
- Assembly merge strategy configured for fat JAR deployment
- Semantic DB enabled for scalafix integration
- Japanese documentation exists in `docs/index.md` for database design
