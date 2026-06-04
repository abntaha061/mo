package com.pdfmaster.ui.word

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import android.view.Gravity
import androidx.fragment.app.Fragment

class WordFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#1A1A2E"))

            addView(TextView(context).apply {
                text = "📝"
                textSize = 64f
                gravity = Gravity.CENTER
            })

            addView(TextView(context).apply {
                text = "أدوات Word\nقريباً..."
                textSize = 22f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(0, 16, 0, 0)
            })
        }
        return layout
    }
}
