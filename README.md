# README for GoHiking! Android Application

## GoHiking! CSCI 310

The **GoHiking!** app is designed to help hikers discover hiking trails, keep track of their favorite trails, and connect with fellow hikers.

---

## Improvements Made in Sprint 2.5

### Key Functionalities Added
1. **Custom Lists Functionality**
    - Users can now create, name, and manage multiple custom lists of hiking trails.
    - Backend database schema updated to support this feature, fully integrated with Firebase.
    - A polished UI provides a seamless experience for managing custom lists.

2. **Enhanced Review Functionality**
    - Users can submit multiple unique reviews, stored in Firebase under the corresponding hike and user as a review object.
    - Functionality added for users to view all reviews they have submitted.

3. **Friend Review Viewing**
    - Users can select and view reviews submitted by their friends, including hike details, review content, and ratings.

4. **UI Enhancements**
    - Fixed scaling issues on the review page that caused navigation buttons to disappear when there were many reviews.
    - Added feedback messages for scenarios such as empty custom lists or missing reviews.

---

### Notable Fixes
- Resolved a bug where users could only submit one review, overwriting previous entries.
- Improved UI elements on pages with dynamically generated content.
- Enhanced feedback for user actions, making the app more intuitive and user-friendly.

---

## Future Work
- Complete additional UI improvements based on ongoing user testing and feedback.
- Refine group activity creation workflows to enhance usability further.
## How to Set Up and Run the App

### Step 1: Download the Project
1. Navigate to the Github Project repository.
2. Select the **"Download Zip"** option.
3. After downloading, extract the contents of the zip file to a personal folder on your computer.

### Step 2: Open the Project
1. Open Android Studio.
2. Select **File -> Open**, then choose the extracted project from your personal folder.

### Step 3: Set Up an Emulator
1. In Android Studio, select **Tools -> AVD Manager**.
2. Select a device of suitable API level (the project was built using API 35 and Pixel 8 Pro).

### Step 4: Run the App
1. Once the Emulator is set up, click the **green run button**.
2. The app will install and run on your selected device.

---

