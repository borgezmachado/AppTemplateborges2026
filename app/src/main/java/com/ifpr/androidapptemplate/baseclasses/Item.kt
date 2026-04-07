package com.ifpr.androidapptemplate.baseclasses

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Item(
    var endereco: String? = null,
    var descricao: String? = null, // Novo campo relacionado ao seu formulário
    var base64Image: String? = null,
    var imageUrl: String? = null,
    var id: String? = null // Recomendado para identificar cada compra de forma única
)