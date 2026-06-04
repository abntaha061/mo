package com.pdfmaster.ui.pdf

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.pdfmaster.R
import com.pdfmaster.databinding.FragmentPdfBinding
import com.pdfmaster.viewmodel.PdfViewModel
import kotlinx.coroutines.launch

class PdfFragment : Fragment() {

    private var _binding: FragmentPdfBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PdfViewModel by viewModels()
    private var currentAction: String = ""

    // File picker - ملف واحد
    private val singleFilePicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleSingleFile(uri)
            }
        }
    }

    // File picker - ملفات متعددة
    private val multipleFilePicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uris = mutableListOf<Uri>()
            result.data?.clipData?.let { clipData ->
                for (i in 0 until clipData.itemCount) {
                    uris.add(clipData.getItemAt(i).uri)
                }
            } ?: result.data?.data?.let { uris.add(it) }

            if (uris.isNotEmpty()) {
                handleMultipleFiles(uris)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPdfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPdfTools()
        observeViewModel()
    }

    private fun setupPdfTools() {
        val tools = listOf(
            ToolItem(
                id = "view",
                name = "عرض PDF",
                description = "افتح وتصفح الملف",
                iconRes = R.drawable.ic_pdf,
                color = "#E94560"
            ),
            ToolItem(
                id = "merge",
                name = "دمج ملفات",
                description = "ادمج عدة ملفات PDF",
                iconRes = R.drawable.ic_merge,
                color = "#7C4DFF"
            ),
            ToolItem(
                id = "split",
                name = "تقسيم PDF",
                description = "قسّم الملف لصفحات",
                iconRes = R.drawable.ic_split,
                color = "#00BCD4"
            ),
            ToolItem(
                id = "compress",
                name = "ضغط PDF",
                description = "قلل حجم الملف",
                iconRes = R.drawable.ic_compress,
                color = "#FF6D00"
            ),
            ToolItem(
                id = "to_image",
                name = "PDF إلى صورة",
                description = "حوّل الصفحات لصور",
                iconRes = R.drawable.ic_image,
                color = "#00C853"
            ),
            ToolItem(
                id = "rotate",
                name = "تدوير الصفحات",
                description = "دوّر صفحات الملف",
                iconRes = R.drawable.ic_rotate,
                color = "#FF4081"
            ),
            ToolItem(
                id = "protect",
                name = "حماية بكلمة سر",
                description = "أضف كلمة مرور",
                iconRes = R.drawable.ic_lock,
                color = "#FFD600"
            ),
            ToolItem(
                id = "watermark",
                name = "علامة مائية",
                description = "أضف علامة مائية",
                iconRes = R.drawable.ic_watermark,
                color = "#2979FF"
            )
        )

        val adapter = ToolsAdapter(tools) { tool ->
            onToolSelected(tool.id)
        }

        binding.rvPdfTools.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            this.adapter = adapter
        }
    }

    private fun onToolSelected(toolId: String) {
        currentAction = toolId
        when (toolId) {
            "view" -> pickSinglePdf()
            "merge" -> pickMultiplePdfs()
            "split" -> pickSinglePdf()
            "compress" -> pickSinglePdf()
            "to_image" -> pickSinglePdf()
            "rotate" -> pickSinglePdf()
            "protect" -> pickSinglePdf()
            "watermark" -> pickSinglePdf()
        }
    }

    private fun pickSinglePdf() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        singleFilePicker.launch(intent)
    }

    private fun pickMultiplePdfs() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        multipleFilePicker.launch(intent)
    }

    private fun handleSingleFile(uri: Uri) {
        when (currentAction) {
            "view" -> openPdfViewer(uri)
            "split" -> splitPdf(uri)
            "compress" -> compressPdf(uri)
            "to_image" -> pdfToImage(uri)
            "rotate" -> rotatePdf(uri)
            "protect" -> protectPdf(uri)
            "watermark" -> addWatermark(uri)
        }
    }

    private fun handleMultipleFiles(uris: List<Uri>) {
        when (currentAction) {
            "merge" -> mergePdfs(uris)
        }
    }

    // ====== الوظائف ======

    private fun openPdfViewer(uri: Uri) {
        val intent = Intent(requireContext(), PdfViewerActivity::class.java).apply {
            putExtra("pdf_uri", uri.toString())
        }
        startActivity(intent)
    }

    private fun mergePdfs(uris: List<Uri>) {
        if (uris.size < 2) {
            showToast("اختر ملفين على الأقل")
            return
        }
        lifecycleScope.launch {
            showToast("جاري الدمج…")
            viewModel.mergePdfs(requireContext(), uris)
        }
    }

    private fun splitPdf(uri: Uri) {
        lifecycleScope.launch {
            showToast("جاري التقسيم…")
            viewModel.splitPdf(requireContext(), uri)
        }
    }

    private fun compressPdf(uri: Uri) {
        lifecycleScope.launch {
            showToast("جاري الضغط…")
            viewModel.compressPdf(requireContext(), uri)
        }
    }

    private fun pdfToImage(uri: Uri) {
        lifecycleScope.launch {
            showToast("جاري التحويل…")
            viewModel.pdfToImages(requireContext(), uri)
        }
    }

    private fun rotatePdf(uri: Uri) {
        lifecycleScope.launch {
            showToast("جاري التدوير…")
            viewModel.rotatePdf(requireContext(), uri)
        }
    }

    private fun protectPdf(uri: Uri) {
        // إظهار dialog لإدخال كلمة المرور
        PasswordDialog(requireContext()) { password ->
            lifecycleScope.launch {
                viewModel.protectPdf(requireContext(), uri, password)
            }
        }.show()
    }

    private fun addWatermark(uri: Uri) {
        // إظهار dialog لإدخال نص العلامة المائية
        WatermarkDialog(requireContext()) { text ->
            lifecycleScope.launch {
                viewModel.addWatermark(requireContext(), uri, text)
            }
        }.show()
    }

    private fun observeViewModel() {
        viewModel.operationResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is PdfViewModel.Result.Success -> {
                    showToast("✅ ${result.message}")
                    // عرض خيار المشاركة أو الفتح
                    result.outputUri?.let { offerToOpen(it) }
                }
                is PdfViewModel.Result.Error -> {
                    showToast("❌ ${result.message}")
                }
                is PdfViewModel.Result.Loading -> {
                    showToast("⏳ جاري المعالجة…")
                }
            }
        }
    }

    private fun offerToOpen(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(Intent.createChooser(intent, "فتح الملف"))
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
