---
sidebar_position: 20
---

# LionWeb Java Architecture Overview

## Project Structure

LionWeb Java is organized into several modules, each with a specific responsibility:

### Core Module
The core module implements the definition of nodes and languages. It provides:
- Core data structures and interfaces
- JSON serialization/deserialization
- Basic validation and processing capabilities

### Repository Client Module
The repository client module provides:
- Client-side implementation of the LionWeb repository protocol
- Support for delta protocol
- Integration with LionWeb repositories

### Extensions module
Capabilities that, while not required by the LionWeb specs, proved to be useful.
For example, serialization and deserialization in ProtoBuf and FlatBuffers format.

### EMF Module
The EMF module provides bidirectional conversion between:
- LionCore ↔ Ecore
- LionWeb ↔ EMF
This enables integration with Eclipse Modeling Framework.

### EMF Builtins Module
This module defines Ecore equivalents for LionCore's built-in elements that don't have direct Ecore representations.

## Key Design Principles

We want to do boring stuff that works. This project is about providing the reliable, foundational capabilities
for you to build interesting things.

## Dependencies

- Java 8+ (runtime)
- Java 11+ (build)

## Version Support

The project currently supports:
- LionWeb 2023.1
- LionWeb 2024.1

## Future Considerations

The architecture is designed to accommodate:
- New LionWeb specification versions
- Additional integration points
- Performance optimizations
