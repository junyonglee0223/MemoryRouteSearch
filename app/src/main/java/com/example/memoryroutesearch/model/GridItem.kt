package com.example.memoryroutesearch.model

data class GridItem(
    val front: String,  // Number or symbol
    val back: String,   // Alphabet
    var isFlipped: Boolean = false // Current flipped state
)
