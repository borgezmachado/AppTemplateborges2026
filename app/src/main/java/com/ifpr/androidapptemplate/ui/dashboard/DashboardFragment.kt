package com.ifpr.androidapptemplate.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item
import com.ifpr.androidapptemplate.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    // Referências dos componentes de UI
    private lateinit var enderecoEditText: EditText
    private lateinit var descricaoEditText: EditText
    private lateinit var itemImageView: ImageView
    private lateinit var salvarButton: Button
    private lateinit var selectImageButton: Button

    // Firebase e Controle de Imagem
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var imageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Inicialização dos componentes via ID (conforme seu XML)
        itemImageView = root.findViewById(R.id.image_item)
        salvarButton = root.findViewById(R.id.salvarItemButton)
        selectImageButton = root.findViewById(R.id.button_select_image)
        enderecoEditText = root.findViewById(R.id.enderecoItemEditText)
        descricaoEditText = root.findViewById(R.id.descricaoItemEditText)

        auth = FirebaseAuth.getInstance()

        selectImageButton.setOnClickListener {
            openFileChooser()
        }

        salvarButton.setOnClickListener {
            valdarESalvar()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun valdarESalvar() {
        val endereco = enderecoEditText.text.toString().trim()
        val descricao = descricaoEditText.text.toString().trim()

        // Validação simples de campos vazios
        if (endereco.isEmpty() || descricao.isEmpty()) {
            Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri == null) {
            Toast.makeText(context, "Selecione uma imagem!", Toast.LENGTH_SHORT).show()
            return
        }

        processarImagemESalvar(endereco, descricao)
    }

    private fun processarImagemESalvar(endereco: String, descricao: String) {
        try {
            val inputStream = context?.contentResolver?.openInputStream(imageUri!!)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes != null) {
                val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)

                // Criando o objeto com os dados coletados
                val item = Item(
                    endereco = endereco,
                    descricao = descricao,
                    base64Image = base64Image
                )

                saveItemIntoDatabase(item)
            }
        } catch (e: Exception) {
            Log.e("DashboardFragment", "Erro ao processar imagem", e)
            Toast.makeText(context, "Erro ao processar imagem", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
            && data != null && data.data != null
        ) {
            imageUri = data.data
            Glide.with(this).load(imageUri).into(itemImageView)
        }
    }

    private fun saveItemIntoDatabase(item: Item) {
        // IMPORTANTE: O caminho "itens" deve ser igual ao definido nas suas Rules do Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("itens")

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            return
        }

        val itemId = databaseReference.push().key
        if (itemId != null) {
            // Salvando no caminho: itens/ID_DO_USUARIO/ID_DO_ITEM
            databaseReference.child(userId).child(itemId).setValue(item)
                .addOnSuccessListener {
                    Toast.makeText(context, "Compra salva com sucesso!", Toast.LENGTH_SHORT).show()
                    limparCampos()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseError", "Erro ao salvar", e)
                    Toast.makeText(context, "Erro de permissão: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun limparCampos() {
        enderecoEditText.text.clear()
        descricaoEditText.text.clear()
        itemImageView.setImageResource(android.R.drawable.gallery_thumb)
        imageUri = null
    }
}