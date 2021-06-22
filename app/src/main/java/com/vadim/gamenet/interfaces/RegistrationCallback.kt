package com.vadim.gamenet.interfaces

import com.vadim.gamenet.models.AppUser

interface RegistrationCallback {
    fun getNewUser(tempUser: AppUser)
}