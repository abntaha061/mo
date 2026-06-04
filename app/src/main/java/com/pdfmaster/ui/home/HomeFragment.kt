package com.pdfmaster.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.pdfmaster.R
import com.pdfmaster.databinding.FragmentHomeBinding
import com.pdfmaster.ui.pdf.ToolItem
import com.pdfmaster.ui.pdf.ToolsAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupQuickTools()
    }

    private fun setupQuickTools() {
        // أدوات سريعة على الصفحة الرئيسية
        val quickTools = listOf(
            ToolItem(
                id = "view",
                name = "عرض PDF",
                description = "افتح وتصفح",
                iconRes = R.drawable.ic_pdf,
                color = "#E94560"
            ),
            ToolItem(
                id = "merge",
                name = "دمج",
                description = "ادمج ملفات",
                iconRes = R.drawable.ic_merge,
                color = "#7C4DFF"
            ),
            ToolItem(
                id = "split",
                name = "تقسيم",
                description = "قسّم الصفحات",
                iconRes = R.drawable.ic_split,
                color = "#00BCD4"
            ),
            ToolItem(
                id = "compress",
                name = "ضغط",
                description = "قلل الحجم",
                iconRes = R.drawable.ic_compress,
                color = "#FF6D00"
            )
        )

        val adapter = ToolsAdapter(quickTools) { tool ->
            navigateToTool(tool.id)
        }

        binding.rvQuickTools.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            this.adapter = adapter
        }

        // إخفاء أو إظهار الملفات الأخيرة
        binding.layoutEmpty.visibility = View.VISIBLE
    }

    private fun navigateToTool(toolId: String) {
        // الانتقال لصفحة PDF مع تحديد الأداة
        findNavController().navigate(R.id.nav_pdf)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
