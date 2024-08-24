package com.tlc.domain.use_cases.firebase.place

import android.net.Uri
import com.tlc.domain.repository.firebase.PlaceRepository
import javax.inject.Inject

class UploadImageUseCase @Inject constructor(
    private val placeRepository: PlaceRepository
){
    suspend operator fun invoke(uri: Uri, imagePathString: String) = placeRepository.uploadImage(uri, imagePathString)


}