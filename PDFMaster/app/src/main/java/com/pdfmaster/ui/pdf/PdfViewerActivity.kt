package com.pdfmaster.ui.pdf

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnErrorListener
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.pdfmaster.databinding.ActivityPdfViewerBinding

class PdfViewerActivity : AppCompatActivity(),
    OnLoadCompleteListener,
    OnPageChangeListener,
    OnErrorListener {

    private lateinit var binding: ActivityPdfViewerBinding
    private var pdfUri: Uri? = null
    private var totalPages = 0
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // استرجاع URI من الـ Intent
        val uriString = intent.getStringExtra("pdf_uri")
        pdfUri = if (uriString != null) Uri.parse(uriString) else intent.data

        setupToolbar()
        loadPdf()
    }

    private fun setupToolbar() {
        val fileName = pdfUri?.lastPathSegment?.substringAfterLast("/") ?: "ملف PDF"
        binding.tvFilename.text = fileName

        binding.btnBack.setOnClickListener { finish() }

        binding.btnShare.setOnClickListener {
            pdfUri?.let { uri ->
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                startActivity(Intent.createChooser(shareIntent, "مشاركة PDF"))
            }
        }

        binding.btnPrevPage.setOnClickListener {
            if (currentPage > 0) {
                binding.pdfView.jumpTo(currentPage - 1, true)
            }
        }

        binding.btnNextPage.setOnClickListener {
            if (currentPage < totalPages - 1) {
                binding.pdfView.jumpTo(currentPage + 1, true)
            }
        }
    }

    private fun loadPdf() {
        val uri = pdfUri ?: run {
            finish()
            return
        }

        binding.layoutLoading.visibility = View.VISIBLE

        binding.pdfView.fromUri(uri)
            .defaultPage(0)
            .onLoad(this)
            .onPageChange(this)
            .onError(this)
            .scrollHandle(DefaultScrollHandle(this))
            .spacing(8) // مسافة بين الصفحات
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .enableAnnotationRendering(true)
            .nightMode(true) // وضع ليلي
            .load()
    }

    // ====== Callbacks ======

    override fun loadComplete(nbPages: Int) {
        totalPages = nbPages
        binding.layoutLoading.visibility = View.GONE
        updatePageInfo()
    }

    override fun onPageChanged(page: Int, pageCount: Int) {
        currentPage = page
        totalPages = pageCount
        updatePageInfo()
    }

    override fun onError(t: Throwable?) {
        binding.layoutLoading.visibility = View.GONE
        // إظهار رسالة خطأ
    }

    private fun updatePageInfo() {
        binding.tvPageInfo.text = "${currentPage + 1} / $totalPages"
        binding.btnPrevPage.isEnabled = currentPage > 0
        binding.btnNextPage.isEnabled = currentPage < totalPages - 1
    }
}
