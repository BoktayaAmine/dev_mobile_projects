package ma.ensaj.comptes_apollo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.exception.ApolloException
import kotlinx.coroutines.launch
import ma.ensaj.AllComptesQuery
import ma.ensaj.DeleteCompteMutation
import ma.ensaj.SaveCompteMutation
import ma.ensaj.comptes_apollo.adapter.ComptesAdapter
import ma.ensaj.type.CompteRequest
import ma.ensaj.type.TypeCompte
import com.apollographql.apollo.api.Optional
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ma.ensaj.comptes_appllo.R

class ComptesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ComptesAdapter
    private lateinit var apolloClient: ApolloClient

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comptes)

        // Initialize Apollo Client
        apolloClient = ApolloClient.Builder()
            .serverUrl("http://10.0.2.2:8090/graphql")
            .build()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Floating Action Button for opening the dialog
        val fab: FloatingActionButton = findViewById(R.id.fab_open_form)
        fab.setOnClickListener {
            showFormDialog() // Call the method to display the dialog
        }

        fetchComptes()
        setupSwipeToDelete()

    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val compte = adapter.getItem(position) // Get swiped item

                // Show confirmation dialog
                AlertDialog.Builder(this@ComptesActivity)
                    .setTitle("Supprimer le compte")
                    .setMessage("Êtes-vous sûr de vouloir supprimer ce compte?")
                    .setPositiveButton("supprimer") { _, _ ->
                        if (compte != null) {
                            compte.id?.let { deleteCompte(it) }
                        }
                    }
                    .setNegativeButton("Annuler") { _, _ ->
                        adapter.notifyItemChanged(position) // Reset swipe
                    }
                    .setCancelable(false)
                    .show()
            }
        }

        // Attach ItemTouchHelper to RecyclerView
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun showFormDialog() {
        val dialog = Dialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_form, null)
        dialog.setContentView(dialogView)

        val soldeInput: EditText = dialogView.findViewById(R.id.input_solde)
        val dateCreationInput: EditText = dialogView.findViewById(R.id.input_date_creation)
        val typeSpinner: Spinner = dialogView.findViewById(R.id.input_type)
        val createButton: Button = dialogView.findViewById(R.id.btn_create_compte_dialog)

        // Clear input fields when dialog opens (optional)
        soldeInput.text.clear()
        dateCreationInput.text.clear()
        typeSpinner.setSelection(0)

        createButton.setOnClickListener {
            val solde = soldeInput.text.toString().toFloatOrNull()
            val dateCreation = dateCreationInput.text.toString()
            val type = typeSpinner.selectedItem.toString()

            if (solde != null && dateCreation.isNotBlank() && type.isNotBlank()) {
                createCompte(solde, dateCreation, type)
                dialog.dismiss() // Close the dialog after successful creation
            } else {
                Toast.makeText(this, "Veuillez remplir correctement tous les champs", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }




    private fun fetchComptes() {
        val query = AllComptesQuery()

        lifecycleScope.launch {
            try {
                val response = apolloClient.query(query).execute()
                val fetchedComptes = response.data?.allComptes?.toMutableList() ?: mutableListOf()

                if (::adapter.isInitialized) {
                    adapter.updateData(fetchedComptes)
                } else {
                    adapter = ComptesAdapter(fetchedComptes) { id ->
                        deleteCompte(id)
                    }
                    recyclerView.adapter = adapter
                }
            } catch (e: ApolloException) {
                e.printStackTrace()
            }
        }
    }



    private fun createCompte(solde: Float, dateCreation: String, type: String) {
        lifecycleScope.launch {
            try {
                val compteRequest = CompteRequest(
                    solde = Optional.Present(solde.toDouble()),
                    dateCreation = Optional.Present(dateCreation),
                    type = Optional.Present(TypeCompte.valueOf(type.uppercase()))
                )

                val response = apolloClient.mutation(SaveCompteMutation(compteRequest)).execute()

                if (response.data?.saveCompte != null) {
                    Toast.makeText(this@ComptesActivity, "Compte créé avec succès!", Toast.LENGTH_SHORT).show()
                    fetchComptes()
                } else {
                    Toast.makeText(this@ComptesActivity, "Impossible de créer un compte", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApolloException) {
                e.printStackTrace()
                Toast.makeText(this@ComptesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun deleteCompte(id: String) {
        lifecycleScope.launch {
            try {
                val response = apolloClient.mutation(DeleteCompteMutation(id)).execute()

                if (response.data?.deleteCompte?.contains("deleted successfully") == true) {
                    runOnUiThread {
                        adapter.removeItem(id)
                        Toast.makeText(this@ComptesActivity, "Compte supprimé avec succès!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ComptesActivity, "Impossible de supprimer le compte", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: ApolloException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@ComptesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}
