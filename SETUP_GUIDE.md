# 📱 دليل إعداد مشروع PDF Master

## الخطوات خطوة بخطوة

---

## 1. إنشاء المشروع في Android Studio

1. افتح **Android Studio**
2. اختر **New Project**
3. اختر **Empty Views Activity**
4. اضبط الإعدادات:
   - **Name:** PDF Master
   - **Package name:** com.pdfmaster
   - **Language:** Kotlin
   - **Minimum SDK:** API 26 (Android 8.0)
5. اضغط **Finish**

---

## 2. استبدال الملفات

انسخ كل الملفات من هذا المشروع إلى المشروع الجديد:

### ملفات Gradle
- `build.gradle` (Project level) ← استبدل المحتوى
- `app/build.gradle` ← استبدل المحتوى
- `settings.gradle` ← استبدل المحتوى
- `gradle.properties` ← استبدل المحتوى

### ملفات الـ Manifest
- `app/src/main/AndroidManifest.xml`

### ملفات الـ Resources
```
app/src/main/res/
├── values/
│   ├── strings.xml
│   ├── colors.xml
│   ├── themes.xml
│   └── dimens.xml
├── layout/
│   ├── activity_main.xml
│   ├── activity_pdf_viewer.xml
│   ├── fragment_home.xml
│   ├── fragment_pdf.xml
│   └── item_tool_card.xml
├── menu/
│   └── bottom_nav_menu.xml
├── navigation/
│   └── nav_graph.xml
├── drawable/
│   ├── (كل ملفات الأيقونات)
│   └── (كل ملفات الخلفيات)
└── xml/
    └── file_paths.xml
```

### ملفات Kotlin
```
app/src/main/java/com/pdfmaster/
├── ui/
│   ├── MainActivity.kt
│   ├── home/
│   │   └── HomeFragment.kt
│   ├── pdf/
│   │   ├── PdfFragment.kt
│   │   ├── PdfViewerActivity.kt
│   │   ├── ToolsAdapter.kt
│   │   └── Dialogs.kt
│   ├── word/
│   │   └── WordFragment.kt
│   └── settings/
│       └── SettingsFragment.kt
└── viewmodel/
    └── PdfViewModel.kt
```

---

## 3. Sync المشروع

بعد نسخ الملفات:
1. اضغط **File → Sync Project with Gradle Files**
2. انتظر تحميل المكتبات (قد يأخذ 2-5 دقائق)
3. تأكد مش في أخطاء حمراء

---

## 4. تشغيل التطبيق

- وصّل هاتفك أو استخدم Emulator
- اضغط **Run ▶️**

---

## ✅ Features في المرحلة 1

| الميزة | الحالة |
|--------|--------|
| عرض PDF | ✅ جاهز |
| دمج ملفات PDF | ✅ جاهز |
| تقسيم PDF | ✅ جاهز |
| ضغط PDF | ✅ جاهز |
| تحويل PDF لصور | ✅ جاهز |
| تدوير الصفحات | ✅ جاهز |
| حماية بكلمة سر | ✅ جاهز |
| علامة مائية | ✅ جاهز |
| أدوات Word | 🔜 المرحلة 2 |

---

## 🎨 تصميم التطبيق

- **ثيم:** داكن احترافي (Dark)
- **ألوان:** أزرق غامق + أحمر accent
- **Bottom Navigation:** 4 tabs
- **RTL:** دعم كامل للعربية

---

## ❓ لو ظهرت مشكلة

أكثر مشكلة شائعة:
```
Cannot resolve symbol 'R'
```
**الحل:** File → Invalidate Caches → Restart
