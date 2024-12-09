package com.example.medisight.data.model

data class ClassificationLabel(val label: String, val description: String)

private val labels = listOf(
    ClassificationLabel("Abrasions", "Abrasions are wounds that affect only the top layer of the skin."),
    ClassificationLabel("Bruises", "Bruises are caused by blunt force trauma leading to blood vessels breaking under the skin."),
    ClassificationLabel("Burns", "Burns are injuries to the skin caused by heat, chemicals, or radiation."),
    ClassificationLabel("Cut", "Cuts are open wounds that are caused by sharp objects."),
    ClassificationLabel("Normal", "No injury detected, skin appears healthy.")
)