# Secure Password Manager iOS App

A robust iOS password manager featuring biometric authentication, encrypted storage, and customizable themes.

## Screenshots & Features

### Main Interface
![Simulator Screenshot - iPhone 16 Pro - 2024-11-15 at 18 56 31](https://github.com/user-attachments/assets/cdecdc58-0446-4bd8-9ca1-b65c95e8d54f)
- Clean, intuitive password list view
- Quick access search functionality 
- Color-coded password entries
- Settings and add password shortcuts

### Add/Edit Password
![Simulator Screenshot - iPhone 16 Pro - 2024-11-15 at 18 56 40](https://github.com/user-attachments/assets/47bf6a21-ad4c-45d3-a3a0-8f32db6c7231)
- Title and username fields
- Secure password entry
- Optional notes section
- Simple save/cancel navigation

### Settings & Customization
![Simulator Screenshot - iPhone 16 Pro - 2024-11-15 at 18 56 35](https://github.com/user-attachments/assets/9c2e8c92-b564-469f-92f6-2dd6ae2ab9c5)
- Theme color customization
  - Background color
  - Box background color
  - Text color
  - Accent color
- Password backup and restore options

## Key Security Features

### Authentication
- Biometric authentication (Face ID/Touch ID) required to access the app
- Fallback passcode option available

### Data Protection
- Encrypted password storage
- Encrypted backup files with password protection
- Auto-clearing clipboard after 60 seconds
- Automatic password hiding after 30 seconds

## Core Features
- 🔐 Secure password storage
- 🔍 Real-time search
- 📝 Notes support for each entry
- 🎨 Customizable themes
- 💾 Encrypted backup/restore
- 📋 Quick copy functionality

## Requirements
- iOS 17.0+
- Face ID/Touch ID capable device
- Xcode 14.0+ for building

## Installation & Setup
1. Clone the repository:
```bash
git clone https://github.com/yourusername/password-manager-ios.git
```

2. Open the project in Xcode:
```bash
cd password-manager-ios
open PasswordManager.xcodeproj
```

3. Build and run the project on your device/simulator

## Testing
- To test Face ID in simulator, go to Features > Face ID > Enrolled
- For physical devices, ensure Face ID/Touch ID is set up

## Security Note
All stored passwords and backup files are encrypted for maximum security. Authentication is required for:
- Launching the app
- Viewing stored passwords
- Creating/restoring backups
- Accessing sensitive information
