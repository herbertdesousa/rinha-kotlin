package com.example.person

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Serializable
data class PersonDTO(
    val nome: String,
    val apelido: String,
    val nascimento: String,
    var stack: List<String> = emptyList()
)  {
    init {
        validateName(nome)
        validateNickname(apelido)
        validateBirthdate(nascimento)
        validateStack(stack)
    }

    companion object {
        private const val MAX_NAME_LENGTH = 100
        private const val MAX_NICKNAME_LENGTH = 32
        private const val MAX_STACK_ELEMENT_LENGTH = 32

        private fun validateName(name: String) {
            if (name.isBlank() || name.length > MAX_NAME_LENGTH) {
                throw SerializationException("Name is required and must be up to $MAX_NAME_LENGTH characters.")
            }
        }

        private fun validateNickname(nickname: String) {
            if (nickname.isBlank() || nickname.length > MAX_NICKNAME_LENGTH) {
                throw SerializationException("Nickname is required and must be up to $MAX_NICKNAME_LENGTH characters.")
            }
        }

        private fun validateBirthdate(birthdate: String) {
            try {
                LocalDate.parse(birthdate)
            } catch (e: DateTimeParseException) {
                throw SerializationException("Birthdate must be in the format YYYY-MM-DD.")
            }
        }

        private fun validateStack(stack: List<String>?) {
            stack?.forEach { item ->
                if (item.isBlank() || item.length > MAX_STACK_ELEMENT_LENGTH) {
                    throw SerializationException("Each stack element must be a non-empty string and up to $MAX_STACK_ELEMENT_LENGTH characters.")
                }
            }
        }
    }
}