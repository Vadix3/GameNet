package com.vadim.gamenet.models

class Game(
    val id: Int = 0,
    val name: String = "",
    val imageUrl: String = "",
) {

    override fun toString(): String {
        return "Game(id=$id, name='$name', imageUrl='$imageUrl')"
    }
}