Reservation App 📅
A multi-module reservation app built with Kotlin, Jetpack Compose, and Firebase.

Overview
This app allows admin and customers to register and login also contains reset password feature, admins to create places with customizable designs (tables and chairs - saving design) and lets customers browse these places, make reservations, and view availability. It leverages Jetpack Compose for UI and follows a multi-module architecture for better scalability and maintainability.

Flowchart link:
https://drive.google.com/file/d/1K_UbTClfNRs3TlYI3QfC7rusKMr2XQje/view?usp=sharing

 Implemented Features:
- login, register, reset password
- customer screen - admin screen - design screen - reservation screen
- create place- save place with reservation times and images- create design - save design
- fetch place - fetch design for users
- save reservation by date and time - fetch reservation times by date
- if there is no available times for it day remove table from design
  
Next Features:
- Show reservations for admins and visitors
- Admins and visitors can cancel reservations
-Settings screen
-More information about places
-Save places by country and city, approve them to show their places
-Fetch places by country and city
-Take comments for places
-Punishment system for non made visits (after 30 minutes of reservation can be automatically cancel)
-If made more than 5 cancel account will be deleted
-Notifications for admins and users
-Reports for admins daily, weekly, monthly


Admin Dashboard:
Add and manage places.
Add image, name, reservation times.
Design tables and chairs for each place (drag-and-drop functionality).
Save and load designs dynamically.

Customer Interface:
Browse available places.
View place designs like a preview.
Make reservations for available tables.

Authentication & Role Management:
Firebase Authentication for user login and registration.
User roles: Admin and Customer.

Reservation Management:
Real-time updates on reserved tables using Firebase Firestore.
Customers can see which tables are available and which are already reserved per day and time.

Tech Stack
Language: Kotlin
UI: Jetpack Compose
Architecture: Multi-module architecture with MVVM pattern
State Management: State and ViewModels
Dependency Injection: Hilt
Backend: Firebase Authentication & Firestore, Cloud Storage
Navigation: Compose Navigation

Modules
App Module: Entry point of the app, handles navigation.
Data Module: Repository and use cases to interact with Firebase.
Domain Module: Repository Implementations to implement features.
Feature Module: Features related to ui and state management.

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
   <img src="https://github.com/user-attachments/assets/73920a5d-e37f-4fe3-9a31-5c055eee7a55" width="300">
</p>
