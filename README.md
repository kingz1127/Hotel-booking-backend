Hotel Booking Management System

📋 Project Overview

A full-stack Hotel Booking Management System that enables hotels to manage room inventory, handle customer bookings (both online and walk-in), and streamline administrative operations. The system provides separate interfaces for customers and administrators with real-time availability tracking and automated booking management.

🎯 Core Features

Customer Features

· ✅ User Registration & Authentication - Secure JWT-based authentication
· ✅ Room Browsing & Filtering - Search by category, price, dates, and availability
· ✅ Online Booking System - Select rooms, choose dates, and make payments
· ✅ Booking Management - View, cancel, or modify existing bookings
· ✅ Real-time Availability - Live room availability updates
· ✅ Email Notifications - Booking confirmations and updates
· ✅ Profile Management - Update personal information and preferences

Admin Features

· ✅ Walk-in Booking System - Direct booking for in-person customers
· ✅ Room Management - Add, edit, and manage room inventory
· ✅ Booking Management - View, confirm, cancel, and check-in/out bookings
· ✅ Real-time Dashboard - Occupancy rates, revenue, and booking analytics
· ✅ Inventory Control - Track room availability and fix inventory issues
· ✅ Customer Management - View and manage customer profiles
· ✅ Payment Processing - Handle cash, card, and partial payments
· ✅ Room Status Monitoring - Live view of all room statuses

System Features

· ✅ Availability Algorithm - Smart room allocation and conflict prevention
· ✅ Automated Check-in/Check-out - Scheduled status updates
· ✅ Revenue Tracking - Real-time revenue calculation and reporting
· ✅ Multi-room Category Support - Presidential, Deluxe, Standard, Suite rooms
· ✅ Discount Management - Percentage-based discounts on room rates
· ✅ Room Fixing Tools - Emergency tools to correct availability issues
· ✅ Email Service - Automated notifications for bookings and updates

🛠️ Technology Stack

Frontend

· React 18 - Modern component-based UI library
· React Router DOM - Client-side routing
· Tailwind CSS - Utility-first CSS framework
· React Hook Form - Form handling with validation
· Zod - Schema validation library
· Lucide React - Icon library
· React Toastify - Notification system
· Axios/Fetch API - HTTP client for API calls
· Local/Session Storage - Client-side data persistence

Backend

· Java 17 - Backend programming language
· Spring Boot 3.x - Framework for building REST APIs
· Spring Data JPA - Database abstraction layer
· Spring Security - Authentication and authorization
· JWT (JSON Web Tokens) - Stateless authentication
· Spring Mail - Email service integration
· Spring Scheduler - Automated task scheduling
· Hibernate - ORM for database operations

Database

· MySQL - Relational database management system
· JPA/Hibernate - Object-relational mapping

Development Tools

· Postman/Insomnia - API testing
· Git - Version control
· Maven - Build automation and dependency management
· IntelliJ IDEA - Java IDE
· VS Code - Frontend development
· Browser DevTools - Debugging and performance monitoring

📁 Project Structure

Frontend Architecture

```
src/
├── components/          # Reusable UI components
│   ├── ui/             # Base components (buttons, forms, etc.)
│   ├── layouts/        # Layout components
│   └── features/       # Feature-specific components
├── pages/              # Page components
│   ├── Customer/       # Customer-facing pages
│   ├── Admin/          # Admin-facing pages
│   └── LandingPage/    # Public pages
├── services/           # API service layers
│   ├── api.Booking.js
│   ├── api.Login.js
│   └── api.Register.js
├── hooks/              # Custom React hooks
├── utils/              # Utility functions
└── assets/             # Static assets
```

Backend Architecture

```
src/main/java/com/example/hotel_booking_system_backend/
├── controller/         # REST API controllers
├── service/            # Business logic layer
├── repository/         # Data access layer (JPA)
├── model/              # Entity and DTO classes
│   ├── entity/         # JPA entities
│   ├── dto/            # Data transfer objects
│   ├── request/        # Request POJOs
│   └── response/       # Response POJOs
├── security/           # Security configuration
└── config/             # Application configuration
```

🔧 Key Technical Implementations

1. Authentication System

· Dual Authentication: Separate flows for customers and admins
· Token Management: Custom token format with role-based access
· Session Persistence: Combined sessionStorage and localStorage for user data

2. Booking Engine

· Availability Algorithm: Real-time room availability calculation
· Date Conflict Prevention: Overlap detection for bookings
· Price Calculation: Dynamic pricing with discount application
· Room Allocation: Smart room number assignment

3. Inventory Management

· Real-time Sync: Live updates between bookings and availability
· Emergency Fix Tools: Admin tools to correct inventory discrepancies
· Occupancy Tracking: Automated check-in/check-out processing

4. Payment Processing

· Multiple Payment Methods: Cash, card, and partial payments
· Transaction Tracking: Unique transaction IDs and payment notes
· Status Management: Payment status synchronization with booking status

5. Email Service

· Automated Notifications: Booking confirmations and updates
· Customer Welcome: New customer account setup emails
· Walk-in Confirmations: Instant booking confirmations for walk-ins

🚀 Setup & Installation

Prerequisites

· Node.js 16+ and npm/yarn
· Java 17 JDK
· MySQL 8.0+
· Maven 3.6+

Backend Setup

```bash
# Clone repository
git clone <repository-url>

# Navigate to backend
cd hotel-booking-backend

# Configure database in application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/hotel_db
spring.datasource.username=root
spring.datasource.password=yourpassword

# Build and run
mvn clean install
mvn spring-boot:run
```

Frontend Setup

```bash
# Navigate to frontend
cd hotel-booking-frontend

# Install dependencies
npm install

# Configure API endpoint
# Update API base URL in services/

# Start development server
npm run dev
```

🔗 API Endpoints

Authentication

· POST /api/v1/login - Customer login
· POST /api/v1/admin/login - Admin login
· POST /api/v1/register - Customer registration
. Post /api/v1/super-admin - Super Admin login

Bookings

· GET /api/v1/bookings - Get all bookings (admin)
· GET /api/v1/bookings/user/{userId} - Get user bookings
· POST /api/v1/bookings - Create booking
· POST /api/v1/bookings/walk-in - Create walk-in booking
· PUT /api/v1/bookings/{id}/checkin - Manual check-in
· PUT /api/v1/bookings/{id}/checkout - Manual check-out

Rooms

· GET /api/v1/rooms - Get all rooms
· GET /api/v1/rooms/availability - Check room availability
· POST /api/v1/bookings/emergency-fix-room/{roomId} - Fix room availability

Customers

· GET /api/v1/customers - Get all customers
· GET /api/v1/customers/{id} - Get customer by ID

📊 Database Schema

Main Entities

· UserRegister - Customer accounts
· Admin - Administrator accounts
. SuperAdmin - Super Admin account
· Rooms - Room inventory and details
· Booking - Booking records (regular and walk-in)
· RoomNumber - Individual room number tracking

Relationships

· One-to-Many: User → Bookings
· One-to-Many: Admin → Walk-in Bookings
· One-to-Many: Rooms → Bookings
· Many-to-One: Booking → Room

🎨 UI/UX Features

Design System

· Responsive Design: Mobile-first approach
· Consistent Styling: Tailwind CSS utility classes
· Interactive Components: Hover states, loading indicators
· Visual Feedback: Toast notifications, form validation
· Progressive Disclosure: Multi-step forms for complex operations

User Experience

· Intuitive Navigation: Clear hierarchy and breadcrumbs
· Real-time Updates: Live availability and status changes
· Error Prevention: Form validation and confirmation dialogs
· Accessibility: Semantic HTML and keyboard navigation support

🛡️ Security Features

Authentication & Authorization

· Role-based access control (CUSTOMER, ADMIN, SUPER_ADMIN)
· JWT token validation
· Session management with fallback mechanisms
· Secure password storage (backend hashing)

Data Protection

· Input validation and sanitization
· SQL injection prevention (JPA parameterized queries)
· XSS protection through React's built-in escaping
· Secure API endpoints with proper authorization checks

🔄 State Management

Frontend State

· React Hooks: useState, useEffect, useContext
· Component State: Local state for UI interactions
· Browser Storage: sessionStorage for temporary data, localStorage for persistence
· URL State: React Router for navigation state

Data Flow

1. User interaction triggers API calls
2. API responses update component state
3. State changes trigger UI re-renders
4. Important data persisted to browser storage

📈 Performance Optimizations

Frontend

· Code Splitting: React lazy loading for routes
· Image Optimization: Proper sizing and lazy loading
· Bundle Optimization: Tree shaking and minification
· Caching: Browser caching for static assets

Backend

· Database Indexing: Optimized queries with proper indexes
· Connection Pooling: Efficient database connection management
· Caching Layer: Spring Cache for frequently accessed data
· Asynchronous Processing: Email sending and scheduled tasks

🧪 Testing Strategy

Manual Testing

· Functional Testing: All user flows and features
· Integration Testing: API endpoint testing with Postman
· UI Testing: Cross-browser compatibility and responsiveness
· Edge Cases: Error scenarios and boundary conditions

Test Coverage

· Authentication flows
· Booking creation and management
· Room availability calculations
· Payment processing
· Admin operations

📚 Lessons Learned

Technical Challenges Solved

1. Inventory Synchronization: Developed algorithm to keep room availability in sync with bookings
2. Walk-in vs Online Bookings: Created unified booking system supporting both customer types
3. Real-time Updates: Implemented live availability without WebSockets using frequent API polling
4. Emergency Recovery: Built admin tools to fix data inconsistencies in production

Best Practices Implemented

· Consistent error handling across frontend and backend
· Comprehensive logging for debugging production issues
· Proper separation of concerns in code architecture
· Documentation of complex business logic

🔮 Future Enhancements

Planned Features

· Online Payment Integration: Stripe/PayPal for card payments
· Room Service Management: Order food and services from room
· Review System: Customer ratings and reviews
· Loyalty Program: Points and rewards system
· Multi-language Support: Internationalization
· Mobile App: React Native mobile application
· Analytics Dashboard: Advanced reporting and insights
· Automated Reporting: Daily/weekly/monthly reports

Technical Improvements

· Microservices Architecture: Split into booking, payment, notification services
· Real-time Updates: WebSocket implementation for live updates
· Docker Containerization: Simplified deployment
· CI/CD Pipeline: Automated testing and deployment
· Load Testing: Performance optimization for high traffic

👥 Team & Contribution

Single Developer Project - Full-stack development including:

· Database design and implementation
· Backend REST API development
· Frontend UI/UX design and implementation
· DevOps and deployment configuration
· Testing and documentation

📄 License

This project is proprietary and developed for portfolio demonstration purposes.

---

Connect with me on LinkedIn to discuss this project or collaboration opportunities!

---

Last Updated: January 2024
Technology Stack: React, Spring Boot, MySQL, Tailwind CSS
Project Status: Production Ready with Ongoing Enhancements
