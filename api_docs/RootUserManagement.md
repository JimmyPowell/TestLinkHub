# API Documentation: Root User Management

This document describes the API endpoints for managing users by a root administrator.

**Base Path:** `/api/root/users`
**Authentication:** All endpoints require `ADMIN` authority.

---

## 1. Get All Users (with Filtering)

- **Functionality:** Retrieves a paginated list of all users. Supports filtering based on various user attributes.
- **Method:** `GET`
- **Endpoint:** `/api/root/users`
- **Query Parameters:**
  - `page` (integer, optional, default: `0`): The page number to retrieve.
  - `size` (integer, optional, default: `10`): The number of users per page.
  - `name` (string, optional): Filter by user name (partial match).
  - `email` (string, optional): Filter by user email (partial match).
  - `status` (string, optional): Filter by user status. Allowed values: `active`, `inactive`, `pending`, `suspended`.
  - `role` (string, optional): Filter by user role. Allowed values: `admin`, `user`, `manager`, `guest`.
  - `company_uuid` (string, optional): Filter by the UUID of the company the user belongs to.
- **Success Response (200 OK):**
  ```json
  {
    "code": 200,
    "message": "Users retrieved successfully",
    "data": {
      "content": [
        {
          "uuid": "a1b2c3d4-0001-4001-8001-000000000001",
          "name": "Admin User",
          "email": "admin@testlinkhub.com",
          "phone_number": "18800000001",
          "role": "admin",
          "status": "active",
          "company_name": null,
          "created_at": "2025-06-30T14:30:00Z"
        }
      ],
      "page": 0,
      "size": 10,
      "total_elements": 1,
      "total_pages": 1,
      "last": true
    }
  }
  ```
- **Error Response (500 Internal Server Error):**
  ```json
  {
    "code": 5000,
    "message": "Error message from server.",
    "data": null
  }
  ```

---

## 2. Get User by UUID

- **Functionality:** Retrieves a single user by their unique identifier (UUID).
- **Method:** `GET`
- **Endpoint:** `/api/root/users/{uuid}`
- **Path Variable:**
  - `uuid` (string, required): The UUID of the user to retrieve.
- **Success Response (200 OK):**
  ```json
  {
    "code": 200,
    "message": "User retrieved successfully",
    "data": {
      "uuid": "a1b2c3d4-0001-4001-8001-000000000001",
      "name": "Admin User",
      "email": "admin@testlinkhub.com",
      "phone_number": "18800000001",
      "address": "123 Admin St, Management City",
      "avatar_url": null,
      "gender": "male",
      "company_uuid": null,
      "company_name": null,
      "role": "admin",
      "status": "active",
      "description": "系统最高管理员，拥有所有权限。",
      "created_at": "2025-06-30T14:30:00Z"
    }
  }
  ```
- **Error Response (404 Not Found):**
  ```json
  {
    "code": 404,
    "message": "User not found with UUID: [uuid]",
    "data": null
  }
  ```

---

## 3. Create User

- **Functionality:** Creates a new user.
- **Method:** `POST`
- **Endpoint:** `/api/root/users`
- **Request Body (JSON, snake_case):**
  ```json
  {
    "name": "New User",
    "email": "new.user@example.com",
    "password": "a-strong-password",
    "phone_number": "18812345678",
    "role": "user",
    "status": "active",
    "company_uuid": "a1b2c3d4-comp-4001-8001-000000000001"
  }
  ```
- **Success Response (201 Created):**
  ```json
  {
    "code": 201,
    "message": "User created successfully",
    "data": {
      "uuid": "a1b2c3d4-new-user-uuid-generated",
      "name": "New User",
      "email": "new.user@example.com",
      "role": "user",
      "status": "active",
      "company_name": "Example Corp"
    }
  }
  ```
- **Error Response (400 Bad Request):**
  ```json
  {
    "code": 400,
    "message": "Email already exists.",
    "data": null
  }
  ```

---

## 4. Update User

- **Functionality:** Updates an existing user's information by their UUID.
- **Method:** `PUT`
- **Endpoint:** `/api/root/users/{uuid}`
- **Path Variable:**
  - `uuid` (string, required): The UUID of the user to update.
- **Request Body (JSON, snake_case):**
  ```json
  {
    "name": "Updated User Name",
    "phone_number": "18887654321",
    "status": "suspended",
    "description": "User has been suspended due to policy violation."
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
    "code": 200,
    "message": "User updated successfully",
    "data": {
      "uuid": "a1b2c3d4-user-uuid-to-update",
      "name": "Updated User Name",
      "email": "original.email@example.com",
      "phone_number": "18887654321",
      "status": "suspended",
      "description": "User has been suspended due to policy violation."
    }
  }
  ```
- **Error Response (404 Not Found):**
  ```json
  {
    "code": 404,
    "message": "User not found with UUID: [uuid]",
    "data": null
  }
  ```

---

## 5. Delete User

- **Functionality:** Deletes a user by their UUID (soft delete).
- **Method:** `DELETE`
- **Endpoint:** `/api/root/users/{uuid}`
- **Path Variable:**
  - `uuid` (string, required): The UUID of the user to delete.
- **Success Response (204 No Content):**
  ```json
  {
    "code": 204,
    "message": "User deleted successfully",
    "data": null
  }
  ```
- **Error Response (404 Not Found):**
  ```json
  {
    "code": 404,
    "message": "User not found with UUID: [uuid]",
    "data": null
  }
  ```

---

## 6. Get Users by Company UUID

- **Functionality:** Retrieves a paginated list of users belonging to a specific company.
- **Method:** `GET`
- **Endpoint:** `/api/root/users/company/{companyUuid}`
- **Path Variable:**
  - `companyUuid` (string, required): The UUID of the company.
- **Query Parameters:**
  - `page` (integer, optional, default: `0`): The page number to retrieve.
  - `size` (integer, optional, default: `10`): The number of users per page.
- **Success Response (200 OK):**
  ```json
  {
    "code": 200,
    "message": "Users retrieved successfully",
    "data": {
      "content": [
        {
          "uuid": "a1b2c3d4-0002-4002-8002-000000000002",
          "name": "Manager Li",
          "email": "manager.li@examplecorp.com",
          "role": "manager",
          "status": "active"
        }
      ],
      "page": 0,
      "size": 10,
      "total_elements": 1,
      "total_pages": 1,
      "last": true
    }
  }
