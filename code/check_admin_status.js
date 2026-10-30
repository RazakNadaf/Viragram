/**
 * Diagnostic script to check Firebase Auth & Firestore status for College Admin UID 8bGcWy66RjN5BM8cIU5r9sR2yR43
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

async function checkStatus() {
    const userRecord = await getAuth().getUser(uid);
    const db = getFirestore();
    const docSnap = await db.collection('users').doc(uid).get();

    console.log('\n=== VIRAGRAM ADMIN STATUS REPORT ===');
    console.log('UID:', userRecord.uid);
    console.log('Email:', userRecord.email);
    console.log('Email Verified (Firebase Auth):', userRecord.emailVerified);
    console.log('Custom Claims:', userRecord.customClaims);
    console.log('Firestore Doc Exists:', docSnap.exists);
    if (docSnap.exists) {
        console.log('Firestore Data:', docSnap.data());
    }
}

checkStatus().catch(console.error);