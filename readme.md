<p align="center">
    <img src="https://manage.eyedid.ai/img/seeso_logo.467ee6a5.png" height="170">
</p>

<div align="center">
    <h1>Eyedid SDK Android Sample (Java)</h1>
    <a href="https://github.com/visualcamp/eyedid-android-sample/releases" alt="release">
        <img src="https://img.shields.io/badge/version-1.0.0--beta-blue" />
    </a>
</div>

## Documentation

- **Overview**: [Eyedid SDK Overview](https://docs.eyedid.ai/docs/Beta/document/eyedid-sdk-overview)
- **Quick Start**: [Android Quick Start Guide](https://docs.eyedid.ai/docs/Beta/quick-start/android-quick-start)
- **API Reference**: [Eyedid SDK Android API Documentation](https://docs.eyedid.ai/docs/Beta/api/android-api-docs)

## Requirements

- **Minimum SDK Version**: 23
- **Target SDK Version**: 34
- **Device**: Must be run on a real Android device (emulator not supported)
- **Internet Connection**: Required
- **License Key**: A license key issued from [Eyedid SDK Manage](https://manage.eyedid.ai/) is required

## How to Run

1. **Clone or Download the Project**
  ```bash
    git clone https://github.com/visualcamp/eyedid-android-sample.git
  ```
2. **Setting License Key**
  - Obtain a license key from https://manage.eyedid.ai/ 
  - Open the  [`MainActivity.java`](/app/src/main/java/camp/visual/android/sdk/sample/MainActivity.java#L37) file and enter your license key:
    ```java
    // TODO: change licence key
    private final String EYEDID_SDK_LICENSE = "typo your license key";
    ```
3. **Grant Camera Permission**
  - Allow the app to access the device’s camera.
4. **Start Tracking**
  - Run the app and start tracking!

## Contact Us
If you have any questions or need assistance, please feel free to [contact us](mailto:development@eyedid.ai) 

----------

## License Information

### SDK License
All rights to the Eyedid Android SDK are owned by VisualCamp. Unauthorized copying, modification, distribution, or any other form of use is strictly prohibited without explicit permission from VisualCamp. Please refer to the license agreement provided with the SDK for more details.

### Sample Project License
The sample project provided with the Eyedid Android SDK is distributed under the MIT License. You are free to use, modify, and distribute this sample project, provided that you include the original copyright and permission notice in all copies or substantial portions of the software.
