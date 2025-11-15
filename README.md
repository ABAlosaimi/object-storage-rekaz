# Rekaz Storage

A flexible object storage service that supports multiple storage backends: Local File System, PostgreSQL Database, and Amazon S3.

## Features

- **Multiple Storage Backends**: Switch between local file storage, database storage, or S3 cloud storage
- **JWT Authentication**: Secure your storage operations with JWT tokens
- **Base64 Data Handling**: Upload and retrieve binary data encoded in Base64
- **Metadata Tracking**: Store and retrieve blob metadata (size, creation time, storage type)

## Technology Stack

- **Java 21**
- **Spring Boot 3.4.0**
- **Spring Security** with JWT authentication
- **Spring WebFlux** for reactive S3 operations
- **PostgreSQL** for database storage and metadata
- **Maven** for dependency management

## Project Structure

```
rekaz-storage/
├── src/main/java/com/rekaz/storage/rekaz_storage/
│   ├── Auth/                    # JWT authentication & security
│   ├── Exception/               # Custom exceptions
│   ├── Registry/                # Storage type registry
│   └── Storage/
│       ├── Configs/             # S3 configuration
│       ├── Dto/                 # Data transfer objects
│       ├── Utils/               # AWS Signature V4 utility
│       ├── BlobMeta.java        # Metadata entity
│       ├── DatabaseStorageService.java
│       ├── LocalStorageService.java
│       ├── S3StorageService.java
│       └── StorageController.java
```

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.9+
- PostgreSQL database
- AWS S3 bucket (optional, for S3 storage)

### Configuration

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update # Use 'update' for development, 'validate' or 'none' for production
spring.jpa.show-sql=true

# JWT Configuration
jwt.secret=your-secret-key-for-jwt-token-generation
jwt.expiration=86400000

# AWS S3 Configuration (if using S3)
aws.s3.bucket-name=your-bucket-name
aws.s3.access-key=your-access-key
aws.s3.secret-key=your-secret-key
aws.s3.region=us-east-1
```

### Installation

1. Clone the repository:
```bash
git clone https://github.com/ABAlosaimi/object-storage-rekaz.git
cd object-storage-rekaz/rekaz-storage
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### 1. Configure Storage Type (Public)

Configure which storage backend to use and receive a JWT token.

**Endpoint:** `POST /api/v1/storage`

**Request Body:**
```json
{
  "storageTpye": "local-file",
  "storagePath": "/path/to/storage" // Optional, required for local-file storage
}
```

**Storage Types:**
- `local-file` - Local file system storage
- `DB` - PostgreSQL database storage
- `s3` - Amazon S3 cloud storage

**Response:**
```
configuration set successfully.
Your Access Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Note:** This is the only public endpoint. All other endpoints require the JWT token.

---

### 2. Store Blob (Protected)

Upload a blob (file) to the configured storage backend.

**Endpoint:** `POST /api/v1/blobs`

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Request Body:**
```json
{
  "id": "my-file-123",
  "encodedData": "SGVsbG8gV29ybGQh..."
}
```
Note: `encodedData` should be Base64 encoded, otherwise, the server will return a 400 Bad Request.

**Response:**
Example:
```
Blob stored successfully in local-file storage.
```

---

### 3. Retrieve Blob (Protected)

Retrieve a blob from storage by its ID.

**Endpoint:** `GET /api/v1/blobs/{id}`

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Response:**
Example:
```
Blob retrieved successfully from local-file storage.
Data: SGVsbG8gV29ybGQh...
Size: 1024
Created At: 2023-11-15T14:30:22Z
```

## Authentication

All endpoints except `/api/v1/storage` require JWT authentication.

### Getting a Token

1. Configure storage type using `POST /api/v1/storage`
2. Copy the JWT token from the response
3. Include the token in the `Authorization` header for subsequent requests:
   ```
   Authorization: Bearer <your-token>
   ```

### Token Format
Example:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```
## Error Handling

The service returns appropriate HTTP status codes and error messages for various failure scenarios, such as:
- 400 Bad Request: Invalid input data (e.g., non-Base64 encoded data)
- 403 Forbidden: Unauthorized access due to missing or invalid JWT token
- 500 Internal Server Error: Issues with storage backends