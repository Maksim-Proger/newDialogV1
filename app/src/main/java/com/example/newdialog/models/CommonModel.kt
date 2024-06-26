package com.example.newdialog.models

data class CommonModel(
    val id: String = "",
    var username: String = "",
    var bio: String = "",
    var fullname: String = "",
    var state: String = "",
    var phone: String = "",
    var photoUrl: String = "empty",
    var fcmtoken: String = "",

    // Расширяем данную модель. Добавляем модель сообщения.
    var text: String = "",
    var type: String = "",
    var from: String = "",
    var timeStamp: Any = "",

    // Расширяем модель. Добавляем свойства для работы с голосовыми сообщениями.
    var fileUrl: String = "empty",

    var lastMessage: String = ""

) {

    // Метод сравнения моделей, чтобы понять уникальная она или нет
    override fun equals(other: Any?): Boolean {
        return (other as CommonModel).id == id // если id одинаковые, возвращаем true

    }
}

