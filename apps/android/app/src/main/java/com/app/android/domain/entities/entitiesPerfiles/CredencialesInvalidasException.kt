/**
 Esta clase nos ayudara a comunicar el error 401 para que el
 ViewModel muestre "correo o contraseña incorrecta"
 **/

package com.app.android.domain.entities.entitiesPerfiles

class CredencialesInvalidasException : Exception("Credenciales inválidas")