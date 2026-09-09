package com.example.ui.i18n

object AppStrings {
  fun appTitle(lang: AppLanguage) = "Car Hisab"

  fun mmrkBranding(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "MMRK ইনোভেশনস"
    else -> "MMRK Innovations"
  }

  fun appTagline(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "নিরাপদ এবং নির্ভরযোগ্য আয় ব্যয়ের ডিজিটাল হিসাব"
    else -> "Secure & reliable income-expense digital accounts"
  }

  // Language screen
  fun selectLanguage(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ভাষা নির্বাচন (Language)"
    AppLanguage.HINDI -> "भाषा चयन (Language)"
    AppLanguage.TAMIL -> "மொழி தேர்வு (Language)"
    AppLanguage.URDU -> "زبان کا انتخاب (Language)"
    else -> "Language Selection (Language)"
  }

  fun languageSubtitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "পরবর্তীতে সেটিংস থেকে যে কোনো সময় পরিবর্তন করতে পারবেন"
    AppLanguage.HINDI -> "सेटिंग्स से कभी भी भाषा बदल सकते हैं"
    AppLanguage.TAMIL -> "அமைப்புகளில் எந்த நேரத்திலும் மாற்றலாம்"
    AppLanguage.URDU -> "سیٹنگز سے کسی بھی وقت تبدیل کر سکتے ہیں"
    else -> "You can change this anytime later in Settings"
  }

  fun banglaLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "বাংলা (ডিফল্ট)"
    else -> "Bangla (বাংলা)"
  }

  fun englishLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "English (ইংরেজি)"
    else -> "English"
  }

  fun hindiLabel(lang: AppLanguage) = "हिन्दी (Hindi)"
  fun tamilLabel(lang: AppLanguage) = "தமிழ் (Tamil)"
  fun urduLabel(lang: AppLanguage) = "اردو (Urdu)"

  fun continueBtn(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "এগিয়ে যান"
    AppLanguage.HINDI -> "आगे बढ़ें"
    AppLanguage.TAMIL -> "தொடரவும்"
    AppLanguage.URDU -> "آگے بڑھیں"
    else -> "Continue"
  }

  // Login screen
  fun loginTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ড্রাইভার লগইন"
    else -> "Driver Login"
  }

  fun loginSubtitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "আপনার হিসাব সুরক্ষিত রাখতে সাইন ইন করুন"
    else -> "Sign in to keep your records safe and synced"
  }

  fun googleSignIn(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "গুগল দিয়ে সাইন ইন করুন"
    else -> "Sign in with Google"
  }

  fun phoneOtpSignIn(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ফোন ওটিপি (OTP) দিয়ে লগইন"
    else -> "Login with Phone OTP"
  }

  fun guestSignIn(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "সরাসরি অ্যাপে প্রবেশ করুন (লগইন ছাড়া)"
    else -> "Continue as Guest"
  }

  // NID screen
  fun nidTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "এনআইডি ভেরিফিকেশন"
    else -> "NID Verification"
  }

  fun nidSubtitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "নিরাপত্তার স্বার্থে জাতীয় পরিচয়পত্র ও সেলফি আপলোড করুন (UI প্রোটোটাইপ)"
    else -> "Upload your National ID & driver selfie for verification (UI Prototype)"
  }

  fun nidFront(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "এনআইডি সামনের ছবি"
    else -> "NID Front Side"
  }

  fun nidBack(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "এনআইডি পেছনের ছবি"
    else -> "NID Back Side"
  }

  fun driverSelfie(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ড্রাইভারের লাইভ সেলফি"
    else -> "Driver Live Selfie"
  }

  fun uploadPhoto(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ছবি তুলুন বা সিলেক্ট করুন"
    else -> "Capture or Select Image"
  }

  fun uploadedStatus(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ছবি সংযুক্ত হয়েছে ✓"
    else -> "Image Attached ✓"
  }

  fun submitNid(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "এনআইডি যাচাইয়ের জন্য পাঠান"
    else -> "Submit NID for Verification"
  }

  fun skipNid(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "পরে যাচাই করব (এগিয়ে যান)"
    else -> "Verify Later (Skip)"
  }

  // Profile setup
  fun profileTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "গাড়ি ও প্রোফাইল সেটআপ"
    else -> "Car & Profile Setup"
  }

  fun profileSubtitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "সঠিক হিসাব ও রসিদের জন্য আপনার গাড়ির তথ্য দিন"
    else -> "Enter your vehicle and driver information"
  }

  fun carName(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "গাড়ির নাম"
    else -> "Car Name"
  }

  fun carNamePlaceholder(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "যেমন: টয়োটা নোয়া / এলিয়ন"
    else -> "e.g. Toyota Noah / Allion"
  }

  fun carModel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "গাড়ির মডেল / সাল"
    else -> "Car Model / Year"
  }

  fun carNumber(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "গাড়ির নম্বর"
    else -> "Car Number Plate"
  }

  fun driverName(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ড্রাইভারের নাম"
    else -> "Driver Name"
  }

  fun phone(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মোবাইল নম্বর"
    else -> "Phone Number"
  }

  fun email(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ইমেইল (ঐচ্ছিক)"
    else -> "Email (Optional)"
  }

  fun saveProfile(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "প্রোফাইল সংরক্ষণ করুন"
    else -> "Save Profile"
  }

  // Dashboard
  fun dashboard(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ড্যাশবোর্ড"
    AppLanguage.HINDI -> "डैशबोर्ड"
    AppLanguage.TAMIL -> "டாஷ்போர்டு"
    AppLanguage.URDU -> "ڈیش بورڈ"
    else -> "Dashboard"
  }

  fun todayIncome(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "আজকের আয়"
    AppLanguage.HINDI -> "आज की आय"
    AppLanguage.TAMIL -> "இன்றைய வருமானம்"
    AppLanguage.URDU -> "آج کی آمدنی"
    else -> "Today's Income"
  }

  fun monthIncome(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "চলতি মাসের আয়"
    AppLanguage.HINDI -> "इस महीने की आय"
    AppLanguage.TAMIL -> "இந்த மாத வருமானம்"
    AppLanguage.URDU -> "اس ماہ کی آمدنی"
    else -> "This Month's Income"
  }

  fun monthProfit(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "চলতি মাসের লাভ"
    AppLanguage.HINDI -> "इस महीने का लाभ"
    AppLanguage.TAMIL -> "இந்த மாத லாபம்"
    AppLanguage.URDU -> "اس ماہ کا منافع"
    else -> "This Month's Profit"
  }

  fun totalTrips(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মোট ট্রিপ"
    AppLanguage.HINDI -> "कुल ट्रিপ्स"
    AppLanguage.TAMIL -> "மொத்த பயணங்கள்"
    AppLanguage.URDU -> "کل ٹرپس"
    else -> "Total Trips"
  }

  fun totalKm(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মোট কিমি চালানো"
    AppLanguage.HINDI -> "कुल किमी"
    AppLanguage.TAMIL -> "மொத்த கிமீ"
    AppLanguage.URDU -> "کل کلومیٹر"
    else -> "Total KM Driven"
  }

  fun subscriptionBanner(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ফ্রি ট্রায়াল সক্রিয় - ৬ মাস বাকি"
    else -> "Free Trial Active - 6 months left"
  }

  fun manageSub(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "সাবস্ক্রিপশন দেখুন"
    else -> "View Plan"
  }

  fun addNewTrip(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "নতুন ট্রিপ যোগ করুন"
    AppLanguage.HINDI -> "नया ট্রিপ जोड़ें"
    AppLanguage.TAMIL -> "புதிய பயணம் சேர்க்க"
    AppLanguage.URDU -> "نیا ٹرپ شامل کریں"
    else -> "Add New Trip"
  }

  fun viewAllTrips(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "সব ট্রিপ দেখুন"
    AppLanguage.HINDI -> "सभी ট্রিপ देखें"
    AppLanguage.TAMIL -> "அனைத்து பயணங்களையும் காண்க"
    AppLanguage.URDU -> "تمام ٹرپس دیکھیں"
    else -> "View All Trips"
  }

  fun recentTrips(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "সাম্প্রতিক ট্রিপসমূহ"
    else -> "Recent Trips"
  }

  // Add Trip
  fun addTripTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ট্রিপ যুক্ত করুন"
    else -> "Add Trip"
  }

  fun dateLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "তারিখ"
    else -> "Date"
  }

  fun placeLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "জায়গার নাম লিখুন"
    AppLanguage.HINDI -> "स्थान का नाम दर्ज करें"
    AppLanguage.TAMIL -> "இடத்தின் பெயரை உள்ளிடவும்"
    AppLanguage.URDU -> "جگہ کا نام درج کریں"
    else -> "Enter Place Name"
  }

  fun placePlaceholder(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "যেমন: ঢাকা টু গাজীপুর"
    else -> "e.g. Dhaka to Gazipur"
  }

  fun passengerNameLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "যাত্রীর নাম (ঐচ্ছিক)"
    else -> "Passenger Name (Optional)"
  }

  fun passengerNamePlaceholder(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "যেমন: যাত্রীর নাম"
    else -> "e.g. Passenger Name"
  }

  fun passengerPhoneLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "যাত্রীর মোবাইল নম্বর (ঐচ্ছিক)"
    else -> "Passenger Phone (Optional)"
  }

  fun passengerPhonePlaceholder(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "যেমন: 017XXXXXXXX"
    else -> "e.g. 017XXXXXXXX"
  }

  fun rentLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ভাড়া (Rent)"
    else -> "Rent (ভাড়া)"
  }

  fun gratuityLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ট্রিপ খরচ"
    else -> "Trip Expense (ট্রিপ খরচ)"
  }

  fun maintenanceLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মেইনটেন্যান্স / সার্ভিস খরচ (৳)"
    else -> "Maintenance / Service Cost (৳)"
  }

  fun kmLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "দূরত্ব (KM)"
    else -> "Distance (KM)"
  }

  fun descLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "কাজের বিবরণ / নোট (ঐচ্ছিক)"
    else -> "Work Description / Notes (Optional)"
  }

  fun descPlaceholder(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "যেমন: ইঞ্জিন অয়েল বদল, ব্রেক প্যাড, পার্টস মেরামত..."
    else -> "e.g. Engine oil change, brake pad, repair notes..."
  }

  fun liveCalculation(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "রিয়েল-টাইম হিসাব"
    else -> "Real-time Calculation"
  }

  fun formulaIncome(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "আয় = ভাড়া − ট্রিপ খরচ"
    else -> "Income = Rent − Trip Expense"
  }

  fun formulaProfit(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "লাভ = আয় − মেইনটেন্যান্স খরচ"
    else -> "Profit = Income − Maintenance Cost"
  }

  fun calculatedIncome(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মোট আয় (Income)"
    else -> "Net Income"
  }

  fun calculatedProfit(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "নীট লাভ (Profit)"
    else -> "Net Profit"
  }

  fun calculatedLoss(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "লোকসান (Loss)"
    else -> "Net Loss"
  }

  fun saveTripBtn(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ট্রিপ সংরক্ষণ করুন"
    else -> "Save Trip"
  }

  fun tripSavedSuccess(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ট্রিপ সফলভাবে সংরক্ষিত হয়েছে!"
    else -> "Trip saved successfully!"
  }

  // All Trips
  fun allTripsTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "সকল ট্রিপ"
    else -> "All Trips"
  }

  fun searchPlaceholder(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "স্থান বা বিবরণ দিয়ে খুঁজুন..."
    else -> "Search by place or description..."
  }

  fun filterAll(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "সব"
    else -> "All"
  }

  fun filterToday(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "আজকের"
    else -> "Today"
  }

  fun filterThisMonth(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "চলতি মাস"
    else -> "This Month"
  }

  fun filterProfit(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "লাভজনক"
    else -> "Profit"
  }

  fun filterLoss(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "লোকসান"
    else -> "Loss"
  }

  fun noTripsFound(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "কোন ট্রিপ পাওয়া যায়নি"
    else -> "No trips found"
  }

  fun noTripsSub(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "নতুন ট্রিপ যোগ করে আপনার হিসাব শুরু করুন"
    else -> "Add a trip to start tracking your earnings"
  }

  fun deleteTrip(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মুছে ফেলুন"
    else -> "Delete"
  }

  fun tripDeleted(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ট্রিপ মুছে ফেলা হয়েছে"
    else -> "Trip deleted"
  }

  // Subscription / Payment
  fun subscriptionTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "সাবস্ক্রিপশন ও পেমেন্ট"
    else -> "Subscription & Payment"
  }

  fun subStatus(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "সাবস্ক্রিপশন স্ট্যাটাস"
    else -> "Subscription Status"
  }

  fun freeTrialStatus(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ফ্রি ট্রায়াল সক্রিয় (৬ মাস বাকি)"
    else -> "Free Trial Active (6 Months Left)"
  }

  fun expiredStatus(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মেয়াদ শেষ হয়েছে - রিনিউ করুন"
    else -> "Expired - Please Renew"
  }

  fun toggleTrialDemo(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "স্ট্যাটাস টেস্ট (সক্রিয় / মেয়াদ শেষ)"
    else -> "Toggle Status Demo"
  }

  fun paymentMethodsTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "পেমেন্ট পদ্ধতিসমূহ"
    else -> "Payment Methods"
  }

  fun paymentNotice(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "নিচের যেকোনো নম্বরে 'Send Money' করুন এবং TrxID ও স্ক্রিনশট দিন:"
    else -> "Send money to any number below and submit TrxID with screenshot:"
  }

  fun bKashTitle(lang: AppLanguage) = "bKash Personal"
  fun bKashNumber() = "01606665209"

  fun nagadTitle(lang: AppLanguage) = "Nagad Personal"
  fun nagadNumber() = "01606665209"

  fun bankAsiaTitle(lang: AppLanguage) = "Bank Asia Ltd"
  fun bankAsiaDetails(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "হিসাব নম্বর: 10234567890\nহিসাবের নাম: Car Hisab Ltd\nশাখা: ধানমন্ডি, ঢাকা"
    else -> "A/C: 10234567890\nName: Car Hisab Ltd\nBranch: Dhanmondi, Dhaka"
  }

  fun copy(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "কপি করুন"
    else -> "Copy"
  }

  fun copiedToast(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "নম্বর ক্লিপবোর্ডে কপি হয়েছে"
    else -> "Copied to clipboard"
  }

  fun trxIdLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ট্রানজেকশন আইডি (TrxID)"
    else -> "Transaction ID (TrxID)"
  }

  fun trxIdPlaceholder(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "যেমন: 9J3K8LM2P"
    else -> "e.g. 9J3K8LM2P"
  }

  fun uploadScreenshot(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "পেমেন্ট স্ক্রিনশট আপলোড করুন"
    else -> "Upload Payment Screenshot"
  }

  fun screenshotSelected(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "স্ক্রিনশট সিলেক্ট হয়েছে ✓"
    else -> "Screenshot Attached ✓"
  }

  fun submitVerificationBtn(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "যাচাইয়ের জন্য জমা দিন"
    else -> "Submit for Verification"
  }

  fun verificationSuccess(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "আপনার পেমেন্ট রিকোয়েস্ট সফলভাবে জমা হয়েছে! ২৪ ঘণ্টার মধ্যে যাচাই করা হবে।"
    else -> "Payment request submitted! Verification will be completed within 24 hours."
  }

  // Packages & Pricing
  fun packagesSectionTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "সাবস্ক্রিপশন প্যাকেজসমূহ"
    else -> "Subscription Packages"
  }

  fun packageMonthlyTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "১ মাসের প্ল্যান"
    else -> "1 Month Regular"
  }

  fun packageMonthlyPrice(lang: AppLanguage) = "৳১০০"
  fun packageMonthlyDesc(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "স্বল্পমেয়াদী ব্যবহারের জন্য (৩০ দিন আনলিমিটেড ট্রিপ)"
    else -> "Short-term usage (30 days unlimited trips)"
  }

  fun packageHalfYearlyTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "৬ মাসের হাফ-ইয়ারলি প্ল্যান"
    else -> "6 Months Plan"
  }

  fun packageHalfYearlyPrice(lang: AppLanguage) = "৳৫০০"
  fun packageHalfYearlyDesc(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "সবচেয়ে জনপ্রিয়! ৳১০০ সাশ্রয়ী প্যাকেজ (১৮০ দিন)"
    else -> "Most Popular! Save ৳100 (180 days unlimited)"
  }

  fun packageYearlyTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "১ বছরের মেগা সেভার প্ল্যান"
    else -> "1 Year Mega Saver"
  }

  fun packageYearlyPrice(lang: AppLanguage) = "৳৯৫০"
  fun packageYearlyDesc(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "সেরা মান! ২৫% মেগা ছাড়ে ৩৬৫ দিন নিশ্চিন্ত হিসাব"
    else -> "Best Value! 25% Mega Discount for full 365 days"
  }

  // Admin Code Section
  fun adminActivationTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "এডমিন অ্যাক্টিভেশন কোড বা পিন"
    else -> "Admin Secret Activation Key"
  }

  fun adminActivationSubtitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "বিকাশ বা নগদে পেমেন্ট সম্পন্ন করার পর এডমিন থেকে প্রাপ্ত গোপন আনলক কোডটি এখানে বসান:"
    else -> "Enter the unlock key received from Admin after your bKash/Nagad payment:"
  }

  fun adminKeyLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "অ্যাক্টিভেশন কোড (যেমন: CH-180-H500)"
    else -> "Activation Key (e.g. CH-180-H500)"
  }

  fun activateKeyBtn(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "কোড যাচাই ও আনলক করুন"
    else -> "Verify & Unlock App"
  }

  fun adminContactCall(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "এডমিনের সাথে যোগাযোগ ও সাহায্য"
    else -> "Contact Admin for Activation Key"
  }

  fun adminPhoneNum() = "01606665209"

  // Settings
  fun settingsTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "সেটিংস"
    else -> "Settings"
  }

  fun languageSetting(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "অ্যাপের ভাষা পরিবর্তন"
    else -> "App Language"
  }

  fun themeSetting(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "থিম মোড (কালার)"
    else -> "Theme Mode"
  }

  fun themeSystem(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "সিস্টেম ডিফল্ট"
    else -> "System Default"
  }

  fun themeLight(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "লাইট মোড"
    else -> "Light Mode"
  }

  fun themeDark(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ডার্ক মোড"
    else -> "Dark Mode"
  }

  fun editCarInfo(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "গাড়ি ও ড্রাইভার তথ্য এডিট"
    else -> "Edit Car & Driver Info"
  }

  fun editCarInfoSub(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "গাড়ির নাম, মডেল, নম্বর ও আপনার নাম পরিবর্তন করুন"
    else -> "Update car details, model, registration and phone"
  }

  fun aboutApp(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "Car Hisab সম্পর্কে"
    else -> "About Car Hisab"
  }

  fun version(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ভার্সন ১.০ (MMRK স্পেশাল এডিশন)"
    else -> "Version 1.0 (MMRK Special Edition)"
  }

  // Export Feature Strings
  fun exportAction(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "এক্সপোর্ট"
    else -> "Export"
  }

  fun exportTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ট্রিপ হিসাব এক্সপোর্ট"
    else -> "Export Trip Logs"
  }

  fun exportSubtitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "হিসাব ও অডিটের জন্য ট্রিপ লগ PDF বা Excel CSV ফাইল হিসেবে শেয়ার করুন"
    else -> "Export your accounting records as a printable PDF or Excel CSV spreadsheet"
  }

  fun exportFormatLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ফাইলের ফরম্যাট নির্বাচন করুন"
    else -> "Select File Format"
  }

  fun exportPdf(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "PDF স্টেটমেন্ট ডকুমেন্ট (.pdf)"
    else -> "PDF Accounting Statement (.pdf)"
  }

  fun exportPdfSub(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "গাড়ির ও ড্রাইভার তথ্যসহ পেশাদার প্রিন্টযোগ্য অডিট রিপোর্ট"
    else -> "Professional printable statement with driver details & totals"
  }

  fun exportCsv(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "Excel / CSV স্প্রেডশিট (.csv)"
    else -> "Excel / CSV Spreadsheet (.csv)"
  }

  fun exportCsvSub(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "Excel বা Google Sheets-এ ওপেন ও এডিটের উপযোগী সম্পূর্ণ টেবিল"
    else -> "Raw tabular dataset formatted for Excel, Google Sheets, or tax software"
  }

  fun exportScopeLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "রেকর্ডের পরিধি"
    else -> "Records to Include"
  }

  fun exportScopeCurrentMonth(lang: AppLanguage, monthName: String, count: Int) = when (lang) {
    AppLanguage.BANGLA -> "চলতি মাস - $monthName ($count টি)"
    else -> "This Month - $monthName ($count)"
  }

  fun exportScopeFiltered(lang: AppLanguage, count: Int) = when (lang) {
    AppLanguage.BANGLA -> "বর্তমান ফোল্ডার ($count টি)"
    else -> "Current Folder ($count)"
  }

  fun exportScopeAll(lang: AppLanguage, count: Int) = when (lang) {
    AppLanguage.BANGLA -> "সকল ট্রিপ ($count টি)"
    else -> "All Trips ($count)"
  }

  fun exportBtn(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "এক্সপোর্ট ও শেয়ার করুন"
    else -> "Export & Share File"
  }

  fun exportSuccess(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ফাইল প্রস্তুত! শেয়ার বা সেভ করার অপশন বেছে নিন।"
    else -> "Export ready! Choose an app to save or share."
  }

  fun exportEmpty(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "এক্সপোর্ট করার মতো কোনো ট্রিপ তালিকাভুক্ত নেই!"
    else -> "There are no trips to export!"
  }

  // Monthly Summary & Charts Strings
  fun monthlySummaryTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "চলতি মাসের হিসাব ও ট্রেন্ড"
    else -> "Monthly Summary & Trends"
  }

  fun monthlySummarySubtitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "আয়, ব্যয় এবং নীট লাভের বিশ্লেষণমূলক ভিজ্যুয়াল চার্ট"
    else -> "Visual trend charts aggregating income, expenses & net profit"
  }

  fun monthlyTrendsTab(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "আয় বনাম ব্যয়"
    else -> "Income vs Expense"
  }

  fun monthlyBreakdownTab(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "অনুপাত"
    else -> "Ratio Split"
  }

  fun monthlyProfitCurveTab(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "লাভের প্রবৃদ্ধি"
    else -> "Profit Curve"
  }

  fun currentMonthBadge(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "চলতি মাস"
    else -> "Current Month"
  }

  fun totalGrossIncome(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মোট আয়"
    else -> "Total Income"
  }

  fun totalExpensesAggregate(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মোট খরচ"
    else -> "Total Expenses"
  }

  fun netProfitMargin(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "লাভের মার্জিন"
    else -> "Profit Margin"
  }

  fun peakDayLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "শীর্ষ আয়ের দিন"
    else -> "Peak Day"
  }

  fun avgProfitPerTripLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "গড় লাভ / ট্রিপ"
    else -> "Avg Profit/Trip"
  }

  fun touchForDetails(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "বিশদ দেখতে চার্টে টাচ করুন"
    else -> "Touch chart points to view details"
  }

  fun maintenanceExpense(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "রক্ষণাবেক্ষণ ও তেল"
    else -> "Maintenance & Fuel"
  }

  fun gratuityExpense(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ট্রিপ খরচ"
    else -> "Trip Expense"
  }

  fun netProfitShare(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "নীট মুনাফা"
    else -> "Net Profit"
  }

  fun noTripsThisMonth(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "এই মাসে কোনো ট্রিপ নেই"
    else -> "No trips recorded this month"
  }

  fun noTripsThisMonthDesc(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ট্রিপ যুক্ত করলে এখানে স্বয়ংক্রিয় চার্ট ও ট্রেন্ড দেখা যাবে।"
    else -> "Log trips to automatically generate visual trends and financial reports."
  }

  // Google Drive Cloud Backup & Restore Strings
  fun googleDriveTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "গুগল ড্রাইভ ক্লাউড ব্যাকআপ"
    else -> "Google Drive Cloud Backup"
  }

  fun googleDriveSubtitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "গুগল একাউন্টে ডেটা সুরক্ষিত রাখুন ও যেকোনো ডিভাইসে রিস্টোর করুন"
    else -> "Safeguard trips in your Google Drive & restore anytime"
  }

  fun backupNowBtn(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ড্রাইভে ব্যাকআপ নিন"
    else -> "Backup to Drive"
  }

  fun restoreNowBtn(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ড্রাইভ থেকে রিস্টোর করুন"
    else -> "Restore from Drive"
  }

  fun lastBackupLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "সর্বশেষ ব্যাকআপ:"
    else -> "Last backup:"
  }

  fun noBackupYet(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "এখনো ব্যাকআপ নেওয়া হয়নি"
    else -> "No backup created yet"
  }

  fun backingUpStatus(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "গুগল ড্রাইভে ব্যাকআপ তৈরি হচ্ছে..."
    else -> "Creating Google Drive backup..."
  }

  fun restoringStatus(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "গুগল ড্রাইভ থেকে তথ্য পুনরুদ্ধার হচ্ছে..."
    else -> "Restoring trips from Google Drive..."
  }

  fun backupSuccess(lang: AppLanguage, count: Int) = when (lang) {
    AppLanguage.BANGLA -> "সফল! $count টি ট্রিপ গুগল ড্রাইভে ব্যাকআপ সংরক্ষিত হয়েছে।"
    else -> "Success! $count trips successfully backed up to Google Drive."
  }

  fun restoreSuccess(lang: AppLanguage, count: Int) = when (lang) {
    AppLanguage.BANGLA -> "সফল! গুগল ড্রাইভ থেকে $count টি ট্রিপ সফলভাবে রিস্টোর হয়েছে।"
    else -> "Success! $count trips restored from Google Drive."
  }

  fun backupEmpty(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ব্যাকআপ নেওয়ার মতো কোনো ট্রিপ এখনও যুক্ত করা হয়নি!"
    else -> "No trips logged yet to back up!"
  }

  fun driveAccountLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "সংযুক্ত গুগল ড্রাইভ"
    else -> "Connected Google Drive"
  }

  fun driveSyncStatusActive(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ক্লাউড স্টোরেজ প্রস্তুত"
    else -> "Cloud Storage Ready"
  }

  fun restoreConfirmTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ড্রাইভ ব্যাকআপ রিস্টোর করবেন?"
    else -> "Restore from Google Drive?"
  }

  fun restoreConfirmMsg(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ড্রাইভে সংরক্ষিত ব্যাকআপ ফাইল থেকে ট্রিপসমূহ আপনার ডিভাইসে সিঙ্ক করা হবে। আপনি কি এগিয়ে যেতে চান?"
    else -> "Trips from your Google Drive backup will be synced to this device. Do you wish to continue?"
  }

  fun autoBackupNote(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "আপনার সমস্ত ট্রিপ ও খরচ Google Drive AppData ফোল্ডারে সম্পূর্ণ এনক্রিপ্টেড অবস্থায় সংরক্ষিত হয়।"
    else -> "All trips and expenses are secured directly in your Google Drive AppData folder."
  }

  // Password / PIN recovery strings
  fun forgotPasswordBtn(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "পাসওয়ার্ড বা ওটিপি ভুলে গেছেন?"
    else -> "Forgot Password or PIN?"
  }

  fun resetPasswordTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "অ্যাকাউন্ট পুনরুদ্ধার ও পিন রিসেট"
    else -> "Account Recovery & Reset PIN"
  }

  fun resetPasswordSubtitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "আপনার নিবন্ধিত মোবাইল নম্বর দিন। ওটিপি ভেরিফিকেশনের মাধ্যমে নতুন পাসওয়ার্ড সেট করতে পারবেন।"
    else -> "Enter your registered mobile number to verify and set a new password/PIN."
  }

  fun newPasswordLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "নতুন পাসওয়ার্ড / পিন (৪-৬ ডিজিট)"
    else -> "New Password / PIN (4-6 digits)"
  }

  fun confirmNewPasswordLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "পাসওয়ার্ড পুনরায় লিখুন"
    else -> "Confirm New Password"
  }

  fun resetSuccessMsg(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "পাসওয়ার্ড সফলভাবে পরিবর্তন হয়েছে! নতুন পাসওয়ার্ড দিয়ে প্রবেশ করুন।"
    else -> "Password successfully reset! You can now log in."
  }

  // Documents & Service Strings
  fun vehicleDocsTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "গাড়ির কাগজপত্র ও সার্ভিসিং"
    else -> "Vehicle Docs & Maintenance"
  }

  fun vehicleDocsTab(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "কাগজপত্রের মেয়াদ"
    else -> "Documents Expiry"
  }

  fun mobilServiceTab(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মবিল ও সার্ভিসিং"
    else -> "Mobil & Service"
  }

  fun taxToken(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ট্যাক্স টোকেন (Tax Token)"
    else -> "Tax Token"
  }

  fun fitnessCertificate(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ফিটনেস সনদ (Fitness)"
    else -> "Fitness Certificate"
  }

  fun routePermit(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "রুট পারমিট (Route Permit)"
    else -> "Route Permit"
  }

  fun insurancePolicy(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ইন্স্যুরেন্স (Insurance)"
    else -> "Insurance"
  }

  fun drivingLicense(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ড্রাইভিং লাইসেন্স (License)"
    else -> "Driving License"
  }

  fun daysRemaining(days: Long, lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> if (days > 0) "$days দিন বাকি" else "মেয়াদ শেষ (${-days} দিন আগে)"
    else -> if (days > 0) "$days days left" else "Expired (${-days} days ago)"
  }

  fun mobilStatus(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ইঞ্জিন অয়েল (মবিল) স্থিতি"
    else -> "Engine Oil (Mobil) Status"
  }

  fun logMobilChange(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মবিল পরিবর্তন এন্ট্রি করুন"
    else -> "Log Mobil Change"
  }

  fun updateOdometerTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ওডোমিটার কিমি আপডেট"
    else -> "Update Odometer KM"
  }

  // Bookings Strings
  fun bookingsTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "অগ্রিম বুকিং ডায়েরি"
    else -> "Advance Booking Diary"
  }

  fun upcomingBookingsTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "আসন্ন বুকিং"
    else -> "Upcoming Bookings"
  }

  fun addBookingTitle(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "নতুন অগ্রিম বুকিং"
    else -> "New Advance Booking"
  }

  fun passengerName(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "যাত্রীর নাম"
    else -> "Passenger Name"
  }

  fun passengerPhone(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মোবাইল নম্বর"
    else -> "Phone Number"
  }

  fun pickupPoint(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "পিকআপ পয়েন্ট"
    else -> "Pickup Location"
  }

  fun destinationPoint(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "গন্তব্য / ড্রপ পয়েন্ট"
    else -> "Destination"
  }

  fun bookingDate(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ট্রিপের তারিখ ও সময়"
    else -> "Trip Date & Time"
  }

  fun totalFareLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মোট নির্ধারিত ভাড়া"
    else -> "Agreed Total Fare"
  }

  fun advancePaidLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "অগ্রিম জমা গ্রহণ"
    else -> "Advance Received"
  }

  fun dueFareLabel(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "বাকি পাওনা"
    else -> "Due Balance"
  }

  fun callPassenger(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "কল করুন"
    else -> "Call"
  }

  fun shareBookingMessage(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "মেসেজ / WhatsApp"
    else -> "WhatsApp / SMS"
  }

  fun completeAndAddTrip(lang: AppLanguage) = when (lang) {
    AppLanguage.BANGLA -> "ট্রিপ হিসেবে যোগ করুন"
    else -> "Convert to Trip"
  }

  fun currencySymbol() = "৳"
}
