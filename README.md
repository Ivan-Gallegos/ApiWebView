# Pokemon Search App

This Android application allows users to search for information about Pokemon using the [PokeAPI](https://pokeapi.co/) and displays the results in a WebView within the app. The architecture follows the official Android architecture guidance, consisting of three layers: UI, Domain, and Data.

## Requirements

### API Interaction

- **Endpoint:** The app interacts with the API via the endpoint https://pokeapi.co/api/v2/query, where the user's query string is appended as a parameter to make a GET request.

### Caching

- **Cache:** API responses are cached locally on the device using Room database.
- **Cache Serving:** Subsequent requests for the same query are served from the cache if made within a 10-minute window, minimizing additional API calls.

### WebView Display

- **Display:** Fetched results are displayed in a WebView within the app to ensure readability.
- **Screenshot:** The app takes a screenshot of the WebView and caches it locally on the device.

### Cache Management

- **Automatic Deletion:** Implements an automatic cache deletion mechanism for data older than 30 minutes to maintain app performance.

## Architecture

This app follows the official Android architecture guidance, which is structured into layers:
1. **UI Layer**: Manages the user interface components.
2. **Domain Layer**: Contains the business logic and use cases.
3. **Data Layer**: Handles data operations such as fetching data from APIs or databases.

## Development Choices

- **Retrofit**: Used for making network requests to the PokeAPI.
- **Room**: Provides an abstraction layer over SQLite to store retrieved data locally.
- **WorkManager**: Utilized for scheduling background tasks, such as cache deletion after a certain time.

## Assumptions Made

- Data from the endpoints is always in JSON format.
- The app is designed to handle queries related to Pokemon information only.

## Additional Features

- **Debouncing**: Implemented to prevent excessive GET requests during auto-search.
- **Keyboard Search**: Supports searching using the keyboard search key.

## Usage

To use this app, simply input the name of the Pokemon you wish to search for in the designated search field. The app will then display the relevant information retrieved from the PokeAPI.

## Requirements

- Android device or emulator running Android 7.1 (API level 24) or higher.

## Installation

1. Clone this repository to your local machine.
2. Open the project in Android Studio.
3. Build and run the project on your device or emulator.

## Dependencies

- Retrofit: Used for making network requests to the PokeAPI.
- Room: Provides an abstraction layer over SQLite to store the retrieved data locally.
- WorkManager: Used for scheduling background tasks, such as deleting stored entities after a certain time.
