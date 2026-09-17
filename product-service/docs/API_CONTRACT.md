# API Contract - Product Service

## Base URL
- **Qua Gateway**: `http://localhost:8080/api`
- **Trực tiếp**: `http://localhost:8082`

## Authentication
- **Customer/Admin**: Header `Authorization: Bearer <JWT>`
- **Partner**: Header `X-API-KEY: SMARTHOME-PARTNER-KEY-2026`

## Endpoints

### 1. Products

#### `GET /api/products`
- **Query params**: `keyword` (string, optional), `page` (int, default 0), `size` (int, default 10), `sort` (string, format `field,direction`)
- **Response**: `200 OK`, `Page<ProductDTO>`
- **Auth**: Public

#### `GET /api/products/{id}`
- **Response**: `200 OK`, `ProductDTO` | `404 Not Found`
- **Auth**: Public

#### `POST /api/products`
- **Request body**:
  ```json
  {
    "name": "string, required, max 100 chars",
    "price": "number, required, >= 0",
    "description": "string, optional",
    "imageUrl": "string, optional",
    "categoryId": "number, optional"
  }