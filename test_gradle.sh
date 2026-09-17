sed -i '/base { archivesName/d' app/build.gradle.kts
echo 'base { archivesName.set("Car_Hisab_v" + android.defaultConfig.versionName) }' >> app/build.gradle.kts
gradle :app:assembleDebug --dry-run
