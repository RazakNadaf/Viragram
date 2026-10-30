/**
 * Server-side script to update College Admin email to razaknadaf31@gmail.com
 * for UID 8bGcWy66RjN5BM8cIU5r9sR2yR43 in project viragram-78116.
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

const uid = '8bGcWy66RjN5BM8cIU5r9sR2yR43';
const newEmail = 'razaknadaf31@gmail.com';

async function updateAdminEmail() {
    console.log(`Updating Firebase Auth email for UID: ${uid} to '${newEmail}'...`);

    // 1. Update Firebase Auth Email & reset emailVerified to false so link must be verified
    const updatedUser = await getAuth().updateUser(uid, {
        email: newEmail,
        emailVerified: false
    });

    // 2. Ensure custom claim role='college_admin' is assigned
    await getAuth().setCustomUserClaims(uid, { role: 'college_admin' });

    // 3. Update Firestore Document users/{uid}
    const db = getFirestore();
    await db.collection('users').doc(uid).set({
        email: newEmail,
        role: 'college_admin',
        updatedAt: new Date()
    }, { merge: true });

    // 4. Verify & Report Output
    const finalRecord = await getAuth().getUser(uid);
    const docSnap = await db.collection('users').doc(uid).get();

    console.log('\n=== ADMIN EMAIL UPDATE SUCCESS ===');
    console.log('UID:', finalRecord.uid);
    console.log('New Email:', finalRecord.email);
    console.log('Email Verified:', finalRecord.emailVerified);
    console.log('Custom Claims:', finalRecord.customClaims);
    console.log('Firestore Doc Data:', docSnap.data());
}

updateAdminEmail().catch((error) => {
    console.error('[ERROR] Failed to update admin email:', error);
    process.exit(1);
});