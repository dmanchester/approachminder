package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.Utils
import com.dmanchester.approachminder.typeswithoutbehavior.AircraftCategory

object AircraftCategories {

  /**
   * In a collection of aircraft categories, determine the most-common one that is "non-blank" (i.e., not `NoInfoAtAll`
   * or `NoADSBEmitterCategoryInfo`). The collection cannot be empty.
   *
   * If multiple categories are equally common, pick the one that is alphabetically first by class name. (This is just
   * to ensure deterministic behavior regardless of the categories' ordering.)
   *
   * @param categories The categories.
   * @throws java.lang.UnsupportedOperationException If collection is empty.
   * @return the most-common `AircraftCategory` as a `Some`; or, `None` if all categories are
   *         `NoInfoAtAll`/`NoADSBEmitterCategoryInfo`.
   */
  @throws(classOf[UnsupportedOperationException])
  def mostCommonNonBlankCategoryInNonEmptyCollection(categories: Iterable[AircraftCategory]): Option[AircraftCategory] = {

    if (categories.isEmpty) {
      throw new UnsupportedOperationException("'categories' must not be empty!")
    }

    val nonBlankCategories = categories.filter(!AircraftCategory.blank.contains(_))
    if (nonBlankCategories.isEmpty) {
      None
    } else {
      val category = Utils.mostCommonValueInNonEmptyCollection(nonBlankCategories) { (a, b) => a.getClass.getSimpleName < b.getClass.getSimpleName }
      Some(category)
    }
  }
}
