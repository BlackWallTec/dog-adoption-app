package com.app.android.domain.entities.entitiesSolicitudes

data class TuHogar(
    // Paso 3 del Formulario | "Tu hogar"
    val hadPet: Boolean? = null, // Pregunta: ¿Has tenido mascotas antes?
    val otherPets: Boolean? = null, // Opción: ¿Tienes otros animales en casa?
    val whyAdopt: String = "", // Pregunta: ¿Por qué deseas adoptar?
    val whereLive: String = "", // Pregunta: ¿Donde vivirá y dormirá el perrito?
    val peopleInHouse: Int = 1, // Cantidad de personas en el hogar
    val allAgree: Boolean? = null, // Pregunta: ¿Todos están de acuerdo?
    val houseType: HouseType = HouseType.CASA, // Opción (Casa, Departamento, Otro): Casa o Departamento
    val hasGarden: String = "", // Pregunta: ¿Tienes jardin o patio?
    val petAccept: Boolean? = null, // Opción (Si, No): Si rentas, ¿tu casero acepta mascotas?
    val aboutPets: String = "", // Pregunta: Cuéntanos sobre tus mascotas
)

enum class HouseType {
    CASA,
    DEPARTAMENTO,
    OTRO,
}
