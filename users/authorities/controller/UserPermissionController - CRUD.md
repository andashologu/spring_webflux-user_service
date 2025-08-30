## CREATE

### 1. Endpoint 1 "/"

```java
@PostMapping
```
Input: RequestBody - permissions
```json
[
  { "name": "PERMISSION_NAME", "description": "PERMISSION_DESCRIPTION"},
  more...
]
```
Output: permissions
```json
[
    {
        "id": 999,
        "name": "PERMISSION_NAME",
        "description": "PERMISSION_DESCRIPTION"
    },
    more...
]
```

## READ

### 1. Endpoint 2 "/{name}"
```java
@GetMapping("/{name}") 
```
Input: PathVariable - name

Output: permission
```json
{
    "id": 999,
    "name": "PERMISSION_NAME",
    "description": "PERMISSION_DESCRIPTION"
}
```

### 2. Endpoint 3 "/"
```java
@GetMapping
```
Output:
```json
[
    {
        "id": 999,
        "name": "PERMISSION_NAME",
        "description": "PERMISSION_DESCRIPTION"
    },
    more....
]
```

## UPDATE

### 1. Endpoint 4 "/"

```java
@PutMapping("/")
```
Input: RequestBody - roleEntity
```json
{
  "id": 999,
  "name": "PERMISSION_NAME",
  "description": "PERMISSION_DESCRIPTION"
}
```
Output: roleEntity
```json
{
  "id": 999,
  "name": "RPERMISSION_NAME",
  "description": "PERMISSION_DESCRIPTION"
}
```

## DELETE

### 1. Endpoint 5 "/{id}"
```java
@DeleteMapping("/{id}") 
```
Input: PathVariable - id

### 2. Endpoint 6 "/"
```java
@DeleteMapping
```

