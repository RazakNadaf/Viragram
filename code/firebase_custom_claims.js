/**
 * Server-side Firebase Auth Custom Claims Migration Script for Viragram (viragram-78116)
 *
 * Assigns role = "authenticated" custom claim to all existing Firebase users so
 * Supabase Storage RLS policies checking (auth.jwt() ->> 'role') = 'authenticated' accept uploads.
 */

const { initializeApp, cert } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');
const path = require('path');
const fs = require('fs');

const serviceAccountPath = path.join(__dirname, 'serviceAccountKey.json');

if (!fs.existsSync(serviceAccountPath)) {
    console.error('\n[ERROR] Missing serviceAccountKey.json file!');
    console.error('Please download your Firebase Admin service account key from:');
    console.error('Firebase Console -> Project Settings -> Service Accounts -> Generate new private key');
    console.error(`And place it at: ${serviceAccountPath}\n`);
    process.exit(1);
}

const serviceAccount = require(serviceAccountPath);

if (serviceAccount.project_id !== 'viragram-78116') {
    console.error(`\n[ERROR] Mismatched Firebase project! Expected 'viragram-78116', got '${serviceAccount.project_id}'`);
    process.exit(1);
}

initializeApp({
    credential: cert(serviceAccount)
});

const auth = getAuth();

async function setRoleForExistingUsers() {
    console.log('Starting custom claim migration for project viragram-78116...');
    let nextPageToken;
    let totalUpdated = 0;

    do {
        const listUsersResult = await auth.listUsers(1000, nextPageToken);
        for (const userRecord of listUsersResult.users) {
            await auth.setCustomUserClaims(userRecord.uid, {
                role: 'authenticated'
            });
            totalUpdated++;
            console.log(`[${totalUpdated}] Assigned role=authenticated to user: ${userRecord.uid} (${userRecord.email || 'no-email'})`);
        }
        nextPageToken = listUsersResult.pageToken;
    } while (nextPageToken);

    console.log(`\n[SUCCESS] Successfully assigned role=authenticated to ${totalUpdated} user(s) in viragram-78116.`);
}

setRoleForExistingUsers().catch((error) => {
    console.error('\n[ERROR] Custom claim migration failed:', error);
    process.exit(1);
});