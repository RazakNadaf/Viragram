/**
 * Express Backend Server for Viragram
 *
 * Provides authenticated ImageKit upload parameters for Viragram mobile client.
 * Uses Firebase Admin SDK to verify Firebase Authentication ID Tokens.
 *
 * IMPORTANT SECURITY RULE:
 * IMAGEKIT_PRIVATE_KEY MUST be provided via environment variable ONLY.
 * Never commit or expose private keys to client apps.
 */

const path = require('path');
const fs = require('fs');

// Ensure .env is explicitly loaded from server root
require('dotenv').config({ path: path.join(__dirname, '.env') });

const express = require('express');
const cors = require('cors');
const ImageKit = require('imagekit');
const { initializeApp, cert } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// 1. Initialize Firebase Admin SDK using existing serviceAccountKey.json
const serviceAccountPath = path.join(__dirname, 'serviceAccountKey.json');
if (fs.existsSync(serviceAccountPath)) {
    const serviceAccount = require(serviceAccountPath);
    initializeApp({
        credential: cert(serviceAccount)
    });
    console.log('Firebase Admin SDK initialized successfully.');
} else {
    console.warn('WARNING: serviceAccountKey.json not found. Firebase ID Token verification will be skipped in local test mode.');
}

// 2. Initialize ImageKit SDK with cleaned keys and default public fallback
const publicKey = (process.env.IMAGEKIT_PUBLIC_KEY || 'public_aorVltxNyRp0M43H6ifaLvAZC4s=').trim().replace(/^["']|["']$/g, '');
const privateKey = (process.env.IMAGEKIT_PRIVATE_KEY || '').trim().replace(/^["']|["']$/g, '');
const urlEndpoint = (process.env.IMAGEKIT_URL_ENDPOINT || 'https://ik.imagekit.io/gf66rtc00').trim().replace(/^["']|["']$/g, '');

if (!privateKey) {
    console.error('\n[ERROR] Missing IMAGEKIT_PRIVATE_KEY environment variable!');
    console.error('Please configure IMAGEKIT_PRIVATE_KEY in .env before running server.js.\n');
    process.exit(1);
}

const imagekit = new ImageKit({
    publicKey: publicKey,
    privateKey: privateKey,
    urlEndpoint: urlEndpoint
});

// 3. Authentication Middleware verifying Firebase ID Tokens
async function verifyFirebaseToken(req, res, next) {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ error: 'Unauthorized. Missing or invalid Authorization header.' });
    }

    const idToken = authHeader.split('Bearer ')[1];
    try {
        if (fs.existsSync(serviceAccountPath)) {
            const decodedToken = await getAuth().verifyIdToken(idToken);
            req.user = decodedToken;
        } else {
            req.user = { uid: 'dev_local_user' };
        }
        next();
    } catch (error) {
        console.error('Firebase ID Token verification failed:', error.message);
        return res.status(401).json({ error: 'Unauthorized. Invalid Firebase ID token.' });
    }
}

// 4. ImageKit Auth Parameters Endpoint (GET /api/imagekit/auth)
app.get('/api/imagekit/auth', verifyFirebaseToken, (req, res) => {
    try {
        const authParameters = imagekit.getAuthenticationParameters();
        console.log(`[ImageKit Auth SUCCESS] UID: ${req.user ? req.user.uid : 'unknown'} | Public Key Prefix: ${publicKey.substring(0, 10)}... | Expire: ${authParameters.expire} | Has Token: ${!!authParameters.token} | Has Signature: ${!!authParameters.signature}`);

        res.json({
            token: String(authParameters.token),
            expire: String(authParameters.expire),
            signature: String(authParameters.signature),
            publicKey: publicKey
        });
    } catch (error) {
        console.error('Error generating ImageKit auth parameters:', error);
        res.status(500).json({ error: 'Failed to generate ImageKit authentication parameters.' });
    }
});

// Health check endpoint
app.get('/health', (req, res) => {
    res.json({ status: 'OK', app: 'Viragram Backend Server' });
});

app.listen(PORT, () => {
    console.log(`\n Viragram Express Backend running on http://localhost:${PORT}`);
    console.log(` ImageKit Auth Endpoint: http://localhost:${PORT}/api/imagekit/auth\n`);
});