# Health Tracking Application

## 📄 Project Description

Health Tracking is a full-stack application designed to help users monitor their personal health metrics and communicate securely with doctors through a web-based platform. The application provides a user-friendly dashboard where users can input and manage various health data such as weight, height, blood pressure, and heart rate. Users have the option to filter doctors by specialization and initiate direct communication via a built-in messaging system. Sent messages are stored along with the recipient's information, allowing users to track their interactions efficiently.

On the other side, doctors have access to a dedicated dashboard where they can view messages received from users, respond to them directly, and manage the list of patients they are in contact with. For deeper evaluation, doctors can download each patient's health data as an XML file, enabling offline analysis or integration with external systems. All health data is securely stored and exchanged in XML format, promoting interoperability and future integration with IoT systems.

This project reflects a comprehensive approach to personal health management and doctor-patient communication, combining modern web technologies, secure data handling, and practical health monitoring tools within a single unified system.

---

## Key Features

- **User Dashboard**
  - View and edit health data (weight, height, blood pressure, heart rate)
  - Browse doctors by specialization
  - Send messages to doctors and view message history

- **Doctor Dashboard**
  - View messages from users with sender details
  - Reply directly within the dashboard
  - Download individual patient data as XML files

- **Messaging System**
  - Two-way communication between users and doctors
  - Stored message history with sender/receiver details

- **Data Sharing**
  - All health data stored and exchanged in XML format
  - Designed for secure offline access and future IoT integration

---

## Technologies Used

- **Frontend**: Angular, TypeScript, HTML, CSS
- **Backend**: Java, Spring Boot, REST API, Maven
- **Database**: Firebase Firestore
- **Authentication**: Firebase Authentication
- **Data Format**: XML for patient health information
- **Future Enhancements**: MQTT for IoT devices, Firebase Cloud Messaging for notifications, real-time charts and analytics
