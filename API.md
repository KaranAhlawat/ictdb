# Domain Models

## Users

### Anonymous User
* None

### SignedIn User
* User ID - Required
* Username - Required
* Email - Optional
* Password - Optional
* Favorites - list

## Conference
* Talk ID - Required
* User ID - Required
* Name/Title - Required
* Link - Required
* Short Description - Optional
* Speaker - Optional
* Tags - Optional
* Organizer - Optional

# APIs

BASE = /api/version/

## User API

BASE = /users/

1. Login = /users/login
   1. Email + Password
   2. Google
   3. GitHub
2. Logout = /users/logout
3. Get user information = /users/id
4. List user favorite talks = /users/id/favorites
5. Add talk to user favorites = /users/id/favorites/talk-id
6. Remove talk from user favorites = /users/id/favorites/talk-id
