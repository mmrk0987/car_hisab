package com.example.ui.i18n

enum class AppLanguage(
  val label: String,
  val nativeName: String,
  val englishName: String
) {
  BANGLA("বাংলা", "বাংলা", "Bangla"),
  ENGLISH("English (Default)", "English", "English"),
  HINDI("हिन्दी (Hindi)", "हिन्दी", "Hindi"),
  TAMIL("தமிழ் (Tamil)", "தமிழ்", "Tamil"),
  URDU("اردو (Urdu)", "اردو", "Urdu")
}
