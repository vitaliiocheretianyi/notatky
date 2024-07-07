# MVC Architecture with SOLID and DRY Principles

To follow the SOLID and DRY principles within the MVC architecture, you'll need to create the following components for each model:

## 1. Models (Entities)

Entities will represent the database tables.

### Entities to create:
- User
- Note
- UserNote
- NoteChild
- TextNode
- ImageNode

## 2. Repositories

Repositories will be interfaces extending `JpaRepository` to provide CRUD operations.

### Repositories to create:
- UserRepository
- NoteRepository
- UserNoteRepository
- NoteChildRepository
- TextNodeRepository
- ImageNodeRepository

## 3. Services

Service classes will contain business logic and interact with the repositories.

### Services to create:
- UserService
- NoteService
- UserNoteService
- NoteChildService
- TextNodeService
- ImageNodeService

## 4. Controllers

Controllers will handle HTTP requests, call the appropriate service methods, and return responses.

### Controllers to create:
- UserController
- NoteController
- UserNoteController
- NoteChildController
- TextNodeController
- ImageNodeController

## 5. DTOs (Data Transfer Objects)

DTOs will be used to transfer data between the frontend and backend.

### DTOs to create:
- UserDTO
- NoteDTO
- UserNoteDTO
- NoteChildDTO
- TextNodeDTO
- ImageNodeDTO

## 6. Mappers

Mapper classes will be used to convert between entities and DTOs.

### Mappers to create:
- UserMapper
- NoteMapper
- UserNoteMapper
- NoteChildMapper
- TextNodeMapper
- ImageNodeMapper

## Endpoints to Implement

For each controller, you'll implement RESTful endpoints that correspond to the CRUD operations and any additional necessary operations.

### Example Endpoints:

**UserController:**
- `POST /users` - Create a new user
- `GET /users/{id}` - Get a user by ID
- `PUT /users/{id}` - Update a user
- `DELETE /users/{id}` - Delete a user

**NoteController:**
- `POST /notes` - Create a new note
- `GET /notes/{id}` - Get a note by ID
- `PUT /notes/{id}` - Update a note
- `DELETE /notes/{id}` - Delete a note

**UserNoteController:**
- `POST /usernotes` - Associate a user with a note
- `DELETE /usernotes` - Disassociate a user from a note

**NoteChildController:**
- `POST /notechildren` - Add a child to a note
- `GET /notechildren/{noteId}` - Get all children of a note
- `DELETE /notechildren/{id}` - Delete a child

**TextNodeController:**
- `POST /textnodes` - Create a text node
- `GET /textnodes/{id}` - Get a text node by ID
- `PUT /textnodes/{id}` - Update a text node
- `DELETE /textnodes/{id}` - Delete a text node

**ImageNodeController:**
- `POST /imagenodes` - Create an image node
- `GET /imagenodes/{id}` - Get an image node by ID
- `PUT /imagenodes/{id}` - Update an image node
- `DELETE /imagenodes/{id}` - Delete an image node

## Summary
- **Models**: Define entities for User, Note, UserNote, NoteChild, TextNode, and ImageNode.
- **Repositories**: Create repository interfaces for each entity.
- **Services**: Implement service classes for business logic.
- **Controllers**: Set up RESTful endpoints in controllers.
- **DTOs**: Use DTOs for data transfer between frontend and backend.
- **Mappers**: Implement mappers to convert between entities and DTOs.
