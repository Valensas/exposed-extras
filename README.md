# Exposed Extras

A library that adds extra features to [Exposed](https://github.com/jetbrains/exposed).

## Installation

```kotlin
implementation("com.valensas:exposed-extras:0.1.0")
```

## Usage

### Column types

```kotlin
enum class MyEnum {
    Value1,
    Value2
}
object MyTable {
    // Define a column of type array
    val arrayColumn = array<Int>("array_column", IntegerColumnType())
    // Define a column of type interval
    val intervalColumn = interval("interval_column")
    // Define a column with an enum UDT
    val enumColumn = enum<MyEnum>("enum_column", "enum_type")
    // Define a column of type inet
    val inetColumn = inet("inet_column")
    // Define a column of type jsonb with jackson to serialize/deserialize
    val jsonbColumn = jsonb("jsonb_column", jacksonObjectMapper())
}
```

### Tables

`CrudTable` defines common methods for CRUD operations on a table:

```kotlin

data class UserRecord(
    val id: Long?,
    val name: String
)

object UserTable: CrudTable<UserRecord, Long>("users") {
    override val id: Column<EntityID<Long>> = long("id").entityId()
    val name = text("name")
    
    override fun rowToRecord(row: ResultRow): UserRecord =
        UserRecord(
            id = row[id].value,
            name = row[name]
        )

    override fun updateRowWithRecord(
        statement: UpdateStatement,
        record: UserRecord
    ) {
        statement[name] = record.name
    }

    override fun insertRowWithRecord(
        statement: InsertStatement<EntityID<Long>>,
        record: UserRecord
    ) {
        statement[name] = record.name
        super.insertRowWithRecord(statement, record)
    }   
}

// Insert a row
val userId = UserTable.insert(UserRecord(id=null, name="some_user"))
// Find a row by id
val user1 = UserTable.findOneById(userId, forUpdate=false)
// List all row
val allUsers = UserTable.findAll()
// Update a row by id
val updateCount = UserTable.update(userId, user1.copy(name="some_other_name"))
// Delete a row by id
val deleteCount = UserTable.deleteById(userId)
```

`AuditableTable` allows to keep track when a row was created or updated and by whom:

```kotlin
data class AuditableUserRecord(
    val id: Long?,
    val name: String,
    val actor: String
)

object AuditableUserTable: AuditableTable<AuditableUserRecord, Long, String>("users", "anonymous") {
    override val id: Column<EntityID<Long>> = long("id").entityId()
    override val createdBy: Column<String> = text("created_by")
    override val updatedBy: Column<String?> = text("updated_by").nullable()
    
    val name = text("name")
    
    override fun getPrincipal(record: AuditableUserRecord): String {
        return record.actor
    }
    
    override fun rowToRecord(row: ResultRow): AuditableUserRecord =
        UserRecord(
            id = row[id].value,
            name = row[name],
            actor = row[updatedBy] ?: row[createdBy]
        )

    override fun updateRowWithRecord(
        statement: UpdateStatement,
        record: AuditableUserRecord
    ) {
        statement[name] = record.name
        // Do NOT forget to call super!
        super.updateRowWithRecord(statement, record)
    }

    override fun insertRowWithRecord(
        statement: InsertStatement<EntityID<Long>>,
        record: AuditableUserRecord
    ) {
        statement[name] = record.name
        // Do NOT forget to call super!
        super.insertRowWithRecord(statement, record)
    }   
}
```

### Querying helpers

You can use the `upsert` and `upsertAndGetId` methods on `IdTable` to perform an `insert ... on conflict do nothing / set ...` query:

```kotlin
object UniqueTable: CrudTable<UniqueRecord, Long>("unique_table") {
    override val id: Column<EntityID<Long>> = long("id").entityId().databaseGenerated()
    val name = text("name").uniqueIndex("idx_unique_table_on_name")
    val value = integer("value")

    override fun rowToRecord(row: ResultRow): UniqueRecord {
        return UniqueRecord(
            id = row[id].value,
            name = row[name],
            value = row[value]
        )
    }

    override fun updateRowWithRecord(statement: UpdateStatement, record: UniqueRecord) {
        statement[name] = record.name
        statement[value] = record.value
    }

    override fun insertRowWithRecord(statement: InsertStatement<EntityID<Long>>, record: UniqueRecord) {
        statement[name] = record.name
        statement[value] = record.value
    }
}

data class UniqueRecord(
    val id: Long?,
    val name: String,
    val value: Int
)

var record = UniqueRecord(id = null, name = "something", value = 0)
val recordId = transaction {
    // First insert, on conflict do nothing
    val recordId = UniqueTable.upsertAndGetId(
        conflictColumns = arrayOf(UniqueTable.name),
        updateOnConflict = false,
    ) {
        it[name] = record.name
        it[value] = record.value
    }
    commit()
    recordId
}!!

record = record.copy(value = 1)
transaction {
    // Second insert, on conflict update the value
    UniqueTable.upsert(
        conflictColumns = arrayOf(UniqueTable.name),
        updateOnConflict = true,
        updateColumns = arrayOf(UniqueTable.value)
    ) {
        it[name] = record.name
        it[value] = record.value
    }

    val savedRecord = UniqueTable.findOneById(recordId)!!
    assertEquals(record.value, savedRecord.value)
    commit()
}
```