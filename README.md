# Car Hisab — Fleet & Driver Financial System

Android app written in Kotlin and Jetpack Compose with Supabase backend.

## Database Migration SQL
Run the following SQL statement in your Supabase SQL Editor:

```sql
-- Add date_of_birth column to profiles table for password recovery verification
alter table profiles add column if not exists date_of_birth text;
```

## Deploying Supabase Edge Function `reset-password-by-dob`

1. Ensure the Supabase CLI is installed and logged in:
   ```bash
   supabase login
   supabase link --project-ref <your-project-ref>
   ```

2. Set `SUPABASE_SERVICE_ROLE_KEY` secret ONLY as an Edge Function secret:
   ```bash
   supabase secrets set SUPABASE_SERVICE_ROLE_KEY=your_service_role_key_here
   ```

3. Deploy the Edge Function:
   ```bash
   supabase functions deploy reset-password-by-dob
   ```
