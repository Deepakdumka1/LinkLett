# LinkLett Share

LinkLett Share is an Android application that allows users to easily share contact information with nearby devices using Bluetooth technology.

## Features

- Select contacts from your device's contact list
- Scan for nearby Bluetooth devices
- Share selected contacts with connected devices
- Visual indicators for Bluetooth and NFC status
- Simple and intuitive user interface

## Getting Started

### Prerequisites

- Android device running Android 5.0 (Lollipop) or higher
- Bluetooth capability
- Access to device contacts

### Installation

1. Clone this repository
2. Open the project in Android Studio
3. Build and run the application on your device

## Usage

1. Launch the LinkLett Share app
2. Ensure Bluetooth is enabled on your device
3. Select a contact you wish to share by tapping the "Select Contact" button
4. Tap "Scan for Devices" to discover nearby Bluetooth devices
5. Select a device to connect with
6. Once connected, your contact information will be shared automatically

## Permissions

The application requires the following permissions:
- Bluetooth permissions (BLUETOOTH, BLUETOOTH_ADMIN, BLUETOOTH_CONNECT, BLUETOOTH_SCAN)
- Contacts permission (READ_CONTACTS)

## Technical Details

- Built with Java for Android
- Utilizes Android's Bluetooth API for device discovery and connection
- Implements proper permission handling for modern Android versions

## Project Structure

The project follows standard Android application architecture:

- `MainActivity.java`: Main entry point of the application, handles UI interactions
- `BluetoothConnectionService.java`: Manages Bluetooth connections and data transfer
- XML layouts: Define the user interface components
- Drawable resources: Icons and visual indicators for the application

## Contributing

Contributions to improve LinkLett Share are welcome. Please feel free to fork the repository and submit pull requests.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Android developer documentation for Bluetooth API
- Contributors to the Android open-source community
