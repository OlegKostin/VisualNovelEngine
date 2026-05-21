package com.olegkos.vnengine.scene

enum class SpriteSheetScale {
  /** Весь кадр виден, возможны полосы по краям. */
  Fit,
  /** На весь экран, пропорции сохранены, края обрезаются. */
  Crop,
  /** На весь экран с растягиванием. */
  Fill,
  ;

  companion object {
    fun fromJson(value: String?): SpriteSheetScale =
      when (value?.trim()?.lowercase()) {
        "crop" -> Crop
        "fill" -> Fill
        else -> Fit
      }
  }
}
