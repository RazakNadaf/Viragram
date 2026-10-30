-- Supabase Storage RLS Policies for viragram-media bucket with Firebase Auth

-- 1. Enable Public Read access for viragram-media bucket
CREATE POLICY "Public Read Access on viragram-media"
ON storage.objects FOR SELECT
USING ( bucket_id = 'viragram-media' );

-- 2. Allow Authenticated Firebase Users to Upload to their own folder:
-- profile/{firebaseUid}/*, posts/{firebaseUid}/*, stories/{firebaseUid}/*
CREATE POLICY "Owner Upload Access on viragram-media"
ON storage.objects FOR INSERT
WITH CHECK (
    bucket_id = 'viragram-media' AND
    (
        (storage.foldername(name))[2] = (auth.jwt() ->> 'sub') OR
        (storage.foldername(name))[2] = (auth.jwt() ->> 'user_id')
    )
);

-- 3. Allow Users to Update their own uploaded media
CREATE POLICY "Owner Update Access on viragram-media"
ON storage.objects FOR UPDATE
USING (
    bucket_id = 'viragram-media' AND
    (
        (storage.foldername(name))[2] = (auth.jwt() ->> 'sub') OR
        (storage.foldername(name))[2] = (auth.jwt() ->> 'user_id')
    )
);

-- 4. Allow Users to Delete their own uploaded media
CREATE POLICY "Owner Delete Access on viragram-media"
ON storage.objects FOR DELETE
USING (
    bucket_id = 'viragram-media' AND
    (
        (storage.foldername(name))[2] = (auth.jwt() ->> 'sub') OR
        (storage.foldername(name))[2] = (auth.jwt() ->> 'user_id')
    )
);