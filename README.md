[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/4-04QCSZ)

# WiFinder
WiFinder allows the user to find their location at the University of Auckland. Currently the application supports a small area on floors G, 1 and 2 in building 302.

## Technologies used to create the application
The WiFinder application was made using Android Studio. The languages used were Kotlin and Java.

Extra tools can be found under other branches:
- The interpolation branch has scripts available to interpolate the data
- The position-tool branch has a local webpage program that allows the user to select points on the map to get an X and Y coordinate of the floor image
- The model-testing branch has a build of the Android application that allows developers to test different machine learning models
- The mainWithPF branch has a build of the Android application which implements the Particle Filter. As of writing this README, the Particle Filter still has some issues which is why it's under a separate branch.

## How to setup the application
- Install the latest version of Android Studio
- Clone the Repository
- Open Android Studio and go through the setup process
- Once you get to the project view, select and open the cloned repository

- Go to the project view on the left side, open Gradle Scripts dropdown and open the build.gradle.kts file
- A prompt will show up at the top of the editor asking you to sync the gradle files. Click the "Sync Now" option.

- Once the gradle files are synced, you can build and run the application on an emulator or load it onto an Android phone.

For more information on running the application on your emulator or phone, refer to the [Android Studio Developer Page](https://developer.android.com/studio/run/device)

## Project Management Tool
[Project Management Tool](https://github.com/orgs/uoa-compsci399-2025-s1/projects/24)