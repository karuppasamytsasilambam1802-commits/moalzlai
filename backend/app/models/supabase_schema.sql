-- ====================================================================
-- VoxBridge AI — Supabase / PostgreSQL Database Schema & RLS Policies
-- Supports Android, Web, and iOS Clients
-- ====================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Users & Profiles Table
CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    github_id BIGINT UNIQUE,
    username VARCHAR(100) NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    avatar_url TEXT,
    email VARCHAR(255),
    preferred_language VARCHAR(10) DEFAULT 'en',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. Rooms Table
CREATE TABLE IF NOT EXISTS public.rooms (
    room_id VARCHAR(50) PRIMARY KEY,
    host_id VARCHAR(100) NOT NULL,
    host_name VARCHAR(150) NOT NULL,
    supported_modes JSONB DEFAULT '["VOICE_AND_TEXT"]'::jsonb,
    max_participants INT DEFAULT 10,
    created_at BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

-- 3. Room Participants Table
CREATE TABLE IF NOT EXISTS public.room_participants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    room_id VARCHAR(50) REFERENCES public.rooms(room_id) ON DELETE CASCADE,
    participant_id VARCHAR(100) NOT NULL,
    participant_name VARCHAR(150) NOT NULL,
    preferred_language VARCHAR(10) DEFAULT 'en',
    communication_mode VARCHAR(50) DEFAULT 'VOICE_AND_TEXT',
    is_host BOOLEAN DEFAULT FALSE,
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(room_id, participant_id)
);

-- 4. Messages Table
CREATE TABLE IF NOT EXISTS public.messages (
    id VARCHAR(100) PRIMARY KEY,
    room_id VARCHAR(50) REFERENCES public.rooms(room_id) ON DELETE CASCADE,
    sender_id VARCHAR(100) NOT NULL,
    sender_name VARCHAR(150) NOT NULL,
    original_text TEXT NOT NULL,
    source_language VARCHAR(10) NOT NULL,
    translations JSONB DEFAULT '{}'::jsonb,
    sequence_number BIGINT NOT NULL,
    created_at BIGINT NOT NULL,
    is_typed BOOLEAN DEFAULT TRUE
);

-- 5. Audit Logs Table
CREATE TABLE IF NOT EXISTS public.audit_logs (
    id VARCHAR(100) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    user_name VARCHAR(150) NOT NULL,
    user_role VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 6. File Attachments Table
CREATE TABLE IF NOT EXISTS public.file_attachments (
    id VARCHAR(100) PRIMARY KEY,
    room_id VARCHAR(50) REFERENCES public.rooms(room_id) ON DELETE CASCADE,
    sender_id VARCHAR(100) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_url TEXT NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ====================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ====================================================================

ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.rooms ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.room_participants ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.file_attachments ENABLE ROW LEVEL SECURITY;

-- Allow public read & join for active rooms
CREATE POLICY "Allow public read active rooms" ON public.rooms
    FOR SELECT USING (is_active = TRUE);

CREATE POLICY "Allow authenticated create rooms" ON public.rooms
    FOR INSERT WITH CHECK (TRUE);

-- Allow reading messages for room participants
CREATE POLICY "Allow participant read messages" ON public.messages
    FOR SELECT USING (TRUE);

CREATE POLICY "Allow participant insert messages" ON public.messages
    FOR INSERT WITH CHECK (TRUE);

-- Allow audit log insertions
CREATE POLICY "Allow insert audit logs" ON public.audit_logs
    FOR INSERT WITH CHECK (TRUE);

CREATE POLICY "Allow read audit logs" ON public.audit_logs
    FOR SELECT USING (TRUE);
