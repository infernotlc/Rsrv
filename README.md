Reservation App 📅
A multi-module reservation app built with Kotlin, Jetpack Compose, and Firebase.

Overview
This app allows admins to create places with customizable designs (tables and chairs) and lets customers browse these places, make reservations, and view availability. It leverages Jetpack Compose for UI and follows a multi-module architecture for better scalability and maintainability.

Flowchart link:
https://drive.google.com/file/d/1K_UbTClfNRs3TlYI3QfC7rusKMr2XQje/view?usp=sharing

Features
Admin Dashboard:

Add and manage places.
Design tables and chairs for each place (drag-and-drop functionality).
Save and load designs dynamically.
Customer Interface:

Browse available places.
View place designs in read-only mode.
Make reservations for available tables and chairs.
Authentication & Role Management:

Firebase Authentication for user login and registration.
User roles: Admin and Customer.
Reservation Management:

Real-time updates on reserved seats using Firebase Firestore.
Customers can see which tables are available and which are already reserved.
Tech Stack
Language: Kotlin
UI: Jetpack Compose
Architecture: Multi-module architecture with MVVM pattern
State Management: State and ViewModels
Dependency Injection: Hilt
Backend: Firebase Authentication & Firestore
Navigation: Compose Navigation
Modules
App Module: Entry point of the app, handles navigation.
Admin Module: Features related to place management and design creation.
Customer Module: Features for customers to browse and reserve places.
Data Module: Repository and use cases to interact with Firebase.
Core Module: Common utilities and shared components (UI elements, theme, etc.).
<p align="left-side" Screenshots: >
<p align="center">
  <img src="https://github.com/user-attachments/assets/66761443-5ec1-4606-8101-98c7f477b09b" width="300">
  <img src="https://github.com/user-attachments/assets/c09d0586-e348-46fd-8658-8a23b5030471" width="300">
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/6faa1edb-e9d1-4005-b0e8-d971d101ae0f" width="300">
  <img src="https://github.com/user-attachments/assets/9d25e8a8-cca4-48a0-a94c-5318ac2a0cf7" width="300">
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/ad6c57af-644b-4680-9ff6-c62b5301b32b" width="300">
</p>
