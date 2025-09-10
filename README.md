# PeerLink

A simple peer-to-peer file sharing application that allows users to share files directly between devices using invite codes.

## Overview

PeerLink consists of a Java backend server and a Next.js frontend that enables secure file sharing without storing files on external servers. Files are transferred directly between peers using dynamically generated port numbers as invite codes.

## Features

- Direct peer-to-peer file transfer
- No external file storage required
- Invite code system for sharing files
- Support for multiple file types: .txt, .jpeg, .img, .pdf
- File size limit of 10 MB (recommended)
- Modern web interface with drag-and-drop functionality

## Architecture

### Backend (Java)
- HTTP server running on port 8080
- File upload and download endpoints
- Dynamic port allocation for peer connections
- Socket-based file transfer between peers
- Built with Maven and Java 17

### Frontend (Next.js)
- React-based user interface
- TypeScript for type safety
- Tailwind CSS for styling
- File upload with drag-and-drop support
- Download functionality using invite codes

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Node.js 18 or higher
- npm or yarn package manager

## Getting Started

### 1. Start the Backend Server

```bash
mvn clean package
java -jar target/p2p-1.0-SNAPSHOT.jar
```

The backend server will start on port 8080.

### 2. Start the Frontend

```bash
cd ui
npm install
npm run dev
```

The frontend will be available at http://localhost:3000.

## Usage

### Sharing a File
1. Navigate to the "Share a File" tab
2. Drag and drop or select a file to upload
3. Receive an invite code (port number)
4. Share the invite code with the recipient

### Receiving a File
1. Navigate to the "Receive a File" tab
2. Enter the invite code provided by the sender
3. Click "Download File"
4. The file will be downloaded to your device

## Project Structure

```
PeerLink/
├── src/main/java/p2p/          # Java backend source code
│   ├── App.java                # Main application entry point
│   ├── controller/             # HTTP request handlers
│   ├── service/                # File sharing business logic
│   └── utils/                  # Utility classes
├── ui/                         # Next.js frontend
│   ├── src/app/               # Application pages
│   ├── src/components/        # React components
│   └── package.json           # Frontend dependencies
├── target/                     # Compiled Java classes
├── pom.xml                     # Maven configuration
└── README.md                   # This file
```

## Building for Production

### Backend
```bash
mvn clean package
```

### Frontend
```bash
cd ui
npm run build
npm start
```

## Technical Details

- Backend uses Java's built-in HTTP server
- File transfers use TCP sockets for direct peer communication
- Frontend communicates with backend via REST API
- CORS enabled for cross-origin requests
- Temporary file storage in system temp directory
- Automatic cleanup of transferred files

## License

This project is available for educational and personal use.