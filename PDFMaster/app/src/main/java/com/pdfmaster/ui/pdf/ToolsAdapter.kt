package com.pdfmaster.ui.pdf

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pdfmaster.databinding.ItemToolCardBinding

// ====== Data Model ======
data class ToolItem(
    val id: String,
    val name: String,
    val description: String,
    val iconRes: Int,
    val color: String // Hex color
)

// ====== Adapter ======
class ToolsAdapter(
    private val tools: List<ToolItem>,
    private val onClick: (ToolItem) -> Unit
) : RecyclerView.Adapter<ToolsAdapter.ToolViewHolder>() {

    inner class ToolViewHolder(private val binding: ItemToolCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tool: ToolItem) {
            binding.tvToolName.text = tool.name
            binding.tvToolDesc.text = tool.description
            binding.ivToolIcon.setImageResource(tool.iconRes)

            // تلوين خلفية الأيقونة
            val color = Color.parseColor(tool.color)
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.argb(30, Color.red(color), Color.green(color), Color.blue(color)))
            }
            binding.iconContainer.background = bg
            binding.ivToolIcon.setColorFilter(color)

            binding.root.setOnClickListener { onClick(tool) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val binding = ItemToolCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ToolViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        holder.bind(tools[position])
    }

    override fun getItemCount() = tools.size
}
