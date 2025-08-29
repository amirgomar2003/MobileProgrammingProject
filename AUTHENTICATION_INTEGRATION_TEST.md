# Authentication Integration Test Guide

## Updated Authentication System

The authentication system has been significantly improved to handle refresh tokens automatically. Here's what was implemented:

### **1. Automatic Token Refresh in API Interceptor**

**Location**: `ApiClient.kt` - `authInterceptor`

**Key Improvements**:
- **401 Detection**: Automatically detects when access token expires
- **Refresh Attempt**: Tries to refresh the token using refresh token
- **Request Retry**: Re-attempts the original request with new access token
- **Fallback**: Only clears tokens if refresh fails completely

**How it works**:
1. User makes API request with expired access token
2. Server returns 401 Unauthorized
3. Interceptor catches 401 and attempts token refresh
4. If refresh succeeds, original request is retried with new token
5. If refresh fails, tokens are cleared and user sees "Session expired"

### **2. Enhanced Error Handling in NotesViewModel**

**Key Features**:
- **Authentication Detection**: Recognizes authentication-related errors
- **User-Friendly Messages**: Shows "Session expired. Please log in again." instead of technical errors
- **Consistent Handling**: All API calls use the same error handling pattern

### **3. Integration with Existing AuthViewModel**

The refresh token mechanism works seamlessly with the existing `AuthViewModel`:
- `AuthViewModel.tryRefreshToken()` still works for manual refresh
- `AuthViewModel.refreshUserInfo()` benefits from automatic refresh
- Login state is maintained correctly

## Testing the Implementation

### **Test Case 1: Expired Access Token (Still Valid Refresh Token)**

**Expected Behavior**: 
- Access token expires after ~15 minutes
- First API call triggers automatic refresh
- User continues working without interruption
- No "Not authenticated" error shown

**Steps to Test**:
1. Login to the app
2. Wait for access token to expire (~15 minutes) OR manually expire it in Django admin
3. Try to load notes, create/edit/delete notes
4. Should work seamlessly without login prompt

### **Test Case 2: Expired Refresh Token (After ~2+ Days)**

**Expected Behavior**:
- Both access and refresh tokens expire
- Automatic refresh fails
- User sees "Session expired. Please log in again."
- App clears tokens and requires re-login

**Steps to Test**:
1. Login to the app
2. Wait 2+ days OR manually expire refresh token in Django admin
3. Try to load notes
4. Should show "Session expired" message and require login

### **Test Case 3: Network Issues During Refresh**

**Expected Behavior**:
- If refresh fails due to network issues
- User sees appropriate network error message
- Tokens are cleared and re-login is required

## Backend Configuration Needed

Ensure your Django backend has proper token expiration settings:

```python
# settings.py
SIMPLE_JWT = {
    'ACCESS_TOKEN_LIFETIME': timedelta(minutes=15),  # Short for testing
    'REFRESH_TOKEN_LIFETIME': timedelta(days=7),     # Adjust as needed
    'ROTATE_REFRESH_TOKENS': True,
    'BLACKLIST_AFTER_ROTATION': True,
}
```

## Verification Points

### **✅ Automatic Refresh Working**:
- No "Not authenticated" errors for first ~15 minutes after login
- Seamless continuation of app usage
- Network logs show token refresh calls (in Android Studio logcat)

### **✅ Proper Error Handling**:
- Clear "Session expired" messages when refresh fails
- No technical error messages shown to users
- App returns to login screen appropriately

### **✅ State Consistency**:
- AuthViewModel and NotesViewModel stay in sync
- User info remains available after token refresh
- No data loss during automatic refresh

## Advanced Features

### **Background Refresh Prevention**:
The interceptor avoids infinite loops by:
- Not attempting refresh for refresh token endpoints
- Using separate HTTP client for refresh calls
- Proper error handling and fallback

### **Performance Optimization**:
- Minimal overhead for normal requests
- Synchronous refresh to avoid race conditions
- Proper resource cleanup

## Monitoring and Debugging

### **Android Studio Logcat**:
Look for these log entries to verify functionality:
```
- "Authorization: Bearer [token]" - Normal API calls
- "POST /api/auth/token/refresh/" - Automatic refresh attempts
- "401 Unauthorized" - Token expiration detection
```

### **Django Server Logs**:
Monitor for:
- Successful token refresh calls
- 401 responses for expired tokens
- Token blacklisting (if enabled)

## Expected User Experience

### **Before Fix**:
❌ "Not authenticated" error after 15 minutes
❌ Must manually login again frequently
❌ Interrupted workflow

### **After Fix**:
✅ Seamless usage for full refresh token lifetime (days)
✅ Automatic background token management  
✅ Clear session expiration messages only when necessary
✅ Uninterrupted user experience

The authentication system now provides a production-ready experience where users can work continuously without authentication interruptions, while still maintaining security through proper token expiration and refresh mechanisms.
