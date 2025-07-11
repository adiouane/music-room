# MusicRoom

MusicRoom is a modern Android application built with Kotlin and Jetpack Compose that allows users to discover, share, and enjoy music with friends in real-time.

## Features

- User Authentication
- Modern UI with Material 3 Design
- Music Discovery
- Playlist Creation & Sharing

## Authentication System

The application implements a comprehensive authentication system with the following capabilities:

### Authentication Methods

- Email/Password Authentication
- Google Sign-In
- Password Reset

### Authentication Flow

1. **AuthContainer**: A container component that manages navigation between login and signup screens with smooth transitions between modes.
2. **Input Validation**: Real-time validation for email, password strength, and password matching.
3. **Error Handling**: Comprehensive error handling and user feedback.

### Architecture

The authentication system follows clean architecture principles:

- **Presentation Layer**: 
  - `AuthContainer.kt`: Container that manages the authentication flow
  - `LoginScreen.kt`: UI for user login
  - `SignUpScreen.kt`: UI for new user registration
  - `AuthViewModel.kt`: ViewModel that handles authentication logic and state
  - Auth component classes for reusable UI elements

- **Domain Layer**:
  - `AuthRepository.kt`: Interface defining authentication operations
  - `User.kt`: Data model representing authenticated users

- **Data Layer**:
  - `MockAuthRepository.kt`: Concrete implementation using mock data
  - Utilities for validation and security

### Security Features

- Password strength validation
- Secure storage of authentication tokens
- Protection against common authentication attacks

## Tech Stack

- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern UI toolkit
- **Firebase Authentication**: Backend authentication service
- **Hilt**: Dependency injection
- **Material 3**: Design system
- **Kotlin Coroutines & Flow**: Asynchronous programming
- **Result API**: Error handling
