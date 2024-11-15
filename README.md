# Secure Password Manager iOS App

A robust iOS password manager featuring biometric authentication, encrypted storage, and customizable themes.

## Screenshots & Features

### Main Interface
![Simulator Screenshot - iPhone 16 Pro - 2024-11-15 at 18 56 31](https://github.com/user-attachments/assets/9c56c5ba-ac9e-44d0-bca3-2fe5a9d4d31a)
- Clean password list with search functionality
- Purple-themed cards showing titles and usernames
- Quick access to settings and add functions
- Real-time search filtering

### Add Password
![Simulator Screenshot - iPhone 16 Pro - 2024-11-15 at 18 56 40](https://github.com/user-attachments/assets/52fc5248-1c0f-4c8b-bc0c-b46be5ee2307)
- Title and username fields
- Secure password entry
- Optional notes section
- Simple save/cancel navigation

### Password Detail View
![Simulator Screenshot - iPhone 16 Pro - 2024-11-15 at 19 16 30](https://github.com/user-attachments/assets/33e36318-dcea-42cb-885b-c4db144d781b)
- One-tap username & password copy feature
- Secure password viewing with eye icon
- Individual copy buttons for credentials
- Edit and delete options
- Notes display section
- Face ID required for viewing password

### Settings & Customization
![Simulator Screenshot - iPhone 16 Pro - 2024-11-15 at 18 56 35](https://github.com/user-attachments/assets/5a96fcd1-bb70-4cf0-a7ae-f0cbcc0e6378)
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
- Authentication required for viewing passwords

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
- 👁️ Secure password viewing

## Requirements
- iOS 14.0+
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
