package com.pdfmaster.ui.pdf

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import android.widget.LinearLayout
import android.graphics.Color
import android.view.Gravity
import android.widget.TextView

// ====== Password Dialog ======
class PasswordDialog(
    private val context: Context,
    private val onConfirm: (String) -> Unit
) {
    fun show() {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 20)
            layoutDirection = LinearLayout.LAYOUT_DIRECTION_RTL
        }

        val label = TextView(context).apply {
            text = "أدخل كلمة المرور"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.END
        }

        val input = EditText(context).apply {
            hint = "كلمة المرور"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            gravity = Gravity.END
        }

        layout.addView(label)
        layout.addView(input)

        AlertDialog.Builder(context)
            .setTitle("حماية PDF")
            .setView(layout)
            .setPositiveButton("تأكيد") { _, _ ->
                val password = input.text.toString().trim()
                if (password.isNotEmpty()) {
                    onConfirm(password)
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }
}

// ====== Watermark Dialog ======
class WatermarkDialog(
    private val context: Context,
    private val onConfirm: (String) -> Unit
) {
    fun show() {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 20)
            layoutDirection = LinearLayout.LAYOUT_DIRECTION_RTL
        }

        val label = TextView(context).apply {
            text = "نص العلامة المائية"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.END
        }

        val input = EditText(context).apply {
            hint = "مثال: سري للغاية"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            gravity = Gravity.END
            setText("سري")
        }

        layout.addView(label)
        layout.addView(input)

        AlertDialog.Builder(context)
            .setTitle("إضافة علامة مائية")
            .setView(layout)
            .setPositiveButton("إضافة") { _, _ ->
                val text = input.text.toString().trim()
                if (text.isNotEmpty()) {
                    onConfirm(text)
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }
}
