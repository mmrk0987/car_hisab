sed -i '/<Card/,/<\/Card>/ {
  /Google Drive Backup & Restore/ {
    c\
    SupabaseSyncSection(\
      activeEmail = activeGmail,\
      language = language,\
      accentColor = accentColor,\
      isDark = isDark\
    )
  }
}' app/src/main/java/com/example/ui/screens/SettingsScreen.kt
