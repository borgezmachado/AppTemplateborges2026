package com.ifpr.androidapptemplate.baseclasses

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Item(
    var id: String? = null,             // ID único da compra
    var descricao: String? = null,      // Título/Descrição do item
    var endereco: String? = null,       // Endereço em texto
    var base64Image: String? = null,    // Imagem em Base64
    var imageUrl: String? = null,       // URL da imagem (se usar Firebase Storage)

    // NOVOS CAMPOS PARA O CÁLCULO DE DISTÂNCIA
    // Iniciamos com 0.0 para evitar erros de valor nulo
    var latitude: Double = -25.110037,
    var longitude: Double = -50.155449,
)