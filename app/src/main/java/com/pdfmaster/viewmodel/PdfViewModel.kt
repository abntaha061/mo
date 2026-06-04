package com.pdfmaster.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.kernel.utils.PdfMerger
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.TextAlignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class PdfViewModel : ViewModel() {

    sealed class Result {
        data class Success(val message: String, val outputUri: Uri? = null) : Result()
        data class Error(val message: String) : Result()
        object Loading : Result()
    }

    private val _operationResult = MutableLiveData<Result>()
    val operationResult: LiveData<Result> = _operationResult

    // ====== دمج PDF ======
    fun mergePdfs(context: Context, uris: List<Uri>) {
        viewModelScope.launch {
            _operationResult.value = Result.Loading
            try {
                val result = withContext(Dispatchers.IO) {
                    val outputFile = createOutputFile(context, "merged_${System.currentTimeMillis()}.pdf")
                    val pdfDoc = PdfDocument(PdfWriter(outputFile))
                    val merger = PdfMerger(pdfDoc)

                    for (uri in uris) {
                        val inputStream: InputStream = context.contentResolver.openInputStream(uri)!!
                        val srcDoc = PdfDocument(PdfReader(inputStream))
                        merger.merge(srcDoc, 1, srcDoc.numberOfPages)
                        srcDoc.close()
                    }

                    pdfDoc.close()
                    outputFile
                }
                val uri = getUriForFile(context, result)
                _operationResult.value = Result.Success("تم الدمج بنجاح! (${uris.size} ملفات)", uri)
            } catch (e: Exception) {
                _operationResult.value = Result.Error("خطأ في الدمج: ${e.message}")
            }
        }
    }

    // ====== تقسيم PDF ======
    fun splitPdf(context: Context, uri: Uri) {
        viewModelScope.launch {
            _operationResult.value = Result.Loading
            try {
                val fileCount = withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri)!!
                    val srcDoc = PdfDocument(PdfReader(inputStream))
                    val totalPages = srcDoc.numberOfPages
                    var count = 0

                    for (i in 1..totalPages) {
                        val outputFile = createOutputFile(context, "page_${i}_${System.currentTimeMillis()}.pdf")
                        val destDoc = PdfDocument(PdfWriter(outputFile))
                        srcDoc.copyPagesTo(i, i, destDoc)
                        destDoc.close()
                        count++
                    }
                    srcDoc.close()
                    count
                }
                _operationResult.value = Result.Success("تم التقسيم بنجاح! ($fileCount صفحة)")
            } catch (e: Exception) {
                _operationResult.value = Result.Error("خطأ في التقسيم: ${e.message}")
            }
        }
    }

    // ====== ضغط PDF ======
    fun compressPdf(context: Context, uri: Uri) {
        viewModelScope.launch {
            _operationResult.value = Result.Loading
            try {
                val result = withContext(Dispatchers.IO) {
                    val outputFile = createOutputFile(context, "compressed_${System.currentTimeMillis()}.pdf")
                    val inputStream = context.contentResolver.openInputStream(uri)!!

                    // قراءة الملف الأصلي
                    val originalBytes = inputStream.readBytes()
                    val originalSize = originalBytes.size

                    // إعادة كتابة بضغط
                    val readerProps = com.itextpdf.kernel.pdf.ReaderProperties()
                    val writerProps = com.itextpdf.kernel.pdf.WriterProperties()
                        .useSmartMode() // ضغط ذكي
                        .setCompressionLevel(9) // أعلى ضغط

                    val srcDoc = PdfDocument(
                        PdfReader(originalBytes.inputStream(), readerProps),
                        PdfWriter(outputFile, writerProps)
                    )
                    srcDoc.close()

                    val newSize = outputFile.length()
                    val savedPercent = ((originalSize - newSize) * 100 / originalSize).toInt()
                    Pair(outputFile, savedPercent)
                }
                val uri = getUriForFile(context, result.first)
                _operationResult.value = Result.Success(
                    "تم الضغط! وفّرنا ${result.second}% من الحجم",
                    uri
                )
            } catch (e: Exception) {
                _operationResult.value = Result.Error("خطأ في الضغط: ${e.message}")
            }
        }
    }

    // ====== تحويل PDF لصور ======
    fun pdfToImages(context: Context, uri: Uri) {
        viewModelScope.launch {
            _operationResult.value = Result.Loading
            try {
                val imageCount = withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri)!!

                    // استخدام Android's built-in PdfRenderer
                    val tempFile = File.createTempFile("temp_pdf", ".pdf", context.cacheDir)
                    tempFile.outputStream().use { out -> inputStream.copyTo(out) }

                    val parcelFd = android.os.ParcelFileDescriptor.open(
                        tempFile, android.os.ParcelFileDescriptor.MODE_READ_ONLY
                    )
                    val renderer = android.graphics.pdf.PdfRenderer(parcelFd)
                    val pageCount = renderer.pageCount
                    var count = 0

                    for (i in 0 until pageCount) {
                        val page = renderer.openPage(i)
                        val width = page.width * 2  // دقة عالية
                        val height = page.height * 2

                        val bitmap = android.graphics.Bitmap.createBitmap(
                            width, height,
                            android.graphics.Bitmap.Config.ARGB_8888
                        )
                        // خلفية بيضاء
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        page.render(bitmap, null, null,
                            android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                        val imgFile = createOutputFile(
                            context, "page_${i + 1}_${System.currentTimeMillis()}.jpg"
                                .replace(".pdf", ".jpg")
                        ).let {
                            File(it.parent, "page_${i + 1}.jpg")
                        }

                        imgFile.outputStream().use { out ->
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                        }
                        bitmap.recycle()
                        page.close()
                        count++
                    }
                    renderer.close()
                    tempFile.delete()
                    count
                }
                _operationResult.value = Result.Success("تم التحويل! ($imageCount صورة)")
            } catch (e: Exception) {
                _operationResult.value = Result.Error("خطأ في التحويل: ${e.message}")
            }
        }
    }

    // ====== تدوير PDF ======
    fun rotatePdf(context: Context, uri: Uri, degrees: Int = 90) {
        viewModelScope.launch {
            _operationResult.value = Result.Loading
            try {
                val result = withContext(Dispatchers.IO) {
                    val outputFile = createOutputFile(context, "rotated_${System.currentTimeMillis()}.pdf")
                    val inputStream = context.contentResolver.openInputStream(uri)!!
                    val srcDoc = PdfDocument(PdfReader(inputStream), PdfWriter(outputFile))

                    for (i in 1..srcDoc.numberOfPages) {
                        val page: PdfPage = srcDoc.getPage(i)
                        val currentRotation = page.rotation
                        page.rotation = (currentRotation + degrees) % 360
                    }
                    srcDoc.close()
                    outputFile
                }
                val outputUri = getUriForFile(context, result)
                _operationResult.value = Result.Success("تم التدوير بنجاح!", outputUri)
            } catch (e: Exception) {
                _operationResult.value = Result.Error("خطأ في التدوير: ${e.message}")
            }
        }
    }

    // ====== حماية PDF بكلمة مرور ======
    fun protectPdf(context: Context, uri: Uri, password: String) {
        viewModelScope.launch {
            _operationResult.value = Result.Loading
            try {
                val result = withContext(Dispatchers.IO) {
                    val outputFile = createOutputFile(context, "protected_${System.currentTimeMillis()}.pdf")
                    val inputStream = context.contentResolver.openInputStream(uri)!!

                    val writerProps = com.itextpdf.kernel.pdf.WriterProperties()
                        .setStandardEncryption(
                            password.toByteArray(),      // user password
                            password.toByteArray(),      // owner password
                            com.itextpdf.kernel.pdf.EncryptionConstants.ALLOW_PRINTING,
                            com.itextpdf.kernel.pdf.EncryptionConstants.ENCRYPTION_AES_256
                        )

                    val srcDoc = PdfDocument(PdfReader(inputStream), PdfWriter(outputFile, writerProps))
                    srcDoc.close()
                    outputFile
                }
                val outputUri = getUriForFile(context, result)
                _operationResult.value = Result.Success("تمت الحماية بنجاح!", outputUri)
            } catch (e: Exception) {
                _operationResult.value = Result.Error("خطأ في الحماية: ${e.message}")
            }
        }
    }

    // ====== علامة مائية ======
    fun addWatermark(context: Context, uri: Uri, watermarkText: String) {
        viewModelScope.launch {
            _operationResult.value = Result.Loading
            try {
                val result = withContext(Dispatchers.IO) {
                    val outputFile = createOutputFile(context, "watermarked_${System.currentTimeMillis()}.pdf")
                    val inputStream = context.contentResolver.openInputStream(uri)!!

                    val srcDoc = PdfDocument(PdfReader(inputStream), PdfWriter(outputFile))
                    val document = Document(srcDoc)

                    for (i in 1..srcDoc.numberOfPages) {
                        val page = srcDoc.getPage(i)
                        val pageSize = page.pageSize

                        // إضافة العلامة المائية في المنتصف
                        val watermark = Paragraph(watermarkText)
                            .setFontSize(60f)
                            .setOpacity(0.15f)
                            .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)

                        document.showTextAligned(
                            watermark,
                            pageSize.width / 2,
                            pageSize.height / 2,
                            i,
                            TextAlignment.CENTER,
                            com.itextpdf.layout.properties.VerticalAlignment.MIDDLE,
                            45f // زاوية ميل 45 درجة
                        )
                    }
                    document.close()
                    outputFile
                }
                val outputUri = getUriForFile(context, result)
                _operationResult.value = Result.Success("تمت إضافة العلامة المائية!", outputUri)
            } catch (e: Exception) {
                _operationResult.value = Result.Error("خطأ: ${e.message}")
            }
        }
    }

    // ====== Helper Functions ======

    private fun createOutputFile(context: Context, fileName: String): File {
        val outputDir = File(context.getExternalFilesDir(null), "PDFMaster")
        if (!outputDir.exists()) outputDir.mkdirs()
        return File(outputDir, fileName)
    }

    private fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
