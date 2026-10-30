/**
 * Server-side script for assigning Viragram Admin Roles (college_admin, club_admin) via Firebase Admin SDK.
 *
 * Usage:
 * node assign_admin_role.js <user_uid> <college_admin|club_admin|student>
 */

const { initializeApp, cert } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');
const { getFirestore } = require('firebase-admin/firestore');
const path = require('path');
const fs = require('fs');

const serviceAccountPath = path.join(__dirname, 'serviceAccountKey.json');
if (!fs.existsSync(serviceAccountPath)) {
    console.error('[ERROR] Missing serviceAccountKey.json file.');
    process.exit(1);
}

const serviceAccount = require(serviceAccountPath);
initializeApp({ credential: cert(serviceAccount) });

const uid = process.argv[2];
const role = process.argv[3] || 'college_admin';

if (!uid) {
    console.log('Usage: node assign_admin_role.js <USER_UID> <ROLE>');
    console.log('Roles: college_admin, club_admin, student');
    process.exit(1);
}

async function setAdminRole() {
    // 1. Set Custom Claim on Firebase Auth User
    await getAuth().setCustomUserClaims(uid, { role: role });
    console.log(`[Firebase Auth] Successfully assigned custom claim role='${role}' to UID: ${uid}`);

    // 2. Update Firestore Document users/{uid}
    const db = getFirestore();
    const userRef = db.collection('users').doc(uid);
    await userRef.set({ role: role, updatedAt: new Date() }, { merge: true });
    console.log(`[Firestore] Successfully updated users/${uid} document with role='${role}'`);

    // 3. Verify Auth Custom Claims & Firestore Document
    const userRecord = await getAuth().getUser(uid);
    console.log(`\n[VERIFICATION SUCCESS]`);
    console.log(`UID: ${userRecord.uid}`);
    console.log(`Email: ${userRecord.email || 'N/A'}`);
    console.log(`Custom Claims:`, userRecord.customClaims);
}

setAdminRole().catch((error) => {
    console.error('[ERROR] Failed to set admin role:', error);
    process.exit(1);
});