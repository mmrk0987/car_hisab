sed -i '/base {/d' app/build.gradle.kts
sed -i '/archivesName.set(/d' app/build.gradle.kts
sed -i '/^}$/d' app/build.gradle.kts # be careful with this, let's just do sed
