# High Society Checkout

## Overview
High Society Checkout is an Android application designed for managing member data and cannabis distribution in Cannabis Social Clubs (CSCs) in Germany. 
The app ensures compliance with the CanG and DSGVO regulations, prioritizing data security and privacy.

find Whitepaper at: https://docs.google.com/document/d/1KYf3KXDWRZwQB5bXlZoLXa5NSpKgPs4NVrdMZYg36nw/edit?usp=sharing

## Features
- **RFID Technology**: Secure member identification using RFID tags.
- **Offline Operation**: No cloud storage; operates entirely offline to enhance security.
- **Local Database**: Stores essential member information and transaction logs on the device.
- **NFC Integration**: Uses NFC for tag reading and writing.
- **Data Security**: Utilizes encryption and strict access controls.
- **Simple backup** via E-Mail

## Setup
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/csc-coder/HighSocietyCheckout.git
   ```
2. **Open in Android Studio**.
3. **Configure NFC Permissions**:
   Ensure the following in `AndroidManifest.xml`:
   ```xml
   <uses-permission android:name="android.permission.NFC" />
   <uses-feature android:name="android.hardware.nfc" android:required="true" />
   ```
4. **Run the App** on an NFC-enabled device. with at least Android 13

## Usage
1. **Scan Screen**: The first screen instructs users to place their RFID tag on the scanner.
2. **Handle NFC Intent**: The app processes NFC intents to read/write data to the RFID tags.
3. **Member Registration**: New members can register by scanning their RFID tag.
4. **Cannabis Distribution**: Records and monitors cannabis distribution to members.

## Contributing
1. Fork the repository.
2. Create a new branch.
3. Commit your changes.
4. Push the branch.
5. Create a pull request.

## License
This project is open-source and available under the MIT License. See the LICENSE file for more details.

## Contact
For questions or support, contact us at csc.codemaker@gmail.com.

## Screenshots ##

![image](https://github.com/csc-coder/HighSocietyCheckout/blob/master/README.res/navigation%20graph.png)
![image](README.res/scan%20screen.png)
![image](README.res/register%20user.png)
![image](README.res/init%20tag.png)
![image](README.res/harvest%20screen.png)
![image](README.res/tools%20menu.png)
![image](README.res/data%20manager%20screen.png)
![image](README.res/clear%20tag%20screen.png)
![image](README.res/logs%20screen.png)
