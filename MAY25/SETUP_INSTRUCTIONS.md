# MAY25 App - Setup Instructions ❤️

## Step 1: Firebase Setup
1. Go to https://console.firebase.google.com
2. Create a new project named "MAY25"
3. Enable these services:
   - Authentication → Email/Password
   - Realtime Database → Start in test mode
   - Storage → Start in test mode
   - Cloud Messaging (for notifications)
4. Add an Android app with package: `com.may25.app`
5. Download `google-services.json` and place it in: `MAY25/app/google-services.json`

## Step 2: Create the Two User Accounts
In Firebase Authentication, you can pre-create:
- user1@may25.com  (your account)
- user2@may25.com  (your friend's account)
Or let each person register from the app (only these emails are accepted).

## Step 3: Agora Voice Call Setup
1. Go to https://www.agora.io → Sign up free
2. Create a new project → Get your App ID
3. Open: `app/src/main/java/com/may25/app/activities/CallActivity.java`
4. Replace `YOUR_AGORA_APP_ID_HERE` with your real App ID

## Step 4: Firebase Database Rules
Go to Firebase Console → Realtime Database → Rules
Paste the contents of `firebase_rules/database.rules.json`

## Step 5: Build the App
1. Open Android Studio
2. File → Open → Select the MAY25 folder
3. Wait for Gradle sync
4. Click Run ▶ or Build → Generate Signed APK

## Features Summary
- 💬 Real-time private chat (text + images)
- 📞 Voice calling (Agora SDK)
- ⏱️ Days counter since 25/05/2023 (shown in drawer)
- 📸 Private shared photo gallery
- ❤️ Love-themed dark pink UI
- 🔒 Only 2 emails can log in (private)
- 🔔 Push notifications for new messages
