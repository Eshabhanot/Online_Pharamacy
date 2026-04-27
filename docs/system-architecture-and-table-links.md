# Sprint Capgemini: System Architecture and Table Linking

## 1. Overview

This project is organized as a set of Spring Boot microservices behind an API Gateway.

Main services in the current workspace:

- `AuthService`
- `AdminService`
- `CatalogService`
- `OrderService`
- `ApiGateway`
- `PharmacyEurekaServer`
- `PharmacyConfigServer`

At a high level:

- `AuthService` manages users, login, roles, and addresses.
- `AdminService` manages medicines for admins and provides dashboard/report APIs.
- `CatalogService` exposes medicines, categories, prescriptions, and inventory APIs.
- `OrderService` manages cart, checkout, orders, and payments.
- `ApiGateway` routes requests and enforces gateway-level JWT checks.

This document explains:

- which tables exist in each service
- how those tables are linked
- which links are real database foreign keys
- which links are cross-service API/service integrations
- how major flows work end to end

---

## 2. Architecture Style

This project uses a mixed model:

- **Direct database relationships inside a service**
  Example: `inventory.medicine_id -> medicines.id`

- **Service-to-service communication across services**
  Example: `OrderService` does not directly join to the medicine table in `CatalogService`; instead it stores `medicineId` and calls `CatalogService` APIs when needed.

This is important:

- Not every connection is a SQL foreign key.
- Some connections are intentionally **logical links** through IDs and REST/Feign clients.

---

## 3. Service-by-Service Tables

### 3.1 AuthService tables

From:

- [User.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/AuthService/src/main/java/in/cg/main/entities/User.java:9)
- [UserAddress.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/AuthService/src/main/java/in/cg/main/entities/UserAddress.java:5)
- [OtpToken.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/AuthService/src/main/java/in/cg/main/entities/OtpToken.java:1)

Tables:

- `users`
- `user_addresses`
- `otp_tokens`

Purpose:

- `users` stores login identity and role data.
- `user_addresses` stores delivery addresses for a user.
- `otp_tokens` stores OTP data for mobile-based flows.

Key fields:

- `users.id`
- `users.email`
- `users.mobile`
- `users.role`
- `user_addresses.userId`

Linking:

- `user_addresses.userId` logically points to `users.id`
- This link is stored as a scalar field, not as a JPA `@ManyToOne`

Meaning:

- The address belongs to a user.
- The relationship exists at the data level, but it is not modeled as a formal ORM entity relationship.

### 3.2 AdminService tables

From:

- [Medicine.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/AdminService/src/main/java/in/cg/main/entities/Medicine.java:9)
- [Inventory.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/AdminService/src/main/java/in/cg/main/entities/Inventory.java:11)
- [Category.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/AdminService/src/main/java/in/cg/main/entities/Category.java:7)
- [Prescription.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/AdminService/src/main/java/in/cg/main/entities/Prescription.java:9)

Tables:

- `medicines`
- `inventory`
- `categories`
- `prescriptions`

Purpose:

- `medicines` stores core medicine master data
- `inventory` stores stock batches for a medicine
- `categories` groups medicines
- `prescriptions` stores uploaded prescription review data

Direct JPA links:

- `medicines.category_id -> categories.id`
- `inventory.medicine_id -> medicines.id`

These are real entity-level links:

- `Medicine` has `@ManyToOne Category`
- `Inventory` has `@ManyToOne Medicine`

Prescription linking:

- `prescriptions.customer_id` logically links to Auth users
- `prescriptions.order_id` logically links to orders
- `prescriptions.reviewed_by` logically links to admin user IDs

These are **ID-based logical links**, not ORM foreign-key entity mappings.

### 3.3 CatalogService tables

From:

- [Medicine.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/CatalogService/src/main/java/in/cg/main/entities/Medicine.java:8)
- [Inventory.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/CatalogService/src/main/java/in/cg/main/entities/Inventory.java:22)

Tables:

- `medicines`
- `inventory`
- `categories`
- `prescriptions`

CatalogService mirrors the same main business concepts as AdminService, but exposes user-facing and inventory APIs.

Important note:

- `CatalogService` is where add inventory, reduce inventory, expiring batches, and low-stock APIs are implemented.
- `Medicine.stock` in CatalogService is treated like a summary value derived from inventory batches.

Direct JPA links:

- `medicines.category_id -> categories.id`
- `inventory.medicine_id -> medicines.id`

### 3.4 OrderService tables

From:

- [Cart.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/OrderService/src/main/java/in/cg/main/entities/Cart.java:11)
- [CartItem.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/OrderService/src/main/java/in/cg/main/entities/CartItem.java:7)
- [Order.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/OrderService/src/main/java/in/cg/main/entities/Order.java:16)
- [OrderItem.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/OrderService/src/main/java/in/cg/main/entities/OrderItem.java:8)
- [Payment.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/OrderService/src/main/java/in/cg/main/entities/Payment.java:13)

Tables:

- `carts`
- `cart_items`
- `orders`
- `order_items`
- `payments`
- `addresses`

Purpose:

- `carts` stores one active cart per customer
- `cart_items` stores medicine selections in a cart
- `orders` stores placed or in-progress orders
- `order_items` stores items inside each order
- `payments` stores payment state
- `addresses` stores order-side address records

Direct JPA links:

- `cart_items.cart_id -> carts.id`
- `orders.address_id -> addresses.id`
- `order_items.order_id -> orders.id`
- `payments.order_id -> orders.id`

Logical cross-service links:

- `carts.customerId` -> Auth user ID
- `cart_items.medicineId` -> medicine in Catalog/Admin domain
- `cart_items.prescriptionId` -> prescription in Catalog/Admin domain
- `orders.customerId` -> Auth user ID
- `orders.prescriptionId` -> prescription in Catalog/Admin domain
- `order_items.medicineId` -> medicine in Catalog/Admin domain

These are not direct cross-database foreign keys. They are service-level links through stored IDs.

---

## 4. Core Table Relationships

## 4.1 Medicine and Category

Medicine belongs to a category.

Direct relationship:

- `medicines.category_id -> categories.id`

Seen in:

- [AdminService Medicine.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/AdminService/src/main/java/in/cg/main/entities/Medicine.java:44)
- [CatalogService Medicine.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/CatalogService/src/main/java/in/cg/main/entities/Medicine.java:32)

## 4.2 Inventory and Medicine

Inventory is batch-based stock for a medicine.

Direct relationship:

- `inventory.medicine_id -> medicines.id`

Seen in:

- [AdminService Inventory.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/AdminService/src/main/java/in/cg/main/entities/Inventory.java:19)
- [CatalogService Inventory.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/CatalogService/src/main/java/in/cg/main/entities/Inventory.java:29)

Meaning:

- One medicine can have multiple inventory batches.
- Batch quantity, expiry, supplier, and batch number live in `inventory`.
- The `medicine.stock` field acts as an aggregated/summary stock field.

## 4.3 Cart and CartItem

Direct relationship:

- `cart_items.cart_id -> carts.id`

Meaning:

- One cart has many cart items.
- Each cart item stores medicine details by ID and denormalized name/price.

Medicine connection:

- `cart_items.medicineId` is an ID reference to medicine
- It is not a JPA foreign key to another service

## 4.4 Order and OrderItem

Direct relationship:

- `order_items.order_id -> orders.id`

Meaning:

- One order has many order items.
- Order items store `medicineId`, `medicineName`, quantity, and price snapshot data.

Medicine connection:

- `order_items.medicineId` links logically to medicine
- This is an ID-based link, not a database join across microservices

## 4.5 Order and Payment

Direct relationship:

- `payments.order_id -> orders.id`

Seen in:

- [Payment.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/OrderService/src/main/java/in/cg/main/entities/Payment.java:21)

Meaning:

- One order has one payment record.

## 4.6 User and Address

Auth side:

- `user_addresses.userId -> users.id` as a logical ID link

Order side:

- Orders can use `addresses.id` for delivery address within OrderService

Meaning:

- AuthService stores profile addresses for the user
- OrderService stores order delivery addresses in its own domain

---

## 5. How the Major Features Work

## 5.1 User Registration and Login

Main files:

- [UserServiceImp.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/AuthService/src/main/java/in/cg/main/service/UserServiceImp.java:67)
- [UserController.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/AuthService/src/main/java/in/cg/main/controller/UserController.java:19)

Flow:

1. User registers through `AuthService`
2. A row is created in `users`
3. Optional address row is created in `user_addresses`
4. Login validates credentials and returns JWT

Important business rule added in this project:

- only the reserved admin user can log in as admin
- `admin@gmail.com` is reserved
- public registration cannot create admin users

## 5.2 Medicine Management

Main files:

- [AdminMedicineService.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/AdminService/src/main/java/in/cg/main/service/AdminMedicineService.java:41)

Flow:

1. Admin creates or updates a medicine in `AdminService`
2. `medicines` table is updated
3. category is resolved and synced
4. a medicine change event is published to RabbitMQ

Meaning:

- `AdminService` is acting as the medicine management owner
- other services react to medicine changes through events or service calls

## 5.3 Inventory Add / Reduce / Low Stock / Expiry

Main files:

- [InventoryController.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/CatalogService/src/main/java/in/cg/main/controller/InventoryController.java:15)
- [InventoryServiceImp.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/CatalogService/src/main/java/in/cg/main/service/InventoryServiceImp.java:39)

### Add inventory

Flow:

1. `POST /api/inventory/add`
2. A new batch row is inserted into `inventory`
3. It is linked to a medicine using `medicine_id`
4. `syncMedicineStock()` recalculates total active batch quantity
5. `medicines.stock` is updated

### Reduce inventory

Flow:

1. `PUT /api/inventory/reduce?medicineId=...&quantity=...`
2. Active inventory batches for the medicine are fetched
3. FIFO reduction is applied by earliest expiry first
4. batch quantities/statuses are updated
5. medicine total stock is synced again

### Low stock

Low stock is mainly determined from the `inventory` table:

- quantity less than threshold
- active status

Seen in:

- [InventoryServiceImp.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/CatalogService/src/main/java/in/cg/main/service/InventoryServiceImp.java:140)

### Expiry

Expiry reporting is mainly batch-based and uses `inventory.expiryDate`.

Seen in:

- [InventoryServiceImp.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/CatalogService/src/main/java/in/cg/main/service/InventoryServiceImp.java:121)

Important conclusion:

- add/reduce/low-stock/expiry are primarily **inventory-table operations**
- medicine is linked and updated as summary data

## 5.4 Cart to Order to Payment

Main files:

- [CartController.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/OrderService/src/main/java/in/cg/main/controller/CartController.java:26)
- [CheckoutController.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/OrderService/src/main/java/in/cg/main/controller/CheckoutController.java:27)
- [CheckoutServiceImpl.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/OrderService/src/main/java/in/cg/main/service/CheckoutServiceImpl.java:72)
- [PaymentServiceImpl.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/OrderService/src/main/java/in/cg/main/service/PaymentServiceImpl.java:46)

### Cart

1. A cart exists per customer in `carts`
2. Cart items are stored in `cart_items`
3. Each cart item keeps `medicineId`, `medicineName`, unit price, and prescription requirement

### Checkout

1. Checkout reads the user cart
2. For each cart item, medicine data is fetched from `CatalogService`
3. If any item requires prescription, prescription validation is applied
4. An order row is created in `orders`
5. Order items are inserted into `order_items`
6. Cart is cleared

### Payment

1. Payment initiation creates a `payments` row linked to the order
2. On payment success, stock reduction is triggered through `CatalogClient.reduceStock(...)`
3. This ultimately updates inventory batches and synced medicine stock in `CatalogService`

The important business chain is:

- `order_items.medicineId`
- `PaymentServiceImpl.reduceStockForOrder(...)`
- `CatalogClient.reduceStock(...)`
- `CatalogService InventoryServiceImp.reduceStock(...)`
- `medicine.stock` sync

So yes, order flow is functionally connected to medicine and inventory.

## 5.5 Dashboard and Admin Reports

Main files:

- [AdminDashboardService.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/AdminService/src/main/java/in/cg/main/service/AdminDashboardService.java:65)
- [AdminReportService.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/AdminService/src/main/java/in/cg/main/service/AdminReportService.java:67)

### Dashboard

The admin dashboard combines:

- medicine counts from `MedicineRepository`
- low stock and expiring batch info from `InventoryRepository`
- pending prescription info from `CatalogService` or local prescription repository fallback
- order stats and tracking from `OrderService` internal APIs

### Reports

- `sales` report: gets order stats from `OrderService`
- `inventory` report: uses medicine + inventory repositories
- `prescriptions` report: uses prescription repository
- `expiries` report: uses inventory repository
- `medicines/export`: exports medicine table data
- `low-stock/export`: exports medicines filtered by `stock`

This means the admin reporting module is connected to:

- `medicines`
- `inventory`
- `prescriptions`
- `orders` through service calls

---

## 6. Direct DB Links vs Logical Service Links

## 6.1 Direct DB / ORM links

These are modeled as actual entity relationships:

- `medicine -> category`
- `inventory -> medicine`
- `cart_item -> cart`
- `order_item -> order`
- `payment -> order`
- `order -> address`

## 6.2 Logical links through IDs

These are stored as plain IDs and resolved through service logic:

- `user_addresses.userId -> users.id`
- `prescriptions.customer_id -> users.id`
- `prescriptions.order_id -> orders.id`
- `cart.customerId -> users.id`
- `cart_item.medicineId -> medicine`
- `cart_item.prescriptionId -> prescription`
- `orders.customerId -> users.id`
- `orders.prescriptionId -> prescription`
- `order_items.medicineId -> medicine`

Why this is done:

- because this is a microservice system
- each service owns its own database and entity model
- cross-service references are usually stored as IDs instead of direct foreign keys

---

## 7. Important Integration Points

## 7.1 API Gateway

The gateway routes:

- `/api/auth/**` -> `AuthService`
- `/api/catalog/**`, `/api/categories/**`, `/api/medicines/**`, `/api/inventory/**`, `/api/prescriptions/**` -> `CatalogService`
- `/api/orders/**` -> `OrderService`
- `/api/admin/**` -> `AdminService`

It also applies gateway-level JWT rules.

## 7.2 RabbitMQ

Medicine-change events are published by `AdminService` and consumed by other services.

Purpose:

- cache invalidation
- medicine synchronization signals

## 7.3 Feign / REST clients

Examples:

- `OrderService` calls `CatalogService` for medicine and stock operations
- `AdminService` calls `CatalogService` for category and prescription-related data
- `AdminDashboardService` calls `OrderService` for order stats/tracking

---

## 8. Current Design Notes and Caveats

### 8.1 Medicine exists in both AdminService and CatalogService

There is medicine data in both services.

That means:

- there is duplication
- consistency depends on service calls/events/sync logic

This is workable, but it means the project relies on application-level synchronization rather than a single shared medicine table.

### 8.2 Inventory is the operational stock source

Even though `medicine.stock` exists, the real stock operations are batch-driven through `inventory`.

That is the correct place for:

- low stock
- expiry
- add batch
- reduce batch quantity

### 8.3 OrderService uses medicine IDs, not medicine joins

`OrderService` stores:

- `medicineId`
- `medicineName`

This is a normal microservice denormalization pattern.

### 8.4 Internal Order APIs should be verified

`AdminDashboardService` expects internal order endpoints such as:

- `/api/internal/orders/stats`
- `/api/internal/orders/tracking`
- `/api/internal/orders/today`

The file [InternalOrderController.java](/C:/Users/Esha%20bhanot/OneDrive/Desktop/cagemini/Sprint_Capgemini/OrderService/src/main/java/in/cg/main/controller/InternalOrderController.java:1) currently appears commented in the checked source shown during inspection, so this integration should be verified in runtime if dashboard endpoints are tested.

---

## 9. Relationship Summary

### Auth domain

- `users` 1 -> many `user_addresses` by `userId` logical link
- `users` 1 -> many `otp_tokens` by mobile/email flow logic

### Medicine domain

- `categories` 1 -> many `medicines`
- `medicines` 1 -> many `inventory`

### Order domain

- `carts` 1 -> many `cart_items`
- `orders` 1 -> many `order_items`
- `orders` 1 -> 1 `payments`
- `orders` many -> 1 `addresses`

### Cross-domain logical links

- user -> cart/order/prescription by stored customer/user IDs
- medicine -> cart item / order item by stored medicine IDs
- prescription -> order/cart by stored prescription IDs

---

## 10. Final Conclusion

The project has the required connections, but they are split into two types:

- **entity/table-level relationships inside each service**
- **ID-based and API-based links across services**

So if you ask:

"Are medicine, inventory, low-stock, expiry, orders, payments, prescriptions, and users connected properly?"

The answer is:

- **Yes, functionally they are connected**
- **Some are direct SQL/JPA relationships**
- **Some are service-level integrations instead of database foreign keys**

That is expected in this architecture.

