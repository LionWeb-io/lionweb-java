---
sidebar_position: 4
---

# LionWeb Java Architecture Overview

## Project Structure

LionWeb Java is organized into several modules, each with a specific responsibility:

### Core Module
The core module implements the LionCore (M2) and LionWeb (M1) specifications in Java. It provides:
- Core data structures and interfaces
- JSON serialization/deserialization
- Basic validation and processing capabilities

### EMF Module
The EMF module provides bidirectional conversion between:
- LionCore ↔ Ecore
- LionWeb ↔ EMF
This enables integration with Eclipse Modeling Framework.

### EMF Builtins
This module defines Ecore equivalents for LionCore's built-in elements that don't have direct Ecore representations.

### Repository Client
The repository client module provides:
- Client-side implementation of the LionWeb repository protocol
- Support for delta protocol
- Integration with LionWeb repositories

## Key Design Principles

1. **Modularity**: Each module has a clear, single responsibility
2. **Extensibility**: The architecture allows for easy addition of new features and integrations
3. **Compatibility**: Support for multiple versions of LionWeb specifications
4. **Interoperability**: Focus on seamless integration with other tools and frameworks

## Dependencies

- Java 8+ (runtime)
- Java 11+ (build)
- EMF (for EMF-related modules)
- JSON processing libraries
- Testing frameworks

## Version Support

The project currently supports:
- LionWeb 2023.1
- LionWeb 2024.1

Each version is maintained in a way that allows for:
- Independent evolution
- Clear migration paths
- Backward compatibility where possible

## Extension Points

The architecture provides several extension points:
1. Custom serialization formats
2. Additional validation rules
3. New repository implementations
4. Custom model transformations

## Future Considerations

The architecture is designed to accommodate:
- New LionWeb specification versions
- Additional integration points
- Performance optimizations
- Enhanced tooling support 