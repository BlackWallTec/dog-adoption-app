package com.app.android.domain.entities.entitiesSolicitudes

data class AdoptionForm(
    // Paso 2 del Formulario | "Datos generales"
    val name: String = "", // para el nombre completo
    val age: Int? = null, // Fuck, no se si la edad ya va a estar puesta cuando el cliente creé una cuenta
    val occupation: String = "",
    val address: String = "",
    val municipio: String = "",
    val phone: String = "",
    val email: String = "",
    // Paso 3 del Formulario | "Tu hogar"
    val hadPet: Boolean = false, // Pregunta: ¿Has tenido mascotas antes?
    val otherPets: Boolean = false, // Opción: ¿Tienes otros animales en casa?
    val whyAdopt: String = "", // Pregunta: ¿Por qué deseas adoptar?
    val whereLive: String = "", // Pregunta: ¿Donde vivirá y dormirá el perrito?
    val peopleInHouse: Int = 0, // Cantidad de personas en el hogar
    val allAgree: Boolean = false, // Pregunta: ¿Todos están de acuerdo?
    val houseType: String = "", // Opción (Casa, Departamento, Otro): Casa o Departamento
    val hasGarden: String = "", // Pregunta: ¿Tienes jardin o patio?
    val petAcept: Boolean = false, // Opción (Si, No): Si rentas, ¿tu casero acepta mascotas?
    val aboutPets: String = "", // Pregunta: Cuéntanos sobre tus mascotas
    // Paso 4 del Formulario | "Compromiso"
    val petNeeds: String = "", // Pregunta: Menciona los cuidados básicos de una mascota
    val petSterilization: String = "", // Pregunta: ¿Que opinas de la esterilización?
    val petFollowing: String = "", // Opción (Si, No): ¿Nos permites dar seguimiento (visitas o fotos)?
)
