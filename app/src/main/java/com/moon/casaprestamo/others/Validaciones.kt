package com.moon.casaprestamo.others

object Validaciones{
    fun isEmailValid(email: String): Boolean {
        return email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPhoneValid(phone: String): Boolean {
        return phone.length == 10 && phone.all { it.isDigit() }
    }

    fun isCurpValid(curp: String): Boolean {
        // Expresión regular básica para CURP
        val regex = Regex("^[A-Z]{4}[0-9]{6}[A-Z]{6}[0-9A-Z]{2}$")
        return curp.matches(regex)
    }

    fun isPasswordSecure(pass: String): Boolean {
        return pass.length >= 8 // Puedes agregar requisitos de mayúsculas/números aquí
    }
}