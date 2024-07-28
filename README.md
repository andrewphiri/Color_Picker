# Color Picker App

The Color Picker App is an Android application that allows users to capture images using their device's camera and pick colors from the captured images. The app displays the selected color in both HEX and RGB formats and allows users to reset the process and capture new images.

## Features

- **Live Camera Preview**: Users can see a live preview of the camera feed.
- **Image Capture**: Users can capture images using the camera.
- **Color Picker**: Users can select a color from the captured image by dragging a picker across the image.
- **Color Display**: The app displays the selected color in HEX and RGB formats.
- **Reset Functionality**: Users can reset the app to capture a new image.

## Installation

1. **Clone the Repository**: Clone the repository to your local machine using:
    ```sh
    git clone <https://github.com/andrewphiri/Color_Picker.git>
    ```

2. **Open in Android Studio**: Open the project in Android Studio.

3. **Build the Project**: Build the project to download necessary dependencies and set up the project.

4. **Run the App**: Run the app on an emulator or a physical device.

## Permissions

The app requires the following permissions:
- **Camera**: To capture images using the device's camera.
- **Storage**: To read and save images.

## Code Structure

### Main Composable Functions

- **ColorPickerApp**: The main composable function that manages the state of the captured image, selected color, and picker position. It displays either the camera preview or the captured image with the color picker overlay.
- **CameraLivePreviewWithCapture**: Handles the live camera preview and image capture functionality.

### Utility Functions

- `getPixelColorAtOffset(image: ImageProxy, offset: Offset)`: Picks a color from the imageProxy at the given position.
- `Color.toHex()`: Converts a Color object to a HEX string.
- `Color.toRgb()`: Converts a Color object to an RGB string.
- `transformCoordinates(pickerPosition: Offset, imageBitmap: ImageProxy, size: IntSize)`: Transforms the coordinates of the picker position to match the imageProxy's coordinates.

## Usage

1. **Launch the App**: Open the app on your Android device.
2. **Capture an Image**: Use the camera to capture an image.
3. **Select a Color**: Drag the picker over the captured image to select a color.
4. **View Selected Color**: The selected color will be displayed in both HEX and RGB formats at the bottom of the screen.
5. **Reset**: Use the reset button to capture a new image.

## Screenshots

_Add screenshots of the app here._

## License

Color Picker App is licensed under the Apache License 2.0. See
the [LICENSE](https://www.apache.org/licenses/LICENSE-2.0) link for details.

## Contributing

1. **Fork the Repository**: Fork the repository on GitHub.
2. **Create a Branch**: Create a new branch for your feature or bugfix.
3. **Commit Changes**: Commit your changes to the new branch.
4. **Push Changes**: Push your changes to your forked repository.
5. **Create a Pull Request**: Create a pull request to merge your changes into the main repository.

## Contact

For any queries, feedback, or issues, feel free to contact our support team
at [andrewalfredphiri@gmail.com](andrewalfredphiri@gmail.com).
