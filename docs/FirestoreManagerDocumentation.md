# BudgetBee FirestoreManager Documentation

## Overview
The FirestoreManager is a singleton class that provides a centralized interface for interacting with Firebase Firestore database. 
It handles all data operations related to users, expenses, categories, and goals in the BudgetBee application.

## User Session Tracking
The application uses Firebase Authentication to manage user sessions. When a user logs in:
1. Their authentication state is managed by `FirebaseAuthManager`
2. The user's ID (uid) is used as the primary key for all their data in Firestore
3. All data operations are tied to the current user's session through their uid

## Data Collections
The Firestore database is organized into several collections:
- `users`: Stores user profile information
- `expenses`: Stores user's expense records
- `categories`: Stores expense categories created by the user
- `goals`: Stores user's budget goals

## Data Gathering Process

Before performing any FirestoreManager operations, ensure you have gathered the necessary data. Here's how to gather data for each type of operation:

### 1. User Data
When working with user operations:
1. Get the current user ID:
``` kotlin
val userId = FirebaseAuth.getInstance().currentUser?.uid
```
2. For user profile data, gather:
   - `userEmail`: The user's email address
   - `userName`: The user's display name

### 2. Expense Data
When adding or retrieving expenses:
1. Get the user ID as above
2. For each expense, gather:
   - `amount`: The expense amount (Double)
   - `description`: Brief description of the expense (String)
   - `date`: Date of the expense (Timestamp)
   - `category`: Reference to the expense category (String)
   - `userId`: The user's ID (automatically included)

### 3. Category Data
When working with expense categories:
1. Get the user ID
2. For each category, gather:
   - `name`: The category name (String)
   - `iconResId`: Resource ID for the category icon (Int)
   - `userId`: The user's ID (automatically included)

### 4. Goal Data
When setting or retrieving goals:
1. Get the user ID
2. For goals, gather:
   - `minMonthlyGoal`: Minimum monthly budget goal (Double)
   - `maxMonthlyGoal`: Maximum monthly budget goal (Double)
   - `otherGoal`: Any additional goal information (String)

## Using FirestoreManager

### 1. Getting the Current User
``` kotlin
val userId = FirebaseAuth.getInstance().currentUser?.uid
```

### 2. User Operations
``` kotlin
// Add new user
FirestoreManager.addUser(userId, user) { success ->
    if (success) {
        // User added successfully
    }
}

// Get user information
FirestoreManager.getUser(userId) { user ->
    if (user != null) {
        // User found
    }
}
```

### 3. Expense Operations
``` kotlin
// Add expense
FirestoreManager.addExpense(expense) { success ->
    if (success) {
         // Expense added successfully
    }
}

// Get user's expenses
FirestoreManager.getExpenses(userId) { expenses ->
    // Process expenses list
}
```

### 4. Category Operations
Here's a complete example of how to add a new category:

``` kotlin
// 1. First, get the current user ID
val userId = FirebaseAuth.getInstance().currentUser?.uid
    ?: throw IllegalStateException("User not logged in")

// 2. Create a new category object with the required data
val newCategory = Category(
    userId = userId,  // Automatically include the user ID
    name = "Groceries",  // Category name from user input
    iconResId = R.drawable.ic_groceries  // Resource ID for grocery icon
)

// 3. Add the category to Firestore
FirestoreManager.addCategory(newCategory) { success ->
    if (success) {
        // Show success message to user
        Toast.makeText(context, "Category added successfully", Toast.LENGTH_SHORT).show()
    } else {
        // Show error message
        Toast.makeText(context, "Failed to add category", Toast.LENGTH_SHORT).show()
    }
}
```

// Add category
FirestoreManager.addCategory(category) { success ->
    if (success) {
        // Category added successfully
    }
}

// Get user's categories
FirestoreManager.getCategories(userId) { categories ->
    // Process categories list
}

### 5. Goal Operations
``` kotlin
// Set user's goal
FirestoreManager.setGoal(userId, goal) { success ->
    if (success) {
        // Goal set successfully
    }
}

// Get user's goal
FirestoreManager.getGoal(userId) { goal ->
    if (goal != null) {
        // Process goal data
    }
}
```

## Important Notes
1. All operations are asynchronous and use callback functions
2. Each operation includes success/error handling through the callback parameters
3. All data is automatically tied to the current user's session through their uid
4. The FirestoreManager is a singleton, ensuring consistent access to the database across the application

## Error Handling
All FirestoreManager operations include error handling through the callback parameters. Make sure to handle both success and failure cases appropriately in your UI.

## Security
1. All data operations require a valid user ID
2. Data is automatically filtered by user ID to ensure users only access their own data
3. Firebase Security Rules should be configured to enforce these security measures

## Best Practices
1. Always check if the user is authenticated before performing database operations
2. Handle all callback cases (success/failure) appropriately
3. Use proper error handling and user feedback
4. Keep the user's session state in sync with Firebase Auth state

## Example Usage in ViewModel
``` kotlin
class HomeViewModel : ViewModel() {
    private val _text = MutableLiveData<String>()
    val text: LiveData<String> = _text

    fun loadUserGreeting() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirestoreManager.getUser(uid) { user ->
            if (user != null) {
                _text.postValue("Welcome back, ${user.userName}")
            } else {
                _text.postValue("Welcome!")
            }
        }
    }
}
```

This documentation provides a comprehensive guide for developers working with the FirestoreManager in the BudgetBee application. Always ensure proper error handling and user session management when implementing new features.
