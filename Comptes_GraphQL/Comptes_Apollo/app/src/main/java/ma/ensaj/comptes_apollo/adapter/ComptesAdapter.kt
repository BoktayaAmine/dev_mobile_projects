package ma.ensaj.comptes_apollo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ma.ensaj.AllComptesQuery
import ma.ensaj.SaveCompteMutation
import ma.ensaj.comptes_appllo.R

class ComptesAdapter(
    private var comptes: MutableList<AllComptesQuery.AllCompte?>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<ComptesAdapter.CompteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_compte, parent, false)
        return CompteViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompteViewHolder, position: Int) {
        val compte = comptes[position]
        holder.bind(compte, onDeleteClick)
    }

    fun getItem(position: Int): AllComptesQuery.AllCompte? {
        return comptes.getOrNull(position)
    }

    override fun getItemCount(): Int = comptes.size

    fun removeItem(id: String) {
        val position = comptes.indexOfFirst { it?.id == id }
        if (position != -1) {
            comptes.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateData(newComptes: List<AllComptesQuery.AllCompte?>) {
        comptes.clear()
        comptes.addAll(newComptes)
        notifyDataSetChanged()
    }






    class CompteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val idTextView: TextView = itemView.findViewById(R.id.compte_id)
        private val soldeTextView: TextView = itemView.findViewById(R.id.compte_solde)
        private val dateCreationTextView: TextView = itemView.findViewById(R.id.compte_date_creation)
        private val typeTextView: TextView = itemView.findViewById(R.id.compte_type)

        fun bind(compte: AllComptesQuery.AllCompte?, onDeleteClick: (String) -> Unit) {
            idTextView.text = "ID: ${compte?.id}"
            soldeTextView.text = "Solde: ${compte?.solde}"
            dateCreationTextView.text = "Date: ${compte?.dateCreation}"
            typeTextView.text = "Type: ${compte?.type}"

        }
    }
}
