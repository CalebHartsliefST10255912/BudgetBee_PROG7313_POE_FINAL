# Firebase Storage Documentation

This document provides guidelines for handling image storage operations using Firebase Storage in the BudgetBee application.

## Collecting Images

Before uploading images to Firebase Storage, you need to collect them from various sources. Here are the main methods for collecting images in Android:

### 1. Camera Capture

To capture images using the device camera:

1. Request camera permissions in your manifest:
``` xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="true" />
```

2. Create an intent to launch the camera:
``` kotlin
val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
startActivityForResult(intent, CAMERA_REQUEST_CODE)
```

3. Handle the result in onActivityResult:
``` kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
        // Get the captured image
        val imageBitmap = data?.extras?.get("data") as Bitmap
        // Save the bitmap to a file before uploading
        saveBitmapToFile(imageBitmap)
    }
}
```

### 2. Gallery Selection

To select images from the device gallery:

1. Request storage permissions:
``` xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

2. Create an intent to open gallery:
``` kotlin
val intent = Intent(Intent.ACTION_PICK)
intent.type = "image/*"
startActivityForResult(intent, GALLERY_REQUEST_CODE)
```

3. Handle the result:
``` kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
        val imageUri = data?.data
        // Convert Uri to File before uploading
        val imageFile = uriToFile(imageUri)
    }
}
```

### 3. Image Compression

Before uploading images, consider compressing them to optimize storage and bandwidth:

``` kotlin
fun compressImage(file: File, targetSize: Int = 512): File {
    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
    val width = bitmap.width
    val height = bitmap.height
    val scale = Math.min(width.toDouble() / targetSize, height.toDouble() / targetSize)
    
    val resizedBitmap = Bitmap.createScaledBitmap(
        bitmap,
        (width / scale).toInt(),
        (height / scale).toInt(),
        true
    )
    
    val outputStream = FileOutputStream(file)
    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
    outputStream.close()
    return file
}
```

## Firebase Storage Manager

The `FirebaseStorageManager` class provides methods for managing images in Firebase Storage. It follows the singleton pattern to ensure only one instance is created.

### Getting the Instance

``` kotlin
val storageManager = FirebaseStorageManager.getInstance()
```

### Uploading Images

To upload an image:

1. Ensure you have a valid `File` object containing the image data
2. Choose an appropriate folder name based on the image type (e.g., "profile_images", "expense_receipts")
3. Call the `uploadImage` method:

``` kotlin
val imageUrl = storageManager.uploadImage(imageFile, "profile_images")
```

The method returns:
- A download URL string if successful
- `null` if there was an error

### Deleting Images

To delete an image from storage:

``` kotlin
storageManager.deleteImage(imageUrl)
```

Pass the complete download URL of the image you want to delete.

### Retrieving Images

To get an image URL from storage:

``` kotlin
val imageUrl = storageManager.getImageUrl("profile_images/${userId}_profile.jpg")
```

### Best Practices

1. **User Authentication**:
   - Always check if user is logged in using `FirebaseAuthManager.getCurrentUserId()`
   - Handle cases where user ID might be null
   - Implement proper error handling for unauthorized access

2. **Image Naming**: 
   - Use UUID for unique filenames
   - Include user ID in the filename for user-specific images
   - Use appropriate file extensions (.jpg, .png, etc.)

3. **Folder Structure**:
   - Organize images into logical folders
   - Use folder names that reflect the image type (e.g., "profile_images", "receipts", "documents")
   - Include user ID in folder paths for user-specific images
   - Use consistent naming conventions across the app

4. **Error Handling**:
   - Always handle null returns from upload operations
   - Implement retry logic for network failures
   - Log errors appropriately
   - Handle authentication-related errors

5. **Security**:
   - Use Firebase Storage security rules to control access
   - Never expose storage paths directly to users
   - Validate image types before upload
   - Implement proper access control based on user roles
   - Use secure file naming conventions

### Example Usage with Current User

``` kotlin
// Get current user ID using FirebaseAuthManager
val userId = FirebaseAuthManager.getCurrentUserId()

// Upload profile image
val profileImage = File("path/to/profile.jpg")
val profileImageUrl = storageManager.uploadImage(profileImage, "users/$userId/profile")

// Get user's profile image
val profileImageRef = "users/$userId/profile/${userId}_profile.jpg"
val profileImageUrl = storageManager.getImageUrl(profileImageRef)

// Delete user's profile image
storageManager.deleteImage(profileImageUrl)

// Handle cases where user is not logged in
if (userId == null) {
    // Handle error: user must be logged in to access storage
    Toast.makeText(context, "Please log in to access storage", Toast.LENGTH_SHORT).show()
    return
}
```

## Important Notes

1. All operations are suspend functions and should be called within a coroutine scope
2. Ensure proper error handling in production code
3. Consider implementing caching for frequently accessed images
4. Follow Firebase Storage best practices for security and performance
5. Always verify user authentication status before storage operations
6. Implement proper error handling for authentication-related failures
