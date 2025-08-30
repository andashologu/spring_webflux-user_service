## Project Structure

> **users**
>> model
>>> `UserEnity.java`
>> repository
>>> `UserRepository.java`
>>> `CustomUserRepository.java`
>>> `CustomUserRepositotyImp.java`
>> service
>>> `UserService.java`
>> controller
>>> `UserController.java`
>> validation
>>> `Unique.java`
>>> `UniqueConstraint.java`
>> ***addresses**
>> ***authorities**
>> ***profiles**

## Validation - Model Entities

To do - show in the table format like below. Nb Column are seperated by |

1. Backend

- These fields are well-protected by annotations:

Field | Annotation & Reason
username | `@Pattern` ensures structure, `@UniquesUsername`
email | `@Email`, `@Size`, `@UniqueEmail`
mobileNumber | `@Pattern` restricts to digits only
countryCode | `@Pattern` for strict format
password | `@Pattern`, `@JsonProperty(WRITE_ONLY)` ensures safe input/output
createdAt etc. | System-managed, not user-supplied

- These fields are ignored for persistence:

Field | Reason
roles | `@Transient`
permissions | `@Transient`
profileEntity | `@Transient`

2. PSQL

`CHECK regex` |	On username, email, etc. | Bad format injection
`UNIQUE` | On username, email, etc.	| Duplicate records
`VARCHAR(n)` | Max length for each field | Oversized input (DoS/bloat)
`NOT NULL` | On password | Null passwords
`DEFAULT` | On timestamps + booleans | Enforced defaults

## CRUD Operations - Controllers

### CREATE

#### 1. Endpoint 1 "/users" tested

```java
@PostMapping
```

Input: RequestBody - userEntity
```json
{
  "username": "username",
  "email": "email@example_mail.com",
  "mobileNumber": "999999999",
  "countryCode": "+999",
  "password": "password",
  "roles": [
      {"name": "ROLE_NAME"},
      more....
    ],
  "permissions": [
      {"name": "PERMISSION_NAME"},
      more....
    ]
}
```
Output: UserEntity - savedUser
```json
{
    "id": 999,
    "username": "username",
    "email": "email@example_mail.com",
    "mobileNumber": "999999999",
    "countryCode": "+999",
    "roles": [
        {
            "id": 999,
            "name": "ROLE_NAME",
            "description": "ROLE_DESCRIPTION"
        },
        more...
    ],
    "permissions": [
        {
            "id": 999,
            "name": "PERMISSION_NAME",
            "description": "PERMISSION_DESCRIPTION"
        },
        more...
    ],
    "active": true/false, //default false
    "accountNonExpired": true/false,
    "accountNonLocked": true/false,
    "credentialsNonExpired": true/false,
    "createdAt": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
    "updatedAt": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
    "accessedAt": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
    "expiryDate": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
    "profileEntity": null,
    "addressEntity": null,
    "contactInfoValid": true/false
}
```
Comment: This method can also update if same id is passed through request body and will replace all of the entity fields

### READ

#### 1. Endpoint 2 "/users/id" tested

```java
@GetMapping("/{id:\\d+}")
```

Input: PathVariable - id

Output: ResponseEntity + Object
```json
{
    "id": 999,
    "username": "username",
    "email": "email@example_mail.com",
    "mobileNumber": "999999999",
    "countryCode": "+999",
    "roles": [
        {
            "id": 999,
            "name": "ROLE_NAME",
            "description": "ROLE_DESCRIPTION"
        },
        more...
    ],
    "permissions": [
        {
            "id": 999,
            "name": "PERMISSION_NAME",
            "description": "PERMISSION_DESCRIPTION"
        },
        more...
    ],
    "active": true/false, //default false
    "accountNonExpired": true/false,
    "accountNonLocked": true/false,
    "credentialsNonExpired": true/false,
    "createdAt": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
    "updatedAt": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
    "accessedAt": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
    "expiryDate": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
    "profileEntity": null,
    "addressEntity": null,
    "contactInfoValid": true/false
}
```

#### 2. Endpoint 3 "/users/username" tested

```java
@GetMapping("/{username:^(?!\\d+$)[A-Za-z0-9][A-Za-z0-9_]{0,29}$}")
```

Input: PathVariable - username

Output: ResponseEntity + Object 
```json
{
    "id": 999,
    "username": "username",
    "email": "email@example_mail.com",
    "mobileNumber": "999999999",
    "countryCode": "+999",
    "roles": [
        {
            "id": 999,
            "name": "ROLE_NAME",
            "description": "ROLE_DESCRIPTION"
        },
        more...
    ],
    "permissions": [
        {
            "id": 999,
            "name": "PERMISSION_NAME",
            "description": "PERMISSION_DESCRIPTION"
        },
        more...
    ],
    "active": true/false, //default false
    "accountNonExpired": true/false,
    "accountNonLocked": true/false,
    "credentialsNonExpired": true/false,
    "createdAt": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
    "updatedAt": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
    "accessedAt": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
    "expiryDate": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
    "profileEntity": null,
    "addressEntity": null,
    "contactInfoValid": true/false
```

#### 3. Endpoint 4 "/users" will test after profile and address are fully tested

```java
@GetMapping("/?cursor=&size=&sortBy=&direction=&search=&country=&city=&town=&street=")
```

tested filters: cursor, size, sortby, direction, search, 

Input (all optional): RequestParam cursor, size, sortBy **column name**, direction **ASC or DESC**, search, country, city, region, street

```json
[
    {
        "id": 999,
        "username": "username",
        "email": "username@example_mail.com",
        "mobileNumber": 999,
        "countryCode": 999,
        "roles": [
            {
                "id": 999,
                "name": "ROLE_NAME",
                "description": "ROLE_DESSCRIPTION"
            },
            more....
        ],
        "permissions": [
            {
                "id": 999,
                "name": "PERMISSION_NAME",
                "description": "PERMISSION_DESCRIPTION"
            },
            more....
        ],
        "active": true/false,
        "accountNonExpired": true/false,
        "accountNonLocked": true/false,
        "credentialsNonExpired": true/false,
        "createdAt": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "updatedAt": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "accessedAt": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "expiryDate": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "profileEntity": {
            "id": 999,
            "userId": 999,
            "firstname": "firstname",
            "lastname": "lastname",
            "profilePicture": "profilePicture",
            "bio": "bio",
            "website": "website",
            "preferences": "preferences",
            "settings": "settings",
            "createdAt": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "updatedAt": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "accessedAt": "yyyy-MM-dd'T'HH:mm:ss.SSSX"
        },
        "addressEntity": {
            "id": 12,
            "userId": 999,
            "country": "country",
            "region": "region",
            "city": "city",
            "street": "street",
            "unitNumber": "unitNumber",
            "zipCode": "zipCode",
            "type": "type",
            "additionalInfo": "additionalInfo",
            "latitude": "latitude",
            "longitude": "longitude",
            "createdAt": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "updatedAt": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "accessedAt": "yyyy-MM-dd'T'HH:mm:ss.SSSX"
        },
        "contactInfoValid": true/false
    },
    more....
]
```

### UPDATE

#### 1. Endpoint 5 "/users/id" tested

```java
@PatchMapping("/{id}")
```
Input: RequestBody - updates
```json
{
  "username": "username",
  "email": "email@example_mail.com",
  "mobileNumber": "999999999",
  "countryCode": "+999",
  "password": "password"
}
```
Output: ResponseEntity + Object (savedUpdates or Validation errors)
```json
{
  "username": "username",
  "email": "email@example_mail.com",
  "mobileNumber": "999999999",
  "countryCode": "+999",
  "password": "password"
}
```
Comments: any of these fields as this endpoint allows to update single fields

### DELETE

#### 1. Endpoint 6 "/users/id" tested

```java
@DeleteMapping("/{id}")
```
Input: PathVariable - id

#### 1. Endpoint 6 "/users/current_user" tested

```java
@DeleteMapping("/current_user")
```


## Pagination & Sorting - Controllers (additional annotations), AND Service or CustumRepository (logic)

1. Methods used

* cursor based pagination
* searching 
* filtering
* grouping 
* sorting

2. Anonymous variables

### controller

```java
    @RequestParam(required = false) String country,
    @RequestParam(required = false) String city,
    @RequestParam(required = false) String region,
    @RequestParam(required = false) String street 
```

### customRepository or service (logic)

```java

/* cursor based
* table_name = users
*/
sql.append(" WHERE users.id > :cursor ");

/* searching
* table_name = users > column_name = username
* table_name = profiles > column_names = firstname, lastname
*/
sql.append("""
    to_tsvector('simple', coalesce(users.username,'')) 
    OR
    to_tsvector('simple', coalesce(profiles.firstname,'') || ' ' || coalesce(profiles.lastname,'')
    """)

/* filtering
* column_name = country > fieldName = country
* column_name = city > fieldName = city
* column_name = region > fieldName = region
* column_name = street > fieldName = street
*/
sql.append(" country ILIKE '%'||TRIM(:country)||'%' ");
sql.append(" country ILIKE '%'||TRIM(:city)||'%' ");
sql.append(" country ILIKE '%'||TRIM(:region)||'%' ");
sql.append(" country ILIKE '%'||TRIM(:street)||'%' ");

/* grouping
* table_name = users > column_name = id
* table_name = profiles > column_name = id
* table_name = addresses > column_name = id
*/
sql.append(" GROUP BY users.id ");
sql.append(" GROUP BY profiles.id ");
sql.append(" GROUP BY addresses.id ");

/* sorting
* table_name = users > column_names = created_at, username, email
* table_name = profiles > column_names = firstname, lastname, fullname
* parent_table_name = users > column_name = id
*/
sql.append(" ORDER BY ").append("users.created_at") //OR
sql.append(" ORDER BY ").append("users.username") //OR
sql.append(" ORDER BY ").append("users.email") //OR
sql.append(" ORDER BY ").append("profiles.firstname") //OR
sql.append(" ORDER BY ").append("profiles.lastname") //OR
sql.append(" ORDER BY ").append("profiles.fullname") //OR
sql.append(" ORDER BY ").append("users.id") //OR

```

All the above method are optional and applied to neccessary endpoints.

### Indexing

users - username
profiles -  firstname and lastname
address - country, city, region, street

## Debugging

Class name | Method/s | Parent method/s
UserService.java | customValidation.validateAll, userEntity.setPassword, userRepository.save, userRepository.findById, customValidation.validate | saveUser, updateUser, 

## Exceptions

1. Controller

### createUser

#### ValidationException: 

- Trigger:
    userService.saveUser(userEntity) → customValidation.validateAll(userEntity):

```java
    if (!errors.isEmpty()) {
            System.out.println("errors: "+errors);
            throw new ValidationException(errors);
        }
```
- Handling: 400 BAD REQUEST

```java
.onErrorMap(ValidationException.class, ex -> new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getErrors().toString(), ex));
```
#### UserNotFoundException: 

- Trigger:
    userService.saveUser(userEntity) → userRepository.findById(validatedUserEntity.getId()).switchIfEmpty:

```java
    .switchIfEmpty(Mono.error(new UserNotFoundException("User with id " + validatedUserEntity.getId() + " not found")));
```
- Handling: 400 BAD REQUEST

```java
.onErrorMap(UserNotFoundException.class, ex -> new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex))
```
#### RoleNotFoundException: 

- Trigger:
    userRoleService.addRolesToSpecificUsers(rolesMap) → roleRepository.findById(id).switchIfEmpty

    OR

    userRoleService.addRolesToSpecificUsers(rolesMap) → roleRepository.findByName(userRole.getName()).switchIfEmpty:

```java
    .switchIfEmpty(Mono.error(new RoleNotFoundException("Role with id " + id + " not found")));

    //or

    .switchIfEmpty(Mono.error(new RoleNotFoundException("Role with name " + userRole.getName() + " not found")));
```
- Handling: 404 NOT_FOUND

```java
    .onErrorMap(UserNotFoundException.class, ex -> new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex))
```
#### PermissionNotFoundException: 

- Trigger:
    userPermissionService.addPermissionsToSpecificUsers(permissionsMap) → permissionRepository.findById(id).switchIfEmpty
    
    OR
    
    userPermissionService.addPermissionsToSpecificUsers(permissionsMap) → permissionRepository.findByName(userPermission.getName()).switchIfEmpty:

```java
    .switchIfEmpty(Mono.error(new PermissionNotFoundException("Permission with id " + id + " not found")));

    //Or

    .switchIfEmpty(Mono.error(new PermissionNotFoundException("Permission with name " + userPermission.getName() + " not found")));
```
- Handling: 404 NOT_FOUND

```java
    .onErrorMap(UserNotFoundException.class, ex -> new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex))
```

