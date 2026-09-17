val context = LocalContext.current
val packageManager = context.packageManager
val versionName = try {
  packageManager.getPackageInfo(context.packageName, 0).versionName
} catch (e: Exception) {
  BuildConfig.VERSION_NAME
}
